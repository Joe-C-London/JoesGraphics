package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import com.joecollins.models.general.PartyResult;
import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class TooCloseToCallScreenTest {

  private static Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
  private static Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
  private static Party lib = new Party("Liberal", "LIB", Color.RED);
  private static Party grn = new Party("Green", "GRN", Color.GREEN.darker());
  private static Party ind = new Party("Independent", "IND", Color.GRAY);

  @Test
  public void testBasic() throws IOException {
    Map<Integer, Map<Candidate, Integer>> candidateVotesRaw = new HashMap<>();
    Map<Integer, PartyResult> partyResultsRaw = new HashMap<>();
    BindableWrapper<Map<Integer, Map<Candidate, Integer>>> candidateVotes =
        new BindableWrapper<>(candidateVotesRaw);
    BindableWrapper<Map<Integer, PartyResult>> partyResults =
        new BindableWrapper<>(partyResultsRaw);

    TooCloseToCallScreen screen =
        TooCloseToCallScreen.of(
                candidateVotes.getBinding(),
                partyResults.getBinding(),
                d -> "DISTRICT " + d,
                Binding.fixedBinding("TOO CLOSE TO CALL"))
            .build(Binding.fixedBinding("PRINCE EDWARD ISLAND"));
    screen.setSize(1024, 512);
    compareRendering("TooCloseToCallScreen", "Basic-1", screen);

    setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, new HashMap<>());
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "Basic-2", screen);

    setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, new HashMap<>());
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "Basic-3", screen);

    setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, new HashMap<>());
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "Basic-4", screen);

    setupFullResults(candidateVotesRaw, partyResultsRaw, new HashMap<>());
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "Basic-1", screen);
  }

  @Test
  public void testPctReporting() throws IOException {
    Map<Integer, Map<Candidate, Integer>> candidateVotesRaw = new HashMap<>();
    Map<Integer, PartyResult> partyResultsRaw = new HashMap<>();
    Map<Integer, Double> pctReportingRaw = new HashMap<>();
    BindableWrapper<Map<Integer, Map<Candidate, Integer>>> candidateVotes =
        new BindableWrapper<>(candidateVotesRaw);
    BindableWrapper<Map<Integer, PartyResult>> partyResults =
        new BindableWrapper<>(partyResultsRaw);
    BindableWrapper<Map<Integer, Double>> pctReporting = new BindableWrapper<>(pctReportingRaw);

    TooCloseToCallScreen screen =
        TooCloseToCallScreen.of(
                candidateVotes.getBinding(),
                partyResults.getBinding(),
                d -> "DISTRICT " + d,
                Binding.fixedBinding("TOO CLOSE TO CALL"))
            .withPctReporting(pctReporting.getBinding())
            .build(Binding.fixedBinding("PRINCE EDWARD ISLAND"));
    screen.setSize(1024, 512);
    compareRendering("TooCloseToCallScreen", "PctReporting-1", screen);

    setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, pctReportingRaw);
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "PctReporting-2", screen);

    setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw);
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "PctReporting-3", screen);

    setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw);
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "PctReporting-4", screen);

    setupFullResults(candidateVotesRaw, partyResultsRaw, pctReportingRaw);
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "PctReporting-1", screen);
  }

  @Test
  public void testLimitRows() throws IOException {
    Map<Integer, Map<Candidate, Integer>> candidateVotesRaw = new HashMap<>();
    Map<Integer, PartyResult> partyResultsRaw = new HashMap<>();
    Map<Integer, Double> pctReportingRaw = new HashMap<>();
    BindableWrapper<Map<Integer, Map<Candidate, Integer>>> candidateVotes =
        new BindableWrapper<>(candidateVotesRaw);
    BindableWrapper<Map<Integer, PartyResult>> partyResults =
        new BindableWrapper<>(partyResultsRaw);
    BindableWrapper<Map<Integer, Double>> pctReporting = new BindableWrapper<>(pctReportingRaw);

    TooCloseToCallScreen screen =
        TooCloseToCallScreen.of(
                candidateVotes.getBinding(),
                partyResults.getBinding(),
                d -> "DISTRICT " + d,
                Binding.fixedBinding("TOO CLOSE TO CALL"))
            .withPctReporting(pctReporting.getBinding())
            .withMaxRows(Binding.fixedBinding(15))
            .build(Binding.fixedBinding("PRINCE EDWARD ISLAND"));
    screen.setSize(1024, 512);
    compareRendering("TooCloseToCallScreen", "LimitRows-1", screen);

    setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, pctReportingRaw);
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "LimitRows-2", screen);

    setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw);
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "LimitRows-3", screen);

    setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw);
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "LimitRows-4", screen);

    setupFullResults(candidateVotesRaw, partyResultsRaw, pctReportingRaw);
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "LimitRows-1", screen);
  }

  @Test
  public void testNumberOfCandidates() throws IOException {
    Map<Integer, Map<Candidate, Integer>> candidateVotesRaw = new HashMap<>();
    Map<Integer, PartyResult> partyResultsRaw = new HashMap<>();
    Map<Integer, Double> pctReportingRaw = new HashMap<>();
    BindableWrapper<Map<Integer, Map<Candidate, Integer>>> candidateVotes =
        new BindableWrapper<>(candidateVotesRaw);
    BindableWrapper<Map<Integer, PartyResult>> partyResults =
        new BindableWrapper<>(partyResultsRaw);
    BindableWrapper<Map<Integer, Double>> pctReporting = new BindableWrapper<>(pctReportingRaw);

    TooCloseToCallScreen screen =
        TooCloseToCallScreen.of(
                candidateVotes.getBinding(),
                partyResults.getBinding(),
                d -> "DISTRICT " + d,
                Binding.fixedBinding("TOO CLOSE TO CALL"))
            .withPctReporting(pctReporting.getBinding())
            .withNumberOfCandidates(Binding.fixedBinding(5))
            .build(Binding.fixedBinding("PRINCE EDWARD ISLAND"));
    screen.setSize(1024, 512);
    compareRendering("TooCloseToCallScreen", "NumCandidates-1", screen);

    setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, pctReportingRaw);
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "NumCandidates-2", screen);

    setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw);
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "NumCandidates-3", screen);

    setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw);
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "NumCandidates-4", screen);

    setupFullResults(candidateVotesRaw, partyResultsRaw, pctReportingRaw);
    candidateVotes.setValue(candidateVotesRaw);
    partyResults.setValue(partyResultsRaw);
    compareRendering("TooCloseToCallScreen", "NumCandidates-1", screen);
  }

  private void setupFirstAdvancePoll(
      Map<Integer, Map<Candidate, Integer>> candidateVotes,
      Map<Integer, PartyResult> partyResults,
      Map<Integer, Double> pctReporting) {
    candidateVotes.put(
        1,
        Map.of(
            new Candidate("Tommy Kickham", lib), 467,
            new Candidate("Colin Lavie", pc), 684,
            new Candidate("Boyd Leard", grn), 365));
    partyResults.put(1, PartyResult.leading(pc));
    pctReporting.put(1, 1.0 / 10);
  }

  private void setupAllAdvancePolls(
      Map<Integer, Map<Candidate, Integer>> candidateVotes,
      Map<Integer, PartyResult> partyResults,
      Map<Integer, Double> pctReporting) {
    candidateVotes.put(
        1,
        Map.of(
            new Candidate("Tommy Kickham", lib), 467,
            new Candidate("Colin Lavie", pc), 684,
            new Candidate("Boyd Leard", grn), 365));
    partyResults.put(1, PartyResult.leading(pc));
    pctReporting.put(1, 1.0 / 10);

    candidateVotes.put(
        2,
        Map.of(
            new Candidate("Kevin Doyle", lib), 288,
            new Candidate("Susan Hartley", grn), 308,
            new Candidate("Steven Myers", pc), 555,
            new Candidate("Edith Perry", ndp), 23));
    partyResults.put(2, PartyResult.leading(pc));
    pctReporting.put(2, 1.0 / 10);

    candidateVotes.put(
        3,
        Map.of(
            new Candidate("Billy Cann", ndp), 78,
            new Candidate("Cory Deagle", pc), 872,
            new Candidate("Daphne Griffin", lib), 451,
            new Candidate("John Allen Maclean", grn), 289));
    partyResults.put(3, PartyResult.elected(pc));
    pctReporting.put(3, 1.0 / 9);

    candidateVotes.put(
        4,
        Map.of(
            new Candidate("Darlene Compton", pc), 588,
            new Candidate("Ian MacPherson", lib), 240,
            new Candidate("James Sanders", grn), 232));
    partyResults.put(4, PartyResult.elected(pc));
    pctReporting.put(4, 1.0 / 10);

    candidateVotes.put(
        5,
        Map.of(
            new Candidate("Michele Beaton", grn), 482,
            new Candidate("Randy Cooper", lib), 518,
            new Candidate("Mary Ellen McInnis", pc), 533,
            new Candidate("Lawrence Millar", ndp), 18));
    partyResults.put(5, PartyResult.leading(pc));
    pctReporting.put(5, 1.0 / 8);

    candidateVotes.put(
        6,
        Map.of(
            new Candidate("James Aylward", pc), 725,
            new Candidate("David Dunphy", lib), 526,
            new Candidate("Devon Strang", grn), 348,
            new Candidate("Lynne Thiele", ndp), 17));
    partyResults.put(6, PartyResult.leading(pc));
    pctReporting.put(6, 1.0 / 9);

    candidateVotes.put(
        7,
        Map.of(
            new Candidate("Margaret Andrade", ndp), 12,
            new Candidate("Kyle MacDonald", grn), 184,
            new Candidate("Sidney MacEwan", pc), 610,
            new Candidate("Susan Myers", lib), 203));
    partyResults.put(7, PartyResult.elected(pc));
    pctReporting.put(7, 1.0 / 11);

    candidateVotes.put(
        8,
        Map.of(
            new Candidate("Sarah Donald", grn), 285,
            new Candidate("Wade MacLauchlan", lib), 620,
            new Candidate("Bloyce Thompson", pc), 609,
            new Candidate("Marian White", ndp), 22));
    partyResults.put(8, PartyResult.leading(lib));
    pctReporting.put(8, 1.0 / 10);

    candidateVotes.put(
        9,
        Map.of(
            new Candidate("John Andrew", grn), 363,
            new Candidate("Gordon Gay", ndp), 19,
            new Candidate("Natalie Jameson", pc), 620,
            new Candidate("Karen Lavers", lib), 395));
    partyResults.put(9, PartyResult.leading(pc));
    pctReporting.put(9, 1.0 / 11);

    candidateVotes.put(
        10,
        Map.of(
            new Candidate("Mike Gillis", pc), 510,
            new Candidate("Robert Mitchell", lib), 808,
            new Candidate("Amanda Morrison", grn), 516,
            new Candidate("Jesse Reddin Cousins", ndp), 27));
    partyResults.put(10, PartyResult.leading(lib));
    pctReporting.put(10, 1.0 / 10);

    candidateVotes.put(
        11,
        Map.of(
            new Candidate("Hannah Bell", grn), 636,
            new Candidate("Ronnie Carragher", pc), 595,
            new Candidate("Roxanne Carter-Thompson", lib), 534,
            new Candidate("Trevor Leclerc", ndp), 36));
    partyResults.put(11, PartyResult.leading(grn));
    pctReporting.put(11, 1.0 / 10);

    candidateVotes.put(
        12,
        Map.of(
            new Candidate("Karla Bernard", grn), 475,
            new Candidate("Richard Brown", lib), 478,
            new Candidate("Joe Byrne", ndp), 172,
            new Candidate("Tim Keizer", pc), 352));
    partyResults.put(12, PartyResult.leading(lib));
    pctReporting.put(12, 1.0 / 10);

    candidateVotes.put(
        13,
        Map.of(
            new Candidate("Jordan Brown", lib), 717,
            new Candidate("Ole Hammarlund", grn), 542,
            new Candidate("Donna Hurry", pc), 331,
            new Candidate("Simone Webster", ndp), 75));
    partyResults.put(13, PartyResult.leading(lib));
    pctReporting.put(13, 1.0 / 10);

    candidateVotes.put(
        14,
        Map.of(
            new Candidate("Angus Birt", pc), 492,
            new Candidate("Bush Dumville", ind), 131,
            new Candidate("Gavin Hall", grn), 437,
            new Candidate("Gord MacNeilly", lib), 699,
            new Candidate("Janis Newman", ndp), 34));
    partyResults.put(14, PartyResult.leading(lib));
    pctReporting.put(14, 1.0 / 10);

    candidateVotes.put(
        15,
        Map.of(
            new Candidate("Greg Bradley", grn), 287,
            new Candidate("Leah-Jane Hayward", ndp), 27,
            new Candidate("Dennis King", pc), 583,
            new Candidate("Windsor Wight", lib), 425));
    partyResults.put(15, PartyResult.leading(pc));
    pctReporting.put(15, 1.0 / 10);

    candidateVotes.put(
        16,
        Map.of(
            new Candidate("Elaine Barnes", pc), 296,
            new Candidate("Ellen Jones", grn), 542,
            new Candidate("Heath MacDonald", lib), 983,
            new Candidate("Craig Nash", ndp), 425));
    partyResults.put(16, PartyResult.leading(lib));
    pctReporting.put(16, 1.0 / 10);

    candidateVotes.put(
        17,
        Map.of(
            new Candidate("Peter Bevan-Baker", grn), 851,
            new Candidate("Kris Currie", pc), 512,
            new Candidate("Judy MacNevin", lib), 290,
            new Candidate("Don Wills", ind), 7));
    partyResults.put(17, PartyResult.elected(grn));
    pctReporting.put(17, 1.0 / 10);

    candidateVotes.put(
        18,
        Map.of(
            new Candidate("Sean Deagle", ndp), 15,
            new Candidate("Colin Jeffrey", grn), 271,
            new Candidate("Sandy MacKay", lib), 196,
            new Candidate("Brad Trivers", pc), 710));
    partyResults.put(18, PartyResult.elected(pc));
    pctReporting.put(18, 1.0 / 10);

    candidateVotes.put(
        19,
        Map.of(
            new Candidate("Jamie Fox", pc), 647,
            new Candidate("Joan Gauvin", ndp), 7,
            new Candidate("Matthew MacFarlane", grn), 311,
            new Candidate("Fred McCardle", ind), 18,
            new Candidate("Jamie Stride", lib), 167));
    partyResults.put(19, PartyResult.elected(pc));
    pctReporting.put(19, 1.0 / 10);

    candidateVotes.put(
        20,
        Map.of(
            new Candidate("Nancy Beth Guptill", lib), 203,
            new Candidate("Carole MacFarlane", ndp), 21,
            new Candidate("Matthew MacKay", pc), 1166,
            new Candidate("Matthew J. MacKay", grn), 342));
    partyResults.put(20, PartyResult.elected(pc));
    pctReporting.put(20, 1.0 / 10);

    candidateVotes.put(
        21,
        Map.of(
            new Candidate("Tyler Desroches", pc), 577,
            new Candidate("Paulette Halupa", ndp), 18,
            new Candidate("Lynne Lund", grn), 617,
            new Candidate("Chris Palmer", lib), 563));
    partyResults.put(21, PartyResult.leading(grn));
    pctReporting.put(21, 1.0 / 10);

    candidateVotes.put(
        22,
        Map.of(
            new Candidate("Steve Howard", grn), 602,
            new Candidate("Tina Mundy", lib), 560,
            new Candidate("Garth Oatway", ndp), 34,
            new Candidate("Paul Walsh", pc), 335));
    partyResults.put(22, PartyResult.leading(grn));
    pctReporting.put(22, 1.0 / 10);

    candidateVotes.put(
        23,
        Map.of(
            new Candidate("Trish Altass", grn), 428,
            new Candidate("Paula Biggar", lib), 379,
            new Candidate("Robin John Robert Ednman", ndp), 34,
            new Candidate("Holton A MacLennan", pc), 436));
    partyResults.put(23, PartyResult.leading(grn));
    pctReporting.put(23, 1.0 / 10);

    candidateVotes.put(
        24,
        Map.of(
            new Candidate("Nick Arsenault", grn), 197,
            new Candidate("Sonny Gallant", lib), 330,
            new Candidate("Grant Gallant", ndp), 14,
            new Candidate("Jaosn Woodbury", pc), 144));
    partyResults.put(24, PartyResult.leading(lib));
    pctReporting.put(24, 1.0 / 8);

    candidateVotes.put(
        25,
        Map.of(
            new Candidate("Barb Broome", pc), 177,
            new Candidate("Jason Charette", grn), 62,
            new Candidate("Dr. Herb Dickieson", ndp), 425,
            new Candidate("Robert Henderson", lib), 454));
    partyResults.put(25, PartyResult.leading(lib));
    pctReporting.put(25, 1.0 / 11);

    candidateVotes.put(
        26,
        Map.of(
            new Candidate("Michelle Arsenault", ndp), 47,
            new Candidate("Ernie Hudson", pc), 700,
            new Candidate("James McKenna", grn), 122,
            new Candidate("Pat Murphy", lib), 686));
    partyResults.put(26, PartyResult.leading(pc));
    pctReporting.put(26, 1.0 / 10);

    candidateVotes.put(
        27,
        Map.of(
            new Candidate("Sean Doyle", grn), 241,
            new Candidate("Melissa Handrahan", pc), 405,
            new Candidate("Hal Perry", lib), 646,
            new Candidate("Dale Ryan", ndp), 18));
    partyResults.put(27, PartyResult.leading(lib));
    pctReporting.put(27, 1.0 / 10);
  }

  private void setupHalfOfPolls(
      Map<Integer, Map<Candidate, Integer>> candidateVotes,
      Map<Integer, PartyResult> partyResults,
      Map<Integer, Double> pctReporting) {
    candidateVotes.put(
        1,
        Map.of(
            new Candidate("Tommy Kickham", lib), 619,
            new Candidate("Colin Lavie", pc), 982,
            new Candidate("Boyd Leard", grn), 577));
    partyResults.put(1, PartyResult.elected(pc));
    pctReporting.put(1, 5.0 / 10);

    candidateVotes.put(
        2,
        Map.of(
            new Candidate("Kevin Doyle", lib), 438,
            new Candidate("Susan Hartley", grn), 571,
            new Candidate("Steven Myers", pc), 1164,
            new Candidate("Edith Perry", ndp), 34));
    partyResults.put(2, PartyResult.elected(pc));
    pctReporting.put(2, 5.0 / 10);

    candidateVotes.put(
        3,
        Map.of(
            new Candidate("Billy Cann", ndp), 93,
            new Candidate("Cory Deagle", pc), 1115,
            new Candidate("Daphne Griffin", lib), 606,
            new Candidate("John Allen Maclean", grn), 468));
    partyResults.put(3, PartyResult.elected(pc));
    pctReporting.put(3, 5.0 / 9);

    candidateVotes.put(
        4,
        Map.of(
            new Candidate("Darlene Compton", pc), 1028,
            new Candidate("Ian MacPherson", lib), 415,
            new Candidate("James Sanders", grn), 498));
    partyResults.put(4, PartyResult.elected(pc));
    pctReporting.put(4, 5.0 / 10);

    candidateVotes.put(
        5,
        Map.of(
            new Candidate("Michele Beaton", grn), 871,
            new Candidate("Randy Cooper", lib), 742,
            new Candidate("Mary Ellen McInnis", pc), 743,
            new Candidate("Lawrence Millar", ndp), 31));
    partyResults.put(5, PartyResult.leading(grn));
    pctReporting.put(5, 5.0 / 8);

    candidateVotes.put(
        6,
        Map.of(
            new Candidate("James Aylward", pc), 995,
            new Candidate("David Dunphy", lib), 684,
            new Candidate("Devon Strang", grn), 578,
            new Candidate("Lynne Thiele", ndp), 25));
    partyResults.put(6, PartyResult.leading(pc));
    pctReporting.put(6, 5.0 / 9);

    candidateVotes.put(
        7,
        Map.of(
            new Candidate("Margaret Andrade", ndp), 22,
            new Candidate("Kyle MacDonald", grn), 369,
            new Candidate("Sidney MacEwan", pc), 1190,
            new Candidate("Susan Myers", lib), 359));
    partyResults.put(7, PartyResult.elected(pc));
    pctReporting.put(7, 5.0 / 11);

    candidateVotes.put(
        8,
        Map.of(
            new Candidate("Sarah Donald", grn), 490,
            new Candidate("Wade MacLauchlan", lib), 832,
            new Candidate("Bloyce Thompson", pc), 948,
            new Candidate("Marian White", ndp), 34));
    partyResults.put(8, PartyResult.leading(pc));
    pctReporting.put(8, 5.0 / 10);

    candidateVotes.put(
        9,
        Map.of(
            new Candidate("John Andrew", grn), 533,
            new Candidate("Gordon Gay", ndp), 38,
            new Candidate("Natalie Jameson", pc), 807,
            new Candidate("Karen Lavers", lib), 492));
    partyResults.put(9, PartyResult.leading(pc));
    pctReporting.put(9, 5.0 / 11);

    candidateVotes.put(
        10,
        Map.of(
            new Candidate("Mike Gillis", pc), 614,
            new Candidate("Robert Mitchell", lib), 1098,
            new Candidate("Amanda Morrison", grn), 759,
            new Candidate("Jesse Reddin Cousins", ndp), 32));
    partyResults.put(10, PartyResult.elected(lib));
    pctReporting.put(10, 5.0 / 10);

    candidateVotes.put(
        11,
        Map.of(
            new Candidate("Hannah Bell", grn), 922,
            new Candidate("Ronnie Carragher", pc), 769,
            new Candidate("Roxanne Carter-Thompson", lib), 678,
            new Candidate("Trevor Leclerc", ndp), 44));
    partyResults.put(11, PartyResult.elected(grn));
    pctReporting.put(11, 5.0 / 10);

    candidateVotes.put(
        12,
        Map.of(
            new Candidate("Karla Bernard", grn), 831,
            new Candidate("Richard Brown", lib), 639,
            new Candidate("Joe Byrne", ndp), 248,
            new Candidate("Tim Keizer", pc), 479));
    partyResults.put(12, PartyResult.leading(grn));
    pctReporting.put(12, 5.0 / 10);

    candidateVotes.put(
        13,
        Map.of(
            new Candidate("Jordan Brown", lib), 952,
            new Candidate("Ole Hammarlund", grn), 840,
            new Candidate("Donna Hurry", pc), 437,
            new Candidate("Simone Webster", ndp), 92));
    partyResults.put(13, PartyResult.leading(lib));
    pctReporting.put(13, 5.0 / 10);

    candidateVotes.put(
        14,
        Map.of(
            new Candidate("Angus Birt", pc), 624,
            new Candidate("Bush Dumville", ind), 171,
            new Candidate("Gavin Hall", grn), 660,
            new Candidate("Gord MacNeilly", lib), 874,
            new Candidate("Janis Newman", ndp), 38));
    partyResults.put(14, PartyResult.leading(lib));
    pctReporting.put(14, 5.0 / 10);

    candidateVotes.put(
        15,
        Map.of(
            new Candidate("Greg Bradley", grn), 567,
            new Candidate("Leah-Jane Hayward", ndp), 45,
            new Candidate("Dennis King", pc), 909,
            new Candidate("Windsor Wight", lib), 652));
    partyResults.put(15, PartyResult.leading(pc));
    pctReporting.put(15, 5.0 / 10);

    candidateVotes.put(
        16,
        Map.of(
            new Candidate("Elaine Barnes", pc), 431,
            new Candidate("Ellen Jones", grn), 819,
            new Candidate("Heath MacDonald", lib), 1286,
            new Candidate("Craig Nash", ndp), 652));
    partyResults.put(16, PartyResult.leading(lib));
    pctReporting.put(16, 5.0 / 10);

    candidateVotes.put(
        17,
        Map.of(
            new Candidate("Peter Bevan-Baker", grn), 1357,
            new Candidate("Kris Currie", pc), 799,
            new Candidate("Judy MacNevin", lib), 421,
            new Candidate("Don Wills", ind), 12));
    partyResults.put(17, PartyResult.elected(grn));
    pctReporting.put(17, 5.0 / 10);

    candidateVotes.put(
        18,
        Map.of(
            new Candidate("Sean Deagle", ndp), 22,
            new Candidate("Colin Jeffrey", grn), 551,
            new Candidate("Sandy MacKay", lib), 330,
            new Candidate("Brad Trivers", pc), 1224));
    partyResults.put(18, PartyResult.elected(pc));
    pctReporting.put(18, 5.0 / 10);

    candidateVotes.put(
        19,
        Map.of(
            new Candidate("Jamie Fox", pc), 1059,
            new Candidate("Joan Gauvin", ndp), 12,
            new Candidate("Matthew MacFarlane", grn), 684,
            new Candidate("Fred McCardle", ind), 26,
            new Candidate("Jamie Stride", lib), 280));
    partyResults.put(19, PartyResult.elected(pc));
    pctReporting.put(19, 5.0 / 10);

    candidateVotes.put(
        20,
        Map.of(
            new Candidate("Nancy Beth Guptill", lib), 277,
            new Candidate("Carole MacFarlane", ndp), 26,
            new Candidate("Matthew MacKay", pc), 1584,
            new Candidate("Matthew J. MacKay", grn), 550));
    partyResults.put(20, PartyResult.elected(pc));
    pctReporting.put(20, 5.0 / 10);

    candidateVotes.put(
        21,
        Map.of(
            new Candidate("Tyler Desroches", pc), 794,
            new Candidate("Paulette Halupa", ndp), 29,
            new Candidate("Lynne Lund", grn), 899,
            new Candidate("Chris Palmer", lib), 713));
    partyResults.put(21, PartyResult.elected(grn));
    pctReporting.put(21, 5.0 / 10);

    candidateVotes.put(
        22,
        Map.of(
            new Candidate("Steve Howard", grn), 885,
            new Candidate("Tina Mundy", lib), 691,
            new Candidate("Garth Oatway", ndp), 46,
            new Candidate("Paul Walsh", pc), 456));
    partyResults.put(22, PartyResult.elected(grn));
    pctReporting.put(22, 5.0 / 10);

    candidateVotes.put(
        23,
        Map.of(
            new Candidate("Trish Altass", grn), 737,
            new Candidate("Paula Biggar", lib), 549,
            new Candidate("Robin John Robert Ednman", ndp), 49,
            new Candidate("Holton A MacLennan", pc), 647));
    partyResults.put(23, PartyResult.elected(grn));
    pctReporting.put(23, 5.0 / 10);

    candidateVotes.put(
        24,
        Map.of(
            new Candidate("Nick Arsenault", grn), 582,
            new Candidate("Sonny Gallant", lib), 774,
            new Candidate("Grant Gallant", ndp), 27,
            new Candidate("Jaosn Woodbury", pc), 434));
    partyResults.put(24, PartyResult.leading(lib));
    pctReporting.put(24, 5.0 / 8);

    candidateVotes.put(
        25,
        Map.of(
            new Candidate("Barb Broome", pc), 329,
            new Candidate("Jason Charette", grn), 189,
            new Candidate("Dr. Herb Dickieson", ndp), 614,
            new Candidate("Robert Henderson", lib), 820));
    partyResults.put(25, PartyResult.elected(lib));
    pctReporting.put(25, 5.0 / 11);

    candidateVotes.put(
        26,
        Map.of(
            new Candidate("Michelle Arsenault", ndp), 60,
            new Candidate("Ernie Hudson", pc), 890,
            new Candidate("James McKenna", grn), 198,
            new Candidate("Pat Murphy", lib), 919));
    partyResults.put(26, PartyResult.leading(lib));
    pctReporting.put(26, 5.0 / 10);

    candidateVotes.put(
        27,
        Map.of(
            new Candidate("Sean Doyle", grn), 360,
            new Candidate("Melissa Handrahan", pc), 530,
            new Candidate("Hal Perry", lib), 913,
            new Candidate("Dale Ryan", ndp), 20));
    partyResults.put(27, PartyResult.elected(lib));
    pctReporting.put(27, 5.0 / 10);
  }

  private void setupFullResults(
      Map<Integer, Map<Candidate, Integer>> candidateVotes,
      Map<Integer, PartyResult> partyResults,
      Map<Integer, Double> pctReporting) {
    candidateVotes.put(
        1,
        Map.of(
            new Candidate("Tommy Kickham", lib), 861,
            new Candidate("Colin Lavie", pc), 1347,
            new Candidate("Boyd Leard", grn), 804));
    partyResults.put(1, PartyResult.elected(pc));
    pctReporting.put(1, 10.0 / 10);

    candidateVotes.put(
        2,
        Map.of(
            new Candidate("Kevin Doyle", lib), 663,
            new Candidate("Susan Hartley", grn), 865,
            new Candidate("Steven Myers", pc), 1493,
            new Candidate("Edith Perry", ndp), 49));
    partyResults.put(2, PartyResult.elected(pc));
    pctReporting.put(2, 10.0 / 10);

    candidateVotes.put(
        3,
        Map.of(
            new Candidate("Billy Cann", ndp), 124,
            new Candidate("Cory Deagle", pc), 1373,
            new Candidate("Daphne Griffin", lib), 785,
            new Candidate("John Allen Maclean", grn), 675));
    partyResults.put(3, PartyResult.elected(pc));
    pctReporting.put(3, 9.0 / 9);

    candidateVotes.put(
        4,
        Map.of(
            new Candidate("Darlene Compton", pc), 1545,
            new Candidate("Ian MacPherson", lib), 615,
            new Candidate("James Sanders", grn), 781));
    partyResults.put(4, PartyResult.elected(pc));
    pctReporting.put(4, 10.0 / 10);

    candidateVotes.put(
        5,
        Map.of(
            new Candidate("Michele Beaton", grn), 1152,
            new Candidate("Randy Cooper", lib), 902,
            new Candidate("Mary Ellen McInnis", pc), 943,
            new Candidate("Lawrence Millar", ndp), 38));
    partyResults.put(5, PartyResult.elected(grn));
    pctReporting.put(5, 8.0 / 8);

    candidateVotes.put(
        6,
        Map.of(
            new Candidate("James Aylward", pc), 1270,
            new Candidate("David Dunphy", lib), 882,
            new Candidate("Devon Strang", grn), 805,
            new Candidate("Lynne Thiele", ndp), 31));
    partyResults.put(6, PartyResult.elected(pc));
    pctReporting.put(6, 9.0 / 9);

    candidateVotes.put(
        7,
        Map.of(
            new Candidate("Margaret Andrade", ndp), 35,
            new Candidate("Kyle MacDonald", grn), 697,
            new Candidate("Sidney MacEwan", pc), 1752,
            new Candidate("Susan Myers", lib), 557));
    partyResults.put(7, PartyResult.elected(pc));
    pctReporting.put(7, 11.0 / 11);

    candidateVotes.put(
        8,
        Map.of(
            new Candidate("Sarah Donald", grn), 747,
            new Candidate("Wade MacLauchlan", lib), 1196,
            new Candidate("Bloyce Thompson", pc), 1300,
            new Candidate("Marian White", ndp), 46));
    partyResults.put(8, PartyResult.elected(pc));
    pctReporting.put(8, 10.0 / 10);

    candidateVotes.put(
        9,
        Map.of(
            new Candidate("John Andrew", grn), 709,
            new Candidate("Gordon Gay", ndp), 46,
            new Candidate("Natalie Jameson", pc), 1080,
            new Candidate("Karen Lavers", lib), 635));
    partyResults.put(9, PartyResult.elected(pc));
    pctReporting.put(9, 11.0 / 11);

    candidateVotes.put(
        10,
        Map.of(
            new Candidate("Mike Gillis", pc), 865,
            new Candidate("Robert Mitchell", lib), 1420,
            new Candidate("Amanda Morrison", grn), 1058,
            new Candidate("Jesse Reddin Cousins", ndp), 41));
    partyResults.put(10, PartyResult.elected(lib));
    pctReporting.put(10, 10.0 / 10);

    candidateVotes.put(
        11,
        Map.of(
            new Candidate("Hannah Bell", grn), 1286,
            new Candidate("Ronnie Carragher", pc), 998,
            new Candidate("Roxanne Carter-Thompson", lib), 846,
            new Candidate("Trevor Leclerc", ndp), 55));
    partyResults.put(11, PartyResult.elected(grn));
    pctReporting.put(11, 10.0 / 10);

    candidateVotes.put(
        12,
        Map.of(
            new Candidate("Karla Bernard", grn), 1272,
            new Candidate("Richard Brown", lib), 875,
            new Candidate("Joe Byrne", ndp), 338,
            new Candidate("Tim Keizer", pc), 656));
    partyResults.put(12, PartyResult.elected(grn));
    pctReporting.put(12, 10.0 / 10);

    candidateVotes.put(
        13,
        Map.of(
            new Candidate("Jordan Brown", lib), 1223,
            new Candidate("Ole Hammarlund", grn), 1301,
            new Candidate("Donna Hurry", pc), 567,
            new Candidate("Simone Webster", ndp), 138));
    partyResults.put(13, PartyResult.elected(grn));
    pctReporting.put(13, 10.0 / 10);

    candidateVotes.put(
        14,
        Map.of(
            new Candidate("Angus Birt", pc), 766,
            new Candidate("Bush Dumville", ind), 202,
            new Candidate("Gavin Hall", grn), 966,
            new Candidate("Gord MacNeilly", lib), 1079,
            new Candidate("Janis Newman", ndp), 56));
    partyResults.put(14, PartyResult.elected(lib));
    pctReporting.put(14, 10.0 / 10);

    candidateVotes.put(
        15,
        Map.of(
            new Candidate("Greg Bradley", grn), 879,
            new Candidate("Leah-Jane Hayward", ndp), 57,
            new Candidate("Dennis King", pc), 1315,
            new Candidate("Windsor Wight", lib), 899));
    partyResults.put(15, PartyResult.elected(pc));
    pctReporting.put(15, 10.0 / 10);

    candidateVotes.put(
        16,
        Map.of(
            new Candidate("Elaine Barnes", pc), 602,
            new Candidate("Ellen Jones", grn), 1137,
            new Candidate("Heath MacDonald", lib), 1643,
            new Candidate("Craig Nash", ndp), 899));
    partyResults.put(16, PartyResult.elected(lib));
    pctReporting.put(16, 10.0 / 10);

    candidateVotes.put(
        17,
        Map.of(
            new Candidate("Peter Bevan-Baker", grn), 1870,
            new Candidate("Kris Currie", pc), 1068,
            new Candidate("Judy MacNevin", lib), 515,
            new Candidate("Don Wills", ind), 26));
    partyResults.put(17, PartyResult.elected(grn));
    pctReporting.put(17, 10.0 / 10);

    candidateVotes.put(
        18,
        Map.of(
            new Candidate("Sean Deagle", ndp), 30,
            new Candidate("Colin Jeffrey", grn), 899,
            new Candidate("Sandy MacKay", lib), 489,
            new Candidate("Brad Trivers", pc), 1920));
    partyResults.put(18, PartyResult.elected(pc));
    pctReporting.put(18, 10.0 / 10);

    candidateVotes.put(
        19,
        Map.of(
            new Candidate("Jamie Fox", pc), 1680,
            new Candidate("Joan Gauvin", ndp), 32,
            new Candidate("Matthew MacFarlane", grn), 1041,
            new Candidate("Fred McCardle", ind), 54,
            new Candidate("Jamie Stride", lib), 417));
    partyResults.put(19, PartyResult.elected(pc));
    pctReporting.put(19, 10.0 / 10);

    candidateVotes.put(
        20,
        Map.of(
            new Candidate("Nancy Beth Guptill", lib), 389,
            new Candidate("Carole MacFarlane", ndp), 31,
            new Candidate("Matthew MacKay", pc), 2008,
            new Candidate("Matthew J. MacKay", grn), 805));
    partyResults.put(20, PartyResult.elected(pc));
    pctReporting.put(20, 10.0 / 10);

    candidateVotes.put(
        21,
        Map.of(
            new Candidate("Tyler Desroches", pc), 1037,
            new Candidate("Paulette Halupa", ndp), 39,
            new Candidate("Lynne Lund", grn), 1258,
            new Candidate("Chris Palmer", lib), 892));
    partyResults.put(21, PartyResult.elected(grn));
    pctReporting.put(21, 10.0 / 10);

    candidateVotes.put(
        22,
        Map.of(
            new Candidate("Steve Howard", grn), 1302,
            new Candidate("Tina Mundy", lib), 938,
            new Candidate("Garth Oatway", ndp), 65,
            new Candidate("Paul Walsh", pc), 662));
    partyResults.put(22, PartyResult.elected(grn));
    pctReporting.put(22, 10.0 / 10);

    candidateVotes.put(
        23,
        Map.of(
            new Candidate("Trish Altass", grn), 1101,
            new Candidate("Paula Biggar", lib), 882,
            new Candidate("Robin John Robert Ednman", ndp), 81,
            new Candidate("Holton A MacLennan", pc), 1026));
    partyResults.put(23, PartyResult.elected(grn));
    pctReporting.put(23, 10.0 / 10);

    candidateVotes.put(
        24,
        Map.of(
            new Candidate("Nick Arsenault", grn), 761,
            new Candidate("Sonny Gallant", lib), 1100,
            new Candidate("Grant Gallant", ndp), 33,
            new Candidate("Jaosn Woodbury", pc), 575));
    partyResults.put(24, PartyResult.elected(lib));
    pctReporting.put(24, 8.0 / 8);

    candidateVotes.put(
        25,
        Map.of(
            new Candidate("Barb Broome", pc), 462,
            new Candidate("Jason Charette", grn), 231,
            new Candidate("Dr. Herb Dickieson", ndp), 898,
            new Candidate("Robert Henderson", lib), 1102));
    partyResults.put(25, PartyResult.elected(lib));
    pctReporting.put(25, 11.0 / 11);

    candidateVotes.put(
        26,
        Map.of(
            new Candidate("Michelle Arsenault", ndp), 99,
            new Candidate("Ernie Hudson", pc), 1312,
            new Candidate("James McKenna", grn), 317,
            new Candidate("Pat Murphy", lib), 1153));
    partyResults.put(26, PartyResult.elected(lib));
    pctReporting.put(26, 10.0 / 10);

    candidateVotes.put(
        27,
        Map.of(
            new Candidate("Sean Doyle", grn), 584,
            new Candidate("Melissa Handrahan", pc), 802,
            new Candidate("Hal Perry", lib), 1388,
            new Candidate("Dale Ryan", ndp), 44));
    partyResults.put(27, PartyResult.elected(lib));
    pctReporting.put(27, 10.0 / 10);
  }
}
