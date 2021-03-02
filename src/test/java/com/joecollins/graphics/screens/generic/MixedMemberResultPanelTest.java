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
    BindableWrapper<PartyResult> selectedResult = new BindableWrapper<>(null);

    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    selectedResult.setValue(PartyResult.elected(lib));

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
    currCandVotes.put(new Candidate("Robert Mitchell", lib, true), 1420);
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
    BindableWrapper<PartyResult> selectedResult = new BindableWrapper<>(null);
    BindableWrapper<Double> candidatePctReporting = new BindableWrapper<>(0.0);
    BindableWrapper<Double> partyPctReporting = new BindableWrapper<>(0.0);
    BindableWrapper<Candidate> winner = new BindableWrapper<>(null);

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
            .withWinner(winner.getBinding())
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
    currCandVotes.put(new Candidate("Robert Mitchell", lib, true), 284);
    currCandVotes.put(new Candidate("Amanda Morrison", grn), 211);
    currentCandidateVotes.setValue(currCandVotes);
    winner.setValue(
        currCandVotes.keySet().stream().filter(c -> c.getParty() == lib).findFirst().get());

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
    selectedResult.setValue(PartyResult.leading(lib));

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
    BindableWrapper<PartyResult> selectedResult = new BindableWrapper<>(null);
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
    currCandVotes.put(new Candidate("Robert Mitchell", lib, true), 0);
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
  public void testZeroVotesForSingleEntry() throws IOException {
    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);

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
    BindableWrapper<PartyResult> selectedResult = new BindableWrapper<>(PartyResult.leading(lib));
    BindableWrapper<Double> candidatePctReporting = new BindableWrapper<>(0.0);
    BindableWrapper<Double> partyPctReporting = new BindableWrapper<>(0.0);

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
    currCandVotes.put(new Candidate("Mike Gillis", pc), 8);
    currCandVotes.put(new Candidate("Robert Mitchell", lib, true), 14);
    currCandVotes.put(new Candidate("Amanda Morrison", grn), 11);
    currentCandidateVotes.setValue(currCandVotes);

    LinkedHashMap<Party, Integer> prevCandVotes = new LinkedHashMap<>();
    prevCandVotes.put(lib, 1425);
    prevCandVotes.put(pc, 1031);
    prevCandVotes.put(ndp, 360);
    prevCandVotes.put(grn, 295);
    previousCandidateVotes.setValue(prevCandVotes);

    LinkedHashMap<Party, Integer> currPartyVotes = new LinkedHashMap<>();
    currPartyVotes.put(grn, 11);
    currPartyVotes.put(lib, 14);
    currPartyVotes.put(ndp, 0);
    currPartyVotes.put(pc, 8);
    currentPartyVotes.setValue(currPartyVotes);

    LinkedHashMap<Party, Integer> prevPartyVotes = new LinkedHashMap<>();
    prevPartyVotes.put(lib, 1397);
    prevPartyVotes.put(pc, 1062);
    prevPartyVotes.put(ndp, 544);
    prevPartyVotes.put(grn, 426);
    previousPartyVotes.setValue(prevPartyVotes);

    candidatePctReporting.setValue(0.01);
    partyPctReporting.setValue(0.01);

    compareRendering("MixedMemberResultPanel", "ZeroVotes", panel);
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
    BindableWrapper<PartyResult> selectedResult = new BindableWrapper<>(null);

    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    selectedResult.setValue(PartyResult.elected(lib));

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
    currCandVotes.put(Candidate.OTHERS, 1106);
    currCandVotes.put(new Candidate("Robert Mitchell", lib, true), 1420);
    currCandVotes.put(new Candidate("Amanda Morrison", grn), 1057);
    currentCandidateVotes.setValue(currCandVotes);

    LinkedHashMap<Party, Integer> prevCandVotes = new LinkedHashMap<>();
    prevCandVotes.put(Party.OTHERS, 1391);
    prevCandVotes.put(lib, 1425);
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
    BindableWrapper<PartyResult> selectedResult = new BindableWrapper<>(null);

    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    selectedResult.setValue(PartyResult.elected(lib));

    MixedMemberResultPanel panel =
        MixedMemberResultPanel.builder()
            .withCandidateVotes(currentCandidateVotes.getBinding(), candidateHeader.getBinding())
            .withPrevCandidateVotes(
                previousCandidateVotes.getBinding(), candidateChangeHeader.getBinding())
            .withIncumbentMarker("(MLA)")
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
    currCandVotes.put(new Candidate("Robert Mitchell", lib, true), 1420);
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
    BindableWrapper<PartyResult> selectedResult = new BindableWrapper<>(null);

    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    selectedResult.setValue(PartyResult.elected(lib));

    MixedMemberResultPanel panel =
        MixedMemberResultPanel.builder()
            .withCandidateVotes(currentCandidateVotes.getBinding(), candidateHeader.getBinding())
            .withPartyVotes(currentPartyVotes.getBinding(), partyHeader.getBinding())
            .withIncumbentMarker("(MLA)")
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
    currCandVotes.put(new Candidate("Robert Mitchell", lib, true), 1420);
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

  @Test
  public void testSubheadWithoutPrev() throws Exception {
    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentCandidateVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> currentPartyVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<String> header = new BindableWrapper<>("CHARLOTTETOWN-WINSLOE");
    BindableWrapper<String> candidateHeader = new BindableWrapper<>("CANDIDATE VOTES");
    BindableWrapper<String> candidateSubhead = new BindableWrapper<>("LIB WIN IN 2015");
    BindableWrapper<String> partyHeader = new BindableWrapper<>("PARTY VOTES");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("CHARLOTTETOWN");
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Integer>> focus =
        new BindableWrapper<>(
            shapesByDistrict.keySet().stream()
                .filter(id -> id >= 10 && id <= 14)
                .collect(Collectors.toList()));
    BindableWrapper<Integer> selectedShape = new BindableWrapper<>(10);
    BindableWrapper<PartyResult> selectedResult = new BindableWrapper<>(null);

    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    selectedResult.setValue(PartyResult.elected(lib));

    MixedMemberResultPanel panel =
        MixedMemberResultPanel.builder()
            .withCandidateVotes(
                currentCandidateVotes.getBinding(),
                candidateHeader.getBinding(),
                candidateSubhead.getBinding())
            .withPartyVotes(currentPartyVotes.getBinding(), partyHeader.getBinding())
            .withIncumbentMarker("(MLA)")
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
    currCandVotes.put(new Candidate("Robert Mitchell", lib, true), 1420);
    currCandVotes.put(new Candidate("Amanda Morrison", grn), 1057);
    currentCandidateVotes.setValue(currCandVotes);

    LinkedHashMap<Party, Integer> currPartyVotes = new LinkedHashMap<>();
    currPartyVotes.put(grn, 1098);
    currPartyVotes.put(lib, 1013);
    currPartyVotes.put(ndp, 112);
    currPartyVotes.put(pc, 822);
    currentPartyVotes.setValue(currPartyVotes);

    compareRendering("MixedMemberResultPanel", "NoPrevSubhead", panel);
  }

  @Test
  public void testTickWithoutPrev() throws Exception {
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
    BindableWrapper<PartyResult> selectedResult = new BindableWrapper<>(null);
    BindableWrapper<Candidate> winner = new BindableWrapper<>(null);

    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    selectedResult.setValue(PartyResult.elected(lib));

    MixedMemberResultPanel panel =
        MixedMemberResultPanel.builder()
            .withCandidateVotes(currentCandidateVotes.getBinding(), candidateHeader.getBinding())
            .withPartyVotes(currentPartyVotes.getBinding(), partyHeader.getBinding())
            .withIncumbentMarker("(MLA)")
            .withWinner(winner.getBinding())
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
    currCandVotes.put(new Candidate("Robert Mitchell", lib, true), 1420);
    currCandVotes.put(new Candidate("Amanda Morrison", grn), 1057);
    currentCandidateVotes.setValue(currCandVotes);
    winner.setValue(
        currCandVotes.keySet().stream().filter(c -> c.getParty() == lib).findFirst().get());

    LinkedHashMap<Party, Integer> currPartyVotes = new LinkedHashMap<>();
    currPartyVotes.put(grn, 1098);
    currPartyVotes.put(lib, 1013);
    currPartyVotes.put(ndp, 112);
    currPartyVotes.put(pc, 822);
    currentPartyVotes.setValue(currPartyVotes);

    compareRendering("MixedMemberResultPanel", "NoPrevTick", panel);
  }

  @Test
  public void testDeclarationInProgress() throws IOException {
    Party lab = new Party("Labour", "LAB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party con = new Party("Conservative", "CON", Color.BLUE);
    Party ld = new Party("Liberal Democrats", "LD", Color.ORANGE);
    Party ukip = new Party("UK Independency Party", "UKIP", Color.MAGENTA.darker());
    Party resp = new Party("Respect", "RESP", Color.RED.darker());
    Party wep = new Party("Women's Equality Party", "WEP", Color.CYAN.darker());
    Party bnp = new Party("British National Party", "BNP", Color.BLUE.darker());
    Party cpa = new Party("Christian People's Alliance", "CPA", Color.MAGENTA);
    Party bf = new Party("Britain First", "BF", Color.BLUE.darker());
    Party house = new Party("The House Party", "HOUSE", Color.GRAY);
    Party awp = new Party("Animal Welfare Party", "AWP", Color.GRAY);
    Party app = new Party("All People's Party", "APP", Color.GRAY);
    Party city = new Party("Take Back The City", "CITY", Color.GRAY);
    Party cl = new Party("Communist League", "CL", Color.GRAY);
    Party cup = new Party("Communities United Party", "CUP", Color.GRAY);
    Party fresh = new Party("Fresh Choice for London", "FRESH", Color.GRAY);
    Party ed = new Party("English Democrats", "ED", Color.GRAY);
    Party tusc = new Party("Trade Unionist and Socialist Coalition", "TUSC", Color.GRAY);
    Party ind = new Party("Independent", "IND", Color.GRAY);

    BindableWrapper<Map<Candidate, Integer>> currentCandidateVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Map<Party, Integer>> previousCandidateVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Map<Party, Integer>> currentPartyVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Map<Party, Integer>> previousPartyVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<String> header = new BindableWrapper<>("CITY & EAST");
    BindableWrapper<String> candidateHeader = new BindableWrapper<>("CONSTITUENCY VOTES");
    BindableWrapper<String> candidateChangeHeader =
        new BindableWrapper<>("CONSTITUENCY CHANGE SINCE 2012");
    BindableWrapper<String> partyHeader = new BindableWrapper<>("AT-LARGE VOTES");
    BindableWrapper<String> partyChangeHeader = new BindableWrapper<>("AT-LARGE CHANGE SINCE 2012");
    BindableWrapper<Party[]> topPartiesWaiting =
        new BindableWrapper<>(new Party[] {con, lab, ld, grn});

    MixedMemberResultPanel panel =
        MixedMemberResultPanel.builder()
            .withCandidateVotes(currentCandidateVotes.getBinding(), candidateHeader.getBinding())
            .withPrevCandidateVotes(
                previousCandidateVotes.getBinding(), candidateChangeHeader.getBinding())
            .withPartyVotes(
                Aggregators.topAndOthers(
                    currentPartyVotes.getBinding(),
                    5,
                    Party.OTHERS,
                    topPartiesWaiting.getBinding()),
                partyHeader.getBinding())
            .withPrevPartyVotes(previousPartyVotes.getBinding(), partyChangeHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);

    LinkedHashMap<Candidate, Integer> currCandVotes = new LinkedHashMap<>();
    currCandVotes.put(new Candidate("Elaine Sheila Bagshaw", ld), null);
    currCandVotes.put(new Candidate("Christopher James Chapman", con), null);
    currCandVotes.put(new Candidate("Rachel Collinson", grn), null);
    currCandVotes.put(new Candidate("Unmesh Desai", lab), null);
    currCandVotes.put(new Candidate("Aaron Anthony Jose Hasan D'Souza", app), null);
    currCandVotes.put(new Candidate("Amina May Kay Gichinga", city), null);
    currCandVotes.put(new Candidate("Peter James Harris", ukip), null);
    currCandVotes.put(new Candidate("Rayne Mickail", resp), null);
    currentCandidateVotes.setValue(currCandVotes);

    LinkedHashMap<Party, Integer> prevCandVotes = new LinkedHashMap<>();
    prevCandVotes.put(lab, 107667);
    prevCandVotes.put(bnp, 7031);
    prevCandVotes.put(cl, 1108);
    prevCandVotes.put(ld, 7351);
    prevCandVotes.put(cup, 6774);
    prevCandVotes.put(con, 24923);
    prevCandVotes.put(grn, 10891);
    prevCandVotes.put(fresh, 5243);
    previousCandidateVotes.setValue(prevCandVotes);

    LinkedHashMap<Party, Integer> currPartyVotes = new LinkedHashMap<>();
    currPartyVotes.put(awp, null);
    currPartyVotes.put(bf, null);
    currPartyVotes.put(bnp, null);
    currPartyVotes.put(cpa, null);
    currPartyVotes.put(con, null);
    currPartyVotes.put(grn, null);
    currPartyVotes.put(lab, null);
    currPartyVotes.put(ld, null);
    currPartyVotes.put(resp, null);
    currPartyVotes.put(house, null);
    currPartyVotes.put(ukip, null);
    currPartyVotes.put(wep, null);
    currentPartyVotes.setValue(currPartyVotes);

    LinkedHashMap<Party, Integer> prevPartyVotes = new LinkedHashMap<>();
    prevPartyVotes.put(bnp, 5702);
    prevPartyVotes.put(cpa, 3360);
    prevPartyVotes.put(con, 25128);
    prevPartyVotes.put(ed, 1565);
    prevPartyVotes.put(grn, 11086);
    prevPartyVotes.put(lab, 108395);
    prevPartyVotes.put(ld, 6140);
    prevPartyVotes.put(bf, 749);
    prevPartyVotes.put(house, 684);
    prevPartyVotes.put(tusc, 1277);
    prevPartyVotes.put(ukip, 5966);
    prevPartyVotes.put(ind, 299 + 1171);
    previousPartyVotes.setValue(prevPartyVotes);

    compareRendering("MixedMemberResultPanel", "Declaration-1", panel);

    currCandVotes.put(new Candidate("Elaine Sheila Bagshaw", ld), 10714);
    currentCandidateVotes.setValue(currCandVotes);
    compareRendering("MixedMemberResultPanel", "Declaration-2", panel);

    currCandVotes.put(new Candidate("Christopher James Chapman", con), 32546);
    currCandVotes.put(new Candidate("Rachel Collinson", grn), 18766);
    currCandVotes.put(new Candidate("Unmesh Desai", lab), 122175);
    currentCandidateVotes.setValue(currCandVotes);
    compareRendering("MixedMemberResultPanel", "Declaration-3", panel);

    currCandVotes.put(new Candidate("Aaron Anthony Jose Hasan D'Souza", app), 1009);
    currCandVotes.put(new Candidate("Amina May Kay Gichinga", city), 1368);
    currCandVotes.put(new Candidate("Peter James Harris", ukip), 18071);
    currCandVotes.put(new Candidate("Rayne Mickail", resp), 6772);
    currentCandidateVotes.setValue(currCandVotes);
    compareRendering("MixedMemberResultPanel", "Declaration-4", panel);

    currPartyVotes.put(awp, 1738);
    currentPartyVotes.setValue(currPartyVotes);
    topPartiesWaiting.setValue(new Party[] {con, lab, ld});
    compareRendering("MixedMemberResultPanel", "Declaration-5", panel);

    currPartyVotes.put(bf, 3591);
    currPartyVotes.put(bnp, 1828);
    currPartyVotes.put(ld, 7799);
    currentPartyVotes.setValue(currPartyVotes);
    topPartiesWaiting.setValue(new Party[0]);
    compareRendering("MixedMemberResultPanel", "Declaration-6", panel);

    currPartyVotes.put(cpa, 2660);
    currPartyVotes.put(con, 30424);
    currentPartyVotes.setValue(currPartyVotes);
    compareRendering("MixedMemberResultPanel", "Declaration-7", panel);

    currPartyVotes.put(grn, 14151);
    currPartyVotes.put(lab, 121871);
    currentPartyVotes.setValue(currPartyVotes);
    compareRendering("MixedMemberResultPanel", "Declaration-8", panel);

    currPartyVotes.put(resp, 6784);
    currPartyVotes.put(house, 858);
    currPartyVotes.put(ukip, 14123);
    currPartyVotes.put(wep, 5718);
    currentPartyVotes.setValue(currPartyVotes);
    compareRendering("MixedMemberResultPanel", "Declaration-9", panel);

    currentCandidateVotes.setValue(Aggregators.topAndOthers(currCandVotes, 6, Candidate.OTHERS));
    compareRendering("MixedMemberResultPanel", "Declaration-10", panel);
  }

  private Map<Integer, Shape> peiShapesByDistrict() throws IOException {
    URL peiMap =
        MapFrameTest.class
            .getClassLoader()
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp");
    return ShapefileReader.readShapes(peiMap, "DIST_NO", Integer.class);
  }
}
