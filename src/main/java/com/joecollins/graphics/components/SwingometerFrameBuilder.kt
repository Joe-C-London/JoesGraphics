package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.bindings.IndexedBinding
import java.awt.Color
import java.util.ArrayList
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import org.apache.commons.lang3.tuple.Pair

class SwingometerFrameBuilder {
    private class Properties : Bindable<Properties, Properties.Property>() {
        enum class Property {
            VALUE
        }

        private var _value: Number = 0
        private var _max: Number = 1
        private var _bucketSize: Number = 1
        private var _tickInterval: Number = 1

        var value: Number
        get() { return _value }
        set(value) {
            _value = if (java.lang.Double.isNaN(value.toDouble())) 0.0 else value
            onPropertyRefreshed(Property.VALUE)
        }

        var max: Number
        get() { return _max }
        set(max) {
            this._max = max
            onPropertyRefreshed(Property.VALUE)
        }

        var bucketSize: Number
        get() { return _bucketSize }
        set(bucketSize) {
            this._bucketSize = bucketSize
            onPropertyRefreshed(Property.VALUE)
        }

        var tickInterval: Number
        get() { return _tickInterval }
        set(tickInterval) {
            this._tickInterval = tickInterval
            onPropertyRefreshed(Property.VALUE)
        }
    }

    private val frame = SwingometerFrame()
    private val properties = Properties()
    fun withRange(range: Binding<Number>): SwingometerFrameBuilder {
        range.bind { max -> properties.max = max }
        frame.setRangeBinding(
                Binding.propertyBinding(properties, { getMax(it) }, Properties.Property.VALUE))
        return this
    }

    private fun getMax(props: Properties): Double {
        return max(
                props.max.toDouble(), props.bucketSize.toDouble() *
                ceil(abs(props.value.toDouble() / props.bucketSize.toDouble())))
    }

    fun withBucketSize(bucketSize: Binding<Number>): SwingometerFrameBuilder {
        bucketSize.bind { properties.bucketSize = it }
        frame.setNumBucketsPerSideBinding(
                Binding.propertyBinding(
                        properties,
                        { (getMax(it) / it.bucketSize.toDouble()).roundToInt() },
                        Properties.Property.VALUE))
        return this
    }

    private inner class Tick(val level: Double, val text: String)

    fun withTickInterval(
        tickInterval: Binding<Number>,
        tickStringFunc: (Number) -> String
    ): SwingometerFrameBuilder {
        val ticks = BindableList<Tick>()
        tickInterval.bind { properties.tickInterval = it }
        Binding.propertyBinding(properties, { getTicks(it, tickStringFunc) }, Properties.Property.VALUE)
                .bind { ticks.setAll(it) }
        frame.setNumTicksBinding(Binding.sizeBinding(ticks))
        frame.setTickPositionBinding(IndexedBinding.propertyBinding(ticks) { it.level })
        frame.setTickTextBinding(IndexedBinding.propertyBinding(ticks) { it.text })
        return this
    }

    private fun getTicks(props: Properties, tickStringFunc: (Number) -> String): List<Tick> {
        val ticks = ArrayList<Tick>()
        val max = getMax(props)
        ticks.add(Tick(0.0, tickStringFunc(0)))
        var i = props.tickInterval.toDouble()
        while (i < max) {
            ticks.add(Tick(i, tickStringFunc(i)))
            ticks.add(Tick(-i, tickStringFunc(i)))
            i += props.tickInterval.toDouble()
        }
        return ticks
    }

    fun withLeftNeedingToWin(leftToWin: Binding<Number>): SwingometerFrameBuilder {
        frame.setLeftToWinBinding(leftToWin)
        return this
    }

    fun withRightNeedingToWin(rightToWin: Binding<Number>): SwingometerFrameBuilder {
        frame.setRightToWinBinding(rightToWin)
        return this
    }

