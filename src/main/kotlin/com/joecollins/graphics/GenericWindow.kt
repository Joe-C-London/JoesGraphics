package com.joecollins.graphics

import com.joecollins.graphics.components.GraphicsFrame
import com.joecollins.graphics.dialogs.MastodonDialog
import com.joecollins.graphics.dialogs.TweetDialog
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
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.filechooser.FileFilter
import kotlin.math.max

class GenericWindow<T : JPanel> @JvmOverloads constructor(private val panel: T, title: String? = panel.javaClass.simpleName) : JFrame() {

    fun withControlPanel(panel: JPanel): GenericWindow<T> {
        contentPane.add(panel, "control-panel")
        return this
    }

    companion object {
        const val ALT_TEXT_MAX_LENGTH = 1000

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
//        val tweetItem = JMenuItem("Tweet...")
//        tweetItem.addActionListener {
//            TweetDialog(p).isVisible = true
//        }
//        imageMenu.add(tweetItem)
        val mastodonItem = JMenuItem("Mastodon...")
        mastodonItem.addActionListener {
            MastodonDialog(p).isVisible = true
        }
        imageMenu.add(mastodonItem)
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
