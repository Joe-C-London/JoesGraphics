package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.utils.StandardFont
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.LayoutManager
import java.util.ArrayList
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder

class SummaryWithoutLabels : JPanel() {
    private var headlineBinding: Binding<String> = Binding.fixedBinding("RESULT")
    private var numEntriesBinding: Binding<Int> = Binding.fixedBinding(1)
    private var entriesBinding = IndexedBinding.singletonBinding(Entry(Color.WHITE, "WAITING..."))
    private val headlinePanel: HeadlinePanel = HeadlinePanel()
    private val entryPanels: MutableList<EntryPanel> = ArrayList()

    class Entry(val color: Color, val value: String)

    fun setNumEntriesBinding(numEntriesBinding: Binding<Int>) {
        this.numEntriesBinding.unbind()
        this.numEntriesBinding = numEntriesBinding
        this.numEntriesBinding.bind { size ->
            while (entryPanels.size < size) {
                val newPanel = EntryPanel()
                add(newPanel)
                entryPanels.add(newPanel)
            }
            while (entryPanels.size > size) {
                remove(entryPanels.removeAt(size))
            }
        }
    }

    internal val headline: String
        get() = headlinePanel.top

    fun setHeadlineBinding(headlineBinding: Binding<String>) {
        this.headlineBinding.unbind()
        this.headlineBinding = headlineBinding
        this.headlineBinding.bind { text -> headlinePanel.top = text }
    }

    internal val numEntries: Int
        get() = entryPanels.size

    fun setEntriesBinding(entriesBinding: IndexedBinding<Entry>) {
        this.entriesBinding.unbind()
        this.entriesBinding = entriesBinding
        this.entriesBinding.bind { idx, entry ->
            entryPanels[idx].bottomPanel.background = entry.color
            entryPanels[idx].bottomLabel.foreground = if (entry.color == Color.WHITE) Color.BLACK else Color.WHITE
            entryPanels[idx].bottomLabel.text = entry.value
        }
    }

    internal fun getEntryColor(index: Int): Color {
        return entryPanels[index].bottomPanel.background
    }

    internal fun getEntryValue(index: Int): String {
        return entryPanels[index].bottomLabel.text
    }

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
        set(top) { topLabel.text = top }

        init {
            add(topPanel)
            topPanel.add(topLabel)
            layout = GridLayout(1, 1)
        }
    }

    private inner class EntryPanel : JPanel() {
        val bottomPanel: JPanel = object : JPanel() {
            init {
                background = Color.WHITE
                layout = GridLayout(1, 1)
            }
        }
        val bottomLabel: JLabel = object : JLabel() {
            init {
                font = StandardFont.readBoldFont(24)
                horizontalAlignment = CENTER
                foreground = Color.BLACK
                border = EmptyBorder(4, 0, -4, 0)
            }
        }

        init {
            add(bottomPanel)
            bottomPanel.add(bottomLabel)
            layout = GridLayout(1, 1)
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
            for (i in entryPanels.indices) {
                val left = width * i / entryPanels.size
                val right = width * (i + 1) / entryPanels.size
                entryPanels[i].setLocation(left + 1, mid + 1)
                entryPanels[i].setSize(right - left, height - mid)
            }
        }
    }

    init {
        background = Color.WHITE
        layout = SummaryLayout()
        border = MatteBorder(1, 1, 1, 1, Color.BLACK)
        setNumEntriesBinding(numEntriesBinding)
        setEntriesBinding(entriesBinding)
        add(headlinePanel)
    }
}
