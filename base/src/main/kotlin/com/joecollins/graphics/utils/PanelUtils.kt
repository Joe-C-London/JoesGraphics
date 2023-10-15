package com.joecollins.graphics.utils

import java.awt.Color
import java.awt.GridLayout
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

object PanelUtils {
    fun JPanel.pad(): JPanel {
        val ret = JPanel()
        ret.background = Color.WHITE
        ret.layout = GridLayout(1, 1, 0, 0)
        ret.addPaddingBorder()
        ret.add(this)
        return ret
    }

    fun JPanel.addPaddingBorder() {
        border = EmptyBorder(5, 5, 5, 5)
    }
}
