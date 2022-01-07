package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateVotes
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyRangeVotes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.Test
import java.awt.Color
import java.io.IOException
import java.util.LinkedHashMap
import kotlin.Throws

class PreferenceVoteViewPanelTest {
    @Test
    @Throws(IOException::class)
    fun testCandidateBasicResult() {
        val alp = Candidate("Mark Monaghan", Party("Labor", "ALP", Color.RED))
        val clp = Candidate("Kylie Bonanni", Party("Country Liberal", "CLP", Color.ORANGE))
        val ta = Candidate("Jeff Collins", Party("Territory Alliance", "TA", Color.BLUE), true)
        val ind = Candidate("Amye Un", Party("Independent", "IND", Color.GRAY))
        val currPrimary = LinkedHashMap<Candidate, Int?>()
        currPrimary[alp] = 1756
        currPrimary[clp] = 1488
        currPrimary[ta] = 497
        currPrimary[ind] = 434
        val prevPrimary = LinkedHashMap<Party, Int>()
        prevPrimary[alp.party] = 1802
        prevPrimary[clp.party] = 1439
        prevPrimary[ta.party] = 356
        prevPrimary[ind.party] = 384
        val curr2CP = LinkedHashMap<Candidate, Int>()
        curr2CP[alp] = 2197
        curr2CP[clp] = 1978
        val prev2PP = LinkedHashMap<Party, Int>()
        prev2PP[alp.party] = 2171
        prev2PP[clp.party] = 1588
        val currentPrimaryVotes = Publisher(currPrimary)
        val previousPrimaryVotes = Publisher(prevPrimary)
        val current2CPVotes = Publisher(curr2CP)
        val previous2PPVotes = Publisher(prev2PP)
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
            currentPrimaryVotes, voteHeader, voteSubhead
        )
            .withPrev(previousPrimaryVotes, changeHeader)
            .withPreferences(current2CPVotes, preferenceHeader, preferenceSubhead)
            .withPrevPreferences(previous2PPVotes)
            .withWinner(leader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "Basic-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testCandidateScreenUpdating() {
        val alp = Candidate("Mark Monaghan", Party("Labor", "ALP", Color.RED))
        val clp = Candidate("Kylie Bonanni", Party("Country Liberal", "CLP", Color.ORANGE))
        val ta = Candidate("Jeff Collins", Party("Territory Alliance", "TA", Color.BLUE), true)
        val ind = Candidate("Amye Un", Party("Independent", "IND", Color.GRAY))
        val currPrimary = LinkedHashMap<Candidate, Int?>()
        currPrimary[alp] = 0
        currPrimary[clp] = 0
        currPrimary[ta] = 0
        currPrimary[ind] = 0
        val prevPrimary = LinkedHashMap<Party, Int>()
        prevPrimary[alp.party] = 1802
        prevPrimary[clp.party] = 1439
        prevPrimary[ta.party] = 356
        prevPrimary[ind.party] = 384
        val curr2CP = LinkedHashMap<Candidate, Int>()
        val prev2PP = LinkedHashMap<Party, Int>()
        prev2PP[alp.party] = 2171
        prev2PP[clp.party] = 1588
        val currentPrimaryVotes = Publisher(currPrimary)
        val previousPrimaryVotes = Publisher(prevPrimary)
        val current2CPVotes = Publisher(curr2CP)
        val previous2PPVotes = Publisher(prev2PP)
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
            currentPrimaryVotes, voteHeader, voteSubhead
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
        currPrimary[alp] = 13
        currPrimary[clp] = 13
        currPrimary[ta] = 6
        currPrimary[ind] = 5
        currentPrimaryVotes.submit(currPrimary)
        curr2CP[alp] = 0
        curr2CP[clp] = 0
        current2CPVotes.submit(curr2CP)
        pctReporting.submit(1.0 / 9)
        voteSubhead.submit("1 OF 9 POLLS REPORTING")
        compareRendering("PreferenceVoteViewPanel", "Update-2", panel)
        currPrimary[alp] = 365
        currPrimary[clp] = 262
        currPrimary[ta] = 86
        currPrimary[ind] = 83
        currentPrimaryVotes.submit(currPrimary)
        curr2CP[alp] = 18
        curr2CP[clp] = 19
        current2CPVotes.submit(curr2CP)
        pctReporting.submit(3.0 / 9)
        voteSubhead.submit("3 OF 9 POLLS REPORTING")
        preferencePctReporting.submit(1.0 / 9)
        preferenceSubhead.submit("1 OF 9 POLLS REPORTING")
        compareRendering("PreferenceVoteViewPanel", "Update-3", panel)
        currPrimary[alp] = 1756
        currPrimary[clp] = 1488
        currPrimary[ta] = 497
        currPrimary[ind] = 434
        currentPrimaryVotes.submit(currPrimary)
        curr2CP[alp] = 464
        curr2CP[clp] = 332
        current2CPVotes.submit(curr2CP)
        pctReporting.submit(9.0 / 9)
        voteSubhead.submit("9 OF 9 POLLS REPORTING")
        preferencePctReporting.submit(3.0 / 9)
        preferenceSubhead.submit("3 OF 9 POLLS REPORTING")
        leader.submit(alp)
        compareRendering("PreferenceVoteViewPanel", "Update-4", panel)
        curr2CP[alp] = 2197
        curr2CP[clp] = 1978
        current2CPVotes.submit(curr2CP)
        preferencePctReporting.submit(9.0 / 9)
        preferenceSubhead.submit("9 OF 9 POLLS REPORTING")
        compareRendering("PreferenceVoteViewPanel", "Update-5", panel)
    }