    fun <T> withOuterLabels(
        labels: BindableList<T>,
        positionFunc: (T) -> Number,
        labelFunc: (T) -> String,
        colorFunc: (T) -> Color
    ): SwingometerFrameBuilder {
        frame.setNumOuterLabelsBinding(Binding.sizeBinding(labels))
        frame.setOuterLabelPositionBinding(IndexedBinding.propertyBinding(labels, positionFunc))
        frame.setOuterLabelTextBinding(IndexedBinding.propertyBinding(labels, labelFunc))
        frame.setOuterLabelColorBinding(IndexedBinding.propertyBinding(labels, colorFunc))
        return this
    }

    fun <T> withDots(
        dots: BindableList<T>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Color
    ): SwingometerFrameBuilder {
        return withDots(dots, positionFunc, colorFunc, { "" })
    }

    fun <T> withDots(
        dots: BindableList<T>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Color,
        labelFunc: (T) -> String
    ): SwingometerFrameBuilder {
        return withDots(dots, positionFunc, colorFunc, labelFunc, { true })
    }

    fun <T> withDotsSolid(
        dots: BindableList<T>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Color,
        solidFunc: (T) -> Boolean
    ): SwingometerFrameBuilder {
        return withDots(dots, positionFunc, colorFunc, { "" }, solidFunc)
    }

    fun <T> withDots(
        dots: BindableList<T>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Color,
        labelFunc: (T) -> String,
        solidFunc: (T) -> Boolean
    ): SwingometerFrameBuilder {
        frame.setNumDotsBinding(Binding.sizeBinding(dots))
        frame.setDotsPositionBinding(IndexedBinding.propertyBinding(dots, positionFunc))
        frame.setDotsColorBinding(IndexedBinding.propertyBinding(dots, colorFunc))
        frame.setDotsLabelBinding(IndexedBinding.propertyBinding(dots, labelFunc))
        frame.setDotsSolidBinding(IndexedBinding.propertyBinding(dots, solidFunc))
        return this
    }

    fun <T> withFixedDots(
        dots: List<T>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Binding<Color>
    ): SwingometerFrameBuilder {
        return withFixedDots(dots, positionFunc, colorFunc, { "" })
    }

    fun <T> withFixedDots(
        dots: List<T>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Binding<Color>,
        labelFunc: (T) -> String
    ): SwingometerFrameBuilder {
        return withFixedDots(dots, positionFunc, colorFunc, labelFunc, { true })
    }

    fun <T> withFixedDotsSolid(
        dots: List<T>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Binding<Color>,
        solidFunc: (T) -> Boolean
    ): SwingometerFrameBuilder {
        return withFixedDots(dots, positionFunc, colorFunc, { "" }, solidFunc)
    }

    fun <T> withFixedDots(
        dots: List<T>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Binding<Color>,
        labelFunc: (T) -> String,
        solidFunc: (T) -> Boolean
    ): SwingometerFrameBuilder {
        frame.setNumDotsBinding(Binding.fixedBinding(dots.size))
        frame.setDotsPositionBinding(
                IndexedBinding.listBinding(dots) { Binding.fixedBinding(positionFunc(it)) })
        frame.setDotsColorBinding(IndexedBinding.listBinding(dots, colorFunc))
        frame.setDotsLabelBinding(IndexedBinding.listBinding(dots) { Binding.fixedBinding(labelFunc(it)) })
        frame.setDotsSolidBinding(IndexedBinding.listBinding(dots) { Binding.fixedBinding(solidFunc(it)) })
        return this
    }

    fun withHeader(header: Binding<String?>): SwingometerFrameBuilder {
        frame.setHeaderBinding(header)
        return this
    }

    fun build(): SwingometerFrame {
        return frame
    }

    companion object {
        @JvmStatic fun basic(
            colors: Binding<Pair<Color, Color>>,
            value: Binding<Number>
        ): SwingometerFrameBuilder {
            val colorsRec: BindingReceiver<Pair<Color, Color>> = BindingReceiver(colors)
            val builder = SwingometerFrameBuilder()
            value.bind { builder.properties.value = it }
            builder.frame.setLeftColorBinding(colorsRec.getBinding { it.left })
            builder.frame.setRightColorBinding(colorsRec.getBinding { it.right })
            builder.frame.setValueBinding(
                    Binding.propertyBinding(
                            builder.properties, { props: Properties -> props.value }, Properties.Property.VALUE))
            return builder
        }
    }
}
