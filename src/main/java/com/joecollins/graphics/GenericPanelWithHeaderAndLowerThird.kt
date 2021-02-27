package com.joecollins.graphics

import com.joecollins.bindings.Binding
import com.joecollins.graphics.components.lowerthird.LowerThird
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class GenericPanelWithHeaderAndLowerThird<T : JPanel>(
    private val panel: T,
    private val label: Binding<String>?,
    private val lowerThird: LowerThird?
) : JPanel() {

    private val _label: JLabel = JLabel()

    constructor(panel: T, label: String) : this(panel, Binding.fixedBinding<String>(label), null) {}
    constructor(panel: T, lowerThird: LowerThird?) : this(panel, null as Binding<String>?, lowerThird) {}
    constructor(panel: T, label: String, lowerThird: LowerThird?) : this(panel, Binding.fixedBinding<String>(label), lowerThird) {}

    init {
        if (label != null) {
            label.bind { text: String? -> this._label.text = text ?: "" }
            this._label.horizontalAlignment = JLabel.CENTER
            this._label.border = EmptyBorder(5, 0, -5, 0)
            this._label.font = readBoldFont(32)
        }
        background = Color.WHITE
        layout = BorderLayout()
        preferredSize = Dimension(1024, 512)
        if (label != null) {
            add(this._label, BorderLayout.NORTH)
        }
        if (lowerThird != null) {
            add(lowerThird, BorderLayout.SOUTH)
        }
        add(panel, BorderLayout.CENTER)
    }
}
