package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.GenericPanel.Companion.pad
import com.joecollins.graphics.components.BarFrameBuilder
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

class RegionalSeatsScreen private constructor(subPanels: List<GenericPanel>) :
    JPanel(),
    AltTextProvider {

    init {
        background = Color.WHITE
        layout = GridLayout(1, 0)
        subPanels.forEach { add(it) }
    }

    override val altText: Flow.Publisher<out String> =
        subPanels.map { it.altText }
            .combine()
            .map { p -> p.filterNotNull().joinToString("\n\n") }

    companion object {
        fun <R> ofCurrPrev(
            regions: List<R>,
            title: R.() -> Flow.Publisher<String>,
            currSeats: R.() -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            prevSeats: R.() -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            totalSeats: (R.() -> Flow.Publisher<Int>),
            seatHeader: R.() -> Flow.Publisher<String>,
            changeHeader: R.() -> Flow.Publisher<String>,
            seatSubhead: R.() -> Flow.Publisher<out String?> = { null.asOneTimePublisher() },
            changeSubhead: R.() -> Flow.Publisher<out String?> = { null.asOneTimePublisher() },
            progressLabel: (R.() -> Flow.Publisher<out String?>)? = null,
        ): RegionalSeatsScreen = RegionalSeatsScreen(
            regions.map {
                buildPanel(
                    it.title(),
                    it.currSeats(),
                    it.prevSeats(),
                    it.seatHeader(),
                    it.changeHeader(),
                    it.seatSubhead(),
                    it.changeSubhead(),
                    it.totalSeats(),
                    progressLabel?.invoke(it),
                )
            },
        )

        private fun buildPanel(
            title: Flow.Publisher<String>,
            currSeats: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            prevSeats: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            seatHeader: Flow.Publisher<String>,
            changeHeader: Flow.Publisher<String>,
            seatSubhead: Flow.Publisher<out String?>,
            changeSubhead: Flow.Publisher<out String?>,
            totalSeats: Flow.Publisher<Int>,
            progressLabel: (Flow.Publisher<out String?>)?,
        ): GenericPanel {
            data class Entry(val party: PartyOrCoalition, val seats: Int?, val prev: Int)

            val entries = currSeats.merge(prevSeats) { curr, prev ->
                val entries = curr.entries.map { e ->
                    Entry(e.key, e.value, (prev[e.key] ?: 0))
                }.toMutableList()
                prev.filterKeys { !curr.containsKey(it) }.forEach { k, v ->
                    entries.add(Entry(k, null, v))
                }
                if (prev.containsKey(Party.OTHERS) && !curr.containsKey(Party.OTHERS)) {
                    entries.add(Entry(Party.OTHERS, null, (prev[Party.OTHERS] ?: 0)))
                }
                entries.sortedByDescending { it.party.overrideSortOrder ?: it.seats }
            }

            val panel = run {
                val curr = BarFrameBuilder.basic(
                    barsPublisher = entries.map { e ->
                        e.filter { it.seats != null }
                            .map {
                                BarFrameBuilder.BasicBar(
                                    it.party.name.uppercase(),
                                    it.party.color,
                                    it.seats!!,
                                    it.seats.toString(),
                                )
                            }
                    },
                    headerPublisher = seatHeader,
                    rightHeaderLabelPublisher = progressLabel,
                    subheadPublisher = seatSubhead,
                    maxPublisher = totalSeats.map { (it / 2).coerceAtLeast(1) },
                )
                val change = BarFrameBuilder.basic(
                    barsPublisher = entries.map { e ->
                        e.map {
                            val change = (it.seats ?: 0) - it.prev
                            BarFrameBuilder.BasicBar(
                                it.party.abbreviation.uppercase(),
                                it.party.color,
                                change,
                                if (change == 0) "±0" else DecimalFormat("+0;-0").format(change),
                            )
                        }
                    },
                    headerPublisher = changeHeader,
                    subheadPublisher = changeSubhead,
                    wingspanPublisher = totalSeats.map { (it / 10).coerceAtLeast(1) },
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
                val seatTop = seatHeader.run {
                    if (progressLabel == null) {
                        this
                    } else {
                        merge(progressLabel) { h, p -> if (p.isNullOrEmpty()) h else "$h [$p]" }
                    }
                }
                    .merge(seatSubhead) { h, s -> if (s.isNullOrEmpty()) h else "$h, $s" }
                val changeTop = changeHeader.merge(changeSubhead) { h, s -> if (s.isNullOrEmpty()) h else "$h, $s" }
                val entriesText = entries.map { e ->
                    e.joinToString("\n") {
                        "${it.party.name.uppercase()}: ${
                            if (it.seats == null) {
                                "-"
                            } else {
                                it.seats.toString()
                            }
                        } (${
                            if (it.seats == it.prev) {
                                "±0"
                            } else {
                                DecimalFormat("+0;-0").format((it.seats ?: 0) - it.prev)
                            }
                        })"
                    }
                }
                seatTop.merge(changeTop) { v, c -> "$v ($c)" }
                    .merge(title) { r, t -> "$t\n$r" }
                    .merge(entriesText) { t, e -> if (e.isEmpty()) t else "$t\n$e" }
            }
            return GenericPanel(
                panel,
                title,
                altText,
            )
        }
    }
}
