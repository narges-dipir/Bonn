package de.app.bonn.android.di

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalTimeProvider @Inject constructor() {

    private val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)

    fun now(): String {
        return LocalTime.now().format(formatter)
    }
}