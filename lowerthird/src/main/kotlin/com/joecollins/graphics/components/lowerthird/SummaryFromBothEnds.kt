package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.LayoutManager
import java.awt.RenderingHints
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder
import kotlin.math.roundToInt

class SummaryFromBothEnds(
    private val headlinePublisher: Flow.Publisher<out String>,
    private val totalPublisher: Flow.Publisher<out Int>,
    private val leftPublisher: Flow.Publisher<out Entry?>,
    private val rightPublisher: Flow.Publisher<out Entry?>,
    private val middlePublisher: Flow.Publisher<out Entry?> = (null as Entry?).asOneTimePublisher(),
) : JPanel() {

    private val headlinePanel: HeadlinePanel = HeadlinePanel()
    private val entryPanel: EntryPanel = EntryPanel()

    internal val headline: String
        get() = headlinePanel.top

    internal val total: Int
        get() = entryPanel.total

    internal val left: Entry?
        get() = entryPanel.left

    internal val right: Entry?
        get() = entryPanel.right

    internal val middle: Entry?
        get() = entryPanel.middle

    private inner class HeadlinePanel : JPanel() {
        private val topPanel: JPanel = object : JPanel() {
            init {
                background = Color.BLACK
                layout = GridLayout(1, 1)
            }
        }
        private val topLabel: JLabel = FontSizeAdjustingLabel().also {
            it.font = StandardFont.readNormalFont(16)
            it.horizontalAlignment = JLabel.CENTER
            it.foreground = Color.WHITE
            it.border = EmptyBorder(3, 0, -3, 0)
        }

        var top: String
            get() {
                return topLabel.text
            }
            set(top) {
                topLabel.text = top
            }

        init {
            add(topPanel)
            topPanel.add(topLabel)
            layout = GridLayout(1, 1)
        }
    }

    class Entry(val color: Color, val label: String, val value: Int)
    private inner class EntryPanel : JPanel() {
        var total: Int = 0
            set(value) {
                field = value
                repaint()
            }

        var left: Entry? = null
            set(value) {
                field = value
                repaint()
            }

        var right: Entry? = null
            set(value) {
                field = value
                repaint()
            }

        var middle: Entry? = null
            set(value) {
                field = value
                repaint()
            }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                )
            val labelFont = StandardFont.readNormalFont(12)
            val valueFont = StandardFont.readBoldFont(20)
            val leftWidth = (1.0 * width * (if (left == null) 0 else left!!.value) / total).roundToInt()
            val rightWidth = (1.0 * width * (if (right == null) 0 else right!!.value) / total).roundToInt()
            val midWidth = (1.0 * width * (if (middle == null) 0 else middle!!.value) / total).roundToInt()
            var midCentre = width / 2
            if (midCentre - midWidth / 2 < leftWidth) {
                midCentre = leftWidth + midWidth / 2
            }
            if (midCentre + midWidth / 2 > width - rightWidth) {
                midCentre = width - rightWidth - midWidth / 2
            }
            if (left != null) {
                g.setColor(left!!.color)
                g.fillRect(0, 0, leftWidth, height)
            }
            if (right != null) {
                g.setColor(right!!.color)
                g.fillRect(width - rightWidth, 0, rightWidth, height)
            }
            if (middle != null) {
                g.setColor(middle!!.color)
                g.fillRect(midCentre - midWidth / 2, 0, midWidth, height)
            }
            if (left != null) {
                g.setColor(left!!.color)
                drawLeftLabels(g, labelFont, valueFont)
                val oldClip = g.getClip()
                g.setClip(0, 0, leftWidth, height)
                g.setColor(Color.WHITE)
                drawLeftLabels(g, labelFont, valueFont)
                g.setClip(oldClip)
            }
            if (right != null) {
                g.setColor(right!!.color)
                drawRightLabels(g, labelFont, valueFont)
                val oldClip = g.getClip()
                g.setClip(width - rightWidth, 0, rightWidth, height)
                g.setColor(Color.WHITE)
                drawRightLabels(g, labelFont, valueFont)
                g.setClip(oldClip)
            }
            if (middle != null) {
                g.setColor(middle!!.color)
                drawMidLabels(g, labelFont, valueFont, midCentre)
                val oldClip = g.getClip()
                g.setClip(midCentre - midWidth / 2, 0, midWidth, height)
                g.setColor(Color.WHITE)
                drawMidLabels(g, labelFont, valueFont, midCentre)
                g.setClip(oldClip)
            }
            g.setColor(Color.BLACK)
            g.drawLine(width / 2, 0, width / 2, height)
        }

        private fun drawMidLabels(g: Graphics, labelFont: Font, valueFont: Font, midCentre: Int) {
            g.font = labelFont
            g.drawString(middle!!.label, midCentre - g.fontMetrics.stringWidth(middle!!.label) / 2, 10)
            g.font = valueFont
            val rightValue = middle!!.value.toString()
            g.drawString(rightValue, midCentre - g.fontMetrics.stringWidth(rightValue) / 2, 28)
        }

        private fun drawRightLabels(g: Graphics, labelFont: Font, valueFont: Font) {
            g.font = labelFont
            g.drawString(right!!.label, width - g.fontMetrics.stringWidth(right!!.label), 10)
            g.font = valueFont
            val rightValue = right!!.value.toString()
            g.drawString(rightValue, width - g.fontMetrics.stringWidth(rightValue), 28)
        }

        private fun drawLeftLabels(g: Graphics, labelFont: Font, valueFont: Font) {
            g.font = labelFont
            g.drawString(left!!.label, 0, 10)
            g.font = valueFont
            g.drawString(left!!.value.toString(), 0, 28)
        }

        init {
            background = Color.WHITE
        }
    }

    private inner class SummaryLayout : LayoutManager {
        override fun addLayoutComponent(name: String, comp: Component) {}
        override fun removeLayoutComponent(comp: Component) {}
        override fun preferredLayoutSize(parent: Container): Dimension {
            return Dimension(512, 50)
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            return Dimension(50, 50)
        }

        override fun layoutContainer(parent: Container) {
            val width = parent.width - 2
            val height = parent.height - 2
            val mid = height * 2 / 5
            headlinePanel.setLocation(1, 1)
            headlinePanel.setSize(width, mid)
            entryPanel.setLocation(1, mid + 1)
            entryPanel.setSize(width, height - mid)
        }
    }

    init {
        background = Color.WHITE
        layout = SummaryLayout()
        border = MatteBorder(1, 1, 1, 1, Color.BLACK)
        add(headlinePanel)
        add(entryPanel)

        this.headlinePublisher.subscribe(Subscriber(eventQueueWrapper { headlinePanel.top = it }))
        this.totalPublisher.subscribe(Subscriber(eventQueueWrapper { entryPanel.total = it }))
        this.leftPublisher.subscribe(Subscriber(eventQueueWrapper { entryPanel.left = it }))
        this.rightPublisher.subscribe(Subscriber(eventQueueWrapper { entryPanel.right = it }))
        this.middlePublisher.subscribe(Subscriber(eventQueueWrapper { entryPanel.middle = it }))
    }
}
