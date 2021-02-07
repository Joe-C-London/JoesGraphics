package com.joecollins.models.general

import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.StandardWatchEventKinds.OVERFLOW
import java.nio.file.WatchEvent
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class FileWatcherService(private val executor: ExecutorService, private val task: Future<*>) : AutoCloseable {

    interface PathConsumer {
        @Throws(IOException::class) operator fun invoke(path: Path)
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic fun createService(path: Path, onUpdate: PathConsumer): FileWatcherService {
            val executor = Executors.newSingleThreadExecutor {
                val t = Executors.defaultThreadFactory().newThread(it)
                t.isDaemon = true
                t
            }
            Files.walk(path)
                .filter { Files.isRegularFile(it) }
                .forEach { onUpdate(it) }

            val watchService = FileSystems.getDefault().newWatchService()
            path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY)
            val r: () -> Unit = {
                while (true) {
                    val key = watchService.take()
                    key.pollEvents().forEach { event ->
                        try {
                            val kind = event.kind()
                            if (kind != OVERFLOW) {
                                @Suppress("UNCHECKED_CAST") val ev = event as WatchEvent<Path>
                                val filename = ev.context()
                                onUpdate(path.resolve(filename))
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    val valid = key.reset()
                    if (!valid) {
                        System.err.println("Key reset not valid")
                    }
                }
            }
            val future = executor.submit(r)
            return FileWatcherService(executor, future)
        }
    }

    override fun close() {
        task.cancel(true)
        executor.shutdown()
    }
}
