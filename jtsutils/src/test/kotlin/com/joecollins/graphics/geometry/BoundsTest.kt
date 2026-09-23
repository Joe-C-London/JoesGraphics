package com.joecollins.graphics.geometry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.geom.Rectangle2D

class BoundsTest {

    /**
     * The constructor mirrors JTS's `Envelope(x1, x2, y1, y2)`, including its normalisation, so that
     * call sites converted from `Envelope` keep behaving identically.  NZ's Countdown, for one,
     * passes its y arguments largest-first.
     */
    @Test
    fun testArgumentsAreNormalisedLikeEnvelope() {
        val reversed = Bounds(0.0, 2_200_000.0, 10_000_000.0, 0.0)
        assertEquals(0.0, reversed.minX)
        assertEquals(2_200_000.0, reversed.maxX)
        assertEquals(0.0, reversed.minY)
        assertEquals(10_000_000.0, reversed.maxY)
        assertEquals(Bounds(2_200_000.0, 0.0, 0.0, 10_000_000.0), reversed)
    }

    @Test
    fun testExpandToIncludeReturnsANewBoxAndLeavesBothInputsAlone() {
        val a = Bounds(0.0, 10.0, 0.0, 10.0)
        val b = Bounds(-5.0, 2.0, 20.0, 25.0)
        val combined = a.expandToInclude(b)
        assertEquals(Bounds(-5.0, 10.0, 0.0, 25.0), combined)
        assertEquals(Bounds(0.0, 10.0, 0.0, 10.0), a)
        assertEquals(Bounds(-5.0, 2.0, 20.0, 25.0), b)
    }

    @Test
    fun testWidthAndHeight() {
        val bounds = Bounds(3.0, 11.0, 5.0, 20.0)
        assertEquals(8.0, bounds.width)
        assertEquals(15.0, bounds.height)
    }

    @Test
    fun testContainsAndIntersects() {
        val outer = Bounds(0.0, 10.0, 0.0, 10.0)
        val inner = Bounds(2.0, 4.0, 2.0, 4.0)
        val overlapping = Bounds(8.0, 20.0, 8.0, 20.0)
        val disjoint = Bounds(100.0, 110.0, 100.0, 110.0)
        assertTrue(outer.contains(inner))
        assertFalse(outer.contains(overlapping))
        assertTrue(outer.intersects(overlapping))
        assertFalse(outer.intersects(disjoint))
    }

    /** AWT space grows downwards, matching the y-flip applied by SafeGeometry.toAwtShape(). */
    @Test
    fun testToAwtRectFlipsY() {
        assertEquals(Rectangle2D.Double(3.0, -20.0, 8.0, 15.0), Bounds(3.0, 11.0, 5.0, 20.0).toAwtRect())
    }

    @Test
    fun testEqualityAndHashCodeAreByValue() {
        assertEquals(Bounds(1.0, 2.0, 3.0, 4.0), Bounds(1.0, 2.0, 3.0, 4.0))
        assertEquals(Bounds(1.0, 2.0, 3.0, 4.0).hashCode(), Bounds(2.0, 1.0, 4.0, 3.0).hashCode())
        assertFalse(Bounds(1.0, 2.0, 3.0, 4.0) == Bounds(1.0, 2.0, 3.0, 5.0))
    }
}
