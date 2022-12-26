package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.GraphicsFrame
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.screens.generic.reverse
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import java.util.concurrent.Flow
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.ceil

class BattlegroundScreen private constructor(
    title: Flow.Publisher<out String?>,
    private val leftPanel: ResultListingFrame,
    private val rightPanel: ResultListingFrame,
    private val leftColumns: Flow.Publisher<Int>,
    private val rightColumns: Flow.Publisher<Int>,
    val headerColor: Flow.Publisher<Color>? = null
) : GenericPanel(
    run {
        val panel = JPanel()
        panel.background = Color.WHITE
        panel.border = EmptyBorder(5, 5, 5, 5)
        val layout = Layout(panel)
        leftColumns.subscribe(Subscriber(eventQueueWrapper { layout.setLeft(it) }))
        rightColumns.subscribe(Subscriber(eventQueueWrapper { layout.setRight(it) }))
        panel.layout = layout
        panel.add(leftPanel, Layout.WEST)
        panel.add(rightPanel, Layout.EAST)
        panel
    },
    title
) {

    init {
        headerColor?.subscribe(Subscriber(eventQueueWrapper { super.label.foreground = it }))
    }

    class SinglePartyBuilder<T>(
        private val prevResults: Flow.Publisher<out Map<T, Map<Party, Int>>>,
        private val currResults: Flow.Publisher<out Map<T, PartyResult?>>,
        private val nameFunc: (T) -> String,
        private val party: Flow.Publisher<out Party>
    ) {
        private var defenseSeatCount: Flow.Publisher<out Int> = 100.asOneTimePublisher()
        private var targetSeatCount: Flow.Publisher<out Int> = 100.asOneTimePublisher()
        private var numRows: Flow.Publisher<out Int> = 20.asOneTimePublisher()
        private var seatFilter: Flow.Publisher<out Set<T>?> = (null as Set<T>?).asOneTimePublisher()
        private var partyChanges: Flow.Publisher<Map<Party, Party>> = emptyMap<Party, Party>().asOneTimePublisher()

        fun withSeatsToShow(
            defenseSeatCountPublisher: Flow.Publisher<out Int>,
            targetSeatCountPublisher: Flow.Publisher<out Int>
        ): SinglePartyBuilder<T> {
            defenseSeatCount = defenseSeatCountPublisher
            targetSeatCount = targetSeatCountPublisher
            return this
        }

        fun withNumRows(numRowsPublisher: Flow.Publisher<out Int>): SinglePartyBuilder<T> {
            numRows = numRowsPublisher
            return this
        }

        fun withSeatFilter(seatFilterPublisher: Flow.Publisher<out Set<T>?>): SinglePartyBuilder<T> {
            seatFilter = seatFilterPublisher
            return this
        }

        fun withPartyChanges(changes: Flow.Publisher<Map<Party, Party>>): SinglePartyBuilder<T> {
            partyChanges = changes
            return this
        }

        fun build(title: Flow.Publisher<out String?>): BattlegroundScreen {
            val defenseInput = DefenseBattlegroundInput<T>()
            prevResults.subscribe(Subscriber { defenseInput.prev = it })
            currResults.subscribe(Subscriber { defenseInput.curr = it })
            defenseSeatCount.subscribe(Subscriber { defenseInput.count = it })
            party.subscribe(Subscriber { defenseInput.party = it })
            seatFilter.subscribe(Subscriber { defenseInput.filteredSeats = it })
            partyChanges.subscribe(Subscriber { defenseInput.partyChanges = it })
            val defenseItems = defenseInput.items
            val defenseFrame = ResultListingFrame(
                headerPublisher = party.map { "$it DEFENSE SEATS" },
                borderColorPublisher = party.map(Party::color),
                headerAlignmentPublisher = GraphicsFrame.Alignment.RIGHT.asOneTimePublisher(),
                numRowsPublisher = numRows,
                itemsPublisher = defenseItems.mapElements {
                    ResultListingFrame.Item(
                        text = nameFunc(it.key),
                        border = it.prevColor,
                        background = (if (it.fill) it.resultColor else Color.WHITE),
                        foreground = (if (!it.fill) it.resultColor else Color.WHITE)
                    )
                },
                reversedPublisher = true.asOneTimePublisher()
            )

            val targetInput = TargetBattlegroundInput<T>()
            prevResults.subscribe(Subscriber { targetInput.prev = it })
            currResults.subscribe(Subscriber { targetInput.curr = it })
            targetSeatCount.subscribe(Subscriber { targetInput.count = it })
            party.subscribe(Subscriber { targetInput.party = it })
            seatFilter.subscribe(Subscriber { targetInput.filteredSeats = it })
            partyChanges.subscribe(Subscriber { targetInput.partyChanges = it })
            val targetItems = targetInput.items
            val targetFrame = ResultListingFrame(
                headerPublisher = party.map { "$it TARGET SEATS" },
                borderColorPublisher = party.map(Party::color),
                headerAlignmentPublisher = GraphicsFrame.Alignment.LEFT.asOneTimePublisher(),
                numRowsPublisher = numRows,
                itemsPublisher = targetItems.mapElements {
                    ResultListingFrame.Item(
                        text = nameFunc(it.key),
                        border = it.prevColor,
                        background = (if (it.fill) it.resultColor else Color.WHITE),
                        foreground = (if (!it.fill) it.resultColor else Color.WHITE)
                    )
                },
                reversedPublisher = false.asOneTimePublisher()
            )

            return BattlegroundScreen(
                title,
                defenseFrame,
                targetFrame,
                defenseSeatCount
                    .merge(numRows) { c, n -> n * ceil(1.0 * c / n).toInt() },
                targetSeatCount
                    .merge(numRows) { c, n -> n * ceil(1.0 * c / n).toInt() },
                party.map { it.color }
            )
        }
    }

    class DoublePartyBuilder<T>(
        private val prevResults: Flow.Publisher<out Map<T, Map<Party, Int>>>,
        private val currResults: Flow.Publisher<out Map<T, PartyResult?>>,
        private val nameFunc: (T) -> String,
        private val parties: Flow.Publisher<out Pair<PartyOrCoalition, PartyOrCoalition>>
    ) {
        private var leftSeatCount: Flow.Publisher<out Int> = 100.asOneTimePublisher()
        private var rightSeatCount: Flow.Publisher<out Int> = 100.asOneTimePublisher()
        private var numRows: Flow.Publisher<out Int> = 20.asOneTimePublisher()
        private var seatFilter: Flow.Publisher<out Set<T>?> = (null as Set<T>?).asOneTimePublisher()
        private var headerFunc: (PartyOrCoalition) -> String = { "$it PREVIOUS SEATS" }
        private var preferences = false

        fun withSeatsToShow(
            leftSeatCountPublisher: Flow.Publisher<out Int>,
            rightSeatCountPublisher: Flow.Publisher<out Int>
        ): DoublePartyBuilder<T> {
            leftSeatCount = leftSeatCountPublisher
            rightSeatCount = rightSeatCountPublisher
            return this
        }

        fun withNumRows(numRowsPublisher: Flow.Publisher<out Int>): DoublePartyBuilder<T> {
            numRows = numRowsPublisher
            return this
        }

        fun withSeatFilter(seatFilterPublisher: Flow.Publisher<out Set<T>?>): DoublePartyBuilder<T> {
            seatFilter = seatFilterPublisher
            return this
        }

        fun withHeaderFunc(headerFunc: (PartyOrCoalition) -> String): DoublePartyBuilder<T> {
            this.headerFunc = headerFunc
            return this
        }

        fun withPreferences(): DoublePartyBuilder<T> {
            this.preferences = true
            return this
        }

        fun build(title: Flow.Publisher<out String?>): BattlegroundScreen {
            val leftInput = DoubleBattlegroundInput<T>()
            prevResults.subscribe(Subscriber { leftInput.prev = it })
            currResults.subscribe(Subscriber { leftInput.curr = it })
            leftSeatCount.subscribe(Subscriber { leftInput.count = it })
            parties.subscribe(Subscriber { leftInput.party = it })
            seatFilter.subscribe(Subscriber { leftInput.filteredSeats = it })
            leftInput.preferences = preferences
            val leftItems = leftInput.items
            val leftFrame = ResultListingFrame(
                headerPublisher = parties.map { headerFunc(it.first) },
                borderColorPublisher = parties.map { it.first.color },
                headerAlignmentPublisher = GraphicsFrame.Alignment.RIGHT.asOneTimePublisher(),
                numRowsPublisher = numRows,
                itemsPublisher = leftItems.mapElements {
                    ResultListingFrame.Item(
                        text = nameFunc(it.key),
                        border = it.prevColor,
                        background = (if (it.fill) it.resultColor else Color.WHITE),
                        foreground = (if (!it.fill) it.resultColor else Color.WHITE)
                    )
                },
                reversedPublisher = true.asOneTimePublisher()
            )

            val rightInput = DoubleBattlegroundInput<T>()
            prevResults.subscribe(Subscriber { rightInput.prev = it })
            currResults.subscribe(Subscriber { rightInput.curr = it })
            rightSeatCount.subscribe(Subscriber { rightInput.count = it })
            parties.subscribe(Subscriber { rightInput.party = it.reverse() })
            seatFilter.subscribe(Subscriber { rightInput.filteredSeats = it })
            rightInput.preferences = preferences
            val rightItems = rightInput.items
            val rightFrame = ResultListingFrame(
                headerPublisher = parties.map { headerFunc(it.second) },
                borderColorPublisher = parties.map { it.second.color },
                headerAlignmentPublisher = GraphicsFrame.Alignment.LEFT.asOneTimePublisher(),
                numRowsPublisher = numRows,
                itemsPublisher = rightItems.mapElements {
                    ResultListingFrame.Item(
                        text = nameFunc(it.key),
                        border = it.prevColor,
                        background = (if (it.fill) it.resultColor else Color.WHITE),
                        foreground = (if (!it.fill) it.resultColor else Color.WHITE)
                    )
                },
                reversedPublisher = false.asOneTimePublisher()
            )

            return BattlegroundScreen(
                title,
                leftFrame,
                rightFrame,
                leftSeatCount
                    .merge(numRows) { c, n -> n * ceil(1.0 * c / n).toInt() },
                rightSeatCount
                    .merge(numRows) { c, n -> n * ceil(1.0 * c / n).toInt() }
            )
        }
    }

    private abstract class BattlegroundInput<T, P> {
        var prev: Map<T, Map<Party, Int>> = HashMap()
            set(value) {
                field = value
                submit()
            }

        var curr: Map<T, PartyResult?> = HashMap()
            set(value) {
                field = value
                submit()
            }

        var count = 0
            set(value) {
                field = value
                submit()
            }

        var party: P? = null
            set(value) {
                field = value
                submit()
            }

        var filteredSeats: Set<T>? = null
            set(value) {
                field = value
                submit()
            }

        var partyChanges: Map<Party, Party> = emptyMap()
            set(value) {
                field = value
                submit()
            }

        protected fun submit() {
            synchronized(this) {
                (items as Publisher<List<Entry<T>>>).submit(getItemsList())
            }
        }

        val items: Flow.Publisher<List<Entry<T>>> = Publisher(getItemsList())

        private fun getItemsList(): List<Entry<T>> {
            return prev.entries.asSequence()
                .mapNotNull { e ->
                    val votes = e.value
                    val prevWinner = votes.maxBy { it.value }.key
                    val margin: Double? = getSortKey(votes)
                    if (margin == null) {
                        null
                    } else {
                        Triple(e.key, margin, prevWinner.color)
                    }
                }
                .sortedBy { it.second }
                .take(count)
                .map {
                    val partyResult = curr[it.first]
                    val resultColor: Color
                    val fill: Boolean
                    if (partyResult == null) {
                        resultColor = Color.BLACK
                        fill = false
                    } else {
                        resultColor = partyResult.party.color
                        fill = partyResult.isElected
                    }
                    val colorFunc =
                        if (filteredSeats?.contains(it.first) != false)
                            { c: Color -> c }
                        else
                            { c -> ColorUtils.lighten(ColorUtils.lighten(c))
                    }
                    Entry(
                        it.first,
                        colorFunc(it.third),
                        colorFunc(resultColor),
                        fill
                    )
                }
                .toList()
        }

        protected abstract fun getSortKey(votes: Map<Party, Int>): Double?
    }

    private class DefenseBattlegroundInput<T> : BattlegroundInput<T, Party>() {
        override fun getSortKey(votes: Map<Party, Int>): Double? {
            val prevWinner = votes.entries.maxBy { it.value }
            if (prevWinner.let { partyChanges[it.key] ?: it.key } != party) {
                return null
            }
            val adjustedVotes = Aggregators.adjustKey(votes) { partyChanges[it] ?: it }
            val partyPrevVotes = adjustedVotes[party] ?: 0
            val prevRunnerUpVotes = adjustedVotes
                .filter { it.key != party }
                .maxOfOrNull { it.value }
                ?: 0
            val total = votes.values.sum()
            return (partyPrevVotes - prevRunnerUpVotes) / total.toDouble()
        }
    }

    private class TargetBattlegroundInput<T> : BattlegroundInput<T, Party>() {
        override fun getSortKey(votes: Map<Party, Int>): Double? {
            val prevWinner = votes.entries.maxBy { it.value }
            if (prevWinner.let { partyChanges[it.key] ?: it.key } == party) {
                return null
            }
            val adjustedVotes = Aggregators.adjustKey(votes) { partyChanges[it] ?: it }
            val partyPrevVotes = adjustedVotes[party] ?: 0
            val prevWinnerVotes = adjustedVotes
                .filter { it.key != party }
                .maxOfOrNull { it.value }
                ?: 0
            val total = votes.values.sum()
            return (prevWinnerVotes - partyPrevVotes) / total.toDouble()
        }
    }

    private class DoubleBattlegroundInput<T> : BattlegroundInput<T, Pair<PartyOrCoalition, PartyOrCoalition>>() {

        var preferences: Boolean = false
            set(value) {
                field = value
                submit()
            }

        override fun getSortKey(votes: Map<Party, Int>): Double? {
            val total = votes.values.sum()
            val prevWinner = votes.entries.maxBy { it.value }.key
            if (!sequenceOf(party?.first)
                .filterNotNull()
                .flatMap { it.constituentParties }
                .contains(prevWinner)
            ) {
                return null
            }
            val topTwo = votes.entries
                .filter { e ->
                    preferences ||
                        sequenceOf(party?.first, party?.second)
                            .filterNotNull()
                            .flatMap { it.constituentParties }
                            .contains(e.key)
                }
                .sortedByDescending { it.value }
                .toList()
            return if (topTwo.isEmpty()) {
                null
            } else if (topTwo.size < 2) {
                topTwo[0].value / total.toDouble()
            } else {
                (topTwo[0].value - topTwo[1].value) / total.toDouble()
            }
        }
    }

    private class Entry<T>(val key: T, val prevColor: Color, val resultColor: Color, val fill: Boolean)

    private class Layout(val parent: JPanel) : LayoutManager {
        private var leftColumn = 0
        private var rightColumn = 0

        private var leftPanel: Component = JPanel()
        private var rightPanel: Component = JPanel()

        companion object {
            val WEST = "WEST"
            val EAST = "EAST"
        }

        fun setLeft(left: Int) {
            this.leftColumn = left
            redoLayout()
        }

        fun setRight(right: Int) {
            this.rightColumn = right
            redoLayout()
        }

        private fun redoLayout() {
            parent.invalidate()
            parent.revalidate()
            parent.repaint()
        }

        override fun addLayoutComponent(name: String, comp: Component) {
            if (name == WEST) leftPanel = comp
            if (name == EAST) rightPanel = comp
        }
        override fun removeLayoutComponent(comp: Component) {}
        override fun preferredLayoutSize(parent: Container): Dimension? {
            return null
        }

        override fun minimumLayoutSize(parent: Container): Dimension? {
            return null
        }

        override fun layoutContainer(parent: Container) {
            leftPanel.isVisible = leftColumn > 0
            rightPanel.isVisible = rightColumn > 0
            val total = leftColumn + rightColumn
            if (total == 0) {
                return
            }
            val width = parent.width
            val height = parent.height
            val mid = width * leftColumn / total
            leftPanel.setLocation(5, 5)
            leftPanel.setSize(mid - 10, height - 10)
            rightPanel.setLocation(mid + 5, 5)
            rightPanel.setSize(width - mid - 10, height - 10)
        }
    }

    companion object {
        fun <T> singleParty(
            prevResultsPublisher: Flow.Publisher<out Map<T, Map<Party, Int>>>,
            currResultsPublisher: Flow.Publisher<out Map<T, PartyResult?>>,
            nameFunc: (T) -> String,
            partyPublisher: Flow.Publisher<out Party>
        ): SinglePartyBuilder<T> {
            return SinglePartyBuilder(prevResultsPublisher, currResultsPublisher, nameFunc, partyPublisher)
        }

        fun <T> doubleParty(
            prevResultsPublisher: Flow.Publisher<out Map<T, Map<Party, Int>>>,
            currResultsPublisher: Flow.Publisher<out Map<T, PartyResult?>>,
            nameFunc: (T) -> String,
            partyPublisher: Flow.Publisher<out Pair<PartyOrCoalition, PartyOrCoalition>>
        ): DoublePartyBuilder<T> {
            return DoublePartyBuilder(prevResultsPublisher, currResultsPublisher, nameFunc, partyPublisher)
        }
    }
}

internal fun <L, R> Pair<L, R>.reverse(): Pair<R, L> {
    return second to first
}