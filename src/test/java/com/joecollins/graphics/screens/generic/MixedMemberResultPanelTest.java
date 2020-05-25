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
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream()
                .filter(id -> id >= 10 && id <= 14)
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
    currCandVotes.put(new Candidate("Jesse Reddin Cousins", ndp), 41);
    currCandVotes.put(new Candidate("Mike Gillis", pc), 865);
    currCandVotes.put(new Candidate("Robert Mitchell", lib), 1420);
    currCandVotes.put(new Candidate("Amanda Morrison", grn), 1057);
    currentCandidateVotes.setValue(currCandVotes);

    LinkedHashMap<Party, Integer> prevCandVotes = new LinkedHashMap<>();
    prevCandVotes.put(lib, 1425);
    prevCandVotes.put(pc, 1031);
    prevCandVotes.put(ndp, 360);
    prevCandVotes.put(grn, 295);
    previousCandidateVotes.setValue(prevCandVotes);

    LinkedHashMap<Party, Integer> currPartyVotes = new LinkedHashMap<>();
    currPartyVotes.put(grn, 1098);
    currPartyVotes.put(lib, 1013);
    currPartyVotes.put(ndp, 112);
    currPartyVotes.put(pc, 822);
    currentPartyVotes.setValue(currPartyVotes);

    LinkedHashMap<Party, Integer> prevPartyVotes = new LinkedHashMap<>();
    prevPartyVotes.put(lib, 1397);
    prevPartyVotes.put(pc, 1062);
    prevPartyVotes.put(ndp, 544);
    prevPartyVotes.put(grn, 426);
    previousPartyVotes.setValue(prevPartyVotes);

    compareRendering("MixedMemberResultPanel", "Basic", panel);
  }

  @Test
  public void testMMPWithPctReporting() throws Exception {
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
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream()
                .filter(id -> id >= 10 && id <= 14)
                .collect(Collectors.toList()));
    BindableWrapper<Integer> selectedShape = new BindableWrapper<>(10);
    BindableWrapper<Result> selectedResult = new BindableWrapper<>();
    BindableWrapper<Double> candidatePctReporting = new BindableWrapper<>(0.0);
    BindableWrapper<Double> partyPctReporting = new BindableWrapper<>(0.0);

    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);

    MixedMemberResultPanel panel =
        MixedMemberResultPanel.builder()
            .withCandidateVotes(currentCandidateVotes.getBinding(), candidateHeader.getBinding())
            .withPrevCandidateVotes(
                previousCandidateVotes.getBinding(), candidateChangeHeader.getBinding())
            .withCandidatePctReporting(candidatePctReporting.getBinding())
            .withPartyVotes(currentPartyVotes.getBinding(), partyHeader.getBinding())
            .withPrevPartyVotes(previousPartyVotes.getBinding(), partyChangeHeader.getBinding())
            .withPartyPctReporting(partyPctReporting.getBinding())
            .withResultMap(
                () -> shapesByDistrict,
                selectedShape.getBinding(),
                selectedResult.getBinding(),
                focus.getBinding(),
                mapHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);

    LinkedHashMap<Candidate, Integer> currCandVotes = new LinkedHashMap<>();
    currCandVotes.put(new Candidate("Jesse Reddin Cousins", ndp), 8);
    currCandVotes.put(new Candidate("Mike Gillis", pc), 173);
    currCandVotes.put(new Candidate("Robert Mitchell", lib), 284);
    currCandVotes.put(new Candidate("Amanda Morrison", grn), 211);
    currentCandidateVotes.setValue(currCandVotes);

    LinkedHashMap<Party, Integer> prevCandVotes = new LinkedHashMap<>();
    prevCandVotes.put(lib, 1425);
    prevCandVotes.put(pc, 1031);
    prevCandVotes.put(ndp, 360);
    prevCandVotes.put(grn, 295);
    previousCandidateVotes.setValue(prevCandVotes);

    LinkedHashMap<Party, Integer> currPartyVotes = new LinkedHashMap<>();
    currPartyVotes.put(grn, 110);
    currPartyVotes.put(lib, 101);
    currPartyVotes.put(ndp, 11);
    currPartyVotes.put(pc, 82);
    currentPartyVotes.setValue(currPartyVotes);

    LinkedHashMap<Party, Integer> prevPartyVotes = new LinkedHashMap<>();
    prevPartyVotes.put(lib, 1397);
    prevPartyVotes.put(pc, 1062);
    prevPartyVotes.put(ndp, 544);
    prevPartyVotes.put(grn, 426);
    previousPartyVotes.setValue(prevPartyVotes);

    candidatePctReporting.setValue(0.2);
    partyPctReporting.setValue(0.1);
    selectedResult.setValue(Result.leading(lib));

    compareRendering("MixedMemberResultPanel", "PctReporting", panel);
  }

  @Test
  public void testMMPWaiting() throws Exception {
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
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream()
                .filter(id -> id >= 10 && id <= 14)
                .collect(Collectors.toList()));
    BindableWrapper<Integer> selectedShape = new BindableWrapper<>(10);
    BindableWrapper<Result> selectedResult = new BindableWrapper<>();
    BindableWrapper<Double> candidatePctReporting = new BindableWrapper<>(0.0);
    BindableWrapper<Double> partyPctReporting = new BindableWrapper<>(0.0);

    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);

    MixedMemberResultPanel panel =
        MixedMemberResultPanel.builder()
            .withCandidateVotes(currentCandidateVotes.getBinding(), candidateHeader.getBinding())
            .withPrevCandidateVotes(
                previousCandidateVotes.getBinding(), candidateChangeHeader.getBinding())
            .withCandidatePctReporting(candidatePctReporting.getBinding())
            .withPartyVotes(currentPartyVotes.getBinding(), partyHeader.getBinding())
            .withPrevPartyVotes(previousPartyVotes.getBinding(), partyChangeHeader.getBinding())
            .withPartyPctReporting(partyPctReporting.getBinding())
            .withResultMap(
                () -> shapesByDistrict,
                selectedShape.getBinding(),
                selectedResult.getBinding(),
                focus.getBinding(),
                mapHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);

    LinkedHashMap<Candidate, Integer> currCandVotes = new LinkedHashMap<>();
    currCandVotes.put(new Candidate("Jesse Reddin Cousins", ndp), 0);
    currCandVotes.put(new Candidate("Mike Gillis", pc), 0);
    currCandVotes.put(new Candidate("Robert Mitchell", lib), 0);
    currCandVotes.put(new Candidate("Amanda Morrison", grn), 0);
    currentCandidateVotes.setValue(currCandVotes);

    LinkedHashMap<Party, Integer> prevCandVotes = new LinkedHashMap<>();
    prevCandVotes.put(lib, 1425);
    prevCandVotes.put(pc, 1031);
    prevCandVotes.put(ndp, 360);
    prevCandVotes.put(grn, 295);
    previousCandidateVotes.setValue(prevCandVotes);

    LinkedHashMap<Party, Integer> currPartyVotes = new LinkedHashMap<>();
    currPartyVotes.put(grn, 0);
    currPartyVotes.put(lib, 0);
    currPartyVotes.put(ndp, 0);
    currPartyVotes.put(pc, 0);
    currentPartyVotes.setValue(currPartyVotes);

    LinkedHashMap<Party, Integer> prevPartyVotes = new LinkedHashMap<>();
    prevPartyVotes.put(lib, 1397);
    prevPartyVotes.put(pc, 1062);
    prevPartyVotes.put(ndp, 544);
    prevPartyVotes.put(grn, 426);
    previousPartyVotes.setValue(prevPartyVotes);

    candidatePctReporting.setValue(0.0);
    partyPctReporting.setValue(0.0);

    compareRendering("MixedMemberResultPanel", "Waiting", panel);
  }

  @Test
  public void testOtherMMP() throws Exception {
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
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream()
                .filter(id -> id >= 10 && id <= 14)
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
    currCandVotes.put(new Candidate("Jesse Reddin Cousins", ndp), 41);
    currCandVotes.put(new Candidate("Mike Gillis", pc), 865);
    currCandVotes.put(new Candidate("Robert Mitchell", lib), 1420);
    currCandVotes.put(new Candidate("Amanda Morrison", grn), 1057);
    currentCandidateVotes.setValue(currCandVotes);

    LinkedHashMap<Party, Integer> prevCandVotes = new LinkedHashMap<>();
    prevCandVotes.put(lib, 1425);
    prevCandVotes.put(pc, 1031);
    prevCandVotes.put(ndp, 360);
    prevCandVotes.put(grn, 295);
    previousCandidateVotes.setValue(prevCandVotes);

    LinkedHashMap<Party, Integer> currPartyVotes = new LinkedHashMap<>();
    currPartyVotes.put(pc, 1098);
    currPartyVotes.put(lib, 1013);
    currPartyVotes.put(Party.OTHERS, 1050);
    currentPartyVotes.setValue(currPartyVotes);

    LinkedHashMap<Party, Integer> prevPartyVotes = new LinkedHashMap<>();
    prevPartyVotes.put(lib, 1397);
    prevPartyVotes.put(pc, 1062);
    prevPartyVotes.put(Party.OTHERS, 1100);
    previousPartyVotes.setValue(prevPartyVotes);

    compareRendering("MixedMemberResultPanel", "Other", panel);
  }

  @Test
  public void testMapAdditionalHighlights() throws Exception {
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
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream()
                .filter(id -> id >= 10 && id <= 14)
                .collect(Collectors.toList()));
    BindableWrapper<List<Integer>> additionalHighlight = new BindableWrapper<>(List.of(9));
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
                additionalHighlight.getBinding(),
                mapHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);

    LinkedHashMap<Candidate, Integer> currCandVotes = new LinkedHashMap<>();
    currCandVotes.put(new Candidate("Jesse Reddin Cousins", ndp), 41);
    currCandVotes.put(new Candidate("Mike Gillis", pc), 865);
    currCandVotes.put(new Candidate("Robert Mitchell", lib), 1420);
    currCandVotes.put(new Candidate("Amanda Morrison", grn), 1057);
    currentCandidateVotes.setValue(currCandVotes);

    LinkedHashMap<Party, Integer> prevCandVotes = new LinkedHashMap<>();
    prevCandVotes.put(lib, 1425);
    prevCandVotes.put(pc, 1031);
    prevCandVotes.put(ndp, 360);
    prevCandVotes.put(grn, 295);
    previousCandidateVotes.setValue(prevCandVotes);

    LinkedHashMap<Party, Integer> currPartyVotes = new LinkedHashMap<>();
    currPartyVotes.put(grn, 1098);
    currPartyVotes.put(lib, 1013);
    currPartyVotes.put(ndp, 112);
    currPartyVotes.put(pc, 822);
    currentPartyVotes.setValue(currPartyVotes);

    LinkedHashMap<Party, Integer> prevPartyVotes = new LinkedHashMap<>();
    prevPartyVotes.put(lib, 1397);
    prevPartyVotes.put(pc, 1062);
    prevPartyVotes.put(ndp, 544);
    prevPartyVotes.put(grn, 426);
    previousPartyVotes.setValue(prevPartyVotes);

    compareRendering("MixedMemberResultPanel", "MapAdditionalHighlight", panel);
  }

  @Test
  public void testWithoutPrev() throws Exception {
    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentCandidateVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> currentPartyVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<String> header = new BindableWrapper<>("CHARLOTTETOWN-WINSLOE");
    BindableWrapper<String> candidateHeader = new BindableWrapper<>("CANDIDATE VOTES");
    BindableWrapper<String> partyHeader = new BindableWrapper<>("PARTY VOTES");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("CHARLOTTETOWN");
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream()
                .filter(id -> id >= 10 && id <= 14)
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
            .withPartyVotes(currentPartyVotes.getBinding(), partyHeader.getBinding())
            .withResultMap(
                () -> shapesByDistrict,
                selectedShape.getBinding(),
                selectedResult.getBinding(),
                focus.getBinding(),
                mapHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);

    LinkedHashMap<Candidate, Integer> currCandVotes = new LinkedHashMap<>();
    currCandVotes.put(new Candidate("Jesse Reddin Cousins", ndp), 41);
    currCandVotes.put(new Candidate("Mike Gillis", pc), 865);
    currCandVotes.put(new Candidate("Robert Mitchell", lib), 1420);
    currCandVotes.put(new Candidate("Amanda Morrison", grn), 1057);
    currentCandidateVotes.setValue(currCandVotes);

    LinkedHashMap<Party, Integer> currPartyVotes = new LinkedHashMap<>();
    currPartyVotes.put(grn, 1098);
    currPartyVotes.put(lib, 1013);
    currPartyVotes.put(ndp, 112);
    currPartyVotes.put(pc, 822);
    currentPartyVotes.setValue(currPartyVotes);

    compareRendering("MixedMemberResultPanel", "NoPrev", panel);
  }

  private Map<Integer, Shape> peiShapesByDistrict() throws IOException {
    URL peiMap =
        MapFrameTest.class
            .getClassLoader()
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp");
    return ShapefileReader.readShapes(peiMap, "DIST_NO", Integer.class);
  }
}
