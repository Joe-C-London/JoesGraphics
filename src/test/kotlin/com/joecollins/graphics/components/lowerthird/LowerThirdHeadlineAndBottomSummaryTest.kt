package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.components.lowerthird.LowerThird.Companion.createImage
import com.joecollins.graphics.components.lowerthird.LowerThirdHeadlineAndBottomSummary.Companion.createSeatEntries
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

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
                BottomSummary.Entry(Color.RED, "LIB", "2"),
                BottomSummary.Entry(Color.BLUE, "CON", "1"),
                BottomSummary.Entry(Color.CYAN.darker(), "BQ", "0"),
                BottomSummary.Entry(Color.GREEN.darker(), "GRN", "0"),
                BottomSummary.Entry(Color.ORANGE, "NDP", "0"),
                BottomSummary.Entry(Color.WHITE, "?", "335"),
            )
                .asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.headline }, IsEqual("POLLS CLOSE ACROSS CENTRAL CANADA"))
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
                BottomSummary.Entry(Color.RED, "LIB", "2"),
                BottomSummary.Entry(Color.BLUE, "CON", "1"),
                BottomSummary.Entry(Color.CYAN.darker(), "BQ", "0"),
                BottomSummary.Entry(Color.GREEN.darker(), "GRN", "0"),
                BottomSummary.Entry(Color.ORANGE, "NDP", "0"),
                BottomSummary.Entry(Color.WHITE, "?", "335"),
            )
                .asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.subhead }, IsEqual("Polls open for 30 minutes on west coast"))
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
                BottomSummary.Entry(Color.RED, "LIB", "2"),
                BottomSummary.Entry(Color.BLUE, "CON", "1"),
                BottomSummary.Entry(Color.CYAN.darker(), "BQ", "0"),
                BottomSummary.Entry(Color.GREEN.darker(), "GRN", "0"),
                BottomSummary.Entry(Color.ORANGE, "NDP", "0"),
                BottomSummary.Entry(Color.WHITE, "?", "335"),
            )
                .asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.numSummaryEntries }, IsEqual(6))
        Assert.assertEquals(Color.RED, lowerThird.getEntryColor(0))
        Assert.assertEquals(Color.BLUE, lowerThird.getEntryColor(1))
        Assert.assertEquals(Color.WHITE, lowerThird.getEntryColor(5))
        Assert.assertEquals("LIB", lowerThird.getEntryLabel(0))
        Assert.assertEquals("CON", lowerThird.getEntryLabel(1))
        Assert.assertEquals("?", lowerThird.getEntryLabel(5))
        Assert.assertEquals("2", lowerThird.getEntryValue(0))
        Assert.assertEquals("1", lowerThird.getEntryValue(1))
        Assert.assertEquals("335", lowerThird.getEntryValue(5))
        Assert.assertEquals("SEATS WON", lowerThird.summaryHeader)
        Assert.assertEquals("170 FOR MAJORITY", lowerThird.summaryFooter)
    }

    @Test
    fun testRenderHeadlineAndSummary() {
        val lowerThird = LowerThirdHeadlineAndBottomSummary(
            leftImagePublisher =
            createImage(
                LowerThirdHeadlineAndBottomSummaryTest::class.java
                    .classLoader
                    .getResource("com/joecollins/graphics/lowerthird-left.png")
            )
                .asOneTimePublisher(),
            placePublisher = ("OTTAWA" to ZoneId.of("Canada/Eastern")).asOneTimePublisher(),
            headlinePublisher = "CENTRAL CANADA POLLS CLOSE".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "SEATS WON".asOneTimePublisher(),
            summaryFooterPublisher = "170 FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            listOf(
                BottomSummary.Entry(Color.RED, "LIB", "2"),
                BottomSummary.Entry(Color.BLUE, "CON", "1"),
                BottomSummary.Entry(Color.CYAN.darker(), "BQ", "0"),
                BottomSummary.Entry(Color.GREEN.darker(), "GRN", "0"),
                BottomSummary.Entry(Color.ORANGE, "NDP", "0"),
                BottomSummary.Entry(Color.WHITE, "?", "335"),
            )
                .asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 70)
        compareRendering("LowerThird", "HeadlineAndBottomSummaryHeader", lowerThird)
    }

    @Test
    fun testRenderHeadlineAndSummarySeatFactory() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val bq = Party("Bloc Quebecois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val lowerThird = LowerThirdHeadlineAndBottomSummary(
            leftImagePublisher =
            createImage(
                LowerThirdHeadlineAndBottomSummaryTest::class.java
                    .classLoader
                    .getResource("com/joecollins/graphics/lowerthird-left.png")
            )
                .asOneTimePublisher(),
            placePublisher = ("OTTAWA" to ZoneId.of("Canada/Eastern")).asOneTimePublisher(),
            headlinePublisher = "CENTRAL CANADA POLLS CLOSE".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "SEATS WON".asOneTimePublisher(),
            summaryFooterPublisher = "170 FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            createSeatEntries(
                mapOf(
                    lib to 2,
                    con to 1,
                ),
                338,
                setOf(bq, con, grn, lib, ndp)
            )
                .asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 70)
        compareRendering("LowerThird", "HeadlineAndBottomSummaryHeader", lowerThird)
    }
}
