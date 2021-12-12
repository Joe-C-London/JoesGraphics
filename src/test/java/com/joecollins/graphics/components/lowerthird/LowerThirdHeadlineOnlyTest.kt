package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.components.lowerthird.LowerThird.Companion.createImage
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.io.IOException
import java.lang.InterruptedException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.Throws

class LowerThirdHeadlineOnlyTest {
    @Test
    fun testHeadline() {
        val lowerThird = LowerThirdHeadlineOnly(
            leftImageBinding = fixedBinding(createImage("", Color.RED, Color.WHITE)),
            placeBinding = fixedBinding(""),
            timezoneBinding = fixedBinding(ZoneId.systemDefault()),
            headlineBinding = fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"),
            subheadBinding = fixedBinding("Polls open for 30 minutes on west coast")
        )
        Assert.assertEquals("POLLS CLOSE ACROSS CENTRAL CANADA", lowerThird.headline)
    }

    @Test
    fun testSubhead() {
        val lowerThird = LowerThirdHeadlineOnly(
            leftImageBinding = fixedBinding(createImage("", Color.RED, Color.WHITE)),
            placeBinding = fixedBinding(""),
            timezoneBinding = fixedBinding(ZoneId.systemDefault()),
            headlineBinding = fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"),
            subheadBinding = fixedBinding("Polls open for 30 minutes on west coast")
        )
        Assert.assertEquals("Polls open for 30 minutes on west coast", lowerThird.subhead)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testRenderHeadlineSubhead() {
        val lowerThird = LowerThirdHeadlineOnly(
            leftImageBinding = fixedBinding(createImage("BREAKING NEWS", Color.WHITE, Color.RED)),
            placeBinding = fixedBinding("OTTAWA"),
            timezoneBinding = fixedBinding(ZoneId.of("Canada/Eastern")),
            headlineBinding = fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"),
            subheadBinding = fixedBinding("Polls open for 30 minutes on west coast"),
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
            leftImageBinding = fixedBinding(createImage("BREAKING NEWS", Color.WHITE, Color.RED)),
            placeBinding = fixedBinding("OTTAWA"),
            timezoneBinding = fixedBinding(ZoneId.of("Canada/Eastern")),
            headlineBinding = fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"),
            subheadBinding = fixedBinding(null),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineOnly", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderHeadlineSubheadAccents() {
        val lowerThird = LowerThirdHeadlineOnly(
            leftImageBinding = fixedBinding(
                createImage("\u00c9LECTION FRAN\u00c7AIS", Color.WHITE, Color.RED)
            ),
            placeBinding = fixedBinding("SAINT-\u00c9TIENNE"),
            timezoneBinding = fixedBinding(ZoneId.of("Europe/Paris")),
            headlineBinding = fixedBinding("\u00c9LECTION FRAN\u00c7AIS EST FINI"),
            subheadBinding = fixedBinding("\u00c9lection fran\u00e7ais est s\u00fbr"),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSubheadAccents", lowerThird)
    }
}
