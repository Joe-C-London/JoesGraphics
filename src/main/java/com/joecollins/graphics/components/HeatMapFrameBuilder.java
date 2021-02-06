package com.joecollins.graphics.components;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.ColorUtils;
import com.joecollins.models.general.Party;
import com.joecollins.models.general.PartyResult;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

public class HeatMapFrameBuilder {

  private HeatMapFrame frame = new HeatMapFrame();

  public static <T> HeatMapFrameBuilder of(
      Binding<Integer> numRows, List<T> entries, Function<T, Binding<Color>> colorFunc) {
    return of(numRows, entries, colorFunc, colorFunc);
  }

  public static <T> HeatMapFrameBuilder of(
      Binding<Integer> numRows,
      List<T> entries,
      Function<T, Binding<Color>> fillFunc,
      Function<T, Binding<Color>> borderFunc) {
    var builder = new HeatMapFrameBuilder();
    builder.frame.setNumRowsBinding(numRows);
    builder.frame.setNumSquaresBinding(Binding.fixedBinding(entries.size()));
    builder.frame.setSquareFillBinding(IndexedBinding.listBinding(entries, fillFunc));
    builder.frame.setSquareBordersBinding(IndexedBinding.listBinding(entries, borderFunc));
    return builder;
  }

  public static <T> HeatMapFrameBuilder ofClustered(
      Binding<Integer> numRows,
      List<T> entries,
      ToIntFunction<T> seatFunc,
      Function<T, Binding<Color>> fillFunc,
      Function<T, Binding<Color>> borderFunc) {
    List<T> allEntries =
        entries.stream()
            .flatMap(e -> Stream.generate(() -> e).limit(seatFunc.applyAsInt(e)))
            .collect(Collectors.toList());
    return of(numRows, allEntries, fillFunc, borderFunc);
  }

