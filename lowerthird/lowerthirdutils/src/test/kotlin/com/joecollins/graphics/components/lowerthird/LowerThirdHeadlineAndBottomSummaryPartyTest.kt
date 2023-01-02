package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class LowerThirdHeadlineAndBottomSummaryPartyTest {
    @Test
    fun testRenderHeadlineAndSummarySeatFactory() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val bq = Party("Bloc Quebecois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val lowerThird = LowerThirdHeadlineAndBottomSummary(
            leftImagePublisher =
            LowerThird.createImage(
                LowerThirdHeadlineAndBottomSummaryPartyTest::class.java
                    .classLoader
                    .getResource("com/joecollins/graphics/lowerthird-left.png"),
            )
                .asOneTimePublisher(),
            placePublisher = ("OTTAWA" to ZoneId.of("Canada/Eastern")).asOneTimePublisher(),
            headlinePublisher = "CENTRAL CANADA POLLS CLOSE".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "SEATS WON".asOneTimePublisher(),
            summaryFooterPublisher = "170 FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            SummaryEntries.createSeatEntries(
                mapOf(
                    lib to 2,
                    con to 1,
                ),
                338,
                setOf(bq, con, grn, lib, ndp),
            )
                .asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()),
        )
        lowerThird.setSize(1024, 70)
        compareRendering("LowerThird", "HeadlineAndBottomSummaryHeader", lowerThird)
    }
}
