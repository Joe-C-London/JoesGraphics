package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import java.awt.Color

class ListingFrameBuilder {

    private var headerBinding: Binding<String?>? = null
    private var subheadTextBinding: Binding<String?>? = null
    private var notesBinding: Binding<String?>? = null
    private var barsBinding: Binding<List<BarFrame.Bar>>? = null

    fun withHeader(headerBinding: Binding<String?>): ListingFrameBuilder {
        this.headerBinding = headerBinding
        return this
    }

    fun withSubhead(subheadBinding: Binding<String?>): ListingFrameBuilder {
        this.subheadTextBinding = subheadBinding
        return this
    }

    fun withNotes(notesBinding: Binding<String?>): ListingFrameBuilder {
        this.notesBinding = notesBinding
        return this
    }

    fun build(): BarFrame {
        return BarFrame(
            headerBinding = headerBinding ?: Binding.fixedBinding(null),
            subheadTextBinding = subheadTextBinding,
            notesBinding = notesBinding,
            barsBinding = barsBinding ?: Binding.fixedBinding(emptyList())
        )
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
            builder.barsBinding = list.map { l ->
                l.map {
                    BarFrame.Bar(leftTextFunc(it), rightTextFunc(it), null, listOf(Pair(colorFunc(it), 1)))
                }
            }
            return builder
        }

        @JvmStatic fun <T> ofFixedList(
            list: List<T>,
            leftTextFunc: (T) -> Binding<String>,
            rightTextFunc: (T) -> Binding<String>,
            colorFunc: (T) -> Binding<Color>
        ): ListingFrameBuilder {
            val builder = ListingFrameBuilder()
            builder.barsBinding = Binding.listBinding(
                list.map {
                    leftTextFunc(it).merge(rightTextFunc(it)) { left, right -> Pair(left, right) }
                        .merge(colorFunc(it)) {
                            (left, right), color ->
                            BarFrame.Bar(left, right, listOf(Pair(color, 1)))
                        }
                }
            )
            return builder
        }
    }
}
