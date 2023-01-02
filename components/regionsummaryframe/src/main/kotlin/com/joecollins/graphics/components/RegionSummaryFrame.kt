package com.joecollins.graphics.components

import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.merge
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.RenderingHints
import java.util.concurrent.Flow
import javax.swing.JPanel

class RegionSummaryFrame private constructor(
    headerPublisher: Flow.Publisher<out String>,
    sectionsPublisher: Flow.Publisher<out List<Section>>,
    summaryColorPublisher: Flow.Publisher<out Color>,
    borderColorPublisher: Flow.Publisher<out Color>? = null,
) : GraphicsFrame(
    headerPublisher = headerPublisher,
    borderColorPublisher = borderColorPublisher,
) {
    constructor(
        headerPublisher: Flow.Publisher<out String>,
        sectionsPublisher: Flow.Publisher<out List<SectionWithoutColor>>,
        summaryColorPublisher: Flow.Publisher<out Color>,
    ) : this(
        headerPublisher,
        sectionsPublisher.merge(summaryColorPublisher) { sections, color ->
            sections.map { s -> Section(s.header, s.value.map { Pair(color, it) }) }
        },
        summaryColorPublisher,
        summaryColorPublisher,
    )

    constructor(
        headerPublisher: Flow.Publisher<out String>,
        sectionsPublisher: Flow.Publisher<out List<Section>>,
    ) : this(
        headerPublisher,
        sectionsPublisher,
        Color.BLACK.asOneTimePublisher(),
        Color.BLACK.asOneTimePublisher(),
    )

    private val centralPanel: JPanel = JPanel()

    class Section(val header: String, val valueColor: List<Pair<Color, String>>)
    class SectionWithoutColor(val header: String, val value: List<String>)

    private var summaryColor = Color.BLACK
    private val sections: MutableList<SectionPanel> = ArrayList()

    init {
        centralPanel.background = Color.WHITE
        centralPanel.layout = GridLayout(0, 1)
        add(centralPanel, BorderLayout.CENTER)

        val onSummaryColorUpdate: (Color) -> Unit = { color ->
            summaryColor = color
        }
        summaryColorPublisher.subscribe(Subscriber(eventQueueWrapper(onSummaryColorUpdate)))

        val onSectionsUpdate: (List<Section>) -> Unit = { s ->
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
        sectionsPublisher.subscribe(Subscriber(eventQueueWrapper(onSectionsUpdate)))
    }

    internal fun getNumSections(): Int {
        return sections.size
    }

    internal fun getSummaryColor(): Color {
        return summaryColor
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

    private inner class SectionPanel : JPanel() {
        var header: String = ""
            set(value) {
                field = value
                repaint()
            }

        var values: List<Pair<Color, String>> = emptyList()
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
            var valueFont: Font
            val startFontSize = 61.coerceAtMost(height * 2 / 3 - 9)
            val valueFonts = ArrayList<Int>()
            for (i in values.indices) {
                g.setColor(ColorUtils.contrastForBackground(values[i].first))
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
                    height / 3 + (startFontSize + fontSize) / 2,
                )
                valueFonts.add(fontSize)
            }
            g.setColor(ColorUtils.contrastForBackground(summaryColor))
            var headerFontSize = 30.coerceAtMost(height / 3 - 5).coerceAtMost((valueFonts.maxOrNull() ?: Int.MAX_VALUE) / 2)
            var headerFont: Font
            var headerWidth: Int
            do {
                headerFont = StandardFont.readBoldFont(headerFontSize)
                g.setFont(headerFont)
                headerWidth = g.getFontMetrics(headerFont).stringWidth(header)
                headerFontSize--
            } while (headerWidth > width - 20)
            g.drawString(header, (width - headerWidth) / 2, height / 3 - 5)
        }

        init {
            background = Color.WHITE
        }
    }
}
