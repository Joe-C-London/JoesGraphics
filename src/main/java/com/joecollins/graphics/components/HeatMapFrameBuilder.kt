package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.bindings.mapElements
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import java.awt.Color

class HeatMapFrameBuilder {
    private var seatBarsBinding: Binding<List<HeatMapFrame.Bar>>? = null
    private var seatBarLabelBinding: Binding<String>? = null
    private var changeBarsBinding: Binding<List<HeatMapFrame.Bar>>? = null
    private var changeBarStartBinding: Binding<Int>? = null
    private var changeBarLabelBinding: Binding<String>? = null
    private var headerBinding: Binding<String?>? = null
    private var borderColorBinding: Binding<Color>? = null
    private var numRowsBinding: Binding<Int>? = null
    private var squaresBinding: Binding<List<HeatMapFrame.Square>>? = null

    fun <T> withSeatBars(
        bars: Binding<List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        labelBinding: Binding<String>
    ): HeatMapFrameBuilder {
        this.seatBarsBinding = bars.mapElements { HeatMapFrame.Bar(colorFunc(it), seatFunc(it)) }
        this.seatBarLabelBinding = labelBinding
        return this
    }

    fun <T> withChangeBars(
        bars: Binding<List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        startBinding: Binding<Int>,
        labelBinding: Binding<String>
    ): HeatMapFrameBuilder {
        this.changeBarsBinding = bars.mapElements { HeatMapFrame.Bar(colorFunc(it), seatFunc(it)) }
        this.changeBarStartBinding = startBinding
        this.changeBarLabelBinding = labelBinding
        return this
    }

    fun withHeader(headerBinding: Binding<String?>): HeatMapFrameBuilder {
        this.headerBinding = headerBinding
        return this
    }

    fun withBorder(colorBinding: Binding<Color>): HeatMapFrameBuilder {
        this.borderColorBinding = colorBinding
        return this
    }

    fun build(): HeatMapFrame {
        val heatMapFrame = HeatMapFrame()
        seatBarsBinding?.let { heatMapFrame.setSeatBarsBinding(it) }
        seatBarLabelBinding?.let { heatMapFrame.setSeatBarLabelBinding(it) }
        changeBarsBinding?.let { heatMapFrame.setChangeBarsBinding(it) }
        changeBarStartBinding?.let { heatMapFrame.setChangeBarStartBinding(it) }
        changeBarLabelBinding?.let { heatMapFrame.setChangeBarLabelBinding(it) }
        headerBinding?.let { heatMapFrame.setHeaderBinding(it) }
        borderColorBinding?.let { heatMapFrame.setBorderColorBinding(it) }
        numRowsBinding?.let { heatMapFrame.setNumRowsBinding(it) }
        squaresBinding?.let { heatMapFrame.setSquaresBinding(it) }
        return heatMapFrame
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
            builder.numRowsBinding = numRows
            builder.squaresBinding = Binding.listBinding(
                entries.map {
                    fillFunc(it).merge(borderFunc(it)) {
                        fill, border -> HeatMapFrame.Square(fillColor = fill, borderColor = border)
                    }
                }
            )
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
            val seats = createSeatBarBinding(resultBindings) { party == it }
            val seatList = seats.getBinding {
                        listOf(
                            Pair(party.color, it.first),
                            Pair(ColorUtils.lighten(party.color), it.second - it.first))
                }

            val change = createChangeBarBinding(resultWithPrevBindings) { party == it }
            val changeLabelFunc = { p: Pair<Int, Int> -> if (showChange(p.first, p.second)) changeLabel(p.first, p.second) else "" }
            val changeList = change.getBinding()
                .map {
                    if (showChange(it.first, it.second)) {
                            listOf(
                                Pair(party.color, it.first),
                                Pair(ColorUtils.lighten(party.color), it.second - it.first))
                    } else {
                        emptyList()
                    }
                }
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
            partyFilter: (Party?) -> Boolean
        ): BindingReceiver<Pair<Int, Int>> {
            val binding = Binding.mapReduceBinding(
                results.map { it.getBinding() }.toList(),
                Pair(0, 0),
                { p: Pair<Int, Int>, r: Pair<PartyResult?, Int> ->
                    val left = r.first
                    if (left == null || !partyFilter(left.party)) {
                        p
                    } else {
                        Pair(p.first + if (left.isElected) r.second else 0, p.second + r.second)
                    }
                },
                { p: Pair<Int, Int>, r: Pair<PartyResult?, Int> ->
                    val left = r.first
                    if (left == null || !partyFilter(left.party)) {
                        p
                    } else {
                        Pair(p.first - if (left.isElected) r.second else 0, p.second - r.second)
                    }
                })
            return BindingReceiver(binding)
        }

        private fun createChangeBarBinding(
            resultWithPrev: List<BindingReceiver<Triple<PartyResult?, Party, Int>>>,
            partyFilter: (Party?) -> Boolean
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
                                ret.second + r.third
                            )
                        }
                        if (partyFilter(r.second)) {
                            ret = Pair(
                                ret.first - if (left.isElected) r.third else 0,
                                ret.second - r.third
                            )
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
                                ret.second - r.third
                            )
                        }
                        if (partyFilter(r.second)) {
                            ret = Pair(
                                ret.first + if (left.isElected) r.third else 0,
                                ret.second + r.third
                            )
                        }
                        ret
                    }
                })
            return BindingReceiver(binding)
        }
    }
}
