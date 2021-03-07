package com.joecollins.graphics.components

import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.Binding.Companion.sizeBinding
import com.joecollins.bindings.IndexedBinding.Companion.listBinding
import com.joecollins.bindings.IndexedBinding.Companion.propertyBinding
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.io.IOException
import java.util.Collections
import kotlin.Throws
import org.junit.Assert
import org.junit.Test

class HemicycleFrameTest {
    @Test
    fun testRowCounts() {
        val rowCounts = listOf(5, 6, 7, 9)
        val frame = HemicycleFrame()
        frame.setNumRowsBinding(fixedBinding(rowCounts.size))
        frame.setRowCountsBinding(listBinding(rowCounts))
        Assert.assertEquals(4, frame.numRows.toLong())
        Assert.assertEquals(5, frame.getRowCount(0).toLong())
        Assert.assertEquals(6, frame.getRowCount(1).toLong())
        Assert.assertEquals(7, frame.getRowCount(2).toLong())
        Assert.assertEquals(9, frame.getRowCount(3).toLong())
    }

    @Test
    fun testDotColors() {
        val dotColors = listOf(
                Color.GREEN, Color.GREEN, Color.BLUE, Color.BLUE, Color.BLUE, //
                Color.GREEN, Color.GREEN, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, //
                Color.GREEN, Color.GREEN, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, //
                Color.GREEN, Color.GREEN, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE)
        val frame = HemicycleFrame()
        frame.setNumDotsBinding(fixedBinding(dotColors.size))
        frame.setDotColorBinding(listBinding(dotColors))
        Assert.assertEquals(27, frame.numDots.toLong())
        Assert.assertEquals(Color.GREEN, frame.getDotColor(0))
        Assert.assertEquals(Color.RED, frame.getDotColor(7))
        Assert.assertEquals(Color.BLUE, frame.getDotColor(17))
    }

    @Test
    fun testDotBorders() {
        val dotColors = listOf(
                Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, //
                Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, //
                Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, //
                Color.GREEN, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE)
        val frame = HemicycleFrame()
        frame.setNumDotsBinding(fixedBinding(dotColors.size))
        frame.setDotBorderBinding(listBinding(dotColors))
        Assert.assertEquals(27, frame.numDots.toLong())
        Assert.assertEquals(Color.RED, frame.getDotBorder(0))
        Assert.assertEquals(Color.BLUE, frame.getDotBorder(9))
        Assert.assertEquals(Color.GREEN, frame.getDotBorder(18))
    }

    @Test
    fun testLeftSeatBar() {
        val frame = HemicycleFrame()
        frame.setLeftSeatBarCountBinding(fixedBinding(2))
        frame.setLeftSeatBarColorBinding(listBinding(Color.GREEN, Color(128, 255, 128)))
        frame.setLeftSeatBarSizeBinding(listBinding(1, 7))
        frame.setLeftSeatBarLabelBinding(fixedBinding("GREEN: 1/8"))
        Assert.assertEquals(2, frame.leftSeatBarCount.toLong())
        Assert.assertEquals(Color.GREEN, frame.getLeftSeatBarColor(0))
        Assert.assertEquals(7, frame.getLeftSeatBarSize(1).toLong())
        Assert.assertEquals("GREEN: 1/8", frame.getLeftSeatBarLabel())
    }

    @Test
    fun testRightSeatBar() {
        val frame = HemicycleFrame()
        frame.setRightSeatBarCountBinding(fixedBinding(2))
        frame.setRightSeatBarColorBinding(listBinding(Color.BLUE, Color(128, 128, 255)))
        frame.setRightSeatBarSizeBinding(listBinding(8, 5))
        frame.setRightSeatBarLabelBinding(fixedBinding("PROGRESSIVE CONSERVATIVE: 8/13"))
        Assert.assertEquals(2, frame.rightSeatBarCount.toLong())
        Assert.assertEquals(Color.BLUE, frame.getRightSeatBarColor(0))
        Assert.assertEquals(5, frame.getRightSeatBarSize(1).toLong())
        Assert.assertEquals("PROGRESSIVE CONSERVATIVE: 8/13", frame.getRightSeatBarLabel())
    }

