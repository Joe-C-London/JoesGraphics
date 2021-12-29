package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.Binding.Companion.propertyBinding
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Test
import java.awt.BorderLayout
import java.awt.Color
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.swing.JPanel
import kotlin.Throws

class GraphicsFrameTest {
    private class TestObject : Bindable<TestObject, TestObject.Properties>() {
        enum class Properties {
            NUM_POLLS
        }

        private var _numPolls = 0

        var numPolls: Int
            get() = _numPolls
            set(numPolls) {
                this._numPolls = numPolls
                onPropertyRefreshed(Properties.NUM_POLLS)
            }
    }

    @Test
    fun testFixedHeader() {
        val graphicsFrame = GraphicsFrame(
            headerPublisher = fixedBinding("HEADER").toPublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ graphicsFrame.header }, IsEqual("HEADER"))
    }

    @Test
    fun testDynamicHeader() {
        val graphicsFrame = GraphicsFrame(
            headerPublisher = propertyBinding(TestObject(), { it.numPolls.toString() + " POLLS REPORTING" }, TestObject.Properties.NUM_POLLS).toPublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ graphicsFrame.header }, IsEqual("0 POLLS REPORTING"))
    }

    @Test
    fun testDynamicHeaderRefreshed() {
        val obj = TestObject()
        val graphicsFrame = GraphicsFrame(
            headerPublisher = propertyBinding(obj, { it.numPolls.toString() + " POLLS REPORTING" }, TestObject.Properties.NUM_POLLS).toPublisher()
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
            headerPublisher = fixedBinding(null).toPublisher(),
            notesPublisher = fixedBinding("SOURCE: BBC").toPublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ graphicsFrame.notes }, IsEqual("SOURCE: BBC"))
    }

    @Test
    fun testBorderColor() {
        val graphicsFrame = GraphicsFrame(
            headerPublisher = fixedBinding(null).toPublisher(),
            borderColorPublisher = fixedBinding(Color.BLUE).toPublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ graphicsFrame.borderColor }, IsEqual(Color.BLUE))
    }

    @Test
    @Throws(IOException::class)
    fun testRenderingHeaderOnly() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = fixedBinding("HEADER").toPublisher()
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
    @Throws(IOException::class)
    fun testRenderingHeaderAndNotes() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = fixedBinding("HEADER").toPublisher(),
            notesPublisher = fixedBinding("SOURCE: BBC").toPublisher()
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
    @Throws(IOException::class)
    fun testRenderingNoHeader() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = fixedBinding(null).toPublisher()
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
    @Throws(IOException::class)
    fun testRenderingBorderColor() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = fixedBinding("HEADER").toPublisher(),
            notesPublisher = fixedBinding("SOURCE: BBC").toPublisher(),
            borderColorPublisher = fixedBinding(Color.RED).toPublisher()
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
    @Throws(IOException::class)
    fun testRenderingAccents() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = fixedBinding("\u00c7A C'EST GR\u00c2VE").toPublisher(),
            notesPublisher = fixedBinding("JOYEUX NO\u00cbL, GAR\u00c7ON!").toPublisher()
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
    @Throws(IOException::class)
    fun testHeaderFontSize() {
        val headerWrapper = BindableWrapper("THIS IS A HEADER")
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = headerWrapper.binding.toPublisher()
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "HeaderFontSize-1", graphicsFrame)
        headerWrapper.value = "THIS IS A VERY MUCH LONGER HEADER"
        compareRendering("GraphicsFrame", "HeaderFontSize-2", graphicsFrame)
        graphicsFrame.setSize(512, 128)
        compareRendering("GraphicsFrame", "HeaderFontSize-3", graphicsFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderHeaderAlignment() {
        val alignment = BindableWrapper(GraphicsFrame.Alignment.CENTER)
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame(
            headerPublisher = fixedBinding("HEADER").toPublisher(),
            headerAlignmentPublisher = alignment.binding.toPublisher()
        ) {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "HeaderAlignment-1", graphicsFrame)
        alignment.value = GraphicsFrame.Alignment.LEFT
        compareRendering("GraphicsFrame", "HeaderAlignment-2", graphicsFrame)
        alignment.value = GraphicsFrame.Alignment.RIGHT
        compareRendering("GraphicsFrame", "HeaderAlignment-3", graphicsFrame)
    }
}
