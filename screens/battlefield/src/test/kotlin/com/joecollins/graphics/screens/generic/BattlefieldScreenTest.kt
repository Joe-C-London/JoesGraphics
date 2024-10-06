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
            
            PC WOULD HAVE MAJORITY ON NO ADVANCES
            """.trimIndent(),
        )
    }

    @Test
    fun testPartiesMerging() {
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val wra = Party("Wildrose", "WRA", Color.GREEN.darker().darker().darker())
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val ap = Party("Alberta Party", "AP", Color.CYAN)
        val grn = Party("Green", "GRN", Color.GREEN)
        val scp = Party("Social Credit", "SCP", Color.CYAN.darker())
        val com = Party("Communist", "COM", Color.RED.darker())
        val af = Party("Alberta First", "AFP", Color.YELLOW)
        val ind = Party("Independent", "IND", Party.OTHERS.color)
        val ucp = Party("United Conservative", "UCP", Color.BLUE.darker())

        val prevResults =
            mapOf(
                "Athabasca-Sturgeon-Redwater" to mapOf(pc to 5016, wra to 4973, ndp to 6797),
                "Barrhead-Morinville-Westlock" to mapOf(pc to 4876, wra to 7206, ndp to 6232),
                "Bonnyville-Cold Lake" to mapOf(pc to 3594, wra to 5452, ndp to 2136, ap to 628),
                "Dunvegan-Central Peace-Notley" to mapOf(pc to 2766, wra to 3147, ndp to 3692),
                "Fort McMurray-Conklin" to mapOf(pc to 1502, wra to 2950, lib to 204, ndp to 2071),
                "Fort McMurray-Wood Buffalo" to mapOf(pc to 2486, wra to 3835, lib to 345, ndp to 2915),
                "Grande Prairie-Smoky" to mapOf(pc to 4968, wra to 5343, lib to 787, ndp to 5009),
                "Grande Prairie-Wapiti" to mapOf(pc to 6229, wra to 4175, ndp to 5062, ap to 2048),
                "Lac La Biche-St. Paul-Two Hills" to mapOf(pc to 3004, wra to 4763, ndp to 4214, grn to 339),
                "Lesser Slave Lake" to mapOf(pc to 1944, wra to 3198, ndp to 3915),
                "Peace River" to mapOf(pc to 3529, wra to 1979, ndp to 3821, ap to 376),
                "Edmonton-Beverly-Clareview" to mapOf(pc to 2524, wra to 1248, lib to 359, ndp to 12049, ap to 147),
                "Edmonton-Calder" to mapOf(pc to 3222, wra to 1565, lib to 527, ndp to 12837),
                "Edmonton-Centre" to mapOf(pc to 2228, wra to 772, lib to 4199, ndp to 8983, ind to 295 + 40),
                "Edmonton-Glenora" to mapOf(pc to 3145, wra to 1394, lib to 553, ndp to 12473, ap to 463, grn to 195),
                "Edmonton-Gold Bar" to mapOf(pc to 4147, wra to 1422, lib to 702, ndp to 15349, ap to 662),
                "Edmonton-Highlands-Norwood" to mapOf(pc to 1778, wra to 967, lib to 494, ndp to 11555),
                "Edmonton-Mill Creek" to mapOf(pc to 3848, wra to 1365, lib to 1896, ndp to 9025),
                "Edmonton-Mill Woods" to mapOf(pc to 2920, wra to 1437, lib to 850, ndp to 9930, ind to 129, com to 44),
                "Edmonton-Riverview" to mapOf(pc to 3732, wra to 1350, lib to 1416, ndp to 12108, ap to 487, grn to 135, ind to 59),
                "Edmonton-Rutherford" to mapOf(pc to 3940, wra to 1644, lib to 741, ndp to 11214),
                "Edmonton-Strathcona" to mapOf(pc to 2242, lib to 658, ndp to 13592),
                "Edmonton-Castle Downs" to mapOf(pc to 4182, wra to 1383, lib to 880, ndp to 11689),
                "Edmonton-Decore" to mapOf(pc to 2847, wra to 1289, lib to 691, ndp to 10531, grn to 150),
                "Edmonton-Ellerslie" to mapOf(pc to 3549, wra to 2499, lib to 839, ndp to 11034),
                "Edmonton-Manning" to mapOf(pc to 2599, wra to 1475, lib to 776, ndp to 12376),
                "Edmonton-McClung" to mapOf(pc to 4408, wra to 2373, ndp to 9412, ap to 808),
                "Edmonton-Meadowlark" to mapOf(pc to 3924, wra to 1972, lib to 1507, ndp to 9796),
                "Edmonton-South West" to mapOf(pc to 6316, wra to 2290, lib to 1199, ndp to 12352, ap to 543),
                "Edmonton-Whitemud" to mapOf(pc to 7177, wra to 1423, lib to 629, ndp to 12805, grn to 182, ind to 73),
                "Sherwood Park" to mapOf(pc to 5655, wra to 4815, ndp to 11365),
                "St. Albert" to mapOf(pc to 6340, wra to 2858, lib to 778, ndp to 12220, ap to 493),
                "Drayton Valley-Devon" to mapOf(pc to 5182, wra to 6284, ndp to 4816, ap to 416, grn to 276),
                "Innisfail-Sylvan Lake" to mapOf(pc to 5136, wra to 7829, ndp to 4244, ap to 1135),
                "Olds-Didsbury-Three Hills" to mapOf(pc to 5274, wra to 10692, ndp to 3366, ap to 685),
                "Red Deer-North" to mapOf(pc to 3836, wra to 4173, lib to 3262, ndp to 4969, ap to 683),
                "Red Deer-South" to mapOf(pc to 5414, wra to 4812, lib to 738, ndp to 7024, ap to 1035, grn to 274, ind to 232 + 60),
                "Rimbey-Rocky Mountain House-Sundre" to mapOf(pc to 5296, wra to 6670, ndp to 2791, ind to 1871),
                "Spruce Grove-St. Albert" to mapOf(pc to 6362, wra to 4631, lib to 916, ndp to 11546, ap to 1081, grn to 269),
                "Stony Plain" to mapOf(pc to 4944, wra to 5586, lib to 657, ndp to 7268, ap to 538, grn to 220),
                "West Yellowhead" to mapOf(pc to 3433, wra to 3055, ndp to 4135),
                "Whitecourt-Ste. Anne" to mapOf(pc to 4721, wra to 4996, ndp to 5442),
                "Battle River-Wainwright" to mapOf(pc to 5057, wra to 6862, lib to 500, ndp to 3807),
                "Drumheller-Stettler" to mapOf(pc to 5388, wra to 7570, ndp to 2927),
                "Fort Saskatchewan-Vegreville" to mapOf(pc to 5527, wra to 3959, lib to 475, ndp to 8983, ap to 324, grn to 285),
                "Lacombe-Ponoka" to mapOf(pc to 5018, wra to 6502, ndp to 5481, ap to 1206),
                "Leduc-Beaumont" to mapOf(pc to 6225, wra to 6543, ndp to 8321, ap to 612, grn to 301),
                "Strathcona-Sherwood Park" to mapOf(pc to 6623, wra to 5286, ndp to 9376, ap to 721),
                "Vermilion-Lloydminster" to mapOf(pc to 5935, wra to 4171, ndp to 2428),
                "Wetaskiwin-Camrose" to mapOf(pc to 5951, wra to 3685, ndp to 7531),
                "Calgary-Acadia" to mapOf(pc to 4602, wra to 4985, lib to 765, ndp to 5506),
                "Calgary-Buffalo" to mapOf(pc to 3738, wra to 1351, lib to 3282, ndp to 4671, grn to 263),
                "Calgary-Cross" to mapOf(pc to 4501, wra to 2060, lib to 1194, ndp to 4602, grn to 236, ind to 143),
                "Calgary-Currie" to mapOf(pc to 4577, wra to 3769, lib to 1441, ndp to 7387, ap to 1006, grn to 373),
                "Calgary-East" to mapOf(pc to 3971, wra to 3663, lib to 806, ndp to 5506, com to 138),
                "Calgary-Elbow" to mapOf(pc to 6254, wra to 1786, lib to 565, ndp to 3256, ap to 8707, scp to 67),
                "Calgary-Fish Creek" to mapOf(pc to 6198, wra to 5568, ndp to 6069, ap to 850, scp to 148),
                "Calgary-Fort" to mapOf(pc to 3204, wra to 3003, lib to 476, ndp to 7027, ap to 410),
                "Calgary-Glenmore" to mapOf(pc to 7015, wra to 5058, lib to 1345, ndp to 7021, ap to 719),
                "Calgary-Klein" to mapOf(pc to 4878, wra to 4206, lib to 1104, ndp to 8098, grn to 0),
                "Calgary-Mountain View" to mapOf(pc to 4699, wra to 2070, lib to 7204, ndp to 5673),
                "Calgary-Varsity" to mapOf(pc to 5700, wra to 2598, lib to 1862, ndp to 8297, grn to 424),
                "Calgary-Bow" to mapOf(pc to 5419, wra to 3752, lib to 682, ndp to 5669, ap to 459, grn to 448),
                "Calgary-Foothills" to mapOf(pc to 7163, wra to 3216, lib to 1271, ndp to 5748, grn to 363),
                "Calgary-Greenway" to mapOf(pc to 5337, wra to 2627, ndp to 4513),
                "Calgary-Hawkwood" to mapOf(pc to 6378, wra to 4448, lib to 736, ndp to 7443, ap to 925, grn to 455, scp to 90),
                "Calgary-Hays" to mapOf(pc to 6671, wra to 4562, lib to 722, ndp to 5138, grn to 250, scp to 93),
                "Calgary-Lougheed" to mapOf(pc to 5939, wra to 4781, lib to 817, ndp to 5437),
                "Calgary-Mackay-Nose Hill" to mapOf(pc to 4587, wra to 4914, lib to 768, ndp to 6177, grn to 316),
                "Calgary-McCall" to mapOf(pc to 2317, wra to 3367, lib to 2224, ndp to 3812, ind to 1010),
                "Calgary-North West" to mapOf(pc to 6320, wra to 5163, lib to 935, ndp to 5724, ap to 1176),
                "Calgary-Northern Hills" to mapOf(pc to 5343, wra to 4392, lib to 1000, ndp to 6641),
                "Calgary-Shaw" to mapOf(pc to 5348, wra to 5301, lib to 668, ndp to 5449, ap to 661),
                "Calgary-South East" to mapOf(pc to 7663, wra to 6892, lib to 1304, ndp to 7358, grn to 374),
                "Calgary-West" to mapOf(pc to 8312, wra to 4512, ndp to 4940),
                "Chestermere-Rocky View" to mapOf(pc to 7454, wra to 7676, ndp to 3706, ind to 1093 + 391, grn to 405),
                "Airdrie" to mapOf(pc to 6181, wra to 7499, ndp to 6388, ap to 912, ind to 399),
                "Banff-Cochrane" to mapOf(pc to 5555, wra to 5692, ndp to 8426),
                "Cardston-Taber-Warner" to mapOf(pc to 4356, wra to 5126, ndp to 2407, ap to 378),
                "Cypress-Medicine Hat" to mapOf(pc to 3389, wra to 8544, lib to 528, ndp to 3201),
                "Highwood" to mapOf(pc to 6827, wra to 8504, ndp to 3937, ap to 892, grn to 390, scp to 187),
                "Lethbridge-East" to mapOf(pc to 4743, wra to 3918, lib to 1201, ndp to 8918),
                "Lethbridge-West" to mapOf(pc to 3938, wra to 3063, lib to 634, ndp to 11144),
                "Little Bow" to mapOf(pc to 4793, wra to 4803, lib to 377, ndp to 3364, scp to 249),
                "Livingstone-Macleod" to mapOf(pc to 6404, wra to 7362, lib to 464, ndp to 4338),
                "Medicine Hat" to mapOf(pc to 3427, wra to 5790, ndp to 6160, ap to 731, ind to 137),
                "Strathmore-Brooks" to mapOf(pc to 4452, wra to 8652, lib to 200, ndp to 2463, ap to 304, grn to 322, af to 72),
            )
        val currResults = mapOf(
            "Athabasca-Sturgeon-Redwater" to ucp,
            "Barrhead-Morinville-Westlock" to ucp,
            "Bonnyville-Cold Lake" to ucp,
            "Dunvegan-Central Peace-Notley" to ucp,
            "Fort McMurray-Conklin" to ucp,
            "Fort McMurray-Wood Buffalo" to ucp,
            "Grande Prairie-Smoky" to ucp,
            "Grande Prairie-Wapiti" to ucp,
            "Lac La Biche-St. Paul-Two Hills" to ucp,
            "Lesser Slave Lake" to ucp,
            "Peace River" to ucp,
            "Edmonton-Beverly-Clareview" to ndp,
            "Edmonton-Calder" to ndp,
            "Edmonton-Centre" to ndp,
            "Edmonton-Glenora" to ndp,
            "Edmonton-Gold Bar" to ndp,
            "Edmonton-Highlands-Norwood" to ndp,
            "Edmonton-Mill Creek" to ndp,
            "Edmonton-Mill Woods" to ndp,
            "Edmonton-Riverview" to ndp,
            "Edmonton-Rutherford" to ndp,
            "Edmonton-Strathcona" to ndp,
            "Edmonton-Castle Downs" to ndp,
            "Edmonton-Decore" to ndp,
            "Edmonton-Ellerslie" to ndp,
            "Edmonton-Manning" to ndp,
            "Edmonton-McClung" to ndp,
            "Edmonton-Meadowlark" to ndp,
            "Edmonton-South West" to ucp,
            "Edmonton-Whitemud" to ndp,
            "Sherwood Park" to ndp,
            "St. Albert" to ndp,
            "Drayton Valley-Devon" to ucp,
            "Innisfail-Sylvan Lake" to ucp,
            "Olds-Didsbury-Three Hills" to ucp,
            "Red Deer-North" to ucp,
            "Red Deer-South" to ucp,
            "Rimbey-Rocky Mountain House-Sundre" to ucp,
            "Spruce Grove-St. Albert" to ndp,
            "Stony Plain" to ucp,
            "West Yellowhead" to ucp,
            "Whitecourt-Ste. Anne" to ucp,
            "Battle River-Wainwright" to ucp,
            "Drumheller-Stettler" to ucp,
            "Fort Saskatchewan-Vegreville" to ucp,
            "Lacombe-Ponoka" to ucp,
            "Leduc-Beaumont" to ucp,
            "Strathcona-Sherwood Park" to ucp,
            "Vermilion-Lloydminster" to ucp,
            "Wetaskiwin-Camrose" to ucp,
            "Calgary-Acadia" to ucp,
            "Calgary-Buffalo" to ndp,
            "Calgary-Cross" to ucp,
            "Calgary-Currie" to ucp,
            "Calgary-East" to ucp,
            "Calgary-Elbow" to ucp,
            "Calgary-Fish Creek" to ucp,
            "Calgary-Fort" to ucp,
            "Calgary-Glenmore" to ucp,
            "Calgary-Klein" to ucp,
            "Calgary-Mountain View" to ndp,
            "Calgary-Varsity" to ucp,
            "Calgary-Bow" to ucp,
            "Calgary-Foothills" to ucp,
            "Calgary-Greenway" to ucp,
            "Calgary-Hawkwood" to ucp,
            "Calgary-Hays" to ucp,
            "Calgary-Lougheed" to ucp,
            "Calgary-Mackay-Nose Hill" to ucp,
            "Calgary-McCall" to ndp,
            "Calgary-North West" to ucp,
            "Calgary-Northern Hills" to ucp,
            "Calgary-Shaw" to ucp,
            "Calgary-South East" to ucp,
            "Calgary-West" to ucp,
            "Chestermere-Rocky View" to ucp,
            "Airdrie" to ucp,
            "Banff-Cochrane" to ucp,
            "Cardston-Taber-Warner" to ucp,
            "Cypress-Medicine Hat" to ucp,
            "Highwood" to ucp,
            "Lethbridge-East" to ucp,
            "Lethbridge-West" to ndp,
            "Little Bow" to ucp,
            "Livingstone-Macleod" to ucp,
            "Medicine Hat" to ucp,
            "Strathmore-Brooks" to ucp,
        ).mapValues { (_, v) -> PartyResult.elected(v) }

        val prevTotal = mapOf(ndp to 604518, wra to 360511, pc to 413610, lib to 62153, ap to 33221, grn to 7215, Party.OTHERS to 7020)
        val currTotal = mapOf(ucp to 1040563, ndp to 619921, ap to 172203, lib to 18544, ap to 172203, grn to 7682, Party.OTHERS to 37629)

        val screen = BattlefieldScreen.build(
            prevVotes = prevResults.asOneTimePublisher().convertToPartyOrCandidateForBattlefield(),
            results = currResults.asOneTimePublisher(),
            parties = {
                left = ndp.asOneTimePublisher()
                right = ucp.asOneTimePublisher()
                bottom = ap.asOneTimePublisher()
            },
            header = "BATTLEFIELD ALBERTA".asOneTimePublisher(),
            totalVotes = {
                prev = prevTotal.asOneTimePublisher()
                curr = currTotal.asOneTimePublisher()
            },
            majorityLines = true.asOneTimePublisher(),
            partyChanges = mapOf(pc to ucp, wra to ucp).asOneTimePublisher(),
            title = "ALBERTA".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("BattlefieldScreen", "MergedParties", screen)
        assertPublishes(
            screen.altText,
            """
            ALBERTA
            BATTLEFIELD ALBERTA
            
            AP ADVANCES 7.4% INTO NDP TERRITORY
            UCP ADVANCES 5.4% INTO NDP TERRITORY
            AP ADVANCES 2.0% INTO UCP TERRITORY
            
            UCP WOULD HAVE MAJORITY ON UNIFORM ADVANCES
            """.trimIndent(),
        )
    }
}
