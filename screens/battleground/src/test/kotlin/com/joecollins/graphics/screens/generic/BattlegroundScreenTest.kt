package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.BattlegroundScreen.Companion.convertToPartyOrCandidateForBattleground
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Coalition
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCandidate
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PartyResult.Companion.elected
import com.joecollins.models.general.PartyResult.Companion.leading
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color
import java.text.DecimalFormat
import kotlin.math.abs

class BattlegroundScreenTest {
    @Test
    fun testSinglePartyBattleground() {
        val prevResult = Publisher(bcPrevResult())
        val currResult = Publisher<Map<String, PartyResult>>(emptyMap())
        val party = Publisher(ndp)
        val targetSeats = Publisher(30)
        val defenseSeats = Publisher(15)
        val numRows = Publisher(15)
        val title = Publisher("NDP BATTLEGROUND")
        val panel = BattlegroundScreen.singleParty(
            prevResults = prevResult.convertToPartyOrCandidateForBattleground(),
            currResults = currResult,
            name = { uppercase() },
            party = party,
            seatsToShow = {
                defense = defenseSeats
                target = targetSeats
            },
            numRows = numRows,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("BattlegroundScreen", "Basic-SingleParty-1", panel)
        assertPublishes(
            panel.altText,
            """
            NDP BATTLEGROUND

            NDP TARGET SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 15 PENDING

            NDP DEFENSE SEATS
            COLUMN 1: 15 PENDING
            """.trimIndent(),
        )

        party.submit(lib)
        targetSeats.submit(15)
        defenseSeats.submit(30)
        title.submit("LIBERAL BATTLEGROUND")
        compareRendering("BattlegroundScreen", "Basic-SingleParty-2", panel)
        assertPublishes(
            panel.altText,
            """
            LIBERAL BATTLEGROUND

            LIB TARGET SEATS
            COLUMN 1: 15 PENDING

            LIB DEFENSE SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 15 PENDING
            """.trimIndent(),
        )

        currResult.submit(bcCurrResult())
        compareRendering("BattlegroundScreen", "Basic-SingleParty-3", panel)
        assertPublishes(
            panel.altText,
            """
            LIBERAL BATTLEGROUND

            LIB TARGET SEATS
            COLUMN 1: 14(15) MISSES

            LIB DEFENSE SEATS
            COLUMN 1: 1(6) HOLDS, 4(9) LOSSES
            COLUMN 2: 3(9) HOLDS, 0(6) LOSSES
            """.trimIndent(),
        )

        party.submit(grn)
        targetSeats.submit(30)
        defenseSeats.submit(5)
        title.submit("GREEN BATTLEGROUND")
        compareRendering("BattlegroundScreen", "Basic-SingleParty-4", panel)
        assertPublishes(
            panel.altText,
            """
            GREEN BATTLEGROUND

            GRN TARGET SEATS
            COLUMN 1: 7(15) MISSES
            COLUMN 2: 8(15) MISSES

            GRN DEFENSE SEATS
            COLUMN 1: 1(2) HOLDS, 1(1) LOSSES
            """.trimIndent(),
        )

        party.submit(ndp)
        targetSeats.submit(30)
        defenseSeats.submit(0)
        title.submit("NDP TARGETS")
        compareRendering("BattlegroundScreen", "Basic-SingleParty-5", panel)
        assertPublishes(
            panel.altText,
            """
            NDP TARGETS

            NDP TARGET SEATS
            COLUMN 1: 4(9) GAINS, 2(6) MISSES
            COLUMN 2: 0(5) GAINS, 2(10) MISSES
            """.trimIndent(),
        )

        party.submit(lib)
        targetSeats.submit(0)
        defenseSeats.submit(30)
        title.submit("LIBERAL DEFENSE")
        compareRendering("BattlegroundScreen", "Basic-SingleParty-6", panel)
        assertPublishes(
            panel.altText,
            """
            LIBERAL DEFENSE

            LIB DEFENSE SEATS
            COLUMN 1: 1(6) HOLDS, 4(9) LOSSES
            COLUMN 2: 3(9) HOLDS, 0(6) LOSSES
            """.trimIndent(),
        )
    }

    @Test
    fun testSinglePartyFilteredBattleground() {
        val prevResult = Publisher(bcPrevResult())
        val currResult = Publisher<Map<String, PartyResult?>>(emptyMap())
        val party = Publisher(ndp)
        val targetSeats = Publisher(30)
        val defenseSeats = Publisher(15)
        val numRows = Publisher(15)
        val filteredSeats = Publisher<Set<String>?>(
            bcPrevResult().keys
                .filter { it.startsWith("Vancouver") }
                .toSet(),
        )
        val title = Publisher("NDP BATTLEGROUND")
        val panel = BattlegroundScreen.singleParty(
            prevResults = prevResult.convertToPartyOrCandidateForBattleground(),
            currResults = currResult,
            name = { uppercase() },
            party = party,
            seatsToShow = {
                defense = defenseSeats
                target = targetSeats
            },
            numRows = numRows,
            seatFilter = filteredSeats,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("BattlegroundScreen", "Filtered-SingleParty-1", panel)
        assertPublishes(
            panel.altText,
            """
            NDP BATTLEGROUND

            NDP TARGET SEATS
            COLUMN 1: 2 PENDING

            NDP DEFENSE SEATS
            COLUMN 1: 1 PENDING
            """.trimIndent(),
        )

        currResult.submit(bcCurrResult())
        compareRendering("BattlegroundScreen", "Filtered-SingleParty-2", panel)
        assertPublishes(
            panel.altText,
            """
            NDP BATTLEGROUND

            NDP TARGET SEATS
            COLUMN 1: 0(1) GAINS, 0(1) MISSES

            NDP DEFENSE SEATS
            COLUMN 1: 1(1) HOLDS
            """.trimIndent(),
        )

        filteredSeats.submit(emptySet())
        compareRendering("BattlegroundScreen", "Filtered-SingleParty-3", panel)
        assertPublishes(
            panel.altText,
            """
            NDP BATTLEGROUND
            """.trimIndent(),
        )

        filteredSeats.submit(null)
        compareRendering("BattlegroundScreen", "Filtered-SingleParty-4", panel)
        assertPublishes(
            panel.altText,
            """
            NDP BATTLEGROUND

            NDP TARGET SEATS
            COLUMN 1: 4(9) GAINS, 2(6) MISSES
            COLUMN 2: 0(5) GAINS, 2(10) MISSES

            NDP DEFENSE SEATS
            COLUMN 1: 15(15) HOLDS
            """.trimIndent(),
        )
    }

    @Test
    fun testDoublePartyBattleground() {
        val prevResult = Publisher(bcPrevResult())
        val currResult = Publisher<Map<String, PartyResult>>(emptyMap())
        val parties = Publisher(ndp to lib)
        val rightSeats = Publisher(30)
        val leftSeats = Publisher(15)
        val numRows = Publisher(15)
        val title = Publisher("BATTLEGROUND")
        val panel = BattlegroundScreen.doubleParty(
            prevResults = prevResult.convertToPartyOrCandidateForBattleground(),
            currResults = currResult,
            name = { uppercase() },
            parties = parties,
            seatsToShow = {
                left = leftSeats
                right = rightSeats
            },
            numRows = numRows,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("BattlegroundScreen", "Basic-DoubleParty-1", panel)
        assertPublishes(
            panel.altText,
            """
            BATTLEGROUND
            
            NDP PREVIOUS SEATS
            COLUMN 1: 15 PENDING
            
            LIB PREVIOUS SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 15 PENDING
            """.trimIndent(),
        )

        parties.submit(grn to lib)
        rightSeats.submit(30)
        leftSeats.submit(15)
        compareRendering("BattlegroundScreen", "Basic-DoubleParty-2", panel)
        assertPublishes(
            panel.altText,
            """
            BATTLEGROUND
            
            GRN PREVIOUS SEATS
            COLUMN 1: 3 PENDING
            
            LIB PREVIOUS SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 15 PENDING
            """.trimIndent(),
        )

        currResult.submit(bcCurrResult())
        compareRendering("BattlegroundScreen", "Basic-DoubleParty-3", panel)
        assertPublishes(
            panel.altText,
            """
            BATTLEGROUND
            
            GRN PREVIOUS SEATS
            COLUMN 1: 1(2) HOLDS, 1(1) OTHER LOSSES
            
            LIB PREVIOUS SEATS
            COLUMN 1: 0(6) HOLDS, 1(9) OTHER LOSSES
            COLUMN 2: 6(9) HOLDS, 3(6) OTHER LOSSES
            """.trimIndent(),
        )

        parties.submit(ndp to grn)
        rightSeats.submit(15)
        leftSeats.submit(15)
        compareRendering("BattlegroundScreen", "Basic-DoubleParty-4", panel)
        assertPublishes(
            panel.altText,
            """
            BATTLEGROUND
            
            NDP PREVIOUS SEATS
            COLUMN 1: 13(15) HOLDS
            
            GRN PREVIOUS SEATS
            COLUMN 1: 1(2) HOLDS, 1(1) LOSSES TO NDP
            """.trimIndent(),
        )

        parties.submit(ndp to lib)
        rightSeats.submit(30)
        leftSeats.submit(0)
        compareRendering("BattlegroundScreen", "Basic-DoubleParty-5", panel)
        assertPublishes(
            panel.altText,
            """
            BATTLEGROUND
            
            LIB PREVIOUS SEATS
            COLUMN 1: 1(5) HOLDS, 4(10) LOSSES TO NDP
            COLUMN 2: 3(10) HOLDS, 0(5) LOSSES TO NDP
            """.trimIndent(),
        )

        parties.submit(ndp to lib)
        rightSeats.submit(0)
        leftSeats.submit(30)
        compareRendering("BattlegroundScreen", "Basic-DoubleParty-6", panel)
        assertPublishes(
            panel.altText,
            """
            BATTLEGROUND
            
            NDP PREVIOUS SEATS
            COLUMN 1: 15(15) HOLDS
            COLUMN 2: 13(15) HOLDS
            """.trimIndent(),
        )
    }

    @Test
    fun testDoubleCoalitionBattleground() {
        val coa = Coalition("Governing Coalition", "NDP/GRN", ndp, grn)
        val prevResult = Publisher(bcPrevResult())
        val currResult = Publisher<Map<String, PartyResult>>(emptyMap())
        val rightSeats = Publisher(30)
        val leftSeats = Publisher(30)
        val numRows = Publisher(15)
        val title = Publisher("BATTLEGROUND")
        val panel = BattlegroundScreen.doubleParty(
            prevResults = prevResult.convertToPartyOrCandidateForBattleground(),
            currResults = currResult,
            name = { uppercase() },
            parties = (coa to lib).asOneTimePublisher(),
            seatsToShow = {
                left = leftSeats
                right = rightSeats
            },
            numRows = numRows,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("BattlegroundScreen", "Basic-DoubleCoalition-1", panel)
        assertPublishes(
            panel.altText,
            """
            BATTLEGROUND
            
            NDP/GRN PREVIOUS SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 15 PENDING
            
            LIB PREVIOUS SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 15 PENDING
            """.trimIndent(),
        )
    }

    @Test
    fun testSinglePartyBattlegroundWithPartyChange() {
        val prevResult = Publisher(bcPrevResult())
        val currResult = Publisher<Map<String, PartyResult>>(emptyMap())
        val party = Publisher(ndp)
        val targetSeats = Publisher(30)
        val defenseSeats = Publisher(15)
        val numRows = Publisher(15)
        val title = Publisher("NDP BATTLEGROUND")
        val panel = BattlegroundScreen.singleParty(
            prevResults = prevResult.convertToPartyOrCandidateForBattleground(),
            currResults = currResult,
            name = { uppercase() },
            party = party,
            seatsToShow = {
                defense = defenseSeats
                target = targetSeats
            },
            numRows = numRows,
            partyChanges = mapOf(lib to bcu).asOneTimePublisher(),
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("BattlegroundScreen", "Basic-SingleParty-1", panel)
        assertPublishes(
            panel.altText,
            """
            NDP BATTLEGROUND

            NDP TARGET SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 15 PENDING

            NDP DEFENSE SEATS
            COLUMN 1: 15 PENDING
            """.trimIndent(),
        )

        party.submit(bcu)
        targetSeats.submit(15)
        defenseSeats.submit(30)
        title.submit("BC UNITED BATTLEGROUND")
        compareRendering("BattlegroundScreen", "Basic-SingleParty-PartyChange-2", panel)
        assertPublishes(
            panel.altText,
            """
            BC UNITED BATTLEGROUND

            BCU TARGET SEATS
            COLUMN 1: 15 PENDING

            BCU DEFENSE SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 15 PENDING
            """.trimIndent(),
        )

        currResult.submit(bcCurrResult().mapValues { if (it.value.leader == lib) PartyResult(bcu, it.value.elected) else it.value })
        compareRendering("BattlegroundScreen", "Basic-SingleParty-PartyChange-3", panel)
        assertPublishes(
            panel.altText,
            """
            BC UNITED BATTLEGROUND

            BCU TARGET SEATS
            COLUMN 1: 14(15) MISSES

            BCU DEFENSE SEATS
            COLUMN 1: 1(6) HOLDS, 4(9) LOSSES
            COLUMN 2: 3(9) HOLDS, 0(6) LOSSES
            """.trimIndent(),
        )

        party.submit(grn)
        targetSeats.submit(30)
        defenseSeats.submit(5)
        title.submit("GREEN BATTLEGROUND")
        compareRendering("BattlegroundScreen", "Basic-SingleParty-PartyChange-4", panel)
        assertPublishes(
            panel.altText,
            """
            GREEN BATTLEGROUND

            GRN TARGET SEATS
            COLUMN 1: 7(15) MISSES
            COLUMN 2: 8(15) MISSES

            GRN DEFENSE SEATS
            COLUMN 1: 1(2) HOLDS, 1(1) LOSSES
            """.trimIndent(),
        )

        party.submit(ndp)
        targetSeats.submit(30)
        defenseSeats.submit(0)
        title.submit("NDP TARGETS")
        compareRendering("BattlegroundScreen", "Basic-SingleParty-PartyChange-5", panel)
        assertPublishes(
            panel.altText,
            """
            NDP TARGETS

            NDP TARGET SEATS
            COLUMN 1: 4(9) GAINS, 2(6) MISSES
            COLUMN 2: 0(5) GAINS, 2(10) MISSES
            """.trimIndent(),
        )

        party.submit(bcu)
        targetSeats.submit(0)
        defenseSeats.submit(30)
        title.submit("BC UNITED DEFENSE")
        compareRendering("BattlegroundScreen", "Basic-SingleParty-PartyChange-6", panel)
        assertPublishes(
            panel.altText,
            """
            BC UNITED DEFENSE

            BCU DEFENSE SEATS
            COLUMN 1: 1(6) HOLDS, 4(9) LOSSES
            COLUMN 2: 3(9) HOLDS, 0(6) LOSSES
            """.trimIndent(),
        )
    }

    @Test
    fun testDoublePartyBattlegroundPreferences() {
        val alp = Party("Labor", "ALP", Color.RED)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val ta = Party("Territory Alliance", "TA", Color.BLUE.darker())
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        val prevResults = listOf(
            "Arafura" to mapOf(alp to 1388, clp to 1203),
            "Araluen" to mapOf(ta to 2203, clp to 2161),
            "Arnhem" to mapOf(alp to 1504, ind to 1420),
            "Barkly" to mapOf(clp to 1723, alp to 1718),
            "Blain" to mapOf(alp to 2095, clp to 2082),
            "Braitling" to mapOf(clp to 2256, alp to 2139),
            "Brennan" to mapOf(clp to 2242, alp to 2138),
            "Casuarina" to mapOf(alp to 3033, clp to 1568),
            "Daly" to mapOf(clp to 1984, alp to 1890),
            "Drysdale" to mapOf(alp to 2263, clp to 1642),
            "Fannie Bay" to mapOf(alp to 2589, clp to 1756),
            "Fong Lim" to mapOf(alp to 2197, clp to 1978),
            "Goyder" to mapOf(ind to 2665, clp to 2030),
            "Gwoja" to mapOf(alp to 1729, clp to 929),
            "Johnston" to mapOf(alp to 2851, clp to 1433),
            "Karama" to mapOf(alp to 2491, clp to 1678),
            "Katherine" to mapOf(clp to 2041, alp to 1845),
            "Mulka" to mapOf(ind to 2252, alp to 1843),
            "Namatjira" to mapOf(clp to 1814, alp to 1792),
            "Nelson" to mapOf(clp to 2657, ind to 1904),
            "Nightcliff" to mapOf(alp to 3286, clp to 1139),
            "Port Darwin" to mapOf(alp to 2233, clp to 2068),
            "Sanderson" to mapOf(alp to 3044, clp to 1351),
            "Spillett" to mapOf(clp to 3219, alp to 1730),
            "Wanguri" to mapOf(alp to 3349, clp to 1627),
        )
        val panel = BattlegroundScreen.doubleParty(
            prevResults = prevResults.associateWith { it.second }.asOneTimePublisher().convertToPartyOrCandidateForBattleground(),
            currResults = emptyMap<Pair<String, Map<Party, Int>>, PartyResult?>().asOneTimePublisher(),
            name = {
                val votes = second.values.toList()
                val pct = abs(votes[0] - votes[1]) / votes.sum().toDouble()
                DecimalFormat("0.0").format(50 * pct) +
                    " " + first.uppercase() + " " +
                    (second.keys.filter { !setOf(alp, clp).contains(it) }.takeUnless { it.isEmpty() }?.toString() ?: "")
            },
            parties = (clp to alp).asOneTimePublisher(),
            seatsToShow = {
                left = 15.asOneTimePublisher()
                right = 15.asOneTimePublisher()
            },
            numRows = 15.asOneTimePublisher(),
            preferencesMode = true,
            title = "PENDULUM".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("BattlegroundScreen", "Basic-DoublePartyPreferences-1", panel)
        assertPublishes(
            panel.altText,
            """
            PENDULUM
            
            CLP PREVIOUS SEATS
            COLUMN 1: 8 PENDING
            
            ALP PREVIOUS SEATS
            COLUMN 1: 14 PENDING
            """.trimIndent(),
        )
    }

    @Test
    fun testSinglePartyBattlegroundWithMultipleIndependents() {
        val prevResult = Publisher(nbPrevResultWithIndependents())
        val currResult = Publisher<Map<String, PartyResult>>(emptyMap())
        val party = Publisher(lib)
        val targetSeats = Publisher(45)
        val defenseSeats = Publisher(30)
        val numRows = Publisher(15)
        val title = Publisher("LIBERAL BATTLEGROUND")
        val panel = BattlegroundScreen.singleParty(
            prevResults = prevResult,
            currResults = currResult,
            name = { uppercase() },
            party = party,
            seatsToShow = {
                defense = defenseSeats
                target = targetSeats
            },
            numRows = numRows,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("BattlegroundScreen", "Basic-SinglePartyIndependents-1", panel)
        assertPublishes(
            panel.altText,
            """
            LIBERAL BATTLEGROUND

            LIB TARGET SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 15 PENDING
            COLUMN 3: 2 PENDING
            
            LIB DEFENSE SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 2 PENDING
            """.trimIndent(),
        )

        party.submit(pc)
        targetSeats.submit(30)
        defenseSeats.submit(30)
        title.submit("PC BATTLEGROUND")
        compareRendering("BattlegroundScreen", "Basic-SinglePartyIndependents-2", panel)
        assertPublishes(
            panel.altText,
            """
            PC BATTLEGROUND

            PC TARGET SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 7 PENDING
            
            PC DEFENSE SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 12 PENDING
            """.trimIndent(),
        )
    }

    @Test
    fun testDoublePartyBattlegroundWithMultipleIndependents() {
        val prevResult = Publisher(nbPrevResultWithIndependents())
        val currResult = Publisher<Map<String, PartyResult>>(emptyMap())
        val parties = Publisher(lib to pc)
        val rightSeats = Publisher(30)
        val leftSeats = Publisher(30)
        val numRows = Publisher(15)
        val title = Publisher("BATTLEGROUND")
        val panel = BattlegroundScreen.doubleParty(
            prevResults = prevResult,
            currResults = currResult,
            name = { uppercase() },
            parties = parties,
            seatsToShow = {
                left = leftSeats
                right = rightSeats
            },
            numRows = numRows,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("BattlegroundScreen", "Basic-DoublePartyIndependents-1", panel)
        assertPublishes(
            panel.altText,
            """
            BATTLEGROUND

            LIB PREVIOUS SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 2 PENDING
            
            PC PREVIOUS SEATS
            COLUMN 1: 15 PENDING
            COLUMN 2: 12 PENDING
            """.trimIndent(),
        )
    }

    companion object {
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val pc = Party("Progressive Conservative", "PC", Color.BLUE)
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

        private fun nbPrevResultWithIndependents(): Map<String, Map<PartyOrCandidate, Int>> {
            val lib = PartyOrCandidate(lib)
            val pc = PartyOrCandidate(pc)
            val grn = PartyOrCandidate("Green")
            val pa = PartyOrCandidate("People's Alliance")
            val ndp = PartyOrCandidate(ndp)
            val kiss = PartyOrCandidate("KISS")
            val ind = PartyOrCandidate("Independent")

            return mapOf(
                "Albert" to mapOf(pc to 5040, lib to 921, grn to 1056, pa to 977, ind to 90),
                "Bathurst East-Nepisiguit-Saint-Isidore" to mapOf(pc to 1568, lib to 4163, grn to 798),
                "Bathurst West-Beresford" to mapOf(pc to 1985, lib to 3730, grn to 965),
                "Campbellton-Dalhousie" to mapOf(pc to 1369, lib to 4540, grn to 1054),
                "Caraquet" to mapOf(pc to 985, lib to 5928, grn to 1290),
                "Carleton" to mapOf(pc to 3536, lib to 1239, grn to 581, pa to 1909, ndp to 80, kiss to 41),
                "Carleton-Victoria" to mapOf(pc to 3330, lib to 2939, grn to 372, pa to 610, ndp to 113),
                "Carleton-York" to mapOf(pc to 4750, lib to 940, grn to 890, pa to 1524, ndp to 110),
                "Dieppe" to mapOf(pc to 1680, lib to 4564, grn to 1142, ndp to 200),
                "Edmundston-Madawaska Centre" to mapOf(pc to 1380, lib to 5236, grn to 415),
                "Fredericton North" to mapOf(pc to 3227, lib to 1464, grn to 2464, pa to 591, ndp to 100),
                "Fredericton South" to mapOf(pc to 2342, lib to 895, grn to 4213, pa to 234, ndp to 117),
                "Fredericton West-Hanwell" to mapOf(pc to 4726, lib to 1510, grn to 1745, pa to 825, ndp to 131),
                "Fredericton-Grand Lake" to mapOf(pc to 2479, lib to 749, grn to 1005, pa to 3759, ndp to 87, kiss to 18),
                "Fredericton-York" to mapOf(pc to 3730, lib to 872, grn to 2110, pa to 1991, ndp to 68, kiss to 24),
                "Fundy-The Isles-Saint John West" to mapOf(pc to 4740, lib to 726, grn to 686, pa to 688, ndp to 291),
                "Gagetown-Petitcodiac" to mapOf(pc to 4773, lib to 867, grn to 1003, pa to 1303, ndp to 131),
                "Hampton" to mapOf(pc to 4351, lib to 1084, grn to 816, pa to 687, ndp to 251),
                "Kent North" to mapOf(pc to 1363, lib to 2933, grn to 4021, ind to 154),
                "Kent South" to mapOf(pc to 2817, lib to 5148, grn to 996, pa to 243, ndp to 118),
                "Kings Centre" to mapOf(pc to 4583, lib to 911, grn to 1006, pa to 693, ndp to 254),
                "Madawaska Les Lacs-Edmundston" to mapOf(pc to 1763, lib to 4583, grn to 542),
                "Memramcook-Tantramar" to mapOf(pc to 1678, lib to 2902, grn to 3425, pa to 192, ind to 34),
                "Miramichi" to mapOf(pc to 1508, lib to 2239, grn to 398, pa to 3527, ndp to 92, ind to 54),
                "Miramichi Bay-Neguac" to mapOf(pc to 2751, lib to 3561, grn to 825, pa to 898, ndp to 139),
                "Moncton Centre" to mapOf(pc to 1642, lib to 2448, grn to 1725, pa to 308, ndp to 168),
                "Moncton East" to mapOf(pc to 3525, lib to 2759, grn to 989, pa to 378, ndp to 153),
                "Moncton Northwest" to mapOf(pc to 4111, lib to 2448, grn to 702, pa to 493, ndp to 229),
                "Moncton South" to mapOf(pc to 2734, lib to 1966, grn to 1245, pa to 331, ndp to 220),
                "Moncton Southwest" to mapOf(pc to 3679, lib to 1561, grn to 927, pa to 667, ndp to 224),
                "New Maryland-Sunbury" to mapOf(pc to 5342, lib to 1048, grn to 1463, pa to 1254, ndp to 141),
                "Oromocto-Lincoln-Fredericton" to mapOf(pc to 3374, lib to 2072, grn to 1306, pa to 745, ndp to 127),
                "Portland-Simonds" to mapOf(pc to 3170, lib to 1654, grn to 483, pa to 282, ndp to 164),
                "Quispamsis" to mapOf(pc to 5697, lib to 1225, grn to 528, pa to 414, ndp to 501),
                "Restigouche West" to mapOf(pc to 1247, lib to 5022, grn to 1755, kiss to 56),
                "Restigouche-Chaleur" to mapOf(pc to 1149, lib to 3823, grn to 1896),
                "Riverview" to mapOf(pc to 4695, lib to 1281, grn to 800, pa to 778, ndp to 261),
                "Rothesay" to mapOf(pc to 4265, lib to 1463, grn to 719, pa to 413, ind to 100),
                "Saint Croix" to mapOf(pc to 3570, lib to 401, grn to 1238, pa to 2546, ndp to 147),
                "Saint John East" to mapOf(pc to 3507, lib to 1639, grn to 394, pa to 434, ndp to 248),
                "Saint John Harbour" to mapOf(pc to 2181, lib to 1207, grn to 1224, pa to 186, ndp to 309),
                "Saint John Lancaster" to mapOf(pc to 3560, lib to 1471, grn to 938, pa to 394, ndp to 201),
                "Shediac Bay-Dieppe" to mapOf(pc to 2971, lib to 5839, pa to 371, ndp to 528),
                "Shediac-Beaubassin-Cap-Pelé" to mapOf(pc to 1820, lib to 4949, grn to 2453),
                "Shippagan-Lamèque-Miscou" to mapOf(pc to 714, lib to 6834, grn to 609),
                "Southwest Miramichi-Bay du Vin" to mapOf(pc to 3887, lib to 1760, pa to 2268, ndp to 188),
                "Sussex-Fundy-St. Martins" to mapOf(pc to 4366, lib to 971, grn to 969, pc to 1321, ndp to 129),
                "Tracadie-Sheila" to mapOf(pc to 2059, lib to 6175, grn to 645),
                "Victoria-La Vallée" to mapOf(pc to 2071, lib to 4365, grn to 426, pa to 292, ind to 92),
            )
        }
    }
}
