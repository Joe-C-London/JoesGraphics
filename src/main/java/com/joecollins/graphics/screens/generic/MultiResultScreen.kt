package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.BarFrameBuilder.BasicBar
import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.SwingFrame
import com.joecollins.graphics.components.SwingFrameBuilder
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.GridLayout
import java.awt.LayoutManager
import java.awt.Shape
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.Comparator
import java.util.HashMap
import java.util.LinkedHashMap
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair

class MultiResultScreen private constructor(builder: Builder<*>, textHeader: Binding<String?>, hasMap: Boolean) : JPanel() {
    private val panels: MutableList<ResultPanel> = ArrayList()

    class Builder<T>(
        internal val list: BindableList<T>,
        private val votesFunc: (T) -> Binding<Map<Candidate, Int>>,
        private val headerFunc: (T) -> Binding<String>,
        private val subheadFunc: (T) -> Binding<String>,
        internal val partiesOnly: Boolean
    ) {
        var pctReportingFunc: (T) -> Binding<Double> = { Binding.fixedBinding(1.0) }
        var winnerFunc: (T) -> Binding<Candidate?> = { Binding.fixedBinding(null) }
        var runoffFunc: (T) -> Binding<Set<Candidate>?> = { Binding.fixedBinding(setOf()) }
        var incumbentMarker = ""
        var prevFunc: ((T) -> Binding<Map<Party, Int>>)? = null
        var swingHeaderFunc: ((T) -> Binding<String>)? = null
        var swingPartyOrder: Comparator<Party>? = null
        var mapShapeFunc: ((T) -> List<Pair<Shape, Binding<Color>>>)? = null
        var mapFocusFunc: ((T) -> List<Shape>?)? = null
        var mapHeaderFunc: ((T) -> Binding<String?>)? = null

        fun withIncumbentMarker(incumbentMarker: String): Builder<T> {
            this.incumbentMarker = incumbentMarker
            return this
        }

        fun withWinner(winnerFunc: (T) -> Binding<Candidate?>): Builder<T> {
            this.winnerFunc = winnerFunc
            return this
        }

        fun withRunoff(runoffFunc: (T) -> Binding<Set<Candidate>?>): Builder<T> {
            this.runoffFunc = runoffFunc
            return this
        }

        fun withPctReporting(pctReportingFunc: (T) -> Binding<Double>): Builder<T> {
            this.pctReportingFunc = pctReportingFunc
            return this
        }

        fun withPrev(
            prevFunc: (T) -> Binding<Map<Party, Int>>,
            swingHeaderFunc: (T) -> Binding<String>,
            swingPartyOrder: Comparator<Party>
        ): Builder<T> {
            this.prevFunc = prevFunc
            this.swingHeaderFunc = swingHeaderFunc
            this.swingPartyOrder = swingPartyOrder
            return this
        }

        fun <K> withMap(
            shapesFunc: (T) -> Map<K, Shape>,
            selectedShapeFunc: (T) -> K,
            leadingPartyFunc: (T) -> Binding<PartyResult?>,
            focusFunc: (T) -> List<K>?,
            mapHeaderFunc: (T) -> Binding<String?>
        ): Builder<T> {
            return withMap(shapesFunc, selectedShapeFunc, leadingPartyFunc, focusFunc, focusFunc, mapHeaderFunc)
        }

        fun <K> withMap(
            shapesFunc: (T) -> Map<K, Shape>,
            selectedShapeFunc: (T) -> K,
            leadingPartyFunc: (T) -> Binding<PartyResult?>,
            focusFunc: (T) -> List<K>?,
            additionalHighlightsFunc: (T) -> List<K>?,
            mapHeaderFunc: (T) -> Binding<String?>
        ): Builder<T> {
            this.mapHeaderFunc = mapHeaderFunc
            mapFocusFunc = label@{ t: T ->
                val focus = focusFunc(t) ?: return@label emptyList()
                val shapes = shapesFunc(t)
                focus.mapNotNull { key: K -> shapes[key] }
            }
            mapShapeFunc = { t: T ->
                val selected = selectedShapeFunc(t)
                val focus = focusFunc(t)
                val additionalHighlight = additionalHighlightsFunc(t)
                val leader = leadingPartyFunc(t).map { p: PartyResult? -> p ?: PartyResult.NO_RESULT }
                shapesFunc(t).entries.asSequence()
                        .map { e: Map.Entry<K, Shape> ->
                            when {
                                e.key == selected -> {
                                    ImmutablePair.of(e.value, leader.map(PartyResult::color))
                                }
                                focus == null || focus.isEmpty() || focus.contains(e.key) -> {
                                    ImmutablePair.of(
                                            e.value, Binding.fixedBinding(Color.LIGHT_GRAY))
                                }
                                additionalHighlight != null && additionalHighlight.contains(e.key) -> {
                                    ImmutablePair.of(
                                            e.value, Binding.fixedBinding(Color.LIGHT_GRAY))
                                }
                                else -> ImmutablePair.of(
                                        e.value, Binding.fixedBinding(Color(220, 220, 220)))
                            }
                        }
                        .toList()
            }
            return this
        }

        fun build(textHeader: Binding<String?>): MultiResultScreen {
            return MultiResultScreen(this, textHeader, mapHeaderFunc != null)
        }

        internal fun votesBinding() = IndexedBinding.propertyBinding(list, votesFunc)
        internal fun winnerBinding() = IndexedBinding.propertyBinding(list, winnerFunc)
        internal fun runOffBinding() = IndexedBinding.propertyBinding(list, runoffFunc)
        internal fun pctReportingBinding() = IndexedBinding.propertyBinding(list, pctReportingFunc)
        internal fun headerBinding() = IndexedBinding.propertyBinding(list, headerFunc)
        internal fun subheadBinding() = IndexedBinding.propertyBinding(list, subheadFunc)
        internal fun prevBinding() = prevFunc?.let { IndexedBinding.propertyBinding(list, it) }
        internal fun swingHeaderBinding() = swingHeaderFunc?.let { IndexedBinding.propertyBinding(list, it) }
        internal fun mapShapeBinding() = mapShapeFunc?.let { IndexedBinding.propertyBinding(list, it) }
        internal fun mapFocusBinding() = mapFocusFunc?.let { IndexedBinding.propertyBinding(list, it) }
        internal fun mapHeaderBinding() = mapHeaderFunc?.let { IndexedBinding.propertyBinding(list, it) }
    }

