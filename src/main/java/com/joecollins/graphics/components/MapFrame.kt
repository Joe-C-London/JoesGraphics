package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.GeneralPath
import java.awt.geom.NoninvertibleTransformException
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.util.ArrayList
import java.util.WeakHashMap
import javax.swing.JPanel
import kotlin.math.min
import kotlin.math.sqrt

class MapFrame : GraphicsFrame() {
    private var numShapesBinding: Binding<Int> = Binding.fixedBinding(0)
    private var shapeBinding = IndexedBinding.emptyBinding<Shape>()
    private var colorBinding = IndexedBinding.emptyBinding<Color>()

    private var focusBinding: Binding<Rectangle2D?> = Binding.fixedBinding(null)
    private var numOutlineShapesBinding: Binding<Int> = Binding.fixedBinding(0)
    private var outlineShapesBinding = IndexedBinding.emptyBinding<Shape>()

    private val shapesToDraw: MutableList<Pair<Shape, Color>> = ArrayList()
    private var focus: Rectangle2D? = null
    private val outlineShapes: MutableList<Shape> = ArrayList()
    private val transformedShapesCache: MutableMap<Shape, Shape> = WeakHashMap()
    private val distanceThreshold = 0.5

    private fun createTransformedShape(transform: AffineTransform, shape: Shape): Shape {
        val pathIterator = transform.createTransformedShape(shape).getPathIterator(null)
        val currentPath = GeneralPath()
        val c = DoubleArray(6)
        var lastPoint: Point2D.Double? = null
        while (!pathIterator.isDone) {
            val type = pathIterator.currentSegment(c)
            var nextPoint: Point2D.Double
            when (type) {
                PathIterator.SEG_MOVETO -> {
                    lastPoint = Point2D.Double(c[0], c[1])
                    currentPath.moveTo(c[0], c[1])
                }
                PathIterator.SEG_LINETO -> {
                    nextPoint = Point2D.Double(c[0], c[1])
                    if (lastPoint == null || lastPoint.distance(nextPoint) > distanceThreshold) {
                        currentPath.lineTo(c[0], c[1])
                        lastPoint = nextPoint
                    }
                }
                PathIterator.SEG_QUADTO -> {
                    nextPoint = Point2D.Double(c[2], c[3])
                    if (lastPoint == null || lastPoint.distance(nextPoint) > distanceThreshold) {
                        currentPath.quadTo(c[0], c[1], c[2], c[3])
                        lastPoint = nextPoint
                    }
                }
                PathIterator.SEG_CUBICTO -> {
                    nextPoint = Point2D.Double(c[4], c[5])
                    if (lastPoint == null || lastPoint.distance(nextPoint) > distanceThreshold) {
                        currentPath.curveTo(c[0], c[1], c[2], c[3], c[4], c[5])
                        lastPoint = nextPoint
                    }
                }
                PathIterator.SEG_CLOSE -> {
                    lastPoint = null
                    currentPath.closePath()
                }
                else -> throw IllegalStateException("Unrecognised segment type $type")
            }
            pathIterator.next()
        }
        return currentPath
    }

    internal val numShapes: Int
        get() = shapesToDraw.size

    fun setNumShapesBinding(numShapesBinding: Binding<Int>) {
        this.numShapesBinding.unbind()
        this.numShapesBinding = numShapesBinding
        this.numShapesBinding.bind { size ->
            while (size > shapesToDraw.size) {
                shapesToDraw.add(Pair(Area(), Color.BLACK))
            }
            while (size < shapesToDraw.size) {
                shapesToDraw.removeAt(size)
            }
            repaint()
        }
    }

    internal fun getShape(idx: Int): Shape {
        return shapesToDraw[idx].first
    }

    fun setShapeBinding(shapeBinding: IndexedBinding<Shape>) {
        this.shapeBinding.unbind()
        this.shapeBinding = shapeBinding
        this.shapeBinding.bind { idx, shape ->
            shapesToDraw[idx] = shapesToDraw[idx].copy(first = shape)
            repaint()
        }
    }

    internal fun getColor(idx: Int): Color {
        return shapesToDraw[idx].second
    }

