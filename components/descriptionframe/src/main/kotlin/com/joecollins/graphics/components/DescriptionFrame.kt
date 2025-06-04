package com.joecollins.graphics.components

import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import java.awt.Color
import java.util.concurrent.Flow
import javax.swing.JLabel

class DescriptionFrame(header: Flow.Publisher<out String?>, text: Flow.Publisher<out String?>) : GraphicsFrame(header) {
    init {
        val centerLabel = JLabel()
        val font = StandardFont.readNormalFont(15)
        centerLabel.font = font
        centerLabel.foreground = Color.BLACK
        centerLabel.horizontalAlignment = JLabel.CENTER
        centerLabel.verticalAlignment = JLabel.TOP
        addCenter(centerLabel)
        text.subscribe(
            Subscriber(
                eventQueueWrapper { desc: String? ->
                    centerLabel.text = "<html><body style='font: ${font.size}px \"${font.name}\", ${font.family};'><center>${desc?.replace("\n", "<br/>")}</center></body></html>"
                    centerLabel.isVisible = desc != null
                },
            ),
        )
    }
}
