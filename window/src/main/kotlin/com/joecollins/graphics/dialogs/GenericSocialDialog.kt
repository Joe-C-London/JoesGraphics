package com.joecollins.graphics.dialogs

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.GenericWindow
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.map
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

@Suppress("LeakingThis")
abstract class GenericSocialDialog(panel: JPanel) : JDialog() {

    abstract val siteColor: Color
    abstract val action: String
    abstract val actionInProgress: String
    abstract val maxLength: Int
    abstract val maxAltTextLength: Int

    init {
        size = Dimension(500, 500)
        isModal = true
        contentPane.layout = BorderLayout()

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
        mainPanel.add(
            JScrollPane(
                altTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
            ),
        )

        val bottomPanel = JPanel()
        bottomPanel.background = siteColor
        bottomPanel.layout = BorderLayout()
        contentPane.add(bottomPanel, BorderLayout.SOUTH)

        var charLabelText = {
            "${textArea.text.length}/$maxLength & ${altTextArea.text.length}/$maxAltTextLength & ${panel.size.width}x${panel.size.height}"
        }
        val charLabel =
            JLabel(charLabelText())
        charLabel.foreground = Color.WHITE
        bottomPanel.add(charLabel, BorderLayout.WEST)
        textArea.addKeyListener(
            object : KeyAdapter() {
                override fun keyReleased(e: KeyEvent) {
                    charLabel.text = charLabelText()
                }
            },
        )

        if (panel is AltTextProvider) {
            subscriber = Subscriber(
                Subscriber.eventQueueWrapper {
                    val text = it?.takeIf { t -> t.length <= maxAltTextLength }
                    altTextArea.text = text ?: ""
                    charLabel.text = charLabelText()
                },
            )
            panel.altText.map { it(maxAltTextLength) }.subscribe(subscriber)
        }

        val sendButton = JButton(action)
        sendButton.background = siteColor
        sendButton.foreground = Color.WHITE
        bottomPanel.add(sendButton, BorderLayout.EAST)
        sendButton.addActionListener {
            val file: File
            try {
                file = File.createTempFile("joes-politics", ".png")
                file.deleteOnExit()
                val img = GenericWindow.generateImage(panel)
                ImageIO.write(img, "png", file)
            } catch (exc: IOException) {
                JOptionPane.showMessageDialog(panel, "Could not save image: " + exc.message)
                exc.printStackTrace()
                return@addActionListener
            }
            try {
                sendButton.isEnabled = false
                sendButton.text = "$actionInProgress..."
                val text = textArea.text
                val altText = altTextArea.text.takeUnless { it.isNullOrEmpty() }
                send(text, file, altText)
                isVisible = false
                subscriber?.unsubscribe()
            } catch (exc: Exception) {
                JOptionPane.showMessageDialog(panel, "Could not send: " + exc.message)
                exc.printStackTrace()
                sendButton.isEnabled = true
                sendButton.text = action
            }
        }
    }

    abstract fun send(post: String, image: File, altText: String? = null)
}
