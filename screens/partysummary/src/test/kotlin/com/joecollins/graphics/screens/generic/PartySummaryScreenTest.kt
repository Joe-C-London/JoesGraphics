package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Coalition
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color

class PartySummaryScreenTest {
    private val lib = Party("Liberal", "LIB", Color.RED)
    private val con = Party("Conservative", "CON", Color.BLUE)
    private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
    private val bq = Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker())
    private val grn = Party("Green", "GRN", Color.GREEN.darker())
    private val oth = Party.OTHERS

    @Test
    fun testBasicPartySummaryWithDiff() {
        val canada = Region("Canada")
        val bc = Region("British Columbia")
        val prairies = Region("Prairies")
        val ontario = Region("Ontario")
        val quebec = Region("Qu\u00e9bec")
        val atlantic = Region("Atlantic")
        val north = Region("North")
        val partySelected = Publisher(lib)
        val screen = PartySummaryScreen.of(
            mainRegion = canada,
            header = { name.uppercase().asOneTimePublisher() },
            seats = {
                curr = { seatsPublisher }
                diff = { seatDiffPublisher }
            },
            votes = {
                currPct = { votePublisher }
                diffPct = { voteDiffPublisher }
            },
            numRows = 3,
            regions = listOf(bc, prairies, ontario, quebec, atlantic, north),
            party = partySelected,
        )
        screen.setSize(1024, 512)
        compareRendering("PartySummaryScreen", "SingleParty-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            
            BRITISH COLUMBIA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            PRAIRIES: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            ONTARIO: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            QUÉBEC: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            ATLANTIC: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            NORTH: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            """.trimIndent(),
        )

        atlantic.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        atlantic.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        atlantic.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        atlantic.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        canada.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        canada.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        canada.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        compareRendering("PartySummaryScreen", "SingleParty-2", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: SEATS: 26 (-6); POPULAR VOTE: 40.9% (-17.8%)
            
            BRITISH COLUMBIA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            PRAIRIES: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            ONTARIO: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            QUÉBEC: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            ATLANTIC: SEATS: 26 (-6); POPULAR VOTE: 40.9% (-17.8%)
            NORTH: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            """.trimIndent(),
        )

        quebec.seats = mapOf(lib to 35, con to 10, ndp to 1, bq to 32)
        quebec.seatDiff = mapOf(lib to -5, con to -2, ndp to -15, bq to +22)
        quebec.votePct = mapOf(lib to 0.3428, con to 0.1598, ndp to 0.1084, grn to 0.0451, bq to 0.3237, oth to 0.0201)
        quebec.votePctDiff = mapOf(lib to -0.0146, con to -0.0074, ndp to -0.1451, grn to +0.0227, bq to +0.1301, oth to +0.0143)
        ontario.seats = mapOf(lib to 79, con to 36, ndp to 6)
        ontario.seatDiff = mapOf(lib to -1, con to +3, ndp to -2)
        ontario.votePct = mapOf(lib to 0.4155, con to 0.3305, ndp to 0.1681, grn to 0.0623, oth to 0.0235)
        ontario.votePctDiff = mapOf(lib to -0.0323, con to -0.0201, ndp to +0.0021, grn to +0.0339, oth to +0.0164)
        prairies.seats = mapOf(lib to 4, con to 54, ndp to 4)
        prairies.seatDiff = mapOf(lib to -8, con to +10, ndp to -2)
        prairies.votePct = mapOf(lib to 0.1574, con to 0.6381, ndp to 0.1470, grn to 0.0321, oth to 0.0253)
        prairies.votePctDiff = mapOf(lib to -0.1258, con to +0.1054, ndp to +0.0026, grn to +0.0064, oth to +0.0115)
        north.seats = mapOf(lib to 1, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3645, con to 0.2574, ndp to 0.2914, grn to 0.0752, oth to 0.0115)
        north.votePctDiff = mapOf(lib to -0.1141, con to +0.0491, ndp to 0.0016, grn to +0.0520, oth to +0.0115)
        canada.seats = mapOf(lib to 145, con to 104, ndp to 13, grn to 1, bq to 32)
        canada.seatDiff = mapOf(lib to -21, con to +15, ndp to -17, grn to +1, bq to +22)
        canada.votePct = mapOf(lib to 0.3418, con to 0.3439, ndp to 0.1469, grn to 0.0564, bq to 0.0880, oth to 0.0230)
        canada.votePctDiff = mapOf(lib to -0.0595, con to 0.0216, ndp to -0.0405, grn to 0.0296, bq to +0.0340, oth to +0.0148)
        compareRendering("PartySummaryScreen", "SingleParty-3", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: SEATS: 145 (-21); POPULAR VOTE: 34.2% (-5.9%)
            
            BRITISH COLUMBIA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            PRAIRIES: SEATS: 4 (-8); POPULAR VOTE: 15.7% (-12.6%)
            ONTARIO: SEATS: 79 (-1); POPULAR VOTE: 41.5% (-3.2%)
            QUÉBEC: SEATS: 35 (-5); POPULAR VOTE: 34.3% (-1.5%)
            ATLANTIC: SEATS: 26 (-6); POPULAR VOTE: 40.9% (-17.8%)
            NORTH: SEATS: 1 (-1); POPULAR VOTE: 36.4% (-11.4%)
            """.trimIndent(),
        )

        partySelected.submit(con)
        compareRendering("PartySummaryScreen", "SingleParty-4", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            CONSERVATIVE SUMMARY
            
            CANADA: SEATS: 104 (+15); POPULAR VOTE: 34.4% (+2.2%)
            
            BRITISH COLUMBIA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            PRAIRIES: SEATS: 54 (+10); POPULAR VOTE: 63.8% (+10.5%)
            ONTARIO: SEATS: 36 (+3); POPULAR VOTE: 33.1% (-2.0%)
            QUÉBEC: SEATS: 10 (-2); POPULAR VOTE: 16.0% (-0.7%)
            ATLANTIC: SEATS: 4 (+4); POPULAR VOTE: 28.6% (+9.6%)
            NORTH: SEATS: 0 (±0); POPULAR VOTE: 25.7% (+4.9%)
            """.trimIndent(),
        )

        north.seats = mapOf(lib to 2, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3511, con to 0.2889, ndp to 0.2591, grn to 0.0885, oth to 0.0124)
        north.votePctDiff = mapOf(lib to -0.1505, con to +0.0669, ndp to +0.0072, grn to +0.0640, oth to +0.0124)
        bc.seats = mapOf(lib to 11, con to 17, ndp to 11, grn to 2, oth to 1)
        bc.seatDiff = mapOf(lib to -6, con to +7, ndp to -3, grn to +1, oth to +1)
        bc.votePct = mapOf(lib to 0.2616, con to 0.3398, ndp to 0.2444, grn to 0.1248, oth to 0.0294)
        bc.votePctDiff = mapOf(lib to -0.0893, con to +0.0404, ndp to -0.0158, grn to +0.0424, oth to +0.0223)
        canada.seats = mapOf(lib to 157, con to 121, ndp to 24, grn to 3, bq to 32, oth to 1)
        canada.seatDiff = mapOf(lib to -27, con to +22, ndp to -20, grn to +2, bq to +22, oth to +1)
        canada.votePct = mapOf(lib to 0.3312, con to 0.3434, ndp to 0.1598, grn to 0.0655, bq to 0.0763, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.0634, con to +0.0243, ndp to -0.0374, grn to +0.0312, bq to +0.0297, oth to +0.0158)
        compareRendering("PartySummaryScreen", "SingleParty-5", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            CONSERVATIVE SUMMARY
            
            CANADA: SEATS: 121 (+22); POPULAR VOTE: 34.3% (+2.4%)
            
            BRITISH COLUMBIA: SEATS: 17 (+7); POPULAR VOTE: 34.0% (+4.0%)
            PRAIRIES: SEATS: 54 (+10); POPULAR VOTE: 63.8% (+10.5%)
            ONTARIO: SEATS: 36 (+3); POPULAR VOTE: 33.1% (-2.0%)
            QUÉBEC: SEATS: 10 (-2); POPULAR VOTE: 16.0% (-0.7%)
            ATLANTIC: SEATS: 4 (+4); POPULAR VOTE: 28.6% (+9.6%)
            NORTH: SEATS: 0 (±0); POPULAR VOTE: 28.9% (+6.7%)
            """.trimIndent(),
        )
    }

    @Test
    fun testBasicPartySummaryWithPrev() {
        val canada = Region("Canada")
        val bc = Region("British Columbia")
        val prairies = Region("Prairies")
        val ontario = Region("Ontario")
        val quebec = Region("Qu\u00e9bec")
        val atlantic = Region("Atlantic")
        val north = Region("North")
        val partySelected = Publisher(lib)
        val screen = PartySummaryScreen.of(
            mainRegion = canada,
            header = { name.uppercase().asOneTimePublisher() },
            seats = {
                curr = { seatsPublisher }
                prev = { prevSeatPublisher }
            },
            votes = {
                currPct = { votePublisher }
                prevPct = { prevVotePublisher }
            },
            numRows = 3,
            regions = listOf(bc, prairies, ontario, quebec, atlantic, north),
            party = partySelected,
        )
        screen.setSize(1024, 512)
        compareRendering("PartySummaryScreen", "SingleParty-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            
            BRITISH COLUMBIA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            PRAIRIES: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            ONTARIO: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            QUÉBEC: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            ATLANTIC: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            NORTH: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            """.trimIndent(),
        )

        atlantic.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        atlantic.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        atlantic.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        atlantic.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        canada.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        canada.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        canada.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        compareRendering("PartySummaryScreen", "SingleParty-2", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: SEATS: 26 (-6); POPULAR VOTE: 40.9% (-17.8%)
            
            BRITISH COLUMBIA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            PRAIRIES: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            ONTARIO: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            QUÉBEC: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            ATLANTIC: SEATS: 26 (-6); POPULAR VOTE: 40.9% (-17.8%)
            NORTH: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            """.trimIndent(),
        )

        quebec.seats = mapOf(lib to 35, con to 10, ndp to 1, bq to 32)
        quebec.seatDiff = mapOf(lib to -5, con to -2, ndp to -15, bq to +22)
        quebec.votePct = mapOf(lib to 0.3428, con to 0.1598, ndp to 0.1084, grn to 0.0451, bq to 0.3237, oth to 0.0201)
        quebec.votePctDiff = mapOf(lib to -0.0146, con to -0.0074, ndp to -0.1451, grn to +0.0227, bq to +0.1301, oth to +0.0143)
        ontario.seats = mapOf(lib to 79, con to 36, ndp to 6)
        ontario.seatDiff = mapOf(lib to -1, con to +3, ndp to -2)
        ontario.votePct = mapOf(lib to 0.4155, con to 0.3305, ndp to 0.1681, grn to 0.0623, oth to 0.0235)
        ontario.votePctDiff = mapOf(lib to -0.0323, con to -0.0201, ndp to +0.0021, grn to +0.0339, oth to +0.0164)
        prairies.seats = mapOf(lib to 4, con to 54, ndp to 4)
        prairies.seatDiff = mapOf(lib to -8, con to +10, ndp to -2)
        prairies.votePct = mapOf(lib to 0.1574, con to 0.6381, ndp to 0.1470, grn to 0.0321, oth to 0.0253)
        prairies.votePctDiff = mapOf(lib to -0.1258, con to +0.1054, ndp to +0.0026, grn to +0.0064, oth to +0.0115)
        north.seats = mapOf(lib to 1, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3645, con to 0.2574, ndp to 0.2914, grn to 0.0752, oth to 0.0115)
        north.votePctDiff = mapOf(lib to -0.1141, con to +0.0491, ndp to 0.0016, grn to +0.0520, oth to +0.0115)
        canada.seats = mapOf(lib to 145, con to 104, ndp to 13, grn to 1, bq to 32)
        canada.seatDiff = mapOf(lib to -21, con to +15, ndp to -17, grn to +1, bq to +22)
        canada.votePct = mapOf(lib to 0.3418, con to 0.3439, ndp to 0.1469, grn to 0.0564, bq to 0.0880, oth to 0.0230)
        canada.votePctDiff = mapOf(lib to -0.0595, con to 0.0216, ndp to -0.0405, grn to 0.0296, bq to +0.0340, oth to +0.0148)
        compareRendering("PartySummaryScreen", "SingleParty-3", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: SEATS: 145 (-21); POPULAR VOTE: 34.2% (-5.9%)
            
            BRITISH COLUMBIA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            PRAIRIES: SEATS: 4 (-8); POPULAR VOTE: 15.7% (-12.6%)
            ONTARIO: SEATS: 79 (-1); POPULAR VOTE: 41.5% (-3.2%)
            QUÉBEC: SEATS: 35 (-5); POPULAR VOTE: 34.3% (-1.5%)
            ATLANTIC: SEATS: 26 (-6); POPULAR VOTE: 40.9% (-17.8%)
            NORTH: SEATS: 1 (-1); POPULAR VOTE: 36.4% (-11.4%)
            """.trimIndent(),
        )

        partySelected.submit(con)
        compareRendering("PartySummaryScreen", "SingleParty-4", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            CONSERVATIVE SUMMARY
            
            CANADA: SEATS: 104 (+15); POPULAR VOTE: 34.4% (+2.2%)
            
            BRITISH COLUMBIA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            PRAIRIES: SEATS: 54 (+10); POPULAR VOTE: 63.8% (+10.5%)
            ONTARIO: SEATS: 36 (+3); POPULAR VOTE: 33.1% (-2.0%)
            QUÉBEC: SEATS: 10 (-2); POPULAR VOTE: 16.0% (-0.7%)
            ATLANTIC: SEATS: 4 (+4); POPULAR VOTE: 28.6% (+9.6%)
            NORTH: SEATS: 0 (±0); POPULAR VOTE: 25.7% (+4.9%)
            """.trimIndent(),
        )

        north.seats = mapOf(lib to 2, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3511, con to 0.2889, ndp to 0.2591, grn to 0.0885, oth to 0.0124)
        north.votePctDiff = mapOf(lib to -0.1505, con to +0.0669, ndp to +0.0072, grn to +0.0640, oth to +0.0124)
        bc.seats = mapOf(lib to 11, con to 17, ndp to 11, grn to 2, oth to 1)
        bc.seatDiff = mapOf(lib to -6, con to +7, ndp to -3, grn to +1, oth to +1)
        bc.votePct = mapOf(lib to 0.2616, con to 0.3398, ndp to 0.2444, grn to 0.1248, oth to 0.0294)
        bc.votePctDiff = mapOf(lib to -0.0893, con to +0.0404, ndp to -0.0158, grn to +0.0424, oth to +0.0223)
        canada.seats = mapOf(lib to 157, con to 121, ndp to 24, grn to 3, bq to 32, oth to 1)
        canada.seatDiff = mapOf(lib to -27, con to +22, ndp to -20, grn to +2, bq to +22, oth to +1)
        canada.votePct = mapOf(lib to 0.3312, con to 0.3434, ndp to 0.1598, grn to 0.0655, bq to 0.0763, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.0634, con to +0.0243, ndp to -0.0374, grn to +0.0312, bq to +0.0297, oth to +0.0158)
        compareRendering("PartySummaryScreen", "SingleParty-5", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            CONSERVATIVE SUMMARY
            
            CANADA: SEATS: 121 (+22); POPULAR VOTE: 34.3% (+2.4%)
            
            BRITISH COLUMBIA: SEATS: 17 (+7); POPULAR VOTE: 34.0% (+4.0%)
            PRAIRIES: SEATS: 54 (+10); POPULAR VOTE: 63.8% (+10.5%)
            ONTARIO: SEATS: 36 (+3); POPULAR VOTE: 33.1% (-2.0%)
            QUÉBEC: SEATS: 10 (-2); POPULAR VOTE: 16.0% (-0.7%)
            ATLANTIC: SEATS: 4 (+4); POPULAR VOTE: 28.6% (+9.6%)
            NORTH: SEATS: 0 (±0); POPULAR VOTE: 28.9% (+6.7%)
            """.trimIndent(),
        )
    }

    @Test
    fun testBasicPartySummarySeatsOnly() {
        val canada = Region("Canada")
        val bc = Region("British Columbia")
        val prairies = Region("Prairies")
        val ontario = Region("Ontario")
        val quebec = Region("Qu\u00e9bec")
        val atlantic = Region("Atlantic")
        val north = Region("North")
        val partySelected = Publisher(lib)
        val screen = PartySummaryScreen.of(
            mainRegion = canada,
            header = { name.uppercase().asOneTimePublisher() },
            numRows = 3,
            seats = {
                curr = { seatsPublisher }
                diff = { seatDiffPublisher }
                header = "SEAT PROJECTION"
            },
            regions = listOf(bc, prairies, ontario, quebec, atlantic, north),
            party = partySelected,
        )
        screen.setSize(1024, 512)
        compareRendering("PartySummaryScreen", "SinglePartySeatsOnly-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: SEAT PROJECTION: 0 (±0)
            
            BRITISH COLUMBIA: SEAT PROJECTION: 0 (±0)
            PRAIRIES: SEAT PROJECTION: 0 (±0)
            ONTARIO: SEAT PROJECTION: 0 (±0)
            QUÉBEC: SEAT PROJECTION: 0 (±0)
            ATLANTIC: SEAT PROJECTION: 0 (±0)
            NORTH: SEAT PROJECTION: 0 (±0)
            """.trimIndent(),
        )

        atlantic.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        atlantic.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        atlantic.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        atlantic.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        canada.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        canada.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        canada.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        compareRendering("PartySummaryScreen", "SinglePartySeatsOnly-2", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: SEAT PROJECTION: 26 (-6)
            
            BRITISH COLUMBIA: SEAT PROJECTION: 0 (±0)
            PRAIRIES: SEAT PROJECTION: 0 (±0)
            ONTARIO: SEAT PROJECTION: 0 (±0)
            QUÉBEC: SEAT PROJECTION: 0 (±0)
            ATLANTIC: SEAT PROJECTION: 26 (-6)
            NORTH: SEAT PROJECTION: 0 (±0)
            """.trimIndent(),
        )

        quebec.seats = mapOf(lib to 35, con to 10, ndp to 1, bq to 32)
        quebec.seatDiff = mapOf(lib to -5, con to -2, ndp to -15, bq to +22)
        quebec.votePct = mapOf(lib to 0.3428, con to 0.1598, ndp to 0.1084, grn to 0.0451, bq to 0.3237, oth to 0.0201)
        quebec.votePctDiff = mapOf(lib to -0.0146, con to -0.0074, ndp to -0.1451, grn to +0.0227, bq to +0.1301, oth to +0.0143)
        ontario.seats = mapOf(lib to 79, con to 36, ndp to 6)
        ontario.seatDiff = mapOf(lib to -1, con to +3, ndp to -2)
        ontario.votePct = mapOf(lib to 0.4155, con to 0.3305, ndp to 0.1681, grn to 0.0623, oth to 0.0235)
        ontario.votePctDiff = mapOf(lib to -0.0323, con to -0.0201, ndp to +0.0021, grn to +0.0339, oth to +0.0164)
        prairies.seats = mapOf(lib to 4, con to 54, ndp to 4)
        prairies.seatDiff = mapOf(lib to -8, con to +10, ndp to -2)
        prairies.votePct = mapOf(lib to 0.1574, con to 0.6381, ndp to 0.1470, grn to 0.0321, oth to 0.0253)
        prairies.votePctDiff = mapOf(lib to -0.1258, con to +0.1054, ndp to +0.0026, grn to +0.0064, oth to +0.0115)
        north.seats = mapOf(lib to 1, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3645, con to 0.2574, ndp to 0.2914, grn to 0.0752, oth to 0.0115)
        north.votePctDiff = mapOf(lib to -0.1141, con to +0.0491, ndp to 0.0016, grn to +0.0520, oth to +0.0115)
        canada.seats = mapOf(lib to 145, con to 104, ndp to 13, grn to 1, bq to 32)
        canada.seatDiff = mapOf(lib to -21, con to +15, ndp to -17, grn to +1, bq to +22)
        canada.votePct = mapOf(lib to 0.3418, con to 0.3439, ndp to 0.1469, grn to 0.0564, bq to 0.0880, oth to 0.0230)
        canada.votePctDiff = mapOf(lib to -0.0595, con to 0.0216, ndp to -0.0405, grn to 0.0296, bq to +0.0340, oth to +0.0148)
        compareRendering("PartySummaryScreen", "SinglePartySeatsOnly-3", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: SEAT PROJECTION: 145 (-21)
            
            BRITISH COLUMBIA: SEAT PROJECTION: 0 (±0)
            PRAIRIES: SEAT PROJECTION: 4 (-8)
            ONTARIO: SEAT PROJECTION: 79 (-1)
            QUÉBEC: SEAT PROJECTION: 35 (-5)
            ATLANTIC: SEAT PROJECTION: 26 (-6)
            NORTH: SEAT PROJECTION: 1 (-1)
            """.trimIndent(),
        )

        partySelected.submit(con)
        compareRendering("PartySummaryScreen", "SinglePartySeatsOnly-4", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            CONSERVATIVE SUMMARY
            
            CANADA: SEAT PROJECTION: 104 (+15)
            
            BRITISH COLUMBIA: SEAT PROJECTION: 0 (±0)
            PRAIRIES: SEAT PROJECTION: 54 (+10)
            ONTARIO: SEAT PROJECTION: 36 (+3)
            QUÉBEC: SEAT PROJECTION: 10 (-2)
            ATLANTIC: SEAT PROJECTION: 4 (+4)
            NORTH: SEAT PROJECTION: 0 (±0)
            """.trimIndent(),
        )

        north.seats = mapOf(lib to 2, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3511, con to 0.2889, ndp to 0.2591, grn to 0.0885, oth to 0.0124)
        north.votePctDiff = mapOf(lib to -0.1505, con to +0.0669, ndp to +0.0072, grn to +0.0640, oth to +0.0124)
        bc.seats = mapOf(lib to 11, con to 17, ndp to 11, grn to 2, oth to 1)
        bc.seatDiff = mapOf(lib to -6, con to +7, ndp to -3, grn to +1, oth to +1)
        bc.votePct = mapOf(lib to 0.2616, con to 0.3398, ndp to 0.2444, grn to 0.1248, oth to 0.0294)
        bc.votePctDiff = mapOf(lib to -0.0893, con to +0.0404, ndp to -0.0158, grn to +0.0424, oth to +0.0223)
        canada.seats = mapOf(lib to 157, con to 121, ndp to 24, grn to 3, bq to 32, oth to 1)
        canada.seatDiff = mapOf(lib to -27, con to +22, ndp to -20, grn to +2, bq to +22, oth to +1)
        canada.votePct = mapOf(lib to 0.3312, con to 0.3434, ndp to 0.1598, grn to 0.0655, bq to 0.0763, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.0634, con to +0.0243, ndp to -0.0374, grn to +0.0312, bq to +0.0297, oth to +0.0158)
        compareRendering("PartySummaryScreen", "SinglePartySeatsOnly-5", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            CONSERVATIVE SUMMARY
            
            CANADA: SEAT PROJECTION: 121 (+22)
            
            BRITISH COLUMBIA: SEAT PROJECTION: 17 (+7)
            PRAIRIES: SEAT PROJECTION: 54 (+10)
            ONTARIO: SEAT PROJECTION: 36 (+3)
            QUÉBEC: SEAT PROJECTION: 10 (-2)
            ATLANTIC: SEAT PROJECTION: 4 (+4)
            NORTH: SEAT PROJECTION: 0 (±0)
            """.trimIndent(),
        )
    }

    @Test
    fun testBasicPartySummaryVotesOnly() {
        val canada = Region("Canada")
        val bc = Region("British Columbia")
        val prairies = Region("Prairies")
        val ontario = Region("Ontario")
        val quebec = Region("Qu\u00e9bec")
        val atlantic = Region("Atlantic")
        val north = Region("North")
        val partySelected = Publisher(lib)
        val screen = PartySummaryScreen.of(
            mainRegion = canada,
            header = { name.uppercase().asOneTimePublisher() },
            numRows = 3,
            votes = {
                currPct = { votePublisher }
                diffPct = { voteDiffPublisher }
                header = "CENTRAL VOTE FORECAST"
            },
            regions = listOf(bc, prairies, ontario, quebec, atlantic, north),
            party = partySelected,
        )
        screen.setSize(1024, 512)
        compareRendering("PartySummaryScreen", "SinglePartyVotesOnly-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            
            BRITISH COLUMBIA: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            PRAIRIES: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            ONTARIO: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            QUÉBEC: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            ATLANTIC: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            NORTH: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            """.trimIndent(),
        )

        atlantic.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        atlantic.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        atlantic.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        atlantic.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        canada.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        canada.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        canada.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        compareRendering("PartySummaryScreen", "SinglePartyVotesOnly-2", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: CENTRAL VOTE FORECAST: 40.9% (-17.8%)
            
            BRITISH COLUMBIA: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            PRAIRIES: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            ONTARIO: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            QUÉBEC: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            ATLANTIC: CENTRAL VOTE FORECAST: 40.9% (-17.8%)
            NORTH: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            """.trimIndent(),
        )

        quebec.seats = mapOf(lib to 35, con to 10, ndp to 1, bq to 32)
        quebec.seatDiff = mapOf(lib to -5, con to -2, ndp to -15, bq to +22)
        quebec.votePct = mapOf(lib to 0.3428, con to 0.1598, ndp to 0.1084, grn to 0.0451, bq to 0.3237, oth to 0.0201)
        quebec.votePctDiff = mapOf(lib to -0.0146, con to -0.0074, ndp to -0.1451, grn to +0.0227, bq to +0.1301, oth to +0.0143)
        ontario.seats = mapOf(lib to 79, con to 36, ndp to 6)
        ontario.seatDiff = mapOf(lib to -1, con to +3, ndp to -2)
        ontario.votePct = mapOf(lib to 0.4155, con to 0.3305, ndp to 0.1681, grn to 0.0623, oth to 0.0235)
        ontario.votePctDiff = mapOf(lib to -0.0323, con to -0.0201, ndp to +0.0021, grn to +0.0339, oth to +0.0164)
        prairies.seats = mapOf(lib to 4, con to 54, ndp to 4)
        prairies.seatDiff = mapOf(lib to -8, con to +10, ndp to -2)
        prairies.votePct = mapOf(lib to 0.1574, con to 0.6381, ndp to 0.1470, grn to 0.0321, oth to 0.0253)
        prairies.votePctDiff = mapOf(lib to -0.1258, con to +0.1054, ndp to +0.0026, grn to +0.0064, oth to +0.0115)
        north.seats = mapOf(lib to 1, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3645, con to 0.2574, ndp to 0.2914, grn to 0.0752, oth to 0.0115)
        north.votePctDiff = mapOf(lib to -0.1141, con to +0.0491, ndp to 0.0016, grn to +0.0520, oth to +0.0115)
        canada.seats = mapOf(lib to 145, con to 104, ndp to 13, grn to 1, bq to 32)
        canada.seatDiff = mapOf(lib to -21, con to +15, ndp to -17, grn to +1, bq to +22)
        canada.votePct = mapOf(lib to 0.3418, con to 0.3439, ndp to 0.1469, grn to 0.0564, bq to 0.0880, oth to 0.0230)
        canada.votePctDiff = mapOf(lib to -0.0595, con to 0.0216, ndp to -0.0405, grn to 0.0296, bq to +0.0340, oth to +0.0148)
        compareRendering("PartySummaryScreen", "SinglePartyVotesOnly-3", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: CENTRAL VOTE FORECAST: 34.2% (-5.9%)
            
            BRITISH COLUMBIA: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            PRAIRIES: CENTRAL VOTE FORECAST: 15.7% (-12.6%)
            ONTARIO: CENTRAL VOTE FORECAST: 41.5% (-3.2%)
            QUÉBEC: CENTRAL VOTE FORECAST: 34.3% (-1.5%)
            ATLANTIC: CENTRAL VOTE FORECAST: 40.9% (-17.8%)
            NORTH: CENTRAL VOTE FORECAST: 36.4% (-11.4%)
            """.trimIndent(),
        )

        partySelected.submit(con)
        compareRendering("PartySummaryScreen", "SinglePartyVotesOnly-4", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            CONSERVATIVE SUMMARY
            
            CANADA: CENTRAL VOTE FORECAST: 34.4% (+2.2%)
            
            BRITISH COLUMBIA: CENTRAL VOTE FORECAST: 0.0% (±0.0%)
            PRAIRIES: CENTRAL VOTE FORECAST: 63.8% (+10.5%)
            ONTARIO: CENTRAL VOTE FORECAST: 33.1% (-2.0%)
            QUÉBEC: CENTRAL VOTE FORECAST: 16.0% (-0.7%)
            ATLANTIC: CENTRAL VOTE FORECAST: 28.6% (+9.6%)
            NORTH: CENTRAL VOTE FORECAST: 25.7% (+4.9%)
            """.trimIndent(),
        )

        north.seats = mapOf(lib to 2, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3511, con to 0.2889, ndp to 0.2591, grn to 0.0885, oth to 0.0124)
        north.votePctDiff = mapOf(lib to -0.1505, con to +0.0669, ndp to +0.0072, grn to +0.0640, oth to +0.0124)
        bc.seats = mapOf(lib to 11, con to 17, ndp to 11, grn to 2, oth to 1)
        bc.seatDiff = mapOf(lib to -6, con to +7, ndp to -3, grn to +1, oth to +1)
        bc.votePct = mapOf(lib to 0.2616, con to 0.3398, ndp to 0.2444, grn to 0.1248, oth to 0.0294)
        bc.votePctDiff = mapOf(lib to -0.0893, con to +0.0404, ndp to -0.0158, grn to +0.0424, oth to +0.0223)
        canada.seats = mapOf(lib to 157, con to 121, ndp to 24, grn to 3, bq to 32, oth to 1)
        canada.seatDiff = mapOf(lib to -27, con to +22, ndp to -20, grn to +2, bq to +22, oth to +1)
        canada.votePct = mapOf(lib to 0.3312, con to 0.3434, ndp to 0.1598, grn to 0.0655, bq to 0.0763, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.0634, con to +0.0243, ndp to -0.0374, grn to +0.0312, bq to +0.0297, oth to +0.0158)
        compareRendering("PartySummaryScreen", "SinglePartyVotesOnly-5", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            CONSERVATIVE SUMMARY
            
            CANADA: CENTRAL VOTE FORECAST: 34.3% (+2.4%)
            
            BRITISH COLUMBIA: CENTRAL VOTE FORECAST: 34.0% (+4.0%)
            PRAIRIES: CENTRAL VOTE FORECAST: 63.8% (+10.5%)
            ONTARIO: CENTRAL VOTE FORECAST: 33.1% (-2.0%)
            QUÉBEC: CENTRAL VOTE FORECAST: 16.0% (-0.7%)
            ATLANTIC: CENTRAL VOTE FORECAST: 28.6% (+9.6%)
            NORTH: CENTRAL VOTE FORECAST: 28.9% (+6.7%)
            """.trimIndent(),
        )
    }

    @Test
    fun testBasicPartySummaryWithPartyChange() {
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ca = Party("Canadian Alliance", "CA", Color.CYAN)

        val canada = RegionWithPrev("Canada")
        val bc = RegionWithPrev("British Columbia")
        val prairies = RegionWithPrev("Prairies")
        val ontario = RegionWithPrev("Ontario")
        val quebec = RegionWithPrev("Qu\u00e9bec")
        val atlantic = RegionWithPrev("Atlantic")
        val north = RegionWithPrev("North")
        val partySelected = Publisher(lib)
        val screen = PartySummaryScreen.of(
            mainRegion = canada,
            header = { name.uppercase().asOneTimePublisher() },
            seats = {
                curr = { seatsPublisher }
                prev = { prevSeatPublisher }
            },
            votes = {
                currPct = { Aggregators.toPct(votePublisher) }
                prevPct = { Aggregators.toPct(prevVotePublisher) }
            },
            numRows = 3,
            regions = listOf(bc, prairies, ontario, quebec, atlantic, north),
            party = partySelected,
            partyChanges = mapOf(pc to con, ca to con).asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("PartySummaryScreen", "PartyChange-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            
            BRITISH COLUMBIA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            PRAIRIES: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            ONTARIO: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            QUÉBEC: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            ATLANTIC: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            NORTH: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            """.trimIndent(),
        )

        atlantic.seats = mapOf(lib to 22, con to 7, ndp to 3)
        atlantic.prevSeats = mapOf(lib to 19, pc to 9, ndp to 4)
        atlantic.votes = mapOf(lib to 474247, con to 325272, ndp to 244871, grn to 32943, oth to 5581)
        atlantic.prevVotes = mapOf(lib to 456797, pc to 351328, ca to 114583, ndp to 185762, grn to 968, oth to 12824)
        canada.seats = mapOf(lib to 22, con to 7, ndp to 3)
        canada.prevSeats = mapOf(lib to 19, pc to 9, ndp to 4)
        canada.votes = mapOf(lib to 474247, con to 325272, ndp to 244871, grn to 32943, oth to 5581)
        canada.prevVotes = mapOf(lib to 456797, pc to 351328, ca to 114583, ndp to 185762, grn to 968, oth to 12824)
        compareRendering("PartySummaryScreen", "PartyChange-2", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY
            
            CANADA: SEATS: 22 (+3); POPULAR VOTE: 43.8% (+3.1%)

            BRITISH COLUMBIA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            PRAIRIES: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            ONTARIO: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            QUÉBEC: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            ATLANTIC: SEATS: 22 (+3); POPULAR VOTE: 43.8% (+3.1%)
            NORTH: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            """.trimIndent(),
        )

        quebec.seats = mapOf(lib to 21, bq to 54)
        quebec.prevSeats = mapOf(lib to 36, pc to 1, bq to 38)
        quebec.votes = mapOf(lib to 1165645, con to 301539, ndp to 158427, grn to 108660, bq to 1680109, oth to 23875)
        quebec.prevVotes = mapOf(lib to 1529642, pc to 192153, ca to 212874, ndp to 63611, grn to 19846, bq to 1377727, oth to 61045)
        ontario.seats = mapOf(lib to 75, con to 24, ndp to 7)
        ontario.prevSeats = mapOf(lib to 100, ca to 2, ndp to 1)
        ontario.votes = mapOf(lib to 2278875, con to 1607337, ndp to 921240, grn to 226812, oth to 66215)
        ontario.prevVotes = mapOf(lib to 2292069, pc to 642438, ca to 1051209, ndp to 368709, grn to 39737, oth to 58437)
        prairies.seats = mapOf(lib to 6, con to 46, ndp to 4)
        prairies.prevSeats = mapOf(lib to 9, pc to 2, ca to 37, ndp to 6)
        prairies.votes = mapOf(lib to 553602, con to 1150344, ndp to 332821, grn to 102569, oth to 36910)
        prairies.prevVotes = mapOf(lib to 511418, pc to 260583, ca to 1094811, ndp to 283730, grn to 9205, oth to 17781)
        north.seats = mapOf(lib to 2)
        north.prevSeats = mapOf(lib to 2)
        north.votes = mapOf(lib to 9135, con to 3389, ndp to 6393, grn to 831, oth to 1172)
        north.prevVotes = mapOf(lib to 11182, pc to 1915, ca to 2273, ndp to 4840, grn to 349)
        canada.seats = mapOf(lib to 126, con to 77, ndp to 14, bq to 54)
        canada.prevSeats = mapOf(lib to 166, pc to 12, ca to 39, ndp to 11, bq to 38)
        canada.votes = mapOf(lib to 4481504, con to 3387881, ndp to 1663752, grn to 471815, bq to 1680109, oth to 133753)
        canada.prevVotes = mapOf(lib to 4801108, pc to 1448417, ca to 2475750, ndp to 906652, grn to 70105, bq to 1377727, oth to 150087)
        compareRendering("PartySummaryScreen", "PartyChange-3", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            LIBERAL SUMMARY

            CANADA: SEATS: 126 (-40); POPULAR VOTE: 37.9% (-4.8%)

            BRITISH COLUMBIA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            PRAIRIES: SEATS: 6 (-3); POPULAR VOTE: 25.4% (+2.0%)
            ONTARIO: SEATS: 75 (-25); POPULAR VOTE: 44.7% (-6.8%)
            QUÉBEC: SEATS: 21 (-15); POPULAR VOTE: 33.9% (-10.3%)
            ATLANTIC: SEATS: 22 (+3); POPULAR VOTE: 43.8% (+3.1%)
            NORTH: SEATS: 2 (±0); POPULAR VOTE: 43.7% (-10.7%)
            """.trimIndent(),
        )

        partySelected.submit(con)
        compareRendering("PartySummaryScreen", "PartyChange-4", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            CONSERVATIVE SUMMARY

            CANADA: SEATS: 77 (+26); POPULAR VOTE: 28.7% (-6.3%)

            BRITISH COLUMBIA: SEATS: 0 (±0); POPULAR VOTE: 0.0% (±0.0%)
            PRAIRIES: SEATS: 46 (+7); POPULAR VOTE: 52.9% (-9.4%)
            ONTARIO: SEATS: 24 (+22); POPULAR VOTE: 31.5% (-6.5%)
            QUÉBEC: SEATS: 0 (-1); POPULAR VOTE: 8.8% (-2.9%)
            ATLANTIC: SEATS: 7 (-2); POPULAR VOTE: 30.0% (-11.5%)
            NORTH: SEATS: 0 (±0); POPULAR VOTE: 16.2% (-4.2%)
            """.trimIndent(),
        )

        north.seats = mapOf(lib to 3)
        north.prevSeats = mapOf(lib to 3)
        north.votes = mapOf(lib to 14859, con to 6007, ndp to 9609, grn to 1402, oth to 1571)
        north.prevVotes = mapOf(lib to 15475, pc to 2906, ca to 5932, ndp to 9063, grn to 349, oth to 53)
        bc.seats = mapOf(lib to 8, con to 22, ndp to 5, oth to 1)
        bc.prevSeats = mapOf(lib to 5, ca to 27, ndp to 2)
        bc.votes = mapOf(lib to 494992, con to 628999, ndp to 460435, grn to 109861, oth to 39073)
        bc.prevVotes = mapOf(lib to 446574, pc to 117614, ca to 797519, ndp to 182993, grn to 34294, oth to 35678)
        canada.seats = mapOf(lib to 135, con to 99, ndp to 19, bq to 54, oth to 1)
        canada.prevSeats = mapOf(lib to 172, pc to 12, ca to 66, ndp to 13, bq to 38)
        canada.votes = mapOf(lib to 4982220, con to 4019498, ndp to 2127403, grn to 582247, bq to 1680109, oth to 173225)
        canada.prevVotes = mapOf(lib to 5251975, pc to 1567022, ca to 3276928, ndp to 1093868, grn to 104399, bq to 1377727, oth to 185818)
        compareRendering("PartySummaryScreen", "PartyChange-5", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            CONSERVATIVE SUMMARY

            CANADA: SEATS: 99 (+21); POPULAR VOTE: 29.6% (-8.0%)

            BRITISH COLUMBIA: SEATS: 22 (-5); POPULAR VOTE: 36.3% (-20.4%)
            PRAIRIES: SEATS: 46 (+7); POPULAR VOTE: 52.9% (-9.4%)
            ONTARIO: SEATS: 24 (+22); POPULAR VOTE: 31.5% (-6.5%)
            QUÉBEC: SEATS: 0 (-1); POPULAR VOTE: 8.8% (-2.9%)
            ATLANTIC: SEATS: 7 (-2); POPULAR VOTE: 30.0% (-11.5%)
            NORTH: SEATS: 0 (±0); POPULAR VOTE: 18.0% (-8.2%)
            """.trimIndent(),
        )
    }

    @Test
    fun testBasicPartySummaryWithCoalition() {
        val lib = Party("Liberal", "LIB", Color.BLUE)
        val nat = Party("National", "NAT", Color.GREEN.darker().darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE.brighter())
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val coa = Coalition("Coalition", "L/NP", Color.BLUE, lib, nat, lnp, clp)

        val alp = Party("Labor", "ALP", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN)
        val ind = Party("Independent", "IND", Color.DARK_GRAY)
        val oth = Party.OTHERS

        val aus = RegionWithPrev("Australia").apply {
            seats = mapOf(alp to 94, coa to 44, grn to 1, ind to 9, oth to 2)
            prevSeats = mapOf(alp to 77, coa to 59, grn to 4, ind to 8, oth to 2)
            votes = mapOf(alp to 5354156, coa to 4929606, grn to 1890004, ind to 1126179, oth to 2190755)
            prevVotes = mapOf(alp to 4776030, coa to 5233334, grn to 1795985, ind to 776169, oth to 2077524)
        }
        val nsw = RegionWithPrev("New South Wales").apply {
            seats = mapOf(alp to 28, coa to 13, ind to 5)
            prevSeats = mapOf(alp to 25, coa to 17, ind to 4)
            votes = mapOf(alp to 1687010, coa to 1511308, grn to 530322, ind to 465248, oth to 599590)
            prevVotes = mapOf(alp to 1552684, coa to 1699324, grn to 466069, ind to 351620, oth to 581243)
        }
        val vic = RegionWithPrev("Victoria").apply {
            seats = mapOf(alp to 27, coa to 9, ind to 2)
            prevSeats = mapOf(alp to 24, coa to 10, grn to 1, ind to 3)
            votes = mapOf(alp to 1375991, coa to 1305070, grn to 550660, ind to 305284, oth to 516411)
            prevVotes = mapOf(alp to 1230842, coa to 1239280, grn to 514893, ind to 243992, oth to 517426)
        }
        val qld = RegionWithPrev("Queensland").apply {
            seats = mapOf(alp to 12, lnp to 16, grn to 1, oth to 1)
            prevSeats = mapOf(alp to 5, lnp to 21, grn to 3, oth to 1)
            votes = mapOf(alp to 975848, lnp to 1099623, grn to 370313, ind to 118228, oth to 585633)
            prevVotes = mapOf(alp to 811069, lnp to 1172515, grn to 382900, ind to 61944, oth to 529598)
        }
        val wa = RegionWithPrev("Western Australia").apply {
            seats = mapOf(alp to 11, lib to 4, ind to 1)
            prevSeats = mapOf(alp to 10, lib to 5, ind to 1)
            votes = mapOf(alp to 568885, lib to 458187, nat to 46062, grn to 191389, ind to 87987, oth to 245925)
            prevVotes = mapOf(alp to 542667, lib to 503254, nat to 9160, grn to 184094, ind to 35968, oth to 198082)
        }
        val sa = RegionWithPrev("South Australia").apply {
            seats = mapOf(alp to 7, lib to 2, oth to 1)
            prevSeats = mapOf(alp to 6, lib to 3, oth to 1)
            votes = mapOf(alp to 433738, lib to 316915, nat to 5181, grn to 151915, ind to 38063, oth to 186426)
            prevVotes = mapOf(alp to 378329, lib to 387664, nat to 2531, grn to 140227, ind to 29500, oth to 159535)
        }
        val tas = RegionWithPrev("Tasmania").apply {
            seats = mapOf(alp to 4, ind to 1)
            prevSeats = mapOf(alp to 2, lib to 2, ind to 1)
            votes = mapOf(alp to 134435, lib to 89988, grn to 40833, ind to 65907, oth to 36096)
            prevVotes = mapOf(alp to 95322, lib to 115184, grn to 41972, ind to 38993, oth to 58227)
        }
        val act = RegionWithPrev("Australian Capital Territory").apply {
            seats = mapOf(alp to 3)
            prevSeats = mapOf(alp to 3)
            votes = mapOf(alp to 138110, lib to 61489, grn to 43753, ind to 37307, oth to 9906)
            prevVotes = mapOf(alp to 126595, lib to 74759, grn to 52648, ind to 12795, oth to 15200)
        }
        val nt = RegionWithPrev("Northern Territory").apply {
            seats = mapOf(alp to 2)
            prevSeats = mapOf(alp to 2)
            votes = mapOf(alp to 40123, clp to 35785, grn to 10813, ind to 8194, oth to 10847)
            prevVotes = mapOf(alp to 38522, clp to 29664, grn to 13182, ind to 1357, oth to 18212)
        }

        val screen = PartySummaryScreen.of(
            mainRegion = aus,
            header = { name.uppercase().asOneTimePublisher() },
            seats = {
                curr = { seatsPublisher }
                prev = { prevSeatPublisher }
            },
            votes = {
                currPct = { Aggregators.toPct(votePublisher) }
                prevPct = { Aggregators.toPct(prevVotePublisher) }
            },
            numRows = 4,
            regions = listOf(nsw, vic, qld, wa, sa, tas, act, nt),
            party = coa.asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("PartySummaryScreen", "Coalition-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            COALITION SUMMARY

            AUSTRALIA: SEATS: 44 (-15); POPULAR VOTE: 31.8% (-3.9%)
            
            NEW SOUTH WALES: SEATS: 13 (-4); POPULAR VOTE: 31.5% (-5.0%)
            VICTORIA: SEATS: 9 (-1); POPULAR VOTE: 32.2% (-0.9%)
            QUEENSLAND: SEATS: 16 (-5); POPULAR VOTE: 34.9% (-4.7%)
            WESTERN AUSTRALIA: SEATS: 4 (-1); POPULAR VOTE: 31.5% (-3.2%)
            SOUTH AUSTRALIA: SEATS: 2 (-1); POPULAR VOTE: 28.4% (-7.1%)
            TASMANIA: SEATS: 0 (-2); POPULAR VOTE: 24.5% (-8.4%)
            AUSTRALIAN CAPITAL TERRITORY: SEATS: 0 (±0); POPULAR VOTE: 21.2% (-5.3%)
            NORTHERN TERRITORY: SEATS: 0 (±0); POPULAR VOTE: 33.8% (+4.4%)
            """.trimIndent(),
        )
    }

    private class Region constructor(val name: String) {
        var seats: Map<PartyOrCoalition, Int> = emptyMap()
            set(value) {
                field = value
                seatsPublisher.submit(seats)
                prevSeatPublisher.submit(prevSeats)
            }
        val seatsPublisher = Publisher(seats)

        var seatDiff: Map<PartyOrCoalition, Int> = emptyMap()
            set(value) {
                field = value
                seatDiffPublisher.submit(seatDiff)
                prevSeatPublisher.submit(prevSeats)
            }
        val seatDiffPublisher = Publisher(seatDiff)

        var votePct: Map<PartyOrCoalition, Double> = emptyMap()
            set(value) {
                field = value
                votePublisher.submit(votePct)
                prevVotePublisher.submit(prevVotePct)
            }
        val votePublisher = Publisher(votePct)

        var votePctDiff: Map<PartyOrCoalition, Double> = emptyMap()
            set(value) {
                field = value
                voteDiffPublisher.submit(votePctDiff)
                prevVotePublisher.submit(prevVotePct)
            }
        val voteDiffPublisher = Publisher(votePctDiff)

        val prevSeats: Map<PartyOrCoalition, Int>
            get() = sequenceOf(seats.keys, seatDiff.keys)
                .flatten()
                .distinct()
                .associateWith { (seats[it] ?: 0) - (seatDiff[it] ?: 0) }
        val prevSeatPublisher = Publisher(prevSeats)

        val prevVotePct: Map<PartyOrCoalition, Double>
            get() = sequenceOf(votePct.keys, votePctDiff.keys)
                .flatten()
                .distinct()
                .associateWith { (votePct[it] ?: 0.0) - (votePctDiff[it] ?: 0.0) }
        val prevVotePublisher = Publisher(prevVotePct)
    }

    private class RegionWithPrev constructor(val name: String) {
        var seats: Map<PartyOrCoalition, Int> = emptyMap()
            set(value) {
                field = value
                seatsPublisher.submit(value)
            }
        val seatsPublisher = Publisher(seats)

        var votes: Map<PartyOrCoalition, Int> = emptyMap()
            set(value) {
                field = value
                votePublisher.submit(value)
            }
        val votePublisher = Publisher(votes)

        var prevSeats: Map<PartyOrCoalition, Int> = emptyMap()
            set(value) {
                field = value
                prevSeatPublisher.submit(value)
            }
        val prevSeatPublisher = Publisher(prevSeats)

        var prevVotes: Map<PartyOrCoalition, Int> = emptyMap()
            set(value) {
                field = value
                prevVotePublisher.submit(value)
            }
        val prevVotePublisher = Publisher(prevVotes)
    }
}
