package com.joecollins.graphics.utils

import java.awt.Color
import kotlin.math.pow
import kotlin.math.roundToInt

object ColorUtils {
    fun lighten(color: Color): Color {
        return Color(
            128 + color.red / 2,
            128 + color.green / 2,
            128 + color.blue / 2,
        )
    }

    fun foregroundToContrast(color: Color): Color {
        val l = calcLum(color)
        return if (l > 0.75) Color.BLACK else Color.WHITE
    }

    fun contrastForBackground(color: Color): Color {
        val l = calcLum(color)
        val factor = (0.75 / l).coerceAtMost(1.0)
        return Color(
            (color.red * factor).roundToInt(),
            (color.green * factor).roundToInt(),
            (color.blue * factor).roundToInt(),
            color.alpha,
        )
    }

    private fun calcLum(color: Color): Double {
        val toLum = { f: Double -> if (f <= 0.03928) f / 12.92 else ((f + 0.055) / 1.055).pow(2.4) }
        val r = toLum(color.red / 255.0)
        val g = toLum(color.green / 255.0)
        val b = toLum(color.blue / 255.0)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }
}
