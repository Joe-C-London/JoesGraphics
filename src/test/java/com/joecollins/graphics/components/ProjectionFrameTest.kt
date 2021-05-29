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
        val image = peiLeg()
        val frame = ProjectionFrame(
            headerBinding = fixedBinding(null),
            borderColorBinding = fixedBinding(Color.GRAY),
            imageBinding = fixedBinding(image),
            backColorBinding = fixedBinding(Color.GRAY),
            footerTextBinding = fixedBinding("MINORITY LEGISLATURE")
        )
        Assert.assertEquals(image, frame.getImage())
    }

    @Test
    fun testBackColor() {
        val frame = ProjectionFrame(
            headerBinding = fixedBinding(null),
            borderColorBinding = fixedBinding(Color.GRAY),
            imageBinding = fixedBinding(peiLeg()),
            backColorBinding = fixedBinding(Color.GRAY),
            footerTextBinding = fixedBinding("MINORITY LEGISLATURE")
        )
        Assert.assertEquals(Color.GRAY, frame.getBackColor())
    }

    @Test
    fun testFooterText() {
        val frame = ProjectionFrame(
            headerBinding = fixedBinding(null),
            borderColorBinding = fixedBinding(Color.GRAY),
            imageBinding = fixedBinding(peiLeg()),
            backColorBinding = fixedBinding(Color.GRAY),
            footerTextBinding = fixedBinding("MINORITY LEGISLATURE")
        )
        Assert.assertEquals("MINORITY LEGISLATURE", frame.getFooterText())
    }

    @Test
    fun testAlignment() {
        val frame = ProjectionFrame(
            headerBinding = fixedBinding(null),
            borderColorBinding = fixedBinding(Color.GRAY),
            imageBinding = fixedBinding(peiLeg()),
            backColorBinding = fixedBinding(Color.GRAY),
            footerTextBinding = fixedBinding("MINORITY LEGISLATURE"),
            imageAlignmentBinding = fixedBinding(ProjectionFrame.Alignment.MIDDLE)
        )
        Assert.assertEquals(ProjectionFrame.Alignment.MIDDLE, frame.getImageAlignment())
    }

    @Test
    fun testDefaultAlignment() {
        val frame = ProjectionFrame(
            headerBinding = fixedBinding(null),
            borderColorBinding = fixedBinding(Color.GRAY),
            imageBinding = fixedBinding(peiLeg()),
            backColorBinding = fixedBinding(Color.GRAY),
            footerTextBinding = fixedBinding("MINORITY LEGISLATURE")
        )
        Assert.assertEquals(ProjectionFrame.Alignment.BOTTOM, frame.getImageAlignment())
    }

    @Test
    @Throws(IOException::class)
    fun testRendering() {
        val frame = ProjectionFrame(
            headerBinding = fixedBinding<String?>("PROJECTION"),
            borderColorBinding = fixedBinding(Color.GRAY),
            imageBinding = fixedBinding(peiLeg()),
            backColorBinding = fixedBinding(Color.GRAY),
            footerTextBinding = fixedBinding("MINORITY LEGISLATURE")
        )
        frame.setSize(1024, 512)
        compareRendering("ProjectionFrame", "Basic", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testLongRendering() {
        val frame = ProjectionFrame(
            headerBinding = fixedBinding<String?>("PROJECTION"),
            borderColorBinding = fixedBinding(Color.GRAY),
            imageBinding = fixedBinding(peiLeg()),
            backColorBinding = fixedBinding(Color.GRAY),
            footerTextBinding = fixedBinding("WE ARE NOW PROJECTING A MINORITY LEGISLATURE")
        )
        frame.setSize(1024, 512)
        compareRendering("ProjectionFrame", "Long", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testCenterRendering() {
        val frame = ProjectionFrame(
            headerBinding = fixedBinding<String?>("PROJECTION"),
            borderColorBinding = fixedBinding(Color.GRAY),
            imageBinding = fixedBinding(peiLeg()),
            backColorBinding = fixedBinding(Color.GRAY),
            imageAlignmentBinding = fixedBinding(ProjectionFrame.Alignment.MIDDLE),
            footerTextBinding = fixedBinding("MINORITY LEGISLATURE")
        )
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
