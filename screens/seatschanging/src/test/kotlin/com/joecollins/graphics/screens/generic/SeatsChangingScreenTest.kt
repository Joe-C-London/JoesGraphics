package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PartyResult.Companion.elected
import com.joecollins.models.general.PartyResult.Companion.leading
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color

class SeatsChangingScreenTest {
    @Test
    fun testSeatsChanging() {
        val prevResult = Publisher(bcPrevResult())
        val prevWinners = Publisher(bcPrevWinners())
        val currResult = Publisher<Map<String, PartyResult?>>(emptyMap())
        val numRows = Publisher(15)
        val title = Publisher("BRITISH COLUMBIA")
        val panel = SeatsChangingScreen.of(
            prevResult = prevResult,
            prevWinner = prevWinners,
            currResult = currResult,
            name = { uppercase() },
            header = "SEATS CHANGING".asOneTimePublisher(),
            numRows = numRows,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SeatsChangingScreen", "Basic-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            BRITISH COLUMBIA

            SEATS CHANGING
            (empty)
            """.trimIndent(),
        )

        currResult.submit(
            mapOf(
                "Coquitlam-Burke Mountain" to leading(ndp),
                "Fraser-Nicola" to leading(ndp),
                "Vancouver-False Creek" to leading(ndp),
            ),
        )
        compareRendering("SeatsChangingScreen", "Basic-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            BRITISH COLUMBIA
            
            SEATS CHANGING
            NDP GAINS FROM LIB: 0/3
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
        compareRendering("SeatsChangingScreen", "Basic-3", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            BRITISH COLUMBIA
            
            SEATS CHANGING
            NDP GAINS FROM LIB: 1/3
            """.trimIndent(),
        )

        currResult.submit(bcCurrResult())
        compareRendering("SeatsChangingScreen", "Basic-4", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            BRITISH COLUMBIA
            
            SEATS CHANGING
            NDP GAINS FROM LIB: 4/15
            NDP GAINS FROM GRN: 1/1
            """.trimIndent(),
        )
    }

    @Test
    fun testFilteredSeatsChanging() {
        val prevResult = Publisher(bcPrevResult())
        val prevWinners = Publisher(bcPrevWinners())
        val currResult = Publisher<Map<String, PartyResult?>>(emptyMap())
        val numRows = Publisher(15)
        val title = Publisher("VANCOUVER")
        val filteredSeats = Publisher(
            bcPrevResult().keys
                .filter { it.startsWith("Vancouver") }
                .toSet(),
        )
        val panel = SeatsChangingScreen.of(
            prevResult = prevResult,
            prevWinner = prevWinners,
            currResult = currResult,
            name = { uppercase() },
            header = "SEATS CHANGING".asOneTimePublisher(),
            numRows = numRows,
            seatFilter = filteredSeats,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SeatsChangingScreen", "Filtered-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            VANCOUVER

            SEATS CHANGING
            (empty)
            """.trimIndent(),
        )

        currResult.submit(
            mapOf(
                "Coquitlam-Burke Mountain" to leading(ndp),
                "Fraser-Nicola" to leading(ndp),
                "Vancouver-False Creek" to leading(ndp),
            ),
        )
        compareRendering("SeatsChangingScreen", "Filtered-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            VANCOUVER

            SEATS CHANGING
            NDP GAINS FROM LIB: 0/1
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
        compareRendering("SeatsChangingScreen", "Filtered-3", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            VANCOUVER

            SEATS CHANGING
            NDP GAINS FROM LIB: 0/1
            """.trimIndent(),
        )

        currResult.submit(bcCurrResult())
        compareRendering("SeatsChangingScreen", "Filtered-4", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            VANCOUVER

            SEATS CHANGING
            NDP GAINS FROM LIB: 0/1
            """.trimIndent(),
        )
    }

    @Test
    fun testSeatsChangingNullResults() {
        val prevResult = Publisher(bcPrevResult())
        val prevWinners = Publisher(bcPrevWinners())
        val results = bcCurrResult().mapValues { null }
        val currResult = Publisher<Map<String, PartyResult?>>(results)
        val numRows = Publisher(15)
        val title = Publisher("BRITISH COLUMBIA")
        val panel = SeatsChangingScreen.of(
            prevResult = prevResult,
            prevWinner = prevWinners,
            currResult = currResult,
            name = { uppercase() },
            header = "SEATS CHANGING".asOneTimePublisher(),
            numRows = numRows,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SeatsChangingScreen", "Basic-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            BRITISH COLUMBIA

            SEATS CHANGING
            (empty)
            """.trimIndent(),
        )

        currResult.submit(bcCurrResult())
        compareRendering("SeatsChangingScreen", "Basic-4", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            BRITISH COLUMBIA
            
            SEATS CHANGING
            NDP GAINS FROM LIB: 4/15
            NDP GAINS FROM GRN: 1/1
            """.trimIndent(),
        )
    }

    @Test
    fun testSeatsChangingWithPartyChanges() {
        val prevResult = Publisher(bcPrevResult())
        val prevWinners = Publisher(bcPrevWinners())
        val currResult = Publisher<Map<String, PartyResult?>>(emptyMap())
        val numRows = Publisher(15)
        val title = Publisher("BRITISH COLUMBIA")
        val panel = SeatsChangingScreen.of(
            prevResult = prevResult,
            prevWinner = prevWinners,
            currResult = currResult,
            name = { uppercase() },
            header = "SEATS CHANGING".asOneTimePublisher(),
            numRows = numRows,
            partyChanges = mapOf(lib to bcu).asOneTimePublisher(),
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SeatsChangingScreen", "PartyChanges-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            BRITISH COLUMBIA

            SEATS CHANGING
            (empty)
            """.trimIndent(),
        )

        currResult.submit(
            mapOf(
                "Coquitlam-Burke Mountain" to leading(ndp),
                "Fraser-Nicola" to leading(ndp),
                "Vancouver-False Creek" to leading(ndp),
            ),
        )
        compareRendering("SeatsChangingScreen", "PartyChanges-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            BRITISH COLUMBIA

            SEATS CHANGING
            NDP GAINS FROM LIB: 0/3
            """.trimIndent(),
        )

        currResult.submit(
            mapOf(
                "Coquitlam-Burke Mountain" to elected(ndp),
                "Fraser-Nicola" to leading(bcu),
                "Richmond-Queensborough" to leading(ndp),
                "Vancouver-False Creek" to leading(ndp),
            ),
        )
        compareRendering("SeatsChangingScreen", "PartyChanges-3", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            BRITISH COLUMBIA

            SEATS CHANGING
            NDP GAINS FROM LIB: 1/3
            """.trimIndent(),
        )

        currResult.submit(
            bcCurrResult().mapValues {
                if (it.value?.leader == lib) {
                    PartyResult(bcu, it.value!!.elected)
                } else {
                    it.value
                }
            },
        )
        compareRendering("SeatsChangingScreen", "PartyChanges-4", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            BRITISH COLUMBIA

            SEATS CHANGING
            NDP GAINS FROM LIB: 4/15
            NDP GAINS FROM GRN: 1/1
            """.trimIndent(),
        )
    }

    companion object {
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val bcu = Party("BC United", "BCU", Color.BLUE)
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
        private val ind = Party("Independent", "IND", Color.GRAY)
        private val oth = Party.OTHERS
        private fun bcPrevResult(): Map<String, Map<Party, Int>> = mapOf(
            "Nechako Lakes" to mapOf(lib to 5307, ndp to 2909, grn to 878, oth to 438 + 226),
            "North Coast" to mapOf(lib to 3079, ndp to 5243, grn to 826),
            "Peace River North" to mapOf(lib to 9707, ndp to 973, oth to 2799 + 884 + 275),
            "Peace River South" to mapOf(lib to 6634, ndp to 2102),
            "Prince George-Mackenzie" to mapOf(lib to 10725, ndp to 5942, grn to 2109),
            "Prince George-Valemount" to mapOf(lib to 11189, ndp to 5683, grn to 2353),
            "Skeena" to mapOf(lib to 6772, ndp to 5613, oth to 580),
            "Stikine" to mapOf(lib to 3531, ndp to 4748, oth to 834),
            "Columbia River-Revelstoke" to mapOf(lib to 6620, ndp to 5248, grn to 1708, oth to 469 + 371 + 154),
            "Kootenay East" to mapOf(lib to 9666, ndp to 5069, grn to 1926, oth to 425),
            "Kootenay West" to mapOf(lib to 4547, ndp to 11164, grn to 2976),
            "Nelson-Creston" to mapOf(lib to 5087, ndp to 7685, grn to 5130, oth to 164 + 149),
            "Boundary-Similkameen" to mapOf(lib to 9513, ndp to 7275, grn to 2274, oth to 3165),
            "Kelowna-Lake Country" to mapOf(lib to 15286, ndp to 5345, grn to 4951),
            "Kelowna-Mission" to mapOf(lib to 15399, ndp to 5720, grn to 3836, oth to 1976),
            "Kelowna West" to mapOf(lib to 15674, ndp to 6672, grn to 3628, oth to 570),
            "Penticton" to mapOf(lib to 14470, ndp to 7874, grn to 5061),
            "Shuswap" to mapOf(lib to 14829, ndp to 7161, grn to 4175, oth to 410),
            "Vernon-Monashee" to mapOf(lib to 13625, ndp to 8355, grn to 6139, oth to 341),
            "Cariboo-Chilcotin" to mapOf(lib to 8520, ndp to 3801, grn to 2174),
            "Cariboo North" to mapOf(lib to 6359, ndp to 4430, grn to 919, oth to 747),
            "Fraser-Nicola" to mapOf(lib to 6597, ndp to 6005, grn to 2517, oth to 598),
            "Kamloops-North Thompson" to mapOf(lib to 12001, ndp to 7538, grn to 5511, oth to 187),
            "Kamloops-South Thompson" to mapOf(lib to 15465, ndp to 6072, grn to 5785, oth to 295 + 109),
            "Abbotsford-Mission" to mapOf(lib to 12879, ndp to 7339, grn to 4298, oth to 644),
            "Abbotsford South" to mapOf(lib to 11673, ndp to 6297, grn to 3338, oth to 942),
            "Abbotsford West" to mapOf(lib to 11618, ndp to 6474, grn to 2280, oth to 516 + 149),
            "Chilliwack" to mapOf(lib to 9180, ndp to 6207, grn to 3277, oth to 402),
            "Chilliwack-Kent" to mapOf(lib to 11814, ndp to 7273, grn to 3335),
            "Langley" to mapOf(lib to 10755, ndp to 8384, grn to 3699, oth to 1221 + 166),
            "Langley East" to mapOf(lib to 16384, ndp to 8820, grn to 4968, oth to 448),
            "Maple Ridge-Mission" to mapOf(lib to 10663, ndp to 10988, grn to 3467, oth to 934 + 145),
            "Maple Ridge-Pitt Meadows" to mapOf(lib to 10428, ndp to 12045, grn to 3329, oth to 676 + 408),
            "Surrey-Cloverdale" to mapOf(lib to 11918, ndp to 9763, grn to 3091, oth to 279),
            "Surrey-Fleetwood" to mapOf(lib to 7413, ndp to 11085, grn to 2190),
            "Surrey-Green Timbers" to mapOf(lib to 5056, ndp to 8945, grn to 1112, oth to 163 + 69),
            "Surrey-Guildford" to mapOf(lib to 7015, ndp to 9262, grn to 1840, oth to 462),
            "Surrey-Newton" to mapOf(lib to 5100, ndp to 9744, grn to 1171, oth to 988),
            "Surrey-Panorama" to mapOf(lib to 10064, ndp to 12226, grn to 1620, oth to 132),
            "Surrey South" to mapOf(lib to 13509, ndp to 8718, grn to 3141, oth to 634 + 311 + 140 + 67),
            "Surrey-Whalley" to mapOf(lib to 5293, ndp to 10315, grn to 1893, oth to 96),
            "Surrey-White Rock" to mapOf(lib to 14101, ndp to 8648, grn to 4574, oth to 950),
            "Delta North" to mapOf(lib to 9319, ndp to 11465, grn to 2697),
            "Delta South" to mapOf(lib to 11123, ndp to 5228, grn to 2349, ind to 6437, oth to 88),
            "Richmond North Centre" to mapOf(lib to 7916, ndp to 5135, grn to 1579, oth to 336 + 117),
            "Richmond-Queensborough" to mapOf(lib to 8218, ndp to 8084, grn to 2524, oth to 694 + 318),
            "Richmond South Centre" to mapOf(lib to 6914, ndp to 5666, grn to 1561),
            "Richmond-Steveston" to mapOf(lib to 10332, ndp to 8524, grn to 2833),
            "Burnaby-Deer Lake" to mapOf(lib to 6491, ndp to 8747, grn to 2209, oth to 589 + 229),
            "Burnaby-Edmonds" to mapOf(lib to 6404, ndp to 10827, grn to 2728),
            "Burnaby-Lougheed" to mapOf(lib to 8391, ndp to 10911, grn to 3127, oth to 145 + 129),
            "Burnaby North" to mapOf(lib to 9290, ndp to 11447, grn to 2830),
            "Coquitlam-Burke Mountain" to mapOf(lib to 10388, ndp to 10301, grn to 2771),
            "Coquitlam-Maillardville" to mapOf(lib to 8519, ndp to 11438, grn to 2467, oth to 175),
            "New Westminster" to mapOf(lib to 5870, ndp to 14377, grn to 6939, oth to 298 + 199),
            "Port Coquitlam" to mapOf(lib to 7582, ndp to 14079, grn to 3237, oth to 248 + 88),
            "Port Moody-Coquitlam" to mapOf(lib to 9910, ndp to 11754, grn to 2985),
            "Vancouver-Fairview" to mapOf(lib to 9436, ndp to 16035, grn to 4007, oth to 149),
            "Vancouver-False Creek" to mapOf(lib to 10370, ndp to 9955, grn to 3880, oth to 213 + 91 + 90),
            "Vancouver-Fraserview" to mapOf(lib to 9985, ndp to 11487, grn to 1826, oth to 179 + 174),
            "Vancouver-Hastings" to mapOf(lib to 5160, ndp to 14351, grn to 4222, oth to 203),
            "Vancouver-Kensington" to mapOf(lib to 7236, ndp to 12504, grn to 2580, oth to 181),
            "Vancouver-Kingsway" to mapOf(lib to 5377, ndp to 12031, grn to 1848, oth to 504 + 85),
            "Vancouver-Langara" to mapOf(lib to 10047, ndp to 8057, grn to 2894, oth to 172),
            "Vancouver-Mount Pleasant" to mapOf(lib to 3917, ndp to 15962, grn to 4136, oth to 212 + 142 + 72),
            "Vancouver-Point Grey" to mapOf(lib to 8414, ndp to 14195, grn to 2604, oth to 84 + 77),
            "Vancouver-Quilchena" to mapOf(lib to 12464, ndp to 6244, grn to 3301, oth to 265),
            "Vancouver-West End" to mapOf(lib to 5064, ndp to 13420, grn to 3059, oth to 352 + 116),
            "North Vancouver-Lonsdale" to mapOf(lib to 10373, ndp to 12361, grn to 4148, oth to 316),
            "North Vancouver-Seymour" to mapOf(lib to 13194, ndp to 9808, grn to 5208, oth to 247),
            "Powell River-Sunshine Coast" to mapOf(lib to 6602, ndp to 13646, grn to 6505, oth to 160),
            "West Vancouver-Capilano" to mapOf(lib to 13596, ndp to 5622, grn to 4575),
            "West Vancouver-Sea to Sky" to mapOf(lib to 10449, ndp to 6532, grn to 6947, oth to 186 + 143),
            "Courtenay-Comox" to mapOf(lib to 10697, ndp to 10886, grn to 5351, oth to 2201),
            "Cowichan Valley" to mapOf(lib to 8400, ndp to 9603, grn to 11475, oth to 502 + 393 + 145 + 124),
            "Mid Island-Pacific Rim" to mapOf(lib to 6578, ndp to 12556, grn to 5206, oth to 878 + 298 + 86),
            "Nanaimo" to mapOf(lib to 8911, ndp to 12746, grn to 5454, oth to 277),
            "Nanaimo-North Cowichan" to mapOf(lib to 7380, ndp to 12275, grn to 6244, oth to 274),
            "North Island" to mapOf(lib to 9148, ndp to 12255, grn to 3846, oth to 543),
            "Parksville-Qualicum" to mapOf(lib to 14468, ndp to 9189, grn to 8157, oth to 245),
            "Esquimalt-Metchosin" to mapOf(lib to 7055, ndp to 11816, grn to 6339, oth to 171 + 102 + 65),
            "Langford-Juan de Fuca" to mapOf(lib to 6544, ndp to 13224, grn to 4795, oth to 262 + 242),
            "Oak Bay-Gordon Head" to mapOf(lib to 6952, ndp to 6912, grn to 15257, oth to 67 + 58),
            "Sannich North and the Islands" to mapOf(lib to 9321, ndp to 10764, grn to 14775, oth to 364),
            "Saanich South" to mapOf(lib to 8716, ndp to 11912, grn to 7129, oth to 177 + 130),
            "Victoria-Beacon Hill" to mapOf(lib to 4689, ndp to 16057, grn to 9194, oth to 190 + 102 + 35),
            "Victoria-Swan Lake" to mapOf(lib to 4005, ndp to 13531, grn to 7491, oth to 207),
        )

        private fun bcPrevWinners(): Map<String, Party> = bcPrevResult().mapValues { e ->
            e.value.maxBy { it.value }.key
        }

        private fun bcCurrResult(): Map<String, PartyResult?> = mapOf(
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
    }
}
