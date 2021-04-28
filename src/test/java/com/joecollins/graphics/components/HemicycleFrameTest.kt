package com.joecollins.graphics.components

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.mapElements
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
        frame.setRowsBinding(fixedBinding(rowCounts))
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
        frame.setDotsBinding(fixedBinding(dotColors.map { HemicycleFrame.Dot(color = it, border = null) }))
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
        frame.setDotsBinding(fixedBinding(dotColors.map { HemicycleFrame.Dot(color = Color.WHITE, border = it) }))
        Assert.assertEquals(27, frame.numDots.toLong())
        Assert.assertEquals(Color.RED, frame.getDotBorder(0))
        Assert.assertEquals(Color.BLUE, frame.getDotBorder(9))
        Assert.assertEquals(Color.GREEN, frame.getDotBorder(18))
    }

    @Test
    fun testLeftSeatBar() {
        val frame = HemicycleFrame()
        frame.setLeftSeatBarBinding(fixedBinding(listOf(
            HemicycleFrame.Bar(Color.GREEN, 1),
            HemicycleFrame.Bar(Color(128, 255, 128), 7)
        )))
        frame.setLeftSeatBarLabelBinding(fixedBinding("GREEN: 1/8"))
        Assert.assertEquals(2, frame.leftSeatBarCount.toLong())
        Assert.assertEquals(Color.GREEN, frame.getLeftSeatBarColor(0))
        Assert.assertEquals(7, frame.getLeftSeatBarSize(1).toLong())
        Assert.assertEquals("GREEN: 1/8", frame.getLeftSeatBarLabel())
    }

    @Test
    fun testRightSeatBar() {
        val frame = HemicycleFrame()
        frame.setRightSeatBarBinding(fixedBinding(listOf(
            HemicycleFrame.Bar(Color.BLUE, 8),
            HemicycleFrame.Bar(Color(128, 128, 255), 5)
        )))
        frame.setRightSeatBarLabelBinding(fixedBinding("PROGRESSIVE CONSERVATIVE: 8/13"))
        Assert.assertEquals(2, frame.rightSeatBarCount.toLong())
        Assert.assertEquals(Color.BLUE, frame.getRightSeatBarColor(0))
        Assert.assertEquals(5, frame.getRightSeatBarSize(1).toLong())
        Assert.assertEquals("PROGRESSIVE CONSERVATIVE: 8/13", frame.getRightSeatBarLabel())
    }

    @Test
    fun testMiddleSeatBar() {
        val frame = HemicycleFrame()
        frame.setMiddleSeatBarBinding(fixedBinding(listOf(
            HemicycleFrame.Bar(Color.RED, 2),
            HemicycleFrame.Bar(Color(255, 128, 128), 4)
        )))
        frame.setMiddleSeatBarLabelBinding(fixedBinding("LIBERAL: 2/6"))
        Assert.assertEquals(2, frame.middleSeatBarCount.toLong())
        Assert.assertEquals(Color.RED, frame.getMiddleSeatBarColor(0))
        Assert.assertEquals(4, frame.getMiddleSeatBarSize(1).toLong())
        Assert.assertEquals("LIBERAL: 2/6", frame.getMiddleSeatBarLabel())
    }

    @Test
    fun testLeftChangeBar() {
        val frame = HemicycleFrame()
        frame.setLeftChangeBarBinding(fixedBinding(listOf(
            HemicycleFrame.Bar(Color.GREEN, 1),
            HemicycleFrame.Bar(Color(128, 255, 128), 6)
        )))
        frame.setLeftChangeBarStartBinding(fixedBinding(1))
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
        frame.setRightChangeBarBinding(fixedBinding(listOf(
            HemicycleFrame.Bar(Color.BLUE, 3),
            HemicycleFrame.Bar(Color(128, 128, 255), 2)
        )))
        frame.setRightChangeBarStartBinding(fixedBinding(8))
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
        frame.setRowsBinding(fixedBinding(rowCounts))
        frame.setDotsBinding(fixedBinding(dotColors.zip(dotBorders) { c, b -> HemicycleFrame.Dot(color = c, border = b) }))
        frame.setHeaderBinding(fixedBinding("PEI HEMICYCLE"))
        frame.setSize(1024, 512)
        compareRendering("HemicycleFrame", "DotsOnly", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSeatsBars() {
        val rowCounts = listOf(7, 9, 11)
        val dotColors = BindableWrapper(Collections.nCopies(27, Color.WHITE))
        val dotBorders = listOf(
                Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, //
                Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, //
                Color.GREEN, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE)
        val leftSeats = BindableWrapper(listOf(0, 0))
        val middleSeats = BindableWrapper(listOf(0, 0))
        val rightSeats = BindableWrapper(listOf(0, 0))
        val leftLabel = BindableWrapper("GREEN: 0/0")
        val middleLabel = BindableWrapper("LIBERAL: 0/0")
        val rightLabel = BindableWrapper("PROGRESSIVE CONSERVATIVE: 0/0")
        val frame = HemicycleFrame()
        frame.setRowsBinding(fixedBinding(rowCounts))
        frame.setDotsBinding(
            dotColors.binding.map { it.zip(dotBorders) { c, b -> HemicycleFrame.Dot(color = c, border = b) } }
        )
        frame.setHeaderBinding(fixedBinding("PEI HEMICYCLE"))
        val lGreen = Color(128, 255, 128)
        frame.setLeftSeatBarBinding(leftSeats.binding.map { seats -> listOf(Color.GREEN, lGreen).zip(seats) { c, s -> HemicycleFrame.Bar(c, s) } })
        frame.setLeftSeatBarLabelBinding(leftLabel.binding)
        val lRed = Color(255, 128, 128)
        frame.setMiddleSeatBarBinding(middleSeats.binding.map { seats -> listOf(Color.RED, lRed).zip(seats) { c, s -> HemicycleFrame.Bar(c, s) } })
        frame.setMiddleSeatBarLabelBinding(middleLabel.binding)
        val lBlue = Color(128, 128, 255)
        frame.setRightSeatBarBinding(rightSeats.binding.map { seats -> listOf(Color.BLUE, lBlue).zip(seats) { c, s -> HemicycleFrame.Bar(c, s) } })
        frame.setRightSeatBarLabelBinding(rightLabel.binding)
        frame.setSize(1024, 512)
        compareRendering("HemicycleFrame", "SeatsBar-1", frame)
        leftSeats.value = listOf(0, 1)
        leftLabel.value = "GREEN: 0/1"
        middleSeats.value = listOf(0, 2)
        middleLabel.value = "LIBERAL: 0/2"
        rightSeats.value = listOf(0, 8)
        rightLabel.value = "PROGRESSIVE CONSERVATIVE: 0/8"
        dotColors.value = listOf(
                lGreen, Color.WHITE, Color.WHITE, lRed, Color.WHITE, lBlue, lBlue, //
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, lRed, Color.WHITE, lBlue, lBlue, lBlue, //
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, lBlue, lBlue, lBlue)
        compareRendering("HemicycleFrame", "SeatsBar-2", frame)
        leftSeats.value = listOf(1, 7)
        leftLabel.value = "GREEN: 1/8"
        middleSeats.value = listOf(2, 4)
        middleLabel.value = "LIBERAL: 2/6"
        rightSeats.value = listOf(8, 5)
        rightLabel.value = "PROGRESSIVE CONSERVATIVE: 8/13"
        dotColors.value = listOf(
                Color.GREEN, lGreen, Color.RED, lBlue, lBlue, Color.BLUE, Color.BLUE, //
                lGreen, lGreen, lGreen, lRed, Color.RED, lBlue, Color.BLUE, Color.BLUE, Color.BLUE, //
                lGreen, lGreen, lGreen, lRed, lRed, lRed, lBlue, lBlue, Color.BLUE, Color.BLUE, Color.BLUE)
        compareRendering("HemicycleFrame", "SeatsBar-3", frame)
        leftSeats.value = listOf(8, 5)
        leftLabel.value = "GREEN: 8/13"
        middleSeats.value = listOf(2, 4)
        middleLabel.value = "LIBERAL: 2/6"
        rightSeats.value = listOf(1, 7)
        rightLabel.value = "PROGRESSIVE CONSERVATIVE: 1/8"
        compareRendering("HemicycleFrame", "SeatsBar-4", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderPositiveChangeBars() {
        val rowCounts = listOf(7, 9, 11)
        val dotColors = BindableWrapper(Collections.nCopies(27, Color.WHITE))
        val dotBorders = listOf(
                Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, //
                Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, //
                Color.GREEN, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE)
        val leftSeats = BindableWrapper(listOf(0, 0))
        val middleSeats = BindableWrapper(listOf(0, 0))
        val rightSeats = BindableWrapper(listOf(0, 0))
        val leftChange = BindableWrapper(listOf(0, 0))
        val rightChange = BindableWrapper(listOf(0, 0))
        val leftLabel = BindableWrapper("GREEN: 0/0")
        val middleLabel = BindableWrapper("LIBERAL: 0/0")
        val rightLabel = BindableWrapper("PROGRESSIVE CONSERVATIVE: 0/0")
        val leftChangeLabel = BindableWrapper("GRN: +0/+0")
        val rightChangeLabel = BindableWrapper("PC: +0/+0")
        val frame = HemicycleFrame()
        frame.setRowsBinding(fixedBinding(rowCounts))
        frame.setDotsBinding(
            dotColors.binding.map { it.zip(dotBorders) { c, b -> HemicycleFrame.Dot(color = c, border = b) } }
        )
        frame.setHeaderBinding(fixedBinding("PEI HEMICYCLE"))
        val lGreen = Color(128, 255, 128)
        frame.setLeftSeatBarBinding(leftSeats.binding.map { seats -> listOf(Color.GREEN, lGreen).zip(seats) { c, s -> HemicycleFrame.Bar(c, s) } })
        frame.setLeftSeatBarLabelBinding(leftLabel.binding)
        frame.setLeftChangeBarBinding(leftChange.binding.map { change -> listOf(Color.GREEN, lGreen).zip(change) { c, s -> HemicycleFrame.Bar(c, s) } })
        frame.setLeftChangeBarLabelBinding(leftChangeLabel.binding)
        frame.setLeftChangeBarStartBinding(fixedBinding(1))
        val lRed = Color(255, 128, 128)
        frame.setMiddleSeatBarBinding(middleSeats.binding.map { seats -> listOf(Color.RED, lRed).zip(seats) { c, s -> HemicycleFrame.Bar(c, s) } })
        frame.setMiddleSeatBarLabelBinding(middleLabel.binding)
        val lBlue = Color(128, 128, 255)
        frame.setRightSeatBarBinding(rightSeats.binding.map { seats -> listOf(Color.BLUE, lBlue).zip(seats) { c, s -> HemicycleFrame.Bar(c, s) } })
        frame.setRightSeatBarLabelBinding(rightLabel.binding)
        frame.setRightChangeBarBinding(rightChange.binding.map { change -> listOf(Color.BLUE, lBlue).zip(change) { c, s -> HemicycleFrame.Bar(c, s) } })
        frame.setRightChangeBarLabelBinding(rightChangeLabel.binding)
        frame.setRightChangeBarStartBinding(fixedBinding(8))
        frame.setSize(1024, 512)
        compareRendering("HemicycleFrame", "ChangeBar-1", frame)
        leftSeats.value = listOf(0, 1)
        leftLabel.value = "GREEN: 0/1"
        middleSeats.value = listOf(0, 2)
        middleLabel.value = "LIBERAL: 0/2"
        rightSeats.value = listOf(0, 8)
        rightLabel.value = "PROGRESSIVE CONSERVATIVE: 0/8"
        leftChange.value = listOf(0, 1)
        leftChangeLabel.value = "GRN: +0/+1"
        rightChange.value = listOf(0, 3)
        rightChangeLabel.value = "PC: +0/+3"
        dotColors.value = listOf(
                lGreen, Color.WHITE, Color.WHITE, lRed, Color.WHITE, lBlue, lBlue, //
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, lRed, Color.WHITE, lBlue, lBlue, lBlue, //
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, lBlue, lBlue, lBlue)
        compareRendering("HemicycleFrame", "ChangeBar-2", frame)
        leftSeats.value = listOf(1, 7)
        leftLabel.value = "GREEN: 1/8"
        middleSeats.value = listOf(2, 4)
        middleLabel.value = "LIBERAL: 2/6"
        rightSeats.value = listOf(8, 5)
        rightLabel.value = "PROGRESSIVE CONSERVATIVE: 8/13"
        leftChange.value = listOf(1, 6)
        leftChangeLabel.value = "GRN: +1/+7"
        rightChange.value = listOf(3, 2)
        rightChangeLabel.value = "PC: +3/+5"
        dotColors.value = listOf(
                Color.GREEN, lGreen, Color.RED, lBlue, lBlue, Color.BLUE, Color.BLUE, //
                lGreen, lGreen, lGreen, lRed, Color.RED, lBlue, Color.BLUE, Color.BLUE, Color.BLUE, //
                lGreen, lGreen, lGreen, lRed, lRed, lRed, lBlue, lBlue, Color.BLUE, Color.BLUE, Color.BLUE)
        compareRendering("HemicycleFrame", "ChangeBar-3", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderNegativeChangeBars() {
        val rowCounts = listOf(87)
        val dotColors = BindableWrapper<List<Color>>(
            listOf(
                Collections.nCopies(5, Color.RED),
                Collections.nCopies(4, Color.ORANGE),
                Collections.nCopies(17, Color.GREEN.darker()),
                Collections.nCopies(61, Color.BLUE)
            ).flatten()
        )
        val leftSeats = BindableWrapper(listOf(5))
        val middleSeats = BindableWrapper(listOf(21))
        val rightSeats = BindableWrapper(listOf(61))
        val leftChange = BindableWrapper(listOf(-3))
        val rightChange = BindableWrapper(listOf(-5))
        val leftLabel = BindableWrapper("LIBERAL: 5")
        val middleLabel = BindableWrapper("OTHERS: 21")
        val rightLabel = BindableWrapper("PROGRESSIVE CONSERVATIVE: 61")
        val leftChangeLabel = BindableWrapper("LIB: -3")
        val rightChangeLabel = BindableWrapper("PC: -5")
        val frame = HemicycleFrame()
        frame.setRowsBinding(fixedBinding(rowCounts))
        frame.setDotsBinding(
            dotColors.binding.mapElements { HemicycleFrame.Dot(color = it, border = null) }
        )
        frame.setHeaderBinding(fixedBinding("ALBERTA HEMICYCLE"))
        frame.setLeftSeatBarBinding(leftSeats.binding.mapElements { HemicycleFrame.Bar(Color.RED, it) })
        frame.setLeftSeatBarLabelBinding(leftLabel.binding)
        frame.setLeftChangeBarBinding(leftChange.binding.mapElements { HemicycleFrame.Bar(Color.RED, it) })
        frame.setLeftChangeBarLabelBinding(leftChangeLabel.binding)
        frame.setLeftChangeBarStartBinding(fixedBinding(8))
        frame.setMiddleSeatBarBinding(middleSeats.binding.mapElements { HemicycleFrame.Bar(Color.GRAY, it) })
        frame.setMiddleSeatBarLabelBinding(middleLabel.binding)
        frame.setRightSeatBarBinding(rightSeats.binding.mapElements { HemicycleFrame.Bar(Color.BLUE, it) })
        frame.setRightSeatBarLabelBinding(rightLabel.binding)
        frame.setRightChangeBarBinding(rightChange.binding.mapElements { HemicycleFrame.Bar(Color.BLUE, it) })
        frame.setRightChangeBarLabelBinding(rightChangeLabel.binding)
        frame.setRightChangeBarStartBinding(fixedBinding(66))
        frame.setSize(1024, 512)
        compareRendering("HemicycleFrame", "ChangeBar-Negative", frame)
    }
}
