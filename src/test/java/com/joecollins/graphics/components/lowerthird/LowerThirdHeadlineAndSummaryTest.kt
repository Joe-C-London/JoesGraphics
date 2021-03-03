package com.joecollins.graphics.components.lowerthird

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

class LowerThirdHeadlineAndSummaryTest {
    @Test
    fun testHeadline() {
        val lowerThird = LowerThirdHeadlineAndSummary()
        lowerThird.setHeadlineBinding(fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"))
        Assert.assertEquals("POLLS CLOSE ACROSS CENTRAL CANADA", lowerThird.headline)
    }

    @Test
    fun testSubhead() {
        val lowerThird = LowerThirdHeadlineAndSummary()
        lowerThird.setSubheadBinding(fixedBinding("Polls open for 30 minutes on west coast"))
        Assert.assertEquals("Polls open for 30 minutes on west coast", lowerThird.subhead)
    }

    @Test
    fun testSummaryPanel() {
        val lowerThird = LowerThirdHeadlineAndSummary()
        lowerThird.setNumSummaryEntriesBinding(fixedBinding(2))
        lowerThird.setSummaryEntriesBinding(
                listBinding(
                        SummaryWithLabels.Entry(Color.RED, "LIB", "2"), SummaryWithLabels.Entry(Color.BLUE, "CON", "1")))
        Assert.assertEquals(2, lowerThird.numSummaryEntries.toLong())
        Assert.assertEquals(Color.RED, lowerThird.getEntryColor(0))
        Assert.assertEquals(Color.BLUE, lowerThird.getEntryColor(1))
        Assert.assertEquals("LIB", lowerThird.getEntryLabel(0))
        Assert.assertEquals("CON", lowerThird.getEntryLabel(1))
        Assert.assertEquals("2", lowerThird.getEntryValue(0))
        Assert.assertEquals("1", lowerThird.getEntryValue(1))
    }

    @Test
    @Throws(IOException::class)
    fun testRenderHeadlineAndSummary() {
        val lowerThird = LowerThirdHeadlineAndSummary()
        lowerThird.setSize(1024, 50)
        lowerThird.setLeftImageBinding(
                fixedBinding(
                        createImage(
                                LowerThirdHeadlineAndSummaryTest::class.java
                                        .classLoader
                                        .getResource("com/joecollins/graphics/lowerthird-left.png"))))
        lowerThird.setPlaceBinding(fixedBinding("OTTAWA"))
        lowerThird.setTimeZoneBinding(fixedBinding(ZoneId.of("Canada/Eastern")))
        lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()))
        lowerThird.setHeadlineBinding(fixedBinding("CENTRAL CANADA POLLS CLOSE"))
        lowerThird.setSubheadBinding(fixedBinding("Polls open for 30 minutes on west coast"))
        lowerThird.setNumSummaryEntriesBinding(fixedBinding(2))
        lowerThird.setSummaryEntriesBinding(
                listBinding(
                        SummaryWithLabels.Entry(Color.RED, "LIB", "2"), SummaryWithLabels.Entry(Color.BLUE, "CON", "1")))
        compareRendering("LowerThird", "HeadlineAndSummary", lowerThird)
    }
}