    @Test
    fun testMiddleSeatBar() {
        val frame = HemicycleFrame()
        frame.setMiddleSeatBarCountBinding(fixedBinding(2))
        frame.setMiddleSeatBarColorBinding(listBinding(Color.RED, Color(255, 128, 128)))
        frame.setMiddleSeatBarSizeBinding(listBinding(2, 4))
        frame.setMiddleSeatBarLabelBinding(fixedBinding("LIBERAL: 2/6"))
        Assert.assertEquals(2, frame.middleSeatBarCount.toLong())
        Assert.assertEquals(Color.RED, frame.getMiddleSeatBarColor(0))
        Assert.assertEquals(4, frame.getMiddleSeatBarSize(1).toLong())
        Assert.assertEquals("LIBERAL: 2/6", frame.getMiddleSeatBarLabel())
    }

    @Test
    fun testLeftChangeBar() {
        val frame = HemicycleFrame()
        frame.setLeftChangeBarCountBinding(fixedBinding(2))
        frame.setLeftChangeBarColorBinding(listBinding(Color.GREEN, Color(128, 255, 128)))
        frame.setLeftChangeBarStartBinding(fixedBinding(1))
        frame.setLeftChangeBarSizeBinding(listBinding(1, 6))
        frame.setLeftChangeBarLabelBinding(fixedBinding("GRN: +1/+7"))
        Assert.assertEquals(2, frame.leftChangeBarCount.toLong())
        Assert.assertEquals(Color.GREEN, frame.getLeftChangeBarColor(0))
        Assert.assertEquals(6, frame.getLeftChangeBarSize(1).toLong())
        Assert.assertEquals(1, frame.getLeftChangeBarStart().toLong())
        Assert.assertEquals("GRN: +1/+7", frame.getLeftChangeBarLabel())
    }

    @Test
    fun testRightChangeBar() {
        val frame = HemicycleFrame()
        frame.setRightChangeBarCountBinding(fixedBinding(2))
        frame.setRightChangeBarColorBinding(listBinding(Color.BLUE, Color(128, 128, 255)))
        frame.setRightChangeBarStartBinding(fixedBinding(8))
        frame.setRightChangeBarSizeBinding(listBinding(3, 2))
        frame.setRightChangeBarLabelBinding(fixedBinding("PC: +3/+5"))
        Assert.assertEquals(2, frame.rightChangeBarCount.toLong())
        Assert.assertEquals(Color.BLUE, frame.getRightChangeBarColor(0))
        Assert.assertEquals(2, frame.getRightChangeBarSize(1).toLong())
        Assert.assertEquals(8, frame.getRightChangeBarStart().toLong())
        Assert.assertEquals("PC: +3/+5", frame.getRightChangeBarLabel())
    }

