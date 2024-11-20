package com.dhaukoos.client_websockets

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {

    val channelInt = Channel<Int>(2)
    val channelFrame = Channel<Frame>(2)
    val emitJob1 = myGsEmitter(channelInt)
    val IntToFrameJob = myGsProcessor<Int, Frame>(channelInt, channelFrame) { i -> Frame.Text(i.toString()) }

    val senderJob = senderProcess(
        9002,
        "localhost",
        "/ws",
        channelFrame
    )

}

suspend fun senderProcess(
    port: Int,
    host: String,
    path: String,
    inChannel: Channel<Frame>
) {
    val client = HttpClient(CIO) {
        install(WebSockets)
    }
    val urlString = "ws://$host:$port$path"
    val session = createSendingWebSocketSession(client, urlString, inChannel)
}
