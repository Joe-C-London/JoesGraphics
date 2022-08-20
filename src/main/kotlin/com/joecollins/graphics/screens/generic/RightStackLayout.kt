package com.joecollins.graphics.screens.generic

import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import java.awt.Point
import javax.swing.JPanel

class RightStackLayout : LayoutManager {
    companion object {
        val WEST = "WEST"
        val EAST = "EAST"
    }

    private var west = JPanel() as Component
    private val east = ArrayList<Component>()

    override fun addLayoutComponent(name: String, comp: Component) {
        if (name == WEST) west = comp
        if (name == EAST) east.add(comp)
    }

    override fun removeLayoutComponent(comp: Component) {
        if (west == comp) west = JPanel()
        else east.remove(comp)
    }

    override fun preferredLayoutSize(parent: Container?): Dimension {
        return Dimension(1024, 512)
    }

    override fun minimumLayoutSize(parent: Container?): Dimension {
        return Dimension(100, 50)
    }

    override fun layoutContainer(parent: Container) {
        val width = parent.width
        val height = parent.height
        val numPanels = east.size
        west.location = Point(5, 5)
        val rightColStart = if (numPanels == 0) width else (width - width / (numPanels + 1))
        west.size = Dimension(rightColStart - 10, height - 10)
        east.forEachIndexed { index, component ->
            if (index == east.size - 1)
                component.location = Point(rightColStart + 5, height - (height / numPanels) + 5)
            else
                component.location = Point(rightColStart + 5, (index * height / numPanels) + 5)
            component.size = Dimension(width / (numPanels + 1) - 10, (height / numPanels) - 10)
        }
    }
}
