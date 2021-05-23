package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.graphics.utils.StandardFont
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder

@Suppress("LeakingThis")
open class GraphicsFrame(
    headerBinding: Binding<String?>,
    notesBinding: Binding<String?>? = null,
    borderColorBinding: Binding<Color>? = null,
    headerAlignmentBinding: Binding<Alignment>? = null
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

        headerBinding.bind {
            headerPanel.isVisible = (it != null)
            headerLabel.text = it ?: ""
        }
        (headerAlignmentBinding ?: Binding.fixedBinding(Alignment.CENTER)).bind { headerLabel.horizontalAlignment = it.jlabelAlignment }
        (notesBinding ?: Binding.fixedBinding(null)).bind {
            notesLabel.isVisible = (it != null)
            notesLabel.text = (it ?: "") + " "
        }
        (borderColorBinding ?: Binding.fixedBinding(Color.BLACK)).bind {
            border = MatteBorder(1, 1, 1, 1, it)
            headerPanel.background = it
            notesLabel.foreground = it
        }
    }

    internal val header: String? get() = if (headerPanel.isVisible) headerLabel.text.trim() else null

    protected val alignment: Alignment get() = Alignment.values().find { it.jlabelAlignment == headerLabel.horizontalAlignment }!!

    internal val notes: String? get() = if (notesLabel.isVisible) notesLabel.text.trim() else null

    internal val borderColor: Color get() = headerPanel.background
}
