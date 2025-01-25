package com.joecollins.graphics.utils

import org.geotools.api.feature.simple.SimpleFeature
import org.geotools.data.geojson.GeoJSONReader
import org.geotools.data.simple.SimpleFeatureIterator
import java.net.URL

object GeoJsonReader : GenericReader() {
    override fun getFeatureIterator(file: URL): SimpleFeatureIterator {
        val reader = GeoJSONReader(file)
        val iterator = reader.iterator
        return object : SimpleFeatureIterator {
            override fun close() {
                iterator.close()
                reader.close()
            }

            override fun hasNext(): Boolean = iterator.hasNext()

            override fun next(): SimpleFeature = iterator.next()
        }
    }

    override val geometryKey: String = "geometry"
}
