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
import java.lang.InterruptedException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.Throws

class LowerThirdTest {
    @Test
    fun testLeftImage() {
        val image = createImage("BREAKING NEWS", Color.WHITE, Color.RED)
        val lowerThird = LowerThird(
            leftImagePublisher = image.asOneTimePublisher(),
            placePublisher = "OTTAWA".asOneTimePublisher(),
            timezonePublisher = ZoneId.of("Canada/Eastern").asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.leftImage }, IsEqual(image))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLocationAndTimeZone() {
        val lowerThird = LowerThird(
            leftImagePublisher = createImage("BREAKING NEWS", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = "OTTAWA".asOneTimePublisher(),
            timezonePublisher = ZoneId.of("Canada/Eastern").asOneTimePublisher(),
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
            leftImagePublisher = createImage("BREAKING NEWS", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = "OTTAWA".asOneTimePublisher(),
            timezonePublisher = ZoneId.of("Canada/Eastern").asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "BlankMiddle", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderBlankMiddleShowingTimeZone() {
        val lowerThird = LowerThird(
            leftImagePublisher = createImage("BREAKING NEWS", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = "OTTAWA".asOneTimePublisher(),
            timezonePublisher = ZoneId.of("Canada/Eastern").asOneTimePublisher(),
            showTimeZone = true,
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "BlankMiddleShowingTimeZone", lowerThird)
    }
}