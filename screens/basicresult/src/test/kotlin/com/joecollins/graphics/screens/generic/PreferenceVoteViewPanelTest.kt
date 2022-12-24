package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateVotes
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyRangeVotes
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color

class PreferenceVoteViewPanelTest {
    @Test
    fun testCandidateBasicResult() {
        val alp = Candidate("Mark Monaghan", Party("Labor", "ALP", Color.RED))
        val clp = Candidate("Kylie Bonanni", Party("Country Liberal", "CLP", Color.ORANGE))
        val ta = Candidate("Jeff Collins", Party("Territory Alliance", "TA", Color.BLUE), true)
        val ind = Candidate("Amye Un", Party("Independent", "IND", Color.GRAY))
        val currentPrimaryVotes = Publisher(
            mapOf(
                alp to 1756,
                clp to 1488,
                ta to 497,
                ind to 434
            )
        )
        val previousPrimaryVotes = Publisher(
            mapOf(
                alp.party to 1802,
                clp.party to 1439,
                ta.party to 356,
                ind.party to 384
            )
        )
        val current2CPVotes = Publisher(
            mapOf(
                alp to 2197,
                clp to 1978
            )
        )
        val previous2PPVotes = Publisher(
            mapOf(
                alp.party to 2171,
                clp.party to 1588
            )
        )
        val header = Publisher("FONG LIM")
        val voteHeader = Publisher("PRIMARY VOTE")
        val voteSubhead = Publisher("9 OF 9 POLLS REPORTING")
        val preferenceHeader = Publisher("TWO CANDIDATE PREFERRED")
        val preferenceSubhead = Publisher("9 OF 9 POLLS REPORTING")
        val changeHeader = Publisher("PRIMARY CHANGE SINCE 2016")
        val swingHeader = Publisher("PREFERENCE SWING SINCE 2016")
        val leader = Publisher(alp)
        val swingPartyOrder = listOf(alp.party, ta.party, clp.party)
        val panel = candidateVotes(
            currentPrimaryVotes,
            voteHeader,
            voteSubhead
        )
            .withPrev(previousPrimaryVotes, changeHeader)
            .withPreferences(current2CPVotes, preferenceHeader, preferenceSubhead)
            .withPrevPreferences(previous2PPVotes)
            .withWinner(leader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "Basic-1", panel)
        assertPublishes(
            panel.altText,
            """
                FONG LIM
                
                PRIMARY VOTE, 9 OF 9 POLLS REPORTING (PRIMARY CHANGE SINCE 2016)
                MARK MONAGHAN (ALP): 1,756 (42.1%, -3.2%) WINNER
                KYLIE BONANNI (CLP): 1,488 (35.6%, -0.5%)
                JEFF COLLINS (TA): 497 (11.9%, +3.0%)
                AMYE UN (IND): 434 (10.4%, +0.7%)
                
                TWO CANDIDATE PREFERRED, 9 OF 9 POLLS REPORTING
                MARK MONAGHAN (ALP): 2,197 (52.6%) WINNER
                KYLIE BONANNI (CLP): 1,978 (47.4%)
                
                PREFERENCE SWING SINCE 2016: 5.1% SWING ALP TO CLP
            """.trimIndent()
        )
    }

