package de.app.bonn.android.di

import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalTimeProvider @Inject constructor() {

    private val timeZone: ZoneId = ZoneId.systemDefault()
    fun timeZone(): String {
        return timeZone.id
    }
}