package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.utils.ColorUtils.lighten
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.mapElements
import java.awt.Color
import java.util.concurrent.Flow

class SeatsChangingScreen private constructor(title: Flow.Publisher<out String?>, frame: ResultListingFrame) : GenericPanel(pad(frame), title) {
    class Builder<T>(
        prevResultPublisher: Flow.Publisher<out Map<T, Map<Party, Int>>>,
        currResultPublisher: Flow.Publisher<out Map<T, PartyResult?>>,
        private val nameFunc: (T) -> String,
        headerPublisher: Flow.Publisher<out String?>
    ) {

        private val prevResults: Flow.Publisher<out Map<T, Map<Party, Int>>> = prevResultPublisher
        private val currResults: Flow.Publisher<out Map<T, PartyResult?>> = currResultPublisher
        private val header: Flow.Publisher<out String> = headerPublisher
        private var numRows: Flow.Publisher<out Int> = 20.asOneTimePublisher()
        private var seatFilter: Flow.Publisher<out Set<T>?> = Publisher(null)
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

        fun build(titlePublisher: Flow.Publisher<out String?>): SeatsChangingScreen {
            val inputs = Input<T>()
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
                        background = (if (it.fill) it.resultColor else Color.WHITE),
                        foreground = (if (!it.fill) it.resultColor else Color.WHITE)
                    )
                }
            )
            return SeatsChangingScreen(titlePublisher, frame)
        }
    }

    private class Input<T> {
        private var prevResults: List<Pair<T, Party>> = emptyList()
        private var currResults: Map<T, PartyResult?> = emptyMap()
        private var seatFilter: Set<T>? = null
        private var partyChanges: Map<Party, Party> = emptyMap()

        fun setPrevResults(prevResults: Map<T, Map<Party, Int>>) {
            this.prevResults = prevResults.entries
                .asSequence()
                .map {
                    val votes = it.value
                    val total = votes.values.sum()
                    val topTwo = votes.values
                        .sortedDescending()
                        .take(2)
                        .toList()
                    Pair(it, 1.0 * (topTwo[0] - topTwo[1]) / total)
                }
                .sortedBy { it.second }
                .map { it.first }
                .map {
                    Pair(
                        it.key,
                        it.value.entries
                            .maxByOrNull { e -> e.value }!!.key
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
                    currResults[it.first]
                )
            }
            .filter { it.third != null }
            .filter { (partyChanges[it.second] ?: it.second) != it.third!!.party }
            .map {
                val seatFilter = seatFilter
                val colorFunc =
                    if (seatFilter == null || seatFilter.contains(it.first)) {
                        { c: Color -> c }
                    } else {
                        { c -> lighten(lighten(c)) }
                    }
                Entry(
                    it.first,
                    colorFunc(it.second.color),
                    colorFunc(it.third!!.party.color),
                    it.third!!.isElected
                )
            }
            .toList()
    }

    private class Entry<T>(val key: T, val prevColor: Color, val resultColor: Color, val fill: Boolean)
    companion object {
        fun <T> of(
            prevResultPublisher: Flow.Publisher<out Map<T, Map<Party, Int>>>,
            currResultPublisher: Flow.Publisher<out Map<T, PartyResult?>>,
            nameFunc: (T) -> String,
            headerPublisher: Flow.Publisher<out String?>
        ): Builder<T> {
            return Builder(prevResultPublisher, currResultPublisher, nameFunc, headerPublisher)
        }
    }
}
