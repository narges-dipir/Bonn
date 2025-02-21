package de.app.bonn

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform