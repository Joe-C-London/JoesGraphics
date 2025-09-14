package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import java.awt.Color
import java.util.concurrent.Flow

class SeatsChangingScreen private constructor(title: Flow.Publisher<out String?>, frame: ResultListingFrame, altText: Flow.Publisher<out String?>) : GenericPanel(pad(frame), title, altText) {
    private class Input<T> {
        private var prevResults: List<Pair<T, Party>> = emptyList()
        private var currResults: Map<T, PartyResult?> = emptyMap()
        private var seatFilter: Set<T>? = null
        private var partyChanges: Map<Party, Party> = emptyMap()

        fun setPrevResults(prevResults: Map<T, Map<Party, Int>>, prevWinners: Map<T, Party>) {
            this.prevResults = prevWinners.entries
                .asSequence()
                .map {
                    val votes = prevResults[it.key] ?: emptyMap()
                    val total = votes.values.sum().coerceAtLeast(1)
                    val winner = votes[it.value] ?: 0
                    val second = votes.filterKeys { e -> e != it.value }.maxOfOrNull { e -> e.value } ?: 0
                    Pair(it, 1.0 * (winner - second) / total)
                }
                .sortedBy { it.second }
                .map { it.first }
                .map {
                    Pair(
                        it.key,
                        it.value,
                    )
                }
                .toList()
            update()
        }

        fun setCurrResults(currResults: Map<T, PartyResult?>) {
            this.currResults = currResults
            update()
        }

        fun setSeatFilter(seatFilter: Set<T>?) {
            this.seatFilter = seatFilter
            update()
        }

        fun setPartyChanges(changes: Map<Party, Party>) {
            this.partyChanges = changes
            update()
        }

        val resultPublisher = Publisher(calculateEntries())

        private fun update() = synchronized(this) { resultPublisher.submit(calculateEntries()) }
        private fun calculateEntries() = prevResults
            .asSequence()
            .map {
                Triple(
                    it.first,
                    it.second,
                    currResults[it.first],
                )
            }
            .filter { it.third != null }
            .filter { (partyChanges[it.second] ?: it.second) != it.third!!.leader }
            .map {
                val seatFilter = seatFilter
                Entry(
                    it.first,
                    it.second,
                    it.third!!,
                    seatFilter?.contains(it.first) != false,
                )
            }
            .toList()
    }

    private class Entry<T>(val key: T, val prevWinner: Party, val currResult: PartyResult, val filterIncludes: Boolean) {

        val prevColor = prevWinner.color.fadeIfNotSelected()

        val resultColor = currResult.leader.color.fadeIfNotSelected()

        val fill = currResult.elected

        private fun Color.fadeIfNotSelected(): Color = if (filterIncludes) {
            this
        } else {
            ColorUtils.lighten(ColorUtils.lighten(this))
        }
    }
    companion object {
        fun <T> of(
            prevResult: Flow.Publisher<out Map<T, Map<Party, Int>>>,
            prevWinner: Flow.Publisher<out Map<T, Party>>,
            currResult: Flow.Publisher<out Map<T, PartyResult?>>,
            name: T.() -> String,
            header: Flow.Publisher<out String?>,
            numRows: Flow.Publisher<out Int>,
            seatFilter: Flow.Publisher<out Set<T>?>? = null,
            partyChanges: Flow.Publisher<Map<Party, Party>>? = null,
            title: Flow.Publisher<out String?>,
        ): SeatsChangingScreen {
            val inputs = Input<T>()
            prevResult.merge(prevWinner) { r, w -> r to w }.subscribe(Subscriber { (r, w) -> inputs.setPrevResults(r, w) })
            currResult.subscribe(Subscriber { inputs.setCurrResults(it) })
            seatFilter?.subscribe(Subscriber { inputs.setSeatFilter(it) })
            partyChanges?.subscribe(Subscriber { inputs.setPartyChanges(it) })
            val frame = ResultListingFrame(
                headerPublisher = header,
                numRowsPublisher = numRows,
                itemsPublisher = inputs.resultPublisher.mapElements {
                    ResultListingFrame.Item(
                        text = it.key.name(),
                        border = it.prevColor,
                        background = (if (it.fill) it.resultColor else Color.WHITE),
                        foreground = (if (!it.fill) it.resultColor else Color.WHITE),
                    )
                },
            )
            val altText = run {
                val head = title.merge(header) { t, h ->
                    when {
                        t == null && h == null -> ""
                        t == null -> h
                        h == null -> t
                        else -> "$t\n\n$h"
                    }
                }
                val entries = inputs.resultPublisher.map { results ->
                    val filteredResults = results.filter { it.filterIncludes }
                    if (filteredResults.isEmpty()) return@map "(empty)"
                    val allElected = results.all { it.currResult.elected }
                    filteredResults
                        .groupBy { it.prevWinner to it.currResult.leader }
                        .entries
                        .sortedByDescending { group -> group.value.size }
                        .joinToString("\n") { group ->
                            val label = group.key.let { (from, to) -> "${to.abbreviation} GAINS FROM ${from.abbreviation}" }
                            val count = group.value.size
                            val value = if (allElected) {
                                "$count"
                            } else {
                                val elected = group.value.count { it.currResult.elected }
                                "$elected/$count"
                            }
                            "$label: $value"
                        }
                }
                head.merge(entries) { h, e -> "$h\n$e" }
            }
            return SeatsChangingScreen(title, frame, altText)
        }
    }
}
