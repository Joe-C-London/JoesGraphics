package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.LayoutManager
import java.awt.Point
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.roundToInt

class BottomSummary(
    private val headerPublisher: Flow.Publisher<out String>,
    private val footerPublisher: Flow.Publisher<out String>,
    private val entriesPublisher: Flow.Publisher<out List<SummaryEntry>>,
) : JPanel() {
    private val headerPanel: LabelWithBackground = LabelWithBackground()
    private val footerPanel: LabelWithBackground = LabelWithBackground()
    private val entryPanels: MutableList<EntryPanel> = ArrayList()

    internal val header: String
        get() = headerPanel.text

    internal val footer: String
        get() = footerPanel.text

    internal val numEntries: Int
        get() = entryPanels.size

    internal fun getEntryColor(index: Int): Color = entryPanels[index].background

    internal fun getEntryLabel(index: Int): String = entryPanels[index].bottomHeaderLabel.text

    internal fun getEntryValue(index: Int): String = entryPanels[index].bottomValueLabel.text

    private inner class LabelWithBackground : JPanel() {
        init {
            background = Color.BLACK
            layout = GridLayout(1, 1)
        }

        private val label: JLabel = FontSizeAdjustingLabel().also {
            it.font = FONT
            it.horizontalAlignment = JLabel.CENTER
            it.foreground = Color.WHITE
            it.border = BORDER
        }

        var text: String
            get() {
                return label.text
            }
            set(top) {
                label.text = top
            }

        var textAlign: Int
            get() {
                return label.horizontalAlignment
            }
            set(align) {
                label.horizontalAlignment = align
            }

        init {
            background = Color.BLACK
            layout = GridLayout(1, 1)
            add(label)
        }
    }

    private inner class EntryPanel : JPanel() {
        init {
            background = Color.WHITE
            layout = GridLayout(1, 2)
        }
        val bottomHeaderLabel: JLabel = FontSizeAdjustingLabel().also {
            it.font = FONT
            it.horizontalAlignment = JLabel.LEFT
            it.foreground = Color.BLACK
            it.border = BORDER
        }
        val bottomValueLabel: JLabel = FontSizeAdjustingLabel().also {
            it.font = FONT
            it.horizontalAlignment = JLabel.RIGHT
            it.foreground = Color.BLACK
            it.border = BORDER
        }

        init {
            add(bottomHeaderLabel)
            add(bottomValueLabel)
        }
    }

    init {
        background = Color.WHITE
        layout = object : LayoutManager {
            override fun addLayoutComponent(name: String, comp: Component) {
            }

            override fun removeLayoutComponent(comp: Component) {
            }

            override fun preferredLayoutSize(parent: Container): Dimension = Dimension(1024, 20)

            override fun minimumLayoutSize(parent: Container): Dimension = Dimension(1024, 20)

            override fun layoutContainer(parent: Container) {
                val width = parent.width
                val height = parent.height

                val headerSize = 200
                headerPanel.location = Point(0, 0)
                headerPanel.size = Dimension(headerSize, height)

                val footerSize = 100
                footerPanel.location = Point(width - footerSize, 0)
                footerPanel.size = Dimension(footerSize, height)

                val centerSize = width - headerSize - footerSize
                val panelWidth = centerSize.toDouble() / entryPanels.size.coerceAtLeast(1)
                entryPanels.forEachIndexed { index, entryPanel ->
                    val left = (panelWidth * index).roundToInt()
                    val right = (panelWidth * (index + 1)).roundToInt()
                    entryPanel.location = Point(left + headerSize, 0)
                    entryPanel.size = Dimension(right - left, height)
                }
            }
        }
        preferredSize = Dimension(1024, 20)

        headerPanel.textAlign = JLabel.RIGHT
        headerPanel.preferredSize = Dimension(200, 20)
        add(headerPanel)

        footerPanel.textAlign = JLabel.LEFT
        footerPanel.preferredSize = Dimension(100, 20)
        add(footerPanel)

        this.headerPublisher.subscribe(Subscriber(eventQueueWrapper { text -> headerPanel.text = text }))
        this.footerPublisher.subscribe(Subscriber(eventQueueWrapper { text -> footerPanel.text = text }))
        this.entriesPublisher.subscribe(
            Subscriber(
                eventQueueWrapper { entries ->
                    while (entryPanels.size < entries.size) {
                        val newPanel = EntryPanel()
                        add(newPanel)
                        entryPanels.add(newPanel)
                    }
                    while (entryPanels.size > entries.size) {
                        remove(entryPanels.removeAt(entries.size))
                    }
                    entries.forEachIndexed { idx, entry ->
                        entryPanels[idx].background = entry.color
                        val foreground = ColorUtils.foregroundToContrast(entry.color)
                        entryPanels[idx].bottomHeaderLabel.foreground = foreground
                        entryPanels[idx].bottomHeaderLabel.text = entry.label
                        entryPanels[idx].bottomValueLabel.foreground = foreground
                        entryPanels[idx].bottomValueLabel.text = entry.value
                    }
                    invalidate()
                    revalidate()
                    repaint()
                },
            ),
        )
    }

    companion object {
        private val FONT = StandardFont.readNormalFont(12)
        private val BORDER = EmptyBorder(3, 2, -3, 2)
    }
}
