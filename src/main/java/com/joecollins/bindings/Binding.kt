package com.joecollins.bindings

import org.apache.commons.lang3.mutable.Mutable
import org.apache.commons.lang3.mutable.MutableObject

interface Binding<T> {

    val value: T

    @Deprecated("Replacing with Kotlin version")
    @JvmDefault fun bindLegacy(onUpdate: java.util.function.Consumer<T>) = bind { onUpdate.accept(it) }

    @JvmDefault fun bind(onUpdate: (T) -> Unit) = onUpdate(value)

    @JvmDefault fun unbind() {}

    @JvmDefault fun <R> map(func: (T) -> R): Binding<R> {
        val me = this
        return object : Binding<R> {
            override val value get() = func(me.value)
            override fun bind(onUpdate: (R) -> Unit) = me.bind { onUpdate(func(it)) }
            override fun unbind() = me.unbind()
        }
    }

    @JvmDefault fun <R> mapNonNull(func: (T) -> R): Binding<R?> = map { if (it == null) null else func(it) }

    @JvmDefault fun <U, R> merge(other: Binding<U>, mergeFunc: (T, U) -> R): Binding<R> {
        val me = this
        return object : Binding<R> {
            val val1: Mutable<T> = MutableObject()
            val val2: Mutable<U> = MutableObject()
            var bound = false

            override val value: R get() = mergeFunc(me.value, other.value)
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
            override val value: T = t
        }

        @JvmStatic fun <T : Bindable<E>, U, E : Enum<E>> propertyBinding(obj: T, func: (T) -> U, vararg properties: E): Binding<U> {
            return object : Binding<U> {
                var consumer: java.util.function.Consumer<T>? = null
                override val value: U get() = func(obj)
                override fun bind(onUpdate: (U) -> Unit) {
                    check(consumer == null) { "Binding is already used" }
                    consumer = java.util.function.Consumer<T> { t -> onUpdate(func(t)) }
                    obj.addBinding(consumer, *properties)
                    consumer!!.accept(obj)
                }
                override fun unbind() {
                    obj.removeBinding(consumer, *properties)
                    consumer = null
                }
            }
        }

        @JvmStatic fun <T> sizeBinding(list: BindableList<T>): Binding<Int> {
            return object : Binding<Int> {
                var consumer: java.util.function.IntConsumer? = null
                override val value: Int get() = list.size
                override fun bind(onUpdate: (Int) -> Unit) {
                    check(consumer == null) { "Binding is already used" }
                    consumer = java.util.function.IntConsumer { t -> onUpdate(t) }
                    list.addSizeBinding(consumer)
                    consumer!!.accept(list.size)
                }
                override fun unbind() {
                    list.removeSizeBinding(consumer)
                    consumer = null
                }
            }
        }

        @JvmStatic fun <T, R> mapReduceBinding(
            bindings: List<Binding<T>>,
            identity: R,
            onValueAdded: (R, T) -> R,
            onValueRemoved: (R, T) -> R
        ) = object : Binding<R> {
            var bound = false
            var aggregate = identity
            val values: MutableList<Mutable<T>> = ArrayList(bindings.size)

            override val value: R get() = bindings.fold(identity) { agg, value -> onValueAdded(agg, value.value) }
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
