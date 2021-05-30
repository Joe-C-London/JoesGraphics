package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.BattlegroundScreen.Companion.singleParty
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PartyResult.Companion.elected
import com.joecollins.models.general.PartyResult.Companion.leading
import java.awt.Color
import java.io.IOException
import java.util.TreeMap
import kotlin.Throws
import org.junit.Test

class BattlegroundScreenTest {
    @Test
    @Throws(IOException::class)
    fun testSinglePartyBattleground() {
        val prevResult = BindableWrapper(bcPrevResult())
        val currResult = BindableWrapper<Map<String, PartyResult>>(emptyMap())
        val party = BindableWrapper(ndp)
        val targetSeats = BindableWrapper(30)
        val defenseSeats = BindableWrapper(15)
        val numRows = BindableWrapper(15)
        val title = BindableWrapper("NDP BATTLEGROUND")
        val nameShortener = { obj: String -> obj.uppercase() }
        val panel = singleParty(
                prevResult.binding,
                currResult.binding,
                { nameShortener(it) },
                party.binding)
                .withSeatsToShow(defenseSeats.binding, targetSeats.binding)
                .withNumRows(numRows.binding)
                .build(title.binding)
        panel.setSize(1024, 512)
        compareRendering("BattlegroundScreen", "Basic-SingleParty-1", panel)
        party.value = lib
        targetSeats.value = 15
        defenseSeats.value = 30
        title.value = "LIBERAL BATTLEGROUND"
        compareRendering("BattlegroundScreen", "Basic-SingleParty-2", panel)
        currResult.value = bcCurrResult()
        compareRendering("BattlegroundScreen", "Basic-SingleParty-3", panel)
        party.value = grn
        targetSeats.value = 30
        defenseSeats.value = 5
        title.value = "GREEN BATTLEGROUND"
        compareRendering("BattlegroundScreen", "Basic-SingleParty-4", panel)
        party.value = ndp
        targetSeats.value = 30
        defenseSeats.value = 0
        title.value = "NDP TARGETS"
        compareRendering("BattlegroundScreen", "Basic-SingleParty-5", panel)
        party.value = lib
        targetSeats.value = 0
        defenseSeats.value = 30
        title.value = "LIBERAL DEFENSE"
        compareRendering("BattlegroundScreen", "Basic-SingleParty-6", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testSinglePartyFilteredBattleground() {
        val prevResult = BindableWrapper(bcPrevResult())
        val currResult = BindableWrapper<Map<String, PartyResult?>>(emptyMap())
        val party = BindableWrapper(ndp)
        val targetSeats = BindableWrapper(30)
        val defenseSeats = BindableWrapper(15)
        val numRows = BindableWrapper(15)
        val filteredSeats = BindableWrapper<Set<String>?>(
                prevResult.value.keys
                        .filter { k: String -> k.startsWith("Vancouver") }
                        .toSet())
        val title = BindableWrapper("NDP BATTLEGROUND")
        val nameShortener = { obj: String -> obj.uppercase() }
        val panel = singleParty(
                prevResult.binding,
                currResult.binding,
                { nameShortener(it) },
                party.binding)
                .withSeatsToShow(defenseSeats.binding, targetSeats.binding)
                .withNumRows(numRows.binding)
                .withSeatFilter(filteredSeats.binding)
                .build(title.binding)
        panel.setSize(1024, 512)
        compareRendering("BattlegroundScreen", "Filtered-SingleParty-1", panel)
        currResult.value = bcCurrResult()
        compareRendering("BattlegroundScreen", "Filtered-SingleParty-2", panel)
        filteredSeats.value = emptySet()
        compareRendering("BattlegroundScreen", "Filtered-SingleParty-3", panel)
        filteredSeats.value = null
        compareRendering("BattlegroundScreen", "Filtered-SingleParty-4", panel)
    }

    companion object {
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
        private val ind = Party("Independent", "IND", Color.GRAY)
        private val oth = Party.OTHERS
        private fun bcPrevResult(): Map<String, Map<Party, Int>> {
            val ret: MutableMap<String, Map<Party, Int>> = TreeMap()
            ret["Nechako Lakes"] = mapOf(lib to 5307, ndp to 2909, grn to 878, oth to 438 + 226)
            ret["North Coast"] = mapOf(lib to 3079, ndp to 5243, grn to 826)
            ret["Peace River North"] = mapOf(lib to 9707, ndp to 973, oth to 2799 + 884 + 275)
            ret["Peace River South"] = mapOf(lib to 6634, ndp to 2102)
            ret["Prince George-Mackenzie"] = mapOf(lib to 10725, ndp to 5942, grn to 2109)
            ret["Prince George-Valemount"] = mapOf(lib to 11189, ndp to 5683, grn to 2353)
            ret["Skeena"] = mapOf(lib to 6772, ndp to 5613, oth to 580)
            ret["Stikine"] = mapOf(lib to 3531, ndp to 4748, oth to 834)
            ret["Columbia River-Revelstoke"] = mapOf(lib to 6620, ndp to 5248, grn to 1708, oth to 469 + 371 + 154)
            ret["Kootenay East"] = mapOf(lib to 9666, ndp to 5069, grn to 1926, oth to 425)
            ret["Kootenay West"] = mapOf(lib to 4547, ndp to 11164, grn to 2976)
            ret["Nelson-Creston"] = mapOf(lib to 5087, ndp to 7685, grn to 5130, oth to 164 + 149)
            ret["Boundary-Similkameen"] = mapOf(lib to 9513, ndp to 7275, grn to 2274, oth to 3165)
            ret["Kelowna-Lake Country"] = mapOf(lib to 15286, ndp to 5345, grn to 4951)
            ret["Kelowna-Mission"] = mapOf(lib to 15399, ndp to 5720, grn to 3836, oth to 1976)
            ret["Kelowna West"] = mapOf(lib to 15674, ndp to 6672, grn to 3628, oth to 570)
            ret["Penticton"] = mapOf(lib to 14470, ndp to 7874, grn to 5061)
            ret["Shuswap"] = mapOf(lib to 14829, ndp to 7161, grn to 4175, oth to 410)
            ret["Vernon-Monashee"] = mapOf(lib to 13625, ndp to 8355, grn to 6139, oth to 341)
            ret["Cariboo-Chilcotin"] = mapOf(lib to 8520, ndp to 3801, grn to 2174)
            ret["Cariboo North"] = mapOf(lib to 6359, ndp to 4430, grn to 919, oth to 747)
            ret["Fraser-Nicola"] = mapOf(lib to 6597, ndp to 6005, grn to 2517, oth to 598)
            ret["Kamloops-North Thompson"] = mapOf(lib to 12001, ndp to 7538, grn to 5511, oth to 187)
            ret["Kamloops-South Thompson"] = mapOf(lib to 15465, ndp to 6072, grn to 5785, oth to 295 + 109)
            ret["Abbotsford-Mission"] = mapOf(lib to 12879, ndp to 7339, grn to 4298, oth to 644)
            ret["Abbotsford South"] = mapOf(lib to 11673, ndp to 6297, grn to 3338, oth to 942)
            ret["Abbotsford West"] = mapOf(lib to 11618, ndp to 6474, grn to 2280, oth to 516 + 149)
            ret["Chilliwack"] = mapOf(lib to 9180, ndp to 6207, grn to 3277, oth to 402)
            ret["Chilliwack-Kent"] = mapOf(lib to 11814, ndp to 7273, grn to 3335)
            ret["Langley"] = mapOf(lib to 10755, ndp to 8384, grn to 3699, oth to 1221 + 166)
            ret["Langley East"] = mapOf(lib to 16384, ndp to 8820, grn to 4968, oth to 448)
            ret["Maple Ridge-Mission"] = mapOf(lib to 10663, ndp to 10988, grn to 3467, oth to 934 + 145)
            ret["Maple Ridge-Pitt Meadows"] = mapOf(lib to 10428, ndp to 12045, grn to 3329, oth to 676 + 408)
            ret["Surrey-Cloverdale"] = mapOf(lib to 11918, ndp to 9763, grn to 3091, oth to 279)
            ret["Surrey-Fleetwood"] = mapOf(lib to 7413, ndp to 11085, grn to 2190)
            ret["Surrey-Green Timbers"] = mapOf(lib to 5056, ndp to 8945, grn to 1112, oth to 163 + 69)
            ret["Surrey-Guildford"] = mapOf(lib to 7015, ndp to 9262, grn to 1840, oth to 462)
            ret["Surrey-Newton"] = mapOf(lib to 5100, ndp to 9744, grn to 1171, oth to 988)
            ret["Surrey-Panorama"] = mapOf(lib to 10064, ndp to 12226, grn to 1620, oth to 132)
            ret["Surrey South"] = mapOf(lib to 13509, ndp to 8718, grn to 3141, oth to 634 + 311 + 140 + 67)
            ret["Surrey-Whalley"] = mapOf(lib to 5293, ndp to 10315, grn to 1893, oth to 96)
            ret["Surrey-White Rock"] = mapOf(lib to 14101, ndp to 8648, grn to 4574, oth to 950)
            ret["Delta North"] = mapOf(lib to 9319, ndp to 11465, grn to 2697)
            ret["Delta South"] = mapOf(lib to 11123, ndp to 5228, grn to 2349, ind to 6437, oth to 88)
            ret["Richmond North Centre"] = mapOf(lib to 7916, ndp to 5135, grn to 1579, oth to 336 + 117)
            ret["Richmond-Queensborough"] = mapOf(lib to 8218, ndp to 8084, grn to 2524, oth to 694 + 318)
            ret["Richmond South Centre"] = mapOf(lib to 6914, ndp to 5666, grn to 1561)
            ret["Richmond-Steveston"] = mapOf(lib to 10332, ndp to 8524, grn to 2833)
            ret["Burnaby-Deer Lake"] = mapOf(lib to 6491, ndp to 8747, grn to 2209, oth to 589 + 229)
            ret["Burnaby-Edmonds"] = mapOf(lib to 6404, ndp to 10827, grn to 2728)
            ret["Burnaby-Lougheed"] = mapOf(lib to 8391, ndp to 10911, grn to 3127, oth to 145 + 129)
            ret["Burnaby North"] = mapOf(lib to 9290, ndp to 11447, grn to 2830)
            ret["Coquitlam-Burke Mountain"] = mapOf(lib to 10388, ndp to 10301, grn to 2771)
            ret["Coquitlam-Maillardville"] = mapOf(lib to 8519, ndp to 11438, grn to 2467, oth to 175)
            ret["New Westminster"] = mapOf(lib to 5870, ndp to 14377, grn to 6939, oth to 298 + 199)
            ret["Port Coquitlam"] = mapOf(lib to 7582, ndp to 14079, grn to 3237, oth to 248 + 88)
            ret["Port Moody-Coquitlam"] = mapOf(lib to 9910, ndp to 11754, grn to 2985)
            ret["Vancouver-Fairview"] = mapOf(lib to 9436, ndp to 16035, grn to 4007, oth to 149)
            ret["Vancouver-False Creek"] = mapOf(lib to 10370, ndp to 9955, grn to 3880, oth to 213 + 91 + 90)
            ret["Vancouver-Fraserview"] = mapOf(lib to 9985, ndp to 11487, grn to 1826, oth to 179 + 174)
            ret["Vancouver-Hastings"] = mapOf(lib to 5160, ndp to 14351, grn to 4222, oth to 203)
            ret["Vancouver-Kensington"] = mapOf(lib to 7236, ndp to 12504, grn to 2580, oth to 181)
            ret["Vancouver-Kingsway"] = mapOf(lib to 5377, ndp to 12031, grn to 1848, oth to 504 + 85)
            ret["Vancouver-Langara"] = mapOf(lib to 10047, ndp to 8057, grn to 2894, oth to 172)
            ret["Vancouver-Mount Pleasant"] = mapOf(lib to 3917, ndp to 15962, grn to 4136, oth to 212 + 142 + 72)
            ret["Vancouver-Point Grey"] = mapOf(lib to 8414, ndp to 14195, grn to 2604, oth to 84 + 77)
            ret["Vancouver-Quilchena"] = mapOf(lib to 12464, ndp to 6244, grn to 3301, oth to 265)
            ret["Vancouver-West End"] = mapOf(lib to 5064, ndp to 13420, grn to 3059, oth to 352 + 116)
            ret["North Vancouver-Lonsdale"] = mapOf(lib to 10373, ndp to 12361, grn to 4148, oth to 316)
            ret["North Vancouver-Seymour"] = mapOf(lib to 13194, ndp to 9808, grn to 5208, oth to 247)
            ret["Powell River-Sunshine Coast"] = mapOf(lib to 6602, ndp to 13646, grn to 6505, oth to 160)
            ret["West Vancouver-Capilano"] = mapOf(lib to 13596, ndp to 5622, grn to 4575)
            ret["West Vancouver-Sea to Sky"] = mapOf(lib to 10449, ndp to 6532, grn to 6947, oth to 186 + 143)
            ret["Courtenay-Comox"] = mapOf(lib to 10697, ndp to 10886, grn to 5351, oth to 2201)
            ret["Cowichan Valley"] = mapOf(lib to 8400, ndp to 9603, grn to 11475, oth to 502 + 393 + 145 + 124)
            ret["Mid Island-Pacific Rim"] = mapOf(lib to 6578, ndp to 12556, grn to 5206, oth to 878 + 298 + 86)
            ret["Nanaimo"] = mapOf(lib to 8911, ndp to 12746, grn to 5454, oth to 277)
            ret["Nanaimo-North Cowichan"] = mapOf(lib to 7380, ndp to 12275, grn to 6244, oth to 274)
            ret["North Island"] = mapOf(lib to 9148, ndp to 12255, grn to 3846, oth to 543)
            ret["Parksville-Qualicum"] = mapOf(lib to 14468, ndp to 9189, grn to 8157, oth to 245)
            ret["Esquimalt-Metchosin"] = mapOf(lib to 7055, ndp to 11816, grn to 6339, oth to 171 + 102 + 65)
            ret["Langford-Juan de Fuca"] = mapOf(lib to 6544, ndp to 13224, grn to 4795, oth to 262 + 242)
            ret["Oak Bay-Gordon Head"] = mapOf(lib to 6952, ndp to 6912, grn to 15257, oth to 67 + 58)
            ret["Sannich North and the Islands"] = mapOf(lib to 9321, ndp to 10764, grn to 14775, oth to 364)
            ret["Saanich South"] = mapOf(lib to 8716, ndp to 11912, grn to 7129, oth to 177 + 130)
            ret["Victoria-Beacon Hill"] = mapOf(lib to 4689, ndp to 16057, grn to 9194, oth to 190 + 102 + 35)
            ret["Victoria-Swan Lake"] = mapOf(lib to 4005, ndp to 13531, grn to 7491, oth to 207)
            return ret
        }

        private fun bcCurrResult(): Map<String, PartyResult> {
            val ret: MutableMap<String, PartyResult> = TreeMap()
            ret["Nechako Lakes"] = elected(lib)
            ret["North Coast"] = elected(ndp)
            ret["Peace River North"] = elected(lib)
            ret["Peace River South"] = elected(lib)
            ret["Prince George-Mackenzie"] = elected(lib)
            ret["Prince George-Valemount"] = elected(lib)
            ret["Skeena"] = elected(lib)
            ret["Stikine"] = elected(ndp)
            ret["Columbia River-Revelstoke"] = leading(lib)
            ret["Kootenay East"] = elected(lib)
            ret["Kootenay West"] = elected(ndp)
            ret["Nelson-Creston"] = leading(ndp)
            ret["Boundary-Similkameen"] = leading(ndp)
            ret["Kelowna-Lake Country"] = elected(lib)
            ret["Kelowna-Mission"] = elected(lib)
            ret["Kelowna West"] = leading(lib)
            ret["Penticton"] = leading(lib)
            ret["Shuswap"] = elected(lib)
            ret["Vernon-Monashee"] = leading(ndp)
            ret["Cariboo-Chilcotin"] = elected(lib)
            ret["Cariboo North"] = leading(lib)
            ret["Fraser-Nicola"] = leading(lib)
            ret["Kamloops-North Thompson"] = leading(lib)
            ret["Kamloops-South Thompson"] = elected(lib)
            ret["Abbotsford-Mission"] = leading(ndp)
            ret["Abbotsford South"] = leading(lib)
            ret["Abbotsford West"] = leading(lib)
            ret["Chilliwack"] = leading(ndp)
            ret["Chilliwack-Kent"] = leading(ndp)
            ret["Langley"] = leading(ndp)
            ret["Langley East"] = leading(ndp)
            ret["Maple Ridge-Mission"] = elected(ndp)
            ret["Maple Ridge-Pitt Meadows"] = elected(ndp)
            ret["Surrey-Cloverdale"] = elected(ndp)
            ret["Surrey-Fleetwood"] = elected(ndp)
            ret["Surrey-Green Timbers"] = elected(ndp)
            ret["Surrey-Guildford"] = elected(ndp)
            ret["Surrey-Newton"] = elected(ndp)
            ret["Surrey-Panorama"] = elected(ndp)
            ret["Surrey South"] = leading(lib)
            ret["Surrey-Whalley"] = elected(ndp)
            ret["Surrey-White Rock"] = leading(lib)
            ret["Delta North"] = elected(ndp)
            ret["Delta South"] = elected(lib)
            ret["Richmond North Centre"] = elected(lib)
            ret["Richmond-Queensborough"] = leading(ndp)
            ret["Richmond South Centre"] = elected(ndp)
            ret["Richmond-Steveston"] = elected(ndp)
            ret["Burnaby-Deer Lake"] = elected(ndp)
            ret["Burnaby-Edmonds"] = elected(ndp)
            ret["Burnaby-Lougheed"] = elected(ndp)
            ret["Burnaby North"] = elected(ndp)
            ret["Coquitlam-Burke Mountain"] = elected(ndp)
            ret["Coquitlam-Maillardville"] = elected(ndp)
            ret["New Westminster"] = elected(ndp)
            ret["Port Coquitlam"] = elected(ndp)
            ret["Port Moody-Coquitlam"] = elected(ndp)
            ret["Vancouver-Fairview"] = elected(ndp)
            ret["Vancouver-False Creek"] = leading(ndp)
            ret["Vancouver-Fraserview"] = elected(ndp)
            ret["Vancouver-Hastings"] = elected(ndp)
            ret["Vancouver-Kensington"] = elected(ndp)
            ret["Vancouver-Kingsway"] = elected(ndp)
            ret["Vancouver-Langara"] = leading(lib)
            ret["Vancouver-Mount Pleasant"] = elected(ndp)
            ret["Vancouver-Point Grey"] = elected(ndp)
            ret["Vancouver-Quilchena"] = elected(lib)
            ret["Vancouver-West End"] = elected(ndp)
            ret["North Vancouver-Lonsdale"] = elected(ndp)
            ret["North Vancouver-Seymour"] = leading(ndp)
            ret["Powell River-Sunshine Coast"] = elected(ndp)
            ret["West Vancouver-Capilano"] = elected(lib)
            ret["West Vancouver-Sea to Sky"] = leading(lib)
            ret["Courtenay-Comox"] = elected(ndp)
            ret["Cowichan Valley"] = leading(grn)
            ret["Mid Island-Pacific Rim"] = elected(ndp)
            ret["Nanaimo"] = elected(ndp)
            ret["Nanaimo-North Cowichan"] = leading(ndp)
            ret["North Island"] = elected(ndp)
            ret["Parksville-Qualicum"] = leading(ndp)
            ret["Esquimalt-Metchosin"] = elected(ndp)
            ret["Langford-Juan de Fuca"] = elected(ndp)
            ret["Oak Bay-Gordon Head"] = elected(ndp)
            ret["Sannich North and the Islands"] = elected(grn)
            ret["Saanich South"] = elected(ndp)
            ret["Victoria-Beacon Hill"] = elected(ndp)
            ret["Victoria-Swan Lake"] = elected(ndp)
            return ret
        }
    }
}
