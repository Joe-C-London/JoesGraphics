package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.utils.StandardFont
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import javax.swing.JPanel
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong
import kotlin.math.sign
import kotlin.math.sin
import org.apache.commons.lang3.tuple.MutablePair
import org.apache.commons.lang3.tuple.MutableTriple

class SwingometerFrame : GraphicsFrame() {
    private var rangeBinding: Binding<Number> = Binding.fixedBinding(1)
    private var valueBinding: Binding<Number> = Binding.fixedBinding(0)
    private var leftColorBinding: Binding<Color> = Binding.fixedBinding(Color.BLACK)
    private var rightColorBinding: Binding<Color> = Binding.fixedBinding(Color.BLACK)
    private var leftToWinBinding: Binding<Number> = Binding.fixedBinding(Double.POSITIVE_INFINITY)
    private var rightToWinBinding: Binding<Number> = Binding.fixedBinding(Double.POSITIVE_INFINITY)

    private var numTicksBinding: Binding<Int> = Binding.fixedBinding(0)
    private var tickPositionBinding: IndexedBinding<Number> = IndexedBinding.emptyBinding()
    private var tickTextBinding = IndexedBinding.emptyBinding<String>()

    private var numOuterLabelsBinding: Binding<Int> = Binding.fixedBinding(0)
    private var outerLabelPositionBinding: IndexedBinding<Number> = IndexedBinding.emptyBinding()
    private var outerLabelTextBinding = IndexedBinding.emptyBinding<String>()
    private var outerLabelColorBinding = IndexedBinding.emptyBinding<Color>()

    private var numBucketsPerSideBinding: Binding<Int> = Binding.fixedBinding(1)

    private var numDotsBinding: Binding<Int> = Binding.fixedBinding(0)
    private var dotsPositionBinding: IndexedBinding<Number> = IndexedBinding.emptyBinding()
    private var dotsColorBinding = IndexedBinding.emptyBinding<Color>()
    private var dotsLabelBinding = IndexedBinding.emptyBinding<String>()
    private var dotsSolidBinding = IndexedBinding.singletonBinding(true)

    private val swingPanel: SwingPanel = SwingPanel()

    internal val leftColor: Color
        get() = swingPanel.leftColor

    fun setLeftColorBinding(leftColorBinding: Binding<Color>) {
        this.leftColorBinding.unbind()
        this.leftColorBinding = leftColorBinding
        this.leftColorBinding.bind { leftColor -> swingPanel.leftColor = leftColor }
    }

    internal val rightColor: Color
        get() = swingPanel.rightColor

    fun setRightColorBinding(rightColorBinding: Binding<Color>) {
        this.rightColorBinding.unbind()
        this.rightColorBinding = rightColorBinding
        this.rightColorBinding.bind { rightColor -> swingPanel.rightColor = rightColor }
    }

    internal val value: Number
        get() = swingPanel.value

    fun setValueBinding(valueBinding: Binding<Number>) {
        this.valueBinding.unbind()
        this.valueBinding = valueBinding
        this.valueBinding.bind { value -> swingPanel.value = value }
    }

    internal val range: Number
        get() = swingPanel.range

    fun setRangeBinding(rangeBinding: Binding<Number>) {
        this.rangeBinding.unbind()
        this.rangeBinding = rangeBinding
        this.rangeBinding.bind { range -> swingPanel.range = range }
    }

    internal val numTicks: Int
        get() = swingPanel.ticks.size

    fun setNumTicksBinding(numTicksBinding: Binding<Int>) {
        this.numTicksBinding.unbind()
        this.numTicksBinding = numTicksBinding
        this.numTicksBinding.bind { numTicks -> swingPanel.setNumTicks(numTicks) }
    }

    internal fun getTickPosition(index: Int): Number {
        return swingPanel.ticks[index].left
    }

    fun setTickPositionBinding(tickPositionBinding: IndexedBinding<Number>) {
        this.tickPositionBinding.unbind()
        this.tickPositionBinding = tickPositionBinding
        this.tickPositionBinding.bind { index, position -> swingPanel.setTickPosition(index, position) }
    }

    internal fun getTickText(index: Int): String {
        return swingPanel.ticks[index].right
    }

    fun setTickTextBinding(tickTextBinding: IndexedBinding<String>) {
        this.tickTextBinding.unbind()
        this.tickTextBinding = tickTextBinding
        this.tickTextBinding.bind { index, text -> swingPanel.setTickText(index, text) }
    }

    internal val leftToWin: Number
        get() = swingPanel.leftToWin

    fun setLeftToWinBinding(leftToWinBinding: Binding<Number>) {
        this.leftToWinBinding.unbind()
        this.leftToWinBinding = leftToWinBinding
        this.leftToWinBinding.bind { leftToWin -> swingPanel.leftToWin = leftToWin }
    }

