package com.joecollins.graphics.utils

import org.geotools.api.feature.simple.SimpleFeature
import org.geotools.data.shapefile.ShapefileDataStore
import org.geotools.data.simple.SimpleFeatureIterator
import java.net.URL

object ShapefileReader : GenericReader() {

    override fun getFeatureIterator(file: URL): SimpleFeatureIterator {
        val store = ShapefileDataStore(file)
        val featureSource = store.featureSource
        val features = featureSource.features.features()
        return object : SimpleFeatureIterator {
            override fun close() {
                features.close()
                store.dispose()
            }

            override fun hasNext(): Boolean {
                return features.hasNext()
            }

            override fun next(): SimpleFeature {
                return features.next()
            }
        }
    }

    override val geometryKey: String = "the_geom"
}
