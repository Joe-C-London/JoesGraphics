package com.joecollins.graphics

import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.lowerthird.LowerThird
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

open class GenericPanel(
    panel: JPanel,
    label: Flow.Publisher<out String?>?,
    lowerThird: LowerThird?
) : JPanel() {

    protected val label: JLabel = FontSizeAdjustingLabel()

    constructor(panel: JPanel, label: String) : this(panel, label.asOneTimePublisher(), null)
    constructor(panel: JPanel, label: Flow.Publisher<out String?>) : this(panel, label, null)
    constructor(panel: JPanel, lowerThird: LowerThird?) : this(panel, null as Flow.Publisher<String>?, lowerThird)
    constructor(panel: JPanel, label: String, lowerThird: LowerThird?) : this(panel, label.asOneTimePublisher(), lowerThird)
    constructor(panel: () -> JPanel, label: Flow.Publisher<out String?>) : this(panel(), label)
    constructor(panel: () -> JPanel, lowerThird: () -> LowerThird) : this(panel(), lowerThird())
    constructor(panel: () -> JPanel, label: Flow.Publisher<out String?>, lowerThird: () -> LowerThird) : this(panel(), label, lowerThird())

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
            val ret = JPanel()
            ret.background = Color.WHITE
            ret.layout = GridLayout(1, 1, 0, 0)
            ret.border = EmptyBorder(5, 5, 5, 5)
            ret.add(panel)
            return ret
        }
    }
}
