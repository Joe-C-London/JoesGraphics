package com.joecollins.graphics.components

import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
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
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Flow
import javax.swing.JPanel
import kotlin.math.min
import kotlin.math.sqrt

class MapFrame(
    headerPublisher: Flow.Publisher<out String?>,
    shapesPublisher: Flow.Publisher<out List<Pair<Shape, Color>>>,
    focusBoxPublisher: Flow.Publisher<out Rectangle2D?>? = null,
    outlineShapesPublisher: Flow.Publisher<out List<Shape>>? = null,
    notesPublisher: Flow.Publisher<out String?>? = null,
    borderColorPublisher: Flow.Publisher<out Color>? = null,
) : GraphicsFrame(
    headerPublisher = headerPublisher,
    notesPublisher = notesPublisher,
    borderColorPublisher = borderColorPublisher,
) {
    private val executor = Executors.newWorkStealingPool()
    private var shapesToDraw: List<Pair<Shape, Color>> = ArrayList()
    private var focus: Rectangle2D? = null
    private var outlineShapes: List<Shape> = ArrayList()
    private val transformedShapesCache: MutableMap<Shape, CompletableFuture<Shape>> = HashMap()
    private val mergedShapes: MutableMap<List<Shape>, CompletableFuture<Shape>> = HashMap()

    private fun createTransformedShape(transform: AffineTransform, shape: Shape): Shape {
        val pathIterator = transform.createTransformedShape(shape).getPathIterator(null)
        val currentPath = GeneralPath()
        val c = DoubleArray(6)
        var lastPoint: Point2D.Double? = null
        val isOffScreen = { p: Point2D? ->
            p != null && (p.x < 0 || p.y < 0 || p.x > width || p.y > height)
        }
        val distanceThreshold = { p1: Point2D, p2: Point2D ->
            if (isOffScreen(p1) && isOffScreen(p2)) 10.0 else 0.5
        }
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
                    if (lastPoint == null || lastPoint.distance(nextPoint) > distanceThreshold(lastPoint, nextPoint)) {
                        currentPath.lineTo(c[0], c[1])
                        lastPoint = nextPoint
                    }
                }
                PathIterator.SEG_QUADTO -> {
                    nextPoint = Point2D.Double(c[2], c[3])
                    if (lastPoint == null || lastPoint.distance(nextPoint) > distanceThreshold(lastPoint, nextPoint)) {
                        currentPath.quadTo(c[0], c[1], c[2], c[3])
                        lastPoint = nextPoint
                    }
                }
                PathIterator.SEG_CUBICTO -> {
                    nextPoint = Point2D.Double(c[4], c[5])
                    if (lastPoint == null || lastPoint.distance(nextPoint) > distanceThreshold(lastPoint, nextPoint)) {
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

    internal fun getShape(idx: Int): Shape {
        return shapesToDraw[idx].first
    }

    internal fun getColor(idx: Int): Color {
        return shapesToDraw[idx].second
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

    internal val numOutlineShapes: Int
        get() = outlineShapes.size

    internal fun getOutlineShape(idx: Int): Shape {
        return outlineShapes[idx]
    }

    init {
        val panel: JPanel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                val g2d = g as Graphics2D
                g2d.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON,
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
                val inverted = transform.createInverse()
                val drawArea = inverted.createTransformedShape(
                    Rectangle2D.Double(0.0, 0.0, width.toDouble(), height.toDouble()),
                )
                inScope = { s: Shape -> drawArea.intersects(s.bounds) }
                shapesToDraw
                    .asSequence()
                    .filter { inScope(it.first) }
                    .map {
                        Pair(
                            transformedShapesCache.computeIfAbsent(it.first) { shape ->
                                CompletableFuture.supplyAsync({
                                    val s = createTransformedShape(transform, shape)
                                    repaint()
                                    s
                                }, executor)
                            },
                            it.second,
                        )
                    }
                    .groupBy { it.second }
                    .asSequence()
                    .flatMap { shapeGroup ->
                        if (shapeGroup.value.all { it.first.isDone }) {
                            val allShapes = shapeGroup.value.map { it.first.join() }
                            val mergedShapeFuture = mergedShapes.computeIfAbsent(allShapes) {
                                CompletableFuture.supplyAsync({
                                    val ret = it
                                        .sortedBy { s -> s.bounds.width * s.bounds.height }
                                        .map { Area(it) }
                                        .reduceOrNull { a, s -> a.add(s); a }
                                        ?: Area()
                                    repaint()
                                    ret
                                }, executor)
                            }
                            if (mergedShapeFuture.isDone) {
                                return@flatMap sequenceOf(mergedShapeFuture.join() to shapeGroup.key)
                            }
                        }
                        shapeGroup.value.asSequence()
                            .filter { it.first.isDone }
                            .map { it.first.join() to shapeGroup.key }
                    }
                    .forEach {
                        g2d.color = it.second
                        g2d.fill(it.first)
                    }
                outlineShapes
                    .filter { inScope(it) }
                    .map {
                        transformedShapesCache.computeIfAbsent(
                            it,
                        ) { shape ->
                            CompletableFuture.supplyAsync({
                                val s = createTransformedShape(transform, shape)
                                repaint()
                                s
                            }, executor)
                        }
                    }
                    .mapNotNull { f -> if (f.isDone) f.join() else null }
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
                    },
                )
            }
        }
        add(panel, BorderLayout.CENTER)

        val onShapesUpdate: (List<Pair<Shape, Color>>) -> Unit = { s ->
            if (this.shapesToDraw != s) {
                shapesToDraw = s
                mergedShapes.values.forEach { it.cancel(true) }
                mergedShapes.clear()
            }
            repaint()
        }
        shapesPublisher.subscribe(Subscriber(eventQueueWrapper(onShapesUpdate)))

        val onFocusBoxUpdate: (Rectangle2D?) -> Unit = { focus ->
            if (this.focus != focus) {
                this.focus = focus
                transformedShapesCache.values.forEach { it.cancel(true) }
                transformedShapesCache.clear()
                mergedShapes.values.forEach { it.cancel(true) }
                mergedShapes.clear()
            }
            repaint()
        }
        if (focusBoxPublisher != null) {
            focusBoxPublisher.subscribe(
                Subscriber(
                    eventQueueWrapper(
                        onFocusBoxUpdate,
                    ),
                ),
            )
        } else {
            onFocusBoxUpdate(null)
        }

        val onOutlineShapesUpdate: (List<Shape>) -> Unit = { s ->
            outlineShapes = s
            repaint()
        }
        if (outlineShapesPublisher != null) {
            outlineShapesPublisher.subscribe(
                Subscriber(
                    eventQueueWrapper(
                        onOutlineShapesUpdate,
                    ),
                ),
            )
        } else {
            onOutlineShapesUpdate(emptyList())
        }
    }
}
