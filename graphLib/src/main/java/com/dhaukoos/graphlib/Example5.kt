package com.dhaukoos.graphlib

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.math.sqrt

fun main()
{
    println("Hello World")
    HelloKotlinFBP5()
}

/*
* A simple FBP example with a subGraph representing the Pythagorean
* formula. The differences from the previous graph include a single emitter
* node that produces a Pair data object (as opposed to a simple data type),
* and a single input, dual output splitter node (Pair(int)=>int).
*/

fun HelloKotlinFBP5() = runBlocking<Unit> {
    val chGraphInAB = Channel<Pair<Int, Int>>(2)

    val chGraphOutC = Channel<Double>(2)

    val delaySeconds = 2

    fun pythagoreanTheoremGraph(
        inChannel1: Channel<Pair<Int, Int>>,
        outChannel: Channel<Double>,
    ): Job {
        return GlobalScope.launch {
            Node.oneInputOneOutputGraph(inChannel1, outChannel) {
                val chInAB = Channel<Pair<Int, Int>>(2)
                val chA = Channel<Int>(2)
                val chB = Channel<Int>(2)
                val chAtoPlus = Channel<Int>(2)
                val chBtoPlus = Channel<Int>(2)
                val chPlusToSqrt = Channel<Int>(2)
                val chOutC = Channel<Double>(2)
                fun squared(
                    inChannel: ReceiveChannel<Int>,
                    outChannel: SendChannel<Int>,
                ): Job {
                    return Node.scopedProcessor(GlobalScope, inChannel, outChannel) { a -> a * a }
                }
                fun squareRoot(
                    inChannel: ReceiveChannel<Int>,
                    outChannel: SendChannel<Double>,
                ): Job {
                    return Node.scopedProcessor(GlobalScope, inChannel, outChannel) { a -> sqrt(a.toDouble()) }
                }
                fun addition(
                    inChannel1: ReceiveChannel<Int>,
                    inChannel2: ReceiveChannel<Int>,
                    outChannel: SendChannel<Int>,
                ): Job {
                    return GlobalScope.launch {
                        Node.twoInputAndProcessor(inChannel1, inChannel2, outChannel) { a, b -> a + b }
                    }
                }
                val job1 = Node.gsPassThruPort(inChannel1, chInAB)
                val job2 = Node.gsPairSplitter(chInAB, chA, chB)
                val job3 = squared(chA, chAtoPlus)
                val job4 = squared(chB, chBtoPlus)
                val job5 = addition(chAtoPlus, chBtoPlus, chPlusToSqrt)
                val job6 = squareRoot(chPlusToSqrt, chOutC)
                val job7 = Node.gsPassThruPort(chOutC, outChannel)
            }
        }
    }

    // input port
    val emitJob1 = Node.gsPairEmitter(chGraphInAB)

    val graphJob = pythagoreanTheoremGraph(
        chGraphInAB,
        chGraphOutC
    )

    // output port
    val receiveJob = Node.gsReceiver(chGraphOutC) { d -> println("Final result $d") }

    delay(300)

    println("Canceling received Jobs")

    coroutineContext.cancelChildren() // cancel all children to let main finish
}