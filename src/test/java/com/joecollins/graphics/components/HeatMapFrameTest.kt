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
import kotlin.Throws
import org.junit.Assert
import org.junit.Test

class HeatMapFrameTest {
    @Test
    fun testDots() {
        val borderColors = sequenceOf(
                generateSequence { Color.BLUE }.take(8),
                generateSequence { Color.RED }.take(18),
                sequenceOf(DARK_GREEN))
                .flatten()
                .toList()
        val fillColors = listOf(
                Color.BLUE,
                Color.BLUE,
                Color.BLUE,
                Color.BLUE,
                Color.BLUE,
                Color.BLUE,
                Color.BLUE,
                Color.BLUE,
                DARK_GREEN,
                DARK_GREEN,
                DARK_GREEN,
                Color.BLUE,
                DARK_GREEN,
                Color.RED,
                Color.RED,
                Color.BLUE,
                DARK_GREEN,
                Color.RED,
                DARK_GREEN,
                Color.RED,
                Color.BLUE,
                Color.BLUE,
                DARK_GREEN,
                Color.BLUE,
                Color.RED,
                Color.RED,
                DARK_GREEN)
        val frame = HeatMapFrame()
        frame.setNumRowsBinding(fixedBinding(3))
        frame.setNumSquaresBinding(fixedBinding(27))
        frame.setSquareBordersBinding(listBinding(borderColors))
        frame.setSquareFillBinding(listBinding(fillColors))
        Assert.assertEquals(3, frame.numRows.toLong())
        Assert.assertEquals(27, frame.numSquares.toLong())
        Assert.assertEquals(Color.BLUE, frame.getSquareBorder(0))
        Assert.assertEquals(Color.RED, frame.getSquareBorder(12))
        Assert.assertEquals(DARK_GREEN, frame.getSquareBorder(26))
        Assert.assertEquals(Color.BLUE, frame.getSquareFill(0))
        Assert.assertEquals(DARK_GREEN, frame.getSquareFill(12))
        Assert.assertEquals(DARK_GREEN, frame.getSquareFill(26))
    }

    @Test
    fun testSeatBar() {
        val frame = HeatMapFrame()
        frame.setNumSeatBarsBinding(fixedBinding(2))
        frame.setSeatBarColorBinding(listBinding(Color.BLUE, Color(128, 128, 255)))
        frame.setSeatBarSizeBinding(listBinding(8, 5))
        frame.setSeatBarLabelBinding(fixedBinding("8/13"))
        Assert.assertEquals(2, frame.seatBarCount.toLong())
        Assert.assertEquals(Color.BLUE, frame.getSeatBarColor(0))
        Assert.assertEquals(5, frame.getSeatBarSize(1).toLong())
        Assert.assertEquals("8/13", frame.seatBarLabel)
    }

    @Test
    fun testChangeBar() {
        val frame = HeatMapFrame()
        frame.setNumChangeBarsBinding(fixedBinding(2))
        frame.setChangeBarColorBinding(listBinding(Color.BLUE, Color(128, 128, 255)))
        frame.setChangeBarSizeBinding(listBinding(3, 2))
        frame.setChangeBarLabelBinding(fixedBinding("+3/+5"))
        frame.setChangeBarStartBinding(fixedBinding(5))
        Assert.assertEquals(2, frame.changeBarCount.toLong())
        Assert.assertEquals(Color.BLUE, frame.getChangeBarColor(0))
        Assert.assertEquals(2, frame.getChangeBarSize(1).toLong())
        Assert.assertEquals("+3/+5", frame.changeBarLabel)
        Assert.assertEquals(5, frame.changeBarStart.toLong())
    }