    @Test
    @Throws(IOException::class)
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
    }

    @Test
    @Throws(IOException::class)
    fun testSinglePreference() {
        val alp = Candidate("Kate Worden", Party("Labor", "ALP", Color.RED))
        val clp = Candidate("Derek Mayger", Party("Country Liberal", "CLP", Color.ORANGE))
        val ta = Candidate("Amelia Nuku", Party("Territory Alliance", "TA", Color.BLUE))
        val ind = Party("Independent", "IND", Color.GRAY)
        val currPrimary = LinkedHashMap<Candidate, Int?>()
        currPrimary[alp] = 2632
        currPrimary[clp] = 968
        currPrimary[ta] = 795
        val prevPrimary = LinkedHashMap<Party, Int>()
        prevPrimary[alp.party] = 2323
        prevPrimary[clp.party] = 1573
        prevPrimary[ta.party] = 135
        prevPrimary[ind] = 331 + 81
        val curr2CP = LinkedHashMap<Candidate, Int>()
        curr2CP[alp] = 0
        val prev2PP = LinkedHashMap<Party, Int>()
        prev2PP[alp.party] = 2578
        prev2PP[clp.party] = 1680
        val currentPrimaryVotes = Publisher(currPrimary)
        val previousPrimaryVotes = Publisher(prevPrimary)
        val current2CPVotes = Publisher(curr2CP)
        val previous2PPVotes = Publisher(prev2PP)
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
            currentPrimaryVotes, voteHeader, voteSubhead
        )
            .withPrev(previousPrimaryVotes, changeHeader)
            .withPreferences(current2CPVotes, preferenceHeader, preferenceSubhead)
            .withPrevPreferences(previous2PPVotes)
            .withWinner(leader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "SinglePreference-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testNoPrevPreference() {
        val alp = Candidate("Selena Uibo", Party("Labor", "ALP", Color.RED))
        val clp = Candidate("Jerry Amato", Party("Country Liberal", "CLP", Color.ORANGE))
        val ind = Party("Independent", "IND", Color.GRAY)
        val lawrence = Candidate("Lance Lawrence", ind)
        val gumbula = Candidate("Ian Mongunu Gumbula", ind)
        val currPrimary = LinkedHashMap<Candidate, Int?>()
        currPrimary[alp] = 1207
        currPrimary[clp] = 487
        currPrimary[lawrence] = 243
        currPrimary[gumbula] = 987
        val prevPrimary = LinkedHashMap<Party, Int>()
        prevPrimary[alp.party] = 1565
        prevPrimary[clp.party] = 804
        prevPrimary[ind] = 211 + 197 + 117
        val curr2CP = LinkedHashMap<Candidate, Int>()
        curr2CP[alp] = 1508
        curr2CP[gumbula] = 1416
        val prev2PP = LinkedHashMap<Party, Int>()
        val currentPrimaryVotes = Publisher(currPrimary)
        val previousPrimaryVotes = Publisher(prevPrimary)
        val current2CPVotes = Publisher(curr2CP)
        val previous2PPVotes = Publisher(prev2PP)
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
            currentPrimaryVotes, voteHeader, voteSubhead
        )
            .withPrev(previousPrimaryVotes, changeHeader)
            .withPreferences(current2CPVotes, preferenceHeader, preferenceSubhead)
            .withPrevPreferences(previous2PPVotes)
            .withWinner(leader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "NoPrevPreference-1", panel)
    }

    @Test
    @Throws(IOException::class)
    fun testChangeInPreference() {
        val alp = Candidate("Sophie Ismail", Party("Labor", "ALP", Color.RED))
        val lib = Candidate("Le Liu", Party("Liberal", "LIB", Color.BLUE))
        val grn = Candidate("Adam Bandt", Party("Greens", "GRN", Color.GREEN.darker()), true)
        val ind = Candidate("Others", Party("Independent", "IND", Color.GRAY))
        val currPrimary = LinkedHashMap<Candidate, Int>()
        currPrimary[alp] = 23130
        currPrimary[lib] = 23878
        currPrimary[grn] = 41377
        currPrimary[ind] = 94579 - currPrimary.values.sum()
        val prevPrimary = LinkedHashMap<Party, Int>()
        prevPrimary[alp.party] = 22490
        prevPrimary[lib.party] = 19301
        prevPrimary[grn.party] = 36035
        prevPrimary[ind.party] = 84551 - prevPrimary.values.sum()
        val curr2CP = LinkedHashMap<Candidate, Int>()
        curr2CP[grn] = 64771
        curr2CP[lib] = 29808
        val prev2PP = LinkedHashMap<Party, Int>()
        prev2PP[grn.party] = 46732
        prev2PP[alp.party] = 37819
        val currentPrimaryVotes = Publisher(currPrimary)
        val previousPrimaryVotes = Publisher(prevPrimary)
        val current2CPVotes = Publisher(curr2CP)
        val previous2PPVotes = Publisher(prev2PP)
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
    }

    @Test
    @Throws(IOException::class)
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
    }

    @Test
    @Throws(IOException::class)
    fun testPreferencesDeclarationInProgress() {
        val lab = Candidate("Sadiq Khan", Party("Labour", "LAB", Color.RED))
        val con = Candidate("Zac Goldsmith", Party("Conservative", "CON", Color.BLUE))
        val ld = Candidate("Caroline Pidgeon", Party("Liberal Democrats", "LD", Color.ORANGE))
        val grn = Candidate("Si\u00e2n Berry", Party("Green", "GRN", Color.GREEN.darker()))
        val ukip = Candidate("Peter Whittle", Party("UK Independence Party", "UKIP", Color.MAGENTA.darker()))
        val oth = Candidate.OTHERS
        val currPrimary = LinkedHashMap<Candidate, Int?>()
        currPrimary[grn] = null
        currPrimary[con] = null
        currPrimary[lab] = null
        currPrimary[ld] = null
        currPrimary[ukip] = null
        currPrimary[oth] = null
        val prevPrimary = LinkedHashMap<Party, Int>()
        prevPrimary[lab.party] = 889918
        prevPrimary[con.party] = 971931
        prevPrimary[ld.party] = 91774
        prevPrimary[grn.party] = 98913
        prevPrimary[ukip.party] = 43274
        prevPrimary[oth.party] = 83914 + 28751
        val curr2CP = LinkedHashMap<Candidate, Int?>()
        curr2CP[con] = null
        curr2CP[lab] = null
        val prev2PP = LinkedHashMap<Party, Int>()
        prev2PP[lab.party] = 992273
        prev2PP[con.party] = 1054811
        val currentPrimaryVotes = Publisher(currPrimary)
        val previousPrimaryVotes = Publisher(prevPrimary)
        val current2CPVotes = Publisher(curr2CP)
        val previous2PPVotes = Publisher(prev2PP)
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
            currentPrimaryVotes, voteHeader, voteSubhead
        )
            .withPrev(previousPrimaryVotes, changeHeader)
            .withPreferences(current2CPVotes, preferenceHeader, preferenceSubhead)
            .withPrevPreferences(previous2PPVotes)
            .withWinner(leader)
            .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "Declaration-1", panel)
        currPrimary[grn] = 150673
        currPrimary[con] = 909755
        currentPrimaryVotes.submit(currPrimary)
        compareRendering("PreferenceVoteViewPanel", "Declaration-2", panel)
        currPrimary[lab] = 1148716
        currPrimary[ld] = 120005
        currentPrimaryVotes.submit(currPrimary)
        compareRendering("PreferenceVoteViewPanel", "Declaration-3", panel)
        currPrimary[ukip] = 94373
        currPrimary[oth] = 53055 + 37007 + 31372 + 20537 + 13325 + 13202 + 4941
        currentPrimaryVotes.submit(currPrimary)
        compareRendering("PreferenceVoteViewPanel", "Declaration-4", panel)
        curr2CP[con] = 994614
        current2CPVotes.submit(curr2CP)
        compareRendering("PreferenceVoteViewPanel", "Declaration-5", panel)
        curr2CP[lab] = 1310143
        current2CPVotes.submit(curr2CP)
        compareRendering("PreferenceVoteViewPanel", "Declaration-6", panel)
    }
}
