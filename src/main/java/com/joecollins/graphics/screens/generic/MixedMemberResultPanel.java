package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.graphics.ImageGenerator;
import com.joecollins.graphics.components.BarFrame;
import com.joecollins.graphics.components.BarFrameBuilder;
import com.joecollins.graphics.components.MapFrame;
import com.joecollins.graphics.utils.StandardFont;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import com.joecollins.models.general.PartyResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Shape;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class MixedMemberResultPanel extends JPanel {

  private static final DecimalFormat PCT_FORMAT = new DecimalFormat("0.0%");
  private static final DecimalFormat PCT_DIFF_FORMAT = new DecimalFormat("+0.0%;-0.0%");
  private static final DecimalFormat THOUSANDS_FORMAT = new DecimalFormat("#,##0");

  private final JLabel label;
  private final BarFrame candidateFrame;
  private final BarFrame candidateChangeFrame;
  private final BarFrame partyFrame;
  private final BarFrame partyChangeFrame;
  private final MapFrame mapFrame;

  private MixedMemberResultPanel(
      JLabel label,
      BarFrame candidateFrame,
      BarFrame candidateChangeFrame,
      BarFrame partyFrame,
      BarFrame partyChangeFrame,
      MapFrame mapFrame) {
    this.label = label;
    this.candidateFrame = candidateFrame;
    this.candidateChangeFrame = candidateChangeFrame;
    this.partyFrame = partyFrame;
    this.partyChangeFrame = partyChangeFrame;
    this.mapFrame = mapFrame;

    setLayout(new BorderLayout());
    setBackground(Color.WHITE);

    add(label, BorderLayout.NORTH);

    JPanel panel = new JPanel();
    panel.setLayout(new ScreenLayout());
    panel.setBackground(Color.WHITE);
    add(panel, BorderLayout.CENTER);

    panel.add(candidateFrame);
    if (candidateChangeFrame != null) {
      panel.add(candidateChangeFrame);
    }
    panel.add(partyFrame);
    if (partyChangeFrame != null) {
      panel.add(partyChangeFrame);
    }
    panel.add(mapFrame);
  }

  private class ScreenLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      return new Dimension(1024, 512);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      return new Dimension(0, 0);
    }

    @Override
    public void layoutContainer(Container parent) {
      int width = parent.getWidth();
      int height = parent.getHeight();
      candidateFrame.setLocation(5, 5);
      candidateFrame.setSize(
          width * 3 / 5 - 10, height / (candidateChangeFrame == null ? 1 : 2) - 10);
      if (candidateChangeFrame != null) {
        candidateChangeFrame.setLocation(5, height / 2 + 5);
        candidateChangeFrame.setSize(width * 3 / 5 - 10, height / 2 - 10);
      }
      partyFrame.setLocation(width * 3 / 5 + 5, 5);
      partyFrame.setSize(width * 2 / 5 - 10, height / (partyChangeFrame == null ? 2 : 3) - 10);
      if (partyChangeFrame != null) {
        partyChangeFrame.setLocation(width * 3 / 5 + 5, height / 3 + 5);
        partyChangeFrame.setSize(width * 2 / 5 - 10, height / 3 - 10);
      }
      mapFrame.setLocation(width * 3 / 5 + 5, height * 2 / (partyChangeFrame == null ? 4 : 3) + 5);
      mapFrame.setSize(width * 2 / 5 - 10, height / (partyChangeFrame == null ? 2 : 3) - 10);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private BindingReceiver<? extends Map<Candidate, Integer>> candidateVotes;
    private BindingReceiver<? extends Map<Party, Integer>> candidatePrev;
    private BindingReceiver<Double> candidatePctReporting;
    private BindingReceiver<Candidate> winner;
    private BindingReceiver<? extends Map<Party, Integer>> partyVotes;
    private BindingReceiver<? extends Map<Party, Integer>> partyPrev;
    private BindingReceiver<Double> partyPctReporting;
    private String incumbentMarker = "";

    private BindingReceiver<String> candidateVoteHeader;
    private BindingReceiver<String> candidateVoteSubheader;
    private BindingReceiver<String> candidateChangeHeader;
    private BindingReceiver<String> partyVoteHeader;
    private BindingReceiver<String> partyChangeHeader;

    protected MapBuilder mapBuilder;

    public Builder withCandidateVotes(
        Binding<? extends Map<Candidate, Integer>> votes, Binding<String> header) {
      return withCandidateVotes(votes, header, () -> null);
    }

    public Builder withCandidateVotes(
        Binding<? extends Map<Candidate, Integer>> votes,
        Binding<String> header,
        Binding<String> subheader) {
      this.candidateVotes = new BindingReceiver<>(votes);
      this.candidateVoteHeader = new BindingReceiver<>(header);
      this.candidateVoteSubheader = new BindingReceiver<>(subheader);
      return this;
    }

    public Builder withIncumbentMarker(String incumbentMarker) {
      this.incumbentMarker = " " + incumbentMarker;
      return this;
    }

    public Builder withWinner(Binding<Candidate> winner) {
      this.winner = new BindingReceiver<>(winner);
      return this;
    }

    public Builder withPrevCandidateVotes(
        Binding<? extends Map<Party, Integer>> votes, Binding<String> header) {
      this.candidatePrev = new BindingReceiver<>(votes);
      this.candidateChangeHeader = new BindingReceiver<>(header);
      return this;
    }

    public Builder withPartyVotes(
        Binding<? extends Map<Party, Integer>> votes, Binding<String> header) {
      this.partyVotes = new BindingReceiver<>(votes);
      this.partyVoteHeader = new BindingReceiver<>(header);
      return this;
    }

    public Builder withPrevPartyVotes(
        Binding<? extends Map<Party, Integer>> votes, Binding<String> header) {
      this.partyPrev = new BindingReceiver<>(votes);
      this.partyChangeHeader = new BindingReceiver<>(header);
      return this;
    }

    public Builder withCandidatePctReporting(Binding<Double> pctReporting) {
      this.candidatePctReporting = new BindingReceiver<>(pctReporting);
      return this;
    }

    public Builder withPartyPctReporting(Binding<Double> pctReporting) {
      this.partyPctReporting = new BindingReceiver<>(pctReporting);
      return this;
    }

    public <T> Builder withResultMap(
        Binding<Map<T, Shape>> shapes,
        Binding<T> selectedShape,
        Binding<PartyResult> leadingParty,
        Binding<List<T>> focus,
        Binding<String> header) {
      this.mapBuilder = new MapBuilder(shapes, selectedShape, leadingParty, focus, header);
      return this;
    }

    public <T> Builder withResultMap(
        Binding<Map<T, Shape>> shapes,
        Binding<T> selectedShape,
        Binding<PartyResult> leadingParty,
        Binding<List<T>> focus,
        Binding<List<T>> additionalHighlight,
        Binding<String> header) {
      this.mapBuilder =
          new MapBuilder(shapes, selectedShape, leadingParty, focus, additionalHighlight, header);
      return this;
    }

    public MixedMemberResultPanel build(Binding<String> header) {
      return new MixedMemberResultPanel(
          createHeaderLabel(header),
          createCandidateVotes(),
          createCandidateChange(),
          createPartyVotes(),
          createPartyChange(),
          createMapFrame());
    }

    private static class Result extends Bindable<Result.Property> {
      private enum Property {
        VOTES,
        WINNER
      }

      private Map<Candidate, Integer> votes = new HashMap<>();
      private Candidate winner = null;

      public void setVotes(Map<Candidate, Integer> votes) {
        this.votes = votes;
        onPropertyRefreshed(Property.VOTES);
      }

      public void setWinner(Candidate winner) {
        this.winner = winner;
        onPropertyRefreshed(Property.WINNER);
      }
    }

    private BarFrame createCandidateVotes() {
      Binding<Candidate> winnerBinding =
          winner == null ? Binding.fixedBinding(null) : winner.getBinding();
      boolean showBothLines = candidatePrev == null;
      Shape shape =
          winner == null
              ? null
              : (showBothLines
                  ? ImageGenerator.createHalfTickShape()
                  : ImageGenerator.createTickShape());

      Result result = new Result();
      candidateVotes.getBinding().bind(result::setVotes);
      winnerBinding.bind(result::setWinner);
      Binding<List<BarFrameBuilder.BasicBar>> bars =
          Binding.propertyBinding(
              result,
              r -> {
                int total = r.votes.values().stream().mapToInt(i -> i).sum();
                return r.votes.entrySet().stream()
                    .sorted(
                        Comparator.<Map.Entry<Candidate, Integer>>comparingInt(
                                e ->
                                    e.getKey() == Candidate.OTHERS
                                        ? Integer.MIN_VALUE
                                        : e.getValue())
                            .reversed())
                    .map(
                        e -> {
                          var candidate = e.getKey();
                          int votes = e.getValue();
                          double pct = 1.0 * e.getValue() / total;
                          String leftLabel;
                          String rightLabel;
                          if (showBothLines) {
                            leftLabel =
                                candidate.getName().toUpperCase()
                                    + (candidate.isIncumbent() ? incumbentMarker : "")
                                    + "\n"
                                    + candidate.getParty().getName().toUpperCase();
                            rightLabel =
                                THOUSANDS_FORMAT.format(votes) + "\n" + PCT_FORMAT.format(pct);
                          } else {
                            leftLabel =
                                candidate.getName().toUpperCase()
                                    + (candidate.isIncumbent() ? incumbentMarker : "")
                                    + " ("
                                    + candidate.getParty().getAbbreviation().toUpperCase()
                                    + ")";
                            rightLabel =
                                THOUSANDS_FORMAT.format(votes)
                                    + " ("
                                    + PCT_FORMAT.format(pct)
                                    + ")";
                          }
                          return new BarFrameBuilder.BasicBar(
                              candidate == Candidate.OTHERS ? "OTHERS" : leftLabel,
                              candidate.getParty().getColor(),
                              Double.isNaN(pct) ? 0 : pct,
                              Double.isNaN(pct) ? "WAITING..." : rightLabel,
                              candidate == r.winner ? shape : null);
                        })
                    .collect(Collectors.toList());
              },
              Result.Property.VOTES,
              Result.Property.WINNER);

      return BarFrameBuilder.basic(bars)
          .withHeader(candidateVoteHeader.getBinding())
          .withSubhead(candidateVoteSubheader.getBinding())
          .withMax(
              candidatePctReporting == null
                  ? Binding.fixedBinding(2.0 / 3)
                  : candidatePctReporting.getBinding(x -> 2.0 / 3 / Math.max(x, 1e-6)))
          .build();
    }

    private static class Change<C> extends Bindable<Change.Property> {
      private enum Property {
        CURR,
        PREV
      }

      private Map<C, Integer> curr = new HashMap<>();
      private Map<Party, Integer> prev = new HashMap<>();

      public void setCurr(Map<C, Integer> curr) {
        this.curr = curr;
        onPropertyRefreshed(Property.CURR);
      }

      public void setPrev(Map<Party, Integer> prev) {
        this.prev = prev;
        onPropertyRefreshed(Property.PREV);
      }
    }

    private BarFrame createCandidateChange() {
      if (candidatePrev == null) {
        return null;
      }

      Change<Candidate> change = new Change<>();
      candidateVotes.getBinding().bind(change::setCurr);
      candidatePrev.getBinding().bind(change::setPrev);
      Binding<List<BarFrameBuilder.BasicBar>> bars =
          Binding.propertyBinding(
              change,
              r -> {
                int currTotal = r.curr.values().stream().mapToInt(i -> i).sum();
                if (currTotal == 0) {
                  return List.of();
                }
                int prevTotal = r.prev.values().stream().mapToInt(i -> i).sum();
                Set<Party> currParties =
                    r.curr.keySet().stream().map(Candidate::getParty).collect(Collectors.toSet());
                var matchingBars =
                    r.curr.entrySet().stream()
                        .sorted(
                            Comparator.<Map.Entry<Candidate, Integer>>comparingInt(
                                    e ->
                                        e.getKey() == Candidate.OTHERS
                                            ? Integer.MIN_VALUE
                                            : e.getValue())
                                .reversed())
                        .map(
                            e -> {
                              double pct =
                                  1.0 * e.getValue() / currTotal
                                      - 1.0
                                          * r.prev.getOrDefault(e.getKey().getParty(), 0)
                                          / prevTotal;
                              return new BarFrameBuilder.BasicBar(
                                  e.getKey().getParty().getAbbreviation().toUpperCase(),
                                  e.getKey().getParty().getColor(),
                                  pct,
                                  PCT_DIFF_FORMAT.format(pct));
                            });
                var nonMatchingBars =
                    r.prev.entrySet().stream()
                        .filter(e -> !currParties.contains(e.getKey()))
                        .map(
                            e -> {
                              double pct = -1.0 * e.getValue() / prevTotal;
                              return new BarFrameBuilder.BasicBar(
                                  e.getKey().getAbbreviation().toUpperCase(),
                                  e.getKey().getColor(),
                                  pct,
                                  PCT_DIFF_FORMAT.format(pct));
                            });
                return Stream.concat(matchingBars, nonMatchingBars).collect(Collectors.toList());
              },
              Change.Property.CURR,
              Change.Property.PREV);

      return BarFrameBuilder.basic(bars)
          .withHeader(candidateChangeHeader.getBinding())
          .withWingspan(
              candidatePctReporting == null
                  ? Binding.fixedBinding(0.05)
                  : candidatePctReporting.getBinding(x -> 0.05 / Math.max(x, 1e-6)))
          .build();
    }

    private BarFrame createPartyVotes() {
      return BarFrameBuilder.basic(
              partyVotes.getBinding(
                  v -> {
                    int total = v.values().stream().mapToInt(i -> i).sum();
                    return v.entrySet().stream()
                        .sorted(
                            Comparator.<Map.Entry<Party, Integer>>comparingInt(
                                    e ->
                                        e.getKey() == Party.OTHERS
                                            ? Integer.MIN_VALUE
                                            : e.getValue())
                                .reversed())
                        .map(
                            e -> {
                              double pct = 1.0 * e.getValue() / total;
                              return new BarFrameBuilder.BasicBar(
                                  e.getKey().getName().toUpperCase(),
                                  e.getKey().getColor(),
                                  Double.isNaN(pct) ? 0 : pct,
                                  Double.isNaN(pct)
                                      ? "WAITING..."
                                      : (THOUSANDS_FORMAT.format(e.getValue())
                                          + " ("
                                          + PCT_FORMAT.format(pct)
                                          + ")"));
                            })
                        .collect(Collectors.toList());
                  }))
          .withHeader(partyVoteHeader.getBinding())
          .withMax(
              partyPctReporting == null
                  ? Binding.fixedBinding(2.0 / 3)
                  : partyPctReporting.getBinding(x -> 2.0 / 3 / Math.max(x, 1e-6)))
          .build();
    }

    private BarFrame createPartyChange() {
      if (partyPrev == null) {
        return null;
      }

      Change<Party> change = new Change<>();
      partyVotes.getBinding().bind(change::setCurr);
      partyPrev.getBinding().bind(change::setPrev);
      Binding<List<BarFrameBuilder.BasicBar>> bars =
          Binding.propertyBinding(
              change,
              r -> {
                var c = r.curr;
                var p = r.prev;
                int currTotal = c.values().stream().mapToInt(i -> i).sum();
                if (currTotal == 0) {
                  return List.of();
                }
                int prevTotal = p.values().stream().mapToInt(i -> i).sum();
                var presentBars =
                    c.entrySet().stream()
                        .sorted(
                            Comparator.<Map.Entry<Party, Integer>>comparingInt(
                                    e ->
                                        e.getKey() == Party.OTHERS
                                            ? Integer.MIN_VALUE
                                            : e.getValue())
                                .reversed())
                        .map(
                            e -> {
                              double pct =
                                  1.0 * e.getValue() / currTotal
                                      - 1.0 * p.getOrDefault(e.getKey(), 0) / prevTotal;
                              return new BarFrameBuilder.BasicBar(
                                  e.getKey().getAbbreviation().toUpperCase(),
                                  e.getKey().getColor(),
                                  pct,
                                  PCT_DIFF_FORMAT.format(pct));
                            });
                var absentBars =
                    p.entrySet().stream()
                        .filter(e -> !c.containsKey(e.getKey()))
                        .map(
                            e -> {
                              double pct = -1.0 * e.getValue() / currTotal;
                              return new BarFrameBuilder.BasicBar(
                                  e.getKey().getAbbreviation().toUpperCase(),
                                  e.getKey().getColor(),
                                  pct,
                                  PCT_DIFF_FORMAT.format(pct));
                            });
                return Stream.concat(presentBars, absentBars).collect(Collectors.toList());
              },
              Change.Property.CURR,
              Change.Property.PREV);

      return BarFrameBuilder.basic(bars)
          .withHeader(partyChangeHeader.getBinding())
          .withWingspan(
              partyPctReporting == null
                  ? Binding.fixedBinding(0.05)
                  : partyPctReporting.getBinding(x -> 0.05 / Math.max(x, 1e-6)))
          .build();
    }

    private MapFrame createMapFrame() {
      return mapBuilder.createMapFrame();
    }
  }

  private static JLabel createHeaderLabel(Binding<String> textBinding) {
    JLabel headerLabel = new JLabel();
    headerLabel.setFont(StandardFont.readBoldFont(32));
    headerLabel.setHorizontalAlignment(JLabel.CENTER);
    headerLabel.setBorder(new EmptyBorder(5, 0, -5, 0));
    textBinding.bind(headerLabel::setText);
    return headerLabel;
  }
}
