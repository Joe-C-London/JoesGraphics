package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.bindings.Binding.Companion.sizeBinding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.ImmutableTriple
import org.apache.commons.lang3.tuple.Pair

class AllSeatsScreen private constructor(title: JLabel, frame: ResultListingFrame) : JPanel() {
    class Builder<T>(
        prevResultBinding: Binding<Map<T, Map<Party, Int>>>,
        currResultBinding: Binding<Map<T, PartyResult?>>,
        private val nameFunc: (T) -> String,
        headerBinding: Binding<String?>
    ) {
        private val prevResults: BindingReceiver<Map<T, Map<Party, Int>>> = BindingReceiver(prevResultBinding)
        private val currResults: BindingReceiver<Map<T, PartyResult?>> = BindingReceiver(currResultBinding)
        private val header: BindingReceiver<String?> = BindingReceiver(headerBinding)
        private var numRows = BindingReceiver(Binding.fixedBinding(20))
        private var seatFilter = BindingReceiver<Set<T>?>(Binding.fixedBinding(null))

        fun withNumRows(numRowsBinding: Binding<Int>): Builder<T> {
            numRows = BindingReceiver(numRowsBinding)
            return this
        }

        fun withSeatFilter(seatFilterBinding: Binding<Set<T>?>): Builder<T> {
            seatFilter = BindingReceiver(seatFilterBinding)
            return this
        }

        fun build(titleBinding: Binding<String?>): AllSeatsScreen {
            val headerLabel = JLabel()
            headerLabel.font = readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            titleBinding.bind { headerLabel.text = it }
            val inputs = Input(nameFunc)
            prevResults.getBinding().bind { inputs.setPrevResults(it) }
            currResults.getBinding().bind { inputs.setCurrResults(it) }
            seatFilter.getBinding().bind { inputs.setSeatFilter(it) }
            val entries = BindableList<Entry<T>>()
            inputs.resultBinding.bind { entries.setAll(it) }
            val frame = ResultListingFrame()
            frame.setHeaderBinding(header.getBinding())
            frame.setNumRowsBinding(numRows.getBinding())
            frame.setNumItemsBinding(sizeBinding(entries))
            frame.setTextBinding(IndexedBinding.propertyBinding(entries) { nameFunc(it.key) })
            frame.setBorderBinding(IndexedBinding.propertyBinding(entries) { it.prevColor })
            frame.setBackgroundBinding(
                    IndexedBinding.propertyBinding(entries) { e: Entry<T> -> if (e.fill) e.resultColor else Color.WHITE })
            frame.setForegroundBinding(
                    IndexedBinding.propertyBinding(entries) { e: Entry<T> -> if (!e.fill) e.resultColor else Color.WHITE })
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
                        ImmutablePair.of(e, 1.0 * (topTwo[0] - topTwo[1]) / total)
                    }
                    .sortedBy { e -> nameFunc(e.getLeft().key).toUpperCase() }
                    .map { it.key }
                    .map { e ->
                        ImmutablePair.of(
                                e.key,
                                e.value.entries
                                        .maxByOrNull { it.value }
                                        !!.key
                        ) as Pair<T, Party>
                    }
                    .toList()
            onPropertyRefreshed(Property.PREV)
        }

        fun setCurrResults(currResults: Map<T, PartyResult?>) {
            this.currResults = currResults
            onPropertyRefreshed(Property.CURR)
        }

        fun setSeatFilter(seatFilter: Set<T>?) {
            this.seatFilter = seatFilter
            onPropertyRefreshed(Property.FILTER)
        }

        val resultBinding: Binding<List<Entry<T>>>
            get() = Binding.propertyBinding(
                    this,
                    { t: Input<T> ->
                        t.prevResults
                                .asSequence()
                                .filter { e: Pair<T, Party> -> t.seatFilter?.contains(e.key) ?: true }
                                .map { e: Pair<T, Party> ->
                                    ImmutableTriple.of(
                                            e.left,
                                            e.right,
                                            t.currResults[e.left] ?: PartyResult.NO_RESULT)
                                }
                                .map { e: ImmutableTriple<T, Party, PartyResult?> ->
                                    val result = e.getRight() ?: PartyResult.NO_RESULT
                                    Entry(
                                            e.getLeft(),
                                            e.getMiddle().color,
                                            result.party?.color ?: Color.BLACK,
                                            result.isElected)
                                }
                                .toList()
                    },
                    Property.PREV,
                    Property.CURR,
                    Property.FILTER)
    }

    private class Entry<T>(val key: T, val prevColor: Color, val resultColor: Color, val fill: Boolean)
    companion object {
        @JvmStatic fun <T> of(
            prevResultBinding: Binding<Map<T, Map<Party, Int>>>,
            currResultBinding: Binding<Map<T, PartyResult?>>,
            nameFunc: (T) -> String,
            headerBinding: Binding<String?>
        ): Builder<T> {
            return Builder(prevResultBinding, currResultBinding, nameFunc, headerBinding)
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