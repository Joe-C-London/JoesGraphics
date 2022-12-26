package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.mapElements
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.util.concurrent.Flow

class AllSeatsScreen private constructor(title: Flow.Publisher<out String?>, frame: ResultListingFrame) : GenericPanel(pad(frame), title) {
    class Builder<T>(
        prevResultPublisher: Flow.Publisher<out Map<T, Map<Party, Int>>>,
        currResultPublisher: Flow.Publisher<out Map<T, PartyResult?>>,
        private val nameFunc: (T) -> String,
        headerPublisher: Flow.Publisher<out String?>
    ) {
        private val prevResults: Flow.Publisher<out Map<T, Map<Party, Int>>> = prevResultPublisher
        private val currResults: Flow.Publisher<out Map<T, PartyResult?>> = currResultPublisher
        private val header: Flow.Publisher<out String?> = headerPublisher
        private var numRows: Flow.Publisher<out Int> = 20.asOneTimePublisher()
        private var seatFilter: Flow.Publisher<out Set<T>?> = (null as Set<T>?).asOneTimePublisher()

        fun withNumRows(numRowsPublisher: Flow.Publisher<out Int>): Builder<T> {
            numRows = numRowsPublisher
            return this
        }

        fun withSeatFilter(seatFilterPublisher: Flow.Publisher<out Set<T>?>): Builder<T> {
            seatFilter = seatFilterPublisher
            return this
        }

        fun build(titlePublisher: Flow.Publisher<out String?>): AllSeatsScreen {
            val inputs = Input(nameFunc)
            prevResults.subscribe(Subscriber { inputs.setPrevResults(it) })
            currResults.subscribe(Subscriber { inputs.setCurrResults(it) })
            seatFilter.subscribe(Subscriber { inputs.setSeatFilter(it) })
            val frame = ResultListingFrame(
                headerPublisher = header,
                numRowsPublisher = numRows,
                itemsPublisher = inputs.resultPublisher.mapElements {
                    ResultListingFrame.Item(
                        text = nameFunc(it.key),
                        border = it.prevColor,
                        background = if (it.fill) it.resultColor else Color.WHITE,
                        foreground = if (!it.fill) it.resultColor else Color.WHITE
                    )
                }
            )
            return AllSeatsScreen(titlePublisher, frame)
        }
    }

    private class Input<T>(private val nameFunc: (T) -> String) {
        private var prevResults: List<Pair<T, Party>> = emptyList()
        private var currResults: Map<T, PartyResult?> = emptyMap()
        private var seatFilter: Set<T>? = null

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
                            .maxByOrNull { it.value }!!.key
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
                    this.currResults[it.first]
                )
            }
            .map {
                val result = it.third
                Entry(
                    it.first,
                    it.second.color,
                    result?.party?.color ?: Color.BLACK,
                    result?.isElected ?: false
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