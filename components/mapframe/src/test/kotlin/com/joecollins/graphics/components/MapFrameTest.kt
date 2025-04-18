package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Rectangle2D

class MapFrameTest {
    @Test
    fun testBindShapes() {
        val shapes = loadShapes { getDistrictColor(it) }
        val mapFrame = MapFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            shapesPublisher = shapes.map { Pair(it.shape, it.color) }.asOneTimePublisher(),
        )
        assertEquals(27, mapFrame.numShapes)
        assertEquals(shapes[0].shape, mapFrame.getShape(0))
        assertEquals(shapes[0].color, mapFrame.getColor(0))
    }

    @Test
    fun testDefaultFocusAreaEncompassesAllShapes() {
        val shapes = loadShapes { getDistrictColor(it) }
        val mapFrame = MapFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            shapesPublisher = shapes.map { Pair(it.shape, it.color) }.asOneTimePublisher(),
        )
        val bindingBox = shapes.asSequence()
            .map { Area(it.shape) }
            .reduce { acc, area ->
                val ret = Area(acc)
                ret.add(area)
                ret
            }
            .bounds2D
        assertEquals(bindingBox, mapFrame.focusBox)
    }

    @Test
    fun testFocusBox() {
        val shapes = loadShapes { getDistrictColor(it) }
        val cityBox = loadCityBox()
        val mapFrame = MapFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            shapesPublisher = shapes.map { Pair(it.shape, it.color) }.asOneTimePublisher(),
            focusBoxPublisher = cityBox.asOneTimePublisher(),
        )
        assertEquals(cityBox, mapFrame.focusBox)
    }

    @Test
    fun testOutlines() {
        val regions = loadRegions()
        val mapFrame = MapFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            shapesPublisher = regions.map { Pair(it, Color.BLACK) }.asOneTimePublisher(),
            outlineShapesPublisher = regions.asOneTimePublisher(),
        )
        assertEquals(4, mapFrame.numOutlineShapes)
        assertEquals(regions[0], mapFrame.getOutlineShape(0))
    }

    @Test
    fun testRenderFull() {
        val shapes = loadShapes { getDistrictColor(it) }
        val mapFrame = MapFrame(
            headerPublisher = "PEI".asOneTimePublisher(),
            shapesPublisher = shapes.map { Pair(it.shape, it.color) }.asOneTimePublisher(),
        )
        mapFrame.setSize(256, 128)
        compareRendering("MapFrame", "RenderFull", mapFrame)
    }

    @Test
    fun testRenderFullThin() {
        val shapes = loadShapes { district -> getDistrictColor(district) }
        val mapFrame = MapFrame(
            headerPublisher = "PEI".asOneTimePublisher(),
            shapesPublisher = shapes.map { Pair(it.shape, it.color) }.asOneTimePublisher(),
        )
        mapFrame.setSize(64, 128)
        compareRendering("MapFrame", "RenderFullThin", mapFrame)
    }

    @Test
    fun testRenderZoomedIn() {
        val shapes = loadShapes { if (it in 9..14) getDistrictColor(it) else Color.GRAY }
        val zoomBox = loadCityBox()
        val mapFrame = MapFrame(
            headerPublisher = "CHARLOTTETOWN".asOneTimePublisher(),
            shapesPublisher = shapes.map { Pair(it.shape, it.color) }.asOneTimePublisher(),
            focusBoxPublisher = zoomBox.asOneTimePublisher(),
        )
        mapFrame.setSize(256, 128)
        compareRendering("MapFrame", "RenderZoomedIn", mapFrame)
    }

    @Test
    fun testRenderWithBorders() {
        val shapes = loadShapes { getDistrictColor(it) }
        val zoomBox = loadCityBox()
        val regions = shapes.map { it.shape }
        val header = Publisher("PEI")
        val focusBox = Publisher<Rectangle2D?>(null)
        val mapFrame = MapFrame(
            headerPublisher = header,
            shapesPublisher = shapes.map { Pair(it.shape, it.color) }.asOneTimePublisher(),
            focusBoxPublisher = focusBox,
            outlineShapesPublisher = regions.asOneTimePublisher(),
        )
        mapFrame.setSize(256, 128)
        compareRendering("MapFrame", "RenderBorders-1", mapFrame)
        focusBox.submit(zoomBox)
        header.submit("CHARLOTTETOWN")
        compareRendering("MapFrame", "RenderBorders-2", mapFrame)
    }

    private fun loadShapes(colorFunc: (Int) -> Color): List<MapEntry> {
        val shapesByDistrict = shapesByDistrict()
        return shapesByDistrict.map { (district: Int, shape: Shape) ->
            val color = colorFunc(district)
            MapEntry(shape, color)
        }
    }

    private fun loadRegions(): List<Shape> {
        val shapesByDistrict = shapesByDistrict()
        val areaReduce = { lhs: Area, rhs: Area ->
            val ret = Area(lhs)
            ret.add(rhs)
            ret
        }
        return sequenceOf(
            (1..7).asSequence(),
            (9..14).asSequence(),
            sequenceOf(8..8, 15..20).flatten(),
            (21..27).asSequence(),
        ).map { seq ->
            seq.map { shapesByDistrict[it] }
                .map { Area(it) }
                .reduce(areaReduce)
        }.toList()
    }

    private fun getDistrictColor(district: Int): Color = when (district) {
        4, 2, 3, 7, 1, 6, 19, 15, 20, 18, 8, 9, 26 -> Color.BLUE
        16, 14, 10, 24, 25, 27 -> Color.RED
        5, 17, 11, 13, 12, 22, 21, 23 -> Color.GREEN
        else -> Color.BLACK
    }

    private fun loadCityBox(): Rectangle2D = shapesByDistrict().entries.asSequence()
        .filter { it.key in 10..14 }
        .map { Area(it.value) }
        .reduce { acc, area ->
            val ret = Area(acc)
            ret.add(area)
            ret
        }
        .bounds2D

    private fun shapesByDistrict(): Map<Int, Shape> {
        val peiMap = MapFrameTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return ShapefileReader.readShapes(peiMap, "DIST_NO", Int::class.java)
    }

    private class MapEntry(val shape: Shape, val color: Color)
}
