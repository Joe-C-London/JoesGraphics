package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.io.IOException
import java.util.Collections
import java.util.concurrent.TimeUnit
import kotlin.Throws

class HemicycleFrameTest {
    @Test
    fun testRowCounts() {
        val rowCounts = listOf(5, 6, 7, 9)
        val frame = HemicycleFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            rowsPublisher = rowCounts.asOneTimePublisher(),
            dotsPublisher = emptyList<HemicycleFrame.Dot>().asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numRows }, IsEqual(4))
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
            Color.GREEN, Color.GREEN, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE
        )
        val frame = HemicycleFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            dotsPublisher = dotColors.map { HemicycleFrame.Dot(color = it, border = null) }.asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(27))
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
            Color.GREEN, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE
        )
        val frame = HemicycleFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            dotsPublisher = dotColors.map { HemicycleFrame.Dot(color = Color.WHITE, border = it) }.asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(27))
        Assert.assertEquals(Color.RED, frame.getDotBorder(0))
        Assert.assertEquals(Color.BLUE, frame.getDotBorder(9))
        Assert.assertEquals(Color.GREEN, frame.getDotBorder(18))
    }

    @Test
    fun testLeftSeatBar() {
        val frame = HemicycleFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            dotsPublisher = emptyList<HemicycleFrame.Dot>().asOneTimePublisher(),
            leftSeatBarPublisher =
            listOf(
                HemicycleFrame.Bar(Color.GREEN, 1),
                HemicycleFrame.Bar(Color(128, 255, 128), 7)
            )
                .asOneTimePublisher(),
            leftSeatBarLabelPublisher = "GREEN: 1/8".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.leftSeatBarCount }, IsEqual(2))
        Assert.assertEquals(Color.GREEN, frame.getLeftSeatBarColor(0))
        Assert.assertEquals(7, frame.getLeftSeatBarSize(1).toLong())
        Assert.assertEquals("GREEN: 1/8", frame.getLeftSeatBarLabel())
    }

    @Test
    fun testRightSeatBar() {
        val frame = HemicycleFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            dotsPublisher = emptyList<HemicycleFrame.Dot>().asOneTimePublisher(),
            rightSeatBarPublisher =
            listOf(
                HemicycleFrame.Bar(Color.BLUE, 8),
                HemicycleFrame.Bar(Color(128, 128, 255), 5)
            )
                .asOneTimePublisher(),
            rightSeatBarLabelPublisher = "PROGRESSIVE CONSERVATIVE: 8/13".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.rightSeatBarCount }, IsEqual(2))
        Assert.assertEquals(Color.BLUE, frame.getRightSeatBarColor(0))
        Assert.assertEquals(5, frame.getRightSeatBarSize(1).toLong())
        Assert.assertEquals("PROGRESSIVE CONSERVATIVE: 8/13", frame.getRightSeatBarLabel())
    }

    @Test
    fun testMiddleSeatBar() {
        val frame = HemicycleFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            dotsPublisher = emptyList<HemicycleFrame.Dot>().asOneTimePublisher(),
            middleSeatBarPublisher =
            listOf(
                HemicycleFrame.Bar(Color.RED, 2),
                HemicycleFrame.Bar(Color(255, 128, 128), 4)
            )
                .asOneTimePublisher(),
            middleSeatBarLabelPublisher = "LIBERAL: 2/6".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.middleSeatBarCount }, IsEqual(2))
        Assert.assertEquals(Color.RED, frame.getMiddleSeatBarColor(0))
        Assert.assertEquals(4, frame.getMiddleSeatBarSize(1).toLong())
        Assert.assertEquals("LIBERAL: 2/6", frame.getMiddleSeatBarLabel())
    }

    @Test
    fun testLeftChangeBar() {
        val frame = HemicycleFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            dotsPublisher = emptyList<HemicycleFrame.Dot>().asOneTimePublisher(),
            leftChangeBarPublisher =
            listOf(
                HemicycleFrame.Bar(Color.GREEN, 1),
                HemicycleFrame.Bar(Color(128, 255, 128), 6)
            )
                .asOneTimePublisher(),
            leftChangeBarStartPublisher = 1.asOneTimePublisher(),
            leftChangeBarLabelPublisher = "GRN: +1/+7".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.leftChangeBarCount }, IsEqual(2))
        Assert.assertEquals(Color.GREEN, frame.getLeftChangeBarColor(0))
        Assert.assertEquals(6, frame.getLeftChangeBarSize(1).toLong())
        Assert.assertEquals(1, frame.getLeftChangeBarStart().toLong())
        Assert.assertEquals("GRN: +1/+7", frame.getLeftChangeBarLabel())
    }

    @Test
    fun testRightChangeBar() {
        val frame = HemicycleFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            dotsPublisher = emptyList<HemicycleFrame.Dot>().asOneTimePublisher(),
            rightChangeBarPublisher =
            listOf(
                HemicycleFrame.Bar(Color.BLUE, 3),
                HemicycleFrame.Bar(Color(128, 128, 255), 2)
            )
                .asOneTimePublisher(),
            rightChangeBarStartPublisher = 8.asOneTimePublisher(),
            rightChangeBarLabelPublisher = "PC: +3/+5".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.rightChangeBarCount }, IsEqual(2))
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
            Color.GREEN, Color.GREEN, Color.GREEN, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE
        )
        val dotBorders = listOf(
            Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, //
            Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, //
            Color.GREEN, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE
        )
        val frame = HemicycleFrame(
            headerPublisher = "PEI HEMICYCLE".asOneTimePublisher(),
            rowsPublisher = rowCounts.asOneTimePublisher(),
            dotsPublisher = dotColors.zip(dotBorders) { c, b -> HemicycleFrame.Dot(color = c, border = b) }.asOneTimePublisher()
        )
        frame.setSize(1024, 512)
        compareRendering("HemicycleFrame", "DotsOnly", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSeatsBars() {
        val rowCounts = listOf(7, 9, 11)
        val dotColors = Publisher(Collections.nCopies(27, Color.WHITE))
        val dotBorders = listOf(
            Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, //
            Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, //
            Color.GREEN, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE
        )
        val leftSeats = Publisher(listOf(0, 0))
        val middleSeats = Publisher(listOf(0, 0))
        val rightSeats = Publisher(listOf(0, 0))
        val leftLabel = Publisher("GREEN: 0/0")
        val middleLabel = Publisher("LIBERAL: 0/0")
        val rightLabel = Publisher("PROGRESSIVE CONSERVATIVE: 0/0")
        val lGreen = Color(128, 255, 128)
        val lRed = Color(255, 128, 128)
        val lBlue = Color(128, 128, 255)
        val frame = HemicycleFrame(
            headerPublisher = "PEI HEMICYCLE".asOneTimePublisher(),
            rowsPublisher = rowCounts.asOneTimePublisher(),
            dotsPublisher = dotColors.map { it.zip(dotBorders) { c, b -> HemicycleFrame.Dot(color = c, border = b) } },
            leftSeatBarPublisher = leftSeats.map { seats -> listOf(Color.GREEN, lGreen).zip(seats) { c, s -> HemicycleFrame.Bar(c, s) } },
            leftSeatBarLabelPublisher = leftLabel,
            middleSeatBarPublisher = middleSeats.map { seats -> listOf(Color.RED, lRed).zip(seats) { c, s -> HemicycleFrame.Bar(c, s) } },
            middleSeatBarLabelPublisher = middleLabel,
            rightSeatBarPublisher = rightSeats.map { seats -> listOf(Color.BLUE, lBlue).zip(seats) { c, s -> HemicycleFrame.Bar(c, s) } },
            rightSeatBarLabelPublisher = rightLabel
        )
        frame.setSize(1024, 512)
        compareRendering("HemicycleFrame", "SeatsBar-1", frame)
        leftSeats.submit(listOf(0, 1))
        leftLabel.submit("GREEN: 0/1")
        middleSeats.submit(listOf(0, 2))
        middleLabel.submit("LIBERAL: 0/2")
        rightSeats.submit(listOf(0, 8))
        rightLabel.submit("PROGRESSIVE CONSERVATIVE: 0/8")
        dotColors.submit(
            listOf(
                lGreen, Color.WHITE, Color.WHITE, lRed, Color.WHITE, lBlue, lBlue, //
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, lRed, Color.WHITE, lBlue, lBlue, lBlue, //
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, lBlue, lBlue, lBlue
            )
        )
        compareRendering("HemicycleFrame", "SeatsBar-2", frame)
        leftSeats.submit(listOf(1, 7))
        leftLabel.submit("GREEN: 1/8")
        middleSeats.submit(listOf(2, 4))
        middleLabel.submit("LIBERAL: 2/6")
        rightSeats.submit(listOf(8, 5))
        rightLabel.submit("PROGRESSIVE CONSERVATIVE: 8/13")
        dotColors.submit(
            listOf(
                Color.GREEN, lGreen, Color.RED, lBlue, lBlue, Color.BLUE, Color.BLUE, //
                lGreen, lGreen, lGreen, lRed, Color.RED, lBlue, Color.BLUE, Color.BLUE, Color.BLUE, //
                lGreen, lGreen, lGreen, lRed, lRed, lRed, lBlue, lBlue, Color.BLUE, Color.BLUE, Color.BLUE
            )
        )
        compareRendering("HemicycleFrame", "SeatsBar-3", frame)
        leftSeats.submit(listOf(8, 5))
        leftLabel.submit("GREEN: 8/13")
        middleSeats.submit(listOf(2, 4))
        middleLabel.submit("LIBERAL: 2/6")
        rightSeats.submit(listOf(1, 7))
        rightLabel.submit("PROGRESSIVE CONSERVATIVE: 1/8")
        compareRendering("HemicycleFrame", "SeatsBar-4", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderPositiveChangeBars() {
        val rowCounts = listOf(7, 9, 11)
        val dotColors = Publisher(Collections.nCopies(27, Color.WHITE))
        val dotBorders = listOf(
            Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, //
            Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, //
            Color.GREEN, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE
        )
        val leftSeats = Publisher(listOf(0, 0))
        val middleSeats = Publisher(listOf(0, 0))
        val rightSeats = Publisher(listOf(0, 0))
        val leftChange = Publisher(listOf(0, 0))
        val rightChange = Publisher(listOf(0, 0))
        val leftLabel = Publisher("GREEN: 0/0")
        val middleLabel = Publisher("LIBERAL: 0/0")
        val rightLabel = Publisher("PROGRESSIVE CONSERVATIVE: 0/0")
        val leftChangeLabel = Publisher("GRN: +0/+0")
        val rightChangeLabel = Publisher("PC: +0/+0")
        val lGreen = Color(128, 255, 128)
        val lRed = Color(255, 128, 128)
        val lBlue = Color(128, 128, 255)
        val frame = HemicycleFrame(
            headerPublisher = "PEI HEMICYCLE".asOneTimePublisher(),
            rowsPublisher = rowCounts.asOneTimePublisher(),
            dotsPublisher = dotColors.map { it.zip(dotBorders) { c, b -> HemicycleFrame.Dot(color = c, border = b) } },
            leftSeatBarPublisher = leftSeats.map { seats -> listOf(Color.GREEN, lGreen).zip(seats) { c, s -> HemicycleFrame.Bar(c, s) } },
            leftSeatBarLabelPublisher = leftLabel,
            middleSeatBarPublisher = middleSeats.map { seats -> listOf(Color.RED, lRed).zip(seats) { c, s -> HemicycleFrame.Bar(c, s) } },
            middleSeatBarLabelPublisher = middleLabel,
            rightSeatBarPublisher = rightSeats.map { seats -> listOf(Color.BLUE, lBlue).zip(seats) { c, s -> HemicycleFrame.Bar(c, s) } },
            rightSeatBarLabelPublisher = rightLabel,
            leftChangeBarPublisher = leftChange.map { change -> listOf(Color.GREEN, lGreen).zip(change) { c, s -> HemicycleFrame.Bar(c, s) } },
            leftChangeBarLabelPublisher = leftChangeLabel,
            leftChangeBarStartPublisher = 1.asOneTimePublisher(),
            rightChangeBarPublisher = rightChange.map { change -> listOf(Color.BLUE, lBlue).zip(change) { c, s -> HemicycleFrame.Bar(c, s) } },
            rightChangeBarLabelPublisher = rightChangeLabel,
            rightChangeBarStartPublisher = 8.asOneTimePublisher()
        )
        frame.setSize(1024, 512)
        compareRendering("HemicycleFrame", "ChangeBar-1", frame)
        leftSeats.submit(listOf(0, 1))
        leftLabel.submit("GREEN: 0/1")
        middleSeats.submit(listOf(0, 2))
        middleLabel.submit("LIBERAL: 0/2")
        rightSeats.submit(listOf(0, 8))
        rightLabel.submit("PROGRESSIVE CONSERVATIVE: 0/8")
        leftChange.submit(listOf(0, 1))
        leftChangeLabel.submit("GRN: +0/+1")
        rightChange.submit(listOf(0, 3))
        rightChangeLabel.submit("PC: +0/+3")
        dotColors.submit(
            listOf(
                lGreen, Color.WHITE, Color.WHITE, lRed, Color.WHITE, lBlue, lBlue, //
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, lRed, Color.WHITE, lBlue, lBlue, lBlue, //
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, lBlue, lBlue, lBlue
            )
        )
        compareRendering("HemicycleFrame", "ChangeBar-2", frame)
        leftSeats.submit(listOf(1, 7))
        leftLabel.submit("GREEN: 1/8")
        middleSeats.submit(listOf(2, 4))
        middleLabel.submit("LIBERAL: 2/6")
        rightSeats.submit(listOf(8, 5))
        rightLabel.submit("PROGRESSIVE CONSERVATIVE: 8/13")
        leftChange.submit(listOf(1, 6))
        leftChangeLabel.submit("GRN: +1/+7")
        rightChange.submit(listOf(3, 2))
        rightChangeLabel.submit("PC: +3/+5")
        dotColors.submit(
            listOf(
                Color.GREEN, lGreen, Color.RED, lBlue, lBlue, Color.BLUE, Color.BLUE, //
                lGreen, lGreen, lGreen, lRed, Color.RED, lBlue, Color.BLUE, Color.BLUE, Color.BLUE, //
                lGreen, lGreen, lGreen, lRed, lRed, lRed, lBlue, lBlue, Color.BLUE, Color.BLUE, Color.BLUE
            )
        )
        compareRendering("HemicycleFrame", "ChangeBar-3", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderNegativeChangeBars() {
        val rowCounts = listOf(87)
        val dotColors = Publisher<List<Color>>(
            listOf(
                Collections.nCopies(5, Color.RED),
                Collections.nCopies(4, Color.ORANGE),
                Collections.nCopies(17, Color.GREEN.darker()),
                Collections.nCopies(61, Color.BLUE)
            ).flatten()
        )
        val leftSeats = Publisher(listOf(5))
        val middleSeats = Publisher(listOf(21))
        val rightSeats = Publisher(listOf(61))
        val leftChange = Publisher(listOf(-3))
        val rightChange = Publisher(listOf(-5))
        val leftLabel = Publisher("LIBERAL: 5")
        val middleLabel = Publisher("OTHERS: 21")
        val rightLabel = Publisher("PROGRESSIVE CONSERVATIVE: 61")
        val leftChangeLabel = Publisher("LIB: -3")
        val rightChangeLabel = Publisher("PC: -5")
        val frame = HemicycleFrame(
            headerPublisher = "ALBERTA HEMICYCLE".asOneTimePublisher(),
            rowsPublisher = rowCounts.asOneTimePublisher(),
            dotsPublisher = dotColors.mapElements { HemicycleFrame.Dot(color = it, border = null) },
            leftSeatBarPublisher = leftSeats.mapElements { HemicycleFrame.Bar(Color.RED, it) },
            leftSeatBarLabelPublisher = leftLabel,
            middleSeatBarPublisher = middleSeats.mapElements { HemicycleFrame.Bar(Color.GRAY, it) },
            middleSeatBarLabelPublisher = middleLabel,
            rightSeatBarPublisher = rightSeats.mapElements { HemicycleFrame.Bar(Color.BLUE, it) },
            rightSeatBarLabelPublisher = rightLabel,
            leftChangeBarPublisher = leftChange.mapElements { HemicycleFrame.Bar(Color.RED, it) },
            leftChangeBarLabelPublisher = leftChangeLabel,
            leftChangeBarStartPublisher = 8.asOneTimePublisher(),
            rightChangeBarPublisher = rightChange.mapElements { HemicycleFrame.Bar(Color.BLUE, it) },
            rightChangeBarLabelPublisher = rightChangeLabel,
            rightChangeBarStartPublisher = 66.asOneTimePublisher()
        )
        frame.setSize(1024, 512)
        compareRendering("HemicycleFrame", "ChangeBar-Negative", frame)
    }
}