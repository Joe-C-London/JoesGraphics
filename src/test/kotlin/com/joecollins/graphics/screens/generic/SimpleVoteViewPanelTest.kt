package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrameTest
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateVotes
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateVotesPctOnly
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyRangeVotes
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyVotes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader.readShapes
import com.joecollins.models.general.Aggregators.topAndOthers
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PartyResult.Companion.elected
import com.joecollins.models.general.PartyResult.Companion.leading
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.Test
import java.awt.Color
import java.awt.Shape
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.IdentityHashMap
import java.util.LinkedHashMap
import kotlin.Throws

class SimpleVoteViewPanelTest {
    @Test
    @Throws(IOException::class)
    fun testCandidatesBasicResult() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val curr = LinkedHashMap<Candidate, Int>()
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = 674
        val prev = LinkedHashMap<Party, Int>()
        prev[ndp.party] = 585
        prev[pc.party] = 785
        prev[lib.party] = 1060
        prev[grn.party] = 106
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val header = Publisher("MONTAGUE-KILMUIR")
        val voteHeader = Publisher("9 OF 9 POLLS REPORTING")
        val voteSubhead = Publisher("PROJECTION: PC GAIN FROM LIB")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("CARDIGAN")
        val leader = Publisher(pc.party)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = Publisher(3)
        val panel = candidateVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .withPartyMap(shapesByDistrict.asOneTimePublisher(), selectedDistrict, leader, focus, mapHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "Basic-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidatesBasicResultShrinkToFit() {
        val ndp = Candidate("Steven Scott", Party("New Democratic Party", "NDP", Color.ORANGE))
        val con = Candidate("Claudio Rocchi", Party("Conservative", "CON", Color.BLUE))
        val lib = Candidate("David Lametti", Party("Liberal", "LIB", Color.RED), true)
        val grn = Candidate("Jency Mercier", Party("Green", "GRN", Color.GREEN.darker()))
        val bq = Candidate(
            "Isabel Dion", Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker())
        )
        val ppc = Candidate("Daniel Turgeon", Party("People's Party", "PPC", Color.MAGENTA.darker()))
        val ml = Candidate("Eileen Studd", Party("Marxist-Leninist", "M-L", Color.RED))
        val rhino = Candidate("Rhino Jacques B\u00e9langer", Party("Rhinoceros", "RHINO", Color.GRAY))
        val ind = Candidate("Julien C\u00f4t\u00e9", Party("Independent", "IND", Color.GRAY))
        val curr = LinkedHashMap<Candidate, Int>()
        curr[ndp] = 8628
        curr[con] = 3690
        curr[lib] = 22803
        curr[grn] = 3583
        curr[bq] = 12619
        curr[ppc] = 490
        curr[ml] = 39
        curr[rhino] = 265
        curr[ind] = 274
        val prev = LinkedHashMap<Party, Int>()
        prev[ndp.party] = 15566
        prev[con.party] = 3713
        prev[lib.party] = 23603
        prev[grn.party] = 1717
        prev[bq.party] = 9164
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val header = Publisher("LASALLE\u2014\u00c9MARD\u2014VERDUN")
        val voteHeader = Publisher("100% OF POLLS REPORTING")
        val voteSubhead = Publisher("PROJECTION: LIB HOLD")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, bq.party, con.party)
        val panel = candidateVotes(
            currentVotes,
            voteHeader,
            voteSubhead,
            "(MP)"
        )
            .withPrev(previousVotes, changeHeader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "Basic-2", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidatesBasicResultPctOnly() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val curr = LinkedHashMap<Candidate, Int>()
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = 674
        val prev = LinkedHashMap<Party, Int>()
        prev[ndp.party] = 585
        prev[pc.party] = 785
        prev[lib.party] = 1060
        prev[grn.party] = 106
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val header = Publisher("MONTAGUE-KILMUIR")
        val voteHeader = Publisher("9 OF 9 POLLS REPORTING")
        val voteSubhead = Publisher("PROJECTION: PC GAIN FROM LIB")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("CARDIGAN")
        val leader = Publisher(pc.party)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = Publisher(3)
        val panel = candidateVotesPctOnly(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .withPartyMap(shapesByDistrict.asOneTimePublisher(), selectedDistrict, leader, focus, mapHeader)
            .withNotes("SOURCE: Elections PEI".asOneTimePublisher())
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PctOnly-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidateScreenUpdating() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val curr = LinkedHashMap<Candidate, Int>()
        curr[ndp] = 0
        curr[pc] = 0
        curr[lib] = 0
        curr[grn] = 0
        val prev = LinkedHashMap<Party, Int>()
        prev[ndp.party] = 585
        prev[pc.party] = 785
        prev[lib.party] = 1060
        prev[grn.party] = 106
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val pctReporting = Publisher(0.0)
        val header = Publisher("MONTAGUE-KILMUIR")
        val voteHeader = Publisher("0 OF 9 POLLS REPORTING")
        val voteSubhead = Publisher("WAITING FOR RESULTS...")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("CARDIGAN")
        val leader = Publisher<PartyResult?>(null)
        val winner = Publisher<Candidate?>(null)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = Publisher(3)
        val panel = candidateVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .withResultMap(shapesByDistrict.asOneTimePublisher(), selectedDistrict, leader, focus, mapHeader)
            .withWinner(winner)
            .withPctReporting(pctReporting)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "Update-1", panel)

        curr[ndp] = 5
        curr[pc] = 47
        curr[lib] = 58
        curr[grn] = 52
        currentVotes.submit(curr)
        voteHeader.submit("1 OF 9 POLLS REPORTING")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(1.0 / 9)
        leader.submit(leading(lib.party))
        compareRendering("SimpleVoteViewPanel", "Update-2", panel)

        curr[ndp] = 8
        curr[pc] = 91
        curr[lib] = 100
        curr[grn] = 106
        currentVotes.submit(curr)
        voteHeader.submit("2 OF 9 POLLS REPORTING")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(2.0 / 9)
        leader.submit(leading(grn.party))
        compareRendering("SimpleVoteViewPanel", "Update-3", panel)

        curr[ndp] = 18
        curr[pc] = 287
        curr[lib] = 197
        curr[grn] = 243
        currentVotes.submit(curr)
        voteHeader.submit("5 OF 9 POLLS REPORTING")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(5.0 / 9)
        leader.submit(leading(pc.party))
        compareRendering("SimpleVoteViewPanel", "Update-4", panel)

        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = 675
        currentVotes.submit(curr)
        voteHeader.submit("9 OF 9 POLLS REPORTING")
        voteSubhead.submit("PROJECTION: PC GAIN FROM LIB")
        pctReporting.submit(9.0 / 9)
        leader.submit(elected(pc.party))
        winner.submit(pc)
        compareRendering("SimpleVoteViewPanel", "Update-5", panel)

        winner.submit(null)
        compareRendering("SimpleVoteViewPanel", "Update-6", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testZeroVotesSingleCandidate() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val curr = LinkedHashMap<Candidate, Int>()
        curr[ndp] = 6
        curr[pc] = 8
        curr[lib] = 11
        curr[grn] = 0
        val prev = LinkedHashMap<Party, Int>()
        prev[ndp.party] = 585
        prev[pc.party] = 785
        prev[lib.party] = 1060
        prev[grn.party] = 106
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val pctReporting = Publisher(1.0 / 9)
        val header = Publisher("MONTAGUE-KILMUIR")
        val voteHeader = Publisher("1 OF 9 POLLS REPORTING")
        val voteSubhead = Publisher("WAITING FOR RESULTS...")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("CARDIGAN")
        val leader = Publisher(leading(lib.party))
        val winner = Publisher<Candidate?>(null)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = Publisher(3)
        val panel = candidateVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .withResultMap(shapesByDistrict.asOneTimePublisher(), selectedDistrict, leader, focus, mapHeader)
            .withWinner(winner)
            .withPctReporting(pctReporting)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "ZeroVotes-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testPartyVoteScreen() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val curr = LinkedHashMap<Party, Int>()
        val prev = LinkedHashMap<Party, Int>()
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val pctReporting = Publisher(0.0)
        val header = Publisher("PRINCE EDWARD ISLAND")
        val voteHeader = Publisher("0 OF 27 DISTRICTS DECLARED")
        val voteSubhead = Publisher("WAITING FOR RESULTS...")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("PEI")
        val swingPartyOrder = listOf(ndp, grn, lib, pc)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher<List<Int>?>(null)
        val winnersByDistrict = Publisher<Map<Int, Party?>>(HashMap())
        val panel = partyVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .withPctReporting(pctReporting)
            .withPartyMap(shapesByDistrict.asOneTimePublisher(), winnersByDistrict, focus, mapHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PopularVote-1", panel)

        val winners = LinkedHashMap<Int, Party?>()
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = 675
        currentVotes.submit(curr)
        prev[ndp] = 585
        prev[pc] = 785
        prev[lib] = 1060
        prev[grn] = 106
        previousVotes.submit(prev)
        voteHeader.submit("1 OF 27 DISTRICTS DECLARED")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(1.0 / 27)
        winners[3] = pc
        winnersByDistrict.submit(winners)
        compareRendering("SimpleVoteViewPanel", "PopularVote-2", panel)

        focus.submit(shapesByDistrict.keys.filter { it <= 7 })
        mapHeader.submit("CARDIGAN")
        header.submit("CARDIGAN")
        pctReporting.submit(1.0 / 7)
        voteHeader.submit("1 OF 7 DISTRICTS DECLARED")
        compareRendering("SimpleVoteViewPanel", "PopularVote-3", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testPartyVoteTickScreen() {
        val dem = Party("DEMOCRAT", "DEM", Color.BLUE)
        val gop = Party("REPUBLICAN", "GOP", Color.RED)
        val curr = LinkedHashMap<Party, Int>()
        curr[dem] = 60572245
        curr[gop] = 50861970
        curr[Party.OTHERS] = 1978774
        val prev = LinkedHashMap<Party, Int>()
        prev[dem] = 61776554
        prev[gop] = 63173815
        prev[Party.OTHERS] = 3676641
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val pctReporting = Publisher(1.0)
        val header = Publisher("UNITED STATES")
        val voteHeader = Publisher("HOUSE OF REPRESENTATIVES")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2016")
        val swingHeader = Publisher("SWING SINCE 2016")
        val winner = Publisher(dem)
        val swingPartyOrder = listOf(dem, gop)
        val panel = partyVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withWinner(winner)
            .withPctReporting(pctReporting)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartyTick-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testMultipleCandidatesSameParty() {
        val dem = Party("DEMOCRAT", "DEM", Color.BLUE)
        val gop = Party("REPUBLICAN", "GOP", Color.RED)
        val lbt = Party("LIBERTARIAN", "LBT", Color.ORANGE)
        val ind = Party("INDEPENDENT", "IND", Color.GRAY)
        val curr = LinkedHashMap<Candidate, Int>()
        curr[Candidate("Raul Barrera", dem)] = 1747
        curr[Candidate("Bech Bruun", gop)] = 1570
        curr[Candidate("Michael Cloud", gop)] = 19856
        curr[Candidate("Judith Cutright", ind)] = 172
        curr[Candidate("Eric Holguin", dem)] = 11595
        curr[Candidate("Marty Perez", gop)] = 276
        curr[Candidate("Christopher Suprun", ind)] = 51
        curr[Candidate("Daniel Tinus", lbt)] = 144
        curr[Candidate("Mike Westergren", dem)] = 858
        val prev = LinkedHashMap<Party, Int>()
        prev[dem] = 88329
        prev[gop] = 142251
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val header = Publisher("TEXAS DISTRICT 27")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("GOP HOLD")
        val changeHeader = Publisher("CHANGE SINCE 2016")
        val swingHeader = Publisher("SWING SINCE 2016")
        val winner = Publisher(curr.entries.maxByOrNull { it.value }!!.key)
        val swingPartyOrder = listOf(dem, gop)
        val panel = candidateVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .withWinner(winner)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "SameParty-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidateMajorityLine() {
        val lrem = Party("LA R\u00c9PUBLIQUE EN MARCHE", "LREM", Color.ORANGE)
        val fn = Party("FRONT NATIONAL", "FN", Color.BLUE)
        val curr = LinkedHashMap<Candidate, Int>()
        curr[Candidate("Emmanuel Macron", lrem)] = 20743128
        curr[Candidate("Marine Le Pen", fn)] = 10638475
        val currentVotes = Publisher(curr)
        val header = Publisher("FRANCE PRESIDENT: ROUND 2")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("MACRON WIN")
        val showMajority = Publisher(true)
        val pctReporting = Publisher(1.0)
        val winner = Publisher<Candidate?>(curr.entries.maxByOrNull { it.value }!!.key)
        val panel = candidateVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withWinner(winner)
            .withMajorityLine(showMajority, "50% TO WIN")
            .withPctReporting(pctReporting)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "MajorityLine-1", panel)
        voteHeader.submit("15.4% REPORTING")
        voteSubhead.submit("PROJECTION: MACRON WIN")
        pctReporting.submit(0.154)
        winner.submit(null)
        curr[Candidate("Emmanuel Macron", lrem)] = 3825279
        curr[Candidate("Marine Le Pen", fn)] = 1033686
        currentVotes.submit(curr)
        compareRendering("SimpleVoteViewPanel", "MajorityLine-2", panel)
        pctReporting.submit(0.0)
        curr[Candidate("Emmanuel Macron", lrem)] = 0
        curr[Candidate("Marine Le Pen", fn)] = 0
        currentVotes.submit(curr)
        voteHeader.submit("0.0% REPORTING")
        voteSubhead.submit("")
        compareRendering("SimpleVoteViewPanel", "MajorityLine-3", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testAdditionalHighlightMap() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val curr = LinkedHashMap<Candidate, Int>()
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = 674
        val prev = LinkedHashMap<Party, Int>()
        prev[ndp.party] = 585
        prev[pc.party] = 785
        prev[lib.party] = 1060
        prev[grn.party] = 106
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val header = Publisher("MONTAGUE-KILMUIR")
        val voteHeader = Publisher("9 OF 9 POLLS REPORTING")
        val voteSubhead = Publisher("PROJECTION: PC GAIN FROM LIB")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val changeSubhead = Publisher("ADJUSTED FOR BOUNDARY CHANGES")
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("CARDIGAN")
        val leader = Publisher(pc.party)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it <= 7 })
        val additionalHighlight = Publisher<List<Int>>(ArrayList(shapesByDistrict.keys))
        val selectedDistrict = Publisher(3)
        val panel = candidateVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader, changeSubhead)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .withResultMap(shapesByDistrict.asOneTimePublisher(), selectedDistrict, leader.map { elected(it) }, focus, additionalHighlight, mapHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "AdditionalHighlightMap-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidateUncontested() {
        val dem = Candidate("Joe Kennedy III", Party("Democratic", "DEM", Color.BLUE))
        val curr = LinkedHashMap<Candidate, Int>()
        curr[dem] = 0
        val prev = LinkedHashMap<Party, Int>()
        prev[dem.party] = 265823
        prev[Party("Republican", "GOP", Color.RED)] = 113055
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val header = Publisher("MASSACHUSETTS DISTRICT 4")
        val voteHeader = Publisher("0.0% OF POLLS REPORTING")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2016")
        val swingHeader = Publisher("SWING SINCE 2016")
        val swingPartyOrder: List<Party> = ArrayList(prev.keys)
        val panel = candidateVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "Uncontested-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidatesSwitchingBetweenSingleAndDoubleLines() {
        val result2017 = LinkedHashMap<Candidate, Int>()
        result2017[Candidate("Boris Johnson", Party("Conservative", "CON", Color.BLUE))] = 23716
        result2017[Candidate("Vincent Lo", Party("Labour", "LAB", Color.RED))] = 18682
        result2017[Candidate("Rosina Robson", Party("Liberal Democrats", "LD", Color.ORANGE))] = 1835
        result2017[Candidate("Lizzy Kemp", Party("UK Independence Party", "UKIP", Color.MAGENTA.darker()))] = 1577
        result2017[Candidate("Mark Keir", Party("Green", "GRN", Color.GREEN.darker()))] = 884
        val result2019 = LinkedHashMap<Candidate, Int>()
        result2019[Candidate("Count Binface", Party("Independent", "IND", Color.GRAY))] = 69
        result2019[Candidate("Lord Buckethead", Party("Monster Raving Loony Party", "MRLP", Color.YELLOW))] = 125
        result2019[Candidate("Norma Burke", Party("Independent", "IND", Color.GRAY))] = 22
        result2019[Candidate("Geoffrey Courtenay", Party("UK Independence Party", "UKIP", Color.MAGENTA.darker()))] = 283
        result2019[Candidate("Joanne Humphreys", Party("Liberal Democrats", "LD", Color.ORANGE))] = 3026
        result2019[Candidate("Boris Johnson", Party("Conservative", "CON", Color.BLUE))] = 25351
        result2019[Candidate("Mark Keir", Party("Green", "GRN", Color.GREEN.darker()))] = 1090
        result2019[Candidate("Ali Milani", Party("Labour", "LAB", Color.RED))] = 18141
        result2019[Candidate("Bobby Smith", Party("Independent", "IND", Color.GRAY))] = 8
        result2019[Candidate("William Tobin", Party("Independent", "IND", Color.GRAY))] = 5
        result2019[Candidate("Alfie Utting", Party("Independent", "IND", Color.GRAY))] = 44
        result2019[Candidate("Yace \"Interplanetary Time Lord\" Yogenstein", Party("Independent", "IND", Color.GRAY))] = 23
        val currentVotes = Publisher(result2017)
        val header = Publisher("UXBRIDGE AND SOUTH RUISLIP")
        val voteHeader = Publisher("2017 RESULT")
        val voteSubhead = Publisher("")
        val winner = Publisher(Candidate("Boris Johnson", Party("Conservative", "CON", Color.BLUE)))
        val panel = candidateVotes(currentVotes, voteHeader, voteSubhead)
            .withWinner(winner)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "LotsOfCandidates-1", panel)
        currentVotes.submit(result2019)
        voteHeader.submit("2019 RESULT")
        compareRendering("SimpleVoteViewPanel", "LotsOfCandidates-2", panel)
        currentVotes.submit(result2017)
        voteHeader.submit("2017 RESULT")
        compareRendering("SimpleVoteViewPanel", "LotsOfCandidates-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidatesLimit() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val curr = LinkedHashMap<Candidate, Int>()
        curr[ndp] = 0
        curr[pc] = 0
        curr[lib] = 0
        curr[grn] = 0
        val prev = LinkedHashMap<Party, Int>()
        prev[ndp.party] = 585
        prev[pc.party] = 785
        prev[lib.party] = 1060
        prev[grn.party] = 106
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val pctReporting = Publisher(0.0)
        val header = Publisher("MONTAGUE-KILMUIR")
        val voteHeader = Publisher("0 OF 9 POLLS REPORTING")
        val voteSubhead = Publisher("WAITING FOR RESULTS...")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("CARDIGAN")
        val leader = Publisher<PartyResult?>(null)
        val winner = Publisher<Candidate?>(null)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = Publisher(3)
        val panel = candidateVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withLimit(3)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .withResultMap(shapesByDistrict.asOneTimePublisher(), selectedDistrict, leader, focus, mapHeader)
            .withWinner(winner)
            .withPctReporting(pctReporting)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "CandidateOthers-1", panel)
        winner.submit(lib)
        compareRendering("SimpleVoteViewPanel", "CandidateOthers-2", panel)
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = 675
        currentVotes.submit(curr)
        voteHeader.submit("9 OF 9 POLLS REPORTING")
        voteSubhead.submit("PROJECTION: PC GAIN FROM LIB")
        pctReporting.submit(9.0 / 9)
        leader.submit(elected(pc.party))
        winner.submit(pc)
        compareRendering("SimpleVoteViewPanel", "CandidateOthers-3", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidatesLimitWithMandatoryParties() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val curr = LinkedHashMap<Candidate, Int>()
        curr[ndp] = 0
        curr[pc] = 0
        curr[lib] = 0
        curr[grn] = 0
        val prev = LinkedHashMap<Party, Int>()
        prev[ndp.party] = 585
        prev[pc.party] = 785
        prev[lib.party] = 1060
        prev[grn.party] = 106
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val pctReporting = Publisher(0.0)
        val header = Publisher("MONTAGUE-KILMUIR")
        val voteHeader = Publisher("0 OF 9 POLLS REPORTING")
        val voteSubhead = Publisher("WAITING FOR RESULTS...")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("CARDIGAN")
        val leader = Publisher<PartyResult?>(null)
        val winner = Publisher<Candidate?>(null)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = Publisher(3)
        val panel = candidateVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withLimit(3, pc.party, lib.party)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .withResultMap(shapesByDistrict.asOneTimePublisher(), selectedDistrict, leader, focus, mapHeader)
            .withWinner(winner)
            .withPctReporting(pctReporting)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "CandidateOthersMandatory-1", panel)
        winner.submit(lib)
        compareRendering("SimpleVoteViewPanel", "CandidateOthersMandatory-2", panel)
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = 675
        currentVotes.submit(curr)
        voteHeader.submit("9 OF 9 POLLS REPORTING")
        voteSubhead.submit("PROJECTION: PC GAIN FROM LIB")
        pctReporting.submit(9.0 / 9)
        leader.submit(elected(pc.party))
        winner.submit(pc)
        compareRendering("SimpleVoteViewPanel", "CandidateOthersMandatory-3", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testPartiesNotRunningAgain() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Candidate("Jean-G\u00e9rard Chiasson", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Eric Mallet", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("Marie Leclerc", Party("Green", "GRN", Color.GREEN.darker()))
        val curr = LinkedHashMap<Candidate, Int>()
        curr[pc] = 714
        curr[lib] = 6834
        curr[grn] = 609
        val prev = LinkedHashMap<Party, Int>()
        prev[ndp] = 578
        prev[pc.party] = 4048
        prev[lib.party] = 3949
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val header = Publisher("SHIPPAGAN-LAM\u00c8QUE-MISCOU")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2018")
        val swingHeader = Publisher("SWING SINCE 2018")
        val winner = Publisher(lib)
        val swingPartyOrder = listOf(ndp, grn.party, lib.party, pc.party)
        val panel = candidateVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withWinner(winner)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartiesNotRunningAgain-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testPartiesNotRunningAgainPrevOthers() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Candidate("Jean-G\u00e9rard Chiasson", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Eric Mallet", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("Marie Leclerc", Party("Green", "GRN", Color.GREEN.darker()))
        val curr = LinkedHashMap<Candidate, Int>()
        curr[pc] = 714
        curr[lib] = 6834
        curr[grn] = 609
        val prev = LinkedHashMap<Party, Int>()
        prev[Party.OTHERS] = 289
        prev[ndp] = 289
        prev[pc.party] = 4048
        prev[lib.party] = 3949
        prev[grn.party] = 0
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val header = Publisher("SHIPPAGAN-LAM\u00c8QUE-MISCOU")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2018")
        val swingHeader = Publisher("SWING SINCE 2018")
        val winner = Publisher(lib)
        val swingPartyOrder = listOf(grn.party, lib.party, pc.party)
        val panel = candidateVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withWinner(winner)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartiesNotRunningAgain-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testPartiesNotRunningAgainOthers() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Candidate("Jean-G\u00e9rard Chiasson", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Eric Mallet", Party("Liberal", "LIB", Color.RED))
        val oth = Candidate.OTHERS
        val curr = LinkedHashMap<Candidate, Int>()
        curr[pc] = 714
        curr[lib] = 6834
        curr[oth] = 609
        val prev = LinkedHashMap<Party, Int>()
        prev[ndp] = 578
        prev[pc.party] = 4048
        prev[lib.party] = 3949
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val header = Publisher("SHIPPAGAN-LAM\u00c8QUE-MISCOU")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2018")
        val swingHeader = Publisher("SWING SINCE 2018")
        val winner = Publisher(lib)
        val swingPartyOrder = listOf(ndp, oth.party, lib.party, pc.party)
        val panel = candidateVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withWinner(winner)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartiesNotRunningAgainOthers-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testPartiesNotRunningAgainOthersPrevOthers() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Candidate("Jean-G\u00e9rard Chiasson", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Eric Mallet", Party("Liberal", "LIB", Color.RED))
        val oth = Candidate.OTHERS
        val curr = LinkedHashMap<Candidate, Int>()
        curr[pc] = 714
        curr[lib] = 6834
        curr[oth] = 609
        val prev = LinkedHashMap<Party, Int>()
        prev[Party.OTHERS] = 289
        prev[ndp] = 289
        prev[pc.party] = 4048
        prev[lib.party] = 3949
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val header = Publisher("SHIPPAGAN-LAM\u00c8QUE-MISCOU")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2018")
        val swingHeader = Publisher("SWING SINCE 2018")
        val winner = Publisher(lib)
        val swingPartyOrder = listOf(ndp, oth.party, lib.party, pc.party)
        val panel = candidateVotes(
            currentVotes, voteHeader, voteSubhead
        )
            .withPrev(previousVotes, changeHeader)
            .withWinner(winner)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartiesNotRunningAgainOthers-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testWinningPartyNotRunningAgain() {
        val con = Candidate("Greg Smith", Party("Conservative", "CON", Color.BLUE))
        val ld = Candidate("Stephen Dorrell", Party("Liberal Democrats", "LD", Color.ORANGE))
        val lab = Candidate("David Morgan", Party("Labour", "LAB", Color.RED))
        val bxp = Candidate("Andrew Bell", Party("Brexit Party", "BXP", Color.CYAN.darker()))
        val ind = Candidate("Ned Thompson", Party("Independent", "IND", Party.OTHERS.color))
        val ed = Candidate("Antonio Vitiello", Party("English Democrats", "ED", Color.ORANGE.darker()))
        val spkr = Party("Speaker", "SPKR", Color.GRAY)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ukip = Party("UK Independence Party", "UKIP", Color.MAGENTA.darker())
        val curr = LinkedHashMap<Candidate, Int>()
        curr[con] = 37035
        curr[ld] = 16624
        curr[lab] = 7638
        curr[bxp] = 1286
        curr[ind] = 681
        curr[ed] = 194
        val prev = LinkedHashMap<Party, Int>()
        prev[spkr] = 34299
        prev[grn] = 8574
        prev[ind.party] = 5638
        prev[ukip] = 4168
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val header = Publisher("BUCKINGHAM")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2017")
        val changeSubhead = Publisher("NOT APPLICABLE: PREVIOUSLY SPEAKER'S SEAT")
        val swingHeader = Publisher("SWING SINCE 2017")
        val winner = Publisher(con)
        val swingPartyOrder = listOf(lab.party, ld.party, con.party, bxp.party)
        val panel = candidateVotes(currentVotes, voteHeader, voteSubhead)
            .withPrev(previousVotes, changeHeader, changeSubhead)
            .withWinner(winner)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "WinningPartyNotRunningAgain-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidateRunoffSingleLine() {
        val macron = Candidate("Emmanuel Macron", Party("En Marche!", "EM", Color.ORANGE))
        val lePen = Candidate("Marine Le Pen", Party("National Front", "FN", Color.BLUE.darker()))
        val fillon = Candidate("Fran\u00e7ois Fillon", Party("The Republicans", "LR", Color.BLUE))
        val melenchon = Candidate("Jean-Luc M\u00e9lenchon", Party("La France Insoumise", "FI", Color.ORANGE.darker()))
        val hamon = Candidate("Beno\u00eet Hamon", Party("Socialist Party", "PS", Color.RED))
        val dupontAignan = Candidate("Nicolas Dupont-Aignan", Party("Debout la France", "DLF", Color.CYAN.darker()))
        val lasalle = Candidate("Jean Lasalle", Party("R\u00e9sistons!", "R\u00c9S", Color.CYAN))
        val poutou = Candidate("Philippe Poutou", Party("New Anticapitalist Party", "NPA", Color.RED.darker()))
        val asselineau = Candidate("Fran\u00e7ois Asselineau", Party("Popular Republican Union", "UPR", Color.CYAN.darker().darker()))
        val arthaud = Candidate("Nathalie Arthaud", Party("Lutte Ouvri\u00e8re", "LO", Color.RED))
        val cheminade = Candidate("Jacques Cheminade", Party("Solidarity and Progress", "S&P", Color.GRAY))
        val curr = LinkedHashMap<Candidate, Int>()
        curr[macron] = 8656346
        curr[lePen] = 7678491
        curr[fillon] = 7212995
        curr[melenchon] = 7059951
        curr[hamon] = 2291288
        curr[dupontAignan] = 1695000
        curr[lasalle] = 435301
        curr[poutou] = 394505
        curr[asselineau] = 332547
        curr[arthaud] = 232384
        curr[cheminade] = 65586
        val currentVotes = Publisher(curr)
        val header = Publisher("ELECTION 2017: FRANCE DECIDES")
        val voteHeader = Publisher("FIRST ROUND RESULT")
        val voteSubhead = Publisher("")
        val runoff = Publisher<Set<Candidate>?>(null)
        val panel = candidateVotes(currentVotes, voteHeader, voteSubhead)
            .withRunoff(runoff)
            .withMajorityLine(true.asOneTimePublisher(), "50% TO WIN")
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "CandidateRunoffSingleLine-1", panel)
        runoff.submit(setOf(macron, lePen))
        compareRendering("SimpleVoteViewPanel", "CandidateRunoffSingleLine-2", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidateRunoffDualLine() {
        val macron = Candidate("Emmanuel Macron", Party("En Marche!", "EM", Color.ORANGE))
        val lePen = Candidate("Marine Le Pen", Party("National Front", "FN", Color.BLUE.darker()))
        val fillon = Candidate("Fran\u00e7ois Fillon", Party("The Republicans", "LR", Color.BLUE))
        val melenchon = Candidate("Jean-Luc M\u00e9lenchon", Party("La France Insoumise", "FI", Color.ORANGE.darker()))
        val hamon = Candidate("Beno\u00eet Hamon", Party("Socialist Party", "PS", Color.RED))
        val dupontAignan = Candidate("Nicolas Dupont-Aignan", Party("Debout la France", "DLF", Color.CYAN.darker()))
        val lasalle = Candidate("Jean Lasalle", Party("R\u00e9sistons!", "R\u00c9S", Color.CYAN))
        val poutou = Candidate("Philippe Poutou", Party("New Anticapitalist Party", "NPA", Color.RED.darker()))
        val asselineau = Candidate("Fran\u00e7ois Asselineau", Party("Popular Republican Union", "UPR", Color.CYAN.darker().darker()))
        val arthaud = Candidate("Nathalie Arthaud", Party("Lutte Ouvri\u00e8re", "LO", Color.RED))
        val cheminade = Candidate("Jacques Cheminade", Party("Solidarity and Progress", "S&P", Color.GRAY))
        val curr = LinkedHashMap<Candidate, Int>()
        curr[macron] = 8656346
        curr[lePen] = 7678491
        curr[fillon] = 7212995
        curr[melenchon] = 7059951
        curr[hamon] = 2291288
        curr[dupontAignan] = 1695000
        curr[lasalle] = 435301
        curr[poutou] = 394505
        curr[asselineau] = 332547
        curr[arthaud] = 232384
        curr[cheminade] = 65586
        val currentVotes = Publisher(topAndOthers(curr, 6, Candidate.OTHERS))
        val header = Publisher("ELECTION 2017: FRANCE DECIDES")
        val voteHeader = Publisher("FIRST ROUND RESULT")
        val voteSubhead = Publisher("")
        val runoff = Publisher<Set<Candidate>?>(null)
        val panel = candidateVotes(currentVotes, voteHeader, voteSubhead)
            .withRunoff(runoff)
            .withMajorityLine(true.asOneTimePublisher(), "50% TO WIN")
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "CandidateRunoffDualLine-1", panel)
        runoff.submit(setOf(macron, lePen))
        compareRendering("SimpleVoteViewPanel", "CandidateRunoffDualLine-2", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testVoteRangeScreen() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val curr = LinkedHashMap<Party, ClosedRange<Double>>()
        curr[ndp] = (0.030).rangeTo(0.046)
        curr[pc] = (0.290).rangeTo(0.353)
        curr[lib] = (0.257).rangeTo(0.292)
        curr[grn] = (0.343).rangeTo(0.400)
        val prev = LinkedHashMap<Party, Int>()
        prev[ndp] = 8997
        prev[pc] = 30663
        prev[lib] = 33481
        prev[grn] = 8857
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val header = Publisher("PRINCE EDWARD ISLAND")
        val voteHeader = Publisher("OPINION POLL RANGE")
        val voteSubhead = Publisher("SINCE ELECTION CALL")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("PEI")
        val swingPartyOrder = listOf(ndp, grn, lib, pc)
        val shapesByDistrict = peiShapesByDistrict()
        val winners: Map<Int, Party> = HashMap()
        val panel = partyRangeVotes(currentVotes, voteHeader, voteSubhead)
            .withPrev(previousVotes, changeHeader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .withPartyMap(shapesByDistrict.asOneTimePublisher(), winners.asOneTimePublisher(), null.asOneTimePublisher(), mapHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "Range-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidatesResultMidDeclaration() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val curr = LinkedHashMap<Candidate, Int?>()
        curr[ndp] = null
        curr[pc] = null
        curr[lib] = null
        curr[grn] = null
        val prev = LinkedHashMap<Party, Int>()
        prev[ndp.party] = 585
        prev[pc.party] = 785
        prev[lib.party] = 1060
        prev[grn.party] = 106
        val currentVotes = Publisher(curr)
        val previousVotes = Publisher(prev)
        val header = Publisher("MONTAGUE-KILMUIR")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("CARDIGAN")
        val leader = Publisher<Party?>(null)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = Publisher(3)
        val panel = candidateVotes(currentVotes, voteHeader, voteSubhead)
            .withPrev(previousVotes, changeHeader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .withPartyMap(shapesByDistrict.asOneTimePublisher(), selectedDistrict, leader, focus, mapHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-1", panel)
        curr[ndp] = 124
        curr[pc] = null
        curr[lib] = null
        curr[grn] = null
        currentVotes.submit(curr)
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-2", panel)
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = null
        curr[grn] = null
        currentVotes.submit(curr)
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-3", panel)
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = null
        currentVotes.submit(curr)
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-4", panel)
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = 675
        currentVotes.submit(curr)
        leader.submit(pc.party)
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-5", panel)
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
        val currentVotes = Publisher(LinkedHashMap<Party, Int>())
        val previousVotes = Publisher(LinkedHashMap<Party, Int>())
        val header = Publisher("NORTHERN IRELAND")
        val seatHeader = Publisher("2017 RESULTS")
        val seatSubhead = Publisher<String?>(null)
        val changeHeader = Publisher("NOTIONAL CHANGE SINCE 2016")
        val panel = partyVotes(currentVotes, seatHeader, seatSubhead)
            .withPrev(previousVotes, changeHeader)
            .withClassification({ mapping.getOrDefault(it, others) }, "BY DESIGNATION".asOneTimePublisher())
            .withSwing(compareBy { listOf(nationalists, others, unionists).indexOf(it) }, "FIRST PREFERENCE SWING SINCE 2016".asOneTimePublisher())
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartyClassifications-1", panel)
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
        prevVotes[ind_u] = 351 + 3270
        prevVotes[ind_n] = 0
        prevVotes[ind_o] = 224 + 124 + 32 + 19380
        previousVotes.submit(prevVotes)
        compareRendering("SimpleVoteViewPanel", "PartyClassifications-2", panel)
    }

    @Test
    fun testPartiesConsolidatedInDiffIfTooMany() {
        val CPA = Party("Christian Peoples Alliance", "CPA", Color(148, 0, 170))
        val GREEN_SOC = Party("Alliance for Green Socialism", "AGS", Color(0, 137, 91))
        val INDEPENDENT = Party("Independent", "IND", Party.OTHERS.color)
        val WORKERS = Party("Workers Party", "WP", Color(215, 13, 13))
        val LIBDEM = Party("Liberal Democrats", "LD", Color(0xfaa01a))
        val ENGLISH_DEM = Party("English Democrats", "ED", Color(140, 15, 15))
        val MRLP = Party("Monster Raving Loony Party", "MRLP", Color(255, 240, 0))
        val HERITAGE = Party("Heritage Party", "HERITAGE", Color(13, 0, 173))
        val LABOUR = Party("Labour", "LAB", Color(0xe4003b))
        val SDP = Party("Social Democratic Party", "SDP", Color(0, 65, 118))
        val YORKSHIRE = Party("Yorkshire Party", "YP", Color(0, 124, 178))
        val REJOIN = Party("Rejoin EU", "REJOIN", Color(0, 51, 153))
        val CONSERVATIVE = Party("Conservative", "CON", Color(0x00aeef))
        val UKIP = Party("UK Independence Party", "UKIP", Color(0x6d3177))
        val FREE_ALL = Party("Freedom Alliance", "FREE-ALL", Color(200, 24, 125))
        val FOR_BRITAIN = Party("For Britain", "FOR", Color(0, 0, 128))
        val REFORM = Party("Reform UK", "REF", Color(0x00c0d5))
        val GREEN = Party("Green", "GRN", Color(0x6ab023))
        val curr = mapOf(
            Candidate("Paul Bickerdike", CPA) to 102,
            Candidate("Mike Davies", GREEN_SOC) to 104,
            Candidate("Jayda Fransen", INDEPENDENT) to 50,
            Candidate("George Galloway", WORKERS) to 8264,
            Candidate("Tom Gordon", LIBDEM) to 1254,
            Candidate("Th\u00e9r\u00e8se Hirst", ENGLISH_DEM) to 207,
            Candidate("Howling Laud Hope", MRLP) to 107,
            Candidate("Susan Laird", HERITAGE) to 33,
            Candidate("Kim Leadbeater", LABOUR) to 13296,
            Candidate("Ollie Purser", SDP) to 66,
            Candidate("Corey Robinson", YORKSHIRE) to 816,
            Candidate("Andrew Smith", REJOIN) to 75,
            Candidate("Ryan Stephenson", CONSERVATIVE) to 12973,
            Candidate("Jack Thomson", UKIP) to 151,
            Candidate("Jonathon Tilt", FREE_ALL) to 100,
            Candidate("Anne Marie Waters", FOR_BRITAIN) to 97
        )
        val prev = mapOf(LABOUR to 22594, CONSERVATIVE to 19069, INDEPENDENT to 6432, LIBDEM to 2462, REFORM to 1678, GREEN to 692)
        val panel = candidateVotes(curr.asOneTimePublisher(), "DECLARED RESULT".asOneTimePublisher(), "".asOneTimePublisher())
            .withPrev(prev.asOneTimePublisher(), "CHANGE SINCE 2019".asOneTimePublisher())
            .build("BATLEY AND SPEN".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "ConsolidateInDiffIfTooMany-1", panel)
    }

    @Test
    fun testNewPartiesCandidatesMergedWithPrevOthers() {
        val alp = Party("Labor", "ALP", Color.RED)
        val lib = Party("Liberal", "LIB", Color.BLUE)
        val grn = Party("Greens", "GRN", Color.GREEN.darker())
        val cdp = Party("Christian Democrats", "CDP", Color.MAGENTA.darker())
        val uap = Party("United Australia Party", "UAP", Color.YELLOW)
        val sci = Party("Science", "SCI", Color.CYAN)
        val sus = Party("Sustainable Australia", "SUS", Color.GREEN.darker().darker())
        val ind = Party("Independent", "IND", Party.OTHERS.color)
        val curr = mapOf(
            Candidate("Julian Leeser", lib, true) to 53741,
            Candidate("Katie Gompertz", alp) to 19821,
            Candidate("Monica Tan", grn) to 11157,
            Candidate("Simon Taylor", cdp) to 2163,
            Candidate("Mick Gallagher", ind) to 2104,
            Candidate("Craig McLaughlin", uap) to 1576,
            Candidate("Brendan Clarke", sci) to 1465,
            Candidate("Justin Thomas", sus) to 1425,
            Candidate("Roger Woodward", ind) to 495
        )
        val curr2CP = mapOf(
            Candidate("Julian Leeser", lib, true) to 61675,
            Candidate("Katie Gompertz", alp) to 32272
        )
        val prev = mapOf(
            lib to 53678,
            alp to 18693,
            grn to 10815,
            Party.OTHERS to 5213 + 2859 + 1933 + 826
        )
        val prev2CP = mapOf(
            lib to 62470,
            alp to 31547
        )
        val panel = candidateVotes(curr.asOneTimePublisher(), "PRIMARY VOTE".asOneTimePublisher(), "100% REPORTING".asOneTimePublisher())
            .withPrev(prev.asOneTimePublisher(), "CHANGE SINCE 2016".asOneTimePublisher())
            .withPreferences(curr2CP.asOneTimePublisher(), "TWO CANDIDATE PREFERRED".asOneTimePublisher(), "100% REPORTING".asOneTimePublisher())
            .withPrevPreferences(prev2CP.asOneTimePublisher())
            .withSwing(Comparator.comparing { when (it) { lib -> 1; alp -> -1; else -> 0 } }, "SWING SINCE 2016".asOneTimePublisher())
            .withWinner(Candidate("Julian Leeser", lib, true).asOneTimePublisher())
            .build("BEROWRA".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "NewPartiesCandidatesMergedWithPrevOthers", panel)
    }

    @Throws(IOException::class)
    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = MapFrameTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return readShapes(peiMap, "DIST_NO", Int::class.java)
    }
}
