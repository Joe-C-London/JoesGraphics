package com.joecollins.models.general

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.HashMap

@Suppress("BlockingMethodInNonBlockingContext")
class FileWatcherServiceTest {
    private var tempPath: Path? = null

    @BeforeEach
    fun setup() {
        tempPath = Files.createTempDirectory("fws-test")
    }

    @AfterEach
    fun teardown() {
        Files.walk(tempPath).map { it.toFile() }.forEach { it.delete() }
        Files.deleteIfExists(tempPath)
    }

    @Test
    fun testFirstRead() {
        val file1Contents = "File 1 Contents"
        val file2Contents = "File 2 Contents"
        Files.writeString(tempPath!!.resolve("file1.txt"), file1Contents, UTF8)
        Files.writeString(tempPath!!.resolve("file2.txt"), file2Contents, UTF8)
        val contents: MutableMap<Path, String> = HashMap()
        FileWatcherService.createService(tempPath!!) { contents[it] = Files.readString(it, UTF8) }.use { }
        Assertions.assertEquals(file1Contents, contents[tempPath!!.resolve("file1.txt")])
        Assertions.assertEquals(file2Contents, contents[tempPath!!.resolve("file2.txt")])
    }

    @Test
    fun testUpdate() {
        val file1Contents = "File 1 Contents"
        val file1NewContents = "File 1 New Contents"
        val file2Contents = "File 2 Contents"
        val file3Contents = "File 3 Contents"
        Files.writeString(tempPath!!.resolve("file1.txt"), file1Contents, UTF8)
        Files.writeString(tempPath!!.resolve("file2.txt"), file2Contents, UTF8)
        val contents: MutableMap<Path, String> = HashMap()
        FileWatcherService.createService(tempPath!!) { contents[it] = Files.readString(it, UTF8) }.use {
            run {
                val tmpFile = Files.createTempFile("file", ".txt")
                Files.writeString(tmpFile, file1NewContents, UTF8)
                Files.move(tmpFile, tempPath!!.resolve("file1.txt"), StandardCopyOption.REPLACE_EXISTING)
            }
            run {
                val tmpFile = Files.createTempFile("file", ".txt")
                Files.writeString(tmpFile, file3Contents, UTF8)
                Files.move(tmpFile, tempPath!!.resolve("file3.txt"), StandardCopyOption.REPLACE_EXISTING)
            }
            Thread.sleep(100)
        }
        Assertions.assertEquals(file1NewContents, contents[tempPath!!.resolve("file1.txt")])
        Assertions.assertEquals(file2Contents, contents[tempPath!!.resolve("file2.txt")])
        Assertions.assertEquals(file3Contents, contents[tempPath!!.resolve("file3.txt")])
    }

    @Test
    fun testIOExceptionThrownOnFirstAttempt() {
        val file1Contents = "File 1 Contents"
        Files.writeString(tempPath!!.resolve("file1.txt"), file1Contents, UTF8)
        Assertions.assertThrows(IOException::class.java) {
            FileWatcherService.createService(tempPath!!) { throw IOException("KABOOM!") }.use { }
        }
    }

    @Test
    fun testIOExceptionOnUpdateIgnored() {
        val file1Contents = "File 1 Contents"
        val file1NewContents = "File 1 New Contents"
        val file2Contents = "File 2 Contents"
        val file3Contents = "File 3 Contents"
        Files.writeString(tempPath!!.resolve("file1.txt"), file1Contents, UTF8)
        Files.writeString(tempPath!!.resolve("file2.txt"), file2Contents, UTF8)
        val contents: MutableMap<Path, String> = HashMap()
        FileWatcherService.createService(tempPath!!) {
            if (it.toString().contains("file3.txt")) {
                throw IOException("KABOOM!")
            }
            contents[it] = Files.readString(it, UTF8)
        }.use {
            run {
                val tmpFile = Files.createTempFile("file", ".txt")
                Files.writeString(tmpFile, file3Contents, UTF8)
                Files.move(tmpFile, tempPath!!.resolve("file3.txt"), StandardCopyOption.REPLACE_EXISTING)
            }
            run {
                val tmpFile = Files.createTempFile("file", ".txt")
                Files.writeString(tmpFile, file1NewContents, UTF8)
                Files.move(tmpFile, tempPath!!.resolve("file1.txt"), StandardCopyOption.REPLACE_EXISTING)
            }
            Thread.sleep(200)
        }
        Assertions.assertEquals(file1NewContents, contents[tempPath!!.resolve("file1.txt")])
        Assertions.assertEquals(file2Contents, contents[tempPath!!.resolve("file2.txt")])
    }

    @Test
    fun testRuntimeExceptionOnUpdateIgnored() {
        val file1Contents = "File 1 Contents"
        val file1NewContents = "File 1 New Contents"
        val file2Contents = "File 2 Contents"
        val file3Contents = "File 3 Contents"
        Files.writeString(tempPath!!.resolve("file1.txt"), file1Contents, UTF8)
        Files.writeString(tempPath!!.resolve("file2.txt"), file2Contents, UTF8)
        val contents: MutableMap<Path, String> = HashMap()
        FileWatcherService.createService(tempPath!!) {
            if (it.toString().contains("file3.txt")) {
                throw IllegalArgumentException("KABOOM!")
            }
            contents[it] = Files.readString(it, UTF8)
        }.use {
            run {
                val tmpFile = Files.createTempFile("file", ".txt")
                Files.writeString(tmpFile, file3Contents, UTF8)
                Files.move(tmpFile, tempPath!!.resolve("file3.txt"), StandardCopyOption.REPLACE_EXISTING)
            }
            run {
                val tmpFile = Files.createTempFile("file", ".txt")
                Files.writeString(tmpFile, file1NewContents, UTF8)
                Files.move(tmpFile, tempPath!!.resolve("file1.txt"), StandardCopyOption.REPLACE_EXISTING)
            }
            Thread.sleep(200)
        }
        Assertions.assertEquals(file1NewContents, contents[tempPath!!.resolve("file1.txt")])
        Assertions.assertEquals(file2Contents, contents[tempPath!!.resolve("file2.txt")])
    }

    companion object {
        private val UTF8 = StandardCharsets.UTF_8
    }
}