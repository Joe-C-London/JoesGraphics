package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.graphics.components.MapFrameTest;
import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.graphics.utils.ShapefileReader;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.awt.Shape;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
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

    SimpleVoteViewPanel panel =
        SimpleVoteViewPanel.Builder.basicCurrPrev(
                currentVotes.getBinding(),
                previousVotes.getBinding(),
                header.getBinding(),
                voteHeader.getBinding(),
                voteSubhead.getBinding(),
                changeHeader.getBinding(),
                swingHeader.getBinding(),
                swingPartyOrder)
            .withMap(
                mapHeader.getBinding(),
                shapesByDistrict,
                selectedDistrict.getBinding(),
                focus.getBinding())
            .build();
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

    SimpleVoteViewPanel panel =
        SimpleVoteViewPanel.Builder.basicCurrPrev(
                currentVotes.getBinding(),
                previousVotes.getBinding(),
                header.getBinding(),
                voteHeader.getBinding(),
                voteSubhead.getBinding(),
                changeHeader.getBinding(),
                swingHeader.getBinding(),
                swingPartyOrder)
            .build();
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "Basic-2", panel);
  }

  private Map<Integer, Shape> peiShapesByDistrict() throws IOException {
    URL peiMap =
        MapFrameTest.class
            .getClassLoader()
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp");
    return ShapefileReader.readShapes(peiMap, "DIST_NO", Integer.class);
  }
}
