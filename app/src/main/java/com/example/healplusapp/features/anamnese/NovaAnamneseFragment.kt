package com.example.healplusapp.features.anamnese

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import com.example.healplusapp.R
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import android.view.ViewGroup
import android.widget.TextView
import org.json.JSONObject
import com.example.healplusapp.features.anamnese.controller.AnamneseController
import com.example.healplusapp.features.anamnese.model.Anamnese
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NovaAnamneseFragment : Fragment() {
    private var selectedImageUri: Uri? = null
    private lateinit var anamneseController: AnamneseController
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
        view?.findViewById<ImageView>(R.id.img_prev_ferida)?.setImageURI(uri)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nova_anamnese, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicializa o controller
        anamneseController = AnamneseController(requireContext())
        
        // Toggle simples de seção (accordion-like)
        fun toggleSection(headerId: Int, firstChildId: Int, lastChildId: Int) {
            val header = view.findViewById<TextView>(headerId)
            header?.setOnClickListener {
                // Show/hide a faixa de views entre firstChildId..lastChildId
                val container = view as ViewGroup
                var toggling = false
                for (i in 0 until container.childCount) {
                    val child = container.getChildAt(i)
                    if (child.id == firstChildId) toggling = true
                    if (toggling && child.id != headerId) {
                        child.visibility = if (child.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    }
                    if (child.id == lastChildId) break
                }
            }
        }

        // Mapear seções por cabeçalho e seus primeiros/últimos elementos
        toggleSection(R.id.tv_header_dados, R.id.et_nome_completo, R.id.et_estado_civil)
        toggleSection(R.id.tv_header_tecido, R.id.img_prev_ferida, R.id.btn_remover_imagem)
        toggleSection(R.id.tv_header_dimensoes, R.id.et_largura, R.id.et_etiologia)
        toggleSection(R.id.tv_header_leito, R.id.et_granulacao_percent, R.id.et_necrose_seca_percent)
        toggleSection(R.id.tv_header_infeccao, R.id.seek_intensidade_dor, R.id.cb_cultura_realizada)
        toggleSection(R.id.tv_header_umidade, R.id.et_exsudato_quantidade, R.id.et_exsudato_consistencia)
        toggleSection(R.id.tv_header_bordas, R.id.et_bordas_caracteristicas, R.id.et_velocidade_cicatrizacao)
        toggleSection(R.id.tv_header_pele, R.id.et_umidade_pele, R.id.cb_pele_edema)
        toggleSection(R.id.tv_header_reparo, R.id.et_observacoes, R.id.et_data_retorno)
        toggleSection(R.id.tv_header_sociais, R.id.et_fatores_sociais, R.id.et_ingestao_agua)
        toggleSection(R.id.tv_header_historico, R.id.et_objetivo_tratamento, R.id.et_pulsos_perifericos)
        // Máscaras e validações simples
        val etDataNascimento = view.findViewById<EditText>(R.id.et_data_nascimento)
        val etDataConsulta = view.findViewById<EditText>(R.id.et_data_consulta)
        val etDataRetorno = view.findViewById<EditText>(R.id.et_data_retorno)
        val etTelefone = view.findViewById<EditText>(R.id.et_telefone)
        val etLargura = view.findViewById<EditText>(R.id.et_largura)
        val etComprimento = view.findViewById<EditText>(R.id.et_comprimento)
        val etProfundidade = view.findViewById<EditText>(R.id.et_profundidade)

        fun addDateMask(editText: EditText?) {
            editText?.addTextChangedListener(object : TextWatcher {
                private var isUpdating = false
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return
                    val digits = s.toString().replace("[^0-9]".toRegex(), "")
                    val builder = StringBuilder()
                    var i = 0
                    if (digits.length > 0) { builder.append(digits.substring(0, Math.min(2, digits.length))); i = Math.min(2, digits.length) }
                    if (digits.length > 2) { builder.append('/'); builder.append(digits.substring(2, Math.min(4, digits.length))); i = Math.min(4, digits.length) }
                    if (digits.length > 4) { builder.append('/'); builder.append(digits.substring(4, Math.min(8, digits.length))); i = Math.min(8, digits.length) }
                    isUpdating = true
                    editText?.setText(builder.toString())
                    editText?.setSelection(editText.text.length)
                    isUpdating = false
                }
            })
        }

        fun addPhoneMaskBR(editText: EditText?) {
            editText?.addTextChangedListener(object : TextWatcher {
                private var isUpdating = false
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return
                    val digits = s.toString().replace("[^0-9]".toRegex(), "")
                    val builder = StringBuilder()
                    var i = 0
                    if (digits.isNotEmpty()) {
                        builder.append('(')
                        val dd = digits.substring(0, Math.min(2, digits.length))
                        builder.append(dd)
                        if (digits.length >= 2) builder.append(") ")
                        i = Math.min(2, digits.length)
                    }
                    if (digits.length > 2) {
                        val nine = if (digits.length - 2 > 9) 9 else digits.length - 2
                        val part1 = digits.substring(2, 2 + Math.min(nine, 5))
                        builder.append(part1)
                        if (digits.length > 7) builder.append('-')
                    }
                    if (digits.length > 7) {
                        val part2 = digits.substring(7, Math.min(digits.length, 11))
                        builder.append(part2)
                    }
                    isUpdating = true
                    editText?.setText(builder.toString())
                    editText?.setSelection(editText.text.length)
                    isUpdating = false
                }
            })
        }

        fun addDecimalSanitizer(editText: EditText?) {
            editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // Permite dígitos, vírgula e ponto; evita múltiplos separadores
                    val txt = s?.toString() ?: return
                    val cleaned = txt.replace("[^0-9,]".toRegex(), "")
                    if (cleaned != txt) {
                        editText.setText(cleaned)
                        editText.setSelection(cleaned.length)
                    }
                }
            })
        }

        addDateMask(etDataNascimento)
        addDateMask(etDataConsulta)
        addDateMask(etDataRetorno)
        addPhoneMaskBR(etTelefone)
        addDecimalSanitizer(etLargura)
        addDecimalSanitizer(etComprimento)
        addDecimalSanitizer(etProfundidade)
        
        // Configurar barra dinâmica do leito da ferida
        configurarBarraLeitoFerida(view)
        
        // Configurar slider de intensidade da dor
        configurarSliderDor(view)

        // Escolha/remoção de imagem
        view.findViewById<Button>(R.id.btn_escolher_imagem)?.setOnClickListener {
            pickImage.launch("image/*")
        }
        view.findViewById<Button>(R.id.btn_remover_imagem)?.setOnClickListener {
            selectedImageUri = null
            view.findViewById<ImageView>(R.id.img_prev_ferida)?.setImageDrawable(null)
            Toast.makeText(requireContext(), "Imagem removida", Toast.LENGTH_SHORT).show()
        }
        val btnSalvar = view.findViewById<Button>(R.id.btn_salvar_anamnese)
        val btnLimpar = view.findViewById<Button>(R.id.btn_limpar_formulario)
        
        btnLimpar.setOnClickListener {
            limparFormulario(view)
            Toast.makeText(requireContext(), "Formulário limpo", Toast.LENGTH_SHORT).show()
        }
        
        btnSalvar.setOnClickListener {
            val data = JSONObject()

            fun putText(id: Int, key: String) {
                val et = view.findViewById<EditText>(id)
                if (et != null) data.put(key, et.text?.toString()?.trim() ?: "")
            }

            fun putCheck(id: Int, key: String) {
                val cb = view.findViewById<CheckBox>(id)
                if (cb != null) data.put(key, cb.isChecked)
            }

            // Dados Pessoais
            putText(R.id.et_nome_completo, "nomeCompleto")
            putText(R.id.et_data_nascimento, "dataNascimento")
            putText(R.id.et_telefone, "telefone")
            putText(R.id.et_email, "email")
            putText(R.id.et_profissao, "profissao")
            putText(R.id.et_estado_civil, "estadoCivil")

            // Dimensões/Características
            fun normalizedDecimalFrom(id: Int): String {
                val et = view.findViewById<EditText>(id)
                val raw = et?.text?.toString()?.trim() ?: ""
                return raw.replace(',', '.')
            }
            data.put("largura", normalizedDecimalFrom(R.id.et_largura))
            data.put("comprimento", normalizedDecimalFrom(R.id.et_comprimento))
            data.put("profundidade", normalizedDecimalFrom(R.id.et_profundidade))
            putText(R.id.et_localizacao, "localizacao")
            putText(R.id.et_tempo_evolucao, "tempoEvolucao")
            putText(R.id.et_etiologia, "etiologia")

            // Leito da ferida
            putText(R.id.et_granulacao_percent, "granulacaoPercent")
            putText(R.id.et_epitelizacao_percent, "epitelizacaoPercent")
            putText(R.id.et_esfacelo_percent, "esfaceloPercent")
            putText(R.id.et_necrose_seca_percent, "necroseSecaPercent")

            // Infecção/Inflamação
            val seekDor = view.findViewById<android.widget.SeekBar>(R.id.seek_intensidade_dor)
            data.put("intensidadeDor", seekDor?.progress ?: 0)
            putText(R.id.et_fatores_dor, "fatoresDor")
            putCheck(R.id.cb_rubor, "rubor")
            putCheck(R.id.cb_calor, "calor")
            putCheck(R.id.cb_edema, "edema")
            putCheck(R.id.cb_dor_local, "dorLocalInflamacao")
            putCheck(R.id.cb_perda_funcao, "perdaFuncao")

            putCheck(R.id.cb_eritema_perilesional, "eritemaPerilesional")
            putCheck(R.id.cb_calor_local, "calorLocal")
            putCheck(R.id.cb_edema_local, "edemaLocal")
            putCheck(R.id.cb_dor_local_inf, "dorLocalInfeccao")
            putCheck(R.id.cb_exsudato_purulento, "exsudatoPurulento")
            putCheck(R.id.cb_odor_fetido, "odorFetido")
            putCheck(R.id.cb_retardo_cicatrizacao, "retardoCicatrizacao")
            putCheck(R.id.cb_cultura_realizada, "culturaRealizada")

            // Umidade (Exsudato)
            putText(R.id.et_exsudato_quantidade, "exsudatoQuantidade")
            putText(R.id.et_exsudato_tipo, "exsudatoTipo")
            putText(R.id.et_exsudato_consistencia, "exsudatoConsistencia")

            // Bordas
            putText(R.id.et_bordas_caracteristicas, "bordasCaracteristicas")
            putText(R.id.et_bordas_fixacao, "bordasFixacao")
            putText(R.id.et_velocidade_cicatrizacao, "velocidadeCicatrizacao")
            putText(R.id.et_tunel_localizacao, "tunelLocalizacao")

            // Pele Perilesional
            putText(R.id.et_umidade_pele, "umidadePele")
            putText(R.id.et_extensao_alteracao, "extensaoAlteracao")
            putCheck(R.id.cb_pele_integra, "peleIntegra")
            putCheck(R.id.cb_pele_eritematosa, "peleEritematosa")
            putCheck(R.id.cb_pele_macerada, "peleMacerada")
            putCheck(R.id.cb_pele_seca, "peleSecaDescamativa")
            putCheck(R.id.cb_pele_eczema, "peleEczematosa")
            putCheck(R.id.cb_pele_hiperpigmentada, "peleHiperpigmentada")
            putCheck(R.id.cb_pele_hipopigmentada, "peleHipopigmentada")
            putCheck(R.id.cb_pele_indurada, "peleIndurada")
            putCheck(R.id.cb_pele_sensivel, "peleSensivel")
            putCheck(R.id.cb_pele_edema, "peleEdema")

            // Reparo e Recomendações
            putText(R.id.et_observacoes, "observacoesPlano")
            putText(R.id.et_data_consulta, "dataConsulta")
            putText(R.id.et_hora_consulta, "horaConsulta")
            putText(R.id.et_profissional, "profissionalResponsavel")
            putText(R.id.et_conselho, "conselhoProfissional")
            putText(R.id.et_data_retorno, "dataRetorno")

            // Sociais e Histórico
            putText(R.id.et_fatores_sociais, "fatoresSociaisAutocuidado")
            putText(R.id.et_nivel_atividade, "nivelAtividade")
            putText(R.id.et_compreensao_adesao, "compreensaoAdesao")
            putText(R.id.et_suporte_social, "suporteSocialCuidadores")
            putCheck(R.id.cb_atividade_fisica, "praticaAtividadeFisica")
            putCheck(R.id.cb_ingere_alcool, "ingereAlcool")
            putCheck(R.id.cb_fumante, "fumante")
            putText(R.id.et_avaliacao_nutricional, "avaliacaoNutricional")
            putText(R.id.et_ingestao_agua, "ingestaoAguaDia")
            putText(R.id.et_objetivo_tratamento, "objetivoTratamento")
            putText(R.id.et_historico_cicatrizacao, "historicoCicatrizacao")
            putCheck(R.id.cb_alergia, "alergia")
            putCheck(R.id.cb_cirurgias, "cirurgias")
            putCheck(R.id.cb_claudicacao, "claudicacaoIntermitente")
            putCheck(R.id.cb_dor_repouso, "dorRepouso")
            putText(R.id.et_pulsos_perifericos, "pulsosPerifericos")

            // Comorbidades
            putCheck(R.id.cb_dmi, "dmi")
            putCheck(R.id.cb_dmii, "dmii")
            putCheck(R.id.cb_has, "has")
            putCheck(R.id.cb_neoplasia, "neoplasia")
            putCheck(R.id.cb_hiv, "hivAids")
            putCheck(R.id.cb_obesidade, "obesidade")
            putCheck(R.id.cb_cardiopatia, "cardiopatia")
            putCheck(R.id.cb_dpoc, "dpoc")
            putCheck(R.id.cb_doenca_hematologica, "doencaHematologica")
            putCheck(R.id.cb_doenca_vascular, "doencaVascular")
            putCheck(R.id.cb_demencia, "demenciaSenil")
            putCheck(R.id.cb_insuficiencia_renal, "insuficienciaRenal")
            putCheck(R.id.cb_hanseniase, "hanseniase")
            putCheck(R.id.cb_insuficiencia_hepatica, "insuficienciaHepatica")
            putCheck(R.id.cb_doenca_autoimune, "doencaAutoimune")
            putText(R.id.et_outras_condicoes, "outrasCondicoes")

            // Medicamentos em Uso
            putCheck(R.id.cb_anti_hipertensivo, "antiHipertensivo")
            putCheck(R.id.cb_corticoides, "corticoides")
            putCheck(R.id.cb_hipoglicemiantes, "hipoglicemiantesOrais")
            putCheck(R.id.cb_aines, "aines")
            putCheck(R.id.cb_insulina, "insulina")
            putCheck(R.id.cb_drogas_vasoativas, "drogasVasoativas")
            putCheck(R.id.cb_suplemento, "suplemento")
            putCheck(R.id.cb_anticoagulante, "anticoagulante")
            putCheck(R.id.cb_vitaminico, "vitaminico")
            putCheck(R.id.cb_antirretroviral, "antirretroviral")
            putText(R.id.et_outros_medicamentos, "outrosMedicamentos")

            val json = data.toString()
            // Inclui URI da imagem, se houver
            selectedImageUri?.let { data.put("imagemUri", it.toString()) }
            // Validações simples obrigatórias
            val obrigatorios = listOf(
                R.id.et_nome_completo to "Nome é obrigatório",
                R.id.et_data_nascimento to "Data de nascimento é obrigatória",
                R.id.et_localizacao to "Localização é obrigatória"
            )
            var invalido = false
            obrigatorios.forEach { (id, msg) ->
                val et = view.findViewById<EditText>(id)
                if (et != null && et.text.isNullOrBlank()) {
                    et.error = msg
                    invalido = true
                }
            }
            if (invalido) {
                Toast.makeText(requireContext(), "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validações avançadas
            fun parseIntSafe(id: Int): Int {
                val et = view.findViewById<EditText>(id)
                return et?.text?.toString()?.trim()?.replace("[^0-9]".toRegex(), "")?.toIntOrNull() ?: 0
            }

            val pGran = parseIntSafe(R.id.et_granulacao_percent)
            val pEpi = parseIntSafe(R.id.et_epitelizacao_percent)
            val pEsf = parseIntSafe(R.id.et_esfacelo_percent)
            val pNec = parseIntSafe(R.id.et_necrose_seca_percent)
            val somaPercentuais = pGran + pEpi + pEsf + pNec
            
            // Validar se a soma é exatamente 100%
            if (somaPercentuais != 100) {
                view.findViewById<EditText>(R.id.et_granulacao_percent)?.error = "Soma deve ser 100%"
                view.findViewById<EditText>(R.id.et_epitelizacao_percent)?.error = "Soma deve ser 100%"
                view.findViewById<EditText>(R.id.et_esfacelo_percent)?.error = "Soma deve ser 100%"
                view.findViewById<EditText>(R.id.et_necrose_seca_percent)?.error = "Soma deve ser 100%"
                Toast.makeText(requireContext(), "A soma dos percentuais do leito da ferida deve ser exatamente 100% (atual: ${'$'}somaPercentuais%).", Toast.LENGTH_LONG).show()
                Log.w("Anamnese", "Percentuais do leito inválidos: soma=${'$'}somaPercentuais")
                return@setOnClickListener
            }

            // E-mail
            val etEmail = view.findViewById<EditText>(R.id.et_email)
            val emailVal = etEmail?.text?.toString()?.trim().orEmpty()
            if (emailVal.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()) {
                etEmail?.error = "E-mail inválido"
                Toast.makeText(requireContext(), "Informe um e-mail válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Telefone básico
            val etFone = view.findViewById<EditText>(R.id.et_telefone)
            val foneDigits = etFone?.text?.toString()?.replace("[^0-9]".toRegex(), "").orEmpty()
            if (foneDigits.length < 10) {
                etFone?.error = "Telefone incompleto"
                Toast.makeText(requireContext(), "Informe um telefone válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Regera JSON final e loga
            val finalJson = data.toString()
            Log.d("Anamnese", finalJson)

            // Salva no banco de dados
            try {
                val nomeCompleto = view.findViewById<EditText>(R.id.et_nome_completo)?.text?.toString()?.trim() ?: ""
                val dataConsulta = view.findViewById<EditText>(R.id.et_data_consulta)?.text?.toString()?.trim() ?: ""
                val localizacao = view.findViewById<EditText>(R.id.et_localizacao)?.text?.toString()?.trim() ?: ""
                
                val anamnese = Anamnese(
                    nomeCompleto = nomeCompleto,
                    dataConsulta = dataConsulta,
                    localizacao = localizacao,
                    dadosJson = finalJson
                )
                
                val id = anamneseController.salvar(anamnese)
                
                Toast.makeText(requireContext(), "Anamnese salva com sucesso! ID: $id", Toast.LENGTH_LONG).show()
                
                // Limpa o formulário após salvar
                limparFormulario(view)
                
            } catch (e: Exception) {
                Log.e("Anamnese", "Erro ao salvar anamnese", e)
                Toast.makeText(requireContext(), "Erro ao salvar anamnese: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun limparFormulario(view: View) {
        // Limpa todos os EditText
        val editTexts = listOf(
            R.id.et_nome_completo, R.id.et_data_nascimento, R.id.et_telefone, R.id.et_email,
            R.id.et_profissao, R.id.et_estado_civil, R.id.et_largura, R.id.et_comprimento,
            R.id.et_profundidade, R.id.et_localizacao, R.id.et_tempo_evolucao, R.id.et_etiologia,
            R.id.et_granulacao_percent, R.id.et_epitelizacao_percent, R.id.et_esfacelo_percent,
            R.id.et_necrose_seca_percent, R.id.et_fatores_dor,
            R.id.et_exsudato_quantidade, R.id.et_exsudato_tipo, R.id.et_exsudato_consistencia,
            R.id.et_bordas_caracteristicas, R.id.et_bordas_fixacao, R.id.et_velocidade_cicatrizacao,
            R.id.et_tunel_localizacao, R.id.et_umidade_pele, R.id.et_extensao_alteracao,
            R.id.et_observacoes, R.id.et_data_consulta, R.id.et_hora_consulta, R.id.et_profissional,
            R.id.et_conselho, R.id.et_data_retorno, R.id.et_fatores_sociais, R.id.et_nivel_atividade,
            R.id.et_compreensao_adesao, R.id.et_suporte_social, R.id.et_avaliacao_nutricional,
            R.id.et_ingestao_agua, R.id.et_objetivo_tratamento, R.id.et_historico_cicatrizacao,
            R.id.et_pulsos_perifericos, R.id.et_outras_condicoes, R.id.et_outros_medicamentos
        )
        
        editTexts.forEach { id ->
            view.findViewById<EditText>(id)?.setText("")
        }
        
        // Desmarca todos os CheckBox
        val checkBoxes = listOf(
            R.id.cb_rubor, R.id.cb_calor, R.id.cb_edema, R.id.cb_dor_local, R.id.cb_perda_funcao,
            R.id.cb_eritema_perilesional, R.id.cb_calor_local, R.id.cb_edema_local, R.id.cb_dor_local_inf,
            R.id.cb_exsudato_purulento, R.id.cb_odor_fetido, R.id.cb_retardo_cicatrizacao, R.id.cb_cultura_realizada,
            R.id.cb_pele_integra, R.id.cb_pele_eritematosa, R.id.cb_pele_macerada, R.id.cb_pele_seca,
            R.id.cb_pele_eczema, R.id.cb_pele_hiperpigmentada, R.id.cb_pele_hipopigmentada, R.id.cb_pele_indurada,
            R.id.cb_pele_sensivel, R.id.cb_pele_edema, R.id.cb_atividade_fisica, R.id.cb_ingere_alcool,
            R.id.cb_fumante, R.id.cb_alergia, R.id.cb_cirurgias, R.id.cb_claudicacao, R.id.cb_dor_repouso,
            R.id.cb_dmi, R.id.cb_dmii, R.id.cb_has, R.id.cb_neoplasia, R.id.cb_hiv, R.id.cb_obesidade,
            R.id.cb_cardiopatia, R.id.cb_dpoc, R.id.cb_doenca_hematologica, R.id.cb_doenca_vascular,
            R.id.cb_demencia, R.id.cb_insuficiencia_renal, R.id.cb_hanseniase, R.id.cb_insuficiencia_hepatica,
            R.id.cb_doenca_autoimune, R.id.cb_anti_hipertensivo, R.id.cb_corticoides, R.id.cb_hipoglicemiantes,
            R.id.cb_aines, R.id.cb_insulina, R.id.cb_drogas_vasoativas, R.id.cb_suplemento,
            R.id.cb_anticoagulante, R.id.cb_vitaminico, R.id.cb_antirretroviral
        )
        
        checkBoxes.forEach { id ->
            view.findViewById<CheckBox>(id)?.isChecked = false
        }
        
        // Remove a imagem selecionada
        selectedImageUri = null
        view.findViewById<ImageView>(R.id.img_prev_ferida)?.setImageDrawable(null)
        
        // Resetar slider de dor
        view.findViewById<android.widget.SeekBar>(R.id.seek_intensidade_dor)?.progress = 0
        
        // Define data atual para alguns campos
        val dataAtual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        view.findViewById<EditText>(R.id.et_data_consulta)?.setText(dataAtual)
    }
    
    fun carregarAnamnese(anamnese: Anamnese) {
        val json = anamnese.toJsonObject()
        
        // Carrega dados básicos
        view?.findViewById<EditText>(R.id.et_nome_completo)?.setText(anamnese.nomeCompleto)
        view?.findViewById<EditText>(R.id.et_data_consulta)?.setText(anamnese.dataConsulta)
        view?.findViewById<EditText>(R.id.et_localizacao)?.setText(anamnese.localizacao)
        
        // Carrega dados do JSON
        fun setText(id: Int, key: String) {
            val et = view?.findViewById<EditText>(id)
            et?.setText(json.optString(key, ""))
        }
        
        fun setCheck(id: Int, key: String) {
            val cb = view?.findViewById<CheckBox>(id)
            cb?.isChecked = json.optBoolean(key, false)
        }
        
        // Dados pessoais
        setText(R.id.et_data_nascimento, "dataNascimento")
        setText(R.id.et_telefone, "telefone")
        setText(R.id.et_email, "email")
        setText(R.id.et_profissao, "profissao")
        setText(R.id.et_estado_civil, "estadoCivil")
        
        // Dimensões
        setText(R.id.et_largura, "largura")
        setText(R.id.et_comprimento, "comprimento")
        setText(R.id.et_profundidade, "profundidade")
        setText(R.id.et_tempo_evolucao, "tempoEvolucao")
        setText(R.id.et_etiologia, "etiologia")
        
        // Leito da ferida
        setText(R.id.et_granulacao_percent, "granulacaoPercent")
        setText(R.id.et_epitelizacao_percent, "epitelizacaoPercent")
        setText(R.id.et_esfacelo_percent, "esfaceloPercent")
        setText(R.id.et_necrose_seca_percent, "necroseSecaPercent")
        
        // Infecção
        val intensidadeDor = json.optInt("intensidadeDor", 0)
        view?.findViewById<android.widget.SeekBar>(R.id.seek_intensidade_dor)?.progress = intensidadeDor
        setText(R.id.et_fatores_dor, "fatoresDor")
        setCheck(R.id.cb_rubor, "rubor")
        setCheck(R.id.cb_calor, "calor")
        setCheck(R.id.cb_edema, "edema")
        setCheck(R.id.cb_dor_local, "dorLocalInflamacao")
        setCheck(R.id.cb_perda_funcao, "perdaFuncao")
        setCheck(R.id.cb_eritema_perilesional, "eritemaPerilesional")
        setCheck(R.id.cb_calor_local, "calorLocal")
        setCheck(R.id.cb_edema_local, "edemaLocal")
        setCheck(R.id.cb_dor_local_inf, "dorLocalInfeccao")
        setCheck(R.id.cb_exsudato_purulento, "exsudatoPurulento")
        setCheck(R.id.cb_odor_fetido, "odorFetido")
        setCheck(R.id.cb_retardo_cicatrizacao, "retardoCicatrizacao")
        setCheck(R.id.cb_cultura_realizada, "culturaRealizada")
        
        // Exsudato
        setText(R.id.et_exsudato_quantidade, "exsudatoQuantidade")
        setText(R.id.et_exsudato_tipo, "exsudatoTipo")
        setText(R.id.et_exsudato_consistencia, "exsudatoConsistencia")
        
        // Bordas
        setText(R.id.et_bordas_caracteristicas, "bordasCaracteristicas")
        setText(R.id.et_bordas_fixacao, "bordasFixacao")
        setText(R.id.et_velocidade_cicatrizacao, "velocidadeCicatrizacao")
        setText(R.id.et_tunel_localizacao, "tunelLocalizacao")
        
        // Pele perilesional
        setText(R.id.et_umidade_pele, "umidadePele")
        setText(R.id.et_extensao_alteracao, "extensaoAlteracao")
        setCheck(R.id.cb_pele_integra, "peleIntegra")
        setCheck(R.id.cb_pele_eritematosa, "peleEritematosa")
        setCheck(R.id.cb_pele_macerada, "peleMacerada")
        setCheck(R.id.cb_pele_seca, "peleSecaDescamativa")
        setCheck(R.id.cb_pele_eczema, "peleEczematosa")
        setCheck(R.id.cb_pele_hiperpigmentada, "peleHiperpigmentada")
        setCheck(R.id.cb_pele_hipopigmentada, "peleHipopigmentada")
        setCheck(R.id.cb_pele_indurada, "peleIndurada")
        setCheck(R.id.cb_pele_sensivel, "peleSensivel")
        setCheck(R.id.cb_pele_edema, "peleEdema")
        
        // Observações
        setText(R.id.et_observacoes, "observacoesPlano")
        setText(R.id.et_hora_consulta, "horaConsulta")
        setText(R.id.et_profissional, "profissionalResponsavel")
        setText(R.id.et_conselho, "conselhoProfissional")
        setText(R.id.et_data_retorno, "dataRetorno")
        
        // Fatores sociais
        setText(R.id.et_fatores_sociais, "fatoresSociaisAutocuidado")
        setText(R.id.et_nivel_atividade, "nivelAtividade")
        setText(R.id.et_compreensao_adesao, "compreensaoAdesao")
        setText(R.id.et_suporte_social, "suporteSocialCuidadores")
        setCheck(R.id.cb_atividade_fisica, "praticaAtividadeFisica")
        setCheck(R.id.cb_ingere_alcool, "ingereAlcool")
        setCheck(R.id.cb_fumante, "fumante")
        setText(R.id.et_avaliacao_nutricional, "avaliacaoNutricional")
        setText(R.id.et_ingestao_agua, "ingestaoAguaDia")
        setText(R.id.et_objetivo_tratamento, "objetivoTratamento")
        setText(R.id.et_historico_cicatrizacao, "historicoCicatrizacao")
        setCheck(R.id.cb_alergia, "alergia")
        setCheck(R.id.cb_cirurgias, "cirurgias")
        setCheck(R.id.cb_claudicacao, "claudicacaoIntermitente")
        setCheck(R.id.cb_dor_repouso, "dorRepouso")
        setText(R.id.et_pulsos_perifericos, "pulsosPerifericos")
        
        // Comorbidades
        setCheck(R.id.cb_dmi, "dmi")
        setCheck(R.id.cb_dmii, "dmii")
        setCheck(R.id.cb_has, "has")
        setCheck(R.id.cb_neoplasia, "neoplasia")
        setCheck(R.id.cb_hiv, "hivAids")
        setCheck(R.id.cb_obesidade, "obesidade")
        setCheck(R.id.cb_cardiopatia, "cardiopatia")
        setCheck(R.id.cb_dpoc, "dpoc")
        setCheck(R.id.cb_doenca_hematologica, "doencaHematologica")
        setCheck(R.id.cb_doenca_vascular, "doencaVascular")
        setCheck(R.id.cb_demencia, "demenciaSenil")
        setCheck(R.id.cb_insuficiencia_renal, "insuficienciaRenal")
        setCheck(R.id.cb_hanseniase, "hanseniase")
        setCheck(R.id.cb_insuficiencia_hepatica, "insuficienciaHepatica")
        setCheck(R.id.cb_doenca_autoimune, "doencaAutoimune")
        setText(R.id.et_outras_condicoes, "outrasCondicoes")
        
        // Medicamentos
        setCheck(R.id.cb_anti_hipertensivo, "antiHipertensivo")
        setCheck(R.id.cb_corticoides, "corticoides")
        setCheck(R.id.cb_hipoglicemiantes, "hipoglicemiantesOrais")
        setCheck(R.id.cb_aines, "aines")
        setCheck(R.id.cb_insulina, "insulina")
        setCheck(R.id.cb_drogas_vasoativas, "drogasVasoativas")
        setCheck(R.id.cb_suplemento, "suplemento")
        setCheck(R.id.cb_anticoagulante, "anticoagulante")
        setCheck(R.id.cb_vitaminico, "vitaminico")
        setCheck(R.id.cb_antirretroviral, "antirretroviral")
        setText(R.id.et_outros_medicamentos, "outrosMedicamentos")
        
        Toast.makeText(requireContext(), "Anamnese carregada para edição", Toast.LENGTH_SHORT).show()
    }
    
    private fun configurarBarraLeitoFerida(view: View) {
        val etGranulacao = view.findViewById<EditText>(R.id.et_granulacao_percent)
        val etEpitelizacao = view.findViewById<EditText>(R.id.et_epitelizacao_percent)
        val etEsfacelo = view.findViewById<EditText>(R.id.et_esfacelo_percent)
        val etNecrose = view.findViewById<EditText>(R.id.et_necrose_seca_percent)
        
        val barGranulacao = view.findViewById<View>(R.id.leito_bar_granulacao)
        val barEpitelizacao = view.findViewById<View>(R.id.leito_bar_epitelizacao)
        val barEsfacelo = view.findViewById<View>(R.id.leito_bar_esfacelo)
        val barNecrose = view.findViewById<View>(R.id.leito_bar_necrose)
        
        // Delay para evitar atualizações excessivas
        var updateHandler: android.os.Handler? = null
        var updateRunnable: Runnable? = null
        
        fun atualizarBarra() {
            val granulacao = etGranulacao?.text?.toString()?.toIntOrNull() ?: 0
            val epitelizacao = etEpitelizacao?.text?.toString()?.toIntOrNull() ?: 0
            val esfacelo = etEsfacelo?.text?.toString()?.toIntOrNull() ?: 0
            val necrose = etNecrose?.text?.toString()?.toIntOrNull() ?: 0
            
            // Garantir que os valores estão entre 0 e 100
            val gran = granulacao.coerceIn(0, 100)
            val epi = epitelizacao.coerceIn(0, 100)
            val esf = esfacelo.coerceIn(0, 100)
            val nec = necrose.coerceIn(0, 100)
            
            val total = gran + epi + esf + nec
            
            // Atualizar larguras usando weight baseado na proporção
            val layoutParamsGranulacao = barGranulacao.layoutParams as android.widget.LinearLayout.LayoutParams
            val layoutParamsEpitelizacao = barEpitelizacao.layoutParams as android.widget.LinearLayout.LayoutParams
            val layoutParamsEsfacelo = barEsfacelo.layoutParams as android.widget.LinearLayout.LayoutParams
            val layoutParamsNecrose = barNecrose.layoutParams as android.widget.LinearLayout.LayoutParams
            
            if (total > 0) {
                // Usar weight proporcional baseado no total
                layoutParamsGranulacao.weight = (gran * 100.0 / total).toFloat()
                layoutParamsEpitelizacao.weight = (epi * 100.0 / total).toFloat()
                layoutParamsEsfacelo.weight = (esf * 100.0 / total).toFloat()
                layoutParamsNecrose.weight = (nec * 100.0 / total).toFloat()
            } else {
                // Se total for 0, todas as barras ficam invisíveis
                layoutParamsGranulacao.weight = 0f
                layoutParamsEpitelizacao.weight = 0f
                layoutParamsEsfacelo.weight = 0f
                layoutParamsNecrose.weight = 0f
            }
            
            // Aplicar os novos layout params
            barGranulacao.layoutParams = layoutParamsGranulacao
            barEpitelizacao.layoutParams = layoutParamsEpitelizacao
            barEsfacelo.layoutParams = layoutParamsEsfacelo
            barNecrose.layoutParams = layoutParamsNecrose
            
            // Atualizar cores baseadas nos percentuais
            atualizarCoresBarra(barGranulacao, gran)
            atualizarCoresBarra(barEpitelizacao, epi)
            atualizarCoresBarra(barEsfacelo, esf)
            atualizarCoresBarra(barNecrose, nec)
            
            // Forçar redraw
            barGranulacao.requestLayout()
            barEpitelizacao.requestLayout()
            barEsfacelo.requestLayout()
            barNecrose.requestLayout()
        }
        
        // Adicionar listeners para todos os campos com delay
        val campos = listOf(etGranulacao, etEpitelizacao, etEsfacelo, etNecrose)
        campos.forEach { campo ->
            campo?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // Cancelar atualização anterior se existir
                    updateRunnable?.let { updateHandler?.removeCallbacks(it) }
                    
                    // Criar nova atualização com delay
                    updateRunnable = Runnable { atualizarBarra() }
                    updateHandler = android.os.Handler(android.os.Looper.getMainLooper())
                    updateHandler?.postDelayed(updateRunnable!!, 100) // 100ms de delay
                }
            })
        }
        
        // Atualizar barra inicial após um pequeno delay para garantir que as views estão prontas
        view.post {
            atualizarBarra()
        }
    }
    
    private fun atualizarCoresBarra(barra: View, percentual: Int) {
        when (barra.id) {
            R.id.leito_bar_granulacao -> {
                // Granulação: vermelho-alaranjado (como na imagem)
                if (percentual == 0) {
                    barra.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                } else {
                    val intensidade = (percentual * 2.55).toInt().coerceIn(50, 255)
                    // Vermelho-alaranjado: mais vermelho, menos verde, sem azul
                    barra.setBackgroundColor(android.graphics.Color.rgb(intensidade, (intensidade * 0.6).toInt(), 0))
                }
            }
            R.id.leito_bar_epitelizacao -> {
                // Epitelização: rosa claro/pêssego (como na imagem)
                if (percentual == 0) {
                    barra.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                } else {
                    val intensidade = (percentual * 2.55).toInt().coerceIn(50, 255)
                    // Rosa claro/pêssego: vermelho alto, verde médio, azul baixo
                    barra.setBackgroundColor(android.graphics.Color.rgb(intensidade, (intensidade * 0.8).toInt(), (intensidade * 0.6).toInt()))
                }
            }
            R.id.leito_bar_esfacelo -> {
                // Esfacelo: amarelo brilhante (como na imagem)
                if (percentual == 0) {
                    barra.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                } else {
                    val intensidade = (percentual * 2.55).toInt().coerceIn(50, 255)
                    // Amarelo brilhante: vermelho e verde altos, sem azul
                    barra.setBackgroundColor(android.graphics.Color.rgb(intensidade, intensidade, 0))
                }
            }
            R.id.leito_bar_necrose -> {
                // Necrose: cinza escuro/preto (como na imagem)
                if (percentual == 0) {
                    barra.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                } else {
                    val intensidade = (255 - percentual * 2.2).toInt().coerceIn(30, 255)
                    // Cinza escuro: todos os valores iguais e baixos
                    barra.setBackgroundColor(android.graphics.Color.rgb(intensidade, intensidade, intensidade))
                }
            }
        }
    }
    
    private fun configurarSliderDor(view: View) {
        val seekBar = view.findViewById<android.widget.SeekBar>(R.id.seek_intensidade_dor)
        val textView = view.findViewById<TextView>(R.id.tv_intensidade_dor_valor)
        
        seekBar?.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val intensidade = progress
                val descricao = when (intensidade) {
                    0 -> "0 - Sem dor"
                    1, 2, 3 -> "$intensidade - Dor leve"
                    4, 5, 6 -> "$intensidade - Dor moderada"
                    7, 8, 9 -> "$intensidade - Dor intensa"
                    10 -> "10 - Dor insuportável"
                    else -> "$intensidade"
                }
                
                textView?.text = descricao
                
                // Atualizar cor baseada na intensidade
                val cor = when (intensidade) {
                    0 -> android.graphics.Color.GREEN
                    1, 2, 3 -> android.graphics.Color.YELLOW
                    4, 5, 6 -> android.graphics.Color.rgb(255, 165, 0) // Laranja
                    7, 8, 9 -> android.graphics.Color.RED
                    10 -> android.graphics.Color.rgb(139, 0, 0) // Vermelho escuro
                    else -> android.graphics.Color.GRAY
                }
                
                textView?.setTextColor(cor)
            }
            
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }
}

