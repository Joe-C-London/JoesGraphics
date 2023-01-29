package com.joecollins.graphics.screens.generic

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
            prevResult,
            currResult,
            { it.uppercase() },
            "ALL SEATS".asOneTimePublisher(),
        )
            .withNumRows(numRows)
            .build(title)
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
            prevResult,
            currResult,
            { it.uppercase() },
            "ALL SEATS".asOneTimePublisher(),
        )
            .withNumRows(numRows)
            .withSeatFilter(filteredSeats)
            .build(title)
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
            prevResult,
            currResult,
            { it.uppercase() },
            "ALL SEATS".asOneTimePublisher(),
        )
            .withNumRows(numRows)
            .build(title)
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
            prevResult,
            currResult,
            { it.uppercase() },
            "ALL SEATS".asOneTimePublisher(),
        )
            .withNumRows(numRows)
            .withPartyChanges(mapOf(lib to bcu).asOneTimePublisher())
            .build(title)
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
                if (it.value.party == lib) {
                    PartyResult(bcu, it.value.isElected)
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

    companion object {
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
        private val ind = Party("Independent", "IND", Color.GRAY)
        private val oth = Party.OTHERS
        private fun bcPrevResult(): Map<String, Party> {
            return mapOf(
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
        }

        private fun bcCurrResult(): Map<String, PartyResult> {
            return mapOf(
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
            ).mapValues { it.value!! }
        }
    }
}
