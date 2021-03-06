package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.utils.StandardFont
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.RenderingHints
import java.util.ArrayList
import javax.swing.JPanel
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair

class RegionSummaryFrame : GraphicsFrame() {
    private val centralPanel: JPanel = JPanel()

    private var summaryColorBinding = Binding.fixedBinding(Color.BLACK)
    private var numSectionsBinding = Binding.fixedBinding(0)
    private var sectionHeaderBinding = IndexedBinding.emptyBinding<String>()
    private var sectionValueColorBinding = IndexedBinding.emptyBinding<List<Pair<Color, String>>>()

    private var summaryColor = Color.BLACK
    private val sections: MutableList<SectionPanel> = ArrayList()

    init {
        centralPanel.background = Color.WHITE
        centralPanel.layout = GridLayout(0, 1)
        add(centralPanel, BorderLayout.CENTER)
    }

    internal fun getNumSections(): Int {
        return sections.size
    }

    fun setNumSectionsBinding(numSectionsBinding: Binding<Int>) {
        this.numSectionsBinding.unbind()
        this.numSectionsBinding = numSectionsBinding
        this.numSectionsBinding.bind { numSections ->
            while (sections.size < numSections) {
                val newPanel = SectionPanel()
                centralPanel.add(newPanel)
                sections.add(newPanel)
            }
            while (sections.size > numSections) {
                centralPanel.remove(sections.removeAt(numSections))
            }
        }
    }

    internal fun getSummaryColor(): Color {
        return summaryColor
    }

    fun setSummaryColorBinding(summaryColorBinding: Binding<Color>) {
        this.summaryColorBinding.unbind()
        this.summaryColorBinding = summaryColorBinding
        this.summaryColorBinding.bind { color ->
            summaryColor = color
            sections.forEach { applySummaryColor(it, color) }
        }
    }

    private fun applySummaryColor(
        panel: SectionPanel,
        color: Color
    ) {
        panel.values = panel.values
            .map { ImmutablePair.of(color, it.right) }
            .toList()
    }

    internal fun getSectionHeader(idx: Int): String {
        return sections[idx].header
    }

    fun setSectionHeaderBinding(sectionHeaderBinding: IndexedBinding<String>) {
        this.sectionHeaderBinding.unbind()
        this.sectionHeaderBinding = sectionHeaderBinding
        this.sectionHeaderBinding.bind { idx, header -> sections[idx].header = header }
    }

    internal fun getValueColor(sectionIdx: Int, valueIdx: Int): Color {
        return sections[sectionIdx].values[valueIdx].left
    }

    internal fun getValue(sectionIdx: Int, valueIdx: Int): String {
        return sections[sectionIdx].values[valueIdx].right
    }

    fun setSectionValueColorBinding(
        sectionValueColorBinding: IndexedBinding<List<Pair<Color, String>>>
    ) {
        this.sectionValueColorBinding.unbind()
        this.sectionValueColorBinding = sectionValueColorBinding
        this.sectionValueColorBinding.bind { idx, values -> sections[idx].values = values }
    }

    fun setSectionValueBinding(sectionValueBinding: IndexedBinding<List<String>>) {
        setSectionValueColorBinding(
            sectionValueBinding.map { values: List<String> ->
                values
                    .map { ImmutablePair.of(summaryColor, it) }
                    .toList()
            })
    }

    private inner class SectionPanel : JPanel() {
        private var _header: String = ""
        private var _values: List<Pair<Color, String>> = emptyList()

        var header: String
        get() { return _header }
        set(header) {
            _header = header
            repaint()
        }

        var values: List<Pair<Color, String>>
        get() { return _values }
        set(values) {
            _values = values
            repaint()
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
            g.setColor(summaryColor)
            val headerFont = StandardFont.readBoldFont(30.coerceAtMost(height / 3 - 5))
            g.setFont(headerFont)
            val headerWidth = g.getFontMetrics(headerFont).stringWidth(header)
            g.drawString(header, (width - headerWidth) / 2, height / 3 - 5)
            var valueFont: Font
            val startFontSize = 61.coerceAtMost(height * 2 / 3 - 9)
            for (i in values.indices) {
                g.setColor(values[i].left)
                val value = values[i].right
                var valueWidth: Int
                var fontSize = startFontSize
                do {
                    fontSize--
                    valueFont = StandardFont.readBoldFont(fontSize)
                    valueWidth = g.getFontMetrics(valueFont).stringWidth(value)
                } while (valueWidth > width / values.size - 20)
                g.setFont(valueFont)
                g.drawString(
                    value,
                    (width / values.size - valueWidth) / 2 + width / values.size * i,
                    height / 3 + (startFontSize + fontSize) / 2
                )
            }
        }

        init {
            background = Color.WHITE
        }
    }
}
