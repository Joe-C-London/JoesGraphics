package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class CountdownFrameTest {
    @Test
    fun testTimeRemaining() {
        val frame = CountdownFrame(
            headerPublisher = "".asOneTimePublisher(),
            timePublisher = ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern")).asOneTimePublisher(),
            labelFunc = { CountdownFrame.formatDDHHMMSS(it) },
        )
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T12:34:56Z"), ZoneId.of("UTC"))
        assertEquals(
            Duration.ofDays(1).plusHours(10).plusMinutes(25).plusSeconds(4),
            frame.getTimeRemaining(),
        )
    }

    @Test
    fun testTimeDisplay() {
        val frame = CountdownFrame(
            headerPublisher = "".asOneTimePublisher(),
            timePublisher = ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern")).asOneTimePublisher(),
            labelFunc = { CountdownFrame.formatMMSS(it) },
        )
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T12:34:56Z"), ZoneId.of("UTC"))
        assertEquals("2065:04", frame.getTimeRemainingString())
    }

    @Test
    fun testCountdown() {
        val frame = CountdownFrame(
            headerPublisher = "".asOneTimePublisher(),
            timePublisher = ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern")).asOneTimePublisher(),
            labelFunc = { CountdownFrame.formatDDHHMMSS(it) },
        )
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T12:34:56Z"), ZoneId.of("UTC"))
        assertEquals("1:10:25:04", frame.getTimeRemainingString())

        frame.clock = Clock.fixed(Instant.parse("2020-07-04T12:34:57Z"), ZoneId.of("UTC"))
        assertEquals("1:10:25:03", frame.getTimeRemainingString())
    }

    @Test
    fun testAdditionalInfo() {
        val frame = CountdownFrame(
            headerPublisher = "".asOneTimePublisher(),
            timePublisher = ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern")).asOneTimePublisher(),
            labelFunc = { CountdownFrame.formatDDHHMMSS(it) },
            additionalInfoPublisher = "ADDITIONAL INFO".asOneTimePublisher(),
        )
        assertEquals("ADDITIONAL INFO", frame.getAdditionalInfo())
    }

    @Test
    fun testCountdownColor() {
        val frame = CountdownFrame(
            headerPublisher = "".asOneTimePublisher(),
            timePublisher = ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern")).asOneTimePublisher(),
            labelFunc = { CountdownFrame.formatDDHHMMSS(it) },
            countdownColorPublisher = Color.RED.asOneTimePublisher(),
        )
        assertEquals(Color.RED, frame.getCountdownColor())
    }

    @Test
    fun testRenderWithoutAdditionalInfo() {
        val frame = CountdownFrame(
            headerPublisher = "TRUMP TERM END".asOneTimePublisher(),
            timePublisher = ZonedDateTime.of(2021, 1, 20, 12, 0, 0, 0, ZoneId.of("US/Eastern")).asOneTimePublisher(),
            labelFunc = { CountdownFrame.formatDDHHMMSS(it) },
            borderColorPublisher = Color.RED.asOneTimePublisher(),
            countdownColorPublisher = Color.RED.asOneTimePublisher(),
        )
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T19:41:10Z"), ZoneId.of("UTC"))
        frame.setSize(200, 100)
        compareRendering("CountdownFrame", "NoAdditionalInfo", frame)
    }

    @Test
    fun testRenderWithAdditionalInfo() {
        val frame = CountdownFrame(
            headerPublisher = "1ST POLLS CLOSE".asOneTimePublisher(),
            timePublisher = ZonedDateTime.of(2020, 11, 3, 23, 0, 0, 0, ZoneId.of("UTC")).asOneTimePublisher(),
            labelFunc = { CountdownFrame.formatDDHHMMSS(it) },
            additionalInfoPublisher = "IN/KY".asOneTimePublisher(),
        )
        frame.clock = Clock.fixed(Instant.parse("2020-07-04T19:41:10Z"), ZoneId.of("UTC"))
        frame.setSize(200, 100)
        compareRendering("CountdownFrame", "AdditionalInfo", frame)
    }
}
