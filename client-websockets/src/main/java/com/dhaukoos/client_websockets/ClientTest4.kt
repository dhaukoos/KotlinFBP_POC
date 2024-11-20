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
    val client = HttpClient(CIO) {
        install(WebSockets)
    }

    val channel1 = Channel<Int>(2)
    val channel2 = Channel<Frame>(2)
    val emitJob1 = myGsEmitter(channel1)
    val IntToFrameJob = myGsProcessor<Int, Frame>(channel1, channel2) { i -> Frame.Text(i.toString()) }

    val session = createSendingWebSocketSession(client, "ws://localhost:9002/ws", channel2)
}

suspend fun createSendingWebSocketSession(
    client: HttpClient,
    urlString: String,
    channel: Channel<Frame>
): DefaultClientWebSocketSession {

    val session = client.webSocketSession(urlString) // Open a websocket connection

    // ... use the 'session' object to send and receive data
    senderChannel(session, channel)
    receiver(session)

    return session
}

suspend fun senderChannel(session: WebSocketSession, channel: Channel<Frame>) {
    //session.send(Frame.Text("Hello, server!"))
    for (frame in channel) {
        when (frame) {
            is Frame.Text -> {
                println("Client sending ${frame.readText()}")
                session.send(frame)
            }
            is Frame.Binary -> session.send(frame)
            is Frame.Close -> session.send(frame)
            is Frame.Ping -> session.send(frame)
            is Frame.Pong -> session.send(frame)
        }
    }
}

suspend fun receiver(session: WebSocketSession) {
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

@OptIn(DelicateCoroutinesApi::class)
fun <T> myGsEmitter(
    outChannel: SendChannel<T>,
): Job {
    return GlobalScope.launch {
        for (i in 1..5) {
            outChannel.send(i as T)
            println("Emitting $i")
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun <T, U> myGsProcessor(
    inChannel: ReceiveChannel<T>,
    outChannel: SendChannel<U>,
    process: (T) -> U
): Job {
    return GlobalScope.launch {
        inChannel.consumeEach { input ->
            val output = process(input)
            println("Processing $input ==> $output")
            outChannel.send(output)
        }
    }
}