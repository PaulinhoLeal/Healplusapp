package com.example.healplusapp.features.agenda

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.healplusapp.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.example.healplusapp.features.anamnese.controller.AnamneseController
import com.example.healplusapp.features.anamnese.model.Anamnese
import com.example.healplusapp.features.anamnese.AnamnesePreviewActivity
import com.example.healplusapp.features.agenda.model.Agendamento
import com.example.healplusapp.features.agenda.model.StatusAgendamento
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

class AgendaAdapter(
    private val agendamentos: List<Agendamento>,
    private val onItemClick: (Agendamento) -> Unit,
    private val onStatusClick: (Agendamento) -> Unit
) : RecyclerView.Adapter<AgendaAdapter.VH>() {
    
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: View = view.findViewById(R.id.card_agendamento)
        val nomePaciente: TextView = view.findViewById(R.id.tv_nome_paciente)
        val dataHora: TextView = view.findViewById(R.id.tv_data_hora)
        val tipoConsulta: TextView = view.findViewById(R.id.tv_tipo_consulta)
        val status: TextView = view.findViewById(R.id.tv_status)
        val iconeStatus: ImageView = view.findViewById(R.id.iv_status_icon)
        val btnVisualizar: Button = view.findViewById(R.id.btn_visualizar)
        val btnStatus: Button = view.findViewById(R.id.btn_status)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_agendamento, parent, false)
        return VH(view)
    }
    
    override fun onBindViewHolder(holder: VH, position: Int) {
        val agendamento = agendamentos[position]
        
        holder.nomePaciente.text = agendamento.pacienteNome
        holder.dataHora.text = "📅 ${agendamento.getDataFormatada()} às ${agendamento.getHoraFormatada()}"
        holder.tipoConsulta.text = "🏥 ${agendamento.tipoConsulta}"
        
        // Status com cor e ícone
        holder.status.text = agendamento.getStatusText()
        holder.status.setTextColor(agendamento.getStatusColor())
        
        when (agendamento.status) {
            StatusAgendamento.AGENDADO -> {
                holder.iconeStatus.setImageResource(android.R.drawable.ic_menu_my_calendar)
                holder.btnStatus.text = "✅ Realizar"
                holder.btnStatus.setBackgroundColor(Color.GREEN)
            }
            StatusAgendamento.REALIZADO -> {
                holder.iconeStatus.setImageResource(android.R.drawable.ic_menu_edit)
                holder.btnStatus.text = "📋 Ver Ficha"
                holder.btnStatus.setBackgroundColor(Color.BLUE)
            }
            StatusAgendamento.CANCELADO -> {
                holder.iconeStatus.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                holder.btnStatus.text = "🔄 Reagendar"
                holder.btnStatus.setBackgroundColor(Color.WHITE)
            }
            StatusAgendamento.REAGENDADO -> {
                holder.iconeStatus.setImageResource(android.R.drawable.ic_menu_recent_history)
                holder.btnStatus.text = "📅 Confirmar"
                holder.btnStatus.setBackgroundColor(Color.CYAN)
            }
        }
        
        holder.btnVisualizar.setOnClickListener { onItemClick(agendamento) }
        holder.btnStatus.setOnClickListener { onStatusClick(agendamento) }
    }
    
    override fun getItemCount(): Int = agendamentos.size
}

class AgendaFragment : Fragment() {
    private lateinit var controller: AnamneseController
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AgendaAdapter
    private lateinit var tvContador: TextView
    private lateinit var tvEmpty: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agenda, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        controller = AnamneseController(requireContext())
        recycler = view.findViewById(R.id.recycler_agenda)
        tvContador = view.findViewById(R.id.tv_contador)
        tvEmpty = view.findViewById(R.id.tv_empty)
        
        recycler.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = AgendaAdapter(
            agendamentos = emptyList(),
            onItemClick = { agendamento -> visualizarAgendamento(agendamento) },
            onStatusClick = { agendamento -> alterarStatus(agendamento) }
        )
        recycler.adapter = adapter
        
