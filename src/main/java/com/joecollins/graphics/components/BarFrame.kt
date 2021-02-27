package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.utils.StandardFont
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.LayoutManager
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.AffineTransform
import java.util.ArrayList
import java.util.LinkedHashMap
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.MutablePair
import org.apache.commons.lang3.tuple.Pair

class BarFrame : GraphicsFrame() {
    private val centralPanel: JPanel
    private val subheadLabel: FontSizeAdjustingLabel = FontSizeAdjustingLabel()
    private val bars: MutableList<Bar> = ArrayList()
    private val lines: MutableList<Line> = ArrayList()
    var min: Number = 0.0
        private set
    var max: Number = 0.0
        private set
    private var usingDefaultMin = true
    private var usingDefaultMax = true

    private var subheadTextBinding: Binding<String?> = Binding.fixedBinding(null)
    private var subheadColorBinding: Binding<Color> = Binding.fixedBinding(Color.BLACK)

    private var numBarsBinding: Binding<Int> = Binding.fixedBinding(0)
    private var leftTextBinding = IndexedBinding.emptyBinding<String>()
    private var rightTextBinding = IndexedBinding.emptyBinding<String>()
    private var leftIconBinding = IndexedBinding.emptyBinding<Shape?>()
    private val seriesBindings: MutableMap<String, Pair<IndexedBinding<Color>, IndexedBinding<out Number>>> =
        LinkedHashMap()
    private var numLinesBinding: Binding<Int> = Binding.fixedBinding(0)
    private var lineLevelsBinding: IndexedBinding<out Number> = IndexedBinding.emptyBinding()
    private var lineLabelsBinding = IndexedBinding.emptyBinding<String>()
    private var minBinding: Binding<out Number> = object : Binding<Number> {
        override val value: Number
            get() = bars
                .map { bar: Bar -> bar.totalNegative.toDouble() }
                .fold(0.0) { a: Double, b: Double -> min(a, b) }
    }
    private var maxBinding: Binding<out Number> = object : Binding<Number> {
        override val value: Number
        get() = bars
            .map { bar: Bar -> bar.totalPositive.toDouble() }
            .fold(0.0) { a: Double, b: Double -> max(a, b) }
    }

    private fun drawLines(g: Graphics, top: Int, bottom: Int) {
        g.color = Color.BLACK
        for (line in lines) {
            val level = getPixelOfValue(line.level).toInt()
            g.drawLine(level, top, level, bottom)
        }
    }

    protected val subheadText: String?
        get() = if (subheadLabel.isVisible) subheadLabel.text else null

    fun setSubheadTextBinding(subheadTextBinding: Binding<String?>) {
        this.subheadTextBinding.unbind()
        this.subheadTextBinding = subheadTextBinding
        this.subheadTextBinding.bind {
            subheadLabel.isVisible = it != null
            subheadLabel.text = it ?: ""
        }
    }

    protected val subheadColor: Color
        get() = subheadLabel.foreground

    fun setSubheadColorBinding(subheadColorBinding: Binding<Color>) {
        this.subheadColorBinding.unbind()
        this.subheadColorBinding = subheadColorBinding
        this.subheadColorBinding.bind { subheadLabel.foreground = it }
    }

    protected val numBars: Int
        get() = bars.size

    fun setNumBarsBinding(numBarsBinding: Binding<Int>) {
        this.numBarsBinding.unbind()
        this.numBarsBinding = numBarsBinding
        this.numBarsBinding.bind { numBars ->
            while (bars.size < numBars) {
                val bar = Bar()
                bars.add(bar)
                centralPanel.add(bar)
            }
            while (bars.size > numBars) {
                val bar = bars.removeAt(numBars)
                centralPanel.remove(bar)
            }
            centralPanel.invalidate()
            centralPanel.revalidate()
            repaint()
        }
    }

    protected fun getLeftText(barNum: Int): String {
        return bars[barNum].leftText
    }

