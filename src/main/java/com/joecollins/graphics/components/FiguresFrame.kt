package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.utils.StandardFont
import java.awt.BorderLayout
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

class FiguresFrame : GraphicsFrame() {
    private var numEntriesBinding: Binding<Int> = Binding.fixedBinding(0)
    private var colorBinding = IndexedBinding.emptyBinding<Color>()
    private var nameBinding = IndexedBinding.emptyBinding<String>()
    private var descriptionBinding = IndexedBinding.emptyBinding<String>()
    private var resultBinding = IndexedBinding.emptyBinding<String>()
    private var resultColorBinding = IndexedBinding.emptyBinding<Color>()
    private val centralPanel: JPanel = JPanel()
    private val entries: MutableList<Entry> = ArrayList()

    fun setNumEntriesBinding(numEntriesBinding: Binding<Int>) {
        this.numEntriesBinding.unbind()
        this.numEntriesBinding = numEntriesBinding
        this.numEntriesBinding.bind { size: Int ->
            while (entries.size < size) {
                val entry = Entry()
                centralPanel.add(entry)
                entries.add(entry)
            }
            while (entries.size > size) {
                centralPanel.remove(entries.removeAt(size))
            }
        }
    }

    internal val numEntries: Int
        get() = entries.size

    fun setColorBinding(colorBinding: IndexedBinding<Color>) {
        this.colorBinding.unbind()
        this.colorBinding = colorBinding
        this.colorBinding.bind { idx, color -> entries[idx].foreground = color }
    }

    internal fun getColor(index: Int): Color {
        return entries[index].foreground
    }

    fun setNameBinding(nameBinding: IndexedBinding<String>) {
        this.nameBinding.unbind()
        this.nameBinding = nameBinding
        this.nameBinding.bind { idx, name -> entries[idx].nameLabel.text = name }
    }

    internal fun getName(index: Int): String {
        return entries[index].nameLabel.text
    }

    fun setDescriptionBinding(descriptionBinding: IndexedBinding<String>) {
        this.descriptionBinding.unbind()
        this.descriptionBinding = descriptionBinding
        this.descriptionBinding.bind { idx, desc -> entries[idx].descriptionLabel.text = desc }
    }

    internal fun getDescription(index: Int): String {
        return entries[index].descriptionLabel.text
    }

    fun setResultBinding(resultBinding: IndexedBinding<String>) {
        this.resultBinding.unbind()
        this.resultBinding = resultBinding
        this.resultBinding.bind { idx, result -> entries[idx].resultLabel.text = result }
    }

    internal fun getResult(index: Int): String {
        return entries[index].resultLabel.text
    }

    fun setResultColorBinding(resultColorBinding: IndexedBinding<Color>) {
        this.resultColorBinding.unbind()
        this.resultColorBinding = resultColorBinding
        this.resultColorBinding.bind { idx, color -> entries[idx].resultPanel.background = color }
    }

    internal fun getResultColor(index: Int): Color {
        return entries[index].resultPanel.background
    }

    private inner class Entry : JPanel() {
        val nameLabel: FontSizeAdjustingLabel
        val descriptionLabel: FontSizeAdjustingLabel
        val resultPanel: JPanel
        val resultLabel: FontSizeAdjustingLabel

        init {
            foreground = Color.LIGHT_GRAY
            background = Color.WHITE
            layout = EntryLayout()
            nameLabel = FontSizeAdjustingLabel()
            nameLabel.font = StandardFont.readBoldFont(15)
            nameLabel.border = EmptyBorder(2, 0, -2, 0)
            add(nameLabel)
            descriptionLabel = FontSizeAdjustingLabel()
            descriptionLabel.font = StandardFont.readNormalFont(10)
            descriptionLabel.border = EmptyBorder(1, 0, -1, 0)
            add(descriptionLabel)
            resultPanel = JPanel()
            resultPanel.layout = GridLayout(1, 1)
            resultPanel.background = Color.LIGHT_GRAY
            add(resultPanel)
            resultLabel = FontSizeAdjustingLabel()
            resultLabel.font = StandardFont.readBoldFont(20)
            resultLabel.foreground = Color.WHITE
            resultLabel.border = EmptyBorder(3, 0, -3, 0)
            resultLabel.horizontalAlignment = JLabel.CENTER
            resultPanel.add(resultLabel)
        }

        @Suppress("SENSELESS_COMPARISON") // labels may not be initialised yet
        override fun setForeground(fg: Color) {
            super.setForeground(fg)
            border = MatteBorder(1, 1, 1, 1, fg)
            if (nameLabel != null) nameLabel.foreground = fg
            if (descriptionLabel != null) descriptionLabel.foreground = fg
        }

        private inner class EntryLayout : LayoutManager {
            override fun addLayoutComponent(name: String, comp: Component) {}
            override fun removeLayoutComponent(comp: Component) {}
            override fun preferredLayoutSize(parent: Container): Dimension {
                return Dimension(1024, 30)
            }

            override fun minimumLayoutSize(parent: Container): Dimension {
                return Dimension(50, 15)
            }

            override fun layoutContainer(parent: Container) {
                val width = parent.width
                val height = parent.height
                nameLabel.setLocation(2, 0)
                nameLabel.setSize(width * 2 / 3 - 4, height * 3 / 5)
                nameLabel.font = StandardFont.readBoldFont(height / 2)
                descriptionLabel.setLocation(2, height * 3 / 5)
                descriptionLabel.setSize(width * 2 / 3 - 4, height * 2 / 5)
                descriptionLabel.font = StandardFont.readNormalFont(height / 3)
                resultPanel.setLocation(width * 2 / 3, 1)
                resultPanel.setSize(width / 3, height - 2)
                resultLabel.font = StandardFont.readBoldFont(height * 2 / 3)
            }
        }
    }

    private inner class FrameLayout : LayoutManager {
        override fun addLayoutComponent(name: String, comp: Component) {}
        override fun removeLayoutComponent(comp: Component) {}
        override fun preferredLayoutSize(parent: Container): Dimension {
            return Dimension(1024, 30 * entries.size)
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            return Dimension(50, 15 * entries.size)
        }

        override fun layoutContainer(parent: Container) {
            val entryHeight = (parent.height / entries.size.coerceAtLeast(1)).coerceAtMost(30)
            for (i in entries.indices) {
                entries[i].setLocation(0, entryHeight * i)
                entries[i].setSize(parent.width, entryHeight)
            }
        }
    }

    init {
        centralPanel.background = Color.WHITE
        centralPanel.layout = FrameLayout()
        add(centralPanel, BorderLayout.CENTER)
    }
}
