package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.bindings.mapElements
import com.joecollins.graphics.components.GraphicsFrame
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.screens.generic.BattlegroundScreen.BattlegroundInput.Side
import com.joecollins.graphics.utils.ColorUtils.lighten
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import java.util.HashMap
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
        prevResults: Binding<Map<T, Map<Party, Int>>>,
        currResults: Binding<Map<T, PartyResult?>>,
        private val nameFunc: (T) -> String,
        party: Binding<Party>
    ) {
        private val prevResults: BindingReceiver<Map<T, Map<Party, Int>>> = BindingReceiver(prevResults)
        private val currResults: BindingReceiver<Map<T, PartyResult?>> = BindingReceiver(currResults)
        private val party: BindingReceiver<Party> = BindingReceiver(party)
        private var defenseSeatCount = BindingReceiver(Binding.fixedBinding(100))
        private var targetSeatCount = BindingReceiver(Binding.fixedBinding(100))
        private var numRows = BindingReceiver(Binding.fixedBinding(20))
        private var seatFilter = BindingReceiver<Set<T>?>(Binding.fixedBinding(null))

        fun withSeatsToShow(
            defenseSeatCountBinding: Binding<Int>,
            targetSeatCountBinding: Binding<Int>
        ): SinglePartyBuilder<T> {
            defenseSeatCount = BindingReceiver(defenseSeatCountBinding)
            targetSeatCount = BindingReceiver(targetSeatCountBinding)
            return this
        }

        fun withNumRows(numRowsBinding: Binding<Int>): SinglePartyBuilder<T> {
            numRows = BindingReceiver(numRowsBinding)
            return this
        }

        fun withSeatFilter(seatFilterBinding: Binding<Set<T>?>): SinglePartyBuilder<T> {
            seatFilter = BindingReceiver(seatFilterBinding)
            return this
        }

        fun build(title: Binding<String?>): BattlegroundScreen {
            val headerLabel = JLabel()
            headerLabel.font = readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            title.bind { headerLabel.text = it }
            party.getBinding(Party::color).bind { headerLabel.foreground = it }

            val defenseInput = BattlegroundInput<T>()
            prevResults.getBinding().bind { defenseInput.setPrev(it) }
            currResults.getBinding().bind { defenseInput.setCurr(it) }
            defenseSeatCount.getBinding().bind { defenseInput.setCount(it) }
            party.getBinding().bind { defenseInput.setParty(it) }
            seatFilter.getBinding().bind { defenseInput.setFilteredSeats(it) }
            defenseInput.setSide(Side.DEFENSE)
            val defenseItems = defenseInput.items
            val defenseFrame = ResultListingFrame(
                headerBinding = party.getBinding { p: Party -> "$p DEFENSE SEATS" },
                borderColorBinding = party.getBinding(Party::color),
                headerAlignmentBinding = Binding.fixedBinding(GraphicsFrame.Alignment.RIGHT),
                numRowsBinding = numRows.getBinding(),
                itemsBinding = defenseItems.mapElements {
                    ResultListingFrame.Item(
                        text = nameFunc(it.key),
                        border = it.prevColor,
                        background = (if (it.fill) it.resultColor else Color.WHITE),
                        foreground = (if (!it.fill) it.resultColor else Color.WHITE)
                    )
                },
                reversedBinding = Binding.fixedBinding(true)
            )

            val targetInput = BattlegroundInput<T>()
            prevResults.getBinding().bind { targetInput.setPrev(it) }
            currResults.getBinding().bind { targetInput.setCurr(it) }
            targetSeatCount.getBinding().bind { targetInput.setCount(it) }
            party.getBinding().bind { targetInput.setParty(it) }
            seatFilter.getBinding().bind { targetInput.setFilteredSeats(it) }
            targetInput.setSide(Side.TARGET)
            val targetItems = targetInput.items
            val targetFrame = ResultListingFrame(
                headerBinding = party.getBinding<String?> { p: Party -> "$p TARGET SEATS" },
                borderColorBinding = party.getBinding(Party::color),
                headerAlignmentBinding = Binding.fixedBinding(GraphicsFrame.Alignment.LEFT),
                numRowsBinding = numRows.getBinding(),
                itemsBinding = targetItems.mapElements {
                    ResultListingFrame.Item(
                        text = nameFunc(it.key),
                        border = it.prevColor,
                        background = (if (it.fill) it.resultColor else Color.WHITE),
                        foreground = (if (!it.fill) it.resultColor else Color.WHITE)
                    )
                },
                reversedBinding = Binding.fixedBinding(false)
            )

            return BattlegroundScreen(
                    headerLabel,
                    defenseFrame,
                    targetFrame
            ) { screen: BattlegroundScreen ->
                val layout = screen.Layout()
                defenseSeatCount
                        .getBinding()
                        .merge(numRows.getBinding()) { c: Int, n: Int -> n * ceil(1.0 * c / n).toInt() }
                        .bind { layout.setLeft(it) }
                targetSeatCount
                        .getBinding()
                        .merge(numRows.getBinding()) { c: Int, n: Int -> n * ceil(1.0 * c / n).toInt() }
                        .bind { layout.setRight(it) }
                layout
            }
        }
    }

    private class BattlegroundInput<T> : Bindable<BattlegroundInput<T>, BattlegroundInput.Property>() {
        private enum class Property {
            PREV, CURR, PARTY, COUNT, SIDE, FILTERED_SEATS
        }

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
            onPropertyRefreshed(Property.PREV)
        }

        fun setCurr(curr: Map<T, PartyResult?>) {
            this.curr = curr
            onPropertyRefreshed(Property.CURR)
        }

        fun setCount(count: Int) {
            this.count = count
            onPropertyRefreshed(Property.COUNT)
        }

        fun setParty(party: Party?) {
            this.party = party
            onPropertyRefreshed(Property.PARTY)
        }

        fun setSide(side: Side) {
            this.side = side
            onPropertyRefreshed(Property.SIDE)
        }

        fun setFilteredSeats(filteredSeats: Set<T>?) {
            this.filteredSeats = filteredSeats
            onPropertyRefreshed(Property.FILTERED_SEATS)
        }

        val items: Binding<List<Entry<T>>>
            get() {
                return Binding.propertyBinding(
                        this, { input -> getItemsList(input) },
                        Property.PREV,
                        Property.COUNT,
                        Property.CURR,
                        Property.PARTY,
                        Property.SIDE,
                        Property.FILTERED_SEATS)
            }

        companion object {
            @JvmStatic private fun <T> getItemsList(t: BattlegroundInput<T>): List<Entry<T>> {
                return t.prev.entries.asSequence()
                        .map { e: Map.Entry<T, Map<Party, Int>> ->
                            val votes = e.value
                            val total = votes.values.sum()
                            val topTwo = votes.entries
                                    .sortedByDescending { it.value }
                                    .take(2)
                                    .toList()
                            val margin: Double = if (t.side == Side.TARGET) {
                                if (topTwo[0].key == t.party) Double.NaN else 1.0 * (topTwo[0].value - (votes[t.party]
                                        ?: 0)) / total
                            } else {
                                if (topTwo[0].key != t.party) Double.NaN else 1.0 * ((votes[t.party]
                                        ?: 0) - topTwo[1].value) / total
                            }
                            Triple(e.key, margin, topTwo[0].key.color)
                        }
                        .filter { !java.lang.Double.isNaN(it.second) }
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
                            val colorFunc = if (t.filteredSeats?.contains(it.first) != false) { c -> c } else { c: Color -> lighten(lighten(c)) }
                            Entry(
                                    it.first, colorFunc(it.third), colorFunc(resultColor), fill)
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
        @JvmStatic fun <T> singleParty(
            prevResultsBinding: Binding<Map<T, Map<Party, Int>>>,
            currResultsBinding: Binding<Map<T, PartyResult?>>,
            nameFunc: (T) -> String,
            partyBinding: Binding<Party>
        ): SinglePartyBuilder<T> {
            return SinglePartyBuilder(prevResultsBinding, currResultsBinding, nameFunc, partyBinding)
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
