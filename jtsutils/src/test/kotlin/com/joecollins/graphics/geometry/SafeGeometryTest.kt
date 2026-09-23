package com.joecollins.graphics.geometry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Envelope
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.impl.CoordinateArraySequence
import org.locationtech.jts.geom.util.AffineTransformation
import java.awt.geom.Rectangle2D
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CyclicBarrier

class SafeGeometryTest {

    private val gf = GeometryFactory()

    private fun ringCoordinates(x: Double, y: Double, size: Double) = arrayOf(
        Coordinate(x, y),
        Coordinate(x + size, y),
        Coordinate(x + size, y + size),
        Coordinate(x, y + size),
        Coordinate(x, y),
    )

    private fun ring(x: Double, y: Double, size: Double) = gf.createLinearRing(ringCoordinates(x, y, size))

    /** A polygon with a hole, so that the recursive warming has interior rings to reach. */
    private fun polygonWithHole(x: Double, y: Double) = gf.createPolygon(
        ring(x, y, 100.0),
        arrayOf(ring(x + 25.0, y + 25.0, 50.0)),
    )

    @Test
    fun testBoundsMatchTheGeometry() {
        val safe = SafeGeometry(polygonWithHole(10.0, 20.0))
        assertEquals(10.0, safe.minX)
        assertEquals(20.0, safe.minY)
        assertEquals(110.0, safe.maxX)
        assertEquals(120.0, safe.maxY)
        assertEquals(100.0, safe.width)
        assertEquals(100.0, safe.height)
    }

    @Test
    fun testEqualityIsExactNotTopological() {
        val square = SafeGeometry(
            gf.createPolygon(
                arrayOf(
                    Coordinate(0.0, 0.0),
                    Coordinate(10.0, 0.0),
                    Coordinate(10.0, 10.0),
                    Coordinate(0.0, 10.0),
                    Coordinate(0.0, 0.0),
                ),
            ),
        )
        val sameSquareExtraVertex = SafeGeometry(
            gf.createPolygon(
                arrayOf(
                    Coordinate(0.0, 0.0),
                    Coordinate(5.0, 0.0),
                    Coordinate(10.0, 0.0),
                    Coordinate(10.0, 10.0),
                    Coordinate(0.0, 10.0),
                    Coordinate(0.0, 0.0),
                ),
            ),
        )
        assertEquals(square.bounds, sameSquareExtraVertex.bounds, "same region")
        assertEquals(square.area, sameSquareExtraVertex.area, "same region")
        assertNotEquals(square, sameSquareExtraVertex, "but not the same coordinates")
    }

    @Test
    fun testEqualityIsGeometric() {
        val a = SafeGeometry(polygonWithHole(0.0, 0.0))
        val b = SafeGeometry(polygonWithHole(0.0, 0.0))
        val c = SafeGeometry(polygonWithHole(1.0, 0.0))
        assertEquals(a, b, "separately-wrapped identical geometries are equal")
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
        assertEquals("x", HashMap<SafeGeometry, String>().apply { put(a, "x") }[b])
    }

    @Test
    fun testBoundsCannotBeMutatedByCallers() {
        val safe = SafeGeometry(polygonWithHole(0.0, 0.0))
        val expanded = safe.bounds.expandToInclude(Bounds(-1000.0, -1000.0, -1000.0, -1000.0))
        assertNotSame(expanded, safe.bounds)
        assertEquals(-1000.0, expanded.minX)
        assertEquals(0.0, safe.bounds.minX, "expanding a returned box must not affect the wrapper")
    }

    /**
     * What a caller actually depends on: hand ONE geometry to a dozen threads and they must all
     * compute the same answer.  This is BC's loading pattern - every riding intersected against one
     * shared `land` on a pool.
     *
     * The shared instances must not be touched on this thread before the workers start: doing so
     * would both initialise them and establish a happens-before edge via `Thread.start()`, leaving
     * nothing to race.  The expected value is therefore computed from separate, equal instances.
     */
    @Test
    fun testConcurrentOperationsAgainstOneSharedGeometryAllAgree() {
        val threadCount = 12
        val expected = SafeGeometry(polygonWithHole(0.0, 0.0))
            .intersection(SafeGeometry(gf.createPolygon(ring(10.0, 0.0, 90.0))))
            .let { it.bounds to it.area }

        repeat(500) {
            // untouched on this thread - the workers are the first to use them
            val shared = SafeGeometry(polygonWithHole(0.0, 0.0))
            val cutter = SafeGeometry(gf.createPolygon(ring(10.0, 0.0, 90.0)))

            val barrier = CyclicBarrier(threadCount)
            val results = ConcurrentHashMap<Int, Pair<Bounds, Double>>()
            (0 until threadCount).map { i ->
                Thread.ofPlatform().start {
                    barrier.await()
                    val result = shared.intersection(cutter)
                    results[i] = result.bounds to result.area
                }
            }.forEach { it.join() }

            (0 until threadCount).forEach { i ->
                assertEquals(expected, results[i], "thread $i disagreed about the shared intersection")
            }
        }
    }

