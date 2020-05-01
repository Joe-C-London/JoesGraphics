package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

public class SwingometerFrameTest {

  @Test
  public void testColors() {
    SwingometerFrame frame = new SwingometerFrame();
    frame.setLeftColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setRightColorBinding(Binding.fixedBinding(Color.RED));
    assertEquals(Color.BLUE, frame.getLeftColor());
    assertEquals(Color.RED, frame.getRightColor());
  }

  @Test
  public void testValue() {
    SwingometerFrame frame = new SwingometerFrame();
    frame.setValueBinding(Binding.fixedBinding(3));
    assertEquals(3, frame.getValue());
  }

  @Test
  public void testRange() {
    SwingometerFrame frame = new SwingometerFrame();
    frame.setRangeBinding(Binding.fixedBinding(10));
    assertEquals(10, frame.getRange());
  }

  @Test
  public void testTicks() {
    BindableList<Integer> ticks =
        IntStream.rangeClosed(-10, 10).boxed().collect(Collectors.toCollection(BindableList::new));
    SwingometerFrame frame = new SwingometerFrame();
    frame.setNumTicksBinding(Binding.sizeBinding(ticks));
    frame.setTickPositionBinding(IndexedBinding.propertyBinding(ticks, Function.identity()));
    frame.setTickTextBinding(
        IndexedBinding.propertyBinding(ticks, t -> String.valueOf(Math.abs(t))));
    assertEquals(21, frame.getNumTicks());
    for (int i = 0; i < 21; i++) {
      assertEquals("Position at index " + i, i - 10, frame.getTickPosition(i));
    }
    for (int i = 0; i <= 10; i++) {
      String text = String.valueOf(i);
      int leftPos = 10 - i;
      int rightPos = 10 + i;
      assertEquals("Text at index " + leftPos, text, frame.getTickText(leftPos));
      assertEquals("Text at index " + rightPos, text, frame.getTickText(rightPos));
    }
  }

  @Test
  public void testWinningPoint() {
    SwingometerFrame frame = new SwingometerFrame();
    frame.setLeftToWinBinding(Binding.fixedBinding(3.0));
    frame.setRightToWinBinding(Binding.fixedBinding(-2.0));
    assertEquals(3.0, frame.getLeftToWin());
    assertEquals(-2.0, frame.getRightToWin());
  }

  @Test
  public void testOuterLabels() {
    BindableList<ImmutableTriple<Double, String, Color>> labels = new BindableList<>();
    labels.add(ImmutableTriple.of(0.0, "50", Color.BLACK));
    labels.add(ImmutableTriple.of(5.0, "75", Color.RED));
    labels.add(ImmutableTriple.of(-5.0, "60", Color.BLUE));
    SwingometerFrame frame = new SwingometerFrame();
    frame.setNumOuterLabelsBinding(Binding.sizeBinding(labels));
    frame.setOuterLabelPositionBinding(IndexedBinding.propertyBinding(labels, Triple::getLeft));
    frame.setOuterLabelTextBinding(IndexedBinding.propertyBinding(labels, Triple::getMiddle));
    frame.setOuterLabelColorBinding(IndexedBinding.propertyBinding(labels, Triple::getRight));
    assertEquals(3, frame.getNumOuterLabels());
    assertEquals(0.0, frame.getOuterLabelPosition(0));
    assertEquals("75", frame.getOuterLabelText(1));
    assertEquals(Color.BLUE, frame.getOuterLabelColor(2));
  }

  @Test
  public void testBuckets() {
    BindableList<Pair<Double, Color>> dots = new BindableList<>();
    dots.add(ImmutablePair.of(0.3, Color.BLUE));
    dots.add(ImmutablePair.of(-0.7, Color.RED));
    dots.add(ImmutablePair.of(2.4, Color.BLACK));
    SwingometerFrame frame = new SwingometerFrame();
    frame.setNumBucketsPerSideBinding(Binding.fixedBinding(20));
    frame.setNumDotsBinding(Binding.sizeBinding(dots));
    frame.setDotsPositionBinding(IndexedBinding.propertyBinding(dots, Pair::getLeft));
    frame.setDotsColorBinding(IndexedBinding.propertyBinding(dots, Pair::getRight));
    assertEquals(20, frame.getNumBucketsPerSide());
    assertEquals(3, frame.getNumDots());
    assertEquals(0.3, frame.getDotPosition(0));
    assertEquals(Color.RED, frame.getDotColor(1));
  }

