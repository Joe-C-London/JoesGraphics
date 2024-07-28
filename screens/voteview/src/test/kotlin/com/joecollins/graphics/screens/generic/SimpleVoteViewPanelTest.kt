package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.candidateVotes
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.createPartyMap
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.createResultMap
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.createSingleNonPartisanResultMap
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.createSinglePartyMap
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.createSingleResultMap
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.nonPartisanVotes
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.partyOrCandidateVotes
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.partyRangeVotes
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.partyVotes
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader.readShapes
import com.joecollins.models.general.Aggregators.topAndOthers
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.NonPartisanCandidate
import com.joecollins.models.general.NonPartisanCandidateResult
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCandidate
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PartyResult.Companion.elected
import com.joecollins.models.general.PartyResult.Companion.leading
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.Shape
import java.awt.geom.Area
import java.text.DecimalFormat
import java.util.IdentityHashMap

class SimpleVoteViewPanelTest {
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
                leadingParty = leader
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
                incumbentMarker = "(MP)"
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
                DAVID LAMETTI (MP) (LIB): 22,803 (43.5%, -0.4%)
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
                leadingParty = leader
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
                leadingParty = leader
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
                leadingParty = leader
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
    fun testPartyVoteScreen() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val currentVotes = Publisher(emptyMap<Party, Int>())
        val previousVotes = Publisher(emptyMap<Party, Int>())
        val pctReporting = Publisher(0.0)
        val title = Publisher("PRINCE EDWARD ISLAND")
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
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
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
            map = createPartyMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                winners = winnersByDistrict
                this.focus = focus
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PopularVote-1", panel)
        assertPublishes(
            panel.altText,
            """
                PRINCE EDWARD ISLAND
                
                0 OF 27 DISTRICTS DECLARED, WAITING FOR RESULTS... (CHANGE SINCE 2015)
                
                SWING SINCE 2015: NOT AVAILABLE
            """.trimIndent(),
        )

        val winners = mutableMapOf<Int, Party?>()
        currentVotes.submit(
            mapOf(
                ndp to 124,
                pc to 1373,
                lib to 785,
                grn to 675,
            ),
        )
        previousVotes.submit(
            mapOf(
                ndp to 585,
                pc to 785,
                lib to 1060,
                grn to 106,
            ),
        )
        voteHeader.submit("1 OF 27 DISTRICTS DECLARED")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(1.0 / 27)
        winners[3] = pc
        winnersByDistrict.submit(winners)
        compareRendering("SimpleVoteViewPanel", "PopularVote-2", panel)
        assertPublishes(
            panel.altText,
            """
                PRINCE EDWARD ISLAND
                
                1 OF 27 DISTRICTS DECLARED, PROJECTION: TOO EARLY TO CALL (CHANGE SINCE 2015)
                PROGRESSIVE CONSERVATIVE: 46.4% (+15.5%)
                LIBERAL: 26.5% (-15.3%)
                GREEN: 22.8% (+18.6%)
                NEW DEMOCRATIC PARTY: 4.2% (-18.9%)
                
                SWING SINCE 2015: 15.4% SWING LIB TO PC
            """.trimIndent(),
        )

