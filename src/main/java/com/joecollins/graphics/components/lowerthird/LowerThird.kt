package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding
import com.joecollins.graphics.utils.StandardFont
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
import java.io.IOException
import java.lang.Exception
import java.lang.Runnable
import java.net.URL
import java.time.Clock
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.Throws
import kotlin.math.max

open class LowerThird : JPanel() {
    private var clock = Clock.systemDefaultZone()
    private val leftPanel: ImagePanel = ImagePanel()
    private val rightPanel = PlaceAndTimePanel()
    private var leftImageBinding = Binding.fixedBinding<Image>(DEFAULT_IMAGE)
    private var placeBinding = Binding.fixedBinding("UTC")
    private var timezoneBinding = Binding.fixedBinding<ZoneId>(ZoneOffset.UTC)

    protected val leftImage: Image
    get() { return leftPanel.leftImage }

    fun setLeftImageBinding(leftImageBinding: Binding<Image>) {
        this.leftImageBinding.unbind()
        this.leftImageBinding = leftImageBinding
        this.leftImageBinding.bind { leftPanel.setImage(it) }
    }

    protected val place: String
        get() = rightPanel.place

    fun setPlaceBinding(placeBinding: Binding<String>) {
        this.placeBinding.unbind()
        this.placeBinding = placeBinding
        this.placeBinding.bind { rightPanel.place = it }
    }

    protected val time: String
        get() = rightPanel.time

    fun setTimeZoneBinding(timezoneBinding: Binding<ZoneId>) {
        this.timezoneBinding.unbind()
        this.timezoneBinding = timezoneBinding
        this.timezoneBinding.bind { rightPanel.timezone = it }
    }

    fun setClock(clock: Clock) {
        this.clock = clock
        rightPanel.updateTime()
        Thread.sleep(100)
    }

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
        private val executor = Executors.newSingleThreadScheduledExecutor { r: Runnable? ->
            val t = Thread(r)
            t.name = "LowerThird-Timer-" + this.hashCode()
            t.isDaemon = true
            t
        }

        private var _timezone: ZoneId = ZoneOffset.UTC
        private val _placeLabel: JLabel = JLabel("UTC")
        private val _timeLabel: JLabel

        init {
            _placeLabel.font = StandardFont.readBoldFont(12)
            _placeLabel.horizontalAlignment = JLabel.CENTER
            _placeLabel.verticalAlignment = JLabel.CENTER
            _placeLabel.foreground = Color.BLACK
            _placeLabel.border = EmptyBorder(10, 0, 0, 0)

            _timeLabel = JLabel(formatter.format(java.time.ZonedDateTime.now(_timezone)))
            _timeLabel.font = StandardFont.readBoldFont(24)
            _timeLabel.horizontalAlignment = JLabel.CENTER
            _timeLabel.verticalAlignment = JLabel.CENTER
            _timeLabel.foreground = Color.BLACK
            _timeLabel.border = EmptyBorder(0, 0, 0, 0)
        }

        var place: String
        get() { return _placeLabel.text }
        set(place) {
            _placeLabel.text = place
            repaint()
        }

        var timezone: ZoneId
        get() { return _timezone }
        set(timezone) {
            _timezone = timezone
            updateTime()
        }

        val time: String
        get() { return _timeLabel.text }

        fun updateTime() {
            try {
                val newTime = formatter.format(clock.instant().atZone(_timezone))
                if (newTime != _timeLabel.text) {
                    _timeLabel.text = newTime
                    EventQueue.invokeLater { _timeLabel.repaint() }
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
                _placeLabel,
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
                })
            add(
                _timeLabel,
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
                })
            executor.scheduleAtFixedRate({ updateTime() }, 0, 100, TimeUnit.MILLISECONDS)
        }
    }

    companion object {
        private val DEFAULT_IMAGE = BufferedImage(200, 50, BufferedImage.TYPE_4BYTE_ABGR)
        @Throws(IOException::class)
        @JvmStatic fun createImage(url: URL): Image {
            return ImageIO.read(url)
        }

        @JvmStatic fun createImage(text: String, foreground: Color, background: Color): Image {
            val font = StandardFont.readBoldFont(24)
            var bounds: Rectangle2D
            run {
                val g = BufferedImage(200, 50, BufferedImage.TYPE_4BYTE_ABGR).graphics
                bounds = g.getFontMetrics(font).getStringBounds(text, g)
                g.dispose()
            }
            val img = BufferedImage(
                max(200, bounds.width.toInt() + 10), 50, BufferedImage.TYPE_4BYTE_ABGR
            )
            val g = img.graphics
            (g as Graphics2D)
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
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
    }
}
