package com.joecollins.graphics.components

import com.joecollins.graphics.components.MapFrameBuilder.Companion.from
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class MapFrameBuilderTest {
    @Test
    fun testBasicMapFrame() {
        val shapes = Publisher(
            listOf(
                Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
                Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE)
            )
        )
        val frame = from(shapes).withHeader("MAP".asOneTimePublisher()).build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numShapes }, IsEqual(2))
        Assert.assertEquals(Ellipse2D.Double::class.java, frame.getShape(0).javaClass)
        Assert.assertEquals(Color.RED, frame.getColor(0))
        Assert.assertEquals(Rectangle2D.Double::class.java, frame.getShape(1).javaClass)
        Assert.assertEquals(Color.BLUE, frame.getColor(1))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.header }, IsEqual("MAP"))
        Assert.assertEquals(Rectangle2D.Double(2.0, 2.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testBasicMapFrameWithListBinding() {
        val shapes: MutableList<Pair<Shape, Color>> = ArrayList()
        shapes.add(Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED))
        shapes.add(Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE))
        val frame: MapFrame = from(shapes.asOneTimePublisher()).withHeader("MAP".asOneTimePublisher()).build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numShapes }, IsEqual(2))
        Assert.assertEquals(Ellipse2D.Double::class.java, frame.getShape(0).javaClass)
        Assert.assertEquals(Color.RED, frame.getColor(0))
        Assert.assertEquals(Rectangle2D.Double::class.java, frame.getShape(1).javaClass)
        Assert.assertEquals(Color.BLUE, frame.getColor(1))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.header }, IsEqual("MAP"))
        Assert.assertEquals(Rectangle2D.Double(2.0, 2.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testMapPropertyBinding() {
        class ConstituencyPair(val shape: Shape, val color: Color)

        val shapes: MutableList<ConstituencyPair> = ArrayList()
        shapes.add(ConstituencyPair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED))
        shapes.add(ConstituencyPair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE))
        val frame = from(shapes.asOneTimePublisher(), { it.shape }, { it.color.asOneTimePublisher() })
            .withHeader("MAP".asOneTimePublisher())
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numShapes }, IsEqual(2))
        Assert.assertEquals(Ellipse2D.Double::class.java, frame.getShape(0).javaClass)
        Assert.assertEquals(Color.RED, frame.getColor(0))
        Assert.assertEquals(Rectangle2D.Double::class.java, frame.getShape(1).javaClass)
        Assert.assertEquals(Color.BLUE, frame.getColor(1))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.header }, IsEqual("MAP"))
        Assert.assertEquals(Rectangle2D.Double(2.0, 2.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testMapItemPropertyBinding() {
        val shapes: MutableList<Pair<Shape, Publisher<Color>>> = ArrayList()
        shapes.add(
            Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Publisher(Color.RED))
        )
        shapes.add(
            Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Publisher(Color.BLUE))
        )
        val frame = from(shapes.asOneTimePublisher(), { it.first }, { it.second })
            .withHeader("MAP".asOneTimePublisher())
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numShapes }, IsEqual(2))
        Assert.assertEquals(Ellipse2D.Double::class.java, frame.getShape(0).javaClass)
        Assert.assertEquals(Color.RED, frame.getColor(0))
        Assert.assertEquals(Rectangle2D.Double::class.java, frame.getShape(1).javaClass)
        Assert.assertEquals(Color.BLUE, frame.getColor(1))
        shapes[0].second.submit(Color.GREEN)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getColor(0) }, IsEqual(Color.GREEN))
        Assert.assertEquals(Color.BLUE, frame.getColor(1))
        shapes[1].second.submit(Color.ORANGE)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getColor(1) }, IsEqual(Color.ORANGE))
        Assert.assertEquals(Color.GREEN, frame.getColor(0))
    }

    @Test
    fun testFocusBox() {
        val shapes = Publisher(
            listOf(
                Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
                Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE)
            )
        )
        val binding = shapes.map { s -> listOf(s[0].first) }
        val frame = from(shapes)
            .withHeader("MAP".asOneTimePublisher())
            .withFocus(binding)
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.focusBox }, IsNot(IsNull()))
        Assert.assertEquals(Rectangle2D.Double(2.0, 2.0, 1.0, 1.0), frame.focusBox)
    }

    @Test
    fun testMultiFocusBox() {
        val shapes = Publisher(
            listOf(
                Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
                Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE)
            )
        )
        val binding = shapes.mapElements { it.first }
        val frame = from(shapes)
            .withHeader("MAP".asOneTimePublisher())
            .withFocus(binding)
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.focusBox }, IsNot(IsNull()))
        Assert.assertEquals(Rectangle2D.Double(2.0, 2.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testNotes() {
        val shapes = Publisher(
            listOf(
                Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
                Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE)
            )
        )
        val frame = from(shapes)
            .withHeader("MAP".asOneTimePublisher())
            .withNotes("A note".asOneTimePublisher())
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.notes }, IsEqual("A note"))
    }

    @Test
    fun testBorderColor() {
        val shapes = Publisher(
            listOf(
                Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
                Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE)
            )
        )
        val frame = from(shapes)
            .withHeader("MAP".asOneTimePublisher())
            .withBorderColor(Color.GRAY.asOneTimePublisher())
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.borderColor }, IsEqual(Color.GRAY))
    }
}