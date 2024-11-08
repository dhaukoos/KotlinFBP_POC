package com.dhaukoos.graphlib

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

/*
* A minimalist FBP example with a single output emitter node (type int->),
* a single input and output processor node (int->int),
* and a single input receiver node (->int).
*/

fun HelloKotlinFBP1() = runBlocking<Unit>{
    val channel1 = Channel<Int>(2)
    val channel2 = Channel<Int>(2)

    val delaySeconds = 2

    val emitJob1 = Node.gsEmitter(channel1)
    val processJob = Node.gsProcessor(channel1, channel2) { i -> i * i }
    val receiveJob = Node.gsReceiver(channel2) { i -> println("Received here $i") }

    delay(200)
    println("Canceling emitJob")
    //  emitJob.cancel()
    println("Canceling processJob")
    //  processJob.cancel()
    println("Canceling receivedJob")
    // receiveJob.cancel()

    coroutineContext.cancelChildren() // cancel all children to let main finish
}