    @Test
    @Throws(IOException::class)
    fun testRenderDotsOnly() {
        val rowCounts = listOf(7, 9, 11)
        val dotColors = listOf(
                Color.GREEN, Color.GREEN, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE, //
                Color.GREEN, Color.GREEN, Color.GREEN, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE, //
                Color.GREEN, Color.GREEN, Color.GREEN, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE)
        val dotBorders = listOf(
                Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, //
                Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, //
                Color.GREEN, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE)
        val frame = HemicycleFrame()
        frame.setNumRowsBinding(fixedBinding(rowCounts.size))
        frame.setRowCountsBinding(listBinding(rowCounts))
        frame.setNumDotsBinding(fixedBinding(dotColors.size))
        frame.setDotColorBinding(listBinding(dotColors))
        frame.setDotBorderBinding(listBinding(dotBorders))
        frame.setHeaderBinding(fixedBinding("PEI HEMICYCLE"))
        frame.setSize(1024, 512)
        compareRendering("HemicycleFrame", "DotsOnly", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSeatsBars() {
        val rowCounts = listOf(7, 9, 11)
        val dotColors = BindableList<Color>()
        dotColors.setAll(Collections.nCopies(27, Color.WHITE))
        val dotBorders = listOf(
                Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, //
                Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, //
                Color.GREEN, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE)
        val leftSeats = BindableList<Int>()
        val middleSeats = BindableList<Int>()
        val rightSeats = BindableList<Int>()
        sequenceOf(leftSeats, middleSeats, rightSeats).forEach { list: BindableList<Int> -> list.setAll(listOf(0, 0)) }
        val leftLabel = BindableWrapper("GREEN: 0/0")
        val middleLabel = BindableWrapper("LIBERAL: 0/0")
        val rightLabel = BindableWrapper("PROGRESSIVE CONSERVATIVE: 0/0")
        val frame = HemicycleFrame()
        frame.setNumRowsBinding(fixedBinding(rowCounts.size))
        frame.setRowCountsBinding(listBinding(rowCounts))
        frame.setNumDotsBinding(sizeBinding(dotColors))
        frame.setDotColorBinding(propertyBinding(dotColors) { t: Color -> t })
        frame.setDotBorderBinding(listBinding(dotBorders))
        frame.setHeaderBinding(fixedBinding("PEI HEMICYCLE"))
        frame.setLeftSeatBarCountBinding(sizeBinding(leftSeats))
        val lGreen = Color(128, 255, 128)
        frame.setLeftSeatBarColorBinding(listBinding(Color.GREEN, lGreen))
        frame.setLeftSeatBarSizeBinding(propertyBinding(leftSeats) { t: Int -> t })
        frame.setLeftSeatBarLabelBinding(leftLabel.binding)
        frame.setMiddleSeatBarCountBinding(sizeBinding(middleSeats))
        val lRed = Color(255, 128, 128)
        frame.setMiddleSeatBarColorBinding(listBinding(Color.RED, lRed))
        frame.setMiddleSeatBarSizeBinding(propertyBinding(middleSeats) { t: Int -> t })
        frame.setMiddleSeatBarLabelBinding(middleLabel.binding)
        frame.setRightSeatBarCountBinding(sizeBinding(rightSeats))
        val lBlue = Color(128, 128, 255)
        frame.setRightSeatBarColorBinding(listBinding(Color.BLUE, lBlue))
        frame.setRightSeatBarSizeBinding(propertyBinding(rightSeats) { t: Int -> t })
        frame.setRightSeatBarLabelBinding(rightLabel.binding)
        frame.setSize(1024, 512)
        compareRendering("HemicycleFrame", "SeatsBar-1", frame)
        leftSeats.setAll(listOf(0, 1))
        leftLabel.value = "GREEN: 0/1"
        middleSeats.setAll(listOf(0, 2))
        middleLabel.value = "LIBERAL: 0/2"
        rightSeats.setAll(listOf(0, 8))
        rightLabel.value = "PROGRESSIVE CONSERVATIVE: 0/8"
        dotColors.setAll(listOf(
                lGreen, Color.WHITE, Color.WHITE, lRed, Color.WHITE, lBlue, lBlue, //
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, lRed, Color.WHITE, lBlue, lBlue, lBlue, //
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, lBlue, lBlue, lBlue))
        compareRendering("HemicycleFrame", "SeatsBar-2", frame)
        leftSeats.setAll(listOf(1, 7))
        leftLabel.value = "GREEN: 1/8"
        middleSeats.setAll(listOf(2, 4))
        middleLabel.value = "LIBERAL: 2/6"
        rightSeats.setAll(listOf(8, 5))
        rightLabel.value = "PROGRESSIVE CONSERVATIVE: 8/13"
        dotColors.setAll(listOf(
                Color.GREEN, lGreen, Color.RED, lBlue, lBlue, Color.BLUE, Color.BLUE, //
                lGreen, lGreen, lGreen, lRed, Color.RED, lBlue, Color.BLUE, Color.BLUE, Color.BLUE, //
                lGreen, lGreen, lGreen, lRed, lRed, lRed, lBlue, lBlue, Color.BLUE, Color.BLUE, Color.BLUE))
        compareRendering("HemicycleFrame", "SeatsBar-3", frame)
        leftSeats.setAll(listOf(8, 5))
        leftLabel.value = "GREEN: 8/13"
        middleSeats.setAll(listOf(2, 4))
        middleLabel.value = "LIBERAL: 2/6"
        rightSeats.setAll(listOf(1, 7))
        rightLabel.value = "PROGRESSIVE CONSERVATIVE: 1/8"
        compareRendering("HemicycleFrame", "SeatsBar-4", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderPositiveChangeBars() {
        val rowCounts = listOf(7, 9, 11)
        val dotColors = BindableList<Color>()
        dotColors.setAll(Collections.nCopies(27, Color.WHITE))
        val dotBorders = listOf(
                Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, //
                Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, //
                Color.GREEN, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE)
        val leftSeats = BindableList<Int>()
        val middleSeats = BindableList<Int>()
        val rightSeats = BindableList<Int>()
        val leftChange = BindableList<Int>()
        val rightChange = BindableList<Int>()
        sequenceOf(leftSeats, middleSeats, rightSeats, leftChange, rightChange)
                .forEach { list: BindableList<Int> -> list.setAll(listOf(0, 0)) }
        val leftLabel = BindableWrapper("GREEN: 0/0")
        val middleLabel = BindableWrapper("LIBERAL: 0/0")
        val rightLabel = BindableWrapper("PROGRESSIVE CONSERVATIVE: 0/0")
        val leftChangeLabel = BindableWrapper("GRN: +0/+0")
        val rightChangeLabel = BindableWrapper("PC: +0/+0")
        val frame = HemicycleFrame()
        frame.setNumRowsBinding(fixedBinding(rowCounts.size))
        frame.setRowCountsBinding(listBinding(rowCounts))
        frame.setNumDotsBinding(sizeBinding(dotColors))
        frame.setDotColorBinding(propertyBinding(dotColors) { t: Color -> t })
        frame.setDotBorderBinding(listBinding(dotBorders))
        frame.setHeaderBinding(fixedBinding("PEI HEMICYCLE"))
        val lGreen = Color(128, 255, 128)
        frame.setLeftSeatBarCountBinding(sizeBinding(leftSeats))
        frame.setLeftSeatBarColorBinding(listBinding(Color.GREEN, lGreen))
        frame.setLeftSeatBarSizeBinding(propertyBinding(leftSeats) { t: Int -> t })
        frame.setLeftSeatBarLabelBinding(leftLabel.binding)
        frame.setLeftChangeBarCountBinding(sizeBinding(leftChange))
        frame.setLeftChangeBarColorBinding(listBinding(Color.GREEN, lGreen))
        frame.setLeftChangeBarSizeBinding(propertyBinding(leftChange) { t: Int -> t })
        frame.setLeftChangeBarLabelBinding(leftChangeLabel.binding)
        frame.setLeftChangeBarStartBinding(fixedBinding(1))
        val lRed = Color(255, 128, 128)
        frame.setMiddleSeatBarCountBinding(sizeBinding(middleSeats))
        frame.setMiddleSeatBarColorBinding(listBinding(Color.RED, lRed))
        frame.setMiddleSeatBarSizeBinding(propertyBinding(middleSeats) { t: Int -> t })
        frame.setMiddleSeatBarLabelBinding(middleLabel.binding)
        val lBlue = Color(128, 128, 255)
        frame.setRightSeatBarCountBinding(sizeBinding(rightSeats))
        frame.setRightSeatBarColorBinding(listBinding(Color.BLUE, lBlue))
        frame.setRightSeatBarSizeBinding(propertyBinding(rightSeats) { t: Int -> t })
        frame.setRightSeatBarLabelBinding(rightLabel.binding)
        frame.setRightChangeBarCountBinding(sizeBinding(rightChange))
        frame.setRightChangeBarColorBinding(listBinding(Color.BLUE, lBlue))
        frame.setRightChangeBarSizeBinding(propertyBinding(rightChange) { t: Int -> t })
        frame.setRightChangeBarLabelBinding(rightChangeLabel.binding)
        frame.setRightChangeBarStartBinding(fixedBinding(8))
        frame.setSize(1024, 512)
        compareRendering("HemicycleFrame", "ChangeBar-1", frame)
        leftSeats.setAll(listOf(0, 1))
        leftLabel.value = "GREEN: 0/1"
        middleSeats.setAll(listOf(0, 2))
        middleLabel.value = "LIBERAL: 0/2"
        rightSeats.setAll(listOf(0, 8))
        rightLabel.value = "PROGRESSIVE CONSERVATIVE: 0/8"
        leftChange.setAll(listOf(0, 1))
        leftChangeLabel.value = "GRN: +0/+1"
        rightChange.setAll(listOf(0, 3))
        rightChangeLabel.value = "PC: +0/+3"
        dotColors.setAll(
                listOf(
                        lGreen, Color.WHITE, Color.WHITE, lRed, Color.WHITE, lBlue, lBlue, //
                        Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, lRed, Color.WHITE, lBlue, lBlue, lBlue, //
                        Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, lBlue, lBlue, lBlue))
        compareRendering("HemicycleFrame", "ChangeBar-2", frame)
        leftSeats.setAll(listOf(1, 7))
        leftLabel.value = "GREEN: 1/8"
        middleSeats.setAll(listOf(2, 4))
        middleLabel.value = "LIBERAL: 2/6"
        rightSeats.setAll(listOf(8, 5))
        rightLabel.value = "PROGRESSIVE CONSERVATIVE: 8/13"
        leftChange.setAll(listOf(1, 6))
        leftChangeLabel.value = "GRN: +1/+7"
        rightChange.setAll(listOf(3, 2))
        rightChangeLabel.value = "PC: +3/+5"
        dotColors.setAll(
                listOf(
                        Color.GREEN, lGreen, Color.RED, lBlue, lBlue, Color.BLUE, Color.BLUE, //
                        lGreen, lGreen, lGreen, lRed, Color.RED, lBlue, Color.BLUE, Color.BLUE, Color.BLUE, //
                        lGreen, lGreen, lGreen, lRed, lRed, lRed, lBlue, lBlue, Color.BLUE, Color.BLUE, Color.BLUE))
        compareRendering("HemicycleFrame", "ChangeBar-3", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderNegativeChangeBars() {
        val rowCounts = listOf(87)
        val dotColors = BindableList<Color>()
        dotColors.addAll(Collections.nCopies(5, Color.RED))
        dotColors.addAll(Collections.nCopies(4, Color.ORANGE))
        dotColors.addAll(Collections.nCopies(17, Color.GREEN.darker()))
        dotColors.addAll(Collections.nCopies(61, Color.BLUE))
        val leftSeats = BindableList<Int>()
        val middleSeats = BindableList<Int>()
        val rightSeats = BindableList<Int>()
        val leftChange = BindableList<Int>()
        val rightChange = BindableList<Int>()
        leftSeats.setAll(listOf(5))
        middleSeats.setAll(listOf(21))
        rightSeats.setAll(listOf(61))
        leftChange.setAll(listOf(-3))
        rightChange.setAll(listOf(-5))
        val leftLabel = BindableWrapper("LIBERAL: 5")
        val middleLabel = BindableWrapper("OTHERS: 21")
        val rightLabel = BindableWrapper("PROGRESSIVE CONSERVATIVE: 61")
        val leftChangeLabel = BindableWrapper("LIB: -3")
        val rightChangeLabel = BindableWrapper("PC: -5")
        val frame = HemicycleFrame()
        frame.setNumRowsBinding(fixedBinding(rowCounts.size))
        frame.setRowCountsBinding(listBinding(rowCounts))
        frame.setNumDotsBinding(sizeBinding(dotColors))
        frame.setDotColorBinding(propertyBinding(dotColors) { t: Color -> t })
        frame.setHeaderBinding(fixedBinding("ALBERTA HEMICYCLE"))
        frame.setLeftSeatBarCountBinding(sizeBinding(leftSeats))
        frame.setLeftSeatBarColorBinding(listBinding(Color.RED))
        frame.setLeftSeatBarSizeBinding(propertyBinding(leftSeats) { t: Int -> t })
        frame.setLeftSeatBarLabelBinding(leftLabel.binding)
        frame.setLeftChangeBarCountBinding(sizeBinding(leftChange))
        frame.setLeftChangeBarColorBinding(listBinding(Color.RED))
        frame.setLeftChangeBarSizeBinding(propertyBinding(leftChange) { t: Int -> t })
        frame.setLeftChangeBarLabelBinding(leftChangeLabel.binding)
        frame.setLeftChangeBarStartBinding(fixedBinding(8))
        frame.setMiddleSeatBarCountBinding(sizeBinding(middleSeats))
        frame.setMiddleSeatBarColorBinding(listBinding(Color.GRAY))
        frame.setMiddleSeatBarSizeBinding(propertyBinding(middleSeats) { t: Int -> t })
        frame.setMiddleSeatBarLabelBinding(middleLabel.binding)
        frame.setRightSeatBarCountBinding(sizeBinding(rightSeats))
        frame.setRightSeatBarColorBinding(listBinding(Color.BLUE))
        frame.setRightSeatBarSizeBinding(propertyBinding(rightSeats) { t: Int -> t })
        frame.setRightSeatBarLabelBinding(rightLabel.binding)
        frame.setRightChangeBarCountBinding(sizeBinding(rightChange))
        frame.setRightChangeBarColorBinding(listBinding(Color.BLUE))
        frame.setRightChangeBarSizeBinding(propertyBinding(rightChange) { t: Int -> t })
        frame.setRightChangeBarLabelBinding(rightChangeLabel.binding)
        frame.setRightChangeBarStartBinding(fixedBinding(66))
        frame.setSize(1024, 512)
        compareRendering("HemicycleFrame", "ChangeBar-Negative", frame)
    }
}
