package com.joecollins.graphics.utils

import java.awt.Color

object ColorUtils {
    @JvmStatic fun lighten(color: Color): Color {
        return Color(
                128 + color.red / 2, 128 + color.green / 2, 128 + color.blue / 2)
    }
}
