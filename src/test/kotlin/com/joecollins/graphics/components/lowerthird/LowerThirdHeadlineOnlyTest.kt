package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.components.lowerthird.LowerThird.Companion.createImage
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Test
import java.awt.Color
import java.io.IOException
import java.lang.InterruptedException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.Throws

class LowerThirdHeadlineOnlyTest {
    @Test
    fun testHeadline() {
        val lowerThird = LowerThirdHeadlineOnly(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = "".asOneTimePublisher(),
            timezonePublisher = ZoneId.systemDefault().asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.headline }, IsEqual("POLLS CLOSE ACROSS CENTRAL CANADA"))
    }

    @Test
    fun testSubhead() {
        val lowerThird = LowerThirdHeadlineOnly(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = "".asOneTimePublisher(),
            timezonePublisher = ZoneId.systemDefault().asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.subhead }, IsEqual("Polls open for 30 minutes on west coast"))
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testRenderHeadlineSubhead() {
        val lowerThird = LowerThirdHeadlineOnly(
            leftImagePublisher = createImage("BREAKING NEWS", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = "OTTAWA".asOneTimePublisher(),
            timezonePublisher = ZoneId.of("Canada/Eastern").asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        Thread.sleep(100)
        compareRendering("LowerThird", "HeadlineAndSubhead", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderHeadlineOnly() {
        val lowerThird = LowerThirdHeadlineOnly(
            leftImagePublisher = createImage("BREAKING NEWS", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = "OTTAWA".asOneTimePublisher(),
            timezonePublisher = ZoneId.of("Canada/Eastern").asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = null.asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineOnly", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderHeadlineSubheadAccents() {
        val lowerThird = LowerThirdHeadlineOnly(
            leftImagePublisher =
            createImage("\u00c9LECTION FRAN\u00c7AIS", Color.WHITE, Color.RED)
                .asOneTimePublisher(),
            placePublisher = "SAINT-\u00c9TIENNE".asOneTimePublisher(),
            timezonePublisher = ZoneId.of("Europe/Paris").asOneTimePublisher(),
            headlinePublisher = "\u00c9LECTION FRAN\u00c7AIS EST FINI".asOneTimePublisher(),
            subheadPublisher = "\u00c9lection fran\u00e7ais est s\u00fbr".asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSubheadAccents", lowerThird)
    }
}
