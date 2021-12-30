package com.joecollins.graphics.components

import com.joecollins.graphics.components.lowerthird.LowerThird.Companion.createImage
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.awt.Image
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.Throws

class ProjectionFrameTest {
    @Test
    @Throws(IOException::class)
    fun testImage() {
        val image = peiLeg()
        val frame = ProjectionFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = image.asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getImage() }, IsEqual(image))
    }

    @Test
    fun testBackColor() {
        val frame = ProjectionFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getBackColor() }, IsEqual(Color.GRAY))
    }

    @Test
    fun testFooterText() {
        val frame = ProjectionFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getFooterText() }, IsEqual("MINORITY LEGISLATURE"))
    }

    @Test
    fun testAlignment() {
        val frame = ProjectionFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher(),
            imageAlignmentPublisher = ProjectionFrame.Alignment.MIDDLE.asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getImageAlignment() }, IsEqual(ProjectionFrame.Alignment.MIDDLE))
    }

    @Test
    fun testDefaultAlignment() {
        val frame = ProjectionFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher()
        )
        Assert.assertEquals(ProjectionFrame.Alignment.BOTTOM, frame.getImageAlignment())
    }

    @Test
    @Throws(IOException::class)
    fun testRendering() {
        val frame = ProjectionFrame(
            headerPublisher = "PROJECTION".asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher()
        )
        frame.setSize(1024, 512)
        compareRendering("ProjectionFrame", "Basic", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testLongRendering() {
        val frame = ProjectionFrame(
            headerPublisher = "PROJECTION".asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            footerTextPublisher = "WE ARE NOW PROJECTING A MINORITY LEGISLATURE".asOneTimePublisher()
        )
        frame.setSize(1024, 512)
        compareRendering("ProjectionFrame", "Long", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testCenterRendering() {
        val frame = ProjectionFrame(
            headerPublisher = "PROJECTION".asOneTimePublisher(),
            borderColorPublisher = Color.GRAY.asOneTimePublisher(),
            imagePublisher = peiLeg().asOneTimePublisher(),
            backColorPublisher = Color.GRAY.asOneTimePublisher(),
            imageAlignmentPublisher = ProjectionFrame.Alignment.MIDDLE.asOneTimePublisher(),
            footerTextPublisher = "MINORITY LEGISLATURE".asOneTimePublisher()
        )
        frame.setSize(1024, 512)
        compareRendering("ProjectionFrame", "Center", frame)
    }

    @Throws(IOException::class)
    private fun peiLeg(): Image {
        return createImage(
            ProjectionFrameTest::class.java
                .classLoader
                .getResource("com/joecollins/graphics/pei-leg.png")
        )
    }
}
