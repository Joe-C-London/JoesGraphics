package com.joecollins.graphics.components;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.ColorUtils;
import com.joecollins.models.general.Party;
import com.joecollins.models.general.PartyResult;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class HeatMapFrameBuilderTest {

  @Test
  public void testHeatMapBasic() {
    List<Pair<Color, Color>> dots = new ArrayList<>();
    dots.addAll(Collections.nCopies(1, ImmutablePair.of(GREEN, GREEN)));
    dots.addAll(Collections.nCopies(7, ImmutablePair.of(GREEN, RED)));
    dots.addAll(Collections.nCopies(6, ImmutablePair.of(RED, RED)));
    dots.addAll(Collections.nCopies(5, ImmutablePair.of(BLUE, RED)));
    dots.addAll(Collections.nCopies(8, ImmutablePair.of(BLUE, BLUE)));

    BindableList<Pair<Color, Integer>> seatBars =
        new BindableList<>(List.of(ImmutablePair.of(GREEN, 8)));
    BindableList<Pair<Color, Integer>> changeBars =
        new BindableList<>(List.of(ImmutablePair.of(GREEN, +7)));

    HeatMapFrame frame =
        HeatMapFrameBuilder.of(
                () -> 3,
                dots,
                e -> Binding.fixedBinding(e.getLeft()),
                e -> Binding.fixedBinding(e.getRight()))
            .withSeatBars(seatBars, Pair::getLeft, Pair::getRight, Binding.fixedBinding("GREEN: 8"))
            .withChangeBars(
                changeBars,
                Pair::getLeft,
                Pair::getRight,
                Binding.fixedBinding(1),
                Binding.fixedBinding("GRN: +7"))
            .withHeader(() -> "PEI")
            .withBorder(() -> GREEN)
            .build();

    assertEquals(3, frame.getNumRows());

    assertEquals(27, frame.getNumSquares());
    List<Color> expectedFills =
        Stream.of(
                Collections.nCopies(8, GREEN),
                Collections.nCopies(6, RED),
                Collections.nCopies(13, BLUE))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    List<Color> expectedBorders =
        Stream.of(
                Collections.nCopies(1, GREEN),
                Collections.nCopies(18, RED),
                Collections.nCopies(8, BLUE))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    for (int i = 0; i < frame.getNumSquares(); i++) {
      assertEquals("Square fill " + i, expectedFills.get(i), frame.getSquareFill(i));
      assertEquals("Square border " + i, expectedBorders.get(i), frame.getSquareBorder(i));
    }

    assertEquals(1, frame.getSeatBarCount());
    assertEquals(GREEN, frame.getSeatBarColor(0));
    assertEquals(8, frame.getSeatBarSize(0));
    assertEquals("GREEN: 8", frame.getSeatBarLabel());

    assertEquals(1, frame.getChangeBarCount());
    assertEquals(GREEN, frame.getChangeBarColor(0));
    assertEquals(7, frame.getChangeBarSize(0));
    assertEquals(1, frame.getChangeBarStart());
    assertEquals("GRN: +7", frame.getChangeBarLabel());

    assertEquals("PEI", frame.getHeader());
    assertEquals(GREEN, frame.getBorderColor());
  }

  @Test
  public void testBasicLeadingElected() {
    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party yp = new Party("Yukon Party", "YP", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);

    class Riding {
      final String name;
      final Party leader;
      final boolean hasWon;
      final Party prev;

      Riding(String name, Party leader, boolean hasWon, Party prev) {
        this.name = name;
        this.leader = leader;
        this.hasWon = hasWon;
        this.prev = prev;
      }
    }

    List<Riding> ridings = new ArrayList<>();
    ridings.add(new Riding("Vuntut Gwitchin", lib, false, lib));
    ridings.add(new Riding("Klondike", lib, true, lib));
    ridings.add(new Riding("Takhini-Copper King", ndp, false, ndp));
    ridings.add(new Riding("Whitehorse Centre", ndp, false, ndp));
    ridings.add(new Riding("Mayo-Tatchun", lib, true, ndp));
    ridings.add(new Riding("Mount Lorne-Southern Lakes", lib, false, ndp));
    ridings.add(new Riding("Riverdale South", lib, false, ndp));
    ridings.add(new Riding("Copperbelt South", yp, false, ndp));
    ridings.add(new Riding("Porter Creek South", lib, false, yp));
    ridings.add(new Riding("Watson Lake", yp, true, yp));
    ridings.add(new Riding("Porter Creek Centre", lib, false, yp));
    ridings.add(new Riding("Riverdale North", lib, true, yp));
    ridings.add(new Riding("Kluane", yp, false, yp));
    ridings.add(new Riding("Mountainview", lib, false, yp));
    ridings.add(new Riding("Copperbelt North", lib, false, yp));
    ridings.add(new Riding("Pelly-Nisutlin", yp, true, yp));
    ridings.add(new Riding("Porter Creek North", yp, false, yp));
    ridings.add(new Riding("Lake Laberge", yp, true, yp));
    ridings.add(new Riding("Whitehorse West", lib, false, yp));

    HeatMapFrame frame =
        HeatMapFrameBuilder.ofElectedLeading(
            () -> 3,
            ridings,
            r -> Binding.fixedBinding(new PartyResult(r.leader, r.hasWon)),
            r -> r.prev,
            lib,
            (e, l) -> "LIB: " + e + "/" + l,
            (e, l) -> l > 0,
            (e, l) ->
                new DecimalFormat("+0;-0").format(e) + "/" + new DecimalFormat("+0;-0").format(l),
            () -> "YUKON");

    assertEquals(19, frame.getNumSquares());
    assertEquals(Color.RED, frame.getSquareBorder(0));
    assertEquals(ColorUtils.lighten(Color.RED), frame.getSquareFill(0));
    assertEquals("YUKON", frame.getHeader());
    assertEquals(Color.RED, frame.getBorderColor());

    assertEquals(2, frame.getSeatBarCount());
    assertEquals(Color.RED, frame.getSeatBarColor(0));
    assertEquals(3, frame.getSeatBarSize(0));
    assertEquals(ColorUtils.lighten(Color.RED), frame.getSeatBarColor(1));
    assertEquals(8, frame.getSeatBarSize(1));
    assertEquals("LIB: 3/11", frame.getSeatBarLabel());

    assertEquals(2, frame.getChangeBarStart());
    assertEquals(2, frame.getChangeBarCount());
    assertEquals(Color.RED, frame.getChangeBarColor(0));
    assertEquals(2, frame.getChangeBarSize(0));
    assertEquals(ColorUtils.lighten(Color.RED), frame.getChangeBarColor(1));
    assertEquals(7, frame.getChangeBarSize(1));
    assertEquals("+2/+9", frame.getChangeBarLabel());
  }

  enum Property {
    PROP
  }

  @Test
  public void testLeadingElectedSeatsRepeated() {
    Party dem = new Party("Democratic", "DEM", Color.BLUE);
    Party gop = new Party("Republican", "GOP", Color.RED);

    class Result extends Bindable<Property> {
      Party leader;
      boolean hasWon;
      final Party prev;

      Result(Party leader, boolean hasWon, Party prev) {
        this.leader = leader;
        this.hasWon = hasWon;
        this.prev = prev;
      }

      Binding<PartyResult> getBinding() {
        return Binding.propertyBinding(
            this,
            t -> {
              if (leader == null) return null;
              return new PartyResult(leader, hasWon);
            },
            Property.PROP);
      }

      void setResult(Party leader, boolean hasWon) {
        this.leader = leader;
        this.hasWon = hasWon;
        onPropertyRefreshed(Property.PROP);
      }
    }

    Result result = new Result(null, false, gop);
    List<Result> results = Collections.nCopies(30, result);
    HeatMapFrame frame =
        HeatMapFrameBuilder.ofElectedLeading(
            Binding.fixedBinding(results.size()),
            results,
            r -> r.getBinding(),
            r -> r.prev,
            dem,
            (e, l) -> "DEM: " + e + "/" + l,
            (e, l) -> l > 0,
            (e, l) ->
                new DecimalFormat("+0;-0").format(e) + "/" + new DecimalFormat("+0;-0").format(l),
            () -> "TEST");
    assertEquals(30, frame.getNumSquares());
    assertEquals(Color.RED, frame.getSquareBorder(0));
    assertEquals(Color.WHITE, frame.getSquareFill(0));
    assertEquals(0, frame.getSeatBarSize(0));
    assertEquals(0, frame.getSeatBarSize(1));

    result.setResult(gop, false);
    assertEquals(Color.RED, frame.getSquareBorder(0));
    assertEquals(ColorUtils.lighten(Color.RED), frame.getSquareFill(0));
    assertEquals(0, frame.getSeatBarSize(0));
    assertEquals(0, frame.getSeatBarSize(1));

    result.setResult(dem, true);
    assertEquals(Color.RED, frame.getSquareBorder(0));
    assertEquals(Color.BLUE, frame.getSquareFill(0));
    assertEquals(30, frame.getSeatBarSize(0));
    assertEquals(0, frame.getSeatBarSize(1));
  }

  @Test
  public void testLeadingElectedMultiseat() {
    Party dem = new Party("Democratic", "DEM", Color.BLUE);
    Party gop = new Party("Republican", "GOP", Color.RED);

    class Result extends Bindable<Property> {
      Party leader;
      boolean hasWon;
      final Party prev;
      final int numSeats;

      Result(Party leader, boolean hasWon, Party prev, int numSeats) {
        this.leader = leader;
        this.hasWon = hasWon;
        this.prev = prev;
        this.numSeats = numSeats;
      }

      Binding<PartyResult> getBinding() {
        return Binding.propertyBinding(
            this,
            t -> {
              if (leader == null) return null;
              return new PartyResult(leader, hasWon);
            },
            Property.PROP);
      }

      void setResult(Party leader, boolean hasWon) {
        this.leader = leader;
        this.hasWon = hasWon;
        onPropertyRefreshed(Property.PROP);
      }
    }

    Result result = new Result(null, false, gop, 30);
    List<Result> results = Arrays.asList(result);
    HeatMapFrame frame =
        HeatMapFrameBuilder.ofElectedLeading(
            Binding.fixedBinding(results.stream().mapToInt(r -> r.numSeats).sum()),
            results,
            r -> r.numSeats,
            r -> r.getBinding(),
            r -> r.prev,
            dem,
            (e, l) -> "DEM: " + e + "/" + l,
            (e, l) -> true,
            (e, l) ->
                new DecimalFormat("+0;-0").format(e) + "/" + new DecimalFormat("+0;-0").format(l),
            () -> "TEST");
    assertEquals(30, frame.getNumSquares());
    assertEquals(Color.RED, frame.getSquareBorder(0));
    assertEquals(Color.WHITE, frame.getSquareFill(0));
    assertEquals(0, frame.getSeatBarSize(0));
    assertEquals(0, frame.getSeatBarSize(1));
    assertEquals(0, frame.getChangeBarSize(0));
    assertEquals(0, frame.getChangeBarSize(1));

    result.setResult(gop, false);
    assertEquals(Color.RED, frame.getSquareBorder(0));
    assertEquals(ColorUtils.lighten(Color.RED), frame.getSquareFill(0));
    assertEquals(0, frame.getSeatBarSize(0));
    assertEquals(0, frame.getSeatBarSize(1));
    assertEquals(0, frame.getChangeBarSize(0));
    assertEquals(0, frame.getChangeBarSize(1));

    result.setResult(dem, false);
    assertEquals(Color.RED, frame.getSquareBorder(0));
    assertEquals(ColorUtils.lighten(Color.BLUE), frame.getSquareFill(0));
    assertEquals(0, frame.getSeatBarSize(0));
    assertEquals(30, frame.getSeatBarSize(1));
    assertEquals(0, frame.getChangeBarSize(0));
    assertEquals(30, frame.getChangeBarSize(1));

    result.setResult(dem, true);
    assertEquals(Color.RED, frame.getSquareBorder(0));
    assertEquals(Color.BLUE, frame.getSquareFill(0));
    assertEquals(30, frame.getSeatBarSize(0));
    assertEquals(0, frame.getSeatBarSize(1));
    assertEquals(30, frame.getChangeBarSize(0));
    assertEquals(0, frame.getChangeBarSize(1));
  }

  @Test
  public void testBarsUpdateProperlyOnIncompleteResults() {
    Party dem = new Party("Democratic", "DEM", Color.BLUE);
    Party gop = new Party("Republican", "GOP", Color.RED);

    class Result extends Bindable<Property> {
      Party leader;
      boolean hasWon;
      final Party prev;
      final int numSeats;

      Result(Party leader, boolean hasWon, Party prev, int numSeats) {
        this.leader = leader;
        this.hasWon = hasWon;
        this.prev = prev;
        this.numSeats = numSeats;
      }

      Binding<PartyResult> getBinding() {
        return Binding.propertyBinding(this, t -> new PartyResult(leader, hasWon), Property.PROP);
      }

      void setResult(Party leader, boolean hasWon) {
        this.leader = leader;
        this.hasWon = hasWon;
        onPropertyRefreshed(Property.PROP);
      }
    }

    Result result = new Result(null, false, gop, 30);
    List<Result> results = Arrays.asList(result);
    HeatMapFrame frame =
        HeatMapFrameBuilder.ofElectedLeading(
            Binding.fixedBinding(results.stream().mapToInt(r -> r.numSeats).sum()),
            results,
            r -> r.numSeats,
            r -> r.getBinding(),
            r -> r.prev,
            dem,
            (e, l) -> "DEM: " + e + "/" + l,
            (e, l) -> true,
            (e, l) ->
                new DecimalFormat("+0;-0").format(e) + "/" + new DecimalFormat("+0;-0").format(l),
            () -> "TEST");
    assertEquals(30, frame.getNumSquares());
    assertEquals(Color.RED, frame.getSquareBorder(0));
    assertEquals(Color.WHITE, frame.getSquareFill(0));
    assertEquals(0, frame.getSeatBarSize(0));
    assertEquals(0, frame.getSeatBarSize(1));
    assertEquals(0, frame.getChangeBarSize(0));
    assertEquals(0, frame.getChangeBarSize(1));

    result.setResult(null, false);
    assertEquals(Color.RED, frame.getSquareBorder(0));
    assertEquals(Color.WHITE, frame.getSquareFill(0));
    assertEquals(0, frame.getSeatBarSize(0));
    assertEquals(0, frame.getSeatBarSize(1));
    assertEquals(0, frame.getChangeBarSize(0));
    assertEquals(0, frame.getChangeBarSize(1));
  }
}
