package com.joecollins.graphics.components.lowerthird

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
    private val middlePublisher: Flow.Publisher<out Entry?> = (null as Entry?).asOneTimePublisher()
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
        private val topLabel: JLabel = object : JLabel() {
            init {
                font = StandardFont.readNormalFont(16)
                horizontalAlignment = CENTER
                foreground = Color.WHITE
                border = EmptyBorder(3, 0, -3, 0)
            }
        }

        var top: String
            get() { return topLabel.text }
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
        private var _total = 0
        private var _left: Entry? = null
        private var _right: Entry? = null
        private var _middle: Entry? = null

        var total: Int
            get() { return _total }
            set(total) {
                _total = total
                repaint()
            }

        var left: Entry?
            get() { return _left }
            set(left) {
                _left = left
                repaint()
            }

        var right: Entry?
            get() { return _right }
            set(right) {
                _right = right
                repaint()
            }

        var middle: Entry?
            get() { return _middle }
            set(middle) {
                _middle = middle
                repaint()
            }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
            val labelFont = StandardFont.readNormalFont(12)
            val valueFont = StandardFont.readBoldFont(20)
            val leftWidth = (1.0 * width * (if (_left == null) 0 else _left!!.value) / _total).roundToInt()
            val rightWidth = (1.0 * width * (if (_right == null) 0 else _right!!.value) / _total).roundToInt()
            val midWidth = (1.0 * width * (if (_middle == null) 0 else _middle!!.value) / _total).roundToInt()
            var midCentre = width / 2
            if (midCentre - midWidth / 2 < leftWidth) {
                midCentre = leftWidth + midWidth / 2
            }
            if (midCentre + midWidth / 2 > width - rightWidth) {
                midCentre = width - rightWidth - midWidth / 2
            }
            if (_left != null) {
                g.setColor(_left!!.color)
                g.fillRect(0, 0, leftWidth, height)
            }
            if (_right != null) {
                g.setColor(_right!!.color)
                g.fillRect(width - rightWidth, 0, rightWidth, height)
            }
            if (_middle != null) {
                g.setColor(_middle!!.color)
                g.fillRect(midCentre - midWidth / 2, 0, midWidth, height)
            }
            if (_left != null) {
                g.setColor(_left!!.color)
                drawLeftLabels(g, labelFont, valueFont)
                val oldClip = g.getClip()
                g.setClip(0, 0, leftWidth, height)
                g.setColor(Color.WHITE)
                drawLeftLabels(g, labelFont, valueFont)
                g.setClip(oldClip)
            }
            if (_right != null) {
                g.setColor(_right!!.color)
                drawRightLabels(g, labelFont, valueFont)
                val oldClip = g.getClip()
                g.setClip(width - rightWidth, 0, rightWidth, height)
                g.setColor(Color.WHITE)
                drawRightLabels(g, labelFont, valueFont)
                g.setClip(oldClip)
            }
            if (_middle != null) {
                g.setColor(_middle!!.color)
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
            g.drawString(_middle!!.label, midCentre - g.fontMetrics.stringWidth(_middle!!.label) / 2, 10)
            g.font = valueFont
            val rightValue = _middle!!.value.toString()
            g.drawString(rightValue, midCentre - g.fontMetrics.stringWidth(rightValue) / 2, 28)
        }

        private fun drawRightLabels(g: Graphics, labelFont: Font, valueFont: Font) {
            g.font = labelFont
            g.drawString(_right!!.label, width - g.fontMetrics.stringWidth(_right!!.label), 10)
            g.font = valueFont
            val rightValue = _right!!.value.toString()
            g.drawString(rightValue, width - g.fontMetrics.stringWidth(rightValue), 28)
        }

        private fun drawLeftLabels(g: Graphics, labelFont: Font, valueFont: Font) {
            g.font = labelFont
            g.drawString(_left!!.label, 0, 10)
            g.font = valueFont
            g.drawString(_left!!.value.toString(), 0, 28)
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
