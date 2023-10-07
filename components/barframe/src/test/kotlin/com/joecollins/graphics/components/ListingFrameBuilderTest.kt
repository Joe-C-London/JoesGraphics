package com.joecollins.graphics.components

import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color

class ListingFrameBuilderTest {
    @Test
    fun testBasicListingFrameWithListBinding() {
        val list = listOf(
            Triple("JUSTIN TRUDEAU", Color.RED, "LIBERAL"),
            Triple("ANDREW SCHEER", Color.BLUE, "CONSERVATIVE"),
        )
        val frame: BarFrame = ListingFrameBuilder.of(
            list.asOneTimePublisher(),
            { first },
            { third },
            { second },
            header = "HEADER".asOneTimePublisher(),
            subhead = "SUBHEAD".asOneTimePublisher(),
        )
        assertEquals(0, frame.numLines.toLong())
        assertEquals(2, frame.numBars)
        assertEquals(1.0, frame.max.toDouble())
        assertEquals(0.0, frame.min.toDouble())
        assertEquals("HEADER", frame.header)
        assertEquals("SUBHEAD", frame.subheadText)
        assertEquals("JUSTIN TRUDEAU", frame.getLeftText(0))
        assertEquals("LIBERAL", frame.getRightText(0))
        assertEquals(1, frame.getSeries(0).size.toLong())
        assertEquals(Color.RED, frame.getSeries(0)[0].first)
        assertEquals(1.0, frame.getSeries(0)[0].second.toDouble(), 1e-6)
        assertEquals("ANDREW SCHEER", frame.getLeftText(1))
        assertEquals("CONSERVATIVE", frame.getRightText(1))
        assertEquals(1, frame.getSeries(1).size.toLong())
        assertEquals(Color.BLUE, frame.getSeries(1)[0].first)
        assertEquals(1.0, frame.getSeries(1)[0].second.toDouble(), 1e-6)
    }

    @Test
    fun testBasicFixedListFrame() {
        val list = listOf(
            Triple(
                Publisher("JUSTIN TRUDEAU"),
                Publisher(Color.RED),
                Publisher("LIBERAL"),
            ),
            Triple(
                Publisher("ANDREW SCHEER"),
                Publisher(Color.BLUE),
                Publisher("CONSERVATIVE"),
            ),
        )
        val frame = ListingFrameBuilder.of(
            list,
            { first },
            { third },
            { second },
            header = "HEADER".asOneTimePublisher(),
            subhead = "SUBHEAD".asOneTimePublisher(),
        )
        assertEquals(0, frame.numLines.toLong())
        assertEquals(2, frame.numBars)
        assertEquals(0.0, frame.min.toDouble(), 1e-6)
        assertEquals(1.0, frame.max.toDouble(), 1e-6)
        assertEquals("HEADER", frame.header)
        assertEquals("SUBHEAD", frame.subheadText)
        assertEquals("JUSTIN TRUDEAU", frame.getLeftText(0))
        assertEquals("LIBERAL", frame.getRightText(0))
        assertEquals(1, frame.getSeries(0).size.toLong())
        assertEquals(Color.RED, frame.getSeries(0)[0].first)
        assertEquals(1.0, frame.getSeries(0)[0].second.toDouble(), 1e-6)
        assertEquals("ANDREW SCHEER", frame.getLeftText(1))
        assertEquals("CONSERVATIVE", frame.getRightText(1))
        assertEquals(1, frame.getSeries(1).size.toLong())
        assertEquals(Color.BLUE, frame.getSeries(1)[0].first)
        assertEquals(1.0, frame.getSeries(1)[0].second.toDouble(), 1e-6)
    }
}
