package com.example.healplusapp.features.anamnese

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.healplusapp.R
import android.widget.TextView

class AnamnesePreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anamnese_preview)
        val tv = findViewById<TextView>(R.id.tv_preview)
        val json = intent.getStringExtra("anamnese_json") ?: "{}"
        tv.text = json
    }
}

