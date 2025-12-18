package com.joecollins.graphics

import com.joecollins.graphics.dialogs.BlueSkyDialog
import com.joecollins.graphics.dialogs.MastodonDialog
import com.joecollins.graphics.dialogs.TweetDialog
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.social.twitter.TwitterV2InstanceFactory
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.map
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import java.awt.Point
import java.awt.Taskbar
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.ClipboardOwner
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.Properties
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.filechooser.FileFilter
import kotlin.math.max
import kotlin.math.roundToInt

class GenericWindow<T : JPanel> constructor(private val panel: T, title: String) : JFrame() {

    fun withControlPanel(panel: JPanel): GenericWindow<T> {
        contentPane.add(panel, "control-panel")
        return this
    }

    companion object {

        init {
            System.setProperty("apple.laf.useScreenMenuBar", "true")
        }

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

                override fun getDescription(): String = "PNG file (*.png)"
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
                        JOptionPane.ERROR_MESSAGE,
                    )
                }
            }
        }

        private fun copyImageToClipboard(panel: JPanel) {
            val img = generateImage(panel)
            val transferableImage: Transferable = object : Transferable {
                override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)

                override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = DataFlavor.imageFlavor.equals(flavor)

                override fun getTransferData(flavor: DataFlavor): Any = if (DataFlavor.imageFlavor.equals(flavor)) {
                    img
                } else {
                    throw UnsupportedFlavorException(flavor)
                }
            }
            val owner = ClipboardOwner { _: Clipboard?, _: Transferable? -> }
            val c = Toolkit.getDefaultToolkit().systemClipboard
            c.setContents(transferableImage, owner)
        }

        private fun copyAltTextToClipboard(html: String) {
            val transferableImage: Transferable = object : Transferable {
                override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.allHtmlFlavor, DataFlavor.stringFlavor)

                override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = transferDataFlavors.contains(flavor)

                override fun getTransferData(flavor: DataFlavor): Any = when (flavor) {
                    DataFlavor.allHtmlFlavor -> {
                        html
                    }
                    DataFlavor.stringFlavor -> {
                        html.replace("<html>", "")
                            .replace("</html>", "")
                            .replace("<br/>", "\n")
                    }
                    else -> {
                        throw UnsupportedFlavorException(flavor)
                    }
                }
            }
            val owner = ClipboardOwner { _: Clipboard?, _: Transferable? -> }
            val c = Toolkit.getDefaultToolkit().systemClipboard
            c.setContents(transferableImage, owner)
        }

        internal fun generateImage(component: JPanel): BufferedImage {
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
        contentPane.add(panel, "main")
        val altText = JLabel()
        altText.foreground = Color.WHITE
        altText.font = StandardFont.readNormalFont(8)
        if (panel is AltTextProvider) {
            panel.altText.map { it(Int.MAX_VALUE) }.subscribe(
                Subscriber(
                    eventQueueWrapper {
                        altText.text =
                            if (it == null) {
                                ""
                            } else {
                                "<html>${it.replace("\n", "<br/>")}</html>"
                            }
                        altText.foreground =
                            if (it == null || it.length <= 1000) {
                                Color.WHITE
                            } else if (it.length <= 2000) {
                                Color.YELLOW
                            } else {
                                Color.RED
                            }
                    },
                ),
            )
        }
        if (panel is TaskbarProvider) {
            panel.taskbarIcon?.subscribe(
                Subscriber(
                    eventQueueWrapper {
                        Taskbar.getTaskbar().iconImage = it
                    },
                ),
            )
            panel.progress?.subscribe(
                Subscriber(
                    eventQueueWrapper {
                        Taskbar.getTaskbar().setProgressValue((it * 100).roundToInt())
                    },
                ),
            )
        }
        contentPane.add(altText, "alt-text")
        val menuBar = JMenuBar()
        jMenuBar = menuBar
        val imageMenu = JMenu("Image")
        menuBar.add(imageMenu)
        val copyItem = JMenuItem("Copy Image to Clipboard")
        copyItem.addActionListener { copyImageToClipboard(panel) }
        imageMenu.add(copyItem)
        val copyText = JMenuItem("Copy AltText to Clipboard")
        copyText.addActionListener { copyAltTextToClipboard(altText.text) }
        imageMenu.add(copyText)
        val fileItem = JMenuItem("Save to File...")
        fileItem.addActionListener { saveImageToFile(panel) }
        imageMenu.add(fileItem)
        if (logIntoTwitterOnStartup) {
            CompletableFuture.runAsync {
                try {
                    TwitterV2InstanceFactory.instance
                } catch (e: Exception) {
                    println("Cannot log into Twitter: ${e.message}")
                }
            }
        }
        if (isTweetingEnabled) {
            val tweetItem = JMenuItem("Tweet...")
            tweetItem.addActionListener {
                TweetDialog(panel).isVisible = true
            }
            imageMenu.add(tweetItem)
        }
        mastodonInstances.forEach { (server, token) ->
            val mastodonItem = JMenuItem("Mastodon ($server)...")
            mastodonItem.addActionListener {
                MastodonDialog(panel, server, token).isVisible = true
            }
            imageMenu.add(mastodonItem)
        }
        if (isBlueSkyEnabled) {
            val tweetItem = JMenuItem("BlueSky...")
            tweetItem.addActionListener {
                BlueSkyDialog(panel).isVisible = true
            }
            imageMenu.add(tweetItem)
        }
        requestFocus()
    }

    private val isTweetingEnabled: Boolean
        get() {
            val twitterPropertiesFile = this::class.java.classLoader.getResourceAsStream("twitter.properties") ?: return false
            val properties = Properties()
            properties.load(twitterPropertiesFile)
            return properties.getProperty("enabled")?.toBoolean() ?: true
        }

    private val isBlueSkyEnabled: Boolean
        get() {
            val blueskyPropertiesFile = this::class.java.classLoader.getResourceAsStream("bluesky.properties") ?: return false
            val properties = Properties()
            properties.load(blueskyPropertiesFile)
            return properties.getProperty("enabled")?.toBoolean() ?: true
        }

    private val logIntoTwitterOnStartup: Boolean
        get() {
            val twitterPropertiesFile = this::class.java.classLoader.getResourceAsStream("twitter.properties") ?: return false
            val properties = Properties()
            properties.load(twitterPropertiesFile)
            return properties.getProperty("loginOnStartup")?.toBoolean() ?: false
        }

    private val mastodonInstances: List<Pair<String, String>>
        get() {
            val mastodonPropertiesFile = this.javaClass.classLoader.getResourceAsStream("mastodon.properties") ?: return emptyList()
            val properties = Properties()
            properties.load(mastodonPropertiesFile)

            val instances = ArrayList<Pair<String, String>>()
            generateSequence(1) { it + 1 }.forEach { idx ->
                val prefix = "instance${if (idx == 1) "" else "$idx"}"
                val server = properties["$prefix.server"]?.toString() ?: return instances
                val token = properties["$prefix.token"]?.toString() ?: return instances
                val enabled = properties["$prefix.enabled"]?.toString()?.toBoolean() ?: true
                if (enabled) instances.add(server to token)
            }
            return instances
        }

    private class GenericWindowLayout : LayoutManager {
        private var main: Component? = null
        private var controlPanel: Component? = null
        private var altText: Component? = null

        override fun addLayoutComponent(name: String, comp: Component) {
            when (name) {
                "main" -> main = comp
                "control-panel" -> controlPanel = comp
                "alt-text" -> altText = comp
                else -> throw IllegalArgumentException("Invalid name $name")
            }
        }

        override fun removeLayoutComponent(comp: Component) {
            if (comp == main) main = null
            if (comp == controlPanel) controlPanel = null
            if (comp == altText) altText = null
        }

        override fun preferredLayoutSize(parent: Container): Dimension {
            val main = this.main?.preferredSize
            val controlPanel = this.controlPanel?.preferredSize
            return Dimension(
                (main?.width ?: 0) + (controlPanel?.width ?: 0),
                max(main?.height ?: 0, controlPanel?.height ?: 0),
            )
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            val main = this.main?.minimumSize
            val controlPanel = this.controlPanel?.minimumSize
            return Dimension(
                (main?.width ?: 0) + (controlPanel?.width ?: 0),
                max(main?.height ?: 0, controlPanel?.height ?: 0),
            )
        }

        override fun layoutContainer(parent: Container) {
            val main = this.main
            val controlPanel = this.controlPanel
            val altText = this.altText
            val mainSize = main?.preferredSize ?: Dimension(0, 0)
            val controlPanelSize = controlPanel?.preferredSize ?: Dimension(0, 0)
            val altTextSize = altText?.preferredSize ?: Dimension(0, 0)
            if (main != null) {
                main.location = Point(0, 0)
                main.size = mainSize
            }
            if (controlPanel != null) {
                controlPanel.location = Point(mainSize.width, 0)
                controlPanel.size = Dimension(
                    controlPanelSize.width.coerceAtMost(parent.width - mainSize.width),
                    controlPanelSize.height,
                )
            }
            if (altText != null) {
                altText.location = Point(0, mainSize.height)
                altText.size = Dimension(
                    altTextSize.width.coerceAtMost(mainSize.width),
                    altTextSize.height.coerceAtMost(parent.height - mainSize.height),
                )
            }
        }
    }
}
