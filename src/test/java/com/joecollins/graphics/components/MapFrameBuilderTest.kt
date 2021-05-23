package com.joecollins.graphics.components

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.mapElements
import com.joecollins.graphics.components.MapFrameBuilder.Companion.from
import com.joecollins.graphics.utils.BindableWrapper
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.util.ArrayList
import org.junit.Assert
import org.junit.Test

class MapFrameBuilderTest {
    @Test
    fun testBasicMapFrame() {
        val shapes = BindableWrapper(listOf(
        Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
        Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE)
        ))
        val frame = from(shapes.binding).withHeader(fixedBinding("MAP")).build()
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
        shapes.add(Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED))
        shapes.add(Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE))
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
                Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), BindableWrapper(Color.RED)))
        shapes.add(
                Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), BindableWrapper(Color.BLUE)))
        val frame = from(fixedBinding(shapes), { it.first }, { it.second.binding })
                .withHeader(fixedBinding("MAP"))
                .build()
        Assert.assertEquals(2, frame.numShapes.toLong())
        Assert.assertEquals(Ellipse2D.Double::class.java, frame.getShape(0).javaClass)
        Assert.assertEquals(Color.RED, frame.getColor(0))
        Assert.assertEquals(Rectangle2D.Double::class.java, frame.getShape(1).javaClass)
        Assert.assertEquals(Color.BLUE, frame.getColor(1))
        shapes[0].second.value = Color.GREEN
        Assert.assertEquals(Color.GREEN, frame.getColor(0))
        Assert.assertEquals(Color.BLUE, frame.getColor(1))
        shapes[1].second.value = Color.ORANGE
        Assert.assertEquals(Color.GREEN, frame.getColor(0))
        Assert.assertEquals(Color.ORANGE, frame.getColor(1))
    }

    @Test
    fun testFocusBox() {
        val shapes = BindableWrapper(listOf(
        Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
        Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE)
        ))
        val binding = shapes.binding.map { s -> listOf(s[0].first) }
        val frame = from(shapes.binding)
                .withHeader(fixedBinding("MAP"))
                .withFocus(binding)
                .build()
        Assert.assertEquals(Rectangle2D.Double(2.0, 2.0, 1.0, 1.0), frame.focusBox)
    }

    @Test
    fun testMultiFocusBox() {
        val shapes = BindableWrapper(listOf(
        Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
        Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE)
        ))
        val binding = shapes.binding.mapElements { it.first }
        val frame = from(shapes.binding)
                .withHeader(fixedBinding("MAP"))
                .withFocus(binding)
                .build()
        Assert.assertEquals(Rectangle2D.Double(2.0, 2.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testNotes() {
        val shapes = BindableWrapper(listOf(
            Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
            Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE)
        ))
        val frame = from(shapes.binding)
            .withHeader(fixedBinding("MAP"))
            .withNotes(fixedBinding("A note"))
            .build()
        Assert.assertEquals("A note", frame.notes)
    }

    @Test
    fun testBorderColor() {
        val shapes = BindableWrapper(listOf(
            Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
            Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE)
        ))
        val frame = from(shapes.binding)
            .withHeader(fixedBinding("MAP"))
            .withBorderColor(fixedBinding(Color.GRAY))
            .build()
        Assert.assertEquals(Color.GRAY, frame.borderColor)
    }
}
