package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.SwingometerScreen.Companion.of
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PartyResult.Companion.elected
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.Test
import java.awt.Color

class SwingometerScreenTest {

    @Test
    fun testBasicTwoParties() {
        val prevResult = Publisher(nbPrevResult())
        val currResult = Publisher(nbCurrResult())
        val parties = Publisher(Pair(lib, pc))
        val swing = Publisher(mapOf(pc to +0.0745, lib to -0.0345, grn to +0.0336, pa to -0.0339, ndp to -0.0335, ind to -0.0062))
        val panel = of(
            prevResult,
            currResult,
            swing,
            parties,
            "SWINGOMETER".asOneTimePublisher()
        )
            .withSeatLabelIncrements(3.asOneTimePublisher())
            .build("NEW BRUNSWICK".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Basic-TwoParty-1", panel)
        parties.submit(Pair(grn, pc))
        compareRendering("SwingometerScreen", "Basic-TwoParty-2", panel)
        parties.submit(Pair(pa, pc))
        compareRendering("SwingometerScreen", "Basic-TwoParty-3", panel)
    }

    @Test
    fun testBasicSwingUpdates() {
        val prevResult = Publisher(nbPrevResult())
        val result = nbCurrResult().mapValues { null }
        val currResult = Publisher<Map<String, PartyResult?>>(result)
        val parties = Publisher(Pair(lib, pc))
        val swing = Publisher<Map<Party, Double>>(emptyMap())
        val panel = of(
            prevResult,
            currResult,
            swing,
            parties,
            "SWINGOMETER".asOneTimePublisher()
        )
            .withSeatLabelIncrements(3.asOneTimePublisher())
            .build("NEW BRUNSWICK".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Basic-Updates-1", panel)
        currResult.submit(nbCurrResult())
        swing.submit(mapOf(pc to +0.0745, lib to -0.0345, grn to +0.0336, pa to -0.0339, ndp to -0.0335, ind to -0.0062))
        compareRendering("SwingometerScreen", "Basic-Updates-2", panel)
    }

    @Test
    fun testFilteredTwoParties() {
        val prevResult = Publisher(nbPrevResult())
        val currResult = Publisher(nbCurrResult())
        val parties = Publisher(Pair(lib, pc))
        val swing = Publisher(mapOf(pc to +0.0745, lib to -0.0345, grn to +0.0336, pa to -0.0339, ndp to -0.0335, ind to -0.0062))
        val seatsFiltered = Publisher<Set<String>?>(
            setOf(
                "Oromocto-Lincoln-Fredericton",
                "Fredericton-Grand Lake",
                "New Maryland-Sunbury",
                "Fredericton South",
                "Fredericton North",
                "Fredericton-York",
                "Fredericton West-Hanwell",
                "Carleton-York"
            )
        )
        val panel = of(
            prevResult,
            currResult,
            swing,
            parties,
            "SWINGOMETER".asOneTimePublisher()
        )
            .withSeatLabelIncrements(3.asOneTimePublisher())
            .withSeatFilter(seatsFiltered)
            .build("NEW BRUNSWICK".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Filtered-TwoParty-1", panel)
        seatsFiltered.submit(emptySet())
        compareRendering("SwingometerScreen", "Filtered-TwoParty-2", panel)
        seatsFiltered.submit(null)
        compareRendering("SwingometerScreen", "Filtered-TwoParty-3", panel)
    }

    @Test
    fun testBasicProgressLabel() {
        val prevResult = Publisher(nbPrevResult())
        val currResult = Publisher(nbCurrResult())
        val parties = Publisher(Pair(lib, pc))
        val swing = Publisher(mapOf(pc to +0.0745, lib to -0.0345, grn to +0.0336, pa to -0.0339, ndp to -0.0335, ind to -0.0062))
        val panel = of(
            prevResult,
            currResult,
            swing,
            parties,
            "SWINGOMETER".asOneTimePublisher()
        )
            .withSeatLabelIncrements(3.asOneTimePublisher())
            .withProgressLabel("100% IN".asOneTimePublisher())
            .build("NEW BRUNSWICK".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Basic-ProgressLabel", panel)
    }

    @Test
    fun testBasicTwoPartiesWithVotes() {
        val prevResult = Publisher(nbPrevResult())
        val currResult = Publisher(nbCurrResult())
        val parties = Publisher(Pair(lib, pc))
        val curr = Publisher(mapOf(pc to 147790, lib to 129025, grn to 57252, pa to 34526, ndp to 6220, ind to 824))
        val prev = Publisher(mapOf(pc to 121300, lib to 143791, grn to 45186, pa to 47860, ndp to 19039, ind to 3187))
        val panel = of(
            prevResult,
            currResult,
            curr,
            prev,
            parties,
            "SWINGOMETER".asOneTimePublisher()
        )
            .withSeatLabelIncrements(3.asOneTimePublisher())
            .build("NEW BRUNSWICK".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Basic-TwoPartyVotes-1", panel)
        parties.submit(Pair(grn, pc))
        compareRendering("SwingometerScreen", "Basic-TwoPartyVotes-2", panel)
        parties.submit(Pair(pa, pc))
        compareRendering("SwingometerScreen", "Basic-TwoPartyVotes-3", panel)
    }

    companion object {
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
        private val pc = Party("Progressive Conservative", "PCP", Color.BLUE)
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val pa = Party("People's Alliance", "PA", Color.MAGENTA.darker())
        private val ind = Party("Independent", "IND", Color.GRAY)
        private fun nbPrevResult(): Map<String, Map<Party, Int>> {
            return mapOf(
                "Restigouche West" to mapOf(lib to 4233, pc to 961, grn to 2540, ndp to 263, ind to 62),
                "Campbellton-Dalhousie" to mapOf(lib to 3720, pc to 1761, grn to 637, ndp to 721, pa to 558),
                "Restigouche-Chaleur" to mapOf(lib to 4430, pc to 826, grn to 831, ndp to 621),
                "Bathurst West-Beresford" to mapOf(lib to 4351, pc to 1082, grn to 503, ndp to 443, ind to 64),
                "Bathurst East-Nepisguit-Saint-Isidore" to mapOf(lib to 3550, pc to 858, grn to 421, ndp to 2026),
                "Caraquet" to mapOf(lib to 5420, pc to 1827, grn to 330, ndp to 548, ind to 373),
                "Shippagan-Lameque-Miscou" to mapOf(lib to 3949, pc to 4048, ndp to 578, ind to 178),
                "Tracadie-Sheila" to mapOf(lib to 4320, pc to 2390, grn to 390, ndp to 1213, ind to 544),
                "Miramichi Bay-Neguac" to mapOf(lib to 3512, pc to 1741, grn to 349, ndp to 718, pa to 2047),
                "Miramichi" to mapOf(lib to 2825, pc to 1154, grn to 189, ndp to 110, pa to 3788),
                "Southwest Miramichi-Bay du Vin" to mapOf(lib to 1909, pc to 2960, grn to 447, ndp to 97, pa to 2925, ind to 19),
                "Kent North" to mapOf(lib to 3301, pc to 1112, grn to 4056, ndp to 171, ind to 194),
                "Kent South" to mapOf(lib to 5595, pc to 1848, grn to 1304, ndp to 436),
                "Shediac Bay-Dieppe" to mapOf(lib to 6162, pc to 1353, grn to 906, ndp to 764),
                "Shediac-Beaubassin-Cap-Pele" to mapOf(lib to 5919, pc to 2081, grn to 888, ndp to 428),
                "Memramcook-Tantramar" to mapOf(lib to 3137, pc to 1518, grn to 3148, ndp to 410),
                "Dieppe" to mapOf(lib to 5173, pc to 998, ndp to 1057),
                "Moncton East" to mapOf(lib to 3626, pc to 2771, grn to 925, ndp to 424),
                "Moncton Centre" to mapOf(lib to 2698, pc to 982, grn to 771, ndp to 229, pa to 309, ind to 1200),
                "Moncton South" to mapOf(lib to 3099, pc to 2090, grn to 628, ndp to 249, pa to 466),
                "Moncton Northwest" to mapOf(lib to 2963, pc to 3186, grn to 437, ndp to 297, pa to 875),
                "Moncton Southwest" to mapOf(lib to 2667, pc to 2920, grn to 907, ndp to 503),
                "Riverview" to mapOf(lib to 2053, pc to 3701, grn to 542, ndp to 249, pa to 1005),
                "Albert" to mapOf(lib to 1775, pc to 3479, grn to 870, ndp to 375, pa to 1546, ind to 87),
                "Gagetown-Petitcodiac" to mapOf(lib to 1153, pc to 3674, grn to 1097, ndp to 165, pa to 1892, ind to 56),
                "Sussex-Fundy-St. Martins" to mapOf(lib to 1212, pc to 3816, grn to 505, ndp to 254, pa to 1874, ind to 54),
                "Hampton" to mapOf(lib to 1454, pc to 3702, grn to 743, ndp to 384, pa to 1246),
                "Quispamsis" to mapOf(lib to 2078, pc to 4691, grn to 445, ndp to 239, pa to 795),
                "Rothesay" to mapOf(lib to 2001, pc to 3542, grn to 571, ndp to 251, pa to 722),
                "Saint John East" to mapOf(lib to 1775, pc to 3017, grn to 373, ndp to 402, pa to 1047),
                "Portland-Simonds" to mapOf(lib to 1703, pc to 3168, grn to 435, ndp to 449, ind to 191),
                "Saint John Harbour" to mapOf(lib to 1865, pc to 1855, grn to 721, ndp to 836, pa to 393),
                "Saint John Lancaster" to mapOf(lib to 1727, pc to 3001, grn to 582, ndp to 414, pa to 922),
                "Kings Centre" to mapOf(lib to 1785, pc to 3267, grn to 731, ndp to 342, pa to 1454),
                "Fundy-The Isles-Saint John West" to mapOf(lib to 2422, pc to 3808, grn to 469, ndp to 203, pa to 1104),
                "Saint Croix" to mapOf(lib to 2436, pc to 3249, grn to 1047, ndp to 89, pa to 1466),
                "Oromocto-Lincoln-Fredericton" to mapOf(lib to 2306, pc to 2399, grn to 903, ndp to 159, pa to 1741),
                "Fredericton-Grand Lake" to mapOf(lib to 955, pc to 2433, grn to 472, ndp to 114, pa to 4799, ind to 19),
                "New Maryland-Sunbury" to mapOf(lib to 2210, pc to 3844, grn to 902, ndp to 143, pa to 2214, ind to 14),
                "Fredericton South" to mapOf(lib to 1525, pc to 1042, grn to 4273, ndp to 132, pa to 616),
                "Fredericton North" to mapOf(lib to 2443, pc to 2182, grn to 1313, ndp to 139, pa to 1651),
                "Fredericton-York" to mapOf(lib to 1652, pc to 2777, grn to 1393, ndp to 103, pa to 3033, ind to 34),
                "Fredericton West-Hanwell" to mapOf(lib to 2404, pc to 2739, grn to 1490, ndp to 171, pa to 1803),
                "Carleton-York" to mapOf(lib to 1556, pc to 3118, grn to 837, ndp to 255, pa to 2583, ind to 40),
                "Carleton" to mapOf(lib to 1197, pc to 2982, grn to 1247, ndp to 82, pa to 2026),
                "Carleton-Victoria" to mapOf(lib to 3116, pc to 2872, grn to 503, ndp to 114, pa to 960, ind to 58),
                "Victoria-La Vallee" to mapOf(lib to 3570, pc to 3212, grn to 468, ndp to 307),
                "Edmunston-Madawaska Centre" to mapOf(lib to 4668, pc to 1437, grn to 702, ndp to 206),
                "Madawaska Les Lacs-Edmunston" to mapOf(lib to 4191, pc to 1826, grn to 945, ndp to 156),
            )
        }

        private fun nbCurrResult(): Map<String, PartyResult?> {
            return mapOf(
                "Restigouche West" to elected(lib),
                "Campbellton-Dalhousie" to elected(lib),
                "Restigouche-Chaleur" to elected(lib),
                "Bathurst West-Beresford" to elected(lib),
                "Bathurst East-Nepisguit-Saint-Isidore" to elected(lib),
                "Caraquet" to elected(lib),
                "Shippagan-Lameque-Miscou" to elected(lib),
                "Tracadie-Sheila" to elected(lib),
                "Miramichi Bay-Neguac" to elected(lib),
                "Miramichi" to elected(pa),
                "Southwest Miramichi-Bay du Vin" to elected(pc),
                "Kent North" to elected(grn),
                "Kent South" to elected(lib),
                "Shediac Bay-Dieppe" to elected(lib),
                "Shediac-Beaubassin-Cap-Pele" to elected(lib),
                "Memramcook-Tantramar" to elected(grn),
                "Dieppe" to elected(lib),
                "Moncton East" to elected(pc),
                "Moncton Centre" to elected(lib),
                "Moncton South" to elected(pc),
                "Moncton Northwest" to elected(pc),
                "Moncton Southwest" to elected(pc),
                "Riverview" to elected(pc),
                "Albert" to elected(pc),
                "Gagetown-Petitcodiac" to elected(pc),
                "Sussex-Fundy-St. Martins" to elected(pc),
                "Hampton" to elected(pc),
                "Quispamsis" to elected(pc),
                "Rothesay" to elected(pc),
                "Saint John East" to elected(pc),
                "Portland-Simonds" to elected(pc),
                "Saint John Harbour" to elected(pc),
                "Saint John Lancaster" to elected(pc),
                "Kings Centre" to elected(pc),
                "Fundy-The Isles-Saint John West" to elected(pc),
                "Saint Croix" to elected(pc),
                "Oromocto-Lincoln-Fredericton" to elected(pc),
                "Fredericton-Grand Lake" to elected(pa),
                "New Maryland-Sunbury" to elected(pc),
                "Fredericton South" to elected(grn),
                "Fredericton North" to elected(pc),
                "Fredericton-York" to elected(pc),
                "Fredericton West-Hanwell" to elected(pc),
                "Carleton-York" to elected(pc),
                "Carleton" to elected(pc),
                "Carleton-Victoria" to elected(pc),
                "Victoria-La Vallee" to elected(lib),
                "Edmunston-Madawaska Centre" to elected(lib),
                "Madawaska Les Lacs-Edmunston" to elected(lib),
            )
        }
    }
}
