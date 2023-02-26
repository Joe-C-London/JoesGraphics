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

class LowerThirdHeadlineAndSummaryTest {
    @Test
    fun testHeadline() {
        val lowerThird = LowerThirdHeadlineAndSummary(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryEntry(Color.RED, "LIB", "2"),
                SummaryEntry(Color.BLUE, "CON", "1"),
            )
                .asOneTimePublisher(),
        )
        assertEquals("POLLS CLOSE ACROSS CENTRAL CANADA", lowerThird.headline)
    }

    @Test
    fun testSubhead() {
        val lowerThird = LowerThirdHeadlineAndSummary(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryEntry(Color.RED, "LIB", "2"),
                SummaryEntry(Color.BLUE, "CON", "1"),
            )
                .asOneTimePublisher(),
        )
        assertEquals("Polls open for 30 minutes on west coast", lowerThird.subhead)
    }

    @Test
    fun testSummaryPanel() {
        val lowerThird = LowerThirdHeadlineAndSummary(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryEntry(Color.RED, "LIB", "2"),
                SummaryEntry(Color.BLUE, "CON", "1"),
            )
                .asOneTimePublisher(),
        )
        assertEquals(2, lowerThird.numSummaryEntries)
        assertEquals(Color.RED, lowerThird.getEntryColor(0))
        assertEquals(Color.BLUE, lowerThird.getEntryColor(1))
        assertEquals("LIB", lowerThird.getEntryLabel(0))
        assertEquals("CON", lowerThird.getEntryLabel(1))
        assertEquals("2", lowerThird.getEntryValue(0))
        assertEquals("1", lowerThird.getEntryValue(1))
    }

    @Test
    fun testRenderHeadlineAndSummary() {
        val lowerThird = LowerThirdHeadlineAndSummary(
            leftImagePublisher =
            createImage(
                LowerThirdHeadlineAndSummaryTest::class.java
                    .classLoader
                    .getResource("com/joecollins/graphics/lowerthird-left.png"),
            )
                .asOneTimePublisher(),
            placePublisher = ("OTTAWA" to ZoneId.of("Canada/Eastern")).asOneTimePublisher(),
            headlineBinding = "CENTRAL CANADA POLLS CLOSE".asOneTimePublisher(),
            subheadBinding = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryEntriesBinding =
            listOf(
                SummaryEntry(Color.RED, "LIB", "2"),
                SummaryEntry(Color.BLUE, "CON", "1"),
            )
                .asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()),
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummary", lowerThird)
    }
}
