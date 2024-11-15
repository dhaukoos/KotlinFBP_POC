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

    val session = client.webSocket("ws://localhost:9002/ws") {
        sender(this)
        receive(this)
    }
}

suspend fun sender(session: WebSocketSession) {
    val text = "Hello, server!"
    println("Client sending $text")
    session.send(Frame.Text(text))
}

suspend fun receive(session: WebSocketSession) {
    for (frame in session.incoming) {
        when (frame) {
            is Frame.Text -> {
                val text = frame.readText()
                println("Client receiving $text")
            }
            is Frame.Binary -> TODO()
            is Frame.Close -> TODO()
            is Frame.Ping -> TODO()
            is Frame.Pong -> TODO()
        }
    }
}