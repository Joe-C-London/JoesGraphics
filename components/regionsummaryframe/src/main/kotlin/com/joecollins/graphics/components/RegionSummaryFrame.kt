package com.joecollins.graphics.components

import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.RenderingHints
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.image.BufferedImage
import java.util.concurrent.Flow
import javax.swing.JPanel

class RegionSummaryFrame private constructor(
    headerPublisher: Flow.Publisher<out String>,
    sectionsPublisher: Flow.Publisher<out List<Section>>,
    summaryColorPublisher: Flow.Publisher<out Color>,
    borderColorPublisher: Flow.Publisher<out Color>? = null,
    notesPublisher: Flow.Publisher<out String>? = null,
) : GraphicsFrame(
    headerPublisher = headerPublisher,
    borderColorPublisher = borderColorPublisher,
    notesPublisher = notesPublisher,
) {
    constructor(
        headerPublisher: Flow.Publisher<out String>,
        sectionsPublisher: Flow.Publisher<out List<SectionWithoutColor>>,
        summaryColorPublisher: Flow.Publisher<out Color>,
        notesPublisher: Flow.Publisher<out String>? = null,
    ) : this(
        headerPublisher,
        sectionsPublisher.merge(summaryColorPublisher) { sections, color ->
            sections.map { s -> Section(s.header, s.value.map { Pair(color, it) }) }
        },
        summaryColorPublisher,
        summaryColorPublisher,
        notesPublisher,
    )

    constructor(
        headerPublisher: Flow.Publisher<out String>,
        sectionsPublisher: Flow.Publisher<out List<Section>>,
        notesPublisher: Flow.Publisher<out String>? = null,
    ) : this(
        headerPublisher,
        sectionsPublisher,
        Color.BLACK.asOneTimePublisher(),
        Color.BLACK.asOneTimePublisher(),
        notesPublisher,
    )

    private val centralPanel: JPanel = JPanel()

    class Section(val header: String, val valueColor: List<Pair<Color, String>>)
    class SectionWithoutColor(val header: String, val value: List<String>)

    private var summaryColor = Color.BLACK
    private val sections: MutableList<SectionPanel> = ArrayList()

    init {
        centralPanel.background = Color.WHITE
        centralPanel.layout = GridLayout(0, 1)
        addCenter(centralPanel)

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
            valueFontSize = sections.minOf { it.maxValueFontSize() }
            headerFontSize = sections.minOf { it.maxHeaderFontSize() }
        }
        sectionsPublisher.subscribe(Subscriber(eventQueueWrapper(onSectionsUpdate)))
    }

    internal fun getNumSections(): Int = sections.size

    internal fun getSummaryColor(): Color = summaryColor

    internal fun getSectionHeader(idx: Int): String = sections[idx].header

    internal fun getValueColor(sectionIdx: Int, valueIdx: Int): Color = sections[sectionIdx].values[valueIdx].first

    internal fun getValue(sectionIdx: Int, valueIdx: Int): String = sections[sectionIdx].values[valueIdx].second

    var valueFontSize = 0
    var headerFontSize = 0

    private inner class SectionPanel : JPanel() {
        init {
            addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    super.componentResized(e)
                    valueFontSize = sections.minOf { it.maxValueFontSize() }
                    headerFontSize = sections.minOf { it.maxHeaderFontSize() }
                    repaint()
                }
            })
        }

        var header: String = ""
            set(value) {
                field = value
                headerFontSize = sections.minOf { it.maxHeaderFontSize() }
                repaint()
            }

        var values: List<Pair<Color, String>> = emptyList()
            set(value) {
                field = value
                valueFontSize = sections.minOf { it.maxValueFontSize() }
                headerFontSize = sections.minOf { it.maxHeaderFontSize() }
                repaint()
            }

        fun maxValueFontSize(): Int {
            val g = BufferedImage(width.coerceAtLeast(1), height.coerceAtLeast(1), BufferedImage.TYPE_INT_ARGB).graphics
            try {
                var valueFont: Font
                val startFontSize = 61.coerceAtMost(height * 2 / 3 - 5)
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
                    } while (valueWidth > width / values.size - 20 && fontSize > 0)
                    valueFonts.add(fontSize)
                }
                return valueFonts.minOrNull() ?: 0
            } finally {
                g.dispose()
            }
        }

        fun maxHeaderFontSize(): Int {
            val g = BufferedImage(width.coerceAtLeast(1), height.coerceAtLeast(1), BufferedImage.TYPE_INT_ARGB).graphics
            try {
                var headerFontSize = 24.coerceAtMost(height / 3 - 2).coerceAtMost(valueFontSize)
                var headerFont: Font
                var headerWidth: Int
                do {
                    headerFont = StandardFont.readBoldFont(headerFontSize)
                    g.setFont(headerFont)
                    headerWidth = g.getFontMetrics(headerFont).stringWidth(header)
                    headerFontSize--
                } while (headerWidth > width - 20 && headerFontSize > 0)
                return headerFontSize
            } finally {
                g.dispose()
            }
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                )
            val startFontSize = 61.coerceAtMost(height * 2 / 3 - 5)
            val valueFont = StandardFont.readBoldFont(valueFontSize)
            for (i in values.indices) {
                g.setColor(ColorUtils.contrastForBackground(values[i].first))
                val value = values[i].second
                val valueWidth = g.getFontMetrics(valueFont).stringWidth(value)
                g.setFont(StandardFont.readBoldFont(valueFontSize))
                g.drawString(
                    value,
                    (width / values.size - valueWidth) / 2 + width / values.size * i,
                    height / 3 + (startFontSize + valueFontSize) / 2,
                )
            }
            g.setColor(ColorUtils.contrastForBackground(summaryColor))
            val headerFont = StandardFont.readBoldFont(headerFontSize)
            val headerWidth = g.getFontMetrics(headerFont).stringWidth(header)
            g.font = headerFont
            g.drawString(header, (width - headerWidth) / 2, height / 3 - 2)
        }

        init {
            background = Color.WHITE
        }
    }
}
