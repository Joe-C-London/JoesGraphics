package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.BindableWrapper;
import java.awt.Color;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
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
  public void testLeftSeatBar() {
    HemicycleFrame frame = new HemicycleFrame();
    frame.setLeftSeatBarCountBinding(Binding.fixedBinding(2));
    frame.setLeftSeatBarColorBinding(
        IndexedBinding.listBinding(Color.GREEN, new Color(128, 255, 128)));
    frame.setLeftSeatBarSizeBinding(IndexedBinding.listBinding(1, 7));
    frame.setLeftSeatBarLabelBinding(Binding.fixedBinding("GREEN: 1/8"));

    assertEquals(2, frame.getLeftSeatBarCount());
    assertEquals(Color.GREEN, frame.getLeftSeatBarColor(0));
    assertEquals(7, frame.getLeftSeatBarSize(1));
    assertEquals("GREEN: 1/8", frame.getLeftSeatBarLabel());
  }

  @Test
  public void testRightSeatBar() {
    HemicycleFrame frame = new HemicycleFrame();
    frame.setRightSeatBarCountBinding(Binding.fixedBinding(2));
    frame.setRightSeatBarColorBinding(
        IndexedBinding.listBinding(Color.BLUE, new Color(128, 128, 255)));
    frame.setRightSeatBarSizeBinding(IndexedBinding.listBinding(8, 5));
    frame.setRightSeatBarLabelBinding(Binding.fixedBinding("PROGRESSIVE CONSERVATIVE: 8/13"));

    assertEquals(2, frame.getRightSeatBarCount());
    assertEquals(Color.BLUE, frame.getRightSeatBarColor(0));
    assertEquals(5, frame.getRightSeatBarSize(1));
    assertEquals("PROGRESSIVE CONSERVATIVE: 8/13", frame.getRightSeatBarLabel());
  }

  @Test
  public void testMiddleSeatBar() {
    HemicycleFrame frame = new HemicycleFrame();
    frame.setMiddleSeatBarCountBinding(Binding.fixedBinding(2));
    frame.setMiddleSeatBarColorBinding(
        IndexedBinding.listBinding(Color.RED, new Color(255, 128, 128)));
    frame.setMiddleSeatBarSizeBinding(IndexedBinding.listBinding(2, 4));
    frame.setMiddleSeatBarLabelBinding(Binding.fixedBinding("LIBERAL: 2/6"));

    assertEquals(2, frame.getMiddleSeatBarCount());
    assertEquals(Color.RED, frame.getMiddleSeatBarColor(0));
    assertEquals(4, frame.getMiddleSeatBarSize(1));
    assertEquals("LIBERAL: 2/6", frame.getMiddleSeatBarLabel());
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

  @Test
  public void testRenderSeatsBars() throws IOException {
    List<Integer> rowCounts = List.of(7, 9, 11);
    BindableList<Color> dotColors = new BindableList<>();
    dotColors.setAll(Collections.nCopies(27, WHITE));
    List<Color> dotBorders =
        List.of(
            RED, RED, RED, RED, RED, BLUE, BLUE, //
            RED, RED, RED, RED, RED, RED, BLUE, BLUE, BLUE, //
            GREEN, RED, RED, RED, RED, RED, RED, RED, BLUE, BLUE, BLUE);

    BindableList<Integer> leftSeats = new BindableList<>();
    BindableList<Integer> middleSeats = new BindableList<>();
    BindableList<Integer> rightSeats = new BindableList<>();
    Stream.of(leftSeats, middleSeats, rightSeats).forEach(list -> list.setAll(List.of(0, 0)));

    BindableWrapper<String> leftLabel = new BindableWrapper<>("GREEN: 0/0");
    BindableWrapper<String> middleLabel = new BindableWrapper<>("LIBERAL: 0/0");
    BindableWrapper<String> rightLabel = new BindableWrapper<>("PROGRESSIVE CONSERVATIVE: 0/0");

    HemicycleFrame frame = new HemicycleFrame();
    frame.setNumRowsBinding(Binding.fixedBinding(rowCounts.size()));
    frame.setRowCountsBinding(IndexedBinding.listBinding(rowCounts));
    frame.setNumDotsBinding(Binding.sizeBinding(dotColors));
    frame.setDotColorBinding(IndexedBinding.propertyBinding(dotColors, Function.identity()));
    frame.setDotBorderBinding(IndexedBinding.listBinding(dotBorders));
    frame.setHeaderBinding(() -> "PEI HEMICYCLE");

    frame.setLeftSeatBarCountBinding(Binding.sizeBinding(leftSeats));
    Color lGreen = new Color(128, 255, 128);
    frame.setLeftSeatBarColorBinding(IndexedBinding.listBinding(GREEN, lGreen));
    frame.setLeftSeatBarSizeBinding(IndexedBinding.propertyBinding(leftSeats, Function.identity()));
    frame.setLeftSeatBarLabelBinding(leftLabel.getBinding());

    frame.setMiddleSeatBarCountBinding(Binding.sizeBinding(middleSeats));
    Color lRed = new Color(255, 128, 128);
    frame.setMiddleSeatBarColorBinding(IndexedBinding.listBinding(RED, lRed));
    frame.setMiddleSeatBarSizeBinding(
        IndexedBinding.propertyBinding(middleSeats, Function.identity()));
    frame.setMiddleSeatBarLabelBinding(middleLabel.getBinding());

    frame.setRightSeatBarCountBinding(Binding.sizeBinding(rightSeats));
    Color lBlue = new Color(128, 128, 255);
    frame.setRightSeatBarColorBinding(IndexedBinding.listBinding(BLUE, lBlue));
    frame.setRightSeatBarSizeBinding(
        IndexedBinding.propertyBinding(rightSeats, Function.identity()));
    frame.setRightSeatBarLabelBinding(rightLabel.getBinding());

    frame.setSize(1024, 512);
    compareRendering("HemicycleFrame", "SeatsBar-1", frame);

    leftSeats.setAll(List.of(0, 1));
    leftLabel.setValue("GREEN: 0/1");
    middleSeats.setAll(List.of(0, 2));
    middleLabel.setValue("LIBERAL: 0/2");
    rightSeats.setAll(List.of(0, 8));
    rightLabel.setValue("PROGRESSIVE CONSERVATIVE: 0/8");
    dotColors.setAll(
        List.of(
            lGreen, WHITE, WHITE, lRed, WHITE, lBlue, lBlue, //
            WHITE, WHITE, WHITE, WHITE, lRed, WHITE, lBlue, lBlue, lBlue, //
            WHITE, WHITE, WHITE, WHITE, WHITE, WHITE, WHITE, WHITE, lBlue, lBlue, lBlue));
    compareRendering("HemicycleFrame", "SeatsBar-2", frame);

    leftSeats.setAll(List.of(1, 7));
    leftLabel.setValue("GREEN: 1/8");
    middleSeats.setAll(List.of(2, 4));
    middleLabel.setValue("LIBERAL: 2/6");
    rightSeats.setAll(List.of(8, 5));
    rightLabel.setValue("PROGRESSIVE CONSERVATIVE: 8/13");
    dotColors.setAll(
        List.of(
            GREEN, lGreen, RED, lBlue, lBlue, BLUE, BLUE, //
            lGreen, lGreen, lGreen, lRed, RED, lBlue, BLUE, BLUE, BLUE, //
            lGreen, lGreen, lGreen, lRed, lRed, lRed, lBlue, lBlue, BLUE, BLUE, BLUE));
    compareRendering("HemicycleFrame", "SeatsBar-3", frame);

    leftSeats.setAll(List.of(8, 5));
    leftLabel.setValue("GREEN: 8/13");
    middleSeats.setAll(List.of(2, 4));
    middleLabel.setValue("LIBERAL: 2/6");
    rightSeats.setAll(List.of(1, 7));
    rightLabel.setValue("PROGRESSIVE CONSERVATIVE: 1/8");
    compareRendering("HemicycleFrame", "SeatsBar-4", frame);
  }
}