    @Test
    fun testCandidateScreenUpdating() {
        val alp = Candidate("Mark Monaghan", Party("Labor", "ALP", Color.RED))
        val clp = Candidate("Kylie Bonanni", Party("Country Liberal", "CLP", Color.ORANGE))
        val ta = Candidate("Jeff Collins", Party("Territory Alliance", "TA", Color.BLUE), true)
        val ind = Candidate("Amye Un", Party("Independent", "IND", Color.GRAY))
        val currentPrimaryVotes = Publisher(sequenceOf(alp, clp, ta, ind).associateWith { 0 })
        val previousPrimaryVotes = Publisher(
            mapOf(
                alp.party to 1802,
                clp.party to 1439,
                ta.party to 356,
                ind.party to 384
            )
        )
        val current2CPVotes = Publisher(emptyMap<Candidate, Int>())
        val previous2PPVotes = Publisher(
            mapOf(
                alp.party to 2171,
                clp.party to 1588
            )
        )
        val pctReporting = Publisher(0.0)
        val preferencePctReporting = Publisher(0.0)
        val header = Publisher("FONG LIM")
        val voteHeader = Publisher("PRIMARY VOTE")
        val voteSubhead = Publisher("0 OF 9 POLLS REPORTING")
        val preferenceHeader = Publisher("TWO CANDIDATE PREFERRED")
        val preferenceSubhead = Publisher("0 OF 9 POLLS REPORTING")
        val changeHeader = Publisher("PRIMARY CHANGE SINCE 2016")
        val swingHeader = Publisher("PREFERENCE SWING SINCE 2016")
        val leader = Publisher<Candidate?>(null)
        val swingPartyOrder = listOf(alp.party, ta.party, clp.party)
        val panel = candidateVotes(
            currentPrimaryVotes,
            voteHeader,
            voteSubhead
        )
            .withPrev(previousPrimaryVotes, changeHeader)
            .withPctReporting(pctReporting)
            .withPreferences(current2CPVotes, preferenceHeader, preferenceSubhead)
            .withPrevPreferences(previous2PPVotes)
            .withPreferencePctReporting(preferencePctReporting)
            .withWinner(leader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "Update-1", panel)
        assertPublishes(
            panel.altText,
            """
                FONG LIM
                
                PRIMARY VOTE, 0 OF 9 POLLS REPORTING (PRIMARY CHANGE SINCE 2016)
                MARK MONAGHAN (ALP): WAITING...
                KYLIE BONANNI (CLP): WAITING...
                JEFF COLLINS (TA): WAITING...
                AMYE UN (IND): WAITING...
                
                TWO CANDIDATE PREFERRED, 0 OF 9 POLLS REPORTING
                
                PREFERENCE SWING SINCE 2016: NOT AVAILABLE
            """.trimIndent()
        )

        currentPrimaryVotes.submit(
            mapOf(
                alp to 13,
                clp to 13,
                ta to 6,
                ind to 5
            )
        )
        current2CPVotes.submit(sequenceOf(alp, clp).associateWith { 0 })
        pctReporting.submit(1.0 / 9)
        voteSubhead.submit("1 OF 9 POLLS REPORTING")
        compareRendering("PreferenceVoteViewPanel", "Update-2", panel)
        assertPublishes(
            panel.altText,
            """
                FONG LIM
                
                PRIMARY VOTE, 1 OF 9 POLLS REPORTING (PRIMARY CHANGE SINCE 2016)
                MARK MONAGHAN (ALP): 13 (35.1%, -10.1%)
                KYLIE BONANNI (CLP): 13 (35.1%, -1.0%)
                JEFF COLLINS (TA): 6 (16.2%, +7.3%)
                AMYE UN (IND): 5 (13.5%, +3.9%)
                
                TWO CANDIDATE PREFERRED, 0 OF 9 POLLS REPORTING
                MARK MONAGHAN (ALP): WAITING...
                KYLIE BONANNI (CLP): WAITING...
                
                PREFERENCE SWING SINCE 2016: NOT AVAILABLE
            """.trimIndent()
        )

        currentPrimaryVotes.submit(
            mapOf(
                alp to 365,
                clp to 262,
                ta to 86,
                ind to 83
            )
        )
        current2CPVotes.submit(
            mapOf(
                alp to 18,
                clp to 19
            )
        )
        pctReporting.submit(3.0 / 9)
        voteSubhead.submit("3 OF 9 POLLS REPORTING")
        preferencePctReporting.submit(1.0 / 9)
        preferenceSubhead.submit("1 OF 9 POLLS REPORTING")
        compareRendering("PreferenceVoteViewPanel", "Update-3", panel)
        assertPublishes(
            panel.altText,
            """
                FONG LIM
                
                PRIMARY VOTE, 3 OF 9 POLLS REPORTING (PRIMARY CHANGE SINCE 2016)
                MARK MONAGHAN (ALP): 365 (45.9%, +0.6%)
                KYLIE BONANNI (CLP): 262 (32.9%, -3.2%)
                JEFF COLLINS (TA): 86 (10.8%, +1.9%)
                AMYE UN (IND): 83 (10.4%, +0.8%)
                
                TWO CANDIDATE PREFERRED, 1 OF 9 POLLS REPORTING
                KYLIE BONANNI (CLP): 19 (51.4%)
                MARK MONAGHAN (ALP): 18 (48.6%)
                
                PREFERENCE SWING SINCE 2016: 9.1% SWING ALP TO CLP
            """.trimIndent()
        )

        currentPrimaryVotes.submit(
            mapOf(
                alp to 1756,
                clp to 1488,
                ta to 497,
                ind to 434
            )
        )
        current2CPVotes.submit(
            mapOf(
                alp to 464,
                clp to 332
            )
        )
        pctReporting.submit(9.0 / 9)
        voteSubhead.submit("9 OF 9 POLLS REPORTING")
        preferencePctReporting.submit(3.0 / 9)
        preferenceSubhead.submit("3 OF 9 POLLS REPORTING")
        leader.submit(alp)
        compareRendering("PreferenceVoteViewPanel", "Update-4", panel)
        assertPublishes(
            panel.altText,
            """
                FONG LIM
                
                PRIMARY VOTE, 9 OF 9 POLLS REPORTING (PRIMARY CHANGE SINCE 2016)
                MARK MONAGHAN (ALP): 1,756 (42.1%, -3.2%) WINNER
                KYLIE BONANNI (CLP): 1,488 (35.6%, -0.5%)
                JEFF COLLINS (TA): 497 (11.9%, +3.0%)
                AMYE UN (IND): 434 (10.4%, +0.7%)
                
                TWO CANDIDATE PREFERRED, 3 OF 9 POLLS REPORTING
                MARK MONAGHAN (ALP): 464 (58.3%) WINNER
                KYLIE BONANNI (CLP): 332 (41.7%)
                
                PREFERENCE SWING SINCE 2016: 0.5% SWING CLP TO ALP
            """.trimIndent()
        )

        current2CPVotes.submit(
            mapOf(
                alp to 2197,
                clp to 1978
            )
        )
        preferencePctReporting.submit(9.0 / 9)
        preferenceSubhead.submit("9 OF 9 POLLS REPORTING")
        compareRendering("PreferenceVoteViewPanel", "Update-5", panel)
        assertPublishes(
            panel.altText,
            """
                FONG LIM
                
                PRIMARY VOTE, 9 OF 9 POLLS REPORTING (PRIMARY CHANGE SINCE 2016)
                MARK MONAGHAN (ALP): 1,756 (42.1%, -3.2%) WINNER
                KYLIE BONANNI (CLP): 1,488 (35.6%, -0.5%)
                JEFF COLLINS (TA): 497 (11.9%, +3.0%)
                AMYE UN (IND): 434 (10.4%, +0.7%)
                
                TWO CANDIDATE PREFERRED, 9 OF 9 POLLS REPORTING
                MARK MONAGHAN (ALP): 2,197 (52.6%) WINNER
                KYLIE BONANNI (CLP): 1,978 (47.4%)
                
                PREFERENCE SWING SINCE 2016: 5.1% SWING ALP TO CLP
            """.trimIndent()
        )
    }

