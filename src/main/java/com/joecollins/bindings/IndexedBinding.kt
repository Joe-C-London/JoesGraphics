package com.joecollins.bindings

import java.lang.IllegalStateException
import java.lang.IndexOutOfBoundsException

interface IndexedBinding<T> {

    val size: Int

    operator fun get(index: Int): T

    @Deprecated("Convert to Kotlin version")
    @JvmDefault fun bindLegacy(onUpdate: java.util.function.BiConsumer<Int, T>) = bind { i, v -> onUpdate.accept(i, v) }

    @JvmDefault fun bind(onUpdate: (Int, T) -> Unit) {
        (0 until size).forEach { onUpdate(it, this[it]) }
    }

    @JvmDefault fun unbind() {}

    @JvmDefault fun <U> map(func: (T) -> U): IndexedBinding<U> {
        val me = this
        return object : IndexedBinding<U> {
            override val size get() = me.size
            override fun get(index: Int) = func(me[index])
            override fun bind(onUpdate: (Int, U) -> Unit) = me.bind { idx, value -> onUpdate(idx, func(value)) }
            override fun unbind() = me.unbind()
        }
    }

    companion object {
        @JvmStatic fun <T> emptyBinding(): IndexedBinding<T> = object : IndexedBinding<T> {
            override val size = 0
            override fun get(index: Int): T {
                throw IndexOutOfBoundsException(index)
            }
        }

        @JvmStatic fun <T> singletonBinding(item: T): IndexedBinding<T> = object : IndexedBinding<T> {
            override val size = 1
            override fun get(index: Int): T {
                check(index == 0) { throw ArrayIndexOutOfBoundsException(index) }
                return item
            }
        }

        @JvmStatic fun <T> listBinding(vararg list: T): IndexedBinding<T> = object : IndexedBinding<T> {
            override val size get() = list.size
            override fun get(index: Int): T = list[index]
        }

        @JvmStatic fun <T> listBinding(list: List<T>): IndexedBinding<T> = object : IndexedBinding<T> {
            override val size get() = list.size
            override fun get(index: Int): T = list[index]
        }

        @JvmStatic fun <T, U> listBinding(list: List<T>, func: (T) -> Binding<U>): IndexedBinding<U> {
            val bindings = list.map(func)
            return object : IndexedBinding<U> {
                override val size get() = list.size
                override fun get(index: Int): U = func(list[index]).value
                override fun bind(onUpdate: (Int, U) -> Unit) {
                    list.indices.forEach { idx -> bindings[idx].bind { onUpdate(idx, it) } }
                }
                override fun unbind() {
                    bindings.forEach { it.unbind() }
                }
            }
        }

        @JvmStatic fun <E : Enum<E>, T : Bindable<E>, U> propertyBinding(item: T, func: (T) -> List<U>, vararg properties: E) = object : IndexedBinding<U> {
            var consumer: java.util.function.Consumer<T>? = null

            override val size get() = func(item).size
            override fun get(index: Int) = func(item)[index]
            override fun bind(onUpdate: (Int, U) -> Unit) {
                check(consumer == null) { "Binding is already used" }
                consumer = java.util.function.Consumer {
                    val values = func(it)
                    values.indices.forEach { idx -> onUpdate(idx, values[idx]) }
                }
                item.addBinding(consumer, *properties)
                consumer!!.accept(item)
            }
            override fun unbind() {
                item.removeBinding(consumer, *properties)
                consumer = null
            }
        }

        @JvmStatic fun <T> propertyBinding(list: BindableList<T>) = propertyBinding(list) { it }

        @JvmStatic fun <T, U> propertyBinding(list: BindableList<T>, func: (T) -> U) = object : IndexedBinding<U> {
            var consumer: ((Int, T) -> Unit)? = null

            override val size get(): Int = list.size
            override fun get(index: Int): U = func(list[index])
            override fun bind(onUpdate: (Int, U) -> Unit) {
                check(consumer == null) { "Binding is already used" }
                val consumer: (Int, T) -> Unit = { i, t -> onUpdate(i, func(t)) }
                list.addItemBinding(consumer)
                list.indices.forEach { consumer(it, list[it]) }
                this.consumer = consumer
            }
            override fun unbind() {
                val consumer = this.consumer
                if (consumer != null) {
                    list.removeItemBinding(consumer)
                    this.consumer = null
                }
            }
        }

        @JvmStatic fun <E : Enum<E>, T : Bindable<E>, U> propertyBinding(list: NestedBindableList<T, E>, func: (T) -> U, vararg properties: E) = object : IndexedBinding<U> {
            var consumer: ((Int, T) -> Unit)? = null

            override val size get(): Int = list.size
            override fun get(index: Int): U = func(list[index])
            override fun bind(onUpdate: (Int, U) -> Unit) {
                check(consumer == null) { throw IllegalStateException("Binding is already used") }
                val consumer: (Int, T) -> Unit = { i, t -> onUpdate(i, func(t)) }
                list.addNestedItemBinding(consumer, *properties)
                list.indices.forEach { consumer(it, list[it]) }
                this.consumer = consumer
            }
            override fun unbind() {
                val consumer = this.consumer
                if (consumer != null) {
                    list.removeNestedItemBinding(consumer, *properties)
                }
                this.consumer = null
            }
        }

        @JvmStatic fun <T> functionBinding(startInclusive: Int, endExclusive: Int, func: (Int) -> T): IndexedBinding<T> = object : IndexedBinding<T> {
            override val size = endExclusive - startInclusive
            override fun get(index: Int) = func(index + startInclusive)
        }
    }
}