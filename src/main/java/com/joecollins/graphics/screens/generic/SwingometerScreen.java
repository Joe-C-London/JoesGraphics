package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.graphics.components.SwingometerFrame;
import com.joecollins.graphics.components.SwingometerFrameBuilder;
import com.joecollins.graphics.utils.StandardFont;
import com.joecollins.models.general.Party;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

public class SwingometerScreen extends JPanel {

  private SwingometerScreen(JLabel title, SwingometerFrame frame) {
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
      Binding<Map<T, Map<Party, Integer>>> prevVotes,
      Binding<Map<T, PartyResult>> results,
      Binding<Map<Party, Double>> swing,
      Binding<Pair<Party, Party>> parties,
      Binding<String> header) {
    return new Builder<>(prevVotes, results, parties, swing, header);
  }

  public static class Builder<T> {

    private static class Inputs<T> extends Bindable<Inputs.Property> {
      enum Property {
        PREV,
        RESULTS,
        FILTERED_SEATS,
        PARTIES,
        SWINGS,
        RANGE,
        LABEL_INCREMENT
      }

      Map<T, Map<Party, Integer>> prevVotes = new HashMap<>();
      Map<T, PartyResult> results = new HashMap<>();
      Set<T> filteredSeats = null;
      Pair<Party, Party> parties = ImmutablePair.of(null, null);
      Map<Party, Double> partySwings = new HashMap<>();
      Number range = 0.09999;
      int seatLabelIncrement = Integer.MAX_VALUE;

      public void setPrevVotes(Map<T, Map<Party, Integer>> prevVotes) {
        this.prevVotes = prevVotes;
        onPropertyRefreshed(Property.PREV);
      }

      public void setResults(Map<T, PartyResult> results) {
        this.results = results;
        onPropertyRefreshed(Property.RESULTS);
      }

      public void setSeatFilter(Set<T> filteredSeats) {
        this.filteredSeats = filteredSeats;
        onPropertyRefreshed(Property.FILTERED_SEATS);
      }

      public void setParties(Pair<Party, Party> parties) {
        this.parties = parties;
        onPropertyRefreshed(Property.PARTIES);
      }

      public void setPartySwings(Map<Party, Double> partySwings) {
        this.partySwings = partySwings;
        onPropertyRefreshed(Property.SWINGS);
      }

      public void setRange(Number range) {
        this.range = range;
        onPropertyRefreshed(Property.RANGE);
      }

      public void setSeatLabelIncrement(int seatLabelIncrement) {
        this.seatLabelIncrement = seatLabelIncrement;
        onPropertyRefreshed(Property.LABEL_INCREMENT);
      }
    }

    private final Inputs<T> inputs = new Inputs<>();
    private final BindingReceiver<String> header;

    private Builder(
        Binding<Map<T, Map<Party, Integer>>> prevVotesBinding,
        Binding<Map<T, PartyResult>> resultsBinding,
        Binding<Pair<Party, Party>> partiesBinding,
        Binding<Map<Party, Double>> partySwingsBinding,
        Binding<String> headerBinding) {
      prevVotesBinding.bind(inputs::setPrevVotes);
      resultsBinding.bind(inputs::setResults);
      partiesBinding.bind(inputs::setParties);
      partySwingsBinding.bind(inputs::setPartySwings);
      header = new BindingReceiver<>(headerBinding);
    }

    public Builder<T> withSeatLabelIncrements(Binding<Integer> incrementBinding) {
      incrementBinding.bind(inputs::setSeatLabelIncrement);
      return this;
    }

    public Builder<T> withSeatFilter(Binding<Set<T>> seatsFilterBinding) {
      seatsFilterBinding.bind(inputs::setSeatFilter);
      return this;
    }

    public SwingometerScreen build(Binding<String> title) {
      JLabel headerLabel = new JLabel();
      headerLabel.setFont(StandardFont.readBoldFont(32));
      headerLabel.setHorizontalAlignment(JLabel.CENTER);
      headerLabel.setBorder(new EmptyBorder(5, 0, -5, 0));
      title.bind(headerLabel::setText);

      var swingometer = createSwingometer();

      return new SwingometerScreen(headerLabel, swingometer);
    }

