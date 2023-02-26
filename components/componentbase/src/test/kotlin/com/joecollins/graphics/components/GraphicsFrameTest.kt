package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.BorderLayout
import java.awt.Color
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
            headerPublisher = "HEADER".asOneTimePublisher(),
        )
        assertEquals("HEADER", graphicsFrame.header)
    }

    @Test
    fun testDynamicHeader() {
        val graphicsFrame = GraphicsFrame(
            headerPublisher = TestObject().numPollsPublisher.map { "$it POLLS REPORTING" },
        )
        assertEquals("0 POLLS REPORTING", graphicsFrame.header)
    }

    @Test
    fun testDynamicHeaderRefreshed() {
        val obj = TestObject()
        val graphicsFrame = GraphicsFrame(
            headerPublisher = obj.numPollsPublisher.map { "$it POLLS REPORTING" },
        )
        assertEquals("0 POLLS REPORTING", graphicsFrame.header)

        obj.numPolls = 1
        assertEquals("1 POLLS REPORTING", graphicsFrame.header)
    }

    @Test
    fun testFixedNotes() {
        val graphicsFrame = GraphicsFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            notesPublisher = "SOURCE: BBC".asOneTimePublisher(),
        )
        assertEquals("SOURCE: BBC", graphicsFrame.notes)
    }

    @Test
    fun testBorderColor() {
        val graphicsFrame = GraphicsFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            borderColorPublisher = Color.BLUE.asOneTimePublisher(),
        )
        assertEquals(Color.BLUE, graphicsFrame.borderColor)
        assertEquals(Color.WHITE, graphicsFrame.headerTextColor)
    }

    @Test
    fun testHeaderTextColor() {
        val graphicsFrame = GraphicsFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            borderColorPublisher = Color.BLUE.asOneTimePublisher(),
            headerTextColorPublisher = Color.YELLOW.asOneTimePublisher(),
        )
        assertEquals(Color.BLUE, graphicsFrame.borderColor)
        assertEquals(Color.YELLOW, graphicsFrame.headerTextColor)
    }

    @Test
    fun testRenderingHeaderOnly() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = "HEADER".asOneTimePublisher(),
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
            notesPublisher = "SOURCE: BBC".asOneTimePublisher(),
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
            headerPublisher = (null as String?).asOneTimePublisher(),
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
            borderColorPublisher = Color.RED.asOneTimePublisher(),
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
    fun testRenderingHeaderTextColor() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = "HEADER".asOneTimePublisher(),
            notesPublisher = "SOURCE: BBC".asOneTimePublisher(),
            borderColorPublisher = Color.RED.asOneTimePublisher(),
            headerTextColorPublisher = Color.YELLOW.asOneTimePublisher(),
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering(
            "GraphicsFrame",
            "HeaderTextColor",
            graphicsFrame,
        )
    }

    @Test
    fun testRenderingAccents() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = "\u00c7A C'EST GR\u00c2VE".asOneTimePublisher(),
            notesPublisher = "JOYEUX NO\u00cbL, GAR\u00c7ON!".asOneTimePublisher(),
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
            headerPublisher = headerWrapper,
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering(
            "GraphicsFrame",
            "HeaderFontSize-1",
            graphicsFrame,
        )
        headerWrapper.submit("THIS IS A VERY MUCH LONGER HEADER")
        compareRendering(
            "GraphicsFrame",
            "HeaderFontSize-2",
            graphicsFrame,
        )
        graphicsFrame.setSize(512, 128)
        compareRendering(
            "GraphicsFrame",
            "HeaderFontSize-3",
            graphicsFrame,
        )
    }

    @Test
    fun testRenderHeaderAlignment() {
        val alignment = Publisher(GraphicsFrame.Alignment.CENTER)
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = "HEADER".asOneTimePublisher(),
            headerAlignmentPublisher = alignment,
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering(
            "GraphicsFrame",
            "HeaderAlignment-1",
            graphicsFrame,
        )
        alignment.submit(GraphicsFrame.Alignment.LEFT)
        compareRendering(
            "GraphicsFrame",
            "HeaderAlignment-2",
            graphicsFrame,
        )
        alignment.submit(GraphicsFrame.Alignment.RIGHT)
        compareRendering(
            "GraphicsFrame",
            "HeaderAlignment-3",
            graphicsFrame,
        )
    }

    @Test
    fun testRightHandLabel() {
        val alignment = Publisher(GraphicsFrame.Alignment.CENTER)
        val headerText = Publisher("HEADER")
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = headerText,
            headerAlignmentPublisher = alignment,
            headerLabelsPublisher = mapOf(HeaderLabelLocation.RIGHT to "100%").asOneTimePublisher(),
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering(
            "GraphicsFrame",
            "RightHandLabel-C",
            graphicsFrame,
        )
        alignment.submit(GraphicsFrame.Alignment.LEFT)
        compareRendering(
            "GraphicsFrame",
            "RightHandLabel-L",
            graphicsFrame,
        )
        alignment.submit(GraphicsFrame.Alignment.RIGHT)
        compareRendering(
            "GraphicsFrame",
            "RightHandLabel-R",
            graphicsFrame,
        )
        alignment.submit(GraphicsFrame.Alignment.CENTER)
        headerText.submit("THE MAIN HEADER TEXT IS LONG")
        compareRendering(
            "GraphicsFrame",
            "RightHandLabel-C2",
            graphicsFrame,
        )
        alignment.submit(GraphicsFrame.Alignment.LEFT)
        compareRendering(
            "GraphicsFrame",
            "RightHandLabel-L2",
            graphicsFrame,
        )
        alignment.submit(GraphicsFrame.Alignment.RIGHT)
        compareRendering(
            "GraphicsFrame",
            "RightHandLabel-R2",
            graphicsFrame,
        )
    }

    @Test
    fun testLeftHandLabel() {
        val alignment = Publisher(GraphicsFrame.Alignment.CENTER)
        val headerText = Publisher("HEADER")
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = headerText,
            headerAlignmentPublisher = alignment,
            headerLabelsPublisher = mapOf(HeaderLabelLocation.LEFT to "100%").asOneTimePublisher(),
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering(
            "GraphicsFrame",
            "LeftHandLabel-C",
            graphicsFrame,
        )
        alignment.submit(GraphicsFrame.Alignment.LEFT)
        compareRendering(
            "GraphicsFrame",
            "LeftHandLabel-L",
            graphicsFrame,
        )
        alignment.submit(GraphicsFrame.Alignment.RIGHT)
        compareRendering(
            "GraphicsFrame",
            "LeftHandLabel-R",
            graphicsFrame,
        )
        alignment.submit(GraphicsFrame.Alignment.CENTER)
        headerText.submit("THE MAIN HEADER TEXT IS LONG")
        compareRendering(
            "GraphicsFrame",
            "LeftHandLabel-C2",
            graphicsFrame,
        )
        alignment.submit(GraphicsFrame.Alignment.LEFT)
        compareRendering(
            "GraphicsFrame",
            "LeftHandLabel-L2",
            graphicsFrame,
        )
        alignment.submit(GraphicsFrame.Alignment.RIGHT)
        compareRendering(
            "GraphicsFrame",
            "LeftHandLabel-R2",
            graphicsFrame,
        )
    }
}
