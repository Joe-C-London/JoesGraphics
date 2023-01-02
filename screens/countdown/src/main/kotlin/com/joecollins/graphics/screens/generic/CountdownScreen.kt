package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.CountdownFrame
import com.joecollins.graphics.components.MapFrame
import com.joecollins.pubsub.asOneTimePublisher
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Shape
import java.awt.geom.Area
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.Flow
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class CountdownScreen private constructor(panel: JPanel, title: Flow.Publisher<String>) : GenericPanel(panel, title) {

    class Builder<K>(private val date: LocalDate, private val shapes: Map<K, Shape>) {

        private val timings = ArrayList<Triple<Instant, String, Collection<Shape>>>()
        private var clock = Clock.systemDefaultZone()
        private var timesUpLabel = ""

        fun atTime(header: String, time: LocalTime, zone: ZoneId, predicate: (K) -> Boolean = { true }): Builder<K> {
            val instant = ZonedDateTime.of(date, time, zone).toInstant()
            timings.add(Triple(instant, header, shapes.entries.filter { predicate(it.key) }.map { it.value }))
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

            (colors.indices).forEach {
                val frame = CountdownFrame(
                    headerPublisher = timings[it].second.asOneTimePublisher(),
                    timePublisher = timings[it].first.asOneTimePublisher(),
                    labelFunc = {
                        if (it.isNegative) {
                            timesUpLabel
                        } else if (it.toHours() == 0L) {
                            CountdownFrame.formatMMSS(it)
                        } else if (it.toDays() == 0L) {
                            CountdownFrame.formatHHMMSS(it)
                        } else {
                            CountdownFrame.formatDDHHMMSS(it)
                        }
                    },
                    borderColorPublisher = colors[it].asOneTimePublisher(),
                    countdownColorPublisher = colors[it].asOneTimePublisher(),
                )
                frame.clock = clock
                top.add(frame)
            }

            val map = MapFrame(
                shapesPublisher = (colors.indices).flatMap { idx -> timings[idx].third.map { s -> s to colors[idx] } }.asOneTimePublisher(),
                headerPublisher = null.asOneTimePublisher(),
                borderColorPublisher = Color.WHITE.asOneTimePublisher(),
            )
            outer.add(map, BorderLayout.CENTER)

            return CountdownScreen(outer, title)
        }
    }

    companion object {
        fun forDate(date: LocalDate): Builder<Unit> {
            return Builder(date, emptyMap())
        }

        fun forDateWithMap(date: LocalDate, map: Collection<Shape>): Builder<Unit> {
            return Builder(date, mapOf(Unit to (map.map { Area(it) }.reduceOrNull { a, s -> a.add(Area(s)); a } ?: Area())))
        }

        fun <K> forDateWithMap(date: LocalDate, map: Map<K, Shape>): Builder<K> {
            return Builder(date, map)
        }
    }
}
