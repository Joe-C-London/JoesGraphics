package com.joecollins.graphics

import com.joecollins.graphics.components.GraphicsFrame
import com.joecollins.models.general.twitter.TwitterV2InstanceFactory
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.twitter.clientlib.model.TweetCreateRequest
import com.twitter.clientlib.model.TweetCreateRequestMedia
import twitter4j.HttpClientFactory
import twitter4j.HttpParameter
import twitter4j.HttpResponseEvent
import twitter4j.HttpResponseListener
import twitter4j.JSONObject
import twitter4j.StatusUpdate
import twitter4j.TwitterFactory
import twitter4j.auth.AuthorizationFactory
import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.LayoutManager
import java.awt.Point
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.ClipboardOwner
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.Properties
import javax.imageio.ImageIO
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.border.EmptyBorder
import javax.swing.filechooser.FileFilter
import kotlin.math.max

class GenericWindow<T : JPanel> @JvmOverloads constructor(private val panel: T, title: String? = panel.javaClass.simpleName) : JFrame() {

    fun withControlPanel(panel: JPanel): GenericWindow<T> {
        contentPane.add(panel, "control-panel")
        return this
    }

    class TweetFrame(panel: JPanel) : JDialog() {
        private fun sendTweet(tweet: String, image: File, altText: String? = null) {
            sendTweetV2(tweet, image, altText)
        }

        private fun sendTweetV2(tweet: String, image: File, altText: String?) {
            val instance = TwitterV2InstanceFactory.instance
            val mediaId = uploadMediaV1(image, altText)
            val mediaRequest = TweetCreateRequestMedia()
            mediaRequest.mediaIds = listOf(mediaId.toString())
            val tweetRequest = TweetCreateRequest()
            tweetRequest.text = tweet
            tweetRequest.media = mediaRequest
            instance.tweets().createTweet(tweetRequest).execute()
        }

        private fun uploadMediaV1(image: File, altText: String?): Long {
            val cb = ConfigurationBuilder()
            val twitterPropertiesFile = this.javaClass.classLoader.getResourceAsStream("twitter.properties")
                ?: throw IllegalStateException("Unable to find twitter.properties")
            val properties = Properties()
            properties.load(twitterPropertiesFile)
            cb.setDebugEnabled(true)
                .setOAuthConsumerKey(properties["oauthConsumerKey"].toString())
                .setOAuthConsumerSecret(properties["oauthConsumerSecret"].toString())
                .setOAuthAccessToken(properties["oauthAccessToken"].toString())
                .setOAuthAccessTokenSecret(properties["oauthAccessTokenSecret"].toString())
            val conf = cb.build()
            val twitter = TwitterFactory(conf).instance
            val response = twitter.uploadMedia(image)

            if (altText != null) {
                addAltText(conf, response.mediaId, altText)
            }

            return response.mediaId
        }

        private fun addAltText(conf: Configuration, mediaId: Long, altText: String) {
            val client = HttpClientFactory.getInstance(conf.httpClientConfiguration)
            val auth = AuthorizationFactory.getInstance(conf)
            val params = JSONObject()
            params.put("media_id", mediaId)
            params.put("alt_text", JSONObject().also { it.put("text", altText) })
            client.post(
                "${conf.uploadBaseURL}/media/metadata/create.json",
                arrayOf(
                    HttpParameter(params)
                ),
                auth,
                object : HttpResponseListener {
                    override fun httpResponseReceived(event: HttpResponseEvent) {
                        // no-op
                    }
                }
            )
        }

        private fun sendTweetV1(tweet: String, image: File) {
            val status = StatusUpdate(tweet)
            status.media(image)
            val cb = ConfigurationBuilder()
            val twitterPropertiesFile = this.javaClass.classLoader.getResourceAsStream("twitter.properties")
                ?: throw IllegalStateException("Unable to find twitter.properties")
            val properties = Properties()
            properties.load(twitterPropertiesFile)
            cb.setDebugEnabled(true)
                .setOAuthConsumerKey(properties["oauthConsumerKey"].toString())
                .setOAuthConsumerSecret(properties["oauthConsumerSecret"].toString())
                .setOAuthAccessToken(properties["oauthAccessToken"].toString())
                .setOAuthAccessTokenSecret(properties["oauthAccessTokenSecret"].toString())
            TwitterFactory(cb.build()).instance.updateStatus(status)
        }

