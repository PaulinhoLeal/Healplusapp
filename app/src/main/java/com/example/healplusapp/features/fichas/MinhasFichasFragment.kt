package com.example.healplusapp.features.fichas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.healplusapp.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.ImageView
import android.widget.Button
import android.widget.Toast
import com.example.healplusapp.features.anamnese.controller.AnamneseController
import com.example.healplusapp.features.anamnese.model.Anamnese
import com.example.healplusapp.features.anamnese.ui.AnamnesePreviewActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FichaResumo(
    val id: Long,
    val paciente: String, 
    val local: String, 
    val data: String,
    val intensidadeDor: Int,
    val status: String
)

class FichasAdapter(
    private val itens: List<FichaResumo>,
    private val onItemClick: (FichaResumo) -> Unit,
    private val onEditClick: (FichaResumo) -> Unit
) : RecyclerView.Adapter<FichasAdapter.VH>() {
    
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: View = view.findViewById(R.id.card_ficha)
        val nomePaciente: TextView = view.findViewById(R.id.tv_nome_paciente)
        val localizacao: TextView = view.findViewById(R.id.tv_localizacao)
        val dataConsulta: TextView = view.findViewById(R.id.tv_data_consulta)
        val intensidadeDor: TextView = view.findViewById(R.id.tv_intensidade_dor)
        val status: TextView = view.findViewById(R.id.tv_status)
        val iconeStatus: ImageView = view.findViewById(R.id.iv_status_icon)
        val btnVisualizar: Button = view.findViewById(R.id.btn_visualizar)
        val btnEditar: Button = view.findViewById(R.id.btn_editar)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ficha_paciente, parent, false)
        return VH(view)
    }
    
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = itens[position]
        
        holder.nomePaciente.text = item.paciente
        holder.localizacao.text = "📍 ${item.local}"
        holder.dataConsulta.text = "📅 ${item.data}"
        
        // Intensidade da dor com cor
        val dorText = when (item.intensidadeDor) {
            0 -> "🟢 Sem dor"
            1, 2, 3 -> "🟡 Dor leve ($item.intensidadeDor/10)"
            4, 5, 6 -> "🟠 Dor moderada ($item.intensidadeDor/10)"
            7, 8, 9 -> "🔴 Dor intensa ($item.intensidadeDor/10)"
            10 -> "⚫ Dor insuportável (10/10)"
            else -> "Dor: $item.intensidadeDor/10"
        }
        holder.intensidadeDor.text = dorText
        
        // Status com cor
        holder.status.text = item.status
        when (item.status) {
            "Agendado" -> {
                holder.iconeStatus.setImageResource(android.R.drawable.ic_menu_my_calendar)
                holder.status.setTextColor(android.graphics.Color.BLUE)
            }
            "Realizado" -> {
                holder.iconeStatus.setImageResource(android.R.drawable.ic_menu_edit)
                holder.status.setTextColor(android.graphics.Color.GREEN)
            }
            "Cancelado" -> {
                holder.iconeStatus.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                holder.status.setTextColor(android.graphics.Color.RED)
            }
        }
        
        holder.btnVisualizar.setOnClickListener { onItemClick(item) }
        holder.btnEditar.setOnClickListener { onEditClick(item) }
    }
    
    override fun getItemCount(): Int = itens.size
}

class MinhasFichasFragment : Fragment() {
    private lateinit var controller: AnamneseController
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: FichasAdapter
    private lateinit var tvContador: TextView
    private lateinit var tvEmpty: TextView
    private var todasFichas: List<FichaResumo> = emptyList()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_minhas_fichas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        controller = AnamneseController(requireContext())
        recycler = view.findViewById(R.id.recycler_fichas)
        tvContador = view.findViewById(R.id.tv_contador)
        tvEmpty = view.findViewById(R.id.tv_empty)
        
        recycler.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = FichasAdapter(
            itens = emptyList(),
            onItemClick = { ficha -> visualizarFicha(ficha) },
            onEditClick = { ficha -> editarFicha(ficha) }
        )
        recycler.adapter = adapter
        
        configurarFiltros(view)
        carregarFichas()
    }
    
    private fun configurarFiltros(view: View) {
        val btnTodos = view.findViewById<Button>(R.id.btn_filtrar_todos)
        val btnAgendados = view.findViewById<Button>(R.id.btn_filtrar_agendados)
        val btnRealizados = view.findViewById<Button>(R.id.btn_filtrar_realizados)
        
        btnTodos.setOnClickListener { filtrarFichas("todos") }
        btnAgendados.setOnClickListener { filtrarFichas("agendados") }
        btnRealizados.setOnClickListener { filtrarFichas("realizados") }
    }
    
    private fun filtrarFichas(filtro: String) {
        val fichasFiltradas = when (filtro) {
            "agendados" -> todasFichas.filter { it.status == "Agendado" }
            "realizados" -> todasFichas.filter { it.status == "Realizado" }
            else -> todasFichas
        }
        
        atualizarLista(fichasFiltradas)
    }
    
    private fun atualizarLista(fichas: List<FichaResumo>) {
        adapter = FichasAdapter(
            itens = fichas,
            onItemClick = { ficha -> visualizarFicha(ficha) },
            onEditClick = { ficha -> editarFicha(ficha) }
        )
        recycler.adapter = adapter
        
        tvContador.text = "${fichas.size} fichas"
        tvEmpty.visibility = if (fichas.isEmpty()) View.VISIBLE else View.GONE
    }
    
    private fun carregarFichas() {
        val anamneses = controller.listar()
        todasFichas = anamneses.map { anamnese ->
            val json = try {
                JSONObject(anamnese.dadosJson)
            } catch (e: Exception) {
                JSONObject()
            }
            
            FichaResumo(
                id = anamnese.id ?: 0L,
                paciente = anamnese.nomeCompleto,
                local = anamnese.localizacao ?: "Não informado",
                data = anamnese.dataConsulta ?: "Não informado",
                intensidadeDor = json.optInt("intensidadeDor", 0),
                status = "Agendado" // Por enquanto, todos como agendado
            )
        }
        
        atualizarLista(todasFichas)
    }
    
    private fun visualizarFicha(ficha: FichaResumo) {
        val anamnese = controller.obter(ficha.id)
        if (anamnese != null) {
            val intent = Intent(requireContext(), AnamnesePreviewActivity::class.java)
            intent.putExtra("anamnese_json", anamnese.dadosJson)
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Erro ao carregar ficha", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun editarFicha(ficha: FichaResumo) {
        val intent = Intent(requireContext(), com.example.healplusapp.features.anamnese.ui.AnamneseFormActivity::class.java)
        intent.putExtra("id", ficha.id)
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        carregarFichas()
    }
}

