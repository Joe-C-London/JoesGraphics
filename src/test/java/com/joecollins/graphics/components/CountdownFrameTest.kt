package com.joecollins.graphics.components

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.io.IOException
import java.lang.InterruptedException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.Throws

class CountdownFrameTest {
    @Test
    fun testTimeRemaining() {
        val frame = CountdownFrame(
            headerBinding = fixedBinding(""),
            timeBinding = fixedBinding(ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern"))),
            labelFunc = { CountdownFrame.formatDDHHMMSS(it) }
        )
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T12:34:56Z"), ZoneId.of("UTC"))
        Assert.assertEquals(
            Duration.ofDays(1).plusHours(10).plusMinutes(25).plusSeconds(4), frame.getTimeRemaining()
        )
    }

    @Test
    fun testTimeDisplay() {
        val frame = CountdownFrame(
            headerBinding = fixedBinding(""),
            timeBinding = fixedBinding(ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern"))),
            labelFunc = { CountdownFrame.formatMMSS(it) }
        )
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T12:34:56Z"), ZoneId.of("UTC"))
        Assert.assertEquals("2065:04", frame.getTimeRemainingString())
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCountdown() {
        val frame = CountdownFrame(
            headerBinding = fixedBinding(""),
            timeBinding = fixedBinding(ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern"))),
            labelFunc = { CountdownFrame.formatDDHHMMSS(it) }
        )
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T12:34:56Z"), ZoneId.of("UTC"))
        Assert.assertEquals("1:10:25:04", frame.getTimeRemainingString())
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T12:34:57Z"), ZoneId.of("UTC"))
        Thread.sleep(200)
        Assert.assertEquals("1:10:25:03", frame.getTimeRemainingString())
    }

    @Test
    fun testAdditionalInfo() {
        val frame = CountdownFrame(
            headerBinding = fixedBinding(""),
            timeBinding = fixedBinding(ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern"))),
            labelFunc = { CountdownFrame.formatDDHHMMSS(it) },
            additionalInfoBinding = fixedBinding("ADDITIONAL INFO")
        )
        Assert.assertEquals("ADDITIONAL INFO", frame.getAdditionalInfo())
    }

    @Test
    fun testCountdownColor() {
        val frame = CountdownFrame(
            headerBinding = fixedBinding(""),
            timeBinding = fixedBinding(ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern"))),
            labelFunc = { CountdownFrame.formatDDHHMMSS(it) },
            countdownColorBinding = fixedBinding(Color.RED)
        )
        Assert.assertEquals(Color.RED, frame.getCountdownColor())
    }

    @Test
    @Throws(IOException::class)
    fun testRenderWithoutAdditionalInfo() {
        val frame = CountdownFrame(
            headerBinding = fixedBinding("TRUMP TERM END"),
            timeBinding = fixedBinding(ZonedDateTime.of(2021, 1, 20, 12, 0, 0, 0, ZoneId.of("US/Eastern"))),
            labelFunc = { CountdownFrame.formatDDHHMMSS(it) },
            borderColorBinding = fixedBinding(Color.RED),
            countdownColorBinding = fixedBinding(Color.RED)
        )
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T19:41:10Z"), ZoneId.of("UTC"))
        frame.setSize(200, 100)
        compareRendering("CountdownFrame", "NoAdditionalInfo", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderWithAdditionalInfo() {
        val frame = CountdownFrame(
            headerBinding = fixedBinding("1ST POLLS CLOSE"),
            timeBinding = fixedBinding(ZonedDateTime.of(2020, 11, 3, 23, 0, 0, 0, ZoneId.of("UTC"))),
            labelFunc = { CountdownFrame.formatDDHHMMSS(it) },
            additionalInfoBinding = fixedBinding("IN/KY")
        )
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T19:41:10Z"), ZoneId.of("UTC"))
        frame.setSize(200, 100)
        compareRendering("CountdownFrame", "AdditionalInfo", frame)
    }
}
