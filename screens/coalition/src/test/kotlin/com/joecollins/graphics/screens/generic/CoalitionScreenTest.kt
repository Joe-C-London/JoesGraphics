package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.CoalitionScreen.Companion.coalition
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color

class CoalitionScreenTest {

    @Test
    fun testCoalitionScreen() {
        val spd = Party("Social Democratic Party", "SPD", Color.RED)
        val union = Party("Christian Democratic Union/Christian Social Union", "CDU/CSU", Color.BLACK)
        val grn = Party("Greens", "GRN", Color.GREEN.darker())
        val fdp = Party("Free Democratic Party", "FDP", Color.YELLOW)
        val afd = Party("Alternative for Germany", "AfD", Color.CYAN.darker())
        val linke = Party("The Left", "LINKE", Color.RED.darker())
        val oth = Party.OTHERS

        val year = Publisher(2021)
        val seats = Publisher(
            mapOf(
                spd to 206,
                union to 152 + 45,
                grn to 118,
                fdp to 92,
                afd to 83,
                linke to 39,
                oth to 1,
            ),
        )
        val total = seats.map { it.values.sum() }

        val panel = CoalitionScreen.of(
            seats = seats,
            totalSeats = total,
            header = year.map { "RESULTS ($it)" },
            subhead = "".asOneTimePublisher(),
            majorityLabel = { "$it FOR MAJORITY" },
            coalitions = listOf(
                coalition("Grand Coalition", setOf(spd, union)),
                coalition("Black-Yellow", setOf(union, fdp)) { (it[fdp] ?: 0) > 0 },
                coalition("Red-Green", setOf(spd, grn)),
                coalition("Jamaica", setOf(union, fdp, grn)) { (it[fdp] ?: 0) > 0 },
                coalition("Red-Green-Red", setOf(spd, grn, linke)),
                coalition("Traffic Light", setOf(spd, fdp, grn)) { (it[fdp] ?: 0) > 0 },
                coalition("Kenya", setOf(union, spd, grn)),
                coalition("German Flag", setOf(union, spd, fdp)) { (it[fdp] ?: 0) > 0 },
            ),
            title = "COALITION BUILDER".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("CoalitionScreen", "Screen-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            COALITION BUILDER
            
            RESULTS (2021)
            GRAND COALITION (SPD, CDU/CSU): 403
            BLACK-YELLOW (CDU/CSU, FDP): 289
            RED-GREEN (SPD, GRN): 324
            JAMAICA (CDU/CSU, GRN, FDP): 407
            RED-GREEN-RED (SPD, GRN, LINKE): 363
            TRAFFIC LIGHT (SPD, GRN, FDP): 416
            KENYA (SPD, CDU/CSU, GRN): 521
            GERMAN FLAG (SPD, CDU/CSU, FDP): 495
            369 FOR MAJORITY
            """.trimIndent(),
        )

        year.submit(2017)
        seats.submit(
            mapOf(
                union to 200 + 46,
                spd to 153,
                afd to 94,
                fdp to 80,
                linke to 69,
                grn to 67,
            ),
        )
        compareRendering("CoalitionScreen", "Screen-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            COALITION BUILDER
            
            RESULTS (2017)
            GRAND COALITION (CDU/CSU, SPD): 399
            BLACK-YELLOW (CDU/CSU, FDP): 326
            RED-GREEN (SPD, GRN): 220
            JAMAICA (CDU/CSU, FDP, GRN): 393
            RED-GREEN-RED (SPD, LINKE, GRN): 289
            TRAFFIC LIGHT (SPD, FDP, GRN): 300
            KENYA (CDU/CSU, SPD, GRN): 466
            GERMAN FLAG (CDU/CSU, SPD, FDP): 479
            355 FOR MAJORITY
            """.trimIndent(),
        )

        year.submit(2013)
        seats.submit(
            mapOf(
                union to 255 + 56,
                spd to 193,
                linke to 64,
                grn to 63,
            ),
        )
        compareRendering("CoalitionScreen", "Screen-3", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            COALITION BUILDER
            
            RESULTS (2013)
            GRAND COALITION (CDU/CSU, SPD): 504
            RED-GREEN (SPD, GRN): 256
            RED-GREEN-RED (SPD, LINKE, GRN): 320
            KENYA (CDU/CSU, SPD, GRN): 567
            316 FOR MAJORITY
            """.trimIndent(),
        )
    }
}
