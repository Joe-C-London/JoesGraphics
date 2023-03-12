package com.joecollins.graphics.components

import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import java.awt.Color
import java.util.concurrent.Flow
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

class SwingometerFrameBuilder {
    private inner class Properties {

        var value: Number = 0
            set(value) {
                field = if (value.toDouble().isNaN()) 0.0 else value
                update()
            }

        val valuePublisher = Publisher(value)

        var max: Number = 1
            set(value) {
                field = value
                update()
            }

        var bucketSize: Number = 1
            set(value) {
                field = value
                update()
            }

        var tickInterval: Number = 1
            set(value) {
                field = value
                update()
            }

        var tickStringFunc: (Number) -> String = { it.toString() }
            set(value) {
                field = value
                update()
            }

        private fun update() {
            synchronized(this) {
                valuePublisher.submit(value)
                maxPublisher.submit(getMax())
                numBucketsPerSidePublisher.submit(getNumBucketsPerSide())
                ticksPublisher.submit(getTicks())
            }
        }

        fun getMax(): Double {
            return max(
                max.toDouble(),
                bucketSize.toDouble() *
                    ceil(abs(value.toDouble() / bucketSize.toDouble())),
            )
        }
        val maxPublisher = Publisher(getMax())

        fun getNumBucketsPerSide(): Int {
            return (getMax() / bucketSize.toDouble()).roundToInt()
        }
        val numBucketsPerSidePublisher = Publisher(getNumBucketsPerSide())

        private fun getTicks(): List<Tick> {
            val ticks = ArrayList<Tick>()
            val max = getMax()
            ticks.add(Tick(0.0, tickStringFunc(0)))
            var i = tickInterval.toDouble()
            while (i < max) {
                ticks.add(Tick(i, tickStringFunc(i)))
                ticks.add(Tick(-i, tickStringFunc(i)))
                i += tickInterval.toDouble()
            }
            return ticks
        }

        val ticksPublisher = Publisher(getTicks())
    }

    private var rangePublisher: Flow.Publisher<out Double>? = null
    private var numBucketsPerSidePublisher: Flow.Publisher<out Int>? = null
    private var ticksPublisher: Flow.Publisher<out List<SwingometerFrame.Tick>>? = null
    private var leftToWinPublisher: Flow.Publisher<out Number>? = null
    private var rightToWinPublisher: Flow.Publisher<out Number>? = null
    private var outerLabelsPublisher: Flow.Publisher<out List<SwingometerFrame.OuterLabel>>? = null
    private var dotsPublisher: Flow.Publisher<out List<SwingometerFrame.Dot>>? = null
    private var headerPublisher: Flow.Publisher<out String?>? = null
    private var leftColorPublisher: Flow.Publisher<out Color>? = null
    private var rightColorPublisher: Flow.Publisher<out Color>? = null
    private var valuePublisher: Flow.Publisher<out Number>? = null
    private var rightLabelPublisher: Flow.Publisher<out String?>? = null

    private val properties = Properties()

    fun withRange(range: Flow.Publisher<out Number>): SwingometerFrameBuilder {
        range.subscribe(Subscriber { max -> properties.max = max })
        this.rangePublisher = properties.maxPublisher
        return this
    }

    fun withBucketSize(bucketSize: Flow.Publisher<out Number>): SwingometerFrameBuilder {
        bucketSize.subscribe(Subscriber { properties.bucketSize = it })
        this.numBucketsPerSidePublisher = properties.numBucketsPerSidePublisher
        return this
    }

    private inner class Tick(val level: Double, val text: String)

    fun withTickInterval(
        tickInterval: Flow.Publisher<out Number>,
        tickStringFunc: (Number) -> String,
    ): SwingometerFrameBuilder {
        properties.tickStringFunc = tickStringFunc
        tickInterval.subscribe(Subscriber { properties.tickInterval = it })
        val ticks = properties.ticksPublisher
        this.ticksPublisher = ticks.mapElements {
            SwingometerFrame.Tick(it.level, it.text)
        }
        return this
    }

    fun withLeftNeedingToWin(leftToWin: Flow.Publisher<out Number>): SwingometerFrameBuilder {
        this.leftToWinPublisher = leftToWin
        return this
    }

    fun withRightNeedingToWin(rightToWin: Flow.Publisher<out Number>): SwingometerFrameBuilder {
        this.rightToWinPublisher = rightToWin
        return this
    }