    internal val rightToWin: Number
        get() = swingPanel.rightToWin

    fun setRightToWinBinding(rightToWinBinding: Binding<Number>) {
        this.rightToWinBinding.unbind()
        this.rightToWinBinding = rightToWinBinding
        this.rightToWinBinding.bind { rightToWin -> swingPanel.rightToWin = rightToWin }
    }

    internal val numOuterLabels: Int
        get() = swingPanel.outerLabels.size

    fun setNumOuterLabelsBinding(numOuterLabelsBinding: Binding<Int>) {
        this.numOuterLabelsBinding.unbind()
        this.numOuterLabelsBinding = numOuterLabelsBinding
        this.numOuterLabelsBinding.bind { numOuterLabels -> swingPanel.setNumOuterLabels(numOuterLabels) }
    }

    internal fun getOuterLabelPosition(index: Int): Number {
        return swingPanel.outerLabels[index].left
    }

    fun setOuterLabelPositionBinding(
        outerLabelPositionBinding: IndexedBinding<Number>
    ) {
        this.outerLabelPositionBinding.unbind()
        this.outerLabelPositionBinding = outerLabelPositionBinding
        this.outerLabelPositionBinding.bind { index, position -> swingPanel.setOuterLabelPosition(index, position) }
    }

    internal fun getOuterLabelText(index: Int): String {
        return swingPanel.outerLabels[index].middle
    }

    fun setOuterLabelTextBinding(outerLabelTextBinding: IndexedBinding<String>) {
        this.outerLabelTextBinding.unbind()
        this.outerLabelTextBinding = outerLabelTextBinding
        this.outerLabelTextBinding.bind { index, text -> swingPanel.setOuterLabelText(index, text) }
    }

    internal fun getOuterLabelColor(index: Int): Color {
        return swingPanel.outerLabels[index].right
    }

    fun setOuterLabelColorBinding(outerLabelColorBinding: IndexedBinding<Color>) {
        this.outerLabelColorBinding.unbind()
        this.outerLabelColorBinding = outerLabelColorBinding
        this.outerLabelColorBinding.bind { index, color -> swingPanel.setOuterLabelColor(index, color) }
    }

    internal val numBucketsPerSide: Int
        get() = swingPanel.numBucketsPerSide

    fun setNumBucketsPerSideBinding(numBucketsPerSideBinding: Binding<Int>) {
        this.numBucketsPerSideBinding.unbind()
        this.numBucketsPerSideBinding = numBucketsPerSideBinding
        this.numBucketsPerSideBinding.bind { numBucketsPerSide -> swingPanel.numBucketsPerSide = numBucketsPerSide }
    }

    internal val numDots: Int
        get() = swingPanel.dots.size

    fun setNumDotsBinding(numDotsBinding: Binding<Int>) {
        this.numDotsBinding.unbind()
        this.numDotsBinding = numDotsBinding
        this.numDotsBinding.bind { numDots -> swingPanel.setNumDots(numDots) }
    }

    internal fun getDotPosition(index: Int): Number {
        return swingPanel.dots[index].position
    }

    fun setDotsPositionBinding(dotsPositionBinding: IndexedBinding<Number>) {
        this.dotsPositionBinding.unbind()
        this.dotsPositionBinding = dotsPositionBinding
        this.dotsPositionBinding.bind { index, position -> swingPanel.setDotPosition(index, position) }
    }

    internal fun getDotColor(index: Int): Color {
        return swingPanel.dots[index].color
    }

    fun setDotsColorBinding(dotsColorBinding: IndexedBinding<Color>) {
        this.dotsColorBinding.unbind()
        this.dotsColorBinding = dotsColorBinding
        this.dotsColorBinding.bind { index, color -> swingPanel.setDotColor(index, color) }
    }

    internal fun getDotLabel(index: Int): String {
        return swingPanel.dots[index].label
    }

    fun setDotsLabelBinding(dotsLabelBinding: IndexedBinding<String>) {
        this.dotsLabelBinding.unbind()
        this.dotsLabelBinding = dotsLabelBinding
        this.dotsLabelBinding.bind { index, label -> swingPanel.setDotLabel(index, label) }
    }

    internal fun isDotSolid(index: Int): Boolean {
        return swingPanel.dots[index].solid
    }

    fun setDotsSolidBinding(dotsSolidBinding: IndexedBinding<Boolean>) {
        this.dotsSolidBinding.unbind()
        this.dotsSolidBinding = dotsSolidBinding
        this.dotsSolidBinding.bind { index, solid -> swingPanel.setDotSolid(index, solid) }
    }

    private inner class Dot {
        var position: Number = 0.0
        var color: Color = Color.WHITE
        var label = ""
        var solid = true
    }

