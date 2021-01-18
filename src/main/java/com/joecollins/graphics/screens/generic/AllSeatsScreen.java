package com.joecollins.graphics.screens.generic;

import static com.joecollins.models.general.PartyResult.NO_RESULT;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.components.ResultListingFrame;
import com.joecollins.graphics.utils.StandardFont;
import com.joecollins.models.general.Party;
import com.joecollins.models.general.PartyResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;

public class AllSeatsScreen extends JPanel {

  private AllSeatsScreen(JLabel title, ResultListingFrame frame) {
    setBackground(Color.WHITE);
    setLayout(new BorderLayout());
    add(title, BorderLayout.NORTH);

    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.setLayout(new GridLayout(1, 1));
    panel.add(frame);
    add(panel, BorderLayout.CENTER);
  }

  public static <T> Builder<T> of(
      Binding<Map<T, Map<Party, Integer>>> prevResultBinding,
      Binding<Map<T, PartyResult>> currResultBinding,
      Function<T, String> nameFunc,
      Binding<String> headerBinding) {
    return new Builder<>(prevResultBinding, currResultBinding, nameFunc, headerBinding);
  }

  public static class Builder<T> {

    private final BindingReceiver<Map<T, Map<Party, Integer>>> prevResults;
    private final BindingReceiver<Map<T, PartyResult>> currResults;
    private final BindingReceiver<String> header;
    private final Function<T, String> nameFunc;

    private BindingReceiver<Integer> numRows = new BindingReceiver<>(() -> 20);
    private BindingReceiver<Set<T>> seatFilter = new BindingReceiver<>(Binding.fixedBinding(null));

    public Builder(
        Binding<Map<T, Map<Party, Integer>>> prevResultBinding,
        Binding<Map<T, PartyResult>> currResultBinding,
        Function<T, String> nameFunc,
        Binding<String> headerBinding) {
      this.prevResults = new BindingReceiver<>(prevResultBinding);
      this.currResults = new BindingReceiver<>(currResultBinding);
      this.header = new BindingReceiver<>(headerBinding);
      this.nameFunc = nameFunc;
    }

    public Builder<T> withNumRows(Binding<Integer> numRowsBinding) {
      this.numRows = new BindingReceiver<>(numRowsBinding);
      return this;
    }

    public Builder<T> withSeatFilter(Binding<Set<T>> seatFilterBinding) {
      this.seatFilter = new BindingReceiver<>(seatFilterBinding);
      return this;
    }

    public AllSeatsScreen build(Binding<String> titleBinding) {
      JLabel headerLabel = new JLabel();
      headerLabel.setFont(StandardFont.readBoldFont(32));
      headerLabel.setHorizontalAlignment(JLabel.CENTER);
      headerLabel.setBorder(new EmptyBorder(5, 0, -5, 0));
      titleBinding.bind(headerLabel::setText);

      Input<T> inputs = new Input<>(nameFunc);
      prevResults.getBinding().bind(inputs::setPrevResults);
      currResults.getBinding().bind(inputs::setCurrResults);
      seatFilter.getBinding().bind(inputs::setSeatFilter);

      BindableList<Entry<T>> entries = new BindableList<>();
      inputs.getResultBinding().bind(entries::setAll);

      ResultListingFrame frame = new ResultListingFrame();
      frame.setHeaderBinding(header.getBinding());
      frame.setNumRowsBinding(numRows.getBinding());
      frame.setNumItemsBinding(Binding.sizeBinding(entries));
      frame.setTextBinding(IndexedBinding.propertyBinding(entries, e -> nameFunc.apply(e.key)));
      frame.setBorderBinding(IndexedBinding.propertyBinding(entries, e -> e.prevColor));
      frame.setBackgroundBinding(
          IndexedBinding.propertyBinding(entries, e -> e.fill ? e.resultColor : Color.WHITE));
      frame.setForegroundBinding(
          IndexedBinding.propertyBinding(entries, e -> !e.fill ? e.resultColor : Color.WHITE));
      return new AllSeatsScreen(headerLabel, frame);
    }
  }

  private static class Input<T> extends Bindable<Input.Property> {
    private enum Property {
      PREV,
      CURR,
      FILTER
    }

    private final Function<T, String> nameFunc;
    private List<Pair<T, Party>> prevResults = List.of();
    private Map<T, PartyResult> currResults = Map.of();
    private Set<T> seatFilter = null;

    private Input(Function<T, String> nameFunc) {
      this.nameFunc = nameFunc;
    }

    public void setPrevResults(Map<T, Map<Party, Integer>> prevResults) {
      this.prevResults =
          prevResults.entrySet().stream()
              .map(
                  e -> {
                    Map<Party, Integer> votes = e.getValue();
                    int total = votes.values().stream().mapToInt(i -> i).sum();
                    List<Integer> topTwo =
                        votes.values().stream()
                            .sorted(Comparator.reverseOrder())
                            .limit(2)
                            .collect(Collectors.toList());
                    return ImmutablePair.of(e, 1.0 * (topTwo.get(0) - topTwo.get(1)) / total);
                  })
              .sorted(Comparator.comparing(e -> nameFunc.apply(e.getLeft().getKey()).toUpperCase()))
              .map(Map.Entry::getKey)
              .map(
                  e ->
                      ImmutablePair.of(
                          e.getKey(),
                          e.getValue().entrySet().stream()
                              .max(Map.Entry.comparingByValue())
                              .map(Map.Entry::getKey)
                              .orElse(null)))
              .collect(Collectors.toList());
      onPropertyRefreshed(Input.Property.PREV);
    }

    public void setCurrResults(Map<T, PartyResult> currResults) {
      this.currResults = currResults;
      onPropertyRefreshed(Input.Property.CURR);
    }

    public void setSeatFilter(Set<T> seatFilter) {
      this.seatFilter = seatFilter;
      onPropertyRefreshed(Input.Property.FILTER);
    }

    public Binding<List<Entry<T>>> getResultBinding() {
      return Binding.propertyBinding(
          this,
          t ->
              t.prevResults.stream()
                  .filter(e -> t.seatFilter == null || t.seatFilter.contains(e.getKey()))
                  .map(
                      e ->
                          ImmutableTriple.of(
                              e.getLeft(),
                              e.getRight(),
                              t.currResults.getOrDefault(e.getLeft(), NO_RESULT)))
                  .map(
                      e -> {
                        var result = e.getRight() == null ? NO_RESULT : e.getRight();
                        return new Entry<>(
                            e.getLeft(),
                            e.getMiddle().getColor(),
                            result.getParty() == null ? Color.BLACK : result.getParty().getColor(),
                            result.isElected());
                      })
                  .collect(Collectors.toList()),
          Input.Property.PREV,
          Input.Property.CURR,
          Input.Property.FILTER);
    }
  }

  private static class Entry<T> {
    private final T key;
    private final Color prevColor;
    private final Color resultColor;
    private final boolean fill;

    private Entry(T key, Color prevColor, Color resultColor, boolean fill) {
      this.key = key;
      this.prevColor = prevColor;
      this.resultColor = resultColor;
      this.fill = fill;
    }
  }
}