    fun setLeftTextBinding(leftTextBinding: IndexedBinding<String>) {
        this.leftTextBinding.unbind()
        this.leftTextBinding = leftTextBinding
        this.leftTextBinding.bind { idx, leftText -> bars[idx].leftText = leftText }
    }

    protected fun getRightText(barNum: Int): String {
        return bars[barNum].rightText
    }

    fun setRightTextBinding(rightTextBinding: IndexedBinding<String>) {
        this.rightTextBinding.unbind()
        this.rightTextBinding = rightTextBinding
        this.rightTextBinding.bind { idx, rightText -> bars[idx].rightText = rightText }
    }

    protected fun getSeries(barNum: Int): List<Pair<Color, Number>> {
        return bars[barNum].series
    }

    fun addSeriesBinding(
        seriesName: String,
        colorBinding: IndexedBinding<Color>,
        valueBinding: IndexedBinding<out Number>
    ) {
        val oldSeries = seriesBindings[seriesName]
        if (oldSeries != null) {
            oldSeries.left.unbind()
            oldSeries.right.unbind()
        }
        seriesBindings[seriesName] = ImmutablePair(colorBinding, valueBinding)
        val seriesNum = ArrayList(seriesBindings.keys).indexOf(seriesName)
        colorBinding.bind { idx, color -> bars[idx].setColor(seriesNum, color) }
        valueBinding.bind { idx, color ->
            bars[idx].setValue(seriesNum, color)
            if (usingDefaultMin) {
                val newMin = minBinding.value
                if (newMin.toDouble() != min.toDouble()) {
                    min = newMin
                    repaint()
                }
            }
            if (usingDefaultMax) {
                val newMax = maxBinding.value
                if (newMax.toDouble() != max.toDouble()) {
                    max = newMax
                    repaint()
                }
            }
        }
    }

    protected fun getLeftIcon(barNum: Int): Shape? {
        return bars[barNum].leftIcon
    }

    fun setLeftIconBinding(leftIconBinding: IndexedBinding<Shape?>) {
        this.leftIconBinding.unbind()
        this.leftIconBinding = leftIconBinding
        this.leftIconBinding.bind { idx, shape -> bars[idx].leftIcon = shape }
    }

    fun setMinBinding(minBinding: Binding<out Number>) {
        usingDefaultMin = false
        this.minBinding.unbind()
        this.minBinding = minBinding
        this.minBinding.bind { min ->
            this.min = min
            repaint()
        }
    }

    fun setMaxBinding(maxBinding: Binding<out Number>) {
        usingDefaultMax = false
        this.maxBinding.unbind()
        this.maxBinding = maxBinding
        this.maxBinding.bind { max ->
            this.max = max
            repaint()
        }
    }

    private fun getPixelOfValue(value: Number): Double {
        val range = max.toDouble() - min.toDouble()
        val progress = value.toDouble() - min.toDouble()
        return (((centralPanel.width - 2 * BAR_MARGIN) * progress / range).toInt() + BAR_MARGIN).toDouble()
    }

    private val maxLines: Int
        get() = bars.map { obj: Bar -> obj.numLines }.maxOrNull() ?: 1

    protected val numLines: Int
        get() = lines.size

    fun setNumLinesBinding(numLinesBinding: Binding<Int>) {
        this.numLinesBinding.unbind()
        this.numLinesBinding = numLinesBinding
        this.numLinesBinding.bind { size: Int ->
            while (size > lines.size) {
                val line = Line()
                lines.add(line)
                centralPanel.add(line._label)
            }
            while (size < lines.size) {
                val line = lines.removeAt(size)
                centralPanel.remove(line._label)
            }
        }
    }

    protected fun getLineLevel(index: Int): Number {
        return lines[index].level
    }

    fun setLineLevelsBinding(lineLevelsBinding: IndexedBinding<out Number>) {
        this.lineLevelsBinding.unbind()
        this.lineLevelsBinding = lineLevelsBinding
        this.lineLevelsBinding.bind { idx, level -> lines[idx].level = level }
    }

