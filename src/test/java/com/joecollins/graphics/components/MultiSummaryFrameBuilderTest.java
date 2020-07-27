package com.joecollins.graphics.components;

import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.Binding;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class MultiSummaryFrameBuilderTest {

  @Test
  public void testTooCloseToCallBuilder() {
    Party yp = new Party("Yukon Party", "YP", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN);
    Party ind = new Party("Independent", "IND", Color.GRAY);

    List<Riding> ridings = new ArrayList<>();
    ridings.add(new Riding("Klondike")); // 0
    ridings.add(new Riding("Kluane")); // 1
    ridings.add(new Riding("Lake Laberge")); // 2
    ridings.add(new Riding("Mayo-Tatchun")); // 3
    ridings.add(new Riding("Mount Lorne-Southern Lakes")); // 4
    ridings.add(new Riding("Pelly-Nisutlin")); // 5
    ridings.add(new Riding("Vuntut Gwitchin")); // 6
    ridings.add(new Riding("Watson Lake")); // 7
    MultiSummaryFrame frame =
        MultiSummaryFrameBuilder.tooClose(
                ridings,
                Riding::isTooClose,
                Riding::getMargin,
                r -> Binding.fixedBinding(r.name.toUpperCase()),
                Riding::getBoxes,
                2)
            .withHeader(() -> "TOO CLOSE TO CALL")
            .build();
    assertEquals("TOO CLOSE TO CALL", frame.getHeader());
    assertEquals(0, frame.getNumRows());

    // add first (36)
    ridings.get(5).setResults(Map.of(yp, 140, ndp, 104, lib, 76, grn, 11));
    assertEquals(1, frame.getNumRows());
    assertEquals("PELLY-NISUTLIN", frame.getRowHeader(0));
    assertEquals(Color.BLUE, frame.getColor(0, 0));
    assertEquals("YP: 140", frame.getValue(0, 0));

    // add to top (3/36)
    ridings.get(6).setResults(Map.of(yp, 35, ndp, 2, lib, 38));
    assertEquals(2, frame.getNumRows());
    assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0));
    assertEquals(Color.RED, frame.getColor(0, 0));
    assertEquals("LIB: 38", frame.getValue(0, 0));
    assertEquals("PELLY-NISUTLIN", frame.getRowHeader(1));
    assertEquals(Color.BLUE, frame.getColor(1, 0));
    assertEquals("YP: 140", frame.getValue(1, 0));

    // add beyond limit (3/36/40)
    ridings.get(7).setResults(Map.of(yp, 150, ndp, 110, lib, 106, ind, 19));
    assertEquals(2, frame.getNumRows());
    assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0));
    assertEquals(Color.RED, frame.getColor(0, 0));
    assertEquals("LIB: 38", frame.getValue(0, 0));
    assertEquals("PELLY-NISUTLIN", frame.getRowHeader(1));
    assertEquals(Color.BLUE, frame.getColor(1, 0));
    assertEquals("YP: 140", frame.getValue(1, 0));

    // existing updated, sorted to bottom (3/40/72)
    ridings.get(5).setResults(Map.of(yp, 280, ndp, 207, lib, 152, grn, 22));
    assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0));
    assertEquals(Color.RED, frame.getColor(0, 0));
    assertEquals("LIB: 38", frame.getValue(0, 0));
    assertEquals("WATSON LAKE", frame.getRowHeader(1));
    assertEquals(Color.BLUE, frame.getColor(1, 0));
    assertEquals("YP: 150", frame.getValue(1, 0));

    // bottom (out of view) removed (3/40)
    ridings.get(5).setWinner(yp);
    assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0));
    assertEquals(Color.RED, frame.getColor(0, 0));
    assertEquals("LIB: 38", frame.getValue(0, 0));
    assertEquals("WATSON LAKE", frame.getRowHeader(1));
    assertEquals(Color.BLUE, frame.getColor(1, 0));
    assertEquals("YP: 150", frame.getValue(1, 0));

    // update in view (7/40)
    ridings.get(6).setResults(Map.of(yp, 70, ndp, 3, lib, 77));
    assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0));
    assertEquals(Color.RED, frame.getColor(0, 0));
    assertEquals("LIB: 77", frame.getValue(0, 0));
    assertEquals("WATSON LAKE", frame.getRowHeader(1));
    assertEquals(Color.BLUE, frame.getColor(1, 0));
    assertEquals("YP: 150", frame.getValue(1, 0));

    // remove from in view (40)
    ridings.get(6).setWinner(lib);
    assertEquals(1, frame.getNumRows());
    assertEquals("WATSON LAKE", frame.getRowHeader(0));
    assertEquals(Color.BLUE, frame.getColor(0, 0));
    assertEquals("YP: 150", frame.getValue(0, 0));

    // add to top (25/40)
    ridings.get(1).setResults(Map.of(yp, 169, ndp, 76, lib, 144));
    assertEquals(2, frame.getNumRows());
    assertEquals("KLUANE", frame.getRowHeader(0));
    assertEquals(Color.BLUE, frame.getColor(0, 0));
    assertEquals("YP: 169", frame.getValue(0, 0));
    assertEquals("WATSON LAKE", frame.getRowHeader(1));
    assertEquals(Color.BLUE, frame.getColor(1, 0));
    assertEquals("YP: 150", frame.getValue(1, 0));

    // update in view, sorted (40/49)
    ridings.get(1).setResults(Map.of(yp, 338, ndp, 153, lib, 289));
    assertEquals(2, frame.getNumRows());
    assertEquals("WATSON LAKE", frame.getRowHeader(0));
    assertEquals(Color.BLUE, frame.getColor(0, 0));
    assertEquals("YP: 150", frame.getValue(0, 0));
    assertEquals("KLUANE", frame.getRowHeader(1));
    assertEquals(Color.BLUE, frame.getColor(1, 0));
    assertEquals("YP: 338", frame.getValue(1, 0));
  }

  private static class Riding extends Bindable {
    enum Property {
      RESULT,
      WINNER
    }

    private final String name;
    private Map<Party, Integer> results = new HashMap<>();
    private Party winner;

    private Riding(String name) {
      this.name = name;
    }

    void setResults(Map<Party, Integer> results) {
      this.results = results;
      onPropertyRefreshed(Property.RESULT);
    }

    void setWinner(Party winner) {
      this.winner = winner;
      onPropertyRefreshed(Property.WINNER);
    }

    Binding<Boolean> isTooClose() {
      return Binding.propertyBinding(
          this,
          t -> t.winner == null && t.results.values().stream().mapToInt(i -> i).sum() > 0,
          Property.RESULT,
          Property.WINNER);
    }

    Binding<Integer> getMargin() {
      return Binding.propertyBinding(
          this,
          t -> {
            List<Integer> topTwoVotes =
                results.values().stream()
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            if (topTwoVotes.size() == 0) return 0;
            return topTwoVotes.get(0) - topTwoVotes.get(1);
          },
          Property.RESULT);
    }

    Binding<List<Pair<Color, String>>> getBoxes() {
      return Binding.propertyBinding(
          this,
          t ->
              t.results.entrySet().stream()
                  .sorted(Map.Entry.<Party, Integer>comparingByValue().reversed())
                  .map(
                      e ->
                          ImmutablePair.of(
                              e.getKey().getColor(),
                              e.getKey().getAbbreviation() + ": " + e.getValue()))
                  .collect(Collectors.toList()),
          Property.RESULT);
    }
  }
}
