package com.joecollins.graphics.components

import com.joecollins.graphics.components.ListingFrameBuilder.Companion.of
import com.joecollins.graphics.components.ListingFrameBuilder.Companion.ofFixedList
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.util.concurrent.TimeUnit

class ListingFrameBuilderTest {
    @Test
    fun testBasicListingFrameWithListBinding() {
        val list = listOf(
            Triple("JUSTIN TRUDEAU", Color.RED, "LIBERAL"),
            Triple("ANDREW SCHEER", Color.BLUE, "CONSERVATIVE")
        )
        val frame: BarFrame = of(list.asOneTimePublisher(), { it.first }, { it.third }) { it.second }
            .withHeader("HEADER".asOneTimePublisher())
            .withSubhead("SUBHEAD".asOneTimePublisher())
            .build()
        Assert.assertEquals(0, frame.numLines.toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(2))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toDouble() }, IsEqual(1.0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toDouble() }, IsEqual(0.0))
        Assert.assertEquals("HEADER", frame.header)
        Assert.assertEquals("SUBHEAD", frame.subheadText)
        Assert.assertEquals("JUSTIN TRUDEAU", frame.getLeftText(0))
        Assert.assertEquals("LIBERAL", frame.getRightText(0))
        Assert.assertEquals(1, frame.getSeries(0).size.toLong())
        Assert.assertEquals(Color.RED, frame.getSeries(0)[0].first)
        Assert.assertEquals(1.0, frame.getSeries(0)[0].second.toDouble(), 1e-6)
        Assert.assertEquals("ANDREW SCHEER", frame.getLeftText(1))
        Assert.assertEquals("CONSERVATIVE", frame.getRightText(1))
        Assert.assertEquals(1, frame.getSeries(1).size.toLong())
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[0].first)
        Assert.assertEquals(1.0, frame.getSeries(1)[0].second.toDouble(), 1e-6)
    }

    @Test
    fun testBasicFixedListFrame() {
        val list = listOf(
            Triple(
                Publisher("JUSTIN TRUDEAU"),
                Publisher(Color.RED),
                Publisher("LIBERAL")
            ),
            Triple(
                Publisher("ANDREW SCHEER"),
                Publisher(Color.BLUE),
                Publisher("CONSERVATIVE")
            )
        )
        val frame = ofFixedList(list, { it.first }, { it.third }) { it.second }
            .withHeader("HEADER".asOneTimePublisher())
            .withSubhead("SUBHEAD".asOneTimePublisher())
            .build()
        Assert.assertEquals(0, frame.numLines.toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(2))
        Assert.assertEquals(0.0, frame.min.toDouble(), 1e-6)
        Assert.assertEquals(1.0, frame.max.toDouble(), 1e-6)
        Assert.assertEquals("HEADER", frame.header)
        Assert.assertEquals("SUBHEAD", frame.subheadText)
        Assert.assertEquals("JUSTIN TRUDEAU", frame.getLeftText(0))
        Assert.assertEquals("LIBERAL", frame.getRightText(0))
        Assert.assertEquals(1, frame.getSeries(0).size.toLong())
        Assert.assertEquals(Color.RED, frame.getSeries(0)[0].first)
        Assert.assertEquals(1.0, frame.getSeries(0)[0].second.toDouble(), 1e-6)
        Assert.assertEquals("ANDREW SCHEER", frame.getLeftText(1))
        Assert.assertEquals("CONSERVATIVE", frame.getRightText(1))
        Assert.assertEquals(1, frame.getSeries(1).size.toLong())
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[0].first)
        Assert.assertEquals(1.0, frame.getSeries(1)[0].second.toDouble(), 1e-6)
    }
}