    private class Result : Bindable<Result, Result.Property>() {
        enum class Property {
            VOTES, WINNER, RUNOFF, MAX_BARS
        }

        private var _votes: Map<Candidate, Int> = HashMap()
        private var _winner: Candidate? = null
        private var _runoff: Set<Candidate> = emptySet()
        private var _maxBars = 0

        var votes: Map<Candidate, Int>
        get() = _votes
        set(votes) {
            _votes = votes
            onPropertyRefreshed(Property.VOTES)
        }

        var winner: Candidate?
        get() = _winner
        set(winner) {
            _winner = winner
            onPropertyRefreshed(Property.WINNER)
        }

        var runoff: Set<Candidate>
        get() = _runoff
        set(runoff) {
            _runoff = runoff
            onPropertyRefreshed(Property.RUNOFF)
        }

        var maxBars: Int
        get() = _maxBars
        set(maxBars) {
            _maxBars = maxBars
            onPropertyRefreshed(Property.MAX_BARS)
        }
    }

    private class ResultPanel constructor(
        private val incumbentMarker: String,
        swingPartyOrder: Comparator<Party>?,
        hasMap: Boolean,
        partiesOnly: Boolean
    ) : JPanel() {
        private val barFrame: BarFrame
        private var swingFrame: SwingFrame? = null
        private var mapFrame: MapFrame? = null
        var displayBothRows = true
        private val votes = WrappedBinding<Map<Candidate, Int>>(Binding.fixedBinding(emptyMap()))
        private val pctReporting = WrappedBinding(Binding.fixedBinding(1.0))
        private val winner = WrappedBinding<Candidate?>(Binding.fixedBinding(null))
        private val runoff = WrappedBinding<Set<Candidate>?>(Binding.fixedBinding(emptySet()))
        private val prevVotes = WrappedBinding<Map<Party, Int>>(Binding.fixedBinding(emptyMap()))
        private val maxBars = WrappedBinding(Binding.fixedBinding(5))

        fun setVotesBinding(votes: Binding<Map<Candidate, Int>>) {
            this.votes.binding = votes
        }

        fun setHeaderBinding(headerBinding: Binding<String>) {
            barFrame.setHeaderBinding(headerBinding)
        }

        fun setSubheadBinding(subheadBinding: Binding<out String?>) {
            barFrame.setSubheadTextBinding(subheadBinding)
        }

        fun setWinnerBinding(winnerBinding: Binding<Candidate?>) {
            winner.binding = winnerBinding
        }

        fun setRunoffBinding(runoffBinding: Binding<Set<Candidate>?>) {
            runoff.binding = runoffBinding
        }

        fun setPctReportingBinding(pctReportingBinding: Binding<Double>) {
            pctReporting.binding = pctReportingBinding
        }

        fun setPrevBinding(prevBinding: Binding<Map<Party, Int>>) {
            prevVotes.binding = prevBinding
        }

        fun setSwingHeaderBinding(swingLabelBinding: Binding<out String?>) {
            swingFrame?.setHeaderBinding(swingLabelBinding)
        }

        fun setMapShapeBinding(shapes: List<Pair<Shape, Binding<Color>>>) {
            mapFrame?.setNumShapesBinding(Binding.fixedBinding(shapes.size))
            mapFrame?.setShapeBinding(
                    IndexedBinding.listBinding(
                            shapes.map { it.key }))
            mapFrame?.setColorBinding(IndexedBinding.listBinding(shapes) { it.value })
        }

        fun setMapFocusBinding(shapes: List<Shape>) {
            mapFrame?.setFocusBoxBinding(
                    Binding.fixedBinding(
                            shapes.asSequence()
                                    .map { obj: Shape -> obj.bounds2D }
                                    .reduceOrNull { agg, r -> agg.createUnion(r) }))
        }

        fun setMapHeaderBinding(mapLabelBinding: Binding<String?>) {
            mapFrame?.setHeaderBinding(mapLabelBinding)
        }

        fun setMaxBarsBinding(maxBarsBinding: Binding<Int>) {
            maxBars.binding = maxBarsBinding
        }

        fun unbindAll() {
            setVotesBinding(Binding.fixedBinding(emptyMap()))
            setHeaderBinding(Binding.fixedBinding(""))
            setSubheadBinding(Binding.fixedBinding(""))
            setWinnerBinding(Binding.fixedBinding(null))
            setRunoffBinding(Binding.fixedBinding(emptySet()))
            setPctReportingBinding(Binding.fixedBinding(0.0))
            setPrevBinding(Binding.fixedBinding(emptyMap()))
            setSwingHeaderBinding(Binding.fixedBinding(""))
            setMapShapeBinding(emptyList())
            setMapFocusBinding(emptyList())
            setMapHeaderBinding(Binding.fixedBinding(""))
            setMaxBarsBinding(Binding.fixedBinding(5))
        }

        private inner class ResultPanelLayout : LayoutManager {
            override fun addLayoutComponent(name: String, comp: Component) {}
            override fun removeLayoutComponent(comp: Component) {}
            override fun preferredLayoutSize(parent: Container): Dimension? {
                return null
            }

            override fun minimumLayoutSize(parent: Container): Dimension? {
                return null
            }

            override fun layoutContainer(parent: Container) {
                val width = parent.width
                val height = parent.height
                barFrame.setLocation(5, 5)
                val barsOnly = !displayBothRows || swingFrame == null && mapFrame == null
                barFrame.setSize(width - 10, height * (if (barsOnly) 3 else 2) / 3 - 10)
                swingFrame?.setLocation(5, height * 2 / 3 + 5)
                swingFrame?.setSize(width / (if (mapFrame == null) 1 else 2) - 10, height / 3 - 10)
                swingFrame?.isVisible = displayBothRows
                mapFrame?.setLocation((if (swingFrame == null) 0 else width / 2) + 5, height * 2 / 3 + 5)
                mapFrame?.setSize(width / (if (swingFrame == null) 1 else 2) - 10, height / 3 - 10)
                mapFrame?.isVisible = displayBothRows
            }
        }

        init {
            background = Color.WHITE
            layout = ResultPanelLayout()
            val result = Result()
            votes.binding.bind { result.votes = it }
            winner.binding.bind { result.winner = it }
            runoff.binding.bind { result.runoff = it ?: emptySet() }
            maxBars.binding.bind { result.maxBars = it }
            val bars = Binding.propertyBinding(
                    result,
                    { r: Result ->
                        val total = r.votes.values.sum()
                        Aggregators.topAndOthers(r.votes, r.maxBars, Candidate.OTHERS, *listOfNotNull(r.winner).toTypedArray())
                                .entries
                                .asSequence()
                                .sortedByDescending { e -> if (e.key === Candidate.OTHERS) Int.MIN_VALUE else e.value }
                                .map { e ->
                                    val candidate = e.key
                                    val votes = e.value
                                    val pct = 1.0 * votes / total
                                    val shape: Shape? = when {
                                                candidate == r.winner -> ImageGenerator.createHalfTickShape()
                                                r.runoff.contains(candidate) -> ImageGenerator.createHalfRunoffShape()
                                                else -> null
                                            }
                                    val leftLabel: String = when {
                                        partiesOnly -> {
                                            candidate.party.name.toUpperCase()
                                        }
                                        candidate === Candidate.OTHERS -> {
                                            "OTHERS"
                                        }
                                        else -> {
                                            "${candidate.name.toUpperCase()}\n${candidate.party.abbreviation}${if (candidate.isIncumbent()) " $incumbentMarker" else ""}"
                                        }
                                    }
                                    val rightLabel: String = when {
                                        java.lang.Double.isNaN(pct) -> {
                                            "WAITING..."
                                        }
                                        partiesOnly -> {
                                            DecimalFormat("0.0%").format(pct)
                                        }
                                        else -> {
                                            "${DecimalFormat("#,##0").format(votes.toLong())}\n${DecimalFormat("0.0%").format(pct)}"
                                        }
                                    }
                                    BasicBar(
                                            leftLabel,
                                            candidate.party.color,
                                            if (java.lang.Double.isNaN(pct)) 0 else pct,
                                            rightLabel,
                                            shape)
                                }
                                .toList()
                    },
                    Result.Property.VOTES,
                    Result.Property.WINNER,
                    Result.Property.RUNOFF,
                    Result.Property.MAX_BARS)
            barFrame = BarFrameBuilder.basic(bars)
                    .withMax(pctReporting.binding.map { d: Double -> 0.5 / d.coerceAtLeast(1e-6) })
                    .build()
            add(barFrame)
            if (swingPartyOrder != null) {
                swingFrame = SwingFrameBuilder.prevCurr(
                        prevVotes.binding,
                        votes.binding
                                .map { m: Map<Candidate, Int> ->
                                    val ret: MutableMap<Party, Int> = LinkedHashMap()
                                    m.forEach { (k: Candidate, v: Int) -> ret.merge(k.party, v) { a, b -> Integer.sum(a, b) } }
                                    ret
                                },
                        swingPartyOrder)
                        .build()
                add(swingFrame)
            }
            if (hasMap) {
                mapFrame = MapFrame()
                add(mapFrame)
            }
        }
    }