    @Test
    fun testCandidateScreenProgressLabel() {
        val alp = Candidate("Mark Monaghan", Party("Labor", "ALP", Color.RED))
        val clp = Candidate("Kylie Bonanni", Party("Country Liberal", "CLP", Color.ORANGE))
        val ta = Candidate("Jeff Collins", Party("Territory Alliance", "TA", Color.BLUE), true)
        val ind = Candidate("Amye Un", Party("Independent", "IND", Color.GRAY))
        val currentPrimaryVotes = Publisher(setOf(alp, clp, ta, ind).associateWith { 0 })
        val previousPrimaryVotes = Publisher(
            mapOf(
                alp.party to 1802,
                clp.party to 1439,
                ta.party to 356,
                ind.party to 384
            )
        )
        val current2CPVotes = Publisher(emptyMap<Candidate, Int>())
        val previous2PPVotes = Publisher(
            mapOf(
                alp.party to 2171,
                clp.party to 1588
            )
        )
        val pctReporting = Publisher(0.0)
        val preferencePctReporting = Publisher(0.0)
        val header = Publisher("FONG LIM")
        val voteHeader = Publisher("PRIMARY VOTE")
        val voteSubhead = Publisher("")
        val voteProgress = Publisher("0/9")
        val preferenceHeader = Publisher("TWO CANDIDATE PREFERRED")
        val preferenceSubhead = Publisher("")
        val preferenceProgress = Publisher("0/9")
        val changeHeader = Publisher("PRIMARY CHANGE SINCE 2016")
        val swingHeader = Publisher("PREFERENCE SWING SINCE 2016")
        val leader = Publisher<Candidate?>(null)
        val swingPartyOrder = listOf(alp.party, ta.party, clp.party)
        val panel = candidateVotes(
            currentPrimaryVotes,
            voteHeader,
            voteSubhead
        )
            .withPrev(previousPrimaryVotes, changeHeader)
            .withPctReporting(pctReporting)
            .withProgressLabel(voteProgress)
            .withPreferences(current2CPVotes, preferenceHeader, preferenceSubhead)
            .withPrevPreferences(previous2PPVotes)
            .withPreferencePctReporting(preferencePctReporting)
            .withPreferenceProgressLabel(preferenceProgress)
            .withWinner(leader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "ProgressLabels-1", panel)
        assertPublishes(
            panel.altText,
            """
                FONG LIM
                
                PRIMARY VOTE [0/9] (PRIMARY CHANGE SINCE 2016)
                MARK MONAGHAN (ALP): WAITING...
                KYLIE BONANNI (CLP): WAITING...
                JEFF COLLINS (TA): WAITING...
                AMYE UN (IND): WAITING...
                
                TWO CANDIDATE PREFERRED [0/9]
                
                PREFERENCE SWING SINCE 2016: NOT AVAILABLE
            """.trimIndent()
        )

        currentPrimaryVotes.submit(
            mapOf(
                alp to 13,
                clp to 13,
                ta to 6,
                ind to 5
            )
        )
        current2CPVotes.submit(setOf(alp, clp).associateWith { 0 })
        pctReporting.submit(1.0 / 9)
        voteProgress.submit("1/9")
        compareRendering("PreferenceVoteViewPanel", "ProgressLabels-2", panel)
        assertPublishes(
            panel.altText,
            """
                FONG LIM
                
                PRIMARY VOTE [1/9] (PRIMARY CHANGE SINCE 2016)
                MARK MONAGHAN (ALP): 13 (35.1%, -10.1%)
                KYLIE BONANNI (CLP): 13 (35.1%, -1.0%)
                JEFF COLLINS (TA): 6 (16.2%, +7.3%)
                AMYE UN (IND): 5 (13.5%, +3.9%)
                
                TWO CANDIDATE PREFERRED [0/9]
                MARK MONAGHAN (ALP): WAITING...
                KYLIE BONANNI (CLP): WAITING...
                
                PREFERENCE SWING SINCE 2016: NOT AVAILABLE
            """.trimIndent()
        )

        currentPrimaryVotes.submit(
            mapOf(
                alp to 365,
                clp to 262,
                ta to 86,
                ind to 83
            )
        )
        current2CPVotes.submit(
            mapOf(
                alp to 18,
                clp to 19
            )
        )
        pctReporting.submit(3.0 / 9)
        voteProgress.submit("3/9")
        preferencePctReporting.submit(1.0 / 9)
        preferenceProgress.submit("1/9")
        compareRendering("PreferenceVoteViewPanel", "ProgressLabels-3", panel)
        assertPublishes(
            panel.altText,
            """
                FONG LIM
                
                PRIMARY VOTE [3/9] (PRIMARY CHANGE SINCE 2016)
                MARK MONAGHAN (ALP): 365 (45.9%, +0.6%)
                KYLIE BONANNI (CLP): 262 (32.9%, -3.2%)
                JEFF COLLINS (TA): 86 (10.8%, +1.9%)
                AMYE UN (IND): 83 (10.4%, +0.8%)
                
                TWO CANDIDATE PREFERRED [1/9]
                KYLIE BONANNI (CLP): 19 (51.4%)
                MARK MONAGHAN (ALP): 18 (48.6%)
                
                PREFERENCE SWING SINCE 2016: 9.1% SWING ALP TO CLP
            """.trimIndent()
        )

        currentPrimaryVotes.submit(
            mapOf(
                alp to 1756,
                clp to 1488,
                ta to 497,
                ind to 434
            )
        )
        current2CPVotes.submit(
            mapOf(
                alp to 464,
                clp to 332
            )
        )
        pctReporting.submit(9.0 / 9)
        voteProgress.submit("9/9")
        preferencePctReporting.submit(3.0 / 9)
        preferenceProgress.submit("3/9")
        leader.submit(alp)
        compareRendering("PreferenceVoteViewPanel", "ProgressLabels-4", panel)
        assertPublishes(
            panel.altText,
            """
                FONG LIM
                
                PRIMARY VOTE [9/9] (PRIMARY CHANGE SINCE 2016)
                MARK MONAGHAN (ALP): 1,756 (42.1%, -3.2%) WINNER
                KYLIE BONANNI (CLP): 1,488 (35.6%, -0.5%)
                JEFF COLLINS (TA): 497 (11.9%, +3.0%)
                AMYE UN (IND): 434 (10.4%, +0.7%)
                
                TWO CANDIDATE PREFERRED [3/9]
                MARK MONAGHAN (ALP): 464 (58.3%) WINNER
                KYLIE BONANNI (CLP): 332 (41.7%)
                
                PREFERENCE SWING SINCE 2016: 0.5% SWING CLP TO ALP
            """.trimIndent()
        )

        current2CPVotes.submit(
            mapOf(
                alp to 2197,
                clp to 1978
            )
        )
        preferencePctReporting.submit(9.0 / 9)
        preferenceProgress.submit("9/9")
        compareRendering("PreferenceVoteViewPanel", "ProgressLabels-5", panel)
        assertPublishes(
            panel.altText,
            """
                FONG LIM
                
                PRIMARY VOTE [9/9] (PRIMARY CHANGE SINCE 2016)
                MARK MONAGHAN (ALP): 1,756 (42.1%, -3.2%) WINNER
                KYLIE BONANNI (CLP): 1,488 (35.6%, -0.5%)
                JEFF COLLINS (TA): 497 (11.9%, +3.0%)
                AMYE UN (IND): 434 (10.4%, +0.7%)
                
                TWO CANDIDATE PREFERRED [9/9]
                MARK MONAGHAN (ALP): 2,197 (52.6%) WINNER
                KYLIE BONANNI (CLP): 1,978 (47.4%)
                
                PREFERENCE SWING SINCE 2016: 5.1% SWING ALP TO CLP
            """.trimIndent()
        )
    }

