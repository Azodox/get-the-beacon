package fr.azodox.gtb.test

fun main() {
    println(lerp(10.0, 0.0, 0.5))
}

fun lerp(x: Double, y: Double, t: Double): Double {
    return x*(1-t)+y*t
}