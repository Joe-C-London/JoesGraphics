package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.RegionalBreakdownScreen.Companion.pct
import com.joecollins.graphics.screens.generic.RegionalBreakdownScreen.Companion.polls
import com.joecollins.graphics.screens.generic.RegionalBreakdownScreen.Companion.seats
import com.joecollins.graphics.screens.generic.RegionalBreakdownScreen.Companion.votes
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Coalition
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.models.general.PollsReporting
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Dimension

class RegionalBreakdownScreenTest {
    @Test
    fun testSeats() {
        val peiSeats = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganSeats = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeSeats = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownSeats = Publisher<Map<Party, Int>>(emptyMap())
        val egmontSeats = Publisher<Map<Party, Int>>(emptyMap())
        val screen = RegionalBreakdownScreen.of(
            entries = seats(
                topRowHeader = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
                topRowSeats = peiSeats,
                topRowTotal = 27.asOneTimePublisher(),
            ) {
                section(
                    items = listOf(
                        Triple("CARDIGAN", cardiganSeats, 7),
                        Triple("MALPEQUE", malpequeSeats, 7),
                        Triple("CHARLOTTETOWN", charlottetownSeats, 6),
                        Triple("EGMONT", egmontSeats, 7),
                    ),
                    header = { first.asOneTimePublisher() },
                    seats = { second },
                    total = { third.asOneTimePublisher() },
                )
            },
            header = "SEATS BY REGION".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "Seats-1", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION
            
            PRINCE EDWARD ISLAND: 0/27
            
            CARDIGAN: 0/7
            MALPEQUE: 0/7
            CHARLOTTETOWN: 0/6
            EGMONT: 0/7
            """.trimIndent(),
        )

        peiSeats.submit(mapOf(grn to 1))
        cardiganSeats.submit(mapOf(grn to 1))
        compareRendering("RegionalBreakdownScreen", "Seats-2", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION
            
            PRINCE EDWARD ISLAND: GRN 1, 1/27
            
            CARDIGAN: GRN 1, 1/7
            MALPEQUE: 0/7
            CHARLOTTETOWN: 0/6
            EGMONT: 0/7
            """.trimIndent(),
        )

        peiSeats.submit(mapOf(pc to 13, grn to 8, lib to 6))
        cardiganSeats.submit(mapOf(pc to 6, grn to 1))
        malpequeSeats.submit(mapOf(pc to 5, grn to 1, lib to 1))
        charlottetownSeats.submit(mapOf(grn to 3, lib to 2, pc to 1))
        egmontSeats.submit(mapOf(grn to 3, lib to 3, pc to 1))
        compareRendering("RegionalBreakdownScreen", "Seats-3", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION
            
            PRINCE EDWARD ISLAND: PC 13, GRN 8, LIB 6, 27/27
            
            CARDIGAN: PC 6, GRN 1, 7/7
            MALPEQUE: PC 5, GRN 1, LIB 1, 7/7
            CHARLOTTETOWN: PC 1, GRN 3, LIB 2, 6/6
            EGMONT: PC 1, GRN 3, LIB 3, 7/7
            """.trimIndent(),
        )
    }

    @Test
    fun testSeatsWithDiff() {
        val peiSeats = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganSeats = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeSeats = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownSeats = Publisher<Map<Party, Int>>(emptyMap())
        val egmontSeats = Publisher<Map<Party, Int>>(emptyMap())
        val peiDiff = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganDiff = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeDiff = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownDiff = Publisher<Map<Party, Int>>(emptyMap())
        val egmontDiff = Publisher<Map<Party, Int>>(emptyMap())

        data class Region(
            val name: String,
            val seats: Publisher<Map<Party, Int>>,
            val diff: Publisher<Map<Party, Int>>,
            val totalSeats: Int,
        )
        val screen = RegionalBreakdownScreen.of(
            entries = seats(
                topRowHeader = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
                topRowSeats = peiSeats,
                topRowDiff = peiDiff,
                topRowTotal = 27.asOneTimePublisher(),
            ) {
                section(
                    items = listOf(
                        Region("CARDIGAN", cardiganSeats, cardiganDiff, 7),
                        Region("MALPEQUE", malpequeSeats, malpequeDiff, 7),
                        Region("CHARLOTTETOWN", charlottetownSeats, charlottetownDiff, 6),
                        Region("EGMONT", egmontSeats, egmontDiff, 7),
                    ),
                    header = { name.asOneTimePublisher() },
                    seats = { seats },
                    diff = { diff },
                    total = { totalSeats.asOneTimePublisher() },
                )
            },
            header = "SEATS BY REGION".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-1", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION
            
            PRINCE EDWARD ISLAND: 0/27
            
            CARDIGAN: 0/7
            MALPEQUE: 0/7
            CHARLOTTETOWN: 0/6
            EGMONT: 0/7
            """.trimIndent(),
        )

        peiSeats.submit(mapOf(grn to 1))
        peiDiff.submit(mapOf(grn to +1, lib to -1))
        cardiganSeats.submit(mapOf(grn to 1))
        cardiganDiff.submit(mapOf(grn to +1, lib to -1))
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-2", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION
            
            PRINCE EDWARD ISLAND: GRN 1 (+1), LIB 0 (-1), 1/27
            
            CARDIGAN: GRN 1 (+1), LIB 0 (-1), 1/7
            MALPEQUE: 0/7
            CHARLOTTETOWN: 0/6
            EGMONT: 0/7
            """.trimIndent(),
        )

        peiSeats.submit(mapOf(pc to 13, grn to 8, lib to 6))
        peiDiff.submit(mapOf(pc to +5, grn to +7, lib to -12))
        cardiganSeats.submit(mapOf(pc to 6, grn to 1))
        cardiganDiff.submit(mapOf(pc to +1, grn to +1, lib to -2))
        malpequeSeats.submit(mapOf(pc to 5, grn to 1, lib to 1))
        malpequeDiff.submit(mapOf(pc to +2, grn to 0, lib to -1))
        charlottetownSeats.submit(mapOf(grn to 3, lib to 2, pc to 1))
        charlottetownDiff.submit(mapOf(grn to +3, lib to -5, pc to +1))
        egmontSeats.submit(mapOf(grn to 3, lib to 3, pc to 1))
        egmontDiff.submit(mapOf(grn to +3, lib to -4, pc to +1))
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-3", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION
            
            PRINCE EDWARD ISLAND: PC 13 (+5), GRN 8 (+7), LIB 6 (-12), 27/27
            
            CARDIGAN: PC 6 (+1), GRN 1 (+1), LIB 0 (-2), 7/7
            MALPEQUE: PC 5 (+2), GRN 1 (±0), LIB 1 (-1), 7/7
            CHARLOTTETOWN: PC 1 (+1), GRN 3 (+3), LIB 2 (-5), 6/6
            EGMONT: PC 1 (+1), GRN 3 (+3), LIB 3 (-4), 7/7
            """.trimIndent(),
        )
    }

    @Test
    fun testSeatsWithPrev() {
        val peiSeats = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganSeats = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeSeats = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownSeats = Publisher<Map<Party, Int>>(emptyMap())
        val egmontSeats = Publisher<Map<Party, Int>>(emptyMap())
        val peiPrev = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganPrev = Publisher<Map<Party, Int>>(emptyMap())
        val malpequePrev = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownPrev = Publisher<Map<Party, Int>>(emptyMap())
        val egmontPrev = Publisher<Map<Party, Int>>(emptyMap())

        data class Region(
            val name: String,
            val seats: Publisher<Map<Party, Int>>,
            val prev: Publisher<Map<Party, Int>>,
            val totalSeats: Int,
        )
        val screen = RegionalBreakdownScreen.of(
            entries = seats(
                topRowHeader = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
                topRowSeats = peiSeats,
                topRowPrev = peiPrev,
                topRowTotal = 27.asOneTimePublisher(),
            ) {
                section(
                    items = listOf(
                        Region("CARDIGAN", cardiganSeats, cardiganPrev, 7),
                        Region("MALPEQUE", malpequeSeats, malpequePrev, 7),
                        Region("CHARLOTTETOWN", charlottetownSeats, charlottetownPrev, 6),
                        Region("EGMONT", egmontSeats, egmontPrev, 7),
                    ),
                    header = { name.asOneTimePublisher() },
                    seats = { seats },
                    prev = { prev },
                    total = { totalSeats.asOneTimePublisher() },
                )
            },
            header = "SEATS BY REGION".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-1", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION
            
            PRINCE EDWARD ISLAND: 0/27
            
            CARDIGAN: 0/7
            MALPEQUE: 0/7
            CHARLOTTETOWN: 0/6
            EGMONT: 0/7
            """.trimIndent(),
        )

        peiSeats.submit(mapOf(grn to 1))
        peiPrev.submit(mapOf(lib to 1))
        cardiganSeats.submit(mapOf(grn to 1))
        cardiganPrev.submit(mapOf(lib to 1))
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-2", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION
            
            PRINCE EDWARD ISLAND: GRN 1 (+1), LIB 0 (-1), 1/27
            
            CARDIGAN: GRN 1 (+1), LIB 0 (-1), 1/7
            MALPEQUE: 0/7
            CHARLOTTETOWN: 0/6
            EGMONT: 0/7
            """.trimIndent(),
        )

        peiSeats.submit(mapOf(pc to 13, grn to 8, lib to 6))
        peiPrev.submit(mapOf(pc to 8, grn to 1, lib to 18))
        cardiganSeats.submit(mapOf(pc to 6, grn to 1))
        cardiganPrev.submit(mapOf(pc to 5, lib to 2))
        malpequeSeats.submit(mapOf(pc to 5, grn to 1, lib to 1))
        malpequePrev.submit(mapOf(pc to 3, grn to 1, lib to 2))
        charlottetownSeats.submit(mapOf(grn to 3, lib to 2, pc to 1))
        charlottetownPrev.submit(mapOf(lib to 7))
        egmontSeats.submit(mapOf(grn to 3, lib to 3, pc to 1))
        egmontPrev.submit(mapOf(lib to 7))
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-3", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION
            
            PRINCE EDWARD ISLAND: PC 13 (+5), GRN 8 (+7), LIB 6 (-12), 27/27
            
            CARDIGAN: PC 6 (+1), GRN 1 (+1), LIB 0 (-2), 7/7
            MALPEQUE: PC 5 (+2), GRN 1 (±0), LIB 1 (-1), 7/7
            CHARLOTTETOWN: PC 1 (+1), GRN 3 (+3), LIB 2 (-5), 6/6
            EGMONT: PC 1 (+1), GRN 3 (+3), LIB 3 (-4), 7/7
            """.trimIndent(),
        )
    }

    @Test
    fun testSeatsCoalition() {
        val lib = Party("Liberal", "LIB", Color.BLUE.darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val coa = Coalition("Coalition", "L/NP", lib, lnp, clp)
        val alp = Party("Labor", "ALP", Color.RED)
        val oth = Party.OTHERS

        data class State(
            val name: String,
            val seats: Map<PartyOrCoalition, Int>,
            val coalitionParty: Party?,
            val abbreviation: String,
        )
        val screen = RegionalBreakdownScreen.of(
            entries = seats(
                topRowHeader = "AUSTRALIA".asOneTimePublisher(),
                topRowSeats = mapOf(alp to 68, coa to 77, oth to 6).asOneTimePublisher(),
                topRowTotal = 151.asOneTimePublisher(),
            ) {
                section(
                    items = listOf(
                        State("New South Wales", mapOf(coa to 22, alp to 24, oth to 1), null, "NSW"),
                        State("Victoria", mapOf(coa to 15, alp to 21, oth to 2), null, "VIC"),
                        State("Queensland", mapOf(lnp to 23, alp to 6, oth to 1), lnp, "QLD"),
                        State("Western Australia", mapOf(lib to 11, alp to 5), lib, "WA"),
                        State("South Australia", mapOf(lib to 4, alp to 5, oth to 1), lib, "SA"),
                        State("Tasmania", mapOf(lib to 2, alp to 2, oth to 1), lib, "TAS"),
                        State("Australian Capital Territory", mapOf(alp to 3), lib, "ACT"),
                        State("Northern Territory", mapOf(alp to 2), clp, "NT"),
                    ),
                    header = { name.uppercase().asOneTimePublisher() },
                    seats = { seats.asOneTimePublisher() },
                    total = { seats.values.sum().asOneTimePublisher() },
                    abbreviatedHeader = { abbreviation.asOneTimePublisher() },
                    coalitionMap = { if (coalitionParty == null) null else mapOf(coa to coalitionParty).asOneTimePublisher() },
                )
            },
            header = "SEATS BY STATE".asOneTimePublisher(),
            title = "AUSTRALIA".asOneTimePublisher(),
        )

        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "Seats-C", screen)
        assertPublishes(
            screen.altText,
            """
            AUSTRALIA
            SEATS BY STATE

            AUSTRALIA: L/NP 77, ALP 68, OTH 6, 151/151

            NSW: L/NP 22, ALP 24, OTH 1, 47/47
            VIC: L/NP 15, ALP 21, OTH 2, 38/38
            QLD: LNP 23, ALP 6, OTH 1, 30/30
            WA: LIB 11, ALP 5, 16/16
            SA: LIB 4, ALP 5, OTH 1, 10/10
            TAS: LIB 2, ALP 2, OTH 1, 5/5
            ACT: ALP 3, 3/3
            NT: ALP 2, 2/2
            """.trimIndent(),
        )
    }

    @Test
    fun testSeatsWithPrevCoalition() {
        val lib = Party("Liberal", "LIB", Color.BLUE.darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val coa = Coalition("Coalition", "L/NP", lib, lnp, clp)
        val alp = Party("Labor", "ALP", Color.RED)
        val oth = Party.OTHERS

        data class State(
            val name: String,
            val seats: Map<PartyOrCoalition, Int>,
            val prev: Map<PartyOrCoalition, Int>,
            val coalitionParty: Party?,
            val abbreviation: String,
        )
        val screen = RegionalBreakdownScreen.of(
            entries = seats(
                topRowHeader = "AUSTRALIA".asOneTimePublisher(),
                topRowSeats = mapOf(alp to 68, coa to 77, oth to 6).asOneTimePublisher(),
                topRowPrev = mapOf(alp to 69, coa to 76, oth to 5).asOneTimePublisher(),
                topRowTotal = 151.asOneTimePublisher(),
            ) {
                section(
                    items = listOf(
                        State("New South Wales", mapOf(coa to 22, alp to 24, oth to 1), mapOf(coa to 23, alp to 24), null, "NSW"),
                        State("Victoria", mapOf(coa to 15, alp to 21, oth to 2), mapOf(coa to 17, alp to 18, oth to 2), null, "VIC"),
                        State("Queensland", mapOf(lnp to 23, alp to 6, oth to 1), mapOf(lnp to 21, alp to 8, oth to 1), lnp, "QLD"),
                        State("Western Australia", mapOf(lib to 11, alp to 5), mapOf(lib to 11, alp to 5), lib, "WA"),
                        State("South Australia", mapOf(lib to 4, alp to 5, oth to 1), mapOf(lib to 4, alp to 6, oth to 1), lib, "SA"),
                        State("Tasmania", mapOf(lib to 2, alp to 2, oth to 1), mapOf(alp to 4, oth to 1), lib, "TAS"),
                        State("Australian Capital Territory", mapOf(alp to 3), mapOf(alp to 2), lib, "ACT"),
                        State("Northern Territory", mapOf(alp to 2), mapOf(alp to 2), clp, "NT"),
                    ),
                    header = { name.uppercase().asOneTimePublisher() },
                    seats = { seats.asOneTimePublisher() },
                    prev = { prev.asOneTimePublisher() },
                    total = { seats.values.sum().asOneTimePublisher() },
                    abbreviatedHeader = { abbreviation.asOneTimePublisher() },
                    coalitionMap = { if (coalitionParty == null) null else mapOf(coa to coalitionParty).asOneTimePublisher() },
                )
            },
            header = "SEATS BY STATE".asOneTimePublisher(),
            title = "AUSTRALIA".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "SeatsWithPrev-C", screen)
        assertPublishes(
            screen.altText,
            """
            AUSTRALIA
            SEATS BY STATE

            AUSTRALIA: L/NP 77 (+1), ALP 68 (-1), OTH 6 (+1), 151/151

            NSW: L/NP 22 (-1), ALP 24 (±0), OTH 1 (+1), 47/47
            VIC: L/NP 15 (-2), ALP 21 (+3), OTH 2 (±0), 38/38
            QLD: LNP 23 (+2), ALP 6 (-2), OTH 1 (±0), 30/30
            WA: LIB 11 (±0), ALP 5 (±0), 16/16
            SA: LIB 4 (±0), ALP 5 (-1), OTH 1 (±0), 10/10
            TAS: LIB 2 (+2), ALP 2 (-2), OTH 1 (±0), 5/5
            ACT: ALP 3 (+1), 3/3
            NT: ALP 2 (±0), 2/2
            """.trimIndent(),
        )
    }

    @Test
    fun testSeatsWithDiffCoalition() {
        val lib = Party("Liberal", "LIB", Color.BLUE.darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val coa = Coalition("Coalition", "L/NP", lib, lnp, clp)
        val alp = Party("Labor", "ALP", Color.RED)
        val oth = Party.OTHERS

        data class State(
            val name: String,
            val seats: Map<PartyOrCoalition, Int>,
            val diff: Map<PartyOrCoalition, Int>,
            val coalitionParty: Party?,
            val abbreviation: String,
        )
        val screen = RegionalBreakdownScreen.of(
            entries = seats(
                topRowHeader = "AUSTRALIA".asOneTimePublisher(),
                topRowSeats = mapOf(alp to 68, coa to 77, oth to 6).asOneTimePublisher(),
                topRowDiff = mapOf(alp to -1, coa to +1, oth to +1).asOneTimePublisher(),
                topRowTotal = 151.asOneTimePublisher(),
            ) {
                section(
                    items = listOf(
                        State("New South Wales", mapOf(coa to 22, alp to 24, oth to 1), mapOf(coa to -1, oth to +1), null, "NSW"),
                        State("Victoria", mapOf(coa to 15, alp to 21, oth to 2), mapOf(coa to -2, alp to +3), null, "VIC"),
                        State("Queensland", mapOf(lnp to 23, alp to 6, oth to 1), mapOf(lnp to +2, alp to -2), lnp, "QLD"),
                        State("Western Australia", mapOf(lib to 11, alp to 5), emptyMap(), lib, "WA"),
                        State("South Australia", mapOf(lib to 4, alp to 5, oth to 1), mapOf(alp to -1), lib, "SA"),
                        State("Tasmania", mapOf(lib to 2, alp to 2, oth to 1), mapOf(lib to +2, alp to -2), lib, "TAS"),
                        State("Australian Capital Territory", mapOf(alp to 3), mapOf(alp to +1), lib, "ACT"),
                        State("Northern Territory", mapOf(alp to 2), mapOf(alp to 0), clp, "NT"),
                    ),
                    header = { name.uppercase().asOneTimePublisher() },
                    seats = { seats.asOneTimePublisher() },
                    diff = { diff.asOneTimePublisher() },
                    total = { seats.values.sum().asOneTimePublisher() },
                    abbreviatedHeader = { abbreviation.asOneTimePublisher() },
                    coalitionMap = { if (coalitionParty == null) null else mapOf(coa to coalitionParty).asOneTimePublisher() },
                )
            },
            header = "SEATS BY STATE".asOneTimePublisher(),
            title = "AUSTRALIA".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "SeatsWithPrev-C", screen)
        assertPublishes(
            screen.altText,
            """
            AUSTRALIA
            SEATS BY STATE

            AUSTRALIA: L/NP 77 (+1), ALP 68 (-1), OTH 6 (+1), 151/151

            NSW: L/NP 22 (-1), ALP 24 (±0), OTH 1 (+1), 47/47
            VIC: L/NP 15 (-2), ALP 21 (+3), OTH 2 (±0), 38/38
            QLD: LNP 23 (+2), ALP 6 (-2), OTH 1 (±0), 30/30
            WA: LIB 11 (±0), ALP 5 (±0), 16/16
            SA: LIB 4 (±0), ALP 5 (-1), OTH 1 (±0), 10/10
            TAS: LIB 2 (+2), ALP 2 (-2), OTH 1 (±0), 5/5
            ACT: ALP 3 (+1), 3/3
            NT: ALP 2 (±0), 2/2
            """.trimIndent(),
        )
    }

    @Test
    fun testSeatsWithLimitedColumns() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val bq = Party("Bloc Quebecois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        val federalSeats = Publisher(emptyMap<Party, Int>()) to Publisher(emptyMap<Party, Int>())
        val provincialSeats = listOf(
            "Newfoundland and Labrador" to 7,
            "Nova Scotia" to 11,
            "Prince Edward Island" to 4,
            "New Brunswick" to 10,
            "Quebec" to 78,
            "Ontario" to 121,
            "Manitoba" to 14,
            "Saskatchewan" to 14,
            "Alberta" to 34,
            "British Columbia" to 42,
            "Northern Canada" to 3,
        ).associateWith { Publisher(emptyMap<Party, Int>()) to Publisher(emptyMap<Party, Int>()) }

        val screen = RegionalBreakdownScreen.of(
            entries = seats(
                topRowHeader = "CANADA".asOneTimePublisher(),
                topRowSeats = federalSeats.first,
                topRowPrev = federalSeats.second,
                topRowTotal = 338.asOneTimePublisher(),
                maxColumns = 4.asOneTimePublisher(),
            ) {
                section(
                    items = provincialSeats.entries,
                    header = { key.first.uppercase().asOneTimePublisher() },
                    seats = { value.first },
                    prev = { value.second },
                    total = { key.second.asOneTimePublisher() },
                )
            },
            header = "SEATS BY PROVINCE".asOneTimePublisher(),
            title = "CANADA".asOneTimePublisher(),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("RegionalBreakdownScreen", "SeatsWithLimitedColumns-1", screen)
        assertPublishes(
            screen.altText,
            """
            CANADA
            SEATS BY PROVINCE
            
            CANADA: 0/338
            
            NEWFOUNDLAND AND LABRADOR: 0/7
            NOVA SCOTIA: 0/11
            PRINCE EDWARD ISLAND: 0/4
            NEW BRUNSWICK: 0/10
            QUEBEC: 0/78
            ONTARIO: 0/121
            MANITOBA: 0/14
            SASKATCHEWAN: 0/14
            ALBERTA: 0/34
            BRITISH COLUMBIA: 0/42
            NORTHERN CANADA: 0/3
            """.trimIndent(),
        )

        federalSeats.also {
            it.first.submit(mapOf(lib to 6, ndp to 1))
            it.second.submit(mapOf(lib to 7))
        }
        provincialSeats["Newfoundland and Labrador" to 7]!!.also {
            it.first.submit(mapOf(lib to 6, ndp to 1))
            it.second.submit(mapOf(lib to 7))
        }
        compareRendering("RegionalBreakdownScreen", "SeatsWithLimitedColumns-2", screen)
        assertPublishes(
            screen.altText,
            """
            CANADA
            SEATS BY PROVINCE
            
            CANADA: LIB 6 (-1), NDP 1 (+1), 7/338
            
            NEWFOUNDLAND AND LABRADOR: LIB 6 (-1), NDP 1 (+1), 7/7
            NOVA SCOTIA: 0/11
            PRINCE EDWARD ISLAND: 0/4
            NEW BRUNSWICK: 0/10
            QUEBEC: 0/78
            ONTARIO: 0/121
            MANITOBA: 0/14
            SASKATCHEWAN: 0/14
            ALBERTA: 0/34
            BRITISH COLUMBIA: 0/42
            NORTHERN CANADA: 0/3
            """.trimIndent(),
        )

        federalSeats.also {
            it.first.submit(mapOf(lib to 26, con to 4, ndp to 1, grn to 1))
            it.second.submit(mapOf(lib to 32))
        }
        provincialSeats["Nova Scotia" to 11]!!.also {
            it.first.submit(mapOf(lib to 10, con to 1))
            it.second.submit(mapOf(lib to 11))
        }
        provincialSeats["Prince Edward Island" to 4]!!.also {
            it.first.submit(mapOf(lib to 4))
            it.second.submit(mapOf(lib to 4))
        }
        provincialSeats["New Brunswick" to 10]!!.also {
            it.first.submit(mapOf(lib to 6, con to 3, grn to 1))
            it.second.submit(mapOf(lib to 10))
        }
        compareRendering("RegionalBreakdownScreen", "SeatsWithLimitedColumns-3", screen)
        assertPublishes(
            screen.altText,
            """
            CANADA
            SEATS BY PROVINCE
            
            CANADA: LIB 26 (-6), CON 4 (+4), NDP 1 (+1), GRN 1 (+1), 32/338
            
            NEWFOUNDLAND AND LABRADOR: LIB 6 (-1), NDP 1 (+1), 7/7
            NOVA SCOTIA: LIB 10 (-1), CON 1 (+1), 11/11
            PRINCE EDWARD ISLAND: LIB 4 (±0), 4/4
            NEW BRUNSWICK: LIB 6 (-4), CON 3 (+3), GRN 1 (+1), 10/10
            QUEBEC: 0/78
            ONTARIO: 0/121
            MANITOBA: 0/14
            SASKATCHEWAN: 0/14
            ALBERTA: 0/34
            BRITISH COLUMBIA: 0/42
            NORTHERN CANADA: 0/3
            """.trimIndent(),
        )

        federalSeats.also {
            it.first.submit(mapOf(lib to 145, con to 104, ndp to 13, bq to 32, grn to 1))
            it.second.submit(mapOf(lib to 166, con to 89, ndp to 30, bq to 10))
        }
        provincialSeats["Quebec" to 78]!!.also {
            it.first.submit(mapOf(lib to 35, con to 10, ndp to 1, bq to 32))
            it.second.submit(mapOf(lib to 40, con to 12, ndp to 16, bq to 10))
        }
        provincialSeats["Ontario" to 121]!!.also {
            it.first.submit(mapOf(lib to 79, con to 36, ndp to 6))
            it.second.submit(mapOf(lib to 80, con to 33, ndp to 8))
        }
        provincialSeats["Manitoba" to 14]!!.also {
            it.first.submit(mapOf(lib to 4, con to 7, ndp to 3))
            it.second.submit(mapOf(lib to 7, con to 5, ndp to 2))
        }
        provincialSeats["Saskatchewan" to 14]!!.also {
            it.first.submit(mapOf(con to 14))
            it.second.submit(mapOf(lib to 1, con to 10, ndp to 3))
        }
        provincialSeats["Alberta" to 34]!!.also {
            it.first.submit(mapOf(con to 33, ndp to 1))
            it.second.submit(mapOf(lib to 4, con to 29, ndp to 1))
        }
        provincialSeats["Northern Canada" to 3]!!.also {
            it.first.submit(mapOf(lib to 1, ndp to 1))
            it.second.submit(mapOf(lib to 2))
        }
        compareRendering("RegionalBreakdownScreen", "SeatsWithLimitedColumns-4", screen)
        assertPublishes(
            screen.altText,
            """
            CANADA
            SEATS BY PROVINCE
            
            CANADA: LIB 145 (-21), CON 104 (+15), BQ 32 (+22), OTH 14 (-16), 295/338
            
            NEWFOUNDLAND AND LABRADOR: LIB 6 (-1), OTH 1 (+1), 7/7
            NOVA SCOTIA: LIB 10 (-1), CON 1 (+1), 11/11
            PRINCE EDWARD ISLAND: LIB 4 (±0), 4/4
            NEW BRUNSWICK: LIB 6 (-4), CON 3 (+3), OTH 1 (+1), 10/10
            QUEBEC: LIB 35 (-5), CON 10 (-2), BQ 32 (+22), OTH 1 (-15), 78/78
            ONTARIO: LIB 79 (-1), CON 36 (+3), OTH 6 (-2), 121/121
            MANITOBA: LIB 4 (-3), CON 7 (+2), OTH 3 (+1), 14/14
            SASKATCHEWAN: LIB 0 (-1), CON 14 (+4), OTH 0 (-3), 14/14
            ALBERTA: LIB 0 (-4), CON 33 (+4), OTH 1 (±0), 34/34
            BRITISH COLUMBIA: 0/42
            NORTHERN CANADA: LIB 1 (-1), OTH 1 (+1), 2/3
            """.trimIndent(),
        )

        federalSeats.also {
            it.first.submit(mapOf(lib to 157, con to 121, ndp to 24, bq to 32, grn to 3, ind to 1))
            it.second.submit(mapOf(lib to 184, con to 99, ndp to 44, bq to 10, grn to 1))
        }
        provincialSeats["British Columbia" to 42]!!.also {
            it.first.submit(mapOf(lib to 11, con to 17, ndp to 11, grn to 2, ind to 1))
            it.second.submit(mapOf(lib to 17, con to 10, ndp to 14, grn to 1))
        }
        provincialSeats["Northern Canada" to 3]!!.also {
            it.first.submit(mapOf(lib to 2, ndp to 1))
            it.second.submit(mapOf(lib to 3))
        }
        compareRendering("RegionalBreakdownScreen", "SeatsWithLimitedColumns-5", screen)
        assertPublishes(
            screen.altText,
            """
            CANADA
            SEATS BY PROVINCE
            
            CANADA: LIB 157 (-27), CON 121 (+22), BQ 32 (+22), OTH 28 (-17), 338/338
            
            NEWFOUNDLAND AND LABRADOR: LIB 6 (-1), OTH 1 (+1), 7/7
            NOVA SCOTIA: LIB 10 (-1), CON 1 (+1), 11/11
            PRINCE EDWARD ISLAND: LIB 4 (±0), 4/4
            NEW BRUNSWICK: LIB 6 (-4), CON 3 (+3), OTH 1 (+1), 10/10
            QUEBEC: LIB 35 (-5), CON 10 (-2), BQ 32 (+22), OTH 1 (-15), 78/78
            ONTARIO: LIB 79 (-1), CON 36 (+3), OTH 6 (-2), 121/121
            MANITOBA: LIB 4 (-3), CON 7 (+2), OTH 3 (+1), 14/14
            SASKATCHEWAN: LIB 0 (-1), CON 14 (+4), OTH 0 (-3), 14/14
            ALBERTA: LIB 0 (-4), CON 33 (+4), OTH 1 (±0), 34/34
            BRITISH COLUMBIA: LIB 11 (-6), CON 17 (+7), OTH 14 (-1), 42/42
            NORTHERN CANADA: LIB 2 (-1), OTH 1 (+1), 3/3
            """.trimIndent(),
        )
    }

    @Test
    fun testSeatsNoProgress() {
        val peiSeats = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganSeats = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeSeats = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownSeats = Publisher<Map<Party, Int>>(emptyMap())
        val egmontSeats = Publisher<Map<Party, Int>>(emptyMap())
        val screen = RegionalBreakdownScreen.of(
            entries = seats(
                topRowHeader = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
                topRowSeats = peiSeats,
            ) {
                section(
                    items = listOf(
                        Pair("CARDIGAN", cardiganSeats),
                        Pair("MALPEQUE", malpequeSeats),
                        Pair("CHARLOTTETOWN", charlottetownSeats),
                        Pair("EGMONT", egmontSeats),
                    ),
                    header = { first.asOneTimePublisher() },
                    seats = { second },
                )
            },
            header = "SEATS BY REGION".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)

        peiSeats.submit(mapOf(pc to 13, grn to 8, lib to 6))
        cardiganSeats.submit(mapOf(pc to 6, grn to 1))
        malpequeSeats.submit(mapOf(pc to 5, grn to 1, lib to 1))
        charlottetownSeats.submit(mapOf(grn to 3, lib to 2, pc to 1))
        egmontSeats.submit(mapOf(grn to 3, lib to 3, pc to 1))
        compareRendering("RegionalBreakdownScreen", "Seats-NoProgress", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION
            
            PRINCE EDWARD ISLAND: PC 13, GRN 8, LIB 6
            
            CARDIGAN: PC 6, GRN 1
            MALPEQUE: PC 5, GRN 1, LIB 1
            CHARLOTTETOWN: PC 1, GRN 3, LIB 2
            EGMONT: PC 1, GRN 3, LIB 3
            """.trimIndent(),
        )
    }

    @Test
    fun testSeatsProgressInHeader() {
        val peiSeats = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganSeats = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeSeats = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownSeats = Publisher<Map<Party, Int>>(emptyMap())
        val egmontSeats = Publisher<Map<Party, Int>>(emptyMap())
        val screen = RegionalBreakdownScreen.of(
            entries = seats(
                topRowHeader = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
                topRowSeats = peiSeats,
            ) {
                section(
                    items = listOf(
                        Pair("CARDIGAN", cardiganSeats),
                        Pair("MALPEQUE", malpequeSeats),
                        Pair("CHARLOTTETOWN", charlottetownSeats),
                        Pair("EGMONT", egmontSeats),
                    ),
                    header = { first.asOneTimePublisher() },
                    seats = { second },
                )
            },
            header = "SEATS BY REGION".asOneTimePublisher(),
            progressLabel = "27/27".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)

        peiSeats.submit(mapOf(pc to 13, grn to 8, lib to 6))
        cardiganSeats.submit(mapOf(pc to 6, grn to 1))
        malpequeSeats.submit(mapOf(pc to 5, grn to 1, lib to 1))
        charlottetownSeats.submit(mapOf(grn to 3, lib to 2, pc to 1))
        egmontSeats.submit(mapOf(grn to 3, lib to 3, pc to 1))
        compareRendering("RegionalBreakdownScreen", "Seats-ProgressInHeader", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION [27/27]
            
            PRINCE EDWARD ISLAND: PC 13, GRN 8, LIB 6
            
            CARDIGAN: PC 6, GRN 1
            MALPEQUE: PC 5, GRN 1, LIB 1
            CHARLOTTETOWN: PC 1, GRN 3, LIB 2
            EGMONT: PC 1, GRN 3, LIB 3
            """.trimIndent(),
        )
    }

    @Test
    fun testSeatsNoZero() {
        val peiSeats = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganSeats = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeSeats = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownSeats = Publisher<Map<Party, Int>>(emptyMap())
        val egmontSeats = Publisher<Map<Party, Int>>(emptyMap())
        val screen = RegionalBreakdownScreen.of(
            entries = seats(
                topRowHeader = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
                topRowSeats = peiSeats,
                topRowTotal = 27.asOneTimePublisher(),
                showZero = false,
            ) {
                section(
                    items = listOf(
                        Triple("CARDIGAN", cardiganSeats, 7),
                        Triple("MALPEQUE", malpequeSeats, 7),
                        Triple("CHARLOTTETOWN", charlottetownSeats, 6),
                        Triple("EGMONT", egmontSeats, 7),
                    ),
                    header = { first.asOneTimePublisher() },
                    seats = { second },
                    total = { third.asOneTimePublisher() },
                )
            },
            header = "SEATS BY REGION".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "SeatsNoZero-1", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION
            
            PRINCE EDWARD ISLAND: 0/27
            
            CARDIGAN: 0/7
            MALPEQUE: 0/7
            CHARLOTTETOWN: 0/6
            EGMONT: 0/7
            """.trimIndent(),
        )

        peiSeats.submit(mapOf(grn to 1))
        cardiganSeats.submit(mapOf(grn to 1))
        compareRendering("RegionalBreakdownScreen", "SeatsNoZero-2", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION
            
            PRINCE EDWARD ISLAND: GRN 1, 1/27
            
            CARDIGAN: GRN 1, 1/7
            MALPEQUE: 0/7
            CHARLOTTETOWN: 0/6
            EGMONT: 0/7
            """.trimIndent(),
        )

        peiSeats.submit(mapOf(pc to 13, grn to 8, lib to 6))
        cardiganSeats.submit(mapOf(pc to 6, grn to 1))
        malpequeSeats.submit(mapOf(pc to 5, grn to 1, lib to 1))
        charlottetownSeats.submit(mapOf(grn to 3, lib to 2, pc to 1))
        egmontSeats.submit(mapOf(grn to 3, lib to 3, pc to 1))
        compareRendering("RegionalBreakdownScreen", "SeatsNoZero-3", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            SEATS BY REGION
            
            PRINCE EDWARD ISLAND: PC 13, GRN 8, LIB 6, 27/27
            
            CARDIGAN: PC 6, GRN 1, 7/7
            MALPEQUE: PC 5, GRN 1, LIB 1, 7/7
            CHARLOTTETOWN: PC 1, GRN 3, LIB 2, 6/6
            EGMONT: PC 1, GRN 3, LIB 3, 7/7
            """.trimIndent(),
        )
    }

    @Test
    fun testVotes() {
        val peiVotes = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganVotes = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeVotes = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownVotes = Publisher<Map<Party, Int>>(emptyMap())
        val egmontVotes = Publisher<Map<Party, Int>>(emptyMap())
        val peiPct = Publisher(0.0)
        val cardiganPct = Publisher(0.0)
        val malpequePct = Publisher(0.0)
        val charlottetownPct = Publisher(0.0)
        val egmontPct = Publisher(0.0)

        val screen = RegionalBreakdownScreen.of(
            entries = votes(
                topRowHeader = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
                topRowVotes = peiVotes,
                topRowReporting = pct(peiPct),
            ) {
                section(
                    items = listOf(
                        Triple("CARDIGAN", cardiganVotes, cardiganPct),
                        Triple("MALPEQUE", malpequeVotes, malpequePct),
                        Triple("CHARLOTTETOWN", charlottetownVotes, charlottetownPct),
                        Triple("EGMONT", egmontVotes, egmontPct),
                    ),
                    header = { first.asOneTimePublisher() },
                    votes = { second },
                    reporting = { pct(third) },
                )
            },
            header = "VOTES BY REGION".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "Votes-1", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            VOTES BY REGION
            
            PRINCE EDWARD ISLAND: 0.0% IN
            
            CARDIGAN: 0.0% IN
            MALPEQUE: 0.0% IN
            CHARLOTTETOWN: 0.0% IN
            EGMONT: 0.0% IN
            """.trimIndent(),
        )

        peiVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        peiPct.submit(1.0 / 27)
        cardiganVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        cardiganPct.submit(1.0 / 7)
        compareRendering("RegionalBreakdownScreen", "Votes-2", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            VOTES BY REGION
            
            PRINCE EDWARD ISLAND: GRN 38.1%, PC 30.9%, LIB 29.8%, NDP 1.3%, 3.7% IN
            
            CARDIGAN: GRN 38.1%, PC 30.9%, LIB 29.8%, NDP 1.3%, 14.3% IN
            MALPEQUE: GRN 0.0%, PC 0.0%, LIB 0.0%, NDP 0.0%, 0.0% IN
            CHARLOTTETOWN: GRN 0.0%, PC 0.0%, LIB 0.0%, NDP 0.0%, 0.0% IN
            EGMONT: GRN 0.0%, PC 0.0%, LIB 0.0%, NDP 0.0%, 0.0% IN
            """.trimIndent(),
        )

        peiVotes.submit(mapOf(lib to 24346, pc to 30415, grn to 25302, ndp to 2454, ind to 282))
        peiPct.submit(1.0)
        cardiganVotes.submit(mapOf(lib to 5265, pc to 9714, grn to 5779, ndp to 277))
        cardiganPct.submit(1.0)
        malpequeVotes.submit(mapOf(lib to 5548, pc to 9893, grn to 7378, ndp to 244, ind to 80))
        malpequePct.submit(1.0)
        charlottetownVotes.submit(mapOf(lib to 6078, pc to 4932, grn to 6591, ndp to 674, ind to 202))
        charlottetownPct.submit(1.0)
        egmontVotes.submit(mapOf(lib to 7455, pc to 5876, grn to 5554, ndp to 1259))
        egmontPct.submit(1.0)
        compareRendering("RegionalBreakdownScreen", "Votes-3", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            VOTES BY REGION
            
            PRINCE EDWARD ISLAND: PC 36.7%, GRN 30.6%, LIB 29.4%, NDP 3.0%, IND 0.3%, 100.0% IN
            
            CARDIGAN: PC 46.2%, GRN 27.5%, LIB 25.0%, NDP 1.3%, IND 0.0%, 100.0% IN
            MALPEQUE: PC 42.7%, GRN 31.9%, LIB 24.0%, NDP 1.1%, IND 0.3%, 100.0% IN
            CHARLOTTETOWN: PC 26.7%, GRN 35.7%, LIB 32.9%, NDP 3.6%, IND 1.1%, 100.0% IN
            EGMONT: PC 29.2%, GRN 27.6%, LIB 37.0%, NDP 6.2%, IND 0.0%, 100.0% IN
            """.trimIndent(),
        )
    }

    @Test
    fun testVotesPollsReporting() {
        val peiVotes = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganVotes = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeVotes = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownVotes = Publisher<Map<Party, Int>>(emptyMap())
        val egmontVotes = Publisher<Map<Party, Int>>(emptyMap())
        val peiPct = Publisher(PollsReporting(0, 27))
        val cardiganPct = Publisher(PollsReporting(0, 7))
        val malpequePct = Publisher(PollsReporting(0, 7))
        val charlottetownPct = Publisher(PollsReporting(0, 6))
        val egmontPct = Publisher(PollsReporting(0, 7))

        val screen = RegionalBreakdownScreen.of(
            entries = votes(
                topRowHeader = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
                topRowVotes = peiVotes,
                topRowReporting = polls(peiPct),
            ) {
                section(
                    items = listOf(
                        Triple("CARDIGAN", cardiganVotes, cardiganPct),
                        Triple("MALPEQUE", malpequeVotes, malpequePct),
                        Triple("CHARLOTTETOWN", charlottetownVotes, charlottetownPct),
                        Triple("EGMONT", egmontVotes, egmontPct),
                    ),
                    header = { first.asOneTimePublisher() },
                    votes = { second },
                    reporting = { polls(third) },
                )
            },
            header = "VOTES BY REGION".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "VotesPollsReporting-1", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            VOTES BY REGION
            
            PRINCE EDWARD ISLAND: 0/27
            
            CARDIGAN: 0/7
            MALPEQUE: 0/7
            CHARLOTTETOWN: 0/6
            EGMONT: 0/7
            """.trimIndent(),
        )

        peiVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        peiPct.submit(PollsReporting(1, 27))
        cardiganVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        cardiganPct.submit(PollsReporting(1, 7))
        compareRendering("RegionalBreakdownScreen", "VotesPollsReporting-2", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            VOTES BY REGION
            
            PRINCE EDWARD ISLAND: GRN 38.1%, PC 30.9%, LIB 29.8%, NDP 1.3%, 1/27
            
            CARDIGAN: GRN 38.1%, PC 30.9%, LIB 29.8%, NDP 1.3%, 1/7
            MALPEQUE: GRN 0.0%, PC 0.0%, LIB 0.0%, NDP 0.0%, 0/7
            CHARLOTTETOWN: GRN 0.0%, PC 0.0%, LIB 0.0%, NDP 0.0%, 0/6
            EGMONT: GRN 0.0%, PC 0.0%, LIB 0.0%, NDP 0.0%, 0/7
            """.trimIndent(),
        )

        peiVotes.submit(mapOf(lib to 24346, pc to 30415, grn to 25302, ndp to 2454, ind to 282))
        peiPct.submit(PollsReporting(27, 27))
        cardiganVotes.submit(mapOf(lib to 5265, pc to 9714, grn to 5779, ndp to 277))
        cardiganPct.submit(PollsReporting(7, 7))
        malpequeVotes.submit(mapOf(lib to 5548, pc to 9893, grn to 7378, ndp to 244, ind to 80))
        malpequePct.submit(PollsReporting(7, 7))
        charlottetownVotes.submit(mapOf(lib to 6078, pc to 4932, grn to 6591, ndp to 674, ind to 202))
        charlottetownPct.submit(PollsReporting(6, 6))
        egmontVotes.submit(mapOf(lib to 7455, pc to 5876, grn to 5554, ndp to 1259))
        egmontPct.submit(PollsReporting(7, 7))
        compareRendering("RegionalBreakdownScreen", "VotesPollsReporting-3", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            VOTES BY REGION
            
            PRINCE EDWARD ISLAND: PC 36.7%, GRN 30.6%, LIB 29.4%, NDP 3.0%, IND 0.3%, 27/27
            
            CARDIGAN: PC 46.2%, GRN 27.5%, LIB 25.0%, NDP 1.3%, IND 0.0%, 7/7
            MALPEQUE: PC 42.7%, GRN 31.9%, LIB 24.0%, NDP 1.1%, IND 0.3%, 7/7
            CHARLOTTETOWN: PC 26.7%, GRN 35.7%, LIB 32.9%, NDP 3.6%, IND 1.1%, 6/6
            EGMONT: PC 29.2%, GRN 27.6%, LIB 37.0%, NDP 6.2%, IND 0.0%, 7/7
            """.trimIndent(),
        )
    }

    @Test
    fun testVotesWithPrev() {
        val peiVotes = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganVotes = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeVotes = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownVotes = Publisher<Map<Party, Int>>(emptyMap())
        val egmontVotes = Publisher<Map<Party, Int>>(emptyMap())
        val peiPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val malpequePrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val egmontPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val peiPct = Publisher(0.0)
        val cardiganPct = Publisher(0.0)
        val malpequePct = Publisher(0.0)
        val charlottetownPct = Publisher(0.0)
        val egmontPct = Publisher(0.0)

        data class Region(
            val name: String,
            val votes: Publisher<Map<Party, Int>>,
            val prev: Publisher<Map<Party, Int>>,
            val pct: Publisher<Double>,
        )
        val screen = RegionalBreakdownScreen.of(
            entries = votes(
                topRowHeader = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
                topRowVotes = peiVotes,
                topRowPrev = peiPrevVotes,
                topRowReporting = pct(peiPct),
            ) {
                section(
                    items = listOf(
                        Region("CARDIGAN", cardiganVotes, cardiganPrevVotes, cardiganPct),
                        Region("MALPEQUE", malpequeVotes, malpequePrevVotes, malpequePct),
                        Region("CHARLOTTETOWN", charlottetownVotes, charlottetownPrevVotes, charlottetownPct),
                        Region("EGMONT", egmontVotes, egmontPrevVotes, egmontPct),
                    ),
                    header = { name.asOneTimePublisher() },
                    votes = { votes },
                    prev = { prev },
                    reporting = { pct(pct) },
                )
            },
            header = "VOTES BY REGION".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "VotesWithPrev-1", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            VOTES BY REGION
            
            PRINCE EDWARD ISLAND: 0.0% IN
            
            CARDIGAN: 0.0% IN
            MALPEQUE: 0.0% IN
            CHARLOTTETOWN: 0.0% IN
            EGMONT: 0.0% IN
            """.trimIndent(),
        )

        peiVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        peiPrevVotes.submit(mapOf(lib to 1173, pc to 1173, grn to 234, ndp to 258))
        peiPct.submit(1.0 / 27)
        cardiganVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        cardiganPrevVotes.submit(mapOf(lib to 1173, pc to 1173, grn to 234, ndp to 258))
        cardiganPct.submit(1.0 / 7)
        compareRendering("RegionalBreakdownScreen", "VotesWithPrev-2", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            VOTES BY REGION
            
            PRINCE EDWARD ISLAND: GRN 38.1% (+29.8), PC 30.9% (-10.5), LIB 29.8% (-11.5), NDP 1.3% (-7.8), 3.7% IN
            
            CARDIGAN: GRN 38.1% (+29.8), PC 30.9% (-10.5), LIB 29.8% (-11.5), NDP 1.3% (-7.8), 14.3% IN
            MALPEQUE: GRN 0.0% (±0.0), PC 0.0% (±0.0), LIB 0.0% (±0.0), NDP 0.0% (±0.0), 0.0% IN
            CHARLOTTETOWN: GRN 0.0% (±0.0), PC 0.0% (±0.0), LIB 0.0% (±0.0), NDP 0.0% (±0.0), 0.0% IN
            EGMONT: GRN 0.0% (±0.0), PC 0.0% (±0.0), LIB 0.0% (±0.0), NDP 0.0% (±0.0), 0.0% IN
            """.trimIndent(),
        )

        peiVotes.submit(mapOf(lib to 24346, pc to 30415, grn to 25302, ndp to 2454, ind to 282))
        peiPrevVotes.submit(mapOf(lib to 33481, pc to 30663, grn to 8857, ndp to 8997))
        peiPct.submit(1.0)
        cardiganVotes.submit(mapOf(lib to 5265, pc to 9714, grn to 5779, ndp to 277))
        cardiganPrevVotes.submit(mapOf(lib to 8016, pc to 9444, grn to 1144, ndp to 2404))
        cardiganPct.submit(1.0)
        malpequeVotes.submit(mapOf(lib to 5548, pc to 9893, grn to 7378, ndp to 244, ind to 80))
        malpequePrevVotes.submit(mapOf(lib to 7767, pc to 8169, grn to 4011, ndp to 1427))
        malpequePct.submit(1.0)
        charlottetownVotes.submit(mapOf(lib to 6078, pc to 4932, grn to 6591, ndp to 674, ind to 202))
        charlottetownPrevVotes.submit(mapOf(lib to 8383, pc to 6405, grn to 2557, ndp to 3261))
        charlottetownPct.submit(1.0)
        egmontVotes.submit(mapOf(lib to 7455, pc to 5876, grn to 5554, ndp to 1259))
        egmontPrevVotes.submit(mapOf(lib to 9312, pc to 6646, grn to 1138, ndp to 1902))
        egmontPct.submit(1.0)
        compareRendering("RegionalBreakdownScreen", "VotesWithPrev-3", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            VOTES BY REGION
            
            PRINCE EDWARD ISLAND: PC 36.7% (-0.7), GRN 30.6% (+19.8), LIB 29.4% (-11.4), NDP 3.0% (-8.0), IND 0.3% (+0.3), 100.0% IN
            
            CARDIGAN: PC 46.2% (+1.2), GRN 27.5% (+22.0), LIB 25.0% (-13.1), NDP 1.3% (-10.1), IND 0.0% (±0.0), 100.0% IN
            MALPEQUE: PC 42.7% (+4.5), GRN 31.9% (+13.1), LIB 24.0% (-12.4), NDP 1.1% (-5.6), IND 0.3% (+0.3), 100.0% IN
            CHARLOTTETOWN: PC 26.7% (-4.4), GRN 35.7% (+23.3), LIB 32.9% (-7.8), NDP 3.6% (-12.2), IND 1.1% (+1.1), 100.0% IN
            EGMONT: PC 29.2% (-5.8), GRN 27.6% (+21.6), LIB 37.0% (-12.0), NDP 6.2% (-3.8), IND 0.0% (±0.0), 100.0% IN
            """.trimIndent(),
        )
    }

    @Test
    fun testVotesWithPrevPollsReporting() {
        val peiVotes = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganVotes = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeVotes = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownVotes = Publisher<Map<Party, Int>>(emptyMap())
        val egmontVotes = Publisher<Map<Party, Int>>(emptyMap())
        val peiPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val malpequePrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val egmontPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val peiPct = Publisher(PollsReporting(0, 27))
        val cardiganPct = Publisher(PollsReporting(0, 7))
        val malpequePct = Publisher(PollsReporting(0, 7))
        val charlottetownPct = Publisher(PollsReporting(0, 6))
        val egmontPct = Publisher(PollsReporting(0, 7))

        data class Region(
            val name: String,
            val votes: Publisher<Map<Party, Int>>,
            val prev: Publisher<Map<Party, Int>>,
            val pct: Publisher<PollsReporting>,
        )
        val screen = RegionalBreakdownScreen.of(
            entries = votes(
                topRowHeader = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
                topRowVotes = peiVotes,
                topRowPrev = peiPrevVotes,
                topRowReporting = polls(peiPct),
            ) {
                section(
                    items = listOf(
                        Region("CARDIGAN", cardiganVotes, cardiganPrevVotes, cardiganPct),
                        Region("MALPEQUE", malpequeVotes, malpequePrevVotes, malpequePct),
                        Region("CHARLOTTETOWN", charlottetownVotes, charlottetownPrevVotes, charlottetownPct),
                        Region("EGMONT", egmontVotes, egmontPrevVotes, egmontPct),
                    ),
                    header = { name.asOneTimePublisher() },
                    votes = { votes },
                    prev = { prev },
                    reporting = { polls(pct) },
                )
            },
            header = "VOTES BY REGION".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "VotesWithPrevPollsReporting-1", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            VOTES BY REGION
            
            PRINCE EDWARD ISLAND: 0/27
            
            CARDIGAN: 0/7
            MALPEQUE: 0/7
            CHARLOTTETOWN: 0/6
            EGMONT: 0/7
            """.trimIndent(),
        )

        peiVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        peiPrevVotes.submit(mapOf(lib to 1173, pc to 1173, grn to 234, ndp to 258))
        peiPct.submit(PollsReporting(1, 27))
        cardiganVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        cardiganPrevVotes.submit(mapOf(lib to 1173, pc to 1173, grn to 234, ndp to 258))
        cardiganPct.submit(PollsReporting(1, 7))
        compareRendering("RegionalBreakdownScreen", "VotesWithPrevPollsReporting-2", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            VOTES BY REGION
            
            PRINCE EDWARD ISLAND: GRN 38.1% (+29.8), PC 30.9% (-10.5), LIB 29.8% (-11.5), NDP 1.3% (-7.8), 1/27
            
            CARDIGAN: GRN 38.1% (+29.8), PC 30.9% (-10.5), LIB 29.8% (-11.5), NDP 1.3% (-7.8), 1/7
            MALPEQUE: GRN 0.0% (±0.0), PC 0.0% (±0.0), LIB 0.0% (±0.0), NDP 0.0% (±0.0), 0/7
            CHARLOTTETOWN: GRN 0.0% (±0.0), PC 0.0% (±0.0), LIB 0.0% (±0.0), NDP 0.0% (±0.0), 0/6
            EGMONT: GRN 0.0% (±0.0), PC 0.0% (±0.0), LIB 0.0% (±0.0), NDP 0.0% (±0.0), 0/7
            """.trimIndent(),
        )

        peiVotes.submit(mapOf(lib to 24346, pc to 30415, grn to 25302, ndp to 2454, ind to 282))
        peiPrevVotes.submit(mapOf(lib to 33481, pc to 30663, grn to 8857, ndp to 8997))
        peiPct.submit(PollsReporting(27, 27))
        cardiganVotes.submit(mapOf(lib to 5265, pc to 9714, grn to 5779, ndp to 277))
        cardiganPrevVotes.submit(mapOf(lib to 8016, pc to 9444, grn to 1144, ndp to 2404))
        cardiganPct.submit(PollsReporting(7, 7))
        malpequeVotes.submit(mapOf(lib to 5548, pc to 9893, grn to 7378, ndp to 244, ind to 80))
        malpequePrevVotes.submit(mapOf(lib to 7767, pc to 8169, grn to 4011, ndp to 1427))
        malpequePct.submit(PollsReporting(7, 7))
        charlottetownVotes.submit(mapOf(lib to 6078, pc to 4932, grn to 6591, ndp to 674, ind to 202))
        charlottetownPrevVotes.submit(mapOf(lib to 8383, pc to 6405, grn to 2557, ndp to 3261))
        charlottetownPct.submit(PollsReporting(6, 6))
        egmontVotes.submit(mapOf(lib to 7455, pc to 5876, grn to 5554, ndp to 1259))
        egmontPrevVotes.submit(mapOf(lib to 9312, pc to 6646, grn to 1138, ndp to 1902))
        egmontPct.submit(PollsReporting(7, 7))
        compareRendering("RegionalBreakdownScreen", "VotesWithPrevPollsReporting-3", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            VOTES BY REGION
            
            PRINCE EDWARD ISLAND: PC 36.7% (-0.7), GRN 30.6% (+19.8), LIB 29.4% (-11.4), NDP 3.0% (-8.0), IND 0.3% (+0.3), 27/27
            
            CARDIGAN: PC 46.2% (+1.2), GRN 27.5% (+22.0), LIB 25.0% (-13.1), NDP 1.3% (-10.1), IND 0.0% (±0.0), 7/7
            MALPEQUE: PC 42.7% (+4.5), GRN 31.9% (+13.1), LIB 24.0% (-12.4), NDP 1.1% (-5.6), IND 0.3% (+0.3), 7/7
            CHARLOTTETOWN: PC 26.7% (-4.4), GRN 35.7% (+23.3), LIB 32.9% (-7.8), NDP 3.6% (-12.2), IND 1.1% (+1.1), 6/6
            EGMONT: PC 29.2% (-5.8), GRN 27.6% (+21.6), LIB 37.0% (-12.0), NDP 6.2% (-3.8), IND 0.0% (±0.0), 7/7
            """.trimIndent(),
        )
    }

    @Test
    fun testVotesCoalition() {
        val lib = Party("Liberal", "LIB", Color.BLUE.darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val coa = Coalition("Coalition", "L/NP", lib, lnp, clp)
        val alp = Party("Labor", "ALP", Color.RED)
        val grn = Party("Greens", "GRN", Color.GREEN.darker())
        val onp = Party("One Nation", "ONP", Color.ORANGE.darker())
        val uap = Party("United Australia", "UAP", Color.YELLOW)
        val oth = Party.OTHERS

        data class State(
            val name: String,
            val abbreviatedName: String,
            val votes: Map<PartyOrCoalition, Int>,
            val reporting: Double,
            val coalitionParty: Party?,
        )
        val screen = RegionalBreakdownScreen.of(
            entries = votes(
                topRowHeader = "AUSTRALIA".asOneTimePublisher(),
                topRowVotes = mapOf(
                    alp to 4776030,
                    coa to 5233334,
                    grn to 1795985,
                    onp to 727464,
                    uap to 604536,
                    oth to 1521693,
                ).asOneTimePublisher(),
                topRowReporting = pct(0.8982.asOneTimePublisher()),
            ) {
                section(
                    items = listOf(
                        State("New South Wales", "NSW", mapOf(coa to 1699323, alp to 1552684, grn to 466069, onp to 224965, uap to 183174, oth to 524725), 0.9070, null),
                        State("Victoria", "VIC", mapOf(coa to 1239280, alp to 1230842, grn to 514893, onp to 143558, uap to 177745, oth to 440115), 0.9059, null),
                        State("Queensland", "QLD", mapOf(lnp to 1172515, alp to 811069, grn to 382900, onp to 221640, uap to 149255, oth to 220647), 0.8816, lnp),
                        State("Western Australia", "WA", mapOf(lib to 512414, alp to 542667, grn to 184094, onp to 58226, uap to 33863, oth to 141961), 0.8799, lib),
                        State("South Australia", "SA", mapOf(lib to 390195, alp to 378329, grn to 140227, onp to 53057, uap to 42688, oth to 93290), 0.9107, lib),
                        State("Tasmania", "TAS", mapOf(lib to 115184, alp to 95322, grn to 41972, onp to 13970, uap to 6437, oth to 76813), 0.9243, lib),
                        State("Australian Capital Territory", "ACT", mapOf(lib to 74759, alp to 126595, grn to 52648, onp to 6630, uap to 6864, oth to 14501), 0.9207, lib),
                        State("Northern Territory", "NT", mapOf(clp to 29664, alp to 38522, grn to 13182, onp to 5418, uap to 4510, oth to 9641), 0.7308, clp),
                    ),
                    header = { name.uppercase().asOneTimePublisher() },
                    abbreviatedHeader = { abbreviatedName.asOneTimePublisher() },
                    votes = { votes.asOneTimePublisher() },
                    reporting = { pct(reporting.asOneTimePublisher()) },
                    coalitionMap = { if (coalitionParty == null) null else mapOf(coa to coalitionParty).asOneTimePublisher() },
                )
            },
            header = "PRIMARY VOTE BY STATE".asOneTimePublisher(),
            title = "AUSTRALIA".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "Votes-C", screen)
        assertPublishes(
            screen.altText,
            """
            AUSTRALIA
            PRIMARY VOTE BY STATE
            
            AUSTRALIA: L/NP 35.7%, ALP 32.6%, GRN 12.3%, ONP 5.0%, UAP 4.1%, OTH 10.4%, 89.8% IN
          
            NSW: L/NP 36.5%, ALP 33.4%, GRN 10.0%, ONP 4.8%, UAP 3.9%, OTH 11.3%, 90.7% IN
            VIC: L/NP 33.1%, ALP 32.9%, GRN 13.7%, ONP 3.8%, UAP 4.7%, OTH 11.7%, 90.6% IN
            QLD: LNP 39.6%, ALP 27.4%, GRN 12.9%, ONP 7.5%, UAP 5.0%, OTH 7.5%, 88.2% IN
            WA: LIB 34.8%, ALP 36.8%, GRN 12.5%, ONP 4.0%, UAP 2.3%, OTH 9.6%, 88.0% IN
            SA: LIB 35.5%, ALP 34.5%, GRN 12.8%, ONP 4.8%, UAP 3.9%, OTH 8.5%, 91.1% IN
            TAS: LIB 32.9%, ALP 27.3%, GRN 12.0%, ONP 4.0%, UAP 1.8%, OTH 22.0%, 92.4% IN
            ACT: LIB 26.5%, ALP 44.9%, GRN 18.7%, ONP 2.4%, UAP 2.4%, OTH 5.1%, 92.1% IN
            NT: CLP 29.4%, ALP 38.2%, GRN 13.1%, ONP 5.4%, UAP 4.5%, OTH 9.6%, 73.1% IN
            """.trimIndent(),
        )
    }

    @Test
    fun testVotesWithPrevCoalition() {
        val lib = Party("Liberal", "LIB", Color.BLUE.darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val coa = Coalition("Coalition", "L/NP", lib, lnp, clp)
        val alp = Party("Labor", "ALP", Color.RED)
        val grn = Party("Greens", "GRN", Color.GREEN.darker())
        val onp = Party("One Nation", "ONP", Color.ORANGE.darker())
        val uap = Party("United Australia", "UAP", Color.YELLOW)
        val oth = Party.OTHERS

        data class State(
            val name: String,
            val abbreviatedName: String,
            val votes: Map<PartyOrCoalition, Int>,
            val prev: Map<PartyOrCoalition, Int>,
            val reporting: Double,
            val coalitionParty: Party?,
        )
        val screen = RegionalBreakdownScreen.of(
            entries = votes(
                topRowHeader = "AUSTRALIA".asOneTimePublisher(),
                topRowVotes = mapOf(
                    alp to 4776030,
                    coa to 5233334,
                    grn to 1795985,
                    onp to 727464,
                    uap to 604536,
                    oth to 1521693,
                ).asOneTimePublisher(),
                topRowPrev = mapOf(
                    alp to 4752110,
                    coa to 5906884,
                    grn to 1482923,
                    onp to 438587,
                    uap to 488817,
                    oth to 1184031,
                ).asOneTimePublisher(),
                topRowReporting = pct(0.8982.asOneTimePublisher()),
            ) {
                section(
                    items = listOf(
                        State(
                            "New South Wales",
                            "NSW",
                            mapOf(coa to 1699323, alp to 1552684, grn to 466069, onp to 224965, uap to 183174, oth to 524725),
                            mapOf(coa to 1930426, alp to 1568173, grn to 395238, onp to 59464, uap to 153477, oth to 430508),
                            0.9070,
                            null,
                        ),
                        State(
                            "Victoria",
                            "VIC",
                            mapOf(coa to 1239280, alp to 1230842, grn to 514893, onp to 143558, uap to 177745, oth to 440115),
                            mapOf(coa to 1425542, alp to 1361913, grn to 439169, onp to 35177, uap to 134581, oth to 298650),
                            0.9059,
                            null,
                        ),
                        State(
                            "Queensland",
                            "QLD",
                            mapOf(lnp to 1172515, alp to 811069, grn to 382900, onp to 221640, uap to 149255, oth to 220647),
                            mapOf(lnp to 1236401, alp to 754792, grn to 292059, onp to 250779, uap to 99329, oth to 195658),
                            0.8816,
                            lnp,
                        ),
                        State(
                            "Western Australia",
                            "WA",
                            mapOf(lib to 512414, alp to 542667, grn to 184094, onp to 58226, uap to 33863, oth to 141961),
                            mapOf(lib to 633930, alp to 417727, grn to 162876, onp to 74478, uap to 28488, oth to 84375),
                            0.8799,
                            lib,
                        ),
                        State(
                            "South Australia",
                            "SA",
                            mapOf(lib to 390195, alp to 378329, grn to 140227, onp to 53057, uap to 42688, oth to 93290),
                            mapOf(lib to 438022, alp to 379495, grn to 103036, onp to 8990, uap to 46007, oth to 97101),
                            0.9107,
                            lib,
                        ),
                        State(
                            "Tasmania",
                            "TAS",
                            mapOf(lib to 115184, alp to 95322, grn to 41972, onp to 13970, uap to 6437, oth to 76813),
                            mapOf(lib to 120415, alp to 116955, grn to 35229, onp to 9699, uap to 16868, oth to 48826),
                            0.9243,
                            lib,
                        ),
                        State(
                            "Australian Capital Territory",
                            "ACT",
                            mapOf(lib to 74759, alp to 126595, grn to 52648, onp to 6630, uap to 6864, oth to 14501),
                            mapOf(lib to 83311, alp to 109300, grn to 44804, uap to 7117, oth to 21443),
                            0.9207,
                            lib,
                        ),
                        State(
                            "Northern Territory",
                            "NT",
                            mapOf(clp to 29664, alp to 38522, grn to 13182, onp to 5418, uap to 4510, oth to 9641),
                            mapOf(clp to 38837, alp to 43755, grn to 10512, uap to 2950, oth to 7464),
                            0.7308,
                            clp,
                        ),
                    ),
                    header = { name.uppercase().asOneTimePublisher() },
                    abbreviatedHeader = { abbreviatedName.asOneTimePublisher() },
                    votes = { votes.asOneTimePublisher() },
                    prev = { prev.asOneTimePublisher() },
                    reporting = { pct(reporting.asOneTimePublisher()) },
                    coalitionMap = { if (coalitionParty == null) null else mapOf(coa to coalitionParty).asOneTimePublisher() },
                )
            },
            header = "PRIMARY VOTE BY STATE".asOneTimePublisher(),
            title = "AUSTRALIA".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "VotesWithPrev-C", screen)
        assertPublishes(
            screen.altText,
            """
            AUSTRALIA
            PRIMARY VOTE BY STATE
            
            AUSTRALIA: L/NP 35.7% (-5.7), ALP 32.6% (-0.8), GRN 12.3% (+1.8), ONP 5.0% (+1.9), UAP 4.1% (+0.7), OTH 10.4% (+2.1), 89.8% IN
          
            NSW: L/NP 36.5% (-6.0), ALP 33.4% (-1.2), GRN 10.0% (+1.3), ONP 4.8% (+3.5), UAP 3.9% (+0.6), OTH 11.3% (+1.8), 90.7% IN
            VIC: L/NP 33.1% (-5.5), ALP 32.9% (-4.0), GRN 13.7% (+1.9), ONP 3.8% (+2.9), UAP 4.7% (+1.1), OTH 11.7% (+3.7), 90.6% IN
            QLD: LNP 39.6% (-4.1), ALP 27.4% (+0.7), GRN 12.9% (+2.6), ONP 7.5% (-1.4), UAP 5.0% (+1.5), OTH 7.5% (+0.5), 88.2% IN
            WA: LIB 34.8% (-10.4), ALP 36.8% (+7.0), GRN 12.5% (+0.9), ONP 4.0% (-1.4), UAP 2.3% (+0.3), OTH 9.6% (+3.6), 88.0% IN
            SA: LIB 35.5% (-5.3), ALP 34.5% (-0.9), GRN 12.8% (+3.2), ONP 4.8% (+4.0), UAP 3.9% (-0.4), OTH 8.5% (-0.6), 91.1% IN
            TAS: LIB 32.9% (-1.7), ALP 27.3% (-6.4), GRN 12.0% (+1.9), ONP 4.0% (+1.2), UAP 1.8% (-3.0), OTH 22.0% (+7.9), 92.4% IN
            ACT: LIB 26.5% (-4.8), ALP 44.9% (+3.8), GRN 18.7% (+1.8), ONP 2.4% (+2.4), UAP 2.4% (-0.2), OTH 5.1% (-2.9), 92.1% IN
            NT: CLP 29.4% (-8.1), ALP 38.2% (-4.1), GRN 13.1% (+2.9), ONP 5.4% (+5.4), UAP 4.5% (+1.6), OTH 9.6% (+2.3), 73.1% IN
            """.trimIndent(),
        )
    }

    @Test
    fun testVotesWithLimitedColumns() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val bq = Party("Bloc Quebecois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ppc = Party("Peoples", "PPC", Color.MAGENTA.darker())
        val oth = Party.OTHERS

        val federalVotes = Triple(
            Publisher(emptyMap<Party, Int>()),
            Publisher(emptyMap<Party, Int>()),
            Publisher(0.0),
        )
        val provincialVotes = listOf(
            "Newfoundland and Labrador",
            "Nova Scotia",
            "Prince Edward Island",
            "New Brunswick",
            "Quebec",
            "Ontario",
            "Manitoba",
            "Saskatchewan",
            "Alberta",
            "British Columbia",
            "Northern Canada",
        ).associateWith {
            Triple(
                Publisher(emptyMap<Party, Int>()),
                Publisher(emptyMap<Party, Int>()),
                Publisher(0.0),
            )
        }

        val screen = RegionalBreakdownScreen.of(
            entries = votes(
                topRowHeader = "CANADA".asOneTimePublisher(),
                topRowVotes = federalVotes.first,
                topRowPrev = federalVotes.second,
                topRowReporting = pct(federalVotes.third),
                maxColumns = 4.asOneTimePublisher(),
            ) {
                section(
                    items = provincialVotes.entries,
                    header = { key.uppercase().asOneTimePublisher() },
                    votes = { value.first },
                    prev = { value.second },
                    reporting = { pct(value.third) },
                )
            },
            header = "POPULAR VOTE BY PROVINCE".asOneTimePublisher(),
            title = "CANADA".asOneTimePublisher(),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("RegionalBreakdownScreen", "VotesWithLimitedColumns-1", screen)
        assertPublishes(
            screen.altText,
            """
            CANADA
            POPULAR VOTE BY PROVINCE
            
            CANADA: 0.0% IN
            
            NEWFOUNDLAND AND LABRADOR: 0.0% IN
            NOVA SCOTIA: 0.0% IN
            PRINCE EDWARD ISLAND: 0.0% IN
            NEW BRUNSWICK: 0.0% IN
            QUEBEC: 0.0% IN
            ONTARIO: 0.0% IN
            MANITOBA: 0.0% IN
            SASKATCHEWAN: 0.0% IN
            ALBERTA: 0.0% IN
            BRITISH COLUMBIA: 0.0% IN
            NORTHERN CANADA: 0.0% IN
            """.trimIndent(),
        )

        federalVotes.also {
            it.first.submit(mapOf(lib to 331, con to 343, ndp to 160, bq to 76, grn to 65, ppc to 16, oth to 2))
            it.second.submit(mapOf(lib to 395, con to 319, ndp to 197, bq to 47, grn to 34, oth to 2))
            it.third.submit(1.0)
        }
        provincialVotes["Newfoundland and Labrador"]!!.also {
            it.first.submit(mapOf(lib to 449, con to 279, ndp to 237, grn to 31, ppc to 1, oth to 2))
            it.second.submit(mapOf(lib to 645, con to 103, ndp to 210, grn to 11, oth to 29))
            it.third.submit(1.0)
        }
        provincialVotes["Nova Scotia"]!!.also {
            it.first.submit(mapOf(lib to 414, con to 257, ndp to 189, grn to 110, ppc to 12, oth to 18))
            it.second.submit(mapOf(lib to 619, con to 179, ndp to 164, grn to 34, oth to 3))
            it.third.submit(1.0)
        }
        provincialVotes["Prince Edward Island"]!!.also {
            it.first.submit(mapOf(lib to 437, con to 273, ndp to 76, grn to 209, oth to 5))
            it.second.submit(mapOf(lib to 583, con to 193, ndp to 160, grn to 60))
            it.third.submit(1.0)
        }
        provincialVotes["New Brunswick"]!!.also {
            it.first.submit(mapOf(lib to 375, con to 328, ndp to 94, grn to 172, ppc to 20, oth to 11))
            it.second.submit(mapOf(lib to 516, con to 253, ndp to 183, grn to 46, oth to 1))
            it.third.submit(1.0)
        }
        provincialVotes["Quebec"]!!.also {
            it.first.submit(mapOf(lib to 343, con to 160, ndp to 108, bq to 324, grn to 45, ppc to 15, oth to 1))
            it.second.submit(mapOf(lib to 357, con to 167, ndp to 254, bq to 193, grn to 23, oth to 1))
            it.third.submit(1.0)
        }
        provincialVotes["Ontario"]!!.also {
            it.first.submit(mapOf(lib to 416, con to 331, ndp to 168, grn to 62, ppc to 16, oth to 2))
            it.second.submit(mapOf(lib to 448, con to 350, ndp to 166, grn to 29, oth to 2))
            it.third.submit(1.0)
        }
        provincialVotes["Manitoba"]!!.also {
            it.first.submit(mapOf(lib to 265, con to 452, ndp to 208, grn to 51, ppc to 17, oth to 6))
            it.second.submit(mapOf(lib to 446, con to 373, ndp to 138, grn to 32, oth to 6))
            it.third.submit(1.0)
        }
        provincialVotes["Saskatchewan"]!!.also {
            it.first.submit(mapOf(lib to 117, con to 640, ndp to 196, grn to 26, ppc to 18, oth to 2))
            it.second.submit(mapOf(lib to 239, con to 485, ndp to 251, grn to 21, oth to 2))
            it.third.submit(1.0)
        }
        provincialVotes["Alberta"]!!.also {
            it.first.submit(mapOf(lib to 138, con to 690, ndp to 116, grn to 28, ppc to 22, oth to 5))
            it.second.submit(mapOf(lib to 246, con to 595, ndp to 116, grn to 25, oth to 8))
            it.third.submit(1.0)
        }
        provincialVotes["British Columbia"]!!.also {
            it.first.submit(mapOf(lib to 262, con to 340, ndp to 244, grn to 125, ppc to 17, oth to 13))
            it.second.submit(mapOf(lib to 352, con to 300, ndp to 259, grn to 82, oth to 1))
            it.third.submit(1.0)
        }
        provincialVotes["Northern Canada"]!!.also {
            it.first.submit(mapOf(lib to 347, con to 281, ndp to 284, grn to 78, ppc to 11))
            it.second.submit(mapOf(lib to 497, con to 223, ndp to 256, grn to 24))
            it.third.submit(1.0)
        }
        compareRendering("RegionalBreakdownScreen", "VotesWithLimitedColumns-2", screen)
        assertPublishes(
            screen.altText,
            """
            CANADA
            POPULAR VOTE BY PROVINCE
            
            CANADA: CON 34.5% (+2.4), LIB 33.3% (-6.4), NDP 16.1% (-3.7), OTH 16.0% (+7.7), 100.0% IN
            
            NEWFOUNDLAND AND LABRADOR: CON 27.9% (+17.6), LIB 44.9% (-19.7), NDP 23.7% (+2.7), OTH 3.4% (-0.6), 100.0% IN
            NOVA SCOTIA: CON 25.7% (+7.8), LIB 41.4% (-20.6), NDP 18.9% (+2.5), OTH 14.0% (+10.3), 100.0% IN
            PRINCE EDWARD ISLAND: CON 27.3% (+7.9), LIB 43.7% (-14.8), NDP 7.6% (-8.5), OTH 21.4% (+15.4), 100.0% IN
            NEW BRUNSWICK: CON 32.8% (+7.5), LIB 37.5% (-14.2), NDP 9.4% (-8.9), OTH 20.3% (+15.6), 100.0% IN
            QUEBEC: CON 16.1% (-0.7), LIB 34.4% (-1.4), NDP 10.8% (-14.7), OTH 38.7% (+16.8), 100.0% IN
            ONTARIO: CON 33.3% (-1.9), LIB 41.8% (-3.2), NDP 16.9% (+0.2), OTH 8.0% (+4.9), 100.0% IN
            MANITOBA: CON 45.2% (+7.8), LIB 26.5% (-18.3), NDP 20.8% (+7.0), OTH 7.4% (+3.6), 100.0% IN
            SASKATCHEWAN: CON 64.1% (+15.5), LIB 11.7% (-12.2), NDP 19.6% (-5.5), OTH 4.6% (+2.3), 100.0% IN
            ALBERTA: CON 69.1% (+9.0), LIB 13.8% (-11.0), NDP 11.6% (-0.1), OTH 5.5% (+2.2), 100.0% IN
            BRITISH COLUMBIA: CON 34.0% (+3.8), LIB 26.2% (-9.2), NDP 24.4% (-1.7), OTH 15.5% (+7.1), 100.0% IN
            NORTHERN CANADA: CON 28.1% (+5.8), LIB 34.7% (-15.0), NDP 28.4% (+2.8), OTH 8.9% (+6.5), 100.0% IN
            """.trimIndent(),
        )
    }

    @Test
    fun testVotesNoProgress() {
        val peiVotes = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganVotes = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeVotes = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownVotes = Publisher<Map<Party, Int>>(emptyMap())
        val egmontVotes = Publisher<Map<Party, Int>>(emptyMap())

        val screen = RegionalBreakdownScreen.of(
            entries = votes(
                topRowHeader = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
                topRowVotes = peiVotes,
            ) {
                section(
                    items = listOf(
                        Pair("CARDIGAN", cardiganVotes),
                        Pair("MALPEQUE", malpequeVotes),
                        Pair("CHARLOTTETOWN", charlottetownVotes),
                        Pair("EGMONT", egmontVotes),
                    ),
                    header = { first.asOneTimePublisher() },
                    votes = { second },
                )
            },
            header = "VOTES BY REGION".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)

        peiVotes.submit(mapOf(lib to 24346, pc to 30415, grn to 25302, ndp to 2454, ind to 282))
        cardiganVotes.submit(mapOf(lib to 5265, pc to 9714, grn to 5779, ndp to 277))
        malpequeVotes.submit(mapOf(lib to 5548, pc to 9893, grn to 7378, ndp to 244, ind to 80))
        charlottetownVotes.submit(mapOf(lib to 6078, pc to 4932, grn to 6591, ndp to 674, ind to 202))
        egmontVotes.submit(mapOf(lib to 7455, pc to 5876, grn to 5554, ndp to 1259))
        compareRendering("RegionalBreakdownScreen", "Votes-NoProgress", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            VOTES BY REGION
            
            PRINCE EDWARD ISLAND: PC 36.7%, GRN 30.6%, LIB 29.4%, NDP 3.0%, IND 0.3%
            
            CARDIGAN: PC 46.2%, GRN 27.5%, LIB 25.0%, NDP 1.3%, IND 0.0%
            MALPEQUE: PC 42.7%, GRN 31.9%, LIB 24.0%, NDP 1.1%, IND 0.3%
            CHARLOTTETOWN: PC 26.7%, GRN 35.7%, LIB 32.9%, NDP 3.6%, IND 1.1%
            EGMONT: PC 29.2%, GRN 27.6%, LIB 37.0%, NDP 6.2%, IND 0.0%
            """.trimIndent(),
        )
    }

    companion object {
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
        private val ind = Party("Independent", "IND", Color.GRAY)
    }
}
