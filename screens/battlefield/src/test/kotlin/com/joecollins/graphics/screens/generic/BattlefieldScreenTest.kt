package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
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
            23 to mapOf(lib to 1147, pc to 810, grn to 240, ndp to 473)
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
            23 to PartyResult.leading(grn)
        )

        val prevTotal = mapOf(lib to 33481, pc to 30663, grn to 8857, ndp to 8997)
        val currTotal = mapOf(pc to 30415, grn to 25302, lib to 24346, ndp to 2454, Party.OTHERS to 282)
        val swings = run {
            val prevSum = prevTotal.values.sum().toDouble()
            val currSum = currTotal.values.sum().toDouble()
            val parties = sequenceOf(prevTotal.keys, currTotal.keys).flatten().toSet()
            parties.associateWith { (currTotal[it] ?: 0) / currSum - (prevTotal[it] ?: 0) / prevSum }
        }

        val screen = BattlefieldScreen.build(
            prevResults.asOneTimePublisher(),
            currResults.asOneTimePublisher(),
            pc.asOneTimePublisher(),
            lib.asOneTimePublisher(),
            grn.asOneTimePublisher(),
            "BATTLEFIELD PEI: ADVANCING TO A MAJORITY".asOneTimePublisher()
        )
            .withPartySwings(swings.asOneTimePublisher())
            .withLines(true.asOneTimePublisher())
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("BattlefieldScreen", "Result", screen)
    }
}
