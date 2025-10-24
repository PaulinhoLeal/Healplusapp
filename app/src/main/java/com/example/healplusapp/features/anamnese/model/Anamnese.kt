package com.example.healplusapp.features.anamnese.model

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Anamnese(
    val id: Long? = null,
    val nomeCompleto: String,
    val dataConsulta: String?,
    val localizacao: String?,
    val dadosJson: String
) {
    companion object {
        fun createEmpty(): Anamnese {
            val dataAtual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            return Anamnese(
                nomeCompleto = "",
                dataConsulta = dataAtual,
                localizacao = "",
                dadosJson = "{}"
            )
        }
    }
    
    fun toJsonObject(): JSONObject {
        return try {
            JSONObject(dadosJson)
        } catch (e: Exception) {
            JSONObject()
        }
    }
    
    fun getDataFormatada(): String {
        return dataConsulta ?: "Não informado"
    }
    
    fun getLocalizacaoFormatada(): String {
        return localizacao ?: "Não informado"
    }
}


