package com.joecollins.graphics.components

import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.utils.PanelUtils.pad
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.social.generic.Link
import com.joecollins.models.general.social.generic.Media
import com.joecollins.models.general.social.generic.Post
import com.joecollins.models.general.social.generic.User
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.map
import com.vdurmont.emoji.EmojiParser
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.Image
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.text.DecimalFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

abstract class SocialMediaFrame<P : Post>(
    post: Flow.Publisher<out Post>,
    private val color: Color,
    private val timezone: ZoneId = ZoneId.systemDefault(),
) : JPanel() {

    abstract val emojiVersion: String
    abstract val protectedUserText: String
    abstract val logo: Shape

    init {
        border = MatteBorder(1, 1, 1, 1, color)
        background = Color.WHITE
        layout = BorderLayout()

        add(HeaderFrame(post.map { it.user }), BorderLayout.NORTH)

        val blank = JPanel()
        blank.background = Color.WHITE
        blank.preferredSize = Dimension(55, 1)
        add(blank, BorderLayout.WEST)

        val postPanel = JPanel()
        postPanel.layout = BoxLayout(postPanel, BoxLayout.Y_AXIS)
        postPanel.background = Color.WHITE
        add(postPanel, BorderLayout.CENTER)

        val postLabel = JLabel()
        postLabel.font = StandardFont.readNormalFont(12)
        postLabel.foreground = Color.BLACK
        postLabel.verticalAlignment = JLabel.TOP
        postLabel.horizontalAlignment = JLabel.LEFT
        postLabel.alignmentX = LEFT_ALIGNMENT
        post.subscribe(
            Subscriber(
                eventQueueWrapper {
                    postLabel.text = formatText(it, false, postLabel)
                },
            ),
        )
        postPanel.add(postLabel)

        postLabel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                postLabel.text = postLabel.text.replace(Regex("<html><body width=[0-9]+"), "<html><body width=${postLabel.width}")
                postPanel.invalidate()
                postPanel.revalidate()
                postPanel.repaint()
            }
        })

        val urlPanel = JPanel()
        urlPanel.background = Color.WHITE
        urlPanel.layout = BoxLayout(urlPanel, BoxLayout.X_AXIS)
        urlPanel.alignmentX = LEFT_ALIGNMENT
        postPanel.add(urlPanel)
        post.subscribe(
            Subscriber(
                eventQueueWrapper { status ->
                    val urls = status.links.filter { !it.isFromSocialNetwork }
                    val quotedURL = status.quoted?.url.toString()
                    urlPanel.isVisible = urls.isNotEmpty()
                    urlPanel.removeAll()
                    urlPanel.add(Box.createHorizontalGlue())
                    urls.filter { !status.user.isProtected }.filter { it.expandedURL.toString() != quotedURL }.mapNotNull { it.preview }.forEach {
                        urlPanel.add(UrlPanel(it))
                    }
                    urlPanel.add(Box.createHorizontalGlue())
                },
            ),
        )

        val mediaPanel = JPanel()
        mediaPanel.background = Color.WHITE
        mediaPanel.layout = GridLayout(0, 1)
        mediaPanel.alignmentX = LEFT_ALIGNMENT
        postPanel.add(mediaPanel)
        post.map { it.mediaEntities }.subscribe(
            Subscriber(
                eventQueueWrapper { media ->
                    mediaPanel.isVisible = media.isNotEmpty()
                    mediaPanel.removeAll()
                    mediaPanel.layout = GridLayout(0, ceil(sqrt(media.size.toDouble())).toInt().coerceAtLeast(1))
                    media.forEach {
                        mediaPanel.add(MediaPanel(it))
                    }
                },
            ),
        )

        val quotedPanel = JPanel()
        quotedPanel.background = Color.WHITE
        quotedPanel.layout = GridLayout(0, 1)
        quotedPanel.alignmentX = LEFT_ALIGNMENT
        quotedPanel.border = EmptyBorder(0, 0, 5, 5)
        postPanel.add(quotedPanel)
        post.map { it.quoted }.subscribe(
            Subscriber(
                eventQueueWrapper { status ->
                    quotedPanel.isVisible = (status != null)
                    quotedPanel.removeAll()
                    status?.let { quotedPanel.add(QuotedPanel(it)) }
                    quotedPanel.invalidate()
                    quotedPanel.revalidate()
                    postPanel.invalidate()
                    postPanel.revalidate()
                    repaint()
                },
            ),
        )

        val pollPanel = BarFrameBuilder.basic(
            barsPublisher = post.map { p ->
                val polls = p.polls
                if (polls.isNotEmpty()) {
                    val options = polls[0].options
                    val total = options.values.sum().toDouble().coerceAtLeast(1e-6)
                    options.entries.map {
                        BarFrameBuilder.BasicBar.of(
                            it.key,
                            color,
                            it.value,
                            "${DecimalFormat("#,##0").format(it.value)} (${DecimalFormat("0.0%").format(it.value / total)})",
                        )
                    }
                } else {
                    emptyList()
                }
            },
        ).pad().apply { isVisible = false }
        post.subscribe(
            Subscriber(
                eventQueueWrapper {
                    pollPanel.isVisible = it.polls.isNotEmpty()
                    pollPanel.invalidate()
                    pollPanel.revalidate()
                },
            ),
        )
        postPanel.add(pollPanel)

        postPanel.add(Box.createVerticalGlue())

        val timeLabel = JLabel()
        timeLabel.font = StandardFont.readNormalFont(12)
        timeLabel.foreground = Color.BLACK
        timeLabel.border = EmptyBorder(2, 0, -2, 0)
        timeLabel.horizontalAlignment = JLabel.RIGHT
        post.map { if (it.user.isProtected) null else it.createdAt }.subscribe(
            Subscriber(
                eventQueueWrapper {
                    timeLabel.text =
                        if (it == null) {
                            ""
                        } else {
                            DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm:ss z ").format(it.atZone(timezone))
                        }
                },
            ),
        )
        add(timeLabel, BorderLayout.SOUTH)
    }

    private fun formatText(status: Post, isQuoted: Boolean, postLabel: JLabel): String {
        val colorHex = (color.rgb.and(0xffffff)).toString(16)
        val quotedURL = status.quoted?.url?.toString()
        val font = postLabel.font
        if (status.user.isProtected) {
            return "<html><body width=${postLabel.width} style='font: ${font.size}px \"${font.name}\", ${font.family};'><span style='color:#$colorHex'>" +
                protectedUserText +
                "<br/>&nbsp;</span></body></html>"
        }
        var htmlText = status.text.replace("\n", "<br/>").let { text ->
            EmojiParser.parseFromUnicode(text) { e ->
                e.emoji.htmlHexadecimal.split(";").asSequence()
                    .filter { it.isNotEmpty() }
                    .joinToString("-") { it.replace("&#x", "") }
                    .let { "<img src='https://images.emojiterra.com/$emojiVersion/512px/$it.png' height='16' width='16' />" }
            }
        }
        status.hashtagEntities.filter { !status.user.isProtected }.forEach {
            htmlText = htmlText.replace("#${it.text}", "<span style='color:#$colorHex'>#${it.text}</span>")
        }
        status.userMentionEntities.filter { !status.user.isProtected }.forEach {
            htmlText = htmlText.replace(it.text, "<span style='color:#$colorHex'>${it.display}</span>")
        }
        status.links.filter { !status.user.isProtected }.filter { isQuoted || !it.isFromSocialNetwork }.forEach {
            htmlText = htmlText.replace(it.shortURL, if (!isQuoted && it.expandedURL.toString() == quotedURL) "" else "<span style='color:#$colorHex'>${it.displayURL}${if (it.displayURL == it.shortURL || it.shortURL.endsWith("...") || it.displayURL == "https://" + it.shortURL) "" else "(${it.shortURL})"}</span>")
        }
        status.links.filter { !status.user.isProtected }.filter { !isQuoted && it.isFromSocialNetwork }.forEach {
            htmlText = htmlText.replace(it.shortURL, "")
        }
        status.mediaEntities.filter { !status.user.isProtected }.filter { it.displayURL != null }.forEach {
            htmlText = htmlText.replace(it.displayURL!!, "")
        }
        status.emojis.filter { !status.user.isProtected }.forEach {
            htmlText = htmlText.replace(it.text, "<img src='${it.url}' height='16' width='16'")
        }
        return "<html><body width=${postLabel.width} style='font: ${font.size}px \"${font.name}\", ${font.family};'>$htmlText<br/>&nbsp;</body></html>"
    }

    private inner class HeaderFrame(user: Flow.Publisher<out User>) : JPanel() {
        private var image: Image? = null
        private var fullName: String = ""
        private var screenName: String = ""
        private var verified: Boolean = false

        init {
            background = color
            preferredSize = Dimension(1024, 50)

            user.subscribe(
                Subscriber(
                    eventQueueWrapper {
                        val originalImage = ImageIO.read(it.profileImageURL)
                        val size = 48
                        val resizedImage = BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR)
                        val g = resizedImage.createGraphics()
                        (g as Graphics2D)
                            .setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON,
                            )
                        g.clip = Ellipse2D.Double(0.0, 0.0, size.toDouble(), size.toDouble())
                        g.drawImage(originalImage, 0, 0, size, size, null)
                        g.dispose()
                        image = resizedImage
                        fullName = it.name
                        screenName = it.screenName
                        repaint()
                    },
                ),
            )
        }

        private val fullNameFont = StandardFont.readNormalFont(24)
        private val screenNameFont = StandardFont.readNormalFont(16)

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON,
                )
            g
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                )
            image?.let { g.drawImage(it, 1, 1, null) }
            g.color = Color.WHITE
            g.font = fullNameFont
            g.drawString(fullName, 55, 22)
            g.font = screenNameFont
            g.drawString(screenName, 55, 42)
            if (verified) {
                val transform = AffineTransform()
                transform.translate(65.0 + g.getFontMetrics(fullNameFont).stringWidth(fullName), 4.0)
                transform.scale(0.2, 0.2)
                val tick = transform.createTransformedShape(ImageGenerator.createTickShape())
                g.fill(tick)
            }

            val logoShape = logo.let { s ->
                val transform = AffineTransform.getTranslateInstance(-s.bounds.minX, -s.bounds.minY)
                transform.createTransformedShape(s)
            }
            val resizeFactor = (height - 20.0) / logoShape.bounds.height
            val resizedLogo = AffineTransform.getScaleInstance(resizeFactor, resizeFactor).createTransformedShape(logoShape)
            val translateFactor = (width - 10.0 - resizedLogo.bounds.width)
            val finalLogo = AffineTransform.getTranslateInstance(translateFactor, 10.0).createTransformedShape(resizedLogo)
            g.fill(finalLogo)
        }
    }

    private inner class UrlPanel(link: Link.Preview) : JPanel() {
        var image: Image? = link.image
        var title: String = link.title
        var domain: String = link.domain

        private val imageWidth = 300
        private val imageHeight = imageWidth / 2
        private val lowerHeight = 40

        init {
            preferredSize = Dimension(imageWidth, imageHeight + lowerHeight)
            border = MatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY)
            background = Color.WHITE
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            (g as Graphics2D)
                .setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON,
                )
            g
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                )
            image?.let {
                val xScale = width.toDouble() / it.getWidth(null)
                val yScale = (height - lowerHeight).toDouble() / it.getHeight(null)
                val scale = min(xScale, yScale).coerceAtMost(1.0)
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

    private inner class MediaPanel(mediaEntity: Media) : JPanel() {
        private var image: Image?

        init {
            border = MatteBorder(1, 1, 1, 1, Color.WHITE)
            background = Color.WHITE

            image = try {
                ImageIO.read(mediaEntity.mediaURL)
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

    private inner class QuotedPanel(quotedStatus: Post) : JPanel() {
        init {
            border = MatteBorder(1, 1, 1, 1, color)
            background = Color.WHITE
            layout = BorderLayout()

            val userPanel = QuotedUserPanel(quotedStatus.user)
            add(userPanel, BorderLayout.NORTH)

            val postPanel = JPanel()
            postPanel.background = Color.WHITE
            postPanel.layout = BoxLayout(postPanel, BoxLayout.Y_AXIS)
            add(postPanel, BorderLayout.CENTER)

            val postLabel = JLabel()
            postLabel.font = StandardFont.readNormalFont(12)
            postLabel.foreground = Color.BLACK
            postLabel.text = formatText(quotedStatus, true, postLabel)
            postLabel.alignmentX = LEFT_ALIGNMENT
            postPanel.add(postLabel)

            postLabel.addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    postLabel.text = postLabel.text.replace(Regex("<html><body width=[0-9]+"), "<html><body width=${postLabel.width}")
                    postPanel.invalidate()
                    postPanel.revalidate()
                    postPanel.repaint()
                }
            })

            if (quotedStatus.mediaEntities.isNotEmpty()) {
                val media = quotedStatus.mediaEntities
                val mediaPanel = JPanel()
                mediaPanel.background = Color.WHITE
                mediaPanel.layout = GridLayout(0, 1)
                mediaPanel.alignmentX = LEFT_ALIGNMENT
                mediaPanel.layout = GridLayout(0, ceil(sqrt(media.size.toDouble())).toInt().coerceAtLeast(1))
                media.forEach {
                    mediaPanel.add(MediaPanel(it))
                }
                postPanel.add(mediaPanel)
            }

            postPanel.add(Box.createVerticalGlue())

            val timeLabel = JLabel()
            timeLabel.font = StandardFont.readNormalFont(12)
            timeLabel.foreground = Color.BLACK
            timeLabel.border = EmptyBorder(2, 0, -2, 0)
            timeLabel.horizontalAlignment = JLabel.RIGHT
            timeLabel.text =
                if (quotedStatus.user.isProtected) {
                    ""
                } else {
                    DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm:ss z ").format(quotedStatus.createdAt.atZone(timezone))
                }
            add(timeLabel, BorderLayout.SOUTH)
        }

        private inner class QuotedUserPanel(val user: User) : JPanel() {
            init {
                background = color
                preferredSize = Dimension(1024, 24)
            }

            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                (g as Graphics2D)
                    .setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON,
                    )
                g
                    .setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
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
                g.drawString(user.screenName, x, 18)
            }
        }
    }

    final override fun add(comp: Component, constraints: Any) {
        super.add(comp, constraints)
    }
}
