package com.joecollins.graphics.components;

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
    return builder;
  }

  public HemicycleFrame build() {
    return frame;
  }
}
