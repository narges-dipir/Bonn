package de.app.bonn.shared.network

import io.ktor.client.HttpClient

internal expect fun platformHttpClient(): HttpClient
