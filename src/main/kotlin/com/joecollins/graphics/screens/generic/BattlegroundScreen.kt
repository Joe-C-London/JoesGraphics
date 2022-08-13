package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.GraphicsFrame
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.utils.ColorUtils.lighten
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.ceil

class BattlegroundScreen private constructor(
    title: JLabel,
    private val leftPanel: ResultListingFrame,
    private val rightPanel: ResultListingFrame,
    lowerLayout: (BattlegroundScreen) -> Layout
) : JPanel() {

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

        fun build(title: Flow.Publisher<out String?>): BattlegroundScreen {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            title.subscribe(Subscriber(eventQueueWrapper { headerLabel.text = it }))
            party.map(Party::color).subscribe(Subscriber(eventQueueWrapper { headerLabel.foreground = it }))

            val defenseInput = DefenseBattlegroundInput<T>()
            prevResults.subscribe(Subscriber { defenseInput.prev = it })
            currResults.subscribe(Subscriber { defenseInput.curr = it })
            defenseSeatCount.subscribe(Subscriber { defenseInput.count = it })
            party.subscribe(Subscriber { defenseInput.party = it })
            seatFilter.subscribe(Subscriber { defenseInput.filteredSeats = it })
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
                headerLabel,
                defenseFrame,
                targetFrame
            ) { screen ->
                val layout = screen.Layout()
                defenseSeatCount
                    .merge(numRows) { c, n -> n * ceil(1.0 * c / n).toInt() }
                    .subscribe(Subscriber(eventQueueWrapper { layout.setLeft(it) }))
                targetSeatCount
                    .merge(numRows) { c, n -> n * ceil(1.0 * c / n).toInt() }
                    .subscribe(Subscriber(eventQueueWrapper { layout.setRight(it) }))
                layout
            }
        }
    }

    class DoublePartyBuilder<T>(
        private val prevResults: Flow.Publisher<out Map<T, Map<Party, Int>>>,
        private val currResults: Flow.Publisher<out Map<T, PartyResult?>>,
        private val nameFunc: (T) -> String,
        private val parties: Flow.Publisher<out Pair<Party, Party>>
    ) {
        private var leftSeatCount: Flow.Publisher<out Int> = 100.asOneTimePublisher()
        private var rightSeatCount: Flow.Publisher<out Int> = 100.asOneTimePublisher()
        private var numRows: Flow.Publisher<out Int> = 20.asOneTimePublisher()
        private var seatFilter: Flow.Publisher<out Set<T>?> = (null as Set<T>?).asOneTimePublisher()
        private var headerFunc: (Party) -> String = { "$it PREVIOUS SEATS" }

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

        fun withHeaderFunc(headerFunc: (Party) -> String): DoublePartyBuilder<T> {
            this.headerFunc = headerFunc
            return this
        }

        fun build(title: Flow.Publisher<out String?>): BattlegroundScreen {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            headerLabel.foreground = Color.BLACK
            title.subscribe(Subscriber(eventQueueWrapper { headerLabel.text = it }))

            val leftInput = DoubleBattlegroundInput<T>()
            prevResults.subscribe(Subscriber { leftInput.prev = it })
            currResults.subscribe(Subscriber { leftInput.curr = it })
            leftSeatCount.subscribe(Subscriber { leftInput.count = it })
            parties.subscribe(Subscriber { leftInput.party = it })
            seatFilter.subscribe(Subscriber { leftInput.filteredSeats = it })
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
                headerLabel,
                leftFrame,
                rightFrame
            ) { screen ->
                val layout = screen.Layout()
                leftSeatCount
                    .merge(numRows) { c, n -> n * ceil(1.0 * c / n).toInt() }
                    .subscribe(Subscriber(eventQueueWrapper { layout.setLeft(it) }))
                rightSeatCount
                    .merge(numRows) { c, n -> n * ceil(1.0 * c / n).toInt() }
                    .subscribe(Subscriber(eventQueueWrapper { layout.setRight(it) }))
                layout
            }
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

        private fun submit() {
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
                    if (margin == null)
                        null
                    else
                        Triple(e.key, margin, prevWinner.color)
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
                        resultColor = partyResult.party?.color ?: Color.BLACK
                        fill = partyResult.isElected
                    }
                    val colorFunc = if (filteredSeats?.contains(it.first) != false) { c: Color -> c } else { c -> lighten(lighten(c)) }
                    Entry(
                        it.first, colorFunc(it.third), colorFunc(resultColor), fill
                    )
                }
                .toList()
        }

        protected abstract fun getSortKey(votes: Map<Party, Int>): Double?
    }

    private class DefenseBattlegroundInput<T> : BattlegroundInput<T, Party>() {
        override fun getSortKey(votes: Map<Party, Int>): Double? {
            val total = votes.values.sum()
            val topTwo = votes.entries
                .sortedByDescending { it.value }
                .take(2)
                .toList()
            return if (topTwo[0].key != party)
                null
            else
                ((votes[party] ?: 0) - topTwo[1].value) / total.toDouble()
        }
    }

    private class TargetBattlegroundInput<T> : BattlegroundInput<T, Party>() {
        override fun getSortKey(votes: Map<Party, Int>): Double? {
            val total = votes.values.sum()
            val topTwo = votes.entries
                .sortedByDescending { it.value }
                .take(2)
                .toList()
            return if (topTwo[0].key == party)
                null
            else
                (topTwo[0].value - (votes[party] ?: 0)) / total.toDouble()
        }
    }

    private class DoubleBattlegroundInput<T> : BattlegroundInput<T, Pair<Party, Party>>() {
        override fun getSortKey(votes: Map<Party, Int>): Double? {
            val total = votes.values.sum()
            val prevWinner = votes.entries.maxBy { it.value }.key
            val topTwo = votes.entries
                .filter { it.key == party?.first || it.key == party?.second }
                .sortedByDescending { it.value }
                .toList()
            return if (topTwo.isEmpty() || prevWinner != party?.first)
                null
            else if (topTwo.size < 2)
                topTwo[0].value / total.toDouble()
            else
                (topTwo[0].value - topTwo[1].value) / total.toDouble()
        }
    }

    private class Entry<T>(val key: T, val prevColor: Color, val resultColor: Color, val fill: Boolean)

    private inner class Layout : LayoutManager {
        private var left = 0
        private var right = 0

        fun setLeft(left: Int) {
            this.left = left
            redoLayout()
        }

        fun setRight(right: Int) {
            this.right = right
            redoLayout()
        }

        private fun redoLayout() {
            invalidate()
            revalidate()
            repaint()
        }

        override fun addLayoutComponent(name: String, comp: Component) {}
        override fun removeLayoutComponent(comp: Component) {}
        override fun preferredLayoutSize(parent: Container): Dimension? {
            return null
        }

        override fun minimumLayoutSize(parent: Container): Dimension? {
            return null
        }

        override fun layoutContainer(parent: Container) {
            leftPanel.isVisible = left > 0
            rightPanel.isVisible = right > 0
            val total = left + right
            if (total == 0) {
                return
            }
            val width = parent.width
            val height = parent.height
            val mid = width * left / total
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
            partyPublisher: Flow.Publisher<out Pair<Party, Party>>
        ): DoublePartyBuilder<T> {
            return DoublePartyBuilder(prevResultsPublisher, currResultsPublisher, nameFunc, partyPublisher)
        }
    }

    init {
        background = Color.WHITE
        layout = BorderLayout()
        add(title, BorderLayout.NORTH)
        val panel = JPanel()
        panel.background = Color.WHITE
        panel.border = EmptyBorder(5, 5, 5, 5)
        panel.layout = lowerLayout(this)
        panel.add(leftPanel)
        panel.add(rightPanel)
        add(panel, BorderLayout.CENTER)
    }
}

internal fun <L, R> Pair<L, R>.reverse(): Pair<R, L> {
    return second to first
}
