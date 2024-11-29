package com.dhaukoos.graphlib

import com.dhaukoos.graphlib.Node.Companion.convertFrameTextToString
import com.dhaukoos.graphlib.Node.Companion.myGsProcessor
import com.dhaukoos.graphlib.Node.Companion.myGsReceiver
import com.dhaukoos.graphlib.Node.Companion.mySessionHandler
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.Channel
import kotlin.time.Duration.Companion.seconds

fun main() {

    val channelString = Channel<String>(2)
    val channelFrame = Channel<Frame>(2)
    val FrameToStringJob = myGsProcessor<Frame, String>(channelFrame, channelString) {
            frame -> convertFrameTextToString(frame)
    }
    val receiveJob = myGsReceiver(channelString) { i -> println("Received here $i") }

    val myServer = receiverProcess2(
        9002,
        "0.0.0.0",
        "/ws",
        channelFrame
    )
}

fun receiverProcess2(
    port: Int,
    host: String,
    path: String,
    outChannel: Channel<Frame>
): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
    return embeddedServer(Netty, port, host) {
        routing {
            install(WebSockets) {
                pingPeriod = 15.seconds
                timeout = 15.seconds
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }
            webSocket(path = path) {
                mySessionHandler (incoming, outChannel)
            }
        }
    }.start(wait = true)
}
