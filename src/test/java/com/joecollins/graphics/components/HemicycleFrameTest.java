package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class HemicycleFrameTest {

  @Test
  public void testRowCounts() {
    List<Integer> rowCounts = List.of(5, 6, 7, 9);

    HemicycleFrame frame = new HemicycleFrame();
    frame.setNumRowsBinding(Binding.fixedBinding(rowCounts.size()));
    frame.setRowCountsBinding(IndexedBinding.listBinding(rowCounts));

    assertEquals(4, frame.getNumRows());
    assertEquals(5, frame.getRowCount(0));
    assertEquals(6, frame.getRowCount(1));
    assertEquals(7, frame.getRowCount(2));
    assertEquals(9, frame.getRowCount(3));
  }

  @Test
  public void testDotColors() {
    List<Color> dotColors =
        List.of(
            GREEN, GREEN, BLUE, BLUE, BLUE, //
            GREEN, GREEN, RED, BLUE, BLUE, BLUE, //
            GREEN, GREEN, RED, RED, BLUE, BLUE, BLUE, //
            GREEN, GREEN, RED, RED, RED, BLUE, BLUE, BLUE, BLUE);

    HemicycleFrame frame = new HemicycleFrame();
    frame.setNumDotsBinding(Binding.fixedBinding(dotColors.size()));
    frame.setDotColorBinding(IndexedBinding.listBinding(dotColors));

    assertEquals(27, frame.getNumDots());
    assertEquals(GREEN, frame.getDotColor(0));
    assertEquals(RED, frame.getDotColor(7));
    assertEquals(BLUE, frame.getDotColor(17));
  }

  @Test
  public void testDotBorders() {
    List<Color> dotColors =
        List.of(
            RED, RED, RED, BLUE, BLUE, //
            RED, RED, RED, RED, BLUE, BLUE, //
            RED, RED, RED, RED, RED, BLUE, BLUE, //
            GREEN, RED, RED, RED, RED, RED, RED, BLUE, BLUE);

    HemicycleFrame frame = new HemicycleFrame();
    frame.setNumDotsBinding(Binding.fixedBinding(dotColors.size()));
    frame.setDotBorderBinding(IndexedBinding.listBinding(dotColors));

    assertEquals(27, frame.getNumDots());
    assertEquals(RED, frame.getDotBorder(0));
    assertEquals(BLUE, frame.getDotBorder(9));
    assertEquals(GREEN, frame.getDotBorder(18));
  }

  @Test
  public void testRenderDotsOnly() throws IOException {
    List<Integer> rowCounts = List.of(7, 9, 11);
    List<Color> dotColors =
        List.of(
            GREEN, GREEN, RED, BLUE, BLUE, BLUE, BLUE, //
            GREEN, GREEN, GREEN, RED, RED, BLUE, BLUE, BLUE, BLUE, //
            GREEN, GREEN, GREEN, RED, RED, RED, BLUE, BLUE, BLUE, BLUE, BLUE);
    List<Color> dotBorders =
        List.of(
            RED, RED, RED, RED, RED, BLUE, BLUE, //
            RED, RED, RED, RED, RED, RED, BLUE, BLUE, BLUE, //
            GREEN, RED, RED, RED, RED, RED, RED, RED, BLUE, BLUE, BLUE);

    HemicycleFrame frame = new HemicycleFrame();
    frame.setNumRowsBinding(Binding.fixedBinding(rowCounts.size()));
    frame.setRowCountsBinding(IndexedBinding.listBinding(rowCounts));
    frame.setNumDotsBinding(Binding.fixedBinding(dotColors.size()));
    frame.setDotColorBinding(IndexedBinding.listBinding(dotColors));
    frame.setDotBorderBinding(IndexedBinding.listBinding(dotBorders));
    frame.setHeaderBinding(() -> "PEI HEMICYCLE");
    frame.setSize(1024, 512);

    compareRendering("HemicycleFrame", "DotsOnly", frame);
  }
}
