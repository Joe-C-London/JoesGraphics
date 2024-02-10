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
                Integer::class.cast(feature.getAttribute(keyProperty)).toInt() as T
            }
        }
        if (keyType == Long::class.java) {
            return readShapes(file) { feature ->
                @Suppress("UNCHECKED_CAST")
                Long::class.cast(feature.getAttribute(keyProperty)).toLong() as T
            }
        }
        if (keyType == Double::class.java) {
            return readShapes(file) { feature ->
                @Suppress("UNCHECKED_CAST")
                Double::class.cast(feature.getAttribute(keyProperty)).toDouble() as T
            }
        }
        return readShapes(file) { feature -> keyType.cast(feature.getAttribute(keyProperty)) }
    }

    fun <T> readShapes(file: URL, keyFunc: (SimpleFeature) -> T): Map<T, Shape> {
        return readShapes(file, keyFunc) { true }
    }

    fun <T> readShapes(
        file: URL,
        keyFunc: (SimpleFeature) -> T,
        filter: (SimpleFeature) -> Boolean,
    ): Map<T, Shape> {
        val shapes: MutableMap<T, Shape> = HashMap()
        var features: SimpleFeatureIterator? = null
        return try {
            features = getFeatureIterator(file)
            while (features.hasNext()) {
                val feature = features.next()
                if (!filter(feature)) {
                    continue
                }
                val key = keyFunc(feature)
                val geom = feature.getAttribute(geometryKey) as Geometry
                shapes.merge(
                    key,
                    toShape(geom),
                ) { s1, s2 ->
                    val s = Area(s1)
                    s.add(Area(s2))
                    s
                }
            }
            shapes
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
}
