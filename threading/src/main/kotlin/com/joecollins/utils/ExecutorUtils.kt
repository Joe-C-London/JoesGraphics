package com.joecollins.utils

import java.awt.EventQueue
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

object ExecutorUtils {

    private val isJunit = Thread.currentThread().stackTrace.any { it.className.startsWith("org.junit") }

    val defaultExecutor: Executor = createExecutor { ForkJoinPool.commonPool() }

    private val scheduledExecutor = Executors.newScheduledThreadPool(1) { r: Runnable ->
        val t = Executors.defaultThreadFactory().newThread(r)
        t.isDaemon = true
        t.name = "Background-Ticker"
        t
    }

    fun createExecutor(factory: () -> Executor): Executor = if (isJunit) {
        Executor { r: Runnable -> r.run() }
    } else {
        factory()
    }

    fun sendToEventQueue(action: () -> Unit) {
        if (isJunit) {
            action()
        } else {
            EventQueue.invokeLater(action)
        }
    }

    fun scheduleTicking(action: () -> Unit, frequencyMs: Int) {
        if (isJunit) {
            action()
        } else {
            scheduledExecutor.scheduleAtFixedRate(action, 0, frequencyMs.toLong(), TimeUnit.MILLISECONDS)
        }
    }
}
