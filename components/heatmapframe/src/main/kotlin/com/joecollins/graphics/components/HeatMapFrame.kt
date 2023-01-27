package com.joecollins.graphics.components

import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import org.jetbrains.annotations.TestOnly
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.LayoutManager
import java.awt.Point
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.geom.Area
import java.util.concurrent.Flow
import javax.swing.JPanel
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class HeatMapFrame(
    headerPublisher: Flow.Publisher<out String?>,
    numRowsPublisher: Flow.Publisher<out Int>,
    squaresPublisher: Flow.Publisher<out List<Square>>,
    seatBarsPublisher: Flow.Publisher<out List<Bar>>? = null,
    seatBarLabelPublisher: Flow.Publisher<out String>? = null,
    changeBarsPublisher: Flow.Publisher<out List<Bar>>? = null,
    changeBarLabelPublisher: Flow.Publisher<out String>? = null,
    changeBarStartPublisher: Flow.Publisher<out Int>? = null,
    borderColorPublisher: Flow.Publisher<out Color>? = null,
) : GraphicsFrame(
    headerPublisher = headerPublisher,
    borderColorPublisher = borderColorPublisher,
) {
    private val barsPanel = SeatBarPanel()
    private val squaresPanel = SquaresPanel()

    private inner class Layout : LayoutManager {
        override fun addLayoutComponent(name: String, comp: Component) {}
        override fun removeLayoutComponent(comp: Component) {}
        override fun preferredLayoutSize(parent: Container): Dimension {
            return Dimension(1024, 512)
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            return Dimension(100, 50)
        }

        override fun layoutContainer(parent: Container) {
            var mid = 0
            if (barsPanel.hasSeats()) {
                mid = min(20, parent.height / 6)
            }
            if (barsPanel.hasChange()) {
                mid = min(40, parent.height / 3)
            }
            barsPanel.setLocation(0, 0)
            barsPanel.setSize(parent.width, mid)
            squaresPanel.setLocation(0, mid)
            squaresPanel.setSize(parent.width, parent.height - mid)
        }
    }

    val numRows: Int
        @TestOnly get() = squaresPanel.numRows

    val numSquares: Int
        @TestOnly get() = squaresPanel.squares.size

    @TestOnly fun getSquareBorder(index: Int): Color? {
        return squaresPanel.squares[index].borderColor
    }

    @TestOnly fun getSquareFill(index: Int): Color {
        return squaresPanel.squares[index].fillColor
    }

    val seatBarCount: Int
        @TestOnly get() = barsPanel.seatBars.size

    @TestOnly fun getSeatBarColor(index: Int): Color {
        return barsPanel.seatBars[index].color
    }

    @TestOnly fun getSeatBarSize(index: Int): Int {
        return barsPanel.seatBars[index].size
    }

    val seatBarLabel: String
        @TestOnly get() = barsPanel.seatBarLabel

    val changeBarCount: Int
        @TestOnly get() = barsPanel.changeBars.size

    @TestOnly fun getChangeBarColor(index: Int): Color {
        return barsPanel.changeBars[index].color
    }

    @TestOnly fun getChangeBarSize(index: Int): Int {
        return barsPanel.changeBars[index].size
    }

    val changeBarLabel: String
        @TestOnly get() = barsPanel.changeBarLabel

    val changeBarStart: Int
        @TestOnly get() = barsPanel.changeBarStart

    private inner class SeatBarPanel : JPanel() {
        var seatBars: List<Bar> = emptyList()
            set(value) {
                field = value
                repaint()
                EventQueue.invokeLater {
                    this@HeatMapFrame.mainPanel.let {
                        it.invalidate()
                        it.revalidate()
                        it.repaint()
                    }
                }
            }

        var seatBarLabel: String = ""
            set(value) {
                field = value
                repaint()
            }

        var changeBars: List<Bar> = emptyList()
            set(value) {
                field = value
                repaint()
                EventQueue.invokeLater {
                    this@HeatMapFrame.mainPanel.let {
                        it.invalidate()
                        it.revalidate()
                        it.repaint()
                    }
                }
            }

        var changeBarLabel: String = ""
            set(value) {
                field = value
                repaint()
            }

        var changeBarStart: Int = 0
            set(value) {
                field = value
                repaint()
            }

        fun hasSeats(): Boolean {
            return seatBars.isNotEmpty()
        }

        fun hasChange(): Boolean {
            return changeBars.isNotEmpty()
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(
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
            g.color = if (seatBars.isEmpty()) Color.BLACK else seatBars[0].color
            g.drawString(seatBarLabel, 5, seatBaseline)
            val seatBarTop = height / 10
            val seatBarHeight = height * 4 / 5
            var leftSoFar = 0
            for (bar in seatBars) {
                val start = getLeftPosition(leftSoFar)
                val end = getLeftPosition(leftSoFar + bar.size)
                g.color = bar.color
                g.fillRect(start, seatBarTop, end - start, seatBarHeight)
                leftSoFar += bar.size
            }
            val leftClip: Shape = Rectangle(0, seatBarTop, getLeftPosition(leftSoFar), seatBarHeight)
            val oldClip = g.clip
            val newClip = Area()
            newClip.add(Area(leftClip))
            g.clip = newClip
            g.color = Color.WHITE
            g.drawString(seatBarLabel, 5, seatBaseline)
            g.clip = oldClip
        }

        private fun paintChangeBars(g: Graphics) {
            val height = height / if (hasChange()) 2 else 1
            val seatBaseline = height * 4 / 5 + height
            g.color = if (changeBars.isEmpty()) Color.BLACK else changeBars[0].color
            var leftLeft = getLeftPosition(changeBarStart) + 5
            if (changeBars.sumOf { it.size } < 0) {
                leftLeft -= g.fontMetrics.stringWidth(changeBarLabel) + 10
            }
            g.drawString(changeBarLabel, leftLeft, seatBaseline)
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
            var leftSoFar = changeBarStart
            val leftBase = getLeftPosition(changeBarStart)
            val newClip = Area()
            for (bar in changeBars) {
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
                val polygon = Polygon(
                    points.map { it.getX().toInt() }.toIntArray(),
                    points.map { it.getY().toInt() }.toIntArray(),
                    points.size,
                )
                g.fillPolygon(polygon)
                newClip.add(Area(polygon))
                leftSoFar += bar.size
            }
            val oldClip = g.clip
            g.clip = newClip
            g.color = Color.WHITE
            g.drawString(changeBarLabel, leftLeft, seatBaseline)
            g.clip = oldClip
        }

        private fun getLeftPosition(seats: Int): Int {
            return getSize(seats)
        }

        private fun getSize(seats: Int): Int {
            return (1.0 * width * seats / numSquares.coerceAtLeast(1)).roundToInt()
        }

        init {
            background = Color.WHITE
        }
    }

    class Bar(val color: Color = Color.WHITE, val size: Int = 0)

    private inner class SquaresPanel : JPanel() {
        var numRows: Int = 1
            set(value) {
                field = value
                repaint()
            }

        var squares: List<Square> = emptyList()
            set(value) {
                field = value
                repaint()
            }

        var label: String? = null
            set(label) {
                field = label
                repaint()
            }

        private val numCols: Int
            get() {
                return ceil(1.0 * squares.size / numRows).toInt()
            }

        private val squareSize: Int
            get() {
                return min((width - 10) / numCols, (height - 10) / numRows)
            }

        private val farLeft: Int
            get() {
                return (width - squareSize * numCols) / 2
            }

        private val farTop: Int
            get() {
                return (height - squareSize * numRows) / 2
            }

        private val padding: Int
            get() {
                val padding: Int = if (squares.size % numRows != 0) {
                    ceil(0.5 * (numRows - squares.size % numRows)).toInt()
                } else {
                    0
                }
                return padding
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
            g.setColor(Color.BLACK)
            g.drawLine(width / 2, 0, width / 2, height)
            if (squares.isEmpty()) return
            for (i in squares.indices) {
                val square = squares[i]
                val index = i + padding
                val row = index % numRows
                val col = index / numRows
                val left = farLeft + col * squareSize
                val top = farTop + row * squareSize
                g.setColor(square.fillColor)
                g.fillRect(left, top, squareSize, squareSize)
                val borderSize = max(2, squareSize / 10)
                g.stroke = BasicStroke(borderSize.toFloat())
                if (square.borderColor != null) {
                    g.setColor(square.borderColor)
                    g.drawRect(
                        left + borderSize / 2,
                        top + borderSize / 2,
                        squareSize - borderSize,
                        squareSize - borderSize,
                    )
                }
                g.stroke = BasicStroke(1f)
                g.setColor(Color.BLACK)
                g.drawRect(left, top, squareSize, squareSize)
            }
            if (label != null) {
                g.color = Color.BLACK
                g.font = StandardFont.readNormalFont(10)
                g.drawString(label ?: "", 0, height - 2)
            }
        }

        init {
            background = Color.WHITE

            addMouseListener(object : MouseAdapter() {
                override fun mouseExited(e: MouseEvent) {
                    label = null
                }
            })
            addMouseMotionListener(object : MouseMotionAdapter() {
                override fun mouseMoved(e: MouseEvent) {
                    val point = e.point
                    val row = (point.y - farTop) / squareSize
                    val col = (point.x - farLeft) / squareSize
                    val index = col * numRows + row - padding
                    label = if (index >= 0 && index < squares.size) {
                        "${index + 1}: ${squares[index].label}"
                    } else {
                        null
                    }
                }
            })
        }
    }

    internal fun moveMouse(x: Int, y: Int) {
        val event = MouseEvent(this, 1, System.currentTimeMillis(), 0, x, y, 0, false, 0)
        if (x < 0 || y < 0) {
            squaresPanel.mouseListeners.forEach { it.mouseExited(event) }
        } else {
            squaresPanel.mouseMotionListeners.forEach { it.mouseMoved(event) }
        }
    }

    class Square(val borderColor: Color? = null, val fillColor: Color = Color.WHITE, val label: String? = null)

    private val mainPanel: JPanel = JPanel()

    init {
        add(mainPanel, BorderLayout.CENTER)
        mainPanel.layout = Layout()
        mainPanel.add(barsPanel)
        mainPanel.add(squaresPanel)

        val onNumRowsUpdate: (Int) -> Unit = { numRows -> squaresPanel.numRows = numRows }
        numRowsPublisher.subscribe(Subscriber(eventQueueWrapper(onNumRowsUpdate)))

        val onSquaresUpdate: (List<Square>) -> Unit = { squares -> squaresPanel.squares = squares }
        squaresPublisher.subscribe(Subscriber(eventQueueWrapper(onSquaresUpdate)))

        val onSeatBarsUpdate: (List<Bar>) -> Unit = { bars -> barsPanel.seatBars = bars }
        if (seatBarsPublisher != null) {
            seatBarsPublisher.subscribe(Subscriber(eventQueueWrapper(onSeatBarsUpdate)))
        } else {
            onSeatBarsUpdate(emptyList())
        }

        val onSeatBarLabelUpdate: (String) -> Unit = { label -> barsPanel.seatBarLabel = label }
        if (seatBarLabelPublisher != null) {
            seatBarLabelPublisher.subscribe(Subscriber(eventQueueWrapper(onSeatBarLabelUpdate)))
        } else {
            onSeatBarLabelUpdate("")
        }

        val onChangeBarsUpdate: (List<Bar>) -> Unit = { bars -> barsPanel.changeBars = bars }
        if (changeBarsPublisher != null) {
            changeBarsPublisher.subscribe(Subscriber(eventQueueWrapper(onChangeBarsUpdate)))
        } else {
            onChangeBarsUpdate(emptyList())
        }

        val onChangeBarLabelUpdate: (String) -> Unit = { label -> barsPanel.changeBarLabel = label }
        if (changeBarLabelPublisher != null) {
            changeBarLabelPublisher.subscribe(Subscriber(eventQueueWrapper(onChangeBarLabelUpdate)))
        } else {
            onChangeBarLabelUpdate("")
        }

        val onChangeBarStartUpdate: (Int) -> Unit = { start -> barsPanel.changeBarStart = start }
        if (changeBarStartPublisher != null) {
            changeBarStartPublisher.subscribe(Subscriber(eventQueueWrapper(onChangeBarStartUpdate)))
        } else {
            onChangeBarStartUpdate(0)
        }
    }
}
