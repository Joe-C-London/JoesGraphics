package com.joecollins.graphics.utils

import java.awt.Color
import kotlin.math.pow

object ColorUtils {
    @JvmStatic fun lighten(color: Color): Color {
        return Color(
            128 + color.red / 2, 128 + color.green / 2, 128 + color.blue / 2
        )
    }

    @JvmStatic fun foregroundToContrast(color: Color): Color {
        val toLum = { f: Double -> if (f <= 0.03928) f / 12.92 else ((f + 0.055) / 1.055).pow(2.4) }
        val r = toLum(color.red / 255.0)
        val g = toLum(color.green / 255.0)
        val b = toLum(color.blue / 255.0)
        val l = 0.2126 * r + 0.7152 * g + 0.0722 * b
        return if (l > 0.5) Color.BLACK else Color.WHITE
    }
}
