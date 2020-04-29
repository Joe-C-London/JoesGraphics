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
}