  public <T> HeatMapFrameBuilder withSeatBars(
      BindableList<T> bars,
      Function<T, Color> colorFunc,
      ToIntFunction<T> seatFunc,
      Binding<String> labelBinding) {
    frame.setNumSeatBarsBinding(Binding.sizeBinding(bars));
    frame.setSeatBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc));
    frame.setSeatBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc::applyAsInt));
    frame.setSeatBarLabelBinding(labelBinding);
    return this;
  }

  public <T> HeatMapFrameBuilder withChangeBars(
      BindableList<T> bars,
      Function<T, Color> colorFunc,
      ToIntFunction<T> seatFunc,
      Binding<Integer> startBinding,
      Binding<String> labelBinding) {
    frame.setNumChangeBarsBinding(Binding.sizeBinding(bars));
    frame.setChangeBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc));
    frame.setChangeBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc::applyAsInt));
    frame.setChangeBarStartBinding(startBinding);
    frame.setChangeBarLabelBinding(labelBinding);
    return this;
  }

  public HeatMapFrameBuilder withHeader(Binding<String> headerBinding) {
    frame.setHeaderBinding(headerBinding);
    return this;
  }

  public HeatMapFrameBuilder withBorder(Binding<Color> colorBinding) {
    frame.setBorderColorBinding(colorBinding);
    return this;
  }

  public HeatMapFrame build() {
    return frame;
  }

  public static <T> HeatMapFrame ofElectedLeading(
      Binding<Integer> rows,
      List<T> entries,
      Function<T, Binding<PartyResult>> resultFunc,
      Function<T, Party> prevResultFunc,
      Party party,
      BiFunction<Integer, Integer, String> seatLabel,
      BiPredicate<Integer, Integer> showChange,
      BiFunction<Integer, Integer, String> changeLabel,
      Binding<String> header) {
    return ofElectedLeading(
        rows,
        entries,
        x -> 1,
        resultFunc,
        prevResultFunc,
        party,
        seatLabel,
        showChange,
        changeLabel,
        header);
  }

  public static <T> HeatMapFrame ofElectedLeading(
      Binding<Integer> rows,
      List<T> entries,
      ToIntFunction<T> seatsFunc,
      Function<T, Binding<PartyResult>> resultFunc,
      Function<T, Party> prevResultFunc,
      Party party,
      BiFunction<Integer, Integer, String> seatLabel,
      BiPredicate<Integer, Integer> showChange,
      BiFunction<Integer, Integer, String> changeLabel,
      Binding<String> header) {

    Map<T, BindingReceiver<PartyResult>> results =
        entries.stream()
            .distinct()
            .collect(
                Collectors.toMap(
                    Function.identity(), e -> new BindingReceiver<>(resultFunc.apply(e))));
    Map<T, Party> prev =
        entries.stream().distinct().collect(Collectors.toMap(Function.identity(), prevResultFunc));

    List<BindingReceiver<Pair<PartyResult, Integer>>> resultBindings =
        entries.stream()
            .map(
                e ->
                    new BindingReceiver<Pair<PartyResult, Integer>>(
                        results
                            .get(e)
                            .getBinding(x -> ImmutablePair.of(x, seatsFunc.applyAsInt(e)))))
            .collect(Collectors.toList());
    List<BindingReceiver<Triple<PartyResult, Party, Integer>>> resultWithPrevBindings =
        entries.stream()
            .map(
                e ->
                    new BindingReceiver<Triple<PartyResult, Party, Integer>>(
                        results
                            .get(e)
                            .getBinding(
                                x -> ImmutableTriple.of(x, prev.get(e), seatsFunc.applyAsInt(e)))))
            .collect(Collectors.toList());

    BindableList<ImmutablePair<Color, Integer>> seatList = new BindableList<>();
    BindingReceiver<ImmutablePair<Integer, Integer>> seats =
        createSeatBarBinding(resultBindings, seatList, party::equals, party.getColor());

    BindableList<ImmutablePair<Color, Integer>> changeList = new BindableList<>();
    BindingReceiver<ImmutablePair<Integer, Integer>> change =
        createChangeBarBinding(
            resultWithPrevBindings, changeList, party::equals, party.getColor(), showChange);

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
                          if (r == null || r.getParty() == null) return Color.WHITE;
                          return (r.isElected()
                                  ? Function.<Color>identity()
                                  : (Function<Color, Color>) ColorUtils::lighten)
                              .apply(r.getParty().getColor());
                        }),
            prevResultFunc.andThen(p -> Binding.fixedBinding(p.getColor())))
        .withSeatBars(
            seatList,
            ImmutablePair::getLeft,
            ImmutablePair::getRight,
            seats.getBinding(p -> seatLabel.apply(p.left, p.right)))
        .withChangeBars(
            changeList,
            ImmutablePair::getLeft,
            ImmutablePair::getRight,
            Binding.fixedBinding(calcPrevForParty(allPrevs, party)),
            change.getBinding(changeLabelFunc))
        .withHeader(header)
        .withBorder(Binding.fixedBinding(party.getColor()))
        .build();
  }

  private static <T> int calcPrevForParty(List<Pair<Party, Integer>> prev, Party party) {
    return prev.stream().filter(e -> party.equals(e.getLeft())).mapToInt(e -> e.getRight()).sum();
  }

  private static <T> BindingReceiver<ImmutablePair<Integer, Integer>> createSeatBarBinding(
      List<BindingReceiver<Pair<PartyResult, Integer>>> results,
      BindableList<ImmutablePair<Color, Integer>> list,
      Predicate<Party> partyFilter,
      Color color) {
    Binding<ImmutablePair<Integer, Integer>> binding =
        Binding.mapReduceBinding(
            results.stream().map(BindingReceiver::getBinding).collect(Collectors.toList()),
            ImmutablePair.of(0, 0),
            (p, r) -> {
              if (r.getLeft() == null || partyFilter.negate().test(r.getLeft().getParty()))
                return p;
              return ImmutablePair.of(
                  p.left + (r.getLeft().isElected() ? r.getRight() : 0), p.right + r.getRight());
            },
            (p, r) -> {
              if (r.getLeft() == null || partyFilter.negate().test(r.getLeft().getParty()))
                return p;
              return ImmutablePair.of(
                  p.left - (r.getLeft().isElected() ? r.getRight() : 0), p.right - r.getRight());
            });
    BindingReceiver<ImmutablePair<Integer, Integer>> seats = new BindingReceiver<>(binding);
    seats
        .getBinding()
        .bindLegacy(
            p -> {
              list.setAll(
                  List.of(
                      ImmutablePair.of(color, p.left),
                      ImmutablePair.of(ColorUtils.lighten(color), p.right - p.left)));
            });
    return seats;
  }

  private static <T> BindingReceiver<ImmutablePair<Integer, Integer>> createChangeBarBinding(
      List<BindingReceiver<Triple<PartyResult, Party, Integer>>> resultWithPrev,
      BindableList<ImmutablePair<Color, Integer>> list,
      Predicate<Party> partyFilter,
      Color color,
      BiPredicate<Integer, Integer> showChangeBars) {
    Binding<ImmutablePair<Integer, Integer>> binding =
        Binding.mapReduceBinding(
            resultWithPrev.stream().map(BindingReceiver::getBinding).collect(Collectors.toList()),
            ImmutablePair.of(0, 0),
            (p, r) -> {
              if (r.getLeft() == null || r.getLeft().getParty() == null) {
                return p;
              }
              if (partyFilter.test(r.getLeft().getParty())) {
                p =
                    ImmutablePair.of(
                        p.left + (r.getLeft().isElected() ? r.getRight() : 0),
                        p.right + r.getRight());
              }
              if (partyFilter.test(r.getMiddle())) {
                p =
                    ImmutablePair.of(
                        p.left - (r.getLeft().isElected() ? r.getRight() : 0),
                        p.right - r.getRight());
              }
              return p;
            },
            (p, r) -> {
              if (r.getLeft() == null || r.getLeft().getParty() == null) {
                return p;
              }
              if (partyFilter.test(r.getLeft().getParty())) {
                p =
                    ImmutablePair.of(
                        p.left - (r.getLeft().isElected() ? r.getRight() : 0),
                        p.right - r.getRight());
              }
              if (partyFilter.test(r.getMiddle())) {
                p =
                    ImmutablePair.of(
                        p.left + (r.getLeft().isElected() ? r.getRight() : 0),
                        p.right + r.getRight());
              }
              return p;
            });
    BindingReceiver<ImmutablePair<Integer, Integer>> seats = new BindingReceiver<>(binding);
    seats
        .getBinding()
        .bindLegacy(
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
