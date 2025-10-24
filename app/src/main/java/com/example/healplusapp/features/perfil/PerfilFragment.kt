package com.example.healplusapp.features.perfil

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.healplusapp.R
import com.example.healplusapp.settings.UserSettings
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class PerfilFragment : Fragment() {

    private lateinit var settings: UserSettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // inicializa aqui
        settings = UserSettings(requireContext())

        Snackbar.make(view, getString(R.string.menu_perfil), Snackbar.LENGTH_SHORT).show()

        val switchDark = view.findViewById<Switch>(R.id.switch_dark_mode)
        val switchContrast = view.findViewById<Switch>(R.id.switch_high_contrast)
        val seekFont = view.findViewById<SeekBar>(R.id.seek_font_scale)
        val textFont = view.findViewById<TextView>(R.id.text_font_scale)
        val spinnerLang = view.findViewById<Spinner>(R.id.spinner_language)
        val buttonSave = view.findViewById<Button>(R.id.button_save_prefs)

        val langs = listOf("pt-BR", "en-US", "es-ES")
        spinnerLang.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, langs)

        seekFont.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val scale = progress / 10f
                textFont.text = "Tamanho da fonte: ${String.format(Locale.getDefault(), "%.1fx", scale)}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        buttonSave.setOnClickListener {
            val scale = seekFont.progress / 10f
            settings.setDarkModeEnabled(switchDark.isChecked)
            settings.setHighContrastEnabled(switchContrast.isChecked)
            settings.setFontScale(scale)
            settings.setLanguage(spinnerLang.selectedItem as String)

            // 🔹 recria a Activity para aplicar idioma e escala
            settings.applyToActivity(requireActivity())
            Snackbar.make(view, "Preferências salvas", Snackbar.LENGTH_SHORT).show()
        }
    }
}
