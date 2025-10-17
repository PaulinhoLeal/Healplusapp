package com.example.healplusapp.features.anamnese.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.healplusapp.R
import com.example.healplusapp.features.anamnese.controller.AnamneseController
import com.example.healplusapp.features.anamnese.model.Anamnese

class AnamneseListActivity : AppCompatActivity() {
    private lateinit var controller: AnamneseController
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var current: List<Anamnese> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anamnese_list)
        controller = AnamneseController(this)

        listView = findViewById(R.id.lv_anamneses)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        findViewById<Button>(R.id.btn_nova_anamnese).setOnClickListener {
            val i = Intent(this, AnamneseFormActivity::class.java)
            startActivity(i)
        }

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val item = current[position]
            val i = Intent(this, AnamneseFormActivity::class.java)
            i.putExtra("id", item.id)
            startActivity(i)
        }

        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            val item = current[position]
            AlertDialog.Builder(this)
                .setTitle("Excluir anamnese")
                .setMessage("Deseja excluir '${item.nomeCompleto}'?")
                .setPositiveButton("Excluir") { _, _ ->
                    controller.excluir(item.id!!)
                    loadData()
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        current = controller.listar()
        val items = current.map { a ->
            val info = listOfNotNull(a.dataConsulta, a.localizacao).joinToString(" • ")
            if (info.isBlank()) a.nomeCompleto else "${a.nomeCompleto} — ${info}"
        }
        adapter.clear()
        adapter.addAll(items)
        adapter.notifyDataSetChanged()
        findViewById<View>(R.id.tv_empty).visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }
}


