package com.joecollins.graphics.components

import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import java.util.concurrent.Flow
import javax.swing.JLabel

class DescriptionFrame(header: Flow.Publisher<out String?>, text: Flow.Publisher<out String?>) : GraphicsFrame(header) {
    init {
        val centerLabel = JLabel()
        centerLabel.font = StandardFont.readNormalFont(20)
        centerLabel.horizontalAlignment = JLabel.CENTER
        centerLabel.verticalAlignment = JLabel.TOP
        addCenter(centerLabel)
        text.subscribe(
            Subscriber(
                eventQueueWrapper { desc: String? ->
                    centerLabel.text = "<html><center>${desc?.replace("\n", "<br/>")}</center></html>"
                    centerLabel.isVisible = desc != null
                },
            ),
        )
    }
}
