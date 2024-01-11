package com.joecollins.graphics.components

import com.joecollins.graphics.components.FontSizeAdjustingLabel.Companion.equaliseFonts
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.map
import org.jetbrains.annotations.TestOnly
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.LayoutManager
import java.awt.Point
import java.util.concurrent.Flow
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder
import kotlin.math.max
import kotlin.math.roundToInt

@Suppress("LeakingThis")
open class GraphicsFrame(
    headerPublisher: Flow.Publisher<out String?>,
    notesPublisher: Flow.Publisher<out String?>? = null,
    borderColorPublisher: Flow.Publisher<out Color>? = null,
    headerAlignmentPublisher: Flow.Publisher<out Alignment>? = null,
    headerTextColorPublisher: Flow.Publisher<out Color>? = null,
    headerLabelsPublisher: Flow.Publisher<out Map<HeaderLabelLocation, String?>>? = null,
) : JPanel() {

    enum class Alignment(internal val jlabelAlignment: Int) {
        LEFT(JLabel.LEFT),
        CENTER(JLabel.CENTER),
        RIGHT(JLabel.RIGHT),
    }

    enum class HeaderLabelLocation {
        LEFT,
        RIGHT,
    }

    companion object {
        private val HEADER_FONT = StandardFont.readNormalFont(24)
        private val ADDITIONAL_HEADER_FONT = StandardFont.readNormalFont(20)
        private val ADDITIONAL_HEADER_BORDER = { c: Color -> MatteBorder(2, 2, 2, 2, c) }

        fun Collection<GraphicsFrame>.equaliseHeaderFonts() {
            map { it.headerLabel }.equaliseFonts()
        }
    }

    private val headerPanel: JPanel
    private val headerLabel: FontSizeAdjustingLabel
    private val notesLabel: JLabel
    private val additionalHeaderPanels: Map<HeaderLabelLocation, JPanel>
    private val additionalHeaderLabels: Map<HeaderLabelLocation, FontSizeAdjustingLabel>

    init {
        layout = BorderLayout()
        background = Color.WHITE
        border = MatteBorder(1, 1, 1, 1, Color.BLACK)
        maximumSize = Dimension(Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())
        preferredSize = Dimension(1024, 1024)
        minimumSize = Dimension(1, 1)

        headerPanel = JPanel().apply {
            layout = HeaderLayout()
            background = Color.BLACK
        }
        add(headerPanel, BorderLayout.NORTH)

        headerLabel = FontSizeAdjustingLabel().apply {
            foreground = Color.WHITE
            horizontalAlignment = JLabel.CENTER
            font = HEADER_FONT
            border = EmptyBorder(3, 0, -3, 0)
        }
        headerPanel.add(headerLabel)

        notesLabel = FontSizeAdjustingLabel().apply {
            foreground = Color.BLACK
            horizontalAlignment = JLabel.RIGHT
            font = StandardFont.readNormalFont(12)
            border = EmptyBorder(2, 0, -2, 0)
        }
        add(notesLabel, BorderLayout.SOUTH)

        additionalHeaderLabels = HeaderLabelLocation.values().associateWith {
            FontSizeAdjustingLabel().apply {
                font = ADDITIONAL_HEADER_FONT
                horizontalAlignment = JLabel.CENTER
                border = EmptyBorder(3, 0, -3, 0)
                foreground = Color.BLACK
            }
        }

        additionalHeaderPanels = HeaderLabelLocation.values().associateWith {
            JPanel().apply {
                layout = GridLayout(1, 1)
                background = Color.WHITE
                border = ADDITIONAL_HEADER_BORDER(Color.BLACK)
                isVisible = false
                add(additionalHeaderLabels[it]!!)
            }.also { panel -> headerPanel.add(panel) }
        }

        headerPublisher.subscribe(
            Subscriber(
                eventQueueWrapper {
                    headerPanel.isVisible = (it != null)
                    headerLabel.text = it ?: ""
                    headerPanel.invalidate()
                    headerPanel.revalidate()
                    repaint()
                },
            ),
        )

        headerAlignmentPublisher?.subscribe(
            Subscriber(
                eventQueueWrapper {
                    headerLabel.horizontalAlignment = it.jlabelAlignment
                    headerPanel.invalidate()
                    headerPanel.revalidate()
                    repaint()
                },
            ),
        )

        notesPublisher?.subscribe(
            Subscriber(
                eventQueueWrapper {
                    notesLabel.isVisible = (it != null)
                    notesLabel.text = (it ?: "") + " "
                },
            ),
        )

        borderColorPublisher?.subscribe(
            Subscriber(
                eventQueueWrapper {
                    border = MatteBorder(1, 1, 1, 1, it)
                    headerPanel.background = it
                    notesLabel.foreground = it
                    additionalHeaderPanels.values.forEach { label -> label.border = ADDITIONAL_HEADER_BORDER(it) }
                    additionalHeaderLabels.values.forEach { label -> label.foreground = it }
                },
            ),
        )

        val headerTextColor = headerTextColorPublisher
            ?: borderColorPublisher?.map { ColorUtils.foregroundToContrast(it) }
        headerTextColor
            ?.subscribe(
                Subscriber(
                    eventQueueWrapper {
                        headerLabel.foreground = it
                        additionalHeaderPanels.values.forEach { label -> label.background = it }
                    },
                ),
            )

        headerLabelsPublisher?.subscribe(
            Subscriber(
                eventQueueWrapper { labels ->
                    HeaderLabelLocation.values().forEach { align ->
                        val text = labels[align]
                        additionalHeaderPanels[align]!!.isVisible = (text != null)
                        additionalHeaderLabels[align]!!.text = text ?: ""
                        headerPanel.invalidate()
                        headerPanel.revalidate()
                        repaint()
                    }
                },
            ),
        )
    }

    protected fun addCenter(panel: JComponent) {
        add(panel, BorderLayout.CENTER)
    }

    private inner class HeaderLayout : LayoutManager {
        override fun addLayoutComponent(name: String, comp: Component) {
        }

        override fun removeLayoutComponent(comp: Component) {
        }

        override fun preferredLayoutSize(parent: Container): Dimension {
            val elements = sequenceOf(additionalHeaderPanels.values.asSequence(), sequenceOf(headerLabel))
                .flatten().filter { it.isVisible }.toList()
            val width = elements.sumOf { it.preferredSize.width }
            val height = elements.maxOf { it.preferredSize.height }
            return Dimension(width, height)
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            val elements = sequenceOf(additionalHeaderPanels.values.asSequence(), sequenceOf(headerLabel))
                .flatten().filter { it.isVisible }.toList()
            val width = elements.sumOf { it.minimumSize.width }
            val height = elements.maxOf { it.minimumSize.height }
            return Dimension(width, height)
        }

        override fun layoutContainer(parent: Container) {
            val mainWidth = headerLabel.preferredSize.width
            val rightWidth = additionalHeaderPanels[HeaderLabelLocation.RIGHT]!!.let { if (it.isVisible) it.preferredSize.width else 0 }
            val leftWidth = additionalHeaderPanels[HeaderLabelLocation.LEFT]!!.let { if (it.isVisible) it.preferredSize.width else 0 }

            val ratio = (parent.width.toDouble() / (mainWidth + leftWidth + rightWidth).coerceAtLeast(1)).coerceAtMost(1.0)

            val leftWidthFinal = (leftWidth * ratio).roundToInt()
            val rightWidthFinal = (rightWidth * ratio).roundToInt()
            val mainWidthFinal = when (alignment) {
                Alignment.CENTER -> when {
                    mainWidth + 2 * max(leftWidth, rightWidth) < parent.width -> parent.width - 2 * (max(leftWidth, rightWidth) * ratio).roundToInt()
                    else -> (mainWidth * ratio).roundToInt()
                }
                else -> parent.width - (leftWidthFinal + rightWidthFinal)
            }
            val mainLeftFinal = when (alignment) {
                Alignment.CENTER -> when {
                    mainWidth + 2 * max(leftWidth, rightWidth) < parent.width -> (max(leftWidth, rightWidth) * ratio).roundToInt()
                    else -> (max(leftWidth, parent.width - mainWidth - rightWidth) * ratio).roundToInt()
                }
                else -> leftWidthFinal
            }

            headerLabel.location = Point(mainLeftFinal, 0)
            headerLabel.size = Dimension(mainWidthFinal, parent.height)
            additionalHeaderPanels[HeaderLabelLocation.RIGHT]!!.also { panel ->
                panel.location = Point(parent.width - rightWidthFinal, 0)
                panel.size = Dimension(rightWidthFinal, parent.height)
            }
            additionalHeaderPanels[HeaderLabelLocation.LEFT]!!.also { panel ->
                panel.location = Point(0, 0)
                panel.size = Dimension(leftWidthFinal, parent.height)
            }
        }
    }

    val header: String? @TestOnly get() = if (headerPanel.isVisible) headerLabel.text.trim() else null

    val alignment: Alignment @TestOnly get() = Alignment.values().find { it.jlabelAlignment == headerLabel.horizontalAlignment }!!

    val notes: String? @TestOnly get() = if (notesLabel.isVisible) notesLabel.text.trim() else null

    val borderColor: Color @TestOnly get() = headerPanel.background

    val headerTextColor: Color @TestOnly get() = headerLabel.foreground
}
