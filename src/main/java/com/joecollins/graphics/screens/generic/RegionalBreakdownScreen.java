package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.components.MultiSummaryFrame;
import com.joecollins.graphics.utils.StandardFont;
import com.joecollins.models.general.Party;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class RegionalBreakdownScreen extends JPanel {

  private RegionalBreakdownScreen(JLabel titleLabel, MultiSummaryFrame multiSummaryFrame) {
    setLayout(new BorderLayout());
    setBackground(Color.WHITE);
    add(titleLabel, BorderLayout.NORTH);

    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.setLayout(new GridLayout(1, 1));
    panel.add(multiSummaryFrame);
    add(panel, BorderLayout.CENTER);
  }

  public static SeatBuilder seats(
      Binding<String> totalHeaderBinding,
      Binding<Map<Party, Integer>> totalSeatsBinding,
      Binding<Integer> numTotalSeatsBinding,
      Binding<String> titleBinding) {
    return new SeatBuilder(
        totalHeaderBinding, totalSeatsBinding, numTotalSeatsBinding, titleBinding);
  }

  public static SeatDiffBuilder seatsWithDiff(
      Binding<String> totalHeaderBinding,
      Binding<Map<Party, Integer>> totalSeatsBinding,
      Binding<Map<Party, Integer>> seatDiffBinding,
      Binding<Integer> numTotalSeatsBinding,
      Binding<String> titleBinding) {
    return new SeatDiffBuilder(
        totalHeaderBinding, totalSeatsBinding, seatDiffBinding, numTotalSeatsBinding, titleBinding);
  }

  public static SeatPrevBuilder seatsWithPrev(
      Binding<String> totalHeaderBinding,
      Binding<Map<Party, Integer>> totalSeatsBinding,
      Binding<Map<Party, Integer>> prevSeatsBinding,
      Binding<Integer> numTotalSeatsBinding,
      Binding<String> titleBinding) {
    return new SeatPrevBuilder(
        totalHeaderBinding,
        totalSeatsBinding,
        prevSeatsBinding,
        numTotalSeatsBinding,
        titleBinding);
  }

  private interface Entry {
    Binding<String> getHeaderBinding();

    Binding<List<Pair<Color, String>>> getValueBinding();
  }

  private static class BlankEntry implements Entry {

    @Override
    public Binding<String> getHeaderBinding() {
      return Binding.fixedBinding("");
    }

    @Override
    public Binding<List<Pair<Color, String>>> getValueBinding() {
      return Binding.fixedBinding(List.of());
    }
  }

  private static class SeatEntry extends Bindable<SeatEntry, SeatEntry.Property> implements Entry {
    enum Property {
      PARTY_ORDER,
      NAME,
      SEATS,
      TOTAL_SEATS
    }

    protected List<Party> partyOrder = List.of();
    protected String name = "";
    protected Map<Party, Integer> seats = Map.of();
    protected int totalSeats = 0;

    public void setPartyOrder(List<Party> partyOrder) {
      this.partyOrder = partyOrder;
      onPropertyRefreshed(Property.PARTY_ORDER);
    }

    public void setName(String name) {
      this.name = name;
      onPropertyRefreshed(Property.NAME);
    }

    public void setSeats(Map<Party, Integer> seats) {
      this.seats = seats;
      onPropertyRefreshed(Property.SEATS);
    }

    public void setTotalSeats(int totalSeats) {
      this.totalSeats = totalSeats;
      onPropertyRefreshed(Property.TOTAL_SEATS);
    }

    public Binding<String> getHeaderBinding() {
      return Binding.propertyBinding(this, t -> t.name, Property.NAME);
    }

    public Binding<List<Pair<Color, String>>> getValueBinding() {
      return Binding.propertyBinding(
          this,
          t -> {
            List<Pair<Color, String>> ret =
                t.partyOrder.stream().map(t::getPartyLabel).collect(Collectors.toList());
            ret.add(
                ImmutablePair.of(
                    Color.WHITE,
                    seats.values().stream().mapToInt(i -> i).sum() + "/" + totalSeats));
            return ret;
          },
          Property.PARTY_ORDER,
          Property.SEATS,
          Property.TOTAL_SEATS);
    }

    protected ImmutablePair<Color, String> getPartyLabel(Party party) {
      return ImmutablePair.of(party.getColor(), String.valueOf(seats.getOrDefault(party, 0)));
    }
  }

  private static class SeatDiffEntry extends SeatEntry {
    private static final DecimalFormat DIFF_FORMAT = new DecimalFormat("+0;-0");
    private Map<Party, Integer> diff = Map.of();

    public void setDiff(Map<Party, Integer> diff) {
      this.diff = diff;
      onPropertyRefreshed(Property.SEATS);
    }

    @Override
    protected ImmutablePair<Color, String> getPartyLabel(Party party) {
      int seats = this.seats.getOrDefault(party, 0);
      int diff = this.diff.getOrDefault(party, 0);
      return ImmutablePair.of(
          party.getColor(),
          seats + " (" + (diff == 0 ? "\u00b10" : DIFF_FORMAT.format(diff)) + ")");
    }
  }

  private static class SeatPrevEntry extends SeatEntry {
    private static final DecimalFormat DIFF_FORMAT = new DecimalFormat("+0;-0");
    private Map<Party, Integer> prev = Map.of();

    public void setPrev(Map<Party, Integer> prev) {
      this.prev = prev;
      onPropertyRefreshed(Property.SEATS);
    }

    @Override
    protected ImmutablePair<Color, String> getPartyLabel(Party party) {
      int seats = this.seats.getOrDefault(party, 0);
      int diff = seats - this.prev.getOrDefault(party, 0);
      return ImmutablePair.of(
          party.getColor(),
          seats + " (" + (diff == 0 ? "\u00b10" : DIFF_FORMAT.format(diff)) + ")");
    }
  }

  private static class MultiPartyResultBuilder {
    protected BindingReceiver<String> title;
    protected List<Entry> entries = new ArrayList<>();
    protected BindingReceiver<List<Party>> partyOrder;

    public RegionalBreakdownScreen build(Binding<String> titleBinding) {
      JLabel headerLabel = new JLabel();
      headerLabel.setFont(StandardFont.readBoldFont(32));
      headerLabel.setHorizontalAlignment(JLabel.CENTER);
      headerLabel.setBorder(new EmptyBorder(5, 0, -5, 0));
      titleBinding.bindLegacy(headerLabel::setText);

      return new RegionalBreakdownScreen(headerLabel, createFrame());
    }

    private MultiSummaryFrame createFrame() {
      var frame = new MultiSummaryFrame();
      frame.setHeaderBinding(title.getBinding());
      frame.setNumRowsBinding(Binding.fixedBinding(entries.size()));
      frame.setRowHeaderBinding(IndexedBinding.listBinding(entries, Entry::getHeaderBinding));
      frame.setValuesBinding(IndexedBinding.listBinding(entries, Entry::getValueBinding));
      return frame;
    }
  }

  public static class SeatBuilder extends MultiPartyResultBuilder {

    public SeatBuilder(
        Binding<String> totalHeaderBinding,
        Binding<Map<Party, Integer>> totalSeatsBinding,
        Binding<Integer> numTotalSeatsBinding,
        Binding<String> titleBinding) {
      var totalSeats = new BindingReceiver<>(totalSeatsBinding);
      this.title = new BindingReceiver<>(titleBinding);

      this.partyOrder =
          new BindingReceiver<>(totalSeats.getBinding(RegionalBreakdownScreen::extractPartyOrder));
      SeatEntry topEntry = new SeatEntry();
      partyOrder.getBinding().bindLegacy(topEntry::setPartyOrder);
      totalHeaderBinding.bindLegacy(topEntry::setName);
      totalSeats.getBinding().bindLegacy(topEntry::setSeats);
      numTotalSeatsBinding.bindLegacy(topEntry::setTotalSeats);
      entries.add(topEntry);
    }

    public SeatBuilder withBlankRow() {
      entries.add(new BlankEntry());
      return this;
    }

    public SeatBuilder withRegion(
        Binding<String> nameBinding,
        Binding<Map<Party, Integer>> seatsBinding,
        Binding<Integer> numSeatsBinding) {
      SeatEntry newEntry = new SeatEntry();
      partyOrder.getBinding().bindLegacy(newEntry::setPartyOrder);
      nameBinding.bindLegacy(newEntry::setName);
      seatsBinding.bindLegacy(newEntry::setSeats);
      numSeatsBinding.bindLegacy(newEntry::setTotalSeats);
      entries.add(newEntry);
      return this;
    }
  }

  public static class SeatDiffBuilder extends MultiPartyResultBuilder {

    public SeatDiffBuilder(
        Binding<String> totalHeaderBinding,
        Binding<Map<Party, Integer>> totalSeatsBinding,
        Binding<Map<Party, Integer>> seatDiffBinding,
        Binding<Integer> numTotalSeatsBinding,
        Binding<String> titleBinding) {
      var totalSeats = new BindingReceiver<>(totalSeatsBinding);
      var seatDiff = new BindingReceiver<>(seatDiffBinding);
      this.title = new BindingReceiver<>(titleBinding);

      this.partyOrder =
          new BindingReceiver<>(
              totalSeats
                  .getBinding()
                  .merge(seatDiff.getBinding(), RegionalBreakdownScreen::extractPartyOrder));
      SeatDiffEntry topEntry = new SeatDiffEntry();
      partyOrder.getBinding().bindLegacy(topEntry::setPartyOrder);
      totalHeaderBinding.bindLegacy(topEntry::setName);
      totalSeats.getBinding().bindLegacy(topEntry::setSeats);
      seatDiff.getBinding().bindLegacy(topEntry::setDiff);
      numTotalSeatsBinding.bindLegacy(topEntry::setTotalSeats);
      entries.add(topEntry);
    }

    public SeatDiffBuilder withBlankRow() {
      entries.add(new BlankEntry());
      return this;
    }

    public SeatDiffBuilder withRegion(
        Binding<String> nameBinding,
        Binding<Map<Party, Integer>> seatsBinding,
        Binding<Map<Party, Integer>> diffBinding,
        Binding<Integer> numSeatsBinding) {
      SeatDiffEntry newEntry = new SeatDiffEntry();
      partyOrder.getBinding().bindLegacy(newEntry::setPartyOrder);
      nameBinding.bindLegacy(newEntry::setName);
      seatsBinding.bindLegacy(newEntry::setSeats);
      diffBinding.bindLegacy(newEntry::setDiff);
      numSeatsBinding.bindLegacy(newEntry::setTotalSeats);
      entries.add(newEntry);
      return this;
    }
  }

  public static class SeatPrevBuilder extends MultiPartyResultBuilder {

    public SeatPrevBuilder(
        Binding<String> totalHeaderBinding,
        Binding<Map<Party, Integer>> totalSeatsBinding,
        Binding<Map<Party, Integer>> prevSeatBinding,
        Binding<Integer> numTotalSeatsBinding,
        Binding<String> titleBinding) {
      var totalSeats = new BindingReceiver<>(totalSeatsBinding);
      var prevSeats = new BindingReceiver<>(prevSeatBinding);
      this.title = new BindingReceiver<>(titleBinding);

      this.partyOrder =
          new BindingReceiver<>(
              totalSeats
                  .getBinding()
                  .merge(prevSeats.getBinding(), RegionalBreakdownScreen::extractPartyOrder));
      SeatPrevEntry topEntry = new SeatPrevEntry();
      partyOrder.getBinding().bindLegacy(topEntry::setPartyOrder);
      totalHeaderBinding.bindLegacy(topEntry::setName);
      totalSeats.getBinding().bindLegacy(topEntry::setSeats);
      prevSeats.getBinding().bindLegacy(topEntry::setPrev);
      numTotalSeatsBinding.bindLegacy(topEntry::setTotalSeats);
      entries.add(topEntry);
    }

    public SeatPrevBuilder withBlankRow() {
      entries.add(new BlankEntry());
      return this;
    }

    public SeatPrevBuilder withRegion(
        Binding<String> nameBinding,
        Binding<Map<Party, Integer>> seatsBinding,
        Binding<Map<Party, Integer>> prevBinding,
        Binding<Integer> numSeatsBinding) {
      SeatPrevEntry newEntry = new SeatPrevEntry();
      partyOrder.getBinding().bindLegacy(newEntry::setPartyOrder);
      nameBinding.bindLegacy(newEntry::setName);
      seatsBinding.bindLegacy(newEntry::setSeats);
      prevBinding.bindLegacy(newEntry::setPrev);
      numSeatsBinding.bindLegacy(newEntry::setTotalSeats);
      entries.add(newEntry);
      return this;
    }
  }

  private static List<Party> extractPartyOrder(Map<Party, Integer> result) {
    return result.entrySet().stream()
        .filter(e -> e.getValue() > 0)
        .sorted(Map.Entry.<Party, Integer>comparingByValue().reversed())
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }

  private static List<Party> extractPartyOrder(
      Map<Party, Integer> result, Map<Party, Integer> diff) {
    return Stream.concat(result.keySet().stream(), diff.keySet().stream())
        .distinct()
        .filter(p -> result.getOrDefault(p, 0) > 0 || diff.getOrDefault(p, 0) != 0)
        .sorted(Comparator.comparing(p -> result.getOrDefault(p, 0)).reversed())
        .collect(Collectors.toList());
  }
}
