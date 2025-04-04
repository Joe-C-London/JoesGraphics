package com.joecollins.graphics.components

import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.map
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.LayoutManager
import java.awt.Point
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.util.concurrent.Flow
import javax.swing.JPanel
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class HemicycleFrame(
    headerPublisher: Flow.Publisher<out String?>,
    rowsPublisher: Flow.Publisher<out List<Int>>? = null,
    dotsPublisher: Flow.Publisher<out List<Dot>>,
    leftSeatBarPublisher: Flow.Publisher<out List<Bar>>? = null,
    leftSeatBarLabelPublisher: Flow.Publisher<out String>? = null,
    rightSeatBarPublisher: Flow.Publisher<out List<Bar>>? = null,
    rightSeatBarLabelPublisher: Flow.Publisher<out String>? = null,
    middleSeatBarPublisher: Flow.Publisher<out List<Bar>>? = null,
    middleSeatBarLabelPublisher: Flow.Publisher<out String>? = null,
    leftChangeBarPublisher: Flow.Publisher<out List<Bar>>? = null,
    leftChangeBarStartPublisher: Flow.Publisher<out Int>? = null,
    leftChangeBarLabelPublisher: Flow.Publisher<out String>? = null,
    rightChangeBarPublisher: Flow.Publisher<out List<Bar>>? = null,
    rightChangeBarStartPublisher: Flow.Publisher<out Int>? = null,
    rightChangeBarLabelPublisher: Flow.Publisher<out String>? = null,
) : GraphicsFrame(
    headerPublisher = headerPublisher,
) {
    private val barsPanel = BarPanel()
    private val dotsPanel = DotsPanel()

    private inner class Layout : LayoutManager {
        override fun addLayoutComponent(name: String, comp: Component) {}
        override fun removeLayoutComponent(comp: Component) {}
        override fun preferredLayoutSize(parent: Container): Dimension? = null

        override fun minimumLayoutSize(parent: Container): Dimension? = null

        override fun layoutContainer(parent: Container) {
            var mid = 0
            if (barsPanel.hasSeats()) {
                mid = min(25, parent.height / 10)
            }
            if (barsPanel.hasChange()) {
                mid = min(50, parent.height / 5)
            }
            barsPanel.setLocation(0, 0)
            barsPanel.setSize(parent.width, mid)
            dotsPanel.setLocation(0, mid)
            dotsPanel.setSize(parent.width, parent.height - mid)
        }
    }

    internal val numRows: Int
        get() = dotsPanel.rows.size

    internal fun getRowCount(rowNum: Int): Int = dotsPanel.rows[rowNum]

    internal val numDots: Int
        get() = dotsPanel.dots.size

    internal fun getDotColor(dotNum: Int): Color = dotsPanel.dots[dotNum].color

    internal fun getDotBorder(dotNum: Int): Color? = dotsPanel.dots[dotNum].border

    internal val leftSeatBarCount: Int
        get() = barsPanel.leftSeatBars.let { it ?: emptyList() }.size

    internal fun getLeftSeatBarColor(idx: Int): Color = barsPanel.leftSeatBars.let { it ?: emptyList() }[idx].color

    internal fun getLeftSeatBarSize(idx: Int): Int = barsPanel.leftSeatBars.let { it ?: emptyList() }[idx].size

    internal fun getLeftSeatBarLabel(): String = barsPanel.leftSeatBarLabel

    internal val rightSeatBarCount: Int
        get() = barsPanel.rightSeatBars.let { it ?: emptyList() }.size

    internal fun getRightSeatBarColor(idx: Int): Color = barsPanel.rightSeatBars.let { it ?: emptyList() }[idx].color

    internal fun getRightSeatBarSize(idx: Int): Int = barsPanel.rightSeatBars.let { it ?: emptyList() }[idx].size

    internal fun getRightSeatBarLabel(): String = barsPanel.rightSeatBarLabel

    internal val middleSeatBarCount: Int
        get() = barsPanel.middleSeatBars.let { it ?: emptyList() }.size

    internal fun getMiddleSeatBarColor(idx: Int): Color = barsPanel.middleSeatBars.let { it ?: emptyList() }[idx].color

    internal fun getMiddleSeatBarSize(idx: Int): Int = barsPanel.middleSeatBars.let { it ?: emptyList() }[idx].size

    internal fun getMiddleSeatBarLabel(): String = barsPanel.middleSeatBarLabel

    internal val leftChangeBarCount: Int
        get() = barsPanel.leftChangeBars.let { it ?: emptyList() }.size

    internal fun getLeftChangeBarColor(idx: Int): Color = barsPanel.leftChangeBars.let { it ?: emptyList() }[idx].color

    internal fun getLeftChangeBarSize(idx: Int): Int = barsPanel.leftChangeBars.let { it ?: emptyList() }[idx].size

    internal fun getLeftChangeBarStart(): Int = barsPanel.leftChangeBarStart

    internal fun getLeftChangeBarLabel(): String = barsPanel.leftChangeBarLabel

    internal val rightChangeBarCount: Int
        get() = barsPanel.rightChangeBars.let { it ?: emptyList() }.size

    internal fun getRightChangeBarColor(idx: Int): Color = barsPanel.rightChangeBars.let { it ?: emptyList() }[idx].color

    internal fun getRightChangeBarSize(idx: Int): Int = barsPanel.rightChangeBars.let { it ?: emptyList() }[idx].size

    internal fun getRightChangeBarStart(): Int = barsPanel.rightChangeBarStart

    internal fun getRightChangeBarLabel(): String = barsPanel.rightChangeBarLabel

    class Bar(val color: Color, val size: Int)

    private inner class BarPanel : JPanel() {
        var leftSeatBars: List<Bar>? = null
        var leftSeatBarLabel = ""
        var rightSeatBars: List<Bar>? = null
        var rightSeatBarLabel = ""
        var middleSeatBars: List<Bar>? = null
        var middleSeatBarLabel = ""
        var leftChangeBars: List<Bar>? = null
        var leftChangeBarLabel = ""
        var leftChangeBarStart = 0
        var rightChangeBars: List<Bar>? = null
        var rightChangeBarLabel = ""
        var rightChangeBarStart = 0

        fun hasSeats(): Boolean = (
            leftSeatBars != null ||
                middleSeatBars != null ||
                rightSeatBars != null ||
                leftSeatBarLabel.isNotEmpty() ||
                middleSeatBarLabel.isNotEmpty() ||
                rightSeatBarLabel.isNotEmpty()
            )

        fun hasChange(): Boolean = (
            leftChangeBars != null ||
                rightChangeBars != null ||
                leftChangeBarLabel.isNotEmpty() ||
                rightChangeBarLabel.isNotEmpty()
            )

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                )
            g.setColor(Color.BLACK)
            g.drawLine(width / 2, 0, width / 2, height)
            g.setFont(StandardFont.readBoldFont(height / (if (hasChange()) 2 else 1) * 4 / 5))
            if (hasSeats()) {
                paintSeatBars(g)
            }
            if (hasChange()) {
                paintChangeBars(g)
            }
        }

        private fun paintSeatBars(g: Graphics) {
            val height = height / if (hasChange()) 2 else 1
            val seatBaseline = height * 4 / 5
            g.color = if (leftSeatBars.isNullOrEmpty()) Color.BLACK else leftSeatBars!![0].color
            g.drawString(leftSeatBarLabel, 5, seatBaseline)
            g.color = if (rightSeatBars.isNullOrEmpty()) Color.BLACK else rightSeatBars!![0].color
            val rightWidth = g.fontMetrics.stringWidth(rightSeatBarLabel)
            val rightLeft = width - rightWidth - 5
            g.drawString(rightSeatBarLabel, rightLeft, seatBaseline)
            g.color = if (middleSeatBars.isNullOrEmpty()) Color.BLACK else middleSeatBars!![0].color
            val middleWidth = g.fontMetrics.stringWidth(middleSeatBarLabel)
            val middleLeft = getMiddleStartPosition(0) - middleWidth / 2
            g.drawString(middleSeatBarLabel, middleLeft, seatBaseline)
            val seatBarTop = height / 10
            val seatBarHeight = height * 4 / 5
            var leftSoFar = 0
            for (bar in (leftSeatBars ?: emptyList())) {
                val start = getLeftPosition(leftSoFar)
                val end = getLeftPosition(leftSoFar + bar.size)
                g.color = bar.color
                g.fillRect(start, seatBarTop, end - start, seatBarHeight)
                leftSoFar += bar.size
            }
            val leftClip: Shape = Rectangle(0, seatBarTop, getLeftPosition(leftSoFar), seatBarHeight)
            var rightSoFar = 0
            for (bar in (rightSeatBars ?: emptyList())) {
                val start = getRightPosition(rightSoFar + bar.size)
                val end = getRightPosition(rightSoFar)
                g.color = bar.color
                g.fillRect(start, seatBarTop, end - start, seatBarHeight)
                rightSoFar += bar.size
            }
            val rightClip: Shape = Rectangle(
                getRightPosition(rightSoFar),
                seatBarTop,
                width - getRightPosition(rightSoFar),
                seatBarHeight,
            )
            var middleSoFar = 0
            for (bar in (middleSeatBars ?: emptyList())) {
                g.color = bar.color
                val startL = getMiddleStartPosition(middleSoFar + bar.size)
                val endL = getMiddleStartPosition(middleSoFar)
                val startR = getMiddleEndPosition(middleSoFar)
                val endR = getMiddleEndPosition(middleSoFar + bar.size)
                g.fillRect(startL, seatBarTop, endL - startL, seatBarHeight)
                g.fillRect(startR, seatBarTop, endR - startR, seatBarHeight)
                middleSoFar += bar.size
            }
            val middleClip: Shape = Rectangle(
                getMiddleStartPosition(middleSoFar),
                seatBarTop,
                getMiddleEndPosition(middleSoFar) - getMiddleStartPosition(middleSoFar),
                seatBarHeight,
            )
            val oldClip = g.clip
            val newClip = Area()
            newClip.add(Area(leftClip))
            newClip.add(Area(rightClip))
            newClip.add(Area(middleClip))
            g.clip = newClip
            g.color = Color.WHITE
            g.drawString(leftSeatBarLabel, 5, seatBaseline)
            g.drawString(rightSeatBarLabel, rightLeft, seatBaseline)
            g.drawString(middleSeatBarLabel, middleLeft, seatBaseline)
            g.clip = oldClip
        }

        private fun paintChangeBars(g: Graphics) {
            val height = height / if (hasChange()) 2 else 1
            val seatBaseline = height * 4 / 5 + height
            g.color = if (leftChangeBars.isNullOrEmpty()) Color.BLACK else leftChangeBars!![0].color
            val leftLeft = getLeftPosition(leftChangeBarStart) + 5
            g.drawString(leftChangeBarLabel, leftLeft, seatBaseline)
            g.color = if (rightChangeBars.isNullOrEmpty()) Color.BLACK else rightChangeBars!![0].color
            val rightWidth = g.fontMetrics.stringWidth(rightChangeBarLabel)
            val rightLeft = getRightPosition(rightChangeBarStart) - rightWidth - 5
            g.drawString(rightChangeBarLabel, rightLeft, seatBaseline)
            val changeBarHeight = height * 4 / 5
            val changeBarTop = height / 10 + height
            val changeBarMid = changeBarTop + changeBarHeight / 2
            val changeBarBottom = changeBarTop + changeBarHeight
            val sideFunc: (Int, Int) -> Int = { zero, point ->
                if (point > zero) {
                    max(zero, point - changeBarHeight / 2)
                } else {
                    min(zero, point + changeBarHeight / 2)
                }
            }
            var leftSoFar = leftChangeBarStart
            val leftBase = getLeftPosition(leftChangeBarStart)
            for (bar in (leftChangeBars ?: emptyList())) {
                val start = getLeftPosition(leftSoFar)
                val end = getLeftPosition(leftSoFar + bar.size)
                val startSide = sideFunc(leftBase, start)
                val endSide = sideFunc(leftBase, end)
                g.color = bar.color
                val points = listOf(
                    Point(startSide, changeBarTop),
                    Point(start, changeBarMid),
                    Point(startSide, changeBarBottom),
                    Point(endSide, changeBarBottom),
                    Point(end, changeBarMid),
                    Point(endSide, changeBarTop),
                )
                g.fillPolygon(
                    points.map { it.getX().toInt() }.toIntArray(),
                    points.map { it.getY().toInt() }.toIntArray(),
                    points.size,
                )
                leftSoFar += bar.size
            }
            val leftTip = getLeftPosition(leftSoFar)
            val leftSize = sideFunc(leftBase, leftTip)
            val leftClip: Shape = Polygon(
                intArrayOf(leftBase, leftBase, leftSize, leftTip, leftSize),
                intArrayOf(
                    changeBarTop,
                    changeBarBottom,
                    changeBarBottom,
                    changeBarMid,
                    changeBarTop,
                ),
                5,
            )
            var rightSoFar = rightChangeBarStart
            val rightBase = getRightPosition(rightChangeBarStart)
            for (bar in (rightChangeBars ?: emptyList())) {
                val start = getRightPosition(rightSoFar)
                val end = getRightPosition(rightSoFar + bar.size)
                val startSide = sideFunc(rightBase, start)
                val endSide = sideFunc(rightBase, end)
                g.color = bar.color
                val points = listOf(
                    Point(startSide, changeBarTop),
                    Point(start, changeBarMid),
                    Point(startSide, changeBarBottom),
                    Point(endSide, changeBarBottom),
                    Point(end, changeBarMid),
                    Point(endSide, changeBarTop),
                )
                g.fillPolygon(
                    points.map { it.getX().toInt() }.toIntArray(),
                    points.map { it.getY().toInt() }.toIntArray(),
                    points.size,
                )
                rightSoFar += bar.size
            }
            val rightTip = getRightPosition(rightSoFar)
            val rightSize = sideFunc(rightBase, rightTip)
            val rightClip: Shape = Polygon(
                intArrayOf(rightBase, rightBase, rightSize, rightTip, rightSize),
                intArrayOf(
                    changeBarTop,
                    changeBarBottom,
                    changeBarBottom,
                    changeBarMid,
                    changeBarTop,
                ),
                5,
            )
            val oldClip = g.clip
            val newClip = Area()
            newClip.add(Area(leftClip))
            newClip.add(Area(rightClip))
            g.clip = newClip
            g.color = Color.WHITE
            g.drawString(leftChangeBarLabel, leftLeft, seatBaseline)
            g.drawString(rightChangeBarLabel, rightLeft, seatBaseline)
            g.clip = oldClip
        }

        private fun getLeftPosition(seats: Int): Int = getSize(seats)

        private fun getRightPosition(seats: Int): Int = width - getSize(seats)

        private fun getMiddleStartPosition(seats: Int): Int {
            val midSize = getSize(middleSeatBars?.sumOf { it.size } ?: 0)
            val leftSize = getSize(leftSeatBars?.sumOf { it.size } ?: 0)
            val rightSize = getSize(rightSeatBars?.sumOf { it.size } ?: 0)
            val midPoint: Int = when {
                leftSize + midSize / 2 > width / 2 -> {
                    leftSize + midSize / 2
                }
                rightSize + midSize / 2 > width / 2 -> {
                    width - rightSize - midSize / 2
                }
                else -> {
                    width / 2
                }
            }
            return midPoint - getSize(seats) / 2
        }

        private fun getMiddleEndPosition(seats: Int): Int = getMiddleStartPosition(seats) + getSize(seats)

        private fun getSize(seats: Int): Int = (1.0 * width * seats / numDots).roundToInt()

        init {
            background = Color.WHITE
        }
    }

    class Dot(val color: Color, val border: Color?)

    private inner class DotsPanel : JPanel() {
        var rows: List<Int> = ArrayList()
        var dots: List<Dot> = ArrayList()
        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                )
            var dFrac = 1.0 / (rows.size - 0.5)
            for (rowsFromInner in rows.indices) {
                val rowsFromOuter = rows.size - rowsFromInner - 1
                val dotsInRow = rows[rowsFromInner]
                val dForRow = Math.PI / (dotsInRow - 1) / (1 + rowsFromOuter * Math.PI / (dotsInRow - 1))
                dFrac = min(dFrac, dForRow)
            }
            val arcLimit = min(height, width / 2)
            val r = 1.0 * arcLimit / (1 + dFrac)
            val d = r * dFrac
            val arcY = (height - d / 2).roundToInt()
            g.setColor(Color.BLACK)
            g.drawLine(width / 2, 0, width / 2, (height - d / 2).roundToInt())
            val rowStartIndexes = run {
                var total = 0
                rows.map { row ->
                    val prevTotal = total
                    total += row
                    prevTotal
                }
            }
            val originalTransform = g.transform
            for (rowsFromInner in rows.indices) {
                val rowsFromOuter = rows.size - rowsFromInner - 1
                val dotsInRow = rows[rowsFromInner]
                val firstDot = rowStartIndexes[rowsFromInner]
                for (dotNum in 0 until dotsInRow) {
                    val dot = dots[firstDot + dotNum]
                    g.transform = createRotationTransform(1.0 * dotNum / (dotsInRow - 1), originalTransform, arcY)
                    val x = width / 2
                    val y = height - (r + (0.5 - rowsFromOuter) * d).roundToInt()
                    val rad = (d / 2 * 4 / 5).roundToInt()
                    g.stroke = BasicStroke(max(1, rad / 5).toFloat())
                    g.setColor(dot.color)
                    g.fillOval(x - rad, y - rad, 2 * rad, 2 * rad)
                    if (dot.border != null) {
                        g.setColor(dot.border)
                        g.drawOval(x - rad, y - rad, 2 * rad, 2 * rad)
                    }
                    g.transform = originalTransform
                }
            }
        }

        private fun createRotationTransform(
            frac: Double,
            originalTransform: AffineTransform,
            arcY: Int,
        ): AffineTransform {
            val arcAngle = Math.PI * (frac - 0.5)
            val newTransform = AffineTransform(originalTransform)
            newTransform.rotate(arcAngle, (width / 2).toDouble(), arcY.toDouble())
            return newTransform
        }

        init {
            background = Color.WHITE
        }
    }

    init {
        val panel = JPanel()
        addCenter(panel)
        panel.layout = Layout()
        panel.add(barsPanel)
        panel.add(dotsPanel)

        val onRowsUpdate: (List<Int>) -> Unit = { r ->
            dotsPanel.rows = r
            dotsPanel.repaint()
        }
        (rowsPublisher ?: dotsPublisher.map { d -> listOf(d.size) }).subscribe(Subscriber(eventQueueWrapper(onRowsUpdate)))

        val onDotsUpdate: (List<Dot>) -> Unit = { d ->
            dotsPanel.dots = d
            dotsPanel.repaint()
        }
        dotsPublisher.subscribe(Subscriber(eventQueueWrapper(onDotsUpdate)))

        val onLeftSeatBarUpdate: (List<Bar>) -> Unit = { b ->
            barsPanel.leftSeatBars = b
            barsPanel.repaint()
        }
        if (leftSeatBarPublisher != null) {
            leftSeatBarPublisher.subscribe(Subscriber(eventQueueWrapper(onLeftSeatBarUpdate)))
        }

        val onLeftSeatBarLabelUpdate: (String) -> Unit = { label ->
            barsPanel.leftSeatBarLabel = label
            barsPanel.repaint()
        }
        if (leftSeatBarLabelPublisher != null) {
            leftSeatBarLabelPublisher.subscribe(Subscriber(eventQueueWrapper(onLeftSeatBarLabelUpdate)))
        } else {
            onLeftSeatBarLabelUpdate("")
        }

        val onRightSeatBarUpdate: (List<Bar>) -> Unit = { b ->
            barsPanel.rightSeatBars = b
            barsPanel.repaint()
        }
        if (rightSeatBarPublisher != null) {
            rightSeatBarPublisher.subscribe(Subscriber(eventQueueWrapper(onRightSeatBarUpdate)))
        }

        val onRightSeatBarLabelUpdate: (String) -> Unit = { label ->
            barsPanel.rightSeatBarLabel = label
            barsPanel.repaint()
        }
        if (rightSeatBarLabelPublisher != null) {
            rightSeatBarLabelPublisher.subscribe(Subscriber(eventQueueWrapper(onRightSeatBarLabelUpdate)))
        } else {
            onRightSeatBarLabelUpdate("")
        }

        val onMiddleSeatBarUpdate: (List<Bar>) -> Unit = { b ->
            barsPanel.middleSeatBars = b
            barsPanel.repaint()
        }
        if (middleSeatBarPublisher != null) {
            middleSeatBarPublisher.subscribe(Subscriber(eventQueueWrapper(onMiddleSeatBarUpdate)))
        }

        val onMiddleSeatBarLabelUpdate: (String) -> Unit = { label ->
            barsPanel.middleSeatBarLabel = label
            barsPanel.repaint()
        }
        if (middleSeatBarLabelPublisher != null) {
            middleSeatBarLabelPublisher.subscribe(Subscriber(eventQueueWrapper(onMiddleSeatBarLabelUpdate)))
        } else {
            onMiddleSeatBarLabelUpdate("")
        }

        val onLeftChangeBarUpdate: (List<Bar>) -> Unit = { b ->
            barsPanel.leftChangeBars = b
            barsPanel.repaint()
        }
        if (leftChangeBarPublisher != null) {
            leftChangeBarPublisher.subscribe(Subscriber(eventQueueWrapper(onLeftChangeBarUpdate)))
        }

        val onLeftChangeBarStartUpdate: (Int) -> Unit = { start ->
            barsPanel.leftChangeBarStart = start
            barsPanel.repaint()
        }
        if (leftChangeBarStartPublisher != null) {
            leftChangeBarStartPublisher.subscribe(Subscriber(eventQueueWrapper(onLeftChangeBarStartUpdate)))
        } else {
            onLeftChangeBarStartUpdate(0)
        }

        val onLeftChangeBarLabelUpdate: (String) -> Unit = { label ->
            barsPanel.leftChangeBarLabel = label
            barsPanel.repaint()
        }
        if (leftChangeBarLabelPublisher != null) {
            leftChangeBarLabelPublisher.subscribe(Subscriber(eventQueueWrapper(onLeftChangeBarLabelUpdate)))
        } else {
            onLeftChangeBarLabelUpdate("")
        }

        val onRightChangeBarUpdate: (List<Bar>) -> Unit = { b ->
            barsPanel.rightChangeBars = b
            barsPanel.repaint()
        }
        if (rightChangeBarPublisher != null) {
            rightChangeBarPublisher.subscribe(Subscriber(eventQueueWrapper(onRightChangeBarUpdate)))
        }

        val onRightChangeBarStartUpdate: (Int) -> Unit = { start ->
            barsPanel.rightChangeBarStart = start
            barsPanel.repaint()
        }
        if (rightChangeBarStartPublisher != null) {
            rightChangeBarStartPublisher.subscribe(Subscriber(eventQueueWrapper(onRightChangeBarStartUpdate)))
        } else {
            onRightChangeBarStartUpdate(0)
        }

        val onRightChangeBarLabelUpdate: (String) -> Unit = { label ->
            barsPanel.rightChangeBarLabel = label
            barsPanel.repaint()
        }
        if (rightChangeBarLabelPublisher != null) {
            rightChangeBarLabelPublisher.subscribe(Subscriber(eventQueueWrapper(onRightChangeBarLabelUpdate)))
        } else {
            onRightChangeBarLabelUpdate("")
        }
    }
}
