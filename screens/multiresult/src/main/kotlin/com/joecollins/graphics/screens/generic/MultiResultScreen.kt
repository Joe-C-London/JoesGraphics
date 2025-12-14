package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.ImageGenerator.plus
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.SwingFrame
import com.joecollins.graphics.components.SwingFrameBuilder
import com.joecollins.graphics.screens.generic.SingleResultMap.Companion.createSingleResultMap
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.CanOverrideSortOrder
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.NonPartisanCandidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
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
import javax.swing.JPanel

class MultiResultScreen private constructor(
    header: Flow.Publisher<out String?>,
    screen: JPanel,
    altText: Flow.Publisher<String>,
) : GenericPanel(screen, header, altText),
    AltTextProvider {
    private val panels: MutableList<ResultPanel<*>> = ArrayList()

    class CurrVotes<T, K> internal constructor() {
        lateinit var votes: T.() -> Flow.Publisher<out Map<K, Int>>
        lateinit var header: T.() -> Flow.Publisher<out String>
        lateinit var subhead: T.() -> Flow.Publisher<out String?>
        var incumbentMarker: String? = null
        var winner: (T.() -> Flow.Publisher<out K?>)? = null
        var runoff: (T.() -> Flow.Publisher<out Set<K>?>)? = null
        var pctReporting: (T.() -> Flow.Publisher<out Double>)? = null
        var progressLabel: (T.() -> Flow.Publisher<out String?>)? = null
    }

    class CurrPartyVotes<T> internal constructor() {
        lateinit var votes: T.() -> Flow.Publisher<out Map<Party, Int>>
        lateinit var header: T.() -> Flow.Publisher<out String>
        lateinit var subhead: T.() -> Flow.Publisher<out String?>
        var pctReporting: (T.() -> Flow.Publisher<out Double>)? = null
        var progressLabel: (T.() -> Flow.Publisher<out String?>)? = null
    }

    class PrevPartyVotes<T> internal constructor() {
        lateinit var votes: T.() -> Flow.Publisher<out Map<Party, Int>>
        lateinit var swing: Swing<T>.() -> Unit

        val swingProps by lazy { Swing<T>().apply(swing) }
    }

    class Swing<T> internal constructor() {
        lateinit var header: (T.() -> Flow.Publisher<out String>)
        lateinit var partyOrder: List<Party>
        var range: Flow.Publisher<Double>? = null

        internal val rangeOrDefault by lazy { range ?: 0.10.asOneTimePublisher() }
    }

    private class Result<K : CanOverrideSortOrder>(
        private val incumbentMarker: String?,
        private val others: K,
        private val isIncumbent: (K) -> Boolean,
        private val name: (K) -> String,
        private val party: ((K) -> Party)?,
        private val color: (K) -> Color,
        private val pctOnly: Boolean,
    ) {
        var votes: Map<K, Int> = emptyMap()
            set(value) {
                field = value
                updateBars()
            }

        var winner: K? = null
            set(value) {
                field = value
                updateBars()
            }

        var runoff: Set<K> = emptySet()
            set(value) {
                field = value
                updateBars()
            }

        var maxBars: Int = 0
            set(value) {
                field = value
                updateBars()
            }

        val toBars = Publisher(calculateBars())
        private fun updateBars() = synchronized(this) { toBars.submit(calculateBars()) }
        private fun calculateBars(): List<BarFrameBuilder.BasicBar> {
            val total = votes.values.sum()
            val bars = Aggregators.topAndOthers(
                votes,
                maxBars,
                others,
                listOfNotNull(winner),
            )
                .entries
                .asSequence()
                .sortedByDescending { e -> e.key.overrideSortOrder ?: e.value }
                .map { e ->
                    val candidate = e.key
                    val votes = e.value
                    val pct = 1.0 * votes / total
                    val useIncumbentMarker = (incumbentMarker != null && isIncumbent(candidate))
                    val isWinner = candidate == winner
                    val isRunoff = runoff.contains(candidate)
                    val shape: List<Shape?> = listOf(
                        when {
                            isWinner -> ImageGenerator.createTickShape()
                            isRunoff -> ImageGenerator.createRunoffShape()
                            else -> null
                        },
                        party?.let { ImageGenerator.createBoxedTextShape(it(candidate).name.uppercase()) } +
                            (
                                if (!useIncumbentMarker) {
                                    null
                                } else {
                                    ImageGenerator.createFilledBoxedTextShape(incumbentMarker)
                                }
                                ),
                    )
                    val leftLabel: List<String> = when {
                        candidate === others -> {
                            listOf("OTHERS")
                        }
                        else -> {
                            listOfNotNull(
                                name(candidate).uppercase(),
                                if (useIncumbentMarker) "" else party?.takeUnless { it(candidate) == candidate }?.let { "" },
                            )
                        }
                    }
                    val rightLabel: List<String> = when {
                        pct.isNaN() -> {
                            listOf("WAITING...")
                        }
                        pctOnly -> {
                            listOf(DecimalFormat("0.0%").format(pct))
                        }
                        else -> {
                            listOf(DecimalFormat("#,##0").format(votes.toLong()), DecimalFormat("0.0%").format(pct))
                        }
                    }
                    BarFrameBuilder.BasicBar.of(
                        leftLabel.zip(shape),
                        color(candidate),
                        if (pct.isNaN()) 0 else pct,
                        rightLabel,
                    )
                }
                .toList()
            return sequenceOf(
                bars.asSequence(),
                generateSequence {
                    BarFrameBuilder.BasicBar.of("", Color.WHITE, 0, "")
                },
            )
                .flatten()
                .take(maxBars)
                .toList()
        }
    }

    private class ResultPanel<K : CanOverrideSortOrder>(
        incumbentMarker: String?,
        swingPartyOrder: List<Party>?,
        hasMap: Boolean,
        others: K,
        isIncumbent: (K) -> Boolean,
        name: (K) -> String,
        private val party: ((K) -> Party)?,
        color: (K) -> Color,
        pctOnly: Boolean,
    ) : JPanel() {
        private val barFrame: BarFrame
        private var swingFrame: SwingFrame? = null
        private var mapFrame: MapFrame? = null
        var displayBothRows = true
        private val votes: Publisher<Flow.Publisher<out Map<K, Int>>> = Publisher(Publisher())
        private val header: Publisher<Flow.Publisher<out String>> = Publisher(Publisher())
        private val subhead: Publisher<Flow.Publisher<out String?>> = Publisher(Publisher())
        private val pctReporting: Publisher<Flow.Publisher<out Double>> = Publisher(Publisher(1.0))
        private val progressLabel: Publisher<Flow.Publisher<out String?>> = Publisher(Publisher())
        private val winner: Publisher<Flow.Publisher<out K?>> = Publisher(Publisher())
        private val runoff: Publisher<Flow.Publisher<out Set<K>?>> = Publisher(Publisher())
        private val prevVotes: Publisher<Flow.Publisher<out Map<Party, Int>>> = Publisher(Publisher())
        private val maxBars: Publisher<Flow.Publisher<out Int>> = Publisher(Publisher())
        private val swingHeader: Publisher<Flow.Publisher<out String?>> = Publisher(Publisher())
        private val swingRange: Publisher<Flow.Publisher<out Double>> = Publisher(Publisher())
        private val map: Publisher<Flow.Publisher<out AbstractSingleResultMap<*, *>>> = Publisher(Publisher())

        fun setVotesPublisher(votes: Flow.Publisher<out Map<K, Int>>) {
            this.votes.submit(votes)
        }

        fun setHeaderPublisher(header: Flow.Publisher<out String>) {
            this.header.submit(header)
        }

        fun setSubheadPublisher(subhead: Flow.Publisher<out String?>) {
            this.subhead.submit(subhead)
        }

        fun setWinnerPublisher(winner: Flow.Publisher<out K?>) {
            this.winner.submit(winner)
        }

        fun setRunoffPublisher(runoff: Flow.Publisher<out Set<K>?>) {
            this.runoff.submit(runoff)
        }

        fun setPctReportingPublisher(pctReporting: Flow.Publisher<out Double>) {
            this.pctReporting.submit(pctReporting)
        }

        fun setProgressLabelPublisher(progressLabel: Flow.Publisher<out String?>) {
            this.progressLabel.submit(progressLabel)
        }

        fun setPrevPublisher(prev: Flow.Publisher<out Map<Party, Int>>) {
            prevVotes.submit(prev)
        }

        fun setSwingHeaderPublisher(swingHeader: Flow.Publisher<out String?>) {
            this.swingHeader.submit(swingHeader)
        }

        fun setSwingRangePublisher(swingRange: Flow.Publisher<out Double>) {
            this.swingRange.submit(swingRange)
        }

        fun setMapPublisher(map: Flow.Publisher<out AbstractSingleResultMap<*, *>>) {
            this.map.submit(map)
        }

        fun setMaxBarsPublisher(maxBars: Flow.Publisher<out Int>) {
            this.maxBars.submit(maxBars)
        }

        fun unbindAll() {
            setVotesPublisher(emptyMap<K, Int>().asOneTimePublisher())
            setHeaderPublisher("".asOneTimePublisher())
            setSubheadPublisher("".asOneTimePublisher())
            setWinnerPublisher(null.asOneTimePublisher())
            setRunoffPublisher(emptySet<K>().asOneTimePublisher())
            setPctReportingPublisher(0.0.asOneTimePublisher())
            setProgressLabelPublisher(null.asOneTimePublisher())
            setPrevPublisher(emptyMap<Party, Int>().asOneTimePublisher())
            setSwingHeaderPublisher("".asOneTimePublisher())
            setSwingRangePublisher(0.10.asOneTimePublisher())
            setMapPublisher(NULL_MAP.asOneTimePublisher())
            setMaxBarsPublisher(5.asOneTimePublisher())
        }

        private inner class ResultPanelLayout : LayoutManager {
            override fun addLayoutComponent(name: String, comp: Component) {}
            override fun removeLayoutComponent(comp: Component) {}
            override fun preferredLayoutSize(parent: Container): Dimension? = null

            override fun minimumLayoutSize(parent: Container): Dimension? = null

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
            val result = Result(
                incumbentMarker,
                others,
                isIncumbent,
                name,
                party,
                color,
                pctOnly,
            )
            votes.selfCompose().subscribe(Subscriber { result.votes = it })
            winner.selfCompose().subscribe(Subscriber { result.winner = it })
            runoff.selfCompose().subscribe(Subscriber { result.runoff = it ?: emptySet() })
            maxBars.selfCompose().subscribe(Subscriber { result.maxBars = it })
            val bars = result.toBars
            barFrame = BarFrameBuilder.basic(
                barsPublisher = bars,
                maxPublisher = pctReporting.selfCompose().map { 0.5 / it.coerceAtLeast(1e-6) },
                headerPublisher = header.selfCompose(),
                rightHeaderLabelPublisher = progressLabel.selfCompose(),
                subheadPublisher = subhead.selfCompose(),
            )
            add(barFrame)
            if (swingPartyOrder != null && party != null) {
                swingFrame = SwingFrameBuilder.prevCurr(
                    prev = prevVotes.selfCompose(),
                    curr = votes.selfCompose()
                        .map { m ->
                            m.entries.groupingBy { party(it.key) }.fold(0) { a, e -> a + e.value }
                        },
                    partyOrder = swingPartyOrder,
                    header = swingHeader.selfCompose(),
                    range = swingRange.selfCompose(),
                )
                add(swingFrame)
            }
            if (hasMap) {
                mapFrame = AbstractSingleResultMap.createMapFrame(map.selfCompose())
                add(mapFrame)
            }
        }
    }

    companion object {

        fun <T> of(
            list: Flow.Publisher<out List<T>>,
            curr: CurrVotes<T, Candidate>.() -> Unit,
            prev: (PrevPartyVotes<T>.() -> Unit)? = null,
            map: ((T) -> AbstractSingleResultMap<*, *>)? = null,
            title: Flow.Publisher<out String?>,
        ): MultiResultScreen = build(
            list,
            CurrVotes<T, Candidate>().apply(curr),
            prev?.let { PrevPartyVotes<T>().apply(it) },
            map,
            title,
            Candidate.OTHERS,
            { it.incumbent },
            { it.name },
            { it.party },
            { it.party.color },
            false,
        )

        fun <T> ofParties(
            list: Flow.Publisher<out List<T>>,
            curr: CurrPartyVotes<T>.() -> Unit,
            prev: (PrevPartyVotes<T>.() -> Unit)? = null,
            map: ((T) -> AbstractSingleResultMap<*, *>)? = null,
            title: Flow.Publisher<out String?>,
        ): MultiResultScreen {
            val currVotes = CurrPartyVotes<T>().apply(curr)
            val candidateVotes = CurrVotes<T, Party>().apply {
                this.votes = { currVotes.votes(this) }
                this.header = currVotes.header
                this.subhead = currVotes.subhead
                this.pctReporting = currVotes.pctReporting
                this.progressLabel = currVotes.progressLabel
            }
            return build(
                list,
                candidateVotes,
                prev?.let { PrevPartyVotes<T>().apply(it) },
                map,
                title,
                Party.OTHERS,
                { false },
                { it.name },
                { it },
                { it.color },
                true,
            )
        }

        fun <T> ofNonPartisan(
            list: Flow.Publisher<out List<T>>,
            curr: CurrVotes<T, NonPartisanCandidate>.() -> Unit,
            map: ((T) -> AbstractSingleResultMap<*, *>)? = null,
            title: Flow.Publisher<out String?>,
        ): MultiResultScreen = build(
            list,
            CurrVotes<T, NonPartisanCandidate>().apply(curr),
            null,
            map,
            title,
            NonPartisanCandidate.OTHERS,
            { it.incumbent },
            { it.fullName },
            null,
            { it.color },
            false,
        )

        private fun <T, K : CanOverrideSortOrder> build(
            listPublisher: Flow.Publisher<out List<T>>,
            curr: CurrVotes<T, K>,
            prev: PrevPartyVotes<T>?,
            map: ((T) -> AbstractSingleResultMap<*, *>)?,
            title: Flow.Publisher<out String?>,
            others: K,
            isIncumbent: (K) -> Boolean,
            name: (K) -> String,
            party: ((K) -> Party)?,
            color: (K) -> Color,
            pctOnly: Boolean,
        ): MultiResultScreen {
            val itemPublishers: MutableList<Flow.Publisher<out T?>> = ArrayList()
            val center = JPanel()
            val altText = listPublisher.map { list ->
                list.filterNotNull().map { e ->
                    val headerPub = curr.header(e).merge(curr.subhead(e)) { head, sub ->
                        if (sub.isNullOrEmpty()) {
                            head
                        } else if (head.isEmpty()) {
                            sub
                        } else {
                            "$head, $sub"
                        }
                    }.run {
                        if (curr.progressLabel == null) {
                            this
                        } else {
                            merge(curr.progressLabel!!(e)) { head, prog ->
                                head + (prog?.let { " [$it]" } ?: "")
                            }
                        }
                    }
                    val winnerAndRunoff = when {
                        curr.winner != null && curr.runoff != null -> curr.winner!!(e).merge(curr.runoff!!(e)) { w, r -> w to r }
                        curr.winner != null -> curr.winner!!(e).map { w -> w to null }
                        curr.runoff != null -> curr.runoff!!(e).map { r -> null to r }
                        else -> (null to null).asOneTimePublisher()
                    }
                    val entriesPub = curr.votes(e).merge(winnerAndRunoff) { votes, (winner, runoff) ->
                        val total = votes.values.sum().toDouble()
                        Aggregators.topAndOthers(votes, (if (list.size > 4) 4 else 5) * (if (pctOnly) 2 else 1), others).entries
                            .sortedByDescending { it.key.overrideSortOrder ?: it.value }
                            .joinToString("\n") { (c, v) ->
                                "${
                                    if (c == others) {
                                        "OTHERS"
                                    } else {
                                        "${name(c).uppercase()}${
                                            if (curr.incumbentMarker != null && isIncumbent(c)) " [${curr.incumbentMarker}]" else ""
                                        }${if (party == null || party(c) == c) "" else " (${party(c).abbreviation})"}"
                                    }
                                }: ${
                                    if (total == 0.0) {
                                        "WAITING..."
                                    } else if (pctOnly) {
                                        DecimalFormat("0.0%").format(v / total)
                                    } else {
                                        "${DecimalFormat("#,##0").format(v)} (${DecimalFormat("0.0%").format(v / total)})"
                                    }
                                }${
                                    if (c == winner) {
                                        " WINNER"
                                    } else if (runoff?.contains(c) == true) {
                                        " RUNOFF"
                                    } else {
                                        ""
                                    }
                                }"
                            }
                    }
                    headerPub.merge(entriesPub) { h, s -> "$h\n$s" }
                }.combine().map { it.joinToString("\n\n") }
            }.selfCompose().merge(title) { items, head ->
                if (head == null) {
                    items
                } else {
                    "$head\n\n$items"
                }
            }
            val screen = MultiResultScreen(title, center, altText)
            center.layout = GridLayout(1, 0)
            center.background = Color.WHITE
            listPublisher.subscribe(
                Subscriber { list ->
                    val size = list.size
                    while (screen.panels.size < size) {
                        val idx = screen.panels.size
                        val itemPublisher =
                            listPublisher.map {
                                if (idx < it.size) {
                                    it[idx]
                                } else {
                                    null
                                }
                            }
                        itemPublishers.add(itemPublisher)
                        val newPanel = ResultPanel(
                            curr.incumbentMarker,
                            prev?.swingProps?.partyOrder,
                            map != null,
                            others,
                            isIncumbent,
                            name,
                            party,
                            color,
                            pctOnly,
                        )
                        newPanel.setVotesPublisher(
                            itemPublisher.compose {
                                it?.let(curr.votes)
                                    ?: emptyMap<K, Int>().asOneTimePublisher()
                            },
                        )
                        if (curr.winner != null) {
                            newPanel.setWinnerPublisher(
                                itemPublisher.compose {
                                    it?.let(curr.winner!!)
                                        ?: (null as K?).asOneTimePublisher()
                                },
                            )
                        }
                        if (curr.runoff != null) {
                            newPanel.setRunoffPublisher(
                                itemPublisher.compose {
                                    it?.let(curr.runoff!!)
                                        ?: (null as Set<K>?).asOneTimePublisher()
                                },
                            )
                        }
                        if (curr.pctReporting != null) {
                            newPanel.setPctReportingPublisher(
                                itemPublisher.compose {
                                    it?.let(curr.pctReporting!!) ?: 0.0.asOneTimePublisher()
                                },
                            )
                        }
                        if (curr.progressLabel != null) {
                            newPanel.setProgressLabelPublisher(
                                itemPublisher.compose {
                                    it?.let(curr.progressLabel!!) ?: null.asOneTimePublisher()
                                },
                            )
                        }
                        newPanel.setHeaderPublisher(
                            itemPublisher.compose {
                                it?.let(curr.header) ?: "".asOneTimePublisher()
                            },
                        )
                        newPanel.setSubheadPublisher(
                            itemPublisher.compose {
                                it?.let(curr.subhead) ?: "".asOneTimePublisher()
                            },
                        )
                        if (prev != null) {
                            prev.votes.let { f ->
                                newPanel.setPrevPublisher(
                                    itemPublisher.compose {
                                        it?.let(f)
                                            ?: emptyMap<Party, Int>().asOneTimePublisher()
                                    },
                                )
                            }
                            newPanel.setSwingHeaderPublisher(
                                itemPublisher.compose {
                                    it?.let(prev.swingProps.header) ?: "".asOneTimePublisher()
                                },
                            )
                            newPanel.setSwingRangePublisher(prev.swingProps.rangeOrDefault)
                        }
                        if (map != null) {
                            newPanel.setMapPublisher(
                                itemPublisher.map { it?.let(map) ?: NULL_MAP },
                            )
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
                    screen.panels.forEach {
                        it.displayBothRows = numRows == 1
                        it.setMaxBarsPublisher(
                            ((if (numRows == 2) 4 else 5) * if (pctOnly) 2 else 1).asOneTimePublisher(),
                        )
                    }
                    EventQueue.invokeLater {
                        screen.panels.forEach { p ->
                            p.invalidate()
                            p.revalidate()
                        }
                        screen.repaint()
                    }
                },
            )

            return screen
        }

        private val NULL_MAP: AbstractSingleResultMap<*, *> = createSingleResultMap {
            shapes = emptyMap<Nothing?, Shape>().asOneTimePublisher()
            leader = null.asOneTimePublisher()
            selectedShape = null.asOneTimePublisher()
            header = null.asOneTimePublisher()
        }
    }
}

private fun <T> Flow.Publisher<out Flow.Publisher<out T>>.selfCompose(): Flow.Publisher<T> = this.compose { it }
