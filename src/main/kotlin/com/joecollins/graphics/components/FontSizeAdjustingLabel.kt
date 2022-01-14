package com.joecollins.graphics.components

import java.awt.Font
import java.awt.Graphics
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import javax.swing.JLabel

class FontSizeAdjustingLabel : JLabel() {

    override fun paintComponent(g: Graphics) {
        val font = super.getFont()
        if (font != null) {
            var newFont = font
            for (size in font.size downTo 2) {
                newFont = font.deriveFont(size.toFloat())
                if (getStringWidth(newFont) <= width - 6) break
            }
            g.font = newFont
        }
        super.paintComponent(g)
    }

    private fun getStringWidth(font: Font): Double {
        val frc = FontRenderContext(AffineTransform(), true, true)
        return font.getStringBounds(text, frc).width
    }
}
