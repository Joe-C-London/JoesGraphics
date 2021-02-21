package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
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
import java.awt.geom.Area
import java.util.ArrayList
import javax.swing.JPanel
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class HeatMapFrame : GraphicsFrame() {
    private var numRowsBinding = Binding.fixedBinding(1)
    private var numSquaresBinding = Binding.fixedBinding(0)
    private var squareBordersBinding = IndexedBinding.emptyBinding<Color>()
    private var squareFillBinding = IndexedBinding.emptyBinding<Color>()
    private var numSeatBarsBinding = Binding.fixedBinding(0)
    private var seatBarColorBinding = IndexedBinding.emptyBinding<Color>()
    private var seatBarSizeBinding = IndexedBinding.emptyBinding<Int>()
    private var seatBarLabelBinding = Binding.fixedBinding("")
    private var numChangeBarsBinding = Binding.fixedBinding(0)
    private var changeBarColorBinding = IndexedBinding.emptyBinding<Color>()
    private var changeBarSizeBinding = IndexedBinding.emptyBinding<Int>()
    private var changeBarLabelBinding = Binding.fixedBinding("")
    private var changeBarStartBinding = Binding.fixedBinding(0)
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

    val numRows: Int
        get() = squaresPanel.numRows

    fun setNumRowsBinding(numRowsBinding: Binding<Int>) {
        this.numRowsBinding.unbind()
        this.numRowsBinding = numRowsBinding
        this.numRowsBinding.bind { numRows -> squaresPanel.numRows = numRows }
    }

    val numSquares: Int
        get() = squaresPanel.squares.size

    fun setNumSquaresBinding(numSquaresBinding: Binding<Int>) {
        this.numSquaresBinding.unbind()
        this.numSquaresBinding = numSquaresBinding
        this.numSquaresBinding.bind { numSquares -> squaresPanel.setNumSquares(numSquares) }
    }

    fun getSquareBorder(index: Int): Color? {
        return squaresPanel.squares[index].borderColor
    }

    fun setSquareBordersBinding(squareBordersBinding: IndexedBinding<Color>) {
        this.squareBordersBinding.unbind()
        this.squareBordersBinding = squareBordersBinding
        this.squareBordersBinding.bind { index, color -> squaresPanel.setSquareBorder(index, color) }
    }

    fun getSquareFill(index: Int): Color {
        return squaresPanel.squares[index].fillColor
    }

    fun setSquareFillBinding(squareFillBinding: IndexedBinding<Color>) {
        this.squareFillBinding.unbind()
        this.squareFillBinding = squareFillBinding
        this.squareFillBinding.bind { index, color -> squaresPanel.setSquareFill(index, color) }
    }

    val seatBarCount: Int
        get() = barsPanel.seatBars.size

    fun setNumSeatBarsBinding(numSeatBarsBinding: Binding<Int>) {
        this.numSeatBarsBinding.unbind()
        this.numSeatBarsBinding = numSeatBarsBinding
        this.numSeatBarsBinding.bind { numSeatBars -> barsPanel.setNumSeatBars(numSeatBars) }
    }

    fun getSeatBarColor(index: Int): Color {
        return barsPanel.seatBars[index].color
    }

    fun setSeatBarColorBinding(seatBarColorBinding: IndexedBinding<Color>) {
        this.seatBarColorBinding.unbind()
        this.seatBarColorBinding = seatBarColorBinding
        this.seatBarColorBinding.bind { index, color -> barsPanel.setSeatBarColor(index, color) }
    }

    fun getSeatBarSize(index: Int): Int {
        return barsPanel.seatBars[index].size
    }

    fun setSeatBarSizeBinding(seatBarSizeBinding: IndexedBinding<Int>) {
        this.seatBarSizeBinding.unbind()
        this.seatBarSizeBinding = seatBarSizeBinding
        this.seatBarSizeBinding.bind { index, size -> barsPanel.setSeatBarSize(index, size) }
    }

    val seatBarLabel: String
        get() = barsPanel.seatBarLabel

    fun setSeatBarLabelBinding(seatBarLabelBinding: Binding<String>) {
        this.seatBarLabelBinding.unbind()
        this.seatBarLabelBinding = seatBarLabelBinding
        this.seatBarLabelBinding.bind { label -> barsPanel.seatBarLabel = label }
    }

    val changeBarCount: Int
        get() = barsPanel.changeBars.size

    fun setNumChangeBarsBinding(numChangeBarsBinding: Binding<Int>) {
        this.numChangeBarsBinding.unbind()
        this.numChangeBarsBinding = numChangeBarsBinding
        this.numChangeBarsBinding.bind { numChangeBars -> barsPanel.setNumChangeBars(numChangeBars) }
    }

    fun getChangeBarColor(index: Int): Color {
        return barsPanel.changeBars[index].color
    }

    fun setChangeBarColorBinding(changeBarColorBinding: IndexedBinding<Color>) {
        this.changeBarColorBinding.unbind()
        this.changeBarColorBinding = changeBarColorBinding
        this.changeBarColorBinding.bind { index, color -> barsPanel.setChangeBarColor(index, color) }
    }

    fun getChangeBarSize(index: Int): Int {
        return barsPanel.changeBars[index].size
    }

    fun setChangeBarSizeBinding(changeBarSizeBinding: IndexedBinding<Int>) {
        this.changeBarSizeBinding.unbind()
        this.changeBarSizeBinding = changeBarSizeBinding
        this.changeBarSizeBinding.bind { index, size -> barsPanel.setChangeBarSize(index, size) }
    }

    val changeBarLabel: String
        get() = barsPanel.changeBarLabel

    fun setChangeBarLabelBinding(changeBarLabelBinding: Binding<String>) {
        this.changeBarLabelBinding.unbind()
        this.changeBarLabelBinding = changeBarLabelBinding
        this.changeBarLabelBinding.bind { label -> barsPanel.changeBarLabel = label }
    }

    val changeBarStart: Int
        get() = barsPanel.changeBarStart

    fun setChangeBarStartBinding(changeBarStartBinding: Binding<Int>) {
        this.changeBarStartBinding.unbind()
        this.changeBarStartBinding = changeBarStartBinding
        this.changeBarStartBinding.bind { start -> barsPanel.changeBarStart = start }
    }

    private inner class SeatBarPanel : JPanel() {
        private val _seatBars: MutableList<Bar> = ArrayList()
        private var _seatBarLabel = ""
        private val _changeBars: MutableList<Bar> = ArrayList()
        private var _changeBarLabel = ""
        private var _changeBarStart = 0

        val seatBars: List<Bar>
        get() { return _seatBars }

        fun setNumSeatBars(numSeatBars: Int) {
            while (_seatBars.size < numSeatBars) {
                _seatBars.add(Bar())
            }
            while (_seatBars.size > numSeatBars) {
                _seatBars.removeAt(numSeatBars)
            }
            repaint()
        }

        fun setSeatBarColor(index: Int, color: Color) {
            seatBars[index].color = color
            repaint()
        }

        fun setSeatBarSize(index: Int, size: Int) {
            seatBars[index].size = size
            repaint()
        }

        var seatBarLabel: String
        get() { return _seatBarLabel }
        set(seatBarLabel) {
            _seatBarLabel = seatBarLabel
            repaint()
        }

        val changeBars: List<Bar>
        get() { return _changeBars }

        fun setNumChangeBars(numChangeBars: Int) {
            while (_changeBars.size < numChangeBars) {
                _changeBars.add(Bar())
            }
            while (_changeBars.size > numChangeBars) {
                _changeBars.removeAt(numChangeBars)
            }
            repaint()
        }

        fun setChangeBarColor(index: Int, color: Color) {
            changeBars[index].color = color
            repaint()
        }

        fun setChangeBarSize(index: Int, size: Int) {
            changeBars[index].size = size
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

    private inner class Bar {
        var color: Color = Color.WHITE
        var size = 0
    }

    private inner class SquaresPanel : JPanel() {
        private var _numRows = 1
        private val _squares: MutableList<Square> = ArrayList()

        var numRows: Int
        get() { return _numRows }
        set(numRows) {
            _numRows = numRows
            repaint()
        }

        val squares: List<Square>
        get() { return _squares }

        fun setNumSquares(numSquares: Int) {
            while (_squares.size < numSquares) {
                _squares.add(Square())
            }
            while (_squares.size > numSquares) {
                _squares.removeAt(numSquares)
            }
            repaint()
        }

        fun setSquareBorder(index: Int, color: Color) {
            _squares[index].borderColor = color
            repaint()
        }

        fun setSquareFill(index: Int, color: Color) {
            _squares[index].fillColor = color
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
            g.setColor(Color.BLACK)
            g.drawLine(width / 2, 0, width / 2, height)
            if (_squares.isEmpty()) return
            val width = width
            val height = height
            val numCols = ceil(1.0 * _squares.size / numRows).toInt()
            val squareSize = min((width - 10) / numCols, (height - 10) / numRows)
            val farLeft = (width - squareSize * numCols) / 2
            val farTop = (height - squareSize * numRows) / 2
            val padding: Int = if (_squares.size % numRows != 0) {
                ceil(0.5 * (numRows - _squares.size % numRows)).toInt()
            } else {
                0
            }
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
        }

        init {
            background = Color.WHITE
        }
    }

    private inner class Square {
        var borderColor: Color? = null
        var fillColor: Color = Color.WHITE
    }

    init {
        val panel = JPanel()
        add(panel, BorderLayout.CENTER)
        panel.layout = Layout()
        panel.add(barsPanel)
        panel.add(squaresPanel)
    }
}
