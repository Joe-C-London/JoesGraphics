package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.utils.StandardFont
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
import javax.swing.JPanel
import kotlin.math.ceil

class ResultListingFrame : GraphicsFrame() {
    private val centralPanel = JPanel()
    private val layout = Layout()
    private val items: MutableList<Item> = ArrayList()

    private var numRowsBinding: Binding<Int> = Binding.fixedBinding(0)
    private var reversedBinding: Binding<Boolean> = Binding.fixedBinding(false)
    private var numItemsBinding: Binding<Int> = Binding.fixedBinding(0)
    private var textBinding = IndexedBinding.emptyBinding<String>()
    private var foregroundBinding = IndexedBinding.emptyBinding<Color>()
    private var backgroundBinding = IndexedBinding.emptyBinding<Color>()
    private var borderBinding = IndexedBinding.emptyBinding<Color>()

    init {
        centralPanel.background = Color.WHITE
        centralPanel.layout = layout
        add(centralPanel, BorderLayout.CENTER)
    }

    protected fun getNumRows(): Int {
        return layout.numRows
    }

    fun setNumRowsBinding(numRowsBinding: Binding<Int>) {
        this.numRowsBinding.unbind()
        this.numRowsBinding = numRowsBinding
        this.numRowsBinding.bind { layout.numRows = it }
    }

    protected fun isReversed(): Boolean {
        return layout.reversed
    }

    fun setReversedBinding(reversedBinding: Binding<Boolean>) {
        this.reversedBinding.unbind()
        this.reversedBinding = reversedBinding
        this.reversedBinding.bind { layout.reversed = it }
    }

    protected fun getNumItems(): Int {
        return items.size
    }

    fun setNumItemsBinding(numItemsBinding: Binding<Int>) {
        this.numItemsBinding.unbind()
        this.numItemsBinding = numItemsBinding
        this.numItemsBinding.bind { numItems ->
            while (numItems > items.size) {
                val item = Item()
                items.add(item)
                centralPanel.add(item)
            }
            while (numItems < items.size) {
                centralPanel.remove(items.removeAt(numItems))
            }
            repaint()
        }
    }

    protected fun getText(index: Int): String {
        return items[index].text
    }

    fun setTextBinding(textBinding: IndexedBinding<String>) {
        this.textBinding.unbind()
        this.textBinding = textBinding
        this.textBinding.bind { idx, text -> items[idx].text = text }
    }

    protected fun getForeground(index: Int): Color {
        return items[index].foreground
    }

    fun setForegroundBinding(foregroundBinding: IndexedBinding<Color>) {
        this.foregroundBinding.unbind()
        this.foregroundBinding = foregroundBinding
        this.foregroundBinding.bind { idx, color -> items[idx].foreground = color }
    }

    protected fun getBackground(index: Int): Color {
        return items[index].background
    }

    fun setBackgroundBinding(backgroundBinding: IndexedBinding<Color>) {
        this.backgroundBinding.unbind()
        this.backgroundBinding = backgroundBinding
        this.backgroundBinding.bind { idx, color -> items[idx].background = color }
    }

    protected fun getBorder(index: Int): Color {
        return items[index].borderColor
    }

    fun setBorderBinding(borderBinding: IndexedBinding<Color>) {
        this.borderBinding.unbind()
        this.borderBinding = borderBinding
        this.borderBinding.bind { idx, color -> items[idx].borderColor = color }
    }

    private inner class Item : JPanel() {
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
                val item: Item = items[i]
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
