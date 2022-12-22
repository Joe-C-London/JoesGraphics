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
        ret.border = EmptyBorder(5, 5, 5, 5)
        ret.add(this)
        return ret
    }
}