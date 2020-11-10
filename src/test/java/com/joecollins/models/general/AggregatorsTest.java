package com.joecollins.models.general;

import static org.junit.Assert.assertEquals;

import com.joecollins.graphics.utils.BindableWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class AggregatorsTest {

  @Test
  public void testKeyChange() {
    BindableWrapper<Map<String, Integer>> input = new BindableWrapper<>(Map.of("ABC", 5, "DEF", 7));
    Mutable<Map<String, Integer>> output = new MutableObject<>();
    Aggregators.adjustKey(input.getBinding(), k -> k.substring(0, 1)).bind(output::setValue);
    assertEquals(Map.of("A", 5, "D", 7), output.getValue());

    input.setValue(Map.of("ABC", 10, "DEF", 9, "GHI", 1));
    assertEquals(Map.of("A", 10, "D", 9, "G", 1), output.getValue());
  }

  @Test
  public void testKeyChangeWithMerge() {
    BindableWrapper<Map<String, Integer>> input = new BindableWrapper<>(Map.of("ABC", 5, "AZY", 7));
    Mutable<Map<String, Integer>> output = new MutableObject<>();
    Aggregators.adjustKey(input.getBinding(), k -> k.substring(0, 1)).bind(output::setValue);
    assertEquals(Map.of("A", 12), output.getValue());

    input.setValue(Map.of("ABC", 10, "DEF", 6, "DCB", 2));
    assertEquals(Map.of("A", 10, "D", 8), output.getValue());
  }

  @Test
  public void testCombine() {
    List<BindableWrapper<Map<String, Integer>>> inputs = new ArrayList<>();
    inputs.add(new BindableWrapper<>(Map.of("ABC", 8, "DEF", 6)));
    inputs.add(new BindableWrapper<>(Map.of("ABC", 7, "GHI", 3)));
    Mutable<Map<String, Integer>> output = new MutableObject<>();
    Aggregators.combine(inputs, BindableWrapper::getBinding).bind(output::setValue);
    assertEquals(Map.of("ABC", 15, "DEF", 6, "GHI", 3), output.getValue());

    inputs.get(0).setValue(Map.of("ABC", 12, "DEF", 7));
    assertEquals(Map.of("ABC", 19, "DEF", 7, "GHI", 3), output.getValue());

    inputs.get(1).setValue(Map.of("ABC", 3));
    assertEquals(Map.of("ABC", 15, "DEF", 7), output.getValue());

    inputs.get(0).setValue(Map.of("ABC", 6, "DEF", 0));
    assertEquals(Map.of("ABC", 9, "DEF", 0), output.getValue());

    inputs.get(1).setValue(Map.of("ABC", 4));
    assertEquals(Map.of("ABC", 10, "DEF", 0), output.getValue());
  }

  @Test
  public void testCombineWithSeed() {
    Map<String, Integer> seed = Map.of("ABC", 0, "DEF", 0);
    List<BindableWrapper<Map<String, Integer>>> inputs = new ArrayList<>();
    inputs.add(new BindableWrapper<>(Map.of("ABC", 8, "DEF", 6)));
    inputs.add(new BindableWrapper<>(Map.of("ABC", 7, "GHI", 3)));
    Mutable<Map<String, Integer>> output = new MutableObject<>();
    Aggregators.combine(inputs, BindableWrapper::getBinding, seed).bind(output::setValue);
    assertEquals(Map.of("ABC", 15, "DEF", 6, "GHI", 3), output.getValue());

    inputs.get(0).setValue(Map.of("ABC", 12, "DEF", 7));
    assertEquals(Map.of("ABC", 19, "DEF", 7, "GHI", 3), output.getValue());

    inputs.get(1).setValue(Map.of("ABC", 3));
    assertEquals(Map.of("ABC", 15, "DEF", 7), output.getValue());

    inputs.get(0).setValue(Map.of("ABC", 6));
    assertEquals(Map.of("ABC", 9, "DEF", 0), output.getValue());

    inputs.get(1).setValue(Map.of("ABC", 4));
    assertEquals(Map.of("ABC", 10, "DEF", 0), output.getValue());
  }

  @Test
  public void testCombineDual() {
    List<BindableWrapper<Map<String, Pair<Integer, Integer>>>> inputs = new ArrayList<>();
    inputs.add(
        new BindableWrapper<>(
            Map.of("ABC", ImmutablePair.of(4, 8), "DEF", ImmutablePair.of(1, 6))));
    inputs.add(
        new BindableWrapper<>(
            Map.of("ABC", ImmutablePair.of(2, 7), "GHI", ImmutablePair.of(0, 3))));
    Mutable<Map<String, Pair<Integer, Integer>>> output = new MutableObject<>();
    Aggregators.combineDual(inputs, BindableWrapper::getBinding).bind(output::setValue);
    assertEquals(
        Map.of(
            "ABC",
            ImmutablePair.of(6, 15),
            "DEF",
            ImmutablePair.of(1, 6),
            "GHI",
            ImmutablePair.of(0, 3)),
        output.getValue());

    inputs.get(0).setValue(Map.of("ABC", ImmutablePair.of(5, 12), "DEF", ImmutablePair.of(4, 7)));
    assertEquals(
        Map.of(
            "ABC",
            ImmutablePair.of(7, 19),
            "DEF",
            ImmutablePair.of(4, 7),
            "GHI",
            ImmutablePair.of(0, 3)),
        output.getValue());

    inputs.get(1).setValue(Map.of("ABC", ImmutablePair.of(2, 3)));
    assertEquals(
        Map.of("ABC", ImmutablePair.of(7, 15), "DEF", ImmutablePair.of(4, 7)), output.getValue());

    inputs.get(0).setValue(Map.of("ABC", ImmutablePair.of(0, 6), "DEF", ImmutablePair.of(0, 0)));
    assertEquals(
        Map.of("ABC", ImmutablePair.of(2, 9), "DEF", ImmutablePair.of(0, 0)), output.getValue());

    inputs.get(1).setValue(Map.of("ABC", ImmutablePair.of(4, 4)));
    assertEquals(
        Map.of("ABC", ImmutablePair.of(4, 10), "DEF", ImmutablePair.of(0, 0)), output.getValue());
  }

  @Test
  public void testCombineDualWithSeeding() {
    Map<String, Pair<Integer, Integer>> seed =
        Map.of("ABC", ImmutablePair.of(0, 0), "DEF", ImmutablePair.of(0, 0));
    List<BindableWrapper<Map<String, Pair<Integer, Integer>>>> inputs = new ArrayList<>();
    inputs.add(
        new BindableWrapper<>(
            Map.of("ABC", ImmutablePair.of(4, 8), "DEF", ImmutablePair.of(1, 6))));
    inputs.add(
        new BindableWrapper<>(
            Map.of("ABC", ImmutablePair.of(2, 7), "GHI", ImmutablePair.of(0, 3))));
    Mutable<Map<String, Pair<Integer, Integer>>> output = new MutableObject<>();
    Aggregators.combineDual(inputs, BindableWrapper::getBinding, seed).bind(output::setValue);
    assertEquals(
        Map.of(
            "ABC",
            ImmutablePair.of(6, 15),
            "DEF",
            ImmutablePair.of(1, 6),
            "GHI",
            ImmutablePair.of(0, 3)),
        output.getValue());

    inputs.get(0).setValue(Map.of("ABC", ImmutablePair.of(5, 12), "DEF", ImmutablePair.of(4, 7)));
    assertEquals(
        Map.of(
            "ABC",
            ImmutablePair.of(7, 19),
            "DEF",
            ImmutablePair.of(4, 7),
            "GHI",
            ImmutablePair.of(0, 3)),
        output.getValue());

    inputs.get(1).setValue(Map.of("ABC", ImmutablePair.of(2, 3)));
    assertEquals(
        Map.of("ABC", ImmutablePair.of(7, 15), "DEF", ImmutablePair.of(4, 7)), output.getValue());

    inputs.get(0).setValue(Map.of("ABC", ImmutablePair.of(0, 6)));
    assertEquals(
        Map.of("ABC", ImmutablePair.of(2, 9), "DEF", ImmutablePair.of(0, 0)), output.getValue());

    inputs.get(1).setValue(Map.of("ABC", ImmutablePair.of(4, 4)));
    assertEquals(
        Map.of("ABC", ImmutablePair.of(4, 10), "DEF", ImmutablePair.of(0, 0)), output.getValue());
  }

  @Test
  public void testAdjustForPctReporting() {
    BindableWrapper<Map<String, Integer>> votes =
        new BindableWrapper<>(Map.of("ABC", 500, "DEF", 300));
    BindableWrapper<Double> pctReporting = new BindableWrapper<>(0.01);
    Mutable<Map<String, Integer>> output = new MutableObject<>();
    Aggregators.adjustForPctReporting(votes.getBinding(), pctReporting.getBinding())
        .bind(output::setValue);
    assertEquals(Map.of("ABC", 5, "DEF", 3), output.getValue());

    pctReporting.setValue(0.10);
    assertEquals(Map.of("ABC", 50, "DEF", 30), output.getValue());

    votes.setValue(Map.of("ABC", 750, "GHI", 30));
    assertEquals(Map.of("ABC", 75, "GHI", 3), output.getValue());
  }

  @Test
  public void testCombinePctReporting() {
    List<BindableWrapper<Double>> inputs = new ArrayList<>();
    inputs.add(new BindableWrapper<>(0.5));
    inputs.add(new BindableWrapper<>(0.3));
    MutableDouble output = new MutableDouble();
    Aggregators.combinePctReporting(inputs, BindableWrapper::getBinding).bind(output::setValue);
    assertEquals(0.4, output.getValue(), 1e-6);

    inputs.get(0).setValue(0.6);
    assertEquals(0.45, output.getValue(), 1e-6);

    inputs.get(1).setValue(0.7);
    assertEquals(0.65, output.getValue(), 1e-6);
  }

  @Test
  public void testCombinePctReportingWithWeights() {
    List<Pair<BindableWrapper<Double>, Double>> inputs = new ArrayList<>();
    inputs.add(ImmutablePair.of(new BindableWrapper<>(0.5), 2.0));
    inputs.add(ImmutablePair.of(new BindableWrapper<>(0.3), 3.0));
    MutableDouble output = new MutableDouble();
    Aggregators.combinePctReporting(inputs, e -> e.getLeft().getBinding(), e -> e.getRight())
        .bind(output::setValue);
    assertEquals(0.38, output.getValue(), 1e-6);

    inputs.get(0).getLeft().setValue(0.6);
    assertEquals(0.42, output.getValue(), 1e-6);

    inputs.get(1).getLeft().setValue(0.7);
    assertEquals(0.66, output.getValue(), 1e-6);
  }

  @Test
  public void testTopAndOthersBelowLimit() {
    BindableWrapper<Map<String, Integer>> votes = new BindableWrapper<>(Map.of("ABC", 5, "DEF", 3));
    Mutable<Map<String, Integer>> output = new MutableObject<>();
    Aggregators.topAndOthers(votes.getBinding(), 3, "OTHERS").bind(output::setValue);
    assertEquals(Map.of("ABC", 5, "DEF", 3), output.getValue());

    votes.setValue(Map.of("ABC", 5, "DEF", 7));
    assertEquals(Map.of("ABC", 5, "DEF", 7), output.getValue());
  }

  @Test
  public void testTopAndOthersAtLimit() {
    BindableWrapper<Map<String, Integer>> votes =
        new BindableWrapper<>(Map.of("ABC", 5, "DEF", 3, "GHI", 2));
    Mutable<Map<String, Integer>> output = new MutableObject<>();
    Aggregators.topAndOthers(votes.getBinding(), 3, "OTHERS").bind(output::setValue);
    assertEquals(Map.of("ABC", 5, "DEF", 3, "GHI", 2), output.getValue());

    votes.setValue(Map.of("ABC", 5, "DEF", 7, "GHI", 6));
    assertEquals(Map.of("ABC", 5, "DEF", 7, "GHI", 6), output.getValue());
  }

  @Test
  public void testTopAndOthersAboveLimit() {
    BindableWrapper<Map<String, Integer>> votes =
        new BindableWrapper<>(Map.of("ABC", 5, "DEF", 3, "GHI", 2, "JKL", 4));
    Mutable<Map<String, Integer>> output = new MutableObject<>();
    Aggregators.topAndOthers(votes.getBinding(), 3, "OTHERS").bind(output::setValue);
    assertEquals(Map.of("ABC", 5, "JKL", 4, "OTHERS", 5), output.getValue());

    votes.setValue(Map.of("ABC", 5, "DEF", 7, "GHI", 6, "JKL", 4));
    assertEquals(Map.of("DEF", 7, "GHI", 6, "OTHERS", 9), output.getValue());
  }

  @Test
  public void testTopAndOthersAboveLimitWithMandatoryInclusion() {
    BindableWrapper<Map<String, Integer>> votes =
        new BindableWrapper<>(Map.of("ABC", 5, "DEF", 3, "GHI", 2, "JKL", 4));
    BindableWrapper<String> winner = new BindableWrapper<>(null);
    Mutable<Map<String, Integer>> output = new MutableObject<>();
    Aggregators.topAndOthers(votes.getBinding(), 3, "OTHERS", winner.getBinding())
        .bind(output::setValue);
    assertEquals(Map.of("ABC", 5, "JKL", 4, "OTHERS", 5), output.getValue());

    votes.setValue(Map.of("ABC", 5, "DEF", 7, "GHI", 6, "JKL", 4));
    winner.setValue("ABC");
    assertEquals(Map.of("DEF", 7, "ABC", 5, "OTHERS", 10), output.getValue());

    winner.setValue(null);
    assertEquals(Map.of("DEF", 7, "GHI", 6, "OTHERS", 9), output.getValue());

    winner.setValue("DEF");
    assertEquals(Map.of("DEF", 7, "GHI", 6, "OTHERS", 9), output.getValue());
  }
}
