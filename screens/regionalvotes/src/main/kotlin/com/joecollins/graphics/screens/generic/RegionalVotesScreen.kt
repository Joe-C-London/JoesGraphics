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
import java.util.concurrent.Flow
import javax.swing.JPanel
import kotlin.math.absoluteValue

class RegionalVotesScreen private constructor(subPanels: List<GenericPanel>) :
    JPanel(),
    AltTextProvider {

    init {
        background = Color.WHITE
        layout = GridLayout(1, 0)
        subPanels.forEach { add(it) }
    }

    override val altText: Flow.Publisher<out (Int) -> String> =
        subPanels.map { it.altText }
            .combine()
            .map { p ->
                { maxLength ->
                    p.mapNotNull { it(maxLength / p.size) }.joinToString("\n\n")
                }
            }

    companion object {
        fun <R> ofCurrPrev(
            regions: List<R>,
            title: R.() -> Flow.Publisher<String>,
            currVotes: R.() -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            prevVotes: R.() -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            voteHeader: R.() -> Flow.Publisher<String>,
            changeHeader: R.() -> Flow.Publisher<String>,
            voteSubhead: R.() -> Flow.Publisher<out String?> = { null.asOneTimePublisher() },
            changeSubhead: R.() -> Flow.Publisher<out String?> = { null.asOneTimePublisher() },
            pctReporting: (R.() -> Flow.Publisher<Double>)? = null,
            progressLabel: (R.() -> Flow.Publisher<out String?>)? = null,
        ): RegionalVotesScreen = RegionalVotesScreen(
            regions.map {
                buildPanel(
                    it.title(),
                    it.currVotes(),
                    it.prevVotes(),
                    it.voteHeader(),
                    it.changeHeader(),
                    it.voteSubhead(),
                    it.changeSubhead(),
                    pctReporting?.invoke(it),
                    progressLabel?.invoke(it),
                )
            },
        )

        private fun buildPanel(
            title: Flow.Publisher<String>,
            currVotes: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            prevVotes: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            voteHeader: Flow.Publisher<String>,
            changeHeader: Flow.Publisher<String>,
            voteSubhead: Flow.Publisher<out String?>,
            changeSubhead: Flow.Publisher<out String?>,
            pctReporting: (Flow.Publisher<Double>)?,
            progressLabel: (Flow.Publisher<out String?>)?,
        ): GenericPanel {
            data class Entry(val party: PartyOrCoalition, val pct: Double?, val prev: Double)

            val entries = currVotes.merge(prevVotes) { curr, prev ->
                val currTotal = curr.values.sum().toDouble()
                if (currTotal == 0.0) return@merge emptyList()
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
                    barsPublisher = entries.map { e ->
                        e.filter { it.pct != null }
                            .map {
                                BarFrameBuilder.BasicBar.of(
                                    it.party.name.uppercase(),
                                    it.party.color,
                                    it.pct!!,
                                    DecimalFormat("0.0%").format(it.pct),
                                )
                            }
                    },
                    headerPublisher = voteHeader,
                    rightHeaderLabelPublisher = progressLabel,
                    subheadPublisher = voteSubhead,
                    maxPublisher = entries.run {
                        if (pctReporting == null) {
                            map { e -> e to 1.0 }
                        } else {
                            merge(pctReporting) { e, p -> e to p }
                        }
                    }
                        .map { (e, p) ->
                            (0.5 / p.coerceAtLeast(1e-6))
                                .coerceAtLeast(e.maxOfOrNull { it.pct ?: 0.0 } ?: 0.0)
                        },
                )
                val change = BarFrameBuilder.basic(
                    barsPublisher = entries.map { e ->
                        e.map {
                            val change = (it.pct ?: 0.0) - it.prev
                            BarFrameBuilder.BasicBar.of(
                                it.party.abbreviation.uppercase(),
                                it.party.color,
                                change,
                                if (change == 0.0) "±0.0%" else DecimalFormat("+0.0%;-0.0%").format(change),
                            )
                        }
                    },
                    headerPublisher = changeHeader,
                    subheadPublisher = changeSubhead,
                    wingspanPublisher = entries.run {
                        if (pctReporting == null) {
                            map { e -> e to 1.0 }
                        } else {
                            merge(pctReporting) { e, p -> e to p }
                        }
                    }
                        .map { (e, p) ->
                            (0.05 / p.coerceAtLeast(1e-6))
                                .coerceAtLeast(e.maxOfOrNull { ((it.pct ?: 0.0) - it.prev).absoluteValue } ?: 0.0)
                        },
                )
                JPanel().let { panel ->
                    panel.background = Color.WHITE
                    panel.layout = GridLayout(2, 1, 5, 5)
                    panel.add(curr)
                    panel.add(change)
                    pad(panel)
                }
            }
            val altText = run {
                val voteTop = voteHeader.run {
                    if (progressLabel == null) {
                        this
                    } else {
                        merge(progressLabel) { h, p -> if (p.isNullOrEmpty()) h else "$h [$p]" }
                    }
                }
                    .merge(voteSubhead) { h, s -> if (s.isNullOrEmpty()) h else "$h, $s" }
                val changeTop = changeHeader.merge(changeSubhead) { h, s -> if (s.isNullOrEmpty()) h else "$h, $s" }
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
                    .map { text -> { _: Int -> text } }
            }
            return GenericPanel(
                panel,
                title,
                altText,
            )
        }
    }
}
