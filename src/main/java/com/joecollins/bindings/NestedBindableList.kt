package com.joecollins.bindings

import java.util.LinkedList
import java.util.WeakHashMap
import java.util.function.Consumer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NestedBindableList<T : Bindable<T, E>, E : Enum<E>>(items: List<T> = ArrayList()) : BindableList<T>(items) {

    private val itemBindings: MutableMap<E, MutableList<(Int, T) -> Unit>> = HashMap()
    private val storedBindings: MutableMap<(Int, T) -> Unit, MutableMap<Int, Consumer<T>>> = WeakHashMap()

    fun addNestedItemBinding(binding: (Int, T) -> Unit, vararg properties: E) {
        addItemBinding(binding)
        properties.forEach { property ->
            itemBindings.computeIfAbsent(property, { LinkedList() }).add(binding)
        }
        (0 until size).forEach { index ->
            val item = get(index)
            item.addBinding(getOrCreateBinding(binding, index), *properties)
            for (property in properties) item.onPropertyRefreshed(property)
        }
    }

    fun removeNestedItemBinding(binding: (Int, T) -> Unit, vararg properties: E) {
        removeItemBinding(binding)
        properties.forEach { property ->
            itemBindings.computeIfAbsent(property, { LinkedList() }).remove(binding)
        }
        (0 until size).forEach { index ->
            val item = get(index)
            item.removeBinding(getOrCreateBinding(binding, index), *properties)
        }
    }

    override fun addAllBindings(index: Int) {
        super.addAllBindings(index)
        val item = get(index)
        itemBindings.forEach { (property, consumers) ->
            for (consumer in consumers) {
                item.addBinding(getOrCreateBinding(consumer, index), property)
            }
        }
    }

    override fun removeAllBindings(index: Int) {
        super.removeAllBindings(index)
        val item = get(index)
        itemBindings.forEach { (property, consumers) ->
            for (consumer in consumers) {
                item.removeBinding(getOrCreateBinding(consumer, index), property)
            }
        }
    }

    private fun getOrCreateBinding(consumer: (Int, T) -> Unit, index: Int): Consumer<T>? {
        return storedBindings
            .computeIfAbsent(consumer, { HashMap() })
            .computeIfAbsent(index) { idx ->
                Consumer { value: T ->
                    consumer(idx, value)
                }
            }
    }
}
