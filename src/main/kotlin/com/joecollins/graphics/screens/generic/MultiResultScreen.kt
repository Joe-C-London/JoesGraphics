package com.joecollins.graphics.screens.generic

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
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
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
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class MultiResultScreen private constructor() : JPanel() {
    private val panels: MutableList<ResultPanel> = ArrayList()

    class Builder<T>(
        private val listPublisher: Flow.Publisher<out List<T>>,
        private val votesFunc: (T) -> Flow.Publisher<out Map<Candidate, Int>>,
        private val headerFunc: (T) -> Flow.Publisher<out String>,
        private val subheadFunc: (T) -> Flow.Publisher<out String>,
        private val partiesOnly: Boolean
    ) {
        private val itemPublishers: MutableList<Flow.Publisher<out T?>> = ArrayList()

        private var pctReportingFunc: (T) -> Flow.Publisher<out Double> = { 1.0.asOneTimePublisher() }
        private var winnerFunc: (T) -> Flow.Publisher<out Candidate?> = { (null as Candidate?).asOneTimePublisher() }
        private var runoffFunc: (T) -> Flow.Publisher<out Set<Candidate>?> = { setOf<Candidate>().asOneTimePublisher() }
        private var incumbentMarker = ""
        private var prevFunc: ((T) -> Flow.Publisher<out Map<Party, Int>>)? = null
        private var swingHeaderFunc: ((T) -> Flow.Publisher<out String>)? = null
        private var swingPartyOrder: Comparator<Party>? = null
        private var mapShapeFunc: ((T) -> List<Pair<Shape, Flow.Publisher<out Color>>>)? = null
        private var mapFocusFunc: ((T) -> List<Shape>?)? = null
        private var mapHeaderFunc: ((T) -> Flow.Publisher<out String>)? = null

        fun withIncumbentMarker(incumbentMarker: String): Builder<T> {
            this.incumbentMarker = incumbentMarker
            return this
        }

        fun withWinner(winnerFunc: (T) -> Flow.Publisher<out Candidate?>): Builder<T> {
            this.winnerFunc = winnerFunc
            return this
        }

        fun withRunoff(runoffFunc: (T) -> Flow.Publisher<out Set<Candidate>?>): Builder<T> {
            this.runoffFunc = runoffFunc
            return this
        }

        fun withPctReporting(pctReportingFunc: (T) -> Flow.Publisher<out Double>): Builder<T> {
            this.pctReportingFunc = pctReportingFunc
            return this
        }

        fun withPrev(
            prevFunc: (T) -> Flow.Publisher<out Map<Party, Int>>,
            swingHeaderFunc: (T) -> Flow.Publisher<out String>,
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
            leadingPartyFunc: (T) -> Flow.Publisher<out PartyResult?>,
            focusFunc: (T) -> List<K>?,
            mapHeaderFunc: (T) -> Flow.Publisher<out String>
        ): Builder<T> {
            return withMap(shapesFunc, selectedShapeFunc, leadingPartyFunc, focusFunc, focusFunc, mapHeaderFunc)
        }

        fun <K> withMap(
            shapesFunc: (T) -> Map<K, Shape>,
            selectedShapeFunc: (T) -> K,
            leadingPartyFunc: (T) -> Flow.Publisher<out PartyResult?>,
            focusFunc: (T) -> List<K>?,
            additionalHighlightsFunc: (T) -> List<K>?,
            mapHeaderFunc: (T) -> Flow.Publisher<out String>
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
                                    e.value, Color.LIGHT_GRAY.asOneTimePublisher()
                                )
                            }
                            additionalHighlight != null && additionalHighlight.contains(e.key) -> {
                                Pair(
                                    e.value, Color.LIGHT_GRAY.asOneTimePublisher()
                                )
                            }
                            else -> Pair(
                                e.value, Color(220, 220, 220).asOneTimePublisher()
                            )
                        }
                    }
                    .toList()
            }
            return this
        }

        fun build(textHeader: Flow.Publisher<out String?>): MultiResultScreen {
            val screen = MultiResultScreen()
            screen.background = Color.WHITE
            screen.layout = BorderLayout()
            screen.add(createHeaderLabel(textHeader), BorderLayout.NORTH)
            val center = JPanel()
            center.layout = GridLayout(1, 0)
            center.background = Color.WHITE
            screen.add(center, BorderLayout.CENTER)
            this.listPublisher.subscribe(
                Subscriber { list ->
                    val size = list.size
                    while (screen.panels.size < size) {
                        val idx = screen.panels.size
                        val itemPublisher =
                            this.listPublisher.map {
                                if (idx < it.size) {
                                    it[idx]
                                } else {
                                    null
                                }
                            }
                        itemPublishers.add(itemPublisher)
                        val newPanel = ResultPanel(
                            this.incumbentMarker,
                            this.swingPartyOrder,
                            mapHeaderFunc != null,
                            this.partiesOnly,
                            idx
                        )
                        newPanel.setVotesPublisher(itemPublisher.compose { it?.let(votesFunc) ?: emptyMap<Candidate, Int>().asOneTimePublisher() })
                        newPanel.setWinnerPublisher(itemPublisher.compose { it?.let(winnerFunc) ?: (null as Candidate?).asOneTimePublisher() })
                        newPanel.setRunoffPublisher(itemPublisher.compose { it?.let(runoffFunc) ?: (null as Set<Candidate>?).asOneTimePublisher() })
                        newPanel.setPctReportingPublisher(itemPublisher.compose { it?.let(pctReportingFunc) ?: 0.0.asOneTimePublisher() })
                        newPanel.setHeaderPublisher(itemPublisher.compose { it?.let(headerFunc) ?: "".asOneTimePublisher() })
                        newPanel.setSubheadPublisher(itemPublisher.compose { it?.let(subheadFunc) ?: "".asOneTimePublisher() })
                        if (swingPartyOrder != null) {
                            prevFunc?.let { f -> newPanel.setPrevPublisher(itemPublisher.compose { it?.let(f) ?: emptyMap<Party, Int>().asOneTimePublisher() }) }
                            swingHeaderFunc?.let { f -> newPanel.setSwingHeaderPublisher(itemPublisher.compose { it?.let(f) ?: "".asOneTimePublisher() }) }
                        }
                        if (mapHeaderFunc != null) {
                            mapShapeFunc?.let { f -> newPanel.setMapShapePublisher(itemPublisher.compose { ((it?.let(f) ?: emptyList()).map { e -> e.second.map { c -> Pair(e.first, c) } }).combine() }) }
                            mapFocusFunc?.let { f -> newPanel.setMapFocusPublisher(itemPublisher.map { it?.let(f) ?: emptyList() }) }
                            mapHeaderFunc?.let { f -> newPanel.setMapHeaderPublisher(itemPublisher.compose { it?.let(f) ?: "".asOneTimePublisher() }) }
                        }
                        center.add(newPanel)
                        screen.panels.add(newPanel)
                    }
                    while (screen.panels.size > size) {
                        val panel = screen.panels.removeAt(size)
                        panel.unbindAll()
                        center.remove(panel)
                        itemPublishers.removeAt(size)
                    }
                    val numRows = if (size > 4) 2 else 1
                    center.layout = GridLayout(numRows, 0)
                    screen.panels.forEach { p: ResultPanel ->
                        p.displayBothRows = numRows == 1
                        p.setMaxBarsPublisher(
                            (
                                (if (numRows == 2) 4 else 5) * if (this.partiesOnly) 2 else 1
                                ).asOneTimePublisher()
                        )
                    }
                    EventQueue.invokeLater {
                        screen.panels.forEach { p ->
                            p.invalidate()
                            p.revalidate()
                        }
                        screen.repaint()
                    }
                }
            )

            return screen
        }
    }

    private class Result(private val index: Int, private val partiesOnly: Boolean, private val incumbentMarker: String) {
        private var _votes: Map<Candidate, Int> = HashMap()
        private var _winner: Candidate? = null
        private var _runoff: Set<Candidate> = emptySet()
        private var _maxBars = 0

        var votes: Map<Candidate, Int>
            get() = _votes
            set(votes) {
                _votes = votes
                updateBars()
            }

        var winner: Candidate?
            get() = _winner
            set(winner) {
                _winner = winner
                updateBars()
            }

        var runoff: Set<Candidate>
            get() = _runoff
            set(runoff) {
                _runoff = runoff
                updateBars()
            }

        var maxBars: Int
            get() = _maxBars
            set(maxBars) {
                _maxBars = maxBars
                updateBars()
            }

        val toBars = Publisher(calculateBars())
        private fun updateBars() = synchronized(this) { toBars.submit(calculateBars()) }
        private fun calculateBars(): List<BasicBar> {
            val total = votes.values.sum()
            return Aggregators.topAndOthers(
                votes,
                maxBars,
                Candidate.OTHERS,
                *listOfNotNull(winner).toTypedArray()
            )
                .entries
                .asSequence()
                .sortedByDescending { e -> if (e.key === Candidate.OTHERS) Int.MIN_VALUE else e.value }
                .map { e ->
                    val candidate = e.key
                    val votes = e.value
                    val pct = 1.0 * votes / total
                    val shape: Shape? = when {
                        candidate == winner -> ImageGenerator.createHalfTickShape()
                        runoff.contains(candidate) -> ImageGenerator.createHalfRunoffShape()
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
        }
    }

    private class ResultPanel constructor(
        incumbentMarker: String,
        swingPartyOrder: Comparator<Party>?,
        hasMap: Boolean,
        partiesOnly: Boolean,
        index: Int
    ) : JPanel() {
        private val barFrame: BarFrame
        private var swingFrame: SwingFrame? = null
        private var mapFrame: MapFrame? = null
        var displayBothRows = true
        private val votes: Publisher<Flow.Publisher<out Map<Candidate, Int>>> = Publisher(Publisher(emptyMap()))
        private val header: Publisher<Flow.Publisher<out String>> = Publisher(Publisher(""))
        private val subhead: Publisher<Flow.Publisher<out String?>> = Publisher(Publisher(null))
        private val pctReporting: Publisher<Flow.Publisher<out Double>> = Publisher(Publisher(1.0))
        private val winner: Publisher<Flow.Publisher<out Candidate?>> = Publisher(Publisher(null))
        private val runoff: Publisher<Flow.Publisher<out Set<Candidate>?>> = Publisher(Publisher(emptySet()))
        private val prevVotes: Publisher<Flow.Publisher<out Map<Party, Int>>> = Publisher(Publisher(emptyMap()))
        private val maxBars: Publisher<Flow.Publisher<out Int>> = Publisher(Publisher(5))
        private val swingHeader: Publisher<Flow.Publisher<out String?>> = Publisher(Publisher(null))
        private val mapShape: Publisher<Flow.Publisher<out List<Pair<Shape, Color>>>> = Publisher(Publisher(emptyList()))
        private val mapFocus: Publisher<Flow.Publisher<out List<Shape>>> = Publisher(Publisher(emptyList()))
        private val mapHeader: Publisher<Flow.Publisher<out String>> = Publisher(Publisher(""))

        fun setVotesPublisher(votes: Flow.Publisher<out Map<Candidate, Int>>) {
            this.votes.submit(votes)
        }

        fun setHeaderPublisher(headerPublisher: Flow.Publisher<out String>) {
            this.header.submit(headerPublisher)
        }

        fun setSubheadPublisher(subheadPublisher: Flow.Publisher<out String?>) {
            this.subhead.submit(subheadPublisher)
        }

        fun setWinnerPublisher(winnerPublisher: Flow.Publisher<out Candidate?>) {
            winner.submit(winnerPublisher)
        }

        fun setRunoffPublisher(runoffPublisher: Flow.Publisher<out Set<Candidate>?>) {
            runoff.submit(runoffPublisher)
        }

        fun setPctReportingPublisher(pctReportingPublisher: Flow.Publisher<out Double>) {
            pctReporting.submit(pctReportingPublisher)
        }

        fun setPrevPublisher(prevPublisher: Flow.Publisher<out Map<Party, Int>>) {
            prevVotes.submit(prevPublisher)
        }

        fun setSwingHeaderPublisher(swingLabelPublisher: Flow.Publisher<out String?>) {
            swingHeader.submit(swingLabelPublisher)
        }

        fun setMapShapePublisher(shapes: Flow.Publisher<out List<Pair<Shape, Color>>>) {
            mapShape.submit(shapes)
        }

        fun setMapFocusPublisher(shapes: Flow.Publisher<out List<Shape>>) {
            mapFocus.submit(shapes)
        }

        fun setMapHeaderPublisher(mapLabelPublisher: Flow.Publisher<out String>) {
            mapHeader.submit(mapLabelPublisher)
        }

        fun setMaxBarsPublisher(maxBarsPublisher: Flow.Publisher<out Int>) {
            maxBars.submit(maxBarsPublisher)
        }

        fun unbindAll() {
            setVotesPublisher(emptyMap<Candidate, Int>().asOneTimePublisher())
            setHeaderPublisher("".asOneTimePublisher())
            setSubheadPublisher("".asOneTimePublisher())
            setWinnerPublisher(null.asOneTimePublisher())
            setRunoffPublisher(emptySet<Candidate>().asOneTimePublisher())
            setPctReportingPublisher(0.0.asOneTimePublisher())
            setPrevPublisher(emptyMap<Party, Int>().asOneTimePublisher())
            setSwingHeaderPublisher("".asOneTimePublisher())
            setMapShapePublisher(emptyList<Pair<Shape, Color>>().asOneTimePublisher())
            setMapFocusPublisher(emptyList<Shape>().asOneTimePublisher())
            setMapHeaderPublisher("".asOneTimePublisher())
            setMaxBarsPublisher(5.asOneTimePublisher())
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
            val result = Result(index, partiesOnly, incumbentMarker)
            votes.selfCompose().subscribe(Subscriber { result.votes = it })
            winner.selfCompose().subscribe(Subscriber { result.winner = it })
            runoff.selfCompose().subscribe(Subscriber { result.runoff = it ?: emptySet() })
            maxBars.selfCompose().subscribe(Subscriber { result.maxBars = it })
            val bars = result.toBars
            barFrame = BarFrameBuilder.basic(bars)
                .withMax(pctReporting.selfCompose().map { d: Double -> 0.5 / d.coerceAtLeast(1e-6) })
                .withHeader(header.selfCompose())
                .withSubhead(subhead.selfCompose())
                .build()
            add(barFrame)
            if (swingPartyOrder != null) {
                swingFrame = SwingFrameBuilder.prevCurr(
                    prevVotes.selfCompose(),
                    votes.selfCompose()
                        .map { m: Map<Candidate, Int> ->
                            val ret: MutableMap<Party, Int> = LinkedHashMap()
                            m.forEach { (k: Candidate, v: Int) -> ret.merge(k.party, v) { a, b -> Integer.sum(a, b) } }
                            ret
                        },
                    swingPartyOrder
                )
                    .withHeader(swingHeader.selfCompose())
                    .build()
                add(swingFrame)
            }
            if (hasMap) {
                mapFrame = MapFrame(
                    headerPublisher = mapHeader.selfCompose(),
                    shapesPublisher = mapShape.selfCompose(),
                    focusBoxPublisher = mapFocus.selfCompose().map {
                        it.asSequence()
                            .map { obj: Shape -> obj.bounds2D }
                            .reduceOrNull { agg, r -> agg.createUnion(r) }
                    }
                )
                add(mapFrame)
            }
        }
    }

    companion object {
        private fun createHeaderLabel(textPublisher: Flow.Publisher<out String?>): JLabel {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            textPublisher.subscribe(Subscriber(eventQueueWrapper { headerLabel.text = it }))
            return headerLabel
        }

        @JvmStatic fun <T> of(
            list: Flow.Publisher<out List<T>>,
            votesFunc: (T) -> Flow.Publisher<out Map<Candidate, Int>>,
            headerFunc: (T) -> Flow.Publisher<out String>,
            subheadFunc: (T) -> Flow.Publisher<out String>
        ): Builder<T> {
            return Builder(list, votesFunc, headerFunc, subheadFunc, false)
        }

        @JvmStatic fun <T> ofParties(
            list: Flow.Publisher<out List<T>>,
            votesFunc: (T) -> Flow.Publisher<out Map<Party, Int>>,
            headerFunc: (T) -> Flow.Publisher<out String>,
            subheadFunc: (T) -> Flow.Publisher<out String>
        ): Builder<T> {
            val adjustedVoteFunc = { t: T -> votesFunc(t).map { m: Map<Party, Int> -> Aggregators.adjustKey(m) { k: Party -> if (k == Party.OTHERS) Candidate.OTHERS else Candidate("", k) } } }
            return Builder(list, adjustedVoteFunc, headerFunc, subheadFunc, true)
        }
    }
}

private fun <T> Flow.Publisher<out Flow.Publisher<out T>>.selfCompose(): Flow.Publisher<T> = this.compose { it }
