package fr.azodox.gtb.test

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

fun main() {
    val executor = Executors.newSingleThreadScheduledExecutor()
    executor.scheduleAtFixedRate(
        LerpRunnable(),
        0, 1, TimeUnit.SECONDS
    )
}

class LerpRunnable : Runnable {
    private var countdown = 50
    private val baseCountdown = countdown

    override fun run() {
        if(countdown < 0){
            exitProcess(0)
        }
        println(lerp(1.0f, 0.0f, countdown.toFloat() / baseCountdown.toFloat()))
        countdown--
    }

}

fun lerp(a: Float, b: Float, f: Float): Float {
    return a + f * (b - a)
}