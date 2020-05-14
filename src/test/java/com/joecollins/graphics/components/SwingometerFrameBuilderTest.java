package com.joecollins.graphics.components;

import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.BindableList;
import com.joecollins.graphics.utils.BindableWrapper;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class SwingometerFrameBuilderTest {

  @Test
  public void testBasic() {
    BindableWrapper<Pair<Color, Color>> colors =
        new BindableWrapper<>(ImmutablePair.of(Color.BLUE, Color.RED));
    BindableWrapper<Double> value = new BindableWrapper<>(-1.0);
    SwingometerFrame frame =
        SwingometerFrameBuilder.basic(colors.getBinding(), value.getBinding()).build();
    assertEquals(Color.BLUE, frame.getLeftColor());
    assertEquals(Color.RED, frame.getRightColor());
    assertEquals(-1.0, frame.getValue().doubleValue(), 0);
  }

  @Test
  public void testMax() {
    BindableWrapper<Pair<Color, Color>> colors =
        new BindableWrapper<>(ImmutablePair.of(Color.BLUE, Color.RED));
    BindableWrapper<Double> value = new BindableWrapper<>(-1.0);
    BindableWrapper<Double> range = new BindableWrapper<>(10.0);
    BindableWrapper<Double> bucketSize = new BindableWrapper<>(0.5);
    SwingometerFrame frame =
        SwingometerFrameBuilder.basic(colors.getBinding(), value.getBinding())
            .withRange(range.getBinding())
            .withBucketSize(bucketSize.getBinding())
            .build();
    assertEquals(-1.0, frame.getValue().doubleValue(), 0.0);
    assertEquals(10.0, frame.getRange().doubleValue(), 0.0);
    assertEquals(20, frame.getNumBucketsPerSide());

    value.setValue(14.8);
    assertEquals(14.8, frame.getValue().doubleValue(), 0.0);
    assertEquals(15.0, frame.getRange().doubleValue(), 0.0);
    assertEquals(30, frame.getNumBucketsPerSide());

    value.setValue(-11.2);
    assertEquals(-11.2, frame.getValue().doubleValue(), 0.0);
    assertEquals(11.5, frame.getRange().doubleValue(), 0.0);
    assertEquals(23, frame.getNumBucketsPerSide());

    value.setValue(2.7);
    assertEquals(2.7, frame.getValue().doubleValue(), 0.0);
    assertEquals(10.0, frame.getRange().doubleValue(), 0.0);
    assertEquals(20, frame.getNumBucketsPerSide());

    value.setValue(-3.4);
    assertEquals(-3.4, frame.getValue().doubleValue(), 0.0);
    assertEquals(10.0, frame.getRange().doubleValue(), 0.0);
    assertEquals(20, frame.getNumBucketsPerSide());
  }

  @Test
  public void testTicks() {
    BindableWrapper<Pair<Color, Color>> colors =
        new BindableWrapper<>(ImmutablePair.of(Color.BLUE, Color.RED));
    BindableWrapper<Double> value = new BindableWrapper<>(-1.0);
    BindableWrapper<Double> range = new BindableWrapper<>(10.0);
    BindableWrapper<Double> tickInterval = new BindableWrapper<>(1.0);
    SwingometerFrame frame =
        SwingometerFrameBuilder.basic(colors.getBinding(), value.getBinding())
            .withRange(range.getBinding())
            .withTickInterval(tickInterval.getBinding(), new DecimalFormat("0")::format)
            .build();

    assertEquals(19, frame.getNumTicks());
    Map<Number, String> ticks =
        IntStream.range(0, frame.getNumTicks())
            .boxed()
            .collect(Collectors.toMap(frame::getTickPosition, frame::getTickText));
    assertEquals("9", ticks.get(-9.0));
    assertEquals("0", ticks.get(0.0));
    assertEquals("5", ticks.get(5.0));

    value.setValue(11.3);
    assertEquals(23, frame.getNumTicks());
  }

  @Test
  public void testNeededToWin() {
    BindableWrapper<Pair<Color, Color>> colors =
        new BindableWrapper<>(ImmutablePair.of(Color.BLUE, Color.RED));
    BindableWrapper<Double> value = new BindableWrapper<>(-1.0);
    BindableWrapper<Double> leftToWin = new BindableWrapper<>(-2.0);
    BindableWrapper<Double> rightToWin = new BindableWrapper<>(4.0);
    SwingometerFrame frame =
        SwingometerFrameBuilder.basic(colors.getBinding(), value.getBinding())
            .withLeftNeedingToWin(leftToWin.getBinding())
            .withRightNeedingToWin(rightToWin.getBinding())
            .build();
    assertEquals(-2.0, frame.getLeftToWin().doubleValue(), 0);
    assertEquals(4.0, frame.getRightToWin().doubleValue(), 0);
  }

  @Test
  public void testOuterLabels() {
    class OuterLabel {
      private final Color color;
      private final String label;
      private final double position;

      OuterLabel(Color color, String label, double position) {
        this.color = color;
        this.label = label;
        this.position = position;
      }
    }
    BindableList<OuterLabel> labels = new BindableList<>();
    labels.add(new OuterLabel(Color.RED, "306", 0.0));
    labels.add(new OuterLabel(Color.RED, "350", 2.66));
    labels.add(new OuterLabel(Color.RED, "400", 7.855));
    labels.add(new OuterLabel(Color.RED, "450", 11.245));
    labels.add(new OuterLabel(Color.RED, "500", 15.055));
    labels.add(new OuterLabel(Color.BLUE, "270", -0.385));
    labels.add(new OuterLabel(Color.BLUE, "350", -2.565));
    labels.add(new OuterLabel(Color.BLUE, "400", -4.495));
    labels.add(new OuterLabel(Color.BLUE, "450", -9.455));
    labels.add(new OuterLabel(Color.BLUE, "500", -13.86));
    BindableWrapper<Pair<Color, Color>> colors =
        new BindableWrapper<>(ImmutablePair.of(Color.BLUE, Color.RED));
    BindableWrapper<Double> value = new BindableWrapper<>(-1.0);
    SwingometerFrame frame =
        SwingometerFrameBuilder.basic(colors.getBinding(), value.getBinding())
            .withOuterLabels(labels, l -> l.position, l -> l.label, l -> l.color)
            .build();
    assertEquals(10, frame.getNumOuterLabels());
    assertEquals(Color.RED, frame.getOuterLabelColor(0));
    assertEquals("350", frame.getOuterLabelText(1));
    assertEquals(7.855, frame.getOuterLabelPosition(2).doubleValue(), 1e-6);
    assertEquals(Color.BLUE, frame.getOuterLabelColor(9));
  }

  @Test
  public void testDotsWithoutLabels() {
    class Dot {
      private final double position;
      private final Color color;

      Dot(double position, Color color) {
        this.position = position;
        this.color = color;
      }
    }
    BindableList<Dot> dots = new BindableList<>();
    dots.add(new Dot(0.115, Color.RED));
    dots.add(new Dot(0.36, Color.RED));
    dots.add(new Dot(0.385, Color.RED));
    dots.add(new Dot(0.6, Color.RED));
    dots.add(new Dot(-0.185, Color.BLUE));
    dots.add(new Dot(-0.76, Color.BLUE));
    dots.add(new Dot(-0.76, Color.BLUE));
    BindableWrapper<Pair<Color, Color>> colors =
        new BindableWrapper<>(ImmutablePair.of(Color.BLUE, Color.RED));
    BindableWrapper<Double> value = new BindableWrapper<>(-1.0);
    SwingometerFrame frame =
        SwingometerFrameBuilder.basic(colors.getBinding(), value.getBinding())
            .withDots(dots, d -> d.position, d -> d.color)
            .build();
    assertEquals(7, frame.getNumDots());
    assertEquals(0.115, frame.getDotPosition(0).doubleValue(), 1e-6);
    assertEquals(Color.RED, frame.getDotColor(1));
    assertEquals("", frame.getDotLabel(2));
  }

  @Test
  public void testDotsWithLabels() {
    class Dot {
      private final double position;
      private final Color color;
      private final String label;

      Dot(double position, Color color, String label) {
        this.position = position;
        this.color = color;
        this.label = label;
      }
    }
    BindableList<Dot> dots = new BindableList<>();
    dots.add(new Dot(0.115, Color.RED, "16"));
    dots.add(new Dot(0.36, Color.RED, "20"));
    dots.add(new Dot(0.385, Color.RED, "10"));
    dots.add(new Dot(0.6, Color.RED, "29"));
    dots.add(new Dot(-0.185, Color.BLUE, "4"));
    dots.add(new Dot(-0.76, Color.BLUE, "10"));
    dots.add(new Dot(-0.76, Color.BLUE, "6"));
    BindableWrapper<Pair<Color, Color>> colors =
        new BindableWrapper<>(ImmutablePair.of(Color.BLUE, Color.RED));
    BindableWrapper<Double> value = new BindableWrapper<>(-1.0);
    SwingometerFrame frame =
        SwingometerFrameBuilder.basic(colors.getBinding(), value.getBinding())
            .withDots(dots, d -> d.position, d -> d.color, d -> d.label)
            .build();
    assertEquals(7, frame.getNumDots());
    assertEquals(0.115, frame.getDotPosition(0).doubleValue(), 1e-6);
    assertEquals(Color.RED, frame.getDotColor(1));
    assertEquals("10", frame.getDotLabel(2));
  }
}
