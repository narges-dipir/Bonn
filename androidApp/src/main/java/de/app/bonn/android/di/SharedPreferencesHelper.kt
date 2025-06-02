package de.app.bonn.android.di

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesHelper {
    private lateinit var prefs: SharedPreferences
    private var initialized = false

    fun init(context: Context) {
        prefs = context.getSharedPreferences("bunn_prefs", Context.MODE_PRIVATE)
        initialized = true
    }

    fun ensureInitialized(context: Context) {
        if (!initialized) {
            init(context.applicationContext)
        }
    }

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).commit() // ← SYNC write
    }

    fun getString(key: String, default: String? = null): String? {
        return prefs.getString(key, default)
    }

    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).commit() // ← SYNC write
    }

    fun getInt(key: String, default: Int = 0): Int {
        return prefs.getInt(key, default)
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).commit() // ← SYNC write
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return prefs.getBoolean(key, default)
    }

    fun remove(key: String) {
        prefs.edit().remove(key).commit() // ← SYNC remove
    }

    fun clear() {
        prefs.edit().clear().commit() // ← SYNC clear
    }
}


