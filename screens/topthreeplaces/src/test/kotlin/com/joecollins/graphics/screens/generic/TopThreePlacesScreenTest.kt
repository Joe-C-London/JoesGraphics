package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color

class TopThreePlacesScreenTest {

    @Test
    fun testBasic() {
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val lib = Party("Liberal", "LIB", Color.RED)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val island = Party("Island", "IP", Color.RED.darker())
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        val results = listOf(
            mapOf(pc to 1510, grn to 420, lib to 520, ndp to 124),
            mapOf(pc to 1961, grn to 352, lib to 340, ndp to 79, island to 78),
            mapOf(pc to 1245, grn to 1207, lib to 254, ndp to 43),
            mapOf(pc to 1847, grn to 379, lib to 271, ndp to 38, island to 38, ind to 58),
            mapOf(pc to 1899, grn to 349, lib to 282, ndp to 115, island to 44),
            mapOf(pc to 1593, grn to 757, lib to 481, ndp to 29, island to 16),
            mapOf(pc to 2209, ndp to 566),
            mapOf(pc to 1479, grn to 847, lib to 471, ndp to 32),
            mapOf(pc to 1719, grn to 995, ndp to 83, island to 61),
            mapOf(pc to 1903, grn to 483, lib to 321, ndp to 83),
            mapOf(pc to 1750, grn to 775, lib to 611, ndp to 60),
            mapOf(pc to 2294, grn to 463, lib to 169, ndp to 67),
            mapOf(pc to 1351, grn to 1457, lib to 502, ndp to 49, island to 49),
            mapOf(pc to 1990, grn to 559, lib to 532, ndp to 102),
            mapOf(pc to 1418, grn to 639, lib to 560, ndp to 133, island to 25),
            mapOf(pc to 1171, grn to 864, lib to 487, ndp to 202),
            mapOf(pc to 1660, grn to 523, lib to 352, ndp to 125, island to 21),
            mapOf(pc to 978, grn to 1052, lib to 293, ndp to 150, island to 32),
            mapOf(pc to 1042, grn to 301, lib to 1207, ndp to 63, island to 28, ind to 36),
            mapOf(pc to 1861, grn to 553, lib to 540, ndp to 78, ind to 41),
            mapOf(pc to 1532, grn to 132, lib to 896, ndp to 102),
            mapOf(pc to 1384, grn to 271, lib to 543, ndp to 45),
            mapOf(pc to 738, grn to 72, lib to 894, ndp to 702),
            mapOf(pc to 1378, grn to 739, lib to 397, ndp to 70),
            mapOf(pc to 1651, grn to 981, lib to 214, ndp to 45, island to 19),
            mapOf(pc to 939, lib to 1527, ndp to 137),
            mapOf(pc to 1326, grn to 964, lib to 212, ndp to 37, ind to 49),
        )

        val screen = TopThreePlacesScreen.of(
            votes = results.asOneTimePublisher(),
            header = "TOP THREE PLACES".asOneTimePublisher(),
            progressLabel = "OFFICIAL RESULT".asOneTimePublisher(),
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
        )
        screen.setSize(1024, 512)
        compareRendering("TopThreePlacesScreen", "Basic-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOP THREE PLACES [OFFICIAL RESULT]
                PROGRESSIVE CONSERVATIVE: FIRST 22, SECOND 5, THIRD 0
                LIBERAL: FIRST 3, SECOND 3, THIRD 19
                GREEN: FIRST 2, SECOND 18, THIRD 4
                NEW DEMOCRATIC PARTY: FIRST 0, SECOND 1, THIRD 3
            """.trimIndent(),
        )
    }
}
