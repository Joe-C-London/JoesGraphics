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

class SnakeyScreenTest {

    @Test
    fun testBasic() {
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())

        val prevWinners = mapOf(
            4 to pc,
            2 to pc,
            3 to lib,
            7 to pc,
            1 to pc,
            6 to pc,
            5 to lib,
            19 to pc,
            16 to lib,
            17 to grn,
            20 to pc,
            18 to pc,
            9 to lib,
            13 to lib,
            14 to lib,
            11 to lib,
            10 to lib,
            12 to lib,
            8 to lib,
            15 to lib,
            26 to lib,
            24 to lib,
            25 to lib,
            22 to lib,
            21 to lib,
            27 to lib,
            23 to lib,
        )
        val currWinners = Publisher(emptyMap<Int, Party?>())

        val screen = SnakeyScreen.build(
            prevWinners = prevWinners.asOneTimePublisher(),
            currWinners = currWinners,
            header = "SEAT CHANGES".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("SnakeyScreen", "Basic-0", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND
            SEAT CHANGES
            
            GRN TO ?: 1
            LIB TO ?: 18
            PC TO ?: 8
            """.trimIndent(),
        )

        currWinners.submit(
            mapOf(
                4 to pc,
                2 to pc,
                5 to grn,
                3 to pc,
                7 to pc,
                1 to pc,
                6 to pc,
                19 to pc,
                15 to pc,
                16 to lib,
                20 to pc,
                17 to grn,
                18 to pc,
                8 to pc,
                11 to grn,
                13 to grn,
                12 to grn,
                14 to lib,
                10 to lib,
                26 to pc,
                24 to lib,
                25 to lib,
                22 to grn,
                21 to grn,
                27 to lib,
                23 to grn,
            ),
        )
        compareRendering("SnakeyScreen", "Basic-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND
            SEAT CHANGES
            
            GRN TO GRN: 1
            LIB TO GRN: 7
            LIB TO LIB: 6
            LIB TO PC: 4
            LIB TO ?: 1
            PC TO PC: 8
            """.trimIndent(),
        )
    }

    @Test
    fun testOverrideOrder() {
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())

        val prevWinners = mapOf(
            4 to pc,
            2 to pc,
            3 to lib,
            7 to pc,
            1 to pc,
            6 to pc,
            5 to lib,
            19 to pc,
            16 to lib,
            17 to grn,
            20 to pc,
            18 to pc,
            9 to lib,
            13 to lib,
            14 to lib,
            11 to lib,
            10 to lib,
            12 to lib,
            8 to lib,
            15 to lib,
            26 to lib,
            24 to lib,
            25 to lib,
            22 to lib,
            21 to lib,
            27 to lib,
            23 to lib,
        )
        val currWinners = Publisher(emptyMap<Int, Party?>())

        val screen = SnakeyScreen.build(
            prevWinners = prevWinners.asOneTimePublisher(),
            currWinners = currWinners,
            header = "SEAT CHANGES".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
            prevPartyOrder = listOf(lib, pc, grn).asOneTimePublisher(),
            currPartyOrder = listOf(pc, grn, lib).asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("SnakeyScreen", "OverridePartyOrder-0", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND
            SEAT CHANGES
            
            LIB TO ?: 18
            PC TO ?: 8
            GRN TO ?: 1
            """.trimIndent(),
        )

        currWinners.submit(
            mapOf(
                4 to pc,
                2 to pc,
                5 to grn,
                3 to pc,
                7 to pc,
                1 to pc,
                6 to pc,
                19 to pc,
                15 to pc,
                16 to lib,
                20 to pc,
                17 to grn,
                18 to pc,
                8 to pc,
                11 to grn,
                13 to grn,
                12 to grn,
                14 to lib,
                10 to lib,
                26 to pc,
                24 to lib,
                25 to lib,
                22 to grn,
                21 to grn,
                27 to lib,
                23 to grn,
            ),
        )
        compareRendering("SnakeyScreen", "OverridePartyOrder-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND
            SEAT CHANGES
            
            LIB TO PC: 4
            LIB TO GRN: 7
            LIB TO LIB: 6
            LIB TO ?: 1
            PC TO PC: 8
            GRN TO GRN: 1
            """.trimIndent(),
        )
    }
}
