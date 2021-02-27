package com.joecollins.graphics.components

import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import java.awt.Color
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.ImmutableTriple
import org.apache.commons.lang3.tuple.Pair
import org.apache.commons.lang3.tuple.Triple

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
                        BindingReceiver<Pair<PartyResult?, Int>>(
                                results[e]!!.getBinding { ImmutablePair.of(it, seatsFunc(e)) })
                    }
                    .toList()
            val resultWithPrevBindings: List<BindingReceiver<Triple<PartyResult?, Party, Int>>> = entries
                    .map { e: T ->
                        BindingReceiver<Triple<PartyResult?, Party, Int>>(
                                results[e]!!.getBinding { ImmutableTriple.of(it, prev[e], seatsFunc(e)) })
                    }
                    .toList()
            val seatList = BindableList<ImmutablePair<Color, Int>>()
            val seats = createSeatBarBinding<T>(resultBindings, seatList, { party == it }, party.color)
            val changeList = BindableList<ImmutablePair<Color, Int>>()
            val change = createChangeBarBinding<T>(
                    resultWithPrevBindings, changeList, { party == it }, party.color, showChange)
            val changeLabelFunc = { p: ImmutablePair<Int, Int> -> if (showChange(p.left, p.right)) changeLabel(p.left, p.right) else "" }
            val allPrevs = entries
                    .map { ImmutablePair.of(prev[it]!!, seatsFunc(it)) as Pair<Party, Int> }
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
                            seatList, { it.getLeft() }, { it.getRight() },
                            seats.getBinding { seatLabel(it.left, it.right) })
                    .withChangeBars(
                            changeList, { it.getLeft() }, { it.getRight() },
                            Binding.fixedBinding(calcPrevForParty<T>(allPrevs, party)),
                            change.getBinding(changeLabelFunc))
                    .withHeader(header)
                    .withBorder(Binding.fixedBinding(party.color))
                    .build()
        }

        private fun <T> calcPrevForParty(prev: List<Pair<Party, Int>>, party: Party): Int {
            return prev.filter { party == it.left }.map { it.right }.sum()
        }

        private fun <T> createSeatBarBinding(
            results: List<BindingReceiver<Pair<PartyResult?, Int>>>,
            list: BindableList<ImmutablePair<Color, Int>>,
            partyFilter: (Party?) -> Boolean,
            color: Color
        ): BindingReceiver<ImmutablePair<Int, Int>> {
            val binding = Binding.mapReduceBinding(
                    results.map { it.getBinding() }.toList(),
                    ImmutablePair.of(0, 0),
                    { p: ImmutablePair<Int, Int>, r: Pair<PartyResult?, Int> ->
                        val left = r.left
                        if (left == null || !partyFilter(left.party)) { p } else {
                            ImmutablePair.of(
                                    p.left + if (left.isElected) r.right else 0, p.right + r.right)
                        }
                    },
                    { p: ImmutablePair<Int, Int>, r: Pair<PartyResult?, Int> ->
                        val left = r.left
                        if (left == null || !partyFilter(left.party)) { p } else {
                            ImmutablePair.of(
                                    p.left - if (left.isElected) r.right else 0, p.right - r.right)
                        }
                    })
            val seats = BindingReceiver(binding)
            seats.getBinding()
                    .bind {
                        list.setAll(
                                listOf(
                                        ImmutablePair.of(color, it.left),
                                        ImmutablePair.of(ColorUtils.lighten(color), it.right - it.left)))
                    }
            return seats
        }

        private fun <T> createChangeBarBinding(
            resultWithPrev: List<BindingReceiver<Triple<PartyResult?, Party, Int>>>,
            list: BindableList<ImmutablePair<Color, Int>>,
            partyFilter: (Party?) -> Boolean,
            color: Color,
            showChangeBars: (Int, Int) -> Boolean
        ): BindingReceiver<ImmutablePair<Int, Int>> {
            val binding = Binding.mapReduceBinding(
                    resultWithPrev.map { it.getBinding() }.toList(),
                    ImmutablePair.of(0, 0),
                    { p: ImmutablePair<Int, Int>, r: Triple<PartyResult?, Party, Int> ->
                        val left = r.left
                        if (left?.party == null) {
                            p
                        } else {
                            var ret = p
                            if (partyFilter(left.party)) {
                                ret = ImmutablePair.of(
                                        ret.left + if (left.isElected) r.right else 0,
                                        ret.right + r.right)
                            }
                            if (partyFilter(r.middle)) {
                                ret = ImmutablePair.of(
                                        ret.left - if (left.isElected) r.right else 0,
                                        ret.right - r.right)
                            }
                            ret
                        }
                    },
                    { p: ImmutablePair<Int, Int>, r: Triple<PartyResult?, Party, Int> ->
                        val left = r.left
                        if (left?.party == null) {
                            p
                        } else {
                            var ret = p
                            if (partyFilter(left.party)) {
                                ret = ImmutablePair.of(
                                        ret.left - if (left.isElected) r.right else 0,
                                        ret.right - r.right)
                            }
                            if (partyFilter(r.middle)) {
                                ret = ImmutablePair.of(
                                        ret.left + if (left.isElected) r.right else 0,
                                        ret.right + r.right)
                            }
                            ret
                        }
                    })
            val seats = BindingReceiver(binding)
            seats.getBinding()
                    .bind {
                        if (showChangeBars(it.left, it.right)) {
                            list.setAll(
                                    listOf(
                                            ImmutablePair.of(color, it.left),
                                            ImmutablePair.of(ColorUtils.lighten(color), it.right - it.left)))
                        } else {
                            list.clear()
                        }
                    }
            return seats
        }
    }
}
