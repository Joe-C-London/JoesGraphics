package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.components.lowerthird.LowerThird.Companion.createImage
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class LowerThirdHeadlineAndSummaryHeaderAndLabelsTest {
    @Test
    fun testHeadline() {
        val lowerThird = LowerThirdHeadlineAndSummaryHeaderAndLabels(
            leftImageBinding = fixedBinding(createImage("", Color.RED, Color.WHITE)),
            placeBinding = fixedBinding(""),
            timezoneBinding = fixedBinding(ZoneId.systemDefault()),
            headlineBinding = fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"),
            subheadBinding = fixedBinding("Polls open for 30 minutes on west coast"),
            summaryHeaderBinding = fixedBinding("170 SEATS FOR MAJORITY"),
            summaryEntriesBinding = fixedBinding(
                listOf(
                    SummaryWithHeaderAndLabels.Entry(Color.RED, "LIB", "2"),
                    SummaryWithHeaderAndLabels.Entry(Color.BLUE, "CON", "1")
                )
            )
        )
        Assert.assertEquals("POLLS CLOSE ACROSS CENTRAL CANADA", lowerThird.headline)
    }

    @Test
    fun testSubhead() {
        val lowerThird = LowerThirdHeadlineAndSummaryHeaderAndLabels(
            leftImageBinding = fixedBinding(createImage("", Color.RED, Color.WHITE)),
            placeBinding = fixedBinding(""),
            timezoneBinding = fixedBinding(ZoneId.systemDefault()),
            headlineBinding = fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"),
            subheadBinding = fixedBinding("Polls open for 30 minutes on west coast"),
            summaryHeaderBinding = fixedBinding("170 SEATS FOR MAJORITY"),
            summaryEntriesBinding = fixedBinding(
                listOf(
                    SummaryWithHeaderAndLabels.Entry(Color.RED, "LIB", "2"),
                    SummaryWithHeaderAndLabels.Entry(Color.BLUE, "CON", "1")
                )
            )
        )
        Assert.assertEquals("Polls open for 30 minutes on west coast", lowerThird.subhead)
    }

    @Test
    fun testSummaryPanel() {
        val lowerThird = LowerThirdHeadlineAndSummaryHeaderAndLabels(
            leftImageBinding = fixedBinding(createImage("", Color.RED, Color.WHITE)),
            placeBinding = fixedBinding(""),
            timezoneBinding = fixedBinding(ZoneId.systemDefault()),
            headlineBinding = fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"),
            subheadBinding = fixedBinding("Polls open for 30 minutes on west coast"),
            summaryHeaderBinding = fixedBinding("170 SEATS FOR MAJORITY"),
            summaryEntriesBinding = fixedBinding(
                listOf(
                    SummaryWithHeaderAndLabels.Entry(Color.RED, "LIB", "2"),
                    SummaryWithHeaderAndLabels.Entry(Color.BLUE, "CON", "1")
                )
            )
        )
        Assert.assertEquals(2, lowerThird.numSummaryEntries.toLong())
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
            leftImageBinding = fixedBinding(
                createImage(
                    LowerThirdHeadlineAndSummaryHeaderAndLabelsTest::class.java
                        .classLoader
                        .getResource("com/joecollins/graphics/lowerthird-left.png")
                )
            ),
            placeBinding = fixedBinding("OTTAWA"),
            timezoneBinding = fixedBinding(ZoneId.of("Canada/Eastern")),
            headlineBinding = fixedBinding("CENTRAL CANADA POLLS CLOSE"),
            subheadBinding = fixedBinding("Polls open for 30 minutes on west coast"),
            summaryHeaderBinding = fixedBinding("170 SEATS FOR MAJORITY"),
            summaryEntriesBinding = fixedBinding(
                listOf(
                    SummaryWithHeaderAndLabels.Entry(Color.RED, "LIB", "2"),
                    SummaryWithHeaderAndLabels.Entry(Color.BLUE, "CON", "1")
                )
            ),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryHeaderAndLabels", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderLongHeadlineAndSummary() {
        val lowerThird = LowerThirdHeadlineAndSummaryHeaderAndLabels(
            leftImageBinding = fixedBinding(
                createImage(
                    LowerThirdHeadlineAndSummaryHeaderAndLabelsTest::class.java
                        .classLoader
                        .getResource("com/joecollins/graphics/lowerthird-left.png")
                )
            ),
            placeBinding = fixedBinding("OTTAWA"),
            timezoneBinding = fixedBinding(ZoneId.of("Canada/Eastern")),
            headlineBinding = fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"),
            subheadBinding = fixedBinding("Polls open for another 30 minutes in British Columbia, Yukon"),
            summaryHeaderBinding = fixedBinding("170 SEATS FOR MAJORITY"),
            summaryEntriesBinding = fixedBinding(
                listOf(
                    SummaryWithHeaderAndLabels.Entry(Color.RED, "LIB", "2"),
                    SummaryWithHeaderAndLabels.Entry(Color.BLUE, "CON", "1")
                )
            ),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "LongHeadlineAndSummaryHeaderAndLabels", lowerThird)
    }
}
