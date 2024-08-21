package com.joecollins.graphics

import java.awt.Polygon
import java.awt.Rectangle
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.Rectangle2D

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

    fun createHalfTickShape(): Shape {
        return createHalfShape(createTickShape())
    }

    private fun createHalfShape(s: Shape): Area {
        val shape = Area(s)
        shape.add(Area(Rectangle2D.Double(s.bounds2D.minX, 2 * s.bounds2D.maxY - s.bounds2D.minY, 1e-6, 1e-6)))
        return shape
    }

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

    fun createHalfRunoffShape(): Shape {
        return createHalfShape(createRunoffShape())
    }
}
