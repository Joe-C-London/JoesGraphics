package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.components.lowerthird.LowerThird.Companion.createImage
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.awt.Color
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class LowerThirdHeadlineAndSummaryHeaderAndLabelsTest {
    @Test
    fun testHeadline() {
        val lowerThird = LowerThirdHeadlineAndSummaryHeaderAndLabels(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "170 SEATS FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryEntry(Color.RED, "LIB", "2"),
                SummaryEntry(Color.BLUE, "CON", "1")
            )
                .asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.headline }, IsEqual("POLLS CLOSE ACROSS CENTRAL CANADA"))
    }

    @Test
    fun testSubhead() {
        val lowerThird = LowerThirdHeadlineAndSummaryHeaderAndLabels(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "170 SEATS FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryEntry(Color.RED, "LIB", "2"),
                SummaryEntry(Color.BLUE, "CON", "1")
            )
                .asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.subhead }, IsEqual("Polls open for 30 minutes on west coast"))
    }

    @Test
    fun testSummaryPanel() {
        val lowerThird = LowerThirdHeadlineAndSummaryHeaderAndLabels(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "170 SEATS FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryEntry(Color.RED, "LIB", "2"),
                SummaryEntry(Color.BLUE, "CON", "1")
            )
                .asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.numSummaryEntries }, IsEqual(2))
        Assertions.assertEquals(Color.RED, lowerThird.getEntryColor(0))
        Assertions.assertEquals(Color.BLUE, lowerThird.getEntryColor(1))
        Assertions.assertEquals("LIB", lowerThird.getEntryLabel(0))
        Assertions.assertEquals("CON", lowerThird.getEntryLabel(1))
        Assertions.assertEquals("2", lowerThird.getEntryValue(0))
        Assertions.assertEquals("1", lowerThird.getEntryValue(1))
        Assertions.assertEquals("170 SEATS FOR MAJORITY", lowerThird.summaryHeader)
    }

    @Test
    fun testRenderHeadlineAndSummary() {
        val lowerThird = LowerThirdHeadlineAndSummaryHeaderAndLabels(
            leftImagePublisher =
            createImage(
                LowerThirdHeadlineAndSummaryHeaderAndLabelsTest::class.java
                    .classLoader
                    .getResource("com/joecollins/graphics/lowerthird-left.png")
            )
                .asOneTimePublisher(),
            placePublisher = ("OTTAWA" to ZoneId.of("Canada/Eastern")).asOneTimePublisher(),
            headlinePublisher = "CENTRAL CANADA POLLS CLOSE".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "170 SEATS FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryEntry(Color.RED, "LIB", "2"),
                SummaryEntry(Color.BLUE, "CON", "1")
            )
                .asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryHeaderAndLabels", lowerThird)
    }

    @Test
    fun testRenderLongHeadlineAndSummary() {
        val lowerThird = LowerThirdHeadlineAndSummaryHeaderAndLabels(
            leftImagePublisher =
            createImage(
                LowerThirdHeadlineAndSummaryHeaderAndLabelsTest::class.java
                    .classLoader
                    .getResource("com/joecollins/graphics/lowerthird-left.png")
            )
                .asOneTimePublisher(),
            placePublisher = ("OTTAWA" to ZoneId.of("Canada/Eastern")).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for another 30 minutes in British Columbia, Yukon".asOneTimePublisher(),
            summaryHeaderPublisher = "170 SEATS FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryEntry(Color.RED, "LIB", "2"),
                SummaryEntry(Color.BLUE, "CON", "1")
            )
                .asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "LongHeadlineAndSummaryHeaderAndLabels", lowerThird)
    }
}