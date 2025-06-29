package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.candidateVotes
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanelTest.Companion.peiShapesByDistrict
import com.joecollins.graphics.screens.generic.SinglePartyMap.Companion.createSinglePartyMap
import com.joecollins.graphics.screens.generic.SingleResultMap.Companion.createSingleResultMap
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Aggregators.topAndOthers
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PartyResult.Companion.elected
import com.joecollins.models.general.PartyResult.Companion.leading
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Dimension
import java.text.DecimalFormat
import kotlin.math.ceil

class SimpleCandidateVoteViewPanelTest {

    @Test
    fun testCandidatesBasicResult() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val currentVotes = Publisher(
            mapOf(
                ndp to 124,
                pc to 1373,
                lib to 785,
                grn to 674,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                ndp.party to 585,
                pc.party to 785,
                lib.party to 1060,
                grn.party to 106,
            ),
        )
        val title = Publisher("MONTAGUE-KILMUIR")
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
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            map = createSinglePartyMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                selectedShape = selectedDistrict
                this.leader = leader
                this.focus = focus
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "Basic-1", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                9 OF 9 POLLS REPORTING, PROJECTION: PC GAIN FROM LIB (CHANGE SINCE 2015)
                CORY DEAGLE (PC): 1,373 (46.4%, +15.5%)
                DAPHNE GRIFFIN (LIB): 785 (26.6%, -15.2%)
                JOHN ALLEN MACLEAN (GRN): 674 (22.8%, +18.6%)
                BILLY CANN (NDP): 124 (4.2%, -18.9%)
                