    fun <T> withOuterLabels(
        labels: Flow.Publisher<out List<T>>,
        positionFunc: (T) -> Number,
        labelFunc: (T) -> String,
        colorFunc: (T) -> Color,
    ): SwingometerFrameBuilder {
        this.outerLabelsPublisher = labels.mapElements {
            SwingometerFrame.OuterLabel(positionFunc(it), labelFunc(it), colorFunc(it))
        }
        return this
    }

    fun <T> withDots(
        dots: Flow.Publisher<out List<T>>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Color,
    ): SwingometerFrameBuilder {
        return withDots(dots, positionFunc, colorFunc) { "" }
    }

    fun <T> withDots(
        dots: Flow.Publisher<out List<T>>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Color,
        labelFunc: (T) -> String,
    ): SwingometerFrameBuilder {
        return withDots(dots, positionFunc, colorFunc, labelFunc) { true }
    }

    fun <T> withDotsSolid(
        dots: Flow.Publisher<out List<T>>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Color,
        solidFunc: (T) -> Boolean,
    ): SwingometerFrameBuilder {
        return withDots(dots, positionFunc, colorFunc, { "" }, solidFunc)
    }

    fun <T> withDots(
        dots: Flow.Publisher<out List<T>>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Color,
        labelFunc: (T) -> String,
        solidFunc: (T) -> Boolean,
    ): SwingometerFrameBuilder {
        this.dotsPublisher =
            dots.mapElements { SwingometerFrame.Dot(positionFunc(it), colorFunc(it), labelFunc(it), solidFunc(it)) }
        return this
    }

    fun <T> withFixedDots(
        dots: List<T>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Flow.Publisher<out Color>,
    ): SwingometerFrameBuilder {
        return withFixedDots(dots, positionFunc, colorFunc) { "" }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun <T> withFixedDots(
        dots: List<T>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Flow.Publisher<out Color>,
        labelFunc: (T) -> String,
    ): SwingometerFrameBuilder {
        return withFixedDots(dots, positionFunc, colorFunc, labelFunc) { true }
    }

    fun <T> withFixedDotsSolid(
        dots: List<T>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Flow.Publisher<out Color>,
        solidFunc: (T) -> Boolean,
    ): SwingometerFrameBuilder {
        return withFixedDots(dots, positionFunc, colorFunc, { "" }, solidFunc)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun <T> withFixedDots(
        dots: List<T>,
        positionFunc: (T) -> Number,
        colorFunc: (T) -> Flow.Publisher<out Color>,
        labelFunc: (T) -> String,
        solidFunc: (T) -> Boolean,
    ): SwingometerFrameBuilder {
        this.dotsPublisher =
            dots.map {
                colorFunc(it).map { c -> SwingometerFrame.Dot(positionFunc(it), c, labelFunc(it), solidFunc(it)) }
            }
                .combine()
        return this
    }

    fun withHeader(header: Flow.Publisher<out String?>, rightLabel: Flow.Publisher<out String?> = null.asOneTimePublisher()): SwingometerFrameBuilder {
        this.headerPublisher = header
        this.rightLabelPublisher = rightLabel
        return this
    }

    fun build(): SwingometerFrame {
        return SwingometerFrame(
            headerPublisher = headerPublisher ?: (null as String?).asOneTimePublisher(),
            rangePublisher = rangePublisher ?: 1.asOneTimePublisher(),
            valuePublisher = valuePublisher ?: 0.asOneTimePublisher(),
            leftColorPublisher = leftColorPublisher ?: Color.BLACK.asOneTimePublisher(),
            rightColorPublisher = rightColorPublisher ?: Color.BLACK.asOneTimePublisher(),
            numBucketsPerSidePublisher = numBucketsPerSidePublisher ?: 1.asOneTimePublisher(),
            dotsPublisher = dotsPublisher ?: emptyList<SwingometerFrame.Dot>().asOneTimePublisher(),
            leftToWinPublisher = leftToWinPublisher,
            rightToWinPublisher = rightToWinPublisher,
            ticksPublisher = ticksPublisher,
            outerLabelsPublisher = outerLabelsPublisher,
            headerLabelsPublisher = rightLabelPublisher?.map { mapOf(GraphicsFrame.HeaderLabelLocation.RIGHT to it) },
        )
    }

    companion object {
        fun basic(
            colors: Flow.Publisher<out Pair<Color, Color>>,
            value: Flow.Publisher<out Number>,
        ): SwingometerFrameBuilder {
            val builder = SwingometerFrameBuilder()
            value.subscribe(Subscriber { builder.properties.value = it })
            builder.leftColorPublisher = colors.map { it.first }
            builder.rightColorPublisher = colors.map { it.second }
            builder.valuePublisher = builder.properties.valuePublisher
            return builder
        }
    }
}
