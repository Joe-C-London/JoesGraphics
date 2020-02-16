package com.joecollins.graphics.components;

import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.Binding;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class BarFrameBuilderTest {

  private enum BindableWrapperValue {
    VALUE
  }

  private static class BindableWrapper<T> extends Bindable {
    private T value;

    T getValue() {
      return value;
    }

    void setValue(T value) {
      this.value = value;
      onPropertyRefreshed(BindableWrapperValue.VALUE);
    }

    Binding<T> getBinding() {
      return Binding.propertyBinding(this, BindableWrapper::getValue, BindableWrapperValue.VALUE);
    }
  }

  @Test
  public void testSimpleBars() {
    BindableWrapper<Map<Pair<String, Color>, Integer>> result = new BindableWrapper<>();
    BarFrame frame =
        BarFrameBuilder.basic(
                result.getBinding(), Pair::getLeft, Pair::getRight, new DecimalFormat("#,##0"))
            .build();
    assertEquals(0, frame.getNumBars());
    assertEquals(0, frame.getNumLines());

    result.setValue(
        Map.of(
            ImmutablePair.of("CLINTON", Color.ORANGE),
            2842,
            ImmutablePair.of("SANDERS", Color.GREEN),
            1865));
    assertEquals(2, frame.getNumBars());
    assertEquals("CLINTON", frame.getLeftText(0));
    assertEquals("SANDERS", frame.getLeftText(1));
    assertEquals("2,842", frame.getRightText(0));
    assertEquals("1,865", frame.getRightText(1));
    assertEquals(Color.ORANGE, frame.getSeries(0).get(0).getLeft());
    assertEquals(Color.GREEN, frame.getSeries(1).get(0).getLeft());
    assertEquals(2842, frame.getSeries(0).get(0).getRight());
    assertEquals(1865, frame.getSeries(1).get(0).getRight());

    assertEquals(0, frame.getMin().intValue());
    assertEquals(2842, frame.getMax().intValue());
  }

  @Test
  public void testSimpleBarsRange() {
    BindableWrapper<Map<Pair<String, Color>, Integer>> result = new BindableWrapper<>();
    BindableWrapper<Integer> max = new BindableWrapper<>();
    max.setValue(2500);
    BarFrame frame =
        BarFrameBuilder.basic(
                result.getBinding(), Pair::getLeft, Pair::getRight, new DecimalFormat("#,##0"))
            .withMax(max.getBinding())
            .build();
    assertEquals(0, frame.getMin().intValue());
    assertEquals(2500, frame.getMax().intValue());

    result.setValue(
        Map.of(
            ImmutablePair.of("CLINTON", Color.ORANGE),
            2205,
            ImmutablePair.of("SANDERS", Color.GREEN),
            1846));
    assertEquals(0, frame.getMin().intValue());
    assertEquals(2500, frame.getMax().intValue());

    result.setValue(
        Map.of(
            ImmutablePair.of("CLINTON", Color.ORANGE),
            2842,
            ImmutablePair.of("SANDERS", Color.GREEN),
            1865));
    assertEquals(0, frame.getMin().intValue());
    assertEquals(2842, frame.getMax().intValue());

    max.setValue(3000);
    assertEquals(0, frame.getMin().intValue());
    assertEquals(3000, frame.getMax().intValue());

    max.setValue(2500);
    assertEquals(0, frame.getMin().intValue());
    assertEquals(2842, frame.getMax().intValue());
  }

  @Test
  public void testHeaderSubheadAndNotes() {
    BindableWrapper<Map<Pair<String, Color>, Integer>> result = new BindableWrapper<>();
    BindableWrapper<String> header = new BindableWrapper<>();
    header.setValue("HEADER");
    BindableWrapper<String> subhead = new BindableWrapper<>();
    subhead.setValue("SUBHEAD");
    BindableWrapper<String> notes = new BindableWrapper<>();
    notes.setValue("NOTES");
    BindableWrapper<Color> borderColor = new BindableWrapper<>();
    borderColor.setValue(Color.BLACK);
    BindableWrapper<Color> subheadColor = new BindableWrapper<>();
    subheadColor.setValue(Color.GRAY);
    DecimalFormat formatter = new DecimalFormat("#,##0");
    BarFrame frame =
        BarFrameBuilder.basic(result.getBinding(), Pair::getLeft, Pair::getRight, formatter)
            .withHeader(header.getBinding())
            .withSubhead(subhead.getBinding())
            .withNotes(notes.getBinding())
            .withBorder(borderColor.getBinding())
            .withSubheadColor(subheadColor.getBinding())
            .build();
    assertEquals("HEADER", frame.getHeader());
    assertEquals("SUBHEAD", frame.getSubheadText());
    assertEquals("NOTES", frame.getNotes());
    assertEquals(Color.BLACK, frame.getBorderColor());
    assertEquals(Color.GRAY, frame.getSubheadColor());

    header.setValue("DEMOCRATIC PRIMARY");
    assertEquals("DEMOCRATIC PRIMARY", frame.getHeader());

    subhead.setValue("PLEDGED DELEGATES");
    assertEquals("PLEDGED DELEGATES", frame.getSubheadText());

    notes.setValue("SOURCE: DNC");
    assertEquals("SOURCE: DNC", frame.getNotes());

    borderColor.setValue(Color.BLUE);
    assertEquals(Color.BLUE, frame.getBorderColor());

    subheadColor.setValue(Color.BLUE);
    assertEquals(Color.BLUE, frame.getSubheadColor());
  }

  @Test
  public void testTarget() {
    BindableWrapper<Map<Pair<String, Color>, Integer>> result = new BindableWrapper<>();
    BindableWrapper<Integer> target = new BindableWrapper<>();
    target.setValue(2382);
    DecimalFormat formatter = new DecimalFormat("#,##0");
    BarFrame frame =
        BarFrameBuilder.basic(result.getBinding(), Pair::getLeft, Pair::getRight, formatter)
            .withTarget(target.getBinding(), t -> formatter.format(t) + " TO WIN")
            .build();
    assertEquals(1, frame.getNumLines());
    assertEquals(2382, frame.getLineLevel(0));
    assertEquals("2,382 TO WIN", frame.getLineLabel(0));
  }
}