  @Test
  public void testDotLabels() {
    BindableList<Triple<Double, Color, String>> dots = new BindableList<>();
    dots.add(ImmutableTriple.of(0.3, Color.BLUE, "A"));
    dots.add(ImmutableTriple.of(-0.7, Color.RED, "B"));
    dots.add(ImmutableTriple.of(2.4, Color.BLACK, "C"));
    SwingometerFrame frame = new SwingometerFrame();
    frame.setNumBucketsPerSideBinding(Binding.fixedBinding(20));
    frame.setNumDotsBinding(Binding.sizeBinding(dots));
    frame.setDotsPositionBinding(IndexedBinding.propertyBinding(dots, Triple::getLeft));
    frame.setDotsColorBinding(IndexedBinding.propertyBinding(dots, Triple::getMiddle));
    frame.setDotsLabelBinding(IndexedBinding.propertyBinding(dots, Triple::getRight));
    assertEquals(20, frame.getNumBucketsPerSide());
    assertEquals(3, frame.getNumDots());
    assertEquals(0.3, frame.getDotPosition(0));
    assertEquals(Color.RED, frame.getDotColor(1));
    assertEquals("C", frame.getDotLabel(2));
  }

  @Test
  public void testRenderBasic() throws IOException {
    BindableList<Integer> ticks =
        IntStream.rangeClosed(-9, 9).boxed().collect(Collectors.toCollection(BindableList::new));

    BindableList<ImmutableTriple<Double, Integer, Color>> outerLabels = new BindableList<>();
    outerLabels.add(ImmutableTriple.of(0.0, 51, Color.RED));
    outerLabels.add(ImmutableTriple.of(-1.55, 51, Color.BLUE));
    outerLabels.add(ImmutableTriple.of(-7.8, 52, Color.BLUE));
    outerLabels.add(ImmutableTriple.of(-8.3, 54, Color.BLUE));
    outerLabels.add(ImmutableTriple.of(2.85, 55, Color.RED));
    outerLabels.add(ImmutableTriple.of(4.55, 60, Color.RED));
    outerLabels.add(ImmutableTriple.of(9.75, 65, Color.RED));

    BindableList<ImmutableTriple<String, Double, Color>> dots = new BindableList<>();
    dots.add(ImmutableTriple.of("WY", -27.00, Color.RED));
    dots.add(ImmutableTriple.of("UT", -17.65, Color.RED));
    dots.add(ImmutableTriple.of("TN", -17.25, Color.RED));
    dots.add(ImmutableTriple.of("MS (S)", -11.00, Color.RED));
    dots.add(ImmutableTriple.of("MS", -8.30, Color.RED));
    dots.add(ImmutableTriple.of("TX", -7.90, Color.RED));
    dots.add(ImmutableTriple.of("NE", -7.80, Color.RED));
    dots.add(ImmutableTriple.of("AZ", -1.55, Color.BLUE));
    dots.add(ImmutableTriple.of("NV", -0.60, Color.BLUE));
    dots.add(ImmutableTriple.of("ND", +0.45, Color.RED));
    dots.add(ImmutableTriple.of("MT", +1.85, Color.BLUE));
    dots.add(ImmutableTriple.of("WI", +2.75, Color.BLUE));
    dots.add(ImmutableTriple.of("NM", +2.85, Color.BLUE));
    dots.add(ImmutableTriple.of("IN", +2.90, Color.RED));
    dots.add(ImmutableTriple.of("VA", +2.95, Color.BLUE));
    dots.add(ImmutableTriple.of("OH", +3.00, Color.BLUE));
    dots.add(ImmutableTriple.of("MA", +3.70, Color.BLUE));
    dots.add(ImmutableTriple.of("PA", +4.55, Color.BLUE));
    dots.add(ImmutableTriple.of("MN (S)", +5.15, Color.BLUE));
    dots.add(ImmutableTriple.of("CT", +5.85, Color.BLUE));
    dots.add(ImmutableTriple.of("FL", +6.50, Color.RED));
    dots.add(ImmutableTriple.of("MO", +7.90, Color.RED));
    dots.add(ImmutableTriple.of("NJ", +9.75, Color.BLUE));
    dots.add(ImmutableTriple.of("MI", +10.40, Color.BLUE));
    dots.add(ImmutableTriple.of("WA", +10.50, Color.BLUE));
    dots.add(ImmutableTriple.of("ME", +11.10, Color.BLUE));
    dots.add(ImmutableTriple.of("WV", +12.05, Color.BLUE));
    dots.add(ImmutableTriple.of("CA", +12.50, Color.BLUE));
    dots.add(ImmutableTriple.of("HI", +12.60, Color.BLUE));
    dots.add(ImmutableTriple.of("MD", +14.85, Color.BLUE));
    dots.add(ImmutableTriple.of("RI", +14.90, Color.BLUE));
    dots.add(ImmutableTriple.of("MN", +17.30, Color.BLUE));
    dots.add(ImmutableTriple.of("DE", +18.70, Color.BLUE));
    dots.add(ImmutableTriple.of("NY", +22.30, Color.BLUE));
    dots.add(ImmutableTriple.of("VT", +23.05, Color.BLUE));

    SwingometerFrame frame = new SwingometerFrame();
    frame.setHeaderBinding(Binding.fixedBinding("2018 SENATE SWINGOMETER"));
    frame.setRangeBinding(Binding.fixedBinding(10));
    frame.setValueBinding(Binding.fixedBinding(-4.0));
    frame.setNumBucketsPerSideBinding(Binding.fixedBinding(20));
    frame.setLeftColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setRightColorBinding(Binding.fixedBinding(Color.RED));
    frame.setLeftToWinBinding(Binding.fixedBinding(1.55));
    frame.setRightToWinBinding(Binding.fixedBinding(-0.60));
    frame.setNumTicksBinding(Binding.sizeBinding(ticks));
    frame.setTickPositionBinding(IndexedBinding.propertyBinding(ticks, Function.identity()));
    frame.setTickTextBinding(IndexedBinding.propertyBinding(ticks, String::valueOf));
    frame.setNumOuterLabelsBinding(Binding.sizeBinding(outerLabels));
    frame.setOuterLabelPositionBinding(
        IndexedBinding.propertyBinding(outerLabels, ImmutableTriple::getLeft));
    frame.setOuterLabelTextBinding(
        IndexedBinding.propertyBinding(outerLabels, label -> label.getMiddle().toString()));
    frame.setOuterLabelColorBinding(
        IndexedBinding.propertyBinding(outerLabels, ImmutableTriple::getRight));
    frame.setNumDotsBinding(Binding.sizeBinding(dots));
    frame.setDotsPositionBinding(IndexedBinding.propertyBinding(dots, Triple::getMiddle));
    frame.setDotsColorBinding(IndexedBinding.propertyBinding(dots, Triple::getRight));

    frame.setSize(1024, 512);
    compareRendering("SwingometerFrame", "Unlabelled", frame);
  }

