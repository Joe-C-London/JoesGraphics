package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.CountdownFrame
import com.joecollins.graphics.components.MapFrame
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import com.joecollins.utils.ExecutorUtils
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Shape
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Flow
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class CountdownScreen private constructor(
    panel: JPanel,
    title: Flow.Publisher<String>,
    altText: Flow.Publisher<String>,
) : GenericPanel(panel, title, altText), AltTextProvider {

    sealed class TimePanel<K> {
        internal abstract val header: String
        internal abstract fun instant(date: LocalDate): Instant
        internal abstract fun filteredShapes(shapes: Flow.Publisher<out Map<K, Shape>>): Flow.Publisher<out Collection<Shape>>
    }

    class TimeWithFilterPanel<K> internal constructor() : TimePanel<K>() {
        public override lateinit var header: String
        var futureDate: Period = Period.ofDays(0)
        lateinit var time: LocalTime
        lateinit var zone: ZoneId
        var filter: K.() -> Boolean = { true }

        override fun instant(date: LocalDate) = ZonedDateTime.of(date.plus(futureDate), time, zone).toInstant()
        override fun filteredShapes(shapes: Flow.Publisher<out Map<K, Shape>>) = shapes.map { s -> s.entries.filter { it.key.filter() }.map { it.value } }
    }

    class TimeWithoutFilterPanel internal constructor() : TimePanel<Unit>() {
        override lateinit var header: String
        var futureDate: Period = Period.ofDays(0)
        lateinit var time: LocalTime
        lateinit var zone: ZoneId

        override fun instant(date: LocalDate) = ZonedDateTime.of(date.plus(futureDate), time, zone).toInstant()
        override fun filteredShapes(shapes: Flow.Publisher<out Map<Unit, Shape>>) = emptyList<Shape>().asOneTimePublisher()
    }

    companion object {
        fun <K> timeWithMapFilter(time: TimeWithFilterPanel<K>.() -> Unit) = TimeWithFilterPanel<K>().apply(time)
        fun timeWithoutMapFilter(time: TimeWithoutFilterPanel.() -> Unit) = TimeWithoutFilterPanel().apply(time)

        fun forDate(
            date: LocalDate,
            times: List<TimeWithoutFilterPanel>,
            timesUpLabel: String,
            title: Flow.Publisher<String>,
        ): CountdownScreen {
            return forDate(
                date = date,
                times = times,
                timesUpLabel = timesUpLabel,
                title = title,
                clock = Clock.systemDefaultZone(),
            )
        }

        internal fun forDate(
            date: LocalDate,
            times: List<TimeWithoutFilterPanel>,
            timesUpLabel: String,
            title: Flow.Publisher<String>,
            clock: Clock,
        ): CountdownScreen {
            return createPanel(
                date = date,
                timings = times,
                shapes = emptyMap<Unit, Shape>().asOneTimePublisher(),
                timesUpLabel = timesUpLabel,
                title = title,
                clock = clock,
            )
        }

        fun forDateWithMapSingle(
            timestamp: ZonedDateTime,
            header: String,
            map: Flow.Publisher<Collection<Shape>>,
            timesUpLabel: String,
            title: Flow.Publisher<String>,
        ): CountdownScreen {
            return forDateWithMapSingle(
                timestamp = timestamp,
                header = header,
                map = map,
                timesUpLabel = timesUpLabel,
                title = title,
                clock = Clock.systemDefaultZone(),
            )
        }

        internal fun forDateWithMapSingle(
            timestamp: ZonedDateTime,
            header: String,
            map: Flow.Publisher<Collection<Shape>>,
            timesUpLabel: String,
            title: Flow.Publisher<String>,
            clock: Clock,
        ): CountdownScreen {
            return createPanel(
                date = timestamp.toLocalDate(),
                timings = listOf(
                    timeWithMapFilter {
                        this.time = timestamp.toLocalTime()
                        this.zone = timestamp.zone
                        this.header = header
                        this.filter = { true }
                    },
                ),
                shapes = map.map { m ->
                    m.associateWith { it }
                },
                timesUpLabel = timesUpLabel,
                title = title,
                clock = clock,
            )
        }

        fun <K> forDateWithMap(
            date: LocalDate,
            times: List<TimeWithFilterPanel<K>>,
            map: Flow.Publisher<out Map<K, Shape>>,
            timesUpLabel: String,
            title: Flow.Publisher<String>,
        ): CountdownScreen {
            return forDateWithMap(
                date = date,
                times = times,
                map = map,
                timesUpLabel = timesUpLabel,
                title = title,
                clock = Clock.systemDefaultZone(),
            )
        }

        internal fun <K> forDateWithMap(
            date: LocalDate,
            times: List<TimeWithFilterPanel<K>>,
            map: Flow.Publisher<out Map<K, Shape>>,
            timesUpLabel: String,
            title: Flow.Publisher<String>,
            clock: Clock,
        ): CountdownScreen {
            return createPanel(
                date = date,
                timings = times,
                shapes = map,
                timesUpLabel = timesUpLabel,
                title = title,
                clock = clock,
            )
        }

        private fun <K> createPanel(
            date: LocalDate,
            shapes: Flow.Publisher<out Map<K, Shape>>,
            timings: List<TimePanel<K>>,
            timesUpLabel: String,
            title: Flow.Publisher<String>,
            clock: Clock,
        ): CountdownScreen {
            val outer = JPanel()
            outer.background = Color.WHITE
            outer.layout = BorderLayout()
            outer.border = EmptyBorder(5, 5, 5, 5)

            val top = JPanel()
            top.background = Color.WHITE
            top.layout = GridLayout(1, 0, 5, 5)
            top.preferredSize = Dimension(1024, 60)
            outer.add(top, BorderLayout.NORTH)

            val colors = when (timings.size) {
                1 -> listOf(Color.BLACK)
                2 -> listOf(Color.RED, Color.BLUE)
                3 -> listOf(Color.RED, Color.GREEN.darker(), Color.BLUE)
                4 -> listOf(Color.RED, Color.GREEN.darker(), Color.BLUE, Color.MAGENTA)
                5 -> listOf(Color.RED, Color.ORANGE, Color.GREEN.darker(), Color.BLUE, Color.MAGENTA)
                6 -> listOf(Color.RED, Color.ORANGE, Color.GREEN.darker(), Color.CYAN.darker(), Color.BLUE, Color.MAGENTA)
                else -> generateSequence { Color((0x1000000 * Math.random()).toInt()) }.take(timings.size).toList()
            }

            (colors.indices).forEach { color ->
                val frame = CountdownFrame(
                    headerPublisher = timings[color].header.asOneTimePublisher(),
                    timePublisher = timings[color].instant(date).asOneTimePublisher(),
                    labelFunc = { timeLabel(it, timesUpLabel) },
                    borderColorPublisher = colors[color].asOneTimePublisher(),
                    countdownColorPublisher = colors[color].asOneTimePublisher(),
                )
                frame.clock = clock
                top.add(frame)
            }

            val map = MapFrame(
                shapesPublisher = (colors.indices).map { idx ->
                    timings[idx].filteredShapes(shapes).map { shapes ->
                        shapes.map { s -> s to colors[idx] }
                    }
                }.combine().map { it.flatten() },
                headerPublisher = null.asOneTimePublisher(),
                borderColorPublisher = Color.WHITE.asOneTimePublisher(),
            )
            outer.add(map, BorderLayout.CENTER)

            val timingTexts = timings.map {
                altTextLabel(it.header, it.instant(date), clock, timesUpLabel)
            }.combine().map { it.joinToString("\n") }
            val altText = title.merge(timingTexts) { t, tt -> "$t\n\n$tt" }

            return CountdownScreen(outer, title, altText)
        }

        private fun altTextLabel(header: String, timestamp: Instant, clock: Clock, timesUpLabel: String): Flow.Publisher<String> {
            val ret = Publisher<String>()
            ExecutorUtils.scheduleTicking({
                ret.submit(header + ": " + timeLabel(Duration.between(clock.instant().truncatedTo(ChronoUnit.SECONDS), timestamp), timesUpLabel))
            }, 100)
            return ret
        }

        private fun timeLabel(it: Duration, timesUpLabel: String) = if (it.isNegative) {
            timesUpLabel
        } else if (it.toHours() == 0L) {
            CountdownFrame.formatMMSS(it)
        } else if (it.toDays() == 0L) {
            CountdownFrame.formatHHMMSS(it)
        } else {
            CountdownFrame.formatDDHHMMSS(it)
        }
    }
}