                SWING SINCE 2015: 15.4% SWING LIB TO PC
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesBasicResultShrinkToFit() {
        val ndp = Candidate("Steven Scott", Party("New Democratic Party", "NDP", Color.ORANGE))
        val con = Candidate("Claudio Rocchi", Party("Conservative", "CON", Color.BLUE))
        val lib = Candidate("David Lametti", Party("Liberal", "LIB", Color.RED), true)
        val grn = Candidate("Jency Mercier", Party("Green", "GRN", Color.GREEN.darker()))
        val bq = Candidate(
            "Isabel Dion",
            Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker()),
        )
        val ppc = Candidate("Daniel Turgeon", Party("People's Party", "PPC", Color.MAGENTA.darker()))
        val ml = Candidate("Eileen Studd", Party("Marxist-Leninist", "M-L", Color.RED))
        val rhino = Candidate("Rhino Jacques B\u00e9langer", Party("Rhinoceros", "RHINO", Color.GRAY))
        val ind = Candidate("Julien C\u00f4t\u00e9", Party("Independent", "IND", Color.GRAY))
        val currentVotes = Publisher(
            mapOf(
                ndp to 8628,
                con to 3690,
                lib to 22803,
                grn to 3583,
                bq to 12619,
                ppc to 490,
                ml to 39,
                rhino to 265,
                ind to 274,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                ndp.party to 15566,
                con.party to 3713,
                lib.party to 23603,
                grn.party to 1717,
                bq.party to 9164,
            ),
        )
        val title = Publisher("LASALLE\u2014\u00c9MARD\u2014VERDUN")
        val voteHeader = Publisher("100% OF POLLS REPORTING")
        val voteSubhead = Publisher("PROJECTION: LIB HOLD")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, bq.party, con.party)
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                incumbentMarker = "MP"
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "Basic-2", panel)
        assertPublishes(
            panel.altText,
            """
                LASALLE—ÉMARD—VERDUN
                
                100% OF POLLS REPORTING, PROJECTION: LIB HOLD (CHANGE SINCE 2015)
                DAVID LAMETTI [MP] (LIB): 22,803 (43.5%, -0.4%)
                ISABEL DION (BQ): 12,619 (24.1%, +7.0%)
                STEVEN SCOTT (NDP): 8,628 (16.5%, -12.5%)
                CLAUDIO ROCCHI (CON): 3,690 (7.0%, +0.1%)
                JENCY MERCIER (GRN): 3,583 (6.8%, +3.6%)
                DANIEL TURGEON (PPC): 490 (0.9%, +0.9%)
                JULIEN CÔTÉ (IND): 274 (0.5%, +0.5%)
                RHINO JACQUES BÉLANGER (RHINO): 265 (0.5%, +0.5%)
                EILEEN STUDD (M-L): 39 (0.1%, +0.1%)
                
                SWING SINCE 2015: 3.7% SWING LIB TO BQ
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesBasicResultPctOnly() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val currentVotes = Publisher(
            mapOf(
                ndp to 124,
                pc to 1373,
                lib to 785,
                grn to 674,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                ndp.party to 585,
                pc.party to 785,
                lib.party to 1060,
                grn.party to 106,
            ),
        )
        val title = Publisher("MONTAGUE-KILMUIR")
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
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                display = SimpleVoteViewPanel.Display.PCT_ONLY
                notes = "SOURCE: Elections PEI".asOneTimePublisher()
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            map = createSinglePartyMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                selectedShape = selectedDistrict
                this.leader = leader
                this.focus = focus
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PctOnly-1", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                9 OF 9 POLLS REPORTING, PROJECTION: PC GAIN FROM LIB (CHANGE SINCE 2015)
                CORY DEAGLE (PC): 46.4% (+15.5%)
                DAPHNE GRIFFIN (LIB): 26.6% (-15.2%)
                JOHN ALLEN MACLEAN (GRN): 22.8% (+18.6%)
                BILLY CANN (NDP): 4.2% (-18.9%)
                
                SWING SINCE 2015: 15.4% SWING LIB TO PC
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidateScreenUpdating() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val currentVotes = Publisher(sequenceOf(ndp, pc, lib, grn).associateWith { 0 })
        val previousVotes = Publisher(
            mapOf(
                ndp.party to 585,
                pc.party to 785,
                lib.party to 1060,
                grn.party to 106,
            ),
        )
        val pctReporting = Publisher(0.0)
        val title = Publisher("MONTAGUE-KILMUIR")
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
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
                this.pctReporting = pctReporting
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            map = createSingleResultMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                selectedShape = selectedDistrict
                this.leader = leader
                this.focus = focus
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "Update-1", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                0 OF 9 POLLS REPORTING, WAITING FOR RESULTS... (CHANGE SINCE 2015)
                BILLY CANN (NDP): WAITING...
                CORY DEAGLE (PC): WAITING...
                DAPHNE GRIFFIN (LIB): WAITING...
                JOHN ALLEN MACLEAN (GRN): WAITING...
                
                SWING SINCE 2015: NOT AVAILABLE
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 5,
                pc to 47,
                lib to 58,
                grn to 52,
            ),
        )
        voteHeader.submit("1 OF 9 POLLS REPORTING")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(1.0 / 9)
        leader.submit(leading(lib.party))
        compareRendering("SimpleVoteViewPanel", "Update-2", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                1 OF 9 POLLS REPORTING, PROJECTION: TOO EARLY TO CALL (CHANGE SINCE 2015)
                DAPHNE GRIFFIN (LIB): 58 (35.8%, -6.0%)
                JOHN ALLEN MACLEAN (GRN): 52 (32.1%, +27.9%)
                CORY DEAGLE (PC): 47 (29.0%, -1.9%)
                BILLY CANN (NDP): 5 (3.1%, -20.0%)
                
                SWING SINCE 2015: 17.0% SWING LIB TO GRN
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 8,
                pc to 91,
                lib to 100,
                grn to 106,
            ),
        )
        voteHeader.submit("2 OF 9 POLLS REPORTING")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(2.0 / 9)
        leader.submit(leading(grn.party))
        compareRendering("SimpleVoteViewPanel", "Update-3", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                2 OF 9 POLLS REPORTING, PROJECTION: TOO EARLY TO CALL (CHANGE SINCE 2015)
                JOHN ALLEN MACLEAN (GRN): 106 (34.8%, +30.6%)
                DAPHNE GRIFFIN (LIB): 100 (32.8%, -9.0%)
                CORY DEAGLE (PC): 91 (29.8%, -1.1%)
                BILLY CANN (NDP): 8 (2.6%, -20.4%)
                
                SWING SINCE 2015: 19.8% SWING LIB TO GRN
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 18,
                pc to 287,
                lib to 197,
                grn to 243,
            ),
        )
        voteHeader.submit("5 OF 9 POLLS REPORTING")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(5.0 / 9)
        leader.submit(leading(pc.party))
        compareRendering("SimpleVoteViewPanel", "Update-4", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                5 OF 9 POLLS REPORTING, PROJECTION: TOO EARLY TO CALL (CHANGE SINCE 2015)
                CORY DEAGLE (PC): 287 (38.5%, +7.6%)
                JOHN ALLEN MACLEAN (GRN): 243 (32.6%, +28.4%)
                DAPHNE GRIFFIN (LIB): 197 (26.4%, -15.4%)
                BILLY CANN (NDP): 18 (2.4%, -20.7%)
                
                SWING SINCE 2015: 11.5% SWING LIB TO PC
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 124,
                pc to 1373,
                lib to 785,
                grn to 675,
            ),
        )
        voteHeader.submit("9 OF 9 POLLS REPORTING")
        voteSubhead.submit("PROJECTION: PC GAIN FROM LIB")
        pctReporting.submit(9.0 / 9)
        leader.submit(elected(pc.party))
        winner.submit(pc)
        compareRendering("SimpleVoteViewPanel", "Update-5", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                9 OF 9 POLLS REPORTING, PROJECTION: PC GAIN FROM LIB (CHANGE SINCE 2015)
                CORY DEAGLE (PC): 1,373 (46.4%, +15.5%) WINNER
                DAPHNE GRIFFIN (LIB): 785 (26.5%, -15.3%)
                JOHN ALLEN MACLEAN (GRN): 675 (22.8%, +18.6%)
                BILLY CANN (NDP): 124 (4.2%, -18.9%)
                
                SWING SINCE 2015: 15.4% SWING LIB TO PC
            """.trimIndent(),
        )

        winner.submit(null)
        compareRendering("SimpleVoteViewPanel", "Update-6", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                9 OF 9 POLLS REPORTING, PROJECTION: PC GAIN FROM LIB (CHANGE SINCE 2015)
                CORY DEAGLE (PC): 1,373 (46.4%, +15.5%)
                DAPHNE GRIFFIN (LIB): 785 (26.5%, -15.3%)
                JOHN ALLEN MACLEAN (GRN): 675 (22.8%, +18.6%)
                BILLY CANN (NDP): 124 (4.2%, -18.9%)
                
                SWING SINCE 2015: 15.4% SWING LIB TO PC
            """.trimIndent(),
        )
    }

    @Test
    fun testZeroVotesSingleCandidate() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val currentVotes = Publisher(
            mapOf(
                ndp to 6,
                pc to 8,
                lib to 11,
                grn to 0,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                ndp.party to 585,
                pc.party to 785,
                lib.party to 1060,
                grn.party to 106,
            ),
        )
        val pctReporting = Publisher(1.0 / 9)
        val title = Publisher("MONTAGUE-KILMUIR")
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
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
                this.pctReporting = pctReporting
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            map = createSingleResultMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                selectedShape = selectedDistrict
                this.leader = leader
                this.focus = focus
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "ZeroVotes-1", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                1 OF 9 POLLS REPORTING, WAITING FOR RESULTS... (CHANGE SINCE 2015)
                DAPHNE GRIFFIN (LIB): 11 (44.0%, +2.2%)
                CORY DEAGLE (PC): 8 (32.0%, +1.0%)
                BILLY CANN (NDP): 6 (24.0%, +0.9%)
                JOHN ALLEN MACLEAN (GRN): 0 (0.0%, -4.2%)
                
                SWING SINCE 2015: 0.6% SWING PC TO LIB
            """.trimIndent(),
        )
    }

    @Test
    fun testMultipleCandidatesSameParty() {
        val dem = Party("DEMOCRAT", "DEM", Color.BLUE)
        val gop = Party("REPUBLICAN", "GOP", Color.RED)
        val lbt = Party("LIBERTARIAN", "LBT", Color.ORANGE)
        val ind = Party("INDEPENDENT", "IND", Color.GRAY)
        val currentVotes = Publisher(
            mapOf(
                Candidate("Raul Barrera", dem) to 1747,
                Candidate("Bech Bruun", gop) to 1570,
                Candidate("Michael Cloud", gop) to 19856,
                Candidate("Judith Cutright", ind) to 172,
                Candidate("Eric Holguin", dem) to 11595,
                Candidate("Marty Perez", gop) to 276,
                Candidate("Christopher Suprun", ind) to 51,
                Candidate("Daniel Tinus", lbt) to 144,
                Candidate("Mike Westergren", dem) to 858,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                dem to 88329,
                gop to 142251,
            ),
        )
        val title = Publisher("TEXAS DISTRICT 27")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("GOP HOLD")
        val changeHeader = Publisher("CHANGE SINCE 2016")
        val swingHeader = Publisher("SWING SINCE 2016")
        val winner = Publisher(Candidate("Michael Cloud", gop))
        val swingPartyOrder = listOf(dem, gop)
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "SameParty-1", panel)
        assertPublishes(
            panel.altText,
            """
                TEXAS DISTRICT 27
                
                OFFICIAL RESULT, GOP HOLD (CHANGE SINCE 2016)
                MICHAEL CLOUD (GOP): 19,856 (54.7%, -1.9%^) WINNER
                ERIC HOLGUIN (DEM): 11,595 (32.0%, +0.8%^)
                RAUL BARRERA (DEM): 1,747 (4.8%, ^)
                BECH BRUUN (GOP): 1,570 (4.3%, ^)
                MIKE WESTERGREN (DEM): 858 (2.4%, ^)
                MARTY PEREZ (GOP): 276 (0.8%, ^)
                JUDITH CUTRIGHT (IND): 172 (0.5%, +0.6%^)
                DANIEL TINUS (LBT): 144 (0.4%, +0.4%)
                CHRISTOPHER SUPRUN (IND): 51 (0.1%, ^)
                ^ AGGREGATED ACROSS CANDIDATES IN PARTY
                
                SWING SINCE 2016: 1.4% SWING GOP TO DEM
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidateMajorityLine() {
        val lrem = Party("LA R\u00c9PUBLIQUE EN MARCHE", "LREM", Color.ORANGE)
        val fn = Party("FRONT NATIONAL", "FN", Color.BLUE)
        val currentVotes = Publisher(
            mapOf(
                Candidate("Emmanuel Macron", lrem) to 20743128,
                Candidate("Marine Le Pen", fn) to 10638475,
            ),
        )
        val title = Publisher("FRANCE PRESIDENT: ROUND 2")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("MACRON WIN")
        val showMajority = Publisher(true)
        val pctReporting = Publisher(1.0)
        val winner = Publisher<Candidate?>(Candidate("Emmanuel Macron", lrem))
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
                this.pctReporting = pctReporting
            },
            winningLine = {
                show(showMajority)
                majority { "50% TO WIN" }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "MajorityLine-1", panel)
        assertPublishes(
            panel.altText,
            """
                FRANCE PRESIDENT: ROUND 2
                
                OFFICIAL RESULT, MACRON WIN
                EMMANUEL MACRON (LREM): 20,743,128 (66.1%) WINNER
                MARINE LE PEN (FN): 10,638,475 (33.9%)
                50% TO WIN
            """.trimIndent(),
        )

        voteHeader.submit("15.4% REPORTING")
        voteSubhead.submit("PROJECTION: MACRON WIN")
        pctReporting.submit(0.154)
        winner.submit(null)
        currentVotes.submit(
            mapOf(
                Candidate("Emmanuel Macron", lrem) to 3825279,
                Candidate("Marine Le Pen", fn) to 1033686,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "MajorityLine-2", panel)
        assertPublishes(
            panel.altText,
            """
                FRANCE PRESIDENT: ROUND 2
                
                15.4% REPORTING, PROJECTION: MACRON WIN
                EMMANUEL MACRON (LREM): 3,825,279 (78.7%)
                MARINE LE PEN (FN): 1,033,686 (21.3%)
                50% TO WIN
            """.trimIndent(),
        )

        pctReporting.submit(0.0)
        currentVotes.submit(
            mapOf(
                Candidate("Emmanuel Macron", lrem) to 0,
                Candidate("Marine Le Pen", fn) to 0,
            ),
        )
        voteHeader.submit("0.0% REPORTING")
        voteSubhead.submit("")
        compareRendering("SimpleVoteViewPanel", "MajorityLine-3", panel)
        assertPublishes(
            panel.altText,
            """
                FRANCE PRESIDENT: ROUND 2
                
                0.0% REPORTING
                EMMANUEL MACRON (LREM): WAITING...
                MARINE LE PEN (FN): WAITING...
                50% TO WIN
            """.trimIndent(),
        )
    }

    @Test
    fun testAdditionalHighlightMap() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val currentVotes = Publisher(
            mapOf(
                ndp to 124,
                pc to 1373,
                lib to 785,
                grn to 674,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                ndp.party to 585,
                pc.party to 785,
                lib.party to 1060,
                grn.party to 106,
            ),
        )
        val title = Publisher("MONTAGUE-KILMUIR")
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
        val additionalHighlight = Publisher(shapesByDistrict.keys.toList())
        val selectedDistrict = Publisher(3)
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                subhead = changeSubhead
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            map = createSingleResultMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                selectedShape = selectedDistrict
                this.leader = leader.map { elected(it) }
                this.focus = focus
                this.additionalHighlight = additionalHighlight
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "AdditionalHighlightMap-1", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                9 OF 9 POLLS REPORTING, PROJECTION: PC GAIN FROM LIB (CHANGE SINCE 2015, ADJUSTED FOR BOUNDARY CHANGES)
                CORY DEAGLE (PC): 1,373 (46.4%, +15.5%)
                DAPHNE GRIFFIN (LIB): 785 (26.6%, -15.2%)
                JOHN ALLEN MACLEAN (GRN): 674 (22.8%, +18.6%)
                BILLY CANN (NDP): 124 (4.2%, -18.9%)
                
                SWING SINCE 2015: 15.4% SWING LIB TO PC
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidateUncontested() {
        val dem = Candidate("Joe Kennedy III", Party("Democratic", "DEM", Color.BLUE))
        val gop = Party("Republican", "GOP", Color.RED)
        val currentVotes = Publisher(mapOf(dem to 0))
        val previousVotes = Publisher(
            mapOf(
                dem.party to 265823,
                gop to 113055,
            ),
        )
        val title = Publisher("MASSACHUSETTS DISTRICT 4")
        val voteHeader = Publisher("0.0% OF POLLS REPORTING")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2016")
        val swingHeader = Publisher("SWING SINCE 2016")
        val swingPartyOrder: List<Party> = listOf(dem.party, gop)
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "Uncontested-1", panel)
        assertPublishes(
            panel.altText,
            """
                MASSACHUSETTS DISTRICT 4
                
                0.0% OF POLLS REPORTING (CHANGE SINCE 2016)
                JOE KENNEDY III (DEM): UNCONTESTED
                
                SWING SINCE 2016: NOT AVAILABLE
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesSwitchingBetweenSingleAndDoubleLines() {
        val currentVotes = Publisher(
            mapOf(
                Candidate("Boris Johnson", Party("Conservative", "CON", Color.BLUE)) to 23716,
                Candidate("Vincent Lo", Party("Labour", "LAB", Color.RED)) to 18682,
                Candidate("Rosina Robson", Party("Liberal Democrats", "LD", Color.ORANGE)) to 1835,
                Candidate("Lizzy Kemp", Party("UK Independence Party", "UKIP", Color.MAGENTA.darker())) to 1577,
                Candidate("Mark Keir", Party("Green", "GRN", Color.GREEN.darker())) to 884,
            ),
        )
        val title = Publisher("UXBRIDGE AND SOUTH RUISLIP")
        val voteHeader = Publisher("2017 RESULT")
        val voteSubhead = Publisher("")
        val winner = Publisher(Candidate("Boris Johnson", Party("Conservative", "CON", Color.BLUE)))
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "LotsOfCandidates-1", panel)
        assertPublishes(
            panel.altText,
            """
                UXBRIDGE AND SOUTH RUISLIP
                
                2017 RESULT
                BORIS JOHNSON (CON): 23,716 (50.8%) WINNER
                VINCENT LO (LAB): 18,682 (40.0%)
                ROSINA ROBSON (LD): 1,835 (3.9%)
                LIZZY KEMP (UKIP): 1,577 (3.4%)
                MARK KEIR (GRN): 884 (1.9%)
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                Candidate("Count Binface", Party("Independent", "IND", Color.GRAY)) to 69,
                Candidate("Lord Buckethead", Party("Monster Raving Loony Party", "MRLP", Color.YELLOW)) to 125,
                Candidate("Norma Burke", Party("Independent", "IND", Color.GRAY)) to 22,
                Candidate("Geoffrey Courtenay", Party("UK Independence Party", "UKIP", Color.MAGENTA.darker())) to 283,
                Candidate("Joanne Humphreys", Party("Liberal Democrats", "LD", Color.ORANGE)) to 3026,
                Candidate("Boris Johnson", Party("Conservative", "CON", Color.BLUE)) to 25351,
                Candidate("Mark Keir", Party("Green", "GRN", Color.GREEN.darker())) to 1090,
                Candidate("Ali Milani", Party("Labour", "LAB", Color.RED)) to 18141,
                Candidate("Bobby Smith", Party("Independent", "IND", Color.GRAY)) to 8,
                Candidate("William Tobin", Party("Independent", "IND", Color.GRAY)) to 5,
                Candidate("Alfie Utting", Party("Independent", "IND", Color.GRAY)) to 44,
                Candidate("Yace \"Interplanetary Time Lord\" Yogenstein", Party("Independent", "IND", Color.GRAY)) to 23,
            ),
        )
        voteHeader.submit("2019 RESULT")
        compareRendering("SimpleVoteViewPanel", "LotsOfCandidates-2", panel)
        assertPublishes(
            panel.altText,
            """
                UXBRIDGE AND SOUTH RUISLIP
                
                2019 RESULT
                BORIS JOHNSON (CON): 25,351 (52.6%) WINNER
                ALI MILANI (LAB): 18,141 (37.6%)
                JOANNE HUMPHREYS (LD): 3,026 (6.3%)
                MARK KEIR (GRN): 1,090 (2.3%)
                GEOFFREY COURTENAY (UKIP): 283 (0.6%)
                LORD BUCKETHEAD (MRLP): 125 (0.3%)
                COUNT BINFACE (IND): 69 (0.1%)
                ALFIE UTTING (IND): 44 (0.1%)
                YACE "INTERPLANETARY TIME LORD" YOGENSTEIN (IND): 23 (0.0%)
                NORMA BURKE (IND): 22 (0.0%)
                BOBBY SMITH (IND): 8 (0.0%)
                WILLIAM TOBIN (IND): 5 (0.0%)
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                Candidate("Boris Johnson", Party("Conservative", "CON", Color.BLUE)) to 23716,
                Candidate("Vincent Lo", Party("Labour", "LAB", Color.RED)) to 18682,
                Candidate("Rosina Robson", Party("Liberal Democrats", "LD", Color.ORANGE)) to 1835,
                Candidate("Lizzy Kemp", Party("UK Independence Party", "UKIP", Color.MAGENTA.darker())) to 1577,
                Candidate("Mark Keir", Party("Green", "GRN", Color.GREEN.darker())) to 884,
            ),
        )
        voteHeader.submit("2017 RESULT")
        compareRendering("SimpleVoteViewPanel", "LotsOfCandidates-1", panel)
        assertPublishes(
            panel.altText,
            """
                UXBRIDGE AND SOUTH RUISLIP
                
                2017 RESULT
                BORIS JOHNSON (CON): 23,716 (50.8%) WINNER
                VINCENT LO (LAB): 18,682 (40.0%)
                ROSINA ROBSON (LD): 1,835 (3.9%)
                LIZZY KEMP (UKIP): 1,577 (3.4%)
                MARK KEIR (GRN): 884 (1.9%)
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesLimit() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val currentVotes = Publisher(sequenceOf(ndp, pc, lib, grn).associateWith { 0 })
        val previousVotes = Publisher(
            mapOf(
                ndp.party to 585,
                pc.party to 785,
                lib.party to 1060,
                grn.party to 106,
            ),
        )
        val pctReporting = Publisher(0.0)
        val title = Publisher("MONTAGUE-KILMUIR")
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
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
                this.pctReporting = pctReporting
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            displayLimit = {
                limit = 3
            },
            map = createSingleResultMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                selectedShape = selectedDistrict
                this.leader = leader
                this.focus = focus
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "CandidateOthers-1", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                0 OF 9 POLLS REPORTING, WAITING FOR RESULTS... (CHANGE SINCE 2015)
                BILLY CANN (NDP): WAITING...
                CORY DEAGLE (PC): WAITING...
                OTHERS: WAITING...
                
                SWING SINCE 2015: NOT AVAILABLE
            """.trimIndent(),
        )

        winner.submit(lib)
        compareRendering("SimpleVoteViewPanel", "CandidateOthers-2", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                0 OF 9 POLLS REPORTING, WAITING FOR RESULTS... (CHANGE SINCE 2015)
                BILLY CANN (NDP): WAITING...
                DAPHNE GRIFFIN (LIB): WAITING... WINNER
                OTHERS: WAITING...
                
                SWING SINCE 2015: NOT AVAILABLE
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 124,
                pc to 1373,
                lib to 785,
                grn to 675,
            ),
        )
        voteHeader.submit("9 OF 9 POLLS REPORTING")
        voteSubhead.submit("PROJECTION: PC GAIN FROM LIB")
        pctReporting.submit(9.0 / 9)
        leader.submit(elected(pc.party))
        winner.submit(pc)
        compareRendering("SimpleVoteViewPanel", "CandidateOthers-3", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                9 OF 9 POLLS REPORTING, PROJECTION: PC GAIN FROM LIB (CHANGE SINCE 2015)
                CORY DEAGLE (PC): 1,373 (46.4%, +15.5%) WINNER
                DAPHNE GRIFFIN (LIB): 785 (26.5%, -15.3%)
                OTHERS: 799 (27.0%, -0.2%)
                
                SWING SINCE 2015: 15.4% SWING LIB TO PC
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesLimitWithMandatoryParties() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val currentVotes = Publisher(sequenceOf(ndp, pc, lib, grn).associateWith { 0 })
        val previousVotes = Publisher(
            mapOf(
                ndp.party to 585,
                pc.party to 785,
                lib.party to 1060,
                grn.party to 106,
            ),
        )
        val pctReporting = Publisher(0.0)
        val title = Publisher("MONTAGUE-KILMUIR")
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
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
                this.pctReporting = pctReporting
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            displayLimit = {
                limit = 3
                mandatoryParties = setOf(pc.party, lib.party)
            },
            map = createSingleResultMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                selectedShape = selectedDistrict
                this.leader = leader
                this.focus = focus
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "CandidateOthersMandatory-1", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                0 OF 9 POLLS REPORTING, WAITING FOR RESULTS... (CHANGE SINCE 2015)
                CORY DEAGLE (PC): WAITING...
                DAPHNE GRIFFIN (LIB): WAITING...
                OTHERS: WAITING...
                
                SWING SINCE 2015: NOT AVAILABLE
            """.trimIndent(),
        )

        winner.submit(lib)
        compareRendering("SimpleVoteViewPanel", "CandidateOthersMandatory-2", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                0 OF 9 POLLS REPORTING, WAITING FOR RESULTS... (CHANGE SINCE 2015)
                CORY DEAGLE (PC): WAITING...
                DAPHNE GRIFFIN (LIB): WAITING... WINNER
                OTHERS: WAITING...
                
                SWING SINCE 2015: NOT AVAILABLE
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 124,
                pc to 1373,
                lib to 785,
                grn to 675,
            ),
        )
        voteHeader.submit("9 OF 9 POLLS REPORTING")
        voteSubhead.submit("PROJECTION: PC GAIN FROM LIB")
        pctReporting.submit(9.0 / 9)
        leader.submit(elected(pc.party))
        winner.submit(pc)
        compareRendering("SimpleVoteViewPanel", "CandidateOthersMandatory-3", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                9 OF 9 POLLS REPORTING, PROJECTION: PC GAIN FROM LIB (CHANGE SINCE 2015)
                CORY DEAGLE (PC): 1,373 (46.4%, +15.5%) WINNER
                DAPHNE GRIFFIN (LIB): 785 (26.5%, -15.3%)
                OTHERS: 799 (27.0%, -0.2%)
                
                SWING SINCE 2015: 15.4% SWING LIB TO PC
            """.trimIndent(),
        )
    }

    @Test
    fun testPartiesNotRunningAgain() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Candidate("Jean-G\u00e9rard Chiasson", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Eric Mallet", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("Marie Leclerc", Party("Green", "GRN", Color.GREEN.darker()))
        val currentVotes = Publisher(
            mapOf(
                pc to 714,
                lib to 6834,
                grn to 609,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                ndp to 578,
                pc.party to 4048,
                lib.party to 3949,
            ),
        )
        val title = Publisher("SHIPPAGAN-LAM\u00c8QUE-MISCOU")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2018")
        val swingHeader = Publisher("SWING SINCE 2018")
        val winner = Publisher(lib)
        val swingPartyOrder = listOf(ndp, grn.party, lib.party, pc.party)
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartiesNotRunningAgain-1", panel)
        assertPublishes(
            panel.altText,
            """
                SHIPPAGAN-LAMÈQUE-MISCOU
                
                OFFICIAL RESULT (CHANGE SINCE 2018)
                ERIC MALLET (LIB): 6,834 (83.8%, +37.7%) WINNER
                JEAN-GÉRARD CHIASSON (PC): 714 (8.8%, -38.5%)
                MARIE LECLERC (GRN): 609 (7.5%, +7.5%)
                OTHERS: - (-6.7%)
                
                SWING SINCE 2018: 38.1% SWING PC TO LIB
            """.trimIndent(),
        )
    }

    @Test
    fun testPartiesNotRunningAgainPrevOthers() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Candidate("Jean-G\u00e9rard Chiasson", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Eric Mallet", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("Marie Leclerc", Party("Green", "GRN", Color.GREEN.darker()))
        val currentVotes = Publisher(
            mapOf(
                pc to 714,
                lib to 6834,
                grn to 609,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                Party.OTHERS to 289,
                ndp to 289,
                pc.party to 4048,
                lib.party to 3949,
                grn.party to 0,
            ),
        )
        val title = Publisher("SHIPPAGAN-LAM\u00c8QUE-MISCOU")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2018")
        val swingHeader = Publisher("SWING SINCE 2018")
        val winner = Publisher(lib)
        val swingPartyOrder = listOf(grn.party, lib.party, pc.party)
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartiesNotRunningAgain-1", panel)
        assertPublishes(
            panel.altText,
            """
                SHIPPAGAN-LAMÈQUE-MISCOU
                
                OFFICIAL RESULT (CHANGE SINCE 2018)
                ERIC MALLET (LIB): 6,834 (83.8%, +37.7%) WINNER
                JEAN-GÉRARD CHIASSON (PC): 714 (8.8%, -38.5%)
                MARIE LECLERC (GRN): 609 (7.5%, +7.5%)
                OTHERS: - (-6.7%)
                
                SWING SINCE 2018: 38.1% SWING PC TO LIB
            """.trimIndent(),
        )
    }

    @Test
    fun testPartiesNotRunningAgainOthers() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Candidate("Jean-G\u00e9rard Chiasson", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Eric Mallet", Party("Liberal", "LIB", Color.RED))
        val oth = Candidate.OTHERS
        val currentVotes = Publisher(
            mapOf(
                pc to 714,
                lib to 6834,
                oth to 609,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                ndp to 578,
                pc.party to 4048,
                lib.party to 3949,
            ),
        )
        val title = Publisher("SHIPPAGAN-LAM\u00c8QUE-MISCOU")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2018")
        val swingHeader = Publisher("SWING SINCE 2018")
        val winner = Publisher(lib)
        val swingPartyOrder = listOf(ndp, oth.party, lib.party, pc.party)
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartiesNotRunningAgainOthers-1", panel)
        assertPublishes(
            panel.altText,
            """
                SHIPPAGAN-LAMÈQUE-MISCOU
                
                OFFICIAL RESULT (CHANGE SINCE 2018)
                ERIC MALLET (LIB): 6,834 (83.8%, +37.7%) WINNER
                JEAN-GÉRARD CHIASSON (PC): 714 (8.8%, -38.5%)
                OTHERS: 609 (7.5%, +0.7%)
                
                SWING SINCE 2018: 38.1% SWING PC TO LIB
            """.trimIndent(),
        )
    }

    @Test
    fun testPartiesNotRunningAgainOthersPrevOthers() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Candidate("Jean-G\u00e9rard Chiasson", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Eric Mallet", Party("Liberal", "LIB", Color.RED))
        val oth = Candidate.OTHERS
        val currentVotes = Publisher(
            mapOf(
                pc to 714,
                lib to 6834,
                oth to 609,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                Party.OTHERS to 289,
                ndp to 289,
                pc.party to 4048,
                lib.party to 3949,
            ),
        )
        val title = Publisher("SHIPPAGAN-LAM\u00c8QUE-MISCOU")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2018")
        val swingHeader = Publisher("SWING SINCE 2018")
        val winner = Publisher(lib)
        val swingPartyOrder = listOf(ndp, oth.party, lib.party, pc.party)
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartiesNotRunningAgainOthers-1", panel)
        assertPublishes(
            panel.altText,
            """
                SHIPPAGAN-LAMÈQUE-MISCOU
                
                OFFICIAL RESULT (CHANGE SINCE 2018)
                ERIC MALLET (LIB): 6,834 (83.8%, +37.7%) WINNER
                JEAN-GÉRARD CHIASSON (PC): 714 (8.8%, -38.5%)
                OTHERS: 609 (7.5%, +0.7%)
                
                SWING SINCE 2018: 38.1% SWING PC TO LIB
            """.trimIndent(),
        )
    }

    @Test
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
        val currentVotes = Publisher(mapOf(con to 37035, ld to 16624, lab to 7638, bxp to 1286, ind to 681, ed to 194))
        val previousVotes = Publisher(mapOf(spkr to 34299, grn to 8574, ind.party to 5638, ukip to 4168))
        val title = Publisher("BUCKINGHAM")
        val voteHeader = Publisher("OFFICIAL RESULT")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2017")
        val changeSubhead = Publisher(null)
        val swingHeader = Publisher("SWING SINCE 2017")
        val winner = Publisher(con)
        val swingPartyOrder = listOf(lab.party, ld.party, con.party, bxp.party)
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                subhead = changeSubhead
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
                winnerNotRunningAgain = {
                    subhead = "NOT APPLICABLE: PREVIOUSLY SPEAKER'S SEAT".asOneTimePublisher()
                }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "WinningPartyNotRunningAgain-1", panel)
        assertPublishes(
            panel.altText,
            """
                BUCKINGHAM
                
                OFFICIAL RESULT (CHANGE SINCE 2017 NOT APPLICABLE: PREVIOUSLY SPEAKER'S SEAT)
                GREG SMITH (CON): 37,035 (58.4%) WINNER
                STEPHEN DORRELL (LD): 16,624 (26.2%)
                DAVID MORGAN (LAB): 7,638 (12.0%)
                ANDREW BELL (BXP): 1,286 (2.0%)
                NED THOMPSON (IND): 681 (1.1%)
                ANTONIO VITIELLO (ED): 194 (0.3%)
                
                SWING SINCE 2017: NOT AVAILABLE
            """.trimIndent(),
        )

        previousVotes.submit(mapOf(con.party to 27748, lab.party to 9619, ld.party to 9508, ukip to 1432))
        changeHeader.submit("CHANGE SINCE 2005")
        swingHeader.submit("SWING SINCE 2005")
        compareRendering("SimpleVoteViewPanel", "WinningPartyNotRunningAgain-2", panel)
        assertPublishes(
            panel.altText,
            """
                BUCKINGHAM
                
                OFFICIAL RESULT (CHANGE SINCE 2005)
                GREG SMITH (CON): 37,035 (58.4%, +0.9%) WINNER
                STEPHEN DORRELL (LD): 16,624 (26.2%, +6.5%)
                DAVID MORGAN (LAB): 7,638 (12.0%, -7.9%)
                ANDREW BELL (BXP): 1,286 (2.0%, +2.0%)
                NED THOMPSON (IND): 681 (1.1%, +1.1%)
                ANTONIO VITIELLO (ED): 194 (0.3%, +0.3%)
                OTHERS: - (-3.0%)
                
                SWING SINCE 2005: 2.8% SWING CON TO LD
            """.trimIndent(),
        )
    }

    @Test
    fun testRunoffMode() {
        val nupes = Party("New Ecological and Social People's Union", "NUPES", Color.RED)
        val lr = Party("The Republicans", "LR", Color.BLUE)
        val modem = Party("Democratic Movement", "MODEM", Color.ORANGE)
        val ens = Party("Together", "ENS", Color.YELLOW)
        val rn = Party("National Rally", "RN", Color.BLUE.darker())
        val swingPartyOrder = listOf(nupes, modem, ens, lr, rn)

        val curr =
            Publisher(mapOf(Candidate("Nicolas Dragos", rn) to 17058, Candidate("Aude Bono-Vandrome", ens) to 14208))
        val prev = Publisher(mapOf(ens to 16684, rn to 12994))
        val constituency = Publisher("AISNE 1st CONSTITUENCY")

        val panel = candidateVotes(
            current = {
                votes = curr
                header = "SECOND ROUND RESULT".asOneTimePublisher()
                subhead = "100.0% REPORTING".asOneTimePublisher()
            },
            prev = {
                votes = prev
                header = "CHANGE SINCE 2018".asOneTimePublisher()
                swing = {
                    partyOrder = swingPartyOrder
                    header = "SWING SINCE 2018".asOneTimePublisher()
                }
                runoff = {
                    subhead = "NOT APPLICABLE: DIFFERENT PARTIES IN RUNOFF".asOneTimePublisher()
                }
            },
            title = constituency,
        )
        panel.size = Dimension(1024, 512)
        compareRendering("SimpleVoteViewPanel", "RunoffMode-1", panel)
        assertPublishes(
            panel.altText,
            """
                AISNE 1st CONSTITUENCY
                
                SECOND ROUND RESULT, 100.0% REPORTING (CHANGE SINCE 2018)
                NICOLAS DRAGOS (RN): 17,058 (54.6%, +10.8%)
                AUDE BONO-VANDROME (ENS): 14,208 (45.4%, -10.8%)
                
                SWING SINCE 2018: 10.8% SWING ENS TO RN
            """.trimIndent(),
        )

        curr.submit(mapOf(Candidate("Xavier Breton", lr) to 24407, Candidate("Sebastien Gueraud", nupes) to 14202))
        prev.submit(mapOf(modem to 15114, lr to 17564))
        constituency.submit("AIN 1st CONSTITUENCY")
        compareRendering("SimpleVoteViewPanel", "RunoffMode-2", panel)
        assertPublishes(
            panel.altText,
            """
                AIN 1st CONSTITUENCY
                
                SECOND ROUND RESULT, 100.0% REPORTING (CHANGE SINCE 2018 NOT APPLICABLE: DIFFERENT PARTIES IN RUNOFF)
                XAVIER BRETON (LR): 24,407 (63.2%)
                SEBASTIEN GUERAUD (NUPES): 14,202 (36.8%)
                
                SWING SINCE 2018: NOT AVAILABLE
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidateRunoffSingleLine() {
        val macron = Candidate("Emmanuel Macron", Party("En Marche!", "EM", Color.ORANGE))
        val lePen = Candidate("Marine Le Pen", Party("National Front", "FN", Color.BLUE.darker()))
        val fillon = Candidate("Fran\u00e7ois Fillon", Party("The Republicans", "LR", Color.BLUE))
        val melenchon = Candidate("Jean-Luc M\u00e9lenchon", Party("La France Insoumise", "FI", Color.ORANGE.darker()))
        val hamon = Candidate("Beno\u00eet Hamon", Party("Socialist Party", "PS", Color.RED))
        val dupontAignan = Candidate("Nicolas Dupont-Aignan", Party("Debout la France", "DLF", Color.CYAN.darker()))
        val lasalle = Candidate("Jean Lasalle", Party("R\u00e9sistons!", "R\u00c9S", Color.CYAN))
        val poutou = Candidate("Philippe Poutou", Party("New Anticapitalist Party", "NPA", Color.RED.darker()))
        val asselineau = Candidate(
            "Fran\u00e7ois Asselineau",
            Party("Popular Republican Union", "UPR", Color.CYAN.darker().darker()),
        )
        val arthaud = Candidate("Nathalie Arthaud", Party("Lutte Ouvri\u00e8re", "LO", Color.RED))
        val cheminade = Candidate("Jacques Cheminade", Party("Solidarity and Progress", "S&P", Color.GRAY))
        val currentVotes = Publisher(
            mapOf(
                macron to 8656346,
                lePen to 7678491,
                fillon to 7212995,
                melenchon to 7059951,
                hamon to 2291288,
                dupontAignan to 1695000,
                lasalle to 435301,
                poutou to 394505,
                asselineau to 332547,
                arthaud to 232384,
                cheminade to 65586,
            ),
        )
        val title = Publisher("ELECTION 2017: FRANCE DECIDES")
        val voteHeader = Publisher("FIRST ROUND RESULT")
        val voteSubhead = Publisher("")
        val runoff = Publisher<Set<Candidate>?>(null)
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.runoff = runoff
            },
            winningLine = {
                majority { "50% TO WIN" }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "CandidateRunoffSingleLine-1", panel)
        assertPublishes(
            panel.altText,
            """
                ELECTION 2017: FRANCE DECIDES
                
                FIRST ROUND RESULT
                EMMANUEL MACRON (EM): 8,656,346 (24.0%)
                MARINE LE PEN (FN): 7,678,491 (21.3%)
                FRANÇOIS FILLON (LR): 7,212,995 (20.0%)
                JEAN-LUC MÉLENCHON (FI): 7,059,951 (19.6%)
                BENOÎT HAMON (PS): 2,291,288 (6.4%)
                NICOLAS DUPONT-AIGNAN (DLF): 1,695,000 (4.7%)
                JEAN LASALLE (RÉS): 435,301 (1.2%)
                PHILIPPE POUTOU (NPA): 394,505 (1.1%)
                FRANÇOIS ASSELINEAU (UPR): 332,547 (0.9%)
                NATHALIE ARTHAUD (LO): 232,384 (0.6%)
                JACQUES CHEMINADE (S&P): 65,586 (0.2%)
                50% TO WIN
            """.trimIndent(),
        )

        runoff.submit(setOf(macron, lePen))
        compareRendering("SimpleVoteViewPanel", "CandidateRunoffSingleLine-2", panel)
        assertPublishes(
            panel.altText,
            """
                ELECTION 2017: FRANCE DECIDES
                
                FIRST ROUND RESULT
                EMMANUEL MACRON (EM): 8,656,346 (24.0%) RUNOFF
                MARINE LE PEN (FN): 7,678,491 (21.3%) RUNOFF
                FRANÇOIS FILLON (LR): 7,212,995 (20.0%)
                JEAN-LUC MÉLENCHON (FI): 7,059,951 (19.6%)
                BENOÎT HAMON (PS): 2,291,288 (6.4%)
                NICOLAS DUPONT-AIGNAN (DLF): 1,695,000 (4.7%)
                JEAN LASALLE (RÉS): 435,301 (1.2%)
                PHILIPPE POUTOU (NPA): 394,505 (1.1%)
                FRANÇOIS ASSELINEAU (UPR): 332,547 (0.9%)
                NATHALIE ARTHAUD (LO): 232,384 (0.6%)
                JACQUES CHEMINADE (S&P): 65,586 (0.2%)
                50% TO WIN
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidateRunoffDualLine() {
        val macron = Candidate("Emmanuel Macron", Party("En Marche!", "EM", Color.ORANGE))
        val lePen = Candidate("Marine Le Pen", Party("National Front", "FN", Color.BLUE.darker()))
        val fillon = Candidate("Fran\u00e7ois Fillon", Party("The Republicans", "LR", Color.BLUE))
        val melenchon = Candidate("Jean-Luc M\u00e9lenchon", Party("La France Insoumise", "FI", Color.ORANGE.darker()))
        val hamon = Candidate("Beno\u00eet Hamon", Party("Socialist Party", "PS", Color.RED))
        val dupontAignan = Candidate("Nicolas Dupont-Aignan", Party("Debout la France", "DLF", Color.CYAN.darker()))
        val lasalle = Candidate("Jean Lasalle", Party("R\u00e9sistons!", "R\u00c9S", Color.CYAN))
        val poutou = Candidate("Philippe Poutou", Party("New Anticapitalist Party", "NPA", Color.RED.darker()))
        val asselineau = Candidate(
            "Fran\u00e7ois Asselineau",
            Party("Popular Republican Union", "UPR", Color.CYAN.darker().darker()),
        )
        val arthaud = Candidate("Nathalie Arthaud", Party("Lutte Ouvri\u00e8re", "LO", Color.RED))
        val cheminade = Candidate("Jacques Cheminade", Party("Solidarity and Progress", "S&P", Color.GRAY))
        val currentVotes = Publisher(
            topAndOthers(
                mapOf(
                    macron to 8656346,
                    lePen to 7678491,
                    fillon to 7212995,
                    melenchon to 7059951,
                    hamon to 2291288,
                    dupontAignan to 1695000,
                    lasalle to 435301,
                    poutou to 394505,
                    asselineau to 332547,
                    arthaud to 232384,
                    cheminade to 65586,
                ),
                6,
                Candidate.OTHERS,
            ),
        )
        val title = Publisher("ELECTION 2017: FRANCE DECIDES")
        val voteHeader = Publisher("FIRST ROUND RESULT")
        val voteSubhead = Publisher("")
        val runoff = Publisher<Set<Candidate>?>(null)
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.runoff = runoff
            },
            winningLine = {
                majority { "50% TO WIN" }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "CandidateRunoffDualLine-1", panel)
        assertPublishes(
            panel.altText,
            """
                ELECTION 2017: FRANCE DECIDES
                
                FIRST ROUND RESULT
                EMMANUEL MACRON (EM): 8,656,346 (24.0%)
                MARINE LE PEN (FN): 7,678,491 (21.3%)
                FRANÇOIS FILLON (LR): 7,212,995 (20.0%)
                JEAN-LUC MÉLENCHON (FI): 7,059,951 (19.6%)
                BENOÎT HAMON (PS): 2,291,288 (6.4%)
                OTHERS: 3,155,323 (8.8%)
                50% TO WIN
            """.trimIndent(),
        )

        runoff.submit(setOf(macron, lePen))
        compareRendering("SimpleVoteViewPanel", "CandidateRunoffDualLine-2", panel)
        assertPublishes(
            panel.altText,
            """
                ELECTION 2017: FRANCE DECIDES
                
                FIRST ROUND RESULT
                EMMANUEL MACRON (EM): 8,656,346 (24.0%) RUNOFF
                MARINE LE PEN (FN): 7,678,491 (21.3%) RUNOFF
                FRANÇOIS FILLON (LR): 7,212,995 (20.0%)
                JEAN-LUC MÉLENCHON (FI): 7,059,951 (19.6%)
                BENOÎT HAMON (PS): 2,291,288 (6.4%)
                OTHERS: 3,155,323 (8.8%)
                50% TO WIN
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesResultMidDeclaration() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val currentVotes: Publisher<Map<Candidate, Int?>> =
            Publisher(sequenceOf(ndp, pc, lib, grn).associateWith { null })
        val previousVotes = Publisher(
            mapOf(
                ndp.party to 585,
                pc.party to 785,
                lib.party to 1060,
                grn.party to 106,
            ),
        )
        val title = Publisher("MONTAGUE-KILMUIR")
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
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            map = createSinglePartyMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                selectedShape = selectedDistrict
                this.leader = leader
                this.focus = focus
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-1", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                OFFICIAL RESULT (CHANGE SINCE 2015)
                BILLY CANN (NDP): WAITING...
                CORY DEAGLE (PC): WAITING...
                DAPHNE GRIFFIN (LIB): WAITING...
                JOHN ALLEN MACLEAN (GRN): WAITING...
                
                SWING SINCE 2015: NOT AVAILABLE
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 124,
                pc to null,
                lib to null,
                grn to null,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-2", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                OFFICIAL RESULT (CHANGE SINCE 2015)
                BILLY CANN (NDP): 124
                CORY DEAGLE (PC): WAITING...
                DAPHNE GRIFFIN (LIB): WAITING...
                JOHN ALLEN MACLEAN (GRN): WAITING...
                
                SWING SINCE 2015: NOT AVAILABLE
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 124,
                pc to 1373,
                lib to null,
                grn to null,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-3", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                OFFICIAL RESULT (CHANGE SINCE 2015)
                CORY DEAGLE (PC): 1,373
                BILLY CANN (NDP): 124
                DAPHNE GRIFFIN (LIB): WAITING...
                JOHN ALLEN MACLEAN (GRN): WAITING...
                
                SWING SINCE 2015: NOT AVAILABLE
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 124,
                pc to 1373,
                lib to 785,
                grn to null,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-4", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                OFFICIAL RESULT (CHANGE SINCE 2015)
                CORY DEAGLE (PC): 1,373
                DAPHNE GRIFFIN (LIB): 785
                BILLY CANN (NDP): 124
                JOHN ALLEN MACLEAN (GRN): WAITING...
                
                SWING SINCE 2015: NOT AVAILABLE
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 124,
                pc to 1373,
                lib to 785,
                grn to 675,
            ),
        )
        leader.submit(pc.party)
        compareRendering("SimpleVoteViewPanel", "MidDeclaration-5", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                OFFICIAL RESULT (CHANGE SINCE 2015)
                CORY DEAGLE (PC): 1,373 (46.4%, +15.5%)
                DAPHNE GRIFFIN (LIB): 785 (26.5%, -15.3%)
                JOHN ALLEN MACLEAN (GRN): 675 (22.8%, +18.6%)
                BILLY CANN (NDP): 124 (4.2%, -18.9%)
                
                SWING SINCE 2015: 15.4% SWING LIB TO PC
            """.trimIndent(),
        )
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
            Candidate("Roger Woodward", ind) to 495,
        )
        val curr2CP = mapOf(
            Candidate("Julian Leeser", lib, true) to 61675,
            Candidate("Katie Gompertz", alp) to 32272,
        )
        val prev = mapOf(
            lib to 53678,
            alp to 18693,
            grn to 10815,
            Party.OTHERS to 5213 + 2859 + 1933 + 826,
        )
        val prev2CP = mapOf(
            lib to 62470,
            alp to 31547,
        )
        val panel = candidateVotes(
            current = {
                votes = curr.asOneTimePublisher()
                header = "PRIMARY VOTE".asOneTimePublisher()
                subhead = "100% REPORTING".asOneTimePublisher()
                winner = Candidate("Julian Leeser", lib, true).asOneTimePublisher()
            },
            prev = {
                votes = prev.asOneTimePublisher()
                header = "CHANGE SINCE 2016".asOneTimePublisher()
            },
            preferences = {
                current = {
                    votes = curr2CP.asOneTimePublisher()
                    header = "TWO CANDIDATE PREFERRED".asOneTimePublisher()
                    subhead = "100% REPORTING".asOneTimePublisher()
                }
                this.prev = {
                    votes = prev2CP.asOneTimePublisher()
                    swing = {
                        partyOrder = listOf(alp, Party.OTHERS, lib)
                        header = "SWING SINCE 2016".asOneTimePublisher()
                    }
                }
            },
            title = "BEROWRA".asOneTimePublisher(),
        )
            .also {
                it.setSize(1024, 512)
                compareRendering("SimpleVoteViewPanel", "NewPartiesCandidatesMergedWithPrevOthers", it)
            }
        assertPublishes(
            panel.altText,
            """
                BEROWRA
                
                PRIMARY VOTE, 100% REPORTING (CHANGE SINCE 2016)
                JULIAN LEESER (LIB): 53,741 (57.2%, +0.1%) WINNER
                KATIE GOMPERTZ (ALP): 19,821 (21.1%, +1.2%)
                MONICA TAN (GRN): 11,157 (11.9%, +0.4%)
                SIMON TAYLOR (CDP): 2,163 (2.3%, *)
                MICK GALLAGHER (IND): 2,104 (2.2%, *)
                CRAIG MCLAUGHLIN (UAP): 1,576 (1.7%, *)
                BRENDAN CLARKE (SCI): 1,465 (1.6%, *)
                JUSTIN THOMAS (SUS): 1,425 (1.5%, *)
                ROGER WOODWARD (IND): 495 (0.5%, *)
                OTHERS: - (-1.7%)
                * CHANGE INCLUDED IN OTHERS
                
                TWO CANDIDATE PREFERRED, 100% REPORTING
                JULIAN LEESER (LIB): 61,675 (65.6%) WINNER
                KATIE GOMPERTZ (ALP): 32,272 (34.4%)
                
                SWING SINCE 2016: 0.8% SWING LIB TO ALP
            """.trimIndent(),
        )
    }

    @Test
    fun testProgressLabel() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val currentVotes = Publisher(sequenceOf(ndp, pc, lib, grn).associateWith { 0 })
        val previousVotes = Publisher(
            mapOf(
                ndp.party to 585,
                pc.party to 785,
                lib.party to 1060,
                grn.party to 106,
            ),
        )
        val pctReporting = Publisher(0.0)
        val title = Publisher("MONTAGUE-KILMUIR")
        val voteHeader = Publisher("PROVISIONAL RESULTS")
        val progressLabel = Publisher("0/9")
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
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
                this.pctReporting = pctReporting
                this.progressLabel = progressLabel
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            map = createSingleResultMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                selectedShape = selectedDistrict
                this.leader = leader
                this.focus = focus
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "ProgressLabel-1", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                PROVISIONAL RESULTS [0/9], WAITING FOR RESULTS... (CHANGE SINCE 2015)
                BILLY CANN (NDP): WAITING...
                CORY DEAGLE (PC): WAITING...
                DAPHNE GRIFFIN (LIB): WAITING...
                JOHN ALLEN MACLEAN (GRN): WAITING...
                
                SWING SINCE 2015: NOT AVAILABLE
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 5,
                pc to 47,
                lib to 58,
                grn to 52,
            ),
        )
        progressLabel.submit("1/9")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(1.0 / 9)
        leader.submit(leading(lib.party))
        compareRendering("SimpleVoteViewPanel", "ProgressLabel-2", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                PROVISIONAL RESULTS [1/9], PROJECTION: TOO EARLY TO CALL (CHANGE SINCE 2015)
                DAPHNE GRIFFIN (LIB): 58 (35.8%, -6.0%)
                JOHN ALLEN MACLEAN (GRN): 52 (32.1%, +27.9%)
                CORY DEAGLE (PC): 47 (29.0%, -1.9%)
                BILLY CANN (NDP): 5 (3.1%, -20.0%)
                
                SWING SINCE 2015: 17.0% SWING LIB TO GRN
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 8,
                pc to 91,
                lib to 100,
                grn to 106,
            ),
        )
        progressLabel.submit("2/9")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(2.0 / 9)
        leader.submit(leading(grn.party))
        compareRendering("SimpleVoteViewPanel", "ProgressLabel-3", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                PROVISIONAL RESULTS [2/9], PROJECTION: TOO EARLY TO CALL (CHANGE SINCE 2015)
                JOHN ALLEN MACLEAN (GRN): 106 (34.8%, +30.6%)
                DAPHNE GRIFFIN (LIB): 100 (32.8%, -9.0%)
                CORY DEAGLE (PC): 91 (29.8%, -1.1%)
                BILLY CANN (NDP): 8 (2.6%, -20.4%)
                
                SWING SINCE 2015: 19.8% SWING LIB TO GRN
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 18,
                pc to 287,
                lib to 197,
                grn to 243,
            ),
        )
        progressLabel.submit("5/9")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(5.0 / 9)
        leader.submit(leading(pc.party))
        compareRendering("SimpleVoteViewPanel", "ProgressLabel-4", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                PROVISIONAL RESULTS [5/9], PROJECTION: TOO EARLY TO CALL (CHANGE SINCE 2015)
                CORY DEAGLE (PC): 287 (38.5%, +7.6%)
                JOHN ALLEN MACLEAN (GRN): 243 (32.6%, +28.4%)
                DAPHNE GRIFFIN (LIB): 197 (26.4%, -15.4%)
                BILLY CANN (NDP): 18 (2.4%, -20.7%)
                
                SWING SINCE 2015: 11.5% SWING LIB TO PC
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 124,
                pc to 1373,
                lib to 785,
                grn to 675,
            ),
        )
        progressLabel.submit("9/9")
        voteSubhead.submit("PROJECTION: PC GAIN FROM LIB")
        pctReporting.submit(9.0 / 9)
        leader.submit(elected(pc.party))
        winner.submit(pc)
        compareRendering("SimpleVoteViewPanel", "ProgressLabel-5", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                PROVISIONAL RESULTS [9/9], PROJECTION: PC GAIN FROM LIB (CHANGE SINCE 2015)
                CORY DEAGLE (PC): 1,373 (46.4%, +15.5%) WINNER
                DAPHNE GRIFFIN (LIB): 785 (26.5%, -15.3%)
                JOHN ALLEN MACLEAN (GRN): 675 (22.8%, +18.6%)
                BILLY CANN (NDP): 124 (4.2%, -18.9%)
                
                SWING SINCE 2015: 15.4% SWING LIB TO PC
            """.trimIndent(),
        )
    }

    @Test
    fun testSwingRange() {
        val ndp = Candidate("Billy Cann", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Cory Deagle", Party("Progressive Conservative", "PC", Color.BLUE))
        val lib = Candidate("Daphne Griffin", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("John Allen MacLean", Party("Green", "GRN", Color.GREEN.darker()))
        val currentVotes = Publisher(
            mapOf(
                ndp to 124,
                pc to 1373,
                lib to 785,
                grn to 674,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                ndp.party to 585,
                pc.party to 785,
                lib.party to 1060,
                grn.party to 106,
            ),
        )
        val title = Publisher("MONTAGUE-KILMUIR")
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
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                    range = 0.2.asOneTimePublisher()
                }
            },
            map = createSinglePartyMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                selectedShape = selectedDistrict
                this.leader = leader
                this.focus = focus
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "SwingRange", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                9 OF 9 POLLS REPORTING, PROJECTION: PC GAIN FROM LIB (CHANGE SINCE 2015)
                CORY DEAGLE (PC): 1,373 (46.4%, +15.5%)
                DAPHNE GRIFFIN (LIB): 785 (26.6%, -15.2%)
                JOHN ALLEN MACLEAN (GRN): 674 (22.8%, +18.6%)
                BILLY CANN (NDP): 124 (4.2%, -18.9%)
                
                SWING SINCE 2015: 15.4% SWING LIB TO PC
            """.trimIndent(),
        )
    }

    @Test
    fun testFadedMap() {
        val ndp = Candidate("Campbell Webster", Party("New Democratic Party", "NDP", Color.ORANGE))
        val pc = Candidate("Zack Bell", Party("Progressive Conservative", "PC", Color.BLUE), incumbent = true)
        val lib = Candidate("Judy Hughes", Party("Liberal", "LIB", Color.RED))
        val grn = Candidate("Charles Sanderson", Party("Green", "GRN", Color.GREEN.darker()))
        val ind = Candidate("Georgina Bassett", Party("Independent", "IND", Party.OTHERS.color))
        val currentVotes = Publisher(
            mapOf(
                pc to 1861,
                grn to 553,
                lib to 540,
                ndp to 78,
                ind to 41,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                lib.party to 1420,
                grn.party to 1057,
                pc.party to 865,
                ndp.party to 41,
            ),
        )
        val title = Publisher("CHARLOTTETOWN-WINSLOE")
        val voteHeader = Publisher("9 OF 9 POLLS REPORTING")
        val voteSubhead = Publisher("PROJECTION: PC GAIN FROM LIB")
        val changeHeader = Publisher("CHANGE SINCE 2019")
        val swingHeader = Publisher("SWING SINCE 2019")
        val mapHeader = Publisher("CHARLOTTETOWN")
        val leader = Publisher(pc.party)
        val swingPartyOrder = listOf(ndp.party, grn.party, lib.party, pc.party)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { (9..14).contains(it) })
        val faded = Publisher(shapesByDistrict.keys.filter { it <= 8 || it >= 21 })
        val selectedDistrict = Publisher(3)
        val panel = candidateVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                incumbentMarker = "MLA"
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = swingPartyOrder
                    header = swingHeader
                }
            },
            map = createSinglePartyMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                selectedShape = selectedDistrict
                this.leader = leader
                this.focus = focus
                this.faded = faded
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "FadedMap-1", panel)
        assertPublishes(
            panel.altText,
            """
                CHARLOTTETOWN-WINSLOE

                9 OF 9 POLLS REPORTING, PROJECTION: PC GAIN FROM LIB (CHANGE SINCE 2019)
                ZACK BELL [MLA] (PC): 1,861 (60.6%, +35.0%)
                CHARLES SANDERSON (GRN): 553 (18.0%, -13.2%)
                JUDY HUGHES (LIB): 540 (17.6%, -24.4%)
                CAMPBELL WEBSTER (NDP): 78 (2.5%, +1.3%)
                GEORGINA BASSETT (IND): 41 (1.3%, +1.3%)
                
                SWING SINCE 2019: 29.7% SWING LIB TO PC
            """.trimIndent(),
        )
    }

    @Test
    fun testMultiWinners() {
        val lab = Party("Labour", "LAB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ld = Party("Liberal Democrats", "LD", Color.ORANGE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ukip = Party("UK Independence Party", "UKIP", Color.MAGENTA.darker())

        val curr = mapOf(
            Candidate("Ruth Hayes", lab) to 1352,
            Candidate("Ben Mackmurdie", lab, true) to 1209,
            Candidate("Matt Nathan", lab, true) to 1199,
            Candidate("Bronwen James", grn) to 473,
            Candidate("George Allen", ld) to 458,
            Candidate("Janet Gormley", grn) to 453,
            Candidate("Helen Redesdale", ld) to 416,
            Candidate("Alexander Baker", con) to 406,
            Candidate("Jason Vickers", ld) to 405,
            Candidate("Lewis Cox", con) to 404,
            Candidate("Mags Joseph", con) to 379,
            Candidate("Cecilie Hestbaek", grn) to 340,
        )
        val prev = mapOf(
            lab to (1568 + 1487 + 1471),
            ld to (479 + 409 + 355),
            con to (376 + 372 + 356),
            grn to (358 + 310 + 267),
            ukip to 81,
        )
        val winners = curr.entries.sortedByDescending { it.value }.take(3).map { it.key }.toSet()

        val panel = candidateVotes(
            current = {
                votes = curr.asOneTimePublisher()
                header = "2022 RESULT".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
                this.winners = winners.asOneTimePublisher()
                incumbentMarker = "INC"
            },
            prev = {
                votes = prev.asOneTimePublisher()
                header = "CHANGE SINCE 2018".asOneTimePublisher()
                swing = {
                    partyOrder = listOf(grn, lab, ld, con, ukip)
                    header = "SWING SINCE 2018".asOneTimePublisher()
                }
            },
            title = "CLERKENWELL".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "MultiWinners-1", panel)
        assertPublishes(
            panel.altText,
            """
                CLERKENWELL

                2022 RESULT (CHANGE SINCE 2018)
                RUTH HAYES (LAB): 1,352 (18.0%, -7.2%^) WINNER
                BEN MACKMURDIE [INC] (LAB): 1,209 (16.1%, ^) WINNER
                MATT NATHAN [INC] (LAB): 1,199 (16.0%, ^) WINNER
                BRONWEN JAMES (GRN): 473 (6.3%, +5.0%^)
                GEORGE ALLEN (LD): 458 (6.1%, +1.3%^)
                JANET GORMLEY (GRN): 453 (6.0%, ^)
                HELEN REDESDALE (LD): 416 (5.6%, ^)
                ALEXANDER BAKER (CON): 406 (5.4%, +1.9%^)
                JASON VICKERS (LD): 405 (5.4%, ^)
                LEWIS COX (CON): 404 (5.4%, ^)
                MAGS JOSEPH (CON): 379 (5.1%, ^)
                CECILIE HESTBAEK (GRN): 340 (4.5%, ^)
                OTHERS: - (-1.0%)
                ^ AGGREGATED ACROSS CANDIDATES IN PARTY
                
                SWING SINCE 2018: 4.3% SWING LAB TO LD
            """.trimIndent(),
        )
    }

    @Test
    fun testVoteBasedWinningLine() {
        val yes = Candidate("Yes", Party("to reduce voting age to 18", "to reduce voting age to 18", Color.CYAN.darker()))
        val no = Candidate("No", Party("to keep voting age at 20", "to keep voting age at 20", Color.YELLOW.darker()))

        val votes = Publisher(mapOf(yes to 0, no to 0))
        val pctReporting = Publisher(0.0)

        val panel = candidateVotes(
            current = {
                this.votes = votes
                header = "REFERENDUM RESULT".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
                this.progressLabel = pctReporting.map { "${DecimalFormat("0.0%").format(it)} IN" }
                this.pctReporting = pctReporting
            },
            winningLine = {
                votes(9619696.asOneTimePublisher()) { "9,619,696 NEEDED TO PASS" }
            },
            title = "TAIWAN".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "VoteWinningLine-1", panel)
        assertPublishes(
            panel.altText,
            """
                TAIWAN

                REFERENDUM RESULT [0.0% IN]
                YES (TO REDUCE VOTING AGE TO 18): WAITING...
                NO (TO KEEP VOTING AGE AT 20): WAITING...
                9,619,696 NEEDED TO PASS
            """.trimIndent(),
        )

        votes.submit(mapOf(yes to 5647102, no to 5016427))
        pctReporting.submit(1.0)
        compareRendering("SimpleVoteViewPanel", "VoteWinningLine-2", panel)
        assertPublishes(
            panel.altText,
            """
                TAIWAN

                REFERENDUM RESULT [100.0% IN]
                YES (TO REDUCE VOTING AGE TO 18): 5,647,102 (53.0%)
                NO (TO KEEP VOTING AGE AT 20): 5,016,427 (47.0%)
                9,619,696 NEEDED TO PASS
            """.trimIndent(),
        )

        votes.submit(mapOf(yes to 564710, no to 501642))
        pctReporting.submit(0.1)
        compareRendering("SimpleVoteViewPanel", "VoteWinningLine-3", panel)
        assertPublishes(
            panel.altText,
            """
                TAIWAN

                REFERENDUM RESULT [10.0% IN]
                YES (TO REDUCE VOTING AGE TO 18): 564,710 (53.0%)
                NO (TO KEEP VOTING AGE AT 20): 501,642 (47.0%)
                9,619,696 NEEDED TO PASS
            """.trimIndent(),
        )
    }

    @Test
    fun testCombinedWinningLine() {
        val rn = Party("National Rally", "RN", Color.BLUE)
        val nfp = Party("New Popular Front", "NFP", Color.RED)
        val ens = Party("Together", "ENS", Color.YELLOW)

        val curr = Publisher(
            mapOf(
                Candidate("Marine Le Pen", rn) to 21219,
                Candidate("Marine Tondelier", nfp) to 9214,
                Candidate("Alexandrine Pintus", ens) to 4846,
                Candidate.OTHERS to 4045,
            ),
        )
        val year = Publisher(2022)
        val electorate = Publisher(86843)

        val panel = candidateVotes(
            current = {
                this.votes = curr
                header = year.map { "$it RESULT" }
                subhead = "".asOneTimePublisher()
            },
            winningLine = {
                (majority and votes(electorate.map { ceil(it.toDouble() * 0.25).toInt() })) { "50% AND ${DecimalFormat("#,##0").format(votes)} VOTES TO WIN" }
            },
            title = "PAS DE CALAIS 11".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "CombinedWinningLine-1", panel)
        assertPublishes(
            panel.altText,
            """
                PAS DE CALAIS 11

                2022 RESULT
                MARINE LE PEN (RN): 21,219 (54.0%)
                MARINE TONDELIER (NFP): 9,214 (23.4%)
                ALEXANDRINE PINTUS (ENS): 4,846 (12.3%)
                OTHERS: 4,045 (10.3%)
                50% AND 21,711 VOTES TO WIN
            """.trimIndent(),
        )
    }

    @Test
    fun testMultiWinningLine() {
        val rn = Party("National Rally", "RN", Color.BLUE)
        val nfp = Party("New Popular Front", "NFP", Color.RED)
        val ens = Party("Together", "ENS", Color.YELLOW)

        val curr = Publisher(
            mapOf(
                Candidate("Marine Le Pen", rn) to 21219,
                Candidate("Marine Tondelier", nfp) to 9214,
                Candidate("Alexandrine Pintus", ens) to 4846,
                Candidate.OTHERS to 4045,
            ),
        )
        val year = Publisher(2022)
        val electorate = Publisher(86843)

        val panel = candidateVotes(
            current = {
                this.votes = curr
                header = year.map { "$it RESULT" }
                subhead = "".asOneTimePublisher()
            },
            winningLine = {
                majority { "50%" }
                votes(electorate.map { ceil(it.toDouble() * 0.25).toInt() }) { DecimalFormat("#,##0").format(votes) }
            },
            title = "PAS DE CALAIS 11".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "MultiWinningLine-1", panel)
        assertPublishes(
            panel.altText,
            """
                PAS DE CALAIS 11

                2022 RESULT
                MARINE LE PEN (RN): 21,219 (54.0%)
                MARINE TONDELIER (NFP): 9,214 (23.4%)
                ALEXANDRINE PINTUS (ENS): 4,846 (12.3%)
                OTHERS: 4,045 (10.3%)
                50%
                21,711
            """.trimIndent(),
        )
    }

    @Test
    fun testPartiesConsolidatedInDiffIfTooMany() {
        val cpa = Party("Christian Peoples Alliance", "CPA", Color(148, 0, 170))
        val greenSoc = Party("Alliance for Green Socialism", "AGS", Color(0, 137, 91))
        val independent = Party("Independent", "IND", Party.OTHERS.color)
        val workers = Party("Workers Party", "WP", Color(215, 13, 13))
        val libdem = Party("Liberal Democrats", "LD", Color(0xfaa01a))
        val englishDem = Party("English Democrats", "ED", Color(140, 15, 15))
        val mrlp = Party("Monster Raving Loony Party", "MRLP", Color(255, 240, 0))
        val heritage = Party("Heritage Party", "HERITAGE", Color(13, 0, 173))
        val labour = Party("Labour", "LAB", Color(0xe4003b))
        val sdp = Party("Social Democratic Party", "SDP", Color(0, 65, 118))
        val yorkshire = Party("Yorkshire Party", "YP", Color(0, 124, 178))
        val rejoin = Party("Rejoin EU", "REJOIN", Color(0, 51, 153))
        val conservative = Party("Conservative", "CON", Color(0x00aeef))
        val ukip = Party("UK Independence Party", "UKIP", Color(0x6d3177))
        val freeAll = Party("Freedom Alliance", "FREE-ALL", Color(200, 24, 125))
        val forBritain = Party("For Britain", "FOR", Color(0, 0, 128))
        val reform = Party("Reform UK", "REF", Color(0x00c0d5))
        val green = Party("Green", "GRN", Color(0x6ab023))
        val curr = mapOf(
            Candidate("Paul Bickerdike", cpa) to 102,
            Candidate("Mike Davies", greenSoc) to 104,
            Candidate("Jayda Fransen", independent) to 50,
            Candidate("George Galloway", workers) to 8264,
            Candidate("Tom Gordon", libdem) to 1254,
            Candidate("Th\u00e9r\u00e8se Hirst", englishDem) to 207,
            Candidate("Howling Laud Hope", mrlp) to 107,
            Candidate("Susan Laird", heritage) to 33,
            Candidate("Kim Leadbeater", labour) to 13296,
            Candidate("Ollie Purser", sdp) to 66,
            Candidate("Corey Robinson", yorkshire) to 816,
            Candidate("Andrew Smith", rejoin) to 75,
            Candidate("Ryan Stephenson", conservative) to 12973,
            Candidate("Jack Thomson", ukip) to 151,
            Candidate("Jonathon Tilt", freeAll) to 100,
            Candidate("Anne Marie Waters", forBritain) to 97,
        )
        val prev = mapOf(
            labour to 22594,
            conservative to 19069,
            independent to 6432,
            libdem to 2462,
            reform to 1678,
            green to 692,
        )
        val panel =
            candidateVotes(
                current = {
                    votes = curr.asOneTimePublisher()
                    header = "DECLARED RESULT".asOneTimePublisher()
                    subhead = "".asOneTimePublisher()
                },
                prev = {
                    votes = prev.asOneTimePublisher()
                    header = "CHANGE SINCE 2019".asOneTimePublisher()
                },
                title = "BATLEY AND SPEN".asOneTimePublisher(),
            )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "ConsolidateInDiffIfTooMany-1", panel)
        assertPublishes(
            panel.altText,
            """
                BATLEY AND SPEN
                
                DECLARED RESULT (CHANGE SINCE 2019)
                KIM LEADBEATER (LAB): 13,296 (35.3%, -7.4%)
                RYAN STEPHENSON (CON): 12,973 (34.4%, -1.6%)
                GEORGE GALLOWAY (WP): 8,264 (21.9%, +21.9%)
                TOM GORDON (LD): 1,254 (3.3%, -1.3%)
                COREY ROBINSON (YP): 816 (2.2%, +2.2%)
                THÉRÈSE HIRST (ED): 207 (0.5%, +0.5%)
                JACK THOMSON (UKIP): 151 (0.4%, +0.4%)
                HOWLING LAUD HOPE (MRLP): 107 (0.3%, +0.3%)
                MIKE DAVIES (AGS): 104 (0.3%, +0.3%)
                PAUL BICKERDIKE (CPA): 102 (0.3%, +0.3%)
                JONATHON TILT (FREE-ALL): 100 (0.3%, +0.3%)
                ANNE MARIE WATERS (FOR): 97 (0.3%, +0.3%)
                ANDREW SMITH (REJOIN): 75 (0.2%, +0.2%)
                OLLIE PURSER (SDP): 66 (0.2%, +0.2%)
                JAYDA FRANSEN (IND): 50 (0.1%, -12.0%)
                SUSAN LAIRD (HERITAGE): 33 (0.1%, +0.1%)
                OTHERS: - (-4.5%)
            """.trimIndent(),
        )
    }
}
