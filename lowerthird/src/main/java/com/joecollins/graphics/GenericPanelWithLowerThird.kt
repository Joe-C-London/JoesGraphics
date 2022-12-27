package com.joecollins.graphics

import com.joecollins.graphics.components.lowerthird.LowerThird
import com.joecollins.pubsub.asOneTimePublisher
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.util.concurrent.Flow
import javax.swing.JPanel

class GenericPanelWithLowerThird(
    private val panel: JPanel,
    lowerThird: LowerThird
) : JPanel(), AltTextProvider {

    init {
        background = Color.WHITE
        layout = BorderLayout()
        preferredSize = Dimension(1024, 512)
        add(panel, BorderLayout.CENTER)
        add(lowerThird, BorderLayout.SOUTH)
    }

    override val altText: Flow.Publisher<String?> =
        if (panel is AltTextProvider) {
            panel.altText
        } else {
            (null as String?).asOneTimePublisher()
        }
}
