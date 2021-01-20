package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.Range;
import org.junit.Test;

public class PreferenceVoteViewPanelTest {

  @Test
  public void testCandidateBasicResult() throws IOException {
    Candidate alp = new Candidate("Mark Monaghan", new Party("Labor", "ALP", Color.RED));
    Candidate clp =
        new Candidate("Kylie Bonanni", new Party("Country Liberal", "CLP", Color.ORANGE));
    Candidate ta =
        new Candidate("Jeff Collins", new Party("Territory Alliance", "TA", Color.BLUE), true);
    Candidate ind = new Candidate("Amye Un", new Party("Independent", "IND", Color.GRAY));

    LinkedHashMap<Candidate, Integer> currPrimary = new LinkedHashMap<>();
    currPrimary.put(alp, 1756);
    currPrimary.put(clp, 1488);
    currPrimary.put(ta, 497);
    currPrimary.put(ind, 434);

    LinkedHashMap<Party, Integer> prevPrimary = new LinkedHashMap<>();
    prevPrimary.put(alp.getParty(), 1802);
    prevPrimary.put(clp.getParty(), 1439);
    prevPrimary.put(ta.getParty(), 356);
    prevPrimary.put(ind.getParty(), 384);

    LinkedHashMap<Candidate, Integer> curr2CP = new LinkedHashMap<>();
    curr2CP.put(alp, 2197);
    curr2CP.put(clp, 1978);

    LinkedHashMap<Party, Integer> prev2PP = new LinkedHashMap<>();
    prev2PP.put(alp.getParty(), 2171);
    prev2PP.put(clp.getParty(), 1588);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentPrimaryVotes =
        new BindableWrapper<>(currPrimary);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousPrimaryVotes =
        new BindableWrapper<>(prevPrimary);
    BindableWrapper<LinkedHashMap<Candidate, Integer>> current2CPVotes =
        new BindableWrapper<>(curr2CP);
    BindableWrapper<LinkedHashMap<Party, Integer>> previous2PPVotes =
        new BindableWrapper<>(prev2PP);
    BindableWrapper<String> header = new BindableWrapper<>("FONG LIM");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("PRIMARY VOTE");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("9 OF 9 POLLS REPORTING");
    BindableWrapper<String> preferenceHeader = new BindableWrapper<>("TWO CANDIDATE PREFERRED");
    BindableWrapper<String> preferenceSubhead = new BindableWrapper<>("9 OF 9 POLLS REPORTING");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("PRIMARY CHANGE SINCE 2016");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("PREFERENCE SWING SINCE 2016");
    BindableWrapper<Candidate> leader = new BindableWrapper<>(alp);
    List<Party> swingPartyOrder = Arrays.asList(alp.getParty(), ta.getParty(), clp.getParty());

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentPrimaryVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousPrimaryVotes.getBinding(), changeHeader.getBinding())
            .withPreferences(
                current2CPVotes.getBinding(),
                preferenceHeader.getBinding(),
                preferenceSubhead.getBinding())
            .withPrevPreferences(previous2PPVotes.getBinding())
            .withWinner(leader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("PreferenceVoteViewPanel", "Basic-1", panel);
  }

