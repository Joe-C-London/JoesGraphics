package com.joecollins.graphics.components

import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder

@Suppress("LeakingThis")
open class GraphicsFrame(
    headerPublisher: Flow.Publisher<out String?>,
    notesPublisher: Flow.Publisher<out String?>? = null,
    borderColorPublisher: Flow.Publisher<out Color>? = null,
    headerAlignmentPublisher: Flow.Publisher<out Alignment>? = null,
    headerTextColorPublisher: Flow.Publisher<out Color>? = null
) : JPanel() {

    enum class Alignment(val jlabelAlignment: Int) {
        LEFT(JLabel.LEFT),
        CENTER(JLabel.CENTER),
        RIGHT(JLabel.RIGHT)
    }

    private val headerFont = StandardFont.readNormalFont(24)
    private val headerPanel = JPanel()
    private val headerLabel = FontSizeAdjustingLabel()
    private val notesLabel = FontSizeAdjustingLabel()

    init {
        layout = BorderLayout()
        background = Color.WHITE
        border = MatteBorder(1, 1, 1, 1, Color.BLACK)
        maximumSize = Dimension(Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())
        preferredSize = Dimension(1024, 1024)
        minimumSize = Dimension(1, 1)

        headerPanel.layout = GridLayout(1, 0)
        headerPanel.background = Color.BLACK
        add(headerPanel, BorderLayout.NORTH)

        headerLabel.foreground = Color.WHITE
        headerLabel.horizontalAlignment = JLabel.CENTER
        headerLabel.font = headerFont
        headerLabel.border = EmptyBorder(3, 0, -3, 0)
        headerPanel.add(headerLabel)

        notesLabel.foreground = Color.BLACK
        notesLabel.horizontalAlignment = JLabel.RIGHT
        notesLabel.font = StandardFont.readNormalFont(12)
        notesLabel.border = EmptyBorder(2, 0, -2, 0)
        add(notesLabel, BorderLayout.SOUTH)

        val onHeaderUpdate: (String?) -> Unit = {
            headerPanel.isVisible = (it != null)
            headerLabel.text = it ?: ""
        }
        headerPublisher.subscribe(Subscriber(eventQueueWrapper(onHeaderUpdate)))

        val onAlignmentUpdate: (Alignment) -> Unit = {
            headerLabel.horizontalAlignment = it.jlabelAlignment
        }
        if (headerAlignmentPublisher != null)
            headerAlignmentPublisher.subscribe(Subscriber(eventQueueWrapper(onAlignmentUpdate)))
        else
            onAlignmentUpdate(Alignment.CENTER)

        val onNotesUpdate: (String?) -> Unit = {
            notesLabel.isVisible = (it != null)
            notesLabel.text = (it ?: "") + " "
        }
        if (notesPublisher != null)
            notesPublisher.subscribe(Subscriber(eventQueueWrapper(onNotesUpdate)))
        else
            onNotesUpdate(null)

        val onBorderColorUpdate: (Color) -> Unit = {
            border = MatteBorder(1, 1, 1, 1, it)
            headerPanel.background = it
            notesLabel.foreground = it
        }
        if (borderColorPublisher != null)
            borderColorPublisher.subscribe(Subscriber(eventQueueWrapper(onBorderColorUpdate)))
        else
            onBorderColorUpdate(Color.BLACK)

        val onForegroundColorUpdate: (Color) -> Unit = {
            headerLabel.foreground = it
        }
        if (headerTextColorPublisher != null)
            headerTextColorPublisher.subscribe(Subscriber(eventQueueWrapper(onForegroundColorUpdate)))
        else if (borderColorPublisher != null)
            borderColorPublisher.subscribe(Subscriber(eventQueueWrapper { onForegroundColorUpdate(ColorUtils.foregroundToContrast(it)) }))
        else
            onForegroundColorUpdate(Color.WHITE)
    }

    internal val header: String? get() = if (headerPanel.isVisible) headerLabel.text.trim() else null

    protected val alignment: Alignment get() = Alignment.values().find { it.jlabelAlignment == headerLabel.horizontalAlignment }!!

    internal val notes: String? get() = if (notesLabel.isVisible) notesLabel.text.trim() else null

    internal val borderColor: Color get() = headerPanel.background

    internal val headerTextColor: Color get() = headerLabel.foreground
}