    private class WrappedBinding<T> constructor(binding: Binding<T>) : Bindable<WrappedBinding<T>, WrappedBinding.Property>() {
        private enum class Property {
            PROP
        }

        private var underBinding: Binding<T> = binding
        private var value: T = binding.value

        var binding: Binding<T>
            get() = Binding.propertyBinding(this, { t -> t.value }, Property.PROP)
            set(underBinding) {
                this.underBinding.unbind()
                this.underBinding = underBinding
                this.underBinding.bind { this.setValue(it) }
            }

        private fun setValue(value: T) {
            this.value = value
            onPropertyRefreshed(Property.PROP)
        }
    }

    companion object {
        private fun createHeaderLabel(textBinding: Binding<String?>): JLabel {
            val headerLabel = JLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            textBinding.bind { headerLabel.text = it }
            return headerLabel
        }

        @JvmStatic fun <T> of(
            list: BindableList<T>,
            votesFunc: (T) -> Binding<Map<Candidate, Int>>,
            headerFunc: (T) -> Binding<String>,
            subheadFunc: (T) -> Binding<String>
        ): Builder<T> {
            return Builder(list, votesFunc, headerFunc, subheadFunc, false)
        }

        @JvmStatic fun <T> ofParties(
            list: BindableList<T>,
            votesFunc: (T) -> Binding<Map<Party, Int>>,
            headerFunc: (T) -> Binding<String>,
            subheadFunc: (T) -> Binding<String>
        ): Builder<T> {
            val adjustedVoteFunc = { t: T -> votesFunc(t).map { m: Map<Party, Int> -> Aggregators.adjustKey(m) { k: Party -> Candidate("", k) } } }
            return Builder(list, adjustedVoteFunc, headerFunc, subheadFunc, true)
        }
    }

