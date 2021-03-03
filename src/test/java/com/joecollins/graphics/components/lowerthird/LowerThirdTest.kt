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

class LowerThirdTest {
    @Test
    fun testLeftImage() {
        val lowerThird = LowerThird()
        val image = createImage("BREAKING NEWS", Color.WHITE, Color.RED)
        lowerThird.setLeftImageBinding(fixedBinding(image))
        Assert.assertEquals(image, lowerThird.leftImage)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLocationAndTimeZone() {
        val lowerThird = LowerThird()
        lowerThird.setPlaceBinding(fixedBinding("OTTAWA"))
        lowerThird.setTimeZoneBinding(fixedBinding(ZoneId.of("Canada/Eastern")))
        lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()))
        Thread.sleep(100)
        Assert.assertEquals("OTTAWA", lowerThird.place)
        Assert.assertEquals("21:30", lowerThird.time)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderBlankMiddle() {
        val lowerThird = LowerThird()
        lowerThird.setSize(1024, 50)
        lowerThird.setLeftImageBinding(
                fixedBinding(createImage("BREAKING NEWS", Color.WHITE, Color.RED)))
        lowerThird.setPlaceBinding(fixedBinding("OTTAWA"))
        lowerThird.setTimeZoneBinding(fixedBinding(ZoneId.of("Canada/Eastern")))
        lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()))
        compareRendering("LowerThird", "BlankMiddle", lowerThird)
    }
}
