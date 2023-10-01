package com.joecollins.graphics.components

import java.awt.Font
import java.awt.Graphics
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import javax.swing.JLabel

class FontSizeAdjustingLabel() : JLabel() {

    constructor(text: String) : this() {
        this.text = text
    }

    override fun paintComponent(g: Graphics) {
        val font = super.getFont()
        if (font != null) {
            val minSize = 2
            val newFont = (font.size downTo minSize).asSequence()
                .map { font.deriveFont(it.toFloat()) }
                .first { it.size == minSize || getStringWidth(it) <= width - 6 }
            g.font = newFont
        }
        super.paintComponent(g)
    }

    private fun getStringWidth(font: Font): Double {
        val frc = FontRenderContext(AffineTransform(), true, true)
        return font.getStringBounds(text, frc).width
    }
}
