package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Shape

class MixedMemberResultPanelTest {
    @Test
    fun testBasicMMP() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(emptyMap<Party, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val previousPartyVotes = Publisher(emptyMap<Party, Int>())
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
        selectedResult.submit(PartyResult.elected(lib))
        val panel = MixedMemberResultPanel.builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPrevCandidateVotes(previousCandidateVotes, candidateChangeHeader)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withPrevPartyVotes(previousPartyVotes, partyChangeHeader)
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader,
            )
            .build(header)
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("Jesse Reddin Cousins", ndp) to 41,
                Candidate("Mike Gillis", pc) to 865,
                Candidate("Robert Mitchell", lib, true) to 1420,
                Candidate("Amanda Morrison", grn) to 1057,

            ),
        )
        previousCandidateVotes.submit(
            mapOf(
                lib to 1425,
                pc to 1031,
                ndp to 360,
                grn to 295,
            ),
        )
        currentPartyVotes.submit(
            mapOf(
                grn to 1098,
                lib to 1013,
                ndp to 112,
                pc to 822,

            ),
        )
        previousPartyVotes.submit(
            mapOf(
                lib to 1397,
                pc to 1062,
                ndp to 544,
                grn to 426,
            ),
        )
        compareRendering("MixedMemberResultPanel", "Basic", panel)
    }

    @Test
    fun testMMPWithPctReporting() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(emptyMap<Party, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val previousPartyVotes = Publisher(emptyMap<Party, Int>())
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
        val panel = MixedMemberResultPanel.builder()
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
                mapHeader,
            )
            .build(header)
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("Jesse Reddin Cousins", ndp) to 8,
                Candidate("Mike Gillis", pc) to 173,
                Candidate("Robert Mitchell", lib, true) to 284,
                Candidate("Amanda Morrison", grn) to 211,
            ),
        )
        winner.submit(Candidate("Robert Mitchell", lib, true))
        previousCandidateVotes.submit(
            mapOf(
                lib to 1425,
                pc to 1031,
                ndp to 360,
                grn to 295,
            ),
        )
        currentPartyVotes.submit(
            mapOf(
                grn to 110,
                lib to 101,
                ndp to 11,
                pc to 82,
            ),
        )
        previousPartyVotes.submit(
            mapOf(
                lib to 1397,
                pc to 1062,
                ndp to 544,
                grn to 426,
            ),
        )
        candidatePctReporting.submit(0.2)
        partyPctReporting.submit(0.1)
        selectedResult.submit(PartyResult.leading(lib))
        compareRendering("MixedMemberResultPanel", "PctReporting", panel)
    }

    @Test
    fun testMMPWithProgressLabels() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(emptyMap<Party, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val previousPartyVotes = Publisher(emptyMap<Party, Int>())
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
        val panel = MixedMemberResultPanel.builder()
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
                mapHeader,
            )
            .build(header)
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("Jesse Reddin Cousins", ndp) to 8,
                Candidate("Mike Gillis", pc) to 173,
                Candidate("Robert Mitchell", lib, true) to 284,
                Candidate("Amanda Morrison", grn) to 211,
            ),
        )
        winner.submit(Candidate("Robert Mitchell", lib, true))
        previousCandidateVotes.submit(
            mapOf(
                lib to 1425,
                pc to 1031,
                ndp to 360,
                grn to 295,
            ),
        )
        currentPartyVotes.submit(
            mapOf(
                grn to 110,
                lib to 101,
                ndp to 11,
                pc to 82,
            ),
        )
        previousPartyVotes.submit(
            mapOf(
                lib to 1397,
                pc to 1062,
                ndp to 544,
                grn to 426,
            ),
        )
        candidatePctReporting.submit(0.2)
        candidateProgress.submit("20% IN")
        partyPctReporting.submit(0.1)
        partyProgress.submit("10% IN")
        selectedResult.submit(PartyResult.leading(lib))
        compareRendering("MixedMemberResultPanel", "ProgressLabels", panel)
    }

    @Test
    fun testMMPWaiting() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(emptyMap<Party, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val previousPartyVotes = Publisher(emptyMap<Party, Int>())
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
        val panel = MixedMemberResultPanel.builder()
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
                mapHeader,
            )
            .build(header)
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("Jesse Reddin Cousins", ndp) to 0,
                Candidate("Mike Gillis", pc) to 0,
                Candidate("Robert Mitchell", lib, true) to 0,
                Candidate("Amanda Morrison", grn) to 0,
            ),
        )
        previousCandidateVotes.submit(
            mapOf(
                lib to 1425,
                pc to 1031,
                ndp to 360,
                grn to 295,
            ),
        )
        currentPartyVotes.submit(
            mapOf(
                grn to 0,
                lib to 0,
                ndp to 0,
                pc to 0,
            ),
        )
        previousPartyVotes.submit(
            mapOf(
                lib to 1397,
                pc to 1062,
                ndp to 544,
                grn to 426,
            ),
        )
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
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(emptyMap<Party, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val previousPartyVotes = Publisher(emptyMap<Party, Int>())
        val header = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val candidateChangeHeader = Publisher("CANDIDATE CHANGE SINCE 2015")
        val partyHeader = Publisher("PARTY VOTES")
        val partyChangeHeader = Publisher("PARTY CHANGE SINCE 2015")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val selectedShape = Publisher(10)
        val selectedResult = Publisher(PartyResult.leading(lib))
        val candidatePctReporting = Publisher(0.0)
        val partyPctReporting = Publisher(0.0)
        val panel = MixedMemberResultPanel.builder()
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
                mapHeader,
            )
            .build(header)
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("Jesse Reddin Cousins", ndp) to 0,
                Candidate("Mike Gillis", pc) to 8,
                Candidate("Robert Mitchell", lib, true) to 14,
                Candidate("Amanda Morrison", grn) to 11,
            ),
        )
        previousCandidateVotes.submit(
            mapOf(
                lib to 1425,
                pc to 1031,
                ndp to 360,
                grn to 295,
            ),
        )
        currentPartyVotes.submit(
            mapOf(
                grn to 11,
                lib to 14,
                ndp to 0,
                pc to 8,
            ),
        )
        previousPartyVotes.submit(
            mapOf(
                lib to 1397,
                pc to 1062,
                ndp to 544,
                grn to 426,
            ),
        )
        candidatePctReporting.submit(0.01)
        partyPctReporting.submit(0.01)
        compareRendering("MixedMemberResultPanel", "ZeroVotes", panel)
    }

    @Test
    fun testOtherMMP() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(emptyMap<Party, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val previousPartyVotes = Publisher(emptyMap<Party, Int>())
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
        selectedResult.submit(PartyResult.elected(lib))
        val panel = MixedMemberResultPanel.builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPrevCandidateVotes(previousCandidateVotes, candidateChangeHeader)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withPrevPartyVotes(previousPartyVotes, partyChangeHeader)
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader,
            )
            .build(header)
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate.OTHERS to 1106,
                Candidate("Robert Mitchell", lib, true) to 1420,
                Candidate("Amanda Morrison", grn) to 1057,
            ),
        )
        previousCandidateVotes.submit(
            mapOf(
                Party.OTHERS to 1391,
                lib to 1425,
                grn to 295,
            ),
        )
        currentPartyVotes.submit(
            mapOf(
                pc to 1098,
                lib to 1013,
                Party.OTHERS to 1050,
            ),
        )
        previousPartyVotes.submit(
            mapOf(
                lib to 1397,
                pc to 1062,
                Party.OTHERS to 1100,
            ),
        )
        compareRendering("MixedMemberResultPanel", "Other", panel)
    }

    @Test
    fun testMapAdditionalHighlights() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(emptyMap<Party, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val previousPartyVotes = Publisher(emptyMap<Party, Int>())
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
        selectedResult.submit(PartyResult.elected(lib))
        val panel = MixedMemberResultPanel.builder()
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
                mapHeader,
            )
            .build(header)
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("Jesse Reddin Cousins", ndp) to 41,
                Candidate("Mike Gillis", pc) to 865,
                Candidate("Robert Mitchell", lib, true) to 1420,
                Candidate("Amanda Morrison", grn) to 1057,
            ),
        )
        previousCandidateVotes.submit(
            mapOf(
                lib to 1425,
                pc to 1031,
                ndp to 360,
                grn to 295,
            ),
        )
        currentPartyVotes.submit(
            mapOf(
                grn to 1098,
                lib to 1013,
                ndp to 112,
                pc to 822,
            ),
        )
        previousPartyVotes.submit(
            mapOf(
                lib to 1397,
                pc to 1062,
                ndp to 544,
                grn to 426,
            ),
        )
        compareRendering("MixedMemberResultPanel", "MapAdditionalHighlight", panel)
    }

    @Test
    fun testWithoutPrev() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
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
        selectedResult.submit(PartyResult.elected(lib))
        val panel = MixedMemberResultPanel.builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withIncumbentMarker("(MLA)")
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader,
            )
            .build(header)
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("Jesse Reddin Cousins", ndp) to 41,
                Candidate("Mike Gillis", pc) to 865,
                Candidate("Robert Mitchell", lib, true) to 1420,
                Candidate("Amanda Morrison", grn) to 1057,
            ),
        )
        currentPartyVotes.submit(
            mapOf(
                grn to 1098,
                lib to 1013,
                ndp to 112,
                pc to 822,
            ),
        )
        compareRendering("MixedMemberResultPanel", "NoPrev", panel)
    }

    @Test
    fun testSubheadWithoutPrev() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
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
        selectedResult.submit(PartyResult.elected(lib))
        val panel = MixedMemberResultPanel.builder()
            .withCandidateVotes(
                currentCandidateVotes,
                candidateHeader,
                candidateSubhead,
            )
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withIncumbentMarker("(MLA)")
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader,
            )
            .build(header)
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("Jesse Reddin Cousins", ndp) to 41,
                Candidate("Mike Gillis", pc) to 865,
                Candidate("Robert Mitchell", lib, true) to 1420,
                Candidate("Amanda Morrison", grn) to 1057,
            ),
        )
        currentPartyVotes.submit(
            mapOf(
                grn to 1098,
                lib to 1013,
                ndp to 112,
                pc to 822,
            ),
        )
        compareRendering("MixedMemberResultPanel", "NoPrevSubhead", panel)
    }

    @Test
    fun testTickWithoutPrev() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
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
        selectedResult.submit(PartyResult.elected(lib))
        val panel = MixedMemberResultPanel.builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withIncumbentMarker("(MLA)")
            .withWinner(winner)
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader,
            )
            .build(header)
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("Jesse Reddin Cousins", ndp) to 41,
                Candidate("Mike Gillis", pc) to 865,
                Candidate("Robert Mitchell", lib, true) to 1420,
                Candidate("Amanda Morrison", grn) to 1057,
            ),
        )
        winner.submit(Candidate("Robert Mitchell", lib, true))
        currentPartyVotes.submit(
            mapOf(
                grn to 1098,
                lib to 1013,
                ndp to 112,
                pc to 822,
            ),
        )
        compareRendering("MixedMemberResultPanel", "NoPrevTick", panel)
    }

    @Test
    fun testPartyOnlyForCandidateVote() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
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
        selectedResult.submit(PartyResult.elected(lib))
        val panel = MixedMemberResultPanel.builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPartyVotes(currentPartyVotes, partyHeader)
            .withIncumbentMarker("(MLA)")
            .withWinner(winner)
            .withResultMap(
                shapesByDistrict.asOneTimePublisher(),
                selectedShape,
                selectedResult,
                focus,
                mapHeader,
            )
            .build(header)
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("", ndp) to 41,
                Candidate("", pc) to 865,
                Candidate("", lib, true) to 1420,
                Candidate("", grn) to 1057,
            ),
        )
        winner.submit(Candidate("", lib, true))
        currentPartyVotes.submit(
            mapOf(
                grn to 1098,
                lib to 1013,
                ndp to 112,
                pc to 822,
            ),
        )
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
        val currentCandidateVotes = Publisher<Map<Candidate, Int?>>(emptyMap())
        val previousCandidateVotes = Publisher<Map<Party, Int>>(emptyMap())
        val currentPartyVotes = Publisher<Map<Party, Int?>>(emptyMap())
        val previousPartyVotes = Publisher<Map<Party, Int>>(emptyMap())
        val header = Publisher("CITY & EAST")
        val candidateHeader = Publisher("CONSTITUENCY VOTES")
        val candidateChangeHeader = Publisher("CONSTITUENCY CHANGE SINCE 2012")
        val partyHeader = Publisher("AT-LARGE VOTES")
        val partyChangeHeader = Publisher("AT-LARGE CHANGE SINCE 2012")
        val topPartiesWaiting = Publisher(arrayOf(con, lab, ld, grn))
        val panel = MixedMemberResultPanel.builder()
            .withCandidateVotes(currentCandidateVotes, candidateHeader)
            .withPrevCandidateVotes(previousCandidateVotes, candidateChangeHeader)
            .withPartyVotes(
                Aggregators.topAndOthers(currentPartyVotes, 5, Party.OTHERS, topPartiesWaiting),
                partyHeader,
            )
            .withPrevPartyVotes(previousPartyVotes, partyChangeHeader)
            .build(header)
        panel.setSize(1024, 512)
        val currCandVotes = mutableMapOf<Candidate, Int?>(
            Candidate("Elaine Sheila Bagshaw", ld) to null,
            Candidate("Christopher James Chapman", con) to null,
            Candidate("Rachel Collinson", grn) to null,
            Candidate("Unmesh Desai", lab) to null,
            Candidate("Aaron Anthony Jose Hasan D'Souza", app) to null,
            Candidate("Amina May Kay Gichinga", city) to null,
            Candidate("Peter James Harris", ukip) to null,
            Candidate("Rayne Mickail", resp) to null,
        )
        currentCandidateVotes.submit(currCandVotes)
        previousCandidateVotes.submit(
            mapOf(
                lab to 107667,
                bnp to 7031,
                cl to 1108,
                ld to 7351,
                cup to 6774,
                con to 24923,
                grn to 10891,
                fresh to 5243,
            ),
        )
        val currPartyVotes = mutableMapOf<Party, Int?>(
            awp to null,
            bf to null,
            bnp to null,
            cpa to null,
            con to null,
            grn to null,
            lab to null,
            ld to null,
            resp to null,
            house to null,
            ukip to null,
            wep to null,
        )
        currentPartyVotes.submit(currPartyVotes)
        previousPartyVotes.submit(
            mapOf(
                bnp to 5702,
                cpa to 3360,
                con to 25128,
                ed to 1565,
                grn to 11086,
                lab to 108395,
                ld to 6140,
                bf to 749,
                house to 684,
                tusc to 1277,
                ukip to 5966,
                ind to 299 + 1171,
            ),
        )
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
        currentCandidateVotes.submit(
            Aggregators.topAndOthers(currCandVotes, 6, Candidate.OTHERS),
        )
        compareRendering("MixedMemberResultPanel", "Declaration-10", panel)
    }

    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = MixedMemberResultPanelTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return ShapefileReader.readShapes(peiMap, "DIST_NO", Int::class.java)
    }
}