    @Test
    fun testCandidatesSwitchingBetweenSingleAndDoubleLines() {
        val alp = Party("Labor", "ALP", Color.RED)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val ta = Party("Territory Alliance", "TA", Color.BLUE)
        val grn = Party("Greens", "GRN", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Color.GRAY)
        val currentPrimaryVotes = Publisher(
            mapOf(
                Candidate("Amye Un", ind) to 434,
                Candidate("Mark Monaghan", alp) to 1756,
                Candidate("Jeff Collins", ta, true) to 497,
                Candidate("Kylie Bonanni", clp) to 1488
            )
        )
        val previousPrimaryVotes = Publisher(mapOf(alp to 1802, clp to 1439, ta to 356, ind to 384))
        val current2CPVotes = Publisher(
            mapOf(
                Candidate("Mark Monaghan", alp) to 2197,
                Candidate("Kylie Bonanni", clp) to 1978
            )
        )
        val previous2PPVotes = Publisher(mapOf(alp to 2171, clp to 1588))
        val header = Publisher("FONG LIM")
        val voteHeader = Publisher("PRIMARY VOTE")
        val voteSubhead = Publisher("9 OF 9 POLLS REPORTING")
        val preferenceHeader = Publisher("TWO CANDIDATE PREFERRED")
        val preferenceSubhead = Publisher("9 OF 9 POLLS REPORTING")
        val changeHeader = Publisher("PRIMARY CHANGE SINCE 2016")
        val swingHeader = Publisher("PREFERENCE SWING SINCE 2016")
        val leader = Publisher<Candidate?>(null)
        val swingPartyOrder = listOf(alp, ta, clp)
        val panel = candidateVotes(
            currentPrimaryVotes,
            voteHeader,
            voteSubhead,
            "(MP)"
        )
            .withPrev(previousPrimaryVotes, changeHeader)
            .withPreferences(current2CPVotes, preferenceHeader, preferenceSubhead)
            .withPrevPreferences(previous2PPVotes)
            .withWinner(leader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "LotsOfCandidates-1", panel)
        assertPublishes(
            panel.altText,
            """
                FONG LIM
                
                PRIMARY VOTE, 9 OF 9 POLLS REPORTING (PRIMARY CHANGE SINCE 2016)
                MARK MONAGHAN (ALP): 1,756 (42.1%, -3.2%)
                KYLIE BONANNI (CLP): 1,488 (35.6%, -0.5%)
                JEFF COLLINS (MP) (TA): 497 (11.9%, +3.0%)
                AMYE UN (IND): 434 (10.4%, +0.7%)
                
                TWO CANDIDATE PREFERRED, 9 OF 9 POLLS REPORTING
                MARK MONAGHAN (ALP): 2,197 (52.6%)
                KYLIE BONANNI (CLP): 1,978 (47.4%)
                
                PREFERENCE SWING SINCE 2016: 5.1% SWING ALP TO CLP
            """.trimIndent()
        )

        header.submit("GOYDER")
        voteSubhead.submit("12 OF 12 POLLS REPORTING")
        preferenceSubhead.submit("12 OF 12 POLLS REPORTING")
        currentPrimaryVotes.submit(
            mapOf(
                Candidate("Rachel Wright", ta) to 614,
                Candidate("Ted Warren", ind) to 249,
                Candidate("Phil Battye", clp) to 1289,
                Candidate("Trevor Jenkins", ind) to 64,
                Candidate("Kezia Purick", ind, true) to 1459,
                Candidate("Mick Taylor", alp) to 590,
                Candidate("Karen Fletcher", grn) to 147,
                Candidate("Pauline Cass", ind) to 283
            )
        )
        previousPrimaryVotes.submit(mapOf(ind to 2496 + 76, clp to 919, grn to 188, alp to 860))
        current2CPVotes.submit(
            mapOf(
                Candidate("Phil Battye", clp) to 2030,
                Candidate("Kezia Purick", ind, true) to 2665
            )
        )
        previous2PPVotes.submit(mapOf(ind to 3109, clp to 1020))
        compareRendering("PreferenceVoteViewPanel", "LotsOfCandidates-2", panel)
        assertPublishes(
            panel.altText,
            """
                GOYDER
                
                PRIMARY VOTE, 12 OF 12 POLLS REPORTING (PRIMARY CHANGE SINCE 2016)
                KEZIA PURICK (MP) (IND): 1,459 (31.1%, -12.9%^)
                PHIL BATTYE (CLP): 1,289 (27.5%, +7.2%)
                RACHEL WRIGHT (TA): 614 (13.1%, +13.1%)
                MICK TAYLOR (ALP): 590 (12.6%, -6.4%)
                PAULINE CASS (IND): 283 (6.0%, ^)
                TED WARREN (IND): 249 (5.3%, ^)
                KAREN FLETCHER (GRN): 147 (3.1%, -1.0%)
                TREVOR JENKINS (IND): 64 (1.4%, ^)
                ^ AGGREGATED ACROSS CANDIDATES IN PARTY
                
                TWO CANDIDATE PREFERRED, 12 OF 12 POLLS REPORTING
                KEZIA PURICK (MP) (IND): 2,665 (56.8%)
                PHIL BATTYE (CLP): 2,030 (43.2%)
                
                PREFERENCE SWING SINCE 2016: 18.5% SWING IND TO CLP
            """.trimIndent()
        )
    }

