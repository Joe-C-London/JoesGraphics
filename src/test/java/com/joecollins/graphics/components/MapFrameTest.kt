package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.Binding.Companion.sizeBinding
import com.joecollins.bindings.IndexedBinding.Companion.listBinding
import com.joecollins.bindings.IndexedBinding.Companion.propertyBinding
import com.joecollins.bindings.NestedBindableList
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader.readShapes
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Rectangle2D
import java.io.IOException
import kotlin.Throws
import org.junit.Assert
import org.junit.Test

class MapFrameTest {
    @Test
    @Throws(IOException::class)
    fun testBindShapes() {
        val shapes = loadShapes { getDistrictColor(it) }
        val mapFrame = MapFrame()
        mapFrame.setNumShapesBinding(sizeBinding(shapes))
        mapFrame.setShapeBinding(propertyBinding(shapes, { it.shape }, MapEntry.Property.SHAPE))
        mapFrame.setColorBinding(propertyBinding(shapes, { it.color }, MapEntry.Property.COLOR))
        Assert.assertEquals(27, mapFrame.numShapes.toLong())
        Assert.assertEquals(shapes[0].shape, mapFrame.getShape(0))
        Assert.assertEquals(shapes[0].color, mapFrame.getColor(0))
    }

    @Test
    @Throws(IOException::class)
    fun testDefaultFocusAreaEncompassesAllShapes() {
        val shapes = loadShapes { getDistrictColor(it) }
        val mapFrame = MapFrame()
        mapFrame.setNumShapesBinding(sizeBinding(shapes))
        mapFrame.setShapeBinding(propertyBinding(shapes, { it.shape }, MapEntry.Property.SHAPE))
        mapFrame.setColorBinding(propertyBinding(shapes, { it.color }, MapEntry.Property.COLOR))
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
        val mapFrame = MapFrame()
        mapFrame.setNumShapesBinding(sizeBinding(shapes))
        mapFrame.setShapeBinding(propertyBinding(shapes, { it.shape }, MapEntry.Property.SHAPE))
        mapFrame.setColorBinding(propertyBinding(shapes, { it.color }, MapEntry.Property.COLOR))
        mapFrame.setFocusBoxBinding(fixedBinding(cityBox))
        Assert.assertEquals(cityBox, mapFrame.focusBox)
    }

    @Test
    @Throws(IOException::class)
    fun testOutlines() {
        val regions = loadRegions()
        val mapFrame = MapFrame()
        mapFrame.setNumOutlineShapesBinding(sizeBinding(regions))
        mapFrame.setOutlineShapesBinding(listBinding(regions))
        Assert.assertEquals(4, mapFrame.numOutlineShapes.toLong())
        Assert.assertEquals(regions[0], mapFrame.getOutlineShape(0))
    }

