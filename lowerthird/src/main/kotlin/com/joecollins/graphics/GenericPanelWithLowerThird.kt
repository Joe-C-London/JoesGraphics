package com.joecollins.graphics

import com.joecollins.graphics.components.lowerthird.LowerThird
import com.joecollins.pubsub.asOneTimePublisher
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.util.concurrent.Flow
import javax.swing.JPanel

open class GenericPanelWithLowerThird(
    panel: JPanel,
    lowerThird: LowerThird,
) : JPanel(), AltTextProvider {

    init {
        background = Color.WHITE
        layout = BorderLayout()
        preferredSize = GenericPanel.DEFAULT_SIZE
        add(panel, BorderLayout.CENTER)
        add(lowerThird, BorderLayout.SOUTH)
    }

    override val altText: Flow.Publisher<out String?> =
        if (panel is AltTextProvider) {
            panel.altText
        } else {
            (null as String?).asOneTimePublisher()
        }

    final override fun add(comp: Component, constraints: Any?) {
        super.add(comp, constraints)
    }
}
