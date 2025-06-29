package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.ShapefileReader.readShapes
import com.joecollins.pubsub.map
import java.awt.Shape
import java.awt.geom.Area

class SimpleVoteViewPanelTest {

    companion object {
        fun peiShapesByDistrict(): Map<Int, Shape> {
            val peiMap = SimpleVoteViewPanelTest::class.java
                .classLoader
                .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
            return readShapes(peiMap, "DIST_NO", Int::class.java)
        }

        fun peiShapesByRegion(): Map<String, Shape> {
            val keys = mapOf(
                "Cardigan" to setOf(4, 2, 5, 3, 7, 1, 6),
                "Malpeque" to setOf(19, 15, 16, 20, 17, 18, 8),
                "Charlottetown" to setOf(11, 13, 9, 12, 14, 10),
                "Egmont" to setOf(26, 24, 25, 22, 21, 27, 23),
            )
            val shapesByDistrict = peiShapesByDistrict()
            return keys.mapValues { e ->
                e.value.map { shapesByDistrict[it]!! }
                    .reduce { acc, shape ->
                        val ret = Area(acc)
                        ret.add(Area(shape))
                        ret
                    }
            }
        }
    }
}
