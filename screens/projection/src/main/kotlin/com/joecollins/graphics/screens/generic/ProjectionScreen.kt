package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.components.ProjectionFrame
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import java.awt.Color
import java.awt.GridLayout
import java.awt.Image
import java.util.concurrent.Flow
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class ProjectionScreen private constructor(
    text: Flow.Publisher<out String?>,
    color: Flow.Publisher<out Color>,
    image: Flow.Publisher<out Image?>,
    imageAlignment: Flow.Publisher<out ProjectionFrame.Alignment>,
    header: String,
    override val altText: Flow.Publisher<out String?>,
) : JPanel(),
    AltTextProvider {

    companion object {
        fun createScreen(
            text: Flow.Publisher<out String?>,
            color: Flow.Publisher<out Color>,
            image: Flow.Publisher<out Image?>,
            imageAlignment: Flow.Publisher<out ProjectionFrame.Alignment> = ProjectionFrame.Alignment.BOTTOM.asOneTimePublisher(),
            header: String = "PROJECTION",
            altText: Flow.Publisher<out String?> = text.map { t -> t?.let { "$header: $it" } ?: header },
        ): ProjectionScreen = ProjectionScreen(text, color, image, imageAlignment, header, altText)
    }

    init {
        layout = GridLayout(1, 1)
        background = Color.WHITE
        border = EmptyBorder(5, 5, 5, 5)
        val frame = ProjectionFrame(
            headerPublisher = header.asOneTimePublisher(),
            borderColorPublisher = color,
            imagePublisher = image,
            backColorPublisher = color,
            footerTextPublisher = text,
            imageAlignmentPublisher = imageAlignment,
        )
        add(frame)
    }
}
