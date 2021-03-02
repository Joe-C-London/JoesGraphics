package com.joecollins.graphics.utils

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.graphics.utils.BindableWrapper.BindableWrapperValue

class BindableWrapper<T>(private var _value: T) : Bindable<BindableWrapper<T>, BindableWrapperValue>() {

    enum class BindableWrapperValue {
        VALUE
    }

    var value: T
    get() = _value
    set(value) {
        this._value = value
        onPropertyRefreshed(BindableWrapperValue.VALUE)
    }

    val binding: Binding<T>
        get() = Binding.propertyBinding(this, { it.value }, BindableWrapperValue.VALUE)
}
