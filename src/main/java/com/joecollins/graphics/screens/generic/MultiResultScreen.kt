package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.BarFrameBuilder.BasicBar
import com.joecollins.graphics.components.FontSizeAdjustingLabel
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
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class MultiResultScreen private constructor() : JPanel() {
    private val panels: MutableList<ResultPanel> = ArrayList()

    class Builder<T>(
        listBinding: Binding<List<T>>,
        private val votesFunc: (T) -> Binding<Map<Candidate, Int>>,
        private val headerFunc: (T) -> Binding<String>,
        private val subheadFunc: (T) -> Binding<String>,
        private val partiesOnly: Boolean
    ) {
        private val listReceiver = BindingReceiver(listBinding)
        private val itemReceivers: MutableList<BindingReceiver<T?>> = ArrayList()

        var pctReportingFunc: (T) -> Binding<Double> = { Binding.fixedBinding(1.0) }
        var winnerFunc: (T) -> Binding<Candidate?> = { Binding.fixedBinding(null) }
        var runoffFunc: (T) -> Binding<Set<Candidate>?> = { Binding.fixedBinding(setOf()) }
        var incumbentMarker = ""
        var prevFunc: ((T) -> Binding<Map<Party, Int>>)? = null
        var swingHeaderFunc: ((T) -> Binding<String>)? = null
        var swingPartyOrder: Comparator<Party>? = null
        var mapShapeFunc: ((T) -> List<Pair<Shape, Binding<Color>>>)? = null
        var mapFocusFunc: ((T) -> List<Shape>?)? = null
        var mapHeaderFunc: ((T) -> Binding<String>)? = null

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
            mapHeaderFunc: (T) -> Binding<String>
        ): Builder<T> {
            return withMap(shapesFunc, selectedShapeFunc, leadingPartyFunc, focusFunc, focusFunc, mapHeaderFunc)
        }

        fun <K> withMap(
            shapesFunc: (T) -> Map<K, Shape>,
            selectedShapeFunc: (T) -> K,
            leadingPartyFunc: (T) -> Binding<PartyResult?>,
            focusFunc: (T) -> List<K>?,
            additionalHighlightsFunc: (T) -> List<K>?,
            mapHeaderFunc: (T) -> Binding<String>
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
                                Pair(e.value, leader.map(PartyResult::color))
                            }
                            focus == null || focus.isEmpty() || focus.contains(e.key) -> {
                                Pair(
                                    e.value, Binding.fixedBinding(Color.LIGHT_GRAY)
                                )
                            }
                            additionalHighlight != null && additionalHighlight.contains(e.key) -> {
                                Pair(
                                    e.value, Binding.fixedBinding(Color.LIGHT_GRAY)
                                )
                            }
                            else -> Pair(
                                e.value, Binding.fixedBinding(Color(220, 220, 220))
                            )
                        }
                    }
                    .toList()
            }
            return this
        }

        fun build(textHeader: Binding<String?>): MultiResultScreen {
            val screen = MultiResultScreen()
            screen.background = Color.WHITE
            screen.layout = BorderLayout()
            screen.add(createHeaderLabel(textHeader), BorderLayout.NORTH)
            val center = JPanel()
            center.layout = GridLayout(1, 0)
            center.background = Color.WHITE
            screen.add(center, BorderLayout.CENTER)
            this.listReceiver.getBinding().bind { list ->
                val size = list.size
                while (screen.panels.size < size) {
                    val idx = screen.panels.size
                    val itemReceiver = BindingReceiver(
                        this.listReceiver.getBinding {
                            if (idx < it.size) {
                                it[idx]
                            } else {
                                null
                            }
                        }
                    )
                    itemReceivers.add(itemReceiver)
                    val newPanel = ResultPanel(
                        this.incumbentMarker,
                        this.swingPartyOrder,
                        mapHeaderFunc != null,
                        this.partiesOnly
                    )
                    newPanel.setVotesBinding(itemReceiver.getFlatBinding { it?.let(votesFunc) ?: Binding.fixedBinding(emptyMap()) })
                    newPanel.setWinnerBinding(itemReceiver.getFlatBinding { it?.let(winnerFunc) ?: Binding.fixedBinding(null) })
                    newPanel.setRunoffBinding(itemReceiver.getFlatBinding { it?.let(runoffFunc) ?: Binding.fixedBinding(null) })
                    newPanel.setPctReportingBinding(itemReceiver.getFlatBinding { it?.let(pctReportingFunc) ?: Binding.fixedBinding(0.0) })
                    newPanel.setHeaderBinding(itemReceiver.getFlatBinding { it?.let(headerFunc) ?: Binding.fixedBinding("") })
                    newPanel.setSubheadBinding(itemReceiver.getFlatBinding { it?.let(subheadFunc) ?: Binding.fixedBinding("") })
                    if (swingPartyOrder != null) {
                        prevFunc?.let { f -> newPanel.setPrevBinding(itemReceiver.getFlatBinding { it?.let(f) ?: Binding.fixedBinding(emptyMap()) }) }
                        swingHeaderFunc?.let { f -> newPanel.setSwingHeaderBinding(itemReceiver.getFlatBinding { it?.let(f) ?: Binding.fixedBinding("") }) }
                    }
                    if (mapHeaderFunc != null) {
                        mapShapeFunc?.let { f -> newPanel.setMapShapeBinding(itemReceiver.getFlatBinding { Binding.listBinding((it?.let(f) ?: emptyList()).map { e -> e.second.map { c -> Pair(e.first, c) } }) }) }
                        mapFocusFunc?.let { f -> newPanel.setMapFocusBinding(itemReceiver.getBinding { it?.let(f) ?: emptyList() }) }
                        mapHeaderFunc?.let { f -> newPanel.setMapHeaderBinding(itemReceiver.getFlatBinding { it?.let(f) ?: Binding.fixedBinding("") }) }
                    }
                    center.add(newPanel)
                    screen.panels.add(newPanel)
                }
                while (screen.panels.size > size) {
                    val panel = screen.panels.removeAt(size)
                    panel.unbindAll()
                    center.remove(panel)
                    itemReceivers.removeAt(size)
                }
                val numRows = if (size > 4) 2 else 1
                center.layout = GridLayout(numRows, 0)
                screen.panels.forEach { p: ResultPanel ->
                    p.displayBothRows = numRows == 1
                    p.setMaxBarsBinding(
                        Binding.fixedBinding(
                            (if (numRows == 2) 4 else 5) * if (this.partiesOnly) 2 else 1
                        )
                    )
                    p.invalidate()
                    p.revalidate()
                }
                EventQueue.invokeLater { screen.repaint() }
            }

            return screen
        }
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
        private val votes = WrappedBinding<Map<Candidate, Int>>(emptyMap())
        private val header = WrappedBinding("")
        private val subhead = WrappedBinding<String?>(null)
        private val pctReporting = WrappedBinding(1.0)
        private val winner = WrappedBinding<Candidate?>(null)
        private val runoff = WrappedBinding<Set<Candidate>?>(emptySet())
        private val prevVotes = WrappedBinding<Map<Party, Int>>(emptyMap())
        private val maxBars = WrappedBinding(5)
        private val swingHeader = WrappedBinding<String?>(null)
        private val mapShape = WrappedBinding<List<Pair<Shape, Color>>>(emptyList())
        private val mapFocus = WrappedBinding<List<Shape>>(emptyList())
        private val mapHeader = WrappedBinding("")

        fun setVotesBinding(votes: Binding<Map<Candidate, Int>>) {
            this.votes.binding = votes
        }

        fun setHeaderBinding(headerBinding: Binding<String>) {
            this.header.binding = headerBinding
        }

        fun setSubheadBinding(subheadBinding: Binding<String?>) {
            this.subhead.binding = subheadBinding
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

        fun setSwingHeaderBinding(swingLabelBinding: Binding<String?>) {
            swingHeader.binding = swingLabelBinding
        }

        fun setMapShapeBinding(shapes: Binding<List<Pair<Shape, Color>>>) {
            mapShape.binding = shapes
        }

        fun setMapFocusBinding(shapes: Binding<List<Shape>>) {
            mapFocus.binding = shapes
        }

        fun setMapHeaderBinding(mapLabelBinding: Binding<String>) {
            mapHeader.binding = mapLabelBinding
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
            setMapShapeBinding(Binding.fixedBinding(emptyList()))
            setMapFocusBinding(Binding.fixedBinding(emptyList()))
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
                                    candidate.party.name.uppercase()
                                }
                                candidate === Candidate.OTHERS -> {
                                    "OTHERS"
                                }
                                else -> {
                                    "${candidate.name.uppercase()}\n${candidate.party.abbreviation}${if (candidate.isIncumbent()) " $incumbentMarker" else ""}"
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
                                shape
                            )
                        }
                        .toList()
                },
                Result.Property.VOTES,
                Result.Property.WINNER,
                Result.Property.RUNOFF,
                Result.Property.MAX_BARS
            )
            barFrame = BarFrameBuilder.basic(bars)
                .withMax(pctReporting.binding.map { d: Double -> 0.5 / d.coerceAtLeast(1e-6) })
                .withHeader(header.binding)
                .withSubhead(subhead.binding)
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
                    swingPartyOrder
                )
                    .withHeader(swingHeader.binding)
                    .build()
                add(swingFrame)
            }
            if (hasMap) {
                mapFrame = MapFrame(
                    headerPublisher = mapHeader.binding.toPublisher(),
                    shapesPublisher = mapShape.binding.toPublisher(),
                    focusBoxPublisher = mapFocus.binding.map {
                        it.asSequence()
                            .map { obj: Shape -> obj.bounds2D }
                            .reduceOrNull { agg, r -> agg.createUnion(r) }
                    }.toPublisher()
                )
                add(mapFrame)
            }
        }
    }

    private class WrappedBinding<T> constructor(private var value: T) : Bindable<WrappedBinding<T>, WrappedBinding.Property>() {
        private enum class Property {
            PROP
        }

        private var underBinding: Binding<T> = Binding.fixedBinding(value)

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
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            textBinding.bind { headerLabel.text = it }
            return headerLabel
        }

        @JvmStatic fun <T> of(
            list: Binding<List<T>>,
            votesFunc: (T) -> Binding<Map<Candidate, Int>>,
            headerFunc: (T) -> Binding<String>,
            subheadFunc: (T) -> Binding<String>
        ): Builder<T> {
            return Builder(list, votesFunc, headerFunc, subheadFunc, false)
        }

        @JvmStatic fun <T> ofParties(
            list: Binding<List<T>>,
            votesFunc: (T) -> Binding<Map<Party, Int>>,
            headerFunc: (T) -> Binding<String>,
            subheadFunc: (T) -> Binding<String>
        ): Builder<T> {
            val adjustedVoteFunc = { t: T -> votesFunc(t).map { m: Map<Party, Int> -> Aggregators.adjustKey(m) { k: Party -> if (k == Party.OTHERS) Candidate.OTHERS else Candidate("", k) } } }
            return Builder(list, adjustedVoteFunc, headerFunc, subheadFunc, true)
        }
    }
}
