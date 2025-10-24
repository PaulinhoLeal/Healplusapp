package com.example.healplusapp.features.agenda.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Agendamento(
    val id: Long? = null,
    val pacienteNome: String,
    val pacienteTelefone: String,
    val dataHora: String,
    val tipoConsulta: String,
    val observacoes: String? = null,
    val status: StatusAgendamento = StatusAgendamento.AGENDADO
) {
    fun getDataFormatada(): String {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dataHora)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dataHora
        }
    }
    
    fun getHoraFormatada(): String {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dataHora)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dataHora
        }
    }
    
    fun getStatusColor(): Int {
        return when (status) {
            StatusAgendamento.AGENDADO -> android.graphics.Color.BLUE
            StatusAgendamento.REALIZADO -> android.graphics.Color.GREEN
            StatusAgendamento.CANCELADO -> android.graphics.Color.RED
            StatusAgendamento.REAGENDADO -> android.graphics.Color.rgb(255, 165, 0) // Laranja
        }
    }
    
    fun getStatusText(): String {
        return when (status) {
            StatusAgendamento.AGENDADO -> "Agendado"
            StatusAgendamento.REALIZADO -> "Realizado"
            StatusAgendamento.CANCELADO -> "Cancelado"
            StatusAgendamento.REAGENDADO -> "Reagendado"
        }
    }
}

enum class StatusAgendamento {
    AGENDADO,
    REALIZADO,
    CANCELADO,
    REAGENDADO
}
