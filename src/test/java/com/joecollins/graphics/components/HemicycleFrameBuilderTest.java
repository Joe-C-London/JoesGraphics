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
import org.junit.Test;

public class HemicycleFrameBuilderTest {

  @Test
  public void testDotsOnly() {
    List<Integer> rows = List.of(7, 9, 11);
    List<Color> dots = new ArrayList<>();
    dots.addAll(Collections.nCopies(8, GREEN));
    dots.addAll(Collections.nCopies(6, RED));
    dots.addAll(Collections.nCopies(13, BLUE));

    HemicycleFrame frame =
        HemicycleFrameBuilder.of(rows, dots, Binding::fixedBinding, Tiebreaker.FRONT_ROW_FROM_RIGHT)
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
    for (int i = 0; i < frame.getNumDots(); i++) {
      assertEquals("Dot " + i, expectedDots.get(i), frame.getDotColor(i));
    }
  }
}
