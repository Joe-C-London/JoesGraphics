package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.GraphicsFrame
import com.joecollins.graphics.components.ResultListingFrame
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
    headerColor: Flow.Publisher<Color>? = null,
    altText: Flow.Publisher<String>,
) : GenericPanel(
    {
        background = Color.WHITE
        border = EmptyBorder(5, 5, 5, 5)
        val layout = Layout(this)
        leftColumns.subscribe(Subscriber(eventQueueWrapper { layout.setLeft(it) }))
        rightColumns.subscribe(Subscriber(eventQueueWrapper { layout.setRight(it) }))
        this.layout = layout
        add(leftPanel, Layout.WEST)
        add(rightPanel, Layout.EAST)
    },
    title,
    altText,
) {

    init {
        headerColor?.subscribe(Subscriber(eventQueueWrapper { super.label.foreground = it }))
    }

    class SingleSeatsToShow internal constructor() {
        lateinit var defense: Flow.Publisher<Int>
        lateinit var target: Flow.Publisher<Int>
    }

    class DoubleSeatsToShow internal constructor() {
        lateinit var left: Flow.Publisher<Int>
        lateinit var right: Flow.Publisher<Int>
    }

    private abstract class BattlegroundInput<T, P> {
        var prev: Map<T, Map<Party, Int>> = HashMap()
            set(value) {
                field = value
                submit()
            }

        var prevWinners: Map<T, Party> = HashMap()
            set(value) {
                field = value
                submit()
            }

        var curr: Map<T, PartyResult?> = HashMap()
            set(value) {
                field = value
                submit()
            }

        var count = DEFAULT_COUNT
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
            return prevWinners.entries.asSequence()
                .mapNotNull { e ->
                    val votes = prev[e.key] ?: emptyMap()
                    val prevWinner = e.value
                    val margin: Double? = getSortKey(votes, prevWinner)
                    if (margin == null) {
                        null
                    } else {
                        Triple(e.key, margin, prevWinner)
                    }
                }
                .sortedBy { it.second }
                .take(count)
                .map {
                    Entry(
                        it.first,
                        it.third,
                        curr[it.first],
                        filteredSeats?.contains(it.first) ?: true,
                    )
                }
                .toList()
        }

        protected abstract fun getSortKey(votes: Map<Party, Int>, prevWinner: Party): Double?
    }

    private class DefenseBattlegroundInput<T> : BattlegroundInput<T, Party>() {
        override fun getSortKey(votes: Map<Party, Int>, prevWinner: Party): Double? {
            if (prevWinner.let { partyChanges[it] ?: it } != party) {
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
        override fun getSortKey(votes: Map<Party, Int>, prevWinner: Party): Double? {
            if (prevWinner.let { partyChanges[it] ?: it } == party) {
                return null
            }
            val adjustedVotes = Aggregators.adjustKey(votes) { partyChanges[it] ?: it }
            val partyPrevVotes = adjustedVotes[party] ?: 0
            val prevWinnerVotes = adjustedVotes[partyChanges[prevWinner] ?: prevWinner] ?: 0
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

        override fun getSortKey(votes: Map<Party, Int>, prevWinner: Party): Double? {
            val total = votes.values.sum()
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

    private class Entry<T>(val key: T, prevWinner: Party, val currResult: PartyResult?, val isIncluded: Boolean) {
        fun Color.adjust(): Color {
            return if (isIncluded) {
                this
            } else {
                ColorUtils.lighten(ColorUtils.lighten(this))
            }
        }
        val prevColor: Color = prevWinner.color.adjust()
        val resultColor: Color = (currResult?.leader?.color ?: Color.LIGHT_GRAY).adjust()
        val fill: Boolean = currResult?.elected ?: false
    }

    private class Layout(val parent: JPanel) : LayoutManager {
        private var leftColumn = 0
        private var rightColumn = 0

        private var leftPanel: Component = JPanel()
        private var rightPanel: Component = JPanel()

        companion object {
            const val WEST = "WEST"
            const val EAST = "EAST"
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
        private const val DEFAULT_COUNT = 100
        private const val DEFAULT_ROWS = 20

        fun <T> singleParty(
            prevResults: Flow.Publisher<out Map<T, Map<Party, Int>>>,
            prevWinners: Flow.Publisher<out Map<T, Party>>,
            currResults: Flow.Publisher<out Map<T, PartyResult?>>,
            name: T.() -> String,
            party: Flow.Publisher<out Party>,
            seatsToShow: (SingleSeatsToShow.() -> Unit)? = null,
            numRows: Flow.Publisher<out Int>? = null,
            seatFilter: Flow.Publisher<out Set<T>?>? = null,
            partyChanges: Flow.Publisher<Map<Party, Party>>? = null,
            title: Flow.Publisher<out String?>,
        ): BattlegroundScreen {
            val seatCounts = if (seatsToShow == null) null else SingleSeatsToShow().apply(seatsToShow)

            val defenseInput = DefenseBattlegroundInput<T>()
            prevResults.subscribe(Subscriber { defenseInput.prev = it })
            prevWinners.subscribe(Subscriber { defenseInput.prevWinners = it })
            currResults.subscribe(Subscriber { defenseInput.curr = it })
            seatCounts?.defense?.subscribe(Subscriber { defenseInput.count = it })
            party.subscribe(Subscriber { defenseInput.party = it })
            seatFilter?.subscribe(Subscriber { defenseInput.filteredSeats = it })
            partyChanges?.subscribe(Subscriber { defenseInput.partyChanges = it })
            val defenseItems = defenseInput.items
            val defenseFrame = ResultListingFrame(
                headerPublisher = party.map { "$it DEFENSE SEATS" },
                borderColorPublisher = party.map(Party::color),
                headerAlignmentPublisher = GraphicsFrame.Alignment.RIGHT.asOneTimePublisher(),
                numRowsPublisher = numRows ?: DEFAULT_ROWS.asOneTimePublisher(),
                itemsPublisher = defenseItems.mapElements {
                    ResultListingFrame.Item(
                        text = it.key.name(),
                        border = it.prevColor,
                        background = (if (it.fill) it.resultColor else Color.WHITE),
                        foreground = (if (!it.fill) it.resultColor else Color.WHITE),
                    )
                },
                reversedPublisher = true.asOneTimePublisher(),
            )

            val targetInput = TargetBattlegroundInput<T>()
            prevResults.subscribe(Subscriber { targetInput.prev = it })
            prevWinners.subscribe(Subscriber { targetInput.prevWinners = it })
            currResults.subscribe(Subscriber { targetInput.curr = it })
            seatCounts?.target?.subscribe(Subscriber { targetInput.count = it })
            party.subscribe(Subscriber { targetInput.party = it })
            seatFilter?.subscribe(Subscriber { targetInput.filteredSeats = it })
            partyChanges?.subscribe(Subscriber { targetInput.partyChanges = it })
            val targetItems = targetInput.items
            val targetFrame = ResultListingFrame(
                headerPublisher = party.map { "$it TARGET SEATS" },
                borderColorPublisher = party.map(Party::color),
                headerAlignmentPublisher = GraphicsFrame.Alignment.LEFT.asOneTimePublisher(),
                numRowsPublisher = numRows ?: DEFAULT_ROWS.asOneTimePublisher(),
                itemsPublisher = targetItems.mapElements {
                    ResultListingFrame.Item(
                        text = it.key.name(),
                        border = it.prevColor,
                        background = (if (it.fill) it.resultColor else Color.WHITE),
                        foreground = (if (!it.fill) it.resultColor else Color.WHITE),
                    )
                },
                reversedPublisher = false.asOneTimePublisher(),
            )

            val altText = run {
                val meta = party.run {
                    if (numRows == null) {
                        map { party -> DEFAULT_ROWS to party }
                    } else {
                        merge(numRows) { party, rows -> rows to party }
                    }
                }
                    .merge(currResults) { (rows, party), result -> Triple(rows, party, result.all { r -> r.value?.elected ?: true }) }

                val textGenerator: (List<Entry<T>>, Int, Party, Boolean, String, String) -> String? = {
                        items, rows, party, allDone, winLabel, loseLabel ->
                    if (items.none { it.isIncluded }) {
                        null
                    } else {
                        val groups = items.mapIndexed { index, entry -> index / rows to entry }
                            .filter { it.second.isIncluded }
                            .groupBy({ it.first }, { it.second })
                        groups.entries.joinToString("\n") { (column, entries) ->
                            val categories = entries.groupBy {
                                when {
                                    it.currResult == null -> "PENDING"
                                    it.currResult.leader == party -> winLabel
                                    else -> loseLabel
                                }
                            }.toSortedMap()
                            val counts = categories.entries.joinToString {
                                val count = if (it.key == "PENDING" || allDone) {
                                    "${it.value.size}"
                                } else {
                                    "${it.value.count { e -> e.currResult?.elected ?: false }}(${it.value.size})"
                                }
                                "$count ${it.key}"
                            }
                            "COLUMN ${column + 1}: $counts"
                        }
                    }
                }

                val targetsText = targetInput.items.merge(meta) { items, (rows, party, allDone) ->
                    textGenerator(items, rows, party, allDone, "GAINS", "MISSES")
                }.merge(party) { t, p -> if (t == null) null else "${p.abbreviation} TARGET SEATS\n$t" }

                val defenseText = defenseInput.items.merge(meta) { items, (rows, party, allDone) ->
                    textGenerator(items, rows, party, allDone, "HOLDS", "LOSSES")
                }.merge(party) { t, p -> if (t == null) null else "${p.abbreviation} DEFENSE SEATS\n$t" }

                targetsText.merge(defenseText) { t, d -> listOfNotNull(t, d).takeUnless { it.isEmpty() }?.joinToString("\n\n") }
                    .merge(title) { t, h -> sequenceOf(h, t).filterNotNull().joinToString("\n\n") }
            }

            return BattlegroundScreen(
                title,
                defenseFrame,
                targetFrame,
                columns(seatCounts?.defense, numRows),
                columns(seatCounts?.target, numRows),
                party.map { it.color },
                altText = altText,
            )
        }

        fun <T> doubleParty(
            prevResults: Flow.Publisher<out Map<T, Map<Party, Int>>>,
            prevWinners: Flow.Publisher<out Map<T, Party>>,
            currResults: Flow.Publisher<out Map<T, PartyResult?>>,
            name: T.() -> String,
            parties: Flow.Publisher<out Pair<PartyOrCoalition, PartyOrCoalition>>,
            seatsToShow: (DoubleSeatsToShow.() -> Unit)? = null,
            numRows: Flow.Publisher<out Int>? = null,
            seatFilter: Flow.Publisher<out Set<T>?>? = null,
            headers: PartyOrCoalition.() -> String = { "$this PREVIOUS SEATS" },
            preferencesMode: Boolean = false,
            title: Flow.Publisher<out String?>,
        ): BattlegroundScreen {
            val seatCounts = if (seatsToShow == null) null else DoubleSeatsToShow().apply(seatsToShow)

            val leftInput = DoubleBattlegroundInput<T>()
            prevResults.subscribe(Subscriber { leftInput.prev = it })
            prevWinners.subscribe(Subscriber { leftInput.prevWinners = it })
            currResults.subscribe(Subscriber { leftInput.curr = it })
            seatCounts?.left?.subscribe(Subscriber { leftInput.count = it })
            parties.subscribe(Subscriber { leftInput.party = it })
            seatFilter?.subscribe(Subscriber { leftInput.filteredSeats = it })
            leftInput.preferences = preferencesMode
            val leftItems = leftInput.items
            val leftFrame = ResultListingFrame(
                headerPublisher = parties.map { it.first.headers() },
                borderColorPublisher = parties.map { it.first.color },
                headerAlignmentPublisher = GraphicsFrame.Alignment.RIGHT.asOneTimePublisher(),
                numRowsPublisher = numRows ?: DEFAULT_ROWS.asOneTimePublisher(),
                itemsPublisher = leftItems.mapElements {
                    ResultListingFrame.Item(
                        text = it.key.name(),
                        border = it.prevColor,
                        background = (if (it.fill) it.resultColor else Color.WHITE),
                        foreground = (if (!it.fill) it.resultColor else Color.WHITE),
                    )
                },
                reversedPublisher = true.asOneTimePublisher(),
            )

            val rightInput = DoubleBattlegroundInput<T>()
            prevResults.subscribe(Subscriber { rightInput.prev = it })
            prevWinners.subscribe(Subscriber { rightInput.prevWinners = it })
            currResults.subscribe(Subscriber { rightInput.curr = it })
            seatCounts?.right?.subscribe(Subscriber { rightInput.count = it })
            parties.subscribe(Subscriber { rightInput.party = it.reverse() })
            seatFilter?.subscribe(Subscriber { rightInput.filteredSeats = it })
            rightInput.preferences = preferencesMode
            val rightItems = rightInput.items
            val rightFrame = ResultListingFrame(
                headerPublisher = parties.map { it.second.headers() },
                borderColorPublisher = parties.map { it.second.color },
                headerAlignmentPublisher = GraphicsFrame.Alignment.LEFT.asOneTimePublisher(),
                numRowsPublisher = numRows ?: DEFAULT_ROWS.asOneTimePublisher(),
                itemsPublisher = rightItems.mapElements {
                    ResultListingFrame.Item(
                        text = it.key.name(),
                        border = it.prevColor,
                        background = (if (it.fill) it.resultColor else Color.WHITE),
                        foreground = (if (!it.fill) it.resultColor else Color.WHITE),
                    )
                },
                reversedPublisher = false.asOneTimePublisher(),
            )

            val altText = run {
                val meta = parties.run {
                    if (numRows == null) {
                        map { party -> DEFAULT_ROWS to party }
                    } else {
                        merge(numRows) { party, rows -> rows to party }
                    }
                }
                    .merge(currResults) { (rows, party), result -> Triple(rows, party, result.all { r -> r.value?.elected ?: true }) }

                val textGenerator: (List<Entry<T>>, Int, PartyOrCoalition, PartyOrCoalition, Boolean) -> String? = {
                        items, rows, party, othParty, allDone ->
                    if (items.none { it.isIncluded }) {
                        null
                    } else {
                        val groups = items.mapIndexed { index, entry -> index / rows to entry }
                            .filter { it.second.isIncluded }
                            .groupBy({ it.first }, { it.second })
                        groups.entries.joinToString("\n") { (column, entries) ->
                            val categories = entries.groupBy {
                                when {
                                    it.currResult == null -> "PENDING"
                                    it.currResult.leader == party || party.constituentParties.contains(it.currResult.leader) -> "HOLDS"
                                    it.currResult.leader == othParty || othParty.constituentParties.contains(it.currResult.leader) -> "LOSSES TO ${othParty.abbreviation}"
                                    else -> "OTHER LOSSES"
                                }
                            }.toSortedMap()
                            val counts = categories.entries.joinToString {
                                val count = if (it.key == "PENDING" || allDone) {
                                    "${it.value.size}"
                                } else {
                                    "${it.value.count { e -> e.currResult?.elected ?: false }}(${it.value.size})"
                                }
                                "$count ${it.key}"
                            }
                            "COLUMN ${column + 1}: $counts"
                        }
                    }
                }

                val targetsText = leftInput.items.merge(meta) { items, (rows, party, allDone) ->
                    textGenerator(items, rows, party.first, party.second, allDone)
                }.merge(parties) { t, p -> if (t == null) null else "${p.first.headers()}\n$t" }

                val defenseText = rightInput.items.merge(meta) { items, (rows, party, allDone) ->
                    textGenerator(items, rows, party.second, party.first, allDone)
                }.merge(parties) { t, p -> if (t == null) null else "${p.second.headers()}\n$t" }

                targetsText.merge(defenseText) { t, d -> listOfNotNull(t, d).takeUnless { it.isEmpty() }?.joinToString("\n\n") }
                    .merge(title) { t, h -> sequenceOf(h, t).filterNotNull().joinToString("\n\n") }
            }

            return BattlegroundScreen(
                title,
                leftFrame,
                rightFrame,
                columns(seatCounts?.left, numRows),
                columns(seatCounts?.right, numRows),
                altText = altText,
            )
        }

        private fun columns(count: Flow.Publisher<out Int>?, rows: Flow.Publisher<out Int>?): Flow.Publisher<Int> {
            return if (count != null && rows != null) {
                count.merge(rows) { c, n -> n * ceil(1.0 * c / n).toInt() }
            } else {
                count?.map { c -> DEFAULT_ROWS * ceil(1.0 * c / DEFAULT_ROWS).toInt() }
                    ?: (
                        rows?.map { n -> n * ceil(1.0 * DEFAULT_COUNT / n).toInt() }
                            ?: (DEFAULT_ROWS * ceil(1.0 * DEFAULT_COUNT / DEFAULT_ROWS).toInt()).asOneTimePublisher()
                        )
            }
        }
    }
}

internal fun <L, R> Pair<L, R>.reverse(): Pair<R, L> {
    return second to first
}
