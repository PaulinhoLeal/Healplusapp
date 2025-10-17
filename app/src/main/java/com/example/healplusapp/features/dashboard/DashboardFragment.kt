package com.example.healplusapp.features.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.healplusapp.R
import android.content.Intent
import com.example.healplusapp.features.anamnese.ui.AnamneseListActivity

class DashboardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val btn = v.findViewById<Button>(R.id.btn_open_anamnese_list)
        btn?.setOnClickListener {
            startActivity(Intent(requireContext(), AnamneseListActivity::class.java))
        }
        return v
    }
}

