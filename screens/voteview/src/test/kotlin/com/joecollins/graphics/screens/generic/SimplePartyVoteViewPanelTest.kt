package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.PartyMap.Companion.createPartyMap
import com.joecollins.graphics.screens.generic.ResultMap.Companion.createResultMap
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.partyOrCandidateVotes
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.partyPct
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.partyRangeVotes
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.partyVotes
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanelTest.Companion.peiShapesByDistrict
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanelTest.Companion.peiShapesByRegion
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCandidate
import com.joecollins.models.general.PartyResult.Companion.elected
import com.joecollins.models.general.PartyResult.Companion.leading
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Dimension
import java.text.DecimalFormat
import java.util.IdentityHashMap
import kotlin.collections.set

class SimplePartyVoteViewPanelTest {

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
                SCOTTISH NATIONAL PARTY: 96,433 (40.4%, +0.6%)
                CONSERVATIVE: 60,779 (25.4%, +3.7%)
                LIBERAL DEMOCRATS: 26,771 (11.2%, -2.1%)
                LABOUR: 22,713 (9.5%, -1.6%)
                GREEN: 17,729 (7.4%, +0.2%)
                ALBA: 3,828 (1.6%, *)
                ANDY WIGHTMAN: 3,367 (1.4%, *)
                SCOTTISH FAMILY PARTY: 1,976 (0.8%, *)
                ALL FOR UNITY: 1,540 (0.6%, *)
                ABOLISH THE SCOTTISH PARLIAMENT: 686 (0.3%, *)
                FREEDOM ALLIANCE: 671 (0.3%, *)
                REFORM UK: 547 (0.2%, *)
                LIBERTARIAN: 488 (0.2%, *)
                UK INDEPENDENT PARTY: 457 (0.2%, *)
                RESTORE SCOTLAND: 437 (0.2%, *)
                TRADE UNIONIST AND SOCIALIST COALITION: 280 (0.1%, *)
                HAZEL MANSFIELD: 219 (0.1%, *)
                OTHERS: - (-0.8%)
                * CHANGE INCLUDED IN OTHERS
                
                SWING SINCE 2016: 1.5% SWING SNP TO CON
            """.trimIndent(),
        )
    }

    @Test
    fun testCurrPct() {
        val fpo = Party("Freedom Party", "FPÖ", Color.BLUE)
        val ovp = Party("People's Party", "ÖVP", Color.CYAN)
        val spo = Party("Social Democratic Party", "SPÖ", Color.RED)
        val neos = Party("New Austria and Liberal Forum", "NEOS", Color.MAGENTA)
        val grune = Party("Green", "GRÜNE", Color.GREEN)
        val kpo = Party("Communist", "KPÖ", Color.RED.darker())
        val bier = Party("Bier Party", "BIER", Color.YELLOW)
        val oth = Party.OTHERS

        val panel = partyPct(
            current = {
                votes = mapOf(
                    fpo to 0.291,
                    ovp to 0.262,
                    spo to 0.204,
                    neos to 0.088,
                    grune to 0.086,
                    kpo to 0.029,
                    bier to 0.021,
                    oth to 0.017,
                ).asOneTimePublisher()
                header = "EXIT POLL".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = mapOf(
                    ovp to 1789417,
                    spo to 1011868,
                    fpo to 772666,
                    grune to 664055,
                    neos to 387124,
                    kpo to 32736,
                    bier to 4946,
                    oth to 114434,
                ).asOneTimePublisher()
                header = "CHANGE SINCE 2019".asOneTimePublisher()
                swing = {
                    partyOrder = listOf(spo, oth, ovp, fpo)
                    header = "SWING SINCE 2019".asOneTimePublisher()
                }
            },
            title = "AUSTRIA".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PartyPct-1", panel)
        assertPublishes(
            panel.altText,
            """
                AUSTRIA

                EXIT POLL (CHANGE SINCE 2019)
                FREEDOM PARTY: 29.1% (+12.9%)
                PEOPLE'S PARTY: 26.2% (-11.3%)
                SOCIAL DEMOCRATIC PARTY: 20.4% (-0.8%)
                NEW AUSTRIA AND LIBERAL FORUM: 8.8% (+0.7%)
                GREEN: 8.6% (-5.3%)
                COMMUNIST: 2.9% (+2.2%)
                BIER PARTY: 2.1% (+2.0%)
                OTHERS: 1.7% (-0.7%)
                
