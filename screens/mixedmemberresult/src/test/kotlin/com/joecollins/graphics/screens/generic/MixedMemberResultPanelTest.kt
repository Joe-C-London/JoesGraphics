package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.MixedMemberResultPanel.Companion.convertToPartyOrCandidateForMixedMember
import com.joecollins.graphics.screens.generic.SingleResultMap.Companion.createSingleResultMap
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCandidate
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
        val title = Publisher("CHARLOTTETOWN-WINSLOE")
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
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = currentCandidateVotes
                header = candidateHeader
            },
            candidateChange = {
                prevVotes = previousCandidateVotes
                header = candidateChangeHeader
            },
            partyVotes = {
                votes = currentPartyVotes.convertToPartyOrCandidateForMixedMember()
                header = partyHeader
            },
            partyChange = {
                prevVotes = previousPartyVotes
                header = partyChangeHeader
            },
            map = createSingleResultMap {
                this.shapes = shapesByDistrict.asOneTimePublisher()
                this.selectedShape = selectedShape
                this.leader = selectedResult
                this.focus = focus
                this.header = mapHeader
            },
            title = title,
        )
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
        assertPublishes(
            panel.altText,
            """
            CHARLOTTETOWN-WINSLOE
            
            CANDIDATE VOTES (CANDIDATE CHANGE SINCE 2015)
            ROBERT MITCHELL (LIB): 1,420 (42.0%, -3.8%)
            AMANDA MORRISON (GRN): 1,057 (31.2%, +21.8%)
            MIKE GILLIS (PC): 865 (25.6%, -7.6%)
            JESSE REDDIN COUSINS (NDP): 41 (1.2%, -10.4%)
            
            PARTY VOTES (PARTY CHANGE SINCE 2015)
            GREEN: 1,098 (36.1%, +23.6%)
            LIBERAL: 1,013 (33.3%, -7.5%)
            PROGRESSIVE CONSERVATIVE: 822 (27.0%, -4.0%)
            NEW DEMOCRATIC PARTY: 112 (3.7%, -12.2%)
            """.trimIndent(),
        )
    }

    @Test
    fun testMMPWithPctReporting() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(emptyMap<Party, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val previousPartyVotes = Publisher(emptyMap<Party, Int>())
        val title = Publisher("CHARLOTTETOWN-WINSLOE")
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
        val candidateWinner = Publisher<Candidate?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = currentCandidateVotes
                header = candidateHeader
                winner = candidateWinner
                pctReporting = candidatePctReporting
            },
            candidateChange = {
                prevVotes = previousCandidateVotes
                header = candidateChangeHeader
            },
            partyVotes = {
                votes = currentPartyVotes.convertToPartyOrCandidateForMixedMember()
                header = partyHeader
                pctReporting = partyPctReporting
            },
            partyChange = {
                prevVotes = previousPartyVotes
                header = partyChangeHeader
            },
            map = createSingleResultMap {
                this.shapes = shapesByDistrict.asOneTimePublisher()
                this.selectedShape = selectedShape
                this.leader = selectedResult
                this.focus = focus
                this.header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("Jesse Reddin Cousins", ndp) to 8,
                Candidate("Mike Gillis", pc) to 173,
                Candidate("Robert Mitchell", lib, true) to 284,
                Candidate("Amanda Morrison", grn) to 211,
            ),
        )
        candidateWinner.submit(Candidate("Robert Mitchell", lib, true))
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
        assertPublishes(
            panel.altText,
            """
            CHARLOTTETOWN-WINSLOE
            
            CANDIDATE VOTES (CANDIDATE CHANGE SINCE 2015)
            ROBERT MITCHELL (LIB): 284 (42.0%, -3.8%) WINNER
            AMANDA MORRISON (GRN): 211 (31.2%, +21.7%)
            MIKE GILLIS (PC): 173 (25.6%, -7.5%)
            JESSE REDDIN COUSINS (NDP): 8 (1.2%, -10.4%)
            
            PARTY VOTES (PARTY CHANGE SINCE 2015)
            GREEN: 110 (36.2%, +23.8%)
            LIBERAL: 101 (33.2%, -7.5%)
            PROGRESSIVE CONSERVATIVE: 82 (27.0%, -4.0%)
            NEW DEMOCRATIC PARTY: 11 (3.6%, -12.2%)
            """.trimIndent(),
        )
    }

    @Test
    fun testMMPWithProgressLabels() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(emptyMap<Party, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val previousPartyVotes = Publisher(emptyMap<Party, Int>())
        val title = Publisher("CHARLOTTETOWN-WINSLOE")
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
        val candidateWinner = Publisher<Candidate?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = currentCandidateVotes
                header = candidateHeader
                winner = candidateWinner
                pctReporting = candidatePctReporting
                progressLabel = candidateProgress
            },
            candidateChange = {
                prevVotes = previousCandidateVotes
                header = candidateChangeHeader
            },
            partyVotes = {
                votes = currentPartyVotes.convertToPartyOrCandidateForMixedMember()
                header = partyHeader
                pctReporting = partyPctReporting
                progressLabel = partyProgress
            },
            partyChange = {
                prevVotes = previousPartyVotes
                header = partyChangeHeader
            },
            map = createSingleResultMap {
                this.shapes = shapesByDistrict.asOneTimePublisher()
                this.selectedShape = selectedShape
                this.leader = selectedResult
                this.focus = focus
                this.header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("Jesse Reddin Cousins", ndp) to 8,
                Candidate("Mike Gillis", pc) to 173,
                Candidate("Robert Mitchell", lib, true) to 284,
                Candidate("Amanda Morrison", grn) to 211,
            ),
        )
        candidateWinner.submit(Candidate("Robert Mitchell", lib, true))
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
        assertPublishes(
            panel.altText,
            """
            CHARLOTTETOWN-WINSLOE
            
            CANDIDATE VOTES (CANDIDATE CHANGE SINCE 2015) [20% IN]
            ROBERT MITCHELL (LIB): 284 (42.0%, -3.8%) WINNER
            AMANDA MORRISON (GRN): 211 (31.2%, +21.7%)
            MIKE GILLIS (PC): 173 (25.6%, -7.5%)
            JESSE REDDIN COUSINS (NDP): 8 (1.2%, -10.4%)
            
            PARTY VOTES (PARTY CHANGE SINCE 2015) [10% IN]
            GREEN: 110 (36.2%, +23.8%)
            LIBERAL: 101 (33.2%, -7.5%)
            PROGRESSIVE CONSERVATIVE: 82 (27.0%, -4.0%)
            NEW DEMOCRATIC PARTY: 11 (3.6%, -12.2%)
            """.trimIndent(),
        )
    }

    @Test
    fun testMMPWaiting() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(emptyMap<Party, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val previousPartyVotes = Publisher(emptyMap<Party, Int>())
        val title = Publisher("CHARLOTTETOWN-WINSLOE")
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
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = currentCandidateVotes
                header = candidateHeader
                pctReporting = candidatePctReporting
            },
            candidateChange = {
                prevVotes = previousCandidateVotes
                header = candidateChangeHeader
            },
            partyVotes = {
                votes = currentPartyVotes.convertToPartyOrCandidateForMixedMember()
                header = partyHeader
                pctReporting = partyPctReporting
            },
            partyChange = {
                prevVotes = previousPartyVotes
                header = partyChangeHeader
            },
            map = createSingleResultMap {
                this.shapes = shapesByDistrict.asOneTimePublisher()
                this.selectedShape = selectedShape
                this.leader = selectedResult
                this.focus = focus
                this.header = mapHeader
            },
            title = title,
        )
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
        assertPublishes(
            panel.altText,
            """
            CHARLOTTETOWN-WINSLOE
            
            CANDIDATE VOTES (CANDIDATE CHANGE SINCE 2015)
            JESSE REDDIN COUSINS (NDP): WAITING...
            MIKE GILLIS (PC): WAITING...
            ROBERT MITCHELL (LIB): WAITING...
            AMANDA MORRISON (GRN): WAITING...
            
            PARTY VOTES (PARTY CHANGE SINCE 2015)
            GREEN: WAITING...
            LIBERAL: WAITING...
            NEW DEMOCRATIC PARTY: WAITING...
            PROGRESSIVE CONSERVATIVE: WAITING...
            """.trimIndent(),
        )
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
        val title = Publisher("CHARLOTTETOWN-WINSLOE")
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
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = currentCandidateVotes
                header = candidateHeader
                pctReporting = candidatePctReporting
            },
            candidateChange = {
                prevVotes = previousCandidateVotes
                header = candidateChangeHeader
            },
            partyVotes = {
                votes = currentPartyVotes.convertToPartyOrCandidateForMixedMember()
                header = partyHeader
                pctReporting = partyPctReporting
            },
            partyChange = {
                prevVotes = previousPartyVotes
                header = partyChangeHeader
            },
            map = createSingleResultMap {
                this.shapes = shapesByDistrict.asOneTimePublisher()
                this.selectedShape = selectedShape
                this.leader = selectedResult
                this.focus = focus
                this.header = mapHeader
            },
            title = title,
        )
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
        assertPublishes(
            panel.altText,
            """
            CHARLOTTETOWN-WINSLOE
            
            CANDIDATE VOTES (CANDIDATE CHANGE SINCE 2015)
            ROBERT MITCHELL (LIB): 14 (42.4%, -3.4%)
            AMANDA MORRISON (GRN): 11 (33.3%, +23.9%)
            MIKE GILLIS (PC): 8 (24.2%, -8.9%)
            JESSE REDDIN COUSINS (NDP): 0 (0.0%, -11.6%)
            
            PARTY VOTES (PARTY CHANGE SINCE 2015)
            LIBERAL: 14 (42.4%, +1.7%)
            GREEN: 11 (33.3%, +20.9%)
            PROGRESSIVE CONSERVATIVE: 8 (24.2%, -6.7%)
            NEW DEMOCRATIC PARTY: 0 (0.0%, -15.9%)
            """.trimIndent(),
        )
    }

    @Test
    fun testOtherMMP() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(emptyMap<Party, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val previousPartyVotes = Publisher(emptyMap<Party, Int>())
        val title = Publisher("CHARLOTTETOWN-WINSLOE")
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
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = currentCandidateVotes
                header = candidateHeader
            },
            candidateChange = {
                prevVotes = previousCandidateVotes
                header = candidateChangeHeader
            },
            partyVotes = {
                votes = currentPartyVotes.convertToPartyOrCandidateForMixedMember()
                header = partyHeader
            },
            partyChange = {
                prevVotes = previousPartyVotes
                header = partyChangeHeader
            },
            map = createSingleResultMap {
                this.shapes = shapesByDistrict.asOneTimePublisher()
                this.selectedShape = selectedShape
                this.leader = selectedResult
                this.focus = focus
                this.header = mapHeader
            },
            title = title,
        )
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
        assertPublishes(
            panel.altText,
            """
            CHARLOTTETOWN-WINSLOE
            
            CANDIDATE VOTES (CANDIDATE CHANGE SINCE 2015)
            ROBERT MITCHELL (LIB): 1,420 (39.6%, -6.2%)
            AMANDA MORRISON (GRN): 1,057 (29.5%, +20.0%)
            OTHERS: 1,106 (30.9%, -13.8%)
            
            PARTY VOTES (PARTY CHANGE SINCE 2015)
            PROGRESSIVE CONSERVATIVE: 1,098 (34.7%, +4.9%)
            LIBERAL: 1,013 (32.0%, -7.2%)
            OTHERS: 1,050 (33.2%, +2.3%)
            """.trimIndent(),
        )
    }

    @Test
    fun testMapAdditionalHighlights() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(emptyMap<Party, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val previousPartyVotes = Publisher(emptyMap<Party, Int>())
        val title = Publisher("CHARLOTTETOWN-WINSLOE")
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
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = currentCandidateVotes
                header = candidateHeader
                incumbentMarker = "MLA"
            },
            candidateChange = {
                prevVotes = previousCandidateVotes
                header = candidateChangeHeader
            },
            partyVotes = {
                votes = currentPartyVotes.convertToPartyOrCandidateForMixedMember()
                header = partyHeader
            },
            partyChange = {
                prevVotes = previousPartyVotes
                header = partyChangeHeader
            },
            map = createSingleResultMap {
                this.shapes = shapesByDistrict.asOneTimePublisher()
                this.selectedShape = selectedShape
                this.leader = selectedResult
                this.focus = focus
                this.additionalHighlight = additionalHighlight
                this.header = mapHeader
            },
            title = title,
        )
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
        assertPublishes(
            panel.altText,
            """
            CHARLOTTETOWN-WINSLOE
            
            CANDIDATE VOTES (CANDIDATE CHANGE SINCE 2015)
            ROBERT MITCHELL [MLA] (LIB): 1,420 (42.0%, -3.8%)
            AMANDA MORRISON (GRN): 1,057 (31.2%, +21.8%)
            MIKE GILLIS (PC): 865 (25.6%, -7.6%)
            JESSE REDDIN COUSINS (NDP): 41 (1.2%, -10.4%)
            
            PARTY VOTES (PARTY CHANGE SINCE 2015)
            GREEN: 1,098 (36.1%, +23.6%)
            LIBERAL: 1,013 (33.3%, -7.5%)
            PROGRESSIVE CONSERVATIVE: 822 (27.0%, -4.0%)
            NEW DEMOCRATIC PARTY: 112 (3.7%, -12.2%)
            """.trimIndent(),
        )
    }

    @Test
    fun testWithoutPrev() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val title = Publisher("CHARLOTTETOWN-WINSLOE")
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
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = currentCandidateVotes
                header = candidateHeader
                incumbentMarker = "MLA"
            },
            partyVotes = {
                votes = currentPartyVotes.convertToPartyOrCandidateForMixedMember()
                header = partyHeader
            },
            map = createSingleResultMap {
                this.shapes = shapesByDistrict.asOneTimePublisher()
                this.selectedShape = selectedShape
                this.leader = selectedResult
                this.focus = focus
                this.header = mapHeader
            },
            title = title,
        )
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
        assertPublishes(
            panel.altText,
            """
            CHARLOTTETOWN-WINSLOE
            
            CANDIDATE VOTES
            ROBERT MITCHELL [MLA] (LIB): 1,420 (42.0%)
            AMANDA MORRISON (GRN): 1,057 (31.2%)
            MIKE GILLIS (PC): 865 (25.6%)
            JESSE REDDIN COUSINS (NDP): 41 (1.2%)
            
            PARTY VOTES
            GREEN: 1,098 (36.1%)
            LIBERAL: 1,013 (33.3%)
            PROGRESSIVE CONSERVATIVE: 822 (27.0%)
            NEW DEMOCRATIC PARTY: 112 (3.7%)
            """.trimIndent(),
        )
    }

    @Test
    fun testSubheadWithoutPrev() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val title = Publisher("CHARLOTTETOWN-WINSLOE")
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
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = currentCandidateVotes
                header = candidateHeader
                subhead = candidateSubhead
                incumbentMarker = "MLA"
            },
            partyVotes = {
                votes = currentPartyVotes.convertToPartyOrCandidateForMixedMember()
                header = partyHeader
            },
            map = createSingleResultMap {
                this.shapes = shapesByDistrict.asOneTimePublisher()
                this.selectedShape = selectedShape
                this.leader = selectedResult
                this.focus = focus
                this.header = mapHeader
            },
            title = title,
        )
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
        assertPublishes(
            panel.altText,
            """
            CHARLOTTETOWN-WINSLOE
            
            CANDIDATE VOTES, LIB WIN IN 2015
            ROBERT MITCHELL [MLA] (LIB): 1,420 (42.0%)
            AMANDA MORRISON (GRN): 1,057 (31.2%)
            MIKE GILLIS (PC): 865 (25.6%)
            JESSE REDDIN COUSINS (NDP): 41 (1.2%)
            
            PARTY VOTES
            GREEN: 1,098 (36.1%)
            LIBERAL: 1,013 (33.3%)
            PROGRESSIVE CONSERVATIVE: 822 (27.0%)
            NEW DEMOCRATIC PARTY: 112 (3.7%)
            """.trimIndent(),
        )
    }

    @Test
    fun testTickWithoutPrev() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val title = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val partyHeader = Publisher("PARTY VOTES")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val selectedShape = Publisher(10)
        val selectedResult = Publisher<PartyResult?>(null)
        val candidateWinner = Publisher<Candidate?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        selectedResult.submit(PartyResult.elected(lib))
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = currentCandidateVotes
                header = candidateHeader
                incumbentMarker = "MLA"
                winner = candidateWinner
            },
            partyVotes = {
                votes = currentPartyVotes.convertToPartyOrCandidateForMixedMember()
                header = partyHeader
            },
            map = createSingleResultMap {
                this.shapes = shapesByDistrict.asOneTimePublisher()
                this.selectedShape = selectedShape
                this.leader = selectedResult
                this.focus = focus
                this.header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("Jesse Reddin Cousins", ndp) to 41,
                Candidate("Mike Gillis", pc) to 865,
                Candidate("Robert Mitchell", lib, true) to 1420,
                Candidate("Amanda Morrison", grn) to 1057,
            ),
        )
        candidateWinner.submit(Candidate("Robert Mitchell", lib, true))
        currentPartyVotes.submit(
            mapOf(
                grn to 1098,
                lib to 1013,
                ndp to 112,
                pc to 822,
            ),
        )
        compareRendering("MixedMemberResultPanel", "NoPrevTick", panel)
        assertPublishes(
            panel.altText,
            """
            CHARLOTTETOWN-WINSLOE
            
            CANDIDATE VOTES
            ROBERT MITCHELL [MLA] (LIB): 1,420 (42.0%) WINNER
            AMANDA MORRISON (GRN): 1,057 (31.2%)
            MIKE GILLIS (PC): 865 (25.6%)
            JESSE REDDIN COUSINS (NDP): 41 (1.2%)
            
            PARTY VOTES
            GREEN: 1,098 (36.1%)
            LIBERAL: 1,013 (33.3%)
            PROGRESSIVE CONSERVATIVE: 822 (27.0%)
            NEW DEMOCRATIC PARTY: 112 (3.7%)
            """.trimIndent(),
        )
    }

    @Test
    fun testPartyOnlyForCandidateVote() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val currentPartyVotes = Publisher(emptyMap<Party, Int>())
        val title = Publisher("CHARLOTTETOWN-WINSLOE")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val partyHeader = Publisher("PARTY VOTES")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it in 10..14 })
        val selectedShape = Publisher(10)
        val selectedResult = Publisher<PartyResult?>(null)
        val candidateWinner = Publisher<Candidate?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        selectedResult.submit(PartyResult.elected(lib))
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = currentCandidateVotes
                header = candidateHeader
                incumbentMarker = "MLA"
                winner = candidateWinner
            },
            partyVotes = {
                votes = currentPartyVotes.convertToPartyOrCandidateForMixedMember()
                header = partyHeader
            },
            map = createSingleResultMap {
                this.shapes = shapesByDistrict.asOneTimePublisher()
                this.selectedShape = selectedShape
                this.leader = selectedResult
                this.focus = focus
                this.header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("", ndp) to 41,
                Candidate("", pc) to 865,
                Candidate("", lib, true) to 1420,
                Candidate("", grn) to 1057,
            ),
        )
        candidateWinner.submit(Candidate("", lib, true))
        currentPartyVotes.submit(
            mapOf(
                grn to 1098,
                lib to 1013,
                ndp to 112,
                pc to 822,
            ),
        )
        compareRendering("MixedMemberResultPanel", "PartyOnlyForCandidateVotes", panel)
        assertPublishes(
            panel.altText,
            """
            CHARLOTTETOWN-WINSLOE
            
            CANDIDATE VOTES
            LIBERAL: 1,420 (42.0%) WINNER
            GREEN: 1,057 (31.2%)
            PROGRESSIVE CONSERVATIVE: 865 (25.6%)
            NEW DEMOCRATIC PARTY: 41 (1.2%)
            
            PARTY VOTES
            GREEN: 1,098 (36.1%)
            LIBERAL: 1,013 (33.3%)
            PROGRESSIVE CONSERVATIVE: 822 (27.0%)
            NEW DEMOCRATIC PARTY: 112 (3.7%)
            """.trimIndent(),
        )
    }

    @Test
    fun testDeclarationInProgress() {
        val lab = Party("Labour", "LAB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val con = Party("Conservative", "CON", Color.BLUE)
        val ld = Party("Liberal Democrats", "LD", Color.ORANGE)
        val ukip = Party("UK Independence Party", "UKIP", Color.MAGENTA.darker())
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
        val title = Publisher("CITY & EAST")
        val candidateHeader = Publisher("CONSTITUENCY VOTES")
        val candidateChangeHeader = Publisher("CONSTITUENCY CHANGE SINCE 2012")
        val partyHeader = Publisher("AT-LARGE VOTES")
        val partyChangeHeader = Publisher("AT-LARGE CHANGE SINCE 2012")
        val topPartiesWaiting = Publisher(listOf(con, lab, ld, grn))
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = currentCandidateVotes
                header = candidateHeader
            },
            candidateChange = {
                prevVotes = previousCandidateVotes
                header = candidateChangeHeader
            },
            partyVotes = {
                votes = Aggregators.topAndOthers(currentPartyVotes, 5, Party.OTHERS, topPartiesWaiting).convertToPartyOrCandidateForMixedMember()
                header = partyHeader
            },
            partyChange = {
                prevVotes = previousPartyVotes
                header = partyChangeHeader
            },
            title = title,
        )
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
        assertPublishes(
            panel.altText,
            """
            CITY & EAST
            
            CONSTITUENCY VOTES (CONSTITUENCY CHANGE SINCE 2012)
            ELAINE SHEILA BAGSHAW (LD): WAITING...
            CHRISTOPHER JAMES CHAPMAN (CON): WAITING...
            RACHEL COLLINSON (GRN): WAITING...
            UNMESH DESAI (LAB): WAITING...
            AARON ANTHONY JOSE HASAN D'SOUZA (APP): WAITING...
            AMINA MAY KAY GICHINGA (CITY): WAITING...
            PETER JAMES HARRIS (UKIP): WAITING...
            RAYNE MICKAIL (RESP): WAITING...
            
            AT-LARGE VOTES (AT-LARGE CHANGE SINCE 2012)
            CONSERVATIVE: WAITING...
            GREEN: WAITING...
            LABOUR: WAITING...
            LIBERAL DEMOCRATS: WAITING...
            OTHERS: WAITING...
            """.trimIndent(),
        )

        currCandVotes[Candidate("Elaine Sheila Bagshaw", ld)] = 10714
        currentCandidateVotes.submit(currCandVotes)
        compareRendering("MixedMemberResultPanel", "Declaration-2", panel)
        assertPublishes(
            panel.altText,
            """
            CITY & EAST
            
            CONSTITUENCY VOTES (CONSTITUENCY CHANGE SINCE 2012)
            ELAINE SHEILA BAGSHAW (LD): 10,714
            CHRISTOPHER JAMES CHAPMAN (CON): WAITING...
            RACHEL COLLINSON (GRN): WAITING...
            UNMESH DESAI (LAB): WAITING...
            AARON ANTHONY JOSE HASAN D'SOUZA (APP): WAITING...
            AMINA MAY KAY GICHINGA (CITY): WAITING...
            PETER JAMES HARRIS (UKIP): WAITING...
            RAYNE MICKAIL (RESP): WAITING...
            
            AT-LARGE VOTES (AT-LARGE CHANGE SINCE 2012)
            CONSERVATIVE: WAITING...
            GREEN: WAITING...
            LABOUR: WAITING...
            LIBERAL DEMOCRATS: WAITING...
            OTHERS: WAITING...
            """.trimIndent(),
        )

        currCandVotes[Candidate("Christopher James Chapman", con)] = 32546
        currCandVotes[Candidate("Rachel Collinson", grn)] = 18766
        currCandVotes[Candidate("Unmesh Desai", lab)] = 122175
        currentCandidateVotes.submit(currCandVotes)
        compareRendering("MixedMemberResultPanel", "Declaration-3", panel)
        assertPublishes(
            panel.altText,
            """
            CITY & EAST
            
            CONSTITUENCY VOTES (CONSTITUENCY CHANGE SINCE 2012)
            UNMESH DESAI (LAB): 122,175
            CHRISTOPHER JAMES CHAPMAN (CON): 32,546
            RACHEL COLLINSON (GRN): 18,766
            ELAINE SHEILA BAGSHAW (LD): 10,714
            AARON ANTHONY JOSE HASAN D'SOUZA (APP): WAITING...
            AMINA MAY KAY GICHINGA (CITY): WAITING...
            PETER JAMES HARRIS (UKIP): WAITING...
            RAYNE MICKAIL (RESP): WAITING...
            
            AT-LARGE VOTES (AT-LARGE CHANGE SINCE 2012)
            CONSERVATIVE: WAITING...
            GREEN: WAITING...
            LABOUR: WAITING...
            LIBERAL DEMOCRATS: WAITING...
            OTHERS: WAITING...
            """.trimIndent(),
        )

        currCandVotes[Candidate("Aaron Anthony Jose Hasan D'Souza", app)] = 1009
        currCandVotes[Candidate("Amina May Kay Gichinga", city)] = 1368
        currCandVotes[Candidate("Peter James Harris", ukip)] = 18071
        currCandVotes[Candidate("Rayne Mickail", resp)] = 6772
        currentCandidateVotes.submit(currCandVotes)
        compareRendering("MixedMemberResultPanel", "Declaration-4", panel)
        assertPublishes(
            panel.altText,
            """
            CITY & EAST
            
            CONSTITUENCY VOTES (CONSTITUENCY CHANGE SINCE 2012)
            UNMESH DESAI (LAB): 122,175 (57.8%, -5.2%)
            CHRISTOPHER JAMES CHAPMAN (CON): 32,546 (15.4%, +0.8%)
            RACHEL COLLINSON (GRN): 18,766 (8.9%, +2.5%)
            PETER JAMES HARRIS (UKIP): 18,071 (8.5%, +8.5%)
            ELAINE SHEILA BAGSHAW (LD): 10,714 (5.1%, +0.8%)
            RAYNE MICKAIL (RESP): 6,772 (3.2%, +3.2%)
            AMINA MAY KAY GICHINGA (CITY): 1,368 (0.6%, +0.6%)
            AARON ANTHONY JOSE HASAN D'SOUZA (APP): 1,009 (0.5%, +0.5%)
            OTHERS: - (-11.8%)
            
            AT-LARGE VOTES (AT-LARGE CHANGE SINCE 2012)
            CONSERVATIVE: WAITING...
            GREEN: WAITING...
            LABOUR: WAITING...
            LIBERAL DEMOCRATS: WAITING...
            OTHERS: WAITING...
            """.trimIndent(),
        )

        currPartyVotes[awp] = 1738
        currentPartyVotes.submit(currPartyVotes)
        topPartiesWaiting.submit(listOf(con, lab, ld))
        compareRendering("MixedMemberResultPanel", "Declaration-5", panel)
        assertPublishes(
            panel.altText,
            """
            CITY & EAST
            
            CONSTITUENCY VOTES (CONSTITUENCY CHANGE SINCE 2012)
            UNMESH DESAI (LAB): 122,175 (57.8%, -5.2%)
            CHRISTOPHER JAMES CHAPMAN (CON): 32,546 (15.4%, +0.8%)
            RACHEL COLLINSON (GRN): 18,766 (8.9%, +2.5%)
            PETER JAMES HARRIS (UKIP): 18,071 (8.5%, +8.5%)
            ELAINE SHEILA BAGSHAW (LD): 10,714 (5.1%, +0.8%)
            RAYNE MICKAIL (RESP): 6,772 (3.2%, +3.2%)
            AMINA MAY KAY GICHINGA (CITY): 1,368 (0.6%, +0.6%)
            AARON ANTHONY JOSE HASAN D'SOUZA (APP): 1,009 (0.5%, +0.5%)
            OTHERS: - (-11.8%)
            
            AT-LARGE VOTES (AT-LARGE CHANGE SINCE 2012)
            ANIMAL WELFARE PARTY: 1,738
            CONSERVATIVE: WAITING...
            LABOUR: WAITING...
            LIBERAL DEMOCRATS: WAITING...
            OTHERS: WAITING...
            """.trimIndent(),
        )

        currPartyVotes[bf] = 3591
        currPartyVotes[bnp] = 1828
        currPartyVotes[ld] = 7799
        currentPartyVotes.submit(currPartyVotes)
        topPartiesWaiting.submit(emptyList())
        compareRendering("MixedMemberResultPanel", "Declaration-6", panel)
        assertPublishes(
            panel.altText,
            """
            CITY & EAST
            
            CONSTITUENCY VOTES (CONSTITUENCY CHANGE SINCE 2012)
            UNMESH DESAI (LAB): 122,175 (57.8%, -5.2%)
            CHRISTOPHER JAMES CHAPMAN (CON): 32,546 (15.4%, +0.8%)
            RACHEL COLLINSON (GRN): 18,766 (8.9%, +2.5%)
            PETER JAMES HARRIS (UKIP): 18,071 (8.5%, +8.5%)
            ELAINE SHEILA BAGSHAW (LD): 10,714 (5.1%, +0.8%)
            RAYNE MICKAIL (RESP): 6,772 (3.2%, +3.2%)
            AMINA MAY KAY GICHINGA (CITY): 1,368 (0.6%, +0.6%)
            AARON ANTHONY JOSE HASAN D'SOUZA (APP): 1,009 (0.5%, +0.5%)
            OTHERS: - (-11.8%)
            
            AT-LARGE VOTES (AT-LARGE CHANGE SINCE 2012)
            LIBERAL DEMOCRATS: 7,799
            BRITAIN FIRST: 3,591
            BRITISH NATIONAL PARTY: 1,828
            ANIMAL WELFARE PARTY: 1,738
            OTHERS: WAITING...
            """.trimIndent(),
        )

        currPartyVotes[cpa] = 2660
        currPartyVotes[con] = 30424
        currentPartyVotes.submit(currPartyVotes)
        compareRendering("MixedMemberResultPanel", "Declaration-7", panel)
        assertPublishes(
            panel.altText,
            """
            CITY & EAST
            
            CONSTITUENCY VOTES (CONSTITUENCY CHANGE SINCE 2012)
            UNMESH DESAI (LAB): 122,175 (57.8%, -5.2%)
            CHRISTOPHER JAMES CHAPMAN (CON): 32,546 (15.4%, +0.8%)
            RACHEL COLLINSON (GRN): 18,766 (8.9%, +2.5%)
            PETER JAMES HARRIS (UKIP): 18,071 (8.5%, +8.5%)
            ELAINE SHEILA BAGSHAW (LD): 10,714 (5.1%, +0.8%)
            RAYNE MICKAIL (RESP): 6,772 (3.2%, +3.2%)
            AMINA MAY KAY GICHINGA (CITY): 1,368 (0.6%, +0.6%)
            AARON ANTHONY JOSE HASAN D'SOUZA (APP): 1,009 (0.5%, +0.5%)
            OTHERS: - (-11.8%)
            
            AT-LARGE VOTES (AT-LARGE CHANGE SINCE 2012)
            CONSERVATIVE: 30,424
            LIBERAL DEMOCRATS: 7,799
            BRITAIN FIRST: 3,591
            CHRISTIAN PEOPLE'S ALLIANCE: 2,660
            OTHERS: WAITING...
            """.trimIndent(),
        )

        currPartyVotes[grn] = 14151
        currPartyVotes[lab] = 121871
        currentPartyVotes.submit(currPartyVotes)
        compareRendering("MixedMemberResultPanel", "Declaration-8", panel)
        assertPublishes(
            panel.altText,
            """
            CITY & EAST
            
            CONSTITUENCY VOTES (CONSTITUENCY CHANGE SINCE 2012)
            UNMESH DESAI (LAB): 122,175 (57.8%, -5.2%)
            CHRISTOPHER JAMES CHAPMAN (CON): 32,546 (15.4%, +0.8%)
            RACHEL COLLINSON (GRN): 18,766 (8.9%, +2.5%)
            PETER JAMES HARRIS (UKIP): 18,071 (8.5%, +8.5%)
            ELAINE SHEILA BAGSHAW (LD): 10,714 (5.1%, +0.8%)
            RAYNE MICKAIL (RESP): 6,772 (3.2%, +3.2%)
            AMINA MAY KAY GICHINGA (CITY): 1,368 (0.6%, +0.6%)
            AARON ANTHONY JOSE HASAN D'SOUZA (APP): 1,009 (0.5%, +0.5%)
            OTHERS: - (-11.8%)
            
            AT-LARGE VOTES (AT-LARGE CHANGE SINCE 2012)
            LABOUR: 121,871
            CONSERVATIVE: 30,424
            GREEN: 14,151
            LIBERAL DEMOCRATS: 7,799
            OTHERS: WAITING...
            """.trimIndent(),
        )

        currPartyVotes[resp] = 6784
        currPartyVotes[house] = 858
        currPartyVotes[ukip] = 14123
        currPartyVotes[wep] = 5718
        currentPartyVotes.submit(currPartyVotes)
        compareRendering("MixedMemberResultPanel", "Declaration-9", panel)
        assertPublishes(
            panel.altText,
            """
            CITY & EAST
            
            CONSTITUENCY VOTES (CONSTITUENCY CHANGE SINCE 2012)
            UNMESH DESAI (LAB): 122,175 (57.8%, -5.2%)
            CHRISTOPHER JAMES CHAPMAN (CON): 32,546 (15.4%, +0.8%)
            RACHEL COLLINSON (GRN): 18,766 (8.9%, +2.5%)
            PETER JAMES HARRIS (UKIP): 18,071 (8.5%, +8.5%)
            ELAINE SHEILA BAGSHAW (LD): 10,714 (5.1%, +0.8%)
            RAYNE MICKAIL (RESP): 6,772 (3.2%, +3.2%)
            AMINA MAY KAY GICHINGA (CITY): 1,368 (0.6%, +0.6%)
            AARON ANTHONY JOSE HASAN D'SOUZA (APP): 1,009 (0.5%, +0.5%)
            OTHERS: - (-11.8%)
            
            AT-LARGE VOTES (AT-LARGE CHANGE SINCE 2012)
            LABOUR: 121,871 (57.6%, -5.6%)
            CONSERVATIVE: 30,424 (14.4%, -0.3%)
            GREEN: 14,151 (6.7%, +0.2%)
            UK INDEPENDENCE PARTY: 14,123 (6.7%, +3.2%)
            OTHERS: 30,976 (14.6%, +2.4%)
            """.trimIndent(),
        )

        currentCandidateVotes.submit(
            Aggregators.topAndOthers(currCandVotes, 6, Candidate.OTHERS),
        )
        compareRendering("MixedMemberResultPanel", "Declaration-10", panel)
        assertPublishes(
            panel.altText,
            """
            CITY & EAST
            
            CONSTITUENCY VOTES (CONSTITUENCY CHANGE SINCE 2012)
            UNMESH DESAI (LAB): 122,175 (57.8%, -5.2%)
            CHRISTOPHER JAMES CHAPMAN (CON): 32,546 (15.4%, +0.8%)
            RACHEL COLLINSON (GRN): 18,766 (8.9%, +2.5%)
            PETER JAMES HARRIS (UKIP): 18,071 (8.5%, +8.5%)
            ELAINE SHEILA BAGSHAW (LD): 10,714 (5.1%, +0.8%)
            OTHERS: 9,149 (4.3%, -7.5%)
            
            AT-LARGE VOTES (AT-LARGE CHANGE SINCE 2012)
            LABOUR: 121,871 (57.6%, -5.6%)
            CONSERVATIVE: 30,424 (14.4%, -0.3%)
            GREEN: 14,151 (6.7%, +0.2%)
            UK INDEPENDENCE PARTY: 14,123 (6.7%, +3.2%)
            OTHERS: 30,976 (14.6%, +2.4%)
            """.trimIndent(),
        )
    }

    @Test
    fun testManyCandidatesGoSingleLine() {
        val lab = Party("Labour", "LAB", Color.RED)
        val nat = Party("National", "NAT", Color.BLUE)
        val nzf = Party("NZ First", "NZF", Color.BLACK)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val act = Party("ACT", "ACT", Color.YELLOW)
        val adv = Party("Advance NZ", "ADV", Color.CYAN.darker())
        val con = Party("New Conservative", "CON", Color.BLUE.brighter())
        val top = Party("Opportunities", "TOP", Color.CYAN)
        val out = Party("Outdoors", "OUT", Color.GREEN.darker().darker())
        val sc = Party("Social Credit", "SC", Color.GREEN)
        val hn = Party("Harmony Network", "HN", Color.GRAY)
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        val candidateVotes = mapOf(
            Candidate("Trevor Barfoote", con) to 686,
            Candidate("Mark Cameron", act) to 1279,
            Candidate("Brad Flutey", sc) to 82,
            Candidate("Darleen Tana Hoff-Nelson", grn) to 1749,
            Candidate("Helen Jeremiah", top) to 326,
            Candidate("Willow-Jean Prime", lab) to 17066,
            Candidate("Shane Jones", nzf) to 5119,
            Candidate("Matt King", nat, true) to 16903,
            Candidate("Michele Mitcalfe", out) to 219,
            Candidate("Nathan Mitchell", adv) to 847,
            Candidate("Mike Shaw", ind) to 480,
            Candidate("Sophia Xiao-Colley", hn) to 28,
        )
        val partyVotes = mapOf(
            lab to 19997,
            nat to 12496,
            nzf to 2651,
            grn to 2772,
            act to 4326,
            adv to 949,
            con to 842,
            top to 326,
            out to 106,
            sc to 69,
            Party.OTHERS to (294 + 248 + 181 + 37 + 30 + 6 + 3),
        ).let { votes ->
            val total = votes.values.sum()
            votes.entries.groupingBy { if (it.value >= total * 0.05) it.key else Party.OTHERS }
                .fold(0) { a, e -> a + e.value }
        }
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = candidateVotes.asOneTimePublisher()
                header = "CANDIDATE VOTES".asOneTimePublisher()
                subhead = "LAB GAIN FROM NAT".asOneTimePublisher()
                incumbentMarker = "INC"
                winner = Candidate("Willow-Jean Prime", lab).asOneTimePublisher()
            },
            partyVotes = {
                votes = partyVotes.asOneTimePublisher().convertToPartyOrCandidateForMixedMember()
                header = "PARTY VOTES".asOneTimePublisher()
            },
            title = "NORTHLAND".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("MixedMemberResultPanel", "ManyCandidates", panel)
        assertPublishes(
            panel.altText,
            """
            NORTHLAND
            
            CANDIDATE VOTES, LAB GAIN FROM NAT
            WILLOW-JEAN PRIME (LAB): 17,066 (38.1%) WINNER
            MATT KING [INC] (NAT): 16,903 (37.7%)
            SHANE JONES (NZF): 5,119 (11.4%)
            DARLEEN TANA HOFF-NELSON (GRN): 1,749 (3.9%)
            MARK CAMERON (ACT): 1,279 (2.9%)
            NATHAN MITCHELL (ADV): 847 (1.9%)
            TREVOR BARFOOTE (CON): 686 (1.5%)
            MIKE SHAW (IND): 480 (1.1%)
            HELEN JEREMIAH (TOP): 326 (0.7%)
            MICHELE MITCALFE (OUT): 219 (0.5%)
            BRAD FLUTEY (SC): 82 (0.2%)
            SOPHIA XIAO-COLLEY (HN): 28 (0.1%)
            
            PARTY VOTES
            LABOUR: 19,997 (44.1%)
            NATIONAL: 12,496 (27.6%)
            ACT: 4,326 (9.5%)
            GREEN: 2,772 (6.1%)
            NZ FIRST: 2,651 (5.8%)
            OTHERS: 3,091 (6.8%)
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesAsList() {
        val currentCandidateVotes = Publisher(emptyMap<Candidate, Int>())
        val previousCandidateVotes = Publisher(emptyMap<Party, Int>())
        val currentPartyVotes = Publisher(emptyMap<PartyOrCandidate, Int>())
        val previousPartyVotes = Publisher(emptyMap<Party, Int>())
        val title = Publisher("CITY AND EAST")
        val candidateHeader = Publisher("CANDIDATE VOTES")
        val candidateChangeHeader = Publisher("CANDIDATE CHANGE SINCE 2021")
        val partyHeader = Publisher("PARTY VOTES")
        val partyChangeHeader = Publisher("PARTY CHANGE SINCE 2021")
        val lab = Party("Labour", "LAB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val con = Party("Conservative", "CON", Color.BLUE)
        val ld = Party("Liberal Democrats", "LD", Color.ORANGE)
        val ref = Party("Reform UK", "REF", Color.CYAN.darker())
        val ind = Party("Independent", "IND", Party.OTHERS.color)
        val panel = MixedMemberResultPanel.of(
            candidateVotes = {
                votes = currentCandidateVotes
                header = candidateHeader
            },
            candidateChange = {
                prevVotes = previousCandidateVotes
                header = candidateChangeHeader
            },
            partyVotes = {
                votes = Aggregators.topAndOthers(currentPartyVotes, 8, PartyOrCandidate.OTHERS)
                header = partyHeader
            },
            partyChange = {
                prevVotes = previousPartyVotes
                header = partyChangeHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        currentCandidateVotes.submit(
            mapOf(
                Candidate("Unmesh Desai", lab, true) to 99570,
                Candidate("Freddie Downing", con) to 29083,
                Candidate("Joe Hudson-Small", grn) to 29073,
                Candidate("David Sandground", ref) to 14535,
                Candidate("Pat Stillman", ld) to 11416,
                Candidate("Ak Goodman", ind) to 5310,
                Candidate("Lois Austin", Party("Trade Unionist and Socialist Coalition", "TUSC", Color.RED.darker())) to 4710,
            ),
        )
        previousCandidateVotes.submit(
            mapOf(
                lab to 125025,
                con to 46718,
                grn to 25596,
                ld to 14136,
                ref to 9060,
            ),
        )
        currentPartyVotes.submit(
            mapOf(
                PartyOrCandidate(lab) to 97432,
                PartyOrCandidate(con) to 28344,
                PartyOrCandidate(grn) to 24912,
                PartyOrCandidate(ref) to 10899,
                PartyOrCandidate(ld) to 9620,
                PartyOrCandidate(Party("Rejoin EU", "REJOIN", Color.BLUE.darker())) to 5384,
                PartyOrCandidate("Farah London") to 3490,
                PartyOrCandidate(Party("Animal Welfare Party", "AWP", Color.RED.darker())) to 3423,
                PartyOrCandidate(Party("Britain First", "BF", Color.BLUE.darker())) to 3104,
                PartyOrCandidate(Party("Christian Peoples Alliance", "CPA", Color.MAGENTA.darker())) to 2666,
                PartyOrCandidate(Party("Social Democratic Party", "SDP", Color.BLUE.darker())) to 2000,
                PartyOrCandidate(Party("Communist", "COM", Color.RED)) to 1063,
                PartyOrCandidate("Laurence Fox") to 1006,
                PartyOrCandidate("Gabe Romualdo") to 372,
                PartyOrCandidate(Party("Heritage", "HERITAGE", Color.BLUE)) to 366,
            ),
        )
        previousPartyVotes.submit(
            mapOf(
                lab to 116148,
                con to 44957,
                grn to 20106,
                ld to 9001,
                Party.OTHERS to 28550,
            ),
        )
        compareRendering("MixedMemberResultPanel", "IndependentList", panel)
        assertPublishes(
            panel.altText,
            """
            CITY AND EAST

            CANDIDATE VOTES (CANDIDATE CHANGE SINCE 2021)
            UNMESH DESAI (LAB): 99,570 (51.4%, -5.3%)
            FREDDIE DOWNING (CON): 29,083 (15.0%, -6.2%)
            JOE HUDSON-SMALL (GRN): 29,073 (15.0%, +3.4%)
            DAVID SANDGROUND (REF): 14,535 (7.5%, +3.4%)
            PAT STILLMAN (LD): 11,416 (5.9%, -0.5%)
            AK GOODMAN (IND): 5,310 (2.7%, +2.7%)
            LOIS AUSTIN (TUSC): 4,710 (2.4%, +2.4%)
            
            PARTY VOTES (PARTY CHANGE SINCE 2021)
            LABOUR: 97,432 (50.2%, -2.9%)
            CONSERVATIVE: 28,344 (14.6%, -5.9%)
            GREEN: 24,912 (12.8%, +3.6%)
            REFORM UK: 10,899 (5.6%, +5.6%)
            LIBERAL DEMOCRATS: 9,620 (5.0%, +0.8%)
            REJOIN EU: 5,384 (2.8%, +2.8%)
            FARAH LONDON: 3,490 (1.8%, +1.8%)
            OTHERS: 14,000 (7.2%, -5.8%)
            """.trimIndent(),
        )
    }

    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = MixedMemberResultPanelTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return ShapefileReader.readShapes(peiMap, "DIST_NO", Int::class.java)
    }
}