  @Test
  public void testCandidateScreenUpdating() throws IOException {
    Candidate alp = new Candidate("Mark Monaghan", new Party("Labor", "ALP", Color.RED));
    Candidate clp =
        new Candidate("Kylie Bonanni", new Party("Country Liberal", "CLP", Color.ORANGE));
    Candidate ta =
        new Candidate("Jeff Collins", new Party("Territory Alliance", "TA", Color.BLUE), true);
    Candidate ind = new Candidate("Amye Un", new Party("Independent", "IND", Color.GRAY));

    LinkedHashMap<Candidate, Integer> currPrimary = new LinkedHashMap<>();
    currPrimary.put(alp, 0);
    currPrimary.put(clp, 0);
    currPrimary.put(ta, 0);
    currPrimary.put(ind, 0);

    LinkedHashMap<Party, Integer> prevPrimary = new LinkedHashMap<>();
    prevPrimary.put(alp.getParty(), 1802);
    prevPrimary.put(clp.getParty(), 1439);
    prevPrimary.put(ta.getParty(), 356);
    prevPrimary.put(ind.getParty(), 384);

    LinkedHashMap<Candidate, Integer> curr2CP = new LinkedHashMap<>();

    LinkedHashMap<Party, Integer> prev2PP = new LinkedHashMap<>();
    prev2PP.put(alp.getParty(), 2171);
    prev2PP.put(clp.getParty(), 1588);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentPrimaryVotes =
        new BindableWrapper<>(currPrimary);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousPrimaryVotes =
        new BindableWrapper<>(prevPrimary);
    BindableWrapper<LinkedHashMap<Candidate, Integer>> current2CPVotes =
        new BindableWrapper<>(curr2CP);
    BindableWrapper<LinkedHashMap<Party, Integer>> previous2PPVotes =
        new BindableWrapper<>(prev2PP);
    BindableWrapper<Double> pctReporting = new BindableWrapper<>(0.0);
    BindableWrapper<Double> preferencePctReporting = new BindableWrapper<>(0.0);
    BindableWrapper<String> header = new BindableWrapper<>("FONG LIM");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("PRIMARY VOTE");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("0 OF 9 POLLS REPORTING");
    BindableWrapper<String> preferenceHeader = new BindableWrapper<>("TWO CANDIDATE PREFERRED");
    BindableWrapper<String> preferenceSubhead = new BindableWrapper<>("0 OF 9 POLLS REPORTING");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("PRIMARY CHANGE SINCE 2016");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("PREFERENCE SWING SINCE 2016");
    BindableWrapper<Candidate> leader = new BindableWrapper<>(null);
    List<Party> swingPartyOrder = Arrays.asList(alp.getParty(), ta.getParty(), clp.getParty());

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentPrimaryVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousPrimaryVotes.getBinding(), changeHeader.getBinding())
            .withPctReporting(pctReporting.getBinding())
            .withPreferences(
                current2CPVotes.getBinding(),
                preferenceHeader.getBinding(),
                preferenceSubhead.getBinding())
            .withPrevPreferences(previous2PPVotes.getBinding())
            .withPreferencePctReporting(preferencePctReporting.getBinding())
            .withWinner(leader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("PreferenceVoteViewPanel", "Update-1", panel);

    currPrimary.put(alp, 13);
    currPrimary.put(clp, 13);
    currPrimary.put(ta, 6);
    currPrimary.put(ind, 5);
    currentPrimaryVotes.setValue(currPrimary);
    curr2CP.put(alp, 0);
    curr2CP.put(clp, 0);
    current2CPVotes.setValue(curr2CP);
    pctReporting.setValue(1.0 / 9);
    voteSubhead.setValue("1 OF 9 POLLS REPORTING");
    compareRendering("PreferenceVoteViewPanel", "Update-2", panel);

    currPrimary.put(alp, 365);
    currPrimary.put(clp, 262);
    currPrimary.put(ta, 86);
    currPrimary.put(ind, 83);
    currentPrimaryVotes.setValue(currPrimary);
    curr2CP.put(alp, 18);
    curr2CP.put(clp, 19);
    current2CPVotes.setValue(curr2CP);
    pctReporting.setValue(3.0 / 9);
    voteSubhead.setValue("3 OF 9 POLLS REPORTING");
    preferencePctReporting.setValue(1.0 / 9);
    preferenceSubhead.setValue("1 OF 9 POLLS REPORTING");
    compareRendering("PreferenceVoteViewPanel", "Update-3", panel);

    currPrimary.put(alp, 1756);
    currPrimary.put(clp, 1488);
    currPrimary.put(ta, 497);
    currPrimary.put(ind, 434);
    currentPrimaryVotes.setValue(currPrimary);
    curr2CP.put(alp, 464);
    curr2CP.put(clp, 332);
    current2CPVotes.setValue(curr2CP);
    pctReporting.setValue(9.0 / 9);
    voteSubhead.setValue("9 OF 9 POLLS REPORTING");
    preferencePctReporting.setValue(3.0 / 9);
    preferenceSubhead.setValue("3 OF 9 POLLS REPORTING");
    leader.setValue(alp);
    compareRendering("PreferenceVoteViewPanel", "Update-4", panel);

    curr2CP.put(alp, 2197);
    curr2CP.put(clp, 1978);
    current2CPVotes.setValue(curr2CP);
    preferencePctReporting.setValue(9.0 / 9);
    preferenceSubhead.setValue("9 OF 9 POLLS REPORTING");
    compareRendering("PreferenceVoteViewPanel", "Update-5", panel);
  }

