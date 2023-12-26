package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.TimePublisher
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.net.URL
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.Flow
import javax.imageio.ImageIO
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.max

@Suppress("LeakingThis")
open class LowerThird internal constructor(
    private val leftImagePublisher: Flow.Publisher<(Graphics2D) -> Dimension>,
    private val placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
    private val clock: Clock,
    private val showTimeZone: Boolean = false,
) : JPanel() {

    constructor(
        leftImagePublisher: Flow.Publisher<(Graphics2D) -> Dimension>,
        placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
        showTimeZone: Boolean = false,
    ) : this(leftImagePublisher, placePublisher, Clock.systemDefaultZone(), showTimeZone)

    private val leftPanel: ImagePanel = ImagePanel()
    private val rightPanel = PlaceAndTimePanel()

    private val now = TimePublisher.forClock(clock)

    protected fun addHeadlinePanel(panel: JPanel) {
        add(panel, BorderLayout.CENTER)
    }

    internal val leftImage: (Graphics2D) -> Dimension
        get() = leftPanel.leftImage

    internal val place: String
        get() = rightPanel.place

    internal val time: String
        get() = rightPanel.time

    private inner class ImagePanel : JPanel() {
        var leftImage: (Graphics2D) -> Dimension = { Dimension(1, 1) }

        fun setImage(image: (Graphics2D) -> Dimension) {
            leftImage = image
            this.preferredSize = image(DEFAULT_IMAGE.graphics as Graphics2D)
            this@LowerThird.revalidate()
            repaint()
        }

        override fun paintComponent(g: Graphics) {
            (g as Graphics2D)
                .setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            leftImage(g)
        }

        init {
            setImage { Dimension(200, 50) }
        }
    }

    private inner class PlaceAndTimePanel : JPanel() {
        private val formatter = DateTimeFormatter.ofPattern("HH:mm")
        private val tzFormatter = DateTimeFormatter.ofPattern("zzz", Locale.ENGLISH)

        private val placeLabel: JLabel = FontSizeAdjustingLabel("UTC")
        private val timeLabel: JLabel
        private val timezoneLabel: JLabel

        var place: String
            get() {
                return placeLabel.text
            }
            set(place) {
                placeLabel.text = place
                repaint()
            }

        var currentTime: Instant = Instant.now()
            set(value) {
                field = value
                updateTime()
            }

        var timezone: ZoneId = ZoneOffset.UTC
            set(value) {
                field = value
                updateTime()
            }

        val time: String
            get() {
                return timeLabel.text
            }

        init {
            placeLabel.font = StandardFont.readBoldFont(12)
            placeLabel.horizontalAlignment = JLabel.CENTER
            placeLabel.verticalAlignment = JLabel.CENTER
            placeLabel.foreground = Color.BLACK
            placeLabel.border = EmptyBorder(10, 0, 0, 0)

            timeLabel = JLabel(formatter.format(java.time.ZonedDateTime.now(timezone)))
            timeLabel.font = StandardFont.readBoldFont(24)
            timeLabel.horizontalAlignment = JLabel.CENTER
            timeLabel.verticalAlignment = JLabel.CENTER
            timeLabel.foreground = Color.BLACK
            timeLabel.border = EmptyBorder(if (showTimeZone) 5 else 0, 0, if (showTimeZone) -5 else 0, 0)

            timezoneLabel = JLabel("UTC")
            timezoneLabel.font = StandardFont.readNormalFont(10)
            timezoneLabel.horizontalAlignment = JLabel.CENTER
            timezoneLabel.verticalAlignment = JLabel.CENTER
            timezoneLabel.foreground = Color.BLACK
            timezoneLabel.border = EmptyBorder(5, 0, 1, 0)
        }

        fun updateTime() {
            try {
                val now = currentTime.atZone(timezone)
                val newTime = formatter.format(now)
                val newTz = tzFormatter.format(now)
                if (newTime != timeLabel.text) {
                    timeLabel.text = newTime
                    EventQueue.invokeLater { timeLabel.repaint() }
                }
                if (newTz != timezoneLabel.text) {
                    timezoneLabel.text = newTz
                    EventQueue.invokeLater { timezoneLabel.repaint() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        init {
            preferredSize = Dimension(100, 50)
            background = Color.YELLOW
            layout = GridBagLayout()
            add(
                placeLabel,
                object : GridBagConstraints() {
                    init {
                        fill = BOTH
                        gridx = 0
                        gridy = 0
                        gridwidth = 1
                        gridheight = 2
                        weightx = 1.0
                        weighty = 1.0
                    }
                },
            )
            add(
                timeLabel,
                object : GridBagConstraints() {
                    init {
                        fill = BOTH
                        gridx = 0
                        gridy = 2
                        gridwidth = 1
                        gridheight = 3
                        weightx = 1.0
                        weighty = 1.0
                    }
                },
            )
            if (showTimeZone) {
                add(
                    timezoneLabel,
                    object : GridBagConstraints() {
                        init {
                            fill = BOTH
                            gridx = 0
                            gridy = 5
                            gridwidth = 1
                            gridheight = 1
                            weightx = 1.0
                            weighty = 1.0
                        }
                    },
                )
            }
        }
    }

    companion object {
        private val DEFAULT_IMAGE = BufferedImage(200, 50, BufferedImage.TYPE_4BYTE_ABGR)
        fun createImage(url: URL): (Graphics2D) -> Dimension {
            val image = ImageIO.read(url)
            return { g ->
                g.drawImage(image, 0, 0, null)
                Dimension(image.width, image.height)
            }
        }

        fun createImage(text: String, foreground: Color, background: Color): (Graphics2D) -> Dimension {
            val font = StandardFont.readBoldFont(24)
            return { g ->
                g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                )

                val bounds = g.getFontMetrics(font).getStringBounds(text, g)
                val width = max(200, bounds.width.toInt() + 10)

                g.color = background
                g.fillRect(0, 0, width, 50)
                g.color = foreground
                g.font = font
                g.drawString(text, (width - bounds.width).toInt() / 2, 35)
                Dimension(width, 50)
            }
        }

        fun createImage(paint: (Graphics2D, Dimension) -> Unit) = createImage(200, paint)

        fun createImage(width: Int, paint: (Graphics2D, Dimension) -> Unit): (Graphics2D) -> Dimension {
            val height = 50
            return { g ->
                g.apply {
                    setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON,
                    )
                    setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                    )
                    setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY,
                    )
                }
                paint(g, Dimension(width, height))
                Dimension(width, height)
            }
        }
    }

    init {
        layout = BorderLayout()
        add(leftPanel, BorderLayout.WEST)
        add(rightPanel, BorderLayout.EAST)
        preferredSize = Dimension(1024, 50)
        this.leftImagePublisher.subscribe(Subscriber(eventQueueWrapper { leftPanel.setImage(it) }))
        this.placePublisher.subscribe(
            Subscriber(
                eventQueueWrapper {
                    rightPanel.place = it.first
                    rightPanel.timezone = it.second
                },
            ),
        )
        this.now.subscribe(Subscriber(eventQueueWrapper { rightPanel.currentTime = it }))
    }
}