    protected fun getLineLabel(index: Int): String {
        return lines[index].label
    }

    fun setLineLabelsBinding(lineLabelsBinding: IndexedBinding<String>) {
        this.lineLabelsBinding.unbind()
        this.lineLabelsBinding = lineLabelsBinding
        this.lineLabelsBinding.bind { idx, label -> lines[idx].label = label }
    }

    private inner class Bar : JPanel() {
        private var _leftText = ""
        private var _rightText = ""
        private val _series: MutableList<MutablePair<Color, Number>> = ArrayList()
        private var _leftIcon: Shape? = null

        private fun resetPreferredSize() {
            preferredSize = Dimension(1024, 30 * numLines)
        }

        var leftText: String
        get() { return _leftText }
        set(leftText) {
            _leftText = leftText
            resetPreferredSize()
            repaint()
        }

        var rightText: String
        get() { return _rightText }
        set(rightText) {
            _rightText = rightText
            resetPreferredSize()
            repaint()
        }

        var leftIcon: Shape?
        get() { return _leftIcon }
        set(leftIcon) {
            _leftIcon = leftIcon
            repaint()
        }

        val series: List<Pair<Color, Number>>
        get() { return _series }

        fun setColor(idx: Int, color: Color) {
            while (_series.size <= idx) {
                _series.add(MutablePair(Color.BLACK, 0))
            }
            _series[idx].setLeft(color)
            repaint()
        }

        fun setValue(idx: Int, value: Number) {
            while (_series.size <= idx) {
                _series.add(MutablePair(Color.BLACK, 0))
            }
            _series[idx].setRight(value)
            repaint()
        }

        val totalPositive: Number
            get() = _series
                .map { it.getRight().toDouble() }
                .filter { it > 0 }
                .sum()
        val totalNegative: Number
            get() = _series
                .map { it.getRight().toDouble() }
                .filter { it < 0 }
                .sum()
        val numLines: Int
            get() {
                val leftLines = leftText.split("\n").toTypedArray().size
                val rightLines = rightText.split("\n").toTypedArray().size
                return max(leftLines, rightLines)
            }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
            drawLines(g, 0, height)
            val font = StandardFont.readBoldFont(barHeight * 3 / 4 / maxLines)
            g.setFont(font)
            val mainColor = if (_series.isEmpty()) Color.BLACK else _series[0].left
            g.setColor(mainColor)
            drawText(g, font)
            val zero = getPixelOfValue(0.0).toInt()
            var posLeft = zero
            var negRight = zero
            for (seriesItem in _series) {
                g.setColor(seriesItem.left)
                val width = getPixelOfValue(seriesItem.right).toInt() - zero
                if (width > 0) {
                    g.fillRect(posLeft, BAR_MARGIN, width, barHeight)
                    posLeft += width
                } else {
                    negRight += width
                    g.fillRect(negRight, BAR_MARGIN, -width, barHeight)
                }
            }
            if (posLeft != zero && negRight != zero) {
                g.setColor(Color.WHITE)
                g.drawLine(zero, 0, zero, height)
            }
            val oldClip = g.getClip()
            g.setClip(negRight, 0, posLeft - negRight, height)
            g.setColor(Color.WHITE)
            drawText(g, font)
            g.setClip(oldClip)
        }

        private val barHeight: Int
            get() = height - 2 * BAR_MARGIN

