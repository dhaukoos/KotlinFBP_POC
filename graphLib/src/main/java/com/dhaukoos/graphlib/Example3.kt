package com.dhaukoos.graphlib

import com.dhaukoos.graphlib.Node.Companion.twoInputAndProcessor
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.math.sqrt

fun main()
{
    println("Hello World")
    HelloKotlinFBP3()
}

/*
* A simple FBP example with a graph representing the Pythagorean
* formula with two emitter nodes (type int->), two squared nodes (int->int)
* a dual input and single output addition node (int=>int),
* a single input and single output square root node (int->double)
* and a single input receiver node (->double).
*/

fun HelloKotlinFBP3() = runBlocking<Unit> {
    val chInA = Channel<Int>(2)
    val chInB = Channel<Int>(2)
    val chAtoPlus = Channel<Int>(2)
    val chBtoPlus = Channel<Int>(2)
    val chPlusToSqrt = Channel<Int>(2)
    val channelOutC = Channel<Double>(2)

    val delaySeconds = 2

    fun squared(
        inChannel: ReceiveChannel<Int>,
        outChannel: SendChannel<Int>,
    ): Job {
        return Node.gsProcessor(inChannel, outChannel) { a -> a * a }
    }

    fun squareRoot(
        inChannel: ReceiveChannel<Int>,
        outChannel: SendChannel<Double>,
    ): Job {
        return Node.gsProcessor(inChannel, outChannel) { a -> sqrt(a.toDouble()) }
    }

    fun addition(
        inChannel1: ReceiveChannel<Int>,
        inChannel2: ReceiveChannel<Int>,
        outChannel: SendChannel<Int>,
    ): Job {
        return Node.gs2inProcessor(inChannel1, inChannel2, outChannel) { a, b -> a + b  }
    }

    val emitJob1 = Node.gsEmitter(chInA)
    val emitJob2 = Node.gsEmitter(chInB)
    val job3 = squared(chInA, chAtoPlus)
    val job4 = squared(chInB, chBtoPlus)
    val job5 = addition(chAtoPlus, chBtoPlus, chPlusToSqrt)
    val job6 = squareRoot(chPlusToSqrt, channelOutC)
    val receiveJob = Node.gsReceiver(channelOutC) { d -> println("Received here $d") }

    delay(200)
    println("Canceling received Jobs")

    coroutineContext.cancelChildren() // cancel all children to let main finish
}

