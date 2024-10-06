package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.BattlefieldScreen.Companion.convertToPartyOrCandidateForBattlefield
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCandidate
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color

class BattlefieldScreenTest {

    @Test
    fun testBattlefieldResult() {
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)

        val prevResults = mapOf(
            4 to mapOf(lib to 1095, pc to 1203, grn to 152, ndp to 216),
            2 to mapOf(lib to 1170, pc to 1448, grn to 145, ndp to 256),
            3 to mapOf(lib to 1060, pc to 785, grn to 106, ndp to 585),
            7 to mapOf(lib to 1114, pc to 1501, grn to 177, ndp to 211),
            1 to mapOf(lib to 952, pc to 1179, ndp to 528),
            6 to mapOf(lib to 1453, pc to 2155, grn to 330, ndp to 350),
            5 to mapOf(lib to 1174, pc to 1173, grn to 234, ndp to 258),
            19 to mapOf(lib to 1154, pc to 1597, grn to 511, ndp to 126),
            16 to mapOf(lib to 1444, pc to 1056, grn to 377, ndp to 243),
            17 to mapOf(lib to 1046, pc to 609, grn to 2077, ndp to 58),
            20 to mapOf(lib to 1033, pc to 1984, grn to 374, ndp to 264),
            18 to mapOf(lib to 1152, pc to 1585, grn to 325, ndp to 294),
            9 to mapOf(lib to 1938, pc to 1338, grn to 347, ndp to 442),
            13 to mapOf(lib to 1054, pc to 1032, grn to 352, ndp to 265),
            14 to mapOf(lib to 1040, pc to 821, grn to 244, ndp to 931),
            11 to mapOf(lib to 1166, pc to 699, grn to 511, ndp to 292),
            10 to mapOf(lib to 1425, pc to 1031, grn to 295, ndp to 360),
            12 to mapOf(lib to 955, pc to 666, grn to 456, ndp to 348),
            8 to mapOf(lib to 1354, pc to 826, grn to 237, ndp to 549),
            15 to mapOf(lib to 1389, pc to 1330, grn to 462, ndp to 516),
            26 to mapOf(lib to 1569, pc to 1166, ndp to 188),
            24 to mapOf(lib to 1419, pc to 586, grn to 125, ndp to 138),
            25 to mapOf(lib to 1310, pc to 1063, ndp to 311),
            22 to mapOf(lib to 1246, pc to 1098, grn to 321, ndp to 358),
            21 to mapOf(lib to 1135, pc to 1105, grn to 285, ndp to 353),
            27 to mapOf(lib to 1486, pc to 818, grn to 167, ndp to 81),
            23 to mapOf(lib to 1147, pc to 810, grn to 240, ndp to 473),
        )
        val currResults = mapOf(
            4 to PartyResult.elected(pc),
            2 to PartyResult.elected(pc),
            5 to PartyResult.elected(grn),
            3 to PartyResult.elected(pc),
            7 to PartyResult.elected(pc),
            1 to PartyResult.elected(pc),
            6 to PartyResult.elected(pc),
            19 to PartyResult.elected(pc),
            15 to PartyResult.elected(pc),
            16 to PartyResult.elected(lib),
            20 to PartyResult.elected(pc),
            17 to PartyResult.elected(grn),
            18 to PartyResult.elected(pc),
            8 to PartyResult.leading(pc),
            11 to PartyResult.elected(grn),
            13 to PartyResult.leading(grn),
            12 to PartyResult.elected(grn),
            14 to PartyResult.leading(lib),
            10 to PartyResult.elected(lib),
            26 to PartyResult.elected(pc),
            24 to PartyResult.elected(lib),
            25 to PartyResult.elected(lib),
            22 to PartyResult.elected(grn),
            21 to PartyResult.elected(grn),
            27 to PartyResult.elected(lib),
            23 to PartyResult.leading(grn),
        )

        val prevTotal = mapOf(lib to 33481, pc to 30663, grn to 8857, ndp to 8997)
        val currTotal = mapOf(pc to 30415, grn to 25302, lib to 24346, ndp to 2454, Party.OTHERS to 282)

        val screen = BattlefieldScreen.build(
            prevVotes = prevResults.asOneTimePublisher().convertToPartyOrCandidateForBattlefield(),
            results = currResults.asOneTimePublisher(),
            parties = {
                left = pc.asOneTimePublisher()
                right = lib.asOneTimePublisher()
                bottom = grn.asOneTimePublisher()
            },
            header = "BATTLEFIELD PEI: ADVANCING TO A MAJORITY".asOneTimePublisher(),
            totalVotes = {
                prev = prevTotal.asOneTimePublisher()
                curr = currTotal.asOneTimePublisher()
            },
            majorityLines = true.asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("BattlefieldScreen", "Result", screen)
        assertPublishes(
            screen.altText,
            """
            PRINCE EDWARD ISLAND
            BATTLEFIELD PEI: ADVANCING TO A MAJORITY
            
            GRN ADVANCES 15.6% INTO LIB TERRITORY
            GRN ADVANCES 10.2% INTO PC TERRITORY
            PC ADVANCES 5.4% INTO LIB TERRITORY
            
            PC WOULD HAVE MAJORITY ON UNIFORM ADVANCES
            """.trimIndent(),
        )
    }

    @Test
    fun testBattlefieldResultMultipleIndependents() {
        val lib = PartyOrCandidate(Party("Liberal", "LIB", Color.RED))
        val pc = PartyOrCandidate(Party("Progressive Conservative", "PC", Color.BLUE))
        val grn = PartyOrCandidate("Green")
        val pa = PartyOrCandidate("People's Alliance")
        val ndp = PartyOrCandidate(Party("New Democratic Party", "NDP", Color.ORANGE))
        val kiss = PartyOrCandidate("KISS")
        val ind = PartyOrCandidate("Independent")

        val prevResults = mapOf(
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
        val currResults = emptyMap<String, PartyResult?>()

        val screen = BattlefieldScreen.build(
            prevVotes = prevResults.asOneTimePublisher(),
            results = currResults.asOneTimePublisher(),
            parties = {
                left = pc.party.asOneTimePublisher()
                right = lib.party.asOneTimePublisher()
                bottom = ndp.party.asOneTimePublisher()
            },
            header = "BATTLEFIELD NEW BRUNSWICK: ADVANCING TO A MAJORITY".asOneTimePublisher(),
            majorityLines = true.asOneTimePublisher(),
            title = "NEW BRUNSWICK".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("BattlefieldScreen", "ResultMultipleIndependents", screen)
        assertPublishes(
            screen.altText,
            """
            NEW BRUNSWICK
            BATTLEFIELD NEW BRUNSWICK: ADVANCING TO A MAJORITY
            
            PC CURRENTLY HAS MAJORITY
            """.trimIndent(),
        )
    }
}