  @Test
  public void testCandidatesSwitchingBetweenSingleAndDoubleLines() throws IOException {
    Party alp = new Party("Labor", "ALP", Color.RED);
    Party clp = new Party("Country Liberal", "CLP", Color.ORANGE);
    Party ta = new Party("Territory Alliance", "TA", Color.BLUE);
    Party grn = new Party("Greens", "GRN", Color.GREEN.darker());
    Party ind = new Party("Independent", "IND", Color.GRAY);

    BindableWrapper<Map<Candidate, Integer>> currentPrimaryVotes =
        new BindableWrapper<>(
            Map.of(
                new Candidate("Amye Un", ind), 434,
                new Candidate("Mark Monaghan", alp), 1756,
                new Candidate("Jeff Collins", ta, true), 497,
                new Candidate("Kylie Bonanni", clp), 1488));
    BindableWrapper<Map<Party, Integer>> previousPrimaryVotes =
        new BindableWrapper<>(Map.of(alp, 1802, clp, 1439, ta, 356, ind, 384));
    BindableWrapper<Map<Candidate, Integer>> current2CPVotes =
        new BindableWrapper<>(
            Map.of(
                new Candidate("Mark Monaghan", alp), 2197,
                new Candidate("Kylie Bonanni", clp), 1978));
    BindableWrapper<Map<Party, Integer>> previous2PPVotes =
        new BindableWrapper<>(Map.of(alp, 2171, clp, 1588));
    BindableWrapper<String> header = new BindableWrapper<>("FONG LIM");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("PRIMARY VOTE");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("9 OF 9 POLLS REPORTING");
    BindableWrapper<String> preferenceHeader = new BindableWrapper<>("TWO CANDIDATE PREFERRED");
    BindableWrapper<String> preferenceSubhead = new BindableWrapper<>("9 OF 9 POLLS REPORTING");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("PRIMARY CHANGE SINCE 2016");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("PREFERENCE SWING SINCE 2016");
    BindableWrapper<Candidate> leader = new BindableWrapper<>(null);
    List<Party> swingPartyOrder = Arrays.asList(alp, ta, clp);

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentPrimaryVotes.getBinding(),
                voteHeader.getBinding(),
                voteSubhead.getBinding(),
                "(MP)")
            .withPrev(previousPrimaryVotes.getBinding(), changeHeader.getBinding())
            .withPreferences(
                current2CPVotes.getBinding(),
                preferenceHeader.getBinding(),
                preferenceSubhead.getBinding())
            .withPrevPreferences(previous2PPVotes.getBinding())
            .withWinner(leader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("PreferenceVoteViewPanel", "LotsOfCandidates-1", panel);

    header.setValue("GOYDER");
    voteSubhead.setValue("12 OF 12 POLLS REPORTING");
    preferenceSubhead.setValue("12 OF 12 POLLS REPORTING");
    currentPrimaryVotes.setValue(
        Map.of(
            new Candidate("Rachel Wright", ta), 614,
            new Candidate("Ted Warren", ind), 249,
            new Candidate("Phil Battye", clp), 1289,
            new Candidate("Trevor Jenkins", ind), 64,
            new Candidate("Kezia Purick", ind, true), 1459,
            new Candidate("Mick Taylor", alp), 590,
            new Candidate("Karen Fletcher", grn), 147,
            new Candidate("Pauline Cass", ind), 283));
    previousPrimaryVotes.setValue(Map.of(ind, 2496 + 76, clp, 919, grn, 188, alp, 860));
    current2CPVotes.setValue(
        Map.of(
            new Candidate("Phil Battye", clp), 2030,
            new Candidate("Kezia Purick", ind, true), 2665));
    previous2PPVotes.setValue(Map.of(ind, 3109, clp, 1020));
    compareRendering("PreferenceVoteViewPanel", "LotsOfCandidates-2", panel);
  }

