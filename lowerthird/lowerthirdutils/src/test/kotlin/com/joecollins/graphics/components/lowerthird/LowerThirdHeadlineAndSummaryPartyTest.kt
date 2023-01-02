package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.utils.RenderTestUtils
import com.joecollins.models.general.Party
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class LowerThirdHeadlineAndSummaryPartyTest {

    @Test
    fun testRenderHeadlineAndSummarySeatFactory() {
        val lowerThird = LowerThirdHeadlineAndSummary(
            leftImagePublisher =
            LowerThird.createImage(
                LowerThirdHeadlineAndSummaryPartyTest::class.java
                    .classLoader
                    .getResource("com/joecollins/graphics/lowerthird-left.png"),
            )
                .asOneTimePublisher(),
            placePublisher = ("OTTAWA" to ZoneId.of("Canada/Eastern")).asOneTimePublisher(),
            headlineBinding = "CENTRAL CANADA POLLS CLOSE".asOneTimePublisher(),
            subheadBinding = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryEntriesBinding =
            SummaryEntries.createSeatEntries(
                mapOf(
                    Party("Liberal", "LIB", Color.RED) to 2,
                    Party("Conservative", "CON", Color.BLUE) to 1,
                ),
            )
                .asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()),
        )
        lowerThird.setSize(1024, 50)
        RenderTestUtils.compareRendering("LowerThird", "HeadlineAndSummary", lowerThird)
    }
}
