package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrameTest
import com.joecollins.graphics.screens.generic.MixedMemberResultPanel.Companion.builder
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
import org.junit.Test
import java.awt.Color
import java.awt.Shape

class MixedMemberResultPanelTest {
    @Test
    fun testBasicMMP() {
        val currentCandidateVotes = Publisher(LinkedHashMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(LinkedHashMap<Party, Int>())
        val currentPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val previousPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val header = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val candidateChangeHeader = Publisher("CANDIDATE CHANGE SINCE 2015")
        val partyHeader = Publisher("PARTY VOTES")
        val partyChangeHeader = Publisher("PARTY CHANGE SINCE 2015")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val selectedShape = Publisher(10)
        val selectedResult = Publisher<PartyResult?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        selectedResult.submit(elected(lib))
        val panel = builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPrevCandidateVotes(previousCandidateVotes, candidateChangeHeader)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withPrevPartyVotes(previousPartyVotes, partyChangeHeader)
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader
            )
            .build(header)
        panel.setSize(1024, 512)
        val currCandVotes = LinkedHashMap<Candidate, Int>()
        currCandVotes[Candidate("Jesse Reddin Cousins", ndp)] = 41
        currCandVotes[Candidate("Mike Gillis", pc)] = 865
        currCandVotes[Candidate("Robert Mitchell", lib, true)] = 1420
        currCandVotes[Candidate("Amanda Morrison", grn)] = 1057
        currentCandidateVotes.submit(currCandVotes)
        val prevCandVotes = LinkedHashMap<Party, Int>()
        prevCandVotes[lib] = 1425
        prevCandVotes[pc] = 1031
        prevCandVotes[ndp] = 360
        prevCandVotes[grn] = 295
        previousCandidateVotes.submit(prevCandVotes)
        val currPartyVotes = LinkedHashMap<Party, Int>()
        currPartyVotes[grn] = 1098
        currPartyVotes[lib] = 1013
        currPartyVotes[ndp] = 112
        currPartyVotes[pc] = 822
        currentPartyVotes.submit(currPartyVotes)
        val prevPartyVotes = LinkedHashMap<Party, Int>()
        prevPartyVotes[lib] = 1397
        prevPartyVotes[pc] = 1062
        prevPartyVotes[ndp] = 544
        prevPartyVotes[grn] = 426
        previousPartyVotes.submit(prevPartyVotes)
        compareRendering("MixedMemberResultPanel", "Basic", panel)
    }

    @Test
    fun testMMPWithPctReporting() {
        val currentCandidateVotes = Publisher(LinkedHashMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(LinkedHashMap<Party, Int>())
        val currentPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val previousPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val header = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val candidateChangeHeader = Publisher("CANDIDATE CHANGE SINCE 2015")
        val partyHeader = Publisher("PARTY VOTES")
        val partyChangeHeader = Publisher("PARTY CHANGE SINCE 2015")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val selectedShape = Publisher(10)
        val selectedResult = Publisher<PartyResult?>(null)
        val candidatePctReporting = Publisher(0.0)
        val partyPctReporting = Publisher(0.0)
        val winner = Publisher<Candidate?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val panel = builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPrevCandidateVotes(previousCandidateVotes, candidateChangeHeader)
            .withCandidatePctReporting(candidatePctReporting)
            .withWinner(winner)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withPrevPartyVotes(previousPartyVotes, partyChangeHeader)
            .withPartyPctReporting(partyPctReporting)
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader
            )
            .build(header)
        panel.setSize(1024, 512)
        val currCandVotes = LinkedHashMap<Candidate, Int>()
        currCandVotes[Candidate("Jesse Reddin Cousins", ndp)] = 8
        currCandVotes[Candidate("Mike Gillis", pc)] = 173
        currCandVotes[Candidate("Robert Mitchell", lib, true)] = 284
        currCandVotes[Candidate("Amanda Morrison", grn)] = 211
        currentCandidateVotes.submit(currCandVotes)
        winner.submit(currCandVotes.keys.first { it.party === lib })
        val prevCandVotes = LinkedHashMap<Party, Int>()
        prevCandVotes[lib] = 1425
        prevCandVotes[pc] = 1031
        prevCandVotes[ndp] = 360
        prevCandVotes[grn] = 295
        previousCandidateVotes.submit(prevCandVotes)
        val currPartyVotes = LinkedHashMap<Party, Int>()
        currPartyVotes[grn] = 110
        currPartyVotes[lib] = 101
        currPartyVotes[ndp] = 11
        currPartyVotes[pc] = 82
        currentPartyVotes.submit(currPartyVotes)
        val prevPartyVotes = LinkedHashMap<Party, Int>()
        prevPartyVotes[lib] = 1397
        prevPartyVotes[pc] = 1062
        prevPartyVotes[ndp] = 544
        prevPartyVotes[grn] = 426
        previousPartyVotes.submit(prevPartyVotes)
        candidatePctReporting.submit(0.2)
        partyPctReporting.submit(0.1)
        selectedResult.submit(leading(lib))
        compareRendering("MixedMemberResultPanel", "PctReporting", panel)
    }

    @Test
    fun testMMPWithProgressLabels() {
        val currentCandidateVotes = Publisher(LinkedHashMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(LinkedHashMap<Party, Int>())
        val currentPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val previousPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val header = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val candidateChangeHeader = Publisher("CANDIDATE CHANGE SINCE 2015")
        val partyHeader = Publisher("PARTY VOTES")
        val partyChangeHeader = Publisher("PARTY CHANGE SINCE 2015")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val selectedShape = Publisher(10)
        val selectedResult = Publisher<PartyResult?>(null)
        val candidatePctReporting = Publisher(0.0)
        val partyPctReporting = Publisher(0.0)
        val candidateProgress = Publisher("0% IN")
        val partyProgress = Publisher("0% IN")
        val winner = Publisher<Candidate?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val panel = builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPrevCandidateVotes(previousCandidateVotes, candidateChangeHeader)
            .withCandidatePctReporting(candidatePctReporting)
            .withCandidateProgressLabel(candidateProgress)
            .withWinner(winner)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withPrevPartyVotes(previousPartyVotes, partyChangeHeader)
            .withPartyPctReporting(partyPctReporting)
            .withPartyProgressLabel(partyProgress)
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader
            )
            .build(header)
        panel.setSize(1024, 512)
        val currCandVotes = LinkedHashMap<Candidate, Int>()
        currCandVotes[Candidate("Jesse Reddin Cousins", ndp)] = 8
        currCandVotes[Candidate("Mike Gillis", pc)] = 173
        currCandVotes[Candidate("Robert Mitchell", lib, true)] = 284
        currCandVotes[Candidate("Amanda Morrison", grn)] = 211
        currentCandidateVotes.submit(currCandVotes)
        winner.submit(currCandVotes.keys.first { it.party === lib })
        val prevCandVotes = LinkedHashMap<Party, Int>()
        prevCandVotes[lib] = 1425
        prevCandVotes[pc] = 1031
        prevCandVotes[ndp] = 360
        prevCandVotes[grn] = 295
        previousCandidateVotes.submit(prevCandVotes)
        val currPartyVotes = LinkedHashMap<Party, Int>()
        currPartyVotes[grn] = 110
        currPartyVotes[lib] = 101
        currPartyVotes[ndp] = 11
        currPartyVotes[pc] = 82
        currentPartyVotes.submit(currPartyVotes)
        val prevPartyVotes = LinkedHashMap<Party, Int>()
        prevPartyVotes[lib] = 1397
        prevPartyVotes[pc] = 1062
        prevPartyVotes[ndp] = 544
        prevPartyVotes[grn] = 426
        previousPartyVotes.submit(prevPartyVotes)
        candidatePctReporting.submit(0.2)
        candidateProgress.submit("20% IN")
        partyPctReporting.submit(0.1)
        partyProgress.submit("10% IN")
        selectedResult.submit(leading(lib))
        compareRendering("MixedMemberResultPanel", "ProgressLabels", panel)
    }

    @Test
    fun testMMPWaiting() {
        val currentCandidateVotes = Publisher(LinkedHashMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(LinkedHashMap<Party, Int>())
        val currentPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val previousPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val header = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val candidateChangeHeader = Publisher("CANDIDATE CHANGE SINCE 2015")
        val partyHeader = Publisher("PARTY VOTES")
        val partyChangeHeader = Publisher("PARTY CHANGE SINCE 2015")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val selectedShape = Publisher(10)
        val selectedResult = Publisher<PartyResult?>(null)
        val candidatePctReporting = Publisher(0.0)
        val partyPctReporting = Publisher(0.0)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val panel = builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPrevCandidateVotes(previousCandidateVotes, candidateChangeHeader)
            .withCandidatePctReporting(candidatePctReporting)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withPrevPartyVotes(previousPartyVotes, partyChangeHeader)
            .withPartyPctReporting(partyPctReporting)
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader
            )
            .build(header)
        panel.setSize(1024, 512)
        val currCandVotes = LinkedHashMap<Candidate, Int>()
        currCandVotes[Candidate("Jesse Reddin Cousins", ndp)] = 0
        currCandVotes[Candidate("Mike Gillis", pc)] = 0
        currCandVotes[Candidate("Robert Mitchell", lib, true)] = 0
        currCandVotes[Candidate("Amanda Morrison", grn)] = 0
        currentCandidateVotes.submit(currCandVotes)
        val prevCandVotes = LinkedHashMap<Party, Int>()
        prevCandVotes[lib] = 1425
        prevCandVotes[pc] = 1031
        prevCandVotes[ndp] = 360
        prevCandVotes[grn] = 295
        previousCandidateVotes.submit(prevCandVotes)
        val currPartyVotes = LinkedHashMap<Party, Int>()
        currPartyVotes[grn] = 0
        currPartyVotes[lib] = 0
        currPartyVotes[ndp] = 0
        currPartyVotes[pc] = 0
        currentPartyVotes.submit(currPartyVotes)
        val prevPartyVotes = LinkedHashMap<Party, Int>()
        prevPartyVotes[lib] = 1397
        prevPartyVotes[pc] = 1062
        prevPartyVotes[ndp] = 544
        prevPartyVotes[grn] = 426
        previousPartyVotes.submit(prevPartyVotes)
        candidatePctReporting.submit(0.0)
        partyPctReporting.submit(0.0)
        compareRendering("MixedMemberResultPanel", "Waiting", panel)
    }

