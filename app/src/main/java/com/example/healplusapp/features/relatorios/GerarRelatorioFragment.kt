package com.example.healplusapp.features.relatorios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.healplusapp.R
import android.widget.Button
import android.widget.ImageView
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.widget.Toast

class GerarRelatorioFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gerar_relatorio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnImg = view.findViewById<Button>(R.id.btn_select_image)
        val imgPreview = view.findViewById<ImageView>(R.id.image_preview)
        val btnPdf = view.findViewById<Button>(R.id.btn_generate_pdf)

        btnImg.setOnClickListener {
            // Placeholder: gera uma imagem dummy
            val bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.LTGRAY)
            imgPreview.setImageBitmap(bitmap)
        }

        btnPdf.setOnClickListener {
            try {
                val document = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = document.startPage(pageInfo)
                page.canvas.drawColor(Color.WHITE)
                // Conteúdo do PDF (placeholder)
                page.canvas.drawText("Relatório da Ferida - Placeholder", 50f, 50f, android.graphics.Paint().apply { textSize = 16f })
                document.finishPage(page)

                val file = java.io.File(requireContext().cacheDir, "relatorio_placeholder.pdf")
                file.outputStream().use { document.writeTo(it) }
                document.close()
                Toast.makeText(requireContext(), "PDF gerado em ${'$'}{file.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao gerar PDF: ${'$'}{e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

