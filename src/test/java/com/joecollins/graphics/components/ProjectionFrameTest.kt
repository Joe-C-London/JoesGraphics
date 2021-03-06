package com.joecollins.graphics.components

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.components.lowerthird.LowerThird.Companion.createImage
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.awt.Image
import java.io.IOException
import kotlin.Throws
import org.junit.Assert
import org.junit.Test

class ProjectionFrameTest {
    @Test
    @Throws(IOException::class)
    fun testImage() {
        val frame = ProjectionFrame()
        val image = peiLeg()
        frame.setImageBinding(fixedBinding(image))
        Assert.assertEquals(image, frame.getImage())
    }

    @Test
    fun testBackColor() {
        val frame = ProjectionFrame()
        frame.setBackColorBinding(fixedBinding(Color.GRAY))
        Assert.assertEquals(Color.GRAY, frame.getBackColor())
    }

    @Test
    fun testFooterText() {
        val frame = ProjectionFrame()
        frame.setFooterTextBinding(fixedBinding("MINORITY LEGISLATURE"))
        Assert.assertEquals("MINORITY LEGISLATURE", frame.getFooterText())
    }

    @Test
    fun testAlignment() {
        val frame = ProjectionFrame()
        Assert.assertEquals(ProjectionFrame.Alignment.BOTTOM, frame.getImageAlignment())
        frame.setImageAlignmentBinding(fixedBinding(ProjectionFrame.Alignment.MIDDLE))
        Assert.assertEquals(ProjectionFrame.Alignment.MIDDLE, frame.getImageAlignment())
    }

    @Test
    @Throws(IOException::class)
    fun testRendering() {
        val frame = ProjectionFrame()
        frame.setImageBinding(fixedBinding(peiLeg()))
        frame.setBackColorBinding(fixedBinding(Color.GRAY))
        frame.setBorderColorBinding(fixedBinding(Color.GRAY))
        frame.setFooterTextBinding(fixedBinding("MINORITY LEGISLATURE"))
        frame.setHeaderBinding(fixedBinding<String?>("PROJECTION"))
        frame.setSize(1024, 512)
        compareRendering("ProjectionFrame", "Basic", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testLongRendering() {
        val frame = ProjectionFrame()
        frame.setImageBinding(fixedBinding(peiLeg()))
        frame.setBackColorBinding(fixedBinding(Color.GRAY))
        frame.setBorderColorBinding(fixedBinding(Color.GRAY))
        frame.setFooterTextBinding(fixedBinding("WE ARE NOW PROJECTING A MINORITY LEGISLATURE"))
        frame.setHeaderBinding(fixedBinding<String?>("PROJECTION"))
        frame.setSize(1024, 512)
        compareRendering("ProjectionFrame", "Long", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testCenterRendering() {
        val frame = ProjectionFrame()
        frame.setImageBinding(fixedBinding(peiLeg()))
        frame.setBackColorBinding(fixedBinding(Color.GRAY))
        frame.setBorderColorBinding(fixedBinding(Color.GRAY))
        frame.setImageAlignmentBinding(fixedBinding(ProjectionFrame.Alignment.MIDDLE))
        frame.setFooterTextBinding(fixedBinding("MINORITY LEGISLATURE"))
        frame.setHeaderBinding(fixedBinding<String?>("PROJECTION"))
        frame.setSize(1024, 512)
        compareRendering("ProjectionFrame", "Center", frame)
    }

    @Throws(IOException::class)
    private fun peiLeg(): Image {
        return createImage(
                ProjectionFrameTest::class.java
                        .classLoader
                        .getResource("com/joecollins/graphics/pei-leg.png"))
    }
}
