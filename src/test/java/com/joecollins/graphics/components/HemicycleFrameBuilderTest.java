package com.joecollins.graphics.components;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.graphics.components.HemicycleFrameBuilder.Tiebreaker;
import java.awt.Color;
import java.util.ArrayList;
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
}