  @Test
  public void testRenderLabels() throws IOException {
    BindableList<Integer> ticks =
        IntStream.rangeClosed(-9, 9).boxed().collect(Collectors.toCollection(BindableList::new));

    BindableList<ImmutableTriple<Double, Integer, Color>> outerLabels = new BindableList<>();
    outerLabels.add(ImmutableTriple.of(0.0, 332, Color.BLUE));
    outerLabels.add(ImmutableTriple.of(-3.91, 350, Color.BLUE));
    outerLabels.add(ImmutableTriple.of(-5.235, 400, Color.BLUE));
    outerLabels.add(ImmutableTriple.of(-7.895, 450, Color.BLUE));
    outerLabels.add(ImmutableTriple.of(2.68, 270, Color.RED));
    outerLabels.add(ImmutableTriple.of(5.075, 350, Color.RED));
    outerLabels.add(ImmutableTriple.of(8.665, 400, Color.RED));

    BindableList<ImmutableTriple<ImmutablePair<String, Integer>, Double, Color>> dots =
        new BindableList<>();
    dots.add(ImmutableTriple.of(ImmutablePair.of("UT", 6), -24.02, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NE-03", 1), -21.31, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("WY", 3), -20.41, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("OK", 7), -17.27, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("ID", 4), -15.945, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("WV", 5), -13.42, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("AR", 6), -11.855, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("KY", 8), -11.345, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("AL", 9), -11.095, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NE-AL", 2), -10.885, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("KS", 6), -10.86, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("TN", 11), -10.20, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("ND", 3), -9.815, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("SD", 3), -9.01, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("LA", 8), -8.60, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NE-01", 1), -8.30, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("TX", 38), -7.895, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("AK", 3), -6.995, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MT", 3), -6.825, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MS", 6), -5.75, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("SC", 9), -5.235, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("IN", 11), -5.10, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MO", 10), -4.69, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("AZ", 11), -4.53, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("GA", 16), -3.91, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NE-02", 1), -3.575, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NC", 15), -1.02, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("FL", 29), +0.44, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("OH", 18), +1.49, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("VA", 13), +1.94, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("CO", 9), +2.68, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("PA", 20), +2.69, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NH", 4), +2.79, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("IA", 6), +2.905, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NV", 6), +3.34, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MN", 10), +3.845, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("ME-02", 1), +4.28, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("WI", 10), +4.47, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MI", 16), +4.75, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NM", 5), +5.075, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("OR", 7), +6.045, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("WA", 12), +7.435, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("ME-AL", 2), +7.645, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("IL", 20), +8.435, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("CT", 7), +8.665, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NJ", 14), +8.895, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("DE", 3), +9.315, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("ME-01", 1), +10.695, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("CA", 55), +11.56, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MA", 11), +11.57, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MD", 10), +13.035, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("RI", 4), +13.92, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NY", 29), +14.09, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("VT", 3), +17.80, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("HI", 4), +21.355, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("DC", 3), +46.815, Color.BLUE));

    SwingometerFrame frame = new SwingometerFrame();
    frame.setHeaderBinding(Binding.fixedBinding("2016 PRESIDENT SWINGOMETER"));
    frame.setRangeBinding(Binding.fixedBinding(10));
    frame.setValueBinding(Binding.fixedBinding(0.885));
    frame.setNumBucketsPerSideBinding(Binding.fixedBinding(20));
    frame.setLeftColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setRightColorBinding(Binding.fixedBinding(Color.RED));
    frame.setLeftToWinBinding(Binding.fixedBinding(-2.68));
    frame.setRightToWinBinding(Binding.fixedBinding(2.68));
    frame.setNumTicksBinding(Binding.sizeBinding(ticks));
    frame.setTickPositionBinding(IndexedBinding.propertyBinding(ticks, Function.identity()));
    frame.setTickTextBinding(IndexedBinding.propertyBinding(ticks, String::valueOf));
    frame.setNumOuterLabelsBinding(Binding.sizeBinding(outerLabels));
    frame.setOuterLabelPositionBinding(
        IndexedBinding.propertyBinding(outerLabels, ImmutableTriple::getLeft));
    frame.setOuterLabelTextBinding(
        IndexedBinding.propertyBinding(outerLabels, label -> label.getMiddle().toString()));
    frame.setOuterLabelColorBinding(
        IndexedBinding.propertyBinding(outerLabels, ImmutableTriple::getRight));
    frame.setNumDotsBinding(Binding.sizeBinding(dots));
    frame.setDotsPositionBinding(IndexedBinding.propertyBinding(dots, Triple::getMiddle));
    frame.setDotsColorBinding(IndexedBinding.propertyBinding(dots, Triple::getRight));
    frame.setDotsLabelBinding(IndexedBinding.propertyBinding(dots, e -> e.left.right.toString()));

    frame.setSize(1024, 512);
    compareRendering("SwingometerFrame", "Labels", frame);
  }

  @Test
  public void testRenderMultiLineLabels() throws IOException {
    BindableList<Integer> ticks =
        IntStream.rangeClosed(-9, 9).boxed().collect(Collectors.toCollection(BindableList::new));

    BindableList<ImmutableTriple<Double, Integer, Color>> outerLabels = new BindableList<>();
    outerLabels.add(ImmutableTriple.of(0.0, 332, Color.BLUE));
    outerLabels.add(ImmutableTriple.of(-3.91, 350, Color.BLUE));
    outerLabels.add(ImmutableTriple.of(-5.235, 400, Color.BLUE));
    outerLabels.add(ImmutableTriple.of(-7.895, 450, Color.BLUE));
    outerLabels.add(ImmutableTriple.of(2.68, 270, Color.RED));
    outerLabels.add(ImmutableTriple.of(5.075, 350, Color.RED));
    outerLabels.add(ImmutableTriple.of(8.665, 400, Color.RED));

    BindableList<ImmutableTriple<ImmutablePair<String, Integer>, Double, Color>> dots =
        new BindableList<>();
    dots.add(ImmutableTriple.of(ImmutablePair.of("UT", 6), -24.02, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NE-03", 1), -21.31, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("WY", 3), -20.41, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("OK", 7), -17.27, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("ID", 4), -15.945, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("WV", 5), -13.42, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("AR", 6), -11.855, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("KY", 8), -11.345, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("AL", 9), -11.095, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NE-AL", 2), -10.885, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("KS", 6), -10.86, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("TN", 11), -10.20, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("ND", 3), -9.815, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("SD", 3), -9.01, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("LA", 8), -8.60, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NE-01", 1), -8.30, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("TX", 38), -7.895, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("AK", 3), -6.995, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MT", 3), -6.825, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MS", 6), -5.75, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("SC", 9), -5.235, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("IN", 11), -5.10, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MO", 10), -4.69, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("AZ", 11), -4.53, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("GA", 16), -3.91, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NE-02", 1), -3.575, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NC", 15), -1.02, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("FL", 29), +0.44, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("OH", 18), +1.49, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("VA", 13), +1.94, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("CO", 9), +2.68, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("PA", 20), +2.69, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NH", 4), +2.79, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("IA", 6), +2.905, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NV", 6), +3.34, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MN", 10), +3.845, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("ME-02", 1), +4.28, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("WI", 10), +4.47, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MI", 16), +4.75, Color.RED));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NM", 5), +5.075, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("OR", 7), +6.045, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("WA", 12), +7.435, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("ME-AL", 2), +7.645, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("IL", 20), +8.435, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("CT", 7), +8.665, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NJ", 14), +8.895, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("DE", 3), +9.315, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("ME-01", 1), +10.695, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("CA", 55), +11.56, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MA", 11), +11.57, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("MD", 10), +13.035, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("RI", 4), +13.92, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("NY", 29), +14.09, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("VT", 3), +17.80, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("HI", 4), +21.355, Color.BLUE));
    dots.add(ImmutableTriple.of(ImmutablePair.of("DC", 3), +46.815, Color.BLUE));

    SwingometerFrame frame = new SwingometerFrame();
    frame.setHeaderBinding(Binding.fixedBinding("2016 PRESIDENT SWINGOMETER"));
    frame.setRangeBinding(Binding.fixedBinding(10));
    frame.setValueBinding(Binding.fixedBinding(0.885));
    frame.setNumBucketsPerSideBinding(Binding.fixedBinding(20));
    frame.setLeftColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setRightColorBinding(Binding.fixedBinding(Color.RED));
    frame.setLeftToWinBinding(Binding.fixedBinding(-2.68));
    frame.setRightToWinBinding(Binding.fixedBinding(2.68));
    frame.setNumTicksBinding(Binding.sizeBinding(ticks));
    frame.setTickPositionBinding(IndexedBinding.propertyBinding(ticks, Function.identity()));
    frame.setTickTextBinding(IndexedBinding.propertyBinding(ticks, String::valueOf));
    frame.setNumOuterLabelsBinding(Binding.sizeBinding(outerLabels));
    frame.setOuterLabelPositionBinding(
        IndexedBinding.propertyBinding(outerLabels, ImmutableTriple::getLeft));
    frame.setOuterLabelTextBinding(
        IndexedBinding.propertyBinding(outerLabels, label -> label.getMiddle().toString()));
    frame.setOuterLabelColorBinding(
        IndexedBinding.propertyBinding(outerLabels, ImmutableTriple::getRight));
    frame.setNumDotsBinding(Binding.sizeBinding(dots));
    frame.setDotsPositionBinding(IndexedBinding.propertyBinding(dots, Triple::getMiddle));
    frame.setDotsColorBinding(IndexedBinding.propertyBinding(dots, Triple::getRight));
    frame.setDotsLabelBinding(
        IndexedBinding.propertyBinding(
            dots,
            e -> {
              if (e.left.left.contains("-")) {
                return e.left.left.replaceAll("-", "\n-");
              }
              return e.left.left + "\n(" + e.left.right + ")";
            }));

    frame.setSize(1024, 512);
    compareRendering("SwingometerFrame", "MultiLineLabels", frame);
  }
}
