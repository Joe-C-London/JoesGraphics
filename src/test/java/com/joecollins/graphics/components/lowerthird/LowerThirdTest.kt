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
        val image = createImage("BREAKING NEWS", Color.WHITE, Color.RED)
        val lowerThird = LowerThird(
            leftImageBinding = fixedBinding(image),
            placeBinding = fixedBinding("OTTAWA"),
            timezoneBinding = fixedBinding(ZoneId.of("Canada/Eastern"))
        )
        Assert.assertEquals(image, lowerThird.leftImage)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLocationAndTimeZone() {
        val lowerThird = LowerThird(
            leftImageBinding = fixedBinding(createImage("BREAKING NEWS", Color.WHITE, Color.RED)),
            placeBinding = fixedBinding("OTTAWA"),
            timezoneBinding = fixedBinding(ZoneId.of("Canada/Eastern")),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        Thread.sleep(100)
        Assert.assertEquals("OTTAWA", lowerThird.place)
        Assert.assertEquals("21:30", lowerThird.time)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderBlankMiddle() {
        val lowerThird = LowerThird(
            leftImageBinding = fixedBinding(createImage("BREAKING NEWS", Color.WHITE, Color.RED)),
            placeBinding = fixedBinding("OTTAWA"),
            timezoneBinding = fixedBinding(ZoneId.of("Canada/Eastern")),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "BlankMiddle", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderBlankMiddleShowingTimeZone() {
        val lowerThird = LowerThird(
            leftImageBinding = fixedBinding(createImage("BREAKING NEWS", Color.WHITE, Color.RED)),
            placeBinding = fixedBinding("OTTAWA"),
            timezoneBinding = fixedBinding(ZoneId.of("Canada/Eastern")),
            showTimeZone = true,
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "BlankMiddleShowingTimeZone", lowerThird)
    }
}
