package com.joecollins.graphics.components

import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.components.MapFrameBuilder.Companion.from
import com.joecollins.graphics.utils.BindableWrapper
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.util.ArrayList
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import org.junit.Assert
import org.junit.Test

class MapFrameBuilderTest {
    @Test
    fun testBasicMapFrame() {
        val shapes = BindableList<Pair<Shape, Color>>()
        shapes.add(ImmutablePair.of(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED))
        shapes.add(ImmutablePair.of(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE))
        val frame = from(shapes).withHeader(fixedBinding("MAP")).build()
        Assert.assertEquals(2, frame.numShapes.toLong())
        Assert.assertEquals(Ellipse2D.Double::class.java, frame.getShape(0).javaClass)
        Assert.assertEquals(Color.RED, frame.getColor(0))
        Assert.assertEquals(Rectangle2D.Double::class.java, frame.getShape(1).javaClass)
        Assert.assertEquals(Color.BLUE, frame.getColor(1))
        Assert.assertEquals("MAP", frame.header)
        Assert.assertEquals(Rectangle2D.Double(2.0, 2.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testBasicMapFrameWithListBinding() {
        val shapes: MutableList<Pair<Shape, Color>> = ArrayList()
        shapes.add(ImmutablePair.of(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED))
        shapes.add(ImmutablePair.of(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE))
        val frame: MapFrame = from(fixedBinding(shapes)).withHeader(fixedBinding("MAP")).build()
        Assert.assertEquals(2, frame.numShapes.toLong())
        Assert.assertEquals(Ellipse2D.Double::class.java, frame.getShape(0).javaClass)
        Assert.assertEquals(Color.RED, frame.getColor(0))
        Assert.assertEquals(Rectangle2D.Double::class.java, frame.getShape(1).javaClass)
        Assert.assertEquals(Color.BLUE, frame.getColor(1))
        Assert.assertEquals("MAP", frame.header)
        Assert.assertEquals(Rectangle2D.Double(2.0, 2.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testMapPropertyBinding() {
        class ConstituencyPair(val shape: Shape, val color: Color)

        val shapes: MutableList<ConstituencyPair> = ArrayList()
        shapes.add(ConstituencyPair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED))
        shapes.add(ConstituencyPair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE))
        val frame = from(fixedBinding(shapes), { it.shape }, { fixedBinding(it.color) })
                .withHeader(fixedBinding("MAP"))
                .build()
        Assert.assertEquals(2, frame.numShapes.toLong())
        Assert.assertEquals(Ellipse2D.Double::class.java, frame.getShape(0).javaClass)
        Assert.assertEquals(Color.RED, frame.getColor(0))
        Assert.assertEquals(Rectangle2D.Double::class.java, frame.getShape(1).javaClass)
        Assert.assertEquals(Color.BLUE, frame.getColor(1))
        Assert.assertEquals("MAP", frame.header)
        Assert.assertEquals(Rectangle2D.Double(2.0, 2.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testMapItemPropertyBinding() {
        val shapes: MutableList<Pair<Shape, BindableWrapper<Color>>> = ArrayList()
        shapes.add(
                ImmutablePair.of(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), BindableWrapper(Color.RED)))
        shapes.add(
                ImmutablePair.of(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), BindableWrapper(Color.BLUE)))
        val frame = from(fixedBinding(shapes), { it.left }, { it.right.binding })
                .withHeader(fixedBinding("MAP"))
                .build()
        Assert.assertEquals(2, frame.numShapes.toLong())
        Assert.assertEquals(Ellipse2D.Double::class.java, frame.getShape(0).javaClass)
        Assert.assertEquals(Color.RED, frame.getColor(0))
        Assert.assertEquals(Rectangle2D.Double::class.java, frame.getShape(1).javaClass)
        Assert.assertEquals(Color.BLUE, frame.getColor(1))
        shapes[0].right.value = Color.GREEN
        Assert.assertEquals(Color.GREEN, frame.getColor(0))
        Assert.assertEquals(Color.BLUE, frame.getColor(1))
        shapes[1].right.value = Color.ORANGE
        Assert.assertEquals(Color.GREEN, frame.getColor(0))
        Assert.assertEquals(Color.ORANGE, frame.getColor(1))
    }

    @Test
    fun testFocusBox() {
        val shapes = BindableList<Pair<Shape, Color>>()
        shapes.add(ImmutablePair.of(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED))
        shapes.add(ImmutablePair.of(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE))
        val binding = listOf(shapes[0].left)
        val frame = from(shapes)
                .withHeader(fixedBinding("MAP"))
                .withFocus(fixedBinding(binding))
                .build()
        Assert.assertEquals(Rectangle2D.Double(2.0, 2.0, 1.0, 1.0), frame.focusBox)
    }

    @Test
    fun testMultiFocusBox() {
        val shapes = BindableList<Pair<Shape, Color>>()
        shapes.add(ImmutablePair.of(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED))
        shapes.add(ImmutablePair.of(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE))
        val binding = shapes.map { it.left }
        val frame = from(shapes)
                .withHeader(fixedBinding("MAP"))
                .withFocus(fixedBinding(binding))
                .build()
        Assert.assertEquals(Rectangle2D.Double(2.0, 2.0, 5.0, 5.0), frame.focusBox)
    }
}
