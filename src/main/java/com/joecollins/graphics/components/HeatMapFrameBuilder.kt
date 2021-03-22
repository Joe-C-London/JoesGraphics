package com.joecollins.graphics.components

import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import java.awt.Color

class HeatMapFrameBuilder {
    private val frame = HeatMapFrame()
    fun <T> withSeatBars(
        bars: BindableList<T>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        labelBinding: Binding<String>
    ): HeatMapFrameBuilder {
        frame.setNumSeatBarsBinding(Binding.sizeBinding(bars))
        frame.setSeatBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc))
        frame.setSeatBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc))
        frame.setSeatBarLabelBinding(labelBinding)
        return this
    }

    fun <T> withChangeBars(
        bars: BindableList<T>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        startBinding: Binding<Int>,
        labelBinding: Binding<String>
    ): HeatMapFrameBuilder {
        frame.setNumChangeBarsBinding(Binding.sizeBinding(bars))
        frame.setChangeBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc))
        frame.setChangeBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc))
        frame.setChangeBarStartBinding(startBinding)
        frame.setChangeBarLabelBinding(labelBinding)
        return this
    }

    fun withHeader(headerBinding: Binding<String?>): HeatMapFrameBuilder {
        frame.setHeaderBinding(headerBinding)
        return this
    }

    fun withBorder(colorBinding: Binding<Color>): HeatMapFrameBuilder {
        frame.setBorderColorBinding(colorBinding)
        return this
    }

    fun build(): HeatMapFrame {
        return frame
    }

    companion object {
        @JvmStatic fun <T> of(
            numRows: Binding<Int>,
            entries: List<T>,
            colorFunc: (T) -> Binding<Color>
        ): HeatMapFrameBuilder {
            return of(numRows, entries, colorFunc, colorFunc)
        }

        @JvmStatic fun <T> of(
            numRows: Binding<Int>,
            entries: List<T>,
            fillFunc: (T) -> Binding<Color>,
            borderFunc: (T) -> Binding<Color>
        ): HeatMapFrameBuilder {
            val builder = HeatMapFrameBuilder()
            builder.frame.setNumRowsBinding(numRows)
            builder.frame.setNumSquaresBinding(Binding.fixedBinding(entries.size))
            builder.frame.setSquareFillBinding(IndexedBinding.listBinding(entries, fillFunc))
            builder.frame.setSquareBordersBinding(IndexedBinding.listBinding(entries, borderFunc))
            return builder
        }

        @JvmStatic fun <T> ofClustered(
            numRows: Binding<Int>,
            entries: List<T>,
            seatFunc: (T) -> Int,
            fillFunc: (T) -> Binding<Color>,
            borderFunc: (T) -> Binding<Color>
        ): HeatMapFrameBuilder {
            val allEntries = entries
                    .flatMap { generateSequence { it }.take(seatFunc(it)) }
                    .toList()
            return of(numRows, allEntries, fillFunc, borderFunc)
        }

        @JvmStatic fun <T> ofElectedLeading(
            rows: Binding<Int>,
            entries: List<T>,
            resultFunc: (T) -> Binding<PartyResult?>,
            prevResultFunc: (T) -> Party,
            party: Party,
            seatLabel: (Int, Int) -> String,
            showChange: (Int, Int) -> Boolean,
            changeLabel: (Int, Int) -> String,
            header: Binding<String?>
        ): HeatMapFrame {
            return ofElectedLeading(
                    rows,
                    entries,
                    { 1 },
                    resultFunc,
                    prevResultFunc,
                    party,
                    seatLabel,
                    showChange,
                    changeLabel,
                    header)
        }

        @JvmStatic fun <T> ofElectedLeading(
            rows: Binding<Int>,
            entries: List<T>,
            seatsFunc: (T) -> Int,
            resultFunc: (T) -> Binding<PartyResult?>,
            prevResultFunc: (T) -> Party,
            party: Party,
            seatLabel: (Int, Int) -> String,
            showChange: (Int, Int) -> Boolean,
            changeLabel: (Int, Int) -> String,
            header: Binding<String?>
        ): HeatMapFrame {
            val results: Map<T, BindingReceiver<PartyResult?>> = entries
                    .distinct()
                    .associateWith { BindingReceiver(resultFunc(it)) }
            val prev = entries.distinct().associateWith(prevResultFunc)
            val resultBindings = entries
                    .map { e: T ->
                        BindingReceiver(results[e]!!.getBinding { Pair(it, seatsFunc(e)) })
                    }
                    .toList()
            val resultWithPrevBindings: List<BindingReceiver<Triple<PartyResult?, Party, Int>>> = entries
                    .map { e: T ->
                        BindingReceiver(results[e]!!.getBinding { Triple(it, prev[e]!!, seatsFunc(e)) })
                    }
                    .toList()
            val seatList = BindableList<Pair<Color, Int>>()
            val seats = createSeatBarBinding(resultBindings, seatList, { party == it }, party.color)
            val changeList = BindableList<Pair<Color, Int>>()
            val change = createChangeBarBinding(
                    resultWithPrevBindings, changeList, { party == it }, party.color, showChange)
            val changeLabelFunc = { p: Pair<Int, Int> -> if (showChange(p.first, p.second)) changeLabel(p.first, p.second) else "" }
            val allPrevs = entries
                    .map { Pair(prev[it]!!, seatsFunc(it)) }
                    .toList()
            return ofClustered(
                    rows,
                    entries,
                    seatsFunc,
                    { e: T ->
                        results[e]!!.getBinding {
                                    when {
                                        it?.party == null -> Color.WHITE
                                        it.isElected -> it.party.color
                                        else -> ColorUtils.lighten(it.party.color)
                                    }
                                }
                    },
                    { Binding.fixedBinding(prevResultFunc(it).color) })
                    .withSeatBars(
                            seatList, { it.first }, { it.second },
                            seats.getBinding { seatLabel(it.first, it.second) })
                    .withChangeBars(
                            changeList, { it.first }, { it.second },
                            Binding.fixedBinding(calcPrevForParty(allPrevs, party)),
                            change.getBinding(changeLabelFunc))
                    .withHeader(header)
                    .withBorder(Binding.fixedBinding(party.color))
                    .build()
        }

        private fun calcPrevForParty(prev: List<Pair<Party, Int>>, party: Party): Int {
            return prev.filter { party == it.first }.map { it.second }.sum()
        }

        private fun createSeatBarBinding(
                results: List<BindingReceiver<Pair<PartyResult?, Int>>>,
                list: BindableList<Pair<Color, Int>>,
                partyFilter: (Party?) -> Boolean,
                color: Color
        ): BindingReceiver<Pair<Int, Int>> {
            val binding = Binding.mapReduceBinding(
                    results.map { it.getBinding() }.toList(),
                    Pair(0, 0),
                    { p: Pair<Int, Int>, r: Pair<PartyResult?, Int> ->
                        val left = r.first
                        if (left == null || !partyFilter(left.party)) { p } else {
                            Pair(p.first + if (left.isElected) r.second else 0, p.second + r.second)
                        }
                    },
                    { p: Pair<Int, Int>, r: Pair<PartyResult?, Int> ->
                        val left = r.first
                        if (left == null || !partyFilter(left.party)) { p } else {
                            Pair(p.first - if (left.isElected) r.second else 0, p.second - r.second)
                        }
                    })
            val seats = BindingReceiver(binding)
            seats.getBinding()
                    .bind {
                        list.setAll(
                                listOf(
                                        Pair(color, it.first),
                                        Pair(ColorUtils.lighten(color), it.second - it.first)))
                    }
            return seats
        }

        private fun createChangeBarBinding(
                resultWithPrev: List<BindingReceiver<Triple<PartyResult?, Party, Int>>>,
                list: BindableList<Pair<Color, Int>>,
                partyFilter: (Party?) -> Boolean,
                color: Color,
                showChangeBars: (Int, Int) -> Boolean
        ): BindingReceiver<Pair<Int, Int>> {
            val binding = Binding.mapReduceBinding(
                    resultWithPrev.map { it.getBinding() }.toList(),
                    Pair(0, 0),
                    { p: Pair<Int, Int>, r: Triple<PartyResult?, Party, Int> ->
                        val left = r.first
                        if (left?.party == null) {
                            p
                        } else {
                            var ret = p
                            if (partyFilter(left.party)) {
                                ret = Pair(
                                        ret.first + if (left.isElected) r.third else 0,
                                        ret.second + r.third)
                            }
                            if (partyFilter(r.second)) {
                                ret = Pair(
                                        ret.first - if (left.isElected) r.third else 0,
                                        ret.second - r.third)
                            }
                            ret
                        }
                    },
                    { p: Pair<Int, Int>, r: Triple<PartyResult?, Party, Int> ->
                        val left = r.first
                        if (left?.party == null) {
                            p
                        } else {
                            var ret = p
                            if (partyFilter(left.party)) {
                                ret = Pair(
                                        ret.first - if (left.isElected) r.third else 0,
                                        ret.second - r.third)
                            }
                            if (partyFilter(r.second)) {
                                ret = Pair(
                                        ret.first + if (left.isElected) r.third else 0,
                                        ret.second + r.third)
                            }
                            ret
                        }
                    })
            val seats = BindingReceiver(binding)
            seats.getBinding()
                    .bind {
                        if (showChangeBars(it.first, it.second)) {
                            list.setAll(
                                    listOf(
                                            Pair(color, it.first),
                                            Pair(ColorUtils.lighten(color), it.second - it.first)))
                        } else {
                            list.clear()
                        }
                    }
            return seats
        }
    }
}
