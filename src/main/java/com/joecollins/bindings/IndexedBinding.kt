package com.joecollins.bindings

import java.lang.IllegalStateException

interface IndexedBinding<out T> {

    @Deprecated("Convert to Kotlin version")
    @JvmDefault fun bindLegacy(onUpdate: java.util.function.BiConsumer<Int, in T>) = bind { i, v -> onUpdate.accept(i, v) }

    fun bind(onUpdate: (Int, T) -> Unit)

    fun unbind()

    @JvmDefault fun <U> map(func: (T) -> U): IndexedBinding<U> {
        val me = this
        return object : IndexedBinding<U> {
            override fun bind(onUpdate: (Int, U) -> Unit) = me.bind { idx, value -> onUpdate(idx, func(value)) }
            override fun unbind() = me.unbind()
        }
    }

    companion object {
        @JvmStatic fun <T> emptyBinding(): IndexedBinding<T> = object : IndexedBinding<T> {
            override fun bind(onUpdate: (Int, T) -> Unit) {}
            override fun unbind() {}
        }

        @JvmStatic fun <T> singletonBinding(item: T): IndexedBinding<T> = object : IndexedBinding<T> {
            override fun bind(onUpdate: (Int, T) -> Unit) { onUpdate(0, item) }
            override fun unbind() {}
        }

        @JvmStatic fun <T> listBinding(vararg list: T): IndexedBinding<T> = object : IndexedBinding<T> {
            override fun bind(onUpdate: (Int, T) -> Unit) {
                list.indices.forEach { onUpdate(it, list[it]) }
            }
            override fun unbind() {}
        }

        @JvmStatic fun <T> listBinding(list: List<T>): IndexedBinding<T> = object : IndexedBinding<T> {
            override fun bind(onUpdate: (Int, T) -> Unit) {
                list.indices.forEach { onUpdate(it, list[it]) }
            }
            override fun unbind() {}
        }

        @JvmStatic fun <T, U> listBinding(list: List<T>, func: (T) -> Binding<U>): IndexedBinding<U> {
            val bindings = list.map(func)
            return object : IndexedBinding<U> {
                override fun bind(onUpdate: (Int, U) -> Unit) {
                    list.indices.forEach { idx -> bindings[idx].bind { onUpdate(idx, it) } }
                }
                override fun unbind() {
                    bindings.forEach { it.unbind() }
                }
            }
        }

        @JvmStatic fun <E : Enum<E>, T : Bindable<T, E>, U> propertyBinding(item: T, func: (T) -> List<U>, vararg properties: E) = object : IndexedBinding<U> {
            private var consumer: ((T) -> Unit)? = null

            override fun bind(onUpdate: (Int, U) -> Unit) {
                check(consumer == null) { "Binding is already used" }
                val consumer: (T) -> Unit = {
                    val values = func(it)
                    values.indices.forEach { idx -> onUpdate(idx, values[idx]) }
                }
                item.addBinding(consumer, *properties)
                consumer(item)
                this.consumer = consumer
            }
            override fun unbind() {
                val consumer = this.consumer
                if (consumer != null) {
                    item.removeBinding(consumer, *properties)
                    this.consumer = null
                }
            }
        }

        @JvmStatic fun <T> propertyBinding(list: BindableList<T>) = propertyBinding(list) { it }

        @JvmStatic fun <T, U> propertyBinding(list: BindableList<T>, func: (T) -> U) = object : IndexedBinding<U> {
            private var consumer: ((Int, T) -> Unit)? = null

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

        @JvmStatic fun <E : Enum<E>, T : Bindable<T, E>, U> propertyBinding(list: NestedBindableList<T, E>, func: (T) -> U, vararg properties: E) = object : IndexedBinding<U> {
            private var consumer: ((Int, T) -> Unit)? = null

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
            override fun bind(onUpdate: (Int, T) -> Unit) {
                (startInclusive until endExclusive).forEach { onUpdate(it - startInclusive, func(it)) }
            }
            override fun unbind() {}
        }
    }
}
