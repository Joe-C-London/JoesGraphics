package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.graphics.utils.StandardFont
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.lang.Double.NEGATIVE_INFINITY
import java.lang.Double.POSITIVE_INFINITY
import javax.swing.JPanel
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong
import kotlin.math.sign
import kotlin.math.sin

class SwingometerFrame(
    headerBinding: Binding<String?>,
    valueBinding: Binding<Number>,
    rangeBinding: Binding<Number>,
    leftColorBinding: Binding<Color>,
    rightColorBinding: Binding<Color>,
    numBucketsPerSideBinding: Binding<Int>,
    dotsBinding: Binding<List<Dot>>,
    leftToWinBinding: Binding<Number>? = null,
    rightToWinBinding: Binding<Number>? = null,
    ticksBinding: Binding<List<Tick>>? = null,
    outerLabelsBinding: Binding<List<OuterLabel>>? = null
) : GraphicsFrame(
    headerPublisher = headerBinding.toPublisher()
) {
    class Tick(val position: Number, val text: String)

    class OuterLabel(val position: Number, val text: String, val color: Color)

    private val swingPanel: SwingPanel = SwingPanel()

    internal val leftColor: Color
        get() = swingPanel.leftColor

    internal val rightColor: Color
        get() = swingPanel.rightColor

    internal val value: Number
        get() = swingPanel.value

    internal val range: Number
        get() = swingPanel.range

    internal val numTicks: Int
        get() = swingPanel.ticks.size

    internal fun getTickPosition(index: Int): Number {
        return swingPanel.ticks[index].position
    }

    internal fun getTickText(index: Int): String {
        return swingPanel.ticks[index].text
    }

    internal val leftToWin: Number
        get() = swingPanel.leftToWin

    internal val rightToWin: Number
        get() = swingPanel.rightToWin

    internal val numOuterLabels: Int
        get() = swingPanel.outerLabels.size

    internal fun getOuterLabelPosition(index: Int): Number {
        return swingPanel.outerLabels[index].position
    }

    internal fun getOuterLabelText(index: Int): String {
        return swingPanel.outerLabels[index].text
    }

    internal fun getOuterLabelColor(index: Int): Color {
        return swingPanel.outerLabels[index].color
    }

    internal val numBucketsPerSide: Int
        get() = swingPanel.numBucketsPerSide

    internal val numDots: Int
        get() = swingPanel.dots.size

    internal fun getDotPosition(index: Int): Number {
        return swingPanel.dots[index].position
    }

    internal fun getDotColor(index: Int): Color {
        return swingPanel.dots[index].color
    }

    internal fun getDotLabel(index: Int): String {
        return swingPanel.dots[index].label
    }

    internal fun isDotSolid(index: Int): Boolean {
        return swingPanel.dots[index].solid
    }

    class Dot(val position: Number, val color: Color, val label: String = "", val solid: Boolean = true)

    private inner class SwingPanel : JPanel() {
        private var _leftColor = Color.BLACK
        private var _rightColor = Color.BLACK
        private var _value: Number = 0
        private var _range: Number = 1
        private var _leftToWin: Number = Double.POSITIVE_INFINITY
        private var _rightToWin: Number = Double.POSITIVE_INFINITY
        private var _ticks: List<Tick> = ArrayList()
        private var _outerLabels: List<OuterLabel> = ArrayList()
        private var _numBucketsPerSide = 1
        private var _dots: List<Dot> = ArrayList()

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

        var value: Number
            get() { return _value }
            set(value) {
                _value = value
                repaint()
            }

        var range: Number
            get() { return _range }
            set(range) {
                _range = range
                repaint()
            }

        var leftToWin: Number
            get() { return _leftToWin }
            set(leftToWin) {
                _leftToWin = leftToWin
                repaint()
            }

        var rightToWin: Number
            get() { return _rightToWin }
            set(rightToWin) {
                _rightToWin = rightToWin
                repaint()
            }

        var ticks: List<Tick>
            get() { return _ticks }
            set(ticks) {
                _ticks = ticks
                repaint()
            }

        var outerLabels: List<OuterLabel>
            get() { return _outerLabels }
            set(outerLabels) {
                _outerLabels = outerLabels
                repaint()
            }

        var numBucketsPerSide: Int
            get() { return _numBucketsPerSide }
            set(numBucketsPerSide) {
                _numBucketsPerSide = numBucketsPerSide
                repaint()
            }

        var dots: List<Dot>
            get() { return _dots }
            set(dots) {
                _dots = dots
                repaint()
            }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
            val margin = 2
            val arcWidth = width / 2 - 2 * margin
            val arcHeight = height - 2 * margin
            val arcSize = min(arcWidth, arcHeight)
            val arcY = (height - arcSize) / 2
            val boundary = arcSize * 2
            val outer = boundary - 2 * 25
            val inner = outer - 2 * 15
            g.setColor(leftColor)
            g.fillArc((width - outer) / 2, arcY - outer / 2, outer, outer, 180, 90)
            g.setColor(rightColor)
            g.fillArc((width - outer) / 2, arcY - outer / 2, outer, outer, 0, -90)
            g.setColor(background)
            g.fillArc((width - inner) / 2, arcY - inner / 2, inner, inner, 0, -180)
            g.setFont(StandardFont.readBoldFont(12))
            val originalTransform = g.transform
            for (tick in ticks) {
                g.transform = createRotationTransform(tick.position, originalTransform, arcY)
                val textWidth = g.getFontMetrics().stringWidth(tick.text)
                g.drawString(tick.text, (width - textWidth) / 2, arcY + outer / 2 - 3)
                g.transform = originalTransform
            }
            val bucketSize = range.toDouble() / numBucketsPerSide
            val bucketedDots = dots
                .filter { e: Dot -> abs(e.position.toDouble()) <= range.toDouble() }
                .sortedBy { e: Dot -> abs(e.position.toDouble()) }
                .groupBy { e: Dot ->
                    (
                        sign(e.position.toDouble())
                            .toInt() *
                            ceil(abs(e.position.toDouble() / bucketSize)).toInt()
                        )
                }
            val maxBucketSize = bucketedDots.values
                .map { obj: List<Dot> -> obj.size }
                .maxOrNull() ?: 0
            val theta = Math.PI / 2 / numBucketsPerSide
            val dotSize = (
                (
                    1.0 *
                        inner
                    ) / 2 /
                    (0.5 / sin(theta / 2) + 1.0 * maxBucketSize / cos(theta / 2))
                ).toInt()
            for ((key, value) in bucketedDots) {
                val bucketMid = (key - 0.5 * sign(key.toDouble())) * bucketSize
                g.transform = createRotationTransform(bucketMid, originalTransform, arcY)
                for (dotNum in value.indices) {
                    val dot = value[dotNum]
                    g.setColor(dot.color)
                    val drawer: (Int, Int, Int, Int) -> Unit = if (dot.solid) { x: Int, y: Int, width: Int, height: Int -> g.fillOval(x, y, width, height) }
                    else { x: Int, y: Int, width: Int, height: Int -> g.drawOval(x, y, width, height) }
                    drawer.invoke(
                        (width - dotSize) / 2 + 2,
                        inner / 2 - (dotNum + 1) * dotSize + 2,
                        dotSize - 4,
                        dotSize - 4
                    )
                    g.setColor(if (dot.solid) Color.WHITE else dot.color)
                    val text = dot.label.split("\n").toTypedArray()
                    var size = max(2, (dotSize - 8) / text.size)
                    var font: Font? = null
                    while (size > 1) {
                        font = StandardFont.readNormalFont(size)
                        val maxWidth =
                            text.map { str -> g.getFontMetrics(font).stringWidth(str) }
                                .maxOrNull() ?: 0
                        if (maxWidth < dotSize - 8) {
                            break
                        }
                        size--
                    }
                    g.setFont(font)
                    for (i in text.indices) {
                        val strWidth = g.getFontMetrics(font).stringWidth(text[i])
                        val totalHeight = size * text.size
                        g.drawString(
                            text[i],
                            (width - strWidth) / 2,
                            inner / 2 - dotNum * dotSize - (dotSize - totalHeight * 3 / 4) / 2 +
                                (i - text.size + 1) * size
                        )
                    }
                }
                g.transform = originalTransform
            }
            g.setColor(Color.BLACK)
            g.transform = createRotationTransform(value.toDouble(), originalTransform, arcY)
            g.drawLine(width / 2, arcY, width / 2, arcY + inner / 2)
            g.fillPolygon(
                intArrayOf(width / 2, width / 2 - 6, width / 2 + 6),
                intArrayOf(arcY + inner / 2, arcY + inner / 2 - 10, arcY + inner / 2 - 10),
                3
            )
            g.transform = originalTransform
            g.stroke = BasicStroke(3f)
            if (abs(leftToWin.toDouble()) < range.toDouble()) {
                val arcAngle = (-90 * leftToWin.toDouble() / range.toDouble()).roundToLong().toInt()
                g.setColor(leftColor)
                g.drawArc(
                    (width - boundary) / 2,
                    arcY - boundary / 2,
                    boundary,
                    boundary,
                    180,
                    arcAngle + 90
                )
            }
            if (abs(rightToWin.toDouble()) < range.toDouble()) {
                val arcAngle = (90 * rightToWin.toDouble() / range.toDouble()).roundToLong().toInt()
                g.setColor(rightColor)
                g.drawArc(
                    (width - boundary) / 2, arcY - boundary / 2, boundary, boundary, 0, arcAngle - 90
                )
            }
            g.setColor(background)
            g.fillRect(0, 0, width, arcY)
            g.setFont(StandardFont.readNormalFont(20))
            for (outerLabel in outerLabels) {
                if (abs(outerLabel.position.toDouble()) <= range.toDouble()) {
                    g.setColor(outerLabel.color)
                    g.transform = createRotationTransform(
                        outerLabel.position.toDouble(), originalTransform, arcY
                    )
                    val textWidth = g.getFontMetrics().stringWidth(outerLabel.text)
                    g.drawString(
                        outerLabel.text, (width - textWidth) / 2, arcY + boundary / 2 - 6
                    )
                    g.transform = originalTransform
                }
            }
        }

        private fun createRotationTransform(
            value: Number,
            originalTransform: AffineTransform,
            arcY: Int
        ): AffineTransform {
            val arcAngle = -Math.PI / 2 * value.toDouble() / range.toDouble()
            val newTransform = AffineTransform(originalTransform)
            newTransform.rotate(arcAngle, (width / 2).toDouble(), arcY.toDouble())
            return newTransform
        }

        init {
            background = Color.WHITE
        }
    }

    init {
        val centerPanel = JPanel()
        centerPanel.background = Color.WHITE
        centerPanel.layout = BorderLayout()
        centerPanel.add(swingPanel, BorderLayout.CENTER)
        add(centerPanel, BorderLayout.CENTER)

        valueBinding.bind { value -> swingPanel.value = value }
        rangeBinding.bind { range -> swingPanel.range = range }
        leftColorBinding.bind { leftColor -> swingPanel.leftColor = leftColor }
        rightColorBinding.bind { rightColor -> swingPanel.rightColor = rightColor }
        numBucketsPerSideBinding.bind { numBucketsPerSide -> swingPanel.numBucketsPerSide = numBucketsPerSide }
        dotsBinding.bind { swingPanel.dots = it }
        (leftToWinBinding ?: Binding.fixedBinding(POSITIVE_INFINITY)).bind { leftToWin -> swingPanel.leftToWin = leftToWin }
        (rightToWinBinding ?: Binding.fixedBinding(NEGATIVE_INFINITY)).bind { rightToWin -> swingPanel.rightToWin = rightToWin }
        (ticksBinding ?: Binding.fixedBinding(emptyList())).bind { t -> swingPanel.ticks = t }
        (outerLabelsBinding ?: Binding.fixedBinding(emptyList())).bind { l -> swingPanel.outerLabels = l }
    }
}
