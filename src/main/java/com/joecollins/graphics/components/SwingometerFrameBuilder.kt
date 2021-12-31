package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.bindings.mapElements
import com.joecollins.pubsub.asOneTimePublisher
import java.awt.Color
import java.util.ArrayList
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

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

    private var rangeBinding: Binding<Double>? = null
    private var numBucketsPerSideBinding: Binding<Int>? = null
    private var ticksBinding: Binding<List<SwingometerFrame.Tick>>? = null
    private var leftToWinBinding: Binding<Number>? = null
    private var rightToWinBinding: Binding<Number>? = null
    private var outerLabelsBinding: Binding<List<SwingometerFrame.OuterLabel>>? = null
    private var dotsBinding: Binding<List<SwingometerFrame.Dot>>? = null
    private var headerBinding: Binding<String?>? = null
    private var leftColorBinding: Binding<Color>? = null
    private var rightColorBinding: Binding<Color>? = null
    private var valueBinding: Binding<Number>? = null

    private val properties = Properties()

    fun withRange(range: Binding<Number>): SwingometerFrameBuilder {
        range.bind { max -> properties.max = max }
        this.rangeBinding = Binding.propertyBinding(properties, { getMax(it) }, Properties.Property.VALUE)
        return this
    }

    private fun getMax(props: Properties): Double {
        return max(
            props.max.toDouble(),
            props.bucketSize.toDouble() *
                ceil(abs(props.value.toDouble() / props.bucketSize.toDouble()))
        )
    }

    fun withBucketSize(bucketSize: Binding<Number>): SwingometerFrameBuilder {
        bucketSize.bind { properties.bucketSize = it }
        this.numBucketsPerSideBinding = Binding.propertyBinding(
            properties,
            { (getMax(it) / it.bucketSize.toDouble()).roundToInt() },
            Properties.Property.VALUE
        )
        return this
    }

    private inner class Tick(val level: Double, val text: String)

    fun withTickInterval(
        tickInterval: Binding<Number>,
        tickStringFunc: (Number) -> String
    ): SwingometerFrameBuilder {
        tickInterval.bind { properties.tickInterval = it }
        val ticks = Binding.propertyBinding(properties, { getTicks(it, tickStringFunc) }, Properties.Property.VALUE)
        this.ticksBinding = ticks.mapElements {
            SwingometerFrame.Tick(it.level, it.text)
        }
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
        this.leftToWinBinding = leftToWin
        return this
    }

    fun withRightNeedingToWin(rightToWin: Binding<Number>): SwingometerFrameBuilder {
        this.rightToWinBinding = rightToWin
        return this
    }

    fun <T> withOuterLabels(
        labels: Binding<List<T>>,
        positionFunc: (T) -> Number,
        labelFunc: (T) -> String,
        colorFunc: (T) -> Color
    ): SwingometerFrameBuilder {
        this.outerLabelsBinding = labels.mapElements {
            SwingometerFrame.OuterLabel(positionFunc(it), labelFunc(it), colorFunc(it))
        }
        return this
    }

    fun <T> withDots(
        dots: Binding<List<T>>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Color
    ): SwingometerFrameBuilder {
        return withDots(dots, positionFunc, colorFunc, { "" })
    }

    fun <T> withDots(
        dots: Binding<List<T>>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Color,
        labelFunc: (T) -> String
    ): SwingometerFrameBuilder {
        return withDots(dots, positionFunc, colorFunc, labelFunc, { true })
    }

    fun <T> withDotsSolid(
        dots: Binding<List<T>>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Color,
        solidFunc: (T) -> Boolean
    ): SwingometerFrameBuilder {
        return withDots(dots, positionFunc, colorFunc, { "" }, solidFunc)
    }

    fun <T> withDots(
        dots: Binding<List<T>>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Color,
        labelFunc: (T) -> String,
        solidFunc: (T) -> Boolean
    ): SwingometerFrameBuilder {
        this.dotsBinding =
            dots.mapElements { SwingometerFrame.Dot(positionFunc(it), colorFunc(it), labelFunc(it), solidFunc(it)) }
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
        this.dotsBinding = Binding.listBinding(
            dots.map {
                colorFunc(it).map { c -> SwingometerFrame.Dot(positionFunc(it), c, labelFunc(it), solidFunc(it)) }
            }
        )
        return this
    }

    fun withHeader(header: Binding<String?>): SwingometerFrameBuilder {
        this.headerBinding = header
        return this
    }

    fun build(): SwingometerFrame {
        return SwingometerFrame(
            headerPublisher = headerBinding?.toPublisher() ?: (null as String?).asOneTimePublisher(),
            rangePublisher = rangeBinding?.toPublisher() ?: 1.asOneTimePublisher(),
            valuePublisher = valueBinding?.toPublisher() ?: 0.asOneTimePublisher(),
            leftColorPublisher = leftColorBinding?.toPublisher() ?: Color.BLACK.asOneTimePublisher(),
            rightColorPublisher = rightColorBinding?.toPublisher() ?: Color.BLACK.asOneTimePublisher(),
            numBucketsPerSidePublisher = numBucketsPerSideBinding?.toPublisher() ?: 1.asOneTimePublisher(),
            dotsPublisher = dotsBinding?.toPublisher() ?: emptyList<SwingometerFrame.Dot>().asOneTimePublisher(),
            leftToWinPublisher = leftToWinBinding?.toPublisher(),
            rightToWinPublisher = rightToWinBinding?.toPublisher(),
            ticksPublisher = ticksBinding?.toPublisher(),
            outerLabelsPublisher = outerLabelsBinding?.toPublisher()
        )
    }

    companion object {
        @JvmStatic fun basic(
            colors: Binding<Pair<Color, Color>>,
            value: Binding<Number>
        ): SwingometerFrameBuilder {
            val colorsRec: BindingReceiver<Pair<Color, Color>> = BindingReceiver(colors)
            val builder = SwingometerFrameBuilder()
            value.bind { builder.properties.value = it }
            builder.leftColorBinding = colorsRec.getBinding { it.first }
            builder.rightColorBinding = colorsRec.getBinding { it.second }
            builder.valueBinding = Binding.propertyBinding(
                builder.properties, { props: Properties -> props.value }, Properties.Property.VALUE
            )
            return builder
        }
    }
}
