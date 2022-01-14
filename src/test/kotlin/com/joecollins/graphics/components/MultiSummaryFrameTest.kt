package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.Throws

class MultiSummaryFrameTest {
    @Test
    fun testEntries() {
        val frame = MultiSummaryFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            rowsPublisher =
            listOf(
                MultiSummaryFrame.Row(
                    "ATLANTIC",
                    listOf(
                        Pair(Color.RED, "26"),
                        Pair(Color.BLUE, "4"),
                        Pair(Color.ORANGE, "1"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "1"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "QU\u00c9BEC",
                    listOf(
                        Pair(Color.RED, "35"),
                        Pair(Color.BLUE, "10"),
                        Pair(Color.ORANGE, "1"),
                        Pair(Color.CYAN.darker(), "32"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "ONTARIO",
                    listOf(
                        Pair(Color.RED, "79"),
                        Pair(Color.BLUE, "36"),
                        Pair(Color.ORANGE, "6"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "WESTERN CANADA",
                    listOf(
                        Pair(Color.RED, "15"),
                        Pair(Color.BLUE, "71"),
                        Pair(Color.ORANGE, "15"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "2"),
                        Pair(Color.GRAY, "1")
                    )
                ),
                MultiSummaryFrame.Row(
                    "THE NORTH",
                    listOf(
                        Pair(Color.RED, "2"),
                        Pair(Color.BLUE, "0"),
                        Pair(Color.ORANGE, "1"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                )
            )
                .asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numRows }, IsEqual(5))
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
        val frame = MultiSummaryFrame(
            headerPublisher = "SEATS BY REGION".asOneTimePublisher(),
            rowsPublisher = listOf(
                MultiSummaryFrame.Row(
                    "ATLANTIC",
                    listOf(
                        Pair(Color.RED, "26"),
                        Pair(Color.BLUE, "4"),
                        Pair(Color.ORANGE, "1"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "1"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "QU\u00c9BEC",
                    listOf(
                        Pair(Color.RED, "35"),
                        Pair(Color.BLUE, "10"),
                        Pair(Color.ORANGE, "1"),
                        Pair(Color.CYAN.darker(), "32"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "ONTARIO",
                    listOf(
                        Pair(Color.RED, "79"),
                        Pair(Color.BLUE, "36"),
                        Pair(Color.ORANGE, "6"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "WESTERN CANADA",
                    listOf(
                        Pair(Color.RED, "15"),
                        Pair(Color.BLUE, "71"),
                        Pair(Color.ORANGE, "15"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "2"),
                        Pair(Color.GRAY, "1")
                    )
                ),
                MultiSummaryFrame.Row(
                    "THE NORTH",
                    listOf(
                        Pair(Color.RED, "2"),
                        Pair(Color.BLUE, "0"),
                        Pair(Color.ORANGE, "1"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                )
            ).asOneTimePublisher()
        )
        frame.setSize(512, 256)
        compareRendering("MultiSummaryFrame", "Basic", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderOverflowSummary() {
        val frame = MultiSummaryFrame(
            headerPublisher = "SEATS BY PROVINCE".asOneTimePublisher(),
            rowsPublisher = listOf(
                MultiSummaryFrame.Row(
                    "NEWFOUNDLAND & LABRADOR",
                    listOf(
                        Pair(Color.RED, "6"),
                        Pair(Color.BLUE, "0"),
                        Pair(Color.ORANGE, "1"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "NOVA SCOTIA",
                    listOf(
                        Pair(Color.RED, "10"),
                        Pair(Color.BLUE, "1"),
                        Pair(Color.ORANGE, "0"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "PRINCE EDWARD ISLAND",
                    listOf(
                        Pair(Color.RED, "4"),
                        Pair(Color.BLUE, "0"),
                        Pair(Color.ORANGE, "0"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "NEW BRUNSWICK",
                    listOf(
                        Pair(Color.RED, "6"),
                        Pair(Color.BLUE, "3"),
                        Pair(Color.ORANGE, "0"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "1"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "QU\u00c9BEC",
                    listOf(
                        Pair(Color.RED, "35"),
                        Pair(Color.BLUE, "10"),
                        Pair(Color.ORANGE, "1"),
                        Pair(Color.CYAN.darker(), "32"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "ONTARIO",
                    listOf(
                        Pair(Color.RED, "79"),
                        Pair(Color.BLUE, "36"),
                        Pair(Color.ORANGE, "6"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "MANITOBA",
                    listOf(
                        Pair(Color.RED, "4"),
                        Pair(Color.BLUE, "7"),
                        Pair(Color.ORANGE, "3"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "SASKATCHEWAN",
                    listOf(
                        Pair(Color.RED, "0"),
                        Pair(Color.BLUE, "14"),
                        Pair(Color.ORANGE, "0"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "ALBERTA",
                    listOf(
                        Pair(Color.RED, "0"),
                        Pair(Color.BLUE, "33"),
                        Pair(Color.ORANGE, "1"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "BRITISH COLUMBIA",
                    listOf(
                        Pair(Color.RED, "11"),
                        Pair(Color.BLUE, "17"),
                        Pair(Color.ORANGE, "11"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "2"),
                        Pair(Color.GRAY, "1")
                    )
                ),
                MultiSummaryFrame.Row(
                    "YUKON",
                    listOf(
                        Pair(Color.RED, "1"),
                        Pair(Color.BLUE, "0"),
                        Pair(Color.ORANGE, "0"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "NORTHWEST TERRITORIES",
                    listOf(
                        Pair(Color.RED, "1"),
                        Pair(Color.BLUE, "0"),
                        Pair(Color.ORANGE, "0"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                ),
                MultiSummaryFrame.Row(
                    "NUNAVUT",
                    listOf(
                        Pair(Color.RED, "0"),
                        Pair(Color.BLUE, "0"),
                        Pair(Color.ORANGE, "1"),
                        Pair(Color.CYAN.darker(), "0"),
                        Pair(Color.GREEN.darker(), "0"),
                        Pair(Color.GRAY, "0")
                    )
                )
            ).asOneTimePublisher()
        )
        frame.setSize(512, 256)
        compareRendering("MultiSummaryFrame", "Overflow", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderDifferentColCounts() {
        val frame = MultiSummaryFrame(
            headerPublisher = "SENATE SEATS".asOneTimePublisher(),
            rowsPublisher = listOf(
                MultiSummaryFrame.Row(
                    "NEW SOUTH WALES",
                    listOf(
                        Pair(Color.BLUE, "LIB"),
                        Pair(Color.RED, "ALP"),
                        Pair(Color.BLUE, "LIB"),
                        Pair(Color.RED, "ALP"),
                        Pair(Color.GREEN.darker().darker(), "NAT"),
                        Pair(Color.GREEN.darker(), "GRN")
                    )
                ),
                MultiSummaryFrame.Row(
                    "VICTORIA",
                    listOf(
                        Pair(Color.BLUE, "LIB"),
                        Pair(Color.RED, "ALP"),
                        Pair(Color.BLUE, "LIB"),
                        Pair(Color.RED, "ALP"),
                        Pair(Color.GREEN.darker(), "GRN"),
                        Pair(Color.BLUE, "LIB")
                    )
                ),
                MultiSummaryFrame.Row(
                    "QUEENSLAND",
                    listOf(
                        Pair(Color.BLUE, "LNP"),
                        Pair(Color.RED, "ALP"),
                        Pair(Color.BLUE, "LNP"),
                        Pair(Color.ORANGE, "ONP"),
                        Pair(Color.BLUE, "LNP"),
                        Pair(Color.GREEN.darker(), "GRN")
                    )
                ),
                MultiSummaryFrame.Row(
                    "WESTERN AUSTRALIA",
                    listOf(
                        Pair(Color.BLUE, "LIB"),
                        Pair(Color.RED, "ALP"),
                        Pair(Color.BLUE, "LIB"),
                        Pair(Color.BLUE, "LIB"),
                        Pair(Color.RED, "ALP"),
                        Pair(Color.GREEN.darker(), "GRN")
                    )
                ),
                MultiSummaryFrame.Row(
                    "SOUTH AUSTRALIA",
                    listOf(
                        Pair(Color.BLUE, "LIB"),
                        Pair(Color.RED, "ALP"),
                        Pair(Color.BLUE, "LIB"),
                        Pair(Color.RED, "ALP"),
                        Pair(Color.GREEN.darker(), "GRN"),
                        Pair(Color.BLUE, "LIB")
                    )
                ),
                MultiSummaryFrame.Row(
                    "TASMANIA",
                    listOf(
                        Pair(Color.BLUE, "LIB"),
                        Pair(Color.RED, "ALP"),
                        Pair(Color.BLUE, "LIB"),
                        Pair(Color.GREEN.darker(), "GRN"),
                        Pair(Color.RED, "ALP"),
                        Pair(Color.YELLOW, "LAMB")
                    )
                ),
                MultiSummaryFrame.Row("ACT", listOf(Pair(Color.RED, "ALP"), Pair(Color.BLUE, "LIB"))),
                MultiSummaryFrame.Row(
                    "NORTHERN TERRITORY", listOf(Pair(Color.RED, "ALP"), Pair(Color.ORANGE, "CLP"))
                )
            ).asOneTimePublisher()
        )
        frame.setSize(512, 256)
        compareRendering("MultiSummaryFrame", "DiffColCounts", frame)
    }
}
