package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.bindings.mapElements
import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.mapElements
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class AllSeatsScreen private constructor(title: JLabel, frame: ResultListingFrame) : JPanel() {
    class Builder<T>(
        prevResultBinding: Binding<out Map<T, Map<Party, Int>>>,
        currResultBinding: Binding<out Map<T, PartyResult?>>,
        private val nameFunc: (T) -> String,
        headerPublisher: Flow.Publisher<out String?>
    ) {
        private val prevResults: BindingReceiver<Map<T, Map<Party, Int>>> = BindingReceiver(prevResultBinding)
        private val currResults: BindingReceiver<Map<T, PartyResult?>> = BindingReceiver(currResultBinding)
        private val header: Flow.Publisher<out String?> = headerPublisher
        private var numRows: Flow.Publisher<out Int> = 20.asOneTimePublisher()
        private var seatFilter = BindingReceiver<Set<T>?>(Binding.fixedBinding(null))

        fun withNumRows(numRowsPublisher: Flow.Publisher<out Int>): Builder<T> {
            numRows = numRowsPublisher
            return this
        }

        fun withSeatFilter(seatFilterBinding: Binding<out Set<T>?>): Builder<T> {
            seatFilter = BindingReceiver(seatFilterBinding)
            return this
        }

        fun build(titleBinding: Binding<out String?>): AllSeatsScreen {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            titleBinding.bind { headerLabel.text = it }
            val inputs = Input(nameFunc)
            prevResults.getBinding().bind { inputs.setPrevResults(it) }
            currResults.getBinding().bind { inputs.setCurrResults(it) }
            seatFilter.getBinding().bind { inputs.setSeatFilter(it) }
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
            return AllSeatsScreen(headerLabel, frame)
        }
    }

    private class Input<T>(private val nameFunc: (T) -> String) : Bindable<Input<T>, Input.Property>() {
        private enum class Property {
            PREV, CURR, FILTER
        }

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
                .sortedBy { e -> nameFunc(e.first.key).uppercase() }
                .map { it.first }
                .map { e ->
                    Pair(
                        e.key,
                        e.value.entries
                            .maxByOrNull { it.value }
                        !!.key
                    )
                }
                .toList()
            _resultPublisher.submit(toEntries())
        }

        fun setCurrResults(currResults: Map<T, PartyResult?>) {
            this.currResults = currResults
            _resultPublisher.submit(toEntries())
        }

        fun setSeatFilter(seatFilter: Set<T>?) {
            this.seatFilter = seatFilter
            _resultPublisher.submit(toEntries())
        }

        val _resultPublisher = Publisher(toEntries())
        val resultPublisher: Flow.Publisher<out List<Entry<T>>> = _resultPublisher

        private fun toEntries() = this.prevResults
            .asSequence()
            .filter { e: Pair<T, Party> -> this.seatFilter?.contains(e.first) ?: true }
            .map { e: Pair<T, Party> ->
                Triple(
                    e.first,
                    e.second,
                    this.currResults[e.first] ?: PartyResult.NO_RESULT
                )
            }
            .map { e: Triple<T, Party, PartyResult?> ->
                val result = e.third ?: PartyResult.NO_RESULT
                Entry(
                    e.first,
                    e.second.color,
                    result.party?.color ?: Color.BLACK,
                    result.isElected
                )
            }
            .toList()
    }

    private class Entry<T>(val key: T, val prevColor: Color, val resultColor: Color, val fill: Boolean)
    companion object {
        @JvmStatic fun <T> of(
            prevResultBinding: Binding<out Map<T, Map<Party, Int>>>,
            currResultBinding: Binding<out Map<T, PartyResult?>>,
            nameFunc: (T) -> String,
            headerPublisher: Flow.Publisher<out String?>
        ): Builder<T> {
            return Builder(prevResultBinding, currResultBinding, nameFunc, headerPublisher)
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
