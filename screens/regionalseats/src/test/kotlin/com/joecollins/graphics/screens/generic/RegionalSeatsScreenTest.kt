package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color

class RegionalSeatsScreenTest {

    @Test
    fun testRegionalSeatsCurrPrev() {
        val general = "General"
        val maori = "Maori"

        val curr = mapOf(
            general to Publisher(emptyMap<Party, Int>()),
            maori to Publisher(emptyMap()),
        )
        val prev = mapOf(
            general to Publisher(emptyMap<Party, Int>()),
            maori to Publisher(emptyMap()),
        )
        val totalSeats = mapOf(
            general to Publisher(65),
            maori to Publisher(7),
        )
        val progress = mapOf(
            general to Publisher("0/65"),
            maori to Publisher("0/7"),
        )

        val panel = RegionalSeatsScreen.ofCurrPrev(
            regions = listOf(general, maori),
            title = { "$this electorates".uppercase().asOneTimePublisher() },
            currSeats = { curr[this]!! },
            prevSeats = { prev[this]!! },
            seatHeader = { "SEATS".asOneTimePublisher() },
            changeHeader = { "CHANGE SINCE 2017".asOneTimePublisher() },
            totalSeats = { totalSeats[this]!! },
            progressLabel = { progress[this]!! },
        )
        panel.setSize(1024, 512)
        compareRendering("RegionalSeatsScreen", "CurrPrev-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            GENERAL ELECTORATES
            SEATS [0/65] (CHANGE SINCE 2017)
            
            MAORI ELECTORATES
            SEATS [0/7] (CHANGE SINCE 2017)
            """.trimIndent(),
        )

        val lab = Party("Labour", "LAB", Color.RED)
        val nat = Party("National", "NAT", Color.BLUE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val act = Party("ACT NZ", "ACT", Color.YELLOW)
        val mri = Party("Maori", "MRI", Color.PINK)

        curr[general]!!.submit(
            mapOf(
                lab to 1,
            ),
        )
        prev[general]!!.submit(
            mapOf(
                nat to 1,
            ),
        )
        progress[general]!!.submit("1/65")
        compareRendering("RegionalSeatsScreen", "CurrPrev-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            GENERAL ELECTORATES
            SEATS [1/65] (CHANGE SINCE 2017)
            LABOUR: 1 (+1)
            NATIONAL: - (-1)
            
            MAORI ELECTORATES
            SEATS [0/7] (CHANGE SINCE 2017)
            """.trimIndent(),
        )

        curr[maori]!!.submit(
            mapOf(
                lab to 1,
            ),
        )
        prev[maori]!!.submit(
            mapOf(
                lab to 1,
            ),
        )
        progress[maori]!!.submit("1/7")
        compareRendering("RegionalSeatsScreen", "CurrPrev-3", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            GENERAL ELECTORATES
            SEATS [1/65] (CHANGE SINCE 2017)
            LABOUR: 1 (+1)
            NATIONAL: - (-1)
            
            MAORI ELECTORATES
            SEATS [1/7] (CHANGE SINCE 2017)
            LABOUR: 1 (±0)
            """.trimIndent(),
        )

        curr[general]!!.submit(
            mapOf(
                act to 1,
                grn to 1,
                lab to 40,
                nat to 23,
            ),
        )
        prev[general]!!.submit(
            mapOf(
                act to 1,
                lab to 22,
                nat to 41,
            ),
        )
        progress[general]!!.submit("65/65")

        curr[maori]!!.submit(
            mapOf(
                lab to 7,
            ),
        )
        prev[maori]!!.submit(
            mapOf(
                lab to 6,
                mri to 1,
            ),
        )
        progress[maori]!!.submit("7/7")

        compareRendering("RegionalSeatsScreen", "CurrPrev-4", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            GENERAL ELECTORATES
            SEATS [65/65] (CHANGE SINCE 2017)
            LABOUR: 40 (+18)
            NATIONAL: 23 (-18)
            ACT NZ: 1 (±0)
            GREEN: 1 (+1)
            
            MAORI ELECTORATES
            SEATS [7/7] (CHANGE SINCE 2017)
            LABOUR: 7 (+1)
            MAORI: - (-1)
            """.trimIndent(),
        )
    }
}