    private SwingometerFrame createSwingometer() {
      BindableList<Triple<Double, Color, Boolean>> dotsList = createDotsForSwingometer();

      return SwingometerFrameBuilder.basic(
              Binding.propertyBinding(
                  inputs,
                  in ->
                      ImmutablePair.of(
                          in.parties.getLeft().getColor(), in.parties.getRight().getColor()),
                  Inputs.Property.PARTIES),
              Binding.propertyBinding(
                  inputs,
                  in -> {
                    double left = in.partySwings.getOrDefault(in.parties.getLeft(), 0.0);
                    double right = in.partySwings.getOrDefault(in.parties.getRight(), 0.0);
                    return (right - left) / 2;
                  },
                  Inputs.Property.PARTIES,
                  Inputs.Property.SWINGS))
          .withDotsSolid(dotsList, Triple::getLeft, Triple::getMiddle, Triple::getRight)
          .withHeader(header.getBinding())
          .withRange(Binding.propertyBinding(inputs, in -> in.range, Inputs.Property.RANGE))
          .withTickInterval(() -> 0.01, n -> String.valueOf(Math.round(n.doubleValue() * 100)))
          .withLeftNeedingToWin(
              Binding.propertyBinding(
                  inputs,
                  in ->
                      getSwingNeededForMajority(
                          in.prevVotes, in.parties.getLeft(), in.parties.getRight()),
                  Inputs.Property.PREV,
                  Inputs.Property.PARTIES))
          .withRightNeedingToWin(
              Binding.propertyBinding(
                  inputs,
                  in ->
                      getSwingNeededForMajority(
                          in.prevVotes, in.parties.getRight(), in.parties.getLeft()),
                  Inputs.Property.PREV,
                  Inputs.Property.PARTIES))
          .withBucketSize(() -> 0.005)
          .withOuterLabels(getOuterLabels(), Triple::getLeft, Triple::getRight, Triple::getMiddle)
          .build();
    }

    private BindableList<Triple<Double, Color, String>> getOuterLabels() {
      BindableList<Triple<Double, Color, String>> labels = new BindableList<>();
      Binding<List<Triple<Double, Color, String>>> binding =
          Binding.propertyBinding(
              inputs,
              in -> {
                var leftSwingList =
                    createSwingList(
                        in.prevVotes.values(), in.parties.getLeft(), in.parties.getRight());
                var rightSwingList =
                    createSwingList(
                        in.prevVotes.values(), in.parties.getRight(), in.parties.getLeft());
                List<Triple<Double, Color, String>> ret = new LinkedList<>();
                int leftSeats = getNumSeats(leftSwingList);
                int rightSeats = getNumSeats(rightSwingList);
                int majority = in.prevVotes.size() / 2 + 1;
                addZeroLabel(ret, in.parties, leftSeats, rightSeats);
                addMajorityLabels(ret, in.parties, leftSwingList, rightSwingList, majority);
                addLeadChangeLabel(
                    ret, in.parties, leftSwingList, rightSwingList, leftSeats, rightSeats);
                addIncrementLabels(
                    ret,
                    leftSwingList,
                    rightSwingList,
                    leftSeats,
                    rightSeats,
                    in.prevVotes,
                    in.seatLabelIncrement,
                    in.parties);
                filterNearbyLabels(ret);
                return ret;
              },
              Inputs.Property.PREV,
              Inputs.Property.PARTIES,
              Inputs.Property.LABEL_INCREMENT);
      binding.bind(labels::setAll);
      return labels;
    }

    private void addIncrementLabels(
        List<Triple<Double, Color, String>> list,
        List<Double> leftSwingList,
        List<Double> rightSwingList,
        int leftSeats,
        int rightSeats,
        Map<T, Map<Party, Integer>> prevVotes,
        int seatLabelIncrement,
        Pair<Party, Party> parties) {
      for (int i = 0; i < prevVotes.size(); i += seatLabelIncrement) {
        if (i <= (leftSeats + rightSeats) / 2) continue;
        if (i <= leftSwingList.size()) {
          list.add(
              ImmutableTriple.of(
                  -leftSwingList.get(i - 1), parties.getLeft().getColor(), String.valueOf(i)));
        }
        if (i <= rightSwingList.size()) {
          list.add(
              ImmutableTriple.of(
                  rightSwingList.get(i - 1), parties.getRight().getColor(), String.valueOf(i)));
        }
      }
    }

    private void filterNearbyLabels(List<Triple<Double, Color, String>> ret) {
      Set<Range<Double>> ranges = new HashSet<>();
      for (var it = ret.iterator(); it.hasNext(); ) {
        var item = it.next();
        if (ranges.stream().anyMatch(r -> r.contains(item.getLeft()))) {
          it.remove();
        } else {
          ranges.add(Range.between(item.getLeft() - 0.005, item.getLeft() + 0.005));
        }
      }
    }

    private void addLeadChangeLabel(
        List<Triple<Double, Color, String>> list,
        Pair<Party, Party> parties,
        List<Double> leftSwingList,
        List<Double> rightSwingList,
        int leftSeats,
        int rightSeats) {
      if (leftSeats != rightSeats) {
        int newLeadSeats = (int) Math.ceil(0.5 * (leftSeats + rightSeats));
        double swing;
        Color color;
        if (leftSeats > rightSeats) {
          swing =
              rightSwingList.stream()
                  .skip(newLeadSeats - 1)
                  .findFirst()
                  .orElse(Double.POSITIVE_INFINITY);
          color = (leftSeats + rightSeats) % 2 == 0 ? Color.BLACK : parties.getRight().getColor();
        } else {
          swing =
              -1
                  * leftSwingList.stream()
                      .skip(newLeadSeats - 1)
                      .findFirst()
                      .orElse(Double.POSITIVE_INFINITY);
          color = (leftSeats + rightSeats) % 2 == 0 ? Color.BLACK : parties.getLeft().getColor();
        }
        list.add(ImmutableTriple.of(swing, color, String.valueOf(newLeadSeats)));
      }
    }

