package de.app.bonn.android.di

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SharedPreferencesHelper {
    private lateinit var prefs: SharedPreferences
    private var initialized = false

    private val _stringFlows: MutableMap<String, MutableStateFlow<String?>> = mutableMapOf()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        _stringFlows[key]?.value = prefs.getString(key, null)
    }

    fun init(context: Context) {
        prefs = context.getSharedPreferences("bunn_prefs", Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(listener)
        initialized = true
    }

    fun ensureInitialized(context: Context) {
        if (!initialized) {
            init(context.applicationContext)
        }
    }

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).commit()
        _stringFlows[key]?.value = value
    }

    fun getString(key: String, default: String? = null): String? {
        return prefs.getString(key, default)
    }
    fun getStringFlow(key: String, default: String? = null): StateFlow<String?> {
        return _stringFlows.getOrPut(key) {
            MutableStateFlow(prefs.getString(key, default))
        }
    }

    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).commit()
    }

    fun getInt(key: String, default: Int = 0): Int {
        return prefs.getInt(key, default)
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).commit()
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return prefs.getBoolean(key, default)
    }

    fun remove(key: String) {
        prefs.edit().remove(key).commit()
    }

    fun clear() {
        prefs.edit().clear().commit()
        _stringFlows.keys.forEach { key -> _stringFlows[key]?.value = null }
    }
}


