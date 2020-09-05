package com.joecollins.graphics.components;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.graphics.components.HemicycleFrameBuilder.Tiebreaker;
import com.joecollins.graphics.utils.ColorUtils;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class HemicycleFrameBuilderTest {

  @Test
  public void testHemicycleBasic() {
    List<Integer> rows = List.of(7, 9, 11);
    List<Pair<Color, Color>> dots = new ArrayList<>();
    dots.addAll(Collections.nCopies(1, ImmutablePair.of(GREEN, GREEN)));
    dots.addAll(Collections.nCopies(7, ImmutablePair.of(GREEN, RED)));
    dots.addAll(Collections.nCopies(6, ImmutablePair.of(RED, RED)));
    dots.addAll(Collections.nCopies(5, ImmutablePair.of(BLUE, RED)));
    dots.addAll(Collections.nCopies(8, ImmutablePair.of(BLUE, BLUE)));

    BindableList<Pair<Color, Integer>> leftSeatBars =
        new BindableList<>(List.of(ImmutablePair.of(GREEN, 8)));
    BindableList<Pair<Color, Integer>> rightSeatBars =
        new BindableList<>(List.of(ImmutablePair.of(BLUE, 13)));
    BindableList<Pair<Color, Integer>> middleSeatBars =
        new BindableList<>(List.of(ImmutablePair.of(RED, 6)));
    BindableList<Pair<Color, Integer>> leftChangeBars =
        new BindableList<>(List.of(ImmutablePair.of(GREEN, +7)));
    BindableList<Pair<Color, Integer>> rightChangeBars =
        new BindableList<>(List.of(ImmutablePair.of(BLUE, +5)));

    HemicycleFrame frame =
        HemicycleFrameBuilder.of(
                rows,
                dots,
                e -> Binding.fixedBinding(e.getLeft()),
                e -> Binding.fixedBinding(e.getRight()),
                Tiebreaker.FRONT_ROW_FROM_RIGHT)
            .withLeftSeatBars(
                leftSeatBars, Pair::getLeft, Pair::getRight, Binding.fixedBinding("GREEN: 8"))
            .withRightSeatBars(
                rightSeatBars,
                Pair::getLeft,
                Pair::getRight,
                Binding.fixedBinding("PROGRESSIVE CONSERVATIVE: 13"))
            .withMiddleSeatBars(
                middleSeatBars, Pair::getLeft, Pair::getRight, Binding.fixedBinding("LIBERAL: 6"))
            .withLeftChangeBars(
                leftChangeBars,
                Pair::getLeft,
                Pair::getRight,
                Binding.fixedBinding(1),
                Binding.fixedBinding("GRN: +7"))
            .withRightChangeBars(
                rightChangeBars,
                Pair::getLeft,
                Pair::getRight,
                Binding.fixedBinding(8),
                Binding.fixedBinding("PC: +5"))
            .withHeader(() -> "PEI")
            .build();

    assertEquals(3, frame.getNumRows());
    assertEquals(7, frame.getRowCount(0));
    assertEquals(9, frame.getRowCount(1));
    assertEquals(11, frame.getRowCount(2));

    assertEquals(27, frame.getNumDots());
    List<Color> expectedDots =
        List.of(
            GREEN, GREEN, RED, BLUE, BLUE, BLUE, BLUE, //
            GREEN, GREEN, GREEN, RED, RED, BLUE, BLUE, BLUE, BLUE, //
            GREEN, GREEN, GREEN, RED, RED, RED, BLUE, BLUE, BLUE, BLUE, BLUE);
    List<Color> expectedBorders =
        List.of(
            RED, RED, RED, RED, RED, BLUE, BLUE, //
            RED, RED, RED, RED, RED, RED, BLUE, BLUE, BLUE, //
            GREEN, RED, RED, RED, RED, RED, RED, RED, BLUE, BLUE, BLUE);
    for (int i = 0; i < frame.getNumDots(); i++) {
      assertEquals("Dot color " + i, expectedDots.get(i), frame.getDotColor(i));
      assertEquals("Dot border " + i, expectedBorders.get(i), frame.getDotBorder(i));
    }

    assertEquals(1, frame.getLeftSeatBarCount());
    assertEquals(GREEN, frame.getLeftSeatBarColor(0));
    assertEquals(8, frame.getLeftSeatBarSize(0));
    assertEquals("GREEN: 8", frame.getLeftSeatBarLabel());

    assertEquals(1, frame.getRightSeatBarCount());
    assertEquals(BLUE, frame.getRightSeatBarColor(0));
    assertEquals(13, frame.getRightSeatBarSize(0));
    assertEquals("PROGRESSIVE CONSERVATIVE: 13", frame.getRightSeatBarLabel());

    assertEquals(1, frame.getMiddleSeatBarCount());
    assertEquals(RED, frame.getMiddleSeatBarColor(0));
    assertEquals(6, frame.getMiddleSeatBarSize(0));
    assertEquals("LIBERAL: 6", frame.getMiddleSeatBarLabel());

    assertEquals(1, frame.getLeftChangeBarCount());
    assertEquals(GREEN, frame.getLeftChangeBarColor(0));
    assertEquals(7, frame.getLeftChangeBarSize(0));
    assertEquals(1, frame.getLeftChangeBarStart());
    assertEquals("GRN: +7", frame.getLeftChangeBarLabel());

    assertEquals(1, frame.getRightChangeBarCount());
    assertEquals(BLUE, frame.getRightChangeBarColor(0));
    assertEquals(5, frame.getRightChangeBarSize(0));
    assertEquals(8, frame.getRightChangeBarStart());
    assertEquals("PC: +5", frame.getRightChangeBarLabel());

    assertEquals("PEI", frame.getHeader());
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

    HemicycleFrame frame =
        HemicycleFrameBuilder.ofElectedLeading(
            List.of(ridings.size()),
            ridings,
            r -> Binding.fixedBinding(new HemicycleFrameBuilder.Result(r.leader, r.hasWon)),
            r -> r.prev,
            lib,
            yp,
            (e, l) -> "LIB: " + e + "/" + l,
            (e, l) -> "YP: " + e + "/" + l,
            (e, l) -> "OTH: " + e + "/" + l,
            (e, l) -> l > 0,
            (e, l) ->
                new DecimalFormat("+0;-0").format(e) + "/" + new DecimalFormat("+0;-0").format(l),
            Tiebreaker.FRONT_ROW_FROM_LEFT,
            () -> "YUKON");

    assertEquals(19, frame.getNumDots());
    assertEquals(Color.RED, frame.getDotBorder(0));
    assertEquals(ColorUtils.lighten(Color.RED), frame.getDotColor(0));
    assertEquals("YUKON", frame.getHeader());

    assertEquals(2, frame.getLeftSeatBarCount());
    assertEquals(Color.RED, frame.getLeftSeatBarColor(0));
    assertEquals(3, frame.getLeftSeatBarSize(0));
    assertEquals(ColorUtils.lighten(Color.RED), frame.getLeftSeatBarColor(1));
    assertEquals(8, frame.getLeftSeatBarSize(1));
    assertEquals("LIB: 3/11", frame.getLeftSeatBarLabel());

    assertEquals(2, frame.getRightSeatBarCount());
    assertEquals(Color.BLUE, frame.getRightSeatBarColor(0));
    assertEquals(3, frame.getRightSeatBarSize(0));
    assertEquals(ColorUtils.lighten(Color.BLUE), frame.getRightSeatBarColor(1));
    assertEquals(3, frame.getRightSeatBarSize(1));
    assertEquals("YP: 3/6", frame.getRightSeatBarLabel());

    assertEquals(2, frame.getMiddleSeatBarCount());
    assertEquals(Color.GRAY, frame.getMiddleSeatBarColor(0));
    assertEquals(0, frame.getMiddleSeatBarSize(0));
    assertEquals(ColorUtils.lighten(Color.GRAY), frame.getMiddleSeatBarColor(1));
    assertEquals(2, frame.getMiddleSeatBarSize(1));
    assertEquals("OTH: 0/2", frame.getMiddleSeatBarLabel());

    assertEquals(2, frame.getLeftChangeBarStart());
    assertEquals(2, frame.getLeftChangeBarCount());
    assertEquals(Color.RED, frame.getLeftChangeBarColor(0));
    assertEquals(2, frame.getLeftChangeBarSize(0));
    assertEquals(ColorUtils.lighten(Color.RED), frame.getLeftChangeBarColor(1));
    assertEquals(7, frame.getLeftChangeBarSize(1));
    assertEquals("+2/+9", frame.getLeftChangeBarLabel());

    assertEquals(11, frame.getRightChangeBarStart());
    assertEquals(0, frame.getRightChangeBarCount());
    assertEquals("", frame.getRightChangeBarLabel());
  }

  enum Property {
    PROP
  }

  @Test
  public void testLeadingElectedSeatsRepeated() {
    Party dem = new Party("Democratic", "DEM", Color.BLUE);
    Party gop = new Party("Republican", "GOP", Color.RED);

    class Result extends Bindable {
      Party leader;
      boolean hasWon;
      final Party prev;

      Result(Party leader, boolean hasWon, Party prev) {
        this.leader = leader;
        this.hasWon = hasWon;
        this.prev = prev;
      }

      Binding<HemicycleFrameBuilder.Result> getBinding() {
        return Binding.propertyBinding(
            this,
            t -> {
              if (leader == null) return null;
              return new HemicycleFrameBuilder.Result(leader, hasWon);
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
    HemicycleFrame frame =
        HemicycleFrameBuilder.ofElectedLeading(
            List.of(results.size()),
            results,
            r -> r.getBinding(),
            r -> r.prev,
            dem,
            gop,
            (e, l) -> "DEM: " + e + "/" + l,
            (e, l) -> "GOP: " + e + "/" + l,
            (e, l) -> "OTH: " + e + "/" + l,
            (e, l) -> l > 0,
            (e, l) ->
                new DecimalFormat("+0;-0").format(e) + "/" + new DecimalFormat("+0;-0").format(l),
            Tiebreaker.FRONT_ROW_FROM_LEFT,
            () -> "TEST");
    assertEquals(30, frame.getNumDots());
    assertEquals(Color.RED, frame.getDotBorder(0));
    assertEquals(Color.WHITE, frame.getDotColor(0));
    assertEquals(0, frame.getLeftSeatBarSize(0));
    assertEquals(0, frame.getLeftSeatBarSize(1));
    assertEquals(0, frame.getRightSeatBarSize(0));
    assertEquals(0, frame.getRightSeatBarSize(1));
    assertEquals(0, frame.getMiddleSeatBarSize(0));
    assertEquals(0, frame.getMiddleSeatBarSize(1));

    result.setResult(gop, false);
    assertEquals(Color.RED, frame.getDotBorder(0));
    assertEquals(ColorUtils.lighten(Color.RED), frame.getDotColor(0));
    assertEquals(0, frame.getLeftSeatBarSize(0));
    assertEquals(0, frame.getLeftSeatBarSize(1));
    assertEquals(0, frame.getRightSeatBarSize(0));
    assertEquals(30, frame.getRightSeatBarSize(1));
    assertEquals(0, frame.getMiddleSeatBarSize(0));
    assertEquals(0, frame.getMiddleSeatBarSize(1));

    result.setResult(dem, true);
    assertEquals(Color.RED, frame.getDotBorder(0));
    assertEquals(Color.BLUE, frame.getDotColor(0));
    assertEquals(30, frame.getLeftSeatBarSize(0));
    assertEquals(0, frame.getLeftSeatBarSize(1));
    assertEquals(0, frame.getRightSeatBarSize(0));
    assertEquals(0, frame.getRightSeatBarSize(1));
    assertEquals(0, frame.getMiddleSeatBarSize(0));
    assertEquals(0, frame.getMiddleSeatBarSize(1));
  }

  @Test
  public void testLeadingElectedMultiseat() {
    Party dem = new Party("Democratic", "DEM", Color.BLUE);
    Party gop = new Party("Republican", "GOP", Color.RED);

    class Result extends Bindable {
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

      Binding<HemicycleFrameBuilder.Result> getBinding() {
        return Binding.propertyBinding(
            this,
            t -> {
              if (leader == null) return null;
              return new HemicycleFrameBuilder.Result(leader, hasWon);
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
    HemicycleFrame frame =
        HemicycleFrameBuilder.ofElectedLeading(
            List.of(results.stream().mapToInt(r -> r.numSeats).sum()),
            results,
            r -> r.numSeats,
            r -> r.getBinding(),
            r -> r.prev,
            dem,
            gop,
            (e, l) -> "DEM: " + e + "/" + l,
            (e, l) -> "GOP: " + e + "/" + l,
            (e, l) -> "OTH: " + e + "/" + l,
            (e, l) -> true,
            (e, l) ->
                new DecimalFormat("+0;-0").format(e) + "/" + new DecimalFormat("+0;-0").format(l),
            Tiebreaker.FRONT_ROW_FROM_LEFT,
            () -> "TEST");
    assertEquals(30, frame.getNumDots());
    assertEquals(Color.RED, frame.getDotBorder(0));
    assertEquals(Color.WHITE, frame.getDotColor(0));
    assertEquals(0, frame.getLeftSeatBarSize(0));
    assertEquals(0, frame.getLeftSeatBarSize(1));
    assertEquals(0, frame.getRightSeatBarSize(0));
    assertEquals(0, frame.getRightSeatBarSize(1));
    assertEquals(0, frame.getMiddleSeatBarSize(0));
    assertEquals(0, frame.getMiddleSeatBarSize(1));
    assertEquals(0, frame.getLeftChangeBarSize(0));
    assertEquals(0, frame.getLeftChangeBarSize(1));
    assertEquals(0, frame.getRightChangeBarSize(0));
    assertEquals(0, frame.getRightChangeBarSize(1));

    result.setResult(gop, false);
    assertEquals(Color.RED, frame.getDotBorder(0));
    assertEquals(ColorUtils.lighten(Color.RED), frame.getDotColor(0));
    assertEquals(0, frame.getLeftSeatBarSize(0));
    assertEquals(0, frame.getLeftSeatBarSize(1));
    assertEquals(0, frame.getRightSeatBarSize(0));
    assertEquals(30, frame.getRightSeatBarSize(1));
    assertEquals(0, frame.getMiddleSeatBarSize(0));
    assertEquals(0, frame.getMiddleSeatBarSize(1));
    assertEquals(0, frame.getLeftChangeBarSize(0));
    assertEquals(0, frame.getLeftChangeBarSize(1));
    assertEquals(0, frame.getRightChangeBarSize(0));
    assertEquals(0, frame.getRightChangeBarSize(1));

    result.setResult(dem, false);
    assertEquals(Color.RED, frame.getDotBorder(0));
    assertEquals(ColorUtils.lighten(Color.BLUE), frame.getDotColor(0));
    assertEquals(0, frame.getLeftSeatBarSize(0));
    assertEquals(30, frame.getLeftSeatBarSize(1));
    assertEquals(0, frame.getRightSeatBarSize(0));
    assertEquals(0, frame.getRightSeatBarSize(1));
    assertEquals(0, frame.getMiddleSeatBarSize(0));
    assertEquals(0, frame.getMiddleSeatBarSize(1));
    assertEquals(0, frame.getLeftChangeBarSize(0));
    assertEquals(30, frame.getLeftChangeBarSize(1));
    assertEquals(0, frame.getRightChangeBarSize(0));
    assertEquals(-30, frame.getRightChangeBarSize(1));

    result.setResult(dem, true);
    assertEquals(Color.RED, frame.getDotBorder(0));
    assertEquals(Color.BLUE, frame.getDotColor(0));
    assertEquals(30, frame.getLeftSeatBarSize(0));
    assertEquals(0, frame.getLeftSeatBarSize(1));
    assertEquals(0, frame.getRightSeatBarSize(0));
    assertEquals(0, frame.getRightSeatBarSize(1));
    assertEquals(0, frame.getMiddleSeatBarSize(0));
    assertEquals(0, frame.getMiddleSeatBarSize(1));
    assertEquals(30, frame.getLeftChangeBarSize(0));
    assertEquals(0, frame.getLeftChangeBarSize(1));
    assertEquals(-30, frame.getRightChangeBarSize(0));
    assertEquals(0, frame.getRightChangeBarSize(1));
  }

  @Test
  public void testBarsUpdateProperlyOnIncompleteResults() {
    Party dem = new Party("Democratic", "DEM", Color.BLUE);
    Party gop = new Party("Republican", "GOP", Color.RED);

    class Result extends Bindable {
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

      Binding<HemicycleFrameBuilder.Result> getBinding() {
        return Binding.propertyBinding(
            this, t -> new HemicycleFrameBuilder.Result(leader, hasWon), Property.PROP);
      }

      void setResult(Party leader, boolean hasWon) {
        this.leader = leader;
        this.hasWon = hasWon;
        onPropertyRefreshed(Property.PROP);
      }
    }

    Result result = new Result(null, false, gop, 30);
    List<Result> results = Arrays.asList(result);
    HemicycleFrame frame =
        HemicycleFrameBuilder.ofElectedLeading(
            List.of(results.stream().mapToInt(r -> r.numSeats).sum()),
            results,
            r -> r.numSeats,
            r -> r.getBinding(),
            r -> r.prev,
            dem,
            gop,
            (e, l) -> "DEM: " + e + "/" + l,
            (e, l) -> "GOP: " + e + "/" + l,
            (e, l) -> "OTH: " + e + "/" + l,
            (e, l) -> true,
            (e, l) ->
                new DecimalFormat("+0;-0").format(e) + "/" + new DecimalFormat("+0;-0").format(l),
            Tiebreaker.FRONT_ROW_FROM_LEFT,
            () -> "TEST");
    assertEquals(30, frame.getNumDots());
    assertEquals(Color.RED, frame.getDotBorder(0));
    assertEquals(Color.WHITE, frame.getDotColor(0));
    assertEquals(0, frame.getLeftSeatBarSize(0));
    assertEquals(0, frame.getLeftSeatBarSize(1));
    assertEquals(0, frame.getRightSeatBarSize(0));
    assertEquals(0, frame.getRightSeatBarSize(1));
    assertEquals(0, frame.getMiddleSeatBarSize(0));
    assertEquals(0, frame.getMiddleSeatBarSize(1));
    assertEquals(0, frame.getLeftChangeBarSize(0));
    assertEquals(0, frame.getLeftChangeBarSize(1));
    assertEquals(0, frame.getRightChangeBarSize(0));
    assertEquals(0, frame.getRightChangeBarSize(1));

    result.setResult(null, false);
    assertEquals(Color.RED, frame.getDotBorder(0));
    assertEquals(Color.WHITE, frame.getDotColor(0));
    assertEquals(0, frame.getLeftSeatBarSize(0));
    assertEquals(0, frame.getLeftSeatBarSize(1));
    assertEquals(0, frame.getRightSeatBarSize(0));
    assertEquals(0, frame.getRightSeatBarSize(1));
    assertEquals(0, frame.getMiddleSeatBarSize(0));
    assertEquals(0, frame.getMiddleSeatBarSize(1));
    assertEquals(0, frame.getLeftChangeBarSize(0));
    assertEquals(0, frame.getLeftChangeBarSize(1));
    assertEquals(0, frame.getRightChangeBarSize(0));
    assertEquals(0, frame.getRightChangeBarSize(1));
  }
}
