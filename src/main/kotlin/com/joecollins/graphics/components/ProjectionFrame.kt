package com.joecollins.graphics.components

import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.Image
import java.awt.RenderingHints
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class ProjectionFrame(
    headerPublisher: Flow.Publisher<out String?>,
    imagePublisher: Flow.Publisher<out Image?>,
    backColorPublisher: Flow.Publisher<out Color>,
    borderColorPublisher: Flow.Publisher<out Color>,
    footerTextPublisher: Flow.Publisher<out String?>,
    imageAlignmentPublisher: Flow.Publisher<out Alignment>? = null
) : GraphicsFrame(
    headerPublisher = headerPublisher,
    borderColorPublisher = borderColorPublisher
) {

    enum class Alignment { BOTTOM, MIDDLE }

    private val imagePanel: ImagePanel = ImagePanel()
    private val footerPanel = JPanel()
    private val footerLabel = FontSizeAdjustingLabel()

    init {
        val centre = JPanel()
        add(centre, BorderLayout.CENTER)

        centre.layout = BorderLayout()
        centre.add(imagePanel, BorderLayout.CENTER)
        centre.add(footerPanel, BorderLayout.SOUTH)

        footerPanel.layout = GridLayout(1, 1)
        footerPanel.add(footerLabel)

        footerLabel.font = StandardFont.readBoldFont(72)
        footerLabel.foreground = Color.WHITE
        footerLabel.horizontalAlignment = JLabel.CENTER
        footerLabel.border = EmptyBorder(15, 0, -15, 0)

        imagePublisher.subscribe(Subscriber(eventQueueWrapper { imagePanel.image = it }))
        backColorPublisher.subscribe(Subscriber(eventQueueWrapper { footerPanel.background = it }))
        footerTextPublisher.subscribe(
            Subscriber(
                eventQueueWrapper {
                    footerLabel.text = it
                    footerLabel.isVisible = it != null
                }
            )
        )
        if (imageAlignmentPublisher != null)
            imageAlignmentPublisher.subscribe(Subscriber(eventQueueWrapper { imagePanel.alignment = it }))
        else
            imagePanel.alignment = Alignment.BOTTOM
    }

    internal fun getImage(): Image? {
        return imagePanel.image
    }

    internal fun getBackColor(): Color {
        return footerPanel.background
    }

    internal fun getFooterText(): String? {
        return if (footerLabel.isVisible) footerLabel.text else null
    }

    internal fun getImageAlignment(): Alignment {
        return imagePanel.alignment
    }

    private inner class ImagePanel : JPanel() {
        private var _image: Image? = null
        private var _alignment: Alignment = Alignment.BOTTOM

        var image: Image?
            get() { return _image }
            set(value) {
                _image = value
                repaint()
            }

        var alignment: Alignment
            get() { return _alignment }
            set(value) {
                _alignment = value
                repaint()
            }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val img = image ?: return
            val xRatio = 1.0 * width / img.getWidth(null)
            val yRatio = 1.0 * height / img.getHeight(null)
            val ratio = 1.0.coerceAtMost(xRatio.coerceAtMost(yRatio))
            val newWidth = (ratio * img.getWidth(null)).toInt()
            val newHeight = (ratio * img.getHeight(null)).toInt()
            (g as Graphics2D)
                .setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            val scaledImage = img.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT)
            g.drawImage(
                scaledImage,
                (width - newWidth) / 2,
                (height - newHeight) / if (alignment == Alignment.BOTTOM) 1 else 2,
                null
            )
        }

        init {
            background = Color.WHITE
        }
    }
}
