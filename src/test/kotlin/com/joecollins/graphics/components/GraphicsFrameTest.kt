package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Test
import java.awt.BorderLayout
import java.awt.Color
import java.util.concurrent.TimeUnit
import javax.swing.JPanel

class GraphicsFrameTest {
    private class TestObject {

        var numPolls: Int = 0
            set(value) {
                field = value
                numPollsPublisher.submit(numPolls)
            }

        val numPollsPublisher = Publisher(numPolls)
    }

    @Test
    fun testFixedHeader() {
        val graphicsFrame = GraphicsFrame(
            headerPublisher = "HEADER".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ graphicsFrame.header }, IsEqual("HEADER"))
    }

    @Test
    fun testDynamicHeader() {
        val graphicsFrame = GraphicsFrame(
            headerPublisher = TestObject().numPollsPublisher.map { "$it POLLS REPORTING" }
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ graphicsFrame.header }, IsEqual("0 POLLS REPORTING"))
    }

    @Test
    fun testDynamicHeaderRefreshed() {
        val obj = TestObject()
        val graphicsFrame = GraphicsFrame(
            headerPublisher = obj.numPollsPublisher.map { "$it POLLS REPORTING" }
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ graphicsFrame.header }, IsEqual("0 POLLS REPORTING"))
        obj.numPolls = 1
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ graphicsFrame.header }, IsEqual("1 POLLS REPORTING"))
    }

    @Test
    fun testFixedNotes() {
        val graphicsFrame = GraphicsFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            notesPublisher = "SOURCE: BBC".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ graphicsFrame.notes }, IsEqual("SOURCE: BBC"))
    }

    @Test
    fun testBorderColor() {
        val graphicsFrame = GraphicsFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            borderColorPublisher = Color.BLUE.asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ graphicsFrame.borderColor }, IsEqual(Color.BLUE))
    }

    @Test
    fun testRenderingHeaderOnly() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = "HEADER".asOneTimePublisher()
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "HeaderOnly", graphicsFrame)
    }

    @Test
    fun testRenderingHeaderAndNotes() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = "HEADER".asOneTimePublisher(),
            notesPublisher = "SOURCE: BBC".asOneTimePublisher()
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "HeaderAndNotes", graphicsFrame)
    }

    @Test
    fun testRenderingNoHeader() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = (null as String?).asOneTimePublisher()
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "NoHeader", graphicsFrame)
    }

    @Test
    fun testRenderingBorderColor() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = "HEADER".asOneTimePublisher(),
            notesPublisher = "SOURCE: BBC".asOneTimePublisher(),
            borderColorPublisher = Color.RED.asOneTimePublisher()
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "BorderColor", graphicsFrame)
    }

    @Test
    fun testRenderingAccents() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = "\u00c7A C'EST GR\u00c2VE".asOneTimePublisher(),
            notesPublisher = "JOYEUX NO\u00cbL, GAR\u00c7ON!".asOneTimePublisher()
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "Accents", graphicsFrame)
    }

    @Test
    fun testHeaderFontSize() {
        val headerWrapper = Publisher("THIS IS A HEADER")
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = headerWrapper
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "HeaderFontSize-1", graphicsFrame)
        headerWrapper.submit("THIS IS A VERY MUCH LONGER HEADER")
        compareRendering("GraphicsFrame", "HeaderFontSize-2", graphicsFrame)
        graphicsFrame.setSize(512, 128)
        compareRendering("GraphicsFrame", "HeaderFontSize-3", graphicsFrame)
    }

    @Test
    fun testRenderHeaderAlignment() {
        val alignment = Publisher(GraphicsFrame.Alignment.CENTER)
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = "HEADER".asOneTimePublisher(),
            headerAlignmentPublisher = alignment
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "HeaderAlignment-1", graphicsFrame)
        alignment.submit(GraphicsFrame.Alignment.LEFT)
        compareRendering("GraphicsFrame", "HeaderAlignment-2", graphicsFrame)
        alignment.submit(GraphicsFrame.Alignment.RIGHT)
        compareRendering("GraphicsFrame", "HeaderAlignment-3", graphicsFrame)
    }
}
