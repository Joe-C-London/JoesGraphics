package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class LowerThirdHeadlineAndSummarySingleLabelPartyTest {

    @Test
    fun testRenderHeadlineAndSummarySeatFactory() {
        val lowerThird = LowerThirdHeadlineAndSummarySingleLabel(
            leftImagePublisher =
            LowerThird.createImage(
                LowerThirdHeadlineAndSummarySingleLabelPartyTest::class.java
                    .classLoader
                    .getResource("com/joecollins/graphics/lowerthird-left.png")
            )
                .asOneTimePublisher(),
            placePublisher = ("OTTAWA" to ZoneId.of("Canada/Eastern")).asOneTimePublisher(),
            headlinePublisher = "CENTRAL CANADA POLLS CLOSE".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "170 SEATS FOR MAJORITY".asOneTimePublisher(),
            summaryEntriesPublisher =
            SummaryEntriesSingleLabel.createSeatEntries(
                mapOf(
                    Party("Liberal", "LIB", Color.RED) to 2,
                    Party("Conservative", "CON", Color.BLUE) to 1
                )
            )
                .asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummarySingleLabel", lowerThird)
    }
}
