package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
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
        @JvmStatic
        fun <T> of(
            list: Binding<List<T>>,
            leftTextFunc: (T) -> String,
            rightTextFunc: (T) -> String,
            colorFunc: (T) -> Color
        ): ListingFrameBuilder {
            val builder = ListingFrameBuilder()
            val barFrame = builder.barFrame
            barFrame.setBarsBinding(list.map { l ->
                l.map {
                    BarFrame.Bar(leftTextFunc(it), rightTextFunc(it), null, listOf(Pair(colorFunc(it), 1)))
                }
            })
            builder.bindings.add(list)
            return builder
        }

        @JvmStatic fun <T> ofFixedList(
            list: List<T>,
            leftTextFunc: (T) -> Binding<String>,
            rightTextFunc: (T) -> Binding<String>,
            colorFunc: (T) -> Binding<Color>
        ): ListingFrameBuilder {
            val builder = ListingFrameBuilder()
            val barFrame = builder.barFrame
            barFrame.setBarsBinding(Binding.listBinding(
                list.map {
                    leftTextFunc(it).merge(rightTextFunc(it)) { left, right -> Pair(left, right) }
                        .merge(colorFunc(it)) {
                            text, color -> BarFrame.Bar(text.first, text.second, listOf(Pair(color, 1)))
                        }
                }
            ))
            return builder
        }
    }
}
