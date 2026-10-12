package com.joecollins.graphics.components

import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.mapElements
import java.awt.Color
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import java.util.concurrent.Flow
import javax.swing.JPanel
import kotlin.math.max
import kotlin.math.roundToInt

class SnakeyFrame private constructor(header: Flow.Publisher<out String?>, panel: JPanel) : GraphicsFrame(header) {

    init {
        addCenter(panel)
    }

    companion object {
        fun <L, R> create(
            leftItems: Flow.Publisher<out List<L>>,
            rightItems: Flow.Publisher<out List<R>>,
            transfers: Flow.Publisher<out Map<out Pair<L?, R?>, Int>>,
            leftLabel: L.() -> String,
            leftColor: L.() -> Color,
            rightLabel: R.() -> String,
            rightColor: R.() -> Color,
            header: Flow.Publisher<out String?>,
        ): SnakeyFrame {
            val panel = SnakeyPanel<L, R>()
            leftItems.mapElements { SnakeyPanel.Item(it, it.leftLabel(), it.leftColor()) }
                .subscribe(Subscriber(eventQueueWrapper { panel.left = it }))
            rightItems.mapElements { SnakeyPanel.Item(it, it.rightLabel(), it.rightColor()) }
                .subscribe(Subscriber(eventQueueWrapper { panel.right = it }))
            transfers.subscribe(Subscriber(eventQueueWrapper { panel.transfers = it }))
            return SnakeyFrame(header, panel)
        }
    }

    private class SnakeyPanel<L, R> : JPanel() {

        data class Item<T>(val item: T, val label: String, val color: Color)

        companion object {
            val FONT = StandardFont.readBoldFont(10)
            val GAP = 5
            val BAR_WIDTH = 10
        }

        var left: List<Item<L>> = emptyList()
            set(value) {
                field = value
                repaint()
            }

        var right: List<Item<R>> = emptyList()
            set(value) {
                field = value
                repaint()
            }

        var transfers: Map<out Pair<L?, R?>, Int> = emptyMap()
            set(value) {
                field = value
                repaint()
            }

        override fun paint(g: Graphics) {
            super.paint(g)
            (g as Graphics2D).color = Color.WHITE
            g.fillRect(0, 0, width, height)
            g.font = FONT
            g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON,
            )

            val leftItemTotals = left.associateWith { item -> transfers.filterKeys { k -> k.first == item.item }.values.sum() }
                .filterValues { it > 0 }
            val rightItemTotals = right.associateWith { item -> transfers.filterKeys { k -> k.second == item.item }.values.sum() }
                .filterValues { it > 0 }
            val total = max(leftItemTotals.values.sum(), rightItemTotals.values.sum()).toDouble()

            val height = this.height - 2 * GAP

            val fontMetrics = g.getFontMetrics(FONT)
            val left = 2 * GAP + (leftItemTotals.keys.maxOfOrNull { fontMetrics.stringWidth(it.label) } ?: 0) + BAR_WIDTH
            val leftTops = LinkedHashMap<L, Double>()
            run {
                var totalHeight = GAP.toDouble()
                leftItemTotals.forEach { (item, size) ->
                    leftTops[item.item] = totalHeight
                    g.color = item.color
                    val h = height * size / total
                    g.fill(Rectangle2D.Double(left.toDouble() - BAR_WIDTH, totalHeight, BAR_WIDTH.toDouble(), h))
                    g.drawString(item.label, left - BAR_WIDTH - GAP - fontMetrics.stringWidth(item.label), (totalHeight + (h + fontMetrics.ascent - fontMetrics.descent) / 2).roundToInt())
                    totalHeight += h
                }
            }

            val right = width - 2 * GAP - BAR_WIDTH - (rightItemTotals.keys.maxOfOrNull { fontMetrics.stringWidth(it.label) } ?: 0)
            val rightTops = LinkedHashMap<R, Double>()
            run {
                var totalHeight = GAP.toDouble()
                rightItemTotals.forEach { (item, size) ->
                    rightTops[item.item] = totalHeight
                    g.color = item.color
                    val h = height * size / total
                    g.fill(Rectangle2D.Double(right.toDouble(), totalHeight, BAR_WIDTH.toDouble(), h))
                    g.drawString(item.label, right + BAR_WIDTH + GAP, (totalHeight + (h + fontMetrics.ascent - fontMetrics.descent) / 2).roundToInt())
                    totalHeight += h
                }
            }

            this.left.forEach { leftItem ->
                this.right.forEach { rightItem ->
                    val size = transfers[leftItem.item to rightItem.item]?.takeIf { it > 0 } ?: return@forEach
                    val snakeHeight = height * size / total

                    val leftX = left.toDouble()
                    val rightX = right.toDouble()
                    val leftY = leftTops[leftItem.item]!!
                    val rightY = rightTops[rightItem.item]!!

                    g.paint = GradientPaint(
                        left.toFloat(),
                        0f,
                        rightItem.color.run { Color(red, green, blue, 64) },
                        right.toFloat(),
                        0f,
                        leftItem.color.run { Color(red, green, blue, 64) },
                    )
                    g.fill(
                        Path2D.Double().apply {
                            val midX = (leftX + rightX) / 2
                            moveTo(leftX, leftY)
                            curveTo(midX, leftY, midX, rightY, rightX, rightY)
                            lineTo(rightX, rightY + snakeHeight)
                            curveTo(midX, rightY + snakeHeight, midX, leftY + snakeHeight, leftX, leftY + snakeHeight)
                            lineTo(leftX, leftY)
                        },
                    )
                    leftTops.merge(leftItem.item, snakeHeight, Double::plus)
                    rightTops.merge(rightItem.item, snakeHeight, Double::plus)
                }
            }
        }
    }
}
