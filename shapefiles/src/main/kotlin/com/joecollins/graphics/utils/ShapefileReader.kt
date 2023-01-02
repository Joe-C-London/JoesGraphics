package com.joecollins.graphics.utils

import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.net.URL
import kotlin.reflect.cast

object ShapefileReader {

    fun <T> readShapes(file: URL, keyProperty: String, keyType: Class<T>, reducePrecision: Boolean = false): Map<T, Shape> {
        if (keyType == Int::class.java) {
            return readShapes(file, { feature ->
                @Suppress("UNCHECKED_CAST")
                Integer::class.cast(feature.getAttribute(keyProperty)).toInt() as T
            }, reducePrecision)
        }
        if (keyType == Long::class.java) {
            return readShapes(file, { feature ->
                @Suppress("UNCHECKED_CAST")
                Long::class.cast(feature.getAttribute(keyProperty)).toLong() as T
            }, reducePrecision)
        }
        if (keyType == Double::class.java) {
            return readShapes(file, { feature ->
                @Suppress("UNCHECKED_CAST")
                Double::class.cast(feature.getAttribute(keyProperty)).toDouble() as T
            }, reducePrecision)
        }
        return readShapes(file, { feature -> keyType.cast(feature.getAttribute(keyProperty)) }, reducePrecision)
    }

    fun <T> readShapes(file: URL, keyFunc: (org.opengis.feature.simple.SimpleFeature) -> T, reducePrecision: Boolean = false): Map<T, Shape> {
        return readShapes(file, keyFunc, { true }, reducePrecision)
    }

    fun <T> readShapes(
        file: URL,
        keyFunc: (org.opengis.feature.simple.SimpleFeature) -> T,
        filter: (org.opengis.feature.simple.SimpleFeature) -> Boolean,
        reducePrecision: Boolean = false,
    ): Map<T, Shape> {
        val shapes: MutableMap<T, Shape> = HashMap()
        var store: org.geotools.data.FileDataStore? = null
        val featureSource: org.geotools.data.simple.SimpleFeatureSource
        var features: org.geotools.data.simple.SimpleFeatureIterator? = null
        return try {
            store = org.geotools.data.FileDataStoreFinder.getDataStore(file)
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
                    toShape(geom, reducePrecision),
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

    private fun toShape(geom: org.locationtech.jts.geom.Geometry, reducePrecision: Boolean): Shape {
        val shapeWriter = org.locationtech.jts.awt.ShapeWriter()
        val pm = org.locationtech.jts.geom.PrecisionModel(org.locationtech.jts.geom.PrecisionModel.FIXED)
        val transform = AffineTransform.getScaleInstance(1.0, -1.0)
        val g = if (reducePrecision) org.locationtech.jts.precision.GeometryPrecisionReducer.reduce(geom, pm) else geom
        return transform.createTransformedShape(shapeWriter.toShape(g))
    }
}
