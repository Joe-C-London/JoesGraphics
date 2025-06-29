package com.joecollins.graphics.utils

import org.junit.jupiter.api.Assertions
import java.awt.Component
import java.awt.Container
import java.awt.EventQueue
import java.awt.image.BufferedImage
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.util.LinkedList
import java.util.Queue
import javax.imageio.ImageIO
import javax.swing.JPanel
import kotlin.math.abs

object RenderTestUtils {

    fun compareRendering(testClass: String, testMethod: String, panel: JPanel) {
        val expectedFile = File(
            "src/test/resources/com/joecollins/graphics/$testClass/$testMethod.png",
        )
        val actualFile = File.createTempFile("test", ".png")
        val isMatch = run {
            ImageIO.write(convertToImage(panel), "png", actualFile)
            compareImage(expectedFile, actualFile) < 0.001
        }
        if (!isMatch) {
            println(expectedFile.absolutePath)
            println(actualFile.absolutePath)
            println(
                String.format(
                    if (System.getProperty("os.name").startsWith("Windows")) "copy /Y %s %s" else "cp %s %s",
                    actualFile.absolutePath,
                    expectedFile.absolutePath,
                ),
            )
        }
        Assertions.assertTrue(isMatch)
        actualFile.deleteOnExit()
    }

    private fun compareImage(fileA: File, fileB: File): Float {
        if (!fileA.exists() || !fileB.exists()) return Float.MAX_VALUE

        val imageA = ImageIO.read(fileA)
        val imageB = ImageIO.read(fileB)
        if (imageA.width != imageB.width) return Float.MAX_VALUE
        if (imageA.height != imageB.height) return Float.MAX_VALUE
        var diff = 0
        for (x in 0 until imageA.width) {
            for (y in 0 until imageA.height) {
                diff += abs(((imageA.getRGB(x, y) and 0x00ff0000) shr 16) - ((imageB.getRGB(x, y) and 0x00ff0000) shr 16))
                diff += abs(((imageA.getRGB(x, y) and 0x0000ff00) shr 8) - ((imageB.getRGB(x, y) and 0x0000ff00) shr 8))
                diff += abs(((imageA.getRGB(x, y) and 0x000000ff) shr 0) - ((imageB.getRGB(x, y) and 0x000000ff) shr 0))
            }
        }
        return diff.toFloat() / (imageA.width * imageA.height * 3)
    }

    private fun convertToImage(component: JPanel): BufferedImage {
        repeat(getChildDepth(component)) {
            val components: Queue<Component> = LinkedList()
            components.offer(component)
            while (!components.isEmpty()) {
                val c = components.poll()
                c.doLayout()
                if (c is Container) {
                    components.addAll(listOf(*c.components))
                }
            }
            EventQueue.invokeAndWait { }
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

    private fun getChildDepth(component: Component): Int = if (component is Container && component.components.isNotEmpty()) {
        component.components.maxOf { getChildDepth(it) } + 1
    } else {
        1
    }
}
