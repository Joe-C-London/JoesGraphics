package com.joecollins.models.general

import com.joecollins.models.general.FileWatcherService.Companion.createService
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.lang.InterruptedException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.HashMap
import kotlin.Throws

@Suppress("BlockingMethodInNonBlockingContext")
class FileWatcherServiceTest {
    private var tempPath: Path? = null
    @Before
    @Throws(IOException::class)
    fun setup() {
        tempPath = Files.createTempDirectory("fws-test")
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        Files.walk(tempPath).map { obj: Path -> obj.toFile() }.forEach { obj: File -> obj.delete() }
        Files.deleteIfExists(tempPath)
    }

    @Test
    @Throws(IOException::class)
    fun testFirstRead() {
        val file1Contents = "File 1 Contents"
        val file2Contents = "File 2 Contents"
        Files.writeString(tempPath!!.resolve("file1.txt"), file1Contents, UTF8)
        Files.writeString(tempPath!!.resolve("file2.txt"), file2Contents, UTF8)
        val contents: MutableMap<Path, String> = HashMap()
        createService(tempPath!!) { p: Path -> contents[p] = Files.readString(p, UTF8) }.use { }
        Assert.assertEquals(file1Contents, contents[tempPath!!.resolve("file1.txt")])
        Assert.assertEquals(file2Contents, contents[tempPath!!.resolve("file2.txt")])
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testUpdate() {
        val file1Contents = "File 1 Contents"
        val file1NewContents = "File 1 New Contents"
        val file2Contents = "File 2 Contents"
        val file3Contents = "File 3 Contents"
        Files.writeString(tempPath!!.resolve("file1.txt"), file1Contents, UTF8)
        Files.writeString(tempPath!!.resolve("file2.txt"), file2Contents, UTF8)
        val contents: MutableMap<Path, String> = HashMap()
        createService(tempPath!!) { p: Path -> contents[p] = Files.readString(p, UTF8) }.use {
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
        Assert.assertEquals(file1NewContents, contents[tempPath!!.resolve("file1.txt")])
        Assert.assertEquals(file2Contents, contents[tempPath!!.resolve("file2.txt")])
        Assert.assertEquals(file3Contents, contents[tempPath!!.resolve("file3.txt")])
    }

    @Test(expected = IOException::class)
    @Throws(IOException::class)
    fun testIOExceptionThrownOnFirstAttempt() {
        val file1Contents = "File 1 Contents"
        Files.writeString(tempPath!!.resolve("file1.txt"), file1Contents, UTF8)
        createService(tempPath!!) { throw IOException("KABOOM!") }.use { }
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testIOExceptionOnUpdateIgnored() {
        val file1Contents = "File 1 Contents"
        val file1NewContents = "File 1 New Contents"
        val file2Contents = "File 2 Contents"
        val file3Contents = "File 3 Contents"
        Files.writeString(tempPath!!.resolve("file1.txt"), file1Contents, UTF8)
        Files.writeString(tempPath!!.resolve("file2.txt"), file2Contents, UTF8)
        val contents: MutableMap<Path, String> = HashMap()
        createService(tempPath!!) { p: Path ->
            if (p.toString().contains("file3.txt")) {
                throw IOException("KABOOM!")
            }
            contents[p] = Files.readString(p, UTF8)
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
        Assert.assertEquals(file1NewContents, contents[tempPath!!.resolve("file1.txt")])
        Assert.assertEquals(file2Contents, contents[tempPath!!.resolve("file2.txt")])
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRuntimeExceptionOnUpdateIgnored() {
        val file1Contents = "File 1 Contents"
        val file1NewContents = "File 1 New Contents"
        val file2Contents = "File 2 Contents"
        val file3Contents = "File 3 Contents"
        Files.writeString(tempPath!!.resolve("file1.txt"), file1Contents, UTF8)
        Files.writeString(tempPath!!.resolve("file2.txt"), file2Contents, UTF8)
        val contents: MutableMap<Path, String> = HashMap()
        createService(tempPath!!) { p: Path ->
            if (p.toString().contains("file3.txt")) {
                throw IllegalArgumentException("KABOOM!")
            }
            contents[p] = Files.readString(p, UTF8)
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
        Assert.assertEquals(file1NewContents, contents[tempPath!!.resolve("file1.txt")])
        Assert.assertEquals(file2Contents, contents[tempPath!!.resolve("file2.txt")])
    }

    companion object {
        private val UTF8 = StandardCharsets.UTF_8
    }
}
