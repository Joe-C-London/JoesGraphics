package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color

class PartyHeatMapScreenTest {

    @Test
    fun testElected() {
        val prevResults = bcPrevResult()
        val currResults = Publisher(emptyMap<String, Party?>())
        val parties = Publisher(listOf(lib, ndp))
        val rows = Publisher(4)
        val panel = PartyHeatMapScreen.ofElected(
            prevResults.keys.asOneTimePublisher(),
            parties,
            { riding ->
                prevResults[riding]!!.maxBy { it.value }.key
            },
            { riding ->
                currResults.map { r -> r[riding] }
            },
        ) { riding, party ->
            val result = prevResults[riding]!!
            val me = result[party] ?: 0
            val oth = result.filter { it.key != party }.maxOf { it.value }
            val total = result.values.sum().toDouble()
            (oth - me) / total
        }
            .withNumRows(rows)
            .build("PARTY HEAT MAPS".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("PartyHeatMapScreen", "Elected-1", panel)
        assertPublishes(
            panel.altText,
            """
            PARTY HEAT MAPS
            
            LIBERAL
            0 (44 FOR MAJORITY)
            ±0 (+1 FOR MAJORITY)
            BALANCE: 0-87
            
            NEW DEMOCRATIC PARTY
            0 (44 FOR MAJORITY)
            ±0 (+3 FOR MAJORITY)
            BALANCE: 0-87
            """.trimIndent(),
        )

        currResults.submit(
            mapOf(
                "Coquitlam-Burke Mountain" to ndp,
            ),
        )
        compareRendering("PartyHeatMapScreen", "Elected-2", panel)
        assertPublishes(
            panel.altText,
            """
            PARTY HEAT MAPS
            
            LIBERAL
            0 (44 FOR MAJORITY)
            -1 (+1 FOR MAJORITY)
            BALANCE: 0-42
            
            NEW DEMOCRATIC PARTY
            1 (44 FOR MAJORITY)
            +1 (+3 FOR MAJORITY)
            BALANCE: 42-87
            """.trimIndent(),
        )

        currResults.submit(
            bcCurrResult().mapValues { r -> r.value?.let { if (it.elected) it.party else null } },
        )
        compareRendering("PartyHeatMapScreen", "Elected-3", panel)
        assertPublishes(
            panel.altText,
            """
            PARTY HEAT MAPS
            
            LIBERAL
            16 (44 FOR MAJORITY)
            -4 (+1 FOR MAJORITY)
            BALANCE: 36-36
            
            NEW DEMOCRATIC PARTY
            44 (44 FOR MAJORITY)
            +5 (+3 FOR MAJORITY)
            BALANCE: 49-54
            """.trimIndent(),
        )

        currResults.submit(
            bcCurrResult().mapValues { r -> r.value?.party },
        )
        compareRendering("PartyHeatMapScreen", "Elected-4", panel)
        assertPublishes(
            panel.altText,
            """
            PARTY HEAT MAPS
            
            LIBERAL
            28 (44 FOR MAJORITY)
            -15 (+1 FOR MAJORITY)
            BALANCE: 26-30
            
            NEW DEMOCRATIC PARTY
            57 (44 FOR MAJORITY)
            +16 (+3 FOR MAJORITY)
            BALANCE: 56-58
            """.trimIndent(),
        )
    }

    @Test
    fun testElectedLeading() {
        val prevResults = bcPrevResult()
        val currResults = Publisher(emptyMap<String, PartyResult?>())
        val parties = Publisher(listOf(lib, ndp))
        val rows = Publisher(4)
        val panel = PartyHeatMapScreen.ofElectedLeading(
            prevResults.keys.asOneTimePublisher(),
            parties,
            { riding ->
                prevResults[riding]!!.maxBy { it.value }.key
            },
            { riding ->
                currResults.map { r -> r[riding] }
            },
        ) { riding, party ->
            val result = prevResults[riding]!!
            val me = result[party] ?: 0
            val oth = result.filter { it.key != party }.maxOf { it.value }
            val total = result.values.sum().toDouble()
            (oth - me) / total
        }
            .withNumRows(rows)
            .build("PARTY HEAT MAPS".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("PartyHeatMapScreen", "ElectedLeading-1", panel)
        assertPublishes(
            panel.altText,
            """
            PARTY HEAT MAPS
            
            LIBERAL
            0/0 (44 FOR MAJORITY)
            ±0/±0 (+1 FOR MAJORITY)
            BALANCE: 0-87
            
            NEW DEMOCRATIC PARTY
            0/0 (44 FOR MAJORITY)
            ±0/±0 (+3 FOR MAJORITY)
            BALANCE: 0-87
            """.trimIndent(),
        )

        currResults.submit(
            mapOf(
                "Coquitlam-Burke Mountain" to PartyResult.elected(ndp),
                "Fraser-Nicola" to PartyResult.leading(lib),
                "Richmond-Queensborough" to PartyResult.leading(ndp),
                "Vancouver-False Creek" to PartyResult.leading(ndp),
            ),
        )
        compareRendering("PartyHeatMapScreen", "ElectedLeading-2", panel)
        assertPublishes(
            panel.altText,
            """
            PARTY HEAT MAPS
            
            LIBERAL
            0/1 (44 FOR MAJORITY)
            -1/-3 (+1 FOR MAJORITY)
            BALANCE: 40-40
            
            NEW DEMOCRATIC PARTY
            1/3 (44 FOR MAJORITY)
            +1/+3 (+3 FOR MAJORITY)
            BALANCE: 44-44
            """.trimIndent(),
        )

        currResults.submit(bcCurrResult())
        compareRendering("PartyHeatMapScreen", "ElectedLeading-3", panel)
        assertPublishes(
            panel.altText,
            """
            PARTY HEAT MAPS
            
            LIBERAL
            16/28 (44 FOR MAJORITY)
            -4/-15 (+1 FOR MAJORITY)
            BALANCE: 26-30
            
            NEW DEMOCRATIC PARTY
            44/57 (44 FOR MAJORITY)
            +5/+16 (+3 FOR MAJORITY)
            BALANCE: 56-58
            """.trimIndent(),
        )

        parties.submit(listOf(lib, ndp, grn))
        rows.submit(2)
        compareRendering("PartyHeatMapScreen", "ElectedLeading-4", panel)
        assertPublishes(
            panel.altText,
            """
            PARTY HEAT MAPS
            
            LIBERAL
            16/28 (44 FOR MAJORITY)
            -4/-15 (+1 FOR MAJORITY)
            BALANCE: 26-30
            
            NEW DEMOCRATIC PARTY
            44/57 (44 FOR MAJORITY)
            +5/+16 (+3 FOR MAJORITY)
            BALANCE: 56-58
            
            GREEN
            1/2 (44 FOR MAJORITY)
            -1/-1 (+41 FOR MAJORITY)
            BALANCE: 2-3
            """.trimIndent(),
        )
    }

    @Test
    fun testPartyChanges() {
        val prevResults = bcPrevResult()
        val currResults = Publisher(emptyMap<String, PartyResult?>())
        val parties = Publisher(listOf(bcu, ndp))
        val rows = Publisher(4)
        val panel = PartyHeatMapScreen.ofElectedLeading(
            prevResults.keys.asOneTimePublisher(),
            parties,
            { riding ->
                prevResults[riding]!!.maxBy { it.value }.key
            },
            { riding ->
                currResults.map { r -> r[riding] }
            },
        ) { riding, party ->
            val result = prevResults[riding]!!
            val me = result[if (party == bcu) lib else party] ?: 0
            val oth = result.filter { it.key != (if (party == bcu) lib else party) }.maxOf { it.value }
            val total = result.values.sum().toDouble()
            (oth - me) / total
        }
            .withNumRows(rows)
            .withPartyChanges(mapOf(lib to bcu).asOneTimePublisher())
            .build("PARTY HEAT MAPS".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("PartyHeatMapScreen", "PartyChanges-1", panel)
        assertPublishes(
            panel.altText,
            """
            PARTY HEAT MAPS
            
            BC UNITED
            0/0 (44 FOR MAJORITY)
            ±0/±0 (+1 FOR MAJORITY)
            BALANCE: 0-87
            
            NEW DEMOCRATIC PARTY
            0/0 (44 FOR MAJORITY)
            ±0/±0 (+3 FOR MAJORITY)
            BALANCE: 0-87
            """.trimIndent(),
        )

        currResults.submit(
            mapOf(
                "Coquitlam-Burke Mountain" to PartyResult.elected(ndp),
                "Fraser-Nicola" to PartyResult.leading(bcu),
                "Richmond-Queensborough" to PartyResult.leading(ndp),
                "Vancouver-False Creek" to PartyResult.leading(ndp),
            ),
        )
        compareRendering("PartyHeatMapScreen", "PartyChanges-2", panel)
        assertPublishes(
            panel.altText,
            """
            PARTY HEAT MAPS
            
            BC UNITED
            0/1 (44 FOR MAJORITY)
            -1/-3 (+1 FOR MAJORITY)
            BALANCE: 40-40
            
            NEW DEMOCRATIC PARTY
            1/3 (44 FOR MAJORITY)
            +1/+3 (+3 FOR MAJORITY)
            BALANCE: 44-44
            """.trimIndent(),
        )

        currResults.submit(
            bcCurrResult().mapValues {
                if (it.value?.party == lib) {
                    PartyResult(bcu, it.value!!.elected)
                } else {
                    it.value
                }
            },
        )
        compareRendering("PartyHeatMapScreen", "PartyChanges-3", panel)
        assertPublishes(
            panel.altText,
            """
            PARTY HEAT MAPS
            
            BC UNITED
            16/28 (44 FOR MAJORITY)
            -4/-15 (+1 FOR MAJORITY)
            BALANCE: 26-30
            
            NEW DEMOCRATIC PARTY
            44/57 (44 FOR MAJORITY)
            +5/+16 (+3 FOR MAJORITY)
            BALANCE: 56-58
            """.trimIndent(),
        )

        parties.submit(listOf(bcu, ndp, grn))
        rows.submit(2)
        compareRendering("PartyHeatMapScreen", "PartyChanges-4", panel)
        assertPublishes(
            panel.altText,
            """
            PARTY HEAT MAPS
            
            BC UNITED
            16/28 (44 FOR MAJORITY)
            -4/-15 (+1 FOR MAJORITY)
            BALANCE: 26-30
            
            NEW DEMOCRATIC PARTY
            44/57 (44 FOR MAJORITY)
            +5/+16 (+3 FOR MAJORITY)
            BALANCE: 56-58
            
            GREEN
            1/2 (44 FOR MAJORITY)
            -1/-1 (+41 FOR MAJORITY)
            BALANCE: 2-3
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
        private fun bcPrevResult(): Map<String, Map<Party, Int>> {
            return mapOf(
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
        }

        private fun bcCurrResult(): Map<String, PartyResult?> {
            return mapOf(
                "Nechako Lakes" to PartyResult.elected(lib),
                "North Coast" to PartyResult.elected(ndp),
                "Peace River North" to PartyResult.elected(lib),
                "Peace River South" to PartyResult.elected(lib),
                "Prince George-Mackenzie" to PartyResult.elected(lib),
                "Prince George-Valemount" to PartyResult.elected(lib),
                "Skeena" to PartyResult.elected(lib),
                "Stikine" to PartyResult.elected(ndp),
                "Columbia River-Revelstoke" to PartyResult.leading(lib),
                "Kootenay East" to PartyResult.elected(lib),
                "Kootenay West" to PartyResult.elected(ndp),
                "Nelson-Creston" to PartyResult.leading(ndp),
                "Boundary-Similkameen" to PartyResult.leading(ndp),
                "Kelowna-Lake Country" to PartyResult.elected(lib),
                "Kelowna-Mission" to PartyResult.elected(lib),
                "Kelowna West" to PartyResult.leading(lib),
                "Penticton" to PartyResult.leading(lib),
                "Shuswap" to PartyResult.elected(lib),
                "Vernon-Monashee" to PartyResult.leading(ndp),
                "Cariboo-Chilcotin" to PartyResult.elected(lib),
                "Cariboo North" to PartyResult.leading(lib),
                "Fraser-Nicola" to PartyResult.leading(lib),
                "Kamloops-North Thompson" to PartyResult.leading(lib),
                "Kamloops-South Thompson" to PartyResult.elected(lib),
                "Abbotsford-Mission" to PartyResult.leading(ndp),
                "Abbotsford South" to PartyResult.leading(lib),
                "Abbotsford West" to PartyResult.leading(lib),
                "Chilliwack" to PartyResult.leading(ndp),
                "Chilliwack-Kent" to PartyResult.leading(ndp),
                "Langley" to PartyResult.leading(ndp),
                "Langley East" to PartyResult.leading(ndp),
                "Maple Ridge-Mission" to PartyResult.elected(ndp),
                "Maple Ridge-Pitt Meadows" to PartyResult.elected(ndp),
                "Surrey-Cloverdale" to PartyResult.elected(ndp),
                "Surrey-Fleetwood" to PartyResult.elected(ndp),
                "Surrey-Green Timbers" to PartyResult.elected(ndp),
                "Surrey-Guildford" to PartyResult.elected(ndp),
                "Surrey-Newton" to PartyResult.elected(ndp),
                "Surrey-Panorama" to PartyResult.elected(ndp),
                "Surrey South" to PartyResult.leading(lib),
                "Surrey-Whalley" to PartyResult.elected(ndp),
                "Surrey-White Rock" to PartyResult.leading(lib),
                "Delta North" to PartyResult.elected(ndp),
                "Delta South" to PartyResult.elected(lib),
                "Richmond North Centre" to PartyResult.elected(lib),
                "Richmond-Queensborough" to PartyResult.leading(ndp),
                "Richmond South Centre" to PartyResult.elected(ndp),
                "Richmond-Steveston" to PartyResult.elected(ndp),
                "Burnaby-Deer Lake" to PartyResult.elected(ndp),
                "Burnaby-Edmonds" to PartyResult.elected(ndp),
                "Burnaby-Lougheed" to PartyResult.elected(ndp),
                "Burnaby North" to PartyResult.elected(ndp),
                "Coquitlam-Burke Mountain" to PartyResult.elected(ndp),
                "Coquitlam-Maillardville" to PartyResult.elected(ndp),
                "New Westminster" to PartyResult.elected(ndp),
                "Port Coquitlam" to PartyResult.elected(ndp),
                "Port Moody-Coquitlam" to PartyResult.elected(ndp),
                "Vancouver-Fairview" to PartyResult.elected(ndp),
                "Vancouver-False Creek" to PartyResult.leading(ndp),
                "Vancouver-Fraserview" to PartyResult.elected(ndp),
                "Vancouver-Hastings" to PartyResult.elected(ndp),
                "Vancouver-Kensington" to PartyResult.elected(ndp),
                "Vancouver-Kingsway" to PartyResult.elected(ndp),
                "Vancouver-Langara" to PartyResult.leading(lib),
                "Vancouver-Mount Pleasant" to PartyResult.elected(ndp),
                "Vancouver-Point Grey" to PartyResult.elected(ndp),
                "Vancouver-Quilchena" to PartyResult.elected(lib),
                "Vancouver-West End" to PartyResult.elected(ndp),
                "North Vancouver-Lonsdale" to PartyResult.elected(ndp),
                "North Vancouver-Seymour" to PartyResult.leading(ndp),
                "Powell River-Sunshine Coast" to PartyResult.elected(ndp),
                "West Vancouver-Capilano" to PartyResult.elected(lib),
                "West Vancouver-Sea to Sky" to PartyResult.leading(lib),
                "Courtenay-Comox" to PartyResult.elected(ndp),
                "Cowichan Valley" to PartyResult.leading(grn),
                "Mid Island-Pacific Rim" to PartyResult.elected(ndp),
                "Nanaimo" to PartyResult.elected(ndp),
                "Nanaimo-North Cowichan" to PartyResult.leading(ndp),
                "North Island" to PartyResult.elected(ndp),
                "Parksville-Qualicum" to PartyResult.leading(ndp),
                "Esquimalt-Metchosin" to PartyResult.elected(ndp),
                "Langford-Juan de Fuca" to PartyResult.elected(ndp),
                "Oak Bay-Gordon Head" to PartyResult.elected(ndp),
                "Sannich North and the Islands" to PartyResult.elected(grn),
                "Saanich South" to PartyResult.elected(ndp),
                "Victoria-Beacon Hill" to PartyResult.elected(ndp),
                "Victoria-Swan Lake" to PartyResult.elected(ndp),
            )
        }
    }
}
