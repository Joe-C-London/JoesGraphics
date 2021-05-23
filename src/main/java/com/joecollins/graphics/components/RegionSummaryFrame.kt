package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.mapElements
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

class RegionSummaryFrame(
    headerBinding: Binding<String>,
    borderColorBinding: Binding<Color>? = null
) : GraphicsFrame(
    headerBinding = headerBinding,
    borderColorBinding = borderColorBinding
) {
    private val centralPanel: JPanel = JPanel()

    class Section(val header: String, val valueColor: List<Pair<Color, String>>)
    class SectionWithoutColor(val header: String, val value: List<String>)

    private var summaryColorBinding = Binding.fixedBinding(Color.BLACK)
    private var sectionsBinding: Binding<List<Section>> = Binding.fixedBinding(emptyList())

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
            .map { Pair(color, it.second) }
            .toList()
    }

    internal fun getSectionHeader(idx: Int): String {
        return sections[idx].header
    }

    internal fun getValueColor(sectionIdx: Int, valueIdx: Int): Color {
        return sections[sectionIdx].values[valueIdx].first
    }

    internal fun getValue(sectionIdx: Int, valueIdx: Int): String {
        return sections[sectionIdx].values[valueIdx].second
    }

    fun setSectionsBinding(sectionsBinding: Binding<List<Section>>) {
        this.sectionsBinding.unbind()
        this.sectionsBinding = sectionsBinding
        this.sectionsBinding.bind { s ->
            while (sections.size < s.size) {
                val newPanel = SectionPanel()
                centralPanel.add(newPanel)
                sections.add(newPanel)
            }
            while (sections.size > s.size) {
                centralPanel.remove(sections.removeAt(s.size))
            }
            s.forEachIndexed { idx, section ->
                sections[idx].header = section.header
                sections[idx].values = section.valueColor
            }
        }
    }

    fun setSectionsBindingWithoutColors(sectionsBinding: Binding<List<SectionWithoutColor>>) {
        setSectionsBinding(sectionsBinding.mapElements { s -> Section(s.header, s.value.map { Pair(summaryColor, it) }) })
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
                g.setColor(values[i].first)
                val value = values[i].second
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
