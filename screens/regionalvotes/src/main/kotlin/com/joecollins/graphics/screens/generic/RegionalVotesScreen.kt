package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.GenericPanel.Companion.pad
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.GridLayout
import java.text.DecimalFormat
import java.util.LinkedList
import java.util.concurrent.Flow
import javax.swing.JPanel
import kotlin.math.absoluteValue

class RegionalVotesScreen(subPanels: List<GenericPanel>) : JPanel(), AltTextProvider {

    init {
        background = Color.WHITE
        layout = GridLayout(1, 0)
        subPanels.forEach { add(it) }
    }

    override val altText: Flow.Publisher<out String> =
        subPanels.map { it.altText }
            .combine()
            .map { p -> p.filterNotNull().joinToString("\n\n") }

    class CurrPrevBuilder(
        private val voteHeader: Flow.Publisher<String>,
        private val changeHeader: Flow.Publisher<String>,
        private val voteSubhead: Flow.Publisher<out String?>,
        private val changeSubhead: Flow.Publisher<out String?>,
    ) {
        private val panels = LinkedList<GenericPanel>()

        fun withRegion(
            title: Flow.Publisher<String>,
            currVotes: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            prevVotes: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            pctReporting: Flow.Publisher<Double> = 1.0.asOneTimePublisher(),
            progressLabel: Flow.Publisher<out String?> = null.asOneTimePublisher(),
        ): CurrPrevBuilder {
            data class Entry(val party: PartyOrCoalition, val pct: Double?, val prev: Double)
            val entries = currVotes.merge(prevVotes) { curr, prev ->
                val currTotal = curr.values.sum().toDouble()
                val prevTotal = prev.values.sum().toDouble()
                val adjPrev = Aggregators.adjustKey(prev) { if (curr.containsKey(it)) it else Party.OTHERS }
                val entries = curr.entries.map { e ->
                    Entry(e.key, e.value / currTotal, (adjPrev[e.key] ?: 0) / prevTotal)
                }.toMutableList()
                if (adjPrev.containsKey(Party.OTHERS) && !curr.containsKey(Party.OTHERS)) {
                    entries.add(Entry(Party.OTHERS, null, (adjPrev[Party.OTHERS] ?: 0) / prevTotal))
                }
                entries.sortedByDescending { it.party.overrideSortOrder?.toDouble() ?: it.pct }
            }

            val panel = run {
                val curr = BarFrameBuilder.basic(
                    entries.map { e ->
                        e.filter { it.pct != null }
                            .map {
                                BarFrameBuilder.BasicBar(
                                    it.party.name.uppercase(),
                                    it.party.color,
                                    it.pct!!,
                                    DecimalFormat("0.0%").format(it.pct),
                                )
                            }
                    },
                )
                    .withHeader(voteHeader, rightLabelPublisher = progressLabel)
                    .withSubhead(voteSubhead)
                    .withMax(
                        entries.merge(pctReporting) { e, p ->
                            (0.5 / p.coerceAtLeast(1e-6))
                                .coerceAtLeast(e.maxOfOrNull { it.pct ?: 0.0 } ?: 0.0)
                        },
                    )
                    .build()
                val change = BarFrameBuilder.basic(
                    entries.map { e ->
                        e.map {
                            val change = (it.pct ?: 0.0) - it.prev
                            BarFrameBuilder.BasicBar(
                                it.party.abbreviation.uppercase(),
                                it.party.color,
                                change,
                                if (change == 0.0) "±0.0%" else DecimalFormat("+0.0%;-0.0%").format(change),
                            )
                        }
                    },
                )
                    .withHeader(changeHeader)
                    .withSubhead(changeSubhead)
                    .withWingspan(
                        entries.merge(pctReporting) { e, p ->
                            (0.05 / p.coerceAtLeast(1e-6))
                                .coerceAtLeast(e.maxOfOrNull { ((it.pct ?: 0.0) - it.prev).absoluteValue } ?: 0.0)
                        },
                    )
                    .build()
                JPanel().let { panel ->
                    panel.background = Color.WHITE
                    panel.layout = GridLayout(2, 1, 5, 5)
                    panel.add(curr)
                    panel.add(change)
                    pad(panel)
                }
            }
            val altText = run {
                val voteTop = voteHeader.merge(progressLabel) { h, p -> if (p == null) h else "$h [$p]" }
                    .merge(voteSubhead) { h, s -> if (s == null) h else "$h, $s" }
                val changeTop = changeHeader.merge(changeSubhead) { h, s -> if (s == null) h else "$h, $s" }
                val entriesText = entries.map { e ->
                    e.joinToString("\n") {
                        "${it.party.name.uppercase()}: ${
                            if (it.pct == null) {
                                "-"
                            } else {
                                DecimalFormat("0.0%").format(it.pct)
                            }
                        } (${
                            if (it.pct == it.prev) {
                                "±0.0%"
                            } else {
                                DecimalFormat("+0.0%;-0.0%").format((it.pct ?: 0.0) - it.prev)
                            }
                        })"
                    }
                }
                voteTop.merge(changeTop) { v, c -> "$v ($c)" }
                    .merge(title) { r, t -> "$t\n$r" }
                    .merge(entriesText) { t, e -> if (e.isEmpty()) t else "$t\n$e" }
            }
            panels.add(
                GenericPanel(
                    panel,
                    title,
                    altText,
                ),
            )
            return this
        }

        fun build(): RegionalVotesScreen {
            return RegionalVotesScreen(panels)
        }
    }

    companion object {
        fun ofCurrPrev(
            voteHeader: Flow.Publisher<String>,
            changeHeader: Flow.Publisher<String>,
            voteSubhead: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher(),
            changeSubhead: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher(),
        ): CurrPrevBuilder {
            return CurrPrevBuilder(voteHeader, changeHeader, voteSubhead, changeSubhead)
        }
    }
}
