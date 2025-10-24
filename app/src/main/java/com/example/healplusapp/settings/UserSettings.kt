package com.example.healplusapp.settings

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import java.util.Locale

class UserSettings(private val context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    
    fun getSharedPreferences() = prefs

    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        applyDarkMode(enabled)
    }

    fun setHighContrastEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
    }

    fun setFontScale(scale: Float) {
        prefs.edit().putFloat(KEY_FONT_SCALE, scale).apply()
    }

    fun setLanguage(langTag: String) {
        prefs.edit().putString(KEY_LANGUAGE, langTag).apply()
    }

    /** 🔹 Aplica tema escuro imediatamente **/
    private fun applyDarkMode(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    /** 🔹 Cria um novo contexto configurado com idioma e escala **/
    fun applyToContext(base: Context): Context {
        val config = Configuration(base.resources.configuration)

        // Aplica escala de fonte
        val scale = prefs.getFloat(KEY_FONT_SCALE, 1.0f)
        config.fontScale = scale.coerceIn(0.8f, 1.6f)

        // Aplica idioma
        val langTag = prefs.getString(KEY_LANGUAGE, Locale.getDefault().toLanguageTag()) ?: "pt-BR"
        val locale = Locale.forLanguageTag(langTag)
        Locale.setDefault(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            config.setLocales(android.os.LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.setLocale(locale)
        }

        // Retorna um novo contexto configurado
        return base.createConfigurationContext(config)
    }

    /** 🔹 Chamada segura para recriar a Activity após salvar preferências **/
    fun applyToActivity(activity: Activity) {
        // Reaplica o tema noturno (pode ser feito em tempo real)
        applyDarkMode(prefs.getBoolean(KEY_DARK_MODE, false))
        // Recria a Activity para aplicar idioma e escala de fonte
        activity.recreate()
    }

    companion object {
        private const val KEY_DARK_MODE = "pref_dark_mode"
        private const val KEY_HIGH_CONTRAST = "pref_high_contrast"
        private const val KEY_FONT_SCALE = "pref_font_scale"
        private const val KEY_LANGUAGE = "pref_language"
    }
}