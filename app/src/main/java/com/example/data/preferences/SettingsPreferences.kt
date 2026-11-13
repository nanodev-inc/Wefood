package com.example.data.preferences

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val label: String) {
  SYSTEM("System"),
  LIGHT("Light"),
  DARK("Dark"),
}

data class AppSettings(
  val themeMode: ThemeMode = ThemeMode.LIGHT,
  val defaultVegOnly: Boolean = false,
  val orderAlertsEnabled: Boolean = true,
)

class SettingsPreferences(context: Context) {
  private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  private val _settings = MutableStateFlow(load())
  val settings: StateFlow<AppSettings> = _settings.asStateFlow()

  private fun load(): AppSettings {
    val modeName = prefs.getString(KEY_THEME_MODE, ThemeMode.LIGHT.name)
    val themeMode = ThemeMode.entries.firstOrNull { it.name == modeName } ?: ThemeMode.LIGHT
    return AppSettings(
      themeMode = themeMode,
      defaultVegOnly = prefs.getBoolean(KEY_DEFAULT_VEG, false),
      orderAlertsEnabled = prefs.getBoolean(KEY_ORDER_ALERTS, true),
    )
  }

  fun setThemeMode(mode: ThemeMode) {
    prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    _settings.value = _settings.value.copy(themeMode = mode)
  }

  fun setDefaultVegOnly(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_DEFAULT_VEG, enabled).apply()
    _settings.value = _settings.value.copy(defaultVegOnly = enabled)
  }

  fun setOrderAlertsEnabled(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_ORDER_ALERTS, enabled).apply()
    _settings.value = _settings.value.copy(orderAlertsEnabled = enabled)
  }

  private companion object {
    const val PREFS_NAME = "cravecart_settings"
    const val KEY_THEME_MODE = "theme_mode"
    const val KEY_DEFAULT_VEG = "default_veg_only"
    const val KEY_ORDER_ALERTS = "order_alerts_enabled"
  }
}
