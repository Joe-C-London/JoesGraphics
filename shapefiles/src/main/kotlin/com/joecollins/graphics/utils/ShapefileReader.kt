package com.joecollins.graphics.utils

import org.geotools.data.FileDataStore
import org.geotools.data.FileDataStoreFinder
import org.geotools.data.simple.SimpleFeatureIterator
import org.geotools.data.simple.SimpleFeatureSource
import org.locationtech.jts.awt.ShapeWriter
import org.locationtech.jts.geom.Geometry
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.net.URL
import kotlin.reflect.cast

object ShapefileReader {

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

    fun <T> readShapes(file: URL, keyFunc: (org.opengis.feature.simple.SimpleFeature) -> T): Map<T, Shape> {
        return readShapes(file, keyFunc) { true }
    }

    fun <T> readShapes(
        file: URL,
        keyFunc: (org.opengis.feature.simple.SimpleFeature) -> T,
        filter: (org.opengis.feature.simple.SimpleFeature) -> Boolean,
    ): Map<T, Shape> {
        val shapes: MutableMap<T, Shape> = HashMap()
        var store: FileDataStore? = null
        val featureSource: SimpleFeatureSource
        var features: SimpleFeatureIterator? = null
        return try {
            store = FileDataStoreFinder.getDataStore(file)
            featureSource = store.featureSource
            features = featureSource.features.features()
            while (features.hasNext()) {
                val feature = features.next()
                if (!filter(feature)) {
                    continue
                }
                val key = keyFunc(feature)
                val geom = feature.getAttribute("the_geom") as org.locationtech.jts.geom.Geometry
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
            store?.dispose()
        }
    }

    private fun toShape(geom: Geometry): Shape {
        val shapeWriter = ShapeWriter()
        val transform = AffineTransform.getScaleInstance(1.0, -1.0)
        return transform.createTransformedShape(shapeWriter.toShape(geom))
    }
}
