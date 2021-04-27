package com.joecollins.bindings

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.Binding.Companion.listBinding
import com.joecollins.bindings.Binding.Companion.mapReduceBinding
import com.joecollins.bindings.Binding.Companion.propertyBinding
import com.joecollins.bindings.Binding.Companion.sizeBinding
import com.joecollins.graphics.utils.BoundResult
import org.junit.Assert
import org.junit.Test

class BindingTest {

    @Test
    fun testFixedBinding() {
        val boundValue: BoundResult<Int> = BoundResult()
        val binding = fixedBinding(42)
        binding.bind { boundValue.value = it }
        Assert.assertEquals(42, boundValue.value.toLong())
    }

    @Test
    fun testPropertyBinding() {
        val boundValue: BoundResult<Int> = BoundResult()
        val bindable = BindableInt(7)
        val binding = propertyBinding(bindable, { it.value }, BindableValue.Property.VALUE)
        binding.bind { boundValue.value = it }
        Assert.assertEquals(7, boundValue.value.toLong())
        bindable.setValue(42)
        Assert.assertEquals(42, boundValue.value.toLong())
        binding.unbind()
        bindable.setValue(7)
        Assert.assertEquals(42, boundValue.value.toLong())
    }

    @Test
    fun testSizeBinding() {
        val boundValue: BoundResult<Int> = BoundResult()
        val list = BindableList<Int>()
        list.add(7)
        val binding = sizeBinding(list)
        binding.bind { boundValue.value = it }
        Assert.assertEquals(1, boundValue.value.toLong())
        list.add(42)
        Assert.assertEquals(2, boundValue.value.toLong())
        list.removeAt(1)
        Assert.assertEquals(1, boundValue.value.toLong())
        list.add(0, 7)
        Assert.assertEquals(2, boundValue.value.toLong())
        list.clear()
        Assert.assertEquals(0, boundValue.value.toLong())
        list.add(7)
        Assert.assertEquals(1, boundValue.value.toLong())
        binding.unbind()
        list.add(42)
        Assert.assertEquals(1, boundValue.value.toLong())
    }

    @Test
    fun testMapBinding() {
        val boundValue: BoundResult<String> = BoundResult()
        val bindable = BindableInt(7)
        val binding = propertyBinding(bindable, { it.value }, BindableValue.Property.VALUE)
                .map { it.toString() }
        binding.bind { boundValue.value = it }
        Assert.assertEquals("7", boundValue.value)
        bindable.setValue(42)
        Assert.assertEquals("42", boundValue.value)
        binding.unbind()
        bindable.setValue(7)
        Assert.assertEquals("42", boundValue.value)
    }

    @Test
    fun testMergeBinding() {
        val boundValue: BoundResult<Int> = BoundResult()
        val bindable1 = BindableInt(7)
        val bindable2 = BindableInt(42)
        val binding1 = propertyBinding(bindable1, { it.value }, BindableValue.Property.VALUE)
        val binding2 = propertyBinding(bindable2, { it.value }, BindableValue.Property.VALUE)
        val binding = binding1.merge(binding2) { a, b -> a + b }
        binding.bind { boundValue.value = it }
        Assert.assertEquals(49, boundValue.value.toLong())
        bindable1.setValue(42)
        Assert.assertEquals(84, boundValue.value.toLong())
        bindable2.setValue(7)
        Assert.assertEquals(49, boundValue.value.toLong())
        binding.unbind()
        bindable1.setValue(7)
        Assert.assertEquals(49, boundValue.value.toLong())
        bindable2.setValue(1)
        Assert.assertEquals(49, boundValue.value.toLong())
    }

    @Test
    fun testBindingReceiverBasic() {
        val boundValue1: BoundResult<Int> = BoundResult()
        val boundValue2: BoundResult<Int> = BoundResult()
        val bindable = BindableInt(7)
        val binding = propertyBinding(bindable, { it.value }, BindableValue.Property.VALUE)
        val receiver = BindingReceiver(binding)
        val binding1 = receiver.getBinding()
        binding1.bind { boundValue1.value = it }
        val binding2 = receiver.getBinding()
        binding2.bind { boundValue2.value = it }
        Assert.assertEquals(7, boundValue1.value.toLong())
        Assert.assertEquals(7, boundValue2.value.toLong())
        bindable.setValue(42)
        Assert.assertEquals(42, boundValue1.value.toLong())
        Assert.assertEquals(42, boundValue2.value.toLong())
        binding1.unbind()
        bindable.setValue(7)
        Assert.assertEquals(42, boundValue1.value.toLong())
        Assert.assertEquals(7, boundValue2.value.toLong())
    }

    @Test
    fun testBindingReceiverMap() {
        val boundValue1: BoundResult<Int> = BoundResult()
        val boundValue2: BoundResult<Int> = BoundResult()
        val bindable = BindableInt(7)
        val binding = propertyBinding(bindable, { it.value }, BindableValue.Property.VALUE)
        val receiver = BindingReceiver(binding)
        val binding1 = receiver.getBinding { it * 2 }
        binding1.bind { boundValue1.value = it }
        val binding2 = receiver.getBinding { it * it }
        binding2.bind { boundValue2.value = it }
        Assert.assertEquals(14, boundValue1.value.toLong())
        Assert.assertEquals(49, boundValue2.value.toLong())
        bindable.setValue(42)
        Assert.assertEquals(84, boundValue1.value.toLong())
        Assert.assertEquals(1764, boundValue2.value.toLong())
        binding1.unbind()
        bindable.setValue(7)
        Assert.assertEquals(84, boundValue1.value.toLong())
        Assert.assertEquals(49, boundValue2.value.toLong())
    }

    @Test
    fun testBindingReceiverFlatMap() {
        val boundValue: BoundResult<Int> = BoundResult()
        val bindable1 = BindableInt(7)
        val nested = NestedBindable(bindable1)
        val nestedBinding = propertyBinding(nested, { it.value }, BindableValue.Property.VALUE)
        val receiver = BindingReceiver(nestedBinding)
        val binding = receiver.getFlatBinding { propertyBinding(it, { e -> e.value }, BindableValue.Property.VALUE) }
        binding.bind { boundValue.value = it }
        Assert.assertEquals(7, boundValue.value.toLong())
        bindable1.setValue(42)
        Assert.assertEquals(42, boundValue.value.toLong())
        val bindable2 = BindableInt(12)
        nested.setValue(bindable2)
        Assert.assertEquals(12, boundValue.value.toLong())
        bindable1.setValue(1)
        Assert.assertEquals(12, boundValue.value.toLong())
        binding.unbind()
        bindable2.setValue(10)
        Assert.assertEquals(12, boundValue.value.toLong())
        nested.setValue(BindableInt(27))
        Assert.assertEquals(12, boundValue.value.toLong())
    }

    @Test
    fun testMapReduceBinding() {
        val boundValue: BoundResult<Int> = BoundResult()
        val list = listOf(BindableInt(1), BindableInt(2), BindableInt(3))
        val bindings = list
                .map { b: BindableInt ->
                    propertyBinding(b, { it.value }, BindableValue.Property.VALUE)
                }
                .toList()
        val binding = mapReduceBinding(bindings, 0, { a: Int, v: Int -> a + v }, { a: Int, v: Int -> a - v })
        binding.bind { boundValue.value = it }
        Assert.assertEquals(6, boundValue.value.toLong())
        list[0].setValue(4)
        list[1].setValue(5)
        list[2].setValue(6)
        Assert.assertEquals(15, boundValue.value.toLong())
        binding.unbind()
        list[0].setValue(1)
        list[1].setValue(2)
        list[2].setValue(3)
        Assert.assertEquals(15, boundValue.value.toLong())
    }

    @Test
    fun testListBinding() {
        val boundValue: BoundResult<List<Int>> = BoundResult()
        val list = listOf(BindableInt(1), BindableInt(2), BindableInt(3))
        val bindings = list
            .map { b: BindableInt ->
                propertyBinding(b, { it.value }, BindableValue.Property.VALUE)
            }
            .toList()
        val binding = listBinding(bindings)
        binding.bind { boundValue.value = it }
        Assert.assertEquals(listOf(1, 2, 3), boundValue.value)
        list[0].setValue(4)
        list[1].setValue(5)
        list[2].setValue(6)
        Assert.assertEquals(listOf(4, 5, 6), boundValue.value)
        binding.unbind()
        list[0].setValue(1)
        list[1].setValue(2)
        list[2].setValue(3)
        Assert.assertEquals(listOf(4, 5, 6), boundValue.value)
    }

    @Test
    fun testEmptyMapReduceBinding() {
        val boundValue: BoundResult<Int> = BoundResult()
        val bindings: List<Binding<Int>> = listOf()
        val binding = mapReduceBinding(bindings, 1, { a, b -> a + b }, { a, b -> a - b })
        binding.bind { boundValue.value = it }
        Assert.assertEquals(1, boundValue.value.toLong())
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
