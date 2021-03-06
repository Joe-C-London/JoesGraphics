package com.joecollins.graphics.components

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.IndexedBinding.Companion.listBinding
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.io.IOException
import kotlin.Throws
import org.apache.commons.lang3.tuple.ImmutablePair
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
                                ImmutablePair.of(Color.RED, "26"),
                                ImmutablePair.of(Color.BLUE, "4"),
                                ImmutablePair.of(Color.ORANGE, "1"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "1"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "35"),
                                ImmutablePair.of(Color.BLUE, "10"),
                                ImmutablePair.of(Color.ORANGE, "1"),
                                ImmutablePair.of(Color.CYAN.darker(), "32"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "79"),
                                ImmutablePair.of(Color.BLUE, "36"),
                                ImmutablePair.of(Color.ORANGE, "6"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "15"),
                                ImmutablePair.of(Color.BLUE, "71"),
                                ImmutablePair.of(Color.ORANGE, "15"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "2"),
                                ImmutablePair.of(Color.GRAY, "1")),
                        listOf(
                                ImmutablePair.of(Color.RED, "2"),
                                ImmutablePair.of(Color.BLUE, "0"),
                                ImmutablePair.of(Color.ORANGE, "1"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0"))))
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
                                ImmutablePair.of(Color.RED, "26"),
                                ImmutablePair.of(Color.BLUE, "4"),
                                ImmutablePair.of(Color.ORANGE, "1"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "1"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "35"),
                                ImmutablePair.of(Color.BLUE, "10"),
                                ImmutablePair.of(Color.ORANGE, "1"),
                                ImmutablePair.of(Color.CYAN.darker(), "32"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "79"),
                                ImmutablePair.of(Color.BLUE, "36"),
                                ImmutablePair.of(Color.ORANGE, "6"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "15"),
                                ImmutablePair.of(Color.BLUE, "71"),
                                ImmutablePair.of(Color.ORANGE, "15"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "2"),
                                ImmutablePair.of(Color.GRAY, "1")),
                        listOf(
                                ImmutablePair.of(Color.RED, "2"),
                                ImmutablePair.of(Color.BLUE, "0"),
                                ImmutablePair.of(Color.ORANGE, "1"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0"))))
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
                                ImmutablePair.of(Color.RED, "6"),
                                ImmutablePair.of(Color.BLUE, "0"),
                                ImmutablePair.of(Color.ORANGE, "1"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "10"),
                                ImmutablePair.of(Color.BLUE, "1"),
                                ImmutablePair.of(Color.ORANGE, "0"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "4"),
                                ImmutablePair.of(Color.BLUE, "0"),
                                ImmutablePair.of(Color.ORANGE, "0"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "6"),
                                ImmutablePair.of(Color.BLUE, "3"),
                                ImmutablePair.of(Color.ORANGE, "0"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "1"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "35"),
                                ImmutablePair.of(Color.BLUE, "10"),
                                ImmutablePair.of(Color.ORANGE, "1"),
                                ImmutablePair.of(Color.CYAN.darker(), "32"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "79"),
                                ImmutablePair.of(Color.BLUE, "36"),
                                ImmutablePair.of(Color.ORANGE, "6"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "4"),
                                ImmutablePair.of(Color.BLUE, "7"),
                                ImmutablePair.of(Color.ORANGE, "3"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "0"),
                                ImmutablePair.of(Color.BLUE, "14"),
                                ImmutablePair.of(Color.ORANGE, "0"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "0"),
                                ImmutablePair.of(Color.BLUE, "33"),
                                ImmutablePair.of(Color.ORANGE, "1"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "11"),
                                ImmutablePair.of(Color.BLUE, "17"),
                                ImmutablePair.of(Color.ORANGE, "11"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "2"),
                                ImmutablePair.of(Color.GRAY, "1")),
                        listOf(
                                ImmutablePair.of(Color.RED, "1"),
                                ImmutablePair.of(Color.BLUE, "0"),
                                ImmutablePair.of(Color.ORANGE, "0"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "1"),
                                ImmutablePair.of(Color.BLUE, "0"),
                                ImmutablePair.of(Color.ORANGE, "0"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0")),
                        listOf(
                                ImmutablePair.of(Color.RED, "0"),
                                ImmutablePair.of(Color.BLUE, "0"),
                                ImmutablePair.of(Color.ORANGE, "1"),
                                ImmutablePair.of(Color.CYAN.darker(), "0"),
                                ImmutablePair.of(Color.GREEN.darker(), "0"),
                                ImmutablePair.of(Color.GRAY, "0"))))
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
                                ImmutablePair.of(Color.BLUE, "LIB"),
                                ImmutablePair.of(Color.RED, "ALP"),
                                ImmutablePair.of(Color.BLUE, "LIB"),
                                ImmutablePair.of(Color.RED, "ALP"),
                                ImmutablePair.of(Color.GREEN.darker().darker(), "NAT"),
                                ImmutablePair.of(Color.GREEN.darker(), "GRN")),
                        listOf(
                                ImmutablePair.of(Color.BLUE, "LIB"),
                                ImmutablePair.of(Color.RED, "ALP"),
                                ImmutablePair.of(Color.BLUE, "LIB"),
                                ImmutablePair.of(Color.RED, "ALP"),
                                ImmutablePair.of(Color.GREEN.darker(), "GRN"),
                                ImmutablePair.of(Color.BLUE, "LIB")),
                        listOf(
                                ImmutablePair.of(Color.BLUE, "LNP"),
                                ImmutablePair.of(Color.RED, "ALP"),
                                ImmutablePair.of(Color.BLUE, "LNP"),
                                ImmutablePair.of(Color.ORANGE, "ONP"),
                                ImmutablePair.of(Color.BLUE, "LNP"),
                                ImmutablePair.of(Color.GREEN.darker(), "GRN")),
                        listOf(
                                ImmutablePair.of(Color.BLUE, "LIB"),
                                ImmutablePair.of(Color.RED, "ALP"),
                                ImmutablePair.of(Color.BLUE, "LIB"),
                                ImmutablePair.of(Color.BLUE, "LIB"),
                                ImmutablePair.of(Color.RED, "ALP"),
                                ImmutablePair.of(Color.GREEN.darker(), "GRN")),
                        listOf(
                                ImmutablePair.of(Color.BLUE, "LIB"),
                                ImmutablePair.of(Color.RED, "ALP"),
                                ImmutablePair.of(Color.BLUE, "LIB"),
                                ImmutablePair.of(Color.RED, "ALP"),
                                ImmutablePair.of(Color.GREEN.darker(), "GRN"),
                                ImmutablePair.of(Color.BLUE, "LIB")),
                        listOf(
                                ImmutablePair.of(Color.BLUE, "LIB"),
                                ImmutablePair.of(Color.RED, "ALP"),
                                ImmutablePair.of(Color.BLUE, "LIB"),
                                ImmutablePair.of(Color.GREEN.darker(), "GRN"),
                                ImmutablePair.of(Color.RED, "ALP"),
                                ImmutablePair.of(Color.YELLOW, "LAMB")),
                        listOf(ImmutablePair.of(Color.RED, "ALP"), ImmutablePair.of(Color.BLUE, "LIB")),
                        listOf(ImmutablePair.of(Color.RED, "ALP"), ImmutablePair.of(Color.ORANGE, "CLP"))))
        frame.setSize(512, 256)
        compareRendering("MultiSummaryFrame", "DiffColCounts", frame)
    }
}
