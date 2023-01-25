package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.util.concurrent.Flow

class AllSeatsScreen private constructor(title: Flow.Publisher<out String?>, frame: ResultListingFrame, altText: Flow.Publisher<String>) : GenericPanel(pad(frame), title, altText) {
    class Builder<T>(
        prevResultPublisher: Flow.Publisher<out Map<T, Map<Party, Int>>>,
        currResultPublisher: Flow.Publisher<out Map<T, PartyResult?>>,
        private val nameFunc: (T) -> String,
        headerPublisher: Flow.Publisher<out String?>,
    ) {
        private val prevResults: Flow.Publisher<out Map<T, Map<Party, Int>>> = prevResultPublisher
        private val currResults: Flow.Publisher<out Map<T, PartyResult?>> = currResultPublisher
        private val header: Flow.Publisher<out String?> = headerPublisher
        private var numRows: Flow.Publisher<out Int> = 20.asOneTimePublisher()
        private var seatFilter: Flow.Publisher<out Set<T>?> = (null as Set<T>?).asOneTimePublisher()
        private var partyChanges: Flow.Publisher<Map<Party, Party>> = Publisher(emptyMap())

        fun withNumRows(numRowsPublisher: Flow.Publisher<out Int>): Builder<T> {
            numRows = numRowsPublisher
            return this
        }

        fun withSeatFilter(seatFilterPublisher: Flow.Publisher<out Set<T>?>): Builder<T> {
            seatFilter = seatFilterPublisher
            return this
        }

        fun withPartyChanges(changes: Flow.Publisher<Map<Party, Party>>): Builder<T> {
            partyChanges = changes
            return this
        }

        fun build(titlePublisher: Flow.Publisher<out String?>): AllSeatsScreen {
            val inputs = Input(nameFunc)
            prevResults.subscribe(Subscriber { inputs.setPrevResults(it) })
            currResults.subscribe(Subscriber { inputs.setCurrResults(it) })
            seatFilter.subscribe(Subscriber { inputs.setSeatFilter(it) })
            partyChanges.subscribe(Subscriber { inputs.setPartyChanges(it) })
            val frame = ResultListingFrame(
                headerPublisher = header,
                numRowsPublisher = numRows,
                itemsPublisher = inputs.resultPublisher.mapElements {
                    ResultListingFrame.Item(
                        text = nameFunc(it.key),
                        border = it.prevColor,
                        background = if (it.fill) it.resultColor else Color.WHITE,
                        foreground = if (!it.fill) it.resultColor else Color.WHITE,
                    )
                },
            )
            val altText = run {
                val head = titlePublisher.merge(header) { t, h -> "$t\n\n$h" }
                val entries = inputs.resultPublisher.map { results ->
                    if (results.isEmpty()) return@map "(empty)"
                    val allElected = results.all { it.currResult?.isElected ?: true }
                    results
                        .groupBy { it.prevWinner to it.currResult?.party }
                        .entries
                        .sortedByDescending { group -> group.value.size }
                        .joinToString("\n") { group ->
                            val label = group.key.let { (from, to) ->
                                when (to) {
                                    null -> "PENDING ${from.abbreviation}"
                                    from -> "${to.abbreviation} HOLD"
                                    inputs.changedParty(from) -> "${to.abbreviation} HOLD (${from.abbreviation})"
                                    else -> "${to.abbreviation} GAIN FROM ${from.abbreviation}"
                                }
                            }
                            val count = group.value.size
                            val value = if (allElected || group.key.second == null) {
                                "$count"
                            } else {
                                val elected = group.value.count { it.currResult?.elected ?: false }
                                "$elected/$count"
                            }
                            "$label: $value"
                        }
                }
                head.merge(entries) { h, e -> "$h\n$e" }
            }
            return AllSeatsScreen(titlePublisher, frame, altText)
        }
    }

    private class Input<T>(private val nameFunc: (T) -> String) {
        private var prevResults: List<Pair<T, Party>> = emptyList()
        private var currResults: Map<T, PartyResult?> = emptyMap()
        private var seatFilter: Set<T>? = null
        private var partyChanges: Map<Party, Party> = emptyMap()

        fun changedParty(party: Party): Party = partyChanges[party] ?: party

        fun setPrevResults(prevResults: Map<T, Map<Party, Int>>) {
            this.prevResults = prevResults.entries
                .asSequence()
                .map { e ->
                    val votes = e.value
                    val total = votes.values.sum()
                    val topTwo = votes.values
                        .sortedDescending()
                        .take(2)
                        .toList()
                    Pair(e, 1.0 * (topTwo[0] - topTwo[1]) / total)
                }
                .sortedBy { e -> StringUtils.stripAccents(nameFunc(e.first.key)).uppercase() }
                .map { it.first }
                .map { e ->
                    Pair(
                        e.key,
                        e.value.entries
                            .maxByOrNull { it.value }!!.key,
                    )
                }
                .toList()
            publishResults()
        }

        fun setCurrResults(currResults: Map<T, PartyResult?>) {
            this.currResults = currResults
            publishResults()
        }

        fun setSeatFilter(seatFilter: Set<T>?) {
            this.seatFilter = seatFilter
            publishResults()
        }

        fun setPartyChanges(changes: Map<Party, Party>) {
            this.partyChanges = changes
            publishResults()
        }

        private fun publishResults() {
            (resultPublisher as Publisher<List<Entry<T>>>).submit(toEntries())
        }

        val resultPublisher: Flow.Publisher<List<Entry<T>>> = Publisher(toEntries())

        private fun toEntries() = this.prevResults
            .asSequence()
            .filter { this.seatFilter?.contains(it.first) ?: true }
            .map {
                Triple(
                    it.first,
                    it.second,
                    this.currResults[it.first],
                )
            }
            .map {
                val result = it.third
                Entry(
                    it.first,
                    it.second,
                    result,
                )
            }
            .toList()
    }

    private class Entry<T>(val key: T, val prevWinner: Party, val currResult: PartyResult?) {

        val prevColor = prevWinner.color

        val resultColor: Color = currResult?.party?.color ?: Color.LIGHT_GRAY
        val fill = currResult?.isElected ?: false
    }
    companion object {
        fun <T> of(
            prevResultPublisher: Flow.Publisher<out Map<T, Map<Party, Int>>>,
            currResultPublisher: Flow.Publisher<out Map<T, PartyResult?>>,
            nameFunc: (T) -> String,
            headerPublisher: Flow.Publisher<out String?>,
        ): Builder<T> {
            return Builder(prevResultPublisher, currResultPublisher, nameFunc, headerPublisher)
        }
    }
}
