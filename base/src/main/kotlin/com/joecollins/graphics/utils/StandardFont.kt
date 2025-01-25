package com.joecollins.graphics.utils

import java.awt.Font
import java.util.HashMap

object StandardFont {

    private val KLAVIKA_BOLD = Font.createFont(
        Font.TRUETYPE_FONT,
        StandardFont::class.java.classLoader.getResourceAsStream("Klavika Bold.otf"),
    )
    private val KLAVIKA_REGULAR = Font.createFont(
        Font.TRUETYPE_FONT,
        StandardFont::class.java.classLoader.getResourceAsStream("Klavika Regular.otf"),
    )

    private val boldFontCache: MutableMap<Int, Font> = HashMap()
    private val normalFontCache: MutableMap<Int, Font> = HashMap()
    private val boldFont = { size: Int -> KLAVIKA_BOLD.deriveFont(Font.PLAIN, size.toFloat()) }
    private val normalFont = { size: Int -> KLAVIKA_REGULAR.deriveFont(Font.PLAIN, size.toFloat()) }

    @Synchronized
    fun readBoldFont(size: Int): Font = boldFontCache.computeIfAbsent(size, boldFont)

    @Synchronized
    fun readNormalFont(size: Int): Font = normalFontCache.computeIfAbsent(size, normalFont)
}
