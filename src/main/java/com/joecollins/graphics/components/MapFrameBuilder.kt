package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Rectangle2D

class MapFrameBuilder {

    private var focusBoxBinding: Binding<Rectangle2D?>? = null
    private var headerBinding: Binding<String?>? = null
    private var notesBinding: Binding<String?>? = null
    private var borderColorBinding: Binding<Color>? = null
    private var outlineBinding: Binding<List<Shape>>? = null
    private var shapesBinding: Binding<List<Pair<Shape, Color>>>? = null

    fun withFocus(focusBinding: Binding<List<Shape>?>): MapFrameBuilder {
        this.focusBoxBinding = focusBinding.map { shapes ->
            shapes
                ?.map { it.bounds2D }
                ?.reduceOrNull { a, b ->
                    val ret = Rectangle2D.Double(a.x, a.y, a.width, a.height)
                    ret.add(b)
                    ret
                }
        }
        return this
    }

    fun withHeader(headerBinding: Binding<String?>): MapFrameBuilder {
        this.headerBinding = headerBinding
        return this
    }

    fun withNotes(notesBinding: Binding<String?>): MapFrameBuilder {
        this.notesBinding = notesBinding
        return this
    }

    fun withBorderColor(borderColorBinding: Binding<Color>): MapFrameBuilder {
        this.borderColorBinding = borderColorBinding
        return this
    }

    fun withOutline(outlineBinding: Binding<List<Shape>>): MapFrameBuilder {
        this.outlineBinding = outlineBinding
        return this
    }

    fun build(): MapFrame {
        return MapFrame(
            headerBinding = headerBinding ?: Binding.fixedBinding(null),
            shapesBinding = shapesBinding ?: Binding.fixedBinding(emptyList()),
            focusBoxBinding = focusBoxBinding,
            notesBinding = notesBinding,
            borderColorBinding = borderColorBinding,
            outlineShapesBinding = outlineBinding
        )
    }

    companion object {
        @JvmStatic fun from(shapes: Binding<List<Pair<Shape, Color>>>): MapFrameBuilder {
            val mapFrameBuilder = MapFrameBuilder()
            mapFrameBuilder.shapesBinding = shapes
            return mapFrameBuilder
        }

        @JvmStatic fun <T> from(
            itemsBinding: Binding<List<T>>,
            shapeFunc: (T) -> Shape,
            colorFunc: (T) -> Binding<Color>
        ): MapFrameBuilder {
            val itemsReceiver = BindingReceiver(itemsBinding)
            val list = itemsReceiver.getFlatBinding { items ->
                Binding.listBinding(
                    items.map { colorFunc(it).map { c -> Pair(shapeFunc(it), c) } }
                )
            }
            val ret = from(list)
            return ret
        }
    }
}
