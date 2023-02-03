package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Coalition
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.pubsub.asOneTimePublisher
import java.awt.Color
import java.text.DecimalFormat
import org.junit.jupiter.api.Test

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
            states,
            { it.name.uppercase().asOneTimePublisher() },
            { it.curr.asOneTimePublisher() },
            { it.prev.asOneTimePublisher() },
            Comparator.comparing { if (it == alp) 0 else 1 },
            2,
        )
            .withProgressLabel { (DecimalFormat("0.0%").format(it.reporting) + " IN").asOneTimePublisher() }
            .build("AUSTRALIA".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("RegionalSwingsScreen", "Basic", screen)
        assertPublishes(
            screen.altText,
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
}
