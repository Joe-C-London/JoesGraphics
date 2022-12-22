package com.joecollins.graphics.utils

import java.awt.Font
import java.awt.FontFormatException
import java.io.IOException
import java.util.HashMap

object StandardFont {
    private val KLAVIKA_BOLD: Font
    private val KLAVIKA_REGULAR: Font

    init {
        try {
            KLAVIKA_BOLD = Font.createFont(
                Font.TRUETYPE_FONT,
                StandardFont::class.java.classLoader.getResourceAsStream("Klavika Bold.otf")
            )
            KLAVIKA_REGULAR = Font.createFont(
                Font.TRUETYPE_FONT,
                StandardFont::class.java.classLoader.getResourceAsStream("Klavika Regular.otf")
            )
        } catch (e: FontFormatException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private val boldFontCache: MutableMap<Int, Font> = HashMap()
    private val normalFontCache: MutableMap<Int, Font> = HashMap()
    private var boldFont = { size: Int -> KLAVIKA_BOLD.deriveFont(Font.PLAIN, size.toFloat()) }
    private var normalFont = { size: Int -> KLAVIKA_REGULAR.deriveFont(Font.PLAIN, size.toFloat()) }

    @Synchronized
    fun readBoldFont(size: Int): Font {
        return boldFontCache.computeIfAbsent(size, boldFont)
    }

    @Synchronized
    fun readNormalFont(size: Int): Font {
        return normalFontCache.computeIfAbsent(size, normalFont)
    }
}