package com.joecollins.graphics.geometry

import org.locationtech.jts.geom.Envelope
import java.awt.geom.Rectangle2D
import kotlin.math.max
import kotlin.math.min

/**
 * An immutable bounding box.
 *
 * JTS's own [Envelope] is mutable - `expandToInclude` and `init` rewrite it in place - so handing one
 * out means either copying defensively at every boundary or trusting callers not to mutate shared
 * state.  This type removes the choice: every operation returns a new instance, so a box can be
 * shared freely across threads and screens.
 *
 * The constructor takes its arguments in the same order as [Envelope] - `(x1, x2, y1, y2)` - and
 * normalises them the same way, so either argument of a pair may be the smaller one.
 */
class Bounds(x1: Double, x2: Double, y1: Double, y2: Double) {

    val minX: Double = min(x1, x2)
    val maxX: Double = max(x1, x2)
    val minY: Double = min(y1, y2)
    val maxY: Double = max(y1, y2)

    val width: Double get() = maxX - minX
    val height: Double get() = maxY - minY

    /** This box grown to cover [other].  Neither input is modified. */
    fun expandToInclude(other: Bounds): Bounds = Bounds(
        min(minX, other.minX),
        max(maxX, other.maxX),
        min(minY, other.minY),
        max(maxY, other.maxY),
    )

    fun contains(other: Bounds): Boolean = other.minX >= minX && other.maxX <= maxX && other.minY >= minY && other.maxY <= maxY

    fun intersects(other: Bounds): Boolean = other.minX <= maxX && other.maxX >= minX && other.minY <= maxY && other.maxY >= minY

    /**
     * This box in AWT space, where y grows downwards - matching [SafeGeometry.toAwtShape].
     *
     * A fresh [Rectangle2D] each call, as that type is mutable too.
     */
    fun toAwtRect(): Rectangle2D = Rectangle2D.Double(minX, -maxY, width, height)

    internal fun toEnvelope(): Envelope = Envelope(minX, maxX, minY, maxY)

    /**
     * Value equality over the normalised edges.
     *
     * Deliberately hand-written rather than a `data class`: the constructor parameters are the
     * un-normalised `x1..y2`, so generated equality would distinguish boxes that describe the same
     * rectangle.
     */
    override fun equals(other: Any?): Boolean = this === other ||
        (other is Bounds && minX == other.minX && maxX == other.maxX && minY == other.minY && maxY == other.maxY)

    override fun hashCode(): Int {
        var result = minX.hashCode()
        result = 31 * result + maxX.hashCode()
        result = 31 * result + minY.hashCode()
        result = 31 * result + maxY.hashCode()
        return result
    }

    override fun toString(): String = "Bounds[$minX : $maxX, $minY : $maxY]"

    companion object {
        internal fun of(envelope: Envelope): Bounds = Bounds(envelope.minX, envelope.maxX, envelope.minY, envelope.maxY)
    }
}
