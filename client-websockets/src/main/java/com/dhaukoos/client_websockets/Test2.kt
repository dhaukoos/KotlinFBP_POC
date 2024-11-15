package com.dhaukoos.client_websockets

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val client = HttpClient(CIO) {
        install(WebSockets)
    }

    client.webSocket("ws://localhost:9002/ws") {
        send(Frame.Text("Hello, server!"))
        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> println(frame.readText())
                is Frame.Binary -> TODO()
                is Frame.Close -> TODO()
                is Frame.Ping -> TODO()
                is Frame.Pong -> TODO()
            }
        }
    }
}