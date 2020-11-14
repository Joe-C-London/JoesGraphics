package com.joecollins.graphics.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.joecollins.bindings.BindableList;
import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.graphics.utils.ColorUtils;
import java.awt.Color;
import java.awt.geom.Rectangle2D.Double;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

public class BarFrameBuilderTest {

  private static final DecimalFormat THOUSANDS = new DecimalFormat("#,##0");
  private static final DecimalFormat DIFF = new DecimalFormat("+0;-0");
  private static final DecimalFormat PCT = new DecimalFormat("0.0%");

  private static class Wrapper<T> {
    final T value;

    private Wrapper(T value) {
      this.value = value;
    }
  }

  @Test
  public void testSimpleBars() {
    BindableWrapper<Map<Pair<String, Color>, Integer>> result = new BindableWrapper<>();
    BarFrame frame =
        BarFrameBuilder.basic(
                result
                    .getBinding()
                    .mapNonNull(
                        map ->
                            map.entrySet().stream()
                                .sorted(
                                    Map.Entry.<Pair<String, Color>, Integer>comparingByValue()
                                        .reversed())
                                .map(
                                    e ->
                                        new BarFrameBuilder.BasicBar(
                                            e.getKey().getLeft(),
                                            e.getKey().getValue(),
                                            e.getValue(),
                                            THOUSANDS.format(e.getValue())))
                                .collect(Collectors.toList())))
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
  public void testSimpleBarsWithValueObject() {
    BindableWrapper<Map<Pair<String, Color>, Wrapper<Integer>>> result = new BindableWrapper<>();
    BarFrame frame =
        BarFrameBuilder.basic(
                result
                    .getBinding()
                    .mapNonNull(
                        map ->
                            map.entrySet().stream()
                                .sorted(
                                    Comparator
                                        .<Map.Entry<Pair<String, Color>, Wrapper<Integer>>>
                                            comparingInt(e -> e.getValue().value)
                                        .reversed())
                                .map(
                                    e ->
                                        new BarFrameBuilder.BasicBar(
                                            e.getKey().getLeft(),
                                            e.getKey().getRight(),
                                            e.getValue().value,
                                            THOUSANDS.format(e.getValue().value)))
                                .collect(Collectors.toList())))
            .build();
    assertEquals(0, frame.getNumBars());
    assertEquals(0, frame.getNumLines());

    result.setValue(
        Map.of(
            ImmutablePair.of("CLINTON", Color.ORANGE),
            new Wrapper<>(2842),
            ImmutablePair.of("SANDERS", Color.GREEN),
            new Wrapper<>(1865)));
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
                result
                    .getBinding()
                    .mapNonNull(
                        map ->
                            map.entrySet().stream()
                                .sorted(
                                    Map.Entry.<Pair<String, Color>, Integer>comparingByValue()
                                        .reversed())
                                .map(
                                    e ->
                                        new BarFrameBuilder.BasicBar(
                                            e.getKey().getLeft(),
                                            e.getKey().getRight(),
                                            e.getValue(),
                                            THOUSANDS.format(e.getValue())))
                                .collect(Collectors.toList())))
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
    BarFrame frame =
        BarFrameBuilder.basic(
                result
                    .getBinding()
                    .mapNonNull(
                        map ->
                            map.entrySet().stream()
                                .sorted(
                                    Map.Entry.<Pair<String, Color>, Integer>comparingByValue()
                                        .reversed())
                                .map(
                                    e ->
                                        new BarFrameBuilder.BasicBar(
                                            e.getKey().getLeft(),
                                            e.getKey().getRight(),
                                            e.getValue(),
                                            THOUSANDS.format(e.getValue())))
                                .collect(Collectors.toList())))
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
    BarFrame frame =
        BarFrameBuilder.basic(
                result
                    .getBinding()
                    .mapNonNull(
                        map ->
                            map.entrySet().stream()
                                .sorted(
                                    Map.Entry.<Pair<String, Color>, Integer>comparingByValue()
                                        .reversed())
                                .map(
                                    e ->
                                        new BarFrameBuilder.BasicBar(
                                            e.getKey().getLeft(),
                                            e.getKey().getRight(),
                                            e.getValue(),
                                            THOUSANDS.format(e.getValue())))
                                .collect(Collectors.toList())))
            .withTarget(target.getBinding(), t -> THOUSANDS.format(t) + " TO WIN")
            .build();
    assertEquals(1, frame.getNumLines());
    assertEquals(2382, frame.getLineLevel(0));
    assertEquals("2,382 TO WIN", frame.getLineLabel(0));
  }

  @Test
  public void testMultiLines() {
    BindableWrapper<Map<Pair<String, Color>, Integer>> result = new BindableWrapper<>();
    BindableList<Integer> lines = new BindableList<>();
    BarFrame frame =
        BarFrameBuilder.basic(
                result
                    .getBinding()
                    .mapNonNull(
                        map ->
                            map.entrySet().stream()
                                .sorted(
                                    Map.Entry.<Pair<String, Color>, Integer>comparingByValue()
                                        .reversed())
                                .map(
                                    e ->
                                        new BarFrameBuilder.BasicBar(
                                            e.getKey().getLeft(),
                                            e.getKey().getRight(),
                                            e.getValue(),
                                            THOUSANDS.format(e.getValue())))
                                .collect(Collectors.toList())))
            .withLines(lines, t -> t + " QUOTA" + (t == 1 ? "" : "S"))
            .build();
    assertEquals(0, frame.getNumLines());

    lines.addAll(Arrays.asList(1, 2, 3, 4, 5));
    assertEquals(5, frame.getNumLines());

    assertEquals(1, frame.getLineLevel(0));
    assertEquals(2, frame.getLineLevel(1));
    assertEquals(3, frame.getLineLevel(2));
    assertEquals(4, frame.getLineLevel(3));
    assertEquals(5, frame.getLineLevel(4));

    assertEquals("1 QUOTA", frame.getLineLabel(0));
    assertEquals("2 QUOTAS", frame.getLineLabel(1));
    assertEquals("3 QUOTAS", frame.getLineLabel(2));
    assertEquals("4 QUOTAS", frame.getLineLabel(3));
    assertEquals("5 QUOTAS", frame.getLineLabel(4));
  }

  @Test
  public void testMultiLinesBinding() {
    BindableWrapper<Map<Pair<String, Color>, Integer>> result = new BindableWrapper<>();
    BindableWrapper<List<Integer>> lines = new BindableWrapper<>();
    lines.setValue(List.of());
    BarFrame frame =
        BarFrameBuilder.basic(
                result
                    .getBinding()
                    .mapNonNull(
                        map ->
                            map.entrySet().stream()
                                .sorted(
                                    Map.Entry.<Pair<String, Color>, Integer>comparingByValue()
                                        .reversed())
                                .map(
                                    e ->
                                        new BarFrameBuilder.BasicBar(
                                            e.getKey().getLeft(),
                                            e.getKey().getRight(),
                                            e.getValue(),
                                            THOUSANDS.format(e.getValue())))
                                .collect(Collectors.toList())))
            .withLines(lines.getBinding(), t -> t + " QUOTA" + (t == 1 ? "" : "S"))
            .build();
    assertEquals(0, frame.getNumLines());

    lines.setValue(Arrays.asList(1, 2, 3, 4, 5));
    assertEquals(5, frame.getNumLines());

    assertEquals(1, frame.getLineLevel(0));
    assertEquals(2, frame.getLineLevel(1));
    assertEquals(3, frame.getLineLevel(2));
    assertEquals(4, frame.getLineLevel(3));
    assertEquals(5, frame.getLineLevel(4));

    assertEquals("1 QUOTA", frame.getLineLabel(0));
    assertEquals("2 QUOTAS", frame.getLineLabel(1));
    assertEquals("3 QUOTAS", frame.getLineLabel(2));
    assertEquals("4 QUOTAS", frame.getLineLabel(3));
    assertEquals("5 QUOTAS", frame.getLineLabel(4));
  }

  @Test
  public void testLeftShape() {
    BindableWrapper<Map<Pair<String, Color>, Pair<Integer, Boolean>>> result =
        new BindableWrapper<>();
    Double shape = new Double(0, 0, 1, 1);
    BarFrame frame =
        BarFrameBuilder.basic(
                result
                    .getBinding()
                    .mapNonNull(
                        map ->
                            map.entrySet().stream()
                                .sorted(
                                    Comparator
                                        .<Map.Entry<Pair<String, Color>, Pair<Integer, Boolean>>>
                                            comparingInt(e -> e.getValue().getLeft())
                                        .reversed())
                                .map(
                                    e ->
                                        new BarFrameBuilder.BasicBar(
                                            e.getKey().getLeft(),
                                            e.getKey().getRight(),
                                            e.getValue().getLeft(),
                                            THOUSANDS.format(e.getValue().getLeft()),
                                            e.getValue().getRight() ? shape : null))
                                .collect(Collectors.toList())))
            .build();
    assertEquals(0, frame.getNumBars());
    assertEquals(0, frame.getNumLines());

    result.setValue(
        Map.of(
            ImmutablePair.of("CLINTON", Color.ORANGE),
            ImmutablePair.of(2842, true),
            ImmutablePair.of("SANDERS", Color.GREEN),
            ImmutablePair.of(1865, false)));
    assertEquals(2, frame.getNumBars());
    assertEquals("CLINTON", frame.getLeftText(0));
    assertEquals("SANDERS", frame.getLeftText(1));
    assertEquals("2,842", frame.getRightText(0));
    assertEquals("1,865", frame.getRightText(1));
    assertEquals(Color.ORANGE, frame.getSeries(0).get(0).getLeft());
    assertEquals(Color.GREEN, frame.getSeries(1).get(0).getLeft());
    assertEquals(2842, frame.getSeries(0).get(0).getRight());
    assertEquals(1865, frame.getSeries(1).get(0).getRight());
    assertEquals(shape, frame.getLeftIcon(0));
    assertNull(frame.getLeftIcon(1));
  }

