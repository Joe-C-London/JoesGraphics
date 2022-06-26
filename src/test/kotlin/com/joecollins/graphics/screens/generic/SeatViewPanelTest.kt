package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrameTest
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateDualSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateRangeSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyDualSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyDualSeatsReversed
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyRangeSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partySeats
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader.readShapes
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.Test
import java.awt.Color
import java.awt.Shape
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.IdentityHashMap
import java.util.LinkedHashMap
import kotlin.Throws

class SeatViewPanelTest {
    @Test
    @Throws(IOException::class)
    fun testBasicCurrPrev() {
        val currentSeats = Publisher(LinkedHashMap<Party, Int>())
        val previousSeats = Publisher(LinkedHashMap<Party, Int>())
        val totalSeats = Publisher(650)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED KINGDOM")
        val seatHeader = Publisher("0 OF 650 CONSTITUENCIES DECLARED")
        val seatSubhead = Publisher("PROJECTION: TOO EARLY TO CALL")
        val changeHeader = Publisher("CHANGE SINCE 2017")
        val changeSubhead = Publisher<String?>("CON NEED +9 FOR MAJORITY")
        val con = Party("Conservative", "CON", Color.BLUE)
        val lab = Party("Labour", "LAB", Color.RED)
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .withPrev(previousSeats, changeHeader, changeSubhead)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Basic-1", panel)

        val curr = LinkedHashMap<Party, Int>()
        val prev = LinkedHashMap<Party, Int>()
        curr[con] = 1
        currentSeats.submit(curr)
        prev[lab] = 1
        previousSeats.submit(prev)
        seatHeader.submit("1 OF 650 SEATS DECLARED")
        compareRendering("SeatViewPanel", "Basic-2", panel)

        curr[lab] = 2
        currentSeats.submit(curr)
        prev[lab] = 3
        previousSeats.submit(prev)
        seatHeader.submit("3 OF 650 SEATS DECLARED")
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
        currentSeats.submit(curr)
        previousSeats.submit(prev)
        seatHeader.submit("650 OF 650 SEATS DECLARED")
        seatSubhead.submit("PROJECTION: CON MAJORITY")
        compareRendering("SeatViewPanel", "Basic-4", panel)
        header.submit("SCOTLAND")
        seatHeader.submit("59 OF 59 SEATS DECLARED")
        seatSubhead.submit("")
        changeSubhead.submit(null)
        totalSeats.submit(59)
        showMajority.submit(false)
        curr.clear()
        prev.clear()
        curr[snp] = 48
        curr[con] = 6
        curr[ld] = 4
        curr[lab] = 1
        currentSeats.submit(curr)
        prev[snp] = 35
        prev[con] = 13
        prev[ld] = 4
        prev[lab] = 7
        previousSeats.submit(prev)
        compareRendering("SeatViewPanel", "Basic-5", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testBasicCurrDiff() {
        val currentSeats = Publisher(LinkedHashMap<Party, Int>())
        val seatDiff = Publisher(LinkedHashMap<Party, Int>())
        val totalSeats = Publisher(650)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED KINGDOM")
        val seatHeader = Publisher("0 OF 650 CONSTITUENCIES DECLARED")
        val seatSubhead = Publisher("PROJECTION: TOO EARLY TO CALL")
        val changeHeader = Publisher("CHANGE SINCE 2017")
        val changeSubhead = Publisher<String?>("CON NEED +9 FOR MAJORITY")
        val con = Party("Conservative", "CON", Color.BLUE)
        val lab = Party("Labour", "LAB", Color.RED)
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .withDiff(seatDiff, changeHeader, changeSubhead)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Basic-1", panel)

        val curr = LinkedHashMap<Party, Int>()
        val diff = LinkedHashMap<Party, Int>()
        curr[con] = 1
        currentSeats.submit(curr)
        diff[con] = +1
        diff[lab] = -1
        seatDiff.submit(diff)
        seatHeader.submit("1 OF 650 SEATS DECLARED")
        compareRendering("SeatViewPanel", "Basic-2", panel)

        curr[lab] = 2
        currentSeats.submit(curr)
        seatHeader.submit("3 OF 650 SEATS DECLARED")
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
        currentSeats.submit(curr)
        seatDiff.submit(diff)
        seatHeader.submit("650 OF 650 SEATS DECLARED")
        seatSubhead.submit("PROJECTION: CON MAJORITY")
        compareRendering("SeatViewPanel", "Basic-4", panel)
        header.submit("SCOTLAND")
        seatHeader.submit("59 OF 59 SEATS DECLARED")
        seatSubhead.submit("")
        changeSubhead.submit(null)
        totalSeats.submit(59)
        showMajority.submit(false)
        curr.clear()
        diff.clear()
        curr[snp] = 48
        curr[con] = 6
        curr[ld] = 4
        curr[lab] = 1
        currentSeats.submit(curr)
        diff[snp] = +13
        diff[con] = -7
        diff[ld] = 0
        diff[lab] = -6
        seatDiff.submit(diff)
        compareRendering("SeatViewPanel", "Basic-5", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testSwing() {
        val currentSeats = Publisher(LinkedHashMap<Party, Int>())
        val previousSeats = Publisher(LinkedHashMap<Party, Int>())
        val currentVotes = Publisher(LinkedHashMap<Party, Int>())
        val previousVotes = Publisher(LinkedHashMap<Party, Int>())
        val totalSeats = Publisher(650)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED KINGDOM")
        val seatHeader = Publisher("0 OF 650 CONSTITUENCIES DECLARED")
        val seatSubhead = Publisher("PROJECTION: TOO EARLY TO CALL")
        val changeHeader = Publisher("CHANGE SINCE 2017")
        val swingHeader = Publisher("SWING SINCE 2017")
        val con = Party("Conservative", "CON", Color.BLUE)
        val lab = Party("Labour", "LAB", Color.RED)
        val ld = Party("Liberal Democrat", "LD", Color.ORANGE)
        val snp = Party("Scottish National Party", "SNP", Color.YELLOW)
        val pc = Party("Plaid Cymru", "PC", Color.GREEN.darker())
        val grn = Party("Green", "GRN", Color.GREEN)
        val oth = Party.OTHERS
        val partyOrder = listOf(snp, lab, pc, grn, ld, oth, con)
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .withSwing(currentVotes, previousVotes, compareBy { partyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Swing-1", panel)

        val currSeats = LinkedHashMap<Party, Int>()
        val prevSeats = LinkedHashMap<Party, Int>()
        val currVotes = LinkedHashMap<Party, Int>()
        val prevVotes = LinkedHashMap<Party, Int>()
        currSeats[lab] = 1
        currentSeats.submit(currSeats)
        prevSeats[lab] = 1
        previousSeats.submit(prevSeats)
        currVotes[lab] = 21568
        currVotes[con] = 9290
        currVotes[ld] = 2709
        currVotes[grn] = 1365
        currVotes[oth] = 2542
        currentVotes.submit(currVotes)
        prevVotes[lab] = 24071
        prevVotes[con] = 9134
        prevVotes[ld] = 1812
        prevVotes[grn] = 595
        prevVotes[oth] = 1482
        previousVotes.submit(prevVotes)
        seatHeader.submit("1 OF 650 SEATS DECLARED")
        compareRendering("SeatViewPanel", "Swing-2", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testMap() {
        val currentSeats = Publisher(LinkedHashMap<Party, Int>())
        val previousSeats = Publisher(LinkedHashMap<Party, Int>())
        val currentVotes = Publisher(LinkedHashMap<Party, Int>())
        val previousVotes = Publisher(LinkedHashMap<Party, Int>())
        val totalSeats = Publisher(27)
        val showMajority = Publisher(true)
        val header = Publisher("PRINCE EDWARD ISLAND")
        val seatHeader = Publisher("0 OF 27 DISTRICTS DECLARED")
        val seatSubhead = Publisher("PROJECTION: TOO EARLY TO CALL")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("PEI")
        val winnersByDistrict = Publisher<Map<Int, Party?>>(HashMap())
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher<List<Int>?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val oth = Party.OTHERS
        val partyOrder = listOf(ndp, grn, lib, oth, pc)
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .withSwing(currentVotes, previousVotes, compareBy { partyOrder.indexOf(it) }, swingHeader)
            .withPartyMap(shapesByDistrict.asOneTimePublisher(), winnersByDistrict, focus, mapHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Map-1", panel)

        val currSeats = LinkedHashMap<Party, Int>()
        val prevSeats = LinkedHashMap<Party, Int>()
        val currVotes = LinkedHashMap<Party, Int>()
        val prevVotes = LinkedHashMap<Party, Int>()
        val winners = LinkedHashMap<Int, Party?>()
        currSeats[pc] = 1
        currentSeats.submit(currSeats)
        prevSeats[pc] = 1
        previousSeats.submit(prevSeats)
        currVotes[pc] = 1347
        currVotes[lib] = 861
        currVotes[grn] = 804
        currentVotes.submit(currVotes)
        prevVotes[pc] = 1179
        prevVotes[lib] = 951
        prevVotes[ndp] = 528
        previousVotes.submit(prevVotes)
        winners[1] = pc
        winnersByDistrict.submit(winners)
        seatHeader.submit("1 OF 27 DISTRICTS DECLARED")
        compareRendering("SeatViewPanel", "Map-2", panel)

        focus.submit(shapesByDistrict.keys.filter { it <= 7 })
        header.submit("CARDIGAN")
        seatHeader.submit("1 OF 7 DISTRICTS DECLARED")
        seatSubhead.submit("")
        totalSeats.submit(7)
        showMajority.submit(false)
        mapHeader.submit("CARDIGAN")
        compareRendering("SeatViewPanel", "Map-3", panel)

        winners[8] = pc
        winnersByDistrict.submit(winners)
        compareRendering("SeatViewPanel", "Map-3", panel)

        currSeats[pc] = 2
        currentSeats.submit(currSeats)
        prevSeats[lib] = 1
        previousSeats.submit(prevSeats)
        currVotes[pc] = 2720
        currVotes[lib] = 1646
        currVotes[grn] = 1478
        currVotes[ndp] = 124
        currentVotes.submit(currVotes)
        prevVotes[pc] = 1964
        prevVotes[lib] = 2011
        prevVotes[ndp] = 1113
        prevVotes[grn] = 106
        previousVotes.submit(prevVotes)
        winners[3] = pc
        winnersByDistrict.submit(winners)
        seatHeader.submit("2 OF 7 DISTRICTS DECLARED")
        compareRendering("SeatViewPanel", "Map-4", panel)

        focus.submit(null)
        header.submit("PRINCE EDWARD ISLAND")
        seatHeader.submit("3 OF 27 DISTRICTS DECLARED")
        seatSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        currSeats[pc] = 3
        currentSeats.submit(currSeats)
        prevSeats[lib] = 2
        previousSeats.submit(prevSeats)
        totalSeats.submit(27)
        showMajority.submit(true)
        mapHeader.submit("PEI")
        compareRendering("SeatViewPanel", "Map-5", panel)

        (1..27).forEach { winners.putIfAbsent(it, null) }
        winnersByDistrict.submit(winners)
        compareRendering("SeatViewPanel", "Map-5", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testDualCurrPrev() {
        val currentSeats = Publisher(LinkedHashMap<Party, Pair<Int, Int>>())
        val previousSeats = Publisher(LinkedHashMap<Party, Pair<Int, Int>>())
        val totalSeats = Publisher(338)
        val showMajority = Publisher(true)
        val header = Publisher("CANADA")
        val seatHeader = Publisher("0 OF 338 RIDINGS REPORTING")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val bq = Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Color.GRAY)
        val panel = partyDualSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Dual-1", panel)

        val currSeats = LinkedHashMap<Party, Pair<Int, Int>>()
        val prevSeats = LinkedHashMap<Party, Pair<Int, Int>>()
        currSeats[lib] = Pair(0, 6)
        currSeats[ndp] = Pair(0, 1)
        currentSeats.submit(currSeats)
        prevSeats[lib] = Pair(0, 7)
        previousSeats.submit(prevSeats)
        seatHeader.submit("7 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-2", panel)

        currSeats[lib] = Pair(6, 26)
        currSeats[ndp] = Pair(1, 1)
        currSeats[con] = Pair(0, 4)
        currSeats[grn] = Pair(0, 1)
        currentSeats.submit(currSeats)
        prevSeats[lib] = Pair(7, 32)
        previousSeats.submit(prevSeats)
        seatHeader.submit("32 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-3", panel)

        currSeats[lib] = Pair(26, 145)
        currSeats[ndp] = Pair(1, 13)
        currSeats[con] = Pair(4, 104)
        currSeats[bq] = Pair(0, 32)
        currSeats[grn] = Pair(1, 1)
        currentSeats.submit(currSeats)
        prevSeats[lib] = Pair(32, 166)
        prevSeats[ndp] = Pair(0, 30)
        prevSeats[con] = Pair(0, 89)
        prevSeats[bq] = Pair(0, 10)
        previousSeats.submit(prevSeats)
        seatHeader.submit("295 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-4", panel)

        currSeats[lib] = Pair(145, 157)
        currSeats[ndp] = Pair(13, 24)
        currSeats[con] = Pair(104, 121)
        currSeats[bq] = Pair(32, 32)
        currSeats[grn] = Pair(1, 3)
        currSeats[ind] = Pair(0, 1)
        currentSeats.submit(currSeats)
        prevSeats[lib] = Pair(166, 184)
        prevSeats[ndp] = Pair(30, 44)
        prevSeats[con] = Pair(89, 99)
        prevSeats[bq] = Pair(10, 10)
        prevSeats[grn] = Pair(0, 1)
        previousSeats.submit(prevSeats)
        seatHeader.submit("338 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-5", panel)

        currSeats[lib] = Pair(157, 157)
        currSeats[ndp] = Pair(24, 24)
        currSeats[con] = Pair(121, 121)
        currSeats[bq] = Pair(32, 32)
        currSeats[grn] = Pair(3, 3)
        currSeats[ind] = Pair(1, 1)
        currentSeats.submit(currSeats)
        prevSeats[lib] = Pair(184, 184)
        prevSeats[ndp] = Pair(44, 44)
        prevSeats[con] = Pair(99, 99)
        prevSeats[bq] = Pair(10, 10)
        prevSeats[grn] = Pair(1, 1)
        previousSeats.submit(prevSeats)
        seatHeader.submit("338 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-6", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testDualReversedCurrPrev() {
        val currentSeats = Publisher(LinkedHashMap<Party, Pair<Int, Int>>())
        val previousSeats = Publisher(LinkedHashMap<Party, Pair<Int, Int>>())
        val totalSeats = Publisher(338)
        val showMajority = Publisher(true)
        val header = Publisher("CANADA")
        val seatHeader = Publisher("0 OF 338 RIDINGS REPORTING")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val bq = Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Color.GRAY)
        val panel = partyDualSeatsReversed(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "DualReversed-1", panel)

        val currSeats = LinkedHashMap<Party, Pair<Int, Int>>()
        val prevSeats = LinkedHashMap<Party, Pair<Int, Int>>()
        currSeats[lib] = Pair(0, 6)
        currSeats[ndp] = Pair(0, 1)
        currentSeats.submit(currSeats)
        prevSeats[lib] = Pair(0, 7)
        previousSeats.submit(prevSeats)
        seatHeader.submit("7 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "DualReversed-2", panel)

        currSeats[lib] = Pair(6, 26)
        currSeats[ndp] = Pair(1, 1)
        currSeats[con] = Pair(0, 4)
        currSeats[grn] = Pair(0, 1)
        currentSeats.submit(currSeats)
        prevSeats[lib] = Pair(7, 32)
        previousSeats.submit(prevSeats)
        seatHeader.submit("32 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "DualReversed-3", panel)

        currSeats[lib] = Pair(26, 145)
        currSeats[ndp] = Pair(1, 13)
        currSeats[con] = Pair(4, 104)
        currSeats[bq] = Pair(0, 32)
        currSeats[grn] = Pair(1, 1)
        currentSeats.submit(currSeats)
        prevSeats[lib] = Pair(32, 166)
        prevSeats[ndp] = Pair(0, 30)
        prevSeats[con] = Pair(0, 89)
        prevSeats[bq] = Pair(0, 10)
        previousSeats.submit(prevSeats)
        seatHeader.submit("295 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "DualReversed-4", panel)

        currSeats[lib] = Pair(145, 157)
        currSeats[ndp] = Pair(13, 24)
        currSeats[con] = Pair(104, 121)
        currSeats[bq] = Pair(32, 32)
        currSeats[grn] = Pair(1, 3)
        currSeats[ind] = Pair(0, 1)
        currentSeats.submit(currSeats)
        prevSeats[lib] = Pair(166, 184)
        prevSeats[ndp] = Pair(30, 44)
        prevSeats[con] = Pair(89, 99)
        prevSeats[bq] = Pair(10, 10)
        prevSeats[grn] = Pair(0, 1)
        previousSeats.submit(prevSeats)
        seatHeader.submit("338 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "DualReversed-5", panel)

        currSeats[lib] = Pair(157, 157)
        currSeats[ndp] = Pair(24, 24)
        currSeats[con] = Pair(121, 121)
        currSeats[bq] = Pair(32, 32)
        currSeats[grn] = Pair(3, 3)
        currSeats[ind] = Pair(1, 1)
        currentSeats.submit(currSeats)
        prevSeats[lib] = Pair(184, 184)
        prevSeats[ndp] = Pair(44, 44)
        prevSeats[con] = Pair(99, 99)
        prevSeats[bq] = Pair(10, 10)
        prevSeats[grn] = Pair(1, 1)
        previousSeats.submit(prevSeats)
        seatHeader.submit("338 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "DualReversed-6", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testDualCurrDiff() {
        val currentSeats = Publisher(LinkedHashMap<Party, Pair<Int, Int>>())
        val seatDiff = Publisher(LinkedHashMap<Party, Pair<Int, Int>>())
        val totalSeats = Publisher(338)
        val showMajority = Publisher(true)
        val header = Publisher("CANADA")
        val seatHeader = Publisher("0 OF 338 RIDINGS REPORTING")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val bq = Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Color.GRAY)
        val panel = partyDualSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withDiff(seatDiff, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Dual-1", panel)

        val currSeats = LinkedHashMap<Party, Pair<Int, Int>>()
        val diff = LinkedHashMap<Party, Pair<Int, Int>>()
        currSeats[lib] = Pair(0, 6)
        currSeats[ndp] = Pair(0, 1)
        currentSeats.submit(currSeats)
        diff[lib] = Pair(0, -1)
        diff[ndp] = Pair(0, +1)
        seatDiff.submit(diff)
        seatHeader.submit("7 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-2", panel)

        currSeats[lib] = Pair(6, 26)
        currSeats[ndp] = Pair(1, 1)
        currSeats[con] = Pair(0, 4)
        currSeats[grn] = Pair(0, 1)
        currentSeats.submit(currSeats)
        diff[lib] = Pair(-1, -6)
        diff[ndp] = Pair(+1, +1)
        diff[con] = Pair(0, +4)
        diff[grn] = Pair(0, +1)
        seatDiff.submit(diff)
        seatHeader.submit("32 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-3", panel)

        currSeats[lib] = Pair(26, 145)
        currSeats[ndp] = Pair(1, 13)
        currSeats[con] = Pair(4, 104)
        currSeats[bq] = Pair(0, 32)
        currSeats[grn] = Pair(1, 1)
        currentSeats.submit(currSeats)
        diff[lib] = Pair(-6, -21)
        diff[ndp] = Pair(+1, -17)
        diff[con] = Pair(+4, +15)
        diff[bq] = Pair(0, +22)
        diff[grn] = Pair(+1, +1)
        seatDiff.submit(diff)
        seatHeader.submit("295 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-4", panel)

        currSeats[lib] = Pair(145, 157)
        currSeats[ndp] = Pair(13, 24)
        currSeats[con] = Pair(104, 121)
        currSeats[bq] = Pair(32, 32)
        currSeats[grn] = Pair(1, 3)
        currSeats[ind] = Pair(0, 1)
        currentSeats.submit(currSeats)
        diff[lib] = Pair(-21, -27)
        diff[ndp] = Pair(-17, -20)
        diff[con] = Pair(+15, +22)
        diff[bq] = Pair(+22, +22)
        diff[grn] = Pair(+1, +2)
        diff[ind] = Pair(0, +1)
        seatDiff.submit(diff)
        seatHeader.submit("338 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-5", panel)

        currSeats[lib] = Pair(157, 157)
        currSeats[ndp] = Pair(24, 24)
        currSeats[con] = Pair(121, 121)
        currSeats[bq] = Pair(32, 32)
        currSeats[grn] = Pair(3, 3)
        currSeats[ind] = Pair(1, 1)
        currentSeats.submit(currSeats)
        diff[lib] = Pair(-27, -27)
        diff[ndp] = Pair(-20, -20)
        diff[con] = Pair(+22, +22)
        diff[bq] = Pair(+22, +22)
        diff[grn] = Pair(+2, +2)
        diff[ind] = Pair(+1, +1)
        seatDiff.submit(diff)
        seatHeader.submit("338 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-6", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testRangeCurrPrev() {
        val currentSeats = Publisher(LinkedHashMap<Party, IntRange>())
        val previousSeats = Publisher(LinkedHashMap<Party, Int>())
        val totalSeats = Publisher(76)
        val showMajority = Publisher(true)
        val header = Publisher("AUSTRALIA")
        val seatHeader = Publisher("SENATE SEATS")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2013")
        val lnp = Party("Liberal/National Coalition", "L/NP", Color.BLUE)
        val alp = Party("Labor Party", "ALP", Color.RED)
        val grn = Party("The Greens", "GRN", Color.GREEN.darker())
        val onp = Party("One Nation Party", "ONP", Color.ORANGE)
        val nxt = Party("Nick Xenophon Team", "NXT", Color.ORANGE)
        val oth = Party.OTHERS
        val panel = partyRangeSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Range-1", panel)

        val currSeats = LinkedHashMap<Party, IntRange>()
        val prevSeats = LinkedHashMap<Party, Int>()
        currSeats[lnp] = IntRange(4, 5)
        currSeats[alp] = IntRange(4, 4)
        currSeats[grn] = IntRange(0, 1)
        currSeats[onp] = IntRange(0, 1)
        currSeats[oth] = IntRange(0, 2)
        currentSeats.submit(currSeats)
        prevSeats[lnp] = 6
        prevSeats[alp] = 4
        prevSeats[grn] = 1
        prevSeats[oth] = 1
        previousSeats.submit(prevSeats)
        compareRendering("SeatViewPanel", "Range-2", panel)

        currSeats[lnp] = IntRange(8, 10)
        currSeats[alp] = IntRange(7, 8)
        currSeats[grn] = IntRange(0, 2)
        currSeats[onp] = IntRange(1, 2)
        currSeats[nxt] = IntRange(0, 1)
        currSeats[oth] = IntRange(0, 4)
        currentSeats.submit(currSeats)
        prevSeats[lnp] = 12
        prevSeats[alp] = 8
        prevSeats[grn] = 2
        prevSeats[oth] = 2
        previousSeats.submit(prevSeats)
        compareRendering("SeatViewPanel", "Range-3", panel)

        currSeats[lnp] = IntRange(27, 31)
        currSeats[alp] = IntRange(25, 27)
        currSeats[grn] = IntRange(5, 9)
        currSeats[onp] = IntRange(1, 4)
        currSeats[nxt] = IntRange(3, 3)
        currSeats[oth] = IntRange(1, 8)
        currentSeats.submit(currSeats)
        prevSeats[lnp] = 33
        prevSeats[alp] = 25
        prevSeats[grn] = 10
        prevSeats[nxt] = 1
        prevSeats[oth] = 7
        previousSeats.submit(prevSeats)
        compareRendering("SeatViewPanel", "Range-4", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testRangeCurrDiff() {
        val currentSeats = Publisher(LinkedHashMap<Party, IntRange>())
        val seatDiff = Publisher(LinkedHashMap<Party, IntRange>())
        val totalSeats = Publisher(76)
        val showMajority = Publisher(true)
        val header = Publisher("AUSTRALIA")
        val seatHeader = Publisher("SENATE SEATS")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2013")
        val lnp = Party("Liberal/National Coalition", "L/NP", Color.BLUE)
        val alp = Party("Labor Party", "ALP", Color.RED)
        val grn = Party("The Greens", "GRN", Color.GREEN.darker())
        val onp = Party("One Nation Party", "ONP", Color.ORANGE)
        val nxt = Party("Nick Xenophon Team", "NXT", Color.ORANGE)
        val oth = Party.OTHERS
        val panel = partyRangeSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withDiff(seatDiff, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Range-1", panel)

        val currSeats = LinkedHashMap<Party, IntRange>()
        val diff = LinkedHashMap<Party, IntRange>()
        currSeats[lnp] = IntRange(4, 5)
        currSeats[alp] = IntRange(4, 4)
        currSeats[grn] = IntRange(0, 1)
        currSeats[onp] = IntRange(0, 1)
        currSeats[oth] = IntRange(0, 2)
        currentSeats.submit(currSeats)
        diff[lnp] = IntRange(-2, -1)
        diff[alp] = IntRange(0, 0)
        diff[grn] = IntRange(-1, 0)
        diff[onp] = IntRange(0, +1)
        diff[oth] = IntRange(-1, +1)
        seatDiff.submit(diff)
        compareRendering("SeatViewPanel", "Range-2", panel)

        currSeats[lnp] = IntRange(8, 10)
        currSeats[alp] = IntRange(7, 8)
        currSeats[grn] = IntRange(0, 2)
        currSeats[onp] = IntRange(1, 2)
        currSeats[nxt] = IntRange(0, 1)
        currSeats[oth] = IntRange(0, 4)
        currentSeats.submit(currSeats)
        diff[lnp] = IntRange(-4, -2)
        diff[alp] = IntRange(-1, 0)
        diff[grn] = IntRange(-2, 0)
        diff[onp] = IntRange(+1, +2)
        diff[nxt] = IntRange(0, +1)
        diff[oth] = IntRange(-2, +2)
        seatDiff.submit(diff)
        compareRendering("SeatViewPanel", "Range-3", panel)

        currSeats[lnp] = IntRange(27, 31)
        currSeats[alp] = IntRange(25, 27)
        currSeats[grn] = IntRange(5, 9)
        currSeats[onp] = IntRange(1, 4)
        currSeats[nxt] = IntRange(3, 3)
        currSeats[oth] = IntRange(1, 8)
        currentSeats.submit(currSeats)
        diff[lnp] = IntRange(-6, -2)
        diff[alp] = IntRange(0, +2)
        diff[grn] = IntRange(-5, -1)
        diff[onp] = IntRange(+1, +4)
        diff[nxt] = IntRange(+2, +2)
        diff[oth] = IntRange(-6, +1)
        seatDiff.submit(diff)
        compareRendering("SeatViewPanel", "Range-4", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidates() {
        val currentSeats = Publisher(LinkedHashMap<Candidate, Int>())
        val previousSeats = Publisher(LinkedHashMap<Party, Int>())
        val totalSeats = Publisher(538)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED STATES")
        val seatHeader = Publisher("PRESIDENT")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2012")
        val winner = Publisher<Candidate?>(null)
        val clinton = Candidate("Hillary Clinton", Party("Democrat", "DEM", Color.BLUE))
        val trump = Candidate("Donald Trump", Party("Republican", "GOP", Color.RED))
        val panel = candidateSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withWinner(winner)
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it ELECTORAL VOTES TO WIN" }
            .build(header)
        panel.setSize(1024, 512)
        val curr = LinkedHashMap<Candidate, Int>()
        curr[clinton] = 232
        curr[trump] = 306
        currentSeats.submit(curr)
        val prev = LinkedHashMap<Party, Int>()
        prev[clinton.party] = 332
        prev[trump.party] = 206
        previousSeats.submit(prev)
        winner.submit(trump)
        compareRendering("SeatViewPanel", "Candidate-1", panel)
        winner.submit(null)
        compareRendering("SeatViewPanel", "Candidate-WinnerRemoved", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidatesDual() {
        val currentSeats = Publisher(LinkedHashMap<Candidate, Pair<Int, Int>>())
        val previousSeats = Publisher(LinkedHashMap<Party, Pair<Int, Int>>())
        val totalSeats = Publisher(538)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED STATES")
        val seatHeader = Publisher("PRESIDENT")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2012")
        val winner = Publisher<Candidate?>(null)
        val panel = candidateDualSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withWinner(winner)
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it ELECTORAL VOTES TO WIN" }
            .build(header)
        panel.setSize(1024, 512)
        val clinton = Candidate("Hillary Clinton", Party("Democrat", "DEM", Color.BLUE))
        val trump = Candidate("Donald Trump", Party("Republican", "GOP", Color.RED))
        val curr = LinkedHashMap<Candidate, Pair<Int, Int>>()
        curr[clinton] = Pair(218, 232)
        curr[trump] = Pair(276, 306)
        currentSeats.submit(curr)
        val prev = LinkedHashMap<Party, Pair<Int, Int>>()
        prev[clinton.party] = Pair(302, 332)
        prev[trump.party] = Pair(192, 206)
        previousSeats.submit(prev)
        winner.submit(trump)
        compareRendering("SeatViewPanel", "Candidate-2", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidatesRange() {
        val currentSeats = Publisher(LinkedHashMap<Candidate, IntRange>())
        val previousSeats = Publisher(LinkedHashMap<Party, Int>())
        val totalSeats = Publisher(538)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED STATES")
        val seatHeader = Publisher("PRESIDENT")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2012")
        val clinton = Candidate("Hillary Clinton", Party("Democrat", "DEM", Color.BLUE))
        val trump = Candidate("Donald Trump", Party("Republican", "GOP", Color.RED))
        val curr = LinkedHashMap<Candidate, IntRange>()
        val prev = LinkedHashMap<Party, Int>()
        curr[clinton] = IntRange(303 - 65, 303 + 65)
        curr[trump] = IntRange(235 - 65, 235 + 65)
        currentSeats.submit(curr)
        prev[clinton.party] = 332
        prev[trump.party] = 206
        previousSeats.submit(prev)
        val panel = candidateRangeSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it ELECTORAL VOTES TO WIN" }
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Candidate-3", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testPartySeatsTicked() {
        val currentSeats = Publisher(LinkedHashMap<Party, Int>())
        val previousSeats = Publisher(LinkedHashMap<Party, Int>())
        val totalSeats = Publisher(435)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED STATES")
        val seatHeader = Publisher("HOUSE OF REPRESENTATIVES")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2016")
        val winner = Publisher<Party?>(null)
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withWinner(winner)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        val dem = Party("Democrat", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)
        val curr = LinkedHashMap<Party, Int>()
        curr[dem] = 235
        curr[gop] = 200
        currentSeats.submit(curr)
        val prev = LinkedHashMap<Party, Int>()
        prev[dem] = 194
        prev[gop] = 241
        previousSeats.submit(prev)
        winner.submit(dem)
        compareRendering("SeatViewPanel", "PartyTick-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testPartySeatsTickedDual() {
        val currentSeats = Publisher(LinkedHashMap<Party, Pair<Int, Int>>())
        val previousSeats = Publisher(LinkedHashMap<Party, Pair<Int, Int>>())
        val totalSeats = Publisher(435)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED STATES")
        val seatHeader = Publisher("HOUSE OF REPRESENTATIVES")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2016")
        val winner = Publisher<Party?>(null)
        val panel = partyDualSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withWinner(winner)
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        val dem = Party("Democrat", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)
        val curr = LinkedHashMap<Party, Pair<Int, Int>>()
        curr[dem] = Pair(224, 235)
        curr[gop] = Pair(192, 200)
        currentSeats.submit(curr)
        val prev = LinkedHashMap<Party, Pair<Int, Int>>()
        prev[dem] = Pair(193, 194)
        prev[gop] = Pair(223, 241)
        previousSeats.submit(prev)
        winner.submit(dem)
        compareRendering("SeatViewPanel", "PartyTick-2", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testAdditionalHighlightMapWithNoResults() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val currentSeats = Publisher(mapOf<Party, Int>())
        val previousSeats = Publisher(mapOf<Party, Int>())
        val currentVotes = Publisher(mapOf<Party, Int>())
        val previousVotes = Publisher(mapOf<Party, Int>())
        val header = Publisher("CARDIGAN")
        val seatHeader = Publisher("0 OF 7 RIDINGS REPORTING")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val changeSubhead = Publisher(null)
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("CARDIGAN")
        val swingPartyOrder = listOf(ndp, grn, lib, pc)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it <= 7 })
        val additionalHighlight = Publisher<List<Int>>(ArrayList(shapesByDistrict.keys))
        val winnerByDistrict = Publisher(mapOf<Int, PartyResult>())
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader, changeSubhead)
            .withSwing(currentVotes, previousVotes, compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .withResultMap(shapesByDistrict.asOneTimePublisher(), winnerByDistrict, focus, additionalHighlight, mapHeader)
            .withTotal(7.asOneTimePublisher())
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "AdditionalHighlightMap-1", panel)
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
        val indU = Party("Independent", "IND", Color.GRAY)
        val indN = Party("Independent", "IND", Color.GRAY)
        val indO = Party("Independent", "IND", Color.GRAY)
        val unionists = Party("Unionists", "Unionists", Color(0xff8200))
        val nationalists = Party("Nationalists", "Nationalists", Color(0x169b62))
        val others = Party.OTHERS
        val mapping: MutableMap<Party, Party> = IdentityHashMap()
        sequenceOf(dup, uup, tuv, con, pup, ukip, indU).forEach { mapping[it] = unionists }
        sequenceOf(sf, sdlp, wp, indN).forEach { mapping[it] = nationalists }
        sequenceOf(apni, grn, pbp, lab, indO).forEach { mapping[it] = others }
        val currentSeats = Publisher(LinkedHashMap<Party, Int>())
        val previousSeats = Publisher(LinkedHashMap<Party, Int>())
        val currentVotes = Publisher(LinkedHashMap<Party, Int>())
        val previousVotes = Publisher(LinkedHashMap<Party, Int>())
        val totalSeats = Publisher(90)
        val header = Publisher("NORTHERN IRELAND")
        val seatHeader = Publisher("2017 RESULTS")
        val seatSubhead = Publisher<String?>(null)
        val changeHeader = Publisher("NOTIONAL CHANGE SINCE 2016")
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withClassification({ mapping.getOrDefault(it, others) }, "BY DESIGNATION".asOneTimePublisher())
            .withSwing(currentVotes, previousVotes, compareBy { listOf(nationalists, others, unionists).indexOf(it) }, "FIRST PREFERENCE SWING SINCE 2016".asOneTimePublisher())
            .build(header)
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
        currSeats[indU] = 1
        currentSeats.submit(currSeats)
        val prevSeats = LinkedHashMap<Party, Int>()
        prevSeats[dup] = 33
        prevSeats[sf] = 23
        prevSeats[sdlp] = 11
        prevSeats[uup] = 11
        prevSeats[apni] = 8
        prevSeats[grn] = 2
        prevSeats[tuv] = 1
        prevSeats[pbp] = 1
        previousSeats.submit(prevSeats)
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
        currVotes[indU] = 4918
        currVotes[indN] = 1639
        currVotes[indO] = 7850
        currentVotes.submit(currVotes)
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
        prevVotes[indU] = 351 + 3270
        prevVotes[indN] = 0
        prevVotes[indO] = 224 + 124 + 32 + 19380
        previousVotes.submit(prevVotes)
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
