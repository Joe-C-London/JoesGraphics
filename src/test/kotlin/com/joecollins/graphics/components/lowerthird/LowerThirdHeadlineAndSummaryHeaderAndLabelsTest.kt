package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.components.lowerthird.LowerThird.Companion.createImage
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class LowerThirdHeadlineAndSummaryHeaderAndLabelsTest {
    @Test
    fun testHeadline() {
        val lowerThird = LowerThirdHeadlineAndSummaryHeaderAndLabels(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = "".asOneTimePublisher(),
            timezonePublisher = ZoneId.systemDefault().asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "170 SEATS FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryWithHeaderAndLabels.Entry(Color.RED, "LIB", "2"),
                SummaryWithHeaderAndLabels.Entry(Color.BLUE, "CON", "1")
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
            placePublisher = "".asOneTimePublisher(),
            timezonePublisher = ZoneId.systemDefault().asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "170 SEATS FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryWithHeaderAndLabels.Entry(Color.RED, "LIB", "2"),
                SummaryWithHeaderAndLabels.Entry(Color.BLUE, "CON", "1")
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
            placePublisher = "".asOneTimePublisher(),
            timezonePublisher = ZoneId.systemDefault().asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "170 SEATS FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryWithHeaderAndLabels.Entry(Color.RED, "LIB", "2"),
                SummaryWithHeaderAndLabels.Entry(Color.BLUE, "CON", "1")
            )
                .asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.numSummaryEntries }, IsEqual(2))
        Assert.assertEquals(Color.RED, lowerThird.getEntryColor(0))
        Assert.assertEquals(Color.BLUE, lowerThird.getEntryColor(1))
        Assert.assertEquals("LIB", lowerThird.getEntryLabel(0))
        Assert.assertEquals("CON", lowerThird.getEntryLabel(1))
        Assert.assertEquals("2", lowerThird.getEntryValue(0))
        Assert.assertEquals("1", lowerThird.getEntryValue(1))
        Assert.assertEquals("170 SEATS FOR MAJORITY", lowerThird.summaryHeader)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderHeadlineAndSummary() {
        val lowerThird = LowerThirdHeadlineAndSummaryHeaderAndLabels(
            leftImagePublisher =
            createImage(
                LowerThirdHeadlineAndSummaryHeaderAndLabelsTest::class.java
                    .classLoader
                    .getResource("com/joecollins/graphics/lowerthird-left.png")
            )
                .asOneTimePublisher(),
            placePublisher = "OTTAWA".asOneTimePublisher(),
            timezonePublisher = ZoneId.of("Canada/Eastern").asOneTimePublisher(),
            headlinePublisher = "CENTRAL CANADA POLLS CLOSE".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "170 SEATS FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryWithHeaderAndLabels.Entry(Color.RED, "LIB", "2"),
                SummaryWithHeaderAndLabels.Entry(Color.BLUE, "CON", "1")
            )
                .asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryHeaderAndLabels", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderLongHeadlineAndSummary() {
        val lowerThird = LowerThirdHeadlineAndSummaryHeaderAndLabels(
            leftImagePublisher =
            createImage(
                LowerThirdHeadlineAndSummaryHeaderAndLabelsTest::class.java
                    .classLoader
                    .getResource("com/joecollins/graphics/lowerthird-left.png")
            )
                .asOneTimePublisher(),
            placePublisher = "OTTAWA".asOneTimePublisher(),
            timezonePublisher = ZoneId.of("Canada/Eastern").asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for another 30 minutes in British Columbia, Yukon".asOneTimePublisher(),
            summaryHeaderPublisher = "170 SEATS FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                SummaryWithHeaderAndLabels.Entry(Color.RED, "LIB", "2"),
                SummaryWithHeaderAndLabels.Entry(Color.BLUE, "CON", "1")
            )
                .asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "LongHeadlineAndSummaryHeaderAndLabels", lowerThird)
    }
}