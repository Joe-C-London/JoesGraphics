package com.joecollins.graphics.components

import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import java.awt.Color
import java.util.ArrayList

class ListingFrameBuilder {
    private val barFrame = BarFrame()
    private val bindings: MutableList<Binding<*>> = ArrayList()

    fun withHeader(headerBinding: Binding<String?>): ListingFrameBuilder {
        barFrame.setHeaderBinding(headerBinding)
        return this
    }

    fun withSubhead(subheadBinding: Binding<String?>): ListingFrameBuilder {
        barFrame.setSubheadTextBinding(subheadBinding)
        return this
    }

    fun withNotes(notesBinding: Binding<String?>): ListingFrameBuilder {
        barFrame.setNotesBinding(notesBinding)
        return this
    }

    fun build(): BarFrame {
        return barFrame
    }

    companion object {
        @JvmStatic fun <T> of(
            list: BindableList<T>,
            leftTextFunc: (T) -> String,
            rightTextFunc: (T) -> String,
            colorFunc: (T) -> Color
        ): ListingFrameBuilder {
            val builder = ListingFrameBuilder()
            val barFrame = builder.barFrame
            barFrame.setNumBarsBinding(Binding.sizeBinding(list))
            barFrame.setLeftTextBinding(IndexedBinding.propertyBinding(list, leftTextFunc))
            barFrame.setRightTextBinding(IndexedBinding.propertyBinding(list, rightTextFunc))
            barFrame.addSeriesBinding(
                    "Item",
                    IndexedBinding.propertyBinding(list, colorFunc),
                    IndexedBinding.propertyBinding(list) { 1 })
            return builder
        }

        @JvmStatic fun <T> of(
            list: Binding<List<T>>,
            leftTextFunc: (T) -> String,
            rightTextFunc: (T) -> String,
            colorFunc: (T) -> Color
        ): ListingFrameBuilder {
            val bindableList = BindableList<T>()
            list.bind { bindableList.setAll(it) }
            val ret = of(bindableList, leftTextFunc, rightTextFunc, colorFunc)
            ret.bindings.add(list)
            return ret
        }

        @JvmStatic fun <T> ofFixedList(
            list: List<T>,
            leftTextFunc: (T) -> Binding<String>,
            rightTextFunc: (T) -> Binding<String>,
            colorFunc: (T) -> Binding<Color>
        ): ListingFrameBuilder {
            val builder = ListingFrameBuilder()
            val barFrame = builder.barFrame
            barFrame.setNumBarsBinding(Binding.fixedBinding(list.size))
            barFrame.setLeftTextBinding(IndexedBinding.listBinding(list, leftTextFunc))
            barFrame.setRightTextBinding(IndexedBinding.listBinding(list, rightTextFunc))
            barFrame.addSeriesBinding(
                    "Item",
                    IndexedBinding.listBinding(list, colorFunc),
                    IndexedBinding.listBinding(list) { Binding.fixedBinding(1) })
            return builder
        }
    }
}
