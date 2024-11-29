package com.dhaukoos.client_websockets

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main() = runBlocking {

    val channelPairInt = Channel<Pair<Int, Int>>(2)
    val channelFrame = Channel<Frame>(2)
    val emitJob1 = myGsPairEmitter(channelPairInt)
    val IntPairToFrameJob = myGsProcessor<Pair<Int, Int>, Frame>(channelPairInt, channelFrame) {
        i -> Frame.Text(i.toString())
    }

    val senderJob = senderProcess2(
        9002,
        "localhost",
        "/ws",
        channelFrame
    )

}

fun <T> myGsPairEmitter(
    outChannel: SendChannel<T>,
): Job {
    return GlobalScope.launch {
        for (i in 1..5) {
            val sides = Pair(i, i)
            outChannel.send(sides as T)
            println("Emitting ${sides.toList()}")
        }
    }
}

suspend fun senderProcess2(
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
