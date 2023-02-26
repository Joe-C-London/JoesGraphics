package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.components.lowerthird.LowerThird.Companion.createImage
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class LowerThirdHeadlineAndBottomSummaryTest {
    @Test
    fun testHeadline() {
        val lowerThird = LowerThirdHeadlineAndBottomSummary(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "SEATS WON".asOneTimePublisher(),
            summaryFooterPublisher = "170 FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryEntry(Color.RED, "LIB", "2"),
                SummaryEntry(Color.BLUE, "CON", "1"),
                SummaryEntry(Color.CYAN.darker(), "BQ", "0"),
                SummaryEntry(Color.GREEN.darker(), "GRN", "0"),
                SummaryEntry(Color.ORANGE, "NDP", "0"),
                SummaryEntry(Color.WHITE, "?", "335"),
            )
                .asOneTimePublisher(),
        )
        assertEquals("POLLS CLOSE ACROSS CENTRAL CANADA", lowerThird.headline)
    }

    @Test
    fun testSubhead() {
        val lowerThird = LowerThirdHeadlineAndBottomSummary(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "SEATS WON".asOneTimePublisher(),
            summaryFooterPublisher = "170 FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryEntry(Color.RED, "LIB", "2"),
                SummaryEntry(Color.BLUE, "CON", "1"),
                SummaryEntry(Color.CYAN.darker(), "BQ", "0"),
                SummaryEntry(Color.GREEN.darker(), "GRN", "0"),
                SummaryEntry(Color.ORANGE, "NDP", "0"),
                SummaryEntry(Color.WHITE, "?", "335"),
            )
                .asOneTimePublisher(),
        )
        assertEquals("Polls open for 30 minutes on west coast", lowerThird.subhead)
    }

    @Test
    fun testSummaryPanel() {
        val lowerThird = LowerThirdHeadlineAndBottomSummary(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "SEATS WON".asOneTimePublisher(),
            summaryFooterPublisher = "170 FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryEntry(Color.RED, "LIB", "2"),
                SummaryEntry(Color.BLUE, "CON", "1"),
                SummaryEntry(Color.CYAN.darker(), "BQ", "0"),
                SummaryEntry(Color.GREEN.darker(), "GRN", "0"),
                SummaryEntry(Color.ORANGE, "NDP", "0"),
                SummaryEntry(Color.WHITE, "?", "335"),
            )
                .asOneTimePublisher(),
        )
        assertEquals(6, lowerThird.numSummaryEntries)
        assertEquals(Color.RED, lowerThird.getEntryColor(0))
        assertEquals(Color.BLUE, lowerThird.getEntryColor(1))
        assertEquals(Color.WHITE, lowerThird.getEntryColor(5))
        assertEquals("LIB", lowerThird.getEntryLabel(0))
        assertEquals("CON", lowerThird.getEntryLabel(1))
        assertEquals("?", lowerThird.getEntryLabel(5))
        assertEquals("2", lowerThird.getEntryValue(0))
        assertEquals("1", lowerThird.getEntryValue(1))
        assertEquals("335", lowerThird.getEntryValue(5))
        assertEquals("SEATS WON", lowerThird.summaryHeader)
        assertEquals("170 FOR MAJORITY", lowerThird.summaryFooter)
    }

    @Test
    fun testRenderHeadlineAndSummary() {
        val lowerThird = LowerThirdHeadlineAndBottomSummary(
            leftImagePublisher =
            createImage(
                LowerThirdHeadlineAndBottomSummaryTest::class.java
                    .classLoader
                    .getResource("com/joecollins/graphics/lowerthird-left.png"),
            )
                .asOneTimePublisher(),
            placePublisher = ("OTTAWA" to ZoneId.of("Canada/Eastern")).asOneTimePublisher(),
            headlinePublisher = "CENTRAL CANADA POLLS CLOSE".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "SEATS WON".asOneTimePublisher(),
            summaryFooterPublisher = "170 FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryEntry(Color.RED, "LIB", "2"),
                SummaryEntry(Color.BLUE, "CON", "1"),
                SummaryEntry(Color.CYAN.darker(), "BQ", "0"),
                SummaryEntry(Color.GREEN.darker(), "GRN", "0"),
                SummaryEntry(Color.ORANGE, "NDP", "0"),
                SummaryEntry(Color.WHITE, "?", "335"),
            )
                .asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()),
        )
        lowerThird.setSize(1024, 70)
        compareRendering("LowerThird", "HeadlineAndBottomSummaryHeader", lowerThird)
    }
}
