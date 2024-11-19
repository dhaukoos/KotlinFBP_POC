package com.dhaukoos.server_websockets

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

fun main() {

    val channelString = Channel<String>(2)
    val channelFrame = Channel<Frame>(2)
    val FrameToIntJob = myGsProcessor2<Frame, String>(channelFrame, channelString) {
        frame -> convertFrameTextToString(frame)
    }
    val receiveJob = myGsReceiver(channelString) { i -> println("Received here $i") }

    val myServer = embeddedServer(Netty, port = 9002) {
        routing {
            install(WebSockets) {
                pingPeriod = 15.seconds
                timeout = 15.seconds
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }
            webSocket("/ws") {
                mySessionHandler (incoming, channelFrame)
            }
        }
    }.start(wait = true)
}

fun convertFrameTextToString(frame: Frame): String {
    when (frame) {
        is Frame.Text -> {
            return frame.readText()
        }
        else -> {return "Could not read text"}
    }
}

suspend fun mySessionHandler (incoming: ReceiveChannel<Frame>, outgoing: SendChannel<Frame>,)  {
    for (frame in incoming) {
        when (frame) {
            is Frame.Text -> {
                val text = frame.readText()
                outgoing.send(Frame.Text(text))
                println("Server receiving: $text")
            }
            is Frame.Binary -> TODO()
            is Frame.Close -> TODO()
            is Frame.Ping -> TODO()
            is Frame.Pong -> TODO()
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun <T, U> myGsProcessor2(
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

@OptIn(DelicateCoroutinesApi::class)
fun <T> myGsReceiver(
    inChannel: ReceiveChannel<T>,
    process: (T) -> Unit
): Job {
    return GlobalScope.launch {
        inChannel.consumeEach { input ->
            process(input)
            println("Node Received $input")
        }
    }
}