    @Test
    @Throws(IOException::class)
    fun testRenderEvenWide() {
        val results = peiResults
        val squares = BindableList<Pair<Color, Color>>()
        squares.setAll(
                sequenceOf(
                        20, 6, 19, 18, 7, 1, 2, 4, 5, 13, 21, 15, 22, 14, 25, 3, 12, 16, 23, 10, 26, 9, 11,
                        8, 27, 24, 17)
                        .map { results[it]!! }
                        .toList())
        val seatBars = BindableList<Pair<Color, Int>>()
        seatBars.setAll(
                listOf(Pair(Color.BLUE, 8), Pair(Color(128, 128, 255), 5)))
        val seatLabel = BindableWrapper("8/13")
        val changeBars = BindableList<Pair<Color, Int>>()
        changeBars.setAll(
                listOf(Pair(Color.BLUE, 3), Pair(Color(128, 128, 255), 2)))
        val changeLabel = BindableWrapper("+3/+5")
        val changeStart = BindableWrapper(8)
        val borderColor = BindableWrapper(Color.BLUE)
        val header = BindableWrapper<String?>("PROGRESSIVE CONSERVATIVE HEAT MAP")
        val frame = HeatMapFrame()
        frame.setNumRowsBinding(fixedBinding(3))
        frame.setNumSquaresBinding(sizeBinding(squares))
        frame.setSquareBordersBinding(propertyBinding(squares) { it.first })
        frame.setSquareFillBinding(propertyBinding(squares) { it.second })
        frame.setNumSeatBarsBinding(sizeBinding(seatBars))
        frame.setSeatBarColorBinding(propertyBinding(seatBars) { it.first })
        frame.setSeatBarSizeBinding(propertyBinding(seatBars) { it.second })
        frame.setSeatBarLabelBinding(seatLabel.binding)
        frame.setNumChangeBarsBinding(sizeBinding(changeBars))
        frame.setChangeBarColorBinding(propertyBinding(changeBars) { it.first })
        frame.setChangeBarSizeBinding(propertyBinding(changeBars) { it.second })
        frame.setChangeBarLabelBinding(changeLabel.binding)
        frame.setChangeBarStartBinding(changeStart.binding)
        frame.setBorderColorBinding(borderColor.binding)
        frame.setHeaderBinding(header.binding)
        frame.setSize(1024, 512)
        compareRendering("HeatMapFrame", "EvenWide-1", frame)
        squares.setAll(
                sequenceOf(
                        24, 27, 8, 11, 9, 26, 10, 23, 16, 12, 3, 25, 14, 22, 15, 21, 13, 5, 4, 2, 1, 7, 18,
                        19, 6, 20, 17)
                        .map { results[it]!! }
                        .toList())
        seatBars.setAll(
                listOf(Pair(Color.RED, 2), Pair(Color(255, 128, 128), 4)))
        seatLabel.value = "2/6"
        changeBars.setAll(
                listOf(Pair(Color.RED, -4), Pair(Color(255, 128, 128), -8)))
        changeLabel.value = "-4/-12"
        changeStart.value = 18
        borderColor.value = Color.RED
        header.value = "LIBERAL HEAT MAP"
        compareRendering("HeatMapFrame", "EvenWide-2", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderEvenHigh() {
        val results = peiResults
        val squares = BindableList<Pair<Color, Color>>()
        squares.setAll(
                sequenceOf(
                        20, 6, 19, 18, 7, 1, 2, 4, 5, 13, 21, 15, 22, 14, 25, 3, 12, 16, 23, 10, 26, 9, 11,
                        8, 27, 24, 17)
                        .map { results[it]!! }
                        .toList())
        val seatBars = BindableList<Pair<Color, Int>>()
        seatBars.setAll(
                listOf(Pair(Color.BLUE, 8), Pair(Color(128, 128, 255), 5)))
        val seatLabel = BindableWrapper("8/13")
        val changeBars = BindableList<Pair<Color, Int>>()
        changeBars.setAll(
                listOf(Pair(Color.BLUE, 3), Pair(Color(128, 128, 255), 2)))
        val changeLabel = BindableWrapper("+3/+5")
        val changeStart = BindableWrapper(8)
        val borderColor = BindableWrapper(Color.BLUE)
        val header = BindableWrapper<String?>("PROGRESSIVE CONSERVATIVE HEAT MAP")
        val frame = HeatMapFrame()
        frame.setNumRowsBinding(fixedBinding(9))
        frame.setNumSquaresBinding(sizeBinding(squares))
        frame.setSquareBordersBinding(propertyBinding(squares) { it.first })
        frame.setSquareFillBinding(propertyBinding(squares) { it.second })
        frame.setNumSeatBarsBinding(sizeBinding(seatBars))
        frame.setSeatBarColorBinding(propertyBinding(seatBars) { it.first })
        frame.setSeatBarSizeBinding(propertyBinding(seatBars) { it.second })
        frame.setSeatBarLabelBinding(seatLabel.binding)
        frame.setNumChangeBarsBinding(sizeBinding(changeBars))
        frame.setChangeBarColorBinding(propertyBinding(changeBars) { it.first })
        frame.setChangeBarSizeBinding(propertyBinding(changeBars) { it.second })
        frame.setChangeBarLabelBinding(changeLabel.binding)
        frame.setChangeBarStartBinding(changeStart.binding)
        frame.setBorderColorBinding(borderColor.binding)
        frame.setHeaderBinding(header.binding)
        frame.setSize(1024, 512)
        compareRendering("HeatMapFrame", "EvenHigh-1", frame)
        squares.setAll(
                sequenceOf(
                        24, 27, 8, 11, 9, 26, 10, 23, 16, 12, 3, 25, 14, 22, 15, 21, 13, 5, 4, 2, 1, 7, 18,
                        19, 6, 20, 17)
                        .map { results[it]!! }
                        .toList())
        seatBars.setAll(
                listOf(Pair(Color.RED, 2), Pair(Color(255, 128, 128), 4)))
        seatLabel.value = "2/6"
        changeBars.setAll(
                listOf(Pair(Color.RED, -4), Pair(Color(255, 128, 128), -8)))
        changeLabel.value = "-4/-12"
        changeStart.value = 18
        borderColor.value = Color.RED
        header.value = "LIBERAL HEAT MAP"
        compareRendering("HeatMapFrame", "EvenHigh-2", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderUneven() {
        val results = peiResults
        val squares = BindableList<Pair<Color, Color>>()
        squares.setAll(
                sequenceOf(
                        20, 6, 19, 18, 7, 1, 2, 4, 5, 13, 21, 15, 22, 14, 25, 3, 12, 16, 23, 10, 26, 9, 11,
                        8, 27, 24, 17)
                        .map { results[it]!! }
                        .toList())
        val seatBars = BindableList<Pair<Color, Int>>()
        seatBars.setAll(
                listOf(Pair(Color.BLUE, 8), Pair(Color(128, 128, 255), 5)))
        val seatLabel = BindableWrapper("8/13")
        val changeBars = BindableList<Pair<Color, Int>>()
        changeBars.setAll(
                listOf(Pair(Color.BLUE, 3), Pair(Color(128, 128, 255), 2)))
        val changeLabel = BindableWrapper("+3/+5")
        val changeStart = BindableWrapper(8)
        val borderColor = BindableWrapper(Color.BLUE)
        val header = BindableWrapper<String?>("PROGRESSIVE CONSERVATIVE HEAT MAP")
        val frame = HeatMapFrame()
        frame.setNumRowsBinding(fixedBinding(5))
        frame.setNumSquaresBinding(sizeBinding(squares))
        frame.setSquareBordersBinding(propertyBinding(squares) { it.first })
        frame.setSquareFillBinding(propertyBinding(squares) { it.second })
        frame.setNumSeatBarsBinding(sizeBinding(seatBars))
        frame.setSeatBarColorBinding(propertyBinding(seatBars) { it.first })
        frame.setSeatBarSizeBinding(propertyBinding(seatBars) { it.second })
        frame.setSeatBarLabelBinding(seatLabel.binding)
        frame.setNumChangeBarsBinding(sizeBinding(changeBars))
        frame.setChangeBarColorBinding(propertyBinding(changeBars) { it.first })
        frame.setChangeBarSizeBinding(propertyBinding(changeBars) { it.second })
        frame.setChangeBarLabelBinding(changeLabel.binding)
        frame.setChangeBarStartBinding(changeStart.binding)
        frame.setBorderColorBinding(borderColor.binding)
        frame.setHeaderBinding(header.binding)
        frame.setSize(1024, 512)
        compareRendering("HeatMapFrame", "Uneven-1", frame)
        squares.setAll(
                sequenceOf(
                        24, 27, 8, 11, 9, 26, 10, 23, 16, 12, 3, 25, 14, 22, 15, 21, 13, 5, 4, 2, 1, 7, 18,
                        19, 6, 20, 17)
                        .map { results[it]!! }
                        .toList())
        seatBars.setAll(
                listOf(Pair(Color.RED, 2), Pair(Color(255, 128, 128), 4)))
        seatLabel.value = "2/6"
        changeBars.setAll(
                listOf(Pair(Color.RED, -4), Pair(Color(255, 128, 128), -8)))
        changeLabel.value = "-4/-12"
        changeStart.value = 18
        borderColor.value = Color.RED
        header.value = "LIBERAL HEAT MAP"
        compareRendering("HeatMapFrame", "Uneven-2", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderChangeReversals() {
        val results = peiResults
        val squares = BindableList<Pair<Color, Color>>()
        squares.setAll(
                sequenceOf(
                        20, 6, 19, 18, 7, 1, 2, 4, 5, 13, 21, 15, 22, 14, 25, 3, 12, 16, 23, 10, 26, 9, 11,
                        8, 27, 24, 17)
                        .map { results[it]!! }
                        .toList())
        val seatBars = BindableList<Pair<Color, Int>>()
        seatBars.setAll(
                listOf(Pair(Color.BLUE, 2), Pair(Color(128, 128, 255), 2)))
        val seatLabel = BindableWrapper("2/4")
        val changeBars = BindableList<Pair<Color, Int>>()
        changeBars.setAll(
                listOf(Pair(Color.BLUE, 3), Pair(Color(128, 128, 255), 2)))
        val changeLabel = BindableWrapper("+3/+5")
        val changeStart = BindableWrapper(8)
        val borderColor = BindableWrapper(Color.BLUE)
        val header = BindableWrapper<String?>("PROGRESSIVE CONSERVATIVE HEAT MAP")
        val frame = HeatMapFrame()
        frame.setNumRowsBinding(fixedBinding(5))
        frame.setNumSquaresBinding(sizeBinding(squares))
        frame.setSquareBordersBinding(propertyBinding(squares) { it.first })
        frame.setSquareFillBinding(propertyBinding(squares) { it.second })
        frame.setNumSeatBarsBinding(sizeBinding(seatBars))
        frame.setSeatBarColorBinding(propertyBinding(seatBars) { it.first })
        frame.setSeatBarSizeBinding(propertyBinding(seatBars) { it.second })
        frame.setSeatBarLabelBinding(seatLabel.binding)
        frame.setNumChangeBarsBinding(sizeBinding(changeBars))
        frame.setChangeBarColorBinding(propertyBinding(changeBars) { it.first })
        frame.setChangeBarSizeBinding(propertyBinding(changeBars) { it.second })
        frame.setChangeBarLabelBinding(changeLabel.binding)
        frame.setChangeBarStartBinding(changeStart.binding)
        frame.setBorderColorBinding(borderColor.binding)
        frame.setHeaderBinding(header.binding)
        frame.setSize(1024, 512)
        changeBars.setAll(
                listOf(Pair(Color.BLUE, 2), Pair(Color(128, 128, 255), -1)))
        changeLabel.value = "+2/+1"
        compareRendering("HeatMapFrame", "ChangeReversals-1", frame)
        changeBars.setAll(
                listOf(Pair(Color.BLUE, -2), Pair(Color(128, 128, 255), 1)))
        changeLabel.value = "-2/-1"
        compareRendering("HeatMapFrame", "ChangeReversals-2", frame)
        changeBars.setAll(
                listOf(Pair(Color.BLUE, 1), Pair(Color(128, 128, 255), -2)))
        changeLabel.value = "+1/-1"
        compareRendering("HeatMapFrame", "ChangeReversals-3", frame)
        changeBars.setAll(
                listOf(Pair(Color.BLUE, -1), Pair(Color(128, 128, 255), 2)))
        changeLabel.value = "-1/+1"
        compareRendering("HeatMapFrame", "ChangeReversals-4", frame)
    }

    private val peiResults: Map<Int, Pair<Color, Color>>
        get() = mapOf(
                1 to Pair(Color.BLUE, Color.BLUE),
                2 to Pair(Color.BLUE, Color.BLUE),
                3 to Pair(Color.RED, Color.BLUE),
                4 to Pair(Color.BLUE, Color.BLUE),
                5 to Pair(Color.RED, DARK_GREEN),
                6 to Pair(Color.BLUE, Color.BLUE),
                7 to Pair(Color.BLUE, Color.BLUE),
                8 to Pair(Color.RED, Color.BLUE),
                9 to Pair(Color.RED, Color.BLUE),
                10 to Pair(Color.RED, Color.RED),
                11 to Pair(Color.RED, DARK_GREEN),
                12 to Pair(Color.RED, DARK_GREEN),
                13 to Pair(Color.RED, DARK_GREEN),
                14 to Pair(Color.RED, Color.RED),
                15 to Pair(Color.RED, Color.BLUE),
                16 to Pair(Color.RED, Color.RED),
                17 to Pair(DARK_GREEN, DARK_GREEN),
                18 to Pair(Color.BLUE, Color.BLUE),
                19 to Pair(Color.BLUE, Color.BLUE),
                20 to Pair(Color.BLUE, Color.BLUE),
                21 to Pair(Color.RED, DARK_GREEN),
                22 to Pair(Color.RED, DARK_GREEN),
                23 to Pair(Color.RED, DARK_GREEN),
                24 to Pair(Color.RED, Color.RED),
                25 to Pair(Color.RED, Color.RED),
                26 to Pair(Color.RED, Color.BLUE),
                27 to Pair(Color.RED, Color.RED))

    companion object {
        private val DARK_GREEN = Color.GREEN.darker()
    }
}
