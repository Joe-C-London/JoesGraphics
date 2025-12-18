package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color

class GainsLossesScreenTest {

    @Test
    fun testBasicGainsLosses() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())

        val prev = mapOf(
            "Alberton-Roseville" to lib,
            "Belfast-Murray River" to lib,
            "Borden-Kinkora" to lib,
            "Charlottetown-Brighton" to lib,
            "Charlottetown-Lewis Point" to lib,
            "Charlottetown-Parkdale" to lib,
            "Charlottetown-Sherwood" to lib,
            "Charlottetown-Victoria Park" to lib,
            "Cornwall-Meadowbank" to lib,
            "Evangeline-Miscouche" to lib,
            "Georgetown-St. Peters" to pc,
            "Kellys Cross-Cumberland" to lib,
            "Kensington-Malpeque" to lib,
            "Montague-Kilmuir" to lib,
            "Morell-Mermaid" to pc,
            "O'Leary-Inverness" to lib,
            "Rustico-Emerald" to lib,
            "Souris-Elmira" to pc,
            "Stratford-Kinlock" to pc,
            "Summerside-St. Eleanors" to lib,
            "Summerside-Wilmot" to lib,
            "Tignish-Palmer Road" to pc,
            "Tracadie-Hillsborough Park" to lib,
            "Tyne Valley-Linkletter" to lib,
            "Vernon River-Stratford" to lib,
            "West Royalty-Springvale" to lib,
            "York-Oyster Bed" to lib,
        )
        val curr = mapOf(
            "Alberton-Roseville" to Publisher<Party?>(null),
            "Belfast-Murray River" to Publisher<Party?>(null),
            "Borden-Kinkora" to Publisher<Party?>(null),
            "Charlottetown-Brighton" to Publisher<Party?>(null),
            "Charlottetown-Lewis Point" to Publisher<Party?>(null),
            "Charlottetown-Parkdale" to Publisher<Party?>(null),
            "Charlottetown-Sherwood" to Publisher<Party?>(null),
            "Charlottetown-Victoria Park" to Publisher<Party?>(null),
            "Cornwall-Meadowbank" to Publisher<Party?>(null),
            "Evangeline-Miscouche" to Publisher<Party?>(null),
            "Georgetown-St. Peters" to Publisher<Party?>(null),
            "Kellys Cross-Cumberland" to Publisher<Party?>(null),
            "Kensington-Malpeque" to Publisher<Party?>(null),
            "Montague-Kilmuir" to Publisher<Party?>(null),
            "Morell-Mermaid" to Publisher<Party?>(null),
            "O'Leary-Inverness" to Publisher<Party?>(null),
            "Rustico-Emerald" to Publisher<Party?>(null),
            "Souris-Elmira" to Publisher<Party?>(null),
            "Stratford-Kinlock" to Publisher<Party?>(null),
            "Summerside-St. Eleanors" to Publisher<Party?>(null),
            "Summerside-Wilmot" to Publisher<Party?>(null),
            "Tignish-Palmer Road" to Publisher<Party?>(null),
            "Tracadie-Hillsborough Park" to Publisher<Party?>(null),
            "Tyne Valley-Linkletter" to Publisher<Party?>(null),
            "Vernon River-Stratford" to Publisher<Party?>(null),
            "West Royalty-Springvale" to Publisher<Party?>(null),
            "York-Oyster Bed" to Publisher<Party?>(null),
        )

        val panel = GainsLossesScreen.of(
            prevWinner = prev.asOneTimePublisher(),
            currWinner = curr.let { Aggregators.toMap(it) },
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
            progressLabel = curr.values.toList().combine().map { v -> "${v.count { it != null }}/${v.size}" },
        )
        panel.setSize(1024, 512)
        compareRendering("GainsLossesScreen", "Basic-0", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND

            GAINS AND LOSSES [0/27]
            """.trimIndent(),
        )

        curr["Belfast-Murray River"]!!.submit(pc)
        compareRendering("GainsLossesScreen", "Basic-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND

            GAINS AND LOSSES [1/27]
            PROGRESSIVE CONSERVATIVE: 1 GAIN, 0 LOSSES
            LIBERAL: 0 GAINS, 1 LOSS
            """.trimIndent(),
        )

        curr["Belfast-Murray River"]!!.submit(pc)
        curr["Georgetown-St. Peters"]!!.submit(pc)
        curr["Montague-Kilmuir"]!!.submit(lib)
        curr["Morell-Mermaid"]!!.submit(pc)
        curr["Souris-Elmira"]!!.submit(pc)
        curr["Stratford-Kinlock"]!!.submit(pc)
        compareRendering("GainsLossesScreen", "Basic-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND

            GAINS AND LOSSES [6/27]
            PROGRESSIVE CONSERVATIVE: 1 GAIN, 0 LOSSES
            LIBERAL: 0 GAINS, 1 LOSS
            """.trimIndent(),
        )

        curr["Alberton-Roseville"]!!.submit(lib)
        curr["Belfast-Murray River"]!!.submit(pc)
        curr["Borden-Kinkora"]!!.submit(pc)
        curr["Charlottetown-Brighton"]!!.submit(lib)
        curr["Charlottetown-Lewis Point"]!!.submit(lib)
        curr["Charlottetown-Parkdale"]!!.submit(lib)
        curr["Charlottetown-Sherwood"]!!.submit(lib)
        curr["Charlottetown-Victoria Park"]!!.submit(lib)
        curr["Cornwall-Meadowbank"]!!.submit(lib)
        curr["Evangeline-Miscouche"]!!.submit(lib)
        curr["Georgetown-St. Peters"]!!.submit(pc)
        curr["Kellys Cross-Cumberland"]!!.submit(grn)
        curr["Kensington-Malpeque"]!!.submit(pc)
        curr["Montague-Kilmuir"]!!.submit(lib)
        curr["Morell-Mermaid"]!!.submit(pc)
        curr["O'Leary-Inverness"]!!.submit(lib)
        curr["Rustico-Emerald"]!!.submit(pc)
        curr["Souris-Elmira"]!!.submit(pc)
        curr["Stratford-Kinlock"]!!.submit(pc)
        curr["Summerside-St. Eleanors"]!!.submit(lib)
        curr["Summerside-Wilmot"]!!.submit(lib)
        curr["Tignish-Palmer Road"]!!.submit(lib)
        curr["Tracadie-Hillsborough Park"]!!.submit(lib)
        curr["Tyne Valley-Linkletter"]!!.submit(lib)
        curr["Vernon River-Stratford"]!!.submit(lib)
        curr["West Royalty-Springvale"]!!.submit(lib)
        curr["York-Oyster Bed"]!!.submit(lib)
        compareRendering("GainsLossesScreen", "Basic-3", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND

            GAINS AND LOSSES [27/27]
            LIBERAL: 1 GAIN, 5 LOSSES
            PROGRESSIVE CONSERVATIVE: 4 GAINS, 1 LOSS
            GREEN: 1 GAIN, 0 LOSSES
            """.trimIndent(),
        )
    }

    @Test
    fun testFilteredGainsLosses() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())

        val prev = mapOf(
            "Alberton-Roseville" to lib,
            "Belfast-Murray River" to lib,
            "Borden-Kinkora" to lib,
            "Charlottetown-Brighton" to lib,
            "Charlottetown-Lewis Point" to lib,
            "Charlottetown-Parkdale" to lib,
            "Charlottetown-Sherwood" to lib,
            "Charlottetown-Victoria Park" to lib,
            "Cornwall-Meadowbank" to lib,
            "Evangeline-Miscouche" to lib,
            "Georgetown-St. Peters" to pc,
            "Kellys Cross-Cumberland" to lib,
            "Kensington-Malpeque" to lib,
            "Montague-Kilmuir" to lib,
            "Morell-Mermaid" to pc,
            "O'Leary-Inverness" to lib,
            "Rustico-Emerald" to lib,
            "Souris-Elmira" to pc,
            "Stratford-Kinlock" to pc,
            "Summerside-St. Eleanors" to lib,
            "Summerside-Wilmot" to lib,
            "Tignish-Palmer Road" to pc,
            "Tracadie-Hillsborough Park" to lib,
            "Tyne Valley-Linkletter" to lib,
            "Vernon River-Stratford" to lib,
            "West Royalty-Springvale" to lib,
            "York-Oyster Bed" to lib,
        )
        val curr = mapOf(
            "Alberton-Roseville" to Publisher(lib),
            "Belfast-Murray River" to Publisher(pc),
            "Borden-Kinkora" to Publisher(pc),
            "Charlottetown-Brighton" to Publisher(lib),
            "Charlottetown-Lewis Point" to Publisher(lib),
            "Charlottetown-Parkdale" to Publisher(lib),
            "Charlottetown-Sherwood" to Publisher(lib),
            "Charlottetown-Victoria Park" to Publisher(lib),
            "Cornwall-Meadowbank" to Publisher(lib),
            "Evangeline-Miscouche" to Publisher(lib),
            "Georgetown-St. Peters" to Publisher(pc),
            "Kellys Cross-Cumberland" to Publisher(grn),
            "Kensington-Malpeque" to Publisher(pc),
            "Montague-Kilmuir" to Publisher(lib),
            "Morell-Mermaid" to Publisher(pc),
            "O'Leary-Inverness" to Publisher(lib),
            "Rustico-Emerald" to Publisher(pc),
            "Souris-Elmira" to Publisher(pc),
            "Stratford-Kinlock" to Publisher(pc),
            "Summerside-St. Eleanors" to Publisher(lib),
            "Summerside-Wilmot" to Publisher(lib),
            "Tignish-Palmer Road" to Publisher(lib),
            "Tracadie-Hillsborough Park" to Publisher(lib),
            "Tyne Valley-Linkletter" to Publisher(lib),
            "Vernon River-Stratford" to Publisher(lib),
            "West Royalty-Springvale" to Publisher(lib),
            "York-Oyster Bed" to Publisher(lib),
        )

        val filter = Publisher<Set<String>?>(null)
        val title = Publisher("PRINCE EDWARD ISLAND")
        val panel = GainsLossesScreen.of(
            prevWinner = prev.asOneTimePublisher(),
            currWinner = curr.let { Aggregators.toMap(it) },
            seatFilter = filter,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("GainsLossesScreen", "Filtered-0", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND

            GAINS AND LOSSES
            LIBERAL: 1 GAIN, 5 LOSSES
            PROGRESSIVE CONSERVATIVE: 4 GAINS, 1 LOSS
            GREEN: 1 GAIN, 0 LOSSES
            """.trimIndent(),
        )

        filter.submit(
            setOf(
                "Belfast-Murray River",
                "Georgetown-St. Peters",
                "Montague-Kilmuir",
                "Morell-Mermaid",
                "Souris-Elmira",
                "Stratford-Kinlock",
            ),
        )
        title.submit("CARDIGAN")
        compareRendering("GainsLossesScreen", "Filtered-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            CARDIGAN

            GAINS AND LOSSES
            PROGRESSIVE CONSERVATIVE: 1 GAIN, 0 LOSSES
            LIBERAL: 0 GAINS, 1 LOSS
            """.trimIndent(),
        )
    }

    @Test
    fun testResultGainsLosses() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())

        val prev = mapOf(
            "Alberton-Roseville" to lib,
            "Belfast-Murray River" to lib,
            "Borden-Kinkora" to lib,
            "Charlottetown-Brighton" to lib,
            "Charlottetown-Lewis Point" to lib,
            "Charlottetown-Parkdale" to lib,
            "Charlottetown-Sherwood" to lib,
            "Charlottetown-Victoria Park" to lib,
            "Cornwall-Meadowbank" to lib,
            "Evangeline-Miscouche" to lib,
            "Georgetown-St. Peters" to pc,
            "Kellys Cross-Cumberland" to lib,
            "Kensington-Malpeque" to lib,
            "Montague-Kilmuir" to lib,
            "Morell-Mermaid" to pc,
            "O'Leary-Inverness" to lib,
            "Rustico-Emerald" to lib,
            "Souris-Elmira" to pc,
            "Stratford-Kinlock" to pc,
            "Summerside-St. Eleanors" to lib,
            "Summerside-Wilmot" to lib,
            "Tignish-Palmer Road" to pc,
            "Tracadie-Hillsborough Park" to lib,
            "Tyne Valley-Linkletter" to lib,
            "Vernon River-Stratford" to lib,
            "West Royalty-Springvale" to lib,
            "York-Oyster Bed" to lib,
        )
        val curr = mapOf(
            "Alberton-Roseville" to Publisher<PartyResult?>(null),
            "Belfast-Murray River" to Publisher<PartyResult?>(null),
            "Borden-Kinkora" to Publisher<PartyResult?>(null),
            "Charlottetown-Brighton" to Publisher<PartyResult?>(null),
            "Charlottetown-Lewis Point" to Publisher<PartyResult?>(null),
            "Charlottetown-Parkdale" to Publisher<PartyResult?>(null),
            "Charlottetown-Sherwood" to Publisher<PartyResult?>(null),
            "Charlottetown-Victoria Park" to Publisher<PartyResult?>(null),
            "Cornwall-Meadowbank" to Publisher<PartyResult?>(null),
            "Evangeline-Miscouche" to Publisher<PartyResult?>(null),
            "Georgetown-St. Peters" to Publisher<PartyResult?>(null),
            "Kellys Cross-Cumberland" to Publisher<PartyResult?>(null),
            "Kensington-Malpeque" to Publisher<PartyResult?>(null),
            "Montague-Kilmuir" to Publisher<PartyResult?>(null),
            "Morell-Mermaid" to Publisher<PartyResult?>(null),
            "O'Leary-Inverness" to Publisher<PartyResult?>(null),
            "Rustico-Emerald" to Publisher<PartyResult?>(null),
            "Souris-Elmira" to Publisher<PartyResult?>(null),
            "Stratford-Kinlock" to Publisher<PartyResult?>(null),
            "Summerside-St. Eleanors" to Publisher<PartyResult?>(null),
            "Summerside-Wilmot" to Publisher<PartyResult?>(null),
            "Tignish-Palmer Road" to Publisher<PartyResult?>(null),
            "Tracadie-Hillsborough Park" to Publisher<PartyResult?>(null),
            "Tyne Valley-Linkletter" to Publisher<PartyResult?>(null),
            "Vernon River-Stratford" to Publisher<PartyResult?>(null),
            "West Royalty-Springvale" to Publisher<PartyResult?>(null),
            "York-Oyster Bed" to Publisher<PartyResult?>(null),
        )

        val panel = GainsLossesScreen.ofResult(
            prevWinner = prev.asOneTimePublisher(),
            currResult = curr.let { Aggregators.toMap(it) },
            title = "PRINCE EDWARD ISLAND".asOneTimePublisher(),
            progressLabel = curr.values.toList().combine().map { v -> "${v.count { it != null }}/${v.size}" },
        )
        panel.setSize(1024, 512)
        compareRendering("GainsLossesScreen", "Basic-0", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND

            GAINS AND LOSSES [0/27]
            """.trimIndent(),
        )

        curr["Belfast-Murray River"]!!.submit(PartyResult.leading(pc))
        compareRendering("GainsLossesScreen", "Result-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND

            GAINS AND LOSSES [1/27]
            PROGRESSIVE CONSERVATIVE: 0/1 GAIN, 0/0 LOSSES
            LIBERAL: 0/0 GAINS, 0/1 LOSS
            """.trimIndent(),
        )

        curr["Belfast-Murray River"]!!.submit(PartyResult.leading(pc))
        curr["Georgetown-St. Peters"]!!.submit(PartyResult.leading(pc))
        curr["Montague-Kilmuir"]!!.submit(PartyResult.leading(lib))
        curr["Morell-Mermaid"]!!.submit(PartyResult.leading(pc))
        curr["Souris-Elmira"]!!.submit(PartyResult.leading(pc))
        curr["Stratford-Kinlock"]!!.submit(PartyResult.elected(pc))
        compareRendering("GainsLossesScreen", "Result-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND

            GAINS AND LOSSES [6/27]
            PROGRESSIVE CONSERVATIVE: 0/1 GAIN, 0/0 LOSSES
            LIBERAL: 0/0 GAINS, 0/1 LOSS
            """.trimIndent(),
        )

        curr["Alberton-Roseville"]!!.submit(PartyResult.elected(lib))
        curr["Belfast-Murray River"]!!.submit(PartyResult.leading(pc))
        curr["Borden-Kinkora"]!!.submit(PartyResult.leading(pc))
        curr["Charlottetown-Brighton"]!!.submit(PartyResult.leading(lib))
        curr["Charlottetown-Lewis Point"]!!.submit(PartyResult.leading(lib))
        curr["Charlottetown-Parkdale"]!!.submit(PartyResult.leading(lib))
        curr["Charlottetown-Sherwood"]!!.submit(PartyResult.leading(lib))
        curr["Charlottetown-Victoria Park"]!!.submit(PartyResult.leading(lib))
        curr["Cornwall-Meadowbank"]!!.submit(PartyResult.leading(lib))
        curr["Evangeline-Miscouche"]!!.submit(PartyResult.elected(lib))
        curr["Georgetown-St. Peters"]!!.submit(PartyResult.leading(pc))
        curr["Kellys Cross-Cumberland"]!!.submit(PartyResult.elected(grn))
        curr["Kensington-Malpeque"]!!.submit(PartyResult.elected(pc))
        curr["Montague-Kilmuir"]!!.submit(PartyResult.leading(lib))
        curr["Morell-Mermaid"]!!.submit(PartyResult.leading(pc))
        curr["O'Leary-Inverness"]!!.submit(PartyResult.leading(lib))
        curr["Rustico-Emerald"]!!.submit(PartyResult.leading(pc))
        curr["Souris-Elmira"]!!.submit(PartyResult.leading(pc))
        curr["Stratford-Kinlock"]!!.submit(PartyResult.elected(pc))
        curr["Summerside-St. Eleanors"]!!.submit(PartyResult.leading(lib))
        curr["Summerside-Wilmot"]!!.submit(PartyResult.leading(lib))
        curr["Tignish-Palmer Road"]!!.submit(PartyResult.elected(lib))
        curr["Tracadie-Hillsborough Park"]!!.submit(PartyResult.leading(lib))
        curr["Tyne Valley-Linkletter"]!!.submit(PartyResult.leading(lib))
        curr["Vernon River-Stratford"]!!.submit(PartyResult.leading(lib))
        curr["West Royalty-Springvale"]!!.submit(PartyResult.leading(lib))
        curr["York-Oyster Bed"]!!.submit(PartyResult.leading(lib))
        compareRendering("GainsLossesScreen", "Result-3", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND

            GAINS AND LOSSES [27/27]
            LIBERAL: 1/1 GAIN, 2/5 LOSSES
            PROGRESSIVE CONSERVATIVE: 1/4 GAINS, 1/1 LOSS
            GREEN: 1/1 GAIN, 0/0 LOSSES
            """.trimIndent(),
        )
    }

    @Test
    fun testFilteredResultGainsLosses() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())

        val prev = mapOf(
            "Alberton-Roseville" to lib,
            "Belfast-Murray River" to lib,
            "Borden-Kinkora" to lib,
            "Charlottetown-Brighton" to lib,
            "Charlottetown-Lewis Point" to lib,
            "Charlottetown-Parkdale" to lib,
            "Charlottetown-Sherwood" to lib,
            "Charlottetown-Victoria Park" to lib,
            "Cornwall-Meadowbank" to lib,
            "Evangeline-Miscouche" to lib,
            "Georgetown-St. Peters" to pc,
            "Kellys Cross-Cumberland" to lib,
            "Kensington-Malpeque" to lib,
            "Montague-Kilmuir" to lib,
            "Morell-Mermaid" to pc,
            "O'Leary-Inverness" to lib,
            "Rustico-Emerald" to lib,
            "Souris-Elmira" to pc,
            "Stratford-Kinlock" to pc,
            "Summerside-St. Eleanors" to lib,
            "Summerside-Wilmot" to lib,
            "Tignish-Palmer Road" to pc,
            "Tracadie-Hillsborough Park" to lib,
            "Tyne Valley-Linkletter" to lib,
            "Vernon River-Stratford" to lib,
            "West Royalty-Springvale" to lib,
            "York-Oyster Bed" to lib,
        )
        val curr = mapOf(
            "Alberton-Roseville" to PartyResult.elected(lib),
            "Belfast-Murray River" to PartyResult.leading(pc),
            "Borden-Kinkora" to PartyResult.leading(pc),
            "Charlottetown-Brighton" to PartyResult.leading(lib),
            "Charlottetown-Lewis Point" to PartyResult.leading(lib),
            "Charlottetown-Parkdale" to PartyResult.leading(lib),
            "Charlottetown-Sherwood" to PartyResult.leading(lib),
            "Charlottetown-Victoria Park" to PartyResult.leading(lib),
            "Cornwall-Meadowbank" to PartyResult.leading(lib),
            "Evangeline-Miscouche" to PartyResult.elected(lib),
            "Georgetown-St. Peters" to PartyResult.leading(pc),
            "Kellys Cross-Cumberland" to PartyResult.elected(grn),
            "Kensington-Malpeque" to PartyResult.elected(pc),
            "Montague-Kilmuir" to PartyResult.leading(lib),
            "Morell-Mermaid" to PartyResult.leading(pc),
            "O'Leary-Inverness" to PartyResult.leading(lib),
            "Rustico-Emerald" to PartyResult.leading(pc),
            "Souris-Elmira" to PartyResult.leading(pc),
            "Stratford-Kinlock" to PartyResult.elected(pc),
            "Summerside-St. Eleanors" to PartyResult.leading(lib),
            "Summerside-Wilmot" to PartyResult.leading(lib),
            "Tignish-Palmer Road" to PartyResult.elected(lib),
            "Tracadie-Hillsborough Park" to PartyResult.leading(lib),
            "Tyne Valley-Linkletter" to PartyResult.leading(lib),
            "Vernon River-Stratford" to PartyResult.leading(lib),
            "West Royalty-Springvale" to PartyResult.leading(lib),
            "York-Oyster Bed" to PartyResult.leading(lib),
        )

        val filter = Publisher<Set<String>?>(null)
        val title = Publisher("PRINCE EDWARD ISLAND")
        val panel = GainsLossesScreen.ofResult(
            prevWinner = prev.asOneTimePublisher(),
            currResult = curr.asOneTimePublisher(),
            seatFilter = filter,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("GainsLossesScreen", "FilteredResult-0", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND

            GAINS AND LOSSES
            LIBERAL: 1/1 GAIN, 2/5 LOSSES
            PROGRESSIVE CONSERVATIVE: 1/4 GAINS, 1/1 LOSS
            GREEN: 1/1 GAIN, 0/0 LOSSES
            """.trimIndent(),
        )

        filter.submit(
            setOf(
                "Belfast-Murray River",
                "Georgetown-St. Peters",
                "Montague-Kilmuir",
                "Morell-Mermaid",
                "Souris-Elmira",
                "Stratford-Kinlock",
            ),
        )
        title.submit("CARDIGAN")
        compareRendering("GainsLossesScreen", "FilteredResult-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            CARDIGAN

            GAINS AND LOSSES
            PROGRESSIVE CONSERVATIVE: 0/1 GAIN, 0/0 LOSSES
            LIBERAL: 0/0 GAINS, 0/1 LOSS
            """.trimIndent(),
        )
    }

    @Test
    fun testPartyChanges() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val con = Party("Conservative", "CON", Color.BLUE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())

        val prev = mapOf(
            "Alberton-Roseville" to lib,
            "Belfast-Murray River" to lib,
            "Borden-Kinkora" to lib,
            "Charlottetown-Brighton" to lib,
            "Charlottetown-Lewis Point" to lib,
            "Charlottetown-Parkdale" to lib,
            "Charlottetown-Sherwood" to lib,
            "Charlottetown-Victoria Park" to lib,
            "Cornwall-Meadowbank" to lib,
            "Evangeline-Miscouche" to lib,
            "Georgetown-St. Peters" to pc,
            "Kellys Cross-Cumberland" to lib,
            "Kensington-Malpeque" to lib,
            "Montague-Kilmuir" to lib,
            "Morell-Mermaid" to pc,
            "O'Leary-Inverness" to lib,
            "Rustico-Emerald" to lib,
            "Souris-Elmira" to pc,
            "Stratford-Kinlock" to pc,
            "Summerside-St. Eleanors" to lib,
            "Summerside-Wilmot" to lib,
            "Tignish-Palmer Road" to pc,
            "Tracadie-Hillsborough Park" to lib,
            "Tyne Valley-Linkletter" to lib,
            "Vernon River-Stratford" to lib,
            "West Royalty-Springvale" to lib,
            "York-Oyster Bed" to lib,
        )
        val curr = mapOf(
            "Alberton-Roseville" to PartyResult.elected(lib),
            "Belfast-Murray River" to PartyResult.leading(con),
            "Borden-Kinkora" to PartyResult.leading(con),
            "Charlottetown-Brighton" to PartyResult.leading(lib),
            "Charlottetown-Lewis Point" to PartyResult.leading(lib),
            "Charlottetown-Parkdale" to PartyResult.leading(lib),
            "Charlottetown-Sherwood" to PartyResult.leading(lib),
            "Charlottetown-Victoria Park" to PartyResult.leading(lib),
            "Cornwall-Meadowbank" to PartyResult.leading(lib),
            "Evangeline-Miscouche" to PartyResult.elected(lib),
            "Georgetown-St. Peters" to PartyResult.leading(con),
            "Kellys Cross-Cumberland" to PartyResult.elected(grn),
            "Kensington-Malpeque" to PartyResult.elected(con),
            "Montague-Kilmuir" to PartyResult.leading(lib),
            "Morell-Mermaid" to PartyResult.leading(con),
            "O'Leary-Inverness" to PartyResult.leading(lib),
            "Rustico-Emerald" to PartyResult.leading(con),
            "Souris-Elmira" to PartyResult.leading(con),
            "Stratford-Kinlock" to PartyResult.elected(con),
            "Summerside-St. Eleanors" to PartyResult.leading(lib),
            "Summerside-Wilmot" to PartyResult.leading(lib),
            "Tignish-Palmer Road" to PartyResult.elected(lib),
            "Tracadie-Hillsborough Park" to PartyResult.leading(lib),
            "Tyne Valley-Linkletter" to PartyResult.leading(lib),
            "Vernon River-Stratford" to PartyResult.leading(lib),
            "West Royalty-Springvale" to PartyResult.leading(lib),
            "York-Oyster Bed" to PartyResult.leading(lib),
        )

        val filter = Publisher<Set<String>?>(null)
        val title = Publisher("PRINCE EDWARD ISLAND")
        val panel = GainsLossesScreen.ofResult(
            prevWinner = prev.asOneTimePublisher(),
            currResult = curr.asOneTimePublisher(),
            partyChanges = mapOf(pc to con).asOneTimePublisher(),
            seatFilter = filter,
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("GainsLossesScreen", "PartyChanges-0", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            PRINCE EDWARD ISLAND

            GAINS AND LOSSES
            LIBERAL: 1/1 GAIN, 2/5 LOSSES
            CONSERVATIVE: 1/4 GAINS, 1/1 LOSS
            GREEN: 1/1 GAIN, 0/0 LOSSES
            """.trimIndent(),
        )
    }
}