  @Test
  public void testSinglePreference() throws IOException {
    Candidate alp = new Candidate("Kate Worden", new Party("Labor", "ALP", Color.RED));
    Candidate clp =
        new Candidate("Derek Mayger", new Party("Country Liberal", "CLP", Color.ORANGE));
    Candidate ta = new Candidate("Amelia Nuku", new Party("Territory Alliance", "TA", Color.BLUE));
    Party ind = new Party("Independent", "IND", Color.GRAY);

    LinkedHashMap<Candidate, Integer> currPrimary = new LinkedHashMap<>();
    currPrimary.put(alp, 2632);
    currPrimary.put(clp, 968);
    currPrimary.put(ta, 795);

    LinkedHashMap<Party, Integer> prevPrimary = new LinkedHashMap<>();
    prevPrimary.put(alp.getParty(), 2323);
    prevPrimary.put(clp.getParty(), 1573);
    prevPrimary.put(ta.getParty(), 135);
    prevPrimary.put(ind, 331 + 81);

    LinkedHashMap<Candidate, Integer> curr2CP = new LinkedHashMap<>();
    curr2CP.put(alp, 0);

    LinkedHashMap<Party, Integer> prev2PP = new LinkedHashMap<>();
    prev2PP.put(alp.getParty(), 2578);
    prev2PP.put(clp.getParty(), 1680);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentPrimaryVotes =
        new BindableWrapper<>(currPrimary);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousPrimaryVotes =
        new BindableWrapper<>(prevPrimary);
    BindableWrapper<LinkedHashMap<Candidate, Integer>> current2CPVotes =
        new BindableWrapper<>(curr2CP);
    BindableWrapper<LinkedHashMap<Party, Integer>> previous2PPVotes =
        new BindableWrapper<>(prev2PP);
    BindableWrapper<String> header = new BindableWrapper<>("SANDERSON");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("PRIMARY VOTE");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("7 OF 7 POLLS REPORTING");
    BindableWrapper<String> preferenceHeader = new BindableWrapper<>("TWO CANDIDATE PREFERRED");
    BindableWrapper<String> preferenceSubhead = new BindableWrapper<>("7 OF 7 POLLS REPORTING");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("PRIMARY CHANGE SINCE 2016");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("PREFERENCE SWING SINCE 2016");
    BindableWrapper<Candidate> leader = new BindableWrapper<>(alp);
    List<Party> swingPartyOrder = Arrays.asList(alp.getParty(), ta.getParty(), clp.getParty());

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentPrimaryVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousPrimaryVotes.getBinding(), changeHeader.getBinding())
            .withPreferences(
                current2CPVotes.getBinding(),
                preferenceHeader.getBinding(),
                preferenceSubhead.getBinding())
            .withPrevPreferences(previous2PPVotes.getBinding())
            .withWinner(leader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("PreferenceVoteViewPanel", "SinglePreference-1", panel);
  }

