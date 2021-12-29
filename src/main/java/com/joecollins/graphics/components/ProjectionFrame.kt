package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.graphics.utils.StandardFont
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.Image
import java.awt.RenderingHints
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class ProjectionFrame(
    headerBinding: Binding<String?>,
    imageBinding: Binding<Image?>,
    backColorBinding: Binding<Color>,
    borderColorBinding: Binding<Color>,
    footerTextBinding: Binding<String?>,
    imageAlignmentBinding: Binding<Alignment>? = null
) : GraphicsFrame(
    headerPublisher = headerBinding.toPublisher(),
    borderColorPublisher = borderColorBinding.toPublisher()
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

        imageBinding.bind { imagePanel.image = it }
        backColorBinding.bind { footerPanel.background = it }
        footerTextBinding.bind {
            footerLabel.text = it
            footerLabel.isVisible = it != null
        }
        (imageAlignmentBinding ?: Binding.fixedBinding(Alignment.BOTTOM)).bind { imagePanel.alignment = it }
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
