package com.joecollins.graphics

import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.lowerthird.LowerThird
import com.joecollins.graphics.utils.PanelUtils.pad
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.merge
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

open class GenericPanel(
    panel: JPanel,
    label: Flow.Publisher<out String?>?,
    lowerThird: LowerThird?,
    override val altText: Flow.Publisher<String?> = null.asOneTimePublisher()
) : JPanel(), AltTextProvider {

    protected val label: JLabel = FontSizeAdjustingLabel()

    constructor(panel: JPanel, label: String, altText: Flow.Publisher<String?> = createAltText(panel, label.asOneTimePublisher())) : this(panel, label.asOneTimePublisher(), null, altText)
    constructor(panel: JPanel, label: Flow.Publisher<out String?>, altText: Flow.Publisher<String?> = createAltText(panel, label)) : this(panel, label, null, altText)
    constructor(panel: JPanel, lowerThird: LowerThird?, altText: Flow.Publisher<String?> = createAltText(panel, null.asOneTimePublisher())) : this(panel, null as Flow.Publisher<String>?, lowerThird, altText)
    constructor(panel: JPanel, label: String, lowerThird: LowerThird?, altText: Flow.Publisher<String?> = createAltText(panel, label.asOneTimePublisher())) : this(panel, label.asOneTimePublisher(), lowerThird, altText)

    init {
        if (label != null) {
            label.subscribe(
                Subscriber(
                    eventQueueWrapper {
                        this.label.text = it ?: ""
                        this.label.isVisible = it != null
                    }
                )
            )
            this.label.horizontalAlignment = JLabel.CENTER
            this.label.border = EmptyBorder(5, 0, -5, 0)
            this.label.font = readBoldFont(32)
        }
        background = Color.WHITE
        layout = BorderLayout()
        preferredSize = Dimension(1024, 512)
        if (label != null) {
            add(this.label, BorderLayout.NORTH)
        }
        if (lowerThird != null) {
            add(lowerThird, BorderLayout.SOUTH)
        }
        add(panel, BorderLayout.CENTER)
    }

    companion object {
        fun pad(panel: JPanel): JPanel {
            return panel.pad()
        }

        private fun createAltText(panel: JPanel, label: Flow.Publisher<out String?>): Flow.Publisher<String?> {
            return if (panel is AltTextProvider) {
                panel.altText.merge(label) { alt, lab -> if (alt == null) null else ((lab?.let { "$it\n" } ?: "") + alt) }
            } else {
                null.asOneTimePublisher()
            }
        }
    }
}
