package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
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
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class BarFrame(
    headerBinding: Binding<String?>,
    subheadTextBinding: Binding<String?>? = null,
    subheadColorBinding: Binding<Color>? = null,
    barsBinding: Binding<List<Bar>>,
    linesBinding: Binding<List<Line>>? = null,
    private val minBinding: Binding<Number>? = null,
    private val maxBinding: Binding<Number>? = null,
    notesBinding: Binding<String?>? = null,
    borderColorBinding: Binding<Color>? = null
) : GraphicsFrame(
    headerBinding = headerBinding,
    notesBinding = notesBinding,
    borderColorBinding = borderColorBinding
) {
    private val centralPanel: JPanel
    private val subheadLabel: FontSizeAdjustingLabel = FontSizeAdjustingLabel()
    private val bars: MutableList<BarPanel> = ArrayList()
    private val lines: MutableList<LinePanel> = ArrayList()
    var min: Number = 0.0
        private set
    var max: Number = 0.0
        private set

    class Bar constructor(
        val leftText: String,
        val rightText: String,
        val leftIcon: Shape? = null,
        val series: List<Pair<Color, Number>>
    ) {
        constructor(leftText: String, rightText: String, series: List<Pair<Color, Number>>) : this(leftText, rightText, null, series)
    }

    class Line(val level: Number, val label: String)

    private fun drawLines(g: Graphics, top: Int, bottom: Int) {
        g.color = Color.BLACK
        for (line in lines) {
            val level = getPixelOfValue(line.level).toInt()
            g.drawLine(level, top, level, bottom)
        }
    }

    internal val subheadText: String?
        get() = if (subheadLabel.isVisible) subheadLabel.text else null

    internal val subheadColor: Color
        get() = subheadLabel.foreground

    internal val numBars: Int
        get() = bars.size

    internal fun getLeftText(barNum: Int): String {
        return bars[barNum].leftText
    }

    internal fun getRightText(barNum: Int): String {
        return bars[barNum].rightText
    }

    internal fun getSeries(barNum: Int): List<Pair<Color, Number>> {
        return bars[barNum].series
    }

    internal fun getLeftIcon(barNum: Int): Shape? {
        return bars[barNum].leftIcon
    }

    private fun getPixelOfValue(value: Number): Double {
        val range = max.toDouble() - min.toDouble()
        val progress = value.toDouble() - min.toDouble()
        return (((centralPanel.width - 2 * BAR_MARGIN) * progress / range).toInt() + BAR_MARGIN).toDouble()
    }

    private val maxLines: Int
        get() = bars.map { obj: BarPanel -> obj.numLines }.maxOrNull() ?: 1

    internal val numLines: Int
        get() = lines.size

    internal fun getLineLevel(index: Int): Number {
        return lines[index].level
    }

    internal fun getLineLabel(index: Int): String {
        return lines[index].label
    }

    private inner class BarPanel : JPanel() {
        private var _leftText = ""
        private var _rightText = ""
        private var _series: List<Pair<Color, Number>> = ArrayList()
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

        var series: List<Pair<Color, Number>>
        get() { return _series }
        set(series) {
            _series = series
            repaint()
        }

        val totalPositive: Number
            get() = _series
                .map { it.second.toDouble() }
                .filter { it > 0 }
                .sum()
        val totalNegative: Number
            get() = _series
                .map { it.second.toDouble() }
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
            val mainColor = if (_series.isEmpty()) Color.BLACK else _series[0].first
            g.setColor(mainColor)
            drawText(g, font)
            val zero = getPixelOfValue(0.0).toInt()
            var posLeft = zero
            var negRight = zero
            for (seriesItem in _series) {
                g.setColor(seriesItem.first)
                val width = getPixelOfValue(seriesItem.second).toInt() - zero
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
                .map { it.second.toDouble() }
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

    private inner class LinePanel {
        var level: Number = 0

        val jLabel: JLabel = object : JLabel("") {
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
        get() { return jLabel.text }
        set(label) {
            jLabel.text = label
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
                height += lines[0].jLabel.preferredSize.height
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
                val labelHeight = min(line.jLabel.preferredSize.height, actualHeight - top)
                line.jLabel.setLocation(left, actualHeight - labelHeight)
                line.jLabel.setSize(width - left, labelHeight)
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

        (subheadTextBinding ?: Binding.fixedBinding(null)).bind {
            subheadLabel.isVisible = it != null
            subheadLabel.text = it ?: ""
        }
        (subheadColorBinding ?: Binding.fixedBinding(Color.BLACK)).bind { subheadLabel.foreground = it }
        val barsReceiver = BindingReceiver(barsBinding)
        barsReceiver.getBinding().bind { b ->
            val numBars = b.size
            while (bars.size < numBars) {
                val bar = BarPanel()
                bars.add(bar)
                centralPanel.add(bar)
            }
            while (bars.size > numBars) {
                val bar = bars.removeAt(numBars)
                centralPanel.remove(bar)
            }
            b.forEachIndexed { idx, bar ->
                bars[idx].leftText = bar.leftText
                bars[idx].rightText = bar.rightText
                bars[idx].leftIcon = bar.leftIcon
                bars[idx].series = bar.series
            }
            centralPanel.invalidate()
            centralPanel.revalidate()
            repaint()
        }
        (linesBinding ?: Binding.fixedBinding(emptyList())).bind { l ->
            while (l.size > lines.size) {
                val line = LinePanel()
                lines.add(line)
                centralPanel.add(line.jLabel)
            }
            while (l.size < lines.size) {
                val line = lines.removeAt(l.size)
                centralPanel.remove(line.jLabel)
            }
            l.forEachIndexed { idx, line ->
                lines[idx].level = line.level
                lines[idx].label = line.label
            }
        }
        (minBinding ?: barsReceiver.getBinding { bl: List<Bar> -> bl.minOfOrNull { b -> b.series.sumOf { min(it.second.toDouble(), 0.0) } } ?: 0.0 }).bind { min ->
            this.min = min
            repaint()
        }
        (maxBinding ?: barsReceiver.getBinding { bl: List<Bar> -> bl.maxOfOrNull { b -> b.series.sumOf { max(it.second.toDouble(), 0.0) } } ?: 0.0 }).bind { max ->
            this.max = max
            repaint()
        }
    }
}
