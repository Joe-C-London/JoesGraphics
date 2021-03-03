package com.joecollins.graphics.utils

import java.awt.Component
import java.awt.Container
import java.awt.EventQueue
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.lang.AssertionError
import java.lang.InterruptedException
import java.lang.reflect.InvocationTargetException
import java.util.LinkedList
import java.util.Queue
import javax.imageio.ImageIO
import javax.swing.JPanel
import kotlin.Throws
import org.apache.commons.io.FileUtils
import org.junit.Assert

object RenderTestUtils {

    @Throws(IOException::class)
    @JvmStatic fun compareRendering(testClass: String, testMethod: String, panel: JPanel) {
        val expectedFile = File(
                "src\\test\\resources\\com\\joecollins\\graphics\\$testClass\\$testMethod.png")
        val actualFile = File.createTempFile("test", ".png")
        ImageIO.write(convertToImage(panel), "png", actualFile)
        val isMatch = FileUtils.contentEquals(expectedFile, actualFile)
        if (!isMatch) {
            println(expectedFile.absolutePath)
            println(actualFile.absolutePath)
            println(String.format(
                    "copy /Y %s %s", actualFile.absolutePath, expectedFile.absolutePath))
        }
        Assert.assertTrue(isMatch)
        actualFile.deleteOnExit()
    }

    private fun convertToImage(component: JPanel): BufferedImage {
        val components: Queue<Component> = LinkedList()
        components.offer(component)
        while (!components.isEmpty()) {
            val c = components.poll()
            c.doLayout()
            if (c is Container) {
                components.addAll(listOf(*c.components))
            }
        }
        val img = BufferedImage(component.width, component.height, BufferedImage.TYPE_INT_ARGB)
        try {
            EventQueue.invokeAndWait { component.print(img.graphics) }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw AssertionError(e)
        } catch (e: InvocationTargetException) {
            throw AssertionError(e)
        }
        return img
    }
}
