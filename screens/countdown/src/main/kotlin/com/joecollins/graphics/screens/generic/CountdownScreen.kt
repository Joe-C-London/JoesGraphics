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
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Shape
import java.awt.geom.Area
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.Flow
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class CountdownScreen private constructor(
    panel: JPanel,
    title: Flow.Publisher<String>,
    altText: Flow.Publisher<String>,
) : GenericPanel(panel, title, altText), AltTextProvider {

    class Builder<K>(private val date: LocalDate, private val shapes: Flow.Publisher<out Map<K, Shape>>) {

        private val timings = ArrayList<Triple<Instant, String, Flow.Publisher<Collection<Shape>>>>()
        private var clock = Clock.systemDefaultZone()
        private var timesUpLabel = ""

        fun atTime(header: String, time: LocalTime, zone: ZoneId, predicate: (K) -> Boolean = { true }): Builder<K> {
            val instant = ZonedDateTime.of(date, time, zone).toInstant()
            timings.add(Triple(instant, header, shapes.map { s -> s.entries.filter { predicate(it.key) }.map { it.value } }))
            return this
        }

        fun withTimesUpLabel(label: String): Builder<K> {
            this.timesUpLabel = label
            return this
        }

        internal fun withClock(clock: Clock): Builder<K> {
            this.clock = clock
            return this
        }

        fun build(title: Flow.Publisher<String>): CountdownScreen {
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
                    headerPublisher = timings[color].second.asOneTimePublisher(),
                    timePublisher = timings[color].first.asOneTimePublisher(),
                    labelFunc = { timeLabel(it) },
                    borderColorPublisher = colors[color].asOneTimePublisher(),
                    countdownColorPublisher = colors[color].asOneTimePublisher(),
                )
                frame.clock = clock
                top.add(frame)
            }

            val map = MapFrame(
                shapesPublisher = (colors.indices).map { idx ->
                    timings[idx].third.map { shapes ->
                        shapes.map { s -> s to colors[idx] }
                    }
                }.combine().map { it.flatten() },
                headerPublisher = null.asOneTimePublisher(),
                borderColorPublisher = Color.WHITE.asOneTimePublisher(),
            )
            outer.add(map, BorderLayout.CENTER)

            val executor = Executors.newScheduledThreadPool(1) { r: Runnable ->
                val t = Executors.defaultThreadFactory().newThread(r)
                t.isDaemon = true
                t
            }
            val timingTexts = timings.map {
                altTextLabel(it.second, it.first, executor)
            }.combine().map { it.joinToString("\n") }
            val altText = title.merge(timingTexts) { t, tt -> "$t\n\n$tt" }

            return CountdownScreen(outer, title, altText)
        }

        private fun altTextLabel(header: String, timestamp: Instant, executor: ScheduledExecutorService): Flow.Publisher<String> {
            val ret = Publisher<String>()
            executor.scheduleAtFixedRate({
                ret.submit(header + ": " + timeLabel(Duration.between(clock.instant().truncatedTo(ChronoUnit.SECONDS), timestamp)))
            }, 0, 100, TimeUnit.MILLISECONDS)
            return ret
        }

        private fun timeLabel(it: Duration) = if (it.isNegative) {
            timesUpLabel
        } else if (it.toHours() == 0L) {
            CountdownFrame.formatMMSS(it)
        } else if (it.toDays() == 0L) {
            CountdownFrame.formatHHMMSS(it)
        } else {
            CountdownFrame.formatDDHHMMSS(it)
        }
    }

    companion object {
        fun forDate(date: LocalDate): Builder<Unit> {
            return Builder(date, emptyMap<Unit, Shape>().asOneTimePublisher())
        }

        fun forDateWithMapSingle(date: LocalDate, map: Flow.Publisher<Collection<Shape>>): Builder<Unit> {
            return Builder(
                date,
                map.map { m ->
                    mapOf(
                        Unit to (
                            m.map { Area(it) }
                                .reduceOrNull { a, s -> a.add(Area(s)); a }
                                ?: Area()
                            ),
                    )
                },
            )
        }

        fun <K> forDateWithMap(date: LocalDate, map: Flow.Publisher<out Map<K, Shape>>): Builder<K> {
            return Builder(date, map)
        }
    }
}
