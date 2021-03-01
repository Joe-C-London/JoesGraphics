package com.joecollins.graphics

import com.joecollins.graphics.components.GraphicsFrame
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
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
import java.lang.Exception
import java.lang.IllegalStateException
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
import javax.swing.JTextArea
import javax.swing.border.EmptyBorder
import javax.swing.filechooser.FileFilter
import kotlin.Throws
import kotlin.jvm.JvmOverloads
import twitter4j.StatusUpdate
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

class GenericWindow<T : JPanel> @JvmOverloads constructor(private val panel: T, title: String? = panel.javaClass.simpleName) : JFrame() {

    fun withControlPanel(panel: JPanel): GenericWindow<T> {
        add(panel)
        return this
    }

    class TweetFrame(panel: JPanel) : JDialog() {
        @Throws(IOException::class, TwitterException::class)
        private fun sendTweet(tweet: String, image: File) {
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
            size = Dimension(300, 300)
            isModal = true
            title = "Tweet"
            contentPane.layout = BorderLayout()
            val twitterColor = Color(0x00acee)
            val textArea = JTextArea()
            textArea.lineWrap = true
            textArea.wrapStyleWord = true
            contentPane.add(textArea, BorderLayout.CENTER)
            val bottomPanel = JPanel()
            bottomPanel.background = twitterColor
            bottomPanel.layout = BorderLayout()
            contentPane.add(bottomPanel, BorderLayout.SOUTH)
            val charLabel = JLabel("0")
            charLabel.foreground = Color.WHITE
            bottomPanel.add(charLabel, BorderLayout.WEST)
            textArea.addKeyListener(
                    object : KeyAdapter() {
                        override fun keyReleased(e: KeyEvent) {
                            charLabel.text = textArea.text.length.toString()
                        }
                    })
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
                    sendTweet(tweet, file)
                    isVisible = false
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
                            panel, e.message, "Cannot save image", JOptionPane.ERROR_MESSAGE)
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

                @Throws(UnsupportedFlavorException::class)
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
        contentPane.layout = FlowLayout()
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
        contentPane.add(p)
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
}