  @Test
  public void testSimpleDiffBars() {
    BindableWrapper<Map<Pair<String, Color>, Pair<Integer, Integer>>> result =
        new BindableWrapper<>();
    BarFrame frame =
        BarFrameBuilder.basic(
                result
                    .getBinding()
                    .mapNonNull(
                        map ->
                            map.entrySet().stream()
                                .sorted(
                                    Comparator
                                        .<Map.Entry<Pair<String, Color>, Pair<Integer, Integer>>>
                                            comparingInt(e -> e.getValue().getLeft())
                                        .reversed())
                                .map(
                                    e ->
                                        new BarFrameBuilder.BasicBar(
                                            e.getKey().getLeft(),
                                            e.getKey().getRight(),
                                            e.getValue().getRight(),
                                            DIFF.format(e.getValue().getRight())))
                                .collect(Collectors.toList())))
            .build();
    assertEquals(0, frame.getNumBars());
    assertEquals(0, frame.getNumLines());

    result.setValue(
        Map.of(
            ImmutablePair.of("LIB", Color.RED),
            ImmutablePair.of(157, -27),
            ImmutablePair.of("CON", Color.BLUE),
            ImmutablePair.of(121, +22),
            ImmutablePair.of("NDP", Color.ORANGE),
            ImmutablePair.of(24, -20),
            ImmutablePair.of("BQ", Color.CYAN),
            ImmutablePair.of(32, +22),
            ImmutablePair.of("GRN", Color.GREEN),
            ImmutablePair.of(3, +2),
            ImmutablePair.of("IND", Color.GRAY),
            ImmutablePair.of(1, +1)));
    assertEquals(6, frame.getNumBars());

    assertEquals("LIB", frame.getLeftText(0));
    assertEquals("CON", frame.getLeftText(1));
    assertEquals("BQ", frame.getLeftText(2));
    assertEquals("NDP", frame.getLeftText(3));
    assertEquals("GRN", frame.getLeftText(4));
    assertEquals("IND", frame.getLeftText(5));

    assertEquals("-27", frame.getRightText(0));
    assertEquals("+22", frame.getRightText(1));
    assertEquals("+22", frame.getRightText(2));
    assertEquals("-20", frame.getRightText(3));
    assertEquals("+2", frame.getRightText(4));
    assertEquals("+1", frame.getRightText(5));

    assertEquals(Color.RED, frame.getSeries(0).get(0).getLeft());
    assertEquals(Color.BLUE, frame.getSeries(1).get(0).getLeft());
    assertEquals(Color.CYAN, frame.getSeries(2).get(0).getLeft());
    assertEquals(Color.ORANGE, frame.getSeries(3).get(0).getLeft());
    assertEquals(Color.GREEN, frame.getSeries(4).get(0).getLeft());
    assertEquals(Color.GRAY, frame.getSeries(5).get(0).getLeft());

    assertEquals(-27, frame.getSeries(0).get(0).getRight());
    assertEquals(+22, frame.getSeries(1).get(0).getRight());
    assertEquals(+22, frame.getSeries(2).get(0).getRight());
    assertEquals(-20, frame.getSeries(3).get(0).getRight());
    assertEquals(+2, frame.getSeries(4).get(0).getRight());
    assertEquals(+1, frame.getSeries(5).get(0).getRight());

    assertEquals(-27, frame.getMin().intValue());
    assertEquals(+22, frame.getMax().intValue());
  }

  @Test
  public void testSimpleDiffWingspan() {
    BindableWrapper<Map<Pair<String, Color>, Pair<Integer, Integer>>> result =
        new BindableWrapper<>();
    BindableWrapper<Integer> range = new BindableWrapper<>();
    range.setValue(10);
    BarFrame frame =
        BarFrameBuilder.basic(
                result
                    .getBinding()
                    .mapNonNull(
                        map ->
                            map.entrySet().stream()
                                .sorted(
                                    Comparator
                                        .<Map.Entry<Pair<String, Color>, Pair<Integer, Integer>>>
                                            comparingInt(e -> e.getValue().getLeft())
                                        .reversed())
                                .map(
                                    e ->
                                        new BarFrameBuilder.BasicBar(
                                            e.getKey().getLeft(),
                                            e.getKey().getRight(),
                                            e.getValue().getRight(),
                                            DIFF.format(e.getValue().getRight())))
                                .collect(Collectors.toList())))
            .withWingspan(range.getBinding())
            .build();
    assertEquals(-10, frame.getMin().intValue());
    assertEquals(+10, frame.getMax().intValue());

    result.setValue(
        Map.of(
            ImmutablePair.of("LIB", Color.RED),
            ImmutablePair.of(157, -27),
            ImmutablePair.of("CON", Color.BLUE),
            ImmutablePair.of(121, +22),
            ImmutablePair.of("NDP", Color.ORANGE),
            ImmutablePair.of(24, -20),
            ImmutablePair.of("BQ", Color.CYAN),
            ImmutablePair.of(32, +22),
            ImmutablePair.of("GRN", Color.GREEN),
            ImmutablePair.of(3, +2),
            ImmutablePair.of("IND", Color.GRAY),
            ImmutablePair.of(1, +1)));

    assertEquals(-27, frame.getMin().intValue());
    assertEquals(+27, frame.getMax().intValue());
  }

