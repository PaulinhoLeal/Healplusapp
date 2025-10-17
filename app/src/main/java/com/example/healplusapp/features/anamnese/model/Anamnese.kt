package com.example.healplusapp.features.anamnese.model

data class Anamnese(
    val id: Long? = null,
    val nomeCompleto: String,
    val dataConsulta: String?,
    val localizacao: String?,
    val dadosJson: String
)


