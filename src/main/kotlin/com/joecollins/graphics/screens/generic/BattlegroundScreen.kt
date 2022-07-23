package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.GraphicsFrame
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.screens.generic.BattlegroundScreen.BattlegroundInput.Side
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

            val defenseInput = BattlegroundInput<T>()
            prevResults.subscribe(Subscriber { defenseInput.setPrev(it) })
            currResults.subscribe(Subscriber { defenseInput.setCurr(it) })
            defenseSeatCount.subscribe(Subscriber { defenseInput.setCount(it) })
            party.subscribe(Subscriber { defenseInput.setParty(it) })
            seatFilter.subscribe(Subscriber { defenseInput.setFilteredSeats(it) })
            defenseInput.setSide(Side.DEFENSE)
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

            val targetInput = BattlegroundInput<T>()
            prevResults.subscribe(Subscriber { targetInput.setPrev(it) })
            currResults.subscribe(Subscriber { targetInput.setCurr(it) })
            targetSeatCount.subscribe(Subscriber { targetInput.setCount(it) })
            party.subscribe(Subscriber { targetInput.setParty(it) })
            seatFilter.subscribe(Subscriber { targetInput.setFilteredSeats(it) })
            targetInput.setSide(Side.TARGET)
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

    private class BattlegroundInput<T> {
        enum class Side {
            DEFENSE, TARGET
        }

        private var prev: Map<T, Map<Party, Int>> = HashMap()
        private var curr: Map<T, PartyResult?> = HashMap()
        private var count = 0
        private var party: Party? = null
        private var side = Side.TARGET
        private var filteredSeats: Set<T>? = null

        fun setPrev(prev: Map<T, Map<Party, Int>>) {
            this.prev = prev
            submit()
        }

        fun setCurr(curr: Map<T, PartyResult?>) {
            this.curr = curr
            submit()
        }

        fun setCount(count: Int) {
            this.count = count
            submit()
        }

        fun setParty(party: Party?) {
            this.party = party
            submit()
        }

        fun setSide(side: Side) {
            this.side = side
            submit()
        }

        fun setFilteredSeats(filteredSeats: Set<T>?) {
            this.filteredSeats = filteredSeats
            submit()
        }

        private fun submit() {
            synchronized(this) {
                (items as Publisher<List<Entry<T>>>).submit(getItemsList(this))
            }
        }

        val items: Flow.Publisher<List<Entry<T>>> = Publisher(getItemsList(this))

        companion object {
            private fun <T> getItemsList(t: BattlegroundInput<T>): List<Entry<T>> {
                return t.prev.entries.asSequence()
                    .map { e ->
                        val votes = e.value
                        val total = votes.values.sum()
                        val topTwo = votes.entries
                            .sortedByDescending { it.value }
                            .take(2)
                            .toList()
                        val margin: Double = if (t.side == Side.TARGET) {
                            if (topTwo[0].key == t.party) Double.NaN else 1.0 * (
                                topTwo[0].value - (
                                    votes[t.party]
                                        ?: 0
                                    )
                                ) / total
                        } else {
                            if (topTwo[0].key != t.party) Double.NaN else 1.0 * (
                                (
                                    votes[t.party]
                                        ?: 0
                                    ) - topTwo[1].value
                                ) / total
                        }
                        Triple(e.key, margin, topTwo[0].key.color)
                    }
                    .filter { !it.second.isNaN() }
                    .sortedBy { it.second }
                    .take(t.count)
                    .map {
                        val partyResult = t.curr[it.first]
                        val resultColor: Color
                        val fill: Boolean
                        if (partyResult == null) {
                            resultColor = Color.BLACK
                            fill = false
                        } else {
                            resultColor = partyResult.party?.color ?: Color.BLACK
                            fill = partyResult.isElected
                        }
                        val colorFunc = if (t.filteredSeats?.contains(it.first) != false) { c: Color -> c } else { c -> lighten(lighten(c)) }
                        Entry(
                            it.first, colorFunc(it.third), colorFunc(resultColor), fill
                        )
                    }
                    .toList()
            }
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
