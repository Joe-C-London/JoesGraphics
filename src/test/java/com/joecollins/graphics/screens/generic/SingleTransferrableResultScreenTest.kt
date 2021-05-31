package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Binding
import com.joecollins.graphics.components.MapFrameTest
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader
import com.joecollins.models.general.Party
import java.awt.Color
import java.awt.Shape
import java.io.IOException
import org.junit.Test

class SingleTransferrableResultScreenTest {

    @Test
    fun testDisplayQuotas() {
        val apni = Party("Alliance", "APNI", Color.ORANGE)
        val uup = Party("Ulster Unionist Party", "UUP", Color.BLUE)
        val dup = Party("Democratic Unionist Party", "DUP", Color.ORANGE.darker())
        val pup = Party("Progressive Unionist Party", "PUP", Color.BLUE.darker())
        val grn = Party("Green", "GRN", Color.GREEN)
        val sf = Party("Sinn F\u00e9in", "SF", Color.GREEN.darker().darker())
        val tuv = Party("Traditional Unionist Voice", "TUV", Color.BLUE.darker().darker())
        val labalt = Party("Labour Alternative", "LAB-ALT", Color.RED.darker())
        val con = Party("NI Conservatives", "CON", Color.CYAN.darker())
        val sdlp = Party("Social Democratic and Labour Party", "SDLP", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Party.OTHERS.color)
        val lab = Party("NI Labour", "LAB", Color.RED)
        val ukip = Party("UK Independence Party", "UKIP", Color.MAGENTA.darker())

        val prevVotes = mapOf(
            dup to 5538 + 4230 + 3875,
            apni to 5428 + 2805 + 2372,
            uup to 3047 + 1095,
            grn to 2183,
            pup to 1772,
            ind to 1099,
            tuv to 887,
            sf to 946,
            Party.OTHERS to 631 + 78,
            labalt to 517,
            con to 477,
            sdlp to 141
        )
        val currQuota = BindableWrapper(1)
        val prevQuota = 5311.0
        val currRound = BindableWrapper(emptyMap<Party, Double>())
        val prevRound = BindableWrapper(prevVotes.mapValues { it.value / prevQuota })
        val currVotes = BindableWrapper(emptyMap<Party, Int>())
        val currWinner = BindableWrapper<Party?>(null)
        val round = BindableWrapper(0)

        val panel = BasicResultPanel.partyQuotas(
            currRound.binding.merge(currQuota.binding) { votes, quota -> votes.mapValues { it.value / quota } },
            Binding.fixedBinding(5),
            Binding.fixedBinding("PARTY SUMMARY"),
            round.binding.map { if (it == 0) "WAITING..." else "COUNT $it" }
        ).withPrev(
            prevRound.binding,
            round.binding.map { if (it <= 1) "CHANGE SINCE 2016" else "CHANGE FROM COUNT ${it - 1}" }
        ).withSwing(
            currVotes.binding,
            Binding.fixedBinding(prevVotes),
            Comparator.comparing { listOf(sf, apni, dup).indexOf(it) },
            Binding.fixedBinding("FIRST PREF SWING")
        ).withPartyMap(
            Binding.fixedBinding(niShapesByConstituency()),
            Binding.fixedBinding(9),
            currWinner.binding,
            Binding.fixedBinding(listOf(9, 10, 12, 15)),
            Binding.fixedBinding("BELFAST")
        ).build(
            Binding.fixedBinding("BELFAST EAST")
        )
        panel.setSize(1024, 512)
        compareRendering("SingleTransferrableResultScreen", "Quotas-0", panel)

        val round1 = mapOf(
            apni to 7610 + 5059,
            uup to 5275,
            dup to 6007 + 4729 + 4431,
            pup to 2658,
            grn to 1447,
            sf to 1173,
            tuv to 917,
            labalt to 442,
            con to 275,
            sdlp to 250,
            ind to 84
        )
        currVotes.value = round1
        val quota = 6727.0
        currRound.value = round1.mapValues { it.value / quota }
        round.value = 1
        currWinner.value = dup
        compareRendering("SingleTransferrableResultScreen", "Quotas-1", panel)

        val round2 = mapOf(
            apni to quota + 5760.03,
            uup to 5313.17,
            dup to 6016.02 + 4734.50 + 4435.18,
            pup to 2673.29,
            grn to 1474.06,
            sf to 1183.34,
            tuv to 918.65,
            labalt to 449.48,
            con to 276.32,
            sdlp to 260.45,
            ind to 85.43
        )
        currRound.value = round2.mapValues { it.value / quota }
        prevRound.value = round1.mapValues { it.value / quota }
        round.value = 2
        compareRendering("SingleTransferrableResultScreen", "Quotas-2", panel)

        val round3 = mapOf(
            apni to quota + 5780.25,
            uup to 5323.39,
            dup to 6021.02 + 4736.50 + 4436.18,
            pup to 2678.51,
            grn to 1500.28,
            sf to 1185.45,
            tuv to 920.65,
            labalt to 456.48,
            con to 278.43,
            sdlp to 260.56
        )
        currRound.value = round3.mapValues { it.value / quota }
        prevRound.value = round2.mapValues { it.value / quota }
        round.value = 3
        compareRendering("SingleTransferrableResultScreen", "Quotas-3", panel)

        val round4 = mapOf(
            apni to quota + 5885.86,
            uup to 5356.60,
            dup to 6026.13 + 4739.50 + 4440.18,
            pup to 2685.73,
            grn to 1539.49,
            sf to 1216.66,
            tuv to 924.76,
            labalt to 467.81,
            con to 280.43
        )
        currRound.value = round4.mapValues { it.value / quota }
        prevRound.value = round3.mapValues { it.value / quota }
        round.value = 4
        compareRendering("SingleTransferrableResultScreen", "Quotas-4", panel)

        val round5 = mapOf(
            apni to quota + 6032.81,
            uup to 5498.59,
            dup to 6057.35 + 4774.50 + 4455.40,
            pup to 2751.95,
            grn to 1763.36,
            sf to 1232.88,
            tuv to 958.87
        )
        currRound.value = round5.mapValues { it.value / quota }
        prevRound.value = round4.mapValues { it.value / quota }
        round.value = 5
        compareRendering("SingleTransferrableResultScreen", "Quotas-5", panel)

        val round6 = mapOf(
            apni to quota + 6072.14,
            uup to 5767.92,
            dup to 6217.46 + 4862.61 + 4568.40,
            pup to 2936.28,
            grn to 1801.58,
            sf to 1235.10
        )
        currRound.value = round6.mapValues { it.value / quota }
        prevRound.value = round5.mapValues { it.value / quota }
        round.value = 6
        compareRendering("SingleTransferrableResultScreen", "Quotas-6", panel)

        val round7 = mapOf(
            apni to quota + 6367.87,
            uup to 5783.14,
            dup to 6228.46 + 4865.61 + 4572.40,
            pup to 2950.28,
            grn to 2080.65
        )
        currRound.value = round7.mapValues { it.value / quota }
        prevRound.value = round6.mapValues { it.value / quota }
        round.value = 7
        compareRendering("SingleTransferrableResultScreen", "Quotas-7", panel)

        val round8 = mapOf(
            apni to quota + 7268.87,
            uup to 6048.62,
            dup to 6275.01 + 4890.72 + 4599.73,
            pup to 3148.79
        )
        currRound.value = round8.mapValues { it.value / quota }
        prevRound.value = round7.mapValues { it.value / quota }
        round.value = 8
        compareRendering("SingleTransferrableResultScreen", "Quotas-8", panel)

        val round9 = mapOf(
            apni to quota + quota,
            uup to 7257.62,
            dup to 6759.01 + 5268.26 + 4995.25
        )
        currRound.value = round9.mapValues { it.value / quota }
        prevRound.value = round8.mapValues { it.value / quota }
        round.value = 9
        compareRendering("SingleTransferrableResultScreen", "Quotas-9", panel)

        val round10 = mapOf(
            apni to quota + quota,
            uup to quota,
            dup to quota + 5333.26 + 5093.25
        )
        currRound.value = round10.mapValues { it.value / quota }
        prevRound.value = round9.mapValues { it.value / quota }
        round.value = 10
        compareRendering("SingleTransferrableResultScreen", "Quotas-10", panel)

        val round11 = mapOf(
            apni to quota + quota,
            uup to quota,
            dup to quota + 5541.65 + 5410.84
        )
        currRound.value = round11.mapValues { it.value / quota }
        prevRound.value = round10.mapValues { it.value / quota }
        round.value = 11
        compareRendering("SingleTransferrableResultScreen", "Quotas-11", panel)
    }

    @Throws(IOException::class)
    private fun niShapesByConstituency(): Map<Int, Shape> {
        val niMap = MapFrameTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/ni-constituencies.shp")
        return ShapefileReader.readShapes(niMap, "OBJECTID", Int::class.java)
    }
}
