package com.example.util

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AppVersionInfo(
    val versionCode: Int,
    val versionName: String,
    val releaseNotes: String,
    val apkUrl: String,
    val minRequiredVersion: Int = 0,
    val publishedDate: String? = null
)

sealed interface UpdateCheckResult {
    data class UpdateAvailable(val info: AppVersionInfo) : UpdateCheckResult
    data class AlreadyUpToDate(
        val currentVersionCode: Int,
        val currentVersionName: String,
        val remoteVersionCode: Int,
        val remoteVersionName: String,
        val checkTime: Long = System.currentTimeMillis()
    ) : UpdateCheckResult
    data class Error(
        val message: String,
        val httpCode: Int? = null,
        val attemptedUrl: String,
        val checkTime: Long = System.currentTimeMillis()
    ) : UpdateCheckResult
}

object UpdateChecker {
    private const val TAG = "UpdateChecker"
    private const val PREFS_NAME = "app_update_prefs"
    private const val KEY_CUSTOM_URL = "custom_version_url"
    private const val KEY_LAST_CHECK_TIME = "last_check_timestamp"

    // URLs padrão para consulta do version.json no repositório GitHub do usuário (Gustex96/ibram)
    const val RAW_VERSION_MAIN_URL =
        "https://raw.githubusercontent.com/Gustex96/ibram/main/version.json"
    const val DEFAULT_VERSION_URL = RAW_VERSION_MAIN_URL
    const val RAW_VERSION_MASTER_URL =
        "https://raw.githubusercontent.com/Gustex96/ibram/master/version.json"
    const val BLOB_VERSION_URL =
        "https://github.com/Gustex96/ibram/blob/main/version.json"
    const val GITHUB_RAW_MAIN_URL =
        "https://github.com/Gustex96/ibram/raw/main/version.json"
    const val GITHUB_RAW_MASTER_URL =
        "https://github.com/Gustex96/ibram/raw/master/version.json"
    const val GITHUB_CONTENTS_API_URL =
        "https://api.github.com/repos/Gustex96/ibram/contents/version.json"
    const val GITHUB_RELEASES_API_URL =
        "https://api.github.com/repos/Gustex96/ibram/releases/latest"
    const val DEFAULT_APK_DOWNLOAD_URL =
        "https://github.com/Gustex96/ibram/releases/download/ibram/app-debug.apk"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    fun getVersionUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CUSTOM_URL, null) ?: DEFAULT_VERSION_URL
    }

    fun setVersionUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_URL, url.trim()).apply()
    }

    fun resetVersionUrl(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_CUSTOM_URL).apply()
    }

    fun getLastCheckTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_CHECK_TIME, 0L)
    }

    fun getFormattedLastCheckTime(context: Context): String {
        val time = getLastCheckTime(context)
        if (time == 0L) return "Nunca verificado"
        val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
        return sdf.format(Date(time))
    }

    private fun setLastCheckTime(context: Context, time: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_CHECK_TIME, time).apply()
    }

    /**
     * Faz a requisição GET no version.json da raiz do repositório GitHub e compara com o versionCode instalado.
     * Suporta URLs raw, blob e APIs do GitHub de forma inteligente e resiliente.
     */
    suspend fun checkUpdateWithResult(
        context: Context,
        customUrl: String? = null
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        val targetUrl = customUrl?.trim()?.ifBlank { null } ?: getVersionUrl(context)

        // Prepara lista ordenada de URLs alternativas para garantir a leitura do version.json
        val candidateUrls = LinkedHashSet<String>()

        // 1. Trata URL customizada ou URL de blob da web
        if (targetUrl.contains("github.com") && targetUrl.contains("/blob/")) {
            val rawUrl = targetUrl
                .replace("https://github.com/", "https://raw.githubusercontent.com/")
                .replace("http://github.com/", "https://raw.githubusercontent.com/")
                .replace("/blob/", "/")
            candidateUrls.add(rawUrl)
        }
        candidateUrls.add(targetUrl)

        // 2. URLs de contingência do repositório Gustex96/ibram na raiz
        candidateUrls.add(RAW_VERSION_MAIN_URL)
        candidateUrls.add(RAW_VERSION_MASTER_URL)
        candidateUrls.add(GITHUB_RAW_MAIN_URL)
        candidateUrls.add(GITHUB_RAW_MASTER_URL)
        candidateUrls.add(GITHUB_CONTENTS_API_URL)
        candidateUrls.add(GITHUB_RELEASES_API_URL)

        var lastCode = 0
        var lastEffectiveUrl = targetUrl

        for (urlToTry in candidateUrls) {
            try {
                Log.d(TAG, "Tentando GET para version.json na raiz do repositório: $urlToTry")
                val requestBuilder = Request.Builder()
                    .url(urlToTry)
                    .header("User-Agent", "EquiDF-Android-App/${BuildConfig.VERSION_NAME}")
                    .header("Cache-Control", "no-cache, no-store")
                    .header("Pragma", "no-cache")

                if (urlToTry.contains("api.github.com")) {
                    requestBuilder.header("Accept", "application/vnd.github.v3+json, application/json")
                } else {
                    requestBuilder.header("Accept", "application/json, text/plain, */*")
                }

                val response = httpClient.newCall(requestBuilder.build()).execute()
                lastCode = response.code
                lastEffectiveUrl = urlToTry

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (!responseBody.isNullOrBlank()) {
                        val parsedInfo = parseVersionResponse(responseBody, urlToTry)
                        if (parsedInfo != null) {
                            val currentVersionCode = BuildConfig.VERSION_CODE
                            val currentVersionName = BuildConfig.VERSION_NAME

                            setLastCheckTime(context, System.currentTimeMillis())
                            Log.d(TAG, "Versão local: $currentVersionCode ($currentVersionName) | Remota GitHub: ${parsedInfo.versionCode} (${parsedInfo.versionName})")

                            return@withContext if (parsedInfo.versionCode > currentVersionCode) {
                                UpdateCheckResult.UpdateAvailable(parsedInfo)
                            } else {
                                UpdateCheckResult.AlreadyUpToDate(
                                    currentVersionCode = currentVersionCode,
                                    currentVersionName = currentVersionName,
                                    remoteVersionCode = parsedInfo.versionCode,
                                    remoteVersionName = parsedInfo.versionName
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Falha na tentativa da URL $urlToTry: ${e.message}")
            }
        }

        val errorMsg = when (lastCode) {
            404 -> "Arquivo 'version.json' não localizado no GitHub (HTTP 404). O arquivo version.json foi mantido exclusivamente na raiz do projeto. Certifique-se de que o repositório 'Gustex96/ibram' é público e que o arquivo version.json foi enviado na branch main."
            403 -> "Limite de requisições temporariamente atingido no GitHub (HTTP 403). Aguarde alguns instantes."
            0 -> "Não foi possível conectar ao GitHub. Verifique se o dispositivo possui conexão ativa com a internet."
            else -> "O servidor GitHub retornou o código HTTP $lastCode ao consultar o version.json."
        }
        Log.w(TAG, "Falha ao consultar version.json: HTTP $lastCode - URL: $lastEffectiveUrl")
        return@withContext UpdateCheckResult.Error(
            message = errorMsg,
            httpCode = if (lastCode > 0) lastCode else null,
            attemptedUrl = targetUrl
        )
    }

    /**
     * Extrai os dados de versão a partir do conteúdo bruto do version.json ou da API do GitHub.
     */
    private fun parseVersionResponse(body: String, requestUrl: String): AppVersionInfo? {
        val trimmed = body.trim()

        // 1. JSON direto do version.json
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                val json = JSONObject(trimmed)

                // Caso da API do GitHub Contents (/contents/version.json)
                if (json.has("content") && json.optString("encoding") == "base64") {
                    val base64Content = json.optString("content").replace("\n", "").replace("\r", "")
                    val decodedBytes = Base64.decode(base64Content, Base64.DEFAULT)
                    val decodedString = String(decodedBytes, StandardCharsets.UTF_8).trim()
                    return parseVersionResponse(decodedString, requestUrl)
                }

                // Caso da API de Releases (/releases/latest)
                if (json.has("tag_name") && !json.has("versionCode")) {
                    val tagName = json.optString("tag_name", "").replace("v", "").replace("V", "")
                    val versionName = json.optString("name", "").ifBlank { "v$tagName" }
                    val releaseNotes = json.optString("body", "Nova versão disponível no GitHub.")
                    val assets = json.optJSONArray("assets")
                    var apkUrl = DEFAULT_APK_DOWNLOAD_URL
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.optJSONObject(i)
                            val name = asset?.optString("name", "") ?: ""
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                apkUrl = asset.optString("browser_download_url", DEFAULT_APK_DOWNLOAD_URL)
                                break
                            }
                        }
                    }
                    val codeNumber = tagName.filter { it.isDigit() }.toIntOrNull() ?: 2
                    return AppVersionInfo(
                        versionCode = codeNumber,
                        versionName = versionName,
                        releaseNotes = releaseNotes,
                        apkUrl = apkUrl
                    )
                }

                // Caso direto do version.json
                if (json.has("versionCode") || json.has("versionName")) {
                    val remoteVersionCode = json.optInt("versionCode", 0)
                    val remoteVersionName = json.optString("versionName", "Nova Versão")
                    val releaseNotes = json.optString(
                        "releaseNotes",
                        "Melhorias operacionais, correções de interface e novos recursos de fiscalização."
                    )
                    val apkUrl = json.optString("apkUrl", "").ifBlank { DEFAULT_APK_DOWNLOAD_URL }
                    val minRequiredVersion = json.optInt("minRequiredVersion", 0)

                    return AppVersionInfo(
                        versionCode = remoteVersionCode,
                        versionName = remoteVersionName,
                        releaseNotes = releaseNotes,
                        apkUrl = apkUrl,
                        minRequiredVersion = minRequiredVersion
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao analisar JSON de versão: ${e.message}")
            }
        }

        // 2. Extração via Regex para caso de HTML do GitHub Blob
        val rawJsonRegex = Regex("""\{[\s\S]*?"versionCode"[\s\S]*?"apkUrl"[\s\S]*?\}""")
        val match = rawJsonRegex.find(body)
        if (match != null) {
            try {
                val json = JSONObject(match.value)
                return AppVersionInfo(
                    versionCode = json.optInt("versionCode", 0),
                    versionName = json.optString("versionName", "Nova Versão"),
                    releaseNotes = json.optString("releaseNotes", "Atualização disponível no GitHub."),
                    apkUrl = json.optString("apkUrl", DEFAULT_APK_DOWNLOAD_URL),
                    minRequiredVersion = json.optInt("minRequiredVersion", 0)
                )
            } catch (_: Exception) {}
        }

        return null
    }

    /**
     * Compatibilidade direta: retorna AppVersionInfo se houver atualização ou null caso contrário.
     */
    suspend fun checkForUpdate(
        context: Context,
        customUrl: String? = null
    ): AppVersionInfo? {
        return when (val result = checkUpdateWithResult(context, customUrl)) {
            is UpdateCheckResult.UpdateAvailable -> result.info
            else -> null
        }
    }

    /**
     * Retorna modelo de versão para teste/simulação em tela baseado no version.json da raiz.
     */
    fun getSampleVersionInfo(context: Context): AppVersionInfo {
        return AppVersionInfo(
            versionCode = 2,
            versionName = "1.1",
            releaseNotes = "• Padronização oficial para Relatório de Apoio em Fiscalização\n• Aviso independente ampliado em destaque com confirmação via botão OK\n• Novo sistema de inserção de tags fotográficas (máx. 20 caracteres) diretamente na captura da foto\n• Suporte completo a cópia e colagem em todos os campos de preenchimento\n• Sistema de atualização e alertas otimizado com version.json exclusivo na raiz",
            apkUrl = DEFAULT_APK_DOWNLOAD_URL,
            minRequiredVersion = 1,
            publishedDate = "Versão Recente"
        )
    }
}
