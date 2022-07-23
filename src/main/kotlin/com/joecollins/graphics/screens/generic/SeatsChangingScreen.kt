package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.utils.ColorUtils.lighten
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.mapElements
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class SeatsChangingScreen private constructor(title: JLabel, frame: ResultListingFrame) : JPanel() {
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

        fun withNumRows(numRowsPublisher: Flow.Publisher<out Int>): Builder<T> {
            numRows = numRowsPublisher
            return this
        }

        fun withSeatFilter(seatFilterPublisher: Flow.Publisher<out Set<T>?>): Builder<T> {
            seatFilter = seatFilterPublisher
            return this
        }

        fun build(titlePublisher: Flow.Publisher<out String?>): SeatsChangingScreen {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            titlePublisher.subscribe(Subscriber(eventQueueWrapper { headerLabel.text = it }))
            val inputs = Input<T>()
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
                        background = (if (it.fill) it.resultColor else Color.WHITE),
                        foreground = (if (!it.fill) it.resultColor else Color.WHITE)
                    )
                }
            )
            return SeatsChangingScreen(headerLabel, frame)
        }
    }

    private class Input<T> {
        private var prevResults: List<Pair<T, Party>> = emptyList()
        private var currResults: Map<T, PartyResult?> = emptyMap()
        private var seatFilter: Set<T>? = null

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
                            .maxByOrNull { e -> e.value }
                        !!.key
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

        val resultPublisher = Publisher(calculateEntries())

        private fun update() = synchronized(this) { resultPublisher.submit(calculateEntries()) }
        private fun calculateEntries() = prevResults
            .asSequence()
            .map {
                Triple(
                    it.first,
                    it.second,
                    currResults[it.first] ?: PartyResult.NO_RESULT
                )
            }
            .filter { it.third.party != null }
            .filter { it.second != it.third.party }
            .map {
                val seatFilter = seatFilter
                val colorFunc =
                    if (seatFilter == null || seatFilter.contains(it.first)) { c: Color -> c }
                    else { c -> lighten(lighten(c)) }
                Entry(
                    it.first,
                    colorFunc(it.second.color),
                    colorFunc(it.third.party!!.color),
                    it.third.isElected
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

    init {
        background = Color.WHITE
        layout = BorderLayout()
        add(title, BorderLayout.NORTH)
        val panel = JPanel()
        panel.background = Color.WHITE
        panel.border = EmptyBorder(5, 5, 5, 5)
        panel.layout = GridLayout(1, 1)
        panel.add(frame)
        add(panel, BorderLayout.CENTER)
    }
}