        focus.submit(shapesByDistrict.keys.filter { it <= 7 })
        mapHeader.submit("CARDIGAN")
        title.submit("CARDIGAN")
        pctReporting.submit(1.0 / 7)
        voteHeader.submit("1 OF 7 DISTRICTS DECLARED")
        compareRendering("SimpleVoteViewPanel", "PopularVote-3", panel)
        assertPublishes(
            panel.altText,
            """
                CARDIGAN
                
                1 OF 7 DISTRICTS DECLARED, PROJECTION: TOO EARLY TO CALL (CHANGE SINCE 2015)
                PROGRESSIVE CONSERVATIVE: 46.4% (+15.5%)
                LIBERAL: 26.5% (-15.3%)
                GREEN: 22.8% (+18.6%)
                NEW DEMOCRATIC PARTY: 4.2% (-18.9%)
                
                SWING SINCE 2015: 15.4% SWING LIB TO PC
            """.trimIndent(),
        )
    }

    @Test
    fun testPartyVoteTickScreen() {
        val dem = Party("DEMOCRAT", "DEM", Color.BLUE)
        val gop = Party("REPUBLICAN", "GOP", Color.RED)
        val currentVotes = Publisher(
            mapOf(
                dem to 60572245,
                gop to 50861970,
                Party.OTHERS to 1978774,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                dem to 61776554,
                gop to 63173815,
                Party.OTHERS to 3676641,
            ),
        )
        val pctReporting = Publisher(1.0)
        val title = Publisher("UNITED STATES")
        val voteHeader = Publisher("HOUSE OF REPRESENTATIVES")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2016")
        val swingHeader = Publisher("SWING SINCE 2016")
        val winner = Publisher(dem)
        val swingPartyOrder = listOf(dem, gop)
        val panel = partyVotes(
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
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartyTick-1", panel)
        assertPublishes(
            panel.altText,
            """
                UNITED STATES
                
                HOUSE OF REPRESENTATIVES (CHANGE SINCE 2016)
                DEMOCRAT: 53.4% (+5.4%) WINNER
                REPUBLICAN: 44.8% (-4.3%)
                OTHERS: 1.7% (-1.1%)
                
                SWING SINCE 2016: 4.8% SWING GOP TO DEM
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
            majority = {
                show = showMajority
                display = "50% TO WIN"
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
                leadingParty = leader.map { elected(it) }
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
                leadingParty = leader
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
                leadingParty = leader
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
            majority = {
                display = "50% TO WIN"
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
            majority = {
                display = "50% TO WIN"
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
    fun testVoteRangeScreen() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val currentVotes = Publisher(
            mapOf(
                ndp to (0.030).rangeTo(0.046),
                pc to (0.290).rangeTo(0.353),
                lib to (0.257).rangeTo(0.292),
                grn to (0.343).rangeTo(0.400),
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                ndp to 8997,
                pc to 30663,
                lib to 33481,
                grn to 8857,
            ),
        )
        val title = Publisher("PRINCE EDWARD ISLAND")
        val voteHeader = Publisher("OPINION POLL RANGE")
        val voteSubhead = Publisher("SINCE ELECTION CALL")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("PEI")
        val swingPartyOrder = listOf(ndp, grn, lib, pc)
        val shapesByDistrict = peiShapesByDistrict()
        val winners: Map<Int, Party> = HashMap()
        val panel = partyRangeVotes(
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
            map = createPartyMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                this.winners = winners.asOneTimePublisher()
                focus = null.asOneTimePublisher()
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "Range-1", panel)
        assertPublishes(
            panel.altText,
            """
                PRINCE EDWARD ISLAND
                
                OPINION POLL RANGE, SINCE ELECTION CALL (CHANGE SINCE 2015)
                GREEN: 34.3-40.0% ((+23.5)-(+29.2)%)
                PROGRESSIVE CONSERVATIVE: 29.0-35.3% ((-8.4)-(-2.1)%)
                LIBERAL: 25.7-29.2% ((-15.1)-(-11.6)%)
                NEW DEMOCRATIC PARTY: 3.0-4.6% ((-8.0)-(-6.4)%)
                
                SWING SINCE 2015: 19.8% SWING LIB TO GRN
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
                leadingParty = leader
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
        val mapping = IdentityHashMap<Party, Party>()
        sequenceOf(dup, uup, tuv, con, pup, ukip, indU).forEach { mapping[it] = unionists }
        sequenceOf(sf, sdlp, wp, indN).forEach { mapping[it] = nationalists }
        sequenceOf(apni, grn, pbp, lab, indO).forEach { mapping[it] = others }
        val currentVotes = Publisher(emptyMap<Party, Int>())
        val previousVotes = Publisher(emptyMap<Party, Int>())
        val title = Publisher("NORTHERN IRELAND")
        val seatHeader = Publisher("2017 RESULTS")
        val seatSubhead = Publisher<String?>(null)
        val changeHeader = Publisher("NOTIONAL CHANGE SINCE 2016")
        val panel = partyVotes(
            current = {
                votes = currentVotes
                header = seatHeader
                subhead = seatSubhead
            },
            prev = {
                votes = previousVotes
                header = changeHeader
                swing = {
                    partyOrder = listOf(nationalists, others, unionists)
                    header = "FIRST PREFERENCE SWING SINCE 2016".asOneTimePublisher()
                }
            },
            partyClassification = {
                classification = { mapping.getOrDefault(it, others) }
                header = "BY DESIGNATION".asOneTimePublisher()
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartyClassifications-1", panel)
        assertPublishes(
            panel.altText,
            """
                NORTHERN IRELAND
                
                2017 RESULTS (NOTIONAL CHANGE SINCE 2016)
                
                BY DESIGNATION
                
                FIRST PREFERENCE SWING SINCE 2016: NOT AVAILABLE
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                dup to 225413,
                sf to 224245,
                sdlp to 95958,
                uup to 103314,
                apni to 72717,
                grn to 18527,
                tuv to 20523,
                pbp to 14100,
                pup to 5590,
                con to 2399,
                lab to 2009,
                ukip to 1579,
                cista to 1273,
                wp to 1261,
                indU to 4918,
                indN to 1639,
                indO to 7850,
            ),
        )
        previousVotes.submit(
            mapOf(
                dup to 202567,
                sf to 166785,
                uup to 87302,
                sdlp to 83368,
                apni to 48447,
                tuv to 23776,
                grn to 18718,
                pbp to 13761,
                ukip to 10109,
                pup to 5955,
                con to 2554,
                cista to 2510,
                lab to 1939 + 1577,
                wp to 1565,
                indU to 351 + 3270,
                indN to 0,
                indO to 224 + 124 + 32 + 19380,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "PartyClassifications-2", panel)
        assertPublishes(
            panel.altText,
            """
                NORTHERN IRELAND
                
                2017 RESULTS (NOTIONAL CHANGE SINCE 2016)
                DEMOCRATIC UNIONIST PARTY: 28.3% (-1.0%)
                SINN FÉIN: 28.1% (+4.0%)
                ULSTER UNIONIST PARTY: 13.0% (+0.3%)
                SOCIAL DEMOCRATIC AND LABOUR PARTY: 12.0% (-0.0%)
                ALLIANCE PARTY: 9.1% (+2.1%)
                TRADITIONAL UNIONIST VOICE: 2.6% (-0.9%)
                GREEN PARTY: 2.3% (-0.4%)
                PEOPLE BEFORE PROFIT: 1.8% (-0.2%)
                INDEPENDENT: 1.0% (-1.9%)
                PROGRESSIVE UNIONIST PARTY: 0.7% (-0.2%)
                NI CONSERVATIVES: 0.3% (-0.1%)
                LABOUR ALTERNATIVE: 0.3% (-0.3%)
                UK INDEPENDENCE PARTY: 0.2% (-1.3%)
                CANNABIS IS SAFER THAN ALCOHOL: 0.2% (-0.2%)
                WORKERS' PARTY: 0.2% (-0.1%)
                
                BY DESIGNATION
                UNIONISTS: 46.0%
                NATIONALISTS: 40.3%
                OTHERS: 13.6%
                
                FIRST PREFERENCE SWING SINCE 2016: 4.4% SWING UNIONISTS TO NATIONALISTS
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
    fun testShowPrev() {
        val ldp = Party("Liberal Democratic Party", "LDP", Color(60, 163, 36))
        val cdp = Party("Constitutional Democratic Party", "CDP", Color(24, 69, 137))
        val kibo = Party("Kib\u014d no T\u014d", "KIB\u014c", Color(24, 100, 57))
        val nippon = Party("Nippon Ishin no Kai", "NIPPON", Color(184, 206, 67))
        val komeito = Party("Komeito", "KOMEITO", Color(245, 88, 129))
        val jcp = Party("Japanese Communist Party", "JCP", Color(219, 0, 28))
        val dpp = Party("Democratic Party for the People", "DPP", Color(255, 215, 0))
        val reiwa = Party("Reiwa Shinsengumi", "REIWA", Color(237, 0, 140))
        val sdp = Party("Social Democratic Party", "SDP", Color(28, 169, 233))
        val oth = Party.OTHERS

        val curr = Publisher(emptyMap<Party, Int>())
        val prev = Publisher(
            mapOf(
                ldp to 18_555_717,
                cdp to 11_084_890,
                kibo to 9_677_524,
                komeito to 6_977_712,
                jcp to 4_404_081,
                nippon to 3_387_097,
                sdp to 941_324,
                oth to 729_207,
            ),
        )
        val voteHeader = Publisher("PROPORTIONAL VOTES")
        val changeHeader = Publisher("2017 RESULT")
        val showPrevRaw = Publisher(true)
        val showPctReporting = Publisher(1.0)

        val panel = partyVotes(
            current = {
                votes = curr
                header = voteHeader
                subhead = "".asOneTimePublisher()
                pctReporting = showPctReporting
            },
            prev = {
                votes = prev
                header = changeHeader
                showRaw = showPrevRaw
            },
            title = "JAPAN".asOneTimePublisher(),
        )
        panel.size = Dimension(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PrevVotes-0", panel)
        assertPublishes(
            panel.altText,
            """
                JAPAN
                
                PROPORTIONAL VOTES
                
                2017 RESULT
                LDP: 33.3%
                CDP: 19.9%
                KIBŌ: 17.4%
                KOMEITO: 12.5%
                JCP: 7.9%
                NIPPON: 6.1%
                SDP: 1.7%
                OTH: 1.3%
            """.trimIndent(),
        )

        curr.submit(
            mapOf(
                ldp to 19_914_883,
                cdp to 11_492_095,
                nippon to 8_050_830,
                komeito to 7_114_282,
                jcp to 4_166_076,
                dpp to 2_593_396,
                reiwa to 2_215_648,
                sdp to 1_018_588,
                oth to 900_181,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "PrevVotes-1", panel)
        assertPublishes(
            panel.altText,
            """
                JAPAN
                
                PROPORTIONAL VOTES
                LIBERAL DEMOCRATIC PARTY: 34.7%
                CONSTITUTIONAL DEMOCRATIC PARTY: 20.0%
                NIPPON ISHIN NO KAI: 14.0%
                KOMEITO: 12.4%
                JAPANESE COMMUNIST PARTY: 7.2%
                DEMOCRATIC PARTY FOR THE PEOPLE: 4.5%
                REIWA SHINSENGUMI: 3.9%
                SOCIAL DEMOCRATIC PARTY: 1.8%
                OTHERS: 1.6%
                
                2017 RESULT
                LDP: 33.3%
                CDP: 19.9%
                KIBŌ: 17.4%
                KOMEITO: 12.5%
                JCP: 7.9%
                NIPPON: 6.1%
                SDP: 1.7%
                OTH: 1.3%
            """.trimIndent(),
        )

        curr.submit(
            mapOf(
                ldp to 27_626_235,
                cdp to 17_215_621,
                nippon to 4_802_793,
                komeito to 872_931,
                jcp to 2_639_631,
                dpp to 1_246_812,
                reiwa to 248_280,
                sdp to 313_193,
                oth to 2_491_536,
            ),
        )
        prev.submit(
            mapOf(
                ldp to 26_500_777,
                cdp to 4_726_326,
                kibo to 11_437_602,
                komeito to 832_453,
                jcp to 4_998_932,
                nippon to 1_765_053,
                sdp to 634_770,
                oth to 4_526_280,
            ),
        )
        voteHeader.submit("CONSTITUENCY VOTES")
        compareRendering("SimpleVoteViewPanel", "PrevVotes-2", panel)
        assertPublishes(
            panel.altText,
            """
                JAPAN
                
                CONSTITUENCY VOTES
                LIBERAL DEMOCRATIC PARTY: 48.1%
                CONSTITUTIONAL DEMOCRATIC PARTY: 30.0%
                NIPPON ISHIN NO KAI: 8.4%
                JAPANESE COMMUNIST PARTY: 4.6%
                DEMOCRATIC PARTY FOR THE PEOPLE: 2.2%
                KOMEITO: 1.5%
                SOCIAL DEMOCRATIC PARTY: 0.5%
                REIWA SHINSENGUMI: 0.4%
                OTHERS: 4.3%
                
                2017 RESULT
                LDP: 47.8%
                KIBŌ: 20.6%
                JCP: 9.0%
                CDP: 8.5%
                NIPPON: 3.2%
                KOMEITO: 1.5%
                SDP: 1.1%
                OTH: 8.2%
            """.trimIndent(),
        )

        showPrevRaw.submit(false)
        changeHeader.submit("CHANGE SINCE 2017")
        compareRendering("SimpleVoteViewPanel", "PrevVotes-3", panel)
        assertPublishes(
            panel.altText,
            """
                JAPAN
                
                CONSTITUENCY VOTES (CHANGE SINCE 2017)
                LIBERAL DEMOCRATIC PARTY: 48.1% (+0.3%)
                CONSTITUTIONAL DEMOCRATIC PARTY: 30.0% (+21.4%)
                NIPPON ISHIN NO KAI: 8.4% (+5.2%)
                JAPANESE COMMUNIST PARTY: 4.6% (-4.4%)
                DEMOCRATIC PARTY FOR THE PEOPLE: 2.2% (*)
                KOMEITO: 1.5% (+0.0%)
                SOCIAL DEMOCRATIC PARTY: 0.5% (-0.6%)
                REIWA SHINSENGUMI: 0.4% (*)
                OTHERS: 4.3% (-21.9%)
                * CHANGE INCLUDED IN OTHERS
            """.trimIndent(),
        )

        curr.submit(
            mapOf(
                ldp to 19_914_883,
                cdp to 11_492_095,
                nippon to 8_050_830,
                komeito to 7_114_282,
                jcp to 4_166_076,
                dpp to 2_593_396,
                reiwa to 2_215_648,
                sdp to 1_018_588,
                oth to 900_181,
            ),
        )
        prev.submit(
            mapOf(
                ldp to 18_555_717,
                cdp to 11_084_890,
                kibo to 9_677_524,
                komeito to 6_977_712,
                jcp to 4_404_081,
                nippon to 3_387_097,
                sdp to 941_324,
                oth to 729_207,
            ),
        )
        voteHeader.submit("PROPORTIONAL VOTES")
        compareRendering("SimpleVoteViewPanel", "PrevVotes-4", panel)
        assertPublishes(
            panel.altText,
            """
                JAPAN
                
                PROPORTIONAL VOTES (CHANGE SINCE 2017)
                LIBERAL DEMOCRATIC PARTY: 34.7% (+1.4%)
                CONSTITUTIONAL DEMOCRATIC PARTY: 20.0% (+0.1%)
                NIPPON ISHIN NO KAI: 14.0% (+7.9%)
                KOMEITO: 12.4% (-0.1%)
                JAPANESE COMMUNIST PARTY: 7.2% (-0.6%)
                DEMOCRATIC PARTY FOR THE PEOPLE: 4.5% (*)
                REIWA SHINSENGUMI: 3.9% (*)
                SOCIAL DEMOCRATIC PARTY: 1.8% (+0.1%)
                OTHERS: 1.6% (-8.7%)
                * CHANGE INCLUDED IN OTHERS
            """.trimIndent(),
        )

        showPrevRaw.submit(true)
        changeHeader.submit("2017 RESULT")
        compareRendering("SimpleVoteViewPanel", "PrevVotes-1", panel)
        assertPublishes(
            panel.altText,
            """
                JAPAN
                
                PROPORTIONAL VOTES
                LIBERAL DEMOCRATIC PARTY: 34.7%
                CONSTITUTIONAL DEMOCRATIC PARTY: 20.0%
                NIPPON ISHIN NO KAI: 14.0%
                KOMEITO: 12.4%
                JAPANESE COMMUNIST PARTY: 7.2%
                DEMOCRATIC PARTY FOR THE PEOPLE: 4.5%
                REIWA SHINSENGUMI: 3.9%
                SOCIAL DEMOCRATIC PARTY: 1.8%
                OTHERS: 1.6%
                
                2017 RESULT
                LDP: 33.3%
                CDP: 19.9%
                KIBŌ: 17.4%
                KOMEITO: 12.5%
                JCP: 7.9%
                NIPPON: 6.1%
                SDP: 1.7%
                OTH: 1.3%
            """.trimIndent(),
        )

        showPctReporting.submit(0.1)
        voteHeader.submit("PROPORTIONAL VOTES (10% IN)")
        compareRendering("SimpleVoteViewPanel", "PrevVotes-1b", panel)
        assertPublishes(
            panel.altText,
            """
                JAPAN
                
                PROPORTIONAL VOTES (10% IN)
                LIBERAL DEMOCRATIC PARTY: 34.7%
                CONSTITUTIONAL DEMOCRATIC PARTY: 20.0%
                NIPPON ISHIN NO KAI: 14.0%
                KOMEITO: 12.4%
                JAPANESE COMMUNIST PARTY: 7.2%
                DEMOCRATIC PARTY FOR THE PEOPLE: 4.5%
                REIWA SHINSENGUMI: 3.9%
                SOCIAL DEMOCRATIC PARTY: 1.8%
                OTHERS: 1.6%
                
                2017 RESULT
                LDP: 33.3%
                CDP: 19.9%
                KIBŌ: 17.4%
                KOMEITO: 12.5%
                JCP: 7.9%
                NIPPON: 6.1%
                SDP: 1.7%
                OTH: 1.3%
            """.trimIndent(),
        )
    }

    @Test
    fun testShowPrevRange() {
        val ldp = Party("Liberal Democratic Party", "LDP", Color(60, 163, 36))
        val cdp = Party("Constitutional Democratic Party", "CDP", Color(24, 69, 137))
        val kibo = Party("Kib\u014d no T\u014d", "KIB\u014c", Color(24, 100, 57))
        val nippon = Party("Nippon Ishin no Kai", "NIPPON", Color(184, 206, 67))
        val komeito = Party("Komeito", "KOMEITO", Color(245, 88, 129))
        val jcp = Party("Japanese Communist Party", "JCP", Color(219, 0, 28))
        val dpp = Party("Democratic Party for the People", "DPP", Color(255, 215, 0))
        val reiwa = Party("Reiwa Shinsengumi", "REIWA", Color(237, 0, 140))
        val sdp = Party("Social Democratic Party", "SDP", Color(28, 169, 233))
        val oth = Party.OTHERS

        val curr = Publisher(emptyMap<Party, ClosedRange<Double>>())
        val prev = Publisher(
            mapOf(
                ldp to 18_555_717,
                cdp to 11_084_890,
                kibo to 9_677_524,
                komeito to 6_977_712,
                jcp to 4_404_081,
                nippon to 3_387_097,
                sdp to 941_324,
                oth to 729_207,
            ),
        )
        val voteHeader = Publisher("PROPORTIONAL VOTES")
        val changeHeader = Publisher("2017 RESULT")
        val showPrevRaw = Publisher(true)

        val panel = partyRangeVotes(
            current = {
                votes = curr
                header = voteHeader
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = prev
                header = changeHeader
                showRaw = showPrevRaw
            },
            title = "JAPAN".asOneTimePublisher(),
        )
        panel.size = Dimension(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PrevVotes-0", panel)
        assertPublishes(
            panel.altText,
            """
                JAPAN
                
                PROPORTIONAL VOTES
                
                2017 RESULT
                LDP: 33.3%
                CDP: 19.9%
                KIBŌ: 17.4%
                KOMEITO: 12.5%
                JCP: 7.9%
                NIPPON: 6.1%
                SDP: 1.7%
                OTH: 1.3%
            """.trimIndent(),
        )

        curr.submit(
            mapOf(
                ldp to 0.320..0.380,
                cdp to 0.130..0.210,
                nippon to 0.040..0.123,
                komeito to 0.070..0.084,
                jcp to 0.050..0.076,
                dpp to 0.020..0.024,
                reiwa to 0.010..0.016,
                sdp to 0.010..0.014,
                oth to 0.017..0.030,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "PrevRangeVotes-1", panel)
        assertPublishes(
            panel.altText,
            """
                JAPAN
                
                PROPORTIONAL VOTES
                LIBERAL DEMOCRATIC PARTY: 32.0-38.0%
                CONSTITUTIONAL DEMOCRATIC PARTY: 13.0-21.0%
                NIPPON ISHIN NO KAI: 4.0-12.3%
                KOMEITO: 7.0-8.4%
                JAPANESE COMMUNIST PARTY: 5.0-7.6%
                DEMOCRATIC PARTY FOR THE PEOPLE: 2.0-2.4%
                REIWA SHINSENGUMI: 1.0-1.6%
                SOCIAL DEMOCRATIC PARTY: 1.0-1.4%
                OTHERS: 1.7-3.0%
                
                2017 RESULT
                LDP: 33.3%
                CDP: 19.9%
                KIBŌ: 17.4%
                KOMEITO: 12.5%
                JCP: 7.9%
                NIPPON: 6.1%
                SDP: 1.7%
                OTH: 1.3%
            """.trimIndent(),
        )

        showPrevRaw.submit(false)
        changeHeader.submit("CHANGE SINCE 2017")
        compareRendering("SimpleVoteViewPanel", "PrevRangeVotes-2", panel)
        assertPublishes(
            panel.altText,
            """
                JAPAN
                
                PROPORTIONAL VOTES (CHANGE SINCE 2017)
                LIBERAL DEMOCRATIC PARTY: 32.0-38.0% ((-1.3)-(+4.7)%)
                CONSTITUTIONAL DEMOCRATIC PARTY: 13.0-21.0% ((-6.9)-(+1.1)%)
                NIPPON ISHIN NO KAI: 4.0-12.3% ((-2.1)-(+6.2)%)
                KOMEITO: 7.0-8.4% ((-5.5)-(-4.1)%)
                JAPANESE COMMUNIST PARTY: 5.0-7.6% ((-2.9)-(-0.3)%)
                DEMOCRATIC PARTY FOR THE PEOPLE: 2.0-2.4% (*)
                REIWA SHINSENGUMI: 1.0-1.6% (*)
                SOCIAL DEMOCRATIC PARTY: 1.0-1.4% ((-0.7)-(-0.3)%)
                OTHERS: 1.7-3.0% ((-14.0)-(-11.7)%)
                * CHANGE INCLUDED IN OTHERS
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
                leadingParty = leader
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
    fun testPartyMerger() {
        val lib = Party("Liberal", "LIB", Color(234, 109, 106))
        val con = Party("Conservative", "CON", Color(100, 149, 237))
        val bq = Party("Bloc Québécois", "BQ", Color(135, 206, 250))
        val ndp = Party("New Democratic Party", "NDP", Color(244, 164, 96))
        val pc = Party("Progressive Conservative", "PC", Color(153, 153, 255))
        val ca = Party("Canadian Alliance", "CA", Color(95, 158, 160))
        val oth = Party.OTHERS

        val currVotes = mapOf(
            lib to 4982220,
            con to 4019498,
            bq to 1680109,
            ndp to 2127403,
            oth to 755472,
        )
        val prevVotes = mapOf(
            lib to 5252031,
            ca to 3276929,
            bq to 1377727,
            ndp to 1093868,
            pc to 1566998,
            oth to 290220,
        )
        val showPrev = Publisher(false)
        val swingOrder = listOf(ndp, lib, oth, pc, bq, con, ca)
        val partyChanges = mapOf(ca to con, pc to con).asOneTimePublisher()

        val panel = partyVotes(
            current = {
                votes = currVotes.asOneTimePublisher()
                header = "2004 VOTE SHARE".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = prevVotes.asOneTimePublisher()
                header = showPrev.map { if (it) "2000 VOTE SHARE" else "CHANGE SINCE 2000" }
                showRaw = showPrev
                this.partyChanges = partyChanges
                swing = {
                    partyOrder = swingOrder
                    header = "SWING SINCE 2000".asOneTimePublisher()
                }
            },
            title = "CANADA".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartyMerge-1", panel)
        assertPublishes(
            panel.altText,
            """
                CANADA
                
                2004 VOTE SHARE (CHANGE SINCE 2000)
                LIBERAL: 36.7% (-4.1%)
                CONSERVATIVE: 29.6% (-8.0%)
                NEW DEMOCRATIC PARTY: 15.7% (+7.2%)
                BLOC QUÉBÉCOIS: 12.4% (+1.7%)
                OTHERS: 5.6% (+3.3%)
                
                SWING SINCE 2000: 2.0% SWING CON TO LIB
            """.trimIndent(),
        )

        showPrev.submit(true)
        compareRendering("SimpleVoteViewPanel", "PartyMerge-2", panel)
        assertPublishes(
            panel.altText,
            """
                CANADA
                
                2004 VOTE SHARE
                LIBERAL: 36.7%
                CONSERVATIVE: 29.6%
                NEW DEMOCRATIC PARTY: 15.7%
                BLOC QUÉBÉCOIS: 12.4%
                OTHERS: 5.6%
                
                2000 VOTE SHARE
                LIB: 40.8%
                CA: 25.5%
                PC: 12.2%
                BQ: 10.7%
                NDP: 8.5%
                OTH: 2.3%
                
                SWING SINCE 2000: 2.0% SWING CON TO LIB
            """.trimIndent(),
        )
    }

    @Test
    fun testPartyMergerRange() {
        val lib = Party("Liberal", "LIB", Color(234, 109, 106))
        val con = Party("Conservative", "CON", Color(100, 149, 237))
        val bq = Party("Bloc Québécois", "BQ", Color(135, 206, 250))
        val ndp = Party("New Democratic Party", "NDP", Color(244, 164, 96))
        val pc = Party("Progressive Conservative", "PC", Color(153, 153, 255))
        val ca = Party("Canadian Alliance", "CA", Color(95, 158, 160))
        val oth = Party.OTHERS

        val currVotes = mapOf(
            lib to 0.29..0.41,
            con to 0.25..0.37,
            bq to 0.09..0.13,
            ndp to 0.15..0.22,
            oth to 0.02..0.07,
        )
        val prevVotes = mapOf(
            lib to 5252031,
            ca to 3276929,
            bq to 1377727,
            ndp to 1093868,
            pc to 1566998,
            oth to 290220,
        )
        val showPrev = Publisher(false)
        val swingOrder = listOf(ndp, lib, oth, pc, bq, con, ca)
        val partyChanges = mapOf(ca to con, pc to con).asOneTimePublisher()

        val panel = partyRangeVotes(
            current = {
                votes = currVotes.asOneTimePublisher()
                header = "2004 POLLING RANGE".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = prevVotes.asOneTimePublisher()
                header = showPrev.map { if (it) "2000 VOTE SHARE" else "CHANGE SINCE 2000" }
                showRaw = showPrev
                this.partyChanges = partyChanges
                swing = {
                    partyOrder = swingOrder
                    header = "SWING SINCE 2000".asOneTimePublisher()
                }
            },
            title = "CANADA".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartyMergeRange-1", panel)
        assertPublishes(
            panel.altText,
            """
                CANADA
                
                2004 POLLING RANGE (CHANGE SINCE 2000)
                LIBERAL: 29.0-41.0% ((-11.8)-(+0.2)%)
                CONSERVATIVE: 25.0-37.0% ((-12.7)-(-0.7)%)
                NEW DEMOCRATIC PARTY: 15.0-22.0% ((+6.5)-(+13.5)%)
                BLOC QUÉBÉCOIS: 9.0-13.0% ((-1.7)-(+2.3)%)
                OTHERS: 2.0-7.0% ((-0.3)-(+4.7)%)
                
                SWING SINCE 2000: 0.4% SWING CON TO LIB
            """.trimIndent(),
        )

        showPrev.submit(true)
        compareRendering("SimpleVoteViewPanel", "PartyMergeRange-2", panel)
        assertPublishes(
            panel.altText,
            """
                CANADA
                
                2004 POLLING RANGE
                LIBERAL: 29.0-41.0%
                CONSERVATIVE: 25.0-37.0%
                NEW DEMOCRATIC PARTY: 15.0-22.0%
                BLOC QUÉBÉCOIS: 9.0-13.0%
                OTHERS: 2.0-7.0%
                
                2000 VOTE SHARE
                LIB: 40.8%
                CA: 25.5%
                PC: 12.2%
                BQ: 10.7%
                NDP: 8.5%
                OTH: 2.3%
                
                SWING SINCE 2000: 0.4% SWING CON TO LIB
            """.trimIndent(),
        )
    }

    @Test
    fun testDualMap() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Color.DARK_GRAY)
        val panel = partyVotes(
            current = {
                votes = mapOf(
                    pc to 29335,
                    grn to 24593,
                    lib to 23711,
                    ndp to 2408,
                    ind to 282,
                ).asOneTimePublisher()
                header = "VOTES COUNTED".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
                pctReporting = (26.0 / 27).asOneTimePublisher()
                progressLabel = (DecimalFormat("0.0%").format(26.0 / 27) + " IN").asOneTimePublisher()
            },
            prev = {
                votes = mapOf(
                    lib to 32127,
                    pc to 29837,
                    grn to 8620,
                    ndp to 8448,
                ).asOneTimePublisher()
                header = "CHANGE SINCE 2015".asOneTimePublisher()
            },
            map = createResultMap<Int> {
                shapes = peiShapesByDistrict().asOneTimePublisher()
                winners = mapOf(
                    pc to setOf(4, 2, 3, 7, 1, 6, 19, 15, 20, 18, 8, 26),
                    grn to setOf(5, 17, 11, 13, 12, 21, 22, 23),
                    lib to setOf(16, 14, 10, 24, 25, 27),
                ).entries.flatMap { e -> e.value.map { it to elected(e.key) } }
                    .toMap().asOneTimePublisher()
                focus = null.asOneTimePublisher()
                header = "DISTRICTS".asOneTimePublisher()
            },
            secondMap = createResultMap<String> {
                shapes = peiShapesByRegion().asOneTimePublisher()
                winners = mapOf(
                    "Cardigan" to elected(pc),
                    "Malpeque" to elected(pc),
                    "Charlottetown" to leading(grn),
                    "Egmont" to elected(lib),
                ).asOneTimePublisher()
                focus = null.asOneTimePublisher()
                header = "REGIONS".asOneTimePublisher()
            },
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "DualMap", panel)
        assertPublishes(
            panel.altText,
            """
            PRINCE EDWARD ISLAND
            
            VOTES COUNTED [96.3% IN] (CHANGE SINCE 2015)
            PROGRESSIVE CONSERVATIVE: 36.5% (-1.2%)
            GREEN: 30.6% (+19.7%)
            LIBERAL: 29.5% (-11.1%)
            NEW DEMOCRATIC PARTY: 3.0% (-7.7%)
            INDEPENDENT: 0.4% (+0.4%)
            """.trimIndent(),
        )
    }

    @Test
    fun testNonPartisanVotes() {
        val ndp = NonPartisanCandidate("Billy Cann", color = Color.ORANGE)
        val pc = NonPartisanCandidate("Cory Deagle", color = Color.BLUE)
        val lib = NonPartisanCandidate("Daphne Griffin", color = Color.RED)
        val grn = NonPartisanCandidate("John Allen MacLean", color = Color.GREEN.darker())
        val currentVotes = Publisher(sequenceOf(ndp, pc, lib, grn).associateWith { 0 })
        val previousVotes = Publisher(
            mapOf(
                NonPartisanCandidate("Michael Redmond", color = Color.ORANGE) to 585,
                NonPartisanCandidate("Andrew Daggett", color = Color.BLUE) to 785,
                NonPartisanCandidate("Allen Roach", color = Color.RED) to 1060,
                NonPartisanCandidate("Jason Furness", color = Color.GREEN.darker()) to 106,
            ),
        )
        val pctReporting = Publisher(0.0)
        val title = Publisher("MONTAGUE-KILMUIR")
        val voteHeader = Publisher("2019 RESULT")
        val voteSubhead = Publisher("WAITING FOR RESULTS...")
        val progress = Publisher("0/9 POLLS")
        val changeHeader = Publisher("2015 RESULT")
        val mapHeader = Publisher("CARDIGAN")
        val leader = Publisher<NonPartisanCandidateResult?>(null)
        val winner = Publisher<NonPartisanCandidate?>(null)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = Publisher(3)
        val panel = nonPartisanVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
                this.pctReporting = pctReporting
                progressLabel = progress
            },
            prev = {
                votes = previousVotes
                header = changeHeader
            },
            map = createSingleNonPartisanResultMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                selectedShape = selectedDistrict
                leadingCandidate = leader
                this.focus = focus
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "NonPartisan-1", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                2019 RESULT [0/9 POLLS], WAITING FOR RESULTS...
                BILLY CANN: WAITING...
                CORY DEAGLE: WAITING...
                DAPHNE GRIFFIN: WAITING...
                JOHN ALLEN MACLEAN: WAITING...
                
                2015 RESULT
                ROACH: 41.8%
                DAGGETT: 31.0%
                REDMOND: 23.1%
                FURNESS: 4.2%
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
        progress.submit("1/9 POLLS")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(1.0 / 9)
        leader.submit(NonPartisanCandidateResult.leading(lib))
        compareRendering("SimpleVoteViewPanel", "NonPartisan-2", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                2019 RESULT [1/9 POLLS], PROJECTION: TOO EARLY TO CALL
                DAPHNE GRIFFIN: 58 (35.8%)
                JOHN ALLEN MACLEAN: 52 (32.1%)
                CORY DEAGLE: 47 (29.0%)
                BILLY CANN: 5 (3.1%)
                
                2015 RESULT
                ROACH: 41.8%
                DAGGETT: 31.0%
                REDMOND: 23.1%
                FURNESS: 4.2%
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
        progress.submit("2/9 POLLS")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(2.0 / 9)
        leader.submit(NonPartisanCandidateResult.leading(grn))
        compareRendering("SimpleVoteViewPanel", "NonPartisan-3", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                2019 RESULT [2/9 POLLS], PROJECTION: TOO EARLY TO CALL
                JOHN ALLEN MACLEAN: 106 (34.8%)
                DAPHNE GRIFFIN: 100 (32.8%)
                CORY DEAGLE: 91 (29.8%)
                BILLY CANN: 8 (2.6%)
                
                2015 RESULT
                ROACH: 41.8%
                DAGGETT: 31.0%
                REDMOND: 23.1%
                FURNESS: 4.2%
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
        progress.submit("5/9 POLLS")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(5.0 / 9)
        leader.submit(NonPartisanCandidateResult.leading(pc))
        compareRendering("SimpleVoteViewPanel", "NonPartisan-4", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                2019 RESULT [5/9 POLLS], PROJECTION: TOO EARLY TO CALL
                CORY DEAGLE: 287 (38.5%)
                JOHN ALLEN MACLEAN: 243 (32.6%)
                DAPHNE GRIFFIN: 197 (26.4%)
                BILLY CANN: 18 (2.4%)
                
                2015 RESULT
                ROACH: 41.8%
                DAGGETT: 31.0%
                REDMOND: 23.1%
                FURNESS: 4.2%
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
        progress.submit("9/9 POLLS")
        voteSubhead.submit("PROJECTION: DEAGLE WIN")
        pctReporting.submit(9.0 / 9)
        leader.submit(NonPartisanCandidateResult.elected(pc))
        winner.submit(pc)
        compareRendering("SimpleVoteViewPanel", "NonPartisan-5", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                2019 RESULT [9/9 POLLS], PROJECTION: DEAGLE WIN
                CORY DEAGLE: 1,373 (46.4%) WINNER
                DAPHNE GRIFFIN: 785 (26.5%)
                JOHN ALLEN MACLEAN: 675 (22.8%)
                BILLY CANN: 124 (4.2%)
                
                2015 RESULT
                ROACH: 41.8%
                DAGGETT: 31.0%
                REDMOND: 23.1%
                FURNESS: 4.2%
            """.trimIndent(),
        )

        winner.submit(null)
        compareRendering("SimpleVoteViewPanel", "NonPartisan-6", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                2019 RESULT [9/9 POLLS], PROJECTION: DEAGLE WIN
                CORY DEAGLE: 1,373 (46.4%)
                DAPHNE GRIFFIN: 785 (26.5%)
                JOHN ALLEN MACLEAN: 675 (22.8%)
                BILLY CANN: 124 (4.2%)
                
                2015 RESULT
                ROACH: 41.8%
                DAGGETT: 31.0%
                REDMOND: 23.1%
                FURNESS: 4.2%
            """.trimIndent(),
        )
    }

    @Test
    fun testNonPartisanVoteMisc() {
        val title = Publisher("IQALUIT-TASILUK")
        val curr = Publisher(
            mapOf(
                NonPartisanCandidate("James T. Arreak") to 133,
                NonPartisanCandidate("George Hicks", description = "Incumbent MLA") to 265,
                NonPartisanCandidate("Jonathan Chul-Hee Min Park") to 41,
                NonPartisanCandidate("Michael Salomonie") to 81,
            ),
        )
        val prev = Publisher(
            mapOf(
                NonPartisanCandidate("George Hicks") to 449,
                NonPartisanCandidate("Jacopoosie Peter") to 121,
            ),
        )
        val panel = nonPartisanVotes(
            current = {
                votes = curr
                header = "2021 RESULT".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = prev
                header = "2017 RESULT".asOneTimePublisher()
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "NonPartisanMisc-1", panel)
        assertPublishes(
            panel.altText,
            """
                IQALUIT-TASILUK
                
                2021 RESULT
                GEORGE HICKS (INCUMBENT MLA): 265 (51.0%)
                JAMES T. ARREAK: 133 (25.6%)
                MICHAEL SALOMONIE: 81 (15.6%)
                JONATHAN CHUL-HEE MIN PARK: 41 (7.9%)
                
                2017 RESULT
                HICKS: 78.8%
                PETER: 21.2%
            """.trimIndent(),
        )

        title.submit("IQALUIT-NIAQUNNGUU")
        curr.submit(
            mapOf(
                NonPartisanCandidate("P.J. Akeeagok") to 404,
                NonPartisanCandidate("Noah Papatsie") to 54,
                NonPartisanCandidate("Dinos Tikivik") to 21,
            ),
        )
        prev.submit(
            mapOf(
                NonPartisanCandidate("Pat Angnakak") to 231,
                NonPartisanCandidate("Franco Buscemi") to 196,
                NonPartisanCandidate("Anne Crawford") to 134,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "NonPartisanMisc-2", panel)
        assertPublishes(
            panel.altText,
            """
                IQALUIT-NIAQUNNGUU

                2021 RESULT
                P.J. AKEEAGOK: 404 (84.3%)
                NOAH PAPATSIE: 54 (11.3%)
                DINOS TIKIVIK: 21 (4.4%)
                
                2017 RESULT
                ANGNAKAK: 41.2%
                BUSCEMI: 34.9%
                CRAWFORD: 23.9%
            """.trimIndent(),
        )

        title.submit("ARVIAT SOUTH")
        curr.submit(
            mapOf(
                NonPartisanCandidate("Joe Savikataaq", description = "Incumbent MLA") to 0,
            ),
        )
        prev.submit(
            mapOf(
                NonPartisanCandidate("Jason Gibbons") to 234,
                NonPartisanCandidate("Joe Savikataaq") to 280,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "NonPartisanMisc-3", panel)
        assertPublishes(
            panel.altText,
            """
                ARVIAT SOUTH

                2021 RESULT
                JOE SAVIKATAAQ (INCUMBENT MLA): UNCONTESTED
                
                2017 RESULT
                SAVIKATAAQ: 54.5%
                GIBBONS: 45.5%
            """.trimIndent(),
        )

        title.submit("KUGLUKTUK")
        curr.submit(
            mapOf(
                NonPartisanCandidate("Bobby Anavilok") to 170,
                NonPartisanCandidate("Angele Kuliktana") to 77,
                NonPartisanCandidate("Genevieve Nivingalok") to 51,
                NonPartisanCandidate("Calvin Aivgak Pedersen", description = "Incumbent MLA") to 140,
            ),
        )
        prev.submit(
            mapOf(
                NonPartisanCandidate("Mila Adjukak Kamingoak") to 0,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "NonPartisanMisc-4", panel)
        assertPublishes(
            panel.altText,
            """
                KUGLUKTUK

                2021 RESULT
                BOBBY ANAVILOK: 170 (38.8%)
                CALVIN AIVGAK PEDERSEN (INCUMBENT MLA): 140 (32.0%)
                ANGELE KULIKTANA: 77 (17.6%)
                GENEVIEVE NIVINGALOK: 51 (11.6%)
                
                2017 RESULT
                KAMINGOAK: UNCONTESTED
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
                leadingParty = leader
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
    fun testPartiesAndIndependentCandidates() {
        val snp = Party("Scottish National Party", "SNP", Color.YELLOW)
        val lab = Party("Labour", "LAB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ld = Party("Liberal Democrats", "LD", Color.ORANGE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())

        val currentVotes = Publisher(
            mapOf(
                PartyOrCandidate(Party("Abolish the Scottish Parliament", "ABOL-SP", Color(29, 141, 255))) to 686,
                PartyOrCandidate(Party("Alba", "ALBA", Color.BLUE.darker())) to 3828,
                PartyOrCandidate(Party("All for Unity", "UNITY", Color(251, 5, 5))) to 1540,
                PartyOrCandidate(Party("Freedom Alliance", "FA", Color(200, 24, 125))) to 671,
                PartyOrCandidate(Party("Reform UK", "REF", Color.CYAN.darker())) to 547,
                PartyOrCandidate(Party("Restore Scotland", "RESTORE", Color.BLACK)) to 437,
                PartyOrCandidate(con) to 60779,
                PartyOrCandidate(Party("Scottish Family Party", "SFP", Color(68, 67, 152))) to 1976,
                PartyOrCandidate(grn) to 17729,
                PartyOrCandidate(lab) to 22713,
                PartyOrCandidate(ld) to 26771,
                PartyOrCandidate(Party("Libertarian", "LBT", Color(250, 188, 24))) to 488,
                PartyOrCandidate(snp) to 96433,
                PartyOrCandidate(Party("Trade Unionist and Socialist Coalition", "TUSC", Color(217, 38, 34))) to 280,
                PartyOrCandidate(Party("UK Independent Party", "UKIP", Color.MAGENTA.darker())) to 457,
                PartyOrCandidate("Hazel Mansfield") to 219,
                PartyOrCandidate("Andy Wightman") to 3367,
            ),
        )
        val previousVotes = Publisher(
            mapOf(
                con to 44693,
                lab to 22894,
                ld to 27223,
                snp to 81600,
                grn to 14781,
                Party.OTHERS to 14122,
            ),
        )
        val title = Publisher("HIGHLANDS AND ISLANDS")
        val voteHeader = Publisher("REGIONAL VOTES")
        val voteSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2016")
        val swingHeader = Publisher("SWING SINCE 2016")
        val swingPartyOrder = listOf(grn, snp, lab, ld, con)
        val panel = partyOrCandidateVotes(
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
        compareRendering("SimpleVoteViewPanel", "PartyOrCandidates", panel)
        assertPublishes(
            panel.altText,
            """
                HIGHLANDS AND ISLANDS

                REGIONAL VOTES (CHANGE SINCE 2016)
                SCOTTISH NATIONAL PARTY: 40.4% (+0.6%)
                CONSERVATIVE: 25.4% (+3.7%)
                LIBERAL DEMOCRATS: 11.2% (-2.1%)
                LABOUR: 9.5% (-1.6%)
                GREEN: 7.4% (+0.2%)
                ALBA: 1.6% (*)
                ANDY WIGHTMAN: 1.4% (*)
                SCOTTISH FAMILY PARTY: 0.8% (*)
                ALL FOR UNITY: 0.6% (*)
                ABOLISH THE SCOTTISH PARLIAMENT: 0.3% (*)
                FREEDOM ALLIANCE: 0.3% (*)
                REFORM UK: 0.2% (*)
                LIBERTARIAN: 0.2% (*)
                UK INDEPENDENT PARTY: 0.2% (*)
                RESTORE SCOTLAND: 0.2% (*)
                TRADE UNIONIST AND SOCIALIST COALITION: 0.1% (*)
                HAZEL MANSFIELD: 0.1% (*)
                OTHERS: - (-0.8%)
                * CHANGE INCLUDED IN OTHERS
                
                SWING SINCE 2016: 1.5% SWING SNP TO CON
            """.trimIndent(),
        )
    }

    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = SimpleVoteViewPanelTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return readShapes(peiMap, "DIST_NO", Int::class.java)
    }

    private fun peiShapesByRegion(): Map<String, Shape> {
        val keys = mapOf(
            "Cardigan" to setOf(4, 2, 5, 3, 7, 1, 6),
            "Malpeque" to setOf(19, 15, 16, 20, 17, 18, 8),
            "Charlottetown" to setOf(11, 13, 9, 12, 14, 10),
            "Egmont" to setOf(26, 24, 25, 22, 21, 27, 23),
        )
        val shapesByDistrict = peiShapesByDistrict()
        return keys.mapValues { e ->
            e.value.map { shapesByDistrict[it]!! }
                .reduce { acc, shape ->
                    val ret = Area(acc)
                    ret.add(Area(shape))
                    ret
                }
        }
    }
}