  @Test
  public void testNoPrevPreference() throws IOException {
    Candidate alp = new Candidate("Selena Uibo", new Party("Labor", "ALP", Color.RED));
    Candidate clp = new Candidate("Jerry Amato", new Party("Country Liberal", "CLP", Color.ORANGE));
    Party ind = new Party("Independent", "IND", Color.GRAY);
    Candidate lawrence = new Candidate("Lance Lawrence", ind);
    Candidate gumbula = new Candidate("Ian Mongunu Gumbula", ind);

    LinkedHashMap<Candidate, Integer> currPrimary = new LinkedHashMap<>();
    currPrimary.put(alp, 1207);
    currPrimary.put(clp, 487);
    currPrimary.put(lawrence, 243);
    currPrimary.put(gumbula, 987);

    LinkedHashMap<Party, Integer> prevPrimary = new LinkedHashMap<>();
    prevPrimary.put(alp.getParty(), 1565);
    prevPrimary.put(clp.getParty(), 804);
    prevPrimary.put(ind, 211 + 197 + 117);

    LinkedHashMap<Candidate, Integer> curr2CP = new LinkedHashMap<>();
    curr2CP.put(alp, 1508);
    curr2CP.put(gumbula, 1416);

    LinkedHashMap<Party, Integer> prev2PP = new LinkedHashMap<>();

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentPrimaryVotes =
        new BindableWrapper<>(currPrimary);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousPrimaryVotes =
        new BindableWrapper<>(prevPrimary);
    BindableWrapper<LinkedHashMap<Candidate, Integer>> current2CPVotes =
        new BindableWrapper<>(curr2CP);
    BindableWrapper<LinkedHashMap<Party, Integer>> previous2PPVotes =
        new BindableWrapper<>(prev2PP);
    BindableWrapper<String> header = new BindableWrapper<>("ARNHEM");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("PRIMARY VOTE");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("7 OF 7 POLLS REPORTING");
    BindableWrapper<String> preferenceHeader = new BindableWrapper<>("TWO CANDIDATE PREFERRED");
    BindableWrapper<String> preferenceSubhead = new BindableWrapper<>("7 OF 7 POLLS REPORTING");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("PRIMARY CHANGE SINCE 2016");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("PREFERENCE SWING SINCE 2016");
    BindableWrapper<Candidate> leader = new BindableWrapper<>(alp);
    List<Party> swingPartyOrder = Arrays.asList(alp.getParty(), clp.getParty());

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentPrimaryVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousPrimaryVotes.getBinding(), changeHeader.getBinding())
            .withPreferences(
                current2CPVotes.getBinding(),
                preferenceHeader.getBinding(),
                preferenceSubhead.getBinding())
            .withPrevPreferences(previous2PPVotes.getBinding())
            .withWinner(leader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("PreferenceVoteViewPanel", "NoPrevPreference-1", panel);
  }

