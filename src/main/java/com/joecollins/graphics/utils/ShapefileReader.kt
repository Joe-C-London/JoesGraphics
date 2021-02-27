package com.joecollins.graphics.utils

import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.io.IOException
import java.net.URL
import java.util.HashMap
import kotlin.Throws
import org.geotools.data.FileDataStore
import org.geotools.data.FileDataStoreFinder
import org.geotools.data.simple.SimpleFeatureIterator
import org.geotools.data.simple.SimpleFeatureSource
import org.locationtech.jts.awt.ShapeWriter
import org.locationtech.jts.geom.Geometry
import org.opengis.feature.simple.SimpleFeature

object ShapefileReader {
    @Throws(IOException::class)
    @JvmStatic fun <T> readShapes(file: URL, keyProperty: String, keyType: Class<T>): Map<T, Shape> {
        return readShapes(file) { feature: SimpleFeature -> keyType.cast(feature.getAttribute(keyProperty)) }
    }

    @Throws(IOException::class)
    @JvmStatic fun <T> readShapes(file: URL, keyFunc: (SimpleFeature) -> T): Map<T, Shape> {
        return readShapes(file, keyFunc, { true })
    }

    @Throws(IOException::class)
    @JvmStatic fun <T> readShapes(
        file: URL,
        keyFunc: (SimpleFeature) -> T,
        filter: (SimpleFeature) -> Boolean
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
                val geom = feature.getAttribute("the_geom") as Geometry
                shapes.merge(
                        key,
                        toShape(geom),
                        { s1, s2 ->
                            val s = Area(s1)
                            s.add(Area(s2))
                            s
                        })
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
