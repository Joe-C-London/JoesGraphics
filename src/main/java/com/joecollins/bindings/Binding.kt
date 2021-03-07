package com.joecollins.bindings

import org.apache.commons.lang3.mutable.Mutable
import org.apache.commons.lang3.mutable.MutableObject

interface Binding<out T> {

    @Deprecated("Replacing with Kotlin version")
    @JvmDefault fun bindLegacy(onUpdate: java.util.function.Consumer<in T>) = bind { onUpdate.accept(it) }

    fun bind(onUpdate: (T) -> Unit)

    fun unbind()

    @JvmDefault fun <R> map(func: (T) -> R): Binding<R> {
        val me = this
        return object : Binding<R> {
            override fun bind(onUpdate: (R) -> Unit) = me.bind { onUpdate(func(it)) }
            override fun unbind() = me.unbind()
        }
    }

    @JvmDefault fun <U, R> merge(other: Binding<U>, mergeFunc: (T, U) -> R): Binding<R> {
        val me = this
        return object : Binding<R> {
            private val val1: Mutable<T> = MutableObject()
            private val val2: Mutable<U> = MutableObject()
            private var bound = false

            override fun bind(onUpdate: (R) -> Unit) {
                me.bind {
                    val1.value = it
                    if (bound) {
                        onUpdate(mergeFunc(val1.value, val2.value))
                    }
                }
                other.bind {
                    val2.value = it
                    onUpdate(mergeFunc(val1.value, val2.value))
                }
                bound = true
            }

            override fun unbind() {
                me.unbind()
                other.unbind()
                bound = false
            }
        }
    }

    companion object {

        @JvmStatic fun <T> fixedBinding(t: T) = object : Binding<T> {
            override fun bind(onUpdate: (T) -> Unit) = onUpdate(t)
            override fun unbind() {}
        }

        @JvmStatic fun <T : Bindable<T, E>, U, E : Enum<E>> propertyBinding(obj: T, func: (T) -> U, vararg properties: E): Binding<U> {
            return object : Binding<U> {
                private var consumer: ((T) -> Unit)? = null
                override fun bind(onUpdate: (U) -> Unit) {
                    check(consumer == null) { "Binding is already used" }
                    val consumer: (T) -> Unit = { t -> onUpdate(func(t)) }
                    obj.addBinding(consumer, *properties)
                    consumer(obj)
                    this.consumer = consumer
                }
                override fun unbind() {
                    val consumer = this.consumer
                    if (consumer != null) {
                        obj.removeBinding(consumer, *properties)
                        this.consumer = null
                    }
                }
            }
        }

        @JvmStatic fun <T> sizeBinding(list: BindableList<T>): Binding<Int> {
            return object : Binding<Int> {
                private var consumer: ((Int) -> Unit)? = null
                override fun bind(onUpdate: (Int) -> Unit) {
                    check(consumer == null) { "Binding is already used" }
                    val consumer: (Int) -> Unit = { t -> onUpdate(t) }
                    list.addSizeBinding(consumer)
                    consumer(list.size)
                    this.consumer = consumer
                }
                override fun unbind() {
                    val consumer = this.consumer
                    if (consumer != null) {
                        list.removeSizeBinding(consumer)
                        this.consumer = null
                    }
                }
            }
        }

        @JvmStatic fun <T, R> mapReduceBinding(
            bindings: List<Binding<T>>,
            identity: R,
            onValueAdded: (R, T) -> R,
            onValueRemoved: (R, T) -> R
        ) = object : Binding<R> {
            private var bound = false
            private var aggregate = identity
            private val values: MutableList<Mutable<T>> = ArrayList(bindings.size)

            override fun bind(onUpdate: (R) -> Unit) {
                check(!bound) { "Binding is already used" }
                for (i in bindings.indices) {
                    values.add(MutableObject())
                }
                aggregate = identity
                for (i in bindings.indices) {
                    bindings[i].bind { newVal ->
                        val oldVal = values[i]
                        if (oldVal != newVal) {
                            if (oldVal.value != null) {
                                aggregate = onValueRemoved(aggregate, oldVal.value)
                            }
                            values[i].value = newVal
                            if (newVal != null) {
                                aggregate = onValueAdded(aggregate, newVal)
                            }
                            if (bound) {
                                onUpdate(aggregate)
                            }
                        }
                    }
                }
                bound = true
                onUpdate(aggregate)
            }
            override fun unbind() {
                bindings.forEach { it.unbind() }
                values.clear()
            }
        }
    }
}