    @Test
    fun testIntersection() {
        val a = SafeGeometry(gf.createPolygon(ring(0.0, 0.0, 100.0)))
        val b = SafeGeometry(gf.createPolygon(ring(50.0, 50.0, 100.0)))
        val result = a.intersection(b)
        assertEquals(Bounds(50.0, 100.0, 50.0, 100.0), result.bounds)
        assertEquals(2500.0, result.area)
    }

    @Test
    fun testUnion() {
        val a = SafeGeometry(gf.createPolygon(ring(0.0, 0.0, 100.0)))
        val b = SafeGeometry(gf.createPolygon(ring(50.0, 50.0, 100.0)))
        val result = a.union(b)
        assertEquals(Bounds(0.0, 150.0, 0.0, 150.0), result.bounds)
        assertEquals(10000.0 + 10000.0 - 2500.0, result.area)
    }

    @Test
    fun testDifference() {
        val a = SafeGeometry(gf.createPolygon(ring(0.0, 0.0, 100.0)))
        val b = SafeGeometry(gf.createPolygon(ring(50.0, 50.0, 100.0)))
        val result = a.difference(b)
        assertEquals(Bounds(0.0, 100.0, 0.0, 100.0), result.bounds)
        assertEquals(10000.0 - 2500.0, result.area)
    }

    @Test
    fun testUnionOfACollection() {
        val shapes = (0 until 4).map { SafeGeometry(gf.createPolygon(ring(it * 100.0, 0.0, 100.0))) }
        val result = SafeGeometry.union(shapes)
        assertEquals(Bounds(0.0, 400.0, 0.0, 100.0), result.bounds)
        assertEquals(40000.0, result.area)
    }

    @Test
    fun testTransform() {
        val original = SafeGeometry(gf.createPolygon(ring(0.0, 0.0, 100.0)))
        val moved = original.transform(AffineTransformation.translationInstance(10.0, -5.0))
        assertEquals(Bounds(10.0, 110.0, -5.0, 95.0), moved.bounds)
        assertEquals(Bounds(0.0, 100.0, 0.0, 100.0), original.bounds, "the original is untouched")
    }

    @Test
    fun testToGeometryUsesThisGeometrysCoordinateSpace() {
        val shape = SafeGeometry(gf.createPolygon(ring(0.0, 0.0, 100.0)))
        val box = shape.toGeometry(Bounds(10.0, 40.0, 10.0, 40.0))
        assertEquals(Bounds(10.0, 40.0, 10.0, 40.0), box.bounds)
        assertEquals(900.0, box.area)
    }

    @Test
    fun testOfBuildsARectangle() {
        val box = SafeGeometry.of(Bounds(0.0, 800.0, 0.0, -500.0))
        assertEquals(Bounds(0.0, 800.0, -500.0, 0.0), box.bounds)
        assertEquals(400000.0, box.area)
    }

    @Test
    fun testContainsAndIntersects() {
        val outer = SafeGeometry(gf.createPolygon(ring(0.0, 0.0, 100.0)))
        val inner = SafeGeometry(gf.createPolygon(ring(25.0, 25.0, 25.0)))
        val overlapping = SafeGeometry(gf.createPolygon(ring(50.0, 50.0, 100.0)))
        val disjoint = SafeGeometry(gf.createPolygon(ring(500.0, 500.0, 10.0)))
        assertTrue(outer.contains(inner))
        assertFalse(outer.contains(overlapping))
        assertTrue(outer.intersects(overlapping))
        assertFalse(outer.intersects(disjoint))
    }

    @Test
    fun testAreaAndIsEmpty() {
        assertEquals(10000.0 - 2500.0, SafeGeometry(polygonWithHole(0.0, 0.0)).area, "the hole is excluded")
        assertFalse(SafeGeometry(polygonWithHole(0.0, 0.0)).isEmpty)
        val disjoint = SafeGeometry(gf.createPolygon(ring(0.0, 0.0, 10.0)))
            .intersection(SafeGeometry(gf.createPolygon(ring(500.0, 500.0, 10.0))))
        assertTrue(disjoint.isEmpty)
    }

    /** AWT grows y downwards, so the shape is flipped relative to the geometry's own coordinates. */
    @Test
    fun testToAwtShapeIsYFlipped() {
        val safe = SafeGeometry(gf.createPolygon(ring(10.0, 20.0, 100.0)))
        assertEquals(Rectangle2D.Double(10.0, -120.0, 100.0, 100.0), safe.toAwtShape().bounds2D)
        assertEquals(safe.awtBounds(), safe.toAwtShape().bounds2D, "awtBounds() must agree with the shape")
    }
}