  @Test
  public void testChangeInPreference() throws IOException {
    Candidate alp = new Candidate("Sophie Ismail", new Party("Labor", "ALP", Color.RED));
    Candidate lib = new Candidate("Le Liu", new Party("Liberal", "LIB", Color.BLUE));
    Candidate grn =
        new Candidate("Adam Bandt", new Party("Greens", "GRN", Color.GREEN.darker()), true);
    Candidate ind = new Candidate("Others", new Party("Independent", "IND", Color.GRAY));

    LinkedHashMap<Candidate, Integer> currPrimary = new LinkedHashMap<>();
    currPrimary.put(alp, 23130);
    currPrimary.put(lib, 23878);
    currPrimary.put(grn, 41377);
    currPrimary.put(ind, 94579 - currPrimary.values().stream().mapToInt(i -> i).sum());

    LinkedHashMap<Party, Integer> prevPrimary = new LinkedHashMap<>();
    prevPrimary.put(alp.getParty(), 22490);
    prevPrimary.put(lib.getParty(), 19301);
    prevPrimary.put(grn.getParty(), 36035);
    prevPrimary.put(ind.getParty(), 84551 - prevPrimary.values().stream().mapToInt(i -> i).sum());

    LinkedHashMap<Candidate, Integer> curr2CP = new LinkedHashMap<>();
    curr2CP.put(grn, 64771);
    curr2CP.put(lib, 29808);

    LinkedHashMap<Party, Integer> prev2PP = new LinkedHashMap<>();
    prev2PP.put(grn.getParty(), 46732);
    prev2PP.put(alp.getParty(), 37819);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentPrimaryVotes =
        new BindableWrapper<>(currPrimary);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousPrimaryVotes =
        new BindableWrapper<>(prevPrimary);
    BindableWrapper<LinkedHashMap<Candidate, Integer>> current2CPVotes =
        new BindableWrapper<>(curr2CP);
    BindableWrapper<LinkedHashMap<Party, Integer>> previous2PPVotes =
        new BindableWrapper<>(prev2PP);
    BindableWrapper<String> header = new BindableWrapper<>("MELBOURNE");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("PRIMARY VOTE");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("2016 RESULTS");
    BindableWrapper<String> preferenceHeader = new BindableWrapper<>("TWO CANDIDATE PREFERRED");
    BindableWrapper<String> preferenceSubhead = new BindableWrapper<>("2016 RESULTS");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("PRIMARY CHANGE SINCE 2013");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("PREFERENCE SWING SINCE 2013");
    BindableWrapper<Candidate> leader = new BindableWrapper<>(grn);
    List<Party> swingPartyOrder = Arrays.asList(alp.getParty(), grn.getParty(), lib.getParty());

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentPrimaryVotes.getBinding(),
                voteHeader.getBinding(),
                voteSubhead.getBinding(),
                "(MP)")
            .withPrev(previousPrimaryVotes.getBinding(), changeHeader.getBinding())
            .withPreferences(
                current2CPVotes.getBinding(),
                preferenceHeader.getBinding(),
                preferenceSubhead.getBinding())
            .withPrevPreferences(previous2PPVotes.getBinding())
            .withWinner(leader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("PreferenceVoteViewPanel", "ChangeInPreference-1", panel);
  }

  @Test
  public void testRanges() throws IOException {
    Party alp = new Party("Labor", "ALP", Color.RED);
    Party coa = new Party("Coalition", "L/NP", Color.BLUE);
    Party grn = new Party("Greens", "GRN", Color.GREEN.darker());
    Party onp = new Party("One Nation", "ONP", Color.ORANGE);
    Party oth = Party.OTHERS;

    BasicResultPanel panel =
        BasicResultPanel.partyRangeVotes(
                Binding.fixedBinding(
                    Map.of(
                        alp, Range.between(0.34, 0.36),
                        coa, Range.between(0.42, 0.43),
                        grn, Range.between(0.11, 0.12),
                        onp, Range.between(0.02, 0.04),
                        oth, Range.between(0.08, 0.08))),
                Binding.fixedBinding("POLLING RANGE"),
                Binding.fixedBinding("NOVEMBER 2020"))
            .withPrev(
                Binding.fixedBinding(
                    Map.of(
                        alp, 4752160,
                        coa, 5906875,
                        grn, 1482923,
                        onp, 438587,
                        oth, 488817 + 69736 + 46931 + 479836 + 587528)),
                Binding.fixedBinding("CHANGE SINCE 2019"))
            .withPreferences(
                Binding.fixedBinding(
                    Map.of(
                        alp, Range.between(0.49, 0.495),
                        coa, Range.between(0.505, 0.51))),
                Binding.fixedBinding("TWO PARTY PREFERRED"),
                Binding.fixedBinding(""))
            .withPrevPreferences(Binding.fixedBinding(Map.of(alp, 6908580, coa, 7344813)))
            .withSwing(
                Comparator.comparing(List.of(alp, coa)::indexOf),
                Binding.fixedBinding("SWING SINCE 2019"))
            .build(Binding.fixedBinding("AUSTRALIA"));
    panel.setSize(1024, 512);
    compareRendering("PreferenceVoteViewPanel", "Ranges-1", panel);
  }
}