    init {
        background = Color.WHITE
        layout = BorderLayout()
        add(createHeaderLabel(textHeader), BorderLayout.NORTH)
        val center = JPanel()
        center.layout = GridLayout(1, 0)
        center.background = Color.WHITE
        add(center, BorderLayout.CENTER)
        Binding.sizeBinding(builder.list)
                .bind { size: Int ->
                    while (panels.size < size) {
                        val newPanel = ResultPanel(
                                builder.incumbentMarker,
                                builder.swingPartyOrder,
                                hasMap,
                                builder.partiesOnly)
                        center.add(newPanel)
                        panels.add(newPanel)
                    }
                    while (panels.size > size) {
                        val panel = panels.removeAt(size)
                        panel.unbindAll()
                        center.remove(panel)
                    }
                    val numRows = if (size > 4) 2 else 1
                    center.layout = GridLayout(numRows, 0)
                    panels.forEach { p: ResultPanel ->
                        p.displayBothRows = numRows == 1
                        p.setMaxBarsBinding(
                                Binding.fixedBinding(
                                        (if (numRows == 2) 4 else 5) * if (builder.partiesOnly) 2 else 1))
                        p.invalidate()
                        p.revalidate()
                    }
                    EventQueue.invokeLater { this.repaint() }
                }
        builder.votesBinding()
                .bind { idx, votes -> panels[idx].setVotesBinding(votes) }
        builder.winnerBinding()
                .bind { idx, winner -> panels[idx].setWinnerBinding(winner) }
        builder.runOffBinding()
                .bind { idx, runoff -> panels[idx].setRunoffBinding(runoff) }
        builder.pctReportingBinding()
                .bind { idx, pctReporting -> panels[idx].setPctReportingBinding(pctReporting) }
        builder.headerBinding()
                .bind { idx, header -> panels[idx].setHeaderBinding(header) }
        builder.subheadBinding()
                .bind { idx, subhead -> panels[idx].setSubheadBinding(subhead) }
        if (builder.swingPartyOrder != null) {
            builder.prevBinding()
                    ?.bind { idx, prev -> panels[idx].setPrevBinding(prev) }
            builder.swingHeaderBinding()
                    ?.bind { idx, header -> panels[idx].setSwingHeaderBinding(header) }
        }
        if (builder.mapHeaderFunc != null) {
            builder.mapShapeBinding()
                    ?.bind { idx, shapes -> panels[idx].setMapShapeBinding(shapes) }
            builder.mapFocusBinding()
                    ?.bind { idx, shapes -> panels[idx].setMapFocusBinding(shapes ?: emptyList()) }
            builder.mapHeaderBinding()
                    ?.bind { idx, header -> panels[idx].setMapHeaderBinding(header) }
        }
    }
}
