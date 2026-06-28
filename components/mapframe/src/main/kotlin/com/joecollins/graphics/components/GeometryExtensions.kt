package com.joecollins.graphics.components

import org.locationtech.jts.awt.ShapeWriter
import org.locationtech.jts.geom.Geometry
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D

private val Y_FLIP = AffineTransform.getScaleInstance(1.0, -1.0)

internal fun Geometry.toAwtShape(): Shape = Y_FLIP.createTransformedShape(ShapeWriter().toShape(this))

internal fun Geometry.awtBounds(): Rectangle2D {
    val e = envelopeInternal
    return Rectangle2D.Double(e.minX, -e.maxY, e.width, e.height)
}
