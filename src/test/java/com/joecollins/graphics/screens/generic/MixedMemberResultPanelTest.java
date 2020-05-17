package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.graphics.components.MapFrameTest;
import com.joecollins.graphics.screens.generic.MapBuilder.Result;
import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.graphics.utils.ShapefileReader;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.awt.Shape;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;

public class MixedMemberResultPanelTest {

  @Test
  public void testBasicMMP() throws Exception {
    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentCandidateVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> previousCandidateVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> currentPartyVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> previousPartyVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<String> header = new BindableWrapper<>("CHARLOTTETOWN-WINSLOE");
    BindableWrapper<String> candidateHeader = new BindableWrapper<>("CANDIDATE VOTES");
    BindableWrapper<String> candidateChangeHeader =
        new BindableWrapper<>("CANDIDATE CHANGE SINCE 2015");
    BindableWrapper<String> partyHeader = new BindableWrapper<>("PARTY VOTES");
    BindableWrapper<String> partyChangeHeader = new BindableWrapper<>("PARTY CHANGE SINCE 2015");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("CHARLOTTETOWN");
    BindableWrapper<Map<Integer, Party>> winnersByDistrict = new BindableWrapper<>(new HashMap<>());
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Shape>> focus =
        new BindableWrapper<>(
            shapesByDistrict.entrySet().stream()
                .filter(e -> e.getKey() >= 10 && e.getKey() <= 14)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList()));
    BindableWrapper<Integer> selectedShape = new BindableWrapper<>(10);
    BindableWrapper<Result> selectedResult = new BindableWrapper<>();

    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    selectedResult.setValue(Result.elected(lib));

    MixedMemberResultPanel panel =
        MixedMemberResultPanel.builder()
            .withCandidateVotes(currentCandidateVotes.getBinding(), candidateHeader.getBinding())
            .withPrevCandidateVotes(
                previousCandidateVotes.getBinding(), candidateChangeHeader.getBinding())
            .withPartyVotes(currentPartyVotes.getBinding(), partyHeader.getBinding())
            .withPrevPartyVotes(previousPartyVotes.getBinding(), partyChangeHeader.getBinding())
            .withResultMap(
                () -> shapesByDistrict,
                selectedShape.getBinding(),
                selectedResult.getBinding(),
                focus.getBinding(),
                mapHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);

    LinkedHashMap<Candidate, Integer> currCandVotes = new LinkedHashMap<>();
    currCandVotes.put(new Candidate("Robert Mitchell", lib), 1420);
    currCandVotes.put(new Candidate("Mike Gillis", pc), 865);
    currCandVotes.put(new Candidate("Amanda Morrison", grn), 1057);
    currCandVotes.put(new Candidate("Jesse Reddin Cousins", ndp), 41);
    currentCandidateVotes.setValue(currCandVotes);

    LinkedHashMap<Party, Integer> prevCandVotes = new LinkedHashMap<>();
    prevCandVotes.put(lib, 1425);
    prevCandVotes.put(pc, 1031);
    prevCandVotes.put(ndp, 360);
    prevCandVotes.put(grn, 295);
    previousCandidateVotes.setValue(prevCandVotes);
    currentCandidateVotes.setValue(currCandVotes);

    LinkedHashMap<Party, Integer> currPartyVotes = new LinkedHashMap<>();
    currPartyVotes.put(lib, 1013);
    currPartyVotes.put(pc, 822);
    currPartyVotes.put(ndp, 112);
    currPartyVotes.put(grn, 1098);
    currentPartyVotes.setValue(currPartyVotes);

    LinkedHashMap<Party, Integer> prevPartyVotes = new LinkedHashMap<>();
    prevPartyVotes.put(lib, 1397);
    prevPartyVotes.put(pc, 1062);
    prevPartyVotes.put(ndp, 544);
    prevPartyVotes.put(grn, 426);
    previousPartyVotes.setValue(prevPartyVotes);

    compareRendering("MixedMemberResultPanel", "Map-1", panel);
  }

  private Map<Integer, Shape> peiShapesByDistrict() throws IOException {
    URL peiMap =
        MapFrameTest.class
            .getClassLoader()
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp");
    return ShapefileReader.readShapes(peiMap, "DIST_NO", Integer.class);
  }
}
