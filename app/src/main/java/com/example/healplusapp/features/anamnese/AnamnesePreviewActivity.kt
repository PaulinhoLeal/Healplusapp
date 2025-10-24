package com.example.healplusapp.features.anamnese

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.healplusapp.R
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import org.json.JSONObject

class AnamnesePreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anamnese_preview)
        
        val tv = findViewById<TextView>(R.id.tv_preview)
        val btnVoltar = findViewById<Button>(R.id.btn_voltar)
        
        val json = intent.getStringExtra("anamnese_json") ?: "{}"
        
        try {
            val jsonObj = JSONObject(json)
            val previewText = formatarAnamnese(jsonObj)
            tv.text = previewText
        } catch (e: Exception) {
            tv.text = "Erro ao formatar dados: ${e.message}\n\nJSON original:\n$json"
        }
        
        btnVoltar.setOnClickListener {
            finish()
        }
    }
    
    private fun formatarAnamnese(json: JSONObject): String {
        val sb = StringBuilder()
        
        sb.append("=== ANÁMNESE DE FERIDA ===\n\n")
        
        // Dados Pessoais
        sb.append("📋 DADOS PESSOAIS\n")
        sb.append("Nome: ${json.optString("nomeCompleto", "Não informado")}\n")
        sb.append("Data de Nascimento: ${json.optString("dataNascimento", "Não informado")}\n")
        sb.append("Telefone: ${json.optString("telefone", "Não informado")}\n")
        sb.append("Email: ${json.optString("email", "Não informado")}\n")
        sb.append("Profissão: ${json.optString("profissao", "Não informado")}\n")
        sb.append("Estado Civil: ${json.optString("estadoCivil", "Não informado")}\n\n")
        
        // Dimensões
        sb.append("📏 DIMENSÕES\n")
        sb.append("Largura: ${json.optString("largura", "0")} cm\n")
        sb.append("Comprimento: ${json.optString("comprimento", "0")} cm\n")
        sb.append("Profundidade: ${json.optString("profundidade", "0")} cm\n")
        sb.append("Localização: ${json.optString("localizacao", "Não informado")}\n")
        sb.append("Tempo de Evolução: ${json.optString("tempoEvolucao", "0")} dias\n")
        sb.append("Etiologia: ${json.optString("etiologia", "Não informado")}\n\n")
        
        // Leito da Ferida
        sb.append("🩹 LEITO DA FERIDA\n")
        sb.append("Granulação: ${json.optString("granulacaoPercent", "0")}%\n")
        sb.append("Epitelização: ${json.optString("epitelizacaoPercent", "0")}%\n")
        sb.append("Esfacelo: ${json.optString("esfaceloPercent", "0")}%\n")
        sb.append("Necrose Seca: ${json.optString("necroseSecaPercent", "0")}%\n\n")
        
        // Infecção
        sb.append("🦠 INFECÇÃO E INFLAMAÇÃO\n")
        sb.append("Intensidade da Dor: ${json.optString("intensidadeDor", "0")}/10\n")
        sb.append("Fatores da Dor: ${json.optString("fatoresDor", "Não informado")}\n")
        
        val sinaisInflamacao = mutableListOf<String>()
        if (json.optBoolean("rubor")) sinaisInflamacao.add("Rubor")
        if (json.optBoolean("calor")) sinaisInflamacao.add("Calor")
        if (json.optBoolean("edema")) sinaisInflamacao.add("Edema")
        if (json.optBoolean("dorLocalInflamacao")) sinaisInflamacao.add("Dor Local")
        if (json.optBoolean("perdaFuncao")) sinaisInflamacao.add("Perda de Função")
        
        if (sinaisInflamacao.isNotEmpty()) {
            sb.append("Sinais de Inflamação: ${sinaisInflamacao.joinToString(", ")}\n")
        }
        
        val sinaisInfeccao = mutableListOf<String>()
        if (json.optBoolean("eritemaPerilesional")) sinaisInfeccao.add("Eritema Perilesional")
        if (json.optBoolean("calorLocal")) sinaisInfeccao.add("Calor Local")
        if (json.optBoolean("edemaLocal")) sinaisInfeccao.add("Edema")
        if (json.optBoolean("dorLocalInfeccao")) sinaisInfeccao.add("Dor Local")
        if (json.optBoolean("exsudatoPurulento")) sinaisInfeccao.add("Exsudato Purulento")
        if (json.optBoolean("odorFetido")) sinaisInfeccao.add("Odor Fétido")
        if (json.optBoolean("retardoCicatrizacao")) sinaisInfeccao.add("Retardo na Cicatrização")
        
        if (sinaisInfeccao.isNotEmpty()) {
            sb.append("Sinais de Infecção: ${sinaisInfeccao.joinToString(", ")}\n")
        }
        
        sb.append("Cultura Realizada: ${if (json.optBoolean("culturaRealizada")) "Sim" else "Não"}\n\n")
        
        // Exsudato
        sb.append("💧 EXSUDATO\n")
        sb.append("Quantidade: ${json.optString("exsudatoQuantidade", "Não informado")}\n")
        sb.append("Tipo: ${json.optString("exsudatoTipo", "Não informado")}\n")
        sb.append("Consistência: ${json.optString("exsudatoConsistencia", "Não informado")}\n\n")
        
        // Bordas
        sb.append("🔲 BORDAS\n")
        sb.append("Características: ${json.optString("bordasCaracteristicas", "Não informado")}\n")
        sb.append("Fixação: ${json.optString("bordasFixacao", "Não informado")}\n")
        sb.append("Velocidade de Cicatrização: ${json.optString("velocidadeCicatrizacao", "Não informado")}\n")
        sb.append("Túnel/Cavidade: ${json.optString("tunelLocalizacao", "Não informado")}\n\n")
        
        // Pele Perilesional
        sb.append("🩸 PELE PERILESIONAL\n")
        sb.append("Umidade: ${json.optString("umidadePele", "Não informado")}\n")
        sb.append("Extensão da Alteração: ${json.optString("extensaoAlteracao", "Não informado")}\n")
        
        val condicoesPele = mutableListOf<String>()
        if (json.optBoolean("peleIntegra")) condicoesPele.add("Íntegra")
        if (json.optBoolean("peleEritematosa")) condicoesPele.add("Eritematosa")
        if (json.optBoolean("peleMacerada")) condicoesPele.add("Macerada")
        if (json.optBoolean("peleSecaDescamativa")) condicoesPele.add("Seca e Descamativa")
        if (json.optBoolean("peleEczematosa")) condicoesPele.add("Eczematosa")
        if (json.optBoolean("peleHiperpigmentada")) condicoesPele.add("Hiperpigmentada")
        if (json.optBoolean("peleHipopigmentada")) condicoesPele.add("Hipopigmentada")
        if (json.optBoolean("peleIndurada")) condicoesPele.add("Indurada")
        if (json.optBoolean("peleSensivel")) condicoesPele.add("Sensível")
        if (json.optBoolean("peleEdema")) condicoesPele.add("Edema")
        
        if (condicoesPele.isNotEmpty()) {
            sb.append("Condições: ${condicoesPele.joinToString(", ")}\n")
        }
        sb.append("\n")
        
        // Comorbidades
        sb.append("🏥 COMORBIDADES\n")
        val comorbidades = mutableListOf<String>()
        if (json.optBoolean("dmi")) comorbidades.add("DMI")
        if (json.optBoolean("dmii")) comorbidades.add("DMII")
        if (json.optBoolean("has")) comorbidades.add("HAS")
        if (json.optBoolean("neoplasia")) comorbidades.add("Neoplasia")
        if (json.optBoolean("hivAids")) comorbidades.add("HIV/AIDS")
        if (json.optBoolean("obesidade")) comorbidades.add("Obesidade")
        if (json.optBoolean("cardiopatia")) comorbidades.add("Cardiopatia")
        if (json.optBoolean("dpoc")) comorbidades.add("DPOC")
        if (json.optBoolean("doencaHematologica")) comorbidades.add("Doença Hematológica")
        if (json.optBoolean("doencaVascular")) comorbidades.add("Doença Vascular")
        if (json.optBoolean("demenciaSenil")) comorbidades.add("Demência Senil")
        if (json.optBoolean("insuficienciaRenal")) comorbidades.add("Insuficiência Renal")
        if (json.optBoolean("hanseniase")) comorbidades.add("Hanseníase")
        if (json.optBoolean("insuficienciaHepatica")) comorbidades.add("Insuficiência Hepática")
        if (json.optBoolean("doencaAutoimune")) comorbidades.add("Doença Autoimune")
        
        if (comorbidades.isNotEmpty()) {
            sb.append("Comorbidades: ${comorbidades.joinToString(", ")}\n")
        }
        
        val outrasCondicoes = json.optString("outrasCondicoes", "")
        if (outrasCondicoes.isNotEmpty()) {
            sb.append("Outras Condições: $outrasCondicoes\n")
        }
        sb.append("\n")
        
        // Medicamentos
        sb.append("💊 MEDICAMENTOS EM USO\n")
        val medicamentos = mutableListOf<String>()
        if (json.optBoolean("antiHipertensivo")) medicamentos.add("Anti-hipertensivo")
        if (json.optBoolean("corticoides")) medicamentos.add("Corticoides")
        if (json.optBoolean("hipoglicemiantesOrais")) medicamentos.add("Hipoglicemiantes Orais")
        if (json.optBoolean("aines")) medicamentos.add("AINES")
        if (json.optBoolean("insulina")) medicamentos.add("Insulina")
        if (json.optBoolean("drogasVasoativas")) medicamentos.add("Drogas Vasoativas")
        if (json.optBoolean("suplemento")) medicamentos.add("Suplemento")
        if (json.optBoolean("anticoagulante")) medicamentos.add("Anticoagulante")
        if (json.optBoolean("vitaminico")) medicamentos.add("Vitamínico")
        if (json.optBoolean("antirretroviral")) medicamentos.add("Antirretroviral")
        
        if (medicamentos.isNotEmpty()) {
            sb.append("Medicamentos: ${medicamentos.joinToString(", ")}\n")
        }
        
        val outrosMedicamentos = json.optString("outrosMedicamentos", "")
        if (outrosMedicamentos.isNotEmpty()) {
            sb.append("Outros Medicamentos: $outrosMedicamentos\n")
        }
        sb.append("\n")
        
        // Observações
        val observacoes = json.optString("observacoesPlano", "")
        if (observacoes.isNotEmpty()) {
            sb.append("📝 OBSERVAÇÕES E PLANO DE TRATAMENTO\n")
            sb.append("$observacoes\n\n")
        }
        
        // Dados da Consulta
        sb.append("📅 DADOS DA CONSULTA\n")
        sb.append("Data: ${json.optString("dataConsulta", "Não informado")}\n")
        sb.append("Hora: ${json.optString("horaConsulta", "Não informado")}\n")
        sb.append("Profissional: ${json.optString("profissionalResponsavel", "Não informado")}\n")
        sb.append("COREN/CRM: ${json.optString("conselhoProfissional", "Não informado")}\n")
        sb.append("Data de Retorno: ${json.optString("dataRetorno", "Não informado")}\n")
        
        return sb.toString()
    }
}

