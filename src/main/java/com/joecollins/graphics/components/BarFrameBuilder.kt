package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.utils.ColorUtils
import java.awt.Color
import java.awt.Shape
import java.util.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class BarFrameBuilder {
    private val barFrame = BarFrame()
    private val rangeFinder = RangeFinder()
    private val bindings: MutableList<Binding<*>> = ArrayList()

    private class RangeFinder : Bindable<RangeFinder, RangeFinder.Property>() {
        enum class Property {
            MIN, MAX
        }

        private var _highest: Number = 0
        private var _lowest: Number = 0
        private var _minFunction = { rf: RangeFinder -> rf.lowest }
        private var _maxFunction = { rf: RangeFinder -> rf.highest }

        var highest: Number
        get() { return _highest }
        set(highest) {
            _highest = highest
            onPropertyRefreshed(Property.MIN)
            onPropertyRefreshed(Property.MAX)
        }

        var lowest: Number
        get() { return _lowest }
        set(lowest) {
            _lowest = lowest
            onPropertyRefreshed(Property.MIN)
            onPropertyRefreshed(Property.MAX)
        }

        var minFunction: (RangeFinder) -> Number
        get() { return _minFunction }
        set(minFunction) {
            _minFunction = minFunction
            onPropertyRefreshed(Property.MIN)
        }

        var maxFunction: (RangeFinder) -> Number
        get() { return _maxFunction }
        set(maxFunction) {
            _maxFunction = maxFunction
            onPropertyRefreshed(Property.MAX)
        }
    }

    class BasicBar @JvmOverloads constructor(val label: String, val color: Color, val value: Number, val valueLabel: String = value.toString(), val shape: Shape? = null) {
        constructor(label: String, color: Color, value: Number, shape: Shape?) : this(label, color, value, value.toString(), shape)
    }

    class DualBar @JvmOverloads constructor(
        val label: String,
        val color: Color,
        val value1: Number,
        val value2: Number,
        val valueLabel: String,
        val shape: Shape? = null
    )

    fun withHeader(headerBinding: Binding<String?>): BarFrameBuilder {
        barFrame.setHeaderBinding(headerBinding)
        return this
    }

    fun withSubhead(subheadBinding: Binding<String?>): BarFrameBuilder {
        barFrame.setSubheadTextBinding(subheadBinding)
        return this
    }

    fun withNotes(notesBinding: Binding<String?>): BarFrameBuilder {
        barFrame.setNotesBinding(notesBinding)
        return this
    }

    fun withBorder(borderColorBinding: Binding<Color>): BarFrameBuilder {
        barFrame.setBorderColorBinding(borderColorBinding)
        return this
    }

    fun withSubheadColor(subheadColorBinding: Binding<Color>): BarFrameBuilder {
        barFrame.setSubheadColorBinding(subheadColorBinding)
        return this
    }

    fun withMax(maxBinding: Binding<Number>): BarFrameBuilder {
        rangeFinder.minFunction = { 0 }
        bind(maxBinding) { max -> rangeFinder.maxFunction = { max(max.toDouble(), it.highest.toDouble()) } }
        return this
    }

    fun withWingspan(wingspanBinding: Binding<Number>): BarFrameBuilder {
        bind(wingspanBinding) { wingspan ->
            val f = { it: RangeFinder ->
                max(
                        wingspan.toDouble(),
                        max(abs(it.lowest.toDouble()), abs(it.highest.toDouble())))
            }
            rangeFinder.minFunction = { -f(it) }
            rangeFinder.maxFunction = { +f(it) }
        }
        return this
    }

    fun <T : Number> withTarget(
        targetBinding: Binding<T>,
        labelFunc: (T) -> String
    ): BarFrameBuilder {
        bind(targetBinding) {
            barFrame.setNumLinesBinding(Binding.fixedBinding(1))
            barFrame.setLineLevelsBinding(IndexedBinding.listBinding(it))
            barFrame.setLineLabelsBinding(IndexedBinding.listBinding(labelFunc(it)))
        }
        return this
    }

    fun <T : Number> withLines(
        lines: BindableList<T>,
        labelFunc: (T) -> String
    ): BarFrameBuilder {
        barFrame.setNumLinesBinding(Binding.sizeBinding(lines))
        barFrame.setLineLevelsBinding(IndexedBinding.propertyBinding(lines))
        barFrame.setLineLabelsBinding(IndexedBinding.propertyBinding(lines, labelFunc))
        return this
    }

    fun <T> withLines(
        lines: BindableList<T>,
        labelFunc: (T) -> String,
        valueFunc: (T) -> Number
    ): BarFrameBuilder {
        barFrame.setNumLinesBinding(Binding.sizeBinding(lines))
        barFrame.setLineLevelsBinding(IndexedBinding.propertyBinding(lines, valueFunc))
        barFrame.setLineLabelsBinding(IndexedBinding.propertyBinding(lines, labelFunc))
        return this
    }

    fun <T : Number> withLines(
        linesBinding: Binding<List<T>>,
        labelFunc: (T) -> String
    ): BarFrameBuilder {
        bind(linesBinding) {
            barFrame.setNumLinesBinding(Binding.fixedBinding(it.size))
            barFrame.setLineLevelsBinding(IndexedBinding.listBinding(*it.toTypedArray<Number>()))
            barFrame.setLineLabelsBinding(
                    IndexedBinding.listBinding(*it.map(labelFunc).toTypedArray()))
        }
        return this
    }

    private fun <T> bind(binding: Binding<T>, onUpdate: (T) -> Unit) {
        binding.bind(onUpdate)
        bindings.add(binding)
    }

    fun build(): BarFrame {
        return barFrame
    }

    companion object {
        @JvmStatic fun basic(binding: Binding<List<BasicBar>>): BarFrameBuilder {
            val builder = BarFrameBuilder()
            val barFrame = builder.barFrame
            val rangeFinder = builder.rangeFinder
            val entries = BindableList<BasicBar>()
            barFrame.setNumBarsBinding(Binding.sizeBinding(entries))
            barFrame.setLeftTextBinding(IndexedBinding.propertyBinding(entries) { it.label })
            barFrame.setRightTextBinding(IndexedBinding.propertyBinding(entries) { it.valueLabel })
            barFrame.setLeftIconBinding(IndexedBinding.propertyBinding(entries) { it.shape })
            barFrame.setMinBinding(
                    Binding.propertyBinding(
                            rangeFinder, { rf: RangeFinder -> rf.minFunction(rf) }, RangeFinder.Property.MIN))
            barFrame.setMaxBinding(
                    Binding.propertyBinding(
                            rangeFinder, { rf: RangeFinder -> rf.maxFunction(rf) }, RangeFinder.Property.MAX))
            barFrame.addSeriesBinding(
                    "Main",
                    IndexedBinding.propertyBinding(entries) { it.color },
                    IndexedBinding.propertyBinding(entries) { it.value })
            builder.bind(binding) { map ->
                entries.setAll(map)
                rangeFinder.highest = map
                        .map { it.value }
                        .map { it.toDouble() }
                        .fold(0.0) { a, b -> max(a, b) }
                rangeFinder.lowest = map
                        .map { it.value }
                        .map { it.toDouble() }
                        .fold(0.0) { a, b -> min(a, b) }
            }
            return builder
        }

        @JvmStatic fun dual(bars: Binding<List<DualBar>>): BarFrameBuilder {
            val builder = BarFrameBuilder()
            val barFrame = builder.barFrame
            val rangeFinder = builder.rangeFinder
            val differentDirections = { bar: DualBar -> sign(bar.value1.toDouble()) * sign(bar.value2.toDouble()) == -1.0 }
            val reverse = { bar: DualBar ->
                (differentDirections(bar) ||
                        abs(bar.value1.toDouble()) < abs(bar.value2.toDouble()))
            }
            val first = { bar: DualBar -> if (reverse(bar)) bar.value1 else bar.value2 }
            val second = { bar: DualBar -> if (reverse(bar)) bar.value2 else bar.value1 }
            val entries = BindableList<DualBar>()
            barFrame.setNumBarsBinding(Binding.sizeBinding(entries))
            barFrame.setLeftTextBinding(IndexedBinding.propertyBinding(entries) { e: DualBar -> e.label })
            barFrame.setRightTextBinding(IndexedBinding.propertyBinding(entries) { e: DualBar -> e.valueLabel })
            barFrame.setMinBinding(
                    Binding.propertyBinding(
                            rangeFinder, { it.minFunction(it) }, RangeFinder.Property.MIN))
            barFrame.setMaxBinding(
                    Binding.propertyBinding(
                            rangeFinder, { it.maxFunction(it) }, RangeFinder.Property.MAX))
            barFrame.addSeriesBinding(
                    "Placeholder",
                    IndexedBinding.propertyBinding(entries) { it.color },
                    IndexedBinding.propertyBinding(entries) { 0 })
            barFrame.addSeriesBinding(
                    "First",
                    IndexedBinding.propertyBinding(
                            entries
                    ) {
                        if (differentDirections(it)) {
                            ColorUtils.lighten(it.color)
                        } else {
                            it.color
                        }
                    },
                    IndexedBinding.propertyBinding(entries, first))
            barFrame.addSeriesBinding(
                    "Second",
                    IndexedBinding.propertyBinding(entries) { ColorUtils.lighten(it.color) },
                    IndexedBinding.propertyBinding(
                            entries
                    ) {
                        (second.invoke(it).toDouble() - if (differentDirections(it)) 0.0 else first.invoke(it).toDouble())
                    })
            barFrame.setLeftIconBinding(IndexedBinding.propertyBinding(entries) { e: DualBar -> e.shape })
            builder.bind(bars) { map ->
                entries.setAll(map)
                rangeFinder.highest = map
                        .flatMap { sequenceOf(it.value1, it.value2) }
                        .map { it.toDouble() }
                        .fold(0.0) { a, b -> max(a, b) }
                rangeFinder.lowest = map
                        .flatMap { sequenceOf(it.value1, it.value2) }
                        .map { it.toDouble() }
                        .fold(0.0) { a, b -> min(a, b) }
            }
            return builder
        }

        @JvmStatic fun dualReversed(bars: Binding<List<DualBar>>): BarFrameBuilder {
            val builder = BarFrameBuilder()
            val barFrame = builder.barFrame
            val rangeFinder = builder.rangeFinder
            val differentDirections = { bar: DualBar -> sign(bar.value1.toDouble()) * sign(bar.value2.toDouble()) == -1.0 }
            val reverse = { bar: DualBar ->
                (differentDirections(bar) ||
                        abs(bar.value1.toDouble()) < abs(bar.value2.toDouble()))
            }
            val first = { bar: DualBar -> if (reverse(bar)) bar.value1 else bar.value2 }
            val second = { bar: DualBar -> if (reverse(bar)) bar.value2 else bar.value1 }
            val entries = BindableList<DualBar>()
            barFrame.setNumBarsBinding(Binding.sizeBinding(entries))
            barFrame.setLeftTextBinding(IndexedBinding.propertyBinding(entries) { e: DualBar -> e.label })
            barFrame.setRightTextBinding(IndexedBinding.propertyBinding(entries) { e: DualBar -> e.valueLabel })
            barFrame.setMinBinding(
                    Binding.propertyBinding(
                            rangeFinder, { it.minFunction(it) }, RangeFinder.Property.MIN))
            barFrame.setMaxBinding(
                    Binding.propertyBinding(
                            rangeFinder, { it.maxFunction(it) }, RangeFinder.Property.MAX))
            barFrame.addSeriesBinding(
                    "Placeholder",
                    IndexedBinding.propertyBinding(entries) { it.color },
                    IndexedBinding.propertyBinding(entries) { 0 })
            barFrame.addSeriesBinding(
                    "First",
                    IndexedBinding.propertyBinding(entries) { ColorUtils.lighten(it.color) },
                    IndexedBinding.propertyBinding(entries, first))
            barFrame.addSeriesBinding(
                    "Second",
                    IndexedBinding.propertyBinding(
                            entries
                    ) {
                        if (differentDirections(it)) {
                            ColorUtils.lighten(it.color)
                        } else {
                            it.color
                        }
                    },
                    IndexedBinding.propertyBinding(
                            entries
                    ) {
                        (second.invoke(it).toDouble() -
                                if (differentDirections(it)) 0.0 else first.invoke(it).toDouble())
                    })
            barFrame.setLeftIconBinding(IndexedBinding.propertyBinding(entries) { e: DualBar -> e.shape })
            builder.bind(bars) { map ->
                entries.setAll(map)
                rangeFinder.highest = map
                        .flatMap { sequenceOf(it.value1, it.value2) }
                        .map { it.toDouble() }
                        .fold(0.0) { a: Double, b: Double -> max(a, b) }
                rangeFinder.lowest = map
                        .flatMap { sequenceOf(it.value1, it.value2) }
                        .map { it.toDouble() }
                        .fold(0.0) { a, b -> min(a, b) }
            }
            return builder
        }
    }
}
