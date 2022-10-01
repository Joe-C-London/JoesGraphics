package com.joecollins.graphics.components

import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.map
import com.vdurmont.emoji.EmojiParser
import io.webfolder.cdp.Launcher
import twitter4j.MediaEntity
import twitter4j.Status
import twitter4j.TwitterFactory
import twitter4j.URLEntity
import twitter4j.User
import twitter4j.conf.ConfigurationBuilder
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.Image
import java.awt.RenderingHints
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.net.URL
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Properties
import java.util.concurrent.Flow
import javax.imageio.ImageIO
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class TweetFrame(tweet: Flow.Publisher<out Status>, private val timezone: ZoneId = ZoneId.systemDefault()) : JPanel() {
    private val twitterColor = Color(0x00acee)

    init {
        border = MatteBorder(1, 1, 1, 1, twitterColor)
        background = Color.WHITE
        layout = BorderLayout()

        add(TweetHeaderFrame(tweet.map { it.user }), BorderLayout.NORTH)

        val blank = JPanel()
        blank.background = Color.WHITE
        blank.preferredSize = Dimension(55, 1)
        add(blank, BorderLayout.WEST)

        val tweetPanel = JPanel()
        tweetPanel.layout = BoxLayout(tweetPanel, BoxLayout.Y_AXIS)
        tweetPanel.background = Color.WHITE
        add(tweetPanel, BorderLayout.CENTER)

        val tweetLabel = JLabel()
        tweetLabel.font = StandardFont.readNormalFont(16)
        tweetLabel.verticalAlignment = JLabel.TOP
        tweetLabel.horizontalAlignment = JLabel.LEFT
        tweetLabel.alignmentX = Component.LEFT_ALIGNMENT
        tweet.subscribe(Subscriber(eventQueueWrapper { tweetLabel.text = formatTweetText(it, false, tweetLabel) }))
        tweetPanel.add(tweetLabel)

        tweetLabel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                tweetLabel.text = tweetLabel.text.replace(Regex("<html><body width=[0-9]+>"), "<html><body width=${tweetLabel.width}>")
                tweetPanel.invalidate()
                tweetPanel.revalidate()
                tweetPanel.repaint()
            }
        })

        val urlPanel = JPanel()
        urlPanel.background = Color.WHITE
        urlPanel.layout = BoxLayout(urlPanel, BoxLayout.X_AXIS)
        urlPanel.alignmentX = Component.LEFT_ALIGNMENT
        tweetPanel.add(urlPanel)
        tweet.subscribe(
            Subscriber(
                eventQueueWrapper { status ->
                    val urls = status.urlEntities.filter { !it.expandedURL.startsWith("https://twitter.com/") }
                    val quotedURL = status.quotedStatus?.let { "https://twitter.com/${it.user.screenName}/status/${it.id}" }
                    urlPanel.isVisible = urls.isNotEmpty()
                    urlPanel.removeAll()
                    urlPanel.add(Box.createHorizontalGlue())
                    urls.filter { it.expandedURL != quotedURL }.forEach {
                        urlPanel.add(UrlPanel(it))
                    }
                    urlPanel.add(Box.createHorizontalGlue())
                }
            )
        )

        val mediaPanel = JPanel()
        mediaPanel.background = Color.WHITE
        mediaPanel.layout = GridLayout(0, 1)
        mediaPanel.alignmentX = Component.LEFT_ALIGNMENT
        tweetPanel.add(mediaPanel)
        tweet.map { it.mediaEntities }.subscribe(
            Subscriber(
                eventQueueWrapper { media ->
                    mediaPanel.isVisible = media.isNotEmpty()
                    mediaPanel.removeAll()
                    mediaPanel.layout = GridLayout(0, ceil(sqrt(media.size.toDouble())).toInt().coerceAtLeast(1))
                    media.forEach {
                        mediaPanel.add(MediaPanel(it))
                    }
                }
            )
        )

        val quotedPanel = JPanel()
        quotedPanel.background = Color.WHITE
        quotedPanel.layout = GridLayout(0, 1)
        quotedPanel.alignmentX = Component.LEFT_ALIGNMENT
        quotedPanel.border = EmptyBorder(0, 0, 5, 5)
        tweetPanel.add(quotedPanel)
        tweet.map { it.quotedStatus }.subscribe(
            Subscriber(
                eventQueueWrapper { status ->
                    quotedPanel.isVisible = (status != null)
                    quotedPanel.removeAll()
                    status?.let { quotedPanel.add(QuotedPanel(it)) }
                    quotedPanel.invalidate()
                    quotedPanel.revalidate()
                    tweetPanel.invalidate()
                    tweetPanel.revalidate()
                    repaint()
                }
            )
        )

        tweetPanel.add(Box.createVerticalGlue())

        val timeLabel = JLabel()
        timeLabel.font = StandardFont.readNormalFont(12)
        timeLabel.border = EmptyBorder(2, 0, -2, 0)
        timeLabel.horizontalAlignment = JLabel.RIGHT
        tweet.map { if (it.user.isProtected) null else it.createdAt }.subscribe(Subscriber(eventQueueWrapper { timeLabel.text = if (it == null) "" else DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm:ss z ").format(it.toInstant().atZone(timezone)) }))
        add(timeLabel, BorderLayout.SOUTH)
    }

    private fun formatTweetText(status: Status, isQuoted: Boolean, tweetLabel: JLabel): String {
        val twitterColorHex = (twitterColor.rgb.and(0xffffff)).toString(16)
        val quotedURL = status.quotedStatus?.let { "https://twitter.com/${it.user.screenName}/status/${it.id}" }
        if (status.user.isProtected) {
            return "<html><body width=${tweetLabel.width}><span style='color:#$twitterColorHex'>" +
                "This user's tweets are protected, and this tweet has therefore been blocked from this frame." +
                "<br/>&nbsp;</span></body></html>"
        }
        var htmlText = status.text.replace("\n", "<br/>").let { text ->
            EmojiParser.parseFromUnicode(text) { e ->
                e.emoji.htmlHexadecimal.split(";").asSequence()
                    .filter { it.isNotEmpty() }
                    .joinToString("-") { it.replace("&#x", "") }
                    .let { "<img src='https://images.emojiterra.com/twitter/v14.0/512px/$it.png' height='16' width='16' />" }
            }
        }
        status.hashtagEntities.forEach {
            htmlText = htmlText.replace("#${it.text}", "<span style='color:#$twitterColorHex'>#${it.text}</span>")
        }
        status.userMentionEntities.forEach {
            htmlText = htmlText.replace("@${it.text}", "<span style='color:#$twitterColorHex'>@${it.text}</span>")
        }
        status.urlEntities.filter { isQuoted || !it.expandedURL.startsWith("https://twitter.com/") }.forEach {
            htmlText = htmlText.replace(it.url, if (!isQuoted && it.expandedURL == quotedURL) "" else "<span style='color:#$twitterColorHex'>${it.displayURL}(${it.url})</span>")
        }
        status.urlEntities.filter { !isQuoted && it.expandedURL.startsWith("https://twitter.com/") }.forEach {
            htmlText = htmlText.replace(it.url, "")
        }
        status.mediaEntities.forEach {
            htmlText = htmlText.replace(it.displayURL, "")
        }
        return "<html><body width=${tweetLabel.width}>$htmlText<br/>&nbsp;</body></html>"
    }

    private inner class TweetHeaderFrame(user: Flow.Publisher<out User>) : JPanel() {
        private var image: Image? = null
        private var fullName: String = ""
        private var screenName: String = ""
        private var verified: Boolean = false

        init {
            background = twitterColor
            preferredSize = Dimension(1024, 50)

            user.subscribe(
                Subscriber(
                    eventQueueWrapper {
                        val originalImage = ImageIO.read(URL(it.profileImageURL))
                        val size = 48
                        val resizedImage = BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR)
                        val g = resizedImage.createGraphics()
                        (g as Graphics2D)
                            .setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON
                            )
                        g.clip = Ellipse2D.Double(0.0, 0.0, size.toDouble(), size.toDouble())
                        g.drawImage(originalImage, 0, 0, size, size, null)
                        g.dispose()
                        image = resizedImage
                        fullName = it.name
                        screenName = it.screenName
                        verified = it.isVerified
                        repaint()
                    }
                )
            )
        }

        private val fullNameFont = StandardFont.readNormalFont(24)
        private val screenNameFont = StandardFont.readNormalFont(16)

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                )
            g
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
            image?.let { g.drawImage(it, 1, 1, null) }
            g.color = Color.WHITE
            g.font = fullNameFont
            g.drawString(fullName, 55, 22)
            g.font = screenNameFont
            g.drawString("@$screenName", 55, 42)
            if (verified) {
                val transform = AffineTransform()
                transform.translate(65.0 + g.getFontMetrics(fullNameFont).stringWidth(fullName), 4.0)
                transform.scale(0.2, 0.2)
                val tick = transform.createTransformedShape(ImageGenerator.createTickShape())
                g.fill(tick)
            }
        }
    }

    private inner class UrlPanel(urlEntity: URLEntity) : JPanel() {
        var image: Image?
        var title: String
        var domain: String

        private val imageWidth = 300
        private val imageHeight = imageWidth / 2
        private val lowerHeight = 40

        init {
            preferredSize = Dimension(imageWidth, imageHeight + lowerHeight)
            border = MatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY)
            background = Color.WHITE

            try {
                val launcher = Launcher()
                launcher.launch().use { sessionFactory ->
                    sessionFactory.create().use { session ->
                        session.navigate(urlEntity.expandedURL).waitDocumentReady().wait(5000)
                        val imageURL = session.getAttribute("//meta[@name='twitter:image:src']", "content")
                        image = imageURL?.let { ImageIO.read(URL(it)) }
                        title = session.getAttribute("//meta[@name='twitter:title']", "content")
                            ?: session.getText("//title")
                        domain = session.getAttribute("//meta[@name='twitter:domain']", "content")
                            ?: urlEntity.expandedURL.let { it.substring(it.indexOf("//") + 2) }.let { it.substring(0, it.indexOf("/")) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                image = null
                title = "Unable to get title"
                domain = ""
            }
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                )
            g
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
            image?.let {
                val xScale = width.toDouble() / it.getWidth(null)
                val yScale = (height - lowerHeight).toDouble() / it.getHeight(null)
                val scale = min(xScale, yScale)
                val w = (it.getWidth(null) * scale).roundToInt()
                val h = (it.getHeight(null) * scale).roundToInt()
                g.drawImage(it, (width - w) / 2, (height - lowerHeight - h) / 2, w, h, null)
            }
            g.color = Color.LIGHT_GRAY
            g.fillRect(0, height - lowerHeight, width, lowerHeight)
            g.color = Color.BLACK
            for (size in 16 downTo 1) {
                g.font = StandardFont.readNormalFont(size)
                if (g.fontMetrics.stringWidth(title) < width) break
            }
            g.drawString(title, 2, height - 20)
            for (size in 10 downTo 1) {
                g.font = StandardFont.readNormalFont(size)
                if (g.fontMetrics.stringWidth(domain) < width) break
            }
            g.drawString(domain, 2, height - 5)
        }
    }

    private inner class MediaPanel(mediaEntity: MediaEntity) : JPanel() {
        private var image: Image?

        init {
            border = MatteBorder(1, 1, 1, 1, Color.WHITE)
            background = Color.WHITE

            image = try {
                ImageIO.read(URL(mediaEntity.mediaURL))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            val imageWidth = image?.getWidth(null) ?: 300
            val imageHeight = image?.getHeight(null) ?: 150
            preferredSize = Dimension(imageWidth, imageHeight)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            image?.let {
                val xScale = width.toDouble() / it.getWidth(null)
                val yScale = (height).toDouble() / it.getHeight(null)
                val scale = min(xScale, yScale)
                val w = (it.getWidth(null) * scale).roundToInt()
                val h = (it.getHeight(null) * scale).roundToInt()
                g.drawImage(it, (width - w) / 2, 0, w, h, null)
            }
        }
    }

    private inner class QuotedPanel(quotedStatus: Status) : JPanel() {
        init {
            border = MatteBorder(1, 1, 1, 1, twitterColor)
            background = Color.WHITE
            layout = BorderLayout()

            val userPanel = QuotedUserPanel(quotedStatus.user)
            add(userPanel, BorderLayout.NORTH)

            val tweetPanel = JPanel()
            tweetPanel.background = Color.WHITE
            tweetPanel.layout = BoxLayout(tweetPanel, BoxLayout.Y_AXIS)
            add(tweetPanel, BorderLayout.CENTER)

            val tweetLabel = JLabel()
            tweetLabel.font = StandardFont.readNormalFont(16)
            tweetLabel.text = formatTweetText(quotedStatus, true, tweetLabel)
            tweetLabel.alignmentX = Component.LEFT_ALIGNMENT
            tweetPanel.add(tweetLabel)

            tweetLabel.addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    tweetLabel.text = tweetLabel.text.replace(Regex("<html><body width=[0-9]+>"), "<html><body width=${tweetLabel.width}>")
                    tweetPanel.invalidate()
                    tweetPanel.revalidate()
                    tweetPanel.repaint()
                }
            })

            if (quotedStatus.mediaEntities.isNotEmpty()) {
                val media = quotedStatus.mediaEntities
                val mediaPanel = JPanel()
                mediaPanel.background = Color.WHITE
                mediaPanel.layout = GridLayout(0, 1)
                mediaPanel.alignmentX = Component.LEFT_ALIGNMENT
                mediaPanel.layout = GridLayout(0, ceil(sqrt(media.size.toDouble())).toInt().coerceAtLeast(1))
                media.forEach {
                    mediaPanel.add(MediaPanel(it))
                }
                tweetPanel.add(mediaPanel)
            }

            tweetPanel.add(Box.createVerticalGlue())

            val timeLabel = JLabel()
            timeLabel.font = StandardFont.readNormalFont(12)
            timeLabel.border = EmptyBorder(2, 0, -2, 0)
            timeLabel.horizontalAlignment = JLabel.RIGHT
            timeLabel.text = if (quotedStatus.user.isProtected) "" else DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm:ss z ").format(quotedStatus.createdAt.toInstant().atZone(timezone))
            add(timeLabel, BorderLayout.SOUTH)
        }

        private inner class QuotedUserPanel(val user: User) : JPanel() {
            init {
                background = twitterColor
                preferredSize = Dimension(1024, 24)
            }

            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                (g as Graphics2D)
                    .setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                    )
                g
                    .setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                    )
                g.color = Color.WHITE
                var x = 0

                g.font = StandardFont.readNormalFont(20)
                g.drawString(user.name, x, 18)
                x += g.fontMetrics.stringWidth(user.name) + 5

                if (user.isVerified) {
                    val transform = AffineTransform()
                    transform.translate(x.toDouble(), 4.0)
                    transform.scale(0.15, 0.15)
                    val tick = transform.createTransformedShape(ImageGenerator.createTickShape())
                    g.fill(tick)
                    x += 20
                }

                g.font = StandardFont.readNormalFont(14)
                g.drawString("@${user.screenName}", x, 18)
            }
        }
    }

    companion object {
        fun createTweetFrame(tweetId: Flow.Publisher<out Long>): TweetFrame {
            val cb = ConfigurationBuilder()
            val twitterPropertiesFile = this::class.java.classLoader.getResourceAsStream("twitter.properties")
                ?: throw IllegalStateException("Unable to find twitter.properties")
            val properties = Properties()
            properties.load(twitterPropertiesFile)
            cb.setDebugEnabled(true)
                .setOAuthConsumerKey(properties["oauthConsumerKey"].toString())
                .setOAuthConsumerSecret(properties["oauthConsumerSecret"].toString())
                .setOAuthAccessToken(properties["oauthAccessToken"].toString())
                .setOAuthAccessTokenSecret(properties["oauthAccessTokenSecret"].toString())
            val instance = TwitterFactory(cb.build()).instance
            return TweetFrame(tweetId.map { instance.showStatus(it) })
        }
    }
}