    fun setColorBinding(colorBinding: IndexedBinding<Color>) {
        this.colorBinding.unbind()
        this.colorBinding = colorBinding
        this.colorBinding.bind { idx, color ->
            shapesToDraw[idx] = shapesToDraw[idx].copy(second = color)
            repaint()
        }
    }

    internal val focusBox: Rectangle2D?
        get() {
            if (focus == null) {
                var bounds: Rectangle2D? = null
                for (entry in shapesToDraw) {
                    if (bounds == null) {
                        bounds = entry.first.bounds2D
                    } else {
                        bounds.add(entry.first.bounds2D)
                    }
                }
                return bounds
            }
            return focus
        }

    fun setFocusBoxBinding(focusBinding: Binding<Rectangle2D?>) {
        this.focusBinding.unbind()
        this.focusBinding = focusBinding
        this.focusBinding.bind { focus ->
            this.focus = focus
            transformedShapesCache.clear()
            repaint()
        }
    }

    internal val numOutlineShapes: Int
        get() = outlineShapes.size

    fun setNumOutlineShapesBinding(numOutlineShapesBinding: Binding<Int>) {
        this.numOutlineShapesBinding.unbind()
        this.numOutlineShapesBinding = numOutlineShapesBinding
        this.numOutlineShapesBinding.bind { size ->
            while (size > outlineShapes.size) {
                outlineShapes.add(Area())
            }
            while (size < outlineShapes.size) {
                outlineShapes.removeAt(size)
            }
            repaint()
        }
    }

    internal fun getOutlineShape(idx: Int): Shape {
        return outlineShapes[idx]
    }

    fun setOutlineShapesBinding(outlineShapesBinding: IndexedBinding<Shape>) {
        this.outlineShapesBinding.unbind()
        this.outlineShapesBinding = outlineShapesBinding
        this.outlineShapesBinding.bind { idx, outline ->
            outlineShapes[idx] = outline
            repaint()
        }
    }

    init {
        val panel: JPanel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                val g2d = g as Graphics2D
                g2d.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
                )
                if (shapesToDraw.isEmpty()) {
                    return
                }
                val bounds = focusBox
                val transform = AffineTransform()
                val boundsWidth = bounds!!.maxX - bounds.minX
                val boundsHeight = bounds.maxY - bounds.minY
                val xScale = (width - 4) / boundsWidth
                val yScale = (height - 4) / boundsHeight
                val scale = min(xScale, yScale)
                val x = (width - scale * boundsWidth) / 2
                val y = (height - scale * boundsHeight) / 2
                transform.translate(x, y)
                transform.scale(scale, scale)
                transform.translate(-bounds.minX, -bounds.minY)
                val inScope: (Shape) -> Boolean
                try {
                    val inverted = transform.createInverse()
                    val drawArea = inverted.createTransformedShape(
                        Rectangle2D.Double(0.0, 0.0, width.toDouble(), height.toDouble())
                    )
                    inScope = { s: Shape -> drawArea.intersects(s.bounds) }
                } catch (e: NoninvertibleTransformException) {
                    throw RuntimeException(e)
                }
                shapesToDraw
                    .filter { inScope(it.first) }
                    .map {
                        Pair(
                            transformedShapesCache.computeIfAbsent(
                                it.first
                            ) { shape: Shape -> createTransformedShape(transform, shape) },
                            it.second
                        )
                    }
                    .forEach {
                        g2d.color = it.second
                        g2d.fill(it.first)
                    }
                outlineShapes
                    .filter { inScope(it) }
                    .map {
                        transformedShapesCache.computeIfAbsent(
                            it
                        ) { s: Shape -> createTransformedShape(transform, s) }
                    }
                    .forEach {
                        g2d.color = Color.WHITE
                        g2d.stroke = BasicStroke(sqrt(0.5).toFloat())
                        g2d.draw(it)
                    }
            }

            init {
                background = Color.WHITE
                addComponentListener(
                    object : ComponentAdapter() {
                        override fun componentResized(e: ComponentEvent) {
                            transformedShapesCache.clear()
                            repaint()
                        }
                    })
            }
        }
        add(panel, BorderLayout.CENTER)
    }
}
