package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color

class RegionSummaryFrameTest {
    @Test
    fun testEntriesDifferentColors() {
        val frame = RegionSummaryFrame(
            headerPublisher = "".asOneTimePublisher(),
            sectionsPublisher =
            listOf(
                RegionSummaryFrame.Section(
                    "ELECTORAL VOTES",
                    listOf(
                        Pair(Color.BLUE, "306"),
                        Pair(Color.BLUE, "<< 74"),
                        Pair(Color.RED, "232"),
                    ),
                ),
                RegionSummaryFrame.Section(
                    "POPULAR VOTE",
                    listOf(
                        Pair(Color.BLUE, "51.1%"),
                        Pair(Color.BLUE, "<< 1.0%"),
                        Pair(Color.RED, "47.2%"),
                    ),
                ),
            )
                .asOneTimePublisher(),
        )
        assertEquals(2, frame.getNumSections())
        assertEquals(Color.BLACK, frame.getSummaryColor())
        assertEquals("ELECTORAL VOTES", frame.getSectionHeader(0))
        assertEquals(Color.BLUE, frame.getValueColor(1, 0))
        assertEquals("<< 1.0%", frame.getValue(1, 1))
    }

    @Test
    fun testEntriesSameColor() {
        val frame = RegionSummaryFrame(
            headerPublisher = "".asOneTimePublisher(),
            summaryColorPublisher = Color.BLUE.asOneTimePublisher(),
            sectionsPublisher =
            listOf(
                RegionSummaryFrame.SectionWithoutColor("ELECTORAL VOTES", listOf("306", "+74")),
                RegionSummaryFrame.SectionWithoutColor("POPULAR VOTE", listOf("51.1%", "+1.0%")),
            )
                .asOneTimePublisher(),
        )
        assertEquals(2, frame.getNumSections())
        assertEquals(Color.BLUE, frame.getSummaryColor())
        assertEquals("ELECTORAL VOTES", frame.getSectionHeader(0))
        assertEquals(Color.BLUE, frame.getValueColor(1, 0))
        assertEquals("+1.0%", frame.getValue(1, 1))
    }

    @Test
    fun testRenderDifferentColors() {
        val frame = RegionSummaryFrame(
            headerPublisher = "UNITED STATES".asOneTimePublisher(),
            sectionsPublisher =
            listOf(
                RegionSummaryFrame.Section(
                    "ELECTORAL VOTES",
                    listOf(
                        Pair(Color.BLUE, "306"),
                        Pair(Color.BLUE, "<< 74"),
                        Pair(Color.RED, "232"),
                    ),
                ),
                RegionSummaryFrame.Section(
                    "POPULAR VOTE",
                    listOf(
                        Pair(Color.BLUE, "51.1%"),
                        Pair(Color.BLUE, "<< 1.0%"),
                        Pair(Color.RED, "47.2%"),
                    ),
                ),
            )
                .asOneTimePublisher(),
        )
        frame.setSize(500, 500)
        compareRendering("RegionSummaryFrame", "DifferentColors", frame)
    }

    @Test
    fun testRenderSameColor() {
        val frame = RegionSummaryFrame(
            headerPublisher = "USA".asOneTimePublisher(),
            summaryColorPublisher = Color.BLUE.asOneTimePublisher(),
            sectionsPublisher =
            listOf(
                RegionSummaryFrame.SectionWithoutColor("ELECTORAL VOTES", listOf("306", "+74")),
                RegionSummaryFrame.SectionWithoutColor("POPULAR VOTE", listOf("51.1%", "+1.0%")),
            )
                .asOneTimePublisher(),
        )
        frame.setSize(125, 125)
        compareRendering("RegionSummaryFrame", "SameColor", frame)
    }

    @Test
    fun testRenderSingleLevel() {
        val frame = RegionSummaryFrame(
            headerPublisher = "CANADA".asOneTimePublisher(),
            summaryColorPublisher = Color.RED.asOneTimePublisher(),
            sectionsPublisher =
            listOf(
                RegionSummaryFrame.SectionWithoutColor("SEATS", listOf("157", "-20")),
            )
                .asOneTimePublisher(),
        )
        frame.setSize(125, 125)
        compareRendering("RegionSummaryFrame", "SingleEntry", frame)
    }
}