    @Test
    fun testSinglePreference() {
        val alp = Candidate("Kate Worden", Party("Labor", "ALP", Color.RED))
        val clp = Candidate("Derek Mayger", Party("Country Liberal", "CLP", Color.ORANGE))
        val ta = Candidate("Amelia Nuku", Party("Territory Alliance", "TA", Color.BLUE))
        val ind = Party("Independent", "IND", Color.GRAY)
        val currentPrimaryVotes = Publisher(
            mapOf(
                alp to 2632,
                clp to 968,
                ta to 795
            )
        )
        val previousPrimaryVotes = Publisher(
            mapOf(
                alp.party to 2323,
                clp.party to 1573,
                ta.party to 135,
                ind to 331 + 81
            )
        )
        val current2CPVotes = Publisher(mapOf(alp to 0))
        val previous2PPVotes = Publisher(
            mapOf(
                alp.party to 2578,
                clp.party to 1680
            )
        )
        val header = Publisher("SANDERSON")
        val voteHeader = Publisher("PRIMARY VOTE")
        val voteSubhead = Publisher("7 OF 7 POLLS REPORTING")
        val preferenceHeader = Publisher("TWO CANDIDATE PREFERRED")
        val preferenceSubhead = Publisher("7 OF 7 POLLS REPORTING")
        val changeHeader = Publisher("PRIMARY CHANGE SINCE 2016")
        val swingHeader = Publisher("PREFERENCE SWING SINCE 2016")
        val leader = Publisher(alp)
        val swingPartyOrder = listOf(alp.party, ta.party, clp.party)
        val panel = candidateVotes(
            currentPrimaryVotes,
            voteHeader,
            voteSubhead
        )
            .withPrev(previousPrimaryVotes, changeHeader)
            .withPreferences(current2CPVotes, preferenceHeader, preferenceSubhead)
            .withPrevPreferences(previous2PPVotes)
            .withWinner(leader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "SinglePreference-1", panel)
        assertPublishes(
            panel.altText,
            """
                SANDERSON
                
                PRIMARY VOTE, 7 OF 7 POLLS REPORTING (PRIMARY CHANGE SINCE 2016)
                KATE WORDEN (ALP): 2,632 (59.9%, +7.6%) WINNER
                DEREK MAYGER (CLP): 968 (22.0%, -13.4%)
                AMELIA NUKU (TA): 795 (18.1%, +15.1%)
                OTHERS: - (-9.3%)
                
                TWO CANDIDATE PREFERRED, 7 OF 7 POLLS REPORTING
                KATE WORDEN (ALP): ELECTED WINNER
                
                PREFERENCE SWING SINCE 2016: NOT AVAILABLE
            """.trimIndent()
        )
    }

