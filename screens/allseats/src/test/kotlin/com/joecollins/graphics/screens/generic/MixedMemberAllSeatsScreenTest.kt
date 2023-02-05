package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color

class MixedMemberAllSeatsScreenTest {

    @Test
    fun testMultiRegionMixedMember() {
        val currConstituencies = Publisher<Map<WalesConstituency, PartyResult?>>(walesConstituencies.associateWith { null })
        val currRegions = Publisher(walesRegion.associateWith { emptyList<PartyResult>() })

        val screen = MixedMemberAllSeatsScreen.multiRegion(
            walesConstituencies.associateWith { it.prevWinner }.asOneTimePublisher(),
            currConstituencies,
            { c -> c.name.uppercase() },
            walesRegion.associateWith { it.prevListSeats }.asOneTimePublisher(),
            currRegions,
            { r -> "LIST: ${r.name.uppercase()}" },
            { r -> walesConstituencies.filter { c -> c.region == r } },
        )
            .withRegion("MID AND WEST".asOneTimePublisher(), midWestWales)
            .withRegion("NORTH".asOneTimePublisher(), northWales)
            .withRegion("SOUTH CENTRAL".asOneTimePublisher(), southCentralWales)
            .withRegion("SOUTH EAST".asOneTimePublisher(), southEastWales)
            .withRegion("SOUTH WEST".asOneTimePublisher(), southWestWales)
            .build("WALES: ALL SEATS".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("MixedMemberAllSeatsScreen", "MultiRegion-1", screen)
        assertPublishes(
            screen.altText,
            """
            WALES: ALL SEATS
            
            MID AND WEST
            PENDING PC: 3 + 1
            PENDING CON: 3 + 0
            PENDING LAB: 1 + 2
            PENDING LD: 1 + 0
            PENDING UKIP: 0 + 1
            
            NORTH
            PENDING LAB: 5 + 0
            PENDING CON: 2 + 1
            PENDING PC: 2 + 1
            PENDING UKIP: 0 + 2
            
            SOUTH CENTRAL
            PENDING LAB: 7 + 0
            PENDING PC: 1 + 1
            PENDING CON: 0 + 2
            PENDING UKIP: 0 + 1
            
            SOUTH EAST
            PENDING LAB: 7 + 0
            PENDING CON: 1 + 1
            PENDING UKIP: 0 + 2
            PENDING PC: 0 + 1
            
            SOUTH WEST
            PENDING LAB: 7 + 0
            PENDING PC: 0 + 2
            PENDING UKIP: 0 + 1
            PENDING CON: 0 + 1
            """.trimIndent(),
        )

        currConstituencies.submit(
            walesConstituencies.associateWith { PartyResult(it.currWinner, true) },
        )
        compareRendering("MixedMemberAllSeatsScreen", "MultiRegion-2", screen)
        assertPublishes(
            screen.altText,
            """
            WALES: ALL SEATS
            
            MID AND WEST
            PC HOLD: 3 + 0
            CON HOLD: 3 + 0
            CON GAIN FROM LD: 1 + 0
            LAB HOLD: 1 + 0
            PENDING LAB: 0 + 2
            PENDING UKIP: 0 + 1
            PENDING PC: 0 + 1
            
            NORTH
            LAB HOLD: 4 + 0
            CON HOLD: 2 + 0
            PC HOLD: 2 + 0
            CON GAIN FROM LAB: 1 + 0
            PENDING UKIP: 0 + 2
            PENDING PC: 0 + 1
            PENDING CON: 0 + 1
            
            SOUTH CENTRAL
            LAB HOLD: 7 + 0
            LAB GAIN FROM PC: 1 + 0
            PENDING CON: 0 + 2
            PENDING UKIP: 0 + 1
            PENDING PC: 0 + 1
            
            SOUTH EAST
            LAB HOLD: 7 + 0
            CON HOLD: 1 + 0
            PENDING UKIP: 0 + 2
            PENDING CON: 0 + 1
            PENDING PC: 0 + 1
            
            SOUTH WEST
            LAB HOLD: 7 + 0
            PENDING PC: 0 + 2
            PENDING UKIP: 0 + 1
            PENDING CON: 0 + 1
            """.trimIndent(),
        )

        currRegions.submit(
            walesRegion.associateWith { it.currListSeats.map { p -> PartyResult(p, true) } },
        )
        compareRendering("MixedMemberAllSeatsScreen", "MultiRegion-3", screen)
        assertPublishes(
            screen.altText,
            """
            WALES: ALL SEATS
            
            MID AND WEST
            PC HOLD: 3 + 1
            CON HOLD: 3 + 0
            LAB HOLD: 1 + 2
            CON GAIN FROM LD: 1 + 0
            LD GAIN FROM UKIP: 0 + 1
            
            NORTH
            LAB HOLD: 4 + 0
            CON HOLD: 2 + 1
            PC HOLD: 2 + 1
            CON GAIN FROM LAB: 1 + 0
            LAB GAIN FROM UKIP: 0 + 1
            CON GAIN FROM UKIP: 0 + 1
            
            SOUTH CENTRAL
            LAB HOLD: 7 + 0
            LAB GAIN FROM PC: 1 + 0
            CON HOLD: 0 + 2
            PC HOLD: 0 + 1
            PC GAIN FROM UKIP: 0 + 1
            
            SOUTH EAST
            LAB HOLD: 7 + 0
            CON HOLD: 1 + 1
            PC HOLD: 0 + 1
            CON GAIN FROM UKIP: 0 + 1
            PC GAIN FROM UKIP: 0 + 1
            
            SOUTH WEST
            LAB HOLD: 7 + 0
            PC HOLD: 0 + 2
            CON HOLD: 0 + 1
            CON GAIN FROM UKIP: 0 + 1
            """.trimIndent(),
        )
    }

    data class WalesRegion(val name: String, val currListSeats: List<Party>, val prevListSeats: List<Party>)
    data class WalesConstituency(val name: String, val currWinner: Party, val prevWinner: Party, val region: WalesRegion)
    companion object {
        private val lab = Party("Labour", "LAB", Color.RED)
        private val con = Party("Conservative", "CON", Color.BLUE)
        private val ld = Party("Liberal Democrat", "LD", Color.ORANGE)
        private val pc = Party("Plaid Cymru", "PC", Color.GREEN.darker().darker())
        private val ukip = Party("UK Independence Party", "UKIP", Color.MAGENTA.darker())

        private val midWestWales = WalesRegion(
            "Mid and West Wales",
            listOf(pc, lab, lab, ld),
            listOf(lab, ukip, pc, lab),
        )
        private val northWales = WalesRegion(
            "North Wales",
            listOf(lab, con, con, pc),
            listOf(pc, con, ukip, ukip),
        )
        private val southCentralWales = WalesRegion(
            "South Wales Central",
            listOf(con, pc, con, pc),
            listOf(con, ukip, con, pc),
        )
        private val southEastWales = WalesRegion(
            "South Wales East",
            listOf(pc, con, con, pc),
            listOf(con, pc, ukip, ukip),
        )
        private val southWestWales = WalesRegion(
            "South Wales West",
            listOf(con, pc, con, pc),
            listOf(ukip, pc, con, pc),
        )
        private val walesRegion = listOf(midWestWales, northWales, southCentralWales, southEastWales, southWestWales)
        private val walesConstituencies = listOf(
            WalesConstituency("Aberavon", lab, lab, southWestWales),
            WalesConstituency("Aberconwy", con, con, northWales),
            WalesConstituency("Alyn and Deeside", lab, lab, northWales),
            WalesConstituency("Arfon", pc, pc, northWales),
            WalesConstituency("Blaenau Gwent", lab, lab, southEastWales),
            WalesConstituency("Brecon and Radnorshire", con, ld, midWestWales),
            WalesConstituency("Bridgend", lab, lab, southWestWales),
            WalesConstituency("Caerphilly", lab, lab, southEastWales),
            WalesConstituency("Cardiff Central", lab, lab, southCentralWales),
            WalesConstituency("Cardiff North", lab, lab, southCentralWales),
            WalesConstituency("Cardiff South and Penarth", lab, lab, southCentralWales),
            WalesConstituency("Cardiff West", lab, lab, southCentralWales),
            WalesConstituency("Carmarthen East and Dinefwr", pc, pc, midWestWales),
            WalesConstituency("Carmarthen West and South Pembrokeshire", con, con, midWestWales),
            WalesConstituency("Ceredigion", pc, pc, midWestWales),
            WalesConstituency("Clwyd South", lab, lab, northWales),
            WalesConstituency("Clwyd West", con, con, northWales),
            WalesConstituency("Cynon Valley", lab, lab, southCentralWales),
            WalesConstituency("Delyn", lab, lab, northWales),
            WalesConstituency("Dwyfor Meirionnydd", pc, pc, midWestWales),
            WalesConstituency("Gower", lab, lab, southWestWales),
            WalesConstituency("Islwyn", lab, lab, southEastWales),
            WalesConstituency("Llanelli", lab, lab, midWestWales),
            WalesConstituency("Merthyr Tydfil and Rhymney", lab, lab, southEastWales),
            WalesConstituency("Monmouth", con, con, southEastWales),
            WalesConstituency("Montgomeryshire", con, con, midWestWales),
            WalesConstituency("Neath", lab, lab, southWestWales),
            WalesConstituency("Newport East", lab, lab, southEastWales),
            WalesConstituency("Newport West", lab, lab, southEastWales),
            WalesConstituency("Ogmore", lab, lab, southWestWales),
            WalesConstituency("Pontypridd", lab, lab, southCentralWales),
            WalesConstituency("Preseli Pembrokeshire", con, con, midWestWales),
            WalesConstituency("Rhondda", lab, pc, southCentralWales),
            WalesConstituency("Swansea East", lab, lab, southWestWales),
            WalesConstituency("Swansea West", lab, lab, southWestWales),
            WalesConstituency("Torfaen", lab, lab, southEastWales),
            WalesConstituency("Vale of Clwyd", con, lab, northWales),
            WalesConstituency("Vale of Glamorgan", lab, lab, southCentralWales),
            WalesConstituency("Wrexham", lab, lab, northWales),
            WalesConstituency("Ynys Môn", pc, pc, northWales),
        )
    }
}
