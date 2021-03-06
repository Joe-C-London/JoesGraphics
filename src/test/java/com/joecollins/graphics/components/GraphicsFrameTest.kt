package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.Binding.Companion.propertyBinding
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.BorderLayout
import java.awt.Color
import java.io.IOException
import javax.swing.JPanel
import kotlin.Throws
import org.junit.Assert
import org.junit.Test

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
        val graphicsFrame = GraphicsFrame()
        graphicsFrame.setHeaderBinding(fixedBinding("HEADER"))
        Assert.assertEquals("HEADER", graphicsFrame.header)
    }

    @Test
    fun testDynamicHeader() {
        val graphicsFrame = GraphicsFrame()
        graphicsFrame.setHeaderBinding(propertyBinding(TestObject(), { it.numPolls.toString() + " POLLS REPORTING" }, TestObject.Properties.NUM_POLLS))
        Assert.assertEquals("0 POLLS REPORTING", graphicsFrame.header)
    }

    @Test
    fun testDynamicHeaderRefreshed() {
        val graphicsFrame = GraphicsFrame()
        val obj = TestObject()
        graphicsFrame.setHeaderBinding(propertyBinding(obj, { it.numPolls.toString() + " POLLS REPORTING" }, TestObject.Properties.NUM_POLLS))
        Assert.assertEquals("0 POLLS REPORTING", graphicsFrame.header)
        obj.numPolls = 1
        Assert.assertEquals("1 POLLS REPORTING", graphicsFrame.header)
    }

    @Test
    fun testUnbind() {
        val graphicsFrame = GraphicsFrame()
        val obj = TestObject()
        graphicsFrame.setHeaderBinding(propertyBinding(obj, { it.numPolls.toString() + " POLLS REPORTING" }, TestObject.Properties.NUM_POLLS))
        Assert.assertEquals("0 POLLS REPORTING", graphicsFrame.header)
        graphicsFrame.setHeaderBinding(fixedBinding("FIXED BINDING"))
        Assert.assertEquals("FIXED BINDING", graphicsFrame.header)
        obj.numPolls = 1
        Assert.assertEquals("FIXED BINDING", graphicsFrame.header)
    }

    @Test
    fun testFixedNotes() {
        val graphicsFrame = GraphicsFrame()
        graphicsFrame.setNotesBinding(fixedBinding("SOURCE: BBC"))
        Assert.assertEquals("SOURCE: BBC", graphicsFrame.notes)
    }

    @Test
    fun testBorderColor() {
        val graphicsFrame = GraphicsFrame()
        graphicsFrame.setBorderColorBinding(fixedBinding(Color.BLUE))
        Assert.assertEquals(Color.BLUE, graphicsFrame.borderColor)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderingHeaderOnly() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame() {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setHeaderBinding(fixedBinding("HEADER"))
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "HeaderOnly", graphicsFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderingHeaderAndNotes() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame() {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setHeaderBinding(fixedBinding("HEADER"))
        graphicsFrame.setNotesBinding(fixedBinding("SOURCE: BBC"))
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "HeaderAndNotes", graphicsFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderingNoHeader() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame() {
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
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame() {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setHeaderBinding(fixedBinding("HEADER"))
        graphicsFrame.setNotesBinding(fixedBinding("SOURCE: BBC"))
        graphicsFrame.setBorderColorBinding(fixedBinding(Color.RED))
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "BorderColor", graphicsFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderingAccents() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame() {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setHeaderBinding(fixedBinding("\u00c7A C'EST GR\u00c2VE"))
        graphicsFrame.setNotesBinding(fixedBinding("JOYEUX NO\u00cbL, GAR\u00c7ON!"))
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "Accents", graphicsFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testHeaderFontSize() {
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame() {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        val headerWrapper = BindableWrapper("THIS IS A HEADER")
        graphicsFrame.setHeaderBinding(headerWrapper.binding)
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
        val graphicsFrame: GraphicsFrame = object : GraphicsFrame() {
            init {
                val panel = JPanel()
                panel.background = Color.YELLOW
                add(panel, BorderLayout.CENTER)
            }
        }
        graphicsFrame.setHeaderBinding(fixedBinding("HEADER"))
        graphicsFrame.setHeaderAlignmentBinding(alignment.binding)
        graphicsFrame.setSize(256, 128)
        compareRendering("GraphicsFrame", "HeaderAlignment-1", graphicsFrame)
        alignment.value = GraphicsFrame.Alignment.LEFT
        compareRendering("GraphicsFrame", "HeaderAlignment-2", graphicsFrame)
        alignment.value = GraphicsFrame.Alignment.RIGHT
        compareRendering("GraphicsFrame", "HeaderAlignment-3", graphicsFrame)
    }
}
