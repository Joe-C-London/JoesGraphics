package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.RegionSummaryFrame
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import com.joecollins.models.general.Party
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import java.text.DecimalFormat
import java.util.ArrayList
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.ceil

class PartySummaryScreen private constructor(
    headerLabel: JLabel,
    mainFrame: RegionSummaryFrame,
    otherFrames: List<RegionSummaryFrame>,
    numRows: Int
) : JPanel() {

    private class Layout constructor(private val numRows: Int) : LayoutManager {
        private var main: Component = JPanel()
        private val others: MutableList<Component> = ArrayList()

        override fun addLayoutComponent(name: String, comp: Component) {
            if ("main" == name) main = comp else others.add(comp)
        }

        override fun removeLayoutComponent(comp: Component) {
            if (main === comp) main = JPanel()
            others.remove(comp)
        }

        override fun preferredLayoutSize(parent: Container): Dimension {
            return Dimension(1024, 512)
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            return Dimension(128, 64)
        }

        override fun layoutContainer(parent: Container) {
            val numOtherCols = ceil(1.0 * others.size / numRows).toInt()
            val numTotalCols = numRows + numOtherCols
            val widthPerCol = 1.0 * parent.width / numTotalCols
            val heightPerRow = 1.0 * parent.height / numRows
            main.setLocation(5, 5)
            main.setSize((numRows * widthPerCol - 10).toInt(), (numRows * heightPerRow - 10).toInt())
            for (i in others.indices) {
                val other = others[i]
                val row = i / numOtherCols
                val col = i % numOtherCols + numRows
                other.setLocation((col * widthPerCol + 5).toInt(), (row * heightPerRow + 5).toInt())
                other.setSize((widthPerCol - 10).toInt(), (heightPerRow - 10).toInt())
            }
        }
    }

    class Builder<T>(
        private val mainRegion: T,
        private val titleFunc: (T) -> Binding<String>,
        private val numRows: Int
    ) {
        private var seatFunc: ((T) -> Binding<Map<Party, Int>>)? = null
        private var seatDiffFunc: ((T) -> Binding<Map<Party, Int>>)? = null
        private var seatsHeader = "SEATS"
        private var votePctFunc: ((T) -> Binding<Map<Party, Double>>)? = null
        private var votePctDiffFunc: ((T) -> Binding<Map<Party, Double>>)? = null
        private var voteHeader = "POPULAR VOTE"

        private val regions: MutableList<T> = ArrayList()

        fun withSeatAndDiff(
            seatFunc: ((T) -> Binding<Map<Party, Int>>),
            seatDiffFunc: ((T) -> Binding<Map<Party, Int>>),
            seatsHeader: String = "SEATS"
        ): Builder<T> {
            this.seatFunc = seatFunc
            this.seatDiffFunc = seatDiffFunc
            this.seatsHeader = seatsHeader
            return this
        }

        fun withSeatAndPrev(
            seatFunc: ((T) -> Binding<Map<Party, Int>>),
            seatPrevFunc: ((T) -> Binding<Map<Party, Int>>),
            seatsHeader: String = "SEATS"
        ): Builder<T> {
            val seatDiffFunc = { t: T ->
                val curr = seatFunc(t)
                val prev = seatPrevFunc(t)
                curr.merge(prev) { c, p ->
                    sequenceOf(c.keys.asSequence(), p.keys.asSequence()).flatten()
                        .distinct()
                        .associateWith { party: Party -> (c[party] ?: 0) - (p[party] ?: 0) }
                }
            }
            return withSeatAndDiff(seatFunc, seatDiffFunc, seatsHeader)
        }

        fun withVotePctAndDiff(
            votePctFunc: ((T) -> Binding<Map<Party, Double>>),
            votePctDiffFunc: ((T) -> Binding<Map<Party, Double>>),
            voteHeader: String = "POPULAR VOTE"
        ): Builder<T> {
            this.votePctFunc = votePctFunc
            this.votePctDiffFunc = votePctDiffFunc
            this.voteHeader = voteHeader
            return this
        }

        fun withVotePctAndPrev(
            votePctFunc: ((T) -> Binding<Map<Party, Double>>),
            votePctPrevFunc: ((T) -> Binding<Map<Party, Double>>),
            voteHeader: String = "POPULAR VOTE"
        ): Builder<T> {
            val votePctDiffFunc = { t: T ->
                val curr = votePctFunc(t)
                val prev = votePctPrevFunc(t)
                curr.merge(prev) { c, p ->
                    sequenceOf(c.keys.asSequence(), p.keys.asSequence()).flatten()
                        .distinct()
                        .associateWith { party: Party -> (c[party] ?: 0.0) - (p[party] ?: 0.0) }
                }
            }
            return withVotePctAndDiff(votePctFunc, votePctDiffFunc, voteHeader)
        }

        fun withRegion(region: T): Builder<T> {
            regions.add(region)
            return this
        }

        fun build(partyBinding: Binding<Party>): PartySummaryScreen {
            val party = BindingReceiver(partyBinding)
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            party.getBinding { it.name.uppercase() + " SUMMARY" }.bind { headerLabel.text = it }
            party.getBinding(Party::color).bind { headerLabel.foreground = it }
            val mainFrame = createFrame(mainRegion, party)
            val otherFrames = regions.map { createFrame(it, party) }
            return PartySummaryScreen(headerLabel, mainFrame, otherFrames, numRows)
        }

        private fun createFrame(region: T, party: BindingReceiver<Party>): RegionSummaryFrame {
            val input = SinglePartyInput()
            seatFunc?.invoke(region)?.bind { input.seats = it }
            seatDiffFunc?.invoke(region)?.bind { input.seatDiff = it }
            votePctFunc?.invoke(region)?.bind { input.votePct = it }
            votePctDiffFunc?.invoke(region)?.bind { input.votePctDiff = it }
            party.getBinding().bind { input.party = it }
            val seatBinding = Binding.propertyBinding<SinglePartyInput, List<String>, SinglePartyInput.Property>(
                input,
                { i: SinglePartyInput ->
                    val seats = i.seats[i.party] ?: 0
                    val diff = i.seatDiff[i.party] ?: 0
                    listOf(
                        seats.toString(),
                        if (diff == 0) "\u00b10" else DecimalFormat("+0;-0").format(diff)
                    )
                },
                SinglePartyInput.Property.SEATS,
                SinglePartyInput.Property.SEAT_DIFF,
                SinglePartyInput.Property.PARTY
            )
            val voteBinding = Binding.propertyBinding<SinglePartyInput, List<String>, SinglePartyInput.Property>(
                input,
                { i: SinglePartyInput ->
                    val vote = i.votePct[i.party] ?: 0.0
                    val diff = i.votePctDiff[i.party] ?: 0.0
                    listOf(
                        DecimalFormat("0.0%").format(vote),
                        if (diff == 0.0) "\u00b10.0%" else DecimalFormat("+0.0%;-0.0%").format(diff)
                    )
                },
                SinglePartyInput.Property.VOTE_PCT,
                SinglePartyInput.Property.VOTE_PCT_DIFF,
                SinglePartyInput.Property.PARTY
            )
            val (headers, values) = when {
                seatFunc == null -> listOf(voteHeader) to voteBinding.map { listOf(it) }
                votePctFunc == null -> listOf(seatsHeader) to seatBinding.map { listOf(it) }
                else -> listOf(seatsHeader, voteHeader) to seatBinding.merge(voteBinding) { s, v -> listOf(s, v) }
            }
            return RegionSummaryFrame(
                headerBinding = titleFunc(region),
                summaryColorBinding = party.getBinding { it.color },
                sectionsBinding = values.map { value ->
                    value.zip(headers) { v, h -> RegionSummaryFrame.SectionWithoutColor(h, v) }
                }
            )
        }
    }

    private class SinglePartyInput : Bindable<SinglePartyInput, SinglePartyInput.Property>() {
        enum class Property {
            SEATS, SEAT_DIFF, VOTE_PCT, VOTE_PCT_DIFF, PARTY
        }

        private var _seats: Map<Party, Int> = emptyMap()
        private var _seatDiff: Map<Party, Int> = emptyMap()
        private var _votePct: Map<Party, Double> = emptyMap()
        private var _votePctDiff: Map<Party, Double> = emptyMap()
        private var _party: Party? = null

        var seats: Map<Party, Int>
        get() = _seats
        set(seats) {
            this._seats = seats
            onPropertyRefreshed(Property.SEATS)
        }

        var seatDiff: Map<Party, Int>
        get() = _seatDiff
        set(seatDiff) {
            this._seatDiff = seatDiff
            onPropertyRefreshed(Property.SEAT_DIFF)
        }

        var votePct: Map<Party, Double>
        get() = _votePct
        set(votePct) {
            this._votePct = votePct
            onPropertyRefreshed(Property.VOTE_PCT)
        }

        var votePctDiff: Map<Party, Double>
        get() = _votePctDiff
        set(votePctDiff) {
            this._votePctDiff = votePctDiff
            onPropertyRefreshed(Property.VOTE_PCT_DIFF)
        }

        var party: Party?
        get() = _party
        set(party) {
            this._party = party
            onPropertyRefreshed(Property.PARTY)
        }
    }

    companion object {
        @JvmStatic fun <T> of(
            mainRegion: T,
            titleFunc: (T) -> Binding<String>,
            numRows: Int
        ): Builder<T> {
            return Builder(mainRegion, titleFunc, numRows)
        }

        @JvmStatic fun <T> ofDiff(
            mainRegion: T,
            titleFunc: (T) -> Binding<String>,
            seatFunc: (T) -> Binding<Map<Party, Int>>,
            seatDiffFunc: (T) -> Binding<Map<Party, Int>>,
            votePctFunc: (T) -> Binding<Map<Party, Double>>,
            votePctDiffFunc: (T) -> Binding<Map<Party, Double>>,
            numRows: Int
        ): Builder<T> {
            return Builder(mainRegion, titleFunc, numRows)
                .withSeatAndDiff(seatFunc, seatDiffFunc)
                .withVotePctAndDiff(votePctFunc, votePctDiffFunc)
        }

        @JvmStatic fun <T> ofPrev(
            mainRegion: T,
            titleFunc: (T) -> Binding<String>,
            seatFunc: (T) -> Binding<Map<Party, Int>>,
            seatPrevFunc: (T) -> Binding<Map<Party, Int>>,
            votePctFunc: (T) -> Binding<Map<Party, Double>>,
            votePctPrevFunc: (T) -> Binding<Map<Party, Double>>,
            numRows: Int
        ): Builder<T> {
            return Builder(mainRegion, titleFunc, numRows)
                .withSeatAndPrev(seatFunc, seatPrevFunc)
                .withVotePctAndPrev(votePctFunc, votePctPrevFunc)
        }
    }

    init {
        background = Color.WHITE
        layout = BorderLayout()
        add(headerLabel, BorderLayout.NORTH)
        val center = JPanel()
        center.background = Color.WHITE
        center.layout = Layout(numRows)
        add(center, BorderLayout.CENTER)
        center.add(mainFrame, "main")
        otherFrames.forEach { center.add(it, "other") }
    }
}
