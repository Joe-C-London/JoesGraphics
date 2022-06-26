package com.joecollins.graphics.utils

class BoundResult<T : Any> {
    private var _value: T? = null

    var value: T
        get() = _value!!
        set(value) { _value = value }
}
