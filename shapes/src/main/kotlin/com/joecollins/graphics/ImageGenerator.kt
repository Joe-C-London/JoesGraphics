package com.joecollins.graphics

import com.joecollins.graphics.utils.StandardFont
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.Shape
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.Rectangle2D
import kotlin.math.roundToInt

object ImageGenerator {

    fun createTickShape(): Shape {
        val shape = Area(Rectangle(0, 0, 100, 100))
        shape.subtract(
            Area(
                Polygon(intArrayOf(10, 40, 90, 80, 40, 20), intArrayOf(50, 80, 30, 20, 60, 40), 6),
            ),
        )
        return shape
    }

    fun createHalfTickShape(): Shape = createHalfShape(createTickShape())

    fun createMidTickShape(): Shape {
        val transform = AffineTransform.getTranslateInstance(0.0, 50.0)
        val shape = Area(transform.createTransformedShape(createTickShape()))
        shape.add(Area(Rectangle2D.Double(200.0, 200.0, 1e-6, 1e-6)))
        shape.add(Area(Rectangle2D.Double(0.0, 0.0, 1e-6, 1e-6)))
        return shape
    }

    fun createCrossShape(): Shape {
        val shape = Area(Rectangle(0, 0, 100, 100))
        shape.subtract(
            Area(
                Polygon(
                    intArrayOf(15, 25, 50, 75, 85, 60, 85, 75, 50, 25, 15, 40),
                    intArrayOf(25, 15, 40, 15, 25, 50, 75, 85, 60, 85, 75, 50),
                    12,
                ),
            ),
        )
        return shape
    }

    fun createRunoffShape(): Shape {
        val shape = Area(Rectangle(0, 0, 100, 100))
        shape.subtract(
            Area(
                Polygon(
                    intArrayOf(10, 10, 50, 50, 90, 50, 50),
                    intArrayOf(30, 70, 70, 90, 50, 10, 30),
                    7,
                ),
            ),
        )
        return shape
    }

    fun createHalfRunoffShape(): Shape = createHalfShape(createRunoffShape())

    fun createBoxedTextShape(text: String): Shape {
        val font = StandardFont.readBoldFont(60)
        val textShape = font.createGlyphVector(FontRenderContext(AffineTransform(), true, true), text).outline
        val area = Area(Rectangle(0, 10, 25 + textShape.bounds2D.width.roundToInt(), 80))
        area.subtract(Area(Rectangle(5, 15, 15 + textShape.bounds2D.width.roundToInt(), 70)))
        area.add(Area(AffineTransform.getTranslateInstance(10.0, 70.0).createTransformedShape(textShape)))
        area.add(Area(Rectangle2D.Double(0.0, 100.0 - 1e-6, 1e-6, 1e-6)))
        area.add(Area(Rectangle2D.Double(0.0, 0.0, 1e-6, 1e-6)))
        return area
    }

    fun createHalfBoxedTextShape(text: String): Shape = createHalfShape(createBoxedTextShape(text))

    private fun createHalfShape(s: Shape): Area {
        val shape = Area(s)
        shape.add(Area(Rectangle2D.Double(s.bounds2D.minX, 2 * s.bounds2D.maxY - s.bounds2D.minY, 1e-6, 1e-6)))
        return shape
    }

    fun Shape?.combineHorizontal(s: Shape?): Shape? {
        if (s == null) return this
        if (this == null) return s
        val area = Area(this)
        area.add(Area(AffineTransform.getTranslateInstance(bounds2D.width + 20, 0.0).createTransformedShape(s)))
        return area
    }

    fun Shape?.combineVertical(s: Shape?): Shape? {
        if (s == null) return this
        if (this == null) return s
        val area = Area(this)
        area.add(Area(AffineTransform.getTranslateInstance(0.0, bounds2D.height + 20).createTransformedShape(s)))
        return area
    }
}
