package com.joecollins.graphics.components

import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.LayoutManager
import java.awt.RenderingHints
import java.util.ArrayList
import java.util.concurrent.Flow
import javax.swing.JPanel
import kotlin.math.ceil

class ResultListingFrame(
    headerPublisher: Flow.Publisher<out String?>,
    numRowsPublisher: Flow.Publisher<out Int>,
    itemsPublisher: Flow.Publisher<out List<Item>>,
    reversedPublisher: Flow.Publisher<out Boolean>? = null,
    borderColorPublisher: Flow.Publisher<out Color>? = null,
    headerAlignmentPublisher: Flow.Publisher<out Alignment>? = null,
    notesPublisher: Flow.Publisher<out String?>? = null
) : GraphicsFrame(
    headerPublisher = headerPublisher,
    borderColorPublisher = borderColorPublisher,
    headerAlignmentPublisher = headerAlignmentPublisher,
    notesPublisher = notesPublisher
) {
    private val centralPanel = JPanel()
    private val layout = Layout()
    private val items: MutableList<ItemPanel> = ArrayList()

    class Item(val text: String, val foreground: Color, val background: Color, val border: Color)

    init {
        centralPanel.background = Color.WHITE
        centralPanel.layout = layout
        add(centralPanel, BorderLayout.CENTER)

        val onNumRowsUpdate: (Int) -> Unit = { layout.numRows = it }
        numRowsPublisher.subscribe(Subscriber(eventQueueWrapper(onNumRowsUpdate)))

        val onReversedUpdate: (Boolean) -> Unit = { layout.reversed = it }
        if (reversedPublisher != null)
            reversedPublisher.subscribe(Subscriber(eventQueueWrapper(onReversedUpdate)))
        else
            onReversedUpdate(false)

        val onItemsUpdate: (List<Item>) -> Unit = { i ->
            while (i.size > items.size) {
                val item = ItemPanel()
                items.add(item)
                centralPanel.add(item)
            }
            while (i.size < items.size) {
                centralPanel.remove(items.removeAt(i.size))
            }
            i.forEachIndexed { idx, item ->
                items[idx].text = item.text
                items[idx].foreground = item.foreground
                items[idx].background = item.background
                items[idx].borderColor = item.border
            }
            repaint()
        }
        itemsPublisher.subscribe(Subscriber(eventQueueWrapper(onItemsUpdate)))
    }

    internal fun getNumRows(): Int {
        return layout.numRows
    }

    internal fun isReversed(): Boolean {
        return layout.reversed
    }

    internal fun getNumItems(): Int {
        return items.size
    }

    internal fun getText(index: Int): String {
        return items[index].text
    }

    internal fun getForeground(index: Int): Color {
        return items[index].foreground
    }

    internal fun getBackground(index: Int): Color {
        return items[index].background
    }

    internal fun getBorder(index: Int): Color {
        return items[index].borderColor
    }

    private inner class ItemPanel : JPanel() {
        private var _text = ""
        private var _borderColor = Color.WHITE

        init {
            foreground = Color.BLACK
            background = Color.WHITE
        }

        var text: String
            get() { return _text }
            set(text) {
                _text = text
                repaint()
            }

        var borderColor: Color
            get() { return _borderColor }
            set(borderColor) {
                _borderColor = borderColor
                repaint()
            }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
            g.setColor(borderColor)
            g.fillRect(0, 0, width, height)
            g.setColor(background)
            g.fillRect(3, 3, width - 6, height - 6)
            g.setColor(foreground)
            val fontSize = (height - 8).coerceAtMost(24)
            g.setFont(StandardFont.readBoldFont(fontSize))
            val oldClip = g.getClip()
            g.setClip(3, 3, width - 6, height - 6)
            g.drawString(text, 5, (height - 4 + fontSize) / 2)
            g.setClip(oldClip)
        }
    }

    private inner class Layout : LayoutManager {
        private var _numRows = 0
        private var _reversed = false

        var numRows: Int
            get() { return _numRows }
            set(numRows) {
                _numRows = numRows
                layoutContainer(centralPanel)
            }

        var reversed: Boolean
            get() { return _reversed }
            set(reversed) {
                _reversed = reversed
                layoutContainer(centralPanel)
            }

        override fun addLayoutComponent(name: String, comp: Component) {}
        override fun removeLayoutComponent(comp: Component) {}
        override fun preferredLayoutSize(parent: Container): Dimension? {
            return null
        }

        override fun minimumLayoutSize(parent: Container): Dimension? {
            return null
        }

        override fun layoutContainer(parent: Container) {
            val totalHeight = parent.height
            val totalWidth = parent.width
            val numRows = numRows.coerceAtLeast(1)
            val numCols = ceil(1.0 * items.size / numRows).toInt().coerceAtLeast(1)
            val itemHeight = totalHeight / numRows
            val itemWidth = totalWidth / numCols
            for (i in items.indices) {
                val item: ItemPanel = items[i]
                val row: Int = i % numRows
                var col: Int = i / numRows
                if (reversed) {
                    col = numCols - col - 1
                }
                item.setSize(itemWidth - 4, itemHeight - 4)
                item.setLocation(col * itemWidth + 2, row * itemHeight + 2)
            }
        }
    }
}
