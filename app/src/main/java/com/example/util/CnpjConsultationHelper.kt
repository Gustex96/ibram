package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object CnpjConsultationHelper {
    private const val TAG = "CnpjConsultation"

    const val RECEITA_FEDERAL_URL =
        "https://solucoes.receita.fazenda.gov.br/servicos/cnpjreva/cnpjreva_solicitacao.asp"

    /**
     * Abre a página oficial da Receita Federal para consulta de Comprovante de Inscrição e de Situação Cadastral.
     * Se houver CNPJ informado, copia-o automaticamente para a Área de Transferência para facilitar a colagem.
     */
    fun abrirConsultaReceitaFederal(context: Context, documento: String) {
        val clean = CpfValidator.clean(documento)
        val formatted = if (clean.length == 14) CpfValidator.format(clean) else clean

        if (clean.isNotBlank()) {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = ClipData.newPlainText("CNPJ", formatted)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(
                    context,
                    "CNPJ $formatted copiado! Abrindo consulta da Receita Federal...",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.w(TAG, "Falha ao copiar para o clipboard: ${e.message}")
            }
        } else {
            Toast.makeText(
                context,
                "Abrindo página oficial de consulta da Receita Federal...",
                Toast.LENGTH_SHORT
            ).show()
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(RECEITA_FEDERAL_URL)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir navegador: ${e.message}", e)
            Toast.makeText(
                context,
                "Não foi possível abrir o navegador. Acesse: $RECEITA_FEDERAL_URL",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    data class CnpjInfo(
        val cnpj: String,
        val razaoSocial: String,
        val nomeFantasia: String,
        val situacaoCadastral: String,
        val logradouro: String,
        val municipio: String,
        val uf: String
    ) {
        val displayNome: String
            get() = nomeFantasia.ifBlank { razaoSocial }
    }

    /**
     * Consulta pública gratuita e sem captcha via BrasilAPI para obtenção de Razão Social e Nome Fantasia,
     * permitindo preenchimento rápido em campo além da checagem na Receita Federal.
     */
    suspend fun buscarDadosCnpjOnline(cnpj: String): CnpjInfo? = withContext(Dispatchers.IO) {
        val clean = CpfValidator.clean(cnpj)
        if (clean.length != 14) return@withContext null

        try {
            val url = URL("https://brasilapi.com.br/api/cnpj/v1/$clean")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "FiscalizacaoDF/1.0")
            }

            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(jsonStr)
                return@withContext CnpjInfo(
                    cnpj = obj.optString("cnpj", clean),
                    razaoSocial = obj.optString("razao_social", ""),
                    nomeFantasia = obj.optString("nome_fantasia", ""),
                    situacaoCadastral = obj.optString("descricao_situacao_cadastral", "Ativa"),
                    logradouro = obj.optString("logradouro", ""),
                    municipio = obj.optString("municipio", ""),
                    uf = obj.optString("uf", "")
                )
            }
            null
        } catch (e: Exception) {
            Log.d(TAG, "Consulta online BrasilAPI não disponível: ${e.message}")
            null
        }
    }
}
