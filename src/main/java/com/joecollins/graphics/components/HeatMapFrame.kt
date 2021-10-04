package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.graphics.utils.StandardFont
import java.awt.BasicStroke
import java.awt.BorderLayout
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
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.geom.Area
import javax.swing.JPanel
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class HeatMapFrame(
    headerBinding: Binding<String?>,
    numRowsBinding: Binding<Int>,
    squaresBinding: Binding<List<Square>>,
    seatBarsBinding: Binding<List<Bar>>? = null,
    seatBarLabelBinding: Binding<String>? = null,
    changeBarsBinding: Binding<List<Bar>>? = null,
    changeBarLabelBinding: Binding<String>? = null,
    changeBarStartBinding: Binding<Int>? = null,
    borderColorBinding: Binding<Color>? = null
) : GraphicsFrame(
    headerBinding = headerBinding,
    borderColorBinding = borderColorBinding
) {
    private val barsPanel = SeatBarPanel()
    private val squaresPanel = SquaresPanel()

    private inner class Layout : LayoutManager {
        override fun addLayoutComponent(name: String, comp: Component) {}
        override fun removeLayoutComponent(comp: Component) {}
        override fun preferredLayoutSize(parent: Container): Dimension? {
            return null
        }

        override fun minimumLayoutSize(parent: Container): Dimension? {
            return null
        }

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
            squaresPanel.setLocation(0, mid)
            squaresPanel.setSize(parent.width, parent.height - mid)
        }
    }

    internal val numRows: Int
        get() = squaresPanel.numRows

    internal val numSquares: Int
        get() = squaresPanel.squares.size

    internal fun getSquareBorder(index: Int): Color? {
        return squaresPanel.squares[index].borderColor
    }

    internal fun getSquareFill(index: Int): Color {
        return squaresPanel.squares[index].fillColor
    }

    internal val seatBarCount: Int
        get() = barsPanel.seatBars.size

    internal fun getSeatBarColor(index: Int): Color {
        return barsPanel.seatBars[index].color
    }

    internal fun getSeatBarSize(index: Int): Int {
        return barsPanel.seatBars[index].size
    }

    internal val seatBarLabel: String
        get() = barsPanel.seatBarLabel

    internal val changeBarCount: Int
        get() = barsPanel.changeBars.size

    internal fun getChangeBarColor(index: Int): Color {
        return barsPanel.changeBars[index].color
    }

    internal fun getChangeBarSize(index: Int): Int {
        return barsPanel.changeBars[index].size
    }

    internal val changeBarLabel: String
        get() = barsPanel.changeBarLabel

    internal val changeBarStart: Int
        get() = barsPanel.changeBarStart

    private inner class SeatBarPanel : JPanel() {
        private var _seatBars: List<Bar> = ArrayList()
        private var _seatBarLabel = ""
        private var _changeBars: List<Bar> = ArrayList()
        private var _changeBarLabel = ""
        private var _changeBarStart = 0

        var seatBars: List<Bar>
        get() { return _seatBars }
        set(seatBars) {
            _seatBars = seatBars
            repaint()
        }

        var seatBarLabel: String
        get() { return _seatBarLabel }
        set(seatBarLabel) {
            _seatBarLabel = seatBarLabel
            repaint()
        }

        var changeBars: List<Bar>
        get() { return _changeBars }
        set(changeBars) {
            _changeBars = changeBars
            repaint()
        }

        var changeBarLabel: String
        get() { return _changeBarLabel }
        set(changeBarLabel) {
            _changeBarLabel = changeBarLabel
            repaint()
        }

        var changeBarStart: Int
        get() { return _changeBarStart }
        set(changeBarStart) {
            _changeBarStart = changeBarStart
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
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
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
            if (changeBars.map { i: Bar -> i.size }.sum() < 0) {
                leftLeft -= g.fontMetrics.stringWidth(changeBarLabel) + 10
            }
            g.drawString(changeBarLabel, leftLeft, seatBaseline)
            val changeBarHeight = height * 4 / 5
            val changeBarTop = height / 10 + height
            val changeBarMid = changeBarTop + changeBarHeight / 2
            val changeBarBottom = changeBarTop + changeBarHeight
            val sideFunc: (Int, Int) -> Int = { zero: Int, point: Int ->
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
                val points: MutableList<Point> = ArrayList()
                points.add(Point(startSide, changeBarTop))
                points.add(Point(start, changeBarMid))
                points.add(Point(startSide, changeBarBottom))
                points.add(Point(endSide, changeBarBottom))
                points.add(Point(end, changeBarMid))
                points.add(Point(endSide, changeBarTop))
                val polygon = Polygon(
                    points.map { p: Point -> p.getX().toInt() }.toIntArray(),
                    points.map { p: Point -> p.getY().toInt() }.toIntArray(),
                    points.size
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
            return (1.0 * width * seats / numSquares).roundToInt()
        }

        init {
            background = Color.WHITE
        }
    }

    class Bar(val color: Color = Color.WHITE, val size: Int = 0)

    private inner class SquaresPanel : JPanel() {
        private var _numRows = 1
        private var _squares: List<Square> = ArrayList()

        var numRows: Int
        get() { return _numRows }
        set(numRows) {
            _numRows = numRows
            repaint()
        }

        var squares: List<Square>
        get() { return _squares }
        set(squares) {
            _squares = squares
            repaint()
        }

        var label: String? = null
            set(label) {
                field = label
                repaint()
            }

        private val numCols: Int
            get() {
                val numCols = ceil(1.0 * _squares.size / numRows).toInt()
                return numCols
            }

        private val squareSize: Int
            get() {
                val squareSize = min((this.width - 10) / this.numCols, (this.height - 10) / numRows)
                return squareSize
            }

        private val farLeft: Int
            get() {
                val farLeft = (this.width - this.squareSize * this.numCols) / 2
                return farLeft
            }

        private val farTop: Int
            get() {
                val farTop = (this.height - this.squareSize * numRows) / 2
                return farTop
            }

        private val padding: Int
            get() {
                val padding: Int = if (_squares.size % numRows != 0) {
                    ceil(0.5 * (numRows - _squares.size % numRows)).toInt()
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
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
            g.setColor(Color.BLACK)
            g.drawLine(width / 2, 0, width / 2, height)
            if (_squares.isEmpty()) return
            for (i in _squares.indices) {
                val square = _squares[i]
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
                        squareSize - borderSize
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

    init {
        val panel = JPanel()
        add(panel, BorderLayout.CENTER)
        panel.layout = Layout()
        panel.add(barsPanel)
        panel.add(squaresPanel)

        numRowsBinding.bind { numRows -> squaresPanel.numRows = numRows }
        squaresBinding.bind { squares -> squaresPanel.squares = squares }
        (seatBarsBinding ?: Binding.fixedBinding(emptyList())).bind { bars -> barsPanel.seatBars = bars }
        (seatBarLabelBinding ?: Binding.fixedBinding("")).bind { label -> barsPanel.seatBarLabel = label }
        (changeBarsBinding ?: Binding.fixedBinding(emptyList())).bind { bars -> barsPanel.changeBars = bars }
        (changeBarLabelBinding ?: Binding.fixedBinding("")).bind { label -> barsPanel.changeBarLabel = label }
        (changeBarStartBinding ?: Binding.fixedBinding(0)).bind { start -> barsPanel.changeBarStart = start }
    }
}
