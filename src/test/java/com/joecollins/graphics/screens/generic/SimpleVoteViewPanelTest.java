package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.graphics.components.MapFrameTest;
import com.joecollins.graphics.screens.generic.BasicResultPanel.Result;
import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.graphics.utils.ShapefileReader;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.awt.Shape;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    BindableWrapper<List<Shape>> focus =
        new BindableWrapper<>(
            shapesByDistrict.entrySet().stream()
                .filter(e -> e.getKey() <= 7)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList()));
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
    Candidate lib = new Candidate("David Lametti", new Party("Liberal", "LIB", Color.RED));
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
                currentVotes.getBinding(), voteHeader.getBinding(), voteSubhead.getBinding())
            .withPrev(previousVotes.getBinding(), changeHeader.getBinding())
            .withSwing(Comparator.comparing(swingPartyOrder::indexOf), swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "Basic-2", panel);
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
    BindableWrapper<BasicResultPanel.Result> leader = new BindableWrapper<>();
    BindableWrapper<Candidate> winner = new BindableWrapper<>();
    List<Party> swingPartyOrder =
        Arrays.asList(ndp.getParty(), grn.getParty(), lib.getParty(), pc.getParty());
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Shape>> focus =
        new BindableWrapper<>(
            shapesByDistrict.entrySet().stream()
                .filter(e -> e.getKey() <= 7)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList()));
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
    leader.setValue(Result.leading(lib.getParty()));
    compareRendering("SimpleVoteViewPanel", "Update-2", panel);

    curr.put(ndp, 8);
    curr.put(pc, 91);
    curr.put(lib, 100);
    curr.put(grn, 106);
    currentVotes.setValue(curr);
    voteHeader.setValue("2 OF 9 POLLS REPORTING");
    voteSubhead.setValue("PROJECTION: TOO EARLY TO CALL");
    pctReporting.setValue(2.0 / 9);
    leader.setValue(Result.leading(grn.getParty()));
    compareRendering("SimpleVoteViewPanel", "Update-3", panel);

    curr.put(ndp, 18);
    curr.put(pc, 287);
    curr.put(lib, 197);
    curr.put(grn, 243);
    currentVotes.setValue(curr);
    voteHeader.setValue("5 OF 9 POLLS REPORTING");
    voteSubhead.setValue("PROJECTION: TOO EARLY TO CALL");
    pctReporting.setValue(5.0 / 9);
    leader.setValue(Result.leading(pc.getParty()));
    compareRendering("SimpleVoteViewPanel", "Update-4", panel);

    curr.put(ndp, 124);
    curr.put(pc, 1373);
    curr.put(lib, 785);
    curr.put(grn, 675);
    currentVotes.setValue(curr);
    voteHeader.setValue("9 OF 9 POLLS REPORTING");
    voteSubhead.setValue("PROJECTION: PC GAIN FROM LIB");
    pctReporting.setValue(9.0 / 9);
    leader.setValue(Result.elected(pc.getParty()));
    winner.setValue(pc);
    compareRendering("SimpleVoteViewPanel", "Update-5", panel);
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
    BindableWrapper<List<Shape>> focus = new BindableWrapper<>();
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
        shapesByDistrict.entrySet().stream()
            .filter(e -> e.getKey() <= 7)
            .map(Map.Entry::getValue)
            .collect(Collectors.toList()));
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

  private Map<Integer, Shape> peiShapesByDistrict() throws IOException {
    URL peiMap =
        MapFrameTest.class
            .getClassLoader()
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp");
    return ShapefileReader.readShapes(peiMap, "DIST_NO", Integer.class);
  }
}
