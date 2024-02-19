package com.joecollins.graphics.utils

import org.geotools.api.feature.simple.SimpleFeature
import org.geotools.data.simple.SimpleFeatureIterator
import org.locationtech.jts.awt.ShapeWriter
import org.locationtech.jts.geom.Geometry
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.net.URL
import kotlin.reflect.cast

abstract class GenericReader {

    fun <T> readShapes(file: URL, keyProperty: String, keyType: Class<T>): Map<T, Shape> {
        if (keyType == Int::class.java) {
            return readShapes(file) { feature ->
                @Suppress("UNCHECKED_CAST")
                Integer::class.cast(feature[keyProperty]).toInt() as T
            }
        }
        if (keyType == Long::class.java) {
            return readShapes(file) { feature ->
                @Suppress("UNCHECKED_CAST")
                Long::class.cast(feature[keyProperty]).toLong() as T
            }
        }
        if (keyType == Double::class.java) {
            return readShapes(file) { feature ->
                @Suppress("UNCHECKED_CAST")
                Double::class.cast(feature[keyProperty]).toDouble() as T
            }
        }
        return readShapes(file) { feature -> keyType.cast(feature[keyProperty]) }
    }

    fun <T> readShapes(file: URL, keyFunc: (Map<String, Any>) -> T): Map<T, Shape> {
        return readShapes(file, keyFunc) { true }
    }

    fun <T> readShapes(
        file: URL,
        keyFunc: (Map<String, Any>) -> T,
        filter: (Map<String, Any>) -> Boolean,
    ): Map<T, Shape> {
        val shapes: MutableMap<T, Collection<Shape>> = HashMap()
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
                shapes.merge(
                    key,
                    listOf(toShape(geom)),
                ) { s1, s2 ->
                    s1 + s2
                }
            }
            shapes.mapValues { (_, s) ->
                s.sortedBy { it.bounds2D.run { width * height } }
                    .reduce { s1, s2 ->
                        Area(s1).apply { add(Area(s2)) }
                    }
            }
        } finally {
            features?.close()
        }
    }

    protected abstract fun getFeatureIterator(file: URL): SimpleFeatureIterator

    protected abstract val geometryKey: String

    private fun toShape(geom: Geometry): Shape {
        val shapeWriter = ShapeWriter()
        val transform = AffineTransform.getScaleInstance(1.0, -1.0)
        return transform.createTransformedShape(shapeWriter.toShape(geom))
    }

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

        override fun isEmpty(): Boolean {
            return feature.attributes.isEmpty()
        }

        override fun get(key: String): Any? {
            return feature.getAttribute(key)
        }

        override fun containsValue(value: Any): Boolean {
            return values.contains(value)
        }

        override fun containsKey(key: String): Boolean {
            return keys.contains(key)
        }
    }
}