    private inner class SwingPanel : JPanel() {
        private var _leftColor = Color.BLACK
        private var _rightColor = Color.BLACK
        private var _value: Number = 0
        private var _range: Number = 1
        private var _leftToWin: Number = Double.POSITIVE_INFINITY
        private var _rightToWin: Number = Double.POSITIVE_INFINITY
        private val _ticks: MutableList<MutablePair<Number, String>> = ArrayList()
        private val _outerLabels: MutableList<MutableTriple<Number, String, Color>> = ArrayList()
        private var _numBucketsPerSide = 1
        private val _dots: MutableList<Dot> = ArrayList()

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

        val ticks: List<org.apache.commons.lang3.tuple.Pair<Number, String>>
        get() { return _ticks }

        fun setNumTicks(numTicks: Int) {
            while (numTicks < _ticks.size) {
                _ticks.removeAt(numTicks)
            }
            while (numTicks > _ticks.size) {
                _ticks.add(MutablePair.of(0.0, ""))
            }
            repaint()
        }

        fun setTickPosition(index: Int, position: Number) {
            _ticks[index].left = position
            repaint()
        }

        fun setTickText(index: Int, text: String) {
            _ticks[index].right = text
            repaint()
        }

        val outerLabels: List<org.apache.commons.lang3.tuple.Triple<Number, String, Color>>
        get() { return _outerLabels }

        fun setNumOuterLabels(numOuterLabels: Int) {
            while (numOuterLabels < _outerLabels.size) {
                _outerLabels.removeAt(numOuterLabels)
            }
            while (numOuterLabels > _outerLabels.size) {
                _outerLabels.add(MutableTriple.of(0.0, "", Color.BLACK))
            }
            repaint()
        }

        fun setOuterLabelPosition(index: Int, position: Number) {
            _outerLabels[index].left = position
            repaint()
        }

        fun setOuterLabelText(index: Int, text: String) {
            _outerLabels[index].middle = text
            repaint()
        }

        fun setOuterLabelColor(index: Int, color: Color) {
            _outerLabels[index].right = color
            repaint()
        }

        var numBucketsPerSide: Int
        get() { return _numBucketsPerSide }
        set(numBucketsPerSide) {
            _numBucketsPerSide = numBucketsPerSide
            repaint()
        }

        val dots: List<Dot>
        get() { return _dots }

        fun setNumDots(numDots: Int) {
            while (numDots < _dots.size) {
                _dots.removeAt(numDots)
            }
            while (numDots > _dots.size) {
                _dots.add(Dot())
            }
            repaint()
        }

        fun setDotPosition(index: Int, position: Number) {
            dots[index].position = position
            repaint()
        }

        fun setDotColor(index: Int, color: Color) {
            dots[index].color = color
            repaint()
        }

        fun setDotLabel(index: Int, label: String) {
            dots[index].label = label
            repaint()
        }

        fun setDotSolid(index: Int, solid: Boolean) {
            dots[index].solid = solid
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
                g.transform = createRotationTransform(tick.left, originalTransform, arcY)
                val textWidth = g.getFontMetrics().stringWidth(tick.right)
                g.drawString(tick.right, (width - textWidth) / 2, arcY + outer / 2 - 3)
                g.transform = originalTransform
            }
            val bucketSize = range.toDouble() / numBucketsPerSide
            val bucketedDots = dots
                .filter { e: Dot -> abs(e.position.toDouble()) <= range.toDouble() }
                .sortedBy { e: Dot -> abs(e.position.toDouble()) }
                .groupBy { e: Dot ->
                        (sign(e.position.toDouble())
                            .toInt() *
                                ceil(abs(e.position.toDouble() / bucketSize)).toInt())
                    }
            val maxBucketSize = bucketedDots.values
                .map { obj: List<Dot> -> obj.size }
                .maxOrNull() ?: 0
            val theta = Math.PI / 2 / numBucketsPerSide
            val dotSize = ((1.0 *
                    inner) / 2 /
                    (0.5 / sin(theta / 2) + 1.0 * maxBucketSize / cos(theta / 2))).toInt()
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
                            (width - strWidth) / 2, inner / 2 - dotNum * dotSize - (dotSize - totalHeight * 3 / 4) / 2 +
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
                if (abs(outerLabel.left.toDouble()) <= range.toDouble()) {
                    g.setColor(outerLabel.right)
                    g.transform = createRotationTransform(
                        outerLabel.left.toDouble(), originalTransform, arcY
                    )
                    val textWidth = g.getFontMetrics().stringWidth(outerLabel.middle)
                    g.drawString(
                        outerLabel.middle, (width - textWidth) / 2, arcY + boundary / 2 - 6
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
    }
}