    @Test
    fun testNoPrevPreference() {
        val alp = Candidate("Selena Uibo", Party("Labor", "ALP", Color.RED))
        val clp = Candidate("Jerry Amato", Party("Country Liberal", "CLP", Color.ORANGE))
        val ind = Party("Independent", "IND", Color.GRAY)
        val lawrence = Candidate("Lance Lawrence", ind)
        val gumbula = Candidate("Ian Mongunu Gumbula", ind)
        val currentPrimaryVotes = Publisher(
            mapOf(
                alp to 1207,
                clp to 487,
                lawrence to 243,
                gumbula to 987
            )
        )
        val previousPrimaryVotes = Publisher(
            mapOf(
                alp.party to 1565,
                clp.party to 804,
                ind to 211 + 197 + 117
            )
        )
        val current2CPVotes = Publisher(
            mapOf(
                alp to 1508,
                gumbula to 1416
            )
        )
        val previous2PPVotes = Publisher(emptyMap<Party, Int>())
        val header = Publisher("ARNHEM")
        val voteHeader = Publisher("PRIMARY VOTE")
        val voteSubhead = Publisher("7 OF 7 POLLS REPORTING")
        val preferenceHeader = Publisher("TWO CANDIDATE PREFERRED")
        val preferenceSubhead = Publisher("7 OF 7 POLLS REPORTING")
        val changeHeader = Publisher("PRIMARY CHANGE SINCE 2016")
        val swingHeader = Publisher("PREFERENCE SWING SINCE 2016")
        val leader = Publisher(alp)
        val swingPartyOrder = listOf(alp.party, clp.party)
        val panel = candidateVotes(
            currentPrimaryVotes,
            voteHeader,
            voteSubhead
        )
            .withPrev(previousPrimaryVotes, changeHeader)
            .withPreferences(current2CPVotes, preferenceHeader, preferenceSubhead)
            .withPrevPreferences(previous2PPVotes)
            .withWinner(leader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "NoPrevPreference-1", panel)
        assertPublishes(
            panel.altText,
            """
                ARNHEM
                
                PRIMARY VOTE, 7 OF 7 POLLS REPORTING (PRIMARY CHANGE SINCE 2016)
                SELENA UIBO (ALP): 1,207 (41.3%, -12.8%) WINNER
                IAN MONGUNU GUMBULA (IND): 987 (33.8%, +23.9%^)
                JERRY AMATO (CLP): 487 (16.7%, -11.1%)
                LANCE LAWRENCE (IND): 243 (8.3%, ^)
                ^ AGGREGATED ACROSS CANDIDATES IN PARTY
                
                TWO CANDIDATE PREFERRED, 7 OF 7 POLLS REPORTING
                SELENA UIBO (ALP): 1,508 (51.6%) WINNER
                IAN MONGUNU GUMBULA (IND): 1,416 (48.4%)
                
                PREFERENCE SWING SINCE 2016: NOT AVAILABLE
            """.trimIndent()
        )
    }

    @Test
    fun testChangeInPreference() {
        val alp = Candidate("Sophie Ismail", Party("Labor", "ALP", Color.RED))
        val lib = Candidate("Le Liu", Party("Liberal", "LIB", Color.BLUE))
        val grn = Candidate("Adam Bandt", Party("Greens", "GRN", Color.GREEN.darker()), true)
        val ind = Candidate("Others", Party("Independent", "IND", Color.GRAY))
        val currentPrimaryVotes = Publisher(
            mapOf(
                alp to 23130,
                lib to 23878,
                grn to 41377,
                ind to 6194
            )
        )
        val previousPrimaryVotes = Publisher(
            mapOf(
                alp.party to 22490,
                lib.party to 19301,
                grn.party to 36035,
                ind.party to 6725
            )
        )
        val current2CPVotes = Publisher(
            mapOf(
                grn to 64771,
                lib to 29808
            )
        )
        val previous2PPVotes = Publisher(
            mapOf(
                grn.party to 46732,
                alp.party to 37819
            )
        )
        val header = Publisher("MELBOURNE")
        val voteHeader = Publisher("PRIMARY VOTE")
        val voteSubhead = Publisher("2016 RESULTS")
        val preferenceHeader = Publisher("TWO CANDIDATE PREFERRED")
        val preferenceSubhead = Publisher("2016 RESULTS")
        val changeHeader = Publisher("PRIMARY CHANGE SINCE 2013")
        val swingHeader = Publisher("PREFERENCE SWING SINCE 2013")
        val leader = Publisher(grn)
        val swingPartyOrder = listOf(alp.party, grn.party, lib.party)
        val panel = candidateVotes(
            currentPrimaryVotes,
            voteHeader,
            voteSubhead,
            "(MP)"
        )
            .withPrev(previousPrimaryVotes, changeHeader)
            .withPreferences(current2CPVotes, preferenceHeader, preferenceSubhead)
            .withPrevPreferences(previous2PPVotes)
            .withWinner(leader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "ChangeInPreference-1", panel)
        assertPublishes(
            panel.altText,
            """
                MELBOURNE
                
                PRIMARY VOTE, 2016 RESULTS (PRIMARY CHANGE SINCE 2013)
                ADAM BANDT (MP) (GRN): 41,377 (43.7%, +1.1%) WINNER
                LE LIU (LIB): 23,878 (25.2%, +2.4%)
                SOPHIE ISMAIL (ALP): 23,130 (24.5%, -2.1%)
                OTHERS (IND): 6,194 (6.5%, -1.4%)
                
                TWO CANDIDATE PREFERRED, 2016 RESULTS
                ADAM BANDT (MP) (GRN): 64,771 (68.5%) WINNER
                LE LIU (LIB): 29,808 (31.5%)
                
                PREFERENCE SWING SINCE 2013: NOT AVAILABLE
            """.trimIndent()
        )
    }

