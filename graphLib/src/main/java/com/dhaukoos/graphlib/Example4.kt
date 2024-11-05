package com.dhaukoos.graphlib

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.math.sqrt

fun main()
{
    println("Hello World")
    HelloKotlinFBP4()
}

/*
* A simple FBP example with a subGraph representing the Pythagorean
* formula. The non-io nodes of the previous graph are encapsulated
* in a subGraph function, with passThru nodes providing the ports
* into and out of the subGraph.
*/

fun HelloKotlinFBP4() = runBlocking<Unit> {
    val chGraphInA = Channel<Int>(2)
    val chGraphInB = Channel<Int>(2)

    val chGraphOutC = Channel<Double>(2)

    val delaySeconds = 2

    fun pythagoreanTheoremGraph(
        inChannel1: Channel<Int>,
        inChannel2: Channel<Int>,
        outChannel: Channel<Double>,
    ): Job {
        return GlobalScope.launch {
            Node.twoInputOneOutputGraph(inChannel1, inChannel2, outChannel) {
                val chInA = Channel<Int>(2)
                val chInB = Channel<Int>(2)
                val chAtoPlus = Channel<Int>(2)
                val chBtoPlus = Channel<Int>(2)
                val chPlusToSqrt = Channel<Int>(2)
                val chOutC = Channel<Double>(2)
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
                    return Node.gs2inProcessor(inChannel1, inChannel2, outChannel) { a, b -> a + b }
                }
                val job1 = Node.gsPassThruPort(inChannel1, chInA)
                val job2 = Node.gsPassThruPort(inChannel2, chInB)
                val job3 = squared(chInA, chAtoPlus)
                val job4 = squared(chInB, chBtoPlus)
                val job5 = addition(chAtoPlus, chBtoPlus, chPlusToSqrt)
                val job6 = squareRoot(chPlusToSqrt, chOutC)
                val job7 = Node.gsPassThruPort(chOutC, outChannel)
            }
        }
    }

    // input ports
    val emitJob1 = Node.gsEmitter(chGraphInA)
    val emitJob2 = Node.gsEmitter(chGraphInB)

    val graphJob = pythagoreanTheoremGraph(
                        chGraphInA,
                        chGraphInB,
                        chGraphOutC
                    )

    // output port
    val receiveJob = Node.gsReceiver(chGraphOutC) { d -> println("Final result $d") }

    delay(300)

    println("Canceling received Jobs")

    coroutineContext.cancelChildren() // cancel all children to let main finish
}

