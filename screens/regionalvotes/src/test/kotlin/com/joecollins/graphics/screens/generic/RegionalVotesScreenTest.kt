package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color

class RegionalVotesScreenTest {

    @Test
    fun testRegionalVotesCurrPrev() {
        val general = "General"
        val maori = "Maori"

        val curr = mapOf(
            general to Publisher(emptyMap<Party, Int>()),
            maori to Publisher(emptyMap<Party, Int>()),
        )
        val prev = mapOf(
            general to Publisher(emptyMap<Party, Int>()),
            maori to Publisher(emptyMap<Party, Int>()),
        )
        val reporting = mapOf(
            general to Publisher(0.0),
            maori to Publisher(0.0),
        )
        val progress = mapOf(
            general to Publisher("0/65"),
            maori to Publisher("0/7"),
        )

        val panel = RegionalVotesScreen.ofCurrPrev(
            listOf(general, maori),
            { "$it electorates".uppercase().asOneTimePublisher() },
            { curr[it]!! },
            { prev[it]!! },
            { "PARTY VOTES".asOneTimePublisher() },
            { "CHANGE SINCE 2017".asOneTimePublisher() },
        )
            .withPctReporting { reporting[it]!! }
            .withProgressLabel { progress[it]!! }
            .build()
        panel.setSize(1024, 512)
        compareRendering("RegionalVotesScreen", "CurrPrev-1", panel)
        assertPublishes(
            panel.altText,
            """
            GENERAL ELECTORATES
            PARTY VOTES [0/65] (CHANGE SINCE 2017)
            
            MAORI ELECTORATES
            PARTY VOTES [0/7] (CHANGE SINCE 2017)
            """.trimIndent(),
        )

        val lab = Party("Labour", "LAB", Color.RED)
        val nat = Party("National", "NAT", Color.BLUE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val act = Party("ACT NZ", "ACT", Color.YELLOW)
        val nzf = Party("NZ First", "NZF", Color.BLACK)
        val mri = Party("Maori", "MRI", Color.PINK)
        val oth = Party.OTHERS

        curr[general]!!.submit(
            mapOf(
                act to 2724,
                grn to 6937,
                lab to 16751,
                nat to 7680,
                nzf to 622,
                oth to 1529,
            ),
        )
        prev[general]!!.submit(
            mapOf(
                act to 317,
                grn to 4170,
                lab to 11340,
                nat to 11773,
                nzf to 1165,
                oth to 1242,
            ),
        )
        reporting[general]!!.submit(1.0 / 65)
        progress[general]!!.submit("1/65")
        compareRendering("RegionalVotesScreen", "CurrPrev-2", panel)
        assertPublishes(
            panel.altText,
            """
            GENERAL ELECTORATES
            PARTY VOTES [1/65] (CHANGE SINCE 2017)
            LABOUR: 46.2% (+8.4%)
            NATIONAL: 21.2% (-18.0%)
            GREEN: 19.1% (+5.2%)
            ACT NZ: 7.5% (+6.5%)
            NZ FIRST: 1.7% (-2.2%)
            OTHERS: 4.2% (+0.1%)
            
            MAORI ELECTORATES
            PARTY VOTES [0/7] (CHANGE SINCE 2017)
            """.trimIndent(),
        )

        curr[maori]!!.submit(
            mapOf(
                grn to 1557,
                lab to 15884,
                mri to 3008,
                nat to 915,
                nzf to 898,
                oth to 2275,
            ),
        )
        prev[maori]!!.submit(
            mapOf(
                grn to 1193,
                lab to 14279,
                mri to 2635,
                nat to 1594,
                nzf to 1936,
                oth to 1177,
            ),
        )
        reporting[maori]!!.submit(1.0 / 7)
        progress[maori]!!.submit("1/7")
        compareRendering("RegionalVotesScreen", "CurrPrev-3", panel)
        assertPublishes(
            panel.altText,
            """
            GENERAL ELECTORATES
            PARTY VOTES [1/65] (CHANGE SINCE 2017)
            LABOUR: 46.2% (+8.4%)
            NATIONAL: 21.2% (-18.0%)
            GREEN: 19.1% (+5.2%)
            ACT NZ: 7.5% (+6.5%)
            NZ FIRST: 1.7% (-2.2%)
            OTHERS: 4.2% (+0.1%)
            
            MAORI ELECTORATES
            PARTY VOTES [1/7] (CHANGE SINCE 2017)
            LABOUR: 64.7% (+2.1%)
            MAORI: 12.3% (+0.7%)
            GREEN: 6.3% (+1.1%)
            NATIONAL: 3.7% (-3.3%)
            NZ FIRST: 3.7% (-4.8%)
            OTHERS: 9.3% (+4.1%)
            """.trimIndent(),
        )

        curr[general]!!.submit(
            mapOf(
                act to 216444,
                grn to 211876,
                lab to 1327675,
                nat to 731811,
                nzf to 67113,
                oth to 145337,
            ),
        )
        prev[general]!!.submit(
            mapOf(
                act to 12865,
                grn to 152638,
                lab to 858903,
                nat to 1140489,
                nzf to 172966,
                oth to 92167,
            ),
        )
        reporting[general]!!.submit(1.0)
        progress[general]!!.submit("65/65")

        curr[maori]!!.submit(
            mapOf(
                grn to 14881,
                lab to 115870,
                mri to 23820,
                nat to 6464,
                nzf to 7907,
                oth to 17222,
            ),
        )
        prev[maori]!!.submit(
            mapOf(
                grn to 9805,
                lab to 97281,
                mri to 19774,
                nat to 11586,
                nzf to 13740,
                oth to 9682,
            ),
        )
        reporting[maori]!!.submit(1.0)
        progress[maori]!!.submit("7/7")

        compareRendering("RegionalVotesScreen", "CurrPrev-4", panel)
        assertPublishes(
            panel.altText,
            """
            GENERAL ELECTORATES
            PARTY VOTES [65/65] (CHANGE SINCE 2017)
            LABOUR: 49.2% (+13.8%)
            NATIONAL: 27.1% (-19.8%)
            ACT NZ: 8.0% (+7.5%)
            GREEN: 7.8% (+1.6%)
            NZ FIRST: 2.5% (-4.6%)
            OTHERS: 5.4% (+1.6%)
            
            MAORI ELECTORATES
            PARTY VOTES [7/7] (CHANGE SINCE 2017)
            LABOUR: 62.2% (+2.1%)
            MAORI: 12.8% (+0.6%)
            GREEN: 8.0% (+1.9%)
            NZ FIRST: 4.2% (-4.2%)
            NATIONAL: 3.5% (-3.7%)
            OTHERS: 9.3% (+3.3%)
            """.trimIndent(),
        )
    }
}
