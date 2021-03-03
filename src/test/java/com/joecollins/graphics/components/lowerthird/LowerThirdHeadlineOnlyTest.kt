package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.components.lowerthird.LowerThird.Companion.createImage
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.io.IOException
import java.lang.InterruptedException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.Throws
import org.junit.Assert
import org.junit.Test

class LowerThirdHeadlineOnlyTest {
    @Test
    fun testHeadline() {
        val lowerThird = LowerThirdHeadlineOnly()
        lowerThird.setHeadlineBinding(fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"))
        Assert.assertEquals("POLLS CLOSE ACROSS CENTRAL CANADA", lowerThird.headline)
    }

    @Test
    fun testSubhead() {
        val lowerThird = LowerThirdHeadlineOnly()
        lowerThird.setSubheadBinding(fixedBinding("Polls open for 30 minutes on west coast"))
        Assert.assertEquals("Polls open for 30 minutes on west coast", lowerThird.subhead)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testRenderHeadlineSubhead() {
        val lowerThird = LowerThirdHeadlineOnly()
        lowerThird.setSize(1024, 50)
        lowerThird.setLeftImageBinding(
                fixedBinding(createImage("BREAKING NEWS", Color.WHITE, Color.RED)))
        lowerThird.setPlaceBinding(fixedBinding("OTTAWA"))
        lowerThird.setTimeZoneBinding(fixedBinding(ZoneId.of("Canada/Eastern")))
        lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()))
        lowerThird.setHeadlineBinding(fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"))
        lowerThird.setSubheadBinding(fixedBinding("Polls open for 30 minutes on west coast"))
        Thread.sleep(100)
        compareRendering("LowerThird", "HeadlineAndSubhead", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderHeadlineOnly() {
        val lowerThird = LowerThirdHeadlineOnly()
        lowerThird.setSize(1024, 50)
        lowerThird.setLeftImageBinding(
                fixedBinding(createImage("BREAKING NEWS", Color.WHITE, Color.RED)))
        lowerThird.setPlaceBinding(fixedBinding("OTTAWA"))
        lowerThird.setTimeZoneBinding(fixedBinding(ZoneId.of("Canada/Eastern")))
        lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()))
        lowerThird.setHeadlineBinding(fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"))
        lowerThird.setSubheadBinding(fixedBinding(null))
        compareRendering("LowerThird", "HeadlineOnly", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderHeadlineSubheadAccents() {
        val lowerThird = LowerThirdHeadlineOnly()
        lowerThird.setSize(1024, 50)
        lowerThird.setLeftImageBinding(
                fixedBinding(
                        createImage("\u00c9LECTION FRAN\u00c7AIS", Color.WHITE, Color.RED)))
        lowerThird.setPlaceBinding(fixedBinding("SAINT-\u00c9TIENNE"))
        lowerThird.setTimeZoneBinding(fixedBinding(ZoneId.of("Europe/Paris")))
        lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()))
        lowerThird.setHeadlineBinding(fixedBinding("\u00c9LECTION FRAN\u00c7AIS EST FINI"))
        lowerThird.setSubheadBinding(fixedBinding("\u00c9lection fran\u00e7ais est s\u00fbr"))
        compareRendering("LowerThird", "HeadlineAndSubheadAccents", lowerThird)
    }
}
