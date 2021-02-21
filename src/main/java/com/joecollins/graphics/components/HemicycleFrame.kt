package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.components.HemicycleFrame.BarPanel
import com.joecollins.graphics.components.HemicycleFrame.DotsPanel
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
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.lang.Math
import java.util.ArrayList
import javax.swing.JPanel
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class HemicycleFrame : GraphicsFrame() {
    private var numRowsBinding: Binding<Int> = Binding.fixedBinding(0)
    private var rowCountBinding = IndexedBinding.emptyBinding<Int>()

    private var numDotsBinding: Binding<Int> = Binding.fixedBinding(0)
    private var dotColorBinding = IndexedBinding.emptyBinding<Color>()
    private var dotBorderBinding = IndexedBinding.emptyBinding<Color>()

    private var leftSeatBarCountBinding: Binding<Int> = Binding.fixedBinding(0)
    private var leftSeatBarColorBinding = IndexedBinding.emptyBinding<Color>()
    private var leftSeatBarSizeBinding = IndexedBinding.emptyBinding<Int>()
    private var leftSeatBarLabelBinding: Binding<String> = Binding.fixedBinding("")

    private var rightSeatBarCountBinding: Binding<Int> = Binding.fixedBinding(0)
    private var rightSeatBarColorBinding = IndexedBinding.emptyBinding<Color>()
    private var rightSeatBarSizeBinding = IndexedBinding.emptyBinding<Int>()
    private var rightSeatBarLabelBinding: Binding<String> = Binding.fixedBinding("")

    private var middleSeatBarCountBinding: Binding<Int> = Binding.fixedBinding(0)
    private var middleSeatBarColorBinding = IndexedBinding.emptyBinding<Color>()
    private var middleSeatBarSizeBinding = IndexedBinding.emptyBinding<Int>()
    private var middleSeatBarLabelBinding: Binding<String> = Binding.fixedBinding("")

    private var leftChangeBarCountBinding: Binding<Int> = Binding.fixedBinding(0)
    private var leftChangeBarColorBinding = IndexedBinding.emptyBinding<Color>()
    private var leftChangeBarSizeBinding = IndexedBinding.emptyBinding<Int>()
    private var leftChangeBarStartBinding: Binding<Int> = Binding.fixedBinding(0)
    private var leftChangeBarLabelBinding: Binding<String> = Binding.fixedBinding("")

    private var rightChangeBarCountBinding: Binding<Int> = Binding.fixedBinding(0)
    private var rightChangeBarColorBinding = IndexedBinding.emptyBinding<Color>()
    private var rightChangeBarSizeBinding = IndexedBinding.emptyBinding<Int>()
    private var rightChangeBarStartBinding: Binding<Int> = Binding.fixedBinding(0)
    private var rightChangeBarLabelBinding: Binding<String> = Binding.fixedBinding("")

    private val barsPanel = BarPanel()
    private val dotsPanel = DotsPanel()

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
            dotsPanel.setLocation(0, mid)
            dotsPanel.setSize(parent.width, parent.height - mid)
        }
    }

    protected val numRows: Int
        get() = dotsPanel.rows.size

    fun setNumRowsBinding(numRowsBinding: Binding<Int>) {
        this.numRowsBinding.unbind()
        this.numRowsBinding = numRowsBinding
        this.numRowsBinding.bind { numRows ->
            setSize(dotsPanel.rows, numRows) { 0 }
            dotsPanel.repaint()
        }
    }

    protected fun getRowCount(rowNum: Int): Int {
        return dotsPanel.rows[rowNum]
    }

    fun setRowCountsBinding(rowCountBinding: IndexedBinding<Int>) {
        this.rowCountBinding.unbind()
        this.rowCountBinding = rowCountBinding
        this.rowCountBinding.bind { idx, count ->
            dotsPanel.rows[idx] = count
            dotsPanel.repaint()
        }
    }

    protected val numDots: Int
        get() = dotsPanel.dots.size

    fun setNumDotsBinding(numDotsBinding: Binding<Int>) {
        this.numDotsBinding.unbind()
        this.numDotsBinding = numDotsBinding
        this.numDotsBinding.bind { numDots ->
            setSize(dotsPanel.dots, numDots) { Dot() }
            dotsPanel.repaint()
        }
    }

    protected fun getDotColor(dotNum: Int): Color {
        return dotsPanel.dots[dotNum].color
    }

    fun setDotColorBinding(dotColorBinding: IndexedBinding<Color>) {
        this.dotColorBinding.unbind()
        this.dotColorBinding = dotColorBinding
        this.dotColorBinding.bind { idx, color ->
            dotsPanel.dots[idx].color = color
            dotsPanel.repaint()
        }
    }

    protected fun getDotBorder(dotNum: Int): Color? {
        return dotsPanel.dots[dotNum].border
    }

    fun setDotBorderBinding(dotBorderBinding: IndexedBinding<Color>) {
        this.dotBorderBinding.unbind()
        this.dotBorderBinding = dotBorderBinding
        this.dotBorderBinding.bind { idx, border ->
            dotsPanel.dots[idx].border = border
            dotsPanel.repaint()
        }
    }

    protected val leftSeatBarCount: Int
        get() = barsPanel.leftSeatBars.size

    fun setLeftSeatBarCountBinding(leftSeatBarCountBinding: Binding<Int>) {
        this.leftSeatBarCountBinding.unbind()
        this.leftSeatBarCountBinding = leftSeatBarCountBinding
        this.leftSeatBarCountBinding.bind { numBars ->
            setSize(barsPanel.leftSeatBars, numBars) { Bar() }
            barsPanel.repaint()
        }
    }

    protected fun getLeftSeatBarColor(idx: Int): Color {
        return barsPanel.leftSeatBars[idx].color
    }

    fun setLeftSeatBarColorBinding(leftSeatBarColorBinding: IndexedBinding<Color>) {
        this.leftSeatBarColorBinding.unbind()
        this.leftSeatBarColorBinding = leftSeatBarColorBinding
        this.leftSeatBarColorBinding.bind { idx, color ->
            barsPanel.leftSeatBars[idx].color = color
            barsPanel.repaint()
        }
    }

    protected fun getLeftSeatBarSize(idx: Int): Int {
        return barsPanel.leftSeatBars[idx].size
    }

    fun setLeftSeatBarSizeBinding(leftSeatBarSizeBinding: IndexedBinding<Int>) {
        this.leftSeatBarSizeBinding.unbind()
        this.leftSeatBarSizeBinding = leftSeatBarSizeBinding
        this.leftSeatBarSizeBinding.bind { idx, size ->
            barsPanel.leftSeatBars[idx].size = size
            barsPanel.repaint()
        }
    }

    protected fun getLeftSeatBarLabel(): String {
        return barsPanel.leftSeatBarLabel
    }

    fun setLeftSeatBarLabelBinding(leftSeatBarLabelBinding: Binding<String>) {
        this.leftSeatBarLabelBinding.unbind()
        this.leftSeatBarLabelBinding = leftSeatBarLabelBinding
        this.leftSeatBarLabelBinding.bind { label ->
            barsPanel.leftSeatBarLabel = label
            barsPanel.repaint()
        }
    }

    protected val rightSeatBarCount: Int
        get() = barsPanel.rightSeatBars.size

    fun setRightSeatBarCountBinding(rightSeatBarCountBinding: Binding<Int>) {
        this.rightSeatBarCountBinding.unbind()
        this.rightSeatBarCountBinding = rightSeatBarCountBinding
        this.rightSeatBarCountBinding.bind { numBars ->
            setSize(barsPanel.rightSeatBars, numBars) { Bar() }
            barsPanel.repaint()
        }
    }

    protected fun getRightSeatBarColor(idx: Int): Color {
        return barsPanel.rightSeatBars[idx].color
    }

    fun setRightSeatBarColorBinding(rightSeatBarColorBinding: IndexedBinding<Color>) {
        this.rightSeatBarColorBinding.unbind()
        this.rightSeatBarColorBinding = rightSeatBarColorBinding
        this.rightSeatBarColorBinding.bind { idx, color ->
            barsPanel.rightSeatBars[idx].color = color
            barsPanel.repaint()
        }
    }

    protected fun getRightSeatBarSize(idx: Int): Int {
        return barsPanel.rightSeatBars[idx].size
    }

    fun setRightSeatBarSizeBinding(rightSeatBarSizeBinding: IndexedBinding<Int>) {
        this.rightSeatBarSizeBinding.unbind()
        this.rightSeatBarSizeBinding = rightSeatBarSizeBinding
        this.rightSeatBarSizeBinding.bind { idx, size ->
            barsPanel.rightSeatBars[idx].size = size
            barsPanel.repaint()
        }
    }

    protected fun getRightSeatBarLabel(): String {
        return barsPanel.rightSeatBarLabel
    }

    fun setRightSeatBarLabelBinding(rightSeatBarLabelBinding: Binding<String>) {
        this.rightSeatBarLabelBinding.unbind()
        this.rightSeatBarLabelBinding = rightSeatBarLabelBinding
        this.rightSeatBarLabelBinding.bind { label ->
            barsPanel.rightSeatBarLabel = label
            barsPanel.repaint()
        }
    }

    protected val middleSeatBarCount: Int
        get() = barsPanel.middleSeatBars.size

    fun setMiddleSeatBarCountBinding(middleSeatBarCountBinding: Binding<Int>) {
        this.middleSeatBarCountBinding.unbind()
        this.middleSeatBarCountBinding = middleSeatBarCountBinding
        this.middleSeatBarCountBinding.bind { numBars ->
            setSize(barsPanel.middleSeatBars, numBars) { Bar() }
            barsPanel.repaint()
        }
    }

    protected fun getMiddleSeatBarColor(idx: Int): Color {
        return barsPanel.middleSeatBars[idx].color
    }

    fun setMiddleSeatBarColorBinding(middleSeatBarColorBinding: IndexedBinding<Color>) {
        this.middleSeatBarColorBinding.unbind()
        this.middleSeatBarColorBinding = middleSeatBarColorBinding
        this.middleSeatBarColorBinding.bind { idx, color ->
            barsPanel.middleSeatBars[idx].color = color
            barsPanel.repaint()
        }
    }

    protected fun getMiddleSeatBarSize(idx: Int): Int {
        return barsPanel.middleSeatBars[idx].size
    }

    fun setMiddleSeatBarSizeBinding(middleSeatBarSizeBinding: IndexedBinding<Int>) {
        this.middleSeatBarSizeBinding.unbind()
        this.middleSeatBarSizeBinding = middleSeatBarSizeBinding
        this.middleSeatBarSizeBinding.bind { idx, size ->
            barsPanel.middleSeatBars[idx].size = size
            barsPanel.repaint()
        }
    }

    protected fun getMiddleSeatBarLabel(): String {
        return barsPanel.middleSeatBarLabel
    }

    fun setMiddleSeatBarLabelBinding(middleSeatBarLabelBinding: Binding<String>) {
        this.middleSeatBarLabelBinding.unbind()
        this.middleSeatBarLabelBinding = middleSeatBarLabelBinding
        this.middleSeatBarLabelBinding.bind { label ->
            barsPanel.middleSeatBarLabel = label
            barsPanel.repaint()
        }
    }

    protected val leftChangeBarCount: Int
        get() = barsPanel.leftChangeBars.size

    fun setLeftChangeBarCountBinding(leftChangeBarCountBinding: Binding<Int>) {
        this.leftChangeBarCountBinding.unbind()
        this.leftChangeBarCountBinding = leftChangeBarCountBinding
        this.leftChangeBarCountBinding.bind { numBars ->
            setSize(barsPanel.leftChangeBars, numBars) { Bar() }
            barsPanel.repaint()
        }
    }

    protected fun getLeftChangeBarColor(idx: Int): Color {
        return barsPanel.leftChangeBars[idx].color
    }

    fun setLeftChangeBarColorBinding(leftChangeBarColorBinding: IndexedBinding<Color>) {
        this.leftChangeBarColorBinding.unbind()
        this.leftChangeBarColorBinding = leftChangeBarColorBinding
        this.leftChangeBarColorBinding.bind { idx, color ->
            barsPanel.leftChangeBars[idx].color = color
            barsPanel.repaint()
        }
    }

    protected fun getLeftChangeBarSize(idx: Int): Int {
        return barsPanel.leftChangeBars[idx].size
    }

    fun setLeftChangeBarSizeBinding(leftChangeBarSizeBinding: IndexedBinding<Int>) {
        this.leftChangeBarSizeBinding.unbind()
        this.leftChangeBarSizeBinding = leftChangeBarSizeBinding
        this.leftChangeBarSizeBinding.bind { idx, size ->
            barsPanel.leftChangeBars[idx].size = size
            barsPanel.repaint()
        }
    }

    protected fun getLeftChangeBarStart(): Int {
        return barsPanel.leftChangeBarStart
    }

    fun setLeftChangeBarStartBinding(leftChangeBarStartBinding: Binding<Int>) {
        this.leftChangeBarStartBinding.unbind()
        this.leftChangeBarStartBinding = leftChangeBarStartBinding
        this.leftChangeBarStartBinding.bind { start ->
            barsPanel.leftChangeBarStart = start
            barsPanel.repaint()
        }
    }

    protected fun getLeftChangeBarLabel(): String {
        return barsPanel.leftChangeBarLabel
    }

    fun setLeftChangeBarLabelBinding(leftChangeBarLabelBinding: Binding<String>) {
        this.leftChangeBarLabelBinding.unbind()
        this.leftChangeBarLabelBinding = leftChangeBarLabelBinding
        this.leftChangeBarLabelBinding.bind { label ->
            barsPanel.leftChangeBarLabel = label
            barsPanel.repaint()
        }
    }

    protected val rightChangeBarCount: Int
        get() = barsPanel.rightChangeBars.size

    fun setRightChangeBarCountBinding(rightChangeBarCountBinding: Binding<Int>) {
        this.rightChangeBarCountBinding.unbind()
        this.rightChangeBarCountBinding = rightChangeBarCountBinding
        this.rightChangeBarCountBinding.bind { numBars ->
            setSize(barsPanel.rightChangeBars, numBars) { Bar() }
            barsPanel.repaint()
        }
    }

    protected fun getRightChangeBarColor(idx: Int): Color {
        return barsPanel.rightChangeBars[idx].color
    }

    fun setRightChangeBarColorBinding(rightChangeBarColorBinding: IndexedBinding<Color>) {
        this.rightChangeBarColorBinding.unbind()
        this.rightChangeBarColorBinding = rightChangeBarColorBinding
        this.rightChangeBarColorBinding.bind { idx, color ->
            barsPanel.rightChangeBars[idx].color = color
            barsPanel.repaint()
        }
    }

    protected fun getRightChangeBarSize(idx: Int): Int {
        return barsPanel.rightChangeBars[idx].size
    }

    fun setRightChangeBarSizeBinding(rightChangeBarSizeBinding: IndexedBinding<Int>) {
        this.rightChangeBarSizeBinding.unbind()
        this.rightChangeBarSizeBinding = rightChangeBarSizeBinding
        this.rightChangeBarSizeBinding.bind { idx, size ->
            barsPanel.rightChangeBars[idx].size = size
            barsPanel.repaint()
        }
    }

    protected fun getRightChangeBarStart(): Int {
        return barsPanel.rightChangeBarStart
    }

    fun setRightChangeBarStartBinding(rightChangeBarStartBinding: Binding<Int>) {
        this.rightChangeBarStartBinding.unbind()
        this.rightChangeBarStartBinding = rightChangeBarStartBinding
        this.rightChangeBarStartBinding.bind { start ->
            barsPanel.rightChangeBarStart = start
            barsPanel.repaint()
        }
    }

    protected fun getRightChangeBarLabel(): String {
        return barsPanel.rightChangeBarLabel
    }

    fun setRightChangeBarLabelBinding(rightChangeBarLabelBinding: Binding<String>) {
        this.rightChangeBarLabelBinding.unbind()
        this.rightChangeBarLabelBinding = rightChangeBarLabelBinding
        this.rightChangeBarLabelBinding.bind { label ->
            barsPanel.rightChangeBarLabel = label
            barsPanel.repaint()
        }
    }

    private inner class Bar {
        var color: Color = Color.WHITE
        var size = 0
    }

    private inner class BarPanel : JPanel() {
        val leftSeatBars: MutableList<Bar> = ArrayList()
        var leftSeatBarLabel = ""
        val rightSeatBars: MutableList<Bar> = ArrayList()
        var rightSeatBarLabel = ""
        val middleSeatBars: MutableList<Bar> = ArrayList()
        var middleSeatBarLabel = ""
        val leftChangeBars: MutableList<Bar> = ArrayList()
        var leftChangeBarLabel = ""
        var leftChangeBarStart = 0
        val rightChangeBars: MutableList<Bar> = ArrayList()
        var rightChangeBarLabel = ""
        var rightChangeBarStart = 0

        fun hasSeats(): Boolean {
            return (leftSeatBars.isNotEmpty() ||
                    middleSeatBars.isNotEmpty() ||
                    rightSeatBars.isNotEmpty() ||
                    leftSeatBarLabel.isNotEmpty() ||
                    middleSeatBarLabel.isNotEmpty() ||
                    rightSeatBarLabel.isNotEmpty())
        }

        fun hasChange(): Boolean {
            return (leftChangeBars.isNotEmpty() ||
                    rightChangeBars.isNotEmpty() ||
                    leftChangeBarLabel.isNotEmpty() ||
                    rightChangeBarLabel.isNotEmpty())
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
            g.color = if (leftSeatBars.isEmpty()) Color.BLACK else leftSeatBars[0].color
            g.drawString(leftSeatBarLabel, 5, seatBaseline)
            g.color = if (rightSeatBars.isEmpty()) Color.BLACK else rightSeatBars[0].color
            val rightWidth = g.fontMetrics.stringWidth(rightSeatBarLabel)
            val rightLeft = width - rightWidth - 5
            g.drawString(rightSeatBarLabel, rightLeft, seatBaseline)
            g.color = if (middleSeatBars.isEmpty()) Color.BLACK else middleSeatBars[0].color
            val middleWidth = g.fontMetrics.stringWidth(middleSeatBarLabel)
            val middleLeft = getMiddleStartPosition(0) - middleWidth / 2
            g.drawString(middleSeatBarLabel, middleLeft, seatBaseline)
            val seatBarTop = height / 10
            val seatBarHeight = height * 4 / 5
            var leftSoFar = 0
            for (bar in leftSeatBars) {
                val start = getLeftPosition(leftSoFar)
                val end = getLeftPosition(leftSoFar + bar.size)
                g.color = bar.color
                g.fillRect(start, seatBarTop, end - start, seatBarHeight)
                leftSoFar += bar.size
            }
            val leftClip: Shape = Rectangle(0, seatBarTop, getLeftPosition(leftSoFar), seatBarHeight)
            var rightSoFar = 0
            for (bar in rightSeatBars) {
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
                seatBarHeight
            )
            var middleSoFar = 0
            for (bar in middleSeatBars) {
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
                seatBarHeight
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
            g.color = if (leftChangeBars.isEmpty()) Color.BLACK else leftChangeBars[0].color
            val leftLeft = getLeftPosition(leftChangeBarStart) + 5
            g.drawString(leftChangeBarLabel, leftLeft, seatBaseline)
            g.color = if (rightChangeBars.isEmpty()) Color.BLACK else rightChangeBars[0].color
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
            for (bar in leftChangeBars) {
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
                g.fillPolygon(
                    points.stream().mapToInt { p: Point -> p.getX().toInt() }.toArray(),
                    points.stream().mapToInt { p: Point -> p.getY().toInt() }.toArray(),
                    points.size
                )
                leftSoFar += bar.size
            }
            val leftTip = getLeftPosition(leftSoFar)
            val leftSize = sideFunc(leftBase, leftTip)
            val leftClip: Shape = Polygon(
                intArrayOf(leftBase, leftBase, leftSize, leftTip, leftSize), intArrayOf(
                    changeBarTop, changeBarBottom, changeBarBottom, changeBarMid, changeBarTop
                ),
                5
            )
            var rightSoFar = rightChangeBarStart
            val rightBase = getRightPosition(rightChangeBarStart)
            for (bar in rightChangeBars) {
                val start = getRightPosition(rightSoFar)
                val end = getRightPosition(rightSoFar + bar.size)
                val startSide = sideFunc(rightBase, start)
                val endSide = sideFunc(rightBase, end)
                g.color = bar.color
                val points: MutableList<Point> = ArrayList()
                points.add(Point(startSide, changeBarTop))
                points.add(Point(start, changeBarMid))
                points.add(Point(startSide, changeBarBottom))
                points.add(Point(endSide, changeBarBottom))
                points.add(Point(end, changeBarMid))
                points.add(Point(endSide, changeBarTop))
                g.fillPolygon(
                    points.stream().mapToInt { p: Point -> p.getX().toInt() }.toArray(),
                    points.stream().mapToInt { p: Point -> p.getY().toInt() }.toArray(),
                    points.size
                )
                rightSoFar += bar.size
            }
            val rightTip = getRightPosition(rightSoFar)
            val rightSize = sideFunc(rightBase, rightTip)
            val rightClip: Shape = Polygon(
                intArrayOf(rightBase, rightBase, rightSize, rightTip, rightSize), intArrayOf(
                    changeBarTop, changeBarBottom, changeBarBottom, changeBarMid, changeBarTop
                ),
                5
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

        private fun getLeftPosition(seats: Int): Int {
            return getSize(seats)
        }

        private fun getRightPosition(seats: Int): Int {
            return width - getSize(seats)
        }

        private fun getMiddleStartPosition(seats: Int): Int {
            val midSize = getSize(middleSeatBars.stream().mapToInt { e: Bar -> e.size }
                .sum())
            val leftSize = getSize(leftSeatBars.stream().mapToInt { e: Bar -> e.size }
                .sum())
            val rightSize = getSize(rightSeatBars.stream().mapToInt { e: Bar -> e.size }
                .sum())
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

        private fun getMiddleEndPosition(seats: Int): Int {
            return getMiddleStartPosition(seats) + getSize(seats)
        }

        private fun getSize(seats: Int): Int {
            return (1.0 * width * seats / numDots).roundToInt()
        }

        init {
            background = Color.WHITE
        }
    }

    private inner class Dot {
        var color: Color = Color.WHITE
        var border: Color? = null
    }

    private inner class DotsPanel : JPanel() {
        val rows: MutableList<Int> = ArrayList()
        val dots: MutableList<Dot> = ArrayList()
        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
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
            val rowStartIndexes: MutableList<Int> = ArrayList(rows.size)
            for (i in rows.indices) {
                if (i == 0) {
                    rowStartIndexes.add(0)
                } else {
                    rowStartIndexes.add(rowStartIndexes[i - 1] + rows[i - 1])
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
            arcY: Int
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

    companion object {
        private fun <T> setSize(list: MutableList<T>, size: Int, defaultItem: () -> T) {
            while (size > list.size) {
                list.add(defaultItem())
            }
            while (size < list.size) {
                list.removeAt(size)
            }
        }
    }

    init {
        val panel = JPanel()
        add(panel, BorderLayout.CENTER)
        panel.layout = Layout()
        panel.add(barsPanel)
        panel.add(dotsPanel)
    }
}
