package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Image
import java.awt.RenderingHints
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.net.URL
import java.time.Clock
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.Flow
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.max

@Suppress("LeakingThis")
open class LowerThird internal constructor(
    private val leftImagePublisher: Flow.Publisher<out Image>,
    private val placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
    private val clock: Clock,
    private val showTimeZone: Boolean = false
) : JPanel() {

    constructor(
        leftImagePublisher: Flow.Publisher<out Image>,
        placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
        showTimeZone: Boolean = false
    ) : this(leftImagePublisher, placePublisher, Clock.systemDefaultZone(), showTimeZone)

    private val leftPanel: ImagePanel = ImagePanel()
    private val rightPanel = PlaceAndTimePanel()

    protected fun addHeadlinePanel(panel: JPanel) {
        add(panel, BorderLayout.CENTER)
    }

    internal val leftImage: Image
        get() { return leftPanel.leftImage }

    internal val place: String
        get() = rightPanel.place

    internal val time: String
        get() = rightPanel.time

    private inner class ImagePanel : JPanel() {
        var leftImage: Image = BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR)

        fun setImage(image: Image) {
            leftImage = image
            this.preferredSize = Dimension(50 * image.getWidth(null) / image.getHeight(null), 50)
            this@LowerThird.revalidate()
            repaint()
        }

        override fun paintComponent(g: Graphics) {
            (g as Graphics2D)
                .setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g.drawImage(leftImage, 0, 0, null)
        }

        init {
            setImage(DEFAULT_IMAGE)
        }
    }

    private inner class PlaceAndTimePanel : JPanel() {
        private val formatter = DateTimeFormatter.ofPattern("HH:mm")
        private val tzFormatter = DateTimeFormatter.ofPattern("zzz", Locale.ENGLISH)
        private val executor = Executors.newSingleThreadScheduledExecutor { r: Runnable ->
            val t = Thread(r)
            t.name = "LowerThird-Timer-" + this.hashCode()
            t.isDaemon = true
            t
        }

        private val placeLabel: JLabel = FontSizeAdjustingLabel("UTC")
        private val timeLabel: JLabel
        private val timezoneLabel: JLabel

        var place: String
            get() { return placeLabel.text }
            set(place) {
                placeLabel.text = place
                repaint()
            }

        var timezone: ZoneId = ZoneOffset.UTC
            set(value) {
                field = value
                updateTime()
            }

        val time: String
            get() { return timeLabel.text }

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
                val now = clock.instant().atZone(timezone)
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
                }
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
                }
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
                    }
                )
            }
            executor.scheduleAtFixedRate({ updateTime() }, 0, 100, TimeUnit.MILLISECONDS)
        }
    }

    companion object {
        private val DEFAULT_IMAGE = BufferedImage(200, 50, BufferedImage.TYPE_4BYTE_ABGR)
        fun createImage(url: URL): Image {
            return ImageIO.read(url)
        }

        fun createImage(text: String, foreground: Color, background: Color): Image {
            val font = StandardFont.readBoldFont(24)
            var bounds: Rectangle2D
            run {
                val g = BufferedImage(200, 50, BufferedImage.TYPE_4BYTE_ABGR).graphics
                bounds = g.getFontMetrics(font).getStringBounds(text, g)
                g.dispose()
            }
            val img = BufferedImage(
                max(200, bounds.width.toInt() + 10),
                50,
                BufferedImage.TYPE_4BYTE_ABGR
            )
            val g = img.graphics
            (g as Graphics2D)
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
            g.setColor(background)
            g.fillRect(0, 0, img.width, 50)
            g.setColor(foreground)
            g.setFont(font)
            g.drawString(text, (img.width - bounds.width).toInt() / 2, 35)
            return img
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
                }
            )
        )
    }
}