  @Test
  public void testDualValueBars() {
    BindableWrapper<Map<Pair<String, Color>, Pair<Integer, Integer>>> result =
        new BindableWrapper<>();
    BarFrame frame =
        BarFrameBuilder.dual(
                result
                    .getBinding()
                    .mapNonNull(
                        map ->
                            map.entrySet().stream()
                                .sorted(
                                    Comparator
                                        .<Map.Entry<Pair<String, Color>, Pair<Integer, Integer>>>
                                            comparingInt(e -> e.getValue().getRight())
                                        .reversed())
                                .map(
                                    e ->
                                        new BarFrameBuilder.DualBar(
                                            e.getKey().getLeft(),
                                            e.getKey().getRight(),
                                            e.getValue().getLeft(),
                                            e.getValue().getRight(),
                                            e.getValue().getLeft() + "/" + e.getValue().getRight()))
                                .collect(Collectors.toList())))
            .build();
    assertEquals(0, frame.getNumBars());
    assertEquals(0, frame.getNumLines());

    result.setValue(
        Map.of(
            ImmutablePair.of("LIBERAL", Color.RED),
            ImmutablePair.of(26, 157),
            ImmutablePair.of("CONSERVATIVE", Color.BLUE),
            ImmutablePair.of(4, 121),
            ImmutablePair.of("NEW DEMOCRATIC PARTY", Color.ORANGE),
            ImmutablePair.of(1, 24),
            ImmutablePair.of("BLOC QU\u00c9B\u00c9COIS", Color.CYAN),
            ImmutablePair.of(0, 32),
            ImmutablePair.of("GREEN", Color.GREEN),
            ImmutablePair.of(1, 3),
            ImmutablePair.of("INDEPENDENT", Color.GRAY),
            ImmutablePair.of(0, 1)));
    assertEquals(6, frame.getNumBars());

    assertEquals("LIBERAL", frame.getLeftText(0));
    assertEquals("CONSERVATIVE", frame.getLeftText(1));
    assertEquals("BLOC QU\u00c9B\u00c9COIS", frame.getLeftText(2));
    assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(3));
    assertEquals("GREEN", frame.getLeftText(4));
    assertEquals("INDEPENDENT", frame.getLeftText(5));

    assertEquals("26/157", frame.getRightText(0));
    assertEquals("4/121", frame.getRightText(1));
    assertEquals("0/32", frame.getRightText(2));
    assertEquals("1/24", frame.getRightText(3));
    assertEquals("1/3", frame.getRightText(4));
    assertEquals("0/1", frame.getRightText(5));

    assertEquals(0, frame.getSeries(0).get(0).getRight().intValue());
    assertEquals(0, frame.getSeries(1).get(0).getRight().intValue());
    assertEquals(0, frame.getSeries(2).get(0).getRight().intValue());
    assertEquals(0, frame.getSeries(3).get(0).getRight().intValue());
    assertEquals(0, frame.getSeries(4).get(0).getRight().intValue());
    assertEquals(0, frame.getSeries(5).get(0).getRight().intValue());

    assertEquals(Color.RED, frame.getSeries(0).get(1).getLeft());
    assertEquals(Color.BLUE, frame.getSeries(1).get(1).getLeft());
    assertEquals(Color.CYAN, frame.getSeries(2).get(1).getLeft());
    assertEquals(Color.ORANGE, frame.getSeries(3).get(1).getLeft());
    assertEquals(Color.GREEN, frame.getSeries(4).get(1).getLeft());
    assertEquals(Color.GRAY, frame.getSeries(5).get(1).getLeft());

    assertEquals(26, frame.getSeries(0).get(1).getRight().intValue());
    assertEquals(4, frame.getSeries(1).get(1).getRight().intValue());
    assertEquals(0, frame.getSeries(2).get(1).getRight().intValue());
    assertEquals(1, frame.getSeries(3).get(1).getRight().intValue());
    assertEquals(1, frame.getSeries(4).get(1).getRight().intValue());
    assertEquals(0, frame.getSeries(5).get(1).getRight().intValue());

    assertEquals(lighten(Color.RED), frame.getSeries(0).get(2).getLeft());
    assertEquals(lighten(Color.BLUE), frame.getSeries(1).get(2).getLeft());
    assertEquals(lighten(Color.CYAN), frame.getSeries(2).get(2).getLeft());
    assertEquals(lighten(Color.ORANGE), frame.getSeries(3).get(2).getLeft());
    assertEquals(lighten(Color.GREEN), frame.getSeries(4).get(2).getLeft());
    assertEquals(lighten(Color.GRAY), frame.getSeries(5).get(2).getLeft());

    assertEquals(157 - 26, frame.getSeries(0).get(2).getRight().intValue());
    assertEquals(121 - 4, frame.getSeries(1).get(2).getRight().intValue());
    assertEquals(32 - 0, frame.getSeries(2).get(2).getRight().intValue());
    assertEquals(24 - 1, frame.getSeries(3).get(2).getRight().intValue());
    assertEquals(3 - 1, frame.getSeries(4).get(2).getRight().intValue());
    assertEquals(1 - 0, frame.getSeries(5).get(2).getRight().intValue());

