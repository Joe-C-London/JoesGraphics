package com.joecollins.graphics.utils

import com.joecollins.graphics.AltTextProvider
import com.joecollins.pubsub.Subscriber
import org.apache.commons.io.FileUtils
import org.awaitility.Awaitility
import org.awaitility.core.ConditionTimeoutException
import org.hamcrest.Matchers
import org.junit.Assert
import java.awt.Component
import java.awt.Container
import java.awt.EventQueue
import java.awt.image.BufferedImage
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.swing.JPanel

object RenderTestUtils {

    fun compareRendering(testClass: String, testMethod: String, panel: JPanel, timeoutSeconds: Long = 1) {
        val expectedFile = File(
            "src\\test\\resources\\com\\joecollins\\graphics\\$testClass\\$testMethod.png"
        )
        val actualFile = File.createTempFile("test", ".png")
        val isMatch = try {
            Awaitility.await("$testClass/$testMethod").atMost(timeoutSeconds, TimeUnit.SECONDS)
                .until({
                    ImageIO.write(convertToImage(panel), "png", actualFile)
                    FileUtils.contentEquals(expectedFile, actualFile)
                }, { it })
        } catch (e: ConditionTimeoutException) {
            false
        }
        if (!isMatch) {
            println(expectedFile.absolutePath)
            println(actualFile.absolutePath)
            println(
                String.format(
                    "copy /Y %s %s",
                    actualFile.absolutePath,
                    expectedFile.absolutePath
                )
            )
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

    fun compareAltTexts(panel: AltTextProvider, expected: String?, timeoutSeconds: Long = 1) {
        var result: String? = null
        panel.altText.subscribe(Subscriber { result = it })
        Awaitility.await().atMost(timeoutSeconds, TimeUnit.SECONDS).until({ result }, Matchers.equalTo(expected))
        Awaitility.await().atMost(timeoutSeconds, TimeUnit.SECONDS).until({ (result ?: "").length }, Matchers.lessThanOrEqualTo(1000))
    }
}
