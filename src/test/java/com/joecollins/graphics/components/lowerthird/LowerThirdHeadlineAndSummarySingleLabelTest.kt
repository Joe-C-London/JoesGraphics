package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.IndexedBinding.Companion.listBinding
import com.joecollins.graphics.components.lowerthird.LowerThird.Companion.createImage
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert
import org.junit.Test

class LowerThirdHeadlineAndSummarySingleLabelTest {
    @Test
    fun testHeadline() {
        val lowerThird = LowerThirdHeadlineAndSummarySingleLabel()
        lowerThird.setHeadlineBinding(fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"))
        Assert.assertEquals("POLLS CLOSE ACROSS CENTRAL CANADA", lowerThird.headline)
    }

    @Test
    fun testSubhead() {
        val lowerThird = LowerThirdHeadlineAndSummarySingleLabel()
        lowerThird.setSubheadBinding(fixedBinding("Polls open for 30 minutes on west coast"))
        Assert.assertEquals("Polls open for 30 minutes on west coast", lowerThird.subhead)
    }

    @Test
    fun testSummaryPanel() {
        val lowerThird = LowerThirdHeadlineAndSummarySingleLabel()
        lowerThird.setNumSummaryEntriesBinding(fixedBinding(2))
        lowerThird.setSummaryHeaderBinding(Binding.fixedBinding("170 SEATS FOR MAJORITY"))
        lowerThird.setSummaryEntriesBinding(
                listBinding(SummaryWithoutLabels.Entry(Color.RED, "2"), SummaryWithoutLabels.Entry(Color.BLUE, "1")))
        Assert.assertEquals(2, lowerThird.numSummaryEntries.toLong())
        Assert.assertEquals(Color.RED, lowerThird.getEntryColor(0))
        Assert.assertEquals(Color.BLUE, lowerThird.getEntryColor(1))
        Assert.assertEquals("2", lowerThird.getEntryValue(0))
        Assert.assertEquals("1", lowerThird.getEntryValue(1))
        Assert.assertEquals("170 SEATS FOR MAJORITY", lowerThird.summaryHeader)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderHeadlineAndSummary() {
        val lowerThird = LowerThirdHeadlineAndSummarySingleLabel()
        lowerThird.setSize(1024, 50)
        lowerThird.setLeftImageBinding(
                fixedBinding(
                        createImage(
                                LowerThirdHeadlineAndSummarySingleLabelTest::class.java
                                        .classLoader
                                        .getResource("com/joecollins/graphics/lowerthird-left.png"))))
        lowerThird.setPlaceBinding(fixedBinding("OTTAWA"))
        lowerThird.setTimeZoneBinding(fixedBinding(ZoneId.of("Canada/Eastern")))
        lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()))
        lowerThird.setHeadlineBinding(fixedBinding("CENTRAL CANADA POLLS CLOSE"))
        lowerThird.setSubheadBinding(fixedBinding("Polls open for 30 minutes on west coast"))
        lowerThird.setNumSummaryEntriesBinding(fixedBinding(2))
        lowerThird.setSummaryEntriesBinding(
                listBinding(SummaryWithoutLabels.Entry(Color.RED, "2"), SummaryWithoutLabels.Entry(Color.BLUE, "1")))
        lowerThird.setSummaryHeaderBinding(fixedBinding("170 SEATS FOR MAJORITY"))
        compareRendering("LowerThird", "HeadlineAndSummarySingleLabel", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderLongHeadlineAndSummary() {
        val lowerThird = LowerThirdHeadlineAndSummarySingleLabel()
        lowerThird.setSize(1024, 50)
        lowerThird.setLeftImageBinding(
                fixedBinding(
                        createImage(
                                LowerThirdHeadlineAndSummarySingleLabelTest::class.java
                                        .classLoader
                                        .getResource("com/joecollins/graphics/lowerthird-left.png"))))
        lowerThird.setPlaceBinding(fixedBinding("OTTAWA"))
        lowerThird.setTimeZoneBinding(fixedBinding(ZoneId.of("Canada/Eastern")))
        lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()))
        lowerThird.setHeadlineBinding(fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"))
        lowerThird.setSubheadBinding(
                fixedBinding("Polls open for another 30 minutes in British Columbia, Yukon"))
        lowerThird.setNumSummaryEntriesBinding(fixedBinding(2))
        lowerThird.setSummaryEntriesBinding(
                listBinding(SummaryWithoutLabels.Entry(Color.RED, "2"), SummaryWithoutLabels.Entry(Color.BLUE, "1")))
        lowerThird.setSummaryHeaderBinding(fixedBinding("170 SEATS FOR MAJORITY"))
        compareRendering("LowerThird", "LongHeadlineAndSummarySingleLabel", lowerThird)
    }
}
