package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.graphics.ImageGenerator;
import com.joecollins.graphics.components.BarFrame;
import com.joecollins.graphics.components.BarFrameBuilder;
import com.joecollins.graphics.components.MapFrame;
import com.joecollins.graphics.screens.generic.MapBuilder.Result;
import com.joecollins.graphics.utils.StandardFont;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Shape;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

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

  public MixedMemberResultPanel(
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
        Binding<Result> leadingParty,
        Binding<List<T>> focus,
        Binding<String> header) {
      this.mapBuilder = new MapBuilder(shapes, selectedShape, leadingParty, focus, header);
      return this;
    }

    public <T> Builder withResultMap(
        Binding<Map<T, Shape>> shapes,
        Binding<T> selectedShape,
        Binding<Result> leadingParty,
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
      return BarFrameBuilder.basicWithShapes(
              candidateVotes
                  .getBinding()
                  .merge(
                      winnerBinding,
                      (v, w) -> {
                        int total = v.values().stream().mapToInt(i -> i).sum();
                        LinkedHashMap<Candidate, Triple<Integer, Double, Boolean>> ret =
                            new LinkedHashMap<>();
                        for (var e : v.entrySet()) {
                          ret.put(
                              e.getKey(),
                              ImmutableTriple.of(
                                  e.getValue(),
                                  total == 0 ? Double.NaN : (1.0 * e.getValue() / total),
                                  e.getKey().equals(w)));
                        }
                        return ret;
                      }),
              c -> {
                if (showBothLines) {
                  return c.getName().toUpperCase()
                      + (c.isIncumbent() ? incumbentMarker : "")
                      + "\n"
                      + c.getParty().getName().toUpperCase();
                }
                return c.getName().toUpperCase()
                    + (c.isIncumbent() ? incumbentMarker : "")
                    + " ("
                    + c.getParty().getAbbreviation().toUpperCase()
                    + ")";
              },
              c -> c.getParty().getColor(),
              v -> Double.isNaN(v.getMiddle()) ? 0 : v.getMiddle(),
              v -> {
                if (Double.isNaN(v.getMiddle())) {
                  return "WAITING...";
                }
                if (showBothLines) {
                  return THOUSANDS_FORMAT.format(v.getLeft())
                      + "\n"
                      + PCT_FORMAT.format(v.getMiddle());
                }
                return THOUSANDS_FORMAT.format(v.getLeft())
                    + " ("
                    + PCT_FORMAT.format(v.getMiddle())
                    + ")";
              },
              (c, v) -> v.getRight() ? shape : null)
          .withHeader(candidateVoteHeader.getBinding())
          .withSubhead(candidateVoteSubheader.getBinding())
          .withMax(
              candidatePctReporting == null
                  ? Binding.fixedBinding(2.0 / 3)
                  : candidatePctReporting.getBinding(x -> 2.0 / 3 / Math.max(x, 1e-6)))
          .build();
    }

    private BarFrame createCandidateChange() {
      if (candidatePrev == null) {
        return null;
      }
      return BarFrameBuilder.basic(
              candidateVotes
                  .getBinding()
                  .merge(
                      candidatePrev.getBinding(),
                      (c, p) -> {
                        int currTotal = c.values().stream().mapToInt(i -> i).sum();
                        int prevTotal = p.values().stream().mapToInt(i -> i).sum();
                        LinkedHashMap<Party, Pair<Integer, Double>> ret = new LinkedHashMap<>();
                        if (currTotal == 0) {
                          return ret;
                        }
                        for (var e : c.entrySet()) {
                          ret.put(
                              e.getKey().getParty(),
                              ImmutablePair.of(
                                  e.getValue(),
                                  1.0 * e.getValue() / currTotal
                                      - 1.0
                                          * p.getOrDefault(e.getKey().getParty(), 0)
                                          / prevTotal));
                        }
                        for (var e : p.entrySet()) {
                          ret.putIfAbsent(
                              e.getKey(), ImmutablePair.of(0, -1.0 * e.getValue() / prevTotal));
                        }
                        return ret;
                      }),
              p -> p.getAbbreviation().toUpperCase(),
              Party::getColor,
              v -> v.getRight(),
              v -> PCT_DIFF_FORMAT.format(v.getRight()),
              (p, v) -> v.getLeft())
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
                    LinkedHashMap<Party, Pair<Integer, Double>> ret = new LinkedHashMap<>();
                    for (var e : v.entrySet()) {
                      ret.put(
                          e.getKey(),
                          ImmutablePair.of(
                              e.getValue(),
                              total == 0 ? Double.NaN : (1.0 * e.getValue() / total)));
                    }
                    return ret;
                  }),
              p -> p.getName().toUpperCase(),
              Party::getColor,
              v -> Double.isNaN(v.getRight()) ? 0 : v.getRight(),
              v ->
                  Double.isNaN(v.getRight())
                      ? "WAITING..."
                      : (THOUSANDS_FORMAT.format(v.getLeft())
                          + " ("
                          + PCT_FORMAT.format(v.getRight())
                          + ")"),
              (p, v) -> p == Party.OTHERS ? -1 : v.getLeft())
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
      return BarFrameBuilder.basic(
              partyVotes
                  .getBinding()
                  .merge(
                      partyPrev.getBinding(),
                      (c, p) -> {
                        int currTotal = c.values().stream().mapToInt(i -> i).sum();
                        int prevTotal = p.values().stream().mapToInt(i -> i).sum();
                        LinkedHashMap<Party, Pair<Integer, Double>> ret = new LinkedHashMap<>();
                        if (currTotal == 0) {
                          return ret;
                        }
                        for (var e : c.entrySet()) {
                          ret.put(
                              e.getKey(),
                              ImmutablePair.of(
                                  e.getValue(),
                                  1.0 * e.getValue() / currTotal
                                      - 1.0 * p.getOrDefault(e.getKey(), 0) / prevTotal));
                        }
                        for (var e : p.entrySet()) {
                          ret.putIfAbsent(
                              e.getKey(), ImmutablePair.of(0, -1.0 * e.getValue() / prevTotal));
                        }
                        return ret;
                      }),
              p -> p.getAbbreviation().toUpperCase(),
              Party::getColor,
              v -> v.getRight(),
              v -> PCT_DIFF_FORMAT.format(v.getRight()),
              (p, v) -> p == Party.OTHERS ? -1 : v.getLeft())
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
