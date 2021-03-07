package com.joecollins.bindings

import org.apache.commons.lang3.mutable.Mutable
import org.apache.commons.lang3.mutable.MutableObject

class BindingReceiver<T>(binding: Binding<T>) {

    private val _value = WrappedValue<T>()

    init {
        binding.bind { _value.value = it }
    }

    private enum class Property { VALUE }

    private class WrappedValue<T> : Bindable<WrappedValue<T>, Property>() {
        private val _value: Mutable<T> = MutableObject()

        var value: T
        get() {
            return _value.value
        }
        set(v) {
            _value.value = v
            onPropertyRefreshed(Property.VALUE)
        }
    }

    val value: T get() = _value.value

    fun getBinding(): Binding<T> = getBinding { it }

    fun <U> getBinding(func: (T) -> U): Binding<U> = Binding.propertyBinding(_value, { func(it.value) }, Property.VALUE)

    fun <U> getFlatBinding(func: (T) -> Binding<U>): Binding<U> {
        val me = this
        return object : Binding<U> {
            private var topBinding: Binding<T>? = null
            private var subBinding: Binding<U>? = null

            override fun bind(onUpdate: (U) -> Unit) {
                topBinding = me.getBinding()
                topBinding!!.bind {
                    subBinding?.unbind()
                    subBinding = func(it)
                    subBinding!!.bind(onUpdate)
                }
            }
            override fun unbind() {
                topBinding?.unbind()
                subBinding?.unbind()
            }
        }
    }
}
