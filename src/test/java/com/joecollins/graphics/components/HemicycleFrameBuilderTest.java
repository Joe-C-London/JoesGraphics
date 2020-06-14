package com.joecollins.graphics.components;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static org.junit.Assert.assertEquals;

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
  public void testDotsOnly() {
    List<Integer> rows = List.of(7, 9, 11);
    List<Pair<Color, Color>> dots = new ArrayList<>();
    dots.addAll(Collections.nCopies(1, ImmutablePair.of(GREEN, GREEN)));
    dots.addAll(Collections.nCopies(7, ImmutablePair.of(GREEN, RED)));
    dots.addAll(Collections.nCopies(6, ImmutablePair.of(RED, RED)));
    dots.addAll(Collections.nCopies(5, ImmutablePair.of(BLUE, RED)));
    dots.addAll(Collections.nCopies(8, ImmutablePair.of(BLUE, BLUE)));

    HemicycleFrame frame =
        HemicycleFrameBuilder.of(
                rows,
                dots,
                e -> Binding.fixedBinding(e.getLeft()),
                e -> Binding.fixedBinding(e.getRight()),
                Tiebreaker.FRONT_ROW_FROM_RIGHT)
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

    assertEquals("PEI", frame.getHeader());
  }
}
