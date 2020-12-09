package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.components.GraphicsFrame;
import com.joecollins.graphics.components.ResultListingFrame;
import com.joecollins.graphics.utils.ColorUtils;
import com.joecollins.graphics.utils.StandardFont;
import com.joecollins.models.general.Party;
import com.joecollins.models.general.PartyResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.tuple.ImmutableTriple;

public class BattlegroundScreen extends JPanel {

  private final ResultListingFrame leftPanel;
  private final ResultListingFrame rightPanel;

  private BattlegroundScreen(
      JLabel title,
      ResultListingFrame left,
      ResultListingFrame right,
      Function<BattlegroundScreen, Layout> lowerLayout) {
    this.leftPanel = left;
    this.rightPanel = right;

    setBackground(Color.WHITE);
    setLayout(new BorderLayout());
    add(title, BorderLayout.NORTH);

    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.setLayout(lowerLayout.apply(this));
    panel.add(left);
    panel.add(right);
    add(panel, BorderLayout.CENTER);
  }

  public static <T> SinglePartyBuilder<T> singleParty(
      Binding<Map<T, Map<Party, Integer>>> prevResultsBinding,
      Binding<Map<T, PartyResult>> currResultsBinding,
      Function<T, String> nameFunc,
      Binding<Party> partyBinding) {
    return new SinglePartyBuilder<>(prevResultsBinding, currResultsBinding, nameFunc, partyBinding);
  }

  public static class SinglePartyBuilder<T> {

    private final BindingReceiver<Map<T, Map<Party, Integer>>> prevResults;
    private final BindingReceiver<Map<T, PartyResult>> currResults;
    private final Function<T, String> nameFunc;
    private final BindingReceiver<Party> party;

    private BindingReceiver<Integer> defenseSeatCount = new BindingReceiver<>(() -> 100);
    private BindingReceiver<Integer> targetSeatCount = new BindingReceiver<>(() -> 100);
    private BindingReceiver<Integer> numRows = new BindingReceiver<>(() -> 20);
    private BindingReceiver<Set<T>> seatFilter = new BindingReceiver<>(Binding.fixedBinding(null));

    private SinglePartyBuilder(
        Binding<Map<T, Map<Party, Integer>>> prevResults,
        Binding<Map<T, PartyResult>> currResults,
        Function<T, String> nameFunc,
        Binding<Party> party) {
      this.prevResults = new BindingReceiver<>(prevResults);
      this.currResults = new BindingReceiver<>(currResults);
      this.nameFunc = nameFunc;
      this.party = new BindingReceiver<>(party);
    }

    public SinglePartyBuilder<T> withSeatsToShow(
        Binding<Integer> defenseSeatCountBinding, Binding<Integer> targetSeatCountBinding) {
      this.defenseSeatCount = new BindingReceiver<>(defenseSeatCountBinding);
      this.targetSeatCount = new BindingReceiver<>(targetSeatCountBinding);
      return this;
    }

    public SinglePartyBuilder<T> withNumRows(Binding<Integer> numRowsBinding) {
      this.numRows = new BindingReceiver<>(numRowsBinding);
      return this;
    }

    public SinglePartyBuilder<T> withSeatFilter(Binding<Set<T>> seatFilterBinding) {
      this.seatFilter = new BindingReceiver<>(seatFilterBinding);
      return this;
    }

