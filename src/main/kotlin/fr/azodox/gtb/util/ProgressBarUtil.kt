package fr.azodox.gtb.util

import com.google.common.base.Strings

object ProgressBarUtil {

    fun getProgressBar(
        current: Double, max: Double, totalBars: Int, symbol: Char, completedColor: String, notCompletedColor: String
    ): String {
        val percent = current.toFloat() / max
        val progressBars = (totalBars * percent).toInt()

        return (Strings.repeat("" + completedColor + symbol, progressBars)
                + Strings.repeat("" + notCompletedColor + symbol, totalBars - progressBars))
    }
}