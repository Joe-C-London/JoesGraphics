package com.joecollins.graphics.components

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.IndexedBinding.Companion.listBinding
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.io.IOException
import kotlin.Throws
import org.junit.Assert
import org.junit.Test

class MultiSummaryFrameTest {
    @Test
    fun testEntries() {
        val frame = MultiSummaryFrame()
        frame.setNumRowsBinding(fixedBinding(5))
        frame.setRowHeaderBinding(
                listBinding(
                        "ATLANTIC", "QU\u00c9BEC", "ONTARIO", "WESTERN CANADA", "THE NORTH"))
        frame.setValuesBinding(
                listBinding(
                        listOf(
                                Pair(Color.RED, "26"),
                                Pair(Color.BLUE, "4"),
                                Pair(Color.ORANGE, "1"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "1"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "35"),
                                Pair(Color.BLUE, "10"),
                                Pair(Color.ORANGE, "1"),
                                Pair(Color.CYAN.darker(), "32"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "79"),
                                Pair(Color.BLUE, "36"),
                                Pair(Color.ORANGE, "6"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "15"),
                                Pair(Color.BLUE, "71"),
                                Pair(Color.ORANGE, "15"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "2"),
                                Pair(Color.GRAY, "1")),
                        listOf(
                                Pair(Color.RED, "2"),
                                Pair(Color.BLUE, "0"),
                                Pair(Color.ORANGE, "1"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0"))))
        Assert.assertEquals(5, frame.numRows.toLong())
        Assert.assertEquals("ATLANTIC", frame.getRowHeader(0))
        Assert.assertEquals("QU\u00c9BEC", frame.getRowHeader(1))
        Assert.assertEquals("ONTARIO", frame.getRowHeader(2))
        Assert.assertEquals("WESTERN CANADA", frame.getRowHeader(3))
        Assert.assertEquals("THE NORTH", frame.getRowHeader(4))
        Assert.assertEquals(6, frame.getNumValues(0).toLong())
        Assert.assertEquals(6, frame.getNumValues(1).toLong())
        Assert.assertEquals(6, frame.getNumValues(2).toLong())
        Assert.assertEquals(6, frame.getNumValues(3).toLong())
        Assert.assertEquals(6, frame.getNumValues(4).toLong())
        Assert.assertEquals(Color.RED, frame.getColor(0, 0))
        Assert.assertEquals(Color.BLUE, frame.getColor(1, 1))
        Assert.assertEquals(Color.ORANGE, frame.getColor(2, 2))
        Assert.assertEquals(Color.CYAN.darker(), frame.getColor(3, 3))
        Assert.assertEquals(Color.GREEN.darker(), frame.getColor(4, 4))
        Assert.assertEquals("26", frame.getValue(0, 0))
        Assert.assertEquals("10", frame.getValue(1, 1))
        Assert.assertEquals("6", frame.getValue(2, 2))
        Assert.assertEquals("0", frame.getValue(3, 3))
        Assert.assertEquals("0", frame.getValue(4, 4))
    }

    @Test
    @Throws(IOException::class)
    fun testRenderBasicSummary() {
        val frame = MultiSummaryFrame()
        frame.setHeaderBinding(fixedBinding("SEATS BY REGION"))
        frame.setNumRowsBinding(fixedBinding(5))
        frame.setRowHeaderBinding(
                listBinding(
                        "ATLANTIC", "QU\u00c9BEC", "ONTARIO", "WESTERN CANADA", "THE NORTH"))
        frame.setValuesBinding(
                listBinding(
                        listOf(
                                Pair(Color.RED, "26"),
                                Pair(Color.BLUE, "4"),
                                Pair(Color.ORANGE, "1"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "1"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "35"),
                                Pair(Color.BLUE, "10"),
                                Pair(Color.ORANGE, "1"),
                                Pair(Color.CYAN.darker(), "32"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "79"),
                                Pair(Color.BLUE, "36"),
                                Pair(Color.ORANGE, "6"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "15"),
                                Pair(Color.BLUE, "71"),
                                Pair(Color.ORANGE, "15"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "2"),
                                Pair(Color.GRAY, "1")),
                        listOf(
                                Pair(Color.RED, "2"),
                                Pair(Color.BLUE, "0"),
                                Pair(Color.ORANGE, "1"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0"))))
        frame.setSize(512, 256)
        compareRendering("MultiSummaryFrame", "Basic", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderOverflowSummary() {
        val frame = MultiSummaryFrame()
        frame.setHeaderBinding(fixedBinding("SEATS BY PROVINCE"))
        frame.setNumRowsBinding(fixedBinding(13))
        frame.setRowHeaderBinding(
                listBinding(
                        "NEWFOUNDLAND & LABRADOR",
                        "NOVA SCOTIA",
                        "PRINCE EDWARD ISLAND",
                        "NEW BRUNSWICK",
                        "QU\u00c9BEC",
                        "ONTARIO",
                        "MANITOBA",
                        "SASKATCHEWAN",
                        "ALBERTA",
                        "BRITISH COLUMBIA",
                        "YUKON",
                        "NORTHWEST TERRITORIES",
                        "NUNAVUT"))
        frame.setValuesBinding(
                listBinding(
                        listOf(
                                Pair(Color.RED, "6"),
                                Pair(Color.BLUE, "0"),
                                Pair(Color.ORANGE, "1"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "10"),
                                Pair(Color.BLUE, "1"),
                                Pair(Color.ORANGE, "0"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "4"),
                                Pair(Color.BLUE, "0"),
                                Pair(Color.ORANGE, "0"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "6"),
                                Pair(Color.BLUE, "3"),
                                Pair(Color.ORANGE, "0"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "1"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "35"),
                                Pair(Color.BLUE, "10"),
                                Pair(Color.ORANGE, "1"),
                                Pair(Color.CYAN.darker(), "32"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "79"),
                                Pair(Color.BLUE, "36"),
                                Pair(Color.ORANGE, "6"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "4"),
                                Pair(Color.BLUE, "7"),
                                Pair(Color.ORANGE, "3"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "0"),
                                Pair(Color.BLUE, "14"),
                                Pair(Color.ORANGE, "0"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "0"),
                                Pair(Color.BLUE, "33"),
                                Pair(Color.ORANGE, "1"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "11"),
                                Pair(Color.BLUE, "17"),
                                Pair(Color.ORANGE, "11"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "2"),
                                Pair(Color.GRAY, "1")),
                        listOf(
                                Pair(Color.RED, "1"),
                                Pair(Color.BLUE, "0"),
                                Pair(Color.ORANGE, "0"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "1"),
                                Pair(Color.BLUE, "0"),
                                Pair(Color.ORANGE, "0"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0")),
                        listOf(
                                Pair(Color.RED, "0"),
                                Pair(Color.BLUE, "0"),
                                Pair(Color.ORANGE, "1"),
                                Pair(Color.CYAN.darker(), "0"),
                                Pair(Color.GREEN.darker(), "0"),
                                Pair(Color.GRAY, "0"))))
        frame.setSize(512, 256)
        compareRendering("MultiSummaryFrame", "Overflow", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderDifferentColCounts() {
        val frame = MultiSummaryFrame()
        frame.setHeaderBinding(fixedBinding("SENATE SEATS"))
        frame.setNumRowsBinding(fixedBinding(8))
        frame.setRowHeaderBinding(
                listBinding(
                        "NEW SOUTH WALES",
                        "VICTORIA",
                        "QUEENSLAND",
                        "WESTERN AUSTRALIA",
                        "SOUTH AUSTRALIA",
                        "TASMANIA",
                        "ACT",
                        "NORTHERN TERRITORY"))
        frame.setValuesBinding(
                listBinding(
                        listOf(
                                Pair(Color.BLUE, "LIB"),
                                Pair(Color.RED, "ALP"),
                                Pair(Color.BLUE, "LIB"),
                                Pair(Color.RED, "ALP"),
                                Pair(Color.GREEN.darker().darker(), "NAT"),
                                Pair(Color.GREEN.darker(), "GRN")),
                        listOf(
                                Pair(Color.BLUE, "LIB"),
                                Pair(Color.RED, "ALP"),
                                Pair(Color.BLUE, "LIB"),
                                Pair(Color.RED, "ALP"),
                                Pair(Color.GREEN.darker(), "GRN"),
                                Pair(Color.BLUE, "LIB")),
                        listOf(
                                Pair(Color.BLUE, "LNP"),
                                Pair(Color.RED, "ALP"),
                                Pair(Color.BLUE, "LNP"),
                                Pair(Color.ORANGE, "ONP"),
                                Pair(Color.BLUE, "LNP"),
                                Pair(Color.GREEN.darker(), "GRN")),
                        listOf(
                                Pair(Color.BLUE, "LIB"),
                                Pair(Color.RED, "ALP"),
                                Pair(Color.BLUE, "LIB"),
                                Pair(Color.BLUE, "LIB"),
                                Pair(Color.RED, "ALP"),
                                Pair(Color.GREEN.darker(), "GRN")),
                        listOf(
                                Pair(Color.BLUE, "LIB"),
                                Pair(Color.RED, "ALP"),
                                Pair(Color.BLUE, "LIB"),
                                Pair(Color.RED, "ALP"),
                                Pair(Color.GREEN.darker(), "GRN"),
                                Pair(Color.BLUE, "LIB")),
                        listOf(
                                Pair(Color.BLUE, "LIB"),
                                Pair(Color.RED, "ALP"),
                                Pair(Color.BLUE, "LIB"),
                                Pair(Color.GREEN.darker(), "GRN"),
                                Pair(Color.RED, "ALP"),
                                Pair(Color.YELLOW, "LAMB")),
                        listOf(Pair(Color.RED, "ALP"), Pair(Color.BLUE, "LIB")),
                        listOf(Pair(Color.RED, "ALP"), Pair(Color.ORANGE, "CLP"))))
        frame.setSize(512, 256)
        compareRendering("MultiSummaryFrame", "DiffColCounts", frame)
    }
}