    assertEquals(0, frame.getMin().intValue());
    assertEquals(157, frame.getMax().intValue());
  }

  @Test
  public void testDualChangeBars() {
    BindableWrapper<Map<Pair<String, Color>, Triple<Integer, Integer, Integer>>> result =
        new BindableWrapper<>();
    BarFrame frame =
        BarFrameBuilder.dual(
                result
                    .getBinding()
                    .mapNonNull(
                        map ->
                            map.entrySet().stream()
                                .sorted(
                                    Comparator
                                        .<Map.Entry<
                                                Pair<String, Color>,
                                                Triple<Integer, Integer, Integer>>>
                                            comparingInt(e -> e.getValue().getRight())
                                        .reversed())
                                .map(
                                    e ->
                                        new BarFrameBuilder.DualBar(
                                            e.getKey().getLeft(),
                                            e.getKey().getRight(),
                                            e.getValue().getLeft(),
                                            e.getValue().getMiddle(),
                                            DIFF.format(e.getValue().getLeft())
                                                + "/"
                                                + DIFF.format(e.getValue().getMiddle())))
                                .collect(Collectors.toList())))
            .build();
    assertEquals(0, frame.getNumBars());
    assertEquals(0, frame.getNumLines());

    result.setValue(
        Map.of(
            ImmutablePair.of("LIB", Color.RED),
            ImmutableTriple.of(-6, -27, 157),
            ImmutablePair.of("CON", Color.BLUE),
            ImmutableTriple.of(+4, +22, 121),
            ImmutablePair.of("NDP", Color.ORANGE),
            ImmutableTriple.of(+1, -20, 24),
            ImmutablePair.of("BQ", Color.CYAN),
            ImmutableTriple.of(0, +22, 32),
            ImmutablePair.of("GRN", Color.GREEN),
            ImmutableTriple.of(+1, +2, 3),
            ImmutablePair.of("IND", Color.GRAY),
            ImmutableTriple.of(0, +1, 1)));
    assertEquals(6, frame.getNumBars());

    assertEquals("LIB", frame.getLeftText(0));
    assertEquals("CON", frame.getLeftText(1));
    assertEquals("BQ", frame.getLeftText(2));
    assertEquals("NDP", frame.getLeftText(3));
    assertEquals("GRN", frame.getLeftText(4));
    assertEquals("IND", frame.getLeftText(5));

    assertEquals("-6/-27", frame.getRightText(0));
    assertEquals("+4/+22", frame.getRightText(1));
    assertEquals("+0/+22", frame.getRightText(2));
    assertEquals("+1/-20", frame.getRightText(3));
    assertEquals("+1/+2", frame.getRightText(4));
    assertEquals("+0/+1", frame.getRightText(5));

    assertEquals(Color.RED, frame.getSeries(0).get(0).getLeft());
    assertEquals(Color.BLUE, frame.getSeries(1).get(0).getLeft());
    assertEquals(Color.CYAN, frame.getSeries(2).get(0).getLeft());
    assertEquals(Color.ORANGE, frame.getSeries(3).get(0).getLeft());
    assertEquals(Color.GREEN, frame.getSeries(4).get(0).getLeft());
    assertEquals(Color.GRAY, frame.getSeries(5).get(0).getLeft());

    assertEquals(0, frame.getSeries(0).get(0).getRight().intValue());
    assertEquals(0, frame.getSeries(1).get(0).getRight().intValue());
    assertEquals(0, frame.getSeries(2).get(0).getRight().intValue());
    assertEquals(0, frame.getSeries(3).get(0).getRight().intValue());
    assertEquals(0, frame.getSeries(4).get(0).getRight().intValue());
    assertEquals(0, frame.getSeries(5).get(0).getRight().intValue());

    assertEquals(Color.RED, frame.getSeries(0).get(1).getLeft());
    assertEquals(Color.BLUE, frame.getSeries(1).get(1).getLeft());
    assertEquals(Color.CYAN, frame.getSeries(2).get(1).getLeft());
    assertEquals(lighten(Color.ORANGE), frame.getSeries(3).get(1).getLeft());
    assertEquals(Color.GREEN, frame.getSeries(4).get(1).getLeft());
    assertEquals(Color.GRAY, frame.getSeries(5).get(1).getLeft());

    assertEquals(-6, frame.getSeries(0).get(1).getRight().intValue());
    assertEquals(+4, frame.getSeries(1).get(1).getRight().intValue());
    assertEquals(0, frame.getSeries(2).get(1).getRight().intValue());
    assertEquals(+1, frame.getSeries(3).get(1).getRight().intValue());
    assertEquals(+1, frame.getSeries(4).get(1).getRight().intValue());
    assertEquals(0, frame.getSeries(5).get(1).getRight().intValue());

    assertEquals(lighten(Color.RED), frame.getSeries(0).get(2).getLeft());
    assertEquals(lighten(Color.BLUE), frame.getSeries(1).get(2).getLeft());
    assertEquals(lighten(Color.CYAN), frame.getSeries(2).get(2).getLeft());
    assertEquals(lighten(Color.ORANGE), frame.getSeries(3).get(2).getLeft());
    assertEquals(lighten(Color.GREEN), frame.getSeries(4).get(2).getLeft());
    assertEquals(lighten(Color.GRAY), frame.getSeries(5).get(2).getLeft());

    assertEquals((-27) - (-6), frame.getSeries(0).get(2).getRight().intValue());
    assertEquals((+22) - (+4), frame.getSeries(1).get(2).getRight().intValue());
    assertEquals((+22) - 0, frame.getSeries(2).get(2).getRight().intValue());
    assertEquals(-20, frame.getSeries(3).get(2).getRight().intValue());
    assertEquals((+2) - (+1), frame.getSeries(4).get(2).getRight().intValue());
    assertEquals((+1) - 0, frame.getSeries(5).get(2).getRight().intValue());

    assertEquals(-27, frame.getMin().intValue());
    assertEquals(+22, frame.getMax().intValue());
  }

  @Test
  public void testDualChangeRangeBars() {
    BindableWrapper<Map<Pair<String, Color>, Triple<Integer, Integer, Integer>>> result =
        new BindableWrapper<>();
    BarFrame frame =
        BarFrameBuilder.dual(
                result
                    .getBinding()
                    .mapNonNull(
                        map ->
                            map.entrySet().stream()
                                .sorted(
                                    Comparator
                                        .<Map.Entry<
                                                Pair<String, Color>,
                                                Triple<Integer, Integer, Integer>>>
                                            comparingInt(e -> e.getValue().getRight())
                                        .reversed())
                                .map(
                                    e ->
                                        new BarFrameBuilder.DualBar(
                                            e.getKey().getLeft(),
                                            e.getKey().getRight(),
                                            e.getValue().getLeft(),
                                            e.getValue().getMiddle(),
                                            "("
                                                + DIFF.format(e.getValue().getLeft())
                                                + ")-("
                                                + DIFF.format(e.getValue().getMiddle())
                                                + ")"))
                                .collect(Collectors.toList())))
            .build();
    assertEquals(0, frame.getNumBars());
    assertEquals(0, frame.getNumLines());

    result.setValue(
        Map.of(
            ImmutablePair.of("LIB", Color.RED),
            ImmutableTriple.of(-27, -6, 157),
            ImmutablePair.of("CON", Color.BLUE),
            ImmutableTriple.of(+4, +22, 121),
            ImmutablePair.of("NDP", Color.ORANGE),
            ImmutableTriple.of(-20, +1, 24),
            ImmutablePair.of("BQ", Color.CYAN),
            ImmutableTriple.of(0, +22, 32),
            ImmutablePair.of("GRN", Color.GREEN),
            ImmutableTriple.of(+1, +2, 3),
            ImmutablePair.of("IND", Color.GRAY),
            ImmutableTriple.of(0, +1, 1)));
    assertEquals(6, frame.getNumBars());

    assertEquals("LIB", frame.getLeftText(0));
    assertEquals("CON", frame.getLeftText(1));
    assertEquals("BQ", frame.getLeftText(2));
    assertEquals("NDP", frame.getLeftText(3));
    assertEquals("GRN", frame.getLeftText(4));
    assertEquals("IND", frame.getLeftText(5));

    assertEquals("(-27)-(-6)", frame.getRightText(0));
    assertEquals("(+4)-(+22)", frame.getRightText(1));
    assertEquals("(+0)-(+22)", frame.getRightText(2));
    assertEquals("(-20)-(+1)", frame.getRightText(3));
    assertEquals("(+1)-(+2)", frame.getRightText(4));
    assertEquals("(+0)-(+1)", frame.getRightText(5));

    assertEquals(Color.RED, frame.getSeries(0).get(1).getLeft());
    assertEquals(Color.BLUE, frame.getSeries(1).get(1).getLeft());
    assertEquals(Color.CYAN, frame.getSeries(2).get(1).getLeft());
    assertEquals(lighten(Color.ORANGE), frame.getSeries(3).get(1).getLeft());
    assertEquals(Color.GREEN, frame.getSeries(4).get(1).getLeft());
    assertEquals(Color.GRAY, frame.getSeries(5).get(1).getLeft());

    assertEquals(-6, frame.getSeries(0).get(1).getRight().intValue());
    assertEquals(+4, frame.getSeries(1).get(1).getRight().intValue());
    assertEquals(0, frame.getSeries(2).get(1).getRight().intValue());
    assertEquals(-20, frame.getSeries(3).get(1).getRight().intValue());
    assertEquals(+1, frame.getSeries(4).get(1).getRight().intValue());
    assertEquals(0, frame.getSeries(5).get(1).getRight().intValue());

    assertEquals(lighten(Color.RED), frame.getSeries(0).get(2).getLeft());
    assertEquals(lighten(Color.BLUE), frame.getSeries(1).get(2).getLeft());
    assertEquals(lighten(Color.CYAN), frame.getSeries(2).get(2).getLeft());
    assertEquals(lighten(Color.ORANGE), frame.getSeries(3).get(2).getLeft());
    assertEquals(lighten(Color.GREEN), frame.getSeries(4).get(2).getLeft());
    assertEquals(lighten(Color.GRAY), frame.getSeries(5).get(2).getLeft());

    assertEquals((-27) - (-6), frame.getSeries(0).get(2).getRight().intValue());
    assertEquals((+22) - (+4), frame.getSeries(1).get(2).getRight().intValue());
    assertEquals((+22) - 0, frame.getSeries(2).get(2).getRight().intValue());
    assertEquals(+1, frame.getSeries(3).get(2).getRight().intValue());
    assertEquals((+2) - (+1), frame.getSeries(4).get(2).getRight().intValue());
    assertEquals((+1) - 0, frame.getSeries(5).get(2).getRight().intValue());

    assertEquals(-27, frame.getMin().intValue());
    assertEquals(+22, frame.getMax().intValue());
  }

  @Test
  public void testBasicBars() {
    List<BarFrameBuilder.BasicBar> regions =
        List.of(
            new BarFrameBuilder.BasicBar("East Midlands", Color.BLACK, 5),
            new BarFrameBuilder.BasicBar("East of England", Color.BLACK, 7),
            new BarFrameBuilder.BasicBar("London", Color.BLACK, 8),
            new BarFrameBuilder.BasicBar("North East England", Color.BLACK, 3),
            new BarFrameBuilder.BasicBar("North West England", Color.BLACK, 8),
            new BarFrameBuilder.BasicBar("South East England", Color.BLACK, 10),
            new BarFrameBuilder.BasicBar("South West England", Color.BLACK, 6),
            new BarFrameBuilder.BasicBar("West Midlands", Color.BLACK, 7),
            new BarFrameBuilder.BasicBar("Yorkshire and the Humber", Color.BLACK, 6),
            new BarFrameBuilder.BasicBar("Scotland", Color.BLACK, 6),
            new BarFrameBuilder.BasicBar("Wales", Color.BLACK, 4),
            new BarFrameBuilder.BasicBar("Northern Ireland", Color.BLACK, 3));
    BarFrame frame = BarFrameBuilder.basic(() -> regions).build();
    assertEquals(12, frame.getNumBars());

    assertEquals("East Midlands", frame.getLeftText(0));
    assertEquals("South East England", frame.getLeftText(5));
    assertEquals("Northern Ireland", frame.getLeftText(11));

    assertEquals("5", frame.getRightText(0));
    assertEquals("10", frame.getRightText(5));
    assertEquals("3", frame.getRightText(11));

    assertEquals(5, frame.getSeries(0).get(0).getRight());
    assertEquals(10, frame.getSeries(5).get(0).getRight());
    assertEquals(3, frame.getSeries(11).get(0).getRight());
  }

  @Test
  public void testDualBarsAllPositive() {
    List<BarFrameBuilder.DualBar> regions =
        List.of(
            new BarFrameBuilder.DualBar("East Midlands", Color.BLACK, 44, 46, "46 > 44"),
            new BarFrameBuilder.DualBar("East of England", Color.BLACK, 56, 58, "58 > 56"),
            new BarFrameBuilder.DualBar("London", Color.BLACK, 68, 73, "73 > 68"),
            new BarFrameBuilder.DualBar("North East England", Color.BLACK, 26, 29, "29 > 26"),
            new BarFrameBuilder.DualBar("North West England", Color.BLACK, 68, 75, "75 > 68"),
            new BarFrameBuilder.DualBar("South East England", Color.BLACK, 83, 84, "84 > 83"),
            new BarFrameBuilder.DualBar("South West England", Color.BLACK, 53, 55, "55 > 53"),
            new BarFrameBuilder.DualBar("West Midlands", Color.BLACK, 54, 59, "59 > 54"),
            new BarFrameBuilder.DualBar("Yorkshire and the Humber", Color.BLACK, 50, 54, "54 > 50"),
            new BarFrameBuilder.DualBar("Scotland", Color.BLACK, 52, 59, "59 > 52"),
            new BarFrameBuilder.DualBar("Wales", Color.BLACK, 30, 40, "40 > 30"),
            new BarFrameBuilder.DualBar("Northern Ireland", Color.BLACK, 16, 18, "18 > 16"));
    BarFrame frame = BarFrameBuilder.dual(() -> regions).build();
    assertEquals(12, frame.getNumBars());

    assertEquals("East Midlands", frame.getLeftText(0));
    assertEquals("South East England", frame.getLeftText(5));
    assertEquals("Northern Ireland", frame.getLeftText(11));

    assertEquals("46 > 44", frame.getRightText(0));
    assertEquals("84 > 83", frame.getRightText(5));
    assertEquals("18 > 16", frame.getRightText(11));

    assertEquals(Color.BLACK, frame.getSeries(0).get(0).getLeft());
    assertEquals(Color.BLACK, frame.getSeries(5).get(0).getLeft());
    assertEquals(Color.BLACK, frame.getSeries(11).get(0).getLeft());

    assertEquals(0, frame.getSeries(0).get(0).getRight().doubleValue(), 0);
    assertEquals(0, frame.getSeries(5).get(0).getRight().doubleValue(), 0);
    assertEquals(0, frame.getSeries(11).get(0).getRight().doubleValue(), 0);

    assertEquals(Color.BLACK, frame.getSeries(0).get(1).getLeft());
    assertEquals(Color.BLACK, frame.getSeries(5).get(1).getLeft());
    assertEquals(Color.BLACK, frame.getSeries(11).get(1).getLeft());

    assertEquals(44, frame.getSeries(0).get(1).getRight().doubleValue(), 0);
    assertEquals(83, frame.getSeries(5).get(1).getRight().doubleValue(), 0);
    assertEquals(16, frame.getSeries(11).get(1).getRight().doubleValue(), 0);

    assertEquals(ColorUtils.lighten(Color.BLACK), frame.getSeries(0).get(2).getLeft());
    assertEquals(ColorUtils.lighten(Color.BLACK), frame.getSeries(5).get(2).getLeft());
    assertEquals(ColorUtils.lighten(Color.BLACK), frame.getSeries(11).get(2).getLeft());

    assertEquals(2, frame.getSeries(0).get(2).getRight().doubleValue(), 0);
    assertEquals(1, frame.getSeries(5).get(2).getRight().doubleValue(), 0);
    assertEquals(2, frame.getSeries(11).get(2).getRight().doubleValue(), 0);

    assertEquals(84, frame.getMax().doubleValue(), 0);
    assertEquals(0, frame.getMin().doubleValue(), 0);
  }

  @Test
  public void testDualVariousCombos() {
    BiConsumer<Pair<Color, Number>, Pair<Color, Number>> doAssert =
        (exp, act) -> {
          assertEquals(exp.getLeft(), act.getLeft());
          assertEquals(exp.getRight().doubleValue(), act.getRight().doubleValue(), 0.0);
        };

    BindableWrapper<List<BarFrameBuilder.DualBar>> regions =
        new BindableWrapper<>(List.of(new BarFrameBuilder.DualBar("", Color.BLACK, 0.0, 0.0, "")));
    BarFrame frame = BarFrameBuilder.dual(regions.getBinding()).build();
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(0));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(1));
    doAssert.accept(ImmutablePair.of(lighten(Color.BLACK), 0.0), frame.getSeries(0).get(2));

    regions.setValue(List.of(new BarFrameBuilder.DualBar("", Color.BLACK, 0.0, 2.0, "")));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(0));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(1));
    doAssert.accept(ImmutablePair.of(lighten(Color.BLACK), 2.0), frame.getSeries(0).get(2));

    regions.setValue(List.of(new BarFrameBuilder.DualBar("", Color.BLACK, 2.0, 0.0, "")));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(0));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(1));
    doAssert.accept(ImmutablePair.of(lighten(Color.BLACK), 2.0), frame.getSeries(0).get(2));

    regions.setValue(List.of(new BarFrameBuilder.DualBar("", Color.BLACK, 0.0, -2.0, "")));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(0));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(1));
    doAssert.accept(ImmutablePair.of(lighten(Color.BLACK), -2.0), frame.getSeries(0).get(2));

    regions.setValue(List.of(new BarFrameBuilder.DualBar("", Color.BLACK, -2.0, 0.0, "")));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(0));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(1));
    doAssert.accept(ImmutablePair.of(lighten(Color.BLACK), -2.0), frame.getSeries(0).get(2));

    regions.setValue(List.of(new BarFrameBuilder.DualBar("", Color.BLACK, 1.0, 3.0, "")));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(0));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 1.0), frame.getSeries(0).get(1));
    doAssert.accept(ImmutablePair.of(lighten(Color.BLACK), 2.0), frame.getSeries(0).get(2));

    regions.setValue(List.of(new BarFrameBuilder.DualBar("", Color.BLACK, 3.0, 1.0, "")));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(0));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 1.0), frame.getSeries(0).get(1));
    doAssert.accept(ImmutablePair.of(lighten(Color.BLACK), 2.0), frame.getSeries(0).get(2));

    regions.setValue(List.of(new BarFrameBuilder.DualBar("", Color.BLACK, -1.0, -3.0, "")));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(0));
    doAssert.accept(ImmutablePair.of(Color.BLACK, -1.0), frame.getSeries(0).get(1));
    doAssert.accept(ImmutablePair.of(lighten(Color.BLACK), -2.0), frame.getSeries(0).get(2));

    regions.setValue(List.of(new BarFrameBuilder.DualBar("", Color.BLACK, -3.0, -1.0, "")));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(0));
    doAssert.accept(ImmutablePair.of(Color.BLACK, -1.0), frame.getSeries(0).get(1));
    doAssert.accept(ImmutablePair.of(lighten(Color.BLACK), -2.0), frame.getSeries(0).get(2));

    regions.setValue(List.of(new BarFrameBuilder.DualBar("", Color.BLACK, -1.0, +1.0, "")));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(0));
    doAssert.accept(ImmutablePair.of(lighten(Color.BLACK), -1.0), frame.getSeries(0).get(1));
    doAssert.accept(ImmutablePair.of(lighten(Color.BLACK), +1.0), frame.getSeries(0).get(2));

    regions.setValue(List.of(new BarFrameBuilder.DualBar("", Color.BLACK, +1.0, -1.0, "")));
    doAssert.accept(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0).get(0));
    doAssert.accept(ImmutablePair.of(lighten(Color.BLACK), +1.0), frame.getSeries(0).get(1));
    doAssert.accept(ImmutablePair.of(lighten(Color.BLACK), -1.0), frame.getSeries(0).get(2));
  }

  private static Color lighten(Color color) {
    return new Color(
        128 + color.getRed() / 2, 128 + color.getGreen() / 2, 128 + color.getBlue() / 2);
  }
}
