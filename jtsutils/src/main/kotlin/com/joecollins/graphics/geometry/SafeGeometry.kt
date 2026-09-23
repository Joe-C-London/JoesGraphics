package com.joecollins.graphics.geometry

import org.locationtech.jts.awt.ShapeWriter
import org.locationtech.jts.geom.Envelope
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.geom.util.AffineTransformation
import org.locationtech.jts.operation.overlayng.OverlayNGRobust
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D

/**
 * A thread-safe view of a JTS [Geometry].
 *
 * JTS computes a geometry's bounding box lazily, into a field that is neither volatile nor final
 * (`Geometry.envelope`), with no synchronisation.  The first caller therefore publishes the
 * [Envelope] unsafely: a concurrent reader can see the reference before the coordinate writes are
 * visible and read the default 0.0s.  `Envelope.isNull()` is `maxx < minx`, so such a read is not
 * rejected - it looks like a legitimate box at the origin, and expanding an envelope to include it
 * drags that envelope out to (0, 0).  `Geometry.hashCode()` is `getEnvelopeInternal().hashCode()`,
 * so even using a geometry as a hash key triggers the same lazy initialisation.
 *
 * This class computes the bounding box once, in the constructor, while holding the geometry's
 * monitor.  Because [geometry] and [envelope] are `val`s assigned before the constructor completes
 * and never mutated afterwards, any thread that sees a fully-constructed `SafeGeometry` also sees a
 * fully-initialised envelope - including the one cached inside the wrapped geometry (JLS 17.5).
 * Reads therefore need no locking at all.
 *
 * Two invariants keep that guarantee:
 *  - the wrapped geometry must never be mutated, and `Geometry.geometryChanged()` must never be
 *    called on it, as that resets the cached envelope to null;
 *  - equality is identity-based.  This must not become a `data class`: the generated `hashCode()`
 *    would delegate to `Geometry.hashCode()` and reintroduce the unsynchronised read.
 */
class SafeGeometry(private val geometry: Geometry) {

    /** The bounding box.  Immutable, so it can be handed out and shared without copying. */
    val bounds: Bounds = synchronized(geometry) {
        geometry.warmEnvelopes()
        Bounds.of(geometry.envelopeInternal)
    }

    val minX: Double get() = bounds.minX
    val maxX: Double get() = bounds.maxX
    val minY: Double get() = bounds.minY
    val maxY: Double get() = bounds.maxY
    val width: Double get() = bounds.width
    val height: Double get() = bounds.height

    /**
     * The bounding box in AWT space, where y grows downwards - matching [toAwtShape].
     */
    fun awtBounds(): Rectangle2D = bounds.toAwtRect()

    /**
     * The geometry as an AWT shape, flipped so that y grows downwards.
     *
     * Verified safe to compute off the wrapped geometry: `ShapeWriter` walks the coordinate
     * sequences and does not touch the lazily-cached envelope.
     */
    fun toAwtShape(): Shape = Y_FLIP.createTransformedShape(ShapeWriter().toShape(geometry))

    fun intersection(other: SafeGeometry): SafeGeometry = SafeGeometry(geometry.intersection(other.geometry))

    fun union(other: SafeGeometry): SafeGeometry = SafeGeometry(geometry.union(other.geometry))

    fun difference(other: SafeGeometry): SafeGeometry = SafeGeometry(geometry.difference(other.geometry))

    fun transform(transformation: AffineTransformation): SafeGeometry = SafeGeometry(transformation.transform(geometry))

    /** A rectangle in this geometry's coordinate space, built by this geometry's factory. */
    fun toGeometry(bounds: Bounds): SafeGeometry = SafeGeometry(geometry.factory.toGeometry(bounds.toEnvelope()))

    fun contains(other: SafeGeometry): Boolean = geometry.contains(other.geometry)

    fun intersects(other: SafeGeometry): Boolean = geometry.intersects(other.geometry)

    val area: Double get() = geometry.area

    val isEmpty: Boolean get() = geometry.isEmpty

    override fun equals(other: Any?): Boolean = this === other || (other is SafeGeometry && geometry == other.geometry)

    override fun hashCode(): Int = geometry.hashCode()

    override fun toString(): String = "SafeGeometry(${geometry.geometryType}, $bounds)"

    companion object {
        private val Y_FLIP = AffineTransform.getScaleInstance(1.0, -1.0)

        /** A rectangle covering [bounds], in whatever coordinate space the caller is using. */
        fun of(bounds: Bounds): SafeGeometry = SafeGeometry(GeometryFactory().toGeometry(bounds.toEnvelope()))

        fun union(geometries: Collection<SafeGeometry>): SafeGeometry = SafeGeometry(OverlayNGRobust.union(geometries.map { it.geometry }))

        /**
         * Forces JTS to compute the cached envelope of this geometry and of every component.
         *
         * This is not for [bounds]' sake - that is captured once here and read from the immutable
         * copy thereafter.  It is for JTS itself: [intersection], [union], [difference], [contains]
         * and [intersects] all consult the component envelopes of their inputs, so without this a
         * shared geometry's rings would be first-touched inside those calls, concurrently, by
         * whichever threads happened to be running them.  That is the original unsafe publication
         * one level down, and worse: an overlay derives its clipping envelope from the inputs'
         * envelopes (`OverlayUtil.resultEnvelope`) and clips to it, so a torn read there truncates
         * the result rather than merely mis-scaling a map.
         *
         * The recursion is required: computing a geometry's own envelope only warms the shell
         * chain, because a polygon defers to its exterior ring and a collection to its children.
         * Loading the PEI shapefile leaves 79 of 600 components cold without it, 20 of them
         * interior rings.
         */
        private fun Geometry.warmEnvelopes() {
            envelopeInternal
            when (this) {
                is Polygon -> {
                    exteriorRing.warmEnvelopes()
                    repeat(numInteriorRing) { getInteriorRingN(it).warmEnvelopes() }
                }

                is GeometryCollection -> repeat(numGeometries) { getGeometryN(it).warmEnvelopes() }

                else -> Unit
            }
        }
    }
}