        private fun drawText(g: Graphics, font: Font) {
            val sumsPosNeg = _series
                .map { it.getRight().toDouble() }
                .groupBy { it > 0 }
                .mapValues { e -> e.value.map { abs(it) }.sum() }
            val isNetPositive = (sumsPosNeg[true] ?: 0).toDouble() >= (sumsPosNeg[false] ?: 0).toDouble()
            val zero = getPixelOfValue(0.0)
            val leftText = leftText.split("\n").toTypedArray()
            val rightText = rightText.split("\n").toTypedArray()
            val maxLeftWidth =
                leftText
                    .map { str -> g.getFontMetrics(font).stringWidth(str) }
                    .maxOrNull() ?: 0
            val maxRightWidth =
                rightText
                    .map { str -> g.getFontMetrics(font).stringWidth(str) }
                    .maxOrNull() ?: 0
            val leftIconWidth: Int = if (leftIcon != null) {
                val leftIconBounds = leftIcon!!.bounds
                val leftIconScale = (barHeight - 2 * BAR_MARGIN) / leftIconBounds.getHeight()
                (leftIconScale * leftIconBounds.getWidth()).toInt()
            } else {
                0
            }
            val minSpaceBetween = 20
            val shrinkLeft: Boolean
            val shrinkRight: Boolean
            if (maxLeftWidth + leftIconWidth + maxRightWidth + minSpaceBetween > width) {
                shrinkLeft = maxLeftWidth + leftIconWidth > width / 2
                shrinkRight = maxRightWidth > width / 2
            } else {
                shrinkLeft = false
                shrinkRight = false
            }
            var leftMax = zero
            for (i in leftText.indices) {
                var lineFont = font
                if (shrinkLeft) {
                    val maxWidth = (width -
                            (if (shrinkRight) (width + minSpaceBetween) / 2 else maxRightWidth + minSpaceBetween) -
                            leftIconWidth)
                    for (fontSize in font.size downTo 2) {
                        lineFont = font.deriveFont(fontSize.toFloat())
                        val width = g.getFontMetrics(lineFont).stringWidth(leftText[i])
                        if (width < maxWidth) {
                            break
                        }
                    }
                }
                g.font = lineFont
                val leftWidth = g.getFontMetrics(lineFont).stringWidth(leftText[i])
                val textHeight = lineFont.size
                val textBase = (i + 1) * (barHeight + textHeight) / (leftText.size + 1)
                leftMax = if (isNetPositive) {
                    g.drawString(leftText[i], zero.toInt(), textBase)
                    max(leftMax, leftWidth + zero)
                } else {
                    g.drawString(leftText[i], zero.toInt() - leftWidth, textBase)
                    min(leftMax, zero - leftWidth)
                }
            }
            for (i in rightText.indices) {
                var lineFont = font
                if (shrinkRight) {
                    val maxWidth = (width -
                            if (shrinkLeft) (width + minSpaceBetween) / 2 else maxLeftWidth + leftIconWidth + minSpaceBetween)
                    for (fontSize in font.size downTo 2) {
                        lineFont = font.deriveFont(fontSize.toFloat())
                        val width = g.getFontMetrics(lineFont).stringWidth(rightText[i])
                        if (width < maxWidth) {
                            break
                        }
                    }
                }
                g.font = lineFont
                val rightWidth = g.getFontMetrics(lineFont).stringWidth(rightText[i])
                val textHeight = lineFont.size
                val textBase = (i + 1) * (barHeight + textHeight) / (rightText.size + 1)
                if (isNetPositive) {
                    g.drawString(rightText[i], width - rightWidth - BAR_MARGIN, textBase)
                } else {
                    g.drawString(rightText[i], BAR_MARGIN, textBase)
                }
            }
            if (leftIcon != null) {
                val leftIconBounds = leftIcon!!.bounds
                val leftIconScale = (barHeight - 2 * BAR_MARGIN) / leftIconBounds.getHeight()
                val transform = AffineTransform()
                val spaceWidth = g.getFontMetrics(font).stringWidth(" ")
                if (isNetPositive) {
                    transform.translate(leftMax + spaceWidth, (2 * BAR_MARGIN).toDouble())
                } else {
                    transform.translate(
                        leftMax - spaceWidth - leftIconScale * leftIconBounds.getWidth(), (2 * BAR_MARGIN).toDouble()
                    )
                }
                transform.scale(leftIconScale, leftIconScale)
                val scaledLeftIcon = transform.createTransformedShape(leftIcon)
                (g as Graphics2D).fill(scaledLeftIcon)
            }
        }

