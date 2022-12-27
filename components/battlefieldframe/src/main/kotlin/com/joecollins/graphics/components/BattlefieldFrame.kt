package com.joecollins.graphics.components

import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.util.concurrent.Flow
import javax.swing.JPanel
import kotlin.math.roundToInt
import kotlin.math.sqrt

class BattlefieldFrame(
    headerPublisher: Flow.Publisher<out String?>,
    limitPublisher: Flow.Publisher<out Number>,
    incrementPublisher: Flow.Publisher<out Number>,
    dotsPublisher: Flow.Publisher<out List<Pair<Dot, Color>>> = emptyList<Pair<Dot, Color>>().asOneTimePublisher(),
    linesPublisher: Flow.Publisher<out List<Pair<Line, Color>>> = emptyList<Pair<Line, Color>>().asOneTimePublisher(),
    swingPublisher: Flow.Publisher<out Dot?> = null.asOneTimePublisher()
) : GraphicsFrame(
    headerPublisher = headerPublisher
) {
    @Suppress("PrivatePropertyName")
    private val SQRT_3 = sqrt(3.0)

    private val panel = BattlefieldPanel()

    init {
        add(panel, BorderLayout.CENTER)
        limitPublisher.subscribe(Subscriber(eventQueueWrapper { panel.limit = it.toDouble() }))
        incrementPublisher.subscribe(Subscriber(eventQueueWrapper { panel.increment = it.toDouble() }))
        dotsPublisher.subscribe(Subscriber(eventQueueWrapper { panel.dots = it }))
        linesPublisher.subscribe(Subscriber(eventQueueWrapper { panel.lines = it }))
        swingPublisher.subscribe(Subscriber(eventQueueWrapper { panel.swing = it }))
    }

    data class Dot(val left: Number, val right: Number, val bottom: Number)

    data class Line(val dots: List<Dot>)

    private inner class BattlefieldPanel : JPanel() {

        var limit = 20.0
            set(value) {
                field = value
                repaint()
            }

        var increment = 1.0
            set(value) {
                field = value
                repaint()
            }

        var dots: List<Pair<Dot, Color>> = emptyList()
            set(value) {
                field = value
                repaint()
            }

        var lines: List<Pair<Line, Color>> = emptyList()
            set(value) {
                field = value
                repaint()
            }

        var swing: Dot? = null
            set(value) {
                field = value
                repaint()
            }

        init {
            background = Color.WHITE
        }

        private fun getCoordinates(dot: Dot): Point {
            val leftToRight = (dot.left.toDouble() - dot.right.toDouble()) / 2
            val bottomToLeft = (dot.bottom.toDouble() - dot.left.toDouble()) / 2
            val bottomToRight = (dot.bottom.toDouble() - dot.right.toDouble()) / 2

            val leftBot = bottomToLeft * 2 / SQRT_3
            val rightBot = bottomToRight * 2 / SQRT_3
            val bottomToTop = (leftBot + rightBot) / 2

            val increment = width / limit / 2
            return Point(
                width / 2 + (leftToRight * increment).roundToInt(),
                height * 3 / 5 - (bottomToTop * increment).roundToInt()
            )
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )

            g.color = Color(224, 224, 224)
            run {
                var index = 0.0
                while (index <= limit) {
                    drawGridlineAtLevel(g, 2 * index)
                    index += increment
                }
            }
            g.color = Color.BLACK
            drawGridlineAtLevel(g, 0.0)

            swing?.also {
                drawSwing(g, it)
            }

            dots.forEach { dot ->
                g.color = dot.second
                val coordinates = getCoordinates(dot.first)
                g.fillOval(coordinates.x - 2, coordinates.y - 2, 5, 5)
            }

            lines.forEach { line ->
                g.color = line.second
                val dots = line.first.dots
                (1 until dots.size).forEach { index ->
                    val p1 = getCoordinates(dots[index - 1])
                    val p2 = getCoordinates(dots[index])
                    g.drawLine(p1.x, p1.y, p2.x, p2.y)
                }
            }
        }

        private fun drawSwing(g: Graphics2D, swing: Dot) {
            val p = getCoordinates(swing)
            g.color = Color.GRAY
            val min = listOf(swing.left, swing.right, swing.bottom).minOf { n -> n.toDouble() }
            if (min == swing.left.toDouble()) {
                val right = getCoordinates(Dot(0, swing.right.toDouble() - swing.left.toDouble(), 0))
                g.drawLine(right.x, right.y, p.x, p.y)
                val bottom = getCoordinates(Dot(0, 0, swing.bottom.toDouble() - swing.left.toDouble()))
                g.drawLine(bottom.x, bottom.y, p.x, p.y)
            }
            if (min == swing.right.toDouble()) {
                val left = getCoordinates(Dot(swing.left.toDouble() - swing.right.toDouble(), 0, 0))
                g.drawLine(left.x, left.y, p.x, p.y)
                val bottom = getCoordinates(Dot(0, 0, swing.bottom.toDouble() - swing.right.toDouble()))
                g.drawLine(bottom.x, bottom.y, p.x, p.y)
            }
            if (min == swing.bottom.toDouble()) {
                val left = getCoordinates(Dot(swing.left.toDouble() - swing.bottom.toDouble(), 0, 0))
                g.drawLine(left.x, left.y, p.x, p.y)
                val bottom = getCoordinates(Dot(0, swing.right.toDouble() - swing.bottom.toDouble(), 0))
                g.drawLine(bottom.x, bottom.y, p.x, p.y)
            }
            g.color = Color.BLACK
            g.drawLine(p.x - 5, p.y - 5, p.x + 5, p.y + 5)
            g.drawLine(p.x + 5, p.y - 5, p.x - 5, p.y + 5)
        }

        private fun drawGridlineAtLevel(g: Graphics, index: Double) {
            run {
                val p1 = getCoordinates(Dot(index, 0, 0))
                val p2 = getCoordinates(Dot(index, 0, 2 * limit))
                val p3 = getCoordinates(Dot(index, 2 * limit, 0))
                g.drawLine(p1.x, p1.y, p2.x, p2.y)
                g.drawLine(p1.x, p1.y, p3.x, p3.y)
            }
            run {
                val p1 = getCoordinates(Dot(0, index, 0))
                val p2 = getCoordinates(Dot(0, index, 2 * limit))
                val p3 = getCoordinates(Dot(2 * limit, index, 0))
                g.drawLine(p1.x, p1.y, p2.x, p2.y)
                g.drawLine(p1.x, p1.y, p3.x, p3.y)
            }
            run {
                val p1 = getCoordinates(Dot(0, 0, index))
                val p2 = getCoordinates(Dot(0, 2 * limit, index))
                val p3 = getCoordinates(Dot(2 * limit, 0, index))
                g.drawLine(p1.x, p1.y, p2.x, p2.y)
                g.drawLine(p1.x, p1.y, p3.x, p3.y)
            }
        }
    }
}
