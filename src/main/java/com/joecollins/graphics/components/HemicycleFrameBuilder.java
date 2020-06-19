package com.joecollins.graphics.components;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.awt.Point;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HemicycleFrameBuilder {

  public enum Tiebreaker {
    FRONT_ROW_FROM_LEFT,
    FRONT_ROW_FROM_RIGHT
  }

  HemicycleFrame frame = new HemicycleFrame();

  public static <T> HemicycleFrameBuilder of(
      List<Integer> rows,
      List<T> entries,
      Function<T, Binding<Color>> colorFunc,
      Tiebreaker tiebreaker) {
    return of(rows, entries, colorFunc, colorFunc, tiebreaker);
  }

  public static <T> HemicycleFrameBuilder of(
      List<Integer> rows,
      List<T> entries,
      Function<T, Binding<Color>> colorFunc,
      Function<T, Binding<Color>> borderFunc,
      Tiebreaker tiebreaker) {
    HemicycleFrameBuilder builder = new HemicycleFrameBuilder();
    builder.frame.setNumRowsBinding(Binding.fixedBinding(rows.size()));
    builder.frame.setRowCountsBinding(IndexedBinding.listBinding(rows));

    List<Point> points =
        IntStream.range(0, rows.size())
            .boxed()
            .flatMap(r -> IntStream.range(0, rows.get(r)).mapToObj(c -> new Point(r, c)))
            .sorted(
                Comparator.comparingDouble((Point p) -> 180.0 * p.y / (rows.get(p.x) - 1))
                    .thenComparingInt(
                        p -> (tiebreaker == Tiebreaker.FRONT_ROW_FROM_LEFT ? 1 : -1) * p.x))
            .collect(Collectors.toList());

    Map<Point, T> dotIndexToEntryIndex = new LinkedHashMap<>();
    for (int dotIndex = 0; dotIndex < points.size(); dotIndex++) {
      Point point = points.get(dotIndex);
      T entry = entries.get(dotIndex);
      dotIndexToEntryIndex.put(point, entry);
    }

    List<T> dots =
        points.stream()
            .sorted(Comparator.comparingInt((Point p) -> p.x).thenComparing(p -> p.y))
            .map(dotIndexToEntryIndex::get)
            .collect(Collectors.toList());

    builder.frame.setNumDotsBinding(Binding.fixedBinding(entries.size()));
    builder.frame.setDotColorBinding(IndexedBinding.listBinding(dots, colorFunc));
    builder.frame.setDotBorderBinding(IndexedBinding.listBinding(dots, borderFunc));
    return builder;
  }

  public HemicycleFrameBuilder withHeader(Binding<String> headerBinding) {
    frame.setHeaderBinding(headerBinding);
    return this;
  }

  public <T> HemicycleFrameBuilder withLeftSeatBars(
      BindableList<T> bars,
      Function<T, Color> colorFunc,
      Function<T, Integer> seatFunc,
      Binding<String> labelBinding) {
    frame.setLeftSeatBarCountBinding(Binding.sizeBinding(bars));
    frame.setLeftSeatBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc));
    frame.setLeftSeatBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc));
    frame.setLeftSeatBarLabelBinding(labelBinding);
    return this;
  }

  public <T> HemicycleFrameBuilder withRightSeatBars(
      BindableList<T> bars,
      Function<T, Color> colorFunc,
      Function<T, Integer> seatFunc,
      Binding<String> labelBinding) {
    frame.setRightSeatBarCountBinding(Binding.sizeBinding(bars));
    frame.setRightSeatBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc));
    frame.setRightSeatBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc));
    frame.setRightSeatBarLabelBinding(labelBinding);
    return this;
  }

  public <T> HemicycleFrameBuilder withMiddleSeatBars(
      BindableList<T> bars,
      Function<T, Color> colorFunc,
      Function<T, Integer> seatFunc,
      Binding<String> labelBinding) {
    frame.setMiddleSeatBarCountBinding(Binding.sizeBinding(bars));
    frame.setMiddleSeatBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc));
    frame.setMiddleSeatBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc));
    frame.setMiddleSeatBarLabelBinding(labelBinding);
    return this;
  }

  public <T> HemicycleFrameBuilder withLeftChangeBars(
      BindableList<T> bars,
      Function<T, Color> colorFunc,
      Function<T, Integer> seatFunc,
      Binding<Integer> startBinding,
      Binding<String> labelBinding) {
    frame.setLeftChangeBarCountBinding(Binding.sizeBinding(bars));
    frame.setLeftChangeBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc));
    frame.setLeftChangeBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc));
    frame.setLeftChangeBarStartBinding(startBinding);
    frame.setLeftChangeBarLabelBinding(labelBinding);
    return this;
  }

  public <T> HemicycleFrameBuilder withRightChangeBars(
      BindableList<T> bars,
      Function<T, Color> colorFunc,
      Function<T, Integer> seatFunc,
      Binding<Integer> startBinding,
      Binding<String> labelBinding) {
    frame.setRightChangeBarCountBinding(Binding.sizeBinding(bars));
    frame.setRightChangeBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc));
    frame.setRightChangeBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc));
    frame.setRightChangeBarStartBinding(startBinding);
    frame.setRightChangeBarLabelBinding(labelBinding);
    return this;
  }

  public HemicycleFrame build() {
    return frame;
  }
}
