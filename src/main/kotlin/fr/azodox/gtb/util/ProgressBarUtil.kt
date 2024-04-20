package fr.azodox.gtb.util

object ProgressBarUtil {

    fun getProgressBar(
        current: Double, max: Double, totalBars: Int, symbol: Char, completedColor: String, notCompletedColor: String
    ): String {
        val percent = current.toFloat() / max
        val progressBars = (totalBars * percent).toInt()

        return "${"$completedColor$symbol".repeat(progressBars)}${"$notCompletedColor$symbol".repeat(totalBars - progressBars)}"
    }
}