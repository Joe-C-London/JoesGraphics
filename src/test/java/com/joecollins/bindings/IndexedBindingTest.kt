package com.joecollins.bindings

import com.joecollins.bindings.Binding.Companion.propertyBinding
import com.joecollins.bindings.Binding.Companion.sizeBinding
import com.joecollins.bindings.IndexedBinding.Companion.emptyBinding
import com.joecollins.bindings.IndexedBinding.Companion.functionBinding
import com.joecollins.bindings.IndexedBinding.Companion.listBinding
import com.joecollins.bindings.IndexedBinding.Companion.propertyBinding
import com.joecollins.bindings.IndexedBinding.Companion.singletonBinding
import com.joecollins.bindings.IndexedBindingTest.BindableToList
import java.util.HashMap
import org.apache.commons.lang3.mutable.MutableInt
import org.junit.Assert
import org.junit.Test

class IndexedBindingTest {
    @Test
    fun indexedBindingBasicTest() {
        val valuesByIndex: MutableMap<Int, Int> = HashMap()
        val list = BindableList<Int>()
        list.add(7)
        val binding = propertyBinding(list)
        binding.bind { key: Int, value: Int -> valuesByIndex[key] = value }
        Assert.assertEquals(7, valuesByIndex[0]!!.toInt().toLong())
        list[0] = 42
        Assert.assertEquals(42, valuesByIndex[0]!!.toInt().toLong())
        binding.unbind()
        list[0] = 7
        Assert.assertEquals(42, valuesByIndex[0]!!.toInt().toLong())
    }

    @Test
    fun indexedBindingListTest() {
        val valuesByIndex: MutableMap<Int, Int> = HashMap()
        val size = MutableInt()
        val list = BindableList<Int>()
        list.add(7)
        val binding = propertyBinding(list)
        binding.bind { key: Int, value: Int -> valuesByIndex[key] = value }
        sizeBinding(list).bind { value: Int? -> size.setValue(value) }
        Assert.assertEquals(7, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(1, size.value.toInt().toLong())
        list.add(0, 42)
        Assert.assertEquals(42, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(7, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(2, size.value.toInt().toLong())
        list.add(17)
        Assert.assertEquals(42, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(7, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(17, valuesByIndex[2]!!.toInt().toLong())
        Assert.assertEquals(3, size.value.toInt().toLong())
        list.removeAt(0)
        Assert.assertEquals(7, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(17, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(2, size.value.toInt().toLong())
        list.addAll(listOf(20, 30))
        Assert.assertEquals(20, valuesByIndex[2]!!.toInt().toLong())
        Assert.assertEquals(30, valuesByIndex[3]!!.toInt().toLong())
        Assert.assertEquals(4, size.value.toInt().toLong())
        list.removeAll(listOf(7, 17))
        Assert.assertEquals(20, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(30, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(2, size.value.toInt().toLong())
        list.setAll(listOf(7, 17, 27))
        Assert.assertEquals(7, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(17, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(27, valuesByIndex[2]!!.toInt().toLong())
        Assert.assertEquals(3, size.value.toInt().toLong())
        list.setAll(listOf(17, 27))
        Assert.assertEquals(17, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(27, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(2, size.value.toInt().toLong())
    }

    @Test
    fun indexedBindingBindableTest() {
        val valuesByIndex: MutableMap<Int, Int> = HashMap()
        val list: NestedBindableList<BindableValue<Int>, BindableValue.Property> = NestedBindableList()
        val bindable1 = BindableInt(7)
        list.add(bindable1)
        val binding: IndexedBinding<Int> = propertyBinding(list, { it.value }, BindableValue.Property.VALUE)
        binding.bind { key: Int, value: Int -> valuesByIndex[key] = value }
        Assert.assertEquals(7, valuesByIndex[0]!!.toInt().toLong())
        bindable1.setValue(17)
        Assert.assertEquals(17, valuesByIndex[0]!!.toInt().toLong())
        val bindable2 = BindableInt(42)
        list[0] = bindable2
        Assert.assertEquals(42, valuesByIndex[0]!!.toInt().toLong())
        bindable1.setValue(7)
        Assert.assertEquals(42, valuesByIndex[0]!!.toInt().toLong())
        bindable2.setValue(7)
        Assert.assertEquals(7, valuesByIndex[0]!!.toInt().toLong())
        binding.unbind()
        bindable2.setValue(17)
        Assert.assertEquals(7, valuesByIndex[0]!!.toInt().toLong())
        list[0] = BindableInt(27)
        Assert.assertEquals(7, valuesByIndex[0]!!.toInt().toLong())
    }

    @Test
    fun testEmptyBinding() {
        val valuesByIndex: MutableMap<Int, Int> = HashMap()
        val binding = emptyBinding<Int>()
        binding.bind { key: Int, value: Int -> valuesByIndex[key] = value }
        Assert.assertTrue(valuesByIndex.isEmpty())
    }

    @Test
    fun testSingletonBinding() {
        val valuesByIndex: MutableMap<Int, Int> = HashMap()
        val binding = singletonBinding(7)
        binding.bind { key: Int, value: Int -> valuesByIndex[key] = value }
        Assert.assertEquals(1, valuesByIndex.size.toLong())
        Assert.assertEquals(7, valuesByIndex[0]!!.toInt().toLong())
    }

    @Test
    fun testListPropertyBinding() {
        val valuesByIndex: MutableMap<Int, Int> = HashMap()
        val bindable = BindableToList(1, 2, 3)
        val binding: IndexedBinding<Int> = propertyBinding(bindable, { it.value }, BindableValue.Property.VALUE)
        binding.bind { key: Int, value: Int -> valuesByIndex[key] = value }
        Assert.assertEquals(1, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(2, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(3, valuesByIndex[2]!!.toInt().toLong())
        bindable.setValue(4, 5, 6)
        Assert.assertEquals(4, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(5, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(6, valuesByIndex[2]!!.toInt().toLong())
        binding.unbind()
        bindable.setValue(7, 8, 9)
        Assert.assertEquals(4, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(5, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(6, valuesByIndex[2]!!.toInt().toLong())
    }

    @Test
    fun testListArrayBinding() {
        val valuesByIndex: MutableMap<Int, Int> = HashMap()
        val binding = listBinding(1, 2, 3)
        binding.bind { key: Int, value: Int -> valuesByIndex[key] = value }
        Assert.assertEquals(1, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(2, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(3, valuesByIndex[2]!!.toInt().toLong())
    }

    @Test
    fun testListListBinding() {
        val valuesByIndex: MutableMap<Int, Int> = HashMap()
        val binding = listBinding(listOf(1, 2, 3))
        binding.bind { key: Int, value: Int -> valuesByIndex[key] = value }
        Assert.assertEquals(1, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(2, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(3, valuesByIndex[2]!!.toInt().toLong())
    }

    @Test
    fun testListFuncBinding() {
        val valuesByIndex: MutableMap<Int, Int> = HashMap()
        val bindables = listOf(BindableInt(1), BindableInt(2), BindableInt(3))
        val binding = listBinding(bindables) { b: BindableInt -> propertyBinding<BindableValue<Int>, Int, BindableValue.Property>(b, { it.value }, BindableValue.Property.VALUE) }
        binding.bind { key: Int, value: Int -> valuesByIndex[key] = value }
        Assert.assertEquals(1, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(2, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(3, valuesByIndex[2]!!.toInt().toLong())
        bindables[0].setValue(4)
        bindables[1].setValue(5)
        bindables[2].setValue(6)
        Assert.assertEquals(4, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(5, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(6, valuesByIndex[2]!!.toInt().toLong())
        binding.unbind()
        bindables[0].setValue(1)
        bindables[1].setValue(2)
        bindables[2].setValue(3)
        Assert.assertEquals(4, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(5, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(6, valuesByIndex[2]!!.toInt().toLong())
    }

    @Test
    fun testRangeBinding() {
        val valuesByIndex: MutableMap<Int, Int> = HashMap()
        val binding = functionBinding(1, 5) { i: Int -> i * i }
        binding.bind { key: Int, value: Int -> valuesByIndex[key] = value }
        Assert.assertEquals(1, valuesByIndex[0]!!.toInt().toLong())
        Assert.assertEquals(4, valuesByIndex[1]!!.toInt().toLong())
        Assert.assertEquals(9, valuesByIndex[2]!!.toInt().toLong())
        Assert.assertEquals(16, valuesByIndex[3]!!.toInt().toLong())
        Assert.assertNull(valuesByIndex[4])
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

    private class BindableToList<T>(vararg items: T) : BindableValue<List<T>>(items.toList()) {
        fun setValue(vararg items: T) {
            setValue(items.toList())
        }
    }
}
