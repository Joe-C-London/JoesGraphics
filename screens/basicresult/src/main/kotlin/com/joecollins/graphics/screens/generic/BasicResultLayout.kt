package com.joecollins.graphics.screens.generic

import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager

class BasicResultLayout : LayoutManager {
    companion object {
        val MAIN = "MAIN"
        val DIFF = "DIFF"
        val SWING = "SWING"
        val MAP = "MAP"
        val PREF = "PREF"
    }

    private val components = HashMap<String, Component>()

    override fun addLayoutComponent(name: String, comp: Component) {
        components[name] = comp
    }

    override fun removeLayoutComponent(comp: Component) {
        components.entries.firstOrNull { it.value == comp }?.let { components.remove(it.key) }
    }

    override fun preferredLayoutSize(parent: Container): Dimension {
        return Dimension(1024, 512)
    }

    override fun minimumLayoutSize(parent: Container): Dimension {
        return Dimension(0, 0)
    }

    override fun layoutContainer(parent: Container) {
        val width = parent.width
        val height = parent.height
        val mainFrame = components[MAIN]!!
        val changeFrame = components[DIFF]
        val swingFrame = components[SWING]
        val mapFrame = components[MAP]
        val preferenceFrame = components[PREF]
        mainFrame.setLocation(5, 5)
        val seatFrameIsAlone = changeFrame == null && swingFrame == null && mapFrame == null
        mainFrame.setSize(
            width * (if (seatFrameIsAlone) 5 else 3) / 5 - 10,
            height * (if (preferenceFrame == null) 3 else 2) / 3 - 10,
        )
        preferenceFrame?.setLocation(5, height * 2 / 3 + 5)
        preferenceFrame?.setSize(width * (if (seatFrameIsAlone) 5 else 3) / 5 - 10, height / 3 - 10)
        changeFrame?.setLocation(width * 3 / 5 + 5, 5)
        changeFrame?.setSize(width * 2 / 5 - 10, height * 2 / 3 - 10)
        swingFrame?.setLocation(width * 3 / 5 + 5, height * 2 / 3 + 5)
        swingFrame?.setSize(width * (if (mapFrame == null) 2 else 1) / 5 - 10, height / 3 - 10)
        mapFrame?.setLocation(width * (if (swingFrame == null) 3 else 4) / 5 + 5, height * 2 / 3 + 5)
        mapFrame?.setSize(width * (if (swingFrame == null) 2 else 1) / 5 - 10, height / 3 - 10)
    }
}