        init {
            size = Dimension(500, 500)
            isModal = true
            title = "Tweet"
            contentPane.layout = BorderLayout()
            val twitterColor = Color(0x00acee)

            var subscriber: Subscriber<String?>? = null

            val mainPanel = JPanel()
            mainPanel.layout = GridLayout(0, 1)
            contentPane.add(mainPanel, BorderLayout.CENTER)

            val textArea = JTextArea()
            textArea.lineWrap = true
            textArea.wrapStyleWord = true
            mainPanel.add(textArea)

            val altTextArea = JTextArea()
            altTextArea.lineWrap = true
            altTextArea.wrapStyleWord = true
            altTextArea.isEditable = false
            altTextArea.text = ""
            altTextArea.background = Color.LIGHT_GRAY
            mainPanel.add(JScrollPane(altTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER))

            val bottomPanel = JPanel()
            bottomPanel.background = twitterColor
            bottomPanel.layout = BorderLayout()
            contentPane.add(bottomPanel, BorderLayout.SOUTH)

            val charLabel = JLabel("${textArea.text.length}/280 & ${altTextArea.text.length}/1000")
            charLabel.foreground = Color.WHITE
            bottomPanel.add(charLabel, BorderLayout.WEST)
            textArea.addKeyListener(
                object : KeyAdapter() {
                    override fun keyReleased(e: KeyEvent) {
                        charLabel.text = "${textArea.text.length}/280 & ${altTextArea.text.length}/1000"
                    }
                }
            )

            if (panel is AltTextProvider) {
                subscriber = Subscriber(
                    eventQueueWrapper {
                        val text = it?.takeIf { t -> t.length <= 1000 }
                        altTextArea.text = text ?: ""
                        charLabel.text = "${textArea.text.length}/280 & ${(text ?: "").length}/1000"
                    }
                )
                panel.altText.subscribe(subscriber)
            }

            val tweetButton = JButton("Tweet")
            tweetButton.background = twitterColor
            tweetButton.foreground = Color.WHITE
            bottomPanel.add(tweetButton, BorderLayout.EAST)
            tweetButton.addActionListener {
                val file: File
                try {
                    file = File.createTempFile("joes-politics", ".png")
                    file.deleteOnExit()
                    val img = generateImage(panel)
                    ImageIO.write(img, "png", file)
                } catch (exc: IOException) {
                    JOptionPane.showMessageDialog(panel, "Could not save image: " + exc.message)
                    exc.printStackTrace()
                    return@addActionListener
                }
                try {
                    tweetButton.isEnabled = false
                    tweetButton.text = "Tweeting..."
                    val tweet = textArea.text
                    val altText = altTextArea.text.takeUnless { it.isNullOrEmpty() }
                    sendTweet(tweet, file, altText)
                    isVisible = false
                    subscriber?.unsubscribe()
                } catch (exc: Exception) {
                    JOptionPane.showMessageDialog(panel, "Could not send tweet: " + exc.message)
                    exc.printStackTrace()
                    tweetButton.isEnabled = true
                    tweetButton.text = "Tweet"
                }
            }
        }
    }

