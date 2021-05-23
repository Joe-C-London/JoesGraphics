package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding
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

class SummaryWithLabels(
    private val entriesBinding: Binding<List<Entry>>
) : JPanel() {
    private val entryPanels: MutableList<EntryPanel> = ArrayList()

    class Entry(val color: Color, val label: String, val value: String)

    internal val numEntries: Int
        get() = entryPanels.size

    internal fun getEntryColor(index: Int): Color {
        return entryPanels[index].bottomPanel.background
    }

    internal fun getEntryLabel(index: Int): String {
        return entryPanels[index].topLabel.text
    }

    internal fun getEntryValue(index: Int): String {
        return entryPanels[index].bottomLabel.text
    }

    private inner class EntryPanel : JPanel() {
        private val topPanel: JPanel = object : JPanel() {
            init {
                background = Color.BLACK
                layout = GridLayout(1, 1)
            }
        }
        val topLabel: JLabel = object : JLabel() {
            init {
                font = StandardFont.readNormalFont(16)
                horizontalAlignment = CENTER
                foreground = Color.WHITE
                border = EmptyBorder(3, 0, -3, 0)
            }
        }
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

        private inner class EntryLayout : LayoutManager {
            override fun addLayoutComponent(name: String, comp: Component) {}
            override fun removeLayoutComponent(comp: Component) {}
            override fun preferredLayoutSize(parent: Container): Dimension {
                return Dimension(50, 50)
            }

            override fun minimumLayoutSize(parent: Container): Dimension {
                return Dimension(10, 50)
            }

            override fun layoutContainer(parent: Container) {
                val width = parent.width
                val height = parent.height
                val mid = height * 2 / 5
                topPanel.setLocation(0, 0)
                topPanel.setSize(width, mid)
                bottomPanel.setLocation(0, mid)
                bottomPanel.setSize(width, height - mid)
            }
        }

        init {
            add(topPanel)
            add(bottomPanel)
            topPanel.add(topLabel)
            bottomPanel.add(bottomLabel)
            layout = EntryLayout()
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
            for (i in entryPanels.indices) {
                val left = width * i / entryPanels.size
                val right = width * (i + 1) / entryPanels.size
                entryPanels[i].setLocation(left + 1, 1)
                entryPanels[i].setSize(right - left, height)
            }
        }
    }

    init {
        background = Color.WHITE
        layout = SummaryLayout()
        border = MatteBorder(1, 1, 1, 1, Color.BLACK)

        this.entriesBinding.bind { entries: List<Entry> ->
            while (entryPanels.size < entries.size) {
                val newPanel = EntryPanel()
                add(newPanel)
                entryPanels.add(newPanel)
            }
            while (entryPanels.size > entries.size) {
                remove(entryPanels.removeAt(entries.size))
            }
            entries.onEachIndexed { idx, entry ->
                entryPanels[idx].topLabel.foreground = if (entry.color == Color.BLACK) Color.WHITE else entry.color
                entryPanels[idx].bottomPanel.background = entry.color
                entryPanels[idx].bottomLabel.foreground = if (entry.color == Color.WHITE) Color.BLACK else Color.WHITE
                entryPanels[idx].topLabel.text = entry.label
                entryPanels[idx].bottomLabel.text = entry.value
            }
        }
    }
}
