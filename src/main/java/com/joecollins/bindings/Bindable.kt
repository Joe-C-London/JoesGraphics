package com.joecollins.bindings

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

abstract class Bindable<T : Bindable<T, E>, E : Enum<E>> {

    private val bindings: MutableMap<E, MutableList<(T) -> Unit>> = ConcurrentHashMap()

    internal fun addBinding(binding: (T) -> Unit, vararg properties: E) {
        for (property in properties) bindings.computeIfAbsent(property, createNewList()).add(binding)
    }

    private fun createNewList(): (E) -> MutableList<(T) -> Unit> {
        return { CopyOnWriteArrayList() }
    }

    internal fun removeBinding(binding: (T) -> Unit, vararg properties: E) {
        for (property in properties) bindings.computeIfAbsent(property, createNewList()).remove(binding)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun onPropertyRefreshed(property: E) {
        bindings
            .getOrDefault(property, emptyList())
            .forEach { binding -> binding(this as T) }
    }

    internal fun onPropertyRefreshedInternal(property: E) {
        onPropertyRefreshed(property)
    }
}