    @Test
    fun testZeroVotesForSingleEntry() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val currentCandidateVotes = Publisher(LinkedHashMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(LinkedHashMap<Party, Int>())
        val currentPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val previousPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val header = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val candidateChangeHeader = Publisher("CANDIDATE CHANGE SINCE 2015")
        val partyHeader = Publisher("PARTY VOTES")
        val partyChangeHeader = Publisher("PARTY CHANGE SINCE 2015")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val selectedShape = Publisher(10)
        val selectedResult = Publisher(leading(lib))
        val candidatePctReporting = Publisher(0.0)
        val partyPctReporting = Publisher(0.0)
        val panel = builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPrevCandidateVotes(previousCandidateVotes, candidateChangeHeader)
            .withCandidatePctReporting(candidatePctReporting)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withPrevPartyVotes(previousPartyVotes, partyChangeHeader)
            .withPartyPctReporting(partyPctReporting)
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader
            )
            .build(header)
        panel.setSize(1024, 512)
        val currCandVotes = LinkedHashMap<Candidate, Int>()
        currCandVotes[Candidate("Jesse Reddin Cousins", ndp)] = 0
        currCandVotes[Candidate("Mike Gillis", pc)] = 8
        currCandVotes[Candidate("Robert Mitchell", lib, true)] = 14
        currCandVotes[Candidate("Amanda Morrison", grn)] = 11
        currentCandidateVotes.submit(currCandVotes)
        val prevCandVotes = LinkedHashMap<Party, Int>()
        prevCandVotes[lib] = 1425
        prevCandVotes[pc] = 1031
        prevCandVotes[ndp] = 360
        prevCandVotes[grn] = 295
        previousCandidateVotes.submit(prevCandVotes)
        val currPartyVotes = LinkedHashMap<Party, Int>()
        currPartyVotes[grn] = 11
        currPartyVotes[lib] = 14
        currPartyVotes[ndp] = 0
        currPartyVotes[pc] = 8
        currentPartyVotes.submit(currPartyVotes)
        val prevPartyVotes = LinkedHashMap<Party, Int>()
        prevPartyVotes[lib] = 1397
        prevPartyVotes[pc] = 1062
        prevPartyVotes[ndp] = 544
        prevPartyVotes[grn] = 426
        previousPartyVotes.submit(prevPartyVotes)
        candidatePctReporting.submit(0.01)
        partyPctReporting.submit(0.01)
        compareRendering("MixedMemberResultPanel", "ZeroVotes", panel)
    }

    @Test
    fun testOtherMMP() {
        val currentCandidateVotes = Publisher(LinkedHashMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(LinkedHashMap<Party, Int>())
        val currentPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val previousPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val header = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val candidateChangeHeader = Publisher("CANDIDATE CHANGE SINCE 2015")
        val partyHeader = Publisher("PARTY VOTES")
        val partyChangeHeader = Publisher("PARTY CHANGE SINCE 2015")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val selectedShape = Publisher(10)
        val selectedResult = Publisher<PartyResult?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        selectedResult.submit(elected(lib))
        val panel = builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPrevCandidateVotes(previousCandidateVotes, candidateChangeHeader)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withPrevPartyVotes(previousPartyVotes, partyChangeHeader)
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader
            )
            .build(header)
        panel.setSize(1024, 512)
        val currCandVotes = LinkedHashMap<Candidate, Int>()
        currCandVotes[Candidate.OTHERS] = 1106
        currCandVotes[Candidate("Robert Mitchell", lib, true)] = 1420
        currCandVotes[Candidate("Amanda Morrison", grn)] = 1057
        currentCandidateVotes.submit(currCandVotes)
        val prevCandVotes = LinkedHashMap<Party, Int>()
        prevCandVotes[Party.OTHERS] = 1391
        prevCandVotes[lib] = 1425
        prevCandVotes[grn] = 295
        previousCandidateVotes.submit(prevCandVotes)
        val currPartyVotes = LinkedHashMap<Party, Int>()
        currPartyVotes[pc] = 1098
        currPartyVotes[lib] = 1013
        currPartyVotes[Party.OTHERS] = 1050
        currentPartyVotes.submit(currPartyVotes)
        val prevPartyVotes = LinkedHashMap<Party, Int>()
        prevPartyVotes[lib] = 1397
        prevPartyVotes[pc] = 1062
        prevPartyVotes[Party.OTHERS] = 1100
        previousPartyVotes.submit(prevPartyVotes)
        compareRendering("MixedMemberResultPanel", "Other", panel)
    }

    @Test
    fun testMapAdditionalHighlights() {
        val currentCandidateVotes = Publisher(LinkedHashMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(LinkedHashMap<Party, Int>())
        val currentPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val previousPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val header = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val candidateChangeHeader = Publisher("CANDIDATE CHANGE SINCE 2015")
        val partyHeader = Publisher("PARTY VOTES")
        val partyChangeHeader = Publisher("PARTY CHANGE SINCE 2015")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val additionalHighlight = Publisher(listOf(9))
        val selectedShape = Publisher(10)
        val selectedResult = Publisher<PartyResult?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        selectedResult.submit(elected(lib))
        val panel = builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPrevCandidateVotes(previousCandidateVotes, candidateChangeHeader)
            .withIncumbentMarker("(MLA)")
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withPrevPartyVotes(previousPartyVotes, partyChangeHeader)
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                additionalHighlight,
                mapHeader
            )
            .build(header)
        panel.setSize(1024, 512)
        val currCandVotes = LinkedHashMap<Candidate, Int>()
        currCandVotes[Candidate("Jesse Reddin Cousins", ndp)] = 41
        currCandVotes[Candidate("Mike Gillis", pc)] = 865
        currCandVotes[Candidate("Robert Mitchell", lib, true)] = 1420
        currCandVotes[Candidate("Amanda Morrison", grn)] = 1057
        currentCandidateVotes.submit(currCandVotes)
        val prevCandVotes = LinkedHashMap<Party, Int>()
        prevCandVotes[lib] = 1425
        prevCandVotes[pc] = 1031
        prevCandVotes[ndp] = 360
        prevCandVotes[grn] = 295
        previousCandidateVotes.submit(prevCandVotes)
        val currPartyVotes = LinkedHashMap<Party, Int>()
        currPartyVotes[grn] = 1098
        currPartyVotes[lib] = 1013
        currPartyVotes[ndp] = 112
        currPartyVotes[pc] = 822
        currentPartyVotes.submit(currPartyVotes)
        val prevPartyVotes = LinkedHashMap<Party, Int>()
        prevPartyVotes[lib] = 1397
        prevPartyVotes[pc] = 1062
        prevPartyVotes[ndp] = 544
        prevPartyVotes[grn] = 426
        previousPartyVotes.submit(prevPartyVotes)
        compareRendering("MixedMemberResultPanel", "MapAdditionalHighlight", panel)
    }

    @Test
    fun testWithoutPrev() {
        val currentCandidateVotes = Publisher(LinkedHashMap<Candidate, Int>())
        val currentPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val header = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val partyHeader = Publisher("PARTY VOTES")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val selectedShape = Publisher(10)
        val selectedResult = Publisher<PartyResult?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        selectedResult.submit(elected(lib))
        val panel = builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withIncumbentMarker("(MLA)")
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader
            )
            .build(header)
        panel.setSize(1024, 512)
        val currCandVotes = LinkedHashMap<Candidate, Int>()
        currCandVotes[Candidate("Jesse Reddin Cousins", ndp)] = 41
        currCandVotes[Candidate("Mike Gillis", pc)] = 865
        currCandVotes[Candidate("Robert Mitchell", lib, true)] = 1420
        currCandVotes[Candidate("Amanda Morrison", grn)] = 1057
        currentCandidateVotes.submit(currCandVotes)
        val currPartyVotes = LinkedHashMap<Party, Int>()
        currPartyVotes[grn] = 1098
        currPartyVotes[lib] = 1013
        currPartyVotes[ndp] = 112
        currPartyVotes[pc] = 822
        currentPartyVotes.submit(currPartyVotes)
        compareRendering("MixedMemberResultPanel", "NoPrev", panel)
    }

    @Test
    fun testSubheadWithoutPrev() {
        val currentCandidateVotes = Publisher(LinkedHashMap<Candidate, Int>())
        val currentPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val header = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val candidateSubhead = Publisher("LIB WIN IN 2015")
        val partyHeader = Publisher("PARTY VOTES")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val selectedShape = Publisher(10)
        val selectedResult = Publisher<PartyResult?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        selectedResult.submit(elected(lib))
        val panel = builder()
            .withCandidateVotes(
                currentCandidateVotes,
                candidateHeader,
                candidateSubhead
            )
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withIncumbentMarker("(MLA)")
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader
            )
            .build(header)
        panel.setSize(1024, 512)
        val currCandVotes = LinkedHashMap<Candidate, Int>()
        currCandVotes[Candidate("Jesse Reddin Cousins", ndp)] = 41
        currCandVotes[Candidate("Mike Gillis", pc)] = 865
        currCandVotes[Candidate("Robert Mitchell", lib, true)] = 1420
        currCandVotes[Candidate("Amanda Morrison", grn)] = 1057
        currentCandidateVotes.submit(currCandVotes)
        val currPartyVotes = LinkedHashMap<Party, Int>()
        currPartyVotes[grn] = 1098
        currPartyVotes[lib] = 1013
        currPartyVotes[ndp] = 112
        currPartyVotes[pc] = 822
        currentPartyVotes.submit(currPartyVotes)
        compareRendering("MixedMemberResultPanel", "NoPrevSubhead", panel)
    }

    @Test
    fun testTickWithoutPrev() {
        val currentCandidateVotes = Publisher(LinkedHashMap<Candidate, Int>())
        val currentPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val header = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val partyHeader = Publisher("PARTY VOTES")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val selectedShape = Publisher(10)
        val selectedResult = Publisher<PartyResult?>(null)
        val winner = Publisher<Candidate?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        selectedResult.submit(elected(lib))
        val panel = builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withIncumbentMarker("(MLA)")
            .withWinner(winner)
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader
            )
            .build(header)
        panel.setSize(1024, 512)
        val currCandVotes = LinkedHashMap<Candidate, Int>()
        currCandVotes[Candidate("Jesse Reddin Cousins", ndp)] = 41
        currCandVotes[Candidate("Mike Gillis", pc)] = 865
        currCandVotes[Candidate("Robert Mitchell", lib, true)] = 1420
        currCandVotes[Candidate("Amanda Morrison", grn)] = 1057
        currentCandidateVotes.submit(currCandVotes)
        winner.submit(currCandVotes.keys.first { it.party === lib })
        val currPartyVotes = LinkedHashMap<Party, Int>()
        currPartyVotes[grn] = 1098
        currPartyVotes[lib] = 1013
        currPartyVotes[ndp] = 112
        currPartyVotes[pc] = 822
        currentPartyVotes.submit(currPartyVotes)
        compareRendering("MixedMemberResultPanel", "NoPrevTick", panel)
    }

    @Test
    fun testPartyOnlyForCandidateVote() {
        val currentCandidateVotes = Publisher(LinkedHashMap<Candidate, Int>())
        val currentPartyVotes = Publisher(LinkedHashMap<Party, Int>())
        val header = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val partyHeader = Publisher("PARTY VOTES")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val selectedShape = Publisher(10)
        val selectedResult = Publisher<PartyResult?>(null)
        val winner = Publisher<Candidate?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        selectedResult.submit(elected(lib))
        val panel = builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withIncumbentMarker("(MLA)")
            .withWinner(winner)
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader
            )
            .build(header)
        panel.setSize(1024, 512)
        val currCandVotes = LinkedHashMap<Candidate, Int>()
        currCandVotes[Candidate("", ndp)] = 41
        currCandVotes[Candidate("", pc)] = 865
        currCandVotes[Candidate("", lib, true)] = 1420
        currCandVotes[Candidate("", grn)] = 1057
        currentCandidateVotes.submit(currCandVotes)
        winner.submit(currCandVotes.keys.first { it.party === lib })
        val currPartyVotes = LinkedHashMap<Party, Int>()
        currPartyVotes[grn] = 1098
        currPartyVotes[lib] = 1013
        currPartyVotes[ndp] = 112
        currPartyVotes[pc] = 822
        currentPartyVotes.submit(currPartyVotes)
        compareRendering("MixedMemberResultPanel", "PartyOnlyForCandidateVotes", panel)
    }

    @Test
    fun testDeclarationInProgress() {
        val lab = Party("Labour", "LAB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val con = Party("Conservative", "CON", Color.BLUE)
        val ld = Party("Liberal Democrats", "LD", Color.ORANGE)
        val ukip = Party("UK Independency Party", "UKIP", Color.MAGENTA.darker())
        val resp = Party("Respect", "RESP", Color.RED.darker())
        val wep = Party("Women's Equality Party", "WEP", Color.CYAN.darker())
        val bnp = Party("British National Party", "BNP", Color.BLUE.darker())
        val cpa = Party("Christian People's Alliance", "CPA", Color.MAGENTA)
        val bf = Party("Britain First", "BF", Color.BLUE.darker())
        val house = Party("The House Party", "HOUSE", Color.GRAY)
        val awp = Party("Animal Welfare Party", "AWP", Color.GRAY)
        val app = Party("All People's Party", "APP", Color.GRAY)
        val city = Party("Take Back The City", "CITY", Color.GRAY)
        val cl = Party("Communist League", "CL", Color.GRAY)
        val cup = Party("Communities United Party", "CUP", Color.GRAY)
        val fresh = Party("Fresh Choice for London", "FRESH", Color.GRAY)
        val ed = Party("English Democrats", "ED", Color.GRAY)
        val tusc = Party("Trade Unionist and Socialist Coalition", "TUSC", Color.GRAY)
        val ind = Party("Independent", "IND", Color.GRAY)
        val currentCandidateVotes = Publisher<Map<Candidate, Int?>>(LinkedHashMap())
        val previousCandidateVotes = Publisher<Map<Party, Int>>(LinkedHashMap())
        val currentPartyVotes = Publisher<Map<Party, Int?>>(LinkedHashMap())
        val previousPartyVotes = Publisher<Map<Party, Int>>(LinkedHashMap())
        val header = Publisher("CITY & EAST")
        val candidateHeader = Publisher("CONSTITUENCY VOTES")
        val candidateChangeHeader = Publisher("CONSTITUENCY CHANGE SINCE 2012")
        val partyHeader = Publisher("AT-LARGE VOTES")
        val partyChangeHeader = Publisher("AT-LARGE CHANGE SINCE 2012")
        val topPartiesWaiting = Publisher(arrayOf(con, lab, ld, grn))
        val panel = builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPrevCandidateVotes(previousCandidateVotes, candidateChangeHeader)
            .withPartyVotes(
                topAndOthers(currentPartyVotes, 5, Party.OTHERS, topPartiesWaiting),
                partyHeader
            )
            .withPrevPartyVotes(previousPartyVotes, partyChangeHeader)
            .build(header)
        panel.setSize(1024, 512)
        val currCandVotes = LinkedHashMap<Candidate, Int?>()
        currCandVotes[Candidate("Elaine Sheila Bagshaw", ld)] = null
        currCandVotes[Candidate("Christopher James Chapman", con)] = null
        currCandVotes[Candidate("Rachel Collinson", grn)] = null
        currCandVotes[Candidate("Unmesh Desai", lab)] = null
        currCandVotes[Candidate("Aaron Anthony Jose Hasan D'Souza", app)] = null
        currCandVotes[Candidate("Amina May Kay Gichinga", city)] = null
        currCandVotes[Candidate("Peter James Harris", ukip)] = null
        currCandVotes[Candidate("Rayne Mickail", resp)] = null
        currentCandidateVotes.submit(currCandVotes)
        val prevCandVotes = LinkedHashMap<Party, Int>()
        prevCandVotes[lab] = 107667
        prevCandVotes[bnp] = 7031
        prevCandVotes[cl] = 1108
        prevCandVotes[ld] = 7351
        prevCandVotes[cup] = 6774
        prevCandVotes[con] = 24923
        prevCandVotes[grn] = 10891
        prevCandVotes[fresh] = 5243
        previousCandidateVotes.submit(prevCandVotes)
        val currPartyVotes = LinkedHashMap<Party, Int?>()
        currPartyVotes[awp] = null
        currPartyVotes[bf] = null
        currPartyVotes[bnp] = null
        currPartyVotes[cpa] = null
        currPartyVotes[con] = null
        currPartyVotes[grn] = null
        currPartyVotes[lab] = null
        currPartyVotes[ld] = null
        currPartyVotes[resp] = null
        currPartyVotes[house] = null
        currPartyVotes[ukip] = null
        currPartyVotes[wep] = null
        currentPartyVotes.submit(currPartyVotes)
        val prevPartyVotes = LinkedHashMap<Party, Int>()
        prevPartyVotes[bnp] = 5702
        prevPartyVotes[cpa] = 3360
        prevPartyVotes[con] = 25128
        prevPartyVotes[ed] = 1565
        prevPartyVotes[grn] = 11086
        prevPartyVotes[lab] = 108395
        prevPartyVotes[ld] = 6140
        prevPartyVotes[bf] = 749
        prevPartyVotes[house] = 684
        prevPartyVotes[tusc] = 1277
        prevPartyVotes[ukip] = 5966
        prevPartyVotes[ind] = 299 + 1171
        previousPartyVotes.submit(prevPartyVotes)
        compareRendering("MixedMemberResultPanel", "Declaration-1", panel)
        currCandVotes[Candidate("Elaine Sheila Bagshaw", ld)] = 10714
        currentCandidateVotes.submit(currCandVotes)
        compareRendering("MixedMemberResultPanel", "Declaration-2", panel)
        currCandVotes[Candidate("Christopher James Chapman", con)] = 32546
        currCandVotes[Candidate("Rachel Collinson", grn)] = 18766
        currCandVotes[Candidate("Unmesh Desai", lab)] = 122175
        currentCandidateVotes.submit(currCandVotes)
        compareRendering("MixedMemberResultPanel", "Declaration-3", panel)
        currCandVotes[Candidate("Aaron Anthony Jose Hasan D'Souza", app)] = 1009
        currCandVotes[Candidate("Amina May Kay Gichinga", city)] = 1368
        currCandVotes[Candidate("Peter James Harris", ukip)] = 18071
        currCandVotes[Candidate("Rayne Mickail", resp)] = 6772
        currentCandidateVotes.submit(currCandVotes)
        compareRendering("MixedMemberResultPanel", "Declaration-4", panel)
        currPartyVotes[awp] = 1738
        currentPartyVotes.submit(currPartyVotes)
        topPartiesWaiting.submit(arrayOf(con, lab, ld))
        compareRendering("MixedMemberResultPanel", "Declaration-5", panel)
        currPartyVotes[bf] = 3591
        currPartyVotes[bnp] = 1828
        currPartyVotes[ld] = 7799
        currentPartyVotes.submit(currPartyVotes)
        topPartiesWaiting.submit(emptyArray())
        compareRendering("MixedMemberResultPanel", "Declaration-6", panel)
        currPartyVotes[cpa] = 2660
        currPartyVotes[con] = 30424
        currentPartyVotes.submit(currPartyVotes)
        compareRendering("MixedMemberResultPanel", "Declaration-7", panel)
        currPartyVotes[grn] = 14151
        currPartyVotes[lab] = 121871
        currentPartyVotes.submit(currPartyVotes)
        compareRendering("MixedMemberResultPanel", "Declaration-8", panel)
        currPartyVotes[resp] = 6784
        currPartyVotes[house] = 858
        currPartyVotes[ukip] = 14123
        currPartyVotes[wep] = 5718
        currentPartyVotes.submit(currPartyVotes)
        compareRendering("MixedMemberResultPanel", "Declaration-9", panel)
        currentCandidateVotes.submit(topAndOthers(currCandVotes, 6, Candidate.OTHERS))
        compareRendering("MixedMemberResultPanel", "Declaration-10", panel)
    }

    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = MapFrameTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return readShapes(peiMap, "DIST_NO", Int::class.java)
    }
}