    private void addMajorityLabels(
        List<Triple<Double, Color, String>> list,
        Pair<Party, Party> parties,
        List<Double> leftSwingList,
        List<Double> rightSwingList,
        int majority) {
      double leftMajority =
          -1
              * leftSwingList.stream()
                  .skip(majority - 1)
                  .findFirst()
                  .orElse(Double.POSITIVE_INFINITY);
      double rightMajority =
          rightSwingList.stream().skip(majority - 1).findFirst().orElse(Double.POSITIVE_INFINITY);
      if (leftMajority != rightMajority || leftMajority < 0) {
        list.add(
            ImmutableTriple.of(
                leftMajority, parties.getLeft().getColor(), String.valueOf(majority)));
      }
      if (leftMajority != rightMajority || rightMajority > 0) {
        list.add(
            ImmutableTriple.of(
                rightMajority, parties.getRight().getColor(), String.valueOf(majority)));
      }
    }

    private void addZeroLabel(
        List<Triple<Double, Color, String>> list,
        Pair<Party, Party> parties,
        int leftSeats,
        int rightSeats) {
      if (leftSeats > rightSeats) {
        list.add(ImmutableTriple.of(0.0, parties.getLeft().getColor(), String.valueOf(leftSeats)));
      } else if (rightSeats > leftSeats) {
        list.add(
            ImmutableTriple.of(0.0, parties.getRight().getColor(), String.valueOf(rightSeats)));
      } else {
        list.add(ImmutableTriple.of(0.0, Color.BLACK, String.valueOf(rightSeats)));
      }
    }

    private int getNumSeats(List<Double> swings) {
      return (int) swings.stream().filter(s -> s < 0).count();
    }

    private Double getSwingNeededForMajority(
        Map<T, Map<Party, Integer>> votes, Party focusParty, Party compParty) {
      int majority = votes.size() / 2 + 1;
      return createSwingList(votes.values(), focusParty, compParty).stream()
          .skip(majority - 1)
          .findFirst()
          .orElse(Double.POSITIVE_INFINITY);
    }

    private List<Double> createSwingList(
        Collection<Map<Party, Integer>> results, Party focusParty, Party compParty) {
      return results.stream()
          .filter(
              m -> {
                Party winner =
                    m.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
                return winner.equals(focusParty) || winner.equals(compParty);
              })
          .map(
              m -> {
                int total = m.values().stream().mapToInt(i -> i).sum();
                int focus = m.getOrDefault(focusParty, 0);
                int comp = m.getOrDefault(compParty, 0);
                return 0.5 * (comp - focus) / total;
              })
          .sorted()
          .collect(Collectors.toList());
    }

    private BindableList<Triple<Double, Color, Boolean>> createDotsForSwingometer() {
      var emptyResult = new PartyResult(null, false);
      Binding<List<Triple<Double, Color, Boolean>>> dotsBinding =
          Binding.propertyBinding(
              inputs,
              in -> {
                return in.prevVotes.entrySet().stream()
                    .map(
                        e ->
                            ImmutableTriple.of(
                                e.getValue(),
                                in.results.getOrDefault(e.getKey(), emptyResult),
                                in.filteredSeats == null || in.filteredSeats.contains(e.getKey())))
                    .filter(
                        e -> {
                          Party winner =
                              e.getLeft().entrySet().stream()
                                  .max(Map.Entry.comparingByValue())
                                  .orElseThrow()
                                  .getKey();
                          return winner.equals(in.parties.getLeft())
                              || winner.equals(in.parties.getRight());
                        })
                    .map(
                        e -> {
                          int total = e.getLeft().values().stream().mapToInt(i -> i).sum();
                          int left = e.getLeft().getOrDefault(in.parties.getLeft(), 0);
                          int right = e.getLeft().getOrDefault(in.parties.getRight(), 0);
                          return ImmutableTriple.of(
                              0.5 * (left - right) / total, e.getMiddle().getColor(), e.getRight());
                        })
                    .collect(Collectors.toList());
              },
              Inputs.Property.PREV,
              Inputs.Property.RESULTS,
              Inputs.Property.PARTIES,
              Inputs.Property.FILTERED_SEATS);
      BindableList<Triple<Double, Color, Boolean>> dotsList = new BindableList<>();
      dotsBinding.bind(dotsList::setAll);
      return dotsList;
    }
  }
}
