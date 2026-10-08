package com.example.util

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class AppVersionInfo(
    val versionCode: Int,
    val versionName: String,
    val releaseNotes: String,
    val apkUrl: String,
    val minRequiredVersion: Int = 0
)

sealed interface UpdateCheckResult {
    data class UpdateAvailable(val info: AppVersionInfo) : UpdateCheckResult
    data class AlreadyUpToDate(
        val currentVersionCode: Int,
        val currentVersionName: String,
        val remoteVersionCode: Int,
        val remoteVersionName: String
    ) : UpdateCheckResult
    data class Error(
        val message: String,
        val httpCode: Int? = null,
        val attemptedUrl: String
    ) : UpdateCheckResult
}

object UpdateChecker {
    private const val TAG = "UpdateChecker"
    private const val PREFS_NAME = "app_update_prefs"
    private const val KEY_CUSTOM_URL = "custom_version_url"

    // URL padrão configurada para o perfil do GitHub do usuário (Gustex96/ibram)
    const val DEFAULT_VERSION_URL =
        "https://raw.githubusercontent.com/Gustex96/ibram/main/version.json"
    const val FALLBACK_VERSION_URL =
        "https://raw.githubusercontent.com/Gustex96/ibram/master/version.json"
    const val DEFAULT_APK_DOWNLOAD_URL =
        "https://github.com/Gustex96/ibram/releases/download/ibram/app-debug.apk"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
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

    /**
     * Faz a requisição GET no version.json do GitHub e compara com o versionCode instalado.
     * Retorna [UpdateCheckResult] detalhado.
     */
    suspend fun checkUpdateWithResult(
        context: Context,
        customUrl: String? = null
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        val targetUrl = customUrl?.trim()?.ifBlank { null } ?: getVersionUrl(context)

        try {
            val request = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", "EquiDF-Android-App/1.0")
                .header("Cache-Control", "no-cache")
                .header("Accept", "application/json")
                .build()

            var response = httpClient.newCall(request).execute()
            var effectiveUrl = targetUrl

            // Se falhou no main e estávamos usando a URL padrão, tenta o branch master automaticamente
            if (response.code == 404 && targetUrl == DEFAULT_VERSION_URL) {
                Log.d(TAG, "Tentando branch alternativo (master): $FALLBACK_VERSION_URL")
                val fallbackRequest = Request.Builder()
                    .url(FALLBACK_VERSION_URL)
                    .header("User-Agent", "EquiDF-Android-App/1.0")
                    .header("Cache-Control", "no-cache")
                    .header("Accept", "application/json")
                    .build()
                val fallbackResponse = httpClient.newCall(fallbackRequest).execute()
                if (fallbackResponse.isSuccessful) {
                    response = fallbackResponse
                    effectiveUrl = FALLBACK_VERSION_URL
                }
            }

            val code = response.code
            if (!response.isSuccessful) {
                val errorMsg = when (code) {
                    404 -> "Arquivo 'version.json' não encontrado no GitHub (HTTP 404). Certifique-se de que o repositório é público e que o arquivo version.json foi commitado na raiz."
                    403 -> "Acesso negado ao repositório do GitHub (HTTP 403)."
                    else -> "Falha na resposta do servidor GitHub (HTTP $code)."
                }
                Log.w(TAG, "Falha ao consultar version.json: HTTP $code - URL: $effectiveUrl")
                return@withContext UpdateCheckResult.Error(
                    message = errorMsg,
                    httpCode = code,
                    attemptedUrl = effectiveUrl
                )
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrBlank()) {
                return@withContext UpdateCheckResult.Error(
                    message = "O arquivo version.json retornado pelo GitHub está vazio.",
                    httpCode = code,
                    attemptedUrl = effectiveUrl
                )
            }

            val json = JSONObject(responseBody)
            val remoteVersionCode = json.optInt("versionCode", 0)
            val remoteVersionName = json.optString("versionName", "Nova Versão")
            val releaseNotes = json.optString(
                "releaseNotes",
                "Melhorias operacionais, correções de interface e novos recursos de fiscalização."
            )
            val apkUrl = json.optString("apkUrl", "").ifBlank { DEFAULT_APK_DOWNLOAD_URL }
            val minRequiredVersion = json.optInt("minRequiredVersion", 0)

            val currentVersionCode = BuildConfig.VERSION_CODE
            val currentVersionName = BuildConfig.VERSION_NAME

            Log.d(TAG, "Versão local: $currentVersionCode ($currentVersionName) | Remota GitHub: $remoteVersionCode ($remoteVersionName)")

            if (remoteVersionCode > currentVersionCode) {
                UpdateCheckResult.UpdateAvailable(
                    AppVersionInfo(
                        versionCode = remoteVersionCode,
                        versionName = remoteVersionName,
                        releaseNotes = releaseNotes,
                        apkUrl = apkUrl,
                        minRequiredVersion = minRequiredVersion
                    )
                )
            } else {
                UpdateCheckResult.AlreadyUpToDate(
                    currentVersionCode = currentVersionCode,
                    currentVersionName = currentVersionName,
                    remoteVersionCode = remoteVersionCode,
                    remoteVersionName = remoteVersionName
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar atualizações do app via $targetUrl", e)
            val msg = e.localizedMessage ?: "Erro de conexão de rede ao consultar o GitHub"
            UpdateCheckResult.Error(
                message = msg,
                httpCode = null,
                attemptedUrl = targetUrl
            )
        }
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
     * Lê o arquivo version.json embutido nos assets do aplicativo caso exista.
     */
    fun getLocalAssetVersionInfo(context: Context): AppVersionInfo? {
        return try {
            val inputStream = context.assets.open("version.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = java.lang.StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()
            val json = JSONObject(sb.toString())
            AppVersionInfo(
                versionCode = json.optInt("versionCode", 2),
                versionName = json.optString("versionName", "1.0.1"),
                releaseNotes = json.optString(
                    "releaseNotes",
                    "• Atualização institucional para a DIFIS-IV / IBRAM\n• Padronização da Avaliação Preliminar de Bem-Estar Animal (PPBEA/CRMV)\n• Unificação dos dados dos tutores e remoção de campos redundantes\n• Melhorias na geolocalização e relatórios em PDF"
                ),
                apkUrl = json.optString(
                    "apkUrl",
                    DEFAULT_APK_DOWNLOAD_URL
                ),
                minRequiredVersion = json.optInt("minRequiredVersion", 1)
            )
        } catch (e: Exception) {
            Log.w(TAG, "Não foi possível ler version.json dos assets: ${e.message}")
            null
        }
    }

    /**
     * Retorna modelo de versão para teste/simulação em tela.
     */
    fun getSampleVersionInfo(context: Context): AppVersionInfo {
        return getLocalAssetVersionInfo(context) ?: AppVersionInfo(
            versionCode = 2,
            versionName = "1.0.1",
            releaseNotes = "• Atualização institucional para a DIFIS-IV / IBRAM\n• Padronização da Avaliação Preliminar de Bem-Estar Animal (PPBEA/CRMV)\n• Unificação dos dados dos tutores e remoção de campos redundantes\n• Melhorias na geolocalização e relatórios em PDF",
            apkUrl = DEFAULT_APK_DOWNLOAD_URL,
            minRequiredVersion = 1
        )
    }
}
