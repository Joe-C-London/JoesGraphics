package com.joecollins.graphics.components;

import com.google.common.annotations.Beta;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.awt.Point;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.MutablePair;

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
    return ofClustered(rows, entries, e -> 1, colorFunc, borderFunc, tiebreaker);
  }

  @Beta
  public static <T> HemicycleFrameBuilder ofClustered(
      List<Integer> rows,
      List<T> entries,
      Function<T, Integer> seatsFunc,
      Function<T, Binding<Color>> colorFunc,
      Tiebreaker tiebreaker) {
    return ofClustered(rows, entries, seatsFunc, colorFunc, colorFunc, tiebreaker);
  }

  @Beta
  public static <T> HemicycleFrameBuilder ofClustered(
      List<Integer> rows,
      List<T> entries,
      Function<T, Integer> seatsFunc,
      Function<T, Binding<Color>> colorFunc,
      Function<T, Binding<Color>> borderFunc,
      Tiebreaker tiebreaker) {

    List<MutablePair<Point, T>> points =
        IntStream.range(0, rows.size())
            .boxed()
            .flatMap(r -> IntStream.range(0, rows.get(r)).mapToObj(c -> new Point(r, c)))
            .sorted(
                Comparator.comparingDouble((Point p) -> 180.0 * p.y / (rows.get(p.x) - 1))
                    .thenComparingInt(
                        p -> (tiebreaker == Tiebreaker.FRONT_ROW_FROM_LEFT ? 1 : -1) * p.x))
            .map(p -> new MutablePair<>(p, (T) null))
            .collect(Collectors.toList());

    for (T entry : entries) {
      List<Point> rejectedPoints = new ArrayList<>();
      List<Point> selectedPoints = new ArrayList<>();
      int numDots = seatsFunc.apply(entry);
      for (int i = 0; i < numDots; i++) {
        Optional<MutablePair<Point, T>> np =
            points.stream()
                .filter(e -> !rejectedPoints.contains(e.left))
                .filter(e -> e.right == null)
                .filter(
                    e ->
                        selectedPoints.isEmpty()
                            || selectedPoints.stream()
                                .anyMatch(p -> pointsAreBesideEachOther(p, e.left, rows)))
                .findFirst();
        if (np.isEmpty()) {
          rejectedPoints.addAll(selectedPoints);
          selectedPoints.clear();
          i--;
          continue;
        }
        MutablePair<Point, T> nextPoint = np.orElseThrow();
        nextPoint.setRight(entry);
        selectedPoints.add(nextPoint.left);
      }
    }

    HemicycleFrameBuilder builder = new HemicycleFrameBuilder();
    builder.frame.setNumRowsBinding(Binding.fixedBinding(rows.size()));
    builder.frame.setRowCountsBinding(IndexedBinding.listBinding(rows));

    List<T> dots =
        points.stream()
            .sorted(
                Comparator.comparingInt((MutablePair<Point, T> p) -> p.left.x)
                    .thenComparing(p -> p.left.y))
            .map(MutablePair::getRight)
            .collect(Collectors.toList());

    builder.frame.setNumDotsBinding(Binding.fixedBinding(dots.size()));
    builder.frame.setDotColorBinding(IndexedBinding.listBinding(dots, colorFunc));
    builder.frame.setDotBorderBinding(IndexedBinding.listBinding(dots, borderFunc));
    return builder;
  }

  private static <T> boolean pointsAreBesideEachOther(Point a, Point b, List<Integer> rows) {
    if (a.x == b.x) {
      return Math.abs(a.y - b.y) <= 1;
    }
    if (Math.abs(a.x - b.x) > 1) {
      return false;
    }
    double aY, bY;
    if (a.x > b.x) {
      aY = 1.0 * a.y;
      bY = 1.0 * b.y / rows.get(b.x) * rows.get(a.x);
    } else {
      aY = 1.0 * a.y / rows.get(a.x) * rows.get(b.x);
      bY = 1.0 * b.y;
    }
    return Math.abs(aY - bY) <= 0.5;
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
