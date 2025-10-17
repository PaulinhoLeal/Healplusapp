package com.example.healplusapp.features.fichas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.healplusapp.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

data class FichaResumo(val paciente: String, val local: String, val data: String)

class FichasAdapter(private val itens: List<FichaResumo>) : RecyclerView.Adapter<FichasAdapter.VH>() {
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(android.R.id.text1)
        val subtitle: TextView = view.findViewById(android.R.id.text2)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return VH(view)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = itens[position]
        holder.title.text = "${'$'}{item.paciente} — ${'$'}{item.local}"
        holder.subtitle.text = item.data
    }
    override fun getItemCount(): Int = itens.size
}

class MinhasFichasFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_minhas_fichas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_fichas)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        val dados = listOf(
            FichaResumo("Ana Lima", "Perna Direita", "10/10/2025"),
            FichaResumo("Carlos Souza", "Calcâneo", "09/10/2025"),
            FichaResumo("Marina Alves", "Joelho", "05/10/2025")
        )
        recycler.adapter = FichasAdapter(dados)
    }
}