    @Test
    @Throws(IOException::class)
    fun testRenderFull() {
        val shapes = loadShapes { getDistrictColor(it) }
        val mapFrame = MapFrame()
        mapFrame.setHeaderBinding(fixedBinding<String?>("PEI"))
        mapFrame.setNumShapesBinding(sizeBinding(shapes))
        mapFrame.setShapeBinding(propertyBinding(shapes, { it.shape }, MapEntry.Property.SHAPE))
        mapFrame.setColorBinding(propertyBinding(shapes, { it.color }, MapEntry.Property.COLOR))
        mapFrame.setSize(256, 128)
        compareRendering("MapFrame", "RenderFull", mapFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderFullThin() {
        val shapes = loadShapes { district: Int -> getDistrictColor(district) }
        val mapFrame = MapFrame()
        mapFrame.setHeaderBinding(fixedBinding<String?>("PEI"))
        mapFrame.setNumShapesBinding(sizeBinding(shapes))
        mapFrame.setShapeBinding(propertyBinding(shapes, { obj: MapEntry -> obj.shape }, MapEntry.Property.SHAPE))
        mapFrame.setColorBinding(propertyBinding(shapes, { obj: MapEntry -> obj.color }, MapEntry.Property.COLOR))
        mapFrame.setSize(64, 128)
        compareRendering("MapFrame", "RenderFullThin", mapFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderZoomedIn() {
        val shapes = loadShapes { if (it in 9..14) getDistrictColor(it) else Color.GRAY }
        val zoomBox = loadCityBox()
        val mapFrame = MapFrame()
        mapFrame.setHeaderBinding(fixedBinding("CHARLOTTETOWN"))
        mapFrame.setNumShapesBinding(sizeBinding(shapes))
        mapFrame.setShapeBinding(propertyBinding(shapes, { it.shape }, MapEntry.Property.SHAPE))
        mapFrame.setColorBinding(propertyBinding(shapes, { it.color }, MapEntry.Property.COLOR))
        mapFrame.setFocusBoxBinding(fixedBinding(zoomBox))
        mapFrame.setSize(256, 128)
        compareRendering("MapFrame", "RenderZoomedIn", mapFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderWithBorders() {
        val shapes = loadShapes { getDistrictColor(it) }
        val zoomBox = loadCityBox()
        val regions = BindableList<Shape>()
        regions.addAll(shapes.map { it.shape })
        val mapFrame = MapFrame()
        mapFrame.setHeaderBinding(fixedBinding<String?>("PEI"))
        mapFrame.setNumShapesBinding(sizeBinding(shapes))
        mapFrame.setShapeBinding(propertyBinding(shapes, { it.shape }, MapEntry.Property.SHAPE))
        mapFrame.setColorBinding(propertyBinding(shapes, { it.color }, MapEntry.Property.COLOR))
        mapFrame.setNumOutlineShapesBinding(sizeBinding(regions))
        mapFrame.setOutlineShapesBinding(listBinding(regions))
        mapFrame.setSize(256, 128)
        compareRendering("MapFrame", "RenderBorders-1", mapFrame)
        mapFrame.setFocusBoxBinding(fixedBinding(zoomBox))
        mapFrame.setHeaderBinding(fixedBinding<String?>("CHARLOTTETOWN"))
        compareRendering("MapFrame", "RenderBorders-2", mapFrame)
    }

    @Throws(IOException::class)
    private fun loadShapes(colorFunc: (Int) -> Color): NestedBindableList<MapEntry, MapEntry.Property> {
        val shapesByDistrict = shapesByDistrict()
        val shapes: NestedBindableList<MapEntry, MapEntry.Property> = NestedBindableList()
        shapesByDistrict.forEach { (district: Int, shape: Shape) ->
            val color = colorFunc(district)
            shapes.add(MapEntry(shape, color))
        }
        return shapes
    }

    @Throws(IOException::class)
    private fun loadRegions(): BindableList<Shape> {
        val shapesByDistrict = shapesByDistrict()
        val regions = BindableList<Shape>()
        val areaReduce = { lhs: Area, rhs: Area ->
            val ret = Area(lhs)
            ret.add(rhs)
            ret
        }
        regions.add(
                (1..7).map { shapesByDistrict[it] }
                        .map { Area(it) }
                        .reduce(areaReduce))
        regions.add(
                (9..14).map { shapesByDistrict[it] }
                        .map { Area(it) }
                        .reduce(areaReduce))
        regions.add(
                sequenceOf(8..8, 15..20).flatten()
                        .map { shapesByDistrict[it] }
                        .map { Area(it) }
                        .reduce(areaReduce))
        regions.add(
                (21..27).map { shapesByDistrict[it] }
                        .map { Area(it) }
                        .reduce(areaReduce))
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

    private class MapEntry(private var _shape: Shape, private var _color: Color) : Bindable<MapEntry, MapEntry.Property>() {
        enum class Property {
            SHAPE, COLOR
        }

        var shape: Shape
        get() = _shape
        set(shape) {
            this._shape = shape
            onPropertyRefreshed(Property.SHAPE)
        }

        var color: Color
        get() = _color
        set(color) {
            this._color = color
            onPropertyRefreshed(Property.COLOR)
        }
    }
}