    public BattlegroundScreen build(Binding<String> title) {
      JLabel headerLabel = new JLabel();
      headerLabel.setFont(StandardFont.readBoldFont(32));
      headerLabel.setHorizontalAlignment(JLabel.CENTER);
      headerLabel.setBorder(new EmptyBorder(5, 0, -5, 0));
      title.bind(headerLabel::setText);
      party.getBinding(Party::getColor).bind(headerLabel::setForeground);

      BattlegroundInput<T> defenseInput = new BattlegroundInput<>();
      prevResults.getBinding().bind(defenseInput::setPrev);
      currResults.getBinding().bind(defenseInput::setCurr);
      defenseSeatCount.getBinding().bind(defenseInput::setCount);
      party.getBinding().bind(defenseInput::setParty);
      seatFilter.getBinding().bind(defenseInput::setFilteredSeats);
      defenseInput.setSide(BattlegroundInput.Side.DEFENSE);
      var defenseItems = defenseInput.getItems();
      var defenseFrame = new ResultListingFrame();
      defenseFrame.setBorderColorBinding(party.getBinding(Party::getColor));
      defenseFrame.setHeaderBinding(party.getBinding(p -> p + " DEFENSE SEATS"));
      defenseFrame.setHeaderAlignmentBinding(Binding.fixedBinding(GraphicsFrame.Alignment.RIGHT));
      defenseFrame.setReversedBinding(() -> true);
      defenseFrame.setNumRowsBinding(numRows.getBinding());
      defenseFrame.setNumItemsBinding(Binding.sizeBinding(defenseItems));
      defenseFrame.setTextBinding(
          IndexedBinding.propertyBinding(defenseItems, e -> nameFunc.apply(e.key)));
      defenseFrame.setBorderBinding(IndexedBinding.propertyBinding(defenseItems, e -> e.prevColor));
      defenseFrame.setBackgroundBinding(
          IndexedBinding.propertyBinding(defenseItems, e -> e.fill ? e.resultColor : Color.WHITE));
      defenseFrame.setForegroundBinding(
          IndexedBinding.propertyBinding(defenseItems, e -> !e.fill ? e.resultColor : Color.WHITE));

      BattlegroundInput<T> targetInput = new BattlegroundInput<>();
      prevResults.getBinding().bind(targetInput::setPrev);
      currResults.getBinding().bind(targetInput::setCurr);
      targetSeatCount.getBinding().bind(targetInput::setCount);
      party.getBinding().bind(targetInput::setParty);
      seatFilter.getBinding().bind(targetInput::setFilteredSeats);
      targetInput.setSide(BattlegroundInput.Side.TARGET);
      var targetItems = targetInput.getItems();
      var targetFrame = new ResultListingFrame();
      targetFrame.setBorderColorBinding(party.getBinding(Party::getColor));
      targetFrame.setHeaderBinding(party.getBinding(p -> p + " TARGET SEATS"));
      targetFrame.setHeaderAlignmentBinding(Binding.fixedBinding(GraphicsFrame.Alignment.LEFT));
      targetFrame.setReversedBinding(() -> false);
      targetFrame.setNumRowsBinding(numRows.getBinding());
      targetFrame.setNumRowsBinding(numRows.getBinding());
      targetFrame.setNumItemsBinding(Binding.sizeBinding(targetItems));
      targetFrame.setTextBinding(
          IndexedBinding.propertyBinding(targetItems, e -> nameFunc.apply(e.key)));
      targetFrame.setBorderBinding(IndexedBinding.propertyBinding(targetItems, e -> e.prevColor));
      targetFrame.setBackgroundBinding(
          IndexedBinding.propertyBinding(targetItems, e -> e.fill ? e.resultColor : Color.WHITE));
      targetFrame.setForegroundBinding(
          IndexedBinding.propertyBinding(targetItems, e -> !e.fill ? e.resultColor : Color.WHITE));

      return new BattlegroundScreen(
          headerLabel,
          defenseFrame,
          targetFrame,
          p -> {
            var layout = p.new Layout();
            defenseSeatCount
                .getBinding()
                .merge(numRows.getBinding(), (c, n) -> n * (int) Math.ceil(1.0 * c / n))
                .bind(layout::setLeft);
            targetSeatCount
                .getBinding()
                .merge(numRows.getBinding(), (c, n) -> n * (int) Math.ceil(1.0 * c / n))
                .bind(layout::setRight);
            return layout;
          });
    }
  }

  private static class BattlegroundInput<T> extends Bindable<BattlegroundInput.Property> {
    private enum Property {
      PREV,
      CURR,
      PARTY,
      COUNT,
      SIDE,
      FILTERED_SEATS
    }

    private enum Side {
      DEFENSE,
      TARGET
    }

    private Map<T, Map<Party, Integer>> prev = new HashMap<>();
    private Map<T, PartyResult> curr = new HashMap<>();
    private int count = 0;
    private Party party = null;
    private Side side = Side.TARGET;
    private Set<T> filteredSeats = null;

