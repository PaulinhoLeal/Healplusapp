package com.example.healplusapp.features.relatorios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.healplusapp.R
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import java.io.InputStream

class CompararRelatoriosFragment : Fragment() {
    private var image1Uri: Uri? = null
    private var image2Uri: Uri? = null

    private val pickImg1 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        image1Uri = uri
        view?.findViewById<ImageView>(R.id.img1)?.setImageURI(uri)
    }
    private val pickImg2 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        image2Uri = uri
        view?.findViewById<ImageView>(R.id.img2)?.setImageURI(uri)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_comparar_relatorios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etReport1 = view.findViewById<EditText>(R.id.et_report1)
        val etReport2 = view.findViewById<EditText>(R.id.et_report2)
        val etDate1 = view.findViewById<EditText>(R.id.et_report1_date)
        val etDate2 = view.findViewById<EditText>(R.id.et_report2_date)
        val btnPick1 = view.findViewById<Button>(R.id.btn_pick_img1)
        val btnPick2 = view.findViewById<Button>(R.id.btn_pick_img2)
        val btnCompare = view.findViewById<Button>(R.id.btn_compare)
        val tvResult = view.findViewById<TextView>(R.id.tv_result)

        btnPick1.setOnClickListener { pickImg1.launch("image/*") }
        btnPick2.setOnClickListener { pickImg2.launch("image/*") }

        btnCompare.setOnClickListener {
            val report1 = etReport1.text?.toString() ?: ""
            val report2 = etReport2.text?.toString() ?: ""
            val date1 = etDate1.text?.toString() ?: ""
            val date2 = etDate2.text?.toString() ?: ""

            val img1DataUri = image1Uri?.let { uri -> buildDataUriFromUri(uri) } ?: ""
            val img2DataUri = image2Uri?.let { uri -> buildDataUriFromUri(uri) } ?: ""

            // Placeholder de comparação multimodal: validações simples e síntese textual
            if (report1.isBlank() || report2.isBlank() || img1DataUri.isBlank() || img2DataUri.isBlank()) {
                tvResult.text = "Preencha textos e selecione as duas imagens para comparar."
                return@setOnClickListener
            }

            val summary = StringBuilder()
            summary.appendLine("Comparação de Relatórios de Feridas")
            summary.appendLine("Data 1: ${'$'}date1  |  Data 2: ${'$'}date2")
            summary.appendLine()
            summary.appendLine("Resumo Textual:")
            summary.appendLine("- Relatório 1: ${'$'}{report1.take(180)}...")
            summary.appendLine("- Relatório 2: ${'$'}{report2.take(180)}...")
            summary.appendLine()
            summary.appendLine("Observação: análise de imagem baseada em IA não está ativa neste build. As imagens foram incorporadas como data URIs para futura análise.")

            Log.d("Compare", "report1Date=${'$'}date1, report2Date=${'$'}date2")
            Log.d("Compare", "image1DataUriPrefix=${'$'}{img1DataUri.take(32)} ...")
            Log.d("Compare", "image2DataUriPrefix=${'$'}{img2DataUri.take(32)} ...")

            tvResult.text = summary.toString()
        }
    }

    private fun buildDataUriFromUri(uri: Uri): String {
        val mime = requireContext().contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = readAllBytes(requireContext().contentResolver.openInputStream(uri))
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "data:${'$'}mime;base64,${'$'}b64"
    }

    private fun readAllBytes(input: InputStream?): ByteArray {
        if (input == null) return ByteArray(0)
        return input.use { it.readBytes() }
    }
}

