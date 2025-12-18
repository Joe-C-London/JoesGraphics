package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Coalition
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color
import java.text.DecimalFormat

class RegionalSwingsScreenTest {

    @Test
    fun testRegionalSwings() {
        data class State(
            val name: String,
            val curr: Map<PartyOrCoalition, Int>,
            val prev: Map<PartyOrCoalition, Int>,
            val reporting: Double,
        )
        val alp = Party("Labor", "ALP", Color.RED)
        val lib = Party("Liberal", "LIB", Color.BLUE)
        val nat = Party("Nationals", "NAT", Color.GREEN.darker())
        val lnp = Party("Liberal National", "LNP", Color(0, 128, 255))
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val coa = Coalition("Coalition", "L/NP", Color.BLUE, lib, nat, lnp, clp)
        val states = listOf(
            State(
                "Aus Capital Terr",
                mapOf(alp to 188799, lib to 93198),
                mapOf(alp to 163878, lib to 102097),
                289113.0 / 314025,
            ),
            State(
                "New South Wales",
                mapOf(alp to 2391301, coa to 2259639),
                mapOf(coa to 2349641, alp to 2187695),
                4959584.0 / 5467993,
            ),
            State(
                "Northern Terr",
                mapOf(alp to 56065, clp to 44872),
                mapOf(alp to 56103, clp to 47415),
                106595.0 / 145851,
            ),
            State(
                "Queensland",
                mapOf(lnp to 1598802, alp to 1359224),
                mapOf(lnp to 1653261, alp to 1175757),
                3086758.0 / 3501287,
            ),
            State(
                "South Aus",
                mapOf(alp to 592512, lib to 505274),
                mapOf(alp to 543898, lib to 528750),
                1157008.0 / 1270400,
            ),
            State(
                "Tasmania",
                mapOf(alp to 189993, lib to 159705),
                mapOf(alp to 194746, lib to 153246),
                371432.0 / 401852,
            ),
            State(
                "Victoria",
                mapOf(alp to 2023880, coa to 1684528),
                mapOf(alp to 1963410, coa to 1731622),
                3931607.0 / 4339960,
            ),
            State(
                "Western Aus",
                mapOf(alp to 810206, lib to 663019),
                mapOf(lib to 778781, alp to 623093),
                1559282.0 / 1772065,
            ),
        )
        val screen = RegionalSwingsScreen.of(
            regions = states,
            name = { name.uppercase().asOneTimePublisher() },
            currVotes = { curr.asOneTimePublisher() },
            prevVotes = { prev.asOneTimePublisher() },
            swingOrder = listOf(alp, Party.OTHERS),
            numRows = 2,
            progressLabel = { (DecimalFormat("0.0%").format(reporting) + " IN").asOneTimePublisher() },
            title = "AUSTRALIA".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalSwingsScreen", "Basic", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            AUSTRALIA
            
            AUS CAPITAL TERR [92.1% IN]: 5.3% SWING LIB TO ALP
            NEW SOUTH WALES [90.7% IN]: 3.2% SWING L/NP TO ALP
            NORTHERN TERR [73.1% IN]: 1.3% SWING CLP TO ALP
            QUEENSLAND [88.2% IN]: 4.4% SWING LNP TO ALP
            SOUTH AUS [91.1% IN]: 3.3% SWING LIB TO ALP
            TASMANIA [92.4% IN]: 1.6% SWING ALP TO LIB
            VICTORIA [90.6% IN]: 1.4% SWING L/NP TO ALP
            WESTERN AUS [88.0% IN]: 10.5% SWING LIB TO ALP
            """.trimIndent(),
        )
    }

    @Test
    fun testRegionalSwingsSelectedParties() {
        data class Region(
            val name: String,
            val curr: Map<PartyOrCoalition, Int>,
            val prev: Map<PartyOrCoalition, Int>,
        )
        val con = Party("Conservative", "CON", Color.BLUE)
        val lab = Party("Labour", "LAB", Color.RED)
        val ld = Party("Liberal Democrat", "LD", Color.ORANGE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val brex = Party("Brexit", "BREX", Color.CYAN.darker())
        val ukip = Party("UK Independence Party", "UKIP", Color.MAGENTA.darker())
        val snp = Party("Scottish National Party", "SNP", Color.YELLOW)
        val pc = Party("Plaid Cymru", "PC", Color.GREEN.darker().darker())
        val oth = Party.OTHERS
        val regions = listOf(
            Region(
                "England",
                mapOf(
                    con to 12710995,
                    lab to 9123203,
                    ld to 3335992,
                    grn to 819759,
                    brex to 545160,
                    ukip to 17831,
                    oth to 349855,
                ),
                mapOf(
                    con to 12376530,
                    lab to 11381899,
                    ld to 2119310,
                    grn to 505165,
                    ukip to 556174,
                    oth to 162229,
                ),
            ),
            Region(
                "Scotland",
                mapOf(
                    snp to 1242380,
                    con to 692939,
                    lab to 511838,
                    ld to 263417,
                    grn to 28122,
                    brex to 13243,
                    ukip to 3303,
                    oth to 3819,
                ),
                mapOf(
                    snp to 977569,
                    con to 757949,
                    lab to 717007,
                    ld to 179061,
                    grn to 5886,
                    ukip to 5302,
                    oth to 6921,
                ),
            ),
            Region(
                "Wales",
                mapOf(
                    lab to 632035,
                    con to 557234,
                    pc to 153265,
                    ld to 92171,
                    brex to 83908,
                    grn to 15828,
                    oth to 9916,
                ),
                mapOf(
                    lab to 771354,
                    con to 528839,
                    pc to 164466,
                    ld to 71039,
                    ukip to 31376,
                    grn to 5128,
                    oth to 3612,
                ),
            ),
            Region(
                "North England",
                mapOf(
                    con to 2896774,
                    lab to 3160167,
                    ld to 567673,
                    grn to 173909,
                    brex to 385736,
                    ukip to 5052,
                    oth to 129259,
                ),
                mapOf(
                    con to 2796274,
                    lab to 3958320,
                    ld to 381243,
                    grn to 87992,
                    ukip to 186095,
                    oth to 49843,
                ),
            ),
            Region(
                "Midlands",
                mapOf(
                    con to 2729983,
                    lab to 1659098,
                    ld to 396568,
                    grn to 140623,
                    brex to 71990,
                    ukip to 2576,
                    oth to 49538,
                ),
                mapOf(
                    con to 2551451,
                    lab to 2127370,
                    ld to 223899,
                    grn to 79563,
                    ukip to 106464,
                    oth to 26483,
                ),
            ),
            Region(
                "South England",
                mapOf(
                    con to 7084238,
                    lab to 4303938,
                    ld to 2371751,
                    grn to 505227,
                    brex to 87434,
                    ukip to 10203,
                    oth to 171058,
                ),
                mapOf(
                    con to 7028805,
                    lab to 5296209,
                    ld to 1514168,
                    grn to 337610,
                    ukip to 263615,
                    oth to 85903,
                ),
            ),
        )
        val screen = RegionalSwingsScreen.of(
            regions = regions,
            name = { name.uppercase().asOneTimePublisher() },
            currVotes = { curr.asOneTimePublisher() },
            prevVotes = { prev.asOneTimePublisher() },
            swingOrder = listOf(lab, oth, con),
            numRows = 2,
            partyFilter = setOf(con, lab).asOneTimePublisher(),
            title = "UNITED KINGDOM".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalSwingsScreen", "PartiesSelected", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            UNITED KINGDOM

            ENGLAND: 4.8% SWING LAB TO CON
            SCOTLAND: 2.5% SWING LAB TO CON
            WALES: 5.3% SWING LAB TO CON
            NORTH ENGLAND: 6.0% SWING LAB TO CON
            MIDLANDS: 6.5% SWING LAB TO CON
            SOUTH ENGLAND: 3.6% SWING LAB TO CON
            """.trimIndent(),
        )
    }

