package com.dhaukoos.graphlib

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

fun main()  {
    println("Hello World")
    HelloKotlinFBP2()
}

/*
* A minimalist FBP example with a single output emitter node (type int->),
* a dual input and single output processor node (int=>int),
* and a single input receiver node (->int).
*/

fun HelloKotlinFBP2()  = runBlocking<Unit>{
    val channel1 = Channel<Int>(2)
    val channel2 = Channel<Int>(2)
    val channel3 = Channel<Int>(2)

    val delaySeconds = 2
    val uiScope = MainScope()

    val emitJob1 = Node.gsEmitter(channel1)
    val emitJob2 = Node.gsEmitter(channel2)
    val processJob = Node.gs2inProcessor(channel1, channel2, channel3) { a, b -> a + b}
    val receiveJob = Node.gsReceiver(channel3) { i -> println("Received here $i") }

    delay(200)
    println("Canceling sendJob")
    //  emitJob.cancel()
    println("Canceling processJob")
    //  processJob.cancel()
    println("Canceling receivedJob")
    // receiveJob.cancel()

    coroutineContext.cancelChildren() // cancel all children to let main finish
}

