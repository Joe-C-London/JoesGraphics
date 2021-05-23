package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.graphics.utils.StandardFont
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.min

class SwingFrame(
    headerBinding: Binding<String?>
) : GraphicsFrame(
    headerBinding = headerBinding
) {
    private var rangeBinding: Binding<Number> = Binding.fixedBinding(1)
    private var valueBinding: Binding<Number> = Binding.fixedBinding(0)
    private var leftColorBinding: Binding<Color> = Binding.fixedBinding(Color.BLACK)
    private var rightColorBinding: Binding<Color> = Binding.fixedBinding(Color.BLACK)
    private var bottomTextBinding: Binding<String?> = Binding.fixedBinding(null)
    private var bottomColorBinding: Binding<Color> = Binding.fixedBinding(Color.BLACK)

    private val swingPanel = SwingPanel()
    private val bottomLabel: JLabel = FontSizeAdjustingLabel()

    init {
        bottomLabel.horizontalAlignment = JLabel.CENTER
        bottomLabel.font = StandardFont.readBoldFont(15)
        bottomLabel.border = EmptyBorder(2, 0, -2, 0)
        val centerPanel = JPanel()
        centerPanel.background = Color.WHITE
        centerPanel.layout = BorderLayout()
        centerPanel.add(swingPanel, BorderLayout.CENTER)
        centerPanel.add(bottomLabel, BorderLayout.SOUTH)
        add(centerPanel, BorderLayout.CENTER)
    }

    internal fun getRange(): Number {
        return swingPanel.range
    }

    fun setRangeBinding(rangeBinding: Binding<Number>) {
        this.rangeBinding.unbind()
        this.rangeBinding = rangeBinding
        this.rangeBinding.bind { swingPanel.range = it }
    }

    internal fun getValue(): Number {
        return swingPanel.value
    }

    fun setValueBinding(valueBinding: Binding<Number>) {
        this.valueBinding.unbind()
        this.valueBinding = valueBinding
        this.valueBinding.bind { swingPanel.value = it }
    }

    internal fun getLeftColor(): Color {
        return swingPanel.leftColor
    }

    fun setLeftColorBinding(leftColorBinding: Binding<Color>) {
        this.leftColorBinding.unbind()
        this.leftColorBinding = leftColorBinding
        this.leftColorBinding.bind { swingPanel.leftColor = it }
    }

    internal fun getRightColor(): Color {
        return swingPanel.rightColor
    }

    fun setRightColorBinding(rightColorBinding: Binding<Color>) {
        this.rightColorBinding.unbind()
        this.rightColorBinding = rightColorBinding
        this.rightColorBinding.bind { swingPanel.rightColor = it }
    }

    internal fun getBottomText(): String? {
        return if (bottomLabel.isVisible) bottomLabel.text else null
    }

    fun setBottomTextBinding(bottomTextBinding: Binding<String?>) {
        this.bottomTextBinding.unbind()
        this.bottomTextBinding = bottomTextBinding
        this.bottomTextBinding.bind {
            bottomLabel.isVisible = it != null
            bottomLabel.text = it ?: ""
        }
    }

    internal fun getBottomColor(): Color {
        return bottomLabel.foreground
    }

    fun setBottomColorBinding(bottomColorBinding: Binding<Color>) {
        this.bottomColorBinding.unbind()
        this.bottomColorBinding = bottomColorBinding
        this.bottomColorBinding.bind { bottomLabel.foreground = it }
    }

    private inner class SwingPanel : JPanel() {
        private var _range: Number = 1
        private var _value: Number = 0
        private var _leftColor: Color = Color.BLACK
        private var _rightColor: Color = Color.BLACK

        var range: Number
        get() { return _range }
        set(range) {
            _range = range
            repaint()
        }

        var value: Number
        get() { return _value }
        set(value) {
            _value = value
            repaint()
        }

        var leftColor: Color
        get() { return _leftColor }
        set(leftColor) {
            _leftColor = leftColor
            repaint()
        }

        var rightColor: Color
        get() { return _rightColor }
        set(rightColor) {
            _rightColor = rightColor
            repaint()
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            val margin = 2
            val arcWidth = width / 2 - 2 * margin
            val arcHeight = height - 2 * margin
            val arcSize = arcWidth.coerceAtMost(arcHeight)
            val arcX = width / 2 - arcSize
            val arcY = (height - arcSize) / 2
            var arcAngle = (90 * value.toDouble() / range.toDouble()).toInt()
            val maxAngle = 85
            arcAngle = (-maxAngle).coerceAtLeast(min(arcAngle, maxAngle))
            g.setColor(leftColor)
            g.fillArc(arcX, arcY - arcSize, arcSize * 2, arcSize * 2, 180, arcAngle + 90)
            g.setColor(rightColor)
            g.fillArc(arcX, arcY - arcSize, arcSize * 2, arcSize * 2, 0, arcAngle - 90)
            g.setColor(background)
            g.drawLine(width / 2, 0, width / 2, height)
        }

        init {
            background = Color.WHITE
        }
    }
}
