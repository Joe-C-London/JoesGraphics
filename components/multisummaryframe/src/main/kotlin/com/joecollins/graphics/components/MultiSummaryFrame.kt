package com.joecollins.graphics.components

import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.map
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.LayoutManager
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder
import kotlin.math.max

class MultiSummaryFrame(
    headerPublisher: Flow.Publisher<out String?>,
    rowsPublisher: Flow.Publisher<out List<Row>>,
    notesPublisher: Flow.Publisher<out String?>? = null,
    progressLabel: Flow.Publisher<out String?>? = null,
) : GraphicsFrame(
    headerPublisher = headerPublisher,
    notesPublisher = notesPublisher,
    headerLabelsPublisher = progressLabel?.map { mapOf(HeaderLabelLocation.RIGHT to it) },
) {
    private val centralPanel: JPanel
    private val entries: MutableList<EntryPanel> = ArrayList()

    class Row(val header: String, val values: List<Pair<Color, String>>)

    internal val numRows: Int
        get() = entries.size

    internal fun getRowHeader(index: Int): String {
        return entries[index].headerLabel.text
    }

    internal fun getNumValues(index: Int): Int {
        return entries[index].panels.size
    }

    internal fun getColor(row: Int, col: Int): Color {
        return entries[row].panels[col].background
    }

    internal fun getValue(row: Int, col: Int): String {
        return entries[row].labels[col].text
    }

    private inner class FrameLayout : LayoutManager {
        override fun addLayoutComponent(name: String, comp: Component) {}
        override fun removeLayoutComponent(comp: Component) {}
        override fun preferredLayoutSize(parent: Container): Dimension {
            return Dimension(1024, 24 * entries.size)
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            return Dimension(50, 10 * entries.size)
        }

        override fun layoutContainer(parent: Container) {
            val entryHeight = (parent.height / entries.size.coerceAtLeast(1)).coerceAtMost(24)
            for (i in entries.indices) {
                entries[i].setLocation(0, entryHeight * i)
                entries[i].setSize(parent.width, entryHeight)
            }
        }
    }

    private inner class EntryPanel : JPanel() {
        val headerLabel: FontSizeAdjustingLabel
        val panels: MutableList<JPanel> = ArrayList()
        val labels: MutableList<JLabel> = ArrayList()

        private inner class EntryLayout : LayoutManager {
            override fun addLayoutComponent(name: String, comp: Component) {}
            override fun removeLayoutComponent(comp: Component) {}
            override fun preferredLayoutSize(parent: Container): Dimension {
                return Dimension(1024, 24)
            }

            override fun minimumLayoutSize(parent: Container): Dimension {
                return Dimension(50, 10)
            }

            override fun layoutContainer(parent: Container) {
                val width = parent.width
                val height = parent.height
                val numCells = entries.map { it.panels.size }
                    .fold(0) { a, b -> max(a, b) }
                headerLabel.setLocation(0, 1)
                headerLabel.setSize(width * 3 / (3 + numCells), height - 3)
                for (i in panels.indices) {
                    val panel = panels[i]
                    panel.setLocation(width * (3 + i) / (3 + numCells) + 1, 1)
                    panel.setSize(width / (3 + numCells) - 2, height - 3)
                }
            }
        }

        init {
            background = Color.WHITE
            layout = EntryLayout()
            border = MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY)
            headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = StandardFont.readBoldFont(16)
            headerLabel.border = EmptyBorder(4, 0, -4, 0)
            add(headerLabel)
            addComponentListener(
                object : ComponentAdapter() {
                    override fun componentResized(e: ComponentEvent) {
                        val height = height.coerceAtMost(24)
                        val font = StandardFont.readBoldFont(height * 2 / 3)
                        headerLabel.font = font
                        labels.forEach { label -> label.font = font }
                        val border = EmptyBorder(height / 6, 0, -height / 6, 0)
                        headerLabel.border = border
                        labels.forEach { label -> label.border = border }
                    }
                },
            )
        }
    }

    init {
        centralPanel = object : JPanel() {
            init {
                background = Color.WHITE
                layout = FrameLayout()
            }
        }
        addCenter(centralPanel)

        val onRowsUpdate: (List<Row>) -> Unit = { r ->
            while (entries.size < r.size) {
                val entryPanel = EntryPanel()
                centralPanel.add(entryPanel)
                entries.add(entryPanel)
            }
            while (entries.size > r.size) {
                centralPanel.remove(entries.removeAt(r.size))
            }
            r.forEachIndexed { idx, row ->
                entries[idx].headerLabel.text = row.header
                val entry = entries[idx]
                val values = row.values
                while (entry.panels.size < values.size) {
                    val panel = JPanel()
                    val label = FontSizeAdjustingLabel()
                    label.font = entry.headerLabel.font
                    label.border = entry.headerLabel.border
                    label.horizontalAlignment = JLabel.CENTER
                    panel.layout = GridLayout(1, 1)
                    panel.add(label)
                    entry.panels.add(panel)
                    entry.labels.add(label)
                    entry.add(panel)
                }
                while (entry.panels.size > values.size) {
                    entry.remove(entry.panels.removeAt(values.size))
                    entry.labels.removeAt(values.size)
                }
                for (i in values.indices) {
                    entry.panels[i].background = values[i].first
                    entry.labels[i].foreground = ColorUtils.foregroundToContrast(values[i].first)
                    entry.labels[i].text = values[i].second
                }
                entries.forEach {
                    it.invalidate()
                    it.revalidate()
                }
            }
            repaint()
        }
        rowsPublisher.subscribe(Subscriber(eventQueueWrapper(onRowsUpdate)))
    }
}