    companion object {
        private fun saveImageToFile(panel: JPanel) {
            val fileChooser = JFileChooser()
            fileChooser.currentDirectory = File(System.getProperty("user.home"), "Pictures/Joe's Politics")
            fileChooser.fileFilter = object : FileFilter() {
                override fun accept(f: File): Boolean {
                    if (f.isDirectory) {
                        return true
                    }
                    val name = f.name
                    return name.endsWith(".png")
                }

                override fun getDescription(): String {
                    return "PNG file (*.png)"
                }
            }
            if (fileChooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
                var file = fileChooser.selectedFile
                if (!file.name.endsWith("png")) {
                    file = File(file.path + ".png")
                }
                try {
                    val img = generateImage(panel)
                    ImageIO.write(img, "png", file)
                } catch (e: IOException) {
                    e.printStackTrace()
                    JOptionPane.showMessageDialog(
                        panel,
                        e.message,
                        "Cannot save image",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            }
        }

        private fun copyImageToClipboard(panel: JPanel) {
            val img = generateImage(panel)
            val transferableImage: Transferable = object : Transferable {
                override fun getTransferDataFlavors(): Array<DataFlavor> {
                    return arrayOf(DataFlavor.imageFlavor)
                }

                override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
                    return DataFlavor.imageFlavor.equals(flavor)
                }

                override fun getTransferData(flavor: DataFlavor): Any {
                    return if (DataFlavor.imageFlavor.equals(flavor)) {
                        img
                    } else {
                        throw UnsupportedFlavorException(flavor)
                    }
                }
            }
            val owner = ClipboardOwner { _: Clipboard?, _: Transferable? -> }
            val c = Toolkit.getDefaultToolkit().systemClipboard
            c.setContents(transferableImage, owner)
        }

        private fun generateImage(component: JPanel): BufferedImage {
            val img = BufferedImage(component.width, component.height, BufferedImage.TYPE_INT_ARGB)
            component.print(img.graphics)
            return img
        }
    }

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        extendedState = MAXIMIZED_BOTH
        setTitle(title)
        isVisible = true
        contentPane.layout = GenericWindowLayout()
        contentPane.background = Color.BLACK
        val p: JPanel
        if (panel is GraphicsFrame) {
            p = JPanel()
            p.background = Color.WHITE
            p.layout = GridLayout(1, 1)
            p.border = EmptyBorder(5, 5, 5, 5)
            p.add(panel)
        } else {
            p = panel
        }
        contentPane.add(p, "main")
        val menuBar = JMenuBar()
        jMenuBar = menuBar
        val imageMenu = JMenu("Image")
        menuBar.add(imageMenu)
        val copyItem = JMenuItem("Copy to Clipboard")
        copyItem.addActionListener { copyImageToClipboard(p) }
        imageMenu.add(copyItem)
        val fileItem = JMenuItem("Save to File...")
        fileItem.addActionListener { saveImageToFile(p) }
        imageMenu.add(fileItem)
        val tweetItem = JMenuItem("Tweet...")
        tweetItem.addActionListener {
            val tweetFrame = TweetFrame(p)
            tweetFrame.isVisible = true
        }
        imageMenu.add(tweetItem)
        requestFocus()
    }

    private class GenericWindowLayout : LayoutManager {
        private var main: Component? = null
        private var controlPanel: Component? = null

        override fun addLayoutComponent(name: String, comp: Component) {
            when (name) {
                "main" -> main = comp
                "control-panel" -> controlPanel = comp
                else -> throw IllegalArgumentException("Invalid name $name")
            }
        }

        override fun removeLayoutComponent(comp: Component) {
            if (comp == main) main = null
            if (comp == controlPanel) controlPanel = null
        }

        override fun preferredLayoutSize(parent: Container): Dimension {
            val main = this.main?.preferredSize
            val controlPanel = this.controlPanel?.preferredSize
            return Dimension(
                (main?.width ?: 0) + (controlPanel?.width ?: 0),
                max(main?.height ?: 0, controlPanel?.height ?: 0)
            )
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            val main = this.main?.minimumSize
            val controlPanel = this.controlPanel?.minimumSize
            return Dimension(
                (main?.width ?: 0) + (controlPanel?.width ?: 0),
                max(main?.height ?: 0, controlPanel?.height ?: 0)
            )
        }

        override fun layoutContainer(parent: Container) {
            val main = this.main
            val controlPanel = this.controlPanel
            val mainSize = main?.preferredSize ?: Dimension(0, 0)
            val controlPanelSize = controlPanel?.preferredSize ?: Dimension(0, 0)
            if (main != null) {
                main.location = Point(0, 0)
                main.size = mainSize
            }
            if (controlPanel != null) {
                controlPanel.location = Point(mainSize.width, 0)
                controlPanel.size = Dimension(controlPanelSize.width.coerceAtMost(parent.width - mainSize.width), controlPanelSize.height)
            }
        }
    }
}