    @Test
    fun testRanges() {
        val alp = Party("Labor", "ALP", Color.RED)
        val coa = Party("Coalition", "L/NP", Color.BLUE)
        val grn = Party("Greens", "GRN", Color.GREEN.darker())
        val onp = Party("One Nation", "ONP", Color.ORANGE)
        val oth = Party.OTHERS
        val panel = partyRangeVotes(
            mapOf(
                alp to (0.34).rangeTo(0.36),
                coa to (0.42).rangeTo(0.43),
                grn to (0.11).rangeTo(0.12),
                onp to (0.02).rangeTo(0.04),
                oth to (0.08).rangeTo(0.08)
            )
                .asOneTimePublisher(),
            "POLLING RANGE".asOneTimePublisher(),
            "NOVEMBER 2020".asOneTimePublisher()
        )
            .withPrev(
                mapOf(
                    alp to 4752160,
                    coa to 5906875,
                    grn to 1482923,
                    onp to 438587,
                    oth to 488817 + 69736 + 46931 + 479836 + 587528
                )
                    .asOneTimePublisher(),
                "CHANGE SINCE 2019".asOneTimePublisher()
            )
            .withPreferences(
                mapOf(
                    alp to (0.49).rangeTo(0.495),
                    coa to (0.505).rangeTo(0.51)
                )
                    .asOneTimePublisher(),
                "TWO PARTY PREFERRED".asOneTimePublisher(),
                "".asOneTimePublisher()
            )
            .withPrevPreferences(mapOf(alp to 6908580, coa to 7344813).asOneTimePublisher())
            .withSwing(
                compareBy { listOf(alp, coa).indexOf(it) },
                "SWING SINCE 2019".asOneTimePublisher()
            )
            .build("AUSTRALIA".asOneTimePublisher())
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "Ranges-1", panel)
        assertPublishes(
            panel.altText,
            """
                AUSTRALIA
                
                POLLING RANGE, NOVEMBER 2020 (CHANGE SINCE 2019)
                COALITION: 42.0-43.0% ((+0.6)-(+1.6)%)
                LABOR: 34.0-36.0% ((+0.7)-(+2.7)%)
                GREENS: 11.0-12.0% ((+0.6)-(+1.6)%)
                ONE NATION: 2.0-4.0% ((-1.1)-(+0.9)%)
                OTHERS: 8.0-8.0% ((-3.7)-(-3.7)%)
                
                TWO PARTY PREFERRED
                COALITION: 50.5-51.0%
                LABOR: 49.0-49.5%
                
                SWING SINCE 2019: 0.3% SWING L/NP TO ALP
            """.trimIndent()
        )
    }

    @Test
    fun testPreferencesDeclarationInProgress() {
        val lab = Candidate("Sadiq Khan", Party("Labour", "LAB", Color.RED))
        val con = Candidate("Zac Goldsmith", Party("Conservative", "CON", Color.BLUE))
        val ld = Candidate("Caroline Pidgeon", Party("Liberal Democrats", "LD", Color.ORANGE))
        val grn = Candidate("Si\u00e2n Berry", Party("Green", "GRN", Color.GREEN.darker()))
        val ukip = Candidate("Peter Whittle", Party("UK Independence Party", "UKIP", Color.MAGENTA.darker()))
        val oth = Candidate.OTHERS
        val currPrimary = mutableMapOf<Candidate, Int?>(
            grn to null,
            con to null,
            lab to null,
            ld to null,
            ukip to null,
            oth to null
        )
        val curr2CP = mutableMapOf<Candidate, Int?>(
            con to null,
            lab to null
        )
        val currentPrimaryVotes = Publisher(currPrimary)
        val previousPrimaryVotes = Publisher(
            mapOf(
                lab.party to 889918,
                con.party to 971931,
                ld.party to 91774,
                grn.party to 98913,
                ukip.party to 43274,
                oth.party to 83914 + 28751
            )
        )
        val current2CPVotes = Publisher(curr2CP)
        val previous2PPVotes = Publisher(
            mapOf(
                lab.party to 992273,
                con.party to 1054811
            )
        )
        val header = Publisher("MAYOR OF LONDON")
        val voteHeader = Publisher("FIRST CHOICE VOTES")
        val voteSubhead = Publisher("2016 RESULTS")
        val preferenceHeader = Publisher("SECOND CHOICE VOTES")
        val preferenceSubhead = Publisher("2016 RESULTS")
        val changeHeader = Publisher("FIRST CHOICE CHANGE SINCE 2012")
        val swingHeader = Publisher("SECOND CHOICE SWING SINCE 2012")
        val leader = Publisher<Candidate?>(null)
        val swingPartyOrder = listOf(lab.party, grn.party, ld.party, con.party)
        val panel = candidateVotes(
            currentPrimaryVotes,
            voteHeader,
            voteSubhead
        )
            .withPrev(previousPrimaryVotes, changeHeader)
            .withPreferences(current2CPVotes, preferenceHeader, preferenceSubhead)
            .withPrevPreferences(previous2PPVotes)
            .withWinner(leader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "Declaration-1", panel)
        assertPublishes(
            panel.altText,
            """
                MAYOR OF LONDON
                
                FIRST CHOICE VOTES, 2016 RESULTS (FIRST CHOICE CHANGE SINCE 2012)
                SIÂN BERRY (GRN): WAITING...
                ZAC GOLDSMITH (CON): WAITING...
                SADIQ KHAN (LAB): WAITING...
                CAROLINE PIDGEON (LD): WAITING...
                PETER WHITTLE (UKIP): WAITING...
                OTHERS: WAITING...
                
                SECOND CHOICE VOTES, 2016 RESULTS
                ZAC GOLDSMITH (CON): WAITING...
                SADIQ KHAN (LAB): WAITING...
                
                SECOND CHOICE SWING SINCE 2012: NOT AVAILABLE
            """.trimIndent()
        )

        currPrimary[grn] = 150673
        currPrimary[con] = 909755
        currentPrimaryVotes.submit(currPrimary)
        compareRendering("PreferenceVoteViewPanel", "Declaration-2", panel)
        assertPublishes(
            panel.altText,
            """
                MAYOR OF LONDON
                
                FIRST CHOICE VOTES, 2016 RESULTS (FIRST CHOICE CHANGE SINCE 2012)
                ZAC GOLDSMITH (CON): 909,755
                SIÂN BERRY (GRN): 150,673
                SADIQ KHAN (LAB): WAITING...
                CAROLINE PIDGEON (LD): WAITING...
                PETER WHITTLE (UKIP): WAITING...
                OTHERS: WAITING...
                
                SECOND CHOICE VOTES, 2016 RESULTS
                ZAC GOLDSMITH (CON): WAITING...
                SADIQ KHAN (LAB): WAITING...
                
                SECOND CHOICE SWING SINCE 2012: NOT AVAILABLE
            """.trimIndent()
        )

        currPrimary[lab] = 1148716
        currPrimary[ld] = 120005
        currentPrimaryVotes.submit(currPrimary)
        compareRendering("PreferenceVoteViewPanel", "Declaration-3", panel)
        assertPublishes(
            panel.altText,
            """
                MAYOR OF LONDON
                
                FIRST CHOICE VOTES, 2016 RESULTS (FIRST CHOICE CHANGE SINCE 2012)
                SADIQ KHAN (LAB): 1,148,716
                ZAC GOLDSMITH (CON): 909,755
                SIÂN BERRY (GRN): 150,673
                CAROLINE PIDGEON (LD): 120,005
                PETER WHITTLE (UKIP): WAITING...
                OTHERS: WAITING...
                
                SECOND CHOICE VOTES, 2016 RESULTS
                ZAC GOLDSMITH (CON): WAITING...
                SADIQ KHAN (LAB): WAITING...
                
                SECOND CHOICE SWING SINCE 2012: NOT AVAILABLE
            """.trimIndent()
        )

        currPrimary[ukip] = 94373
        currPrimary[oth] = 53055 + 37007 + 31372 + 20537 + 13325 + 13202 + 4941
        currentPrimaryVotes.submit(currPrimary)
        compareRendering("PreferenceVoteViewPanel", "Declaration-4", panel)
        assertPublishes(
            panel.altText,
            """
                MAYOR OF LONDON
                
                FIRST CHOICE VOTES, 2016 RESULTS (FIRST CHOICE CHANGE SINCE 2012)
                SADIQ KHAN (LAB): 1,148,716 (44.2%, +3.9%)
                ZAC GOLDSMITH (CON): 909,755 (35.0%, -9.0%)
                SIÂN BERRY (GRN): 150,673 (5.8%, +1.3%)
                CAROLINE PIDGEON (LD): 120,005 (4.6%, +0.5%)
                PETER WHITTLE (UKIP): 94,373 (3.6%, +1.7%)
                OTHERS: 173,439 (6.7%, +1.6%)
                
                SECOND CHOICE VOTES, 2016 RESULTS
                ZAC GOLDSMITH (CON): WAITING...
                SADIQ KHAN (LAB): WAITING...
                
                SECOND CHOICE SWING SINCE 2012: NOT AVAILABLE
            """.trimIndent()
        )

        curr2CP[con] = 994614
        current2CPVotes.submit(curr2CP)
        compareRendering("PreferenceVoteViewPanel", "Declaration-5", panel)
        assertPublishes(
            panel.altText,
            """
                MAYOR OF LONDON
                
                FIRST CHOICE VOTES, 2016 RESULTS (FIRST CHOICE CHANGE SINCE 2012)
                SADIQ KHAN (LAB): 1,148,716 (44.2%, +3.9%)
                ZAC GOLDSMITH (CON): 909,755 (35.0%, -9.0%)
                SIÂN BERRY (GRN): 150,673 (5.8%, +1.3%)
                CAROLINE PIDGEON (LD): 120,005 (4.6%, +0.5%)
                PETER WHITTLE (UKIP): 94,373 (3.6%, +1.7%)
                OTHERS: 173,439 (6.7%, +1.6%)
                
                SECOND CHOICE VOTES, 2016 RESULTS
                ZAC GOLDSMITH (CON): 994,614
                SADIQ KHAN (LAB): WAITING...
                
                SECOND CHOICE SWING SINCE 2012: NOT AVAILABLE
            """.trimIndent()
        )

        curr2CP[lab] = 1310143
        current2CPVotes.submit(curr2CP)
        compareRendering("PreferenceVoteViewPanel", "Declaration-6", panel)
        assertPublishes(
            panel.altText,
            """
                MAYOR OF LONDON
                
                FIRST CHOICE VOTES, 2016 RESULTS (FIRST CHOICE CHANGE SINCE 2012)
                SADIQ KHAN (LAB): 1,148,716 (44.2%, +3.9%)
                ZAC GOLDSMITH (CON): 909,755 (35.0%, -9.0%)
                SIÂN BERRY (GRN): 150,673 (5.8%, +1.3%)
                CAROLINE PIDGEON (LD): 120,005 (4.6%, +0.5%)
                PETER WHITTLE (UKIP): 94,373 (3.6%, +1.7%)
                OTHERS: 173,439 (6.7%, +1.6%)
                
                SECOND CHOICE VOTES, 2016 RESULTS
                SADIQ KHAN (LAB): 1,310,143 (56.8%)
                ZAC GOLDSMITH (CON): 994,614 (43.2%)
                
                SECOND CHOICE SWING SINCE 2012: 8.4% SWING CON TO LAB
            """.trimIndent()
        )
    }
}
