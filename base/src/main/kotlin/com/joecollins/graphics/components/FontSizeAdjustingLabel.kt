package com.joecollins.graphics.components

import com.joecollins.utils.ExecutorUtils
import java.awt.Font
import java.awt.Graphics
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import javax.swing.JLabel

class FontSizeAdjustingLabel() : JLabel() {

    constructor(text: String) : this() {
        this.text = text
    }

    var renderedFont: Font = super.getFont()
        private set

    override fun setFont(font: Font?) {
        super.setFont(font)
        determineRenderedFont(font, width, text)
    }

    override fun setText(text: String) {
        super.setText(text)
        determineRenderedFont(font, width, text)
    }

    init {
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                super.componentResized(e)
                determineRenderedFont(font, width, text)
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        g.font = renderedFont
        super.paintComponent(g)
    }

    private fun determineRenderedFont(font: Font?, width: Int, text: String) {
        if (font != null) {
            val minSize = 2
            val newFont = (font.size downTo minSize).asSequence()
                .map { font.deriveFont(it.toFloat()) }
                .first { it.size == minSize || getStringWidth(it, text) <= width - 6 }
            renderedFont = newFont
        }
    }

    private fun getStringWidth(font: Font, text: String): Double {
        val frc = FontRenderContext(AffineTransform(), true, true)
        return font.getStringBounds(text, frc).width
    }

    companion object {
        fun List<FontSizeAdjustingLabel>.equaliseFonts() {
            forEach { label ->
                label.addComponentListener(object : ComponentAdapter() {
                    override fun componentResized(e: ComponentEvent?) {
                        super.componentResized(e)
                        equaliseFontsNow()
                    }
                })
                label.addPropertyChangeListener { equaliseFontsNow() }
            }
        }

        private fun Collection<FontSizeAdjustingLabel>.equaliseFontsNow() {
            forEach { it.determineRenderedFont(it.font, it.width, it.text) }
            ExecutorUtils.sendToEventQueue {
                val font = map { it.renderedFont }.minBy { it.size }
                forEach { it.renderedFont = font }
            }
        }
    }
}