    @Test
    fun testRegionalSwingsPartyChange() {
        data class Region(
            val name: String,
            val curr: Map<PartyOrCoalition, Int>,
            val prev: Map<PartyOrCoalition, Int>,
        )
        val lib = Party("Liberal", "LIB", Color.RED)
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ca = Party("Canadian Alliance", "CA", Color.CYAN)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val bq = Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val oth = Party.OTHERS
        val regions = listOf(
            Region(
                "Atlantic",
                mapOf(lib to 474247, con to 325272, ndp to 244871, grn to 32943, oth to 5581),
                mapOf(lib to 456797, pc to 351328, ca to 114583, ndp to 185762, grn to 968, oth to 12824),
            ),
            Region(
                "Québec",
                mapOf(lib to 1165645, con to 301539, ndp to 158427, grn to 108660, bq to 1680109, oth to 23875),
                mapOf(lib to 1529642, pc to 192153, ca to 212874, ndp to 63611, grn to 19846, bq to 1377727, oth to 61045),
            ),
            Region(
                "Ontario",
                mapOf(lib to 2278875, con to 1607337, ndp to 921240, grn to 226812, oth to 66215),
                mapOf(lib to 2292069, pc to 642438, ca to 1051209, ndp to 368709, grn to 39737, oth to 58437),
            ),
            Region(
                "Prairies",
                mapOf(lib to 553602, con to 1150344, ndp to 332821, grn to 102569, oth to 36910),
                mapOf(lib to 511418, pc to 260583, ca to 1094811, ndp to 283730, grn to 9205, oth to 17781),
            ),
            Region(
                "British Columbia",
                mapOf(lib to 494992, con to 628999, ndp to 460435, grn to 109861, oth to 39073),
                mapOf(lib to 446574, pc to 117614, ca to 797519, ndp to 182993, grn to 34294, oth to 35678),
            ),
            Region(
                "North",
                mapOf(lib to 14859, con to 6007, ndp to 9609, grn to 1402, oth to 1571),
                mapOf(lib to 15475, pc to 2906, ca to 5932, ndp to 9063, grn to 349, oth to 53),
            ),
        )
        val screen = RegionalSwingsScreen.of(
            regions = regions,
            name = { name.uppercase().asOneTimePublisher() },
            currVotes = { curr.asOneTimePublisher() },
            prevVotes = { prev.asOneTimePublisher() },
            swingOrder = listOf(ndp, grn, lib, oth, pc, bq, con, ca),
            numRows = 2,
            title = "CANADA".asOneTimePublisher(),
            partyChanges = mapOf(pc to con, ca to con).asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalSwingsScreen", "PartyChange", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            CANADA

            ATLANTIC: 7.3% SWING CON TO LIB
            QUÉBEC: 9.7% SWING LIB TO BQ
            ONTARIO: 0.1% SWING LIB TO CON
            PRAIRIES: 5.7% SWING CON TO LIB
            BRITISH COLUMBIA: 10.6% SWING CON TO LIB
            NORTH: 1.6% SWING LIB TO NDP
            """.trimIndent(),
        )
    }
}
