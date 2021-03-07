package com.joecollins.bindings

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.Binding.Companion.mapReduceBinding
import com.joecollins.bindings.Binding.Companion.propertyBinding
import com.joecollins.bindings.Binding.Companion.sizeBinding
import com.joecollins.bindings.BindingTest.NestedBindable
import org.apache.commons.lang3.mutable.Mutable
import org.apache.commons.lang3.mutable.MutableObject
import org.junit.Assert
import org.junit.Test

class BindingTest {

    @Test
    fun testFixedBinding() {
        val boundValue: Mutable<Int> = MutableObject()
        val binding = fixedBinding(42)
        binding.bind { boundValue.setValue(it) }
        Assert.assertEquals(42, boundValue.value.toInt().toLong())
    }

    @Test
    fun testPropertyBinding() {
        val boundValue: Mutable<Int> = MutableObject()
        val bindable = BindableInt(7)
        val binding = propertyBinding(bindable, { it.value }, BindableValue.Property.VALUE)
        binding.bind { boundValue.setValue(it) }
        Assert.assertEquals(7, boundValue.value.toInt().toLong())
        bindable.setValue(42)
        Assert.assertEquals(42, boundValue.value.toInt().toLong())
        binding.unbind()
        bindable.setValue(7)
        Assert.assertEquals(42, boundValue.value.toInt().toLong())
    }

    @Test
    fun testSizeBinding() {
        val boundValue: Mutable<Int> = MutableObject()
        val list = BindableList<Int>()
        list.add(7)
        val binding = sizeBinding(list)
        binding.bind { boundValue.setValue(it) }
        Assert.assertEquals(1, boundValue.value.toInt().toLong())
        list.add(42)
        Assert.assertEquals(2, boundValue.value.toInt().toLong())
        list.removeAt(1)
        Assert.assertEquals(1, boundValue.value.toInt().toLong())
        list.add(0, 7)
        Assert.assertEquals(2, boundValue.value.toInt().toLong())
        list.clear()
        Assert.assertEquals(0, boundValue.value.toInt().toLong())
        list.add(7)
        Assert.assertEquals(1, boundValue.value.toInt().toLong())
        binding.unbind()
        list.add(42)
        Assert.assertEquals(1, boundValue.value.toInt().toLong())
    }

    @Test
    fun testMapBinding() {
        val boundValue: Mutable<String> = MutableObject()
        val bindable = BindableInt(7)
        val binding = propertyBinding(bindable, { it.value }, BindableValue.Property.VALUE)
                .map { it.toString() }
        binding.bind { boundValue.setValue(it) }
        Assert.assertEquals("7", boundValue.value)
        bindable.setValue(42)
        Assert.assertEquals("42", boundValue.value)
        binding.unbind()
        bindable.setValue(7)
        Assert.assertEquals("42", boundValue.value)
    }

    @Test
    fun testMergeBinding() {
        val boundValue: Mutable<Int> = MutableObject()
        val bindable1 = BindableInt(7)
        val bindable2 = BindableInt(42)
        val binding1 = propertyBinding(bindable1, { it.value }, BindableValue.Property.VALUE)
        val binding2 = propertyBinding(bindable2, { it.value }, BindableValue.Property.VALUE)
        val binding = binding1.merge(binding2) { a, b -> a + b }
        binding.bind { boundValue.setValue(it) }
        Assert.assertEquals(49, boundValue.value.toInt().toLong())
        bindable1.setValue(42)
        Assert.assertEquals(84, boundValue.value.toInt().toLong())
        bindable2.setValue(7)
        Assert.assertEquals(49, boundValue.value.toInt().toLong())
        binding.unbind()
        bindable1.setValue(7)
        Assert.assertEquals(49, boundValue.value.toInt().toLong())
        bindable2.setValue(1)
        Assert.assertEquals(49, boundValue.value.toInt().toLong())
    }

    @Test
    fun testBindingReceiverBasic() {
        val boundValue1: Mutable<Int> = MutableObject()
        val boundValue2: Mutable<Int> = MutableObject()
        val bindable = BindableInt(7)
        val binding = propertyBinding(bindable, { it.value }, BindableValue.Property.VALUE)
        val receiver = BindingReceiver(binding)
        val binding1 = receiver.getBinding()
        binding1.bind { boundValue1.setValue(it) }
        val binding2 = receiver.getBinding()
        binding2.bind { boundValue2.setValue(it) }
        Assert.assertEquals(7, boundValue1.value.toInt().toLong())
        Assert.assertEquals(7, boundValue2.value.toInt().toLong())
        bindable.setValue(42)
        Assert.assertEquals(42, boundValue1.value.toInt().toLong())
        Assert.assertEquals(42, boundValue2.value.toInt().toLong())
        binding1.unbind()
        bindable.setValue(7)
        Assert.assertEquals(42, boundValue1.value.toInt().toLong())
        Assert.assertEquals(7, boundValue2.value.toInt().toLong())
    }

