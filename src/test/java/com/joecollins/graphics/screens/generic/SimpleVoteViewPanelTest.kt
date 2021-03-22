package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.components.MapFrameTest
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateVotes
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateVotesPctOnly
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyRangeVotes
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyVotes
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader.readShapes
import com.joecollins.models.general.Aggregators.topAndOthers
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PartyResult.Companion.elected
import com.joecollins.models.general.PartyResult.Companion.leading
import java.awt.Color
import java.awt.Shape
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.IdentityHashMap
import java.util.LinkedHashMap
import kotlin.Throws
import org.junit.Test

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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val header = BindableWrapper("MONTAGUE-KILMUIR")
        val voteHeader = BindableWrapper("9 OF 9 POLLS REPORTING")
        val voteSubhead = BindableWrapper("PROJECTION: PC GAIN FROM LIB")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val swingHeader = BindableWrapper("SWING SINCE 2015")
        val mapHeader = BindableWrapper("CARDIGAN")
        val leader = BindableWrapper(pc.party)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = BindableWrapper(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = BindableWrapper(3)
        val panel = candidateVotes(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .withPartyMap(fixedBinding(shapesByDistrict), selectedDistrict.binding, leader.binding, focus.binding, mapHeader.binding)
                .build(header.binding)
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
                "Isabel Dion", Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker()))
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val header = BindableWrapper("LASALLE\u2014\u00c9MARD\u2014VERDUN")
        val voteHeader = BindableWrapper("100% OF POLLS REPORTING")
        val voteSubhead = BindableWrapper("PROJECTION: LIB HOLD")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val swingHeader = BindableWrapper("SWING SINCE 2015")
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, bq.party, con.party)
        val panel = candidateVotes(
                currentVotes.binding,
                voteHeader.binding,
                voteSubhead.binding,
                "(MP)")
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val header = BindableWrapper("MONTAGUE-KILMUIR")
        val voteHeader = BindableWrapper("9 OF 9 POLLS REPORTING")
        val voteSubhead = BindableWrapper("PROJECTION: PC GAIN FROM LIB")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val swingHeader = BindableWrapper("SWING SINCE 2015")
        val mapHeader = BindableWrapper("CARDIGAN")
        val leader = BindableWrapper(pc.party)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = BindableWrapper(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = BindableWrapper(3)
        val panel = candidateVotesPctOnly(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .withPartyMap(fixedBinding(shapesByDistrict), selectedDistrict.binding, leader.binding, focus.binding, mapHeader.binding)
                .withNotes(fixedBinding("SOURCE: Elections PEI"))
                .build(header.binding)
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val pctReporting = BindableWrapper(0.0)
        val header = BindableWrapper("MONTAGUE-KILMUIR")
        val voteHeader = BindableWrapper("0 OF 9 POLLS REPORTING")
        val voteSubhead = BindableWrapper("WAITING FOR RESULTS...")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val swingHeader = BindableWrapper("SWING SINCE 2015")
        val mapHeader = BindableWrapper("CARDIGAN")
        val leader = BindableWrapper<PartyResult?>(null)
        val winner = BindableWrapper<Candidate?>(null)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = BindableWrapper(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = BindableWrapper(3)
        val panel = candidateVotes(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .withResultMap(fixedBinding(shapesByDistrict), selectedDistrict.binding, leader.binding, focus.binding, mapHeader.binding)
                .withWinner(winner.binding)
                .withPctReporting(pctReporting.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "Update-1", panel)

        curr[ndp] = 5
        curr[pc] = 47
        curr[lib] = 58
        curr[grn] = 52
        currentVotes.value = curr
        voteHeader.value = "1 OF 9 POLLS REPORTING"
        voteSubhead.value = "PROJECTION: TOO EARLY TO CALL"
        pctReporting.value = 1.0 / 9
        leader.value = leading(lib.party)
        compareRendering("SimpleVoteViewPanel", "Update-2", panel)

        curr[ndp] = 8
        curr[pc] = 91
        curr[lib] = 100
        curr[grn] = 106
        currentVotes.value = curr
        voteHeader.value = "2 OF 9 POLLS REPORTING"
        voteSubhead.value = "PROJECTION: TOO EARLY TO CALL"
        pctReporting.value = 2.0 / 9
        leader.value = leading(grn.party)
        compareRendering("SimpleVoteViewPanel", "Update-3", panel)

        curr[ndp] = 18
        curr[pc] = 287
        curr[lib] = 197
        curr[grn] = 243
        currentVotes.value = curr
        voteHeader.value = "5 OF 9 POLLS REPORTING"
        voteSubhead.value = "PROJECTION: TOO EARLY TO CALL"
        pctReporting.value = 5.0 / 9
        leader.value = leading(pc.party)
        compareRendering("SimpleVoteViewPanel", "Update-4", panel)

        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = 675
        currentVotes.value = curr
        voteHeader.value = "9 OF 9 POLLS REPORTING"
        voteSubhead.value = "PROJECTION: PC GAIN FROM LIB"
        pctReporting.value = 9.0 / 9
        leader.value = elected(pc.party)
        winner.value = pc
        compareRendering("SimpleVoteViewPanel", "Update-5", panel)

        winner.value = null
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val pctReporting = BindableWrapper(1.0 / 9)
        val header = BindableWrapper("MONTAGUE-KILMUIR")
        val voteHeader = BindableWrapper("1 OF 9 POLLS REPORTING")
        val voteSubhead = BindableWrapper("WAITING FOR RESULTS...")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val swingHeader = BindableWrapper("SWING SINCE 2015")
        val mapHeader = BindableWrapper("CARDIGAN")
        val leader = BindableWrapper(leading(lib.party))
        val winner = BindableWrapper<Candidate?>(null)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = BindableWrapper(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = BindableWrapper(3)
        val panel = candidateVotes(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .withResultMap(fixedBinding(shapesByDistrict), selectedDistrict.binding, leader.binding, focus.binding, mapHeader.binding)
                .withWinner(winner.binding)
                .withPctReporting(pctReporting.binding)
                .build(header.binding)
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val pctReporting = BindableWrapper(0.0)
        val header = BindableWrapper("PRINCE EDWARD ISLAND")
        val voteHeader = BindableWrapper("0 OF 27 DISTRICTS DECLARED")
        val voteSubhead = BindableWrapper("WAITING FOR RESULTS...")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val swingHeader = BindableWrapper("SWING SINCE 2015")
        val mapHeader = BindableWrapper("PEI")
        val swingPartyOrder = listOf(ndp, grn, lib, pc)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = BindableWrapper<List<Int>?>(null)
        val winnersByDistrict = BindableWrapper<Map<Int, Party?>>(HashMap())
        val panel = partyVotes(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .withPctReporting(pctReporting.binding)
                .withPartyMap(fixedBinding(shapesByDistrict), winnersByDistrict.binding, focus.binding, mapHeader.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PopularVote-1", panel)

        val winners = LinkedHashMap<Int, Party?>()
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = 675
        currentVotes.value = curr
        prev[ndp] = 585
        prev[pc] = 785
        prev[lib] = 1060
        prev[grn] = 106
        previousVotes.value = prev
        voteHeader.value = "1 OF 27 DISTRICTS DECLARED"
        voteSubhead.value = "PROJECTION: TOO EARLY TO CALL"
        pctReporting.value = 1.0 / 27
        winners[3] = pc
        winnersByDistrict.value = winners
        compareRendering("SimpleVoteViewPanel", "PopularVote-2", panel)

        focus.value = shapesByDistrict.keys.filter { it <= 7 }
        mapHeader.value = "CARDIGAN"
        header.value = "CARDIGAN"
        pctReporting.value = 1.0 / 7
        voteHeader.value = "1 OF 7 DISTRICTS DECLARED"
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val pctReporting = BindableWrapper(1.0)
        val header = BindableWrapper("UNITED STATES")
        val voteHeader = BindableWrapper("HOUSE OF REPRESENTATIVES")
        val voteSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2016")
        val swingHeader = BindableWrapper("SWING SINCE 2016")
        val winner = BindableWrapper(dem)
        val swingPartyOrder = listOf(dem, gop)
        val panel = partyVotes(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withWinner(winner.binding)
                .withPctReporting(pctReporting.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val header = BindableWrapper("TEXAS DISTRICT 27")
        val voteHeader = BindableWrapper("OFFICIAL RESULT")
        val voteSubhead = BindableWrapper("GOP HOLD")
        val changeHeader = BindableWrapper("CHANGE SINCE 2016")
        val swingHeader = BindableWrapper("SWING SINCE 2016")
        val winner = BindableWrapper(curr.entries.maxByOrNull { it.value }!!.key)
        val swingPartyOrder = listOf(dem, gop)
        val panel = candidateVotes(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .withWinner(winner.binding)
                .build(header.binding)
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
        val currentVotes = BindableWrapper(curr)
        val header = BindableWrapper("FRANCE PRESIDENT: ROUND 2")
        val voteHeader = BindableWrapper("OFFICIAL RESULT")
        val voteSubhead = BindableWrapper("MACRON WIN")
        val showMajority = BindableWrapper(true)
        val pctReporting = BindableWrapper(1.0)
        val winner = BindableWrapper<Candidate?>(curr.entries.maxByOrNull { it.value }!!.key)
        val panel = candidateVotes(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withWinner(winner.binding)
                .withMajorityLine(showMajority.binding, fixedBinding("50% TO WIN"))
                .withPctReporting(pctReporting.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "MajorityLine-1", panel)
        voteHeader.value = "15.4% REPORTING"
        voteSubhead.value = "PROJECTION: MACRON WIN"
        pctReporting.value = 0.154
        winner.value = null
        curr[Candidate("Emmanuel Macron", lrem)] = 3825279
        curr[Candidate("Marine Le Pen", fn)] = 1033686
        currentVotes.value = curr
        compareRendering("SimpleVoteViewPanel", "MajorityLine-2", panel)
        pctReporting.value = 0.0
        curr[Candidate("Emmanuel Macron", lrem)] = 0
        curr[Candidate("Marine Le Pen", fn)] = 0
        currentVotes.value = curr
        voteHeader.value = "0.0% REPORTING"
        voteSubhead.value = ""
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val header = BindableWrapper("MONTAGUE-KILMUIR")
        val voteHeader = BindableWrapper("9 OF 9 POLLS REPORTING")
        val voteSubhead = BindableWrapper("PROJECTION: PC GAIN FROM LIB")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val changeSubhead = BindableWrapper("ADJUSTED FOR BOUNDARY CHANGES")
        val swingHeader = BindableWrapper("SWING SINCE 2015")
        val mapHeader = BindableWrapper("CARDIGAN")
        val leader = BindableWrapper(pc.party)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = BindableWrapper(shapesByDistrict.keys.filter { it <= 7 })
        val additionalHighlight = BindableWrapper<List<Int>>(ArrayList(shapesByDistrict.keys))
        val selectedDistrict = BindableWrapper(3)
        val panel = candidateVotes(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding, changeSubhead.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .withResultMap(fixedBinding(shapesByDistrict), selectedDistrict.binding, leader.binding.map { elected(it) }, focus.binding, additionalHighlight.binding, mapHeader.binding)
                .build(header.binding)
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val header = BindableWrapper("MASSACHUSETTS DISTRICT 4")
        val voteHeader = BindableWrapper("0.0% OF POLLS REPORTING")
        val voteSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2016")
        val swingHeader = BindableWrapper("SWING SINCE 2016")
        val swingPartyOrder: List<Party> = ArrayList(prev.keys)
        val panel = candidateVotes(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
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
        val currentVotes = BindableWrapper(result2017)
        val header = BindableWrapper("UXBRIDGE AND SOUTH RUISLIP")
        val voteHeader = BindableWrapper("2017 RESULT")
        val voteSubhead = BindableWrapper("")
        val winner = BindableWrapper(Candidate("Boris Johnson", Party("Conservative", "CON", Color.BLUE)))
        val panel = candidateVotes(currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withWinner(winner.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "LotsOfCandidates-1", panel)
        currentVotes.value = result2019
        voteHeader.value = "2019 RESULT"
        compareRendering("SimpleVoteViewPanel", "LotsOfCandidates-2", panel)
        currentVotes.value = result2017
        voteHeader.value = "2017 RESULT"
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val pctReporting = BindableWrapper(0.0)
        val header = BindableWrapper("MONTAGUE-KILMUIR")
        val voteHeader = BindableWrapper("0 OF 9 POLLS REPORTING")
        val voteSubhead = BindableWrapper("WAITING FOR RESULTS...")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val swingHeader = BindableWrapper("SWING SINCE 2015")
        val mapHeader = BindableWrapper("CARDIGAN")
        val leader = BindableWrapper<PartyResult?>(null)
        val winner = BindableWrapper<Candidate?>(null)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = BindableWrapper(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = BindableWrapper(3)
        val panel = candidateVotes(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withLimit(3)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .withResultMap(fixedBinding(shapesByDistrict), selectedDistrict.binding, leader.binding, focus.binding, mapHeader.binding)
                .withWinner(winner.binding)
                .withPctReporting(pctReporting.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "CandidateOthers-1", panel)
        winner.value = lib
        compareRendering("SimpleVoteViewPanel", "CandidateOthers-2", panel)
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = 675
        currentVotes.value = curr
        voteHeader.value = "9 OF 9 POLLS REPORTING"
        voteSubhead.value = "PROJECTION: PC GAIN FROM LIB"
        pctReporting.value = 9.0 / 9
        leader.value = elected(pc.party)
        winner.value = pc
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val pctReporting = BindableWrapper(0.0)
        val header = BindableWrapper("MONTAGUE-KILMUIR")
        val voteHeader = BindableWrapper("0 OF 9 POLLS REPORTING")
        val voteSubhead = BindableWrapper("WAITING FOR RESULTS...")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val swingHeader = BindableWrapper("SWING SINCE 2015")
        val mapHeader = BindableWrapper("CARDIGAN")
        val leader = BindableWrapper<PartyResult?>(null)
        val winner = BindableWrapper<Candidate?>(null)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = BindableWrapper(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = BindableWrapper(3)
        val panel = candidateVotes(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withLimit(3, pc.party, lib.party)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .withResultMap(fixedBinding(shapesByDistrict), selectedDistrict.binding, leader.binding, focus.binding, mapHeader.binding)
                .withWinner(winner.binding)
                .withPctReporting(pctReporting.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "CandidateOthersMandatory-1", panel)
        winner.value = lib
        compareRendering("SimpleVoteViewPanel", "CandidateOthersMandatory-2", panel)
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = 675
        currentVotes.value = curr
        voteHeader.value = "9 OF 9 POLLS REPORTING"
        voteSubhead.value = "PROJECTION: PC GAIN FROM LIB"
        pctReporting.value = 9.0 / 9
        leader.value = elected(pc.party)
        winner.value = pc
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val header = BindableWrapper("SHIPPAGAN-LAM\u00c8QUE-MISCOU")
        val voteHeader = BindableWrapper("OFFICIAL RESULT")
        val voteSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2018")
        val swingHeader = BindableWrapper("SWING SINCE 2018")
        val winner = BindableWrapper(lib)
        val swingPartyOrder = listOf(ndp, grn.party, lib.party, pc.party)
        val panel = candidateVotes(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withWinner(winner.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val header = BindableWrapper("SHIPPAGAN-LAM\u00c8QUE-MISCOU")
        val voteHeader = BindableWrapper("OFFICIAL RESULT")
        val voteSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2018")
        val swingHeader = BindableWrapper("SWING SINCE 2018")
        val winner = BindableWrapper(lib)
        val swingPartyOrder = listOf(ndp, oth.party, lib.party, pc.party)
        val panel = candidateVotes(
                currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withWinner(winner.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val header = BindableWrapper("BUCKINGHAM")
        val voteHeader = BindableWrapper("OFFICIAL RESULT")
        val voteSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2017")
        val changeSubhead = BindableWrapper("NOT APPLICABLE: PREVIOUSLY SPEAKER'S SEAT")
        val swingHeader = BindableWrapper("SWING SINCE 2017")
        val winner = BindableWrapper(con)
        val swingPartyOrder = listOf(lab.party, ld.party, con.party, bxp.party)
        val panel = candidateVotes(currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding, changeSubhead.binding)
                .withWinner(winner.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
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
        val currentVotes = BindableWrapper(curr)
        val header = BindableWrapper("ELECTION 2017: FRANCE DECIDES")
        val voteHeader = BindableWrapper("FIRST ROUND RESULT")
        val voteSubhead = BindableWrapper("")
        val runoff = BindableWrapper<Set<Candidate>?>(null)
        val panel = candidateVotes(currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withRunoff(runoff.binding)
                .withMajorityLine(fixedBinding(true), fixedBinding("50% TO WIN"))
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "CandidateRunoffSingleLine-1", panel)
        runoff.value = setOf(macron, lePen)
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
        val currentVotes = BindableWrapper(topAndOthers(curr, 6, Candidate.OTHERS))
        val header = BindableWrapper("ELECTION 2017: FRANCE DECIDES")
        val voteHeader = BindableWrapper("FIRST ROUND RESULT")
        val voteSubhead = BindableWrapper("")
        val runoff = BindableWrapper<Set<Candidate>?>(null)
        val panel = candidateVotes(currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withRunoff(runoff.binding)
                .withMajorityLine(fixedBinding(true), fixedBinding("50% TO WIN"))
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "CandidateRunoffDualLine-1", panel)
        runoff.value = setOf(macron, lePen)
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val header = BindableWrapper("PRINCE EDWARD ISLAND")
        val voteHeader = BindableWrapper("OPINION POLL RANGE")
        val voteSubhead = BindableWrapper("SINCE ELECTION CALL")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val swingHeader = BindableWrapper("SWING SINCE 2015")
        val mapHeader = BindableWrapper("PEI")
        val swingPartyOrder = listOf(ndp, grn, lib, pc)
        val shapesByDistrict = peiShapesByDistrict()
        val winners: Map<Int, Party> = HashMap()
        val panel = partyRangeVotes(currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .withPartyMap(fixedBinding(shapesByDistrict), fixedBinding(winners), fixedBinding(null), mapHeader.binding)
                .build(header.binding)
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
        val currentVotes = BindableWrapper(curr)
        val previousVotes = BindableWrapper(prev)
        val header = BindableWrapper("MONTAGUE-KILMUIR")
        val voteHeader = BindableWrapper("OFFICIAL RESULT")
        val voteSubhead = BindableWrapper("")
        val changeHeader = BindableWrapper("CHANGE SINCE 2015")
        val swingHeader = BindableWrapper("SWING SINCE 2015")
        val mapHeader = BindableWrapper("CARDIGAN")
        val leader = BindableWrapper<Party?>(null)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = BindableWrapper(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = BindableWrapper(3)
        val panel = candidateVotes(currentVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .withPartyMap(fixedBinding(shapesByDistrict), selectedDistrict.binding, leader.binding, focus.binding, mapHeader.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-1", panel)
        curr[ndp] = 124
        curr[pc] = null
        curr[lib] = null
        curr[grn] = null
        currentVotes.value = curr
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-2", panel)
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = null
        curr[grn] = null
        currentVotes.value = curr
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-3", panel)
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = null
        currentVotes.value = curr
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-4", panel)
        curr[ndp] = 124
        curr[pc] = 1373
        curr[lib] = 785
        curr[grn] = 675
        currentVotes.value = curr
        leader.value = pc.party
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
        val currentVotes = BindableWrapper(LinkedHashMap<Party, Int>())
        val previousVotes = BindableWrapper(LinkedHashMap<Party, Int>())
        val header = BindableWrapper("NORTHERN IRELAND")
        val seatHeader = BindableWrapper("2017 RESULTS")
        val seatSubhead = BindableWrapper<String?>(null)
        val changeHeader = BindableWrapper("NOTIONAL CHANGE SINCE 2016")
        val panel = partyVotes(currentVotes.binding, seatHeader.binding, seatSubhead.binding)
                .withPrev(previousVotes.binding, changeHeader.binding)
                .withClassification({ mapping.getOrDefault(it, others) }, fixedBinding("BY DESIGNATION"))
                .withSwing(compareBy { listOf(nationalists, others, unionists).indexOf(it) }, fixedBinding("FIRST PREFERENCE SWING SINCE 2016"))
                .build(header.binding)
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
        compareRendering("SimpleVoteViewPanel", "PartyClassifications-2", panel)
    }

    @Throws(IOException::class)
    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = MapFrameTest::class.java
                .classLoader
                .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return readShapes(peiMap, "DIST_NO", Int::class.java)
    }
}