    public void setPrev(Map<T, Map<Party, Integer>> prev) {
      this.prev = prev;
      onPropertyRefreshed(Property.PREV);
    }

    public void setCurr(Map<T, PartyResult> curr) {
      this.curr = curr;
      onPropertyRefreshed(Property.CURR);
    }

    public void setCount(int count) {
      this.count = count;
      onPropertyRefreshed(Property.COUNT);
    }

    public void setParty(Party party) {
      this.party = party;
      onPropertyRefreshed(Property.PARTY);
    }

    public void setSide(Side side) {
      this.side = side;
      onPropertyRefreshed(Property.SIDE);
    }

    public void setFilteredSeats(Set<T> filteredSeats) {
      this.filteredSeats = filteredSeats;
      onPropertyRefreshed(Property.FILTERED_SEATS);
    }

    public BindableList<Entry<T>> getItems() {
      BindableList<Entry<T>> ret = new BindableList<>();
      Binding.propertyBinding(
              this,
              BattlegroundInput::getItemsList,
              Property.PREV,
              Property.COUNT,
              Property.CURR,
              Property.PARTY,
              Property.SIDE,
              Property.FILTERED_SEATS)
          .bind(ret::setAll);
      return ret;
    }

    private static <T> List<Entry<T>> getItemsList(BattlegroundInput<T> t) {
      return t.prev.entrySet().stream()
          .map(
              e -> {
                var votes = e.getValue();
                int total = votes.values().stream().mapToInt(i -> i).sum();
                List<Party> topTwo =
                    votes.entrySet().stream()
                        .sorted(Map.Entry.<Party, Integer>comparingByValue().reversed())
                        .limit(2)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                double margin;
                if (t.side == Side.TARGET) {
                  if (topTwo.get(0).equals(t.party)) margin = Double.NaN;
                  else
                    margin =
                        1.0 * (votes.get(topTwo.get(0)) - votes.getOrDefault(t.party, 0)) / total;
                } else {
                  if (!topTwo.get(0).equals(t.party)) margin = Double.NaN;
                  else
                    margin =
                        1.0 * (votes.getOrDefault(t.party, 0) - votes.get(topTwo.get(1))) / total;
                }
                return ImmutableTriple.of(e.getKey(), margin, topTwo.get(0).getColor());
              })
          .filter(e -> !Double.isNaN(e.getMiddle()))
          .sorted(Comparator.comparingDouble(ImmutableTriple::getMiddle))
          .limit(t.count)
          .map(
              e -> {
                PartyResult partyResult = t.curr.get(e.getLeft());
                Color resultColor;
                boolean fill;
                if (partyResult == null) {
                  resultColor = Color.BLACK;
                  fill = false;
                } else {
                  resultColor =
                      partyResult.getParty() == null
                          ? Color.BLACK
                          : partyResult.getParty().getColor();
                  fill = partyResult.isElected();
                }
                UnaryOperator<Color> colorFunc =
                    (t.filteredSeats == null || t.filteredSeats.contains(e.getLeft()))
                        ? UnaryOperator.identity()
                        : (c -> ColorUtils.lighten(ColorUtils.lighten(c)));
                return new Entry<>(
                    e.getLeft(), colorFunc.apply(e.getRight()), colorFunc.apply(resultColor), fill);
              })
          .collect(Collectors.toList());
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

  private class Layout implements LayoutManager {
    private int left;
    private int right;

    void setLeft(int left) {
      this.left = left;
      redoLayout();
    }

    void setRight(int right) {
      this.right = right;
      redoLayout();
    }

    private void redoLayout() {
      invalidate();
      revalidate();
      repaint();
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      return null;
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      return null;
    }

    @Override
    public void layoutContainer(Container parent) {
      leftPanel.setVisible(left > 0);
      rightPanel.setVisible(right > 0);
      int total = left + right;
      if (total == 0) {
        return;
      }
      var width = parent.getWidth();
      var height = parent.getHeight();
      int mid = width * left / total;
      leftPanel.setLocation(5, 5);
      leftPanel.setSize(mid - 10, height - 10);
      rightPanel.setLocation(mid + 5, 5);
      rightPanel.setSize(width - mid - 10, height - 10);
    }
  }
}
