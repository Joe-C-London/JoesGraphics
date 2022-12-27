package com.joecollins.graphics.components

import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.LayoutManager
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder

class FiguresFrame(
    headerPublisher: Flow.Publisher<out String?>,
    entriesPublisher: Flow.Publisher<out List<Entry>>
) : GraphicsFrame(
    headerPublisher = headerPublisher
) {

    class Entry(
        val color: Color,
        val name: String,
        val description: String,
        val result: String,
        val resultColor: Color
    )

    private val centralPanel: JPanel = JPanel()
    private val entries: MutableList<EntryPanel> = ArrayList()

    internal val numEntries: Int
        get() = entries.size

    internal fun getColor(index: Int): Color {
        return entries[index].foreground
    }

    internal fun getName(index: Int): String {
        return entries[index].nameLabel.text
    }

    internal fun getDescription(index: Int): String {
        return entries[index].descriptionLabel.text
    }

    internal fun getResult(index: Int): String {
        return entries[index].resultLabel.text
    }

    internal fun getResultColor(index: Int): Color {
        return entries[index].resultPanel.background
    }

    private inner class EntryPanel : JPanel() {
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

        val onEntriesUpdate: (List<Entry>) -> Unit = { e ->
            while (entries.size < e.size) {
                val entry = EntryPanel()
                centralPanel.add(entry)
                entries.add(entry)
            }
            while (entries.size > e.size) {
                centralPanel.remove(entries.removeAt(e.size))
            }
            e.forEachIndexed { idx, entry ->
                entries[idx].foreground = ColorUtils.contrastForBackground(entry.color)
                entries[idx].nameLabel.text = entry.name
                entries[idx].descriptionLabel.text = entry.description
                entries[idx].resultLabel.text = entry.result
                entries[idx].resultPanel.background = entry.resultColor
                entries[idx].resultLabel.foreground = ColorUtils.foregroundToContrast(entry.resultColor)
            }
        }
        entriesPublisher.subscribe(Subscriber(eventQueueWrapper(onEntriesUpdate)))
    }
}
