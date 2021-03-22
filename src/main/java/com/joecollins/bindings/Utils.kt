package com.joecollins.bindings

import java.util.function.Consumer

object Utils {

    @JvmStatic fun <T> convertConsumer(consumer: Consumer<T>): (T) -> Unit = {
        consumer.accept(it)
    }

    @Deprecated("No longer needed after Kotlin conversion")
    fun <T> convertConsumer(consumer: (T) -> Unit): (T) -> Unit = TODO("KABOOM!")
}