    @Test
    fun testBindingReceiverMap() {
        val boundValue1: Mutable<Int> = MutableObject()
        val boundValue2: Mutable<Int> = MutableObject()
        val bindable = BindableInt(7)
        val binding = propertyBinding(bindable, { it.value }, BindableValue.Property.VALUE)
        val receiver = BindingReceiver(binding)
        val binding1 = receiver.getBinding { it * 2 }
        binding1.bind { boundValue1.setValue(it) }
        val binding2 = receiver.getBinding { it * it }
        binding2.bind { boundValue2.setValue(it) }
        Assert.assertEquals(14, boundValue1.value.toInt().toLong())
        Assert.assertEquals(49, boundValue2.value.toInt().toLong())
        bindable.setValue(42)
        Assert.assertEquals(84, boundValue1.value.toInt().toLong())
        Assert.assertEquals(1764, boundValue2.value.toInt().toLong())
        binding1.unbind()
        bindable.setValue(7)
        Assert.assertEquals(84, boundValue1.value.toInt().toLong())
        Assert.assertEquals(49, boundValue2.value.toInt().toLong())
    }

    @Test
    fun testBindingReceiverFlatMap() {
        val boundValue: Mutable<Int> = MutableObject()
        val bindable1 = BindableInt(7)
        val nested = NestedBindable(bindable1)
        val nestedBinding = propertyBinding(nested, { it.value }, BindableValue.Property.VALUE)
        val receiver = BindingReceiver(nestedBinding)
        val binding = receiver.getFlatBinding { propertyBinding(it, { it.value }, BindableValue.Property.VALUE) }
        binding.bind { boundValue.setValue(it) }
        Assert.assertEquals(7, boundValue.value.toInt().toLong())
        bindable1.setValue(42)
        Assert.assertEquals(42, boundValue.value.toInt().toLong())
        val bindable2 = BindableInt(12)
        nested.setValue(bindable2)
        Assert.assertEquals(12, boundValue.value.toInt().toLong())
        bindable1.setValue(1)
        Assert.assertEquals(12, boundValue.value.toInt().toLong())
        binding.unbind()
        bindable2.setValue(10)
        Assert.assertEquals(12, boundValue.value.toInt().toLong())
        nested.setValue(BindableInt(27))
        Assert.assertEquals(12, boundValue.value.toInt().toLong())
    }

    @Test
    fun testMapReduceBinding() {
        val boundValue: Mutable<Int> = MutableObject()
        val list = listOf(BindableInt(1), BindableInt(2), BindableInt(3))
        val bindings = list
                .map { b: BindableInt ->
                    propertyBinding(b, { it.value }, BindableValue.Property.VALUE)
                }
                .toList()
        val binding = mapReduceBinding(bindings, 0, { a: Int, v: Int -> a + v }, { a: Int, v: Int -> a - v })
        binding.bind { boundValue.setValue(it) }
        Assert.assertEquals(6, boundValue.value.toInt().toLong())
        list[0].setValue(4)
        list[1].setValue(5)
        list[2].setValue(6)
        Assert.assertEquals(15, boundValue.value.toInt().toLong())
        binding.unbind()
        list[0].setValue(1)
        list[1].setValue(2)
        list[2].setValue(3)
        Assert.assertEquals(15, boundValue.value.toInt().toLong())
    }

    @Test
    fun testEmptyMapReduceBinding() {
        val boundValue: Mutable<Int> = MutableObject()
        val bindings: List<Binding<Int>> = listOf()
        val binding = mapReduceBinding(bindings, 1, { a, b -> a + b }, { a, b -> a - b })
        binding.bind { boundValue.setValue(it) }
        Assert.assertEquals(1, boundValue.value.toInt().toLong())
    }

    private open class BindableValue<T>(value: T) : Bindable<BindableValue<T>, BindableValue.Property>() {
        enum class Property {
            VALUE
        }

        var value: T = value
            private set

        fun setValue(value: T) {
            this.value = value
            onPropertyRefreshed(Property.VALUE)
        }
    }

    private class BindableInt(value: Int) : BindableValue<Int>(value)

    private class NestedBindable<T>(value: BindableValue<T>) : BindableValue<BindableValue<T>>(value)
}
