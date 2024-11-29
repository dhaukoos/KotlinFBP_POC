package com.dhaukoos.graphlib

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

class Node {

    companion object {
        private val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        /**
         *  Suspending functions
         *
         */

        // Emitter function: => output only

        suspend fun <T> emitter(
            inputSignal: T,
            sendChannel: SendChannel<T>,
        ) {
            sendChannel.send(inputSignal)
        }

        // Processor functions: input => process => output

        suspend fun <T, U> processor(
            receiveChannel: ReceiveChannel<T>,
            sendChannel: SendChannel<U>,
            process: (T) -> U
        ) {
            receiveChannel.consumeEach { input ->
                val output = process(input)
                println("Processing $input ==> $output")
                sendChannel.send(output)
            }
        }

        suspend fun <T, U, V> twoInputAndProcessor(
            inChannel1: ReceiveChannel<T>,
            inChannel2: ReceiveChannel<U>,
            sendChannel: SendChannel<V>,
            process: (T, U) -> V
        ) {
            while (true) {
                val in1 = inChannel1.receive()
                val in2 = inChannel2.receive()
                if (in1 != null && in2 != null) {
                    val output = process(in1, in2)
                    println("Processing $in1 , $in2 ==> $output")
                    sendChannel.send(output)
                } else if (in1 == null) {
                    println("inChannel1 is empty or closed")
                } else {
                    println("inChannel2 is empty or closed")
                }
            }
        }

        suspend fun <U, V> splitPair(
            inputChannel: ReceiveChannel<Pair<U, V>>,
            outputChannelU: SendChannel<U>,
            outputChannelV: SendChannel<V>
        ) {
            for (pair in inputChannel) {
                outputChannelU.send(pair.first)
                outputChannelV.send(pair.second)
            }
        }

        // Receiver function: input => process only

        suspend fun <T> receiver(
            receiveChannel: ReceiveChannel<T>,
            process: (T) -> Unit
        ) {
            receiveChannel.consumeEach { input ->
                process(input)
                println("Receiving $input")
            }
        }

        // Node graph functions

        suspend fun <T, U> oneInputOneOutputGraph(
            inChannel: Channel<T>,
            outChannel: Channel<U>,
            process: () -> Unit
        ) {
            while (true) {
                process()
            }
        }

        suspend fun <T, U, V> twoInputOneOutputGraph(
            inChannel1: Channel<T>,
            inChannel2: Channel<U>,
            outChannel: Channel<V>,
            process: () -> Unit
        ) {
            while (true) {
                process()
            }
        }

        /**
         *  Coroutine jobs
         *
         */

        // Emitter jobs: => output only

        fun <T> gsDoubleEmitter(
            outChannel: SendChannel<T>,
        ): Job {
            return GlobalScope.launch {
                for (i in 1..4) {
                    val d = i.toDouble()
                    emitter(d as T, outChannel)
                    println("Emitting $i")
                }
            }
        }

        fun <T> scopedEmitter(
            scope: CoroutineScope,
            outChannel: SendChannel<T>,
        ): Job {
            return scope.launch {
                for (i in 1..5) {
                    emitter(i as T, outChannel)
                    println("Emitting $i")
                }
            }
        }

        fun <T> gsEmitter(
            outChannel: SendChannel<T>,
        ): Job {
            return GlobalScope.launch {
                for (i in 1..5) {
                    emitter(i as T, outChannel)
                    println("Emitting $i")
                }
            }
        }

        fun <T> gsPairEmitter(
            outChannel: SendChannel<T>,
        ): Job {
            return GlobalScope.launch {
                for (i in 1..5) {
                    val sides = Pair(i, i)
                    emitter(sides as T, outChannel)
                    println("Emitting ${sides.toList()}")
                }
            }
        }

        // Processor jobs: input => process => output

        fun <T, U> scopedProcessor(
            scope: CoroutineScope,
            inChannel: ReceiveChannel<T>,
            outChannel: SendChannel<U>,
            process: (T) -> U
        ): Job {
            return scope.launch {
                processor(inChannel, outChannel, process)
            }

        }

        fun <T, U> gsProcessor(
            inChannel: ReceiveChannel<T>,
            outChannel: SendChannel<U>,
            process: (T) -> U
        ): Job {
            return GlobalScope.launch {
                processor(inChannel, outChannel, process)
            }
        }

        fun <T, U, V> gs2inProcessor(
            inChannel1: ReceiveChannel<T>,
            inChannel2: ReceiveChannel<U>,
            outChannel: SendChannel<V>,
            process: (T, U) -> V
        ): Job {
            return GlobalScope.launch {
                twoInputAndProcessor(inChannel1, inChannel2, outChannel, process)
            }
        }

        fun <U, V> gsPairSplitter(
            inputChannel: ReceiveChannel<Pair<U, V>>,
            outputChannelU: SendChannel<U>,
            outputChannelV: SendChannel<V>
        ): Job {
            return GlobalScope.launch {
                splitPair(inputChannel, outputChannelU, outputChannelV)
            }
        }

        // PassThrough jobs  input => output

        fun <T> gsPassThruPort(
            inChannel: ReceiveChannel<T>,
            outChannel: SendChannel<T>,
        ): Job {
            return GlobalScope.launch {
                inChannel.consumeEach { i ->
                    emitter(i as T, outChannel)
                    println("Passing through $i")
                }
            }
        }

        // Receiver jobs: input => process only

        fun <T> gsReceiver(
            inChannel: ReceiveChannel<T>,
            process: (T) -> Unit
        ): Job {
            return GlobalScope.launch {
                receiver(inChannel, process)
            }
        }

        // TODO cleanup websockets additions

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

    }
}

