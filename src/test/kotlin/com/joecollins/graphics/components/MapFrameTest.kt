package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader.readShapes
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Rectangle2D
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.Throws

class MapFrameTest {
    @Test
    @Throws(IOException::class)
    fun testBindShapes() {
        val shapes = loadShapes { getDistrictColor(it) }
        val mapFrame = MapFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            shapesPublisher = shapes.map { Pair(it.shape, it.color) }.asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ mapFrame.numShapes }, IsEqual(27))
        Assert.assertEquals(shapes[0].shape, mapFrame.getShape(0))
        Assert.assertEquals(shapes[0].color, mapFrame.getColor(0))
    }

    @Test
    @Throws(IOException::class)
    fun testDefaultFocusAreaEncompassesAllShapes() {
        val shapes = loadShapes { getDistrictColor(it) }
        val mapFrame = MapFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            shapesPublisher = shapes.map { Pair(it.shape, it.color) }.asOneTimePublisher()
        )
        val bindingBox = shapes.asSequence()
            .map { Area(it.shape) }
            .reduce { acc, area ->
                val ret = Area(acc)
                ret.add(area)
                ret
            }
            .bounds2D
        Assert.assertEquals(bindingBox, mapFrame.focusBox)
    }

    @Test
    @Throws(IOException::class)
    fun testFocusBox() {
        val shapes = loadShapes { getDistrictColor(it) }
        val cityBox = loadCityBox()
        val mapFrame = MapFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            shapesPublisher = shapes.map { Pair(it.shape, it.color) }.asOneTimePublisher(),
            focusBoxPublisher = cityBox.asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ mapFrame.focusBox }, IsEqual(cityBox))
    }

    @Test
    @Throws(IOException::class)
    fun testOutlines() {
        val regions = loadRegions()
        val mapFrame = MapFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            shapesPublisher = regions.map { Pair(it, Color.BLACK) }.asOneTimePublisher(),
            outlineShapesPublisher = regions.asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ mapFrame.numOutlineShapes }, IsEqual(4))
        Assert.assertEquals(regions[0], mapFrame.getOutlineShape(0))
    }

    @Test
    @Throws(IOException::class)
    fun testRenderFull() {
        val shapes = loadShapes { getDistrictColor(it) }
        val mapFrame = MapFrame(
            headerPublisher = "PEI".asOneTimePublisher(),
            shapesPublisher = shapes.map { Pair(it.shape, it.color) }.asOneTimePublisher()
        )
        mapFrame.setSize(256, 128)
        compareRendering("MapFrame", "RenderFull", mapFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderFullThin() {
        val shapes = loadShapes { district: Int -> getDistrictColor(district) }
        val mapFrame = MapFrame(
            headerPublisher = "PEI".asOneTimePublisher(),
            shapesPublisher = shapes.map { Pair(it.shape, it.color) }.asOneTimePublisher()
        )
        mapFrame.setSize(64, 128)
        compareRendering("MapFrame", "RenderFullThin", mapFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderZoomedIn() {
        val shapes = loadShapes { if (it in 9..14) getDistrictColor(it) else Color.GRAY }
        val zoomBox = loadCityBox()
        val mapFrame = MapFrame(
            headerPublisher = "CHARLOTTETOWN".asOneTimePublisher(),
            shapesPublisher = shapes.map { Pair(it.shape, it.color) }.asOneTimePublisher(),
            focusBoxPublisher = zoomBox.asOneTimePublisher()
        )
        mapFrame.setSize(256, 128)
        compareRendering("MapFrame", "RenderZoomedIn", mapFrame)
    }

    @Test
    @Throws(IOException::class)
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
            outlineShapesPublisher = regions.asOneTimePublisher()
        )
        mapFrame.setSize(256, 128)
        compareRendering("MapFrame", "RenderBorders-1", mapFrame)
        focusBox.submit(zoomBox)
        header.submit("CHARLOTTETOWN")
        compareRendering("MapFrame", "RenderBorders-2", mapFrame)
    }

    @Throws(IOException::class)
    private fun loadShapes(colorFunc: (Int) -> Color): List<MapEntry> {
        val shapesByDistrict = shapesByDistrict()
        return shapesByDistrict.map { (district: Int, shape: Shape) ->
            val color = colorFunc(district)
            MapEntry(shape, color)
        }
    }

    @Throws(IOException::class)
    private fun loadRegions(): List<Shape> {
        val shapesByDistrict = shapesByDistrict()
        val regions = ArrayList<Shape>()
        val areaReduce = { lhs: Area, rhs: Area ->
            val ret = Area(lhs)
            ret.add(rhs)
            ret
        }
        regions.add(
            (1..7).map { shapesByDistrict[it] }
                .map { Area(it) }
                .reduce(areaReduce)
        )
        regions.add(
            (9..14).map { shapesByDistrict[it] }
                .map { Area(it) }
                .reduce(areaReduce)
        )
        regions.add(
            sequenceOf(8..8, 15..20).flatten()
                .map { shapesByDistrict[it] }
                .map { Area(it) }
                .reduce(areaReduce)
        )
        regions.add(
            (21..27).map { shapesByDistrict[it] }
                .map { Area(it) }
                .reduce(areaReduce)
        )
        return regions
    }

    private fun getDistrictColor(district: Int): Color {
        return when (district) {
            4, 2, 3, 7, 1, 6, 19, 15, 20, 18, 8, 9, 26 -> Color.BLUE
            16, 14, 10, 24, 25, 27 -> Color.RED
            5, 17, 11, 13, 12, 22, 21, 23 -> Color.GREEN
            else -> Color.BLACK
        }
    }

    @Throws(IOException::class)
    private fun loadCityBox(): Rectangle2D {
        return shapesByDistrict().entries.asSequence()
            .filter { it.key in 10..14 }
            .map { Area(it.value) }
            .reduce { acc, area ->
                val ret = Area(acc)
                ret.add(area)
                ret
            }
            .bounds2D
    }

    @Throws(IOException::class)
    private fun shapesByDistrict(): Map<Int, Shape> {
        val peiMap = MapFrameTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return readShapes(peiMap, "DIST_NO", Int::class.java)
    }

    private class MapEntry(private val _shape: Shape, private val _color: Color) {
        val shape: Shape
            get() = _shape

        val color: Color
            get() = _color
    }
}