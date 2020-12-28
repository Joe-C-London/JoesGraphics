package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.components.RegionSummaryFrame;
import com.joecollins.graphics.utils.StandardFont;
import com.joecollins.models.general.Party;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class PartySummaryScreen extends JPanel {

  private PartySummaryScreen(
      JLabel headerLabel,
      RegionSummaryFrame mainFrame,
      List<RegionSummaryFrame> otherFrames,
      int numRows) {
    setBackground(Color.WHITE);
    setLayout(new BorderLayout());
    add(headerLabel, BorderLayout.NORTH);

    JPanel center = new JPanel();
    center.setBackground(Color.WHITE);
    center.setLayout(new Layout(numRows));
    add(center, BorderLayout.CENTER);

    center.add(mainFrame, "main");
    otherFrames.forEach(o -> center.add(o, "other"));
  }

  private static class Layout implements LayoutManager {

    private final int numRows;
    private Component main = new JPanel();
    private List<Component> others = new ArrayList<>();

    private Layout(int numRows) {
      this.numRows = numRows;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
      if ("main".equals(name)) main = comp;
      else others.add(comp);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
      if (main == comp) main = new JPanel();
      others.remove(comp);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      return new Dimension(1024, 512);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      return new Dimension(128, 64);
    }

    @Override
    public void layoutContainer(Container parent) {
      int numOtherCols = (int) Math.ceil(1.0 * others.size() / numRows);
      int numTotalCols = numRows + numOtherCols;
      double widthPerCol = 1.0 * parent.getWidth() / numTotalCols;
      double heightPerRow = 1.0 * parent.getHeight() / numRows;
      main.setLocation(5, 5);
      main.setSize((int) (numRows * widthPerCol - 10), (int) (numRows * heightPerRow - 10));
      for (int i = 0; i < others.size(); i++) {
        Component other = others.get(i);
        int row = i / numOtherCols;
        int col = i % numOtherCols + numRows;
        other.setLocation((int) (col * widthPerCol + 5), (int) (row * heightPerRow + 5));
        other.setSize((int) (widthPerCol - 10), (int) (heightPerRow - 10));
      }
    }
  }

  public static <T> Builder<T> ofDiff(
      T mainRegion,
      Function<T, Binding<String>> titleFunc,
      Function<T, Binding<Map<Party, Integer>>> seatFunc,
      Function<T, Binding<Map<Party, Integer>>> seatDiffFunc,
      Function<T, Binding<Map<Party, Double>>> votePctFunc,
      Function<T, Binding<Map<Party, Double>>> votePctDiffFunc,
      int numRows) {
    return new Builder<>(
        mainRegion, titleFunc, seatFunc, seatDiffFunc, votePctFunc, votePctDiffFunc, numRows);
  }

  public static <T> Builder<T> ofPrev(
      T mainRegion,
      Function<T, Binding<String>> titleFunc,
      Function<T, Binding<Map<Party, Integer>>> seatFunc,
      Function<T, Binding<Map<Party, Integer>>> seatPrevFunc,
      Function<T, Binding<Map<Party, Double>>> votePctFunc,
      Function<T, Binding<Map<Party, Double>>> votePctPrevFunc,
      int numRows) {
    Function<T, Binding<Map<Party, Integer>>> seatDiffFunc =
        t -> {
          var curr = seatFunc.apply(t);
          var prev = seatPrevFunc.apply(t);
          return curr.merge(
              prev,
              (c, p) ->
                  Stream.concat(c.keySet().stream(), p.keySet().stream())
                      .distinct()
                      .collect(
                          Collectors.toMap(
                              Function.identity(),
                              party -> c.getOrDefault(party, 0) - p.getOrDefault(party, 0))));
        };
    Function<T, Binding<Map<Party, Double>>> votePctDiffFunc =
        t -> {
          var curr = votePctFunc.apply(t);
          var prev = votePctPrevFunc.apply(t);
          return curr.merge(
              prev,
              (c, p) ->
                  Stream.concat(c.keySet().stream(), p.keySet().stream())
                      .distinct()
                      .collect(
                          Collectors.toMap(
                              Function.identity(),
                              party -> c.getOrDefault(party, 0.0) - p.getOrDefault(party, 0.0))));
        };
    return new Builder<>(
        mainRegion, titleFunc, seatFunc, seatDiffFunc, votePctFunc, votePctDiffFunc, numRows);
  }

  public static class Builder<T> {
    private T mainRegion;
    private List<T> regions = new ArrayList<>();
    private Function<T, Binding<String>> titleFunc;
    private Function<T, Binding<Map<Party, Integer>>> seatFunc;
    private Function<T, Binding<Map<Party, Integer>>> seatDiffFunc;
    private Function<T, Binding<Map<Party, Double>>> votePctFunc;
    private Function<T, Binding<Map<Party, Double>>> votePctDiffFunc;
    private int numRows;

    private Builder(
        T mainRegion,
        Function<T, Binding<String>> titleFunc,
        Function<T, Binding<Map<Party, Integer>>> seatFunc,
        Function<T, Binding<Map<Party, Integer>>> seatDiffFunc,
        Function<T, Binding<Map<Party, Double>>> votePctFunc,
        Function<T, Binding<Map<Party, Double>>> votePctDiffFunc,
        int numRows) {
      this.mainRegion = mainRegion;
      this.titleFunc = titleFunc;
      this.seatFunc = seatFunc;
      this.seatDiffFunc = seatDiffFunc;
      this.votePctFunc = votePctFunc;
      this.votePctDiffFunc = votePctDiffFunc;
      this.numRows = numRows;
    }

    public Builder<T> withRegion(T region) {
      regions.add(region);
      return this;
    }

    public PartySummaryScreen build(Binding<Party> partyBinding) {
      var party = new BindingReceiver<>(partyBinding);

      JLabel headerLabel = new JLabel();
      headerLabel.setFont(StandardFont.readBoldFont(32));
      headerLabel.setHorizontalAlignment(JLabel.CENTER);
      headerLabel.setBorder(new EmptyBorder(5, 0, -5, 0));
      party.getBinding(p -> p.getName().toUpperCase() + " SUMMARY").bind(headerLabel::setText);
      party.getBinding(Party::getColor).bind(headerLabel::setForeground);

      RegionSummaryFrame mainFrame = createFrame(mainRegion, party);
      List<RegionSummaryFrame> otherFrames =
          regions.stream().map(t -> createFrame(t, party)).collect(Collectors.toList());

      return new PartySummaryScreen(headerLabel, mainFrame, otherFrames, numRows);
    }

    private RegionSummaryFrame createFrame(T region, BindingReceiver<Party> party) {
      SinglePartyInput input = new SinglePartyInput();
      seatFunc.apply(region).bind(input::setSeats);
      seatDiffFunc.apply(region).bind(input::setSeatDiff);
      votePctFunc.apply(region).bind(input::setVotePct);
      votePctDiffFunc.apply(region).bind(input::setVotePctDiff);
      party.getBinding().bind(input::setParty);

      BindableList<List<String>> values = new BindableList<>();
      values.setAll(List.of(List.of("", ""), List.of("", "")));
      Binding.propertyBinding(
              input,
              i -> {
                int seats = i.seats.getOrDefault(i.party, 0);
                int diff = i.seatDiff.getOrDefault(i.party, 0);
                return List.of(
                    String.valueOf(seats),
                    diff == 0 ? "\u00b10" : new DecimalFormat("+0;-0").format(diff));
              },
              SinglePartyInput.Property.SEATS,
              SinglePartyInput.Property.SEAT_DIFF,
              SinglePartyInput.Property.PARTY)
          .bind(p -> values.set(0, p));
      Binding.propertyBinding(
              input,
              i -> {
                double vote = i.votePct.getOrDefault(i.party, 0.0);
                double diff = i.votePctDiff.getOrDefault(i.party, 0.0);
                return List.of(
                    new DecimalFormat("0.0%").format(vote),
                    diff == 0 ? "\u00b10.0%" : new DecimalFormat("+0.0%;-0.0%").format(diff));
              },
              SinglePartyInput.Property.VOTE_PCT,
              SinglePartyInput.Property.VOTE_PCT_DIFF,
              SinglePartyInput.Property.PARTY)
          .bind(p -> values.set(1, p));

      RegionSummaryFrame frame = new RegionSummaryFrame();
      frame.setHeaderBinding(titleFunc.apply(region));
      frame.setBorderColorBinding(party.getBinding(Party::getColor));
      frame.setSummaryColorBinding(party.getBinding(Party::getColor));
      frame.setNumSectionsBinding(() -> 2);
      frame.setSectionHeaderBinding(IndexedBinding.listBinding("SEATS", "POPULAR VOTE"));
      frame.setSectionValueBinding(IndexedBinding.propertyBinding(values, Function.identity()));
      return frame;
    }
  }

  private static class SinglePartyInput extends Bindable<SinglePartyInput.Property> {
    private enum Property {
      SEATS,
      SEAT_DIFF,
      VOTE_PCT,
      VOTE_PCT_DIFF,
      PARTY
    }

    private Map<Party, Integer> seats;
    private Map<Party, Integer> seatDiff;
    private Map<Party, Double> votePct;
    private Map<Party, Double> votePctDiff;
    private Party party;

    public void setSeats(Map<Party, Integer> seats) {
      this.seats = seats;
      onPropertyRefreshed(Property.SEATS);
    }

    public void setSeatDiff(Map<Party, Integer> seatDiff) {
      this.seatDiff = seatDiff;
      onPropertyRefreshed(Property.SEAT_DIFF);
    }

    public void setVotePct(Map<Party, Double> votePct) {
      this.votePct = votePct;
      onPropertyRefreshed(Property.VOTE_PCT);
    }

    public void setVotePctDiff(Map<Party, Double> votePctDiff) {
      this.votePctDiff = votePctDiff;
      onPropertyRefreshed(Property.VOTE_PCT_DIFF);
    }

    public void setParty(Party party) {
      this.party = party;
      onPropertyRefreshed(Property.PARTY);
    }
  }
}
