package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Image
import javax.imageio.ImageIO

class ProjectionFrameTest {
    @Test
    fun testImage() {
        val image = peiLeg()
        val frame = ProjectionFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = image.asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher(),
        )
        assertEquals(image, frame.getImage())
    }

    @Test
    fun testBackColor() {
        val frame = ProjectionFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher(),
        )
        assertEquals(Color.GRAY, frame.getBackColor())
    }

    @Test
    fun testFooterText() {
        val frame = ProjectionFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher(),
        )
        assertEquals("MINORITY LEGISLATURE", frame.getFooterText())
    }

    @Test
    fun testAlignment() {
        val frame = ProjectionFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher(),
            imageAlignmentPublisher = ProjectionFrame.Alignment.MIDDLE.asOneTimePublisher(),
        )
        assertEquals(ProjectionFrame.Alignment.MIDDLE, frame.getImageAlignment())
    }

    @Test
    fun testDefaultAlignment() {
        val frame = ProjectionFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher(),
        )
        assertEquals(ProjectionFrame.Alignment.BOTTOM, frame.getImageAlignment())
    }

    @Test
    fun testRendering() {
        val frame = ProjectionFrame(
            headerPublisher = "PROJECTION".asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher(),
        )
        frame.setSize(1024, 512)
        compareRendering("ProjectionFrame", "Basic", frame)
    }

    @Test
    fun testLongRendering() {
        val frame = ProjectionFrame(
            headerPublisher = "PROJECTION".asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "WE ARE NOW PROJECTING A MINORITY LEGISLATURE".asOneTimePublisher(),
        )
        frame.setSize(1024, 512)
        compareRendering("ProjectionFrame", "Long", frame)
    }

    @Test
    fun testCenterRendering() {
        val frame = ProjectionFrame(
            headerPublisher = "PROJECTION".asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            imageAlignmentPublisher = ProjectionFrame.Alignment.MIDDLE.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher(),
        )
        frame.setSize(1024, 512)
        compareRendering("ProjectionFrame", "Center", frame)
    }

    private fun peiLeg(): Image {
        return ImageIO.read(
            ProjectionFrameTest::class.java
                .classLoader
                .getResource("com/joecollins/graphics/pei-leg.png"),
        )
    }
}
