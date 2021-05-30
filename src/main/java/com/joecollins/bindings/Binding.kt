package com.joecollins.bindings

interface Binding<out T> {

    fun bind(onUpdate: (T) -> Unit)

    fun unbind()

    fun <R> map(func: (T) -> R): Binding<R> {
        val me = this
        return object : Binding<R> {
            override fun bind(onUpdate: (R) -> Unit) = me.bind { onUpdate(func(it)) }
            override fun unbind() = me.unbind()
        }
    }

    fun <U, R> merge(other: Binding<U>, mergeFunc: (T, U) -> R): Binding<R> {
        val me = this
        return object : Binding<R> {
            private var val1: T? = null
            private var val2: U? = null
            private var bound = false

            @Suppress("UNCHECKED_CAST")
            override fun bind(onUpdate: (R) -> Unit) {
                me.bind {
                    val1 = it
                    if (bound) {
                        onUpdate(mergeFunc(val1 as T, val2 as U))
                    }
                }
                other.bind {
                    val2 = it
                    onUpdate(mergeFunc(val1 as T, val2 as U))
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

        @JvmStatic fun <T, R> mapReduceBinding(
            bindings: List<Binding<T>>,
            identity: R,
            onValueAdded: (R, T) -> R,
            onValueRemoved: (R, T) -> R
        ) = object : Binding<R> {
            private var bound = false
            private var aggregate = identity
            private val values: MutableList<T?> = ArrayList(bindings.size)

            override fun bind(onUpdate: (R) -> Unit) {
                check(!bound) { "Binding is already used" }
                for (i in bindings.indices) {
                    values.add(null)
                }
                aggregate = identity
                for (i in bindings.indices) {
                    bindings[i].bind { newVal ->
                        val oldVal = values[i]
                        if (oldVal != newVal) {
                            if (oldVal != null) {
                                aggregate = onValueRemoved(aggregate, oldVal)
                            }
                            values[i] = newVal
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

        @JvmStatic fun <T> listBinding(bindings: List<Binding<T>>): Binding<List<T>> = object : Binding<List<T>> {
            private var bound = false
            private val values: MutableList<T?> = ArrayList(bindings.size)

            @Suppress("UNCHECKED_CAST")
            override fun bind(onUpdate: (List<T>) -> Unit) {
                check(!bound) { "Binding is already used" }
                for (i in bindings.indices) {
                    values.add(null)
                }
                for (i in bindings.indices) {
                    bindings[i].bind { newVal ->
                        val oldVal = values[i]
                        if (oldVal != newVal) {
                            values[i] = newVal
                            if (bound) {
                                onUpdate(values.map { it as T })
                            }
                        }
                    }
                }
                bound = true
                onUpdate(values.map { it as T })
            }
            override fun unbind() {
                bindings.forEach { it.unbind() }
                values.clear()
            }
        }
    }
}

fun <T, R> Binding<List<T>>.mapElements(func: (T) -> R): Binding<List<R>> = this.map { list -> list.map(func) }