                SWING SINCE 2019: 12.1% SWING ÖVP TO FPÖ
            """.trimIndent(),
        )
    }

    @Test
    fun testPercentageBasedWinningLine() {
        val spd = Party("Social Democratic Party", "SPD", Color.RED)
        val union = Party("Christian Democratic Union/Christian Social Union", "CDU/CSU", Color.BLACK)
        val grune = Party("Grüne", "GRÜNE", Color.GREEN.darker())
        val fdp = Party("Free Democratic Party", "FDP", Color.YELLOW)
        val afd = Party("Alternative for Germany", "AfD", Color.CYAN.darker())
        val linke = Party("The Left", "LINKE", Color.RED.darker())
        val oth = Party.OTHERS

        val currVotes = Publisher(emptyMap<Party, Int>())
        val prevVotes = Publisher(emptyMap<Party, Int>())
        val pctReporting = Publisher(0.0)
        val reporting = Publisher("0/299")
        val swingOrder = listOf(linke, spd, grune, oth, fdp, union, afd)

        val panel = partyVotes(
            current = {
                votes = currVotes
                header = "SECOND VOTE".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
                this.progressLabel = reporting
                this.pctReporting = pctReporting
            },
            prev = {
                votes = prevVotes
                header = "CHANGE SINCE 2017".asOneTimePublisher()
                this.swing = {
                    partyOrder = swingOrder
                    header = "SWING SINCE 2017".asOneTimePublisher()
                }
            },
            winningLine = {
                percentage(0.05.asOneTimePublisher()) { "5% TO ENTER BUNDESTAG" }
            },
            title = "GERMANY".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "PctWinningLine-1", panel)
        assertPublishes(
            panel.altText,
            """
                GERMANY

                SECOND VOTE [0/299] (CHANGE SINCE 2017)
                5% TO ENTER BUNDESTAG

                SWING SINCE 2017: NOT AVAILABLE
            """.trimIndent(),
        )

        currVotes.submit(mapOf(spd to 494055, union to 388399, grune to 322763, fdp to 220039, afd to 119566, linke to 64238, oth to 153694))
        pctReporting.submit(11.0 / 299)
        reporting.submit("11/299")
        prevVotes.submit(mapOf(union to 583135, spd to 399505, fdp to 216844, grune to 205471, afd to 140362, linke to 124678, oth to 45646))
        compareRendering("SimpleVoteViewPanel", "PctWinningLine-2", panel)
        assertPublishes(
            panel.altText,
            """
                GERMANY
                
                SECOND VOTE [11/299] (CHANGE SINCE 2017)
                SOCIAL DEMOCRATIC PARTY: 28.0% (+4.7%)
                CHRISTIAN DEMOCRATIC UNION/CHRISTIAN SOCIAL UNION: 22.0% (-12.0%)
                GRÜNE: 18.3% (+6.3%)
                FREE DEMOCRATIC PARTY: 12.5% (-0.2%)
                ALTERNATIVE FOR GERMANY: 6.8% (-1.4%)
                THE LEFT: 3.6% (-3.6%)
                OTHERS: 8.7% (+6.1%)
                5% TO ENTER BUNDESTAG
                
                SWING SINCE 2017: 8.3% SWING CDU/CSU TO SPD
            """.trimIndent(),
        )

        currVotes.submit(mapOf(spd to 12184094, union to 13233971, grune to 6435360, fdp to 4019562, afd to 4699917, linke to 2286070, oth to 3359844))
        pctReporting.submit(1.0)
        reporting.submit("299/299")
        prevVotes.submit(mapOf(union to 15317344, spd to 9539381, afd to 5878115, fdp to 4999449, linke to 4297270, grune to 4158400, oth to 2325533))
        compareRendering("SimpleVoteViewPanel", "PctWinningLine-3", panel)
        assertPublishes(
            panel.altText,
            """
                GERMANY

                SECOND VOTE [299/299] (CHANGE SINCE 2017)
                CHRISTIAN DEMOCRATIC UNION/CHRISTIAN SOCIAL UNION: 28.6% (-4.3%)
                SOCIAL DEMOCRATIC PARTY: 26.4% (+5.9%)
                GRÜNE: 13.9% (+5.0%)
                ALTERNATIVE FOR GERMANY: 10.2% (-2.5%)
                FREE DEMOCRATIC PARTY: 8.7% (-2.1%)
                THE LEFT: 4.9% (-4.3%)
                OTHERS: 7.3% (+2.3%)
                5% TO ENTER BUNDESTAG
                
                SWING SINCE 2017: 5.1% SWING CDU/CSU TO SPD
            """.trimIndent(),
        )
    }
}
