package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateVotes
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyRangeVotes
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import java.awt.Color
import java.io.IOException
import java.util.LinkedHashMap
import kotlin.Throws
import org.junit.Test

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
        val currentPrimaryVotes = BindableWrapper(currPrimary)
        val previousPrimaryVotes = BindableWrapper(prevPrimary)
        val current2CPVotes = BindableWrapper(curr2CP)
        val previous2PPVotes = BindableWrapper(prev2PP)
        val header = BindableWrapper("FONG LIM")
        val voteHeader = BindableWrapper("PRIMARY VOTE")
        val voteSubhead = BindableWrapper("9 OF 9 POLLS REPORTING")
        val preferenceHeader = BindableWrapper("TWO CANDIDATE PREFERRED")
        val preferenceSubhead = BindableWrapper("9 OF 9 POLLS REPORTING")
        val changeHeader = BindableWrapper("PRIMARY CHANGE SINCE 2016")
        val swingHeader = BindableWrapper("PREFERENCE SWING SINCE 2016")
        val leader = BindableWrapper(alp)
        val swingPartyOrder = listOf(alp.party, ta.party, clp.party)
        val panel = candidateVotes(
                currentPrimaryVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousPrimaryVotes.binding, changeHeader.binding)
                .withPreferences(current2CPVotes.binding, preferenceHeader.binding, preferenceSubhead.binding)
                .withPrevPreferences(previous2PPVotes.binding)
                .withWinner(leader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
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
        val currentPrimaryVotes = BindableWrapper(currPrimary)
        val previousPrimaryVotes = BindableWrapper(prevPrimary)
        val current2CPVotes = BindableWrapper(curr2CP)
        val previous2PPVotes = BindableWrapper(prev2PP)
        val pctReporting = BindableWrapper(0.0)
        val preferencePctReporting = BindableWrapper(0.0)
        val header = BindableWrapper("FONG LIM")
        val voteHeader = BindableWrapper("PRIMARY VOTE")
        val voteSubhead = BindableWrapper("0 OF 9 POLLS REPORTING")
        val preferenceHeader = BindableWrapper("TWO CANDIDATE PREFERRED")
        val preferenceSubhead = BindableWrapper("0 OF 9 POLLS REPORTING")
        val changeHeader = BindableWrapper("PRIMARY CHANGE SINCE 2016")
        val swingHeader = BindableWrapper("PREFERENCE SWING SINCE 2016")
        val leader = BindableWrapper<Candidate?>(null)
        val swingPartyOrder = listOf(alp.party, ta.party, clp.party)
        val panel = candidateVotes(
                currentPrimaryVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousPrimaryVotes.binding, changeHeader.binding)
                .withPctReporting(pctReporting.binding)
                .withPreferences(current2CPVotes.binding, preferenceHeader.binding, preferenceSubhead.binding)
                .withPrevPreferences(previous2PPVotes.binding)
                .withPreferencePctReporting(preferencePctReporting.binding)
                .withWinner(leader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "Update-1", panel)
        currPrimary[alp] = 13
        currPrimary[clp] = 13
        currPrimary[ta] = 6
        currPrimary[ind] = 5
        currentPrimaryVotes.value = currPrimary
        curr2CP[alp] = 0
        curr2CP[clp] = 0
        current2CPVotes.value = curr2CP
        pctReporting.value = 1.0 / 9
        voteSubhead.value = "1 OF 9 POLLS REPORTING"
        compareRendering("PreferenceVoteViewPanel", "Update-2", panel)
        currPrimary[alp] = 365
        currPrimary[clp] = 262
        currPrimary[ta] = 86
        currPrimary[ind] = 83
        currentPrimaryVotes.value = currPrimary
        curr2CP[alp] = 18
        curr2CP[clp] = 19
        current2CPVotes.value = curr2CP
        pctReporting.value = 3.0 / 9
        voteSubhead.value = "3 OF 9 POLLS REPORTING"
        preferencePctReporting.value = 1.0 / 9
        preferenceSubhead.value = "1 OF 9 POLLS REPORTING"
        compareRendering("PreferenceVoteViewPanel", "Update-3", panel)
        currPrimary[alp] = 1756
        currPrimary[clp] = 1488
        currPrimary[ta] = 497
        currPrimary[ind] = 434
        currentPrimaryVotes.value = currPrimary
        curr2CP[alp] = 464
        curr2CP[clp] = 332
        current2CPVotes.value = curr2CP
        pctReporting.value = 9.0 / 9
        voteSubhead.value = "9 OF 9 POLLS REPORTING"
        preferencePctReporting.value = 3.0 / 9
        preferenceSubhead.value = "3 OF 9 POLLS REPORTING"
        leader.value = alp
        compareRendering("PreferenceVoteViewPanel", "Update-4", panel)
        curr2CP[alp] = 2197
        curr2CP[clp] = 1978
        current2CPVotes.value = curr2CP
        preferencePctReporting.value = 9.0 / 9
        preferenceSubhead.value = "9 OF 9 POLLS REPORTING"
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
        val currentPrimaryVotes = BindableWrapper(
                mapOf(
                        Candidate("Amye Un", ind) to 434,
                        Candidate("Mark Monaghan", alp) to 1756,
                        Candidate("Jeff Collins", ta, true) to 497,
                        Candidate("Kylie Bonanni", clp) to 1488))
        val previousPrimaryVotes = BindableWrapper(mapOf(alp to 1802, clp to 1439, ta to 356, ind to 384))
        val current2CPVotes = BindableWrapper(
                mapOf(
                        Candidate("Mark Monaghan", alp) to 2197,
                        Candidate("Kylie Bonanni", clp) to 1978))
        val previous2PPVotes = BindableWrapper(mapOf(alp to 2171, clp to 1588))
        val header = BindableWrapper("FONG LIM")
        val voteHeader = BindableWrapper("PRIMARY VOTE")
        val voteSubhead = BindableWrapper("9 OF 9 POLLS REPORTING")
        val preferenceHeader = BindableWrapper("TWO CANDIDATE PREFERRED")
        val preferenceSubhead = BindableWrapper("9 OF 9 POLLS REPORTING")
        val changeHeader = BindableWrapper("PRIMARY CHANGE SINCE 2016")
        val swingHeader = BindableWrapper("PREFERENCE SWING SINCE 2016")
        val leader = BindableWrapper<Candidate?>(null)
        val swingPartyOrder = listOf(alp, ta, clp)
        val panel = candidateVotes(
                currentPrimaryVotes.binding,
                voteHeader.binding,
                voteSubhead.binding,
                "(MP)")
                .withPrev(previousPrimaryVotes.binding, changeHeader.binding)
                .withPreferences(current2CPVotes.binding, preferenceHeader.binding, preferenceSubhead.binding)
                .withPrevPreferences(previous2PPVotes.binding)
                .withWinner(leader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "LotsOfCandidates-1", panel)
        header.value = "GOYDER"
        voteSubhead.value = "12 OF 12 POLLS REPORTING"
        preferenceSubhead.value = "12 OF 12 POLLS REPORTING"
        currentPrimaryVotes.value = mapOf(
                Candidate("Rachel Wright", ta) to 614,
                Candidate("Ted Warren", ind) to 249,
                Candidate("Phil Battye", clp) to 1289,
                Candidate("Trevor Jenkins", ind) to 64,
                Candidate("Kezia Purick", ind, true) to 1459,
                Candidate("Mick Taylor", alp) to 590,
                Candidate("Karen Fletcher", grn) to 147,
                Candidate("Pauline Cass", ind) to 283)
        previousPrimaryVotes.value = mapOf(ind to 2496 + 76, clp to 919, grn to 188, alp to 860)
        current2CPVotes.value = mapOf(
                Candidate("Phil Battye", clp) to 2030,
                Candidate("Kezia Purick", ind, true) to 2665)
        previous2PPVotes.value = mapOf(ind to 3109, clp to 1020)
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
        val currentPrimaryVotes = BindableWrapper(currPrimary)
        val previousPrimaryVotes = BindableWrapper(prevPrimary)
        val current2CPVotes = BindableWrapper(curr2CP)
        val previous2PPVotes = BindableWrapper(prev2PP)
        val header = BindableWrapper("SANDERSON")
        val voteHeader = BindableWrapper("PRIMARY VOTE")
        val voteSubhead = BindableWrapper("7 OF 7 POLLS REPORTING")
        val preferenceHeader = BindableWrapper("TWO CANDIDATE PREFERRED")
        val preferenceSubhead = BindableWrapper("7 OF 7 POLLS REPORTING")
        val changeHeader = BindableWrapper("PRIMARY CHANGE SINCE 2016")
        val swingHeader = BindableWrapper("PREFERENCE SWING SINCE 2016")
        val leader = BindableWrapper(alp)
        val swingPartyOrder = listOf(alp.party, ta.party, clp.party)
        val panel = candidateVotes(
                currentPrimaryVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousPrimaryVotes.binding, changeHeader.binding)
                .withPreferences(current2CPVotes.binding, preferenceHeader.binding, preferenceSubhead.binding)
                .withPrevPreferences(previous2PPVotes.binding)
                .withWinner(leader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
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
        val currentPrimaryVotes = BindableWrapper(currPrimary)
        val previousPrimaryVotes = BindableWrapper(prevPrimary)
        val current2CPVotes = BindableWrapper(curr2CP)
        val previous2PPVotes = BindableWrapper(prev2PP)
        val header = BindableWrapper("ARNHEM")
        val voteHeader = BindableWrapper("PRIMARY VOTE")
        val voteSubhead = BindableWrapper("7 OF 7 POLLS REPORTING")
        val preferenceHeader = BindableWrapper("TWO CANDIDATE PREFERRED")
        val preferenceSubhead = BindableWrapper("7 OF 7 POLLS REPORTING")
        val changeHeader = BindableWrapper("PRIMARY CHANGE SINCE 2016")
        val swingHeader = BindableWrapper("PREFERENCE SWING SINCE 2016")
        val leader = BindableWrapper(alp)
        val swingPartyOrder = listOf(alp.party, clp.party)
        val panel = candidateVotes(
                currentPrimaryVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousPrimaryVotes.binding, changeHeader.binding)
                .withPreferences(current2CPVotes.binding, preferenceHeader.binding, preferenceSubhead.binding)
                .withPrevPreferences(previous2PPVotes.binding)
                .withWinner(leader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
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
        val currentPrimaryVotes = BindableWrapper(currPrimary)
        val previousPrimaryVotes = BindableWrapper(prevPrimary)
        val current2CPVotes = BindableWrapper(curr2CP)
        val previous2PPVotes = BindableWrapper(prev2PP)
        val header = BindableWrapper("MELBOURNE")
        val voteHeader = BindableWrapper("PRIMARY VOTE")
        val voteSubhead = BindableWrapper("2016 RESULTS")
        val preferenceHeader = BindableWrapper("TWO CANDIDATE PREFERRED")
        val preferenceSubhead = BindableWrapper("2016 RESULTS")
        val changeHeader = BindableWrapper("PRIMARY CHANGE SINCE 2013")
        val swingHeader = BindableWrapper("PREFERENCE SWING SINCE 2013")
        val leader = BindableWrapper(grn)
        val swingPartyOrder = listOf(alp.party, grn.party, lib.party)
        val panel = candidateVotes(
                currentPrimaryVotes.binding,
                voteHeader.binding,
                voteSubhead.binding,
                "(MP)")
                .withPrev(previousPrimaryVotes.binding, changeHeader.binding)
                .withPreferences(current2CPVotes.binding, preferenceHeader.binding, preferenceSubhead.binding)
                .withPrevPreferences(previous2PPVotes.binding)
                .withWinner(leader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
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
                fixedBinding(
                        mapOf(
                                alp to (0.34).rangeTo(0.36),
                                coa to (0.42).rangeTo(0.43),
                                grn to (0.11).rangeTo(0.12),
                                onp to (0.02).rangeTo(0.04),
                                oth to (0.08).rangeTo(0.08))),
                fixedBinding("POLLING RANGE"),
                fixedBinding("NOVEMBER 2020"))
                .withPrev(
                        fixedBinding(
                                mapOf(
                                        alp to 4752160,
                                        coa to 5906875,
                                        grn to 1482923,
                                        onp to 438587,
                                        oth to 488817 + 69736 + 46931 + 479836 + 587528)),
                        fixedBinding("CHANGE SINCE 2019"))
                .withPreferences(
                        fixedBinding(
                                mapOf(
                                        alp to (0.49).rangeTo(0.495),
                                        coa to (0.505).rangeTo(0.51))),
                        fixedBinding("TWO PARTY PREFERRED"),
                        fixedBinding(""))
                .withPrevPreferences(fixedBinding(mapOf(alp to 6908580, coa to 7344813)))
                .withSwing(
                        compareBy { listOf(alp, coa).indexOf(it) },
                        fixedBinding("SWING SINCE 2019"))
                .build(fixedBinding("AUSTRALIA"))
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
        val currentPrimaryVotes = BindableWrapper(currPrimary)
        val previousPrimaryVotes = BindableWrapper(prevPrimary)
        val current2CPVotes = BindableWrapper(curr2CP)
        val previous2PPVotes = BindableWrapper(prev2PP)
        val header = BindableWrapper("MAYOR OF LONDON")
        val voteHeader = BindableWrapper("FIRST CHOICE VOTES")
        val voteSubhead = BindableWrapper("2016 RESULTS")
        val preferenceHeader = BindableWrapper("SECOND CHOICE VOTES")
        val preferenceSubhead = BindableWrapper("2016 RESULTS")
        val changeHeader = BindableWrapper("FIRST CHOICE CHANGE SINCE 2012")
        val swingHeader = BindableWrapper("SECOND CHOICE SWING SINCE 2012")
        val leader = BindableWrapper<Candidate?>(null)
        val swingPartyOrder = listOf(lab.party, grn.party, ld.party, con.party)
        val panel = candidateVotes(
                currentPrimaryVotes.binding, voteHeader.binding, voteSubhead.binding)
                .withPrev(previousPrimaryVotes.binding, changeHeader.binding)
                .withPreferences(current2CPVotes.binding, preferenceHeader.binding, preferenceSubhead.binding)
                .withPrevPreferences(previous2PPVotes.binding)
                .withWinner(leader.binding)
                .withSwing(compareBy { swingPartyOrder.indexOf(it) }, swingHeader.binding)
                .build(header.binding)
        panel.setSize(1024, 512)
        compareRendering("PreferenceVoteViewPanel", "Declaration-1", panel)
        currPrimary[grn] = 150673
        currPrimary[con] = 909755
        currentPrimaryVotes.value = currPrimary
        compareRendering("PreferenceVoteViewPanel", "Declaration-2", panel)
        currPrimary[lab] = 1148716
        currPrimary[ld] = 120005
        currentPrimaryVotes.value = currPrimary
        compareRendering("PreferenceVoteViewPanel", "Declaration-3", panel)
        currPrimary[ukip] = 94373
        currPrimary[oth] = 53055 + 37007 + 31372 + 20537 + 13325 + 13202 + 4941
        currentPrimaryVotes.value = currPrimary
        compareRendering("PreferenceVoteViewPanel", "Declaration-4", panel)
        curr2CP[con] = 994614
        current2CPVotes.value = curr2CP
        compareRendering("PreferenceVoteViewPanel", "Declaration-5", panel)
        curr2CP[lab] = 1310143
        current2CPVotes.value = curr2CP
        compareRendering("PreferenceVoteViewPanel", "Declaration-6", panel)
    }
}
