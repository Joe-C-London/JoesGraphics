package com.joecollins.bindings

import java.util.LinkedList

open class BindableList<T>(items: List<T> = ArrayList()) : AbstractMutableList<T>() {

    private val underlying = ArrayList<T>()

    private val sizeBindings: MutableList<(Int) -> Unit> = LinkedList()
    private val itemBindings: MutableList<(Int, T) -> Unit> = LinkedList()

    init {
        setAll(items)
    }

    fun addSizeBinding(binding: (Int) -> Unit) {
        sizeBindings.add(binding)
    }

    fun removeSizeBinding(binding: (Int) -> Unit) {
        sizeBindings.remove(binding)
    }

    fun addItemBinding(binding: (Int, T) -> Unit) {
        itemBindings.add(binding)
    }

    fun removeItemBinding(binding: (Int, T) -> Unit) {
        itemBindings.remove(binding)
    }

    private fun onSizeUpdated() {
        sizeBindings.forEach { binding -> binding(size) }
    }

    override fun get(index: Int): T {
        return underlying[index]
    }

    override val size: Int get() {
        return underlying.size
    }

    override fun set(index: Int, element: T): T {
        removeAllBindings(index)
        val ret = underlying.set(index, element)
        addAllBindings(index)
        return ret
    }

    fun setAll(elements: Collection<T>) {
        val elementsInOrder: List<T> = ArrayList(elements)
        for (i in elementsInOrder.indices) {
            if (i < size) {
                set(i, elementsInOrder[i])
            } else {
                add(elementsInOrder[i])
            }
        }
        while (size > elements.size) {
            removeAt(elements.size)
        }
    }

    override fun add(index: Int, element: T) {
        for (i in index until size) {
            removeAllBindings(i)
        }
        underlying.add(index, element)
        onSizeUpdated()
        for (i in index until size) {
            addAllBindings(i)
        }
    }

    override fun removeAt(index: Int): T {
        for (i in index until size) {
            removeAllBindings(i)
        }
        val ret = underlying.removeAt(index)
        onSizeUpdated()
        for (i in index until size) {
            addAllBindings(i)
        }
        return ret
    }

    protected open fun addAllBindings(index: Int) {
        val item = get(index)
        itemBindings.forEach { c ->
            c(index, item)
        }
    }

    protected open fun removeAllBindings(index: Int) {}
}
