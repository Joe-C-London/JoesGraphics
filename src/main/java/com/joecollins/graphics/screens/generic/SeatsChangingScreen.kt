package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.bindings.mapElements
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.utils.ColorUtils.lighten
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class SeatsChangingScreen private constructor(title: JLabel, frame: ResultListingFrame) : JPanel() {
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

        fun build(titleBinding: Binding<String?>): SeatsChangingScreen {
            val headerLabel = JLabel()
            headerLabel.font = readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            titleBinding.bind { headerLabel.text = it }
            val inputs = Input<T>()
            prevResults.getBinding().bind { inputs.setPrevResults(it) }
            currResults.getBinding().bind { inputs.setCurrResults(it) }
            seatFilter.getBinding().bind { inputs.setSeatFilter(it) }
            val frame = ResultListingFrame(
                headerBinding = header.getBinding(),
                numRowsBinding = numRows.getBinding(),
                itemsBinding = inputs.resultBinding.mapElements {
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

    private class Input<T> : Bindable<Input<T>, Input.Property>() {
        private enum class Property {
            PREV, CURR, FILTER
        }

        private var prevResults: List<Pair<T, Party>> = emptyList()
        private var currResults: Map<T, PartyResult?> = emptyMap()
        private var seatFilter: Set<T>? = null

        fun setPrevResults(prevResults: Map<T, Map<Party, Int>>) {
            this.prevResults = prevResults.entries
                    .asSequence()
                    .map { e: Map.Entry<T, Map<Party, Int>> ->
                        val votes = e.value
                        val total = votes.values.sum()
                        val topTwo = votes.values
                                .sortedDescending()
                                .take(2)
                                .toList()
                        Pair(e, 1.0 * (topTwo[0] - topTwo[1]) / total)
                    }
                    .sortedBy { it.second }
                    .map { it.first }
                    .map { it: Map.Entry<T, Map<Party, Int>> ->
                        Pair(
                                it.key,
                                it.value.entries
                                        .maxByOrNull { it.value }
                                        !!.key)
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
                                .map {
                                    Triple(
                                            it.first,
                                            it.second,
                                            t.currResults[it.first] ?: PartyResult.NO_RESULT)
                                }
                                .filter { it.third.party != null }
                                .filter { it.second != it.third.party }
                                .map {
                                    val seatFilter = t.seatFilter
                                    val colorFunc = if (seatFilter == null || seatFilter.contains(it.first)) { c: Color -> c } else { c: Color -> lighten(lighten(c)) }
                                    Entry(
                                            it.first,
                                            colorFunc(it.second.color),
                                            colorFunc(it.third.party!!.color),
                                            it.third.isElected)
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
