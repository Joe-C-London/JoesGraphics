package com.joecollins.graphics.components

import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.util.concurrent.Flow
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
    headerPublisher: Flow.Publisher<out String?>,
    valuePublisher: Flow.Publisher<out Number>,
    rangePublisher: Flow.Publisher<out Number>,
    leftColorPublisher: Flow.Publisher<out Color>,
    rightColorPublisher: Flow.Publisher<out Color>,
    numBucketsPerSidePublisher: Flow.Publisher<out Int>,
    dotsPublisher: Flow.Publisher<out List<Dot>>,
    leftToWinPublisher: Flow.Publisher<out Number>? = null,
    rightToWinPublisher: Flow.Publisher<out Number>? = null,
    ticksPublisher: Flow.Publisher<out List<Tick>>? = null,
    outerLabelsPublisher: Flow.Publisher<out List<OuterLabel>>? = null,
    headerLabelsPublisher: Flow.Publisher<out Map<HeaderLabelLocation, String?>>? = null,
) : GraphicsFrame(
    headerPublisher = headerPublisher,
    headerLabelsPublisher = headerLabelsPublisher,
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
        var leftColor: Color = Color.BLACK
            set(value) {
                field = value
                repaint()
            }

        var rightColor: Color = Color.BLACK
            set(value) {
                field = value
                repaint()
            }

        var value: Number = 0
            set(value) {
                field = value
                repaint()
            }

        var range: Number = 1
            set(value) {
                field = value
                repaint()
            }

        var leftToWin: Number = Double.POSITIVE_INFINITY
            set(value) {
                field = value
                repaint()
            }

        var rightToWin: Number = Double.NEGATIVE_INFINITY
            set(value) {
                field = value
                repaint()
            }

        var ticks: List<Tick> = emptyList()
            set(value) {
                field = value
                repaint()
            }

        var outerLabels: List<OuterLabel> = emptyList()
            set(value) {
                field = value
                repaint()
            }

        var numBucketsPerSide: Int = 1
            set(value) {
                field = value
                repaint()
            }

        var dots: List<Dot> = emptyList()
            set(value) {
                field = value
                repaint()
            }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
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
                .filter { abs(it.position.toDouble()) <= range.toDouble() }
                .sortedBy { abs(it.position.toDouble()) }
                .groupBy {
                    (
                        sign(it.position.toDouble())
                            .toInt() *
                            ceil(abs(it.position.toDouble() / bucketSize)).toInt()
                        )
                }
            val maxBucketSize = bucketedDots.values.maxOfOrNull { it.size } ?: 0
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
                    val drawer: (Int, Int, Int, Int) -> Unit = if (dot.solid) {
                        { x, y, width, height -> g.fillOval(x, y, width, height) }
                    } else {
                        { x, y, width, height -> g.drawOval(x, y, width, height) }
                    }
                    drawer.invoke(
                        (width - dotSize) / 2 + 2,
                        inner / 2 - (dotNum + 1) * dotSize + 2,
                        dotSize - 4,
                        dotSize - 4,
                    )
                    g.setColor(if (dot.solid) Color.WHITE else dot.color)
                    val text = dot.label.split("\n").toTypedArray()
                    var size = max(2, (dotSize - 8) / text.size)
                    var font: Font? = null
                    while (size > 1) {
                        font = StandardFont.readNormalFont(size)
                        val maxWidth =
                            text.maxOfOrNull { str -> g.getFontMetrics(font).stringWidth(str) } ?: 0
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
                                (i - text.size + 1) * size,
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
                3,
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
                    arcAngle + 90,
                )
            } else if (leftToWin.toDouble() <= -range.toDouble()) {
                g.setColor(leftColor)
                g.drawArc(
                    (width - boundary) / 2,
                    arcY - boundary / 2,
                    boundary,
                    boundary,
                    180,
                    180,
                )
            }
            if (abs(rightToWin.toDouble()) < range.toDouble()) {
                val arcAngle = (90 * rightToWin.toDouble() / range.toDouble()).roundToLong().toInt()
                g.setColor(rightColor)
                g.drawArc(
                    (width - boundary) / 2,
                    arcY - boundary / 2,
                    boundary,
                    boundary,
                    0,
                    arcAngle - 90,
                )
            } else if (rightToWin.toDouble() <= -range.toDouble()) {
                g.setColor(rightColor)
                g.drawArc(
                    (width - boundary) / 2,
                    arcY - boundary / 2,
                    boundary,
                    boundary,
                    0,
                    -180,
                )
            }
            g.setColor(background)
            g.fillRect(0, 0, width, arcY)
            g.setFont(StandardFont.readNormalFont(20))
            for (outerLabel in outerLabels) {
                if (abs(outerLabel.position.toDouble()) <= range.toDouble()) {
                    g.setColor(outerLabel.color)
                    g.transform = createRotationTransform(
                        outerLabel.position.toDouble(),
                        originalTransform,
                        arcY,
                    )
                    val textWidth = g.getFontMetrics().stringWidth(outerLabel.text)
                    g.drawString(
                        outerLabel.text,
                        (width - textWidth) / 2,
                        arcY + boundary / 2 - 6,
                    )
                    g.transform = originalTransform
                }
            }
        }

        private fun createRotationTransform(
            value: Number,
            originalTransform: AffineTransform,
            arcY: Int,
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
        addCenter(centerPanel)

        val onValueUpdate: (Number) -> Unit = { value -> swingPanel.value = value }
        valuePublisher.subscribe(Subscriber(eventQueueWrapper(onValueUpdate)))

        val onRangeUpdate: (Number) -> Unit = { range -> swingPanel.range = range }
        rangePublisher.subscribe(Subscriber(eventQueueWrapper(onRangeUpdate)))

        val onLeftColorUpdate: (Color) -> Unit = { leftColor -> swingPanel.leftColor = leftColor }
        leftColorPublisher.subscribe(Subscriber(eventQueueWrapper(onLeftColorUpdate)))

        val onRightColorUpdate: (Color) -> Unit = { rightColor -> swingPanel.rightColor = rightColor }
        rightColorPublisher.subscribe(Subscriber(eventQueueWrapper(onRightColorUpdate)))

        val onNumBucketsPerSideUpdate: (Int) -> Unit = { numBucketsPerSide -> swingPanel.numBucketsPerSide = numBucketsPerSide }
        numBucketsPerSidePublisher.subscribe(Subscriber(eventQueueWrapper(onNumBucketsPerSideUpdate)))

        val onDotsUpdate: (List<Dot>) -> Unit = { swingPanel.dots = it }
        dotsPublisher.subscribe(Subscriber(eventQueueWrapper(onDotsUpdate)))

        val onLeftToWinUpdate: (Number) -> Unit = { leftToWin -> swingPanel.leftToWin = leftToWin }
        if (leftToWinPublisher != null) {
            leftToWinPublisher.subscribe(Subscriber(eventQueueWrapper(onLeftToWinUpdate)))
        } else {
            onLeftToWinUpdate(Double.POSITIVE_INFINITY)
        }

        val onRightToWinUpdate: (Number) -> Unit = { rightToWin -> swingPanel.rightToWin = rightToWin }
        if (rightToWinPublisher != null) {
            rightToWinPublisher.subscribe(Subscriber(eventQueueWrapper(onRightToWinUpdate)))
        } else {
            onRightToWinUpdate(Double.NEGATIVE_INFINITY)
        }

        val onTicksUpdate: (List<Tick>) -> Unit = { t -> swingPanel.ticks = t }
        if (ticksPublisher != null) {
            ticksPublisher.subscribe(Subscriber(eventQueueWrapper(onTicksUpdate)))
        } else {
            onTicksUpdate(emptyList())
        }

        val onOuterLabelsUpdate: (List<OuterLabel>) -> Unit = { l -> swingPanel.outerLabels = l }
        if (outerLabelsPublisher != null) {
            outerLabelsPublisher.subscribe(Subscriber(eventQueueWrapper(onOuterLabelsUpdate)))
        } else {
            onOuterLabelsUpdate(emptyList())
        }
    }
}
