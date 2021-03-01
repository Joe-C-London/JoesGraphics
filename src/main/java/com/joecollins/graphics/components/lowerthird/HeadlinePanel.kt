package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.utils.StandardFont
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class HeadlinePanel : JPanel() {
    private val headlineLabel: JLabel
    private val subheadLabel: JLabel

    var headline: String?
        get() = if (headlineLabel.isVisible) headlineLabel.text else null
        set(headline) {
            headlineLabel.isVisible = headline != null
            headlineLabel.text = headline ?: ""
        }
    var subhead: String?
        get() = if (subheadLabel.isVisible) subheadLabel.text else null
        set(subhead) {
            subheadLabel.isVisible = subhead != null
            subheadLabel.text = subhead ?: ""
        }

    private inner class HeadlinePanelLayout : LayoutManager {
        override fun addLayoutComponent(name: String, comp: Component) {}
        override fun removeLayoutComponent(comp: Component) {}
        override fun preferredLayoutSize(parent: Container): Dimension {
            return Dimension(1024, 50)
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            return Dimension(50, 50)
        }

        override fun layoutContainer(parent: Container) {
            val width = parent.width
            val height = parent.height
            val headlineHeight = height * 11 / 20
            val subheadHeight = height * 9 / 20
            headlineLabel.setLocation(0, 0)
            headlineLabel.setSize(width, if (subheadLabel.isVisible) headlineHeight else height)
            subheadLabel.setLocation(0, headlineHeight)
            subheadLabel.setSize(width, subheadHeight)
        }
    }

    init {
        layout = HeadlinePanelLayout()

        headlineLabel = FontSizeAdjustingLabel()
        headlineLabel.text = ""
        headlineLabel.horizontalAlignment = JLabel.LEFT
        headlineLabel.verticalAlignment = JLabel.CENTER
        headlineLabel.font = StandardFont.readNormalFont(24)
        headlineLabel.foreground = Color.BLACK
        headlineLabel.border = EmptyBorder(6, 5, -6, 5)
        add(headlineLabel)

        subheadLabel = FontSizeAdjustingLabel()
        subheadLabel.text = ""
        subheadLabel.isVisible = false
        subheadLabel.horizontalAlignment = JLabel.LEFT
        subheadLabel.verticalAlignment = JLabel.CENTER
        subheadLabel.font = StandardFont.readNormalFont(16)
        subheadLabel.foreground = Color.BLACK
        subheadLabel.border = EmptyBorder(2, 5, -2, 5)
        add(subheadLabel)
    }
}
