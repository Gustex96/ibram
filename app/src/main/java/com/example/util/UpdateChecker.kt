package com.example.util

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AppVersionInfo(
    val versionCode: Int,
    val versionName: String,
    val releaseNotes: String,
    val apkUrl: String,
    val minRequiredVersion: Int = 0
)

object UpdateChecker {
    private const val TAG = "UpdateChecker"

    // URL padrão para verificação no GitHub Raw conforme solicitado pelo usuário
    // Caso o repositório específico ainda não esteja configurado, utiliza o endpoint raw do repositório
    private const val DEFAULT_VERSION_URL =
        "https://raw.githubusercontent.com/gustavoarc/equidf-inspections/main/version.json"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Faz a requisição GET no version.json do GitHub e compara com o versionCode instalado.
     * Retorna [AppVersionInfo] se houver versão mais recente disponível no GitHub, ou null se já estiver atualizado.
     */
    suspend fun checkForUpdate(
        context: Context,
        customUrl: String? = null
    ): AppVersionInfo? = withContext(Dispatchers.IO) {
        val targetUrl = customUrl ?: DEFAULT_VERSION_URL

        try {
            val request = Request.Builder()
                .url(targetUrl)
                .header("Cache-Control", "no-cache")
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Falha ao consultar version.json: HTTP ${response.code}")
                return@withContext null
            }

            val responseBody = response.body?.string() ?: return@withContext null
            val json = JSONObject(responseBody)

            val remoteVersionCode = json.optInt("versionCode", 0)
            val remoteVersionName = json.optString("versionName", "Nova Versão")
            val releaseNotes = json.optString(
                "releaseNotes",
                "Melhorias operacionais, correções de interface e novos recursos de fiscalização."
            )
            val apkUrl = json.optString("apkUrl", "")
            val minRequiredVersion = json.optInt("minRequiredVersion", 0)

            // Obtém o versionCode atual do app instalado
            val currentVersionCode = BuildConfig.VERSION_CODE

            Log.d(TAG, "Versão atual: $currentVersionCode, Versão remota GitHub: $remoteVersionCode")

            if (remoteVersionCode > currentVersionCode) {
                return@withContext AppVersionInfo(
                    versionCode = remoteVersionCode,
                    versionName = remoteVersionName,
                    releaseNotes = releaseNotes,
                    apkUrl = apkUrl,
                    minRequiredVersion = minRequiredVersion
                )
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar atualizações do app", e)
            return@withContext null
        }
    }
}
