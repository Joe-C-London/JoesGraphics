package com.joecollins.graphics.utils

import org.geotools.api.feature.simple.SimpleFeature
import org.geotools.data.simple.SimpleFeatureIterator
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.geom.util.GeometryFixer
import org.locationtech.jts.operation.overlayng.OverlayNGRobust
import java.net.URL
import kotlin.reflect.cast

abstract class GenericReader {

    fun <T> readShapes(file: URL, keyProperty: String, keyType: Class<T>): Map<T, Geometry> {
        if (keyType == Int::class.java) {
            return readShapes(file) { feature ->
                @Suppress("UNCHECKED_CAST")
                Int::class.cast(feature[keyProperty]) as T
            }
        }
        if (keyType == Long::class.java) {
            return readShapes(file) { feature ->
                @Suppress("UNCHECKED_CAST")
                Long::class.cast(feature[keyProperty]) as T
            }
        }
        if (keyType == Double::class.java) {
            return readShapes(file) { feature ->
                @Suppress("UNCHECKED_CAST")
                Double::class.cast(feature[keyProperty]) as T
            }
        }
        return readShapes(file) { feature -> keyType.cast(feature[keyProperty]) }
    }

    fun <T> readShapes(file: URL, keyFunc: (Map<String, Any>) -> T): Map<T, Geometry> = readShapes(file, keyFunc) { true }

    fun <T> readShapes(
        file: URL,
        keyFunc: (Map<String, Any>) -> T,
        filter: (Map<String, Any>) -> Boolean,
    ): Map<T, Geometry> {
        val geometries: MutableMap<T, MutableList<Geometry>> = HashMap()
        var features: SimpleFeatureIterator? = null
        return try {
            features = getFeatureIterator(file)
            while (features.hasNext()) {
                val feature = features.next()
                val map = WrappedMap(feature)
                if (!filter(map)) {
                    continue
                }
                val key = keyFunc(map)
                val geom = feature.getAttribute(geometryKey) as Geometry
                geometries.getOrPut(key) { mutableListOf() }.add(geom.let { if (it.isValid) it else GeometryFixer.fix(it) })
            }
            geometries.mapValues { (_, geoms) -> OverlayNGRobust.union(geoms).also(this::warmEnvelopes) }
        } finally {
            features?.close()
        }
    }

    protected abstract fun getFeatureIterator(file: URL): SimpleFeatureIterator

    protected abstract val geometryKey: String

    private class WrappedMap(private val feature: SimpleFeature) : Map<String, Any> {
        override val entries: Set<Map.Entry<String, Any>>
            get() = keys.map {
                object : Map.Entry<String, Any> {
                    override val key: String = it
                    override val value: Any = get(it)!!
                }
            }.toSet()
        override val keys: Set<String>
            get() = feature.attributes.map { it.toString() }.toSet()
        override val size: Int
            get() = feature.attributes.size
        override val values: Collection<Any>
            get() = keys.map { feature.getAttribute(it) }

        override fun isEmpty(): Boolean = feature.attributes.isEmpty()

        override fun get(key: String): Any? = feature.getAttribute(key)

        override fun containsValue(value: Any): Boolean = values.contains(value)

        override fun containsKey(key: String): Boolean = keys.contains(key)
    }

    /**
     * JTS computes [Geometry.getEnvelopeInternal] lazily into a non-volatile field, so the first
     * caller unsafely publishes the [org.locationtech.jts.geom.Envelope]: a concurrent reader can see
     * the reference before the coordinate writes and read the default 0.0s.  Since
     * `Envelope.isNull()` is `maxx < minx`, an all-zero read is indistinguishable from a real box at
     * the origin, and silently drags any envelope expanded to include it out to (0, 0).
     *
     * Forcing the computation here means every envelope is written on the loading thread, before the
     * shapes are published, and is only ever read afterwards.
     *
     * The recursion is needed because computing a geometry's own envelope only warms the shell chain:
     * a polygon defers to its exterior ring and a collection to its children, so interior rings would
     * otherwise stay uninitialised.
     */
    private fun warmEnvelopes(geometry: Geometry) {
        geometry.envelopeInternal
        when (geometry) {
            is Polygon -> {
                warmEnvelopes(geometry.exteriorRing)
                repeat(geometry.numInteriorRing) { warmEnvelopes(geometry.getInteriorRingN(it)) }
            }

            is GeometryCollection -> repeat(geometry.numGeometries) { warmEnvelopes(geometry.getGeometryN(it)) }
        }
    }
}
