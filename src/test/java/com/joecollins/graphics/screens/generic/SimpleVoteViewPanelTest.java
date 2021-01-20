package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.graphics.components.MapFrameTest;
import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.graphics.utils.ShapefileReader;
import com.joecollins.models.general.Aggregators;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import com.joecollins.models.general.PartyResult;
import java.awt.Color;
import java.awt.Shape;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Range;
import org.junit.Test;

public class SimpleVoteViewPanelTest {

  @Test
  public void testCandidatesBasicResult() throws IOException {
    Candidate ndp =
        new Candidate("Billy Cann", new Party("New Democratic Party", "NDP", Color.ORANGE));
    Candidate pc =
        new Candidate("Cory Deagle", new Party("Progressive Conservative", "PC", Color.BLUE));
    Candidate lib = new Candidate("Daphne Griffin", new Party("Liberal", "LIB", Color.RED));
    Candidate grn =
        new Candidate("John Allen MacLean", new Party("Green", "GRN", Color.GREEN.darker()));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(ndp, 124);
    curr.put(pc, 1373);
    curr.put(lib, 785);
    curr.put(grn, 674);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(ndp.getParty(), 585);
    prev.put(pc.getParty(), 785);
    prev.put(lib.getParty(), 1060);
    prev.put(grn.getParty(), 106);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<String> header = new BindableWrapper<>("MONTAGUE-KILMUIR");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("9 OF 9 POLLS REPORTING");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("PROJECTION: PC GAIN FROM LIB");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2015");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("CARDIGAN");
    BindableWrapper<Party> leader = new BindableWrapper<>(pc.getParty());
    List<Party> swingPartyOrder =
        Arrays.asList(ndp.getParty(), grn.getParty(), lib.getParty(), pc.getParty());
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream().filter(id -> id <= 7).collect(Collectors.toList()));
    BindableWrapper<Integer> selectedDistrict = new BindableWrapper<>(3);

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .withPartyMap(
                () -> shapesByDistrict,
                selectedDistrict.getBinding(),
                leader.getBinding(),
                focus.getBinding(),
                mapHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "Basic-1", panel);
  }

  @Test
  public void testCandidatesBasicResultShrinkToFit() throws IOException {
    Candidate ndp =
        new Candidate("Steven Scott", new Party("New Democratic Party", "NDP", Color.ORANGE));
    Candidate con = new Candidate("Claudio Rocchi", new Party("Conservative", "CON", Color.BLUE));
    Candidate lib = new Candidate("David Lametti", new Party("Liberal", "LIB", Color.RED), true);
    Candidate grn = new Candidate("Jency Mercier", new Party("Green", "GRN", Color.GREEN.darker()));
    Candidate bq =
        new Candidate(
            "Isabel Dion", new Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker()));
    Candidate ppc =
        new Candidate("Daniel Turgeon", new Party("People's Party", "PPC", Color.MAGENTA.darker()));
    Candidate ml = new Candidate("Eileen Studd", new Party("Marxist-Leninist", "M-L", Color.RED));
    Candidate rhino =
        new Candidate("Rhino Jacques B\u00e9langer", new Party("Rhinoceros", "RHINO", Color.GRAY));
    Candidate ind =
        new Candidate("Julien C\u00f4t\u00e9", new Party("Independent", "IND", Color.GRAY));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(ndp, 8628);
    curr.put(con, 3690);
    curr.put(lib, 22803);
    curr.put(grn, 3583);
    curr.put(bq, 12619);
    curr.put(ppc, 490);
    curr.put(ml, 39);
    curr.put(rhino, 265);
    curr.put(ind, 274);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(ndp.getParty(), 15566);
    prev.put(con.getParty(), 3713);
    prev.put(lib.getParty(), 23603);
    prev.put(grn.getParty(), 1717);
    prev.put(bq.getParty(), 9164);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<String> header = new BindableWrapper<>("LASALLE\u2014\u00c9MARD\u2014VERDUN");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("100% OF POLLS REPORTING");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("PROJECTION: LIB HOLD");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2015");
    List<Party> swingPartyOrder =
        Arrays.asList(
            ndp.getParty(), grn.getParty(), lib.getParty(), bq.getParty(), con.getParty());

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(),
                voteHeader.getBinding(),
                voteSubhead.getBinding(),
                "(MP)")
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "Basic-2", panel);
  }

  @Test
  public void testCandidatesBasicResultPctOnly() throws IOException {
    Candidate ndp =
        new Candidate("Billy Cann", new Party("New Democratic Party", "NDP", Color.ORANGE));
    Candidate pc =
        new Candidate("Cory Deagle", new Party("Progressive Conservative", "PC", Color.BLUE));
    Candidate lib = new Candidate("Daphne Griffin", new Party("Liberal", "LIB", Color.RED));
    Candidate grn =
        new Candidate("John Allen MacLean", new Party("Green", "GRN", Color.GREEN.darker()));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(ndp, 124);
    curr.put(pc, 1373);
    curr.put(lib, 785);
    curr.put(grn, 674);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(ndp.getParty(), 585);
    prev.put(pc.getParty(), 785);
    prev.put(lib.getParty(), 1060);
    prev.put(grn.getParty(), 106);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<String> header = new BindableWrapper<>("MONTAGUE-KILMUIR");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("9 OF 9 POLLS REPORTING");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("PROJECTION: PC GAIN FROM LIB");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2015");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("CARDIGAN");
    BindableWrapper<Party> leader = new BindableWrapper<>(pc.getParty());
    List<Party> swingPartyOrder =
        Arrays.asList(ndp.getParty(), grn.getParty(), lib.getParty(), pc.getParty());
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream().filter(id -> id <= 7).collect(Collectors.toList()));
    BindableWrapper<Integer> selectedDistrict = new BindableWrapper<>(3);

    BasicResultPanel panel =
        BasicResultPanel.candidateVotesPctOnly(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .withPartyMap(
                () -> shapesByDistrict,
                selectedDistrict.getBinding(),
                leader.getBinding(),
                focus.getBinding(),
                mapHeader.getBinding())
            .withNotes(() -> "SOURCE: Elections PEI")
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "PctOnly-1", panel);
  }

  @Test
  public void testCandidateScreenUpdating() throws IOException {
    Candidate ndp =
        new Candidate("Billy Cann", new Party("New Democratic Party", "NDP", Color.ORANGE));
    Candidate pc =
        new Candidate("Cory Deagle", new Party("Progressive Conservative", "PC", Color.BLUE));
    Candidate lib = new Candidate("Daphne Griffin", new Party("Liberal", "LIB", Color.RED));
    Candidate grn =
        new Candidate("John Allen MacLean", new Party("Green", "GRN", Color.GREEN.darker()));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(ndp, 0);
    curr.put(pc, 0);
    curr.put(lib, 0);
    curr.put(grn, 0);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(ndp.getParty(), 585);
    prev.put(pc.getParty(), 785);
    prev.put(lib.getParty(), 1060);
    prev.put(grn.getParty(), 106);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<Double> pctReporting = new BindableWrapper<>(0.0);
    BindableWrapper<String> header = new BindableWrapper<>("MONTAGUE-KILMUIR");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("0 OF 9 POLLS REPORTING");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("WAITING FOR RESULTS...");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2015");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("CARDIGAN");
    BindableWrapper<PartyResult> leader = new BindableWrapper<>();
    BindableWrapper<Candidate> winner = new BindableWrapper<>();
    List<Party> swingPartyOrder =
        Arrays.asList(ndp.getParty(), grn.getParty(), lib.getParty(), pc.getParty());
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream().filter(id -> id <= 7).collect(Collectors.toList()));
    BindableWrapper<Integer> selectedDistrict = new BindableWrapper<>(3);

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .withResultMap(
                () -> shapesByDistrict,
                selectedDistrict.getBinding(),
                leader.getBinding(),
                focus.getBinding(),
                mapHeader.getBinding())
            .withWinner(winner.getBinding())
            .withPctReporting(pctReporting.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "Update-1", panel);

    curr.put(ndp, 5);
    curr.put(pc, 47);
    curr.put(lib, 58);
    curr.put(grn, 52);
    currentVotes.setValue(curr);
    voteHeader.setValue("1 OF 9 POLLS REPORTING");
    voteSubhead.setValue("PROJECTION: TOO EARLY TO CALL");
    pctReporting.setValue(1.0 / 9);
    leader.setValue(PartyResult.leading(lib.getParty()));
    compareRendering("SimpleVoteViewPanel", "Update-2", panel);

    curr.put(ndp, 8);
    curr.put(pc, 91);
    curr.put(lib, 100);
    curr.put(grn, 106);
    currentVotes.setValue(curr);
    voteHeader.setValue("2 OF 9 POLLS REPORTING");
    voteSubhead.setValue("PROJECTION: TOO EARLY TO CALL");
    pctReporting.setValue(2.0 / 9);
    leader.setValue(PartyResult.leading(grn.getParty()));
    compareRendering("SimpleVoteViewPanel", "Update-3", panel);

    curr.put(ndp, 18);
    curr.put(pc, 287);
    curr.put(lib, 197);
    curr.put(grn, 243);
    currentVotes.setValue(curr);
    voteHeader.setValue("5 OF 9 POLLS REPORTING");
    voteSubhead.setValue("PROJECTION: TOO EARLY TO CALL");
    pctReporting.setValue(5.0 / 9);
    leader.setValue(PartyResult.leading(pc.getParty()));
    compareRendering("SimpleVoteViewPanel", "Update-4", panel);

    curr.put(ndp, 124);
    curr.put(pc, 1373);
    curr.put(lib, 785);
    curr.put(grn, 675);
    currentVotes.setValue(curr);
    voteHeader.setValue("9 OF 9 POLLS REPORTING");
    voteSubhead.setValue("PROJECTION: PC GAIN FROM LIB");
    pctReporting.setValue(9.0 / 9);
    leader.setValue(PartyResult.elected(pc.getParty()));
    winner.setValue(pc);
    compareRendering("SimpleVoteViewPanel", "Update-5", panel);

    winner.setValue(null);
    compareRendering("SimpleVoteViewPanel", "Update-6", panel);
  }

  @Test
  public void testZeroVotesSingleCandidate() throws IOException {
    Candidate ndp =
        new Candidate("Billy Cann", new Party("New Democratic Party", "NDP", Color.ORANGE));
    Candidate pc =
        new Candidate("Cory Deagle", new Party("Progressive Conservative", "PC", Color.BLUE));
    Candidate lib = new Candidate("Daphne Griffin", new Party("Liberal", "LIB", Color.RED));
    Candidate grn =
        new Candidate("John Allen MacLean", new Party("Green", "GRN", Color.GREEN.darker()));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(ndp, 6);
    curr.put(pc, 8);
    curr.put(lib, 11);
    curr.put(grn, 0);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(ndp.getParty(), 585);
    prev.put(pc.getParty(), 785);
    prev.put(lib.getParty(), 1060);
    prev.put(grn.getParty(), 106);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<Double> pctReporting = new BindableWrapper<>(1.0 / 9);
    BindableWrapper<String> header = new BindableWrapper<>("MONTAGUE-KILMUIR");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("1 OF 9 POLLS REPORTING");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("WAITING FOR RESULTS...");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2015");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("CARDIGAN");
    BindableWrapper<PartyResult> leader =
        new BindableWrapper<>(PartyResult.leading(lib.getParty()));
    BindableWrapper<Candidate> winner = new BindableWrapper<>();
    List<Party> swingPartyOrder =
        Arrays.asList(ndp.getParty(), grn.getParty(), lib.getParty(), pc.getParty());
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream().filter(id -> id <= 7).collect(Collectors.toList()));
    BindableWrapper<Integer> selectedDistrict = new BindableWrapper<>(3);

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .withResultMap(
                () -> shapesByDistrict,
                selectedDistrict.getBinding(),
                leader.getBinding(),
                focus.getBinding(),
                mapHeader.getBinding())
            .withWinner(winner.getBinding())
            .withPctReporting(pctReporting.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "ZeroVotes-1", panel);
  }

  @Test
  public void testPartyVoteScreen() throws IOException {
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());

    LinkedHashMap<Party, Integer> curr = new LinkedHashMap<>();
    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();

    BindableWrapper<LinkedHashMap<Party, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<Double> pctReporting = new BindableWrapper<>(0.0);
    BindableWrapper<String> header = new BindableWrapper<>("PRINCE EDWARD ISLAND");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("0 OF 27 DISTRICTS DECLARED");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("WAITING FOR RESULTS...");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2015");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("PEI");
    List<Party> swingPartyOrder = Arrays.asList(ndp, grn, lib, pc);
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus = new BindableWrapper<>();
    BindableWrapper<Map<Integer, Party>> winnersByDistrict = new BindableWrapper<>(new HashMap<>());

    BasicResultPanel panel =
        BasicResultPanel.partyVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .withPctReporting(pctReporting.getBinding())
            .withPartyMap(
                () -> shapesByDistrict,
                winnersByDistrict.getBinding(),
                focus.getBinding(),
                mapHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "PopularVote-1", panel);

    LinkedHashMap<Integer, Party> winners = new LinkedHashMap<>();

    curr.put(ndp, 124);
    curr.put(pc, 1373);
    curr.put(lib, 785);
    curr.put(grn, 675);
    currentVotes.setValue(curr);

    prev.put(ndp, 585);
    prev.put(pc, 785);
    prev.put(lib, 1060);
    prev.put(grn, 106);
    previousVotes.setValue(prev);

    voteHeader.setValue("1 OF 27 DISTRICTS DECLARED");
    voteSubhead.setValue("PROJECTION: TOO EARLY TO CALL");
    pctReporting.setValue(1.0 / 27);
    winners.put(3, pc);
    winnersByDistrict.setValue(winners);
    compareRendering("SimpleVoteViewPanel", "PopularVote-2", panel);

    focus.setValue(
        shapesByDistrict.keySet().stream().filter(id -> id <= 7).collect(Collectors.toList()));
    mapHeader.setValue("CARDIGAN");
    header.setValue("CARDIGAN");
    pctReporting.setValue(1.0 / 7);
    voteHeader.setValue("1 OF 7 DISTRICTS DECLARED");
    compareRendering("SimpleVoteViewPanel", "PopularVote-3", panel);
  }

  @Test
  public void testPartyVoteTickScreen() throws IOException {
    Party dem = new Party("DEMOCRAT", "DEM", Color.BLUE);
    Party gop = new Party("REPUBLICAN", "GOP", Color.RED);

    LinkedHashMap<Party, Integer> curr = new LinkedHashMap<>();
    curr.put(dem, 60572245);
    curr.put(gop, 50861970);
    curr.put(Party.OTHERS, 1978774);
    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(dem, 61776554);
    prev.put(gop, 63173815);
    prev.put(Party.OTHERS, 3676641);

    BindableWrapper<LinkedHashMap<Party, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<Double> pctReporting = new BindableWrapper<>(1.0);
    BindableWrapper<String> header = new BindableWrapper<>("UNITED STATES");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("HOUSE OF REPRESENTATIVES");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2016");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2016");
    BindableWrapper<Party> winner = new BindableWrapper<>(dem);
    List<Party> swingPartyOrder = List.of(dem, gop);

    BasicResultPanel panel =
        BasicResultPanel.partyVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withWinner(winner.getBinding())
            .withPctReporting(pctReporting.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "PartyTick-1", panel);
  }

  @Test
  public void testMultipleCandidatesSameParty() throws IOException {
    Party dem = new Party("DEMOCRAT", "DEM", Color.BLUE);
    Party gop = new Party("REPUBLICAN", "GOP", Color.RED);
    Party lbt = new Party("LIBERTARIAN", "LBT", Color.ORANGE);
    Party ind = new Party("INDEPENDENT", "IND", Color.GRAY);

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(new Candidate("Raul Barrera", dem), 1747);
    curr.put(new Candidate("Bech Bruun", gop), 1570);
    curr.put(new Candidate("Michael Cloud", gop), 19856);
    curr.put(new Candidate("Judith Cutright", ind), 172);
    curr.put(new Candidate("Eric Holguin", dem), 11595);
    curr.put(new Candidate("Marty Perez", gop), 276);
    curr.put(new Candidate("Christopher Suprun", ind), 51);
    curr.put(new Candidate("Daniel Tinus", lbt), 144);
    curr.put(new Candidate("Mike Westergren", dem), 858);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(dem, 88329);
    prev.put(gop, 142251);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<String> header = new BindableWrapper<>("TEXAS DISTRICT 27");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("OFFICIAL RESULT");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("GOP HOLD");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2016");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2016");
    BindableWrapper<Candidate> winner =
        new BindableWrapper<>(
            curr.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey());
    List<Party> swingPartyOrder = Arrays.asList(dem, gop);

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .withWinner(winner.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "SameParty-1", panel);
  }

  @Test
  public void testCandidateMajorityLine() throws IOException {
    Party lrem = new Party("LA R\u00c9PUBLIQUE EN MARCHE", "LREM", Color.ORANGE);
    Party fn = new Party("FRONT NATIONAL", "FN", Color.BLUE);

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(new Candidate("Emmanuel Macron", lrem), 20743128);
    curr.put(new Candidate("Marine Le Pen", fn), 10638475);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<String> header = new BindableWrapper<>("FRANCE PRESIDENT: ROUND 2");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("OFFICIAL RESULT");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("MACRON WIN");
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<Double> pctReporting = new BindableWrapper<>(1.0);
    BindableWrapper<Candidate> winner =
        new BindableWrapper<>(
            curr.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey());

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withWinner(winner.getBinding())
            .withMajorityLine(showMajority.getBinding(), () -> "50% TO WIN")
            .withPctReporting(pctReporting.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "MajorityLine-1", panel);

    voteHeader.setValue("15.4% REPORTING");
    voteSubhead.setValue("PROJECTION: MACRON WIN");
    pctReporting.setValue(0.154);
    winner.setValue(null);
    curr.put(new Candidate("Emmanuel Macron", lrem), 3825279);
    curr.put(new Candidate("Marine Le Pen", fn), 1033686);
    currentVotes.setValue(curr);
    compareRendering("SimpleVoteViewPanel", "MajorityLine-2", panel);

    pctReporting.setValue(0.0);
    curr.put(new Candidate("Emmanuel Macron", lrem), 0);
    curr.put(new Candidate("Marine Le Pen", fn), 0);
    currentVotes.setValue(curr);
    voteHeader.setValue("0.0% REPORTING");
    voteSubhead.setValue("");
    compareRendering("SimpleVoteViewPanel", "MajorityLine-3", panel);
  }

  @Test
  public void testAdditionalHighlightMap() throws IOException {
    Candidate ndp =
        new Candidate("Billy Cann", new Party("New Democratic Party", "NDP", Color.ORANGE));
    Candidate pc =
        new Candidate("Cory Deagle", new Party("Progressive Conservative", "PC", Color.BLUE));
    Candidate lib = new Candidate("Daphne Griffin", new Party("Liberal", "LIB", Color.RED));
    Candidate grn =
        new Candidate("John Allen MacLean", new Party("Green", "GRN", Color.GREEN.darker()));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(ndp, 124);
    curr.put(pc, 1373);
    curr.put(lib, 785);
    curr.put(grn, 674);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(ndp.getParty(), 585);
    prev.put(pc.getParty(), 785);
    prev.put(lib.getParty(), 1060);
    prev.put(grn.getParty(), 106);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<String> header = new BindableWrapper<>("MONTAGUE-KILMUIR");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("9 OF 9 POLLS REPORTING");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("PROJECTION: PC GAIN FROM LIB");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");
    BindableWrapper<String> changeSubhead = new BindableWrapper<>("ADJUSTED FOR BOUNDARY CHANGES");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2015");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("CARDIGAN");
    BindableWrapper<Party> leader = new BindableWrapper<>(pc.getParty());
    List<Party> swingPartyOrder =
        Arrays.asList(ndp.getParty(), grn.getParty(), lib.getParty(), pc.getParty());
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream().filter(id -> id <= 7).collect(Collectors.toList()));
    BindableWrapper<List<Integer>> additionalHighlight =
        new BindableWrapper<>(new ArrayList<>(shapesByDistrict.keySet()));
    BindableWrapper<Integer> selectedDistrict = new BindableWrapper<>(3);

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(
                previousVotes.getBinding(), changeHeader.getBinding(), changeSubhead.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .withResultMap(
                () -> shapesByDistrict,
                selectedDistrict.getBinding(),
                leader.getBinding().map(PartyResult::elected),
                focus.getBinding(),
                additionalHighlight.getBinding(),
                mapHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "AdditionalHighlightMap-1", panel);
  }

  @Test
  public void testCandidateUncontested() throws IOException {
    Candidate dem = new Candidate("Joe Kennedy III", new Party("Democratic", "DEM", Color.BLUE));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(dem, 0);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(dem.getParty(), 265823);
    prev.put(new Party("Republican", "GOP", Color.RED), 113055);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<String> header = new BindableWrapper<>("MASSACHUSETTS DISTRICT 4");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("0.0% OF POLLS REPORTING");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2016");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2016");
    List<Party> swingPartyOrder = new ArrayList<>(prev.keySet());

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "Uncontested-1", panel);
  }

  @Test
  public void testCandidatesSwitchingBetweenSingleAndDoubleLines() throws IOException {
    LinkedHashMap<Candidate, Integer> result2017 = new LinkedHashMap<>();
    result2017.put(
        new Candidate("Boris Johnson", new Party("Conservative", "CON", Color.BLUE)), 23716);
    result2017.put(new Candidate("Vincent Lo", new Party("Labour", "LAB", Color.RED)), 18682);
    result2017.put(
        new Candidate("Rosina Robson", new Party("Liberal Democrats", "LD", Color.ORANGE)), 1835);
    result2017.put(
        new Candidate(
            "Lizzy Kemp", new Party("UK Independence Party", "UKIP", Color.MAGENTA.darker())),
        1577);
    result2017.put(
        new Candidate("Mark Keir", new Party("Green", "GRN", Color.GREEN.darker())), 884);

    LinkedHashMap<Candidate, Integer> result2019 = new LinkedHashMap<>();
    result2019.put(new Candidate("Count Binface", new Party("Independent", "IND", Color.GRAY)), 69);
    result2019.put(
        new Candidate(
            "Lord Buckethead", new Party("Monster Raving Loony Party", "MRLP", Color.YELLOW)),
        125);
    result2019.put(new Candidate("Norma Burke", new Party("Independent", "IND", Color.GRAY)), 22);
    result2019.put(
        new Candidate(
            "Geoffrey Courtenay",
            new Party("UK Independence Party", "UKIP", Color.MAGENTA.darker())),
        283);
    result2019.put(
        new Candidate("Joanne Humphreys", new Party("Liberal Democrats", "LD", Color.ORANGE)),
        3026);
    result2019.put(
        new Candidate("Boris Johnson", new Party("Conservative", "CON", Color.BLUE)), 25351);
    result2019.put(
        new Candidate("Mark Keir", new Party("Green", "GRN", Color.GREEN.darker())), 1090);
    result2019.put(new Candidate("Ali Milani", new Party("Labour", "LAB", Color.RED)), 18141);
    result2019.put(new Candidate("Bobby Smith", new Party("Independent", "IND", Color.GRAY)), 8);
    result2019.put(new Candidate("William Tobin", new Party("Independent", "IND", Color.GRAY)), 5);
    result2019.put(new Candidate("Alfie Utting", new Party("Independent", "IND", Color.GRAY)), 44);
    result2019.put(
        new Candidate(
            "Yace \"Interplanetary Time Lord\" Yogenstein",
            new Party("Independent", "IND", Color.GRAY)),
        23);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes =
        new BindableWrapper<>(result2017);
    BindableWrapper<String> header = new BindableWrapper<>("UXBRIDGE AND SOUTH RUISLIP");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("2017 RESULT");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("");
    BindableWrapper<Candidate> winner =
        new BindableWrapper<>(
            new Candidate("Boris Johnson", new Party("Conservative", "CON", Color.BLUE)));

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withWinner(winner.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "LotsOfCandidates-1", panel);

    currentVotes.setValue(result2019);
    voteHeader.setValue("2019 RESULT");
    compareRendering("SimpleVoteViewPanel", "LotsOfCandidates-2", panel);

    currentVotes.setValue(result2017);
    voteHeader.setValue("2017 RESULT");
    compareRendering("SimpleVoteViewPanel", "LotsOfCandidates-1", panel);
  }

  @Test
  public void testCandidatesLimit() throws IOException {
    Candidate ndp =
        new Candidate("Billy Cann", new Party("New Democratic Party", "NDP", Color.ORANGE));
    Candidate pc =
        new Candidate("Cory Deagle", new Party("Progressive Conservative", "PC", Color.BLUE));
    Candidate lib = new Candidate("Daphne Griffin", new Party("Liberal", "LIB", Color.RED));
    Candidate grn =
        new Candidate("John Allen MacLean", new Party("Green", "GRN", Color.GREEN.darker()));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(ndp, 0);
    curr.put(pc, 0);
    curr.put(lib, 0);
    curr.put(grn, 0);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(ndp.getParty(), 585);
    prev.put(pc.getParty(), 785);
    prev.put(lib.getParty(), 1060);
    prev.put(grn.getParty(), 106);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<Double> pctReporting = new BindableWrapper<>(0.0);
    BindableWrapper<String> header = new BindableWrapper<>("MONTAGUE-KILMUIR");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("0 OF 9 POLLS REPORTING");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("WAITING FOR RESULTS...");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2015");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("CARDIGAN");
    BindableWrapper<PartyResult> leader = new BindableWrapper<>();
    BindableWrapper<Candidate> winner = new BindableWrapper<>();
    List<Party> swingPartyOrder =
        Arrays.asList(ndp.getParty(), grn.getParty(), lib.getParty(), pc.getParty());
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream().filter(id -> id <= 7).collect(Collectors.toList()));
    BindableWrapper<Integer> selectedDistrict = new BindableWrapper<>(3);

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withLimit(3)
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .withResultMap(
                () -> shapesByDistrict,
                selectedDistrict.getBinding(),
                leader.getBinding(),
                focus.getBinding(),
                mapHeader.getBinding())
            .withWinner(winner.getBinding())
            .withPctReporting(pctReporting.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "CandidateOthers-1", panel);

    winner.setValue(lib);
    compareRendering("SimpleVoteViewPanel", "CandidateOthers-2", panel);

    curr.put(ndp, 124);
    curr.put(pc, 1373);
    curr.put(lib, 785);
    curr.put(grn, 675);
    currentVotes.setValue(curr);
    voteHeader.setValue("9 OF 9 POLLS REPORTING");
    voteSubhead.setValue("PROJECTION: PC GAIN FROM LIB");
    pctReporting.setValue(9.0 / 9);
    leader.setValue(PartyResult.elected(pc.getParty()));
    winner.setValue(pc);
    compareRendering("SimpleVoteViewPanel", "CandidateOthers-3", panel);
  }

  @Test
  public void testCandidatesLimitWithMandatoryParties() throws IOException {
    Candidate ndp =
        new Candidate("Billy Cann", new Party("New Democratic Party", "NDP", Color.ORANGE));
    Candidate pc =
        new Candidate("Cory Deagle", new Party("Progressive Conservative", "PC", Color.BLUE));
    Candidate lib = new Candidate("Daphne Griffin", new Party("Liberal", "LIB", Color.RED));
    Candidate grn =
        new Candidate("John Allen MacLean", new Party("Green", "GRN", Color.GREEN.darker()));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(ndp, 0);
    curr.put(pc, 0);
    curr.put(lib, 0);
    curr.put(grn, 0);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(ndp.getParty(), 585);
    prev.put(pc.getParty(), 785);
    prev.put(lib.getParty(), 1060);
    prev.put(grn.getParty(), 106);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<Double> pctReporting = new BindableWrapper<>(0.0);
    BindableWrapper<String> header = new BindableWrapper<>("MONTAGUE-KILMUIR");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("0 OF 9 POLLS REPORTING");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("WAITING FOR RESULTS...");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2015");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("CARDIGAN");
    BindableWrapper<PartyResult> leader = new BindableWrapper<>();
    BindableWrapper<Candidate> winner = new BindableWrapper<>();
    List<Party> swingPartyOrder =
        Arrays.asList(ndp.getParty(), grn.getParty(), lib.getParty(), pc.getParty());
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream().filter(id -> id <= 7).collect(Collectors.toList()));
    BindableWrapper<Integer> selectedDistrict = new BindableWrapper<>(3);

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withLimit(3, pc.getParty(), lib.getParty())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .withResultMap(
                () -> shapesByDistrict,
                selectedDistrict.getBinding(),
                leader.getBinding(),
                focus.getBinding(),
                mapHeader.getBinding())
            .withWinner(winner.getBinding())
            .withPctReporting(pctReporting.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "CandidateOthersMandatory-1", panel);

    winner.setValue(lib);
    compareRendering("SimpleVoteViewPanel", "CandidateOthersMandatory-2", panel);

    curr.put(ndp, 124);
    curr.put(pc, 1373);
    curr.put(lib, 785);
    curr.put(grn, 675);
    currentVotes.setValue(curr);
    voteHeader.setValue("9 OF 9 POLLS REPORTING");
    voteSubhead.setValue("PROJECTION: PC GAIN FROM LIB");
    pctReporting.setValue(9.0 / 9);
    leader.setValue(PartyResult.elected(pc.getParty()));
    winner.setValue(pc);
    compareRendering("SimpleVoteViewPanel", "CandidateOthersMandatory-3", panel);
  }

  @Test
  public void testPartiesNotRunningAgain() throws IOException {
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    Candidate pc =
        new Candidate(
            "Jean-G\u00e9rard Chiasson", new Party("Progressive Conservative", "PC", Color.BLUE));
    Candidate lib = new Candidate("Eric Mallet", new Party("Liberal", "LIB", Color.RED));
    Candidate grn = new Candidate("Marie Leclerc", new Party("Green", "GRN", Color.GREEN.darker()));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(pc, 714);
    curr.put(lib, 6834);
    curr.put(grn, 609);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(ndp, 578);
    prev.put(pc.getParty(), 4048);
    prev.put(lib.getParty(), 3949);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<String> header = new BindableWrapper<>("SHIPPAGAN-LAM\u00c8QUE-MISCOU");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("OFFICIAL RESULT");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2018");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2018");
    BindableWrapper<Candidate> winner = new BindableWrapper<>(lib);
    List<Party> swingPartyOrder = Arrays.asList(ndp, grn.getParty(), lib.getParty(), pc.getParty());

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withWinner(winner.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "PartiesNotRunningAgain-1", panel);
  }

  @Test
  public void testPartiesNotRunningAgainOthers() throws IOException {
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    Candidate pc =
        new Candidate(
            "Jean-G\u00e9rard Chiasson", new Party("Progressive Conservative", "PC", Color.BLUE));
    Candidate lib = new Candidate("Eric Mallet", new Party("Liberal", "LIB", Color.RED));
    Candidate oth = Candidate.OTHERS;

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(pc, 714);
    curr.put(lib, 6834);
    curr.put(oth, 609);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(ndp, 578);
    prev.put(pc.getParty(), 4048);
    prev.put(lib.getParty(), 3949);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<String> header = new BindableWrapper<>("SHIPPAGAN-LAM\u00c8QUE-MISCOU");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("OFFICIAL RESULT");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2018");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2018");
    BindableWrapper<Candidate> winner = new BindableWrapper<>(lib);
    List<Party> swingPartyOrder = Arrays.asList(ndp, oth.getParty(), lib.getParty(), pc.getParty());

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withWinner(winner.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "PartiesNotRunningAgainOthers-1", panel);
  }

  @Test
  public void testWinningPartyNotRunningAgain() throws IOException {
    Candidate con = new Candidate("Greg Smith", new Party("Conservative", "CON", Color.BLUE));
    Candidate ld =
        new Candidate("Stephen Dorrell", new Party("Liberal Democrats", "LD", Color.ORANGE));
    Candidate lab = new Candidate("David Morgan", new Party("Labour", "LAB", Color.RED));
    Candidate bxp =
        new Candidate("Andrew Bell", new Party("Brexit Party", "BXP", Color.CYAN.darker()));
    Candidate ind =
        new Candidate("Ned Thompson", new Party("Independent", "IND", Party.OTHERS.getColor()));
    Candidate ed =
        new Candidate(
            "Antonio Vitiello", new Party("English Democrats", "ED", Color.ORANGE.darker()));

    Party spkr = new Party("Speaker", "SPKR", Color.GRAY);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party ukip = new Party("UK Independence Party", "UKIP", Color.MAGENTA.darker());

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(con, 37035);
    curr.put(ld, 16624);
    curr.put(lab, 7638);
    curr.put(bxp, 1286);
    curr.put(ind, 681);
    curr.put(ed, 194);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(spkr, 34299);
    prev.put(grn, 8574);
    prev.put(ind.getParty(), 5638);
    prev.put(ukip, 4168);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<String> header = new BindableWrapper<>("BUCKINGHAM");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("OFFICIAL RESULT");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2017");
    BindableWrapper<String> changeSubhead =
        new BindableWrapper<>("NOT APPLICABLE: PREVIOUSLY SPEAKER'S SEAT");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2017");
    BindableWrapper<Candidate> winner = new BindableWrapper<>(con);
    List<Party> swingPartyOrder =
        Arrays.asList(lab.getParty(), ld.getParty(), con.getParty(), bxp.getParty());

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(
                previousVotes.getBinding(), changeHeader.getBinding(), changeSubhead.getBinding())
            .withWinner(winner.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "WinningPartyNotRunningAgain-1", panel);
  }

  @Test
  public void testCandidateRunoffSingleLine() throws IOException {
    Candidate macron =
        new Candidate("Emmanuel Macron", new Party("En Marche!", "EM", Color.ORANGE));
    Candidate lePen =
        new Candidate("Marine Le Pen", new Party("National Front", "FN", Color.BLUE.darker()));
    Candidate fillon =
        new Candidate("Fran\u00e7ois Fillon", new Party("The Republicans", "LR", Color.BLUE));
    Candidate melenchon =
        new Candidate(
            "Jean-Luc M\u00e9lenchon",
            new Party("La France Insoumise", "FI", Color.ORANGE.darker()));
    Candidate hamon =
        new Candidate("Beno\u00eet Hamon", new Party("Socialist Party", "PS", Color.RED));
    Candidate dupontAignan =
        new Candidate(
            "Nicolas Dupont-Aignan", new Party("Debout la France", "DLF", Color.CYAN.darker()));
    Candidate lasalle =
        new Candidate("Jean Lasalle", new Party("R\u00e9sistons!", "R\u00c9S", Color.CYAN));
    Candidate poutou =
        new Candidate(
            "Philippe Poutou", new Party("New Anticapitalist Party", "NPA", Color.RED.darker()));
    Candidate asselineau =
        new Candidate(
            "Fran\u00e7ois Asselineau",
            new Party("Popular Republican Union", "UPR", Color.CYAN.darker().darker()));
    Candidate arthaud =
        new Candidate("Nathalie Arthaud", new Party("Lutte Ouvri\u00e8re", "LO", Color.RED));
    Candidate cheminade =
        new Candidate("Jacques Cheminade", new Party("Solidarity and Progress", "S&P", Color.GRAY));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(macron, 8656346);
    curr.put(lePen, 7678491);
    curr.put(fillon, 7212995);
    curr.put(melenchon, 7059951);
    curr.put(hamon, 2291288);
    curr.put(dupontAignan, 1695000);
    curr.put(lasalle, 435301);
    curr.put(poutou, 394505);
    curr.put(asselineau, 332547);
    curr.put(arthaud, 232384);
    curr.put(cheminade, 65586);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<String> header = new BindableWrapper<>("ELECTION 2017: FRANCE DECIDES");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("FIRST ROUND RESULT");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("");
    BindableWrapper<Set<Candidate>> runoff = new BindableWrapper<>();

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withRunoff(runoff.getBinding())
            .withMajorityLine(() -> true, () -> "50% TO WIN")
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "CandidateRunoffSingleLine-1", panel);

    runoff.setValue(Set.of(macron, lePen));
    compareRendering("SimpleVoteViewPanel", "CandidateRunoffSingleLine-2", panel);
  }

  @Test
  public void testCandidateRunoffDualLine() throws IOException {
    Candidate macron =
        new Candidate("Emmanuel Macron", new Party("En Marche!", "EM", Color.ORANGE));
    Candidate lePen =
        new Candidate("Marine Le Pen", new Party("National Front", "FN", Color.BLUE.darker()));
    Candidate fillon =
        new Candidate("Fran\u00e7ois Fillon", new Party("The Republicans", "LR", Color.BLUE));
    Candidate melenchon =
        new Candidate(
            "Jean-Luc M\u00e9lenchon",
            new Party("La France Insoumise", "FI", Color.ORANGE.darker()));
    Candidate hamon =
        new Candidate("Beno\u00eet Hamon", new Party("Socialist Party", "PS", Color.RED));
    Candidate dupontAignan =
        new Candidate(
            "Nicolas Dupont-Aignan", new Party("Debout la France", "DLF", Color.CYAN.darker()));
    Candidate lasalle =
        new Candidate("Jean Lasalle", new Party("R\u00e9sistons!", "R\u00c9S", Color.CYAN));
    Candidate poutou =
        new Candidate(
            "Philippe Poutou", new Party("New Anticapitalist Party", "NPA", Color.RED.darker()));
    Candidate asselineau =
        new Candidate(
            "Fran\u00e7ois Asselineau",
            new Party("Popular Republican Union", "UPR", Color.CYAN.darker().darker()));
    Candidate arthaud =
        new Candidate("Nathalie Arthaud", new Party("Lutte Ouvri\u00e8re", "LO", Color.RED));
    Candidate cheminade =
        new Candidate("Jacques Cheminade", new Party("Solidarity and Progress", "S&P", Color.GRAY));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(macron, 8656346);
    curr.put(lePen, 7678491);
    curr.put(fillon, 7212995);
    curr.put(melenchon, 7059951);
    curr.put(hamon, 2291288);
    curr.put(dupontAignan, 1695000);
    curr.put(lasalle, 435301);
    curr.put(poutou, 394505);
    curr.put(asselineau, 332547);
    curr.put(arthaud, 232384);
    curr.put(cheminade, 65586);

    BindableWrapper<Map<Candidate, Integer>> currentVotes =
        new BindableWrapper<>(Aggregators.topAndOthers(curr, 6, Candidate.OTHERS));
    BindableWrapper<String> header = new BindableWrapper<>("ELECTION 2017: FRANCE DECIDES");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("FIRST ROUND RESULT");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("");
    BindableWrapper<Set<Candidate>> runoff = new BindableWrapper<>();

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withRunoff(runoff.getBinding())
            .withMajorityLine(() -> true, () -> "50% TO WIN")
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "CandidateRunoffDualLine-1", panel);

    runoff.setValue(Set.of(macron, lePen));
    compareRendering("SimpleVoteViewPanel", "CandidateRunoffDualLine-2", panel);
  }

  @Test
  public void testVoteRangeScreen() throws IOException {
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());

    LinkedHashMap<Party, Range<Double>> curr = new LinkedHashMap<>();
    curr.put(ndp, Range.between(0.030, 0.046));
    curr.put(pc, Range.between(0.290, 0.353));
    curr.put(lib, Range.between(0.257, 0.292));
    curr.put(grn, Range.between(0.343, 0.400));

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(ndp, 8997);
    prev.put(pc, 30663);
    prev.put(lib, 33481);
    prev.put(grn, 8857);

    BindableWrapper<LinkedHashMap<Party, Range<Double>>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<String> header = new BindableWrapper<>("PRINCE EDWARD ISLAND");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("OPINION POLL RANGE");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("SINCE ELECTION CALL");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2015");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("PEI");
    List<Party> swingPartyOrder = Arrays.asList(ndp, grn, lib, pc);
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    Map<Integer, Party> winners = new HashMap<>();

    BasicResultPanel panel =
        BasicResultPanel.partyRangeVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .withPartyMap(() -> shapesByDistrict, () -> winners, () -> null, mapHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "Range-1", panel);
  }

  @Test
  public void testCandidatesResultMidDeclaration() throws IOException {
    Candidate ndp =
        new Candidate("Billy Cann", new Party("New Democratic Party", "NDP", Color.ORANGE));
    Candidate pc =
        new Candidate("Cory Deagle", new Party("Progressive Conservative", "PC", Color.BLUE));
    Candidate lib = new Candidate("Daphne Griffin", new Party("Liberal", "LIB", Color.RED));
    Candidate grn =
        new Candidate("John Allen MacLean", new Party("Green", "GRN", Color.GREEN.darker()));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(ndp, null);
    curr.put(pc, null);
    curr.put(lib, null);
    curr.put(grn, null);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(ndp.getParty(), 585);
    prev.put(pc.getParty(), 785);
    prev.put(lib.getParty(), 1060);
    prev.put(grn.getParty(), 106);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<String> header = new BindableWrapper<>("MONTAGUE-KILMUIR");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("OFFICIAL RESULT");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2015");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("CARDIGAN");
    BindableWrapper<Party> leader = new BindableWrapper<>(null);
    List<Party> swingPartyOrder =
        Arrays.asList(ndp.getParty(), grn.getParty(), lib.getParty(), pc.getParty());
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream().filter(id -> id <= 7).collect(Collectors.toList()));
    BindableWrapper<Integer> selectedDistrict = new BindableWrapper<>(3);

    BasicResultPanel panel =
        BasicResultPanel.candidateVotes(
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .withPartyMap(
                () -> shapesByDistrict,
                selectedDistrict.getBinding(),
                leader.getBinding(),
                focus.getBinding(),
                mapHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "MidDeclaration-1", panel);

    curr.put(ndp, 124);
    curr.put(pc, null);
    curr.put(lib, null);
    curr.put(grn, null);
    currentVotes.setValue(curr);
    compareRendering("SimpleVoteViewPanel", "MidDeclaration-2", panel);

    curr.put(ndp, 124);
    curr.put(pc, 1373);
    curr.put(lib, null);
    curr.put(grn, null);
    currentVotes.setValue(curr);
    compareRendering("SimpleVoteViewPanel", "MidDeclaration-3", panel);

    curr.put(ndp, 124);
    curr.put(pc, 1373);
    curr.put(lib, 785);
    curr.put(grn, null);
    currentVotes.setValue(curr);
    compareRendering("SimpleVoteViewPanel", "MidDeclaration-4", panel);

    curr.put(ndp, 124);
    curr.put(pc, 1373);
    curr.put(lib, 785);
    curr.put(grn, 675);
    currentVotes.setValue(curr);
    leader.setValue(pc.getParty());
    compareRendering("SimpleVoteViewPanel", "MidDeclaration-5", panel);
  }

  private Map<Integer, Shape> peiShapesByDistrict() throws IOException {
    URL peiMap =
        MapFrameTest.class
            .getClassLoader()
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp");
    return ShapefileReader.readShapes(peiMap, "DIST_NO", Integer.class);
  }
}