        configurarFiltros(view)
        carregarAgenda()
    }
    
    private fun configurarFiltros(view: View) {
        val btnHoje = view.findViewById<Button>(R.id.btn_filtrar_hoje)
        val btnSemana = view.findViewById<Button>(R.id.btn_filtrar_semana)
        val btnMes = view.findViewById<Button>(R.id.btn_filtrar_mes)
        
        btnHoje.setOnClickListener { filtrarPorPeriodo("hoje") }
        btnSemana.setOnClickListener { filtrarPorPeriodo("semana") }
        btnMes.setOnClickListener { filtrarPorPeriodo("mes") }
    }
    
    private fun filtrarPorPeriodo(periodo: String) {
        val agendamentos = gerarAgendamentosFicticios()
        val filtrados = when (periodo) {
            "hoje" -> agendamentos.filter { isToday(it.dataHora) }
            "semana" -> agendamentos.filter { isThisWeek(it.dataHora) }
            "mes" -> agendamentos.filter { isThisMonth(it.dataHora) }
            else -> agendamentos
        }
        
        atualizarLista(filtrados)
    }
    
    private fun carregarAgenda() {
        val agendamentos = gerarAgendamentosFicticios()
        atualizarLista(agendamentos)
    }
    
    private fun gerarAgendamentosFicticios(): List<Agendamento> {
        val anamneses = controller.listar()
        val agendamentos = mutableListOf<Agendamento>()
        
        // Converter anamneses em agendamentos
        anamneses.forEach { anamnese ->
            val json = try {
                JSONObject(anamnese.dadosJson)
            } catch (e: Exception) {
                JSONObject()
            }
            
            agendamentos.add(
                Agendamento(
                    id = anamnese.id,
                    pacienteNome = anamnese.nomeCompleto,
                    pacienteTelefone = json.optString("telefone", "Não informado"),
                    dataHora = anamnese.dataConsulta ?: getRandomDateTime(),
                    tipoConsulta = "Consulta de Enfermagem",
                    observacoes = "Avaliação de ferida",
                    status = StatusAgendamento.AGENDADO
                )
            )
        }
        
        // Adicionar alguns agendamentos fictícios para demonstração
        if (agendamentos.isEmpty()) {
            agendamentos.addAll(listOf(
                Agendamento(
                    id = 1L,
                    pacienteNome = "Maria Silva",
                    pacienteTelefone = "(11) 99999-9999",
                    dataHora = getTodayDateTime(9, 0),
                    tipoConsulta = "Consulta de Enfermagem",
                    observacoes = "Troca de curativo",
                    status = StatusAgendamento.AGENDADO
                ),
                Agendamento(
                    id = 2L,
                    pacienteNome = "João Santos",
                    pacienteTelefone = "(11) 88888-8888",
                    dataHora = getTodayDateTime(14, 30),
                    tipoConsulta = "Avaliação de Ferida",
                    observacoes = "Primeira consulta",
                    status = StatusAgendamento.REALIZADO
                ),
                Agendamento(
                    id = 3L,
                    pacienteNome = "Ana Costa",
                    pacienteTelefone = "(11) 77777-7777",
                    dataHora = getTomorrowDateTime(10, 0),
                    tipoConsulta = "Consulta de Enfermagem",
                    observacoes = "Acompanhamento",
                    status = StatusAgendamento.AGENDADO
                )
            ))
        }
        
        return agendamentos.sortedBy { it.dataHora }
    }
    
    private fun atualizarLista(agendamentos: List<Agendamento>) {
        adapter = AgendaAdapter(
            agendamentos = agendamentos,
            onItemClick = { agendamento -> visualizarAgendamento(agendamento) },
            onStatusClick = { agendamento -> alterarStatus(agendamento) }
        )
        recycler.adapter = adapter
        
        tvContador.text = "${agendamentos.size} consultas"
        tvEmpty.visibility = if (agendamentos.isEmpty()) View.VISIBLE else View.GONE
    }
    
    private fun visualizarAgendamento(agendamento: Agendamento) {
        if (agendamento.id != null) {
            val anamnese = controller.obter(agendamento.id)
            if (anamnese != null) {
                val intent = Intent(requireContext(), AnamnesePreviewActivity::class.java)
                intent.putExtra("anamnese_json", anamnese.dadosJson)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Ficha não encontrada", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Agendamento: ${agendamento.pacienteNome}\n${agendamento.tipoConsulta}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun alterarStatus(agendamento: Agendamento) {
        when (agendamento.status) {
            StatusAgendamento.AGENDADO -> {
                Toast.makeText(requireContext(), "Consulta realizada: ${agendamento.pacienteNome}", Toast.LENGTH_SHORT).show()
            }
            StatusAgendamento.REALIZADO -> {
                visualizarAgendamento(agendamento)
            }
            StatusAgendamento.CANCELADO -> {
                Toast.makeText(requireContext(), "Reagendando: ${agendamento.pacienteNome}", Toast.LENGTH_SHORT).show()
            }
            StatusAgendamento.REAGENDADO -> {
                Toast.makeText(requireContext(), "Confirmando reagendamento: ${agendamento.pacienteNome}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun isToday(dateTime: String): Boolean {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        return dateTime.startsWith(today)
    }
    
    private fun isThisWeek(dateTime: String): Boolean {
        val calendar = Calendar.getInstance()
        val weekStart = calendar.apply { 
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.time
        
        val weekEnd = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.time
        
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = format.parse(dateTime)
            date != null && date >= weekStart && date <= weekEnd
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isThisMonth(dateTime: String): Boolean {
        val thisMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
        return dateTime.contains(thisMonth)
    }
    
    private fun getRandomDateTime(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, (1..30).random())
        calendar.set(Calendar.HOUR_OF_DAY, (8..17).random())
        calendar.set(Calendar.MINUTE, listOf(0, 30).random())
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
    }
    
    private fun getTodayDateTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
    }
    
    private fun getTomorrowDateTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
    }
    
    override fun onResume() {
        super.onResume()
        carregarAgenda()
    }
}

