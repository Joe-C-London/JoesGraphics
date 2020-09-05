package com.joecollins.graphics.components;

import com.google.common.annotations.Beta;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.ColorUtils;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.awt.Point;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

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
      ToIntFunction<T> seatsFunc,
      Function<T, Binding<Color>> colorFunc,
      Tiebreaker tiebreaker) {
    return ofClustered(rows, entries, seatsFunc, colorFunc, colorFunc, tiebreaker);
  }

  @Beta
  public static <T> HemicycleFrameBuilder ofClustered(
      List<Integer> rows,
      List<T> entries,
      ToIntFunction<T> seatsFunc,
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
      int numDots = seatsFunc.applyAsInt(entry);
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
      ToIntFunction<T> seatFunc,
      Binding<String> labelBinding) {
    frame.setLeftSeatBarCountBinding(Binding.sizeBinding(bars));
    frame.setLeftSeatBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc));
    frame.setLeftSeatBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc::applyAsInt));
    frame.setLeftSeatBarLabelBinding(labelBinding);
    return this;
  }

  public <T> HemicycleFrameBuilder withRightSeatBars(
      BindableList<T> bars,
      Function<T, Color> colorFunc,
      ToIntFunction<T> seatFunc,
      Binding<String> labelBinding) {
    frame.setRightSeatBarCountBinding(Binding.sizeBinding(bars));
    frame.setRightSeatBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc));
    frame.setRightSeatBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc::applyAsInt));
    frame.setRightSeatBarLabelBinding(labelBinding);
    return this;
  }

  public <T> HemicycleFrameBuilder withMiddleSeatBars(
      BindableList<T> bars,
      Function<T, Color> colorFunc,
      ToIntFunction<T> seatFunc,
      Binding<String> labelBinding) {
    frame.setMiddleSeatBarCountBinding(Binding.sizeBinding(bars));
    frame.setMiddleSeatBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc));
    frame.setMiddleSeatBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc::applyAsInt));
    frame.setMiddleSeatBarLabelBinding(labelBinding);
    return this;
  }

  public <T> HemicycleFrameBuilder withLeftChangeBars(
      BindableList<T> bars,
      Function<T, Color> colorFunc,
      ToIntFunction<T> seatFunc,
      Binding<Integer> startBinding,
      Binding<String> labelBinding) {
    frame.setLeftChangeBarCountBinding(Binding.sizeBinding(bars));
    frame.setLeftChangeBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc));
    frame.setLeftChangeBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc::applyAsInt));
    frame.setLeftChangeBarStartBinding(startBinding);
    frame.setLeftChangeBarLabelBinding(labelBinding);
    return this;
  }

  public <T> HemicycleFrameBuilder withRightChangeBars(
      BindableList<T> bars,
      Function<T, Color> colorFunc,
      ToIntFunction<T> seatFunc,
      Binding<Integer> startBinding,
      Binding<String> labelBinding) {
    frame.setRightChangeBarCountBinding(Binding.sizeBinding(bars));
    frame.setRightChangeBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc));
    frame.setRightChangeBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc::applyAsInt));
    frame.setRightChangeBarStartBinding(startBinding);
    frame.setRightChangeBarLabelBinding(labelBinding);
    return this;
  }

  public HemicycleFrame build() {
    return frame;
  }

  public static class Result {
    private final Party winner;
    private final boolean hasWon;

    public Result(Party winner, boolean hasWon) {
      this.winner = winner;
      this.hasWon = hasWon;
    }
  }

  public static <T> HemicycleFrame ofElectedLeading(
      List<Integer> rows,
      List<T> entries,
      Function<T, Binding<Result>> resultFunc,
      Function<T, Party> prevResultFunc,
      Party leftParty,
      Party rightParty,
      BiFunction<Integer, Integer, String> leftLabel,
      BiFunction<Integer, Integer, String> rightLabel,
      BiFunction<Integer, Integer, String> otherLabel,
      BiPredicate<Integer, Integer> showChange,
      BiFunction<Integer, Integer, String> changeLabel,
      Tiebreaker tiebreaker,
      Binding<String> header) {
    return ofElectedLeading(
        rows,
        entries,
        e -> 1,
        resultFunc,
        prevResultFunc,
        leftParty,
        rightParty,
        leftLabel,
        rightLabel,
        otherLabel,
        showChange,
        changeLabel,
        tiebreaker,
        header);
  }

  @Beta
  public static <T> HemicycleFrame ofElectedLeading(
      List<Integer> rows,
      List<T> entries,
      ToIntFunction<T> seatsFunc,
      Function<T, Binding<Result>> resultFunc,
      Function<T, Party> prevResultFunc,
      Party leftParty,
      Party rightParty,
      BiFunction<Integer, Integer, String> leftLabel,
      BiFunction<Integer, Integer, String> rightLabel,
      BiFunction<Integer, Integer, String> otherLabel,
      BiPredicate<Integer, Integer> showChange,
      BiFunction<Integer, Integer, String> changeLabel,
      Tiebreaker tiebreaker,
      Binding<String> header) {
    Map<T, BindingReceiver<Result>> results =
        entries.stream()
            .distinct()
            .collect(
                Collectors.toMap(
                    Function.identity(), e -> new BindingReceiver<>(resultFunc.apply(e))));
    Map<T, Party> prev =
        entries.stream().distinct().collect(Collectors.toMap(Function.identity(), prevResultFunc));

    List<BindingReceiver<Pair<Result, Integer>>> resultBindings =
        entries.stream()
            .map(
                e ->
                    new BindingReceiver<Pair<Result, Integer>>(
                        results
                            .get(e)
                            .getBinding(x -> ImmutablePair.of(x, seatsFunc.applyAsInt(e)))))
            .collect(Collectors.toList());
    List<BindingReceiver<Triple<Result, Party, Integer>>> resultWithPrevBindings =
        entries.stream()
            .map(
                e ->
                    new BindingReceiver<Triple<Result, Party, Integer>>(
                        results
                            .get(e)
                            .getBinding(
                                x -> ImmutableTriple.of(x, prev.get(e), seatsFunc.applyAsInt(e)))))
            .collect(Collectors.toList());

    BindableList<ImmutablePair<Color, Integer>> leftList = new BindableList<>();
    BindingReceiver<ImmutablePair<Integer, Integer>> leftSeats =
        createSeatBarBinding(resultBindings, leftList, leftParty::equals, leftParty.getColor());

    BindableList<ImmutablePair<Color, Integer>> rightList = new BindableList<>();
    BindingReceiver<ImmutablePair<Integer, Integer>> rightSeats =
        createSeatBarBinding(resultBindings, rightList, rightParty::equals, rightParty.getColor());

    BindableList<ImmutablePair<Color, Integer>> middleList = new BindableList<>();
    BindingReceiver<ImmutablePair<Integer, Integer>> middleSeats =
        createSeatBarBinding(
            resultBindings,
            middleList,
            p -> p != null && !p.equals(leftParty) && !p.equals(rightParty),
            Party.OTHERS.getColor());

    BindableList<ImmutablePair<Color, Integer>> leftChangeList = new BindableList<>();
    BindingReceiver<ImmutablePair<Integer, Integer>> leftChange =
        createChangeBarBinding(
            resultWithPrevBindings,
            leftChangeList,
            leftParty::equals,
            leftParty.getColor(),
            showChange);

    BindableList<ImmutablePair<Color, Integer>> rightChangeList = new BindableList<>();
    BindingReceiver<ImmutablePair<Integer, Integer>> rightChange =
        createChangeBarBinding(
            resultWithPrevBindings,
            rightChangeList,
            rightParty::equals,
            rightParty.getColor(),
            showChange);

    Function<ImmutablePair<Integer, Integer>, String> changeLabelFunc =
        p -> showChange.test(p.left, p.right) ? changeLabel.apply(p.left, p.right) : "";
    List<Pair<Party, Integer>> allPrevs =
        entries.stream()
            .map(e -> ImmutablePair.of(prev.get(e), seatsFunc.applyAsInt(e)))
            .collect(Collectors.toList());
    return ofClustered(
            rows,
            entries,
            seatsFunc,
            e ->
                results
                    .get(e)
                    .getBinding(
                        r -> {
                          if (r == null || r.winner == null) return Color.WHITE;
                          return (r.hasWon
                                  ? Function.<Color>identity()
                                  : (Function<Color, Color>) ColorUtils::lighten)
                              .apply(r.winner.getColor());
                        }),
            prevResultFunc.andThen(p -> Binding.fixedBinding(p.getColor())),
            tiebreaker)
        .withLeftSeatBars(
            leftList,
            ImmutablePair::getLeft,
            ImmutablePair::getRight,
            leftSeats.getBinding(p -> leftLabel.apply(p.left, p.right)))
        .withRightSeatBars(
            rightList,
            ImmutablePair::getLeft,
            ImmutablePair::getRight,
            rightSeats.getBinding(p -> rightLabel.apply(p.left, p.right)))
        .withMiddleSeatBars(
            middleList,
            ImmutablePair::getLeft,
            ImmutablePair::getRight,
            middleSeats.getBinding(p -> otherLabel.apply(p.left, p.right)))
        .withLeftChangeBars(
            leftChangeList,
            ImmutablePair::getLeft,
            ImmutablePair::getRight,
            Binding.fixedBinding(calcPrevForParty(allPrevs, leftParty)),
            leftChange.getBinding(changeLabelFunc))
        .withRightChangeBars(
            rightChangeList,
            ImmutablePair::getLeft,
            ImmutablePair::getRight,
            Binding.fixedBinding(calcPrevForParty(allPrevs, rightParty)),
            rightChange.getBinding(changeLabelFunc))
        .withHeader(header)
        .build();
  }

  private static <T> int calcPrevForParty(List<Pair<Party, Integer>> prev, Party party) {
    return prev.stream().filter(e -> party.equals(e.getLeft())).mapToInt(e -> e.getRight()).sum();
  }

  private static <T> BindingReceiver<ImmutablePair<Integer, Integer>> createSeatBarBinding(
      List<BindingReceiver<Pair<Result, Integer>>> results,
      BindableList<ImmutablePair<Color, Integer>> list,
      Predicate<Party> partyFilter,
      Color color) {
    Binding<ImmutablePair<Integer, Integer>> binding =
        Binding.mapReduceBinding(
            results.stream().map(BindingReceiver::getBinding).collect(Collectors.toList()),
            ImmutablePair.of(0, 0),
            (p, r) -> {
              if (r.getLeft() == null || partyFilter.negate().test(r.getLeft().winner)) return p;
              return ImmutablePair.of(
                  p.left + (r.getLeft().hasWon ? r.getRight() : 0), p.right + r.getRight());
            },
            (p, r) -> {
              if (r.getLeft() == null || partyFilter.negate().test(r.getLeft().winner)) return p;
              return ImmutablePair.of(
                  p.left - (r.getLeft().hasWon ? r.getRight() : 0), p.right - r.getRight());
            });
    BindingReceiver<ImmutablePair<Integer, Integer>> seats = new BindingReceiver<>(binding);
    seats
        .getBinding()
        .bind(
            p -> {
              list.setAll(
                  List.of(
                      ImmutablePair.of(color, p.left),
                      ImmutablePair.of(ColorUtils.lighten(color), p.right - p.left)));
            });
    return seats;
  }

  private static <T> BindingReceiver<ImmutablePair<Integer, Integer>> createChangeBarBinding(
      List<BindingReceiver<Triple<Result, Party, Integer>>> resultWithPrev,
      BindableList<ImmutablePair<Color, Integer>> list,
      Predicate<Party> partyFilter,
      Color color,
      BiPredicate<Integer, Integer> showChangeBars) {
    Binding<ImmutablePair<Integer, Integer>> binding =
        Binding.mapReduceBinding(
            resultWithPrev.stream().map(BindingReceiver::getBinding).collect(Collectors.toList()),
            ImmutablePair.of(0, 0),
            (p, r) -> {
              if (r.getLeft() == null || r.getLeft().winner == null) {
                return p;
              }
              if (partyFilter.test(r.getLeft().winner)) {
                p =
                    ImmutablePair.of(
                        p.left + (r.getLeft().hasWon ? r.getRight() : 0), p.right + r.getRight());
              }
              if (partyFilter.test(r.getMiddle())) {
                p =
                    ImmutablePair.of(
                        p.left - (r.getLeft().hasWon ? r.getRight() : 0), p.right - r.getRight());
              }
              return p;
            },
            (p, r) -> {
              if (r.getLeft() == null || r.getLeft().winner == null) {
                return p;
              }
              if (partyFilter.test(r.getLeft().winner)) {
                p =
                    ImmutablePair.of(
                        p.left - (r.getLeft().hasWon ? r.getRight() : 0), p.right - r.getRight());
              }
              if (partyFilter.test(r.getMiddle())) {
                p =
                    ImmutablePair.of(
                        p.left + (r.getLeft().hasWon ? r.getRight() : 0), p.right + r.getRight());
              }
              return p;
            });
    BindingReceiver<ImmutablePair<Integer, Integer>> seats = new BindingReceiver<>(binding);
    seats
        .getBinding()
        .bind(
            p -> {
              if (showChangeBars.test(p.left, p.right)) {
                list.setAll(
                    List.of(
                        ImmutablePair.of(color, p.left),
                        ImmutablePair.of(ColorUtils.lighten(color), p.right - p.left)));
              } else {
                list.clear();
              }
            });
    return seats;
  }
}
