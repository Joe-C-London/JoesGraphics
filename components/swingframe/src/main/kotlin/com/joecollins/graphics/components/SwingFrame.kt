package com.joecollins.graphics.components

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.map
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.min

class SwingFrame(
    headerPublisher: Flow.Publisher<out String?>,
    valuePublisher: Flow.Publisher<out Number>,
    rangePublisher: Flow.Publisher<out Number>,
    leftColorPublisher: Flow.Publisher<out Color>,
    rightColorPublisher: Flow.Publisher<out Color>,
    bottomTextPublisher: Flow.Publisher<out String?>,
    bottomColorPublisher: Flow.Publisher<out Color>,
    progressPublisher: Flow.Publisher<out String?>? = null,
) : GraphicsFrame(
    headerPublisher = headerPublisher,
    headerLabelsPublisher = progressPublisher?.map { p -> mapOf(HeaderLabelLocation.RIGHT to p) },
) {
    private val swingPanel = SwingPanel()
    private val bottomLabel: JLabel = FontSizeAdjustingLabel()

    val altText: Flow.Publisher<out String?> = bottomTextPublisher

    init {
        bottomLabel.horizontalAlignment = JLabel.CENTER
        bottomLabel.font = StandardFont.readBoldFont(15)
        bottomLabel.border = EmptyBorder(2, 0, -2, 0)
        val centerPanel = JPanel()
        centerPanel.background = Color.WHITE
        centerPanel.layout = BorderLayout()
        centerPanel.add(swingPanel, BorderLayout.CENTER)
        centerPanel.add(bottomLabel, BorderLayout.SOUTH)
        addCenter(centerPanel)

        valuePublisher.subscribe(Subscriber(eventQueueWrapper { swingPanel.value = it }))
        rangePublisher.subscribe(Subscriber(eventQueueWrapper { swingPanel.range = it }))
        leftColorPublisher.subscribe(Subscriber(eventQueueWrapper { swingPanel.leftColor = it }))
        rightColorPublisher.subscribe(Subscriber(eventQueueWrapper { swingPanel.rightColor = it }))
        bottomTextPublisher.subscribe(
            Subscriber(
                eventQueueWrapper {
                    bottomLabel.isVisible = it != null
                    bottomLabel.text = it ?: ""
                },
            ),
        )
        bottomColorPublisher.subscribe(Subscriber(eventQueueWrapper { bottomLabel.foreground = ColorUtils.contrastForBackground(it) }))
    }

    internal fun getRange(): Number = swingPanel.range

    internal fun getValue(): Number = swingPanel.value

    internal fun getLeftColor(): Color = swingPanel.leftColor

    internal fun getRightColor(): Color = swingPanel.rightColor

    internal fun getBottomText(): String? = if (bottomLabel.isVisible) bottomLabel.text else null

    internal fun getBottomColor(): Color = bottomLabel.foreground

    private inner class SwingPanel : JPanel() {
        var range: Number = 1
            set(value) {
                field = value
                repaint()
            }

        var value: Number = 0
            set(value) {
                field = value
                repaint()
            }

        var leftColor: Color = Color.BLACK
            set(value) {
                field = value
                repaint()
            }

        var rightColor: Color = Color.BLACK
            set(value) {
                field = value
                repaint()
            }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            val margin = 2
            val arcWidth = width / 2 - 2 * margin
            val arcHeight = height - 2 * margin
            val arcSize = arcWidth.coerceAtMost(arcHeight)
            val arcX = width / 2 - arcSize
            val arcY = (height - arcSize) / 2
            var arcAngle = (90 * value.toDouble() / range.toDouble()).toInt()
            val maxAngle = 85
            arcAngle = (-maxAngle).coerceAtLeast(min(arcAngle, maxAngle))
            g.setColor(leftColor)
            g.fillArc(arcX, arcY - arcSize, arcSize * 2, arcSize * 2, 180, arcAngle + 90)
            g.setColor(rightColor)
            g.fillArc(arcX, arcY - arcSize, arcSize * 2, arcSize * 2, 0, arcAngle - 90)
            g.setColor(background)
            g.drawLine(width / 2, 0, width / 2, height)
        }

        init {
            background = Color.WHITE
        }
    }
}
