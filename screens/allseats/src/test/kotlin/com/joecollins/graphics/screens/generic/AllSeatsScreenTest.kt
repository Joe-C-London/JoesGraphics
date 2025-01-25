package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.AllSeatsScreen.Companion.group
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PartyResult.Companion.elected
import com.joecollins.models.general.PartyResult.Companion.leading
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color

class AllSeatsScreenTest {
    @Test
    fun testAllSeats() {
        val prevResult = Publisher(bcPrevResult())
        val currResult = Publisher<Map<String, PartyResult?>>(emptyMap())
        val numRows = Publisher(15)
        val title = Publisher("BRITISH COLUMBIA")
        val panel = AllSeatsScreen.of(
            prevWinner = prevResult,
            currResult = currResult,
            nameFunc = { uppercase() },
            header = "ALL SEATS".asOneTimePublisher(),
            numRows = numRows,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("AllSeatsScreen", "Basic-1", panel)
        assertPublishes(
            panel.altText,
            """
            BRITISH COLUMBIA

            ALL SEATS
            PENDING LIB: 43
            PENDING NDP: 41
            PENDING GRN: 3
            """.trimIndent(),
        )

        currResult.submit(
            mapOf(
                "Coquitlam-Burke Mountain" to leading(ndp),
                "Fraser-Nicola" to leading(ndp),
                "Vancouver-False Creek" to leading(ndp),
            ),
        )
        compareRendering("AllSeatsScreen", "Basic-2", panel)
        assertPublishes(
            panel.altText,
            """
            BRITISH COLUMBIA

            ALL SEATS
            PENDING NDP: 41
            PENDING LIB: 40
            NDP GAIN FROM LIB: 0/3
            PENDING GRN: 3
            """.trimIndent(),
        )

        currResult.submit(
            mapOf(
                "Coquitlam-Burke Mountain" to elected(ndp),
                "Fraser-Nicola" to leading(lib),
                "Richmond-Queensborough" to leading(ndp),
                "Vancouver-False Creek" to leading(ndp),
            ),
        )
        compareRendering("AllSeatsScreen", "Basic-3", panel)
        assertPublishes(
            panel.altText,
            """
            BRITISH COLUMBIA

            ALL SEATS
            PENDING NDP: 41
            PENDING LIB: 39
            NDP GAIN FROM LIB: 1/3
            PENDING GRN: 3
            LIB HOLD: 0/1
            """.trimIndent(),
        )

        currResult.submit(bcCurrResult())
        compareRendering("AllSeatsScreen", "Basic-4", panel)
        assertPublishes(
            panel.altText,
            """
            BRITISH COLUMBIA

            ALL SEATS
            NDP HOLD: 39/41
            LIB HOLD: 16/28
            NDP GAIN FROM LIB: 4/15
            GRN HOLD: 1/2
            NDP GAIN FROM GRN: 1/1
            """.trimIndent(),
        )
    }

    @Test
    fun testFilteredSeats() {
        val prevResult = Publisher(bcPrevResult())
        val currResult = Publisher<Map<String, PartyResult?>>(emptyMap())
        val numRows = Publisher(15)
        val title = Publisher("VANCOUVER")
        val filteredSeats = Publisher(
            bcPrevResult().keys
                .filter { it.startsWith("Vancouver") }
                .toSet(),
        )
        val panel = AllSeatsScreen.of(
            prevWinner = prevResult,
            currResult = currResult,
            nameFunc = { uppercase() },
            header = "ALL SEATS".asOneTimePublisher(),
            numRows = numRows,
            seatFilter = filteredSeats,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("AllSeatsScreen", "Filtered-1", panel)
        assertPublishes(
            panel.altText,
            """
            VANCOUVER

            ALL SEATS
            PENDING NDP: 8
            PENDING LIB: 3
            """.trimIndent(),
        )

        currResult.submit(
            mapOf(
                "Coquitlam-Burke Mountain" to leading(ndp),
                "Fraser-Nicola" to leading(ndp),
                "Vancouver-False Creek" to leading(ndp),
            ),
        )
        compareRendering("AllSeatsScreen", "Filtered-2", panel)
        assertPublishes(
            panel.altText,
            """
            VANCOUVER

            ALL SEATS
            PENDING NDP: 8
            PENDING LIB: 2
            NDP GAIN FROM LIB: 0/1
            """.trimIndent(),
        )

        currResult.submit(
            mapOf(
                "Coquitlam-Burke Mountain" to elected(ndp),
                "Fraser-Nicola" to leading(lib),
                "Richmond-Queensborough" to leading(ndp),
                "Vancouver-False Creek" to leading(ndp),
            ),
        )
        compareRendering("AllSeatsScreen", "Filtered-3", panel)
        assertPublishes(
            panel.altText,
            """
            VANCOUVER

            ALL SEATS
            PENDING NDP: 8
            PENDING LIB: 2
            NDP GAIN FROM LIB: 0/1
            """.trimIndent(),
        )

        currResult.submit(bcCurrResult())
        compareRendering("AllSeatsScreen", "Filtered-4", panel)
        assertPublishes(
            panel.altText,
            """
            VANCOUVER

            ALL SEATS
            NDP HOLD: 8/8
            LIB HOLD: 1/2
            NDP GAIN FROM LIB: 0/1
            """.trimIndent(),
        )
    }

    @Test
    fun testAllSeatsWithNullResults() {
        val prevResult = Publisher(bcPrevResult())
        val currResult = Publisher<Map<String, PartyResult?>>(bcCurrResult().mapValues { null })
        val numRows = Publisher(15)
        val title = Publisher("BRITISH COLUMBIA")
        val panel = AllSeatsScreen.of(
            prevWinner = prevResult,
            currResult = currResult,
            nameFunc = { uppercase() },
            header = "ALL SEATS".asOneTimePublisher(),
            numRows = numRows,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("AllSeatsScreen", "Basic-1", panel)
        assertPublishes(
            panel.altText,
            """
            BRITISH COLUMBIA

            ALL SEATS
            PENDING LIB: 43
            PENDING NDP: 41
            PENDING GRN: 3
            """.trimIndent(),
        )

        currResult.submit(bcCurrResult())
        compareRendering("AllSeatsScreen", "Basic-4", panel)
        assertPublishes(
            panel.altText,
            """
            BRITISH COLUMBIA

            ALL SEATS
            NDP HOLD: 39/41
            LIB HOLD: 16/28
            NDP GAIN FROM LIB: 4/15
            GRN HOLD: 1/2
            NDP GAIN FROM GRN: 1/1
            """.trimIndent(),
        )
    }

    @Test
    fun testAllSeatsWithPartyChange() {
        val bcu = Party("BC United", "BCU", lib.color)
        val prevResult = Publisher(bcPrevResult())
        val currResult = Publisher<Map<String, PartyResult?>>(bcCurrResult().mapValues { null })
        val numRows = Publisher(15)
        val title = Publisher("BRITISH COLUMBIA")
        val panel = AllSeatsScreen.of(
            prevWinner = prevResult,
            currResult = currResult,
            nameFunc = { uppercase() },
            header = "ALL SEATS".asOneTimePublisher(),
            numRows = numRows,
            partyChanges = mapOf(lib to bcu).asOneTimePublisher(),
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("AllSeatsScreen", "Basic-1", panel)
        assertPublishes(
            panel.altText,
            """
            BRITISH COLUMBIA

            ALL SEATS
            PENDING LIB: 43
            PENDING NDP: 41
            PENDING GRN: 3
            """.trimIndent(),
        )

        currResult.submit(
            bcCurrResult().mapValues {
                if (it.value.leader == lib) {
                    PartyResult(bcu, it.value.elected)
                } else {
                    it.value
                }
            },
        )
        compareRendering("AllSeatsScreen", "Basic-4", panel)
        assertPublishes(
            panel.altText,
            """
            BRITISH COLUMBIA

            ALL SEATS
            NDP HOLD: 39/41
            BCU HOLD (LIB): 16/28
            NDP GAIN FROM LIB: 4/15
            GRN HOLD: 1/2
            NDP GAIN FROM GRN: 1/1
            """.trimIndent(),
        )
    }

    @Test
    fun testAllSeatsGrouped() {
        val prevResult = Publisher(bcPrevResult())
        val currResult = Publisher<Map<String, PartyResult?>>(bcCurrResult().mapValues { null })
        val numRows = Publisher(15)
        val filteredSeats = Publisher(null as Set<String>?)
        val title = Publisher("BRITISH COLUMBIA")
        val panel = AllSeatsScreen.ofGrouped(
            prevWinner = prevResult,
            currResult = currResult,
            nameFunc = { uppercase() },
            numRows = numRows,
            seatFilter = filteredSeats,
            groups = BCRegion.values().map { region -> group(region.name.asOneTimePublisher()) { bcRegions[this] == region } },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("AllSeatsScreen", "Grouped-1", panel)
        assertPublishes(
            panel.altText,
            """
            BRITISH COLUMBIA
            
            VANCOUVER
            PENDING NDP: 25
            PENDING LIB: 15
            
            ISLAND
            PENDING NDP: 10
            PENDING GRN: 3
            PENDING LIB: 1
            
            SOUTH
            PENDING LIB: 16
            PENDING NDP: 4
            
            NORTH
            PENDING LIB: 11
            PENDING NDP: 2
            """.trimIndent(),
        )

        filteredSeats.submit(
            bcPrevResult().keys
                .filter { it.startsWith("Vancouver") }
                .toSet(),
        )
        title.submit("CITY OF VANCOUVER")
        compareRendering("AllSeatsScreen", "Grouped-2", panel)
        assertPublishes(
            panel.altText,
            """
            CITY OF VANCOUVER
            
            VANCOUVER
            PENDING NDP: 8
            PENDING LIB: 3
            """.trimIndent(),
        )
    }

    companion object {
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())

        private fun bcPrevResult(): Map<String, Party> = mapOf(
            "Nechako Lakes" to lib,
            "North Coast" to ndp,
            "Peace River North" to lib,
            "Peace River South" to lib,
            "Prince George-Mackenzie" to lib,
            "Prince George-Valemount" to lib,
            "Skeena" to lib,
            "Stikine" to ndp,
            "Columbia River-Revelstoke" to lib,
            "Kootenay East" to lib,
            "Kootenay West" to ndp,
            "Nelson-Creston" to ndp,
            "Boundary-Similkameen" to lib,
            "Kelowna-Lake Country" to lib,
            "Kelowna-Mission" to lib,
            "Kelowna West" to lib,
            "Penticton" to lib,
            "Shuswap" to lib,
            "Vernon-Monashee" to lib,
            "Cariboo-Chilcotin" to lib,
            "Cariboo North" to lib,
            "Fraser-Nicola" to lib,
            "Kamloops-North Thompson" to lib,
            "Kamloops-South Thompson" to lib,
            "Abbotsford-Mission" to lib,
            "Abbotsford South" to lib,
            "Abbotsford West" to lib,
            "Chilliwack" to lib,
            "Chilliwack-Kent" to lib,
            "Langley" to lib,
            "Langley East" to lib,
            "Maple Ridge-Mission" to ndp,
            "Maple Ridge-Pitt Meadows" to ndp,
            "Surrey-Cloverdale" to lib,
            "Surrey-Fleetwood" to ndp,
            "Surrey-Green Timbers" to ndp,
            "Surrey-Guildford" to ndp,
            "Surrey-Newton" to ndp,
            "Surrey-Panorama" to ndp,
            "Surrey South" to lib,
            "Surrey-Whalley" to ndp,
            "Surrey-White Rock" to lib,
            "Delta North" to ndp,
            "Delta South" to lib,
            "Richmond North Centre" to lib,
            "Richmond-Queensborough" to lib,
            "Richmond South Centre" to lib,
            "Richmond-Steveston" to lib,
            "Burnaby-Deer Lake" to ndp,
            "Burnaby-Edmonds" to ndp,
            "Burnaby-Lougheed" to ndp,
            "Burnaby North" to ndp,
            "Coquitlam-Burke Mountain" to lib,
            "Coquitlam-Maillardville" to ndp,
            "New Westminster" to ndp,
            "Port Coquitlam" to ndp,
            "Port Moody-Coquitlam" to ndp,
            "Vancouver-Fairview" to ndp,
            "Vancouver-False Creek" to lib,
            "Vancouver-Fraserview" to ndp,
            "Vancouver-Hastings" to ndp,
            "Vancouver-Kensington" to ndp,
            "Vancouver-Kingsway" to ndp,
            "Vancouver-Langara" to lib,
            "Vancouver-Mount Pleasant" to ndp,
            "Vancouver-Point Grey" to ndp,
            "Vancouver-Quilchena" to lib,
            "Vancouver-West End" to ndp,
            "North Vancouver-Lonsdale" to ndp,
            "North Vancouver-Seymour" to lib,
            "Powell River-Sunshine Coast" to ndp,
            "West Vancouver-Capilano" to lib,
            "West Vancouver-Sea to Sky" to lib,
            "Courtenay-Comox" to ndp,
            "Cowichan Valley" to grn,
            "Mid Island-Pacific Rim" to ndp,
            "Nanaimo" to ndp,
            "Nanaimo-North Cowichan" to ndp,
            "North Island" to ndp,
            "Parksville-Qualicum" to lib,
            "Esquimalt-Metchosin" to ndp,
            "Langford-Juan de Fuca" to ndp,
            "Oak Bay-Gordon Head" to grn,
            "Sannich North and the Islands" to grn,
            "Saanich South" to ndp,
            "Victoria-Beacon Hill" to ndp,
            "Victoria-Swan Lake" to ndp,
        )

        private fun bcCurrResult(): Map<String, PartyResult> = mapOf(
            "Nechako Lakes" to elected(lib),
            "North Coast" to elected(ndp),
            "Peace River North" to elected(lib),
            "Peace River South" to elected(lib),
            "Prince George-Mackenzie" to elected(lib),
            "Prince George-Valemount" to elected(lib),
            "Skeena" to elected(lib),
            "Stikine" to elected(ndp),
            "Columbia River-Revelstoke" to leading(lib),
            "Kootenay East" to elected(lib),
            "Kootenay West" to elected(ndp),
            "Nelson-Creston" to leading(ndp),
            "Boundary-Similkameen" to leading(ndp),
            "Kelowna-Lake Country" to elected(lib),
            "Kelowna-Mission" to elected(lib),
            "Kelowna West" to leading(lib),
            "Penticton" to leading(lib),
            "Shuswap" to elected(lib),
            "Vernon-Monashee" to leading(ndp),
            "Cariboo-Chilcotin" to elected(lib),
            "Cariboo North" to leading(lib),
            "Fraser-Nicola" to leading(lib),
            "Kamloops-North Thompson" to leading(lib),
            "Kamloops-South Thompson" to elected(lib),
            "Abbotsford-Mission" to leading(ndp),
            "Abbotsford South" to leading(lib),
            "Abbotsford West" to leading(lib),
            "Chilliwack" to leading(ndp),
            "Chilliwack-Kent" to leading(ndp),
            "Langley" to leading(ndp),
            "Langley East" to leading(ndp),
            "Maple Ridge-Mission" to elected(ndp),
            "Maple Ridge-Pitt Meadows" to elected(ndp),
            "Surrey-Cloverdale" to elected(ndp),
            "Surrey-Fleetwood" to elected(ndp),
            "Surrey-Green Timbers" to elected(ndp),
            "Surrey-Guildford" to elected(ndp),
            "Surrey-Newton" to elected(ndp),
            "Surrey-Panorama" to elected(ndp),
            "Surrey South" to leading(lib),
            "Surrey-Whalley" to elected(ndp),
            "Surrey-White Rock" to leading(lib),
            "Delta North" to elected(ndp),
            "Delta South" to elected(lib),
            "Richmond North Centre" to elected(lib),
            "Richmond-Queensborough" to leading(ndp),
            "Richmond South Centre" to elected(ndp),
            "Richmond-Steveston" to elected(ndp),
            "Burnaby-Deer Lake" to elected(ndp),
            "Burnaby-Edmonds" to elected(ndp),
            "Burnaby-Lougheed" to elected(ndp),
            "Burnaby North" to elected(ndp),
            "Coquitlam-Burke Mountain" to elected(ndp),
            "Coquitlam-Maillardville" to elected(ndp),
            "New Westminster" to elected(ndp),
            "Port Coquitlam" to elected(ndp),
            "Port Moody-Coquitlam" to elected(ndp),
            "Vancouver-Fairview" to elected(ndp),
            "Vancouver-False Creek" to leading(ndp),
            "Vancouver-Fraserview" to elected(ndp),
            "Vancouver-Hastings" to elected(ndp),
            "Vancouver-Kensington" to elected(ndp),
            "Vancouver-Kingsway" to elected(ndp),
            "Vancouver-Langara" to leading(lib),
            "Vancouver-Mount Pleasant" to elected(ndp),
            "Vancouver-Point Grey" to elected(ndp),
            "Vancouver-Quilchena" to elected(lib),
            "Vancouver-West End" to elected(ndp),
            "North Vancouver-Lonsdale" to elected(ndp),
            "North Vancouver-Seymour" to leading(ndp),
            "Powell River-Sunshine Coast" to elected(ndp),
            "West Vancouver-Capilano" to elected(lib),
            "West Vancouver-Sea to Sky" to leading(lib),
            "Courtenay-Comox" to elected(ndp),
            "Cowichan Valley" to leading(grn),
            "Mid Island-Pacific Rim" to elected(ndp),
            "Nanaimo" to elected(ndp),
            "Nanaimo-North Cowichan" to leading(ndp),
            "North Island" to elected(ndp),
            "Parksville-Qualicum" to leading(ndp),
            "Esquimalt-Metchosin" to elected(ndp),
            "Langford-Juan de Fuca" to elected(ndp),
            "Oak Bay-Gordon Head" to elected(ndp),
            "Sannich North and the Islands" to elected(grn),
            "Saanich South" to elected(ndp),
            "Victoria-Beacon Hill" to elected(ndp),
            "Victoria-Swan Lake" to elected(ndp),
        )

        private enum class BCRegion { VANCOUVER, ISLAND, SOUTH, NORTH }
        private val bcRegions: Map<String, BCRegion> =
            mapOf(
                "Nechako Lakes" to BCRegion.NORTH,
                "North Coast" to BCRegion.NORTH,
                "Peace River North" to BCRegion.NORTH,
                "Peace River South" to BCRegion.NORTH,
                "Prince George-Mackenzie" to BCRegion.NORTH,
                "Prince George-Valemount" to BCRegion.NORTH,
                "Skeena" to BCRegion.NORTH,
                "Stikine" to BCRegion.NORTH,
                "Columbia River-Revelstoke" to BCRegion.SOUTH,
                "Kootenay East" to BCRegion.SOUTH,
                "Kootenay West" to BCRegion.SOUTH,
                "Nelson-Creston" to BCRegion.SOUTH,
                "Boundary-Similkameen" to BCRegion.SOUTH,
                "Kelowna-Lake Country" to BCRegion.SOUTH,
                "Kelowna-Mission" to BCRegion.SOUTH,
                "Kelowna West" to BCRegion.SOUTH,
                "Penticton" to BCRegion.SOUTH,
                "Shuswap" to BCRegion.SOUTH,
                "Vernon-Monashee" to BCRegion.SOUTH,
                "Cariboo-Chilcotin" to BCRegion.NORTH,
                "Cariboo North" to BCRegion.NORTH,
                "Fraser-Nicola" to BCRegion.NORTH,
                "Kamloops-North Thompson" to BCRegion.NORTH,
                "Kamloops-South Thompson" to BCRegion.NORTH,
                "Abbotsford-Mission" to BCRegion.SOUTH,
                "Abbotsford South" to BCRegion.SOUTH,
                "Abbotsford West" to BCRegion.SOUTH,
                "Chilliwack" to BCRegion.SOUTH,
                "Chilliwack-Kent" to BCRegion.SOUTH,
                "Langley" to BCRegion.SOUTH,
                "Langley East" to BCRegion.SOUTH,
                "Maple Ridge-Mission" to BCRegion.SOUTH,
                "Maple Ridge-Pitt Meadows" to BCRegion.SOUTH,
                "Surrey-Cloverdale" to BCRegion.VANCOUVER,
                "Surrey-Fleetwood" to BCRegion.VANCOUVER,
                "Surrey-Green Timbers" to BCRegion.VANCOUVER,
                "Surrey-Guildford" to BCRegion.VANCOUVER,
                "Surrey-Newton" to BCRegion.VANCOUVER,
                "Surrey-Panorama" to BCRegion.VANCOUVER,
                "Surrey South" to BCRegion.VANCOUVER,
                "Surrey-Whalley" to BCRegion.VANCOUVER,
                "Surrey-White Rock" to BCRegion.VANCOUVER,
                "Delta North" to BCRegion.VANCOUVER,
                "Delta South" to BCRegion.VANCOUVER,
                "Richmond North Centre" to BCRegion.VANCOUVER,
                "Richmond-Queensborough" to BCRegion.VANCOUVER,
                "Richmond South Centre" to BCRegion.VANCOUVER,
                "Richmond-Steveston" to BCRegion.VANCOUVER,
                "Burnaby-Deer Lake" to BCRegion.VANCOUVER,
                "Burnaby-Edmonds" to BCRegion.VANCOUVER,
                "Burnaby-Lougheed" to BCRegion.VANCOUVER,
                "Burnaby North" to BCRegion.VANCOUVER,
                "Coquitlam-Burke Mountain" to BCRegion.VANCOUVER,
                "Coquitlam-Maillardville" to BCRegion.VANCOUVER,
                "New Westminster" to BCRegion.VANCOUVER,
                "Port Coquitlam" to BCRegion.VANCOUVER,
                "Port Moody-Coquitlam" to BCRegion.VANCOUVER,
                "Vancouver-Fairview" to BCRegion.VANCOUVER,
                "Vancouver-False Creek" to BCRegion.VANCOUVER,
                "Vancouver-Fraserview" to BCRegion.VANCOUVER,
                "Vancouver-Hastings" to BCRegion.VANCOUVER,
                "Vancouver-Kensington" to BCRegion.VANCOUVER,
                "Vancouver-Kingsway" to BCRegion.VANCOUVER,
                "Vancouver-Langara" to BCRegion.VANCOUVER,
                "Vancouver-Mount Pleasant" to BCRegion.VANCOUVER,
                "Vancouver-Point Grey" to BCRegion.VANCOUVER,
                "Vancouver-Quilchena" to BCRegion.VANCOUVER,
                "Vancouver-West End" to BCRegion.VANCOUVER,
                "North Vancouver-Lonsdale" to BCRegion.VANCOUVER,
                "North Vancouver-Seymour" to BCRegion.VANCOUVER,
                "Powell River-Sunshine Coast" to BCRegion.VANCOUVER,
                "West Vancouver-Capilano" to BCRegion.VANCOUVER,
                "West Vancouver-Sea to Sky" to BCRegion.VANCOUVER,
                "Courtenay-Comox" to BCRegion.ISLAND,
                "Cowichan Valley" to BCRegion.ISLAND,
                "Mid Island-Pacific Rim" to BCRegion.ISLAND,
                "Nanaimo" to BCRegion.ISLAND,
                "Nanaimo-North Cowichan" to BCRegion.ISLAND,
                "North Island" to BCRegion.ISLAND,
                "Parksville-Qualicum" to BCRegion.ISLAND,
                "Esquimalt-Metchosin" to BCRegion.ISLAND,
                "Langford-Juan de Fuca" to BCRegion.ISLAND,
                "Oak Bay-Gordon Head" to BCRegion.ISLAND,
                "Sannich North and the Islands" to BCRegion.ISLAND,
                "Saanich South" to BCRegion.ISLAND,
                "Victoria-Beacon Hill" to BCRegion.ISLAND,
                "Victoria-Swan Lake" to BCRegion.ISLAND,
            )
    }
}
