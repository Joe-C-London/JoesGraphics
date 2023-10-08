package com.joecollins.graphics.components

import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D

class MapFrameBuilderTest {
    @Test
    fun testBasicMapFrame() {
        val shapes = Publisher(
            listOf(
                Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
                Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE),
            ),
        )
        val frame = MapFrameBuilder.from(
            shapes = shapes,
            header = "MAP".asOneTimePublisher(),
        )
        assertEquals(2, frame.numShapes)
        assertEquals(Ellipse2D.Double::class.java, frame.getShape(0).javaClass)
        assertEquals(Color.RED, frame.getColor(0))
        assertEquals(Rectangle2D.Double::class.java, frame.getShape(1).javaClass)
        assertEquals(Color.BLUE, frame.getColor(1))
        assertEquals("MAP", frame.header)
        assertEquals(Rectangle2D.Double(2.0, 2.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testBasicMapFrameWithListBinding() {
        val shapes = listOf(
            Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
            Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE),
        )
        val frame: MapFrame = MapFrameBuilder.from(
            shapes = shapes.asOneTimePublisher(),
            header = "MAP".asOneTimePublisher(),
        )
        assertEquals(2, frame.numShapes)
        assertEquals(Ellipse2D.Double::class.java, frame.getShape(0).javaClass)
        assertEquals(Color.RED, frame.getColor(0))
        assertEquals(Rectangle2D.Double::class.java, frame.getShape(1).javaClass)
        assertEquals(Color.BLUE, frame.getColor(1))
        assertEquals("MAP", frame.header)
        assertEquals(Rectangle2D.Double(2.0, 2.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testMapPropertyBinding() {
        class ConstituencyPair(val shape: Shape, val color: Color)

        val shapes = listOf(
            ConstituencyPair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
            ConstituencyPair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE),
        )
        val frame = MapFrameBuilder.from(
            items = shapes.asOneTimePublisher(),
            shape = { shape },
            color = { color.asOneTimePublisher() },
            header = "MAP".asOneTimePublisher(),
        )
        assertEquals(2, frame.numShapes)
        assertEquals(Ellipse2D.Double::class.java, frame.getShape(0).javaClass)
        assertEquals(Color.RED, frame.getColor(0))
        assertEquals(Rectangle2D.Double::class.java, frame.getShape(1).javaClass)
        assertEquals(Color.BLUE, frame.getColor(1))
        assertEquals("MAP", frame.header)
        assertEquals(Rectangle2D.Double(2.0, 2.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testMapItemPropertyBinding() {
        val shapes = listOf(
            Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Publisher(Color.RED)),
            Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Publisher(Color.BLUE)),
        )
        val frame = MapFrameBuilder.from(
            items = shapes.asOneTimePublisher(),
            shape = { first },
            color = { second },
            header = "MAP".asOneTimePublisher(),
        )
        assertEquals(2, frame.numShapes)
        assertEquals(Ellipse2D.Double::class.java, frame.getShape(0).javaClass)
        assertEquals(Color.RED, frame.getColor(0))
        assertEquals(Rectangle2D.Double::class.java, frame.getShape(1).javaClass)
        assertEquals(Color.BLUE, frame.getColor(1))

        shapes[0].second.submit(Color.GREEN)
        assertEquals(Color.GREEN, frame.getColor(0))
        assertEquals(Color.BLUE, frame.getColor(1))

        shapes[1].second.submit(Color.ORANGE)
        assertEquals(Color.ORANGE, frame.getColor(1))
        assertEquals(Color.GREEN, frame.getColor(0))
    }

    @Test
    fun testFocusBox() {
        val shapes = Publisher(
            listOf(
                Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
                Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE),
            ),
        )
        val focus = shapes.map { s -> listOf(s[0].first) }
        val frame = MapFrameBuilder.from(
            shapes = shapes,
            header = "MAP".asOneTimePublisher(),
            focus = focus,
        )

        assertEquals(Rectangle2D.Double(2.0, 2.0, 1.0, 1.0), frame.focusBox)
    }

    @Test
    fun testMultiFocusBox() {
        val shapes = Publisher(
            listOf(
                Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
                Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE),
            ),
        )
        val focus = shapes.mapElements { it.first }
        val frame = MapFrameBuilder.from(
            shapes = shapes,
            header = "MAP".asOneTimePublisher(),
            focus = focus,
        )
        assertEquals(Rectangle2D.Double(2.0, 2.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testNotes() {
        val shapes = Publisher(
            listOf(
                Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
                Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE),
            ),
        )
        val frame = MapFrameBuilder.from(
            shapes = shapes,
            header = "MAP".asOneTimePublisher(),
            notes = "A note".asOneTimePublisher(),
        )
        assertEquals("A note", frame.notes)
    }

    @Test
    fun testBorderColor() {
        val shapes = Publisher(
            listOf(
                Pair(Ellipse2D.Double(2.0, 2.0, 1.0, 1.0), Color.RED),
                Pair(Rectangle2D.Double(5.0, 5.0, 2.0, 2.0), Color.BLUE),
            ),
        )
        val frame = MapFrameBuilder.from(
            shapes = shapes,
            header = "MAP".asOneTimePublisher(),
            borderColor = Color.GRAY.asOneTimePublisher(),
        )
        assertEquals(Color.GRAY, frame.borderColor)
    }
}
