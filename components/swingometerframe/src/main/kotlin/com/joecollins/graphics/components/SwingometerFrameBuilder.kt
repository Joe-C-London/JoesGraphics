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

object SwingometerFrameBuilder {
    private class Properties {

        var value: Number = 0
            set(value) {
                field = if (value.toDouble().isNaN()) 0.0 else value
                update()
            }

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

    private class Tick(val level: Double, val text: String)

    class TickInterval internal constructor(
        internal val tickInterval: Flow.Publisher<out Number>,
        internal val tickStringFunc: (Number) -> String,
    )

    fun every(
        tickInterval: Flow.Publisher<out Number>,
        tickStringFunc: (Number) -> String,
    ) = TickInterval(tickInterval, tickStringFunc)

    fun <T> labels(
        labels: Flow.Publisher<out List<T>>,
        position: T.() -> Number,
        label: T.() -> String,
        color: T.() -> Color,
    ) = labels.mapElements {
        SwingometerFrame.OuterLabel(it.position(), it.label(), it.color())
    }

    fun <T> dots(
        dots: Flow.Publisher<out List<T>>,
        position: T.() -> Number,
        color: T.() -> Color,
        label: T.() -> String = { "" },
        solid: T.() -> Boolean = { true },
    ) = dots.mapElements { SwingometerFrame.Dot(it.position(), it.color(), it.label(), it.solid()) }

    fun <T> dots(
        dots: List<T>,
        position: T.() -> Number,
        color: T.() -> Flow.Publisher<out Color>,
        label: T.() -> String = { "" },
        solid: T.() -> Boolean = { true },
    ) = dots.map {
        it.color().map { c -> SwingometerFrame.Dot(it.position(), c, it.label(), it.solid()) }
    }
        .combine()

    fun build(
        colors: Flow.Publisher<out Pair<Color, Color>>,
        value: Flow.Publisher<out Number>,
        range: Flow.Publisher<out Number>? = null,
        bucketSize: Flow.Publisher<out Number>? = null,
        tickInterval: TickInterval? = null,
        leftToWin: Flow.Publisher<out Number>? = null,
        rightToWin: Flow.Publisher<out Number>? = null,
        outerLabels: Flow.Publisher<List<SwingometerFrame.OuterLabel>>? = null,
        dots: Flow.Publisher<out List<SwingometerFrame.Dot>>? = null,
        header: Flow.Publisher<out String?>,
        rightHeaderLabel: Flow.Publisher<out String?>? = null,
    ): SwingometerFrame {
        val properties = Properties()
        value.subscribe(Subscriber { properties.value = it })
        range?.subscribe(Subscriber { max -> properties.max = max })
        bucketSize?.subscribe(Subscriber { properties.bucketSize = it })
        tickInterval?.tickInterval?.subscribe(Subscriber { properties.tickInterval = it })
        properties.tickStringFunc = tickInterval?.tickStringFunc ?: { "" }

        return SwingometerFrame(
            headerPublisher = header,
            rangePublisher = properties.maxPublisher,
            valuePublisher = value.map { if (it.toDouble().isNaN()) 0.0 else it },
            leftColorPublisher = colors.map { it.first },
            rightColorPublisher = colors.map { it.second },
            numBucketsPerSidePublisher = properties.numBucketsPerSidePublisher,
            dotsPublisher = dots ?: emptyList<SwingometerFrame.Dot>().asOneTimePublisher(),
            leftToWinPublisher = leftToWin,
            rightToWinPublisher = rightToWin,
            ticksPublisher = properties.ticksPublisher.mapElements {
                SwingometerFrame.Tick(it.level, it.text)
            },
            outerLabelsPublisher = outerLabels,
            headerLabelsPublisher = rightHeaderLabel?.map { mapOf(GraphicsFrame.HeaderLabelLocation.RIGHT to it) },
        )
    }
}
