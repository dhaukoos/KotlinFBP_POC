package com.dhaukoos.server_websockets

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.time.Duration.Companion.seconds

fun main() {

    suspend fun mySessionHandler (incoming: ReceiveChannel<Frame>, outgoing: SendChannel<Frame>,)  {
        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    outgoing.send(Frame.Text("you said: $text"))
                    println("Server receiving: $text")
                }
                is Frame.Binary -> TODO()
                is Frame.Close -> TODO()
                is Frame.Ping -> TODO()
                is Frame.Pong -> TODO()
            }
        }
    }

    val myServer = embeddedServer(Netty, port = 9002) {
        routing {
            install(WebSockets) {
                pingPeriod = 15.seconds
                timeout = 15.seconds
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }
            webSocket("/ws") {
                mySessionHandler(incoming, outgoing)
            }
        }
    }.start(wait = true)


}

