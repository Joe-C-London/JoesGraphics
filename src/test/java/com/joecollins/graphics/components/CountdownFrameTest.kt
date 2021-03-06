package com.joecollins.graphics.components

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.io.IOException
import java.lang.InterruptedException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.Throws
import org.junit.Assert
import org.junit.Test

class CountdownFrameTest {
    @Test
    fun testTimeRemaining() {
        val frame = CountdownFrame()
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T12:34:56Z"), ZoneId.of("UTC"))
        frame.setTimeBinding(
                fixedBinding(ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern"))))
        Assert.assertEquals(
                Duration.ofDays(1).plusHours(10).plusMinutes(25).plusSeconds(4), frame.getTimeRemaining())
    }

    @Test
    fun testTimeDisplay() {
        val frame = CountdownFrame()
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T12:34:56Z"), ZoneId.of("UTC"))
        frame.setTimeBinding(
                fixedBinding(ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern"))))
        frame.setLabelFunction { CountdownFrame.formatDDHHMMSS(it) }
        Assert.assertEquals("1:10:25:04", frame.getTimeRemainingString())
        frame.setLabelFunction { CountdownFrame.formatHHMMSS(it) }
        Assert.assertEquals("34:25:04", frame.getTimeRemainingString())
        frame.setLabelFunction { CountdownFrame.formatMMSS(it) }
        Assert.assertEquals("2065:04", frame.getTimeRemainingString())
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCountdown() {
        val frame = CountdownFrame()
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T12:34:56Z"), ZoneId.of("UTC"))
        frame.setTimeBinding(
                fixedBinding(ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern"))))
        frame.setLabelFunction { it: Duration -> CountdownFrame.formatDDHHMMSS(it) }
        Assert.assertEquals("1:10:25:04", frame.getTimeRemainingString())
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T12:34:57Z"), ZoneId.of("UTC"))
        Thread.sleep(200)
        Assert.assertEquals("1:10:25:03", frame.getTimeRemainingString())
    }

    @Test
    fun testAdditionalInfo() {
        val frame = CountdownFrame()
        frame.setAdditionalInfoBinding(fixedBinding("ADDITIONAL INFO"))
        Assert.assertEquals("ADDITIONAL INFO", frame.getAdditionalInfo())
    }

    @Test
    fun testCountdownColor() {
        val frame = CountdownFrame()
        frame.setCountdownColorBinding(fixedBinding(Color.RED))
        Assert.assertEquals(Color.RED, frame.getCountdownColor())
    }

    @Test
    @Throws(IOException::class)
    fun testRenderWithoutAdditionalInfo() {
        val frame = CountdownFrame()
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T19:41:10Z"), ZoneId.of("UTC"))
        frame.setTimeBinding(
                fixedBinding(ZonedDateTime.of(2021, 1, 20, 12, 0, 0, 0, ZoneId.of("US/Eastern"))))
        frame.setLabelFunction { CountdownFrame.formatDDHHMMSS(it) }
        frame.setHeaderBinding(fixedBinding("TRUMP TERM END"))
        frame.setCountdownColorBinding(fixedBinding(Color.RED))
        frame.setBorderColorBinding(fixedBinding(Color.RED))
        frame.setSize(200, 100)
        compareRendering("CountdownFrame", "NoAdditionalInfo", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderWithAdditionalInfo() {
        val frame = CountdownFrame()
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T19:41:10Z"), ZoneId.of("UTC"))
        frame.setTimeBinding(
                fixedBinding(ZonedDateTime.of(2020, 11, 3, 23, 0, 0, 0, ZoneId.of("UTC"))))
        frame.setLabelFunction { CountdownFrame.formatDDHHMMSS(it) }
        frame.setHeaderBinding(fixedBinding("1ST POLLS CLOSE"))
        frame.setAdditionalInfoBinding(fixedBinding("IN/KY"))
        frame.setSize(200, 100)
        compareRendering("CountdownFrame", "AdditionalInfo", frame)
    }
}
