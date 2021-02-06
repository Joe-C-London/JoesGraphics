package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.components.MultiSummaryFrame;
import com.joecollins.graphics.utils.StandardFont;
import com.joecollins.models.general.Aggregators;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import com.joecollins.models.general.PartyResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class TooCloseToCallScreen extends JPanel {

  private TooCloseToCallScreen(JLabel titleLabel, MultiSummaryFrame multiSummaryFrame) {
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

  public static <T> Builder<T> of(
      Binding<Map<T, Map<Candidate, Integer>>> votesBinding,
      Binding<Map<T, PartyResult>> resultBinding,
      Function<T, String> labelFunc,
      Binding<String> headerBinding) {
    return new Builder<>(headerBinding, votesBinding, resultBinding, labelFunc);
  }

  public static <T> Builder<T> ofParty(
      Binding<Map<T, Map<Party, Integer>>> votesBinding,
      Binding<Map<T, PartyResult>> resultBinding,
      Function<T, String> labelFunc,
      Binding<String> headerBinding) {
    return new Builder<>(
        headerBinding,
        votesBinding.map(
            all ->
                all.entrySet().stream()
                    .collect(
                        Collectors.toMap(
                            Map.Entry::getKey,
                            e -> Aggregators.adjustKey(e.getValue(), p -> new Candidate("", p))))),
        resultBinding,
        labelFunc);
  }

  private static class Input<T> extends Bindable<Input.Property> {
    enum Property {
      VOTES,
      RESULTS,
      PCT_REPORTING,
      MAX_ROWS,
      NUM_CANDIDATES
    }

    private Map<T, Map<Candidate, Integer>> votes = new HashMap<>();
    private Map<T, PartyResult> results = new HashMap<>();
    private Map<T, Double> pctReporting = new HashMap<>();
    private int maxRows = Integer.MAX_VALUE;
    private int numCandidates = 2;

    public void setVotes(Map<T, Map<Candidate, Integer>> votes) {
      this.votes = votes;
      onPropertyRefreshed(Property.VOTES);
    }

    public void setResults(Map<T, PartyResult> results) {
      this.results = results;
      onPropertyRefreshed(Property.RESULTS);
    }

    public void setPctReporting(Map<T, Double> pctReporting) {
      this.pctReporting = pctReporting;
      onPropertyRefreshed(Property.PCT_REPORTING);
    }

    public void setMaxRows(int maxRows) {
      this.maxRows = maxRows;
      onPropertyRefreshed(Property.MAX_ROWS);
    }

    public void setNumCandidates(int numCandidates) {
      this.numCandidates = numCandidates;
      onPropertyRefreshed(Property.NUM_CANDIDATES);
    }

    public BindableList<Entry<T>> toEntries() {
      BindableList<Entry<T>> entries = new BindableList<>();
      Binding.propertyBinding(
              this,
              t -> {
                return t.votes.entrySet().stream()
                    .map(
                        e ->
                            new Entry<>(
                                e.getKey(),
                                e.getValue(),
                                t.results.get(e.getKey()),
                                t.pctReporting.getOrDefault(e.getKey(), 0.0),
                                t.numCandidates))
                    .filter(e -> e.votes.values().stream().mapToInt(i -> i).sum() > 0)
                    .filter(e -> e.result == null || !e.result.isElected())
                    .sorted(Comparator.comparingInt(e -> e.lead))
                    .limit(t.maxRows)
                    .collect(Collectors.toList());
              },
              Property.VOTES,
              Property.RESULTS,
              Property.PCT_REPORTING,
              Property.MAX_ROWS,
              Property.NUM_CANDIDATES)
          .bindLegacy(entries::setAll);
      return entries;
    }
  }

  private static class Entry<T> {
    private final T key;
    private final Map<Candidate, Integer> votes;
    private final List<Map.Entry<Candidate, Integer>> topCandidates;
    private final int lead;
    private final PartyResult result;
    private final double pctReporting;
    private final int numCandidates;

    private Entry(
        T key,
        Map<Candidate, Integer> votes,
        PartyResult result,
        double pctReporting,
        int numCandidates) {
      this.key = key;
      this.votes = votes;
      this.result = result;
      this.pctReporting = pctReporting;
      this.numCandidates = numCandidates;
      this.topCandidates =
          votes.entrySet().stream()
              .sorted(Map.Entry.<Candidate, Integer>comparingByValue().reversed())
              .collect(Collectors.toList());
      this.lead =
          switch (topCandidates.size()) {
            case 0 -> 0;
            case 1 -> topCandidates.get(0).getValue();
            default -> topCandidates.get(0).getValue() - topCandidates.get(1).getValue();
          };
    }
  }

  public static class Builder<T> {

    private final BindingReceiver<String> header;
    private final BindingReceiver<Map<T, Map<Candidate, Integer>>> votes;
    private final BindingReceiver<Map<T, PartyResult>> results;
    private final Function<T, String> rowHeaderFunc;
    private BindingReceiver<Map<T, Double>> pctReporting = null;
    private BindingReceiver<Integer> rowsLimit = null;
    private BindingReceiver<Integer> numCandidates = null;

    public Builder(
        Binding<String> header,
        Binding<Map<T, Map<Candidate, Integer>>> votes,
        Binding<Map<T, PartyResult>> results,
        Function<T, String> rowHeaderFunc) {
      this.header = new BindingReceiver<>(header);
      this.votes = new BindingReceiver<>(votes);
      this.results = new BindingReceiver<>(results);
      this.rowHeaderFunc = rowHeaderFunc;
    }

    public Builder<T> withPctReporting(Binding<Map<T, Double>> pctReportingBinding) {
      this.pctReporting = new BindingReceiver<>(pctReportingBinding);
      return this;
    }

    public Builder<T> withMaxRows(Binding<Integer> rowsLimitBinding) {
      this.rowsLimit = new BindingReceiver<>(rowsLimitBinding);
      return this;
    }

    public Builder<T> withNumberOfCandidates(Binding<Integer> numCandidatesBinding) {
      this.numCandidates = new BindingReceiver<>(numCandidatesBinding);
      return this;
    }

    public TooCloseToCallScreen build(Binding<String> titleBinding) {
      JLabel headerLabel = new JLabel();
      headerLabel.setFont(StandardFont.readBoldFont(32));
      headerLabel.setHorizontalAlignment(JLabel.CENTER);
      headerLabel.setBorder(new EmptyBorder(5, 0, -5, 0));
      titleBinding.bindLegacy(headerLabel::setText);

      return new TooCloseToCallScreen(headerLabel, createFrame());
    }

    private MultiSummaryFrame createFrame() {
      Input<T> input = new Input<>();
      votes.getBinding().bindLegacy(input::setVotes);
      results.getBinding().bindLegacy(input::setResults);
      if (pctReporting != null) {
        pctReporting.getBinding().bindLegacy(input::setPctReporting);
      }
      if (rowsLimit != null) {
        rowsLimit.getBinding().bindLegacy(input::setMaxRows);
      }
      if (numCandidates != null) {
        numCandidates.getBinding().bindLegacy(input::setNumCandidates);
      }
      BindableList<Entry<T>> entries = input.toEntries();

      DecimalFormat thousandsFormatter = new DecimalFormat("#,##0");
      DecimalFormat pctFormatter = new DecimalFormat("0.0%");
      var frame = new MultiSummaryFrame();
      frame.setHeaderBinding(header.getBinding());
      frame.setNumRowsBinding(Binding.sizeBinding(entries));
      frame.setRowHeaderBinding(
          IndexedBinding.propertyBinding(entries, e -> rowHeaderFunc.apply(e.key)));
      frame.setValuesBinding(
          IndexedBinding.propertyBinding(
              entries,
              e -> {
                List<Pair<Color, String>> ret =
                    Stream.concat(
                            e.topCandidates.stream()
                                .map(
                                    v ->
                                        ImmutablePair.of(
                                            v.getKey().getParty().getColor(),
                                            v.getKey().getParty().getAbbreviation().toUpperCase()
                                                + ": "
                                                + thousandsFormatter.format(v.getValue()))),
                            Stream.generate(() -> ImmutablePair.of(Color.WHITE, "")))
                        .limit(e.numCandidates)
                        .collect(Collectors.toList());
                ret.add(
                    ImmutablePair.of(Color.WHITE, "LEAD: " + thousandsFormatter.format(e.lead)));
                if (pctReporting != null) {
                  ret.add(
                      ImmutablePair.of(Color.WHITE, pctFormatter.format(e.pctReporting) + " IN"));
                }
                return ret;
              }));
      return frame;
    }
  }
}
