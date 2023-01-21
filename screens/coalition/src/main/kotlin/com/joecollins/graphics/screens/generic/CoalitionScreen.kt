package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.GraphicsFrame
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.util.LinkedList
import java.util.concurrent.Flow
import kotlin.math.max

class CoalitionScreen private constructor(builder: Builder, title: Flow.Publisher<out String?>) : GenericPanel(
    run {
        BarFrame(
            barsPublisher = builder.rows.map { rows ->
                rows.map { row ->
                    BarFrame.Bar(
                        leftText = row.name,
                        rightText = row.total.toString(),
                        series = row.parts.map { it.first.color to it.second },
                    )
                }
            },
            headerPublisher = builder.header,
            subheadTextPublisher = builder.subhead,
            maxPublisher = builder.totalSeats.merge(builder.rows) { total, rows -> max(total * 3 / 4, rows.maxOf { it.total }) },
            linesPublisher = builder.totalSeats.map {
                val majority = it / 2 + 1
                listOf(BarFrame.Line(majority, builder.majorityLabel(majority)))
            },
            headerLabelsPublisher = builder.progressLabel.map { mapOf(GraphicsFrame.HeaderLabelLocation.RIGHT to it) },
        )
            .let { pad(it) }
    },
    title,
    run {
        val top = builder.header.merge(builder.subhead) { h, s -> h + (if (s.isNullOrEmpty()) "" else ", $s") }
            .merge(builder.progressLabel) { h, p -> h + (if (p.isNullOrEmpty()) "" else " [$p]") }
            .merge(title) { h, t -> "$t\n\n$h" }
        val entries = builder.rows.map { rows ->
            rows.joinToString("\n") { row ->
                "${row.name} (${row.parts.joinToString { it.first.abbreviation }}): ${row.total}"
            }
        }
        val bottom = builder.totalSeats.map { builder.majorityLabel(it / 2 + 1) }
        top.merge(entries) { t, e -> "$t\n$e" }.merge(bottom) { t, b -> "$t\n$b" }
    },
) {

    companion object {
        fun of(
            seats: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            totalSeats: Flow.Publisher<Int>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
            majorityLabel: (Int) -> String,
        ): Builder {
            return Builder(seats, totalSeats, header, subhead, majorityLabel)
        }
    }

    class Builder(
        private val seats: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
        internal val totalSeats: Flow.Publisher<Int>,
        internal val header: Flow.Publisher<out String?>,
        internal val subhead: Flow.Publisher<out String?>,
        internal val majorityLabel: (Int) -> String,
    ) {
        internal var progressLabel: Flow.Publisher<String?> = null.asOneTimePublisher()

        internal class Coalition(val name: String, val parties: Set<PartyOrCoalition>, val include: (Map<out PartyOrCoalition, Int>) -> Boolean)
        private val coalitions = LinkedList<Coalition>()

        fun withCoalition(name: String, parties: Set<PartyOrCoalition>, include: (Map<out PartyOrCoalition, Int>) -> Boolean = { true }): Builder {
            coalitions.add(Coalition(name, parties, include))
            return this
        }

        fun withProgressLabel(label: Flow.Publisher<String?>): Builder {
            this.progressLabel = label
            return this
        }

        fun build(title: Flow.Publisher<out String?>): CoalitionScreen {
            return CoalitionScreen(this, title)
        }

        internal class Row(val name: String, val parts: List<Pair<PartyOrCoalition, Int>>) {
            val total = parts.sumOf { it.second }
        }
        internal val rows: Flow.Publisher<List<Row>>
            get() = seats.map { seats ->
                coalitions.filter { it.include(seats) }
                    .map { coalition ->
                        val parts = seats.filterKeys { coalition.parties.contains(it) }
                            .filterValues { it > 0 }
                            .entries
                            .sortedByDescending { it.value }
                            .map { it.key to it.value }
                        Row(coalition.name.uppercase(), parts)
                    }
            }
    }
}
