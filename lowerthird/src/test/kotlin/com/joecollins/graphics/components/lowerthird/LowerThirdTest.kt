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

class LowerThirdTest {
    @Test
    fun testLeftImage() {
        val image = createImage("BREAKING NEWS", Color.WHITE, Color.RED)
        val lowerThird = LowerThird(
            leftImagePublisher = image.asOneTimePublisher(),
            placePublisher = ("OTTAWA" to ZoneId.of("Canada/Eastern")).asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.leftImage }, IsEqual(image))
    }

    @Test
    fun testLocationAndTimeZone() {
        val lowerThird = LowerThird(
            leftImagePublisher = createImage("BREAKING NEWS", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = ("OTTAWA" to ZoneId.of("Canada/Eastern")).asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        Thread.sleep(100)
        Assertions.assertEquals("OTTAWA", lowerThird.place)
        Assertions.assertEquals("21:30", lowerThird.time)
    }

    @Test
    fun testRenderBlankMiddle() {
        val lowerThird = LowerThird(
            leftImagePublisher = createImage("BREAKING NEWS", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = ("OTTAWA" to ZoneId.of("Canada/Eastern")).asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "BlankMiddle", lowerThird)
    }

    @Test
    fun testRenderBlankMiddleShowingTimeZone() {
        val lowerThird = LowerThird(
            leftImagePublisher = createImage("BREAKING NEWS", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = ("OTTAWA" to ZoneId.of("Canada/Eastern")).asOneTimePublisher(),
            showTimeZone = true,
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "BlankMiddleShowingTimeZone", lowerThird)
    }
}
