package com.joecollins.graphics

import com.joecollins.graphics.components.lowerthird.LowerThird
import com.joecollins.pubsub.asOneTimePublisher
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Image
import java.util.concurrent.Flow
import javax.swing.JPanel

open class GenericPanelWithLowerThird(
    panel: JPanel,
    lowerThird: LowerThird,
) : JPanel(),
    AltTextProvider,
    TaskbarProvider {

    init {
        background = Color.WHITE
        layout = BorderLayout()
        preferredSize = GenericPanel.DEFAULT_SIZE
        add(panel, BorderLayout.CENTER)
        add(lowerThird, BorderLayout.SOUTH)
    }

    override val altText: Flow.Publisher<out (Int) -> String?> =
        if (panel is AltTextProvider) {
            panel.altText
        } else {
            { _: Int -> null as String? }.asOneTimePublisher()
        }

    override val taskbarIcon: Flow.Publisher<Image>? =
        if (panel is TaskbarProvider) {
            panel.taskbarIcon
        } else {
            null
        }

    override val progress: Flow.Publisher<Double>? =
        if (panel is TaskbarProvider) {
            panel.progress
        } else {
            null
        }

    final override fun add(comp: Component, constraints: Any?) {
        super.add(comp, constraints)
    }
}
