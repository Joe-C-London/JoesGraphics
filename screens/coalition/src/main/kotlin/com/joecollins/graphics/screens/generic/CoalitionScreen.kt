package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.GraphicsFrame
import com.joecollins.graphics.utils.PanelUtils.pad
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import java.util.concurrent.Flow
import javax.swing.JPanel
import kotlin.math.max

class CoalitionScreen private constructor(
    panel: JPanel,
    title: Flow.Publisher<out String?>,
    altText: () -> Flow.Publisher<String>,
) : GenericPanel(
    panel,
    title,
    altText,
) {

    companion object {
        fun of(
            seats: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            totalSeats: Flow.Publisher<Int>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
            majorityLabel: (Int) -> String,
            coalitions: List<Coalition>,
            progressLabel: Flow.Publisher<out String?>? = null,
            title: Flow.Publisher<out String?>,
        ): CoalitionScreen {
            val rows: Flow.Publisher<List<Row>> = seats.map { s ->
                coalitions.filter { it.include(s) }
                    .map { coalition ->
                        val parts = s.filterKeys { coalition.parties.contains(it) }
                            .filterValues { it > 0 }
                            .entries
                            .sortedByDescending { it.value }
                            .map { it.key to it.value }
                        Row(coalition.name.uppercase(), parts)
                    }
            }
            return CoalitionScreen(
                BarFrame(
                    barsPublisher = rows.mapElements { row ->
                        BarFrame.Bar.of(
                            leftText = row.name,
                            rightText = row.total.toString(),
                            series = row.parts.map { it.first.color to it.second },
                        )
                    },
                    headerPublisher = header,
                    subheadTextPublisher = subhead,
                    maxPublisher = totalSeats.merge(rows) { total, row ->
                        max(
                            total * 3 / 4,
                            row.maxOf { it.total },
                        )
                    },
                    linesPublisher = totalSeats.map {
                        val majority = it / 2 + 1
                        listOf(BarFrame.Line(majority, majorityLabel(majority)))
                    },
                    headerLabelsPublisher = progressLabel?.map { mapOf(GraphicsFrame.HeaderLabelLocation.RIGHT to it) },
                ).pad(),
                title,
                altText = {
                    val top = header.merge(subhead) { h, s -> h + (if (s.isNullOrEmpty()) "" else ", $s") }
                        .run {
                            if (progressLabel == null) {
                                this
                            } else {
                                merge(progressLabel) { h, p -> h + (if (p.isNullOrEmpty()) "" else " [$p]") }
                            }
                        }
                        .merge(title) { h, t -> "$t\n\n$h" }
                    val entries = rows.map { rows ->
                        rows.joinToString("\n") { row ->
                            "${row.name} (${row.parts.joinToString { it.first.abbreviation }}): ${row.total}"
                        }
                    }
                    val bottom = totalSeats.map { majorityLabel(it / 2 + 1) }
                    top.merge(entries) { t, e -> "$t\n$e" }.merge(bottom) { t, b -> "$t\n$b" }
                },
            )
        }

        fun coalition(
            name: String,
            parties: Set<PartyOrCoalition>,
            include: (Map<out PartyOrCoalition, Int>) -> Boolean = { true },
        ) = Coalition(name, parties, include)
    }

    class Coalition internal constructor(val name: String, val parties: Set<PartyOrCoalition>, val include: (Map<out PartyOrCoalition, Int>) -> Boolean)

    private class Row(val name: String, val parts: List<Pair<PartyOrCoalition, Int>>) {
        val total = parts.sumOf { it.second }
    }
}