        init {
            resetPreferredSize()
            background = Color.WHITE
        }
    }

    private inner class Line {
        var level: Number = 0

        val _label: JLabel = object : JLabel("") {
            init {
                foreground = Color.BLACK
                preferredSize = Dimension(1024, 15)
                font = StandardFont.readNormalFont(10)
                isVisible = true
                horizontalAlignment = LEFT
                verticalAlignment = BOTTOM
                addComponentListener(
                    object : ComponentAdapter() {
                        override fun componentResized(e: ComponentEvent) {
                            font = StandardFont.readNormalFont(height * 2 / 3)
                        }
                    })
            }
        }

        var label: String
        get() { return _label.text }
        set(label) {
            _label.text = label
        }
    }

    private inner class BarFrameLayout : LayoutManager {
        override fun addLayoutComponent(name: String, comp: Component) {}
        override fun removeLayoutComponent(comp: Component) {}
        override fun preferredLayoutSize(parent: Container): Dimension {
            return getLayoutSize { obj: JComponent -> obj.preferredSize }
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            return getLayoutSize { obj: JComponent -> obj.minimumSize }
        }

        private fun getLayoutSize(func: (JComponent) -> Dimension): Dimension {
            var width = 0
            var height = 0
            if (subheadLabel.isVisible) {
                val subheadSize = func(subheadLabel)
                width = subheadSize.width
                height = subheadSize.height
            }
            var barHeight = 0
            for (bar in bars) {
                val barSize = func(bar)
                width = max(width, barSize.width)
                barHeight = max(barHeight, barSize.height)
            }
            height += bars.size * barHeight
            if (lines.isNotEmpty()) {
                height += lines[0]._label.preferredSize.height
            }
            return Dimension(width, height)
        }

        override fun layoutContainer(parent: Container) {
            val preferredHeight = preferredLayoutSize(parent).height
            val actualHeight = parent.height
            val factor = min(1.0, 1.0 * actualHeight / preferredHeight)
            val width = parent.width
            var top = 0
            if (subheadLabel.isVisible) {
                val height = (subheadLabel.preferredSize.height * factor).toInt()
                subheadLabel.setLocation(0, top)
                subheadLabel.setSize(width, height)
                top += height
            }
            val barHeight = (
                    (bars
                .map { i -> i.preferredSize.height }
                .maxOrNull() ?: 0) *
                            factor).toInt()
            for (bar in bars) {
                bar.setLocation(0, top)
                bar.setSize(width, barHeight)
                top += barHeight
            }
            for (line in lines) {
                val left = getPixelOfValue(line.level).toInt() + BAR_MARGIN
                val labelHeight = min(line._label.preferredSize.height, actualHeight - top)
                line._label.setLocation(left, actualHeight - labelHeight)
                line._label.setSize(width - left, labelHeight)
            }
        }
    }

    companion object {
        private const val BAR_MARGIN = 2
    }

    init {
        subheadLabel.foreground = Color.BLACK
        subheadLabel.preferredSize = Dimension(1024, 30)
        subheadLabel.font = StandardFont.readBoldFont(20)
        subheadLabel.isVisible = false
        subheadLabel.horizontalAlignment = JLabel.CENTER
        subheadLabel.verticalAlignment = JLabel.BOTTOM
        subheadLabel.addComponentListener(
            object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    subheadLabel.font = StandardFont.readBoldFont(subheadLabel.height * 2 / 3)
                }
            })
        centralPanel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                (g as Graphics2D)
                    .setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                    )
                g
                    .setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
                    )
                drawLines(
                    g,
                    if (subheadLabel.isVisible) subheadLabel.height else BAR_MARGIN,
                    height - BAR_MARGIN
                )
            }

            init {
                background = Color.WHITE
                layout = BarFrameLayout()
            }
        }
        add(centralPanel, BorderLayout.CENTER)
        centralPanel.add(subheadLabel)
    }
}
