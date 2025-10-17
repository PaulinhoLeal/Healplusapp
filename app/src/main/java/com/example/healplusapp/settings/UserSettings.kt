package com.example.healplusapp.settings

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import java.util.Locale

class UserSettings(private val context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

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

    fun applyToActivity(activity: android.app.Activity) {
        // Atenção: aplicarOverrideConfiguration depois que resources já foi acessado causa crash.
        // Aqui aplicamos apenas o Dark Mode. Idioma e fonte devem ser aplicados cedo na Activity (antes de setContentView).
        applyDarkMode(prefs.getBoolean(KEY_DARK_MODE, false))
    }

    // Chamar o mais cedo possível em Activity.onCreate, ANTES de setContentView
    fun applyEarlyInActivity(activity: android.app.Activity) {
        applyDarkMode(prefs.getBoolean(KEY_DARK_MODE, false))
        applyFontScale(activity, prefs.getFloat(KEY_FONT_SCALE, 1.0f))
        applyLanguage(activity, prefs.getString(KEY_LANGUAGE, Locale.getDefault().toLanguageTag()) ?: "pt-BR")
    }

    private fun applyDarkMode(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun applyFontScale(activity: android.app.Activity, scale: Float) {
        val config = Configuration(activity.resources.configuration)
        config.fontScale = scale.coerceIn(0.8f, 1.6f)
        activity.applyOverrideConfiguration(config)
    }

    private fun applyLanguage(activity: android.app.Activity, langTag: String) {
        val locale = Locale.forLanguageTag(langTag)
        Locale.setDefault(locale)
        val config = Configuration(activity.resources.configuration)

        // Define o local da nova configuração de forma compatível com diferentes versões do Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            config.setLocales(android.os.LocaleList(locale))
        } else {
            // Suprime o aviso de deprecation para setLocale, que é necessário para APIs mais antigas
            @Suppress("DEPRECATION")
            config.setLocale(locale)
        }

        // Usa o método moderno para aplicar a configuração na atividade
        activity.applyOverrideConfiguration(config)
    }

    companion object {
        private const val KEY_DARK_MODE = "pref_dark_mode"
        private const val KEY_HIGH_CONTRAST = "pref_high_contrast"
        private const val KEY_FONT_SCALE = "pref_font_scale"
        private const val KEY_LANGUAGE = "pref_language"
    }
}

