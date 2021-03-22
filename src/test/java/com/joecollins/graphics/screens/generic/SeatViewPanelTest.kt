package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.components.MapFrameTest
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateDualSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateRangeSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyDualSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyRangeSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partySeats
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader.readShapes
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import java.awt.Color
import java.awt.Shape
import java.io.IOException
import java.util.HashMap
import java.util.IdentityHashMap
import java.util.LinkedHashMap
import kotlin.Throws
import org.junit.Test

class SeatViewPanelTest {
    @Test
    @Throws(IOException::class)
    fun testBasicCurrPrev() {
        val currentSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val previousSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val totalSeats = BindableWrapper(650)
        val showMajority = BindableWrapper(true)
        val header = BindableWrapper("UNITED KINGDOM")
        val seatHeader = BindableWrapper("0 OF 650 CONSTITUENCIES DECLARED")
        val seatSubhead = BindableWrapper("PROJECTION: TOO EARLY TO CALL")
        val changeHeader = BindableWrapper("CHANGE SINCE 2017")
        val changeSubhead = BindableWrapper<String?>("CON NEED +9 FOR MAJORITY")
        val con = Party("Conservative", "CON", Color.BLUE)
        val lab = Party("Labour", "LAB", Color.RED)
        val panel = partySeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withTotal(totalSeats.binding)
                .withMajorityLine(showMajority.binding) { "$it SEATS FOR MAJORITY" }
                .withPrev(previousSeats.binding, changeHeader.binding, changeSubhead.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Basic-1", panel)

        val curr = LinkedHashMap<Party, Int>()
        val prev = LinkedHashMap<Party, Int>()
        curr[con] = 1
        currentSeats.value = curr
        prev[lab] = 1
        previousSeats.value = prev
        seatHeader.value = "1 OF 650 SEATS DECLARED"
        compareRendering("SeatViewPanel", "Basic-2", panel)

        curr[lab] = 2
        currentSeats.value = curr
        prev[lab] = 3
        previousSeats.value = prev
        seatHeader.value = "3 OF 650 SEATS DECLARED"
        compareRendering("SeatViewPanel", "Basic-3", panel)

        val ld = Party("Liberal Democrat", "LD", Color.ORANGE)
        val snp = Party("Scottish National Party", "SNP", Color.YELLOW)
        val pc = Party("Plaid Cymru", "PC", Color.GREEN.darker())
        val grn = Party("Green", "GRN", Color.GREEN)
        val oth = Party.OTHERS
        curr[con] = 365
        curr[lab] = 202
        curr[ld] = 11
        curr[snp] = 48
        curr[grn] = 1
        curr[pc] = 4
        curr[oth] = 19
        prev[con] = 317
        prev[lab] = 262
        prev[ld] = 12
        prev[snp] = 35
        prev[grn] = 1
        prev[pc] = 4
        prev[oth] = 19
        currentSeats.value = curr
        previousSeats.value = prev
        seatHeader.value = "650 OF 650 SEATS DECLARED"
        seatSubhead.value = "PROJECTION: CON MAJORITY"
        compareRendering("SeatViewPanel", "Basic-4", panel)
        header.value = "SCOTLAND"
        seatHeader.value = "59 OF 59 SEATS DECLARED"
        seatSubhead.value = ""
        changeSubhead.value = null
        totalSeats.value = 59
        showMajority.value = false
        curr.clear()
        prev.clear()
        curr[snp] = 48
        curr[con] = 6
        curr[ld] = 4
        curr[lab] = 1
        currentSeats.value = curr
        prev[snp] = 35
        prev[con] = 13
        prev[ld] = 4
        prev[lab] = 7
        previousSeats.value = prev
        compareRendering("SeatViewPanel", "Basic-5", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testBasicCurrDiff() {
        val currentSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val seatDiff = BindableWrapper(LinkedHashMap<Party, Int>())
        val totalSeats = BindableWrapper(650)
        val showMajority = BindableWrapper(true)
        val header = BindableWrapper("UNITED KINGDOM")
        val seatHeader = BindableWrapper("0 OF 650 CONSTITUENCIES DECLARED")
        val seatSubhead = BindableWrapper("PROJECTION: TOO EARLY TO CALL")
        val changeHeader = BindableWrapper("CHANGE SINCE 2017")
        val changeSubhead = BindableWrapper<String?>("CON NEED +9 FOR MAJORITY")
        val con = Party("Conservative", "CON", Color.BLUE)
        val lab = Party("Labour", "LAB", Color.RED)
        val panel = partySeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withTotal(totalSeats.binding)
                .withMajorityLine(showMajority.binding) { "$it SEATS FOR MAJORITY" }
                .withDiff(seatDiff.binding, changeHeader.binding, changeSubhead.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Basic-1", panel)

        val curr = LinkedHashMap<Party, Int>()
        val diff = LinkedHashMap<Party, Int>()
        curr[con] = 1
        currentSeats.value = curr
        diff[con] = +1
        diff[lab] = -1
        seatDiff.value = diff
        seatHeader.value = "1 OF 650 SEATS DECLARED"
        compareRendering("SeatViewPanel", "Basic-2", panel)

        curr[lab] = 2
        currentSeats.value = curr
        seatHeader.value = "3 OF 650 SEATS DECLARED"
        compareRendering("SeatViewPanel", "Basic-3", panel)

        val ld = Party("Liberal Democrat", "LD", Color.ORANGE)
        val snp = Party("Scottish National Party", "SNP", Color.YELLOW)
        val pc = Party("Plaid Cymru", "PC", Color.GREEN.darker())
        val grn = Party("Green", "GRN", Color.GREEN)
        val oth = Party.OTHERS
        curr[con] = 365
        curr[lab] = 202
        curr[ld] = 11
        curr[snp] = 48
        curr[grn] = 1
        curr[pc] = 4
        curr[oth] = 19
        diff[con] = +48
        diff[lab] = -60
        diff[ld] = -1
        diff[snp] = +13
        diff[grn] = 0
        diff[pc] = 0
        diff[oth] = 0
        currentSeats.value = curr
        seatDiff.value = diff
        seatHeader.value = "650 OF 650 SEATS DECLARED"
        seatSubhead.value = "PROJECTION: CON MAJORITY"
        compareRendering("SeatViewPanel", "Basic-4", panel)
        header.value = "SCOTLAND"
        seatHeader.value = "59 OF 59 SEATS DECLARED"
        seatSubhead.value = ""
        changeSubhead.value = null
        totalSeats.value = 59
        showMajority.value = false
        curr.clear()
        diff.clear()
        curr[snp] = 48
        curr[con] = 6
        curr[ld] = 4
        curr[lab] = 1
        currentSeats.value = curr
        diff[snp] = +13
        diff[con] = -7
        diff[ld] = 0
        diff[lab] = -6
        seatDiff.value = diff
        compareRendering("SeatViewPanel", "Basic-5", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testSwing() {
        val currentSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val previousSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val currentVotes = BindableWrapper(LinkedHashMap<Party, Int>())
        val previousVotes = BindableWrapper(LinkedHashMap<Party, Int>())
        val totalSeats = BindableWrapper(650)
        val showMajority = BindableWrapper(true)
        val header = BindableWrapper("UNITED KINGDOM")
        val seatHeader = BindableWrapper("0 OF 650 CONSTITUENCIES DECLARED")
        val seatSubhead = BindableWrapper("PROJECTION: TOO EARLY TO CALL")
        val changeHeader = BindableWrapper("CHANGE SINCE 2017")
        val swingHeader = BindableWrapper("SWING SINCE 2017")
        val con = Party("Conservative", "CON", Color.BLUE)
        val lab = Party("Labour", "LAB", Color.RED)
        val ld = Party("Liberal Democrat", "LD", Color.ORANGE)
        val snp = Party("Scottish National Party", "SNP", Color.YELLOW)
        val pc = Party("Plaid Cymru", "PC", Color.GREEN.darker())
        val grn = Party("Green", "GRN", Color.GREEN)
        val oth = Party.OTHERS
        val partyOrder = listOf(snp, lab, pc, grn, ld, oth, con)
        val panel = partySeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withPrev(previousSeats.binding, changeHeader.binding)
                .withTotal(totalSeats.binding)
                .withMajorityLine(showMajority.binding) { "$it SEATS FOR MAJORITY" }
                .withSwing(currentVotes.binding, previousVotes.binding, compareBy { partyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Swing-1", panel)

        val currSeats = LinkedHashMap<Party, Int>()
        val prevSeats = LinkedHashMap<Party, Int>()
        val currVotes = LinkedHashMap<Party, Int>()
        val prevVotes = LinkedHashMap<Party, Int>()
        currSeats[lab] = 1
        currentSeats.value = currSeats
        prevSeats[lab] = 1
        previousSeats.value = prevSeats
        currVotes[lab] = 21568
        currVotes[con] = 9290
        currVotes[ld] = 2709
        currVotes[grn] = 1365
        currVotes[oth] = 2542
        currentVotes.value = currVotes
        prevVotes[lab] = 24071
        prevVotes[con] = 9134
        prevVotes[ld] = 1812
        prevVotes[grn] = 595
        prevVotes[oth] = 1482
        previousVotes.value = prevVotes
        seatHeader.value = "1 OF 650 SEATS DECLARED"
        compareRendering("SeatViewPanel", "Swing-2", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testMap() {
        val currentSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val previousSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val currentVotes = BindableWrapper(LinkedHashMap<Party, Int>())
        val previousVotes = BindableWrapper(LinkedHashMap<Party, Int>())
        val totalSeats = BindableWrapper(27)
        val showMajority = BindableWrapper(true)
        val header = BindableWrapper("PRINCE EDWARD ISLAND")
        val seatHeader = BindableWrapper("0 OF 27 DISTRICTS DECLARED")
        val seatSubhead = BindableWrapper("PROJECTION: TOO EARLY TO CALL")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val swingHeader = BindableWrapper("SWING SINCE 2015")
        val mapHeader = BindableWrapper("PEI")
        val winnersByDistrict = BindableWrapper<Map<Int, Party?>>(HashMap())
        val shapesByDistrict = peiShapesByDistrict()
        val focus = BindableWrapper<List<Int>?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val oth = Party.OTHERS
        val partyOrder = listOf(ndp, grn, lib, oth, pc)
        val panel = partySeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withPrev(previousSeats.binding, changeHeader.binding)
                .withTotal(totalSeats.binding)
                .withMajorityLine(showMajority.binding) { "$it SEATS FOR MAJORITY" }
                .withSwing(currentVotes.binding, previousVotes.binding, compareBy { partyOrder.indexOf(it) }, swingHeader.binding)
                .withPartyMap(fixedBinding(shapesByDistrict), winnersByDistrict.binding, focus.binding, mapHeader.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Map-1", panel)

        val currSeats = LinkedHashMap<Party, Int>()
        val prevSeats = LinkedHashMap<Party, Int>()
        val currVotes = LinkedHashMap<Party, Int>()
        val prevVotes = LinkedHashMap<Party, Int>()
        val winners = LinkedHashMap<Int, Party?>()
        currSeats[pc] = 1
        currentSeats.value = currSeats
        prevSeats[pc] = 1
        previousSeats.value = prevSeats
        currVotes[pc] = 1347
        currVotes[lib] = 861
        currVotes[grn] = 804
        currentVotes.value = currVotes
        prevVotes[pc] = 1179
        prevVotes[lib] = 951
        prevVotes[ndp] = 528
        previousVotes.value = prevVotes
        winners[1] = pc
        winnersByDistrict.value = winners
        seatHeader.value = "1 OF 27 DISTRICTS DECLARED"
        compareRendering("SeatViewPanel", "Map-2", panel)

        focus.value = shapesByDistrict.keys.filter { it <= 7 }
        header.value = "CARDIGAN"
        seatHeader.value = "1 OF 7 DISTRICTS DECLARED"
        seatSubhead.value = ""
        totalSeats.value = 7
        showMajority.value = false
        mapHeader.value = "CARDIGAN"
        compareRendering("SeatViewPanel", "Map-3", panel)

        currSeats[pc] = 2
        currentSeats.value = currSeats
        prevSeats[lib] = 1
        previousSeats.value = prevSeats
        currVotes[pc] = 2720
        currVotes[lib] = 1646
        currVotes[grn] = 1478
        currVotes[ndp] = 124
        currentVotes.value = currVotes
        prevVotes[pc] = 1964
        prevVotes[lib] = 2011
        prevVotes[ndp] = 1113
        prevVotes[grn] = 106
        previousVotes.value = prevVotes
        winners[3] = pc
        winnersByDistrict.value = winners
        seatHeader.value = "2 OF 7 DISTRICTS DECLARED"
        compareRendering("SeatViewPanel", "Map-4", panel)
        focus.value = null
        header.value = "PRINCE EDWARD ISLAND"
        seatHeader.value = "2 OF 27 DISTRICTS DECLARED"
        seatSubhead.value = "PROJECTION: TOO EARLY TO CALL"
        totalSeats.value = 27
        showMajority.value = true
        mapHeader.value = "PEI"
        compareRendering("SeatViewPanel", "Map-5", panel)

        (1..27).forEach { winners.putIfAbsent(it, null) }
        winnersByDistrict.value = winners
        compareRendering("SeatViewPanel", "Map-5", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testDualCurrPrev() {
        val currentSeats = BindableWrapper(LinkedHashMap<Party, Pair<Int, Int>>())
        val previousSeats = BindableWrapper(LinkedHashMap<Party, Pair<Int, Int>>())
        val totalSeats = BindableWrapper(338)
        val showMajority = BindableWrapper(true)
        val header = BindableWrapper("CANADA")
        val seatHeader = BindableWrapper("0 OF 338 RIDINGS REPORTING")
        val seatSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val bq = Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Color.GRAY)
        val panel = partyDualSeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withPrev(previousSeats.binding, changeHeader.binding)
                .withTotal(totalSeats.binding)
                .withMajorityLine(showMajority.binding) { "$it SEATS FOR MAJORITY" }
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Dual-1", panel)

        val currSeats = LinkedHashMap<Party, Pair<Int, Int>>()
        val prevSeats = LinkedHashMap<Party, Pair<Int, Int>>()
        currSeats[lib] = Pair(0, 6)
        currSeats[ndp] = Pair(0, 1)
        currentSeats.value = currSeats
        prevSeats[lib] = Pair(0, 7)
        previousSeats.value = prevSeats
        seatHeader.value = "7 OF 338 RIDINGS REPORTING"
        compareRendering("SeatViewPanel", "Dual-2", panel)

        currSeats[lib] = Pair(6, 26)
        currSeats[ndp] = Pair(1, 1)
        currSeats[con] = Pair(0, 4)
        currSeats[grn] = Pair(0, 1)
        currentSeats.value = currSeats
        prevSeats[lib] = Pair(7, 32)
        previousSeats.value = prevSeats
        seatHeader.value = "32 OF 338 RIDINGS REPORTING"
        compareRendering("SeatViewPanel", "Dual-3", panel)

        currSeats[lib] = Pair(26, 145)
        currSeats[ndp] = Pair(1, 13)
        currSeats[con] = Pair(4, 104)
        currSeats[bq] = Pair(0, 32)
        currSeats[grn] = Pair(1, 1)
        currentSeats.value = currSeats
        prevSeats[lib] = Pair(32, 166)
        prevSeats[ndp] = Pair(0, 30)
        prevSeats[con] = Pair(0, 89)
        prevSeats[bq] = Pair(0, 10)
        previousSeats.value = prevSeats
        seatHeader.value = "295 OF 338 RIDINGS REPORTING"
        compareRendering("SeatViewPanel", "Dual-4", panel)

        currSeats[lib] = Pair(145, 157)
        currSeats[ndp] = Pair(13, 24)
        currSeats[con] = Pair(104, 121)
        currSeats[bq] = Pair(32, 32)
        currSeats[grn] = Pair(1, 3)
        currSeats[ind] = Pair(0, 1)
        currentSeats.value = currSeats
        prevSeats[lib] = Pair(166, 184)
        prevSeats[ndp] = Pair(30, 44)
        prevSeats[con] = Pair(89, 99)
        prevSeats[bq] = Pair(10, 10)
        prevSeats[grn] = Pair(0, 1)
        previousSeats.value = prevSeats
        seatHeader.value = "338 OF 338 RIDINGS REPORTING"
        compareRendering("SeatViewPanel", "Dual-5", panel)

        currSeats[lib] = Pair(157, 157)
        currSeats[ndp] = Pair(24, 24)
        currSeats[con] = Pair(121, 121)
        currSeats[bq] = Pair(32, 32)
        currSeats[grn] = Pair(3, 3)
        currSeats[ind] = Pair(1, 1)
        currentSeats.value = currSeats
        prevSeats[lib] = Pair(184, 184)
        prevSeats[ndp] = Pair(44, 44)
        prevSeats[con] = Pair(99, 99)
        prevSeats[bq] = Pair(10, 10)
        prevSeats[grn] = Pair(1, 1)
        previousSeats.value = prevSeats
        seatHeader.value = "338 OF 338 RIDINGS REPORTING"
        compareRendering("SeatViewPanel", "Dual-6", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testDualCurrDiff() {
        val currentSeats = BindableWrapper(LinkedHashMap<Party, Pair<Int, Int>>())
        val seatDiff = BindableWrapper(LinkedHashMap<Party, Pair<Int, Int>>())
        val totalSeats = BindableWrapper(338)
        val showMajority = BindableWrapper(true)
        val header = BindableWrapper("CANADA")
        val seatHeader = BindableWrapper("0 OF 338 RIDINGS REPORTING")
        val seatSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val bq = Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Color.GRAY)
        val panel = partyDualSeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withDiff(seatDiff.binding, changeHeader.binding)
                .withTotal(totalSeats.binding)
                .withMajorityLine(showMajority.binding) { "$it SEATS FOR MAJORITY" }
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Dual-1", panel)

        val currSeats = LinkedHashMap<Party, Pair<Int, Int>>()
        val diff = LinkedHashMap<Party, Pair<Int, Int>>()
        currSeats[lib] = Pair(0, 6)
        currSeats[ndp] = Pair(0, 1)
        currentSeats.value = currSeats
        diff[lib] = Pair(0, -1)
        diff[ndp] = Pair(0, +1)
        seatDiff.value = diff
        seatHeader.value = "7 OF 338 RIDINGS REPORTING"
        compareRendering("SeatViewPanel", "Dual-2", panel)

        currSeats[lib] = Pair(6, 26)
        currSeats[ndp] = Pair(1, 1)
        currSeats[con] = Pair(0, 4)
        currSeats[grn] = Pair(0, 1)
        currentSeats.value = currSeats
        diff[lib] = Pair(-1, -6)
        diff[ndp] = Pair(+1, +1)
        diff[con] = Pair(0, +4)
        diff[grn] = Pair(0, +1)
        seatDiff.value = diff
        seatHeader.value = "32 OF 338 RIDINGS REPORTING"
        compareRendering("SeatViewPanel", "Dual-3", panel)

        currSeats[lib] = Pair(26, 145)
        currSeats[ndp] = Pair(1, 13)
        currSeats[con] = Pair(4, 104)
        currSeats[bq] = Pair(0, 32)
        currSeats[grn] = Pair(1, 1)
        currentSeats.value = currSeats
        diff[lib] = Pair(-6, -21)
        diff[ndp] = Pair(+1, -17)
        diff[con] = Pair(+4, +15)
        diff[bq] = Pair(0, +22)
        diff[grn] = Pair(+1, +1)
        seatDiff.value = diff
        seatHeader.value = "295 OF 338 RIDINGS REPORTING"
        compareRendering("SeatViewPanel", "Dual-4", panel)

        currSeats[lib] = Pair(145, 157)
        currSeats[ndp] = Pair(13, 24)
        currSeats[con] = Pair(104, 121)
        currSeats[bq] = Pair(32, 32)
        currSeats[grn] = Pair(1, 3)
        currSeats[ind] = Pair(0, 1)
        currentSeats.value = currSeats
        diff[lib] = Pair(-21, -27)
        diff[ndp] = Pair(-17, -20)
        diff[con] = Pair(+15, +22)
        diff[bq] = Pair(+22, +22)
        diff[grn] = Pair(+1, +2)
        diff[ind] = Pair(0, +1)
        seatDiff.value = diff
        seatHeader.value = "338 OF 338 RIDINGS REPORTING"
        compareRendering("SeatViewPanel", "Dual-5", panel)

        currSeats[lib] = Pair(157, 157)
        currSeats[ndp] = Pair(24, 24)
        currSeats[con] = Pair(121, 121)
        currSeats[bq] = Pair(32, 32)
        currSeats[grn] = Pair(3, 3)
        currSeats[ind] = Pair(1, 1)
        currentSeats.value = currSeats
        diff[lib] = Pair(-27, -27)
        diff[ndp] = Pair(-20, -20)
        diff[con] = Pair(+22, +22)
        diff[bq] = Pair(+22, +22)
        diff[grn] = Pair(+2, +2)
        diff[ind] = Pair(+1, +1)
        seatDiff.value = diff
        seatHeader.value = "338 OF 338 RIDINGS REPORTING"
        compareRendering("SeatViewPanel", "Dual-6", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testRangeCurrPrev() {
        val currentSeats = BindableWrapper(LinkedHashMap<Party, IntRange>())
        val previousSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val totalSeats = BindableWrapper(76)
        val showMajority = BindableWrapper(true)
        val header = BindableWrapper("AUSTRALIA")
        val seatHeader = BindableWrapper("SENATE SEATS")
        val seatSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2013")
        val lnp = Party("Liberal/National Coalition", "L/NP", Color.BLUE)
        val alp = Party("Labor Party", "ALP", Color.RED)
        val grn = Party("The Greens", "GRN", Color.GREEN.darker())
        val onp = Party("One Nation Party", "ONP", Color.ORANGE)
        val nxt = Party("Nick Xenophon Team", "NXT", Color.ORANGE)
        val oth = Party.OTHERS
        val panel = partyRangeSeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withPrev(previousSeats.binding, changeHeader.binding)
                .withTotal(totalSeats.binding)
                .withMajorityLine(showMajority.binding) { "$it SEATS FOR MAJORITY" }
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Range-1", panel)

        val currSeats = LinkedHashMap<Party, IntRange>()
        val prevSeats = LinkedHashMap<Party, Int>()
        currSeats[lnp] = IntRange(4, 5)
        currSeats[alp] = IntRange(4, 4)
        currSeats[grn] = IntRange(0, 1)
        currSeats[onp] = IntRange(0, 1)
        currSeats[oth] = IntRange(0, 2)
        currentSeats.value = currSeats
        prevSeats[lnp] = 6
        prevSeats[alp] = 4
        prevSeats[grn] = 1
        prevSeats[oth] = 1
        previousSeats.value = prevSeats
        compareRendering("SeatViewPanel", "Range-2", panel)

        currSeats[lnp] = IntRange(8, 10)
        currSeats[alp] = IntRange(7, 8)
        currSeats[grn] = IntRange(0, 2)
        currSeats[onp] = IntRange(1, 2)
        currSeats[nxt] = IntRange(0, 1)
        currSeats[oth] = IntRange(0, 4)
        currentSeats.value = currSeats
        prevSeats[lnp] = 12
        prevSeats[alp] = 8
        prevSeats[grn] = 2
        prevSeats[oth] = 2
        previousSeats.value = prevSeats
        compareRendering("SeatViewPanel", "Range-3", panel)

        currSeats[lnp] = IntRange(27, 31)
        currSeats[alp] = IntRange(25, 27)
        currSeats[grn] = IntRange(5, 9)
        currSeats[onp] = IntRange(1, 4)
        currSeats[nxt] = IntRange(3, 3)
        currSeats[oth] = IntRange(1, 8)
        currentSeats.value = currSeats
        prevSeats[lnp] = 33
        prevSeats[alp] = 25
        prevSeats[grn] = 10
        prevSeats[nxt] = 1
        prevSeats[oth] = 7
        previousSeats.value = prevSeats
        compareRendering("SeatViewPanel", "Range-4", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testRangeCurrDiff() {
        val currentSeats = BindableWrapper(LinkedHashMap<Party, IntRange>())
        val seatDiff = BindableWrapper(LinkedHashMap<Party, IntRange>())
        val totalSeats = BindableWrapper(76)
        val showMajority = BindableWrapper(true)
        val header = BindableWrapper("AUSTRALIA")
        val seatHeader = BindableWrapper("SENATE SEATS")
        val seatSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2013")
        val lnp = Party("Liberal/National Coalition", "L/NP", Color.BLUE)
        val alp = Party("Labor Party", "ALP", Color.RED)
        val grn = Party("The Greens", "GRN", Color.GREEN.darker())
        val onp = Party("One Nation Party", "ONP", Color.ORANGE)
        val nxt = Party("Nick Xenophon Team", "NXT", Color.ORANGE)
        val oth = Party.OTHERS
        val panel = partyRangeSeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withDiff(seatDiff.binding, changeHeader.binding)
                .withTotal(totalSeats.binding)
                .withMajorityLine(showMajority.binding) { "$it SEATS FOR MAJORITY" }
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Range-1", panel)

        val currSeats = LinkedHashMap<Party, IntRange>()
        val diff = LinkedHashMap<Party, IntRange>()
        currSeats[lnp] = IntRange(4, 5)
        currSeats[alp] = IntRange(4, 4)
        currSeats[grn] = IntRange(0, 1)
        currSeats[onp] = IntRange(0, 1)
        currSeats[oth] = IntRange(0, 2)
        currentSeats.value = currSeats
        diff[lnp] = IntRange(-2, -1)
        diff[alp] = IntRange(0, 0)
        diff[grn] = IntRange(-1, 0)
        diff[onp] = IntRange(0, +1)
        diff[oth] = IntRange(-1, +1)
        seatDiff.value = diff
        compareRendering("SeatViewPanel", "Range-2", panel)

        currSeats[lnp] = IntRange(8, 10)
        currSeats[alp] = IntRange(7, 8)
        currSeats[grn] = IntRange(0, 2)
        currSeats[onp] = IntRange(1, 2)
        currSeats[nxt] = IntRange(0, 1)
        currSeats[oth] = IntRange(0, 4)
        currentSeats.value = currSeats
        diff[lnp] = IntRange(-4, -2)
        diff[alp] = IntRange(-1, 0)
        diff[grn] = IntRange(-2, 0)
        diff[onp] = IntRange(+1, +2)
        diff[nxt] = IntRange(0, +1)
        diff[oth] = IntRange(-2, +2)
        seatDiff.value = diff
        compareRendering("SeatViewPanel", "Range-3", panel)

        currSeats[lnp] = IntRange(27, 31)
        currSeats[alp] = IntRange(25, 27)
        currSeats[grn] = IntRange(5, 9)
        currSeats[onp] = IntRange(1, 4)
        currSeats[nxt] = IntRange(3, 3)
        currSeats[oth] = IntRange(1, 8)
        currentSeats.value = currSeats
        diff[lnp] = IntRange(-6, -2)
        diff[alp] = IntRange(0, +2)
        diff[grn] = IntRange(-5, -1)
        diff[onp] = IntRange(+1, +4)
        diff[nxt] = IntRange(+2, +2)
        diff[oth] = IntRange(-6, +1)
        seatDiff.value = diff
        compareRendering("SeatViewPanel", "Range-4", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidates() {
        val currentSeats = BindableWrapper(LinkedHashMap<Candidate, Int>())
        val previousSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val totalSeats = BindableWrapper(538)
        val showMajority = BindableWrapper(true)
        val header = BindableWrapper("UNITED STATES")
        val seatHeader = BindableWrapper("PRESIDENT")
        val seatSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2012")
        val winner = BindableWrapper<Candidate?>(null)
        val clinton = Candidate("Hillary Clinton", Party("Democrat", "DEM", Color.BLUE))
        val trump = Candidate("Donald Trump", Party("Republican", "GOP", Color.RED))
        val panel = candidateSeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withWinner(winner.binding)
                .withPrev(previousSeats.binding, changeHeader.binding)
                .withTotal(totalSeats.binding)
                .withMajorityLine(showMajority.binding) { "$it ELECTORAL VOTES TO WIN" }
                .build(header.binding)
        panel.setSize(1024, 512)
        val curr = LinkedHashMap<Candidate, Int>()
        curr[clinton] = 232
        curr[trump] = 306
        currentSeats.value = curr
        val prev = LinkedHashMap<Party, Int>()
        prev[clinton.party] = 332
        prev[trump.party] = 206
        previousSeats.value = prev
        winner.value = trump
        compareRendering("SeatViewPanel", "Candidate-1", panel)
        winner.value = null
        compareRendering("SeatViewPanel", "Candidate-WinnerRemoved", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidatesDual() {
        val currentSeats = BindableWrapper(LinkedHashMap<Candidate, Pair<Int, Int>>())
        val previousSeats = BindableWrapper(LinkedHashMap<Party, Pair<Int, Int>>())
        val totalSeats = BindableWrapper(538)
        val showMajority = BindableWrapper(true)
        val header = BindableWrapper("UNITED STATES")
        val seatHeader = BindableWrapper("PRESIDENT")
        val seatSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2012")
        val winner = BindableWrapper<Candidate?>(null)
        val panel = candidateDualSeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withWinner(winner.binding)
                .withPrev(previousSeats.binding, changeHeader.binding)
                .withTotal(totalSeats.binding)
                .withMajorityLine(showMajority.binding) { "$it ELECTORAL VOTES TO WIN" }
                .build(header.binding)
        panel.setSize(1024, 512)
        val clinton = Candidate("Hillary Clinton", Party("Democrat", "DEM", Color.BLUE))
        val trump = Candidate("Donald Trump", Party("Republican", "GOP", Color.RED))
        val curr = LinkedHashMap<Candidate, Pair<Int, Int>>()
        curr[clinton] = Pair(218, 232)
        curr[trump] = Pair(276, 306)
        currentSeats.value = curr
        val prev = LinkedHashMap<Party, Pair<Int, Int>>()
        prev[clinton.party] = Pair(302, 332)
        prev[trump.party] = Pair(192, 206)
        previousSeats.value = prev
        winner.value = trump
        compareRendering("SeatViewPanel", "Candidate-2", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidatesRange() {
        val currentSeats = BindableWrapper(LinkedHashMap<Candidate, IntRange>())
        val previousSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val totalSeats = BindableWrapper(538)
        val showMajority = BindableWrapper(true)
        val header = BindableWrapper("UNITED STATES")
        val seatHeader = BindableWrapper("PRESIDENT")
        val seatSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2012")
        val clinton = Candidate("Hillary Clinton", Party("Democrat", "DEM", Color.BLUE))
        val trump = Candidate("Donald Trump", Party("Republican", "GOP", Color.RED))
        val curr = LinkedHashMap<Candidate, IntRange>()
        val prev = LinkedHashMap<Party, Int>()
        curr[clinton] = IntRange(303 - 65, 303 + 65)
        curr[trump] = IntRange(235 - 65, 235 + 65)
        currentSeats.value = curr
        prev[clinton.party] = 332
        prev[trump.party] = 206
        previousSeats.value = prev
        val panel = candidateRangeSeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withPrev(previousSeats.binding, changeHeader.binding)
                .withTotal(totalSeats.binding)
                .withMajorityLine(showMajority.binding) { "$it ELECTORAL VOTES TO WIN" }
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Candidate-3", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testPartySeatsTicked() {
        val currentSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val previousSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val totalSeats = BindableWrapper(435)
        val showMajority = BindableWrapper(true)
        val header = BindableWrapper("UNITED STATES")
        val seatHeader = BindableWrapper("HOUSE OF REPRESENTATIVES")
        val seatSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2016")
        val winner = BindableWrapper<Party?>(null)
        val panel = partySeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withPrev(previousSeats.binding, changeHeader.binding)
                .withWinner(winner.binding)
                .withTotal(totalSeats.binding)
                .withMajorityLine(showMajority.binding) { "$it SEATS FOR MAJORITY" }
                .build(header.binding)
        panel.setSize(1024, 512)
        val dem = Party("Democrat", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)
        val curr = LinkedHashMap<Party, Int>()
        curr[dem] = 235
        curr[gop] = 200
        currentSeats.value = curr
        val prev = LinkedHashMap<Party, Int>()
        prev[dem] = 194
        prev[gop] = 241
        previousSeats.value = prev
        winner.value = dem
        compareRendering("SeatViewPanel", "PartyTick-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testPartySeatsTickedDual() {
        val currentSeats = BindableWrapper(LinkedHashMap<Party, Pair<Int, Int>>())
        val previousSeats = BindableWrapper(LinkedHashMap<Party, Pair<Int, Int>>())
        val totalSeats = BindableWrapper(435)
        val showMajority = BindableWrapper(true)
        val header = BindableWrapper("UNITED STATES")
        val seatHeader = BindableWrapper("HOUSE OF REPRESENTATIVES")
        val seatSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2016")
        val winner = BindableWrapper<Party?>(null)
        val panel = partyDualSeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withWinner(winner.binding)
                .withPrev(previousSeats.binding, changeHeader.binding)
                .withTotal(totalSeats.binding)
                .withMajorityLine(showMajority.binding) { "$it SEATS FOR MAJORITY" }
                .build(header.binding)
        panel.setSize(1024, 512)
        val dem = Party("Democrat", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)
        val curr = LinkedHashMap<Party, Pair<Int, Int>>()
        curr[dem] = Pair(224, 235)
        curr[gop] = Pair(192, 200)
        currentSeats.value = curr
        val prev = LinkedHashMap<Party, Pair<Int, Int>>()
        prev[dem] = Pair(193, 194)
        prev[gop] = Pair(223, 241)
        previousSeats.value = prev
        winner.value = dem
        compareRendering("SeatViewPanel", "PartyTick-2", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testPartyClassification() {
        val dup = Party("Democratic Unionist Party", "DUP", Color.ORANGE.darker())
        val sf = Party("Sinn F\u00e9in", "SF", Color.GREEN.darker().darker())
        val sdlp = Party("Social Democratic and Labour Party", "SDLP", Color.GREEN.darker())
        val uup = Party("Ulster Unionist Party", "UUP", Color.BLUE)
        val apni = Party("Alliance Party", "APNI", Color.YELLOW)
        val grn = Party("Green Party", "GRN", Color.GREEN)
        val tuv = Party("Traditional Unionist Voice", "TUV", Color.BLUE.darker())
        val pbp = Party("People Before Profit", "PBP", Color.MAGENTA)
        val pup = Party("Progressive Unionist Party", "PUP", Color.BLUE.darker())
        val con = Party("NI Conservatives", "CON", Color.BLUE)
        val lab = Party("Labour Alternative", "LAB", Color.RED)
        val ukip = Party("UK Independence Party", "UKIP", Color.MAGENTA.darker())
        val cista = Party("Cannabis is Safer than Alcohol", "CISTA", Color.GRAY)
        val wp = Party("Workers' Party", "WP", Color.RED)
        val ind_u = Party("Independent", "IND", Color.GRAY)
        val ind_n = Party("Independent", "IND", Color.GRAY)
        val ind_o = Party("Independent", "IND", Color.GRAY)
        val unionists = Party("Unionists", "Unionists", Color(0xff8200))
        val nationalists = Party("Nationalists", "Nationalists", Color(0x169b62))
        val others = Party.OTHERS
        val mapping: MutableMap<Party, Party> = IdentityHashMap()
        sequenceOf(dup, uup, tuv, con, pup, ukip, ind_u).forEach { mapping[it] = unionists }
        sequenceOf(sf, sdlp, wp, ind_n).forEach { mapping[it] = nationalists }
        sequenceOf(apni, grn, pbp, lab, ind_o).forEach { mapping[it] = others }
        val currentSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val previousSeats = BindableWrapper(LinkedHashMap<Party, Int>())
        val currentVotes = BindableWrapper(LinkedHashMap<Party, Int>())
        val previousVotes = BindableWrapper(LinkedHashMap<Party, Int>())
        val totalSeats = BindableWrapper(90)
        val header = BindableWrapper("NORTHERN IRELAND")
        val seatHeader = BindableWrapper("2017 RESULTS")
        val seatSubhead = BindableWrapper<String?>(null)
        val changeHeader = BindableWrapper("NOTIONAL CHANGE SINCE 2016")
        val panel = partySeats(
                currentSeats.binding, seatHeader.binding, seatSubhead.binding)
                .withPrev(previousSeats.binding, changeHeader.binding)
                .withTotal(totalSeats.binding)
                .withClassification({ mapping.getOrDefault(it, others) }, fixedBinding("BY DESIGNATION"))
                .withSwing(currentVotes.binding, previousVotes.binding, compareBy { listOf(nationalists, others, unionists).indexOf(it) }, fixedBinding("FIRST PREFERENCE SWING SINCE 2016"))
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "PartyClassifications-1", panel)

        val currSeats = LinkedHashMap<Party, Int>()
        currSeats[dup] = 28
        currSeats[sf] = 27
        currSeats[sdlp] = 12
        currSeats[uup] = 10
        currSeats[apni] = 8
        currSeats[grn] = 2
        currSeats[tuv] = 1
        currSeats[pbp] = 1
        currSeats[ind_u] = 1
        currentSeats.value = currSeats
        val prevSeats = LinkedHashMap<Party, Int>()
        prevSeats[dup] = 33
        prevSeats[sf] = 23
        prevSeats[sdlp] = 11
        prevSeats[uup] = 11
        prevSeats[apni] = 8
        prevSeats[grn] = 2
        prevSeats[tuv] = 1
        prevSeats[pbp] = 1
        previousSeats.value = prevSeats
        val currVotes = LinkedHashMap<Party, Int>()
        currVotes[dup] = 225413
        currVotes[sf] = 224245
        currVotes[sdlp] = 95958
        currVotes[uup] = 103314
        currVotes[apni] = 72717
        currVotes[grn] = 18527
        currVotes[tuv] = 20523
        currVotes[pbp] = 14100
        currVotes[pup] = 5590
        currVotes[con] = 2399
        currVotes[lab] = 2009
        currVotes[ukip] = 1579
        currVotes[cista] = 1273
        currVotes[wp] = 1261
        currVotes[ind_u] = 4918
        currVotes[ind_n] = 1639
        currVotes[ind_o] = 7850
        currentVotes.value = currVotes
        val prevVotes = LinkedHashMap<Party, Int>()
        prevVotes[dup] = 202567
        prevVotes[sf] = 166785
        prevVotes[uup] = 87302
        prevVotes[sdlp] = 83368
        prevVotes[apni] = 48447
        prevVotes[tuv] = 23776
        prevVotes[grn] = 18718
        prevVotes[pbp] = 13761
        prevVotes[ukip] = 10109
        prevVotes[pup] = 5955
        prevVotes[con] = 2554
        prevVotes[cista] = 2510
        prevVotes[lab] = 1939 + 1577
        prevVotes[wp] = 1565
        prevVotes[ind_u] = 351 + 3270
        prevVotes[ind_n] = 0
        prevVotes[ind_o] = 224 + 124 + 32 + 19380
        previousVotes.value = prevVotes
        compareRendering("SeatViewPanel", "PartyClassifications-2", panel)
    }

    @Throws(IOException::class)
    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = MapFrameTest::class.java
                .classLoader
                .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return readShapes(peiMap, "DIST_NO", Int::class.java)
    }
}
