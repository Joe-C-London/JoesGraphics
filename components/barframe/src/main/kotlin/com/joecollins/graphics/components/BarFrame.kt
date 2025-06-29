package com.joecollins.graphics.components

import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.map
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.LayoutManager
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.util.concurrent.Flow
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class BarFrame(
    headerPublisher: Flow.Publisher<out String?>,
    subheadTextPublisher: Flow.Publisher<out String?>? = null,
    subheadColorPublisher: Flow.Publisher<out Color>? = null,
    barsPublisher: Flow.Publisher<out List<Bar>>,
    private val minBarLines: Int = 0,
    linesPublisher: Flow.Publisher<out List<Line>>? = null,
    minPublisher: Flow.Publisher<out Number>? = null,
    maxPublisher: Flow.Publisher<out Number>? = null,
    notesPublisher: Flow.Publisher<out String?>? = null,
    borderColorPublisher: Flow.Publisher<out Color>? = null,
    headerLabelsPublisher: Flow.Publisher<out Map<HeaderLabelLocation, String?>>? = null,
) : GraphicsFrame(
    headerPublisher = headerPublisher,
    notesPublisher = notesPublisher,
    borderColorPublisher = borderColorPublisher,
    headerLabelsPublisher = headerLabelsPublisher,
) {
    private val centralPanel: JPanel
    private val subheadLabel: FontSizeAdjustingLabel = FontSizeAdjustingLabel()
    private val bars: MutableList<BarPanel> = ArrayList()
    private val lines: MutableList<LinePanel> = ArrayList()

    internal var min: Number = 0.0
        private set
    internal var max: Number = 0.0
        private set

    class Bar private constructor(
        val leftText: List<TextLine>,
        val rightText: List<String>,
        val leftIcon: Shape?,
        val series: List<Pair<Color, Number>>,
    ) {
        @ConsistentCopyVisibility
        data class TextLine internal constructor(internal val text: String, internal val icon: Shape?)

        companion object {
            fun String.withIcon(icon: Shape?) = TextLine(this, icon)
            fun String.withNoIcon() = TextLine(this, null)

            @JvmName("ofTextLine")
            fun of(
                leftText: List<TextLine>,
                rightText: List<String>,
                series: List<Pair<Color, Number>>,
            ) = Bar(leftText, rightText, null, series)

            fun of(
                leftText: List<String>,
                rightText: List<String>,
                series: List<Pair<Color, Number>>,
            ) = Bar(leftText.map { it.withNoIcon() }, rightText, null, series)

            fun of(
                leftText: TextLine,
                rightText: String,
                series: List<Pair<Color, Number>>,
            ) = Bar(listOf(leftText), listOf(rightText), null, series)

            fun of(
                leftText: String,
                rightText: String,
                series: List<Pair<Color, Number>>,
            ) = Bar(listOf(leftText.withNoIcon()), listOf(rightText), null, series)
        }
    }

    class Line(val level: Number, val label: String)

    private fun drawLines(g: Graphics, top: Int, bottom: Int) {
        g.color = Color.BLACK
        for (line in lines) {
            val level = getPixelOfValue(line.level).roundToInt()
            g.drawLine(level, top, level, bottom)
        }
    }

    internal val subheadText: String?
        get() = if (subheadLabel.isVisible) subheadLabel.text else null

    internal val subheadColor: Color
        get() = subheadLabel.foreground

    internal val numBars: Int
        get() = bars.size

    internal fun getLeftText(barNum: Int): List<Bar.TextLine> = bars[barNum].leftText

    internal fun getRightText(barNum: Int): List<String> = bars[barNum].rightText

    internal fun getSeries(barNum: Int): List<Pair<Color, Number>> = bars[barNum].series

    internal fun getLeftIcon(barNum: Int): Shape? = bars[barNum].leftIcon

    private fun getPixelOfValue(value: Number): Double {
        val range = max.toDouble() - min.toDouble()
        val progress = value.toDouble() - min.toDouble()
        return (((centralPanel.width - 2 * BAR_MARGIN) * progress / range.coerceAtLeast(1e-6)).roundToInt() + BAR_MARGIN).toDouble()
    }

    private val maxLines: Int
        get() = bars.maxOfOrNull { it.numLines } ?: 1

    internal val numLines: Int
        get() = lines.size

    internal fun getLineLevel(index: Int): Number = lines[index].level

    internal fun getLineLabel(index: Int): String = lines[index].label

    private inner class BarPanel : JPanel() {

        private fun resetPreferredSize() {
            preferredSize = Dimension(1024, 30 * numLines)
        }

        var leftText: List<Bar.TextLine> = emptyList()
            set(value) {
                field = value
                resetPreferredSize()
                repaint()
            }

        var rightText: List<String> = emptyList()
            set(value) {
                field = value
                resetPreferredSize()
                repaint()
            }

        var leftIcon: Shape? = null
            set(value) {
                field = value
                repaint()
            }

        var series: List<Pair<Color, Number>> = emptyList()
            set(value) {
                field = value
                repaint()
            }

        val numLines: Int
            get() {
                val leftLines = leftText.toTypedArray().size
                val rightLines = rightText.toTypedArray().size
                return max(leftLines, rightLines).coerceAtLeast(minBarLines)
            }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                )
            g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON,
            )
            drawLines(g, 0, height)
            val font = StandardFont.readBoldFont(barHeight * 3 / 4 / maxLines)
            g.setFont(font)
            val mainColor = if (series.isEmpty()) Color.BLACK else series[0].first
            g.setColor(ColorUtils.contrastForBackground(mainColor))
            drawText(g, font)
            val zero = getPixelOfValue(0.0).roundToInt()
            var posLeft = zero
            var negRight = zero
            for (seriesItem in series) {
                g.setColor(seriesItem.first)
                val width = getPixelOfValue(seriesItem.second).roundToInt() - zero
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
            g.setColor(ColorUtils.foregroundToContrast(mainColor))
            drawText(g, font)
            g.setClip(oldClip)
        }

        private val barHeight: Int
            get() = height - 2 * BAR_MARGIN

        private fun drawText(g: Graphics, font: Font) {
            val sumsPosNeg = series
                .map { it.second.toDouble() }
                .groupBy { it > 0 }
                .mapValues { e -> e.value.sumOf { abs(it) } }
            val isNetPositive = (sumsPosNeg[true] ?: 0).toDouble() >= (sumsPosNeg[false] ?: 0).toDouble()
            val zero = getPixelOfValue(0.0)
            val leftText = leftText.toTypedArray()
            val rightText = rightText.toTypedArray()
            val maxLeftWidth =
                leftText.maxOfOrNull { str ->
                    g.getFontMetrics(font).stringWidth(str.text) +
                        (
                            str.icon?.run {
                                val leftIconBounds = bounds
                                val leftIconScale = (barHeight / leftText.size - 2 * BAR_MARGIN) / leftIconBounds.getHeight()
                                leftIconBounds.width * leftIconScale
                            }?.roundToInt() ?: 0
                            )
                } ?: 0
            val maxRightWidth =
                rightText.maxOfOrNull { str -> g.getFontMetrics(font).stringWidth(str) } ?: 0
            val leftIconWidth: Int = if (leftIcon != null) {
                val leftIconBounds = leftIcon!!.bounds
                val leftIconScale = (barHeight - 2 * BAR_MARGIN) / leftIconBounds.getHeight()
                (leftIconScale * leftIconBounds.getWidth()).roundToInt()
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
            val stringBoundsByLine = ArrayList<Double>()
            for (i in leftText.withIndex()) {
                var lineFont = font
                val lineString = i.value.text
                val lineIcon = i.value.icon?.run {
                    val leftIconBounds = bounds
                    val leftIconScale = (barHeight / numLines - 2 * BAR_MARGIN) / leftIconBounds.getHeight()
                    val transform = AffineTransform.getScaleInstance(leftIconScale, leftIconScale)
                    transform.createTransformedShape(this)
                }
                if (shrinkLeft) {
                    val maxWidth = (
                        width - (lineIcon?.bounds?.width ?: 0) -
                            (if (shrinkRight) (width + minSpaceBetween) / 2 else maxRightWidth + minSpaceBetween) -
                            leftIconWidth
                        )
                    for (fontSize in font.size downTo 2) {
                        lineFont = font.deriveFont(fontSize.toFloat())
                        val width = g.getFontMetrics(lineFont).stringWidth(lineString + (if (lineIcon == null) "" else " "))
                        if (width < maxWidth) {
                            break
                        }
                    }
                }
                g.font = lineFont
                val lineLeftTextWidth = g.getFontMetrics(lineFont).stringWidth(lineString)
                val spaceWidth = g.getFontMetrics(lineFont).stringWidth(" ")
                val lineLeftIconWidth = lineIcon?.bounds?.width
                val textHeight = lineFont.size
                val textBase = (i.index + 1) * (barHeight + textHeight) / (leftText.size + 1)
                val iconTop = (i.index + 1) * (barHeight + (lineIcon?.bounds?.height ?: 0)) / (leftText.size + 1) - (lineIcon?.bounds?.height ?: 0) + BAR_MARGIN.toDouble()
                stringBoundsByLine.add(
                    if (isNetPositive) {
                        g.drawString(lineString, zero.roundToInt(), textBase)
                        if (lineIcon != null) {
                            (g as Graphics2D).fill(AffineTransform.getTranslateInstance(zero.roundToInt() + lineLeftTextWidth + spaceWidth.toDouble(), iconTop).createTransformedShape(lineIcon))
                        }
                        lineLeftTextWidth + (lineLeftIconWidth?.let { it + spaceWidth } ?: 0) + zero
                    } else {
                        g.drawString(lineString, zero.roundToInt() - lineLeftTextWidth, textBase)
                        if (lineIcon != null) {
                            (g as Graphics2D).fill(AffineTransform.getTranslateInstance(zero.roundToInt() - lineLeftTextWidth - spaceWidth.toDouble() - lineLeftIconWidth!!, iconTop).createTransformedShape(lineIcon))
                        }
                        zero - lineLeftTextWidth - (lineLeftIconWidth?.let { it + spaceWidth } ?: 0)
                    },
                )
            }
            for (i in rightText.indices) {
                var lineFont = font
                if (shrinkRight) {
                    val maxWidth = (
                        width -
                            if (shrinkLeft) (width + minSpaceBetween) / 2 else maxLeftWidth + leftIconWidth + minSpaceBetween
                        )
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
                val leftIconPieces = stringBoundsByLine.indices.map { idx ->
                    val start = leftIconBounds.height * idx / stringBoundsByLine.size + leftIconBounds.y
                    val end = leftIconBounds.height * (idx + 1) / stringBoundsByLine.size + leftIconBounds.y
                    val area = Area(leftIcon)
                    area.intersect(Area(Rectangle(leftIconBounds.x, start, leftIconBounds.width, end - start)))
                    area
                }
                if (isNetPositive) {
                    val endOfStrings = (
                        stringBoundsByLine
                            .filterIndexed { index, _ -> leftIconPieces[index].bounds2D.let { it.width > 1 && it.height > 1 } }
                            .maxOrNull() ?: zero
                        ).coerceAtLeast(zero)
                    transform.translate(endOfStrings + spaceWidth, (2 * BAR_MARGIN).toDouble())
                } else {
                    val endOfStrings = (
                        stringBoundsByLine
                            .filterIndexed { index, _ -> leftIconPieces[index].bounds2D.let { it.width > 1 && it.height > 1 } }
                            .minOrNull() ?: zero
                        ).coerceAtMost(zero)
                    transform.translate(
                        endOfStrings - spaceWidth - leftIconScale * leftIconBounds.getWidth(),
                        (2 * BAR_MARGIN).toDouble(),
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

        val jLabel: JLabel = JLabel("").apply {
            foreground = Color.BLACK
            preferredSize = Dimension(1024, 15)
            font = StandardFont.readNormalFont(10)
            isVisible = true
            horizontalAlignment = JLabel.LEFT
            verticalAlignment = JLabel.BOTTOM
            addComponentListener(
                object : ComponentAdapter() {
                    override fun componentResized(e: ComponentEvent) {
                        font = StandardFont.readNormalFont(height * 2 / 3)
                    }
                },
            )
        }

        var label: String
            get() {
                return jLabel.text
            }
            set(label) {
                jLabel.text = label
            }
    }

    private inner class BarFrameLayout : LayoutManager {
        override fun addLayoutComponent(name: String, comp: Component) {}
        override fun removeLayoutComponent(comp: Component) {}
        override fun preferredLayoutSize(parent: Container): Dimension = getLayoutSize { it.preferredSize }

        override fun minimumLayoutSize(parent: Container): Dimension = getLayoutSize { it.minimumSize }

        private fun getLayoutSize(func: (JComponent) -> Dimension): Dimension {
            val subheadSize = if (subheadLabel.isVisible) func(subheadLabel) else Dimension(0, 0)
            val barsSize = bars.asSequence()
                .map(func)
                .fold(Dimension(0, 0)) { acc, dim ->
                    Dimension(
                        max(acc.width, dim.width),
                        max(acc.height, dim.height),
                    )
                }.run { Dimension(width, bars.size * height) }
            val width = max(subheadSize.width, barsSize.width)
            val height = sequenceOf(
                subheadSize.height,
                barsSize.height,
                if (lines.isNotEmpty()) lines[0].jLabel.preferredSize.height else 0,
            ).sum()
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
                (
                    bars.maxOfOrNull { i -> i.preferredSize.height } ?: 0
                    ) *
                    factor
                ).toInt()
            for (bar in bars) {
                bar.setLocation(0, top)
                bar.setSize(width, barHeight)
                top += barHeight
            }
            for (line in lines) {
                val left = getPixelOfValue(line.level).roundToInt() + BAR_MARGIN
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
            },
        )
        centralPanel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                (g as Graphics2D)
                    .setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                    )
                g
                    .setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON,
                    )
                drawLines(
                    g,
                    if (subheadLabel.isVisible) subheadLabel.height else BAR_MARGIN,
                    height - BAR_MARGIN,
                )
            }

            init {
                background = Color.WHITE
                layout = BarFrameLayout()
            }
        }
        addCenter(centralPanel)
        centralPanel.add(subheadLabel)

        val onSubheadTextUpdate: (String?) -> Unit = {
            subheadLabel.isVisible = it != null
            subheadLabel.text = it ?: ""
        }
        if (subheadTextPublisher != null) {
            subheadTextPublisher.subscribe(Subscriber(eventQueueWrapper(onSubheadTextUpdate)))
        } else {
            onSubheadTextUpdate(null)
        }

        val onSubheadColorUpdate: (Color) -> Unit = { subheadLabel.foreground = it }
        if (subheadColorPublisher != null) {
            subheadColorPublisher.subscribe(Subscriber(eventQueueWrapper(onSubheadColorUpdate)))
        } else {
            onSubheadColorUpdate(Color.BLACK)
        }

        val onBarsUpdate: (List<Bar>) -> Unit = { b ->
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
        barsPublisher.subscribe(Subscriber(eventQueueWrapper(onBarsUpdate)))

        val onLinesUpdate: (List<Line>) -> Unit = { l ->
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
        if (linesPublisher != null) {
            linesPublisher.subscribe(Subscriber(eventQueueWrapper(onLinesUpdate)))
        } else {
            onLinesUpdate(emptyList())
        }

        val onMinUpdate: (Number) -> Unit = { min ->
            this.min = min
            repaint()
        }
        val minFromBars = { bars: List<Bar> -> bars.minOfOrNull { bar -> bar.series.sumOf { min(it.second.toDouble(), 0.0) } } ?: 0.0 }
        (minPublisher ?: barsPublisher.map(minFromBars))
            .subscribe(Subscriber(eventQueueWrapper(onMinUpdate)))

        val onMaxUpdate: (Number) -> Unit = { max ->
            this.max = max
            repaint()
        }
        val maxFromBars = { bl: List<Bar> -> bl.maxOfOrNull { b -> b.series.sumOf { max(it.second.toDouble(), 0.0) } } ?: 0.0 }
        (maxPublisher ?: barsPublisher.map(maxFromBars))
            .subscribe(Subscriber(eventQueueWrapper(onMaxUpdate)))
    }
}
