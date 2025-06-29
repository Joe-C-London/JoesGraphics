package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.PollsReporting
import com.joecollins.models.general.ReferendumOption
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color

class SimpleReferendumVoteViewPanelTest {

    enum class EuReferendumOption(override val description: String, override val color: Color) : ReferendumOption {
        REMAIN("Remain a Member of the European Union", Color.BLUE),
        LEAVE("Leave the European Union", Color.RED),
    }

    @Test
    fun testReferendumVoteView() {
        val votes = Publisher(mapOf(EuReferendumOption.REMAIN to 0, EuReferendumOption.LEAVE to 0))
        val reporting = Publisher(PollsReporting(0, 382))
        val showWinningLine = Publisher(true)
        val winner = Publisher(null as EuReferendumOption?)
        val title = Publisher("UNITED KINGDOM")

        val panel = SimpleVoteViewPanel.referendumVotes<EuReferendumOption>(
            current = {
                this.votes = votes
                this.winner = winner
                header = "EU REFERENDUM".asOneTimePublisher()
                subhead = "VOTES COUNTED".asOneTimePublisher()
                this.pctReporting = reporting.map { it.toPct() }
                this.progressLabel = reporting.map { "${it.reporting} OF ${it.total}" }
            },
            winningLine = {
                show(showWinningLine)
                majority { "50% TO WIN" }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "Referendum-1", panel)
        assertPublishes(
            panel.altText,
            """
                UNITED KINGDOM

                EU REFERENDUM [0 OF 382], VOTES COUNTED
                REMAIN A MEMBER OF THE EUROPEAN UNION: WAITING...
                LEAVE THE EUROPEAN UNION: WAITING...
                50% TO WIN
            """.trimIndent(),
        )

        votes.submit(mapOf(EuReferendumOption.REMAIN to 19322, EuReferendumOption.LEAVE to 823))
        reporting.submit(PollsReporting(1, 382))
        compareRendering("SimpleVoteViewPanel", "Referendum-2", panel)
        assertPublishes(
            panel.altText,
            """
                UNITED KINGDOM

                EU REFERENDUM [1 OF 382], VOTES COUNTED
                REMAIN A MEMBER OF THE EUROPEAN UNION: 19,322 (95.9%)
                LEAVE THE EUROPEAN UNION: 823 (4.1%)
                50% TO WIN
            """.trimIndent(),
        )

        showWinningLine.submit(false)
        reporting.submit(PollsReporting(1, 56))
        title.submit("OUTSIDE ENGLAND")
        compareRendering("SimpleVoteViewPanel", "Referendum-3", panel)
        assertPublishes(
            panel.altText,
            """
                OUTSIDE ENGLAND

                EU REFERENDUM [1 OF 56], VOTES COUNTED
                REMAIN A MEMBER OF THE EUROPEAN UNION: 19,322 (95.9%)
                LEAVE THE EUROPEAN UNION: 823 (4.1%)
            """.trimIndent(),
        )

        votes.submit(mapOf(EuReferendumOption.REMAIN to 460029, EuReferendumOption.LEAVE to 350265))
        reporting.submit(PollsReporting(2, 56))
        compareRendering("SimpleVoteViewPanel", "Referendum-4", panel)
        assertPublishes(
            panel.altText,
            """
                OUTSIDE ENGLAND

                EU REFERENDUM [2 OF 56], VOTES COUNTED
                REMAIN A MEMBER OF THE EUROPEAN UNION: 460,029 (56.8%)
                LEAVE THE EUROPEAN UNION: 350,265 (43.2%)
            """.trimIndent(),
        )

        votes.submit(mapOf(EuReferendumOption.REMAIN to 1232376, EuReferendumOption.LEAVE to 1204837))
        reporting.submit(PollsReporting(24, 56))
        compareRendering("SimpleVoteViewPanel", "Referendum-5", panel)
        assertPublishes(
            panel.altText,
            """
                OUTSIDE ENGLAND

                EU REFERENDUM [24 OF 56], VOTES COUNTED
                REMAIN A MEMBER OF THE EUROPEAN UNION: 1,232,376 (50.6%)
                LEAVE THE EUROPEAN UNION: 1,204,837 (49.4%)
            """.trimIndent(),
        )

        showWinningLine.submit(true)
        reporting.submit(PollsReporting(24, 382))
        title.submit("UNITED KINGDOM")
        compareRendering("SimpleVoteViewPanel", "Referendum-6", panel)
        assertPublishes(
            panel.altText,
            """
                UNITED KINGDOM

                EU REFERENDUM [24 OF 382], VOTES COUNTED
                REMAIN A MEMBER OF THE EUROPEAN UNION: 1,232,376 (50.6%)
                LEAVE THE EUROPEAN UNION: 1,204,837 (49.4%)
                50% TO WIN
            """.trimIndent(),
        )

        votes.submit(mapOf(EuReferendumOption.REMAIN to 2893567, EuReferendumOption.LEAVE to 2223159))
        reporting.submit(PollsReporting(56, 382))
        compareRendering("SimpleVoteViewPanel", "Referendum-7", panel)
        assertPublishes(
            panel.altText,
            """
                UNITED KINGDOM

                EU REFERENDUM [56 OF 382], VOTES COUNTED
                REMAIN A MEMBER OF THE EUROPEAN UNION: 2,893,567 (56.6%)
                LEAVE THE EUROPEAN UNION: 2,223,159 (43.4%)
                50% TO WIN
            """.trimIndent(),
        )

        votes.submit(mapOf(EuReferendumOption.REMAIN to 16141241, EuReferendumOption.LEAVE to 17410742))
        reporting.submit(PollsReporting(382, 382))
        winner.submit(EuReferendumOption.LEAVE)
        compareRendering("SimpleVoteViewPanel", "Referendum-8", panel)
        assertPublishes(
            panel.altText,
            """
                UNITED KINGDOM

                EU REFERENDUM [382 OF 382], VOTES COUNTED
                REMAIN A MEMBER OF THE EUROPEAN UNION: 16,141,241 (48.1%)
                LEAVE THE EUROPEAN UNION: 17,410,742 (51.9%) WINNER
                50% TO WIN
            """.trimIndent(),
        )
    }
}
