package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.ImageGenerator;
import com.joecollins.graphics.components.BarFrame;
import com.joecollins.graphics.components.BarFrameBuilder;
import com.joecollins.graphics.components.MapFrame;
import com.joecollins.graphics.components.SwingFrame;
import com.joecollins.graphics.components.SwingFrameBuilder;
import com.joecollins.graphics.utils.StandardFont;
import com.joecollins.models.general.Aggregators;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import com.joecollins.models.general.PartyResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class MultiResultScreen extends JPanel {

  private List<ResultPanel> panels = new ArrayList<>();

  private <T> MultiResultScreen(Builder<T> builder, Binding<String> textHeader, boolean hasMap) {
    setBackground(Color.WHITE);
    setLayout(new BorderLayout());
    add(createHeaderLabel(textHeader), BorderLayout.NORTH);

    JPanel center = new JPanel();
    center.setLayout(new GridLayout(1, 0));
    center.setBackground(Color.WHITE);
    add(center, BorderLayout.CENTER);

    Binding.sizeBinding(builder.list)
        .bindLegacy(
            size -> {
              while (panels.size() < size) {
                ResultPanel newPanel =
                    new ResultPanel(
                        builder.incumbentMarker,
                        builder.swingPartyOrder,
                        hasMap,
                        builder.partiesOnly);
                center.add(newPanel);
                panels.add(newPanel);
              }
              while (panels.size() > size) {
                ResultPanel panel = panels.remove(size.intValue());
                panel.unbindAll();
                center.remove(panel);
              }
              int numRows = (size > 4 ? 2 : 1);
              center.setLayout(new GridLayout(numRows, 0));
              panels.forEach(
                  p -> {
                    p.displayBothRows = (numRows == 1);
                    p.setMaxBarsBinding(
                        Binding.fixedBinding(
                            (numRows == 2 ? 4 : 5) * (builder.partiesOnly ? 2 : 1)));
                    p.invalidate();
                    p.revalidate();
                  });
              EventQueue.invokeLater(this::repaint);
            });

    IndexedBinding.propertyBinding(builder.list, builder.votesFunc)
        .bind(
            (idx, votes) -> {
              panels.get(idx).setVotesBinding(votes);
            });

    IndexedBinding.propertyBinding(builder.list, builder.winnerFunc)
        .bind(
            (idx, winner) -> {
              panels.get(idx).setWinnerBinding(winner);
            });

    IndexedBinding.propertyBinding(builder.list, builder.runoffFunc)
        .bind((idx, runoff) -> panels.get(idx).setRunoffBinding(runoff));

    IndexedBinding.propertyBinding(builder.list, builder.pctReportingFunc)
        .bind(
            (idx, pctReporting) -> {
              panels.get(idx).setPctReportingBinding(pctReporting);
            });

    IndexedBinding.propertyBinding(builder.list, builder.headerFunc)
        .bind(
            (idx, header) -> {
              panels.get(idx).setHeaderBinding(header);
            });

    IndexedBinding.propertyBinding(builder.list, builder.subheadFunc)
        .bind(
            (idx, subhead) -> {
              panels.get(idx).setSubheadBinding(subhead);
            });

    if (builder.swingPartyOrder != null) {
      IndexedBinding.propertyBinding(builder.list, builder.prevFunc)
          .bind((idx, prev) -> panels.get(idx).setPrevBinding(prev));
      IndexedBinding.propertyBinding(builder.list, builder.swingHeaderFunc)
          .bind((idx, header) -> panels.get(idx).setSwingHeaderBinding(header));
    }

    if (builder.mapHeaderFunc != null) {
      IndexedBinding.propertyBinding(builder.list, builder.mapShapeFunc)
          .bind((idx, shapes) -> panels.get(idx).setMapShapeBinding(shapes));
      IndexedBinding.propertyBinding(builder.list, builder.mapFocusFunc)
          .bind((idx, shapes) -> panels.get(idx).setMapFocusBinding(shapes));
      IndexedBinding.propertyBinding(builder.list, builder.mapHeaderFunc)
          .bind((idx, header) -> panels.get(idx).setMapHeaderBinding(header));
    }
  }

  private static JLabel createHeaderLabel(Binding<String> textBinding) {
    JLabel headerLabel = new JLabel();
    headerLabel.setFont(StandardFont.readBoldFont(32));
    headerLabel.setHorizontalAlignment(JLabel.CENTER);
    headerLabel.setBorder(new EmptyBorder(5, 0, -5, 0));
    textBinding.bindLegacy(headerLabel::setText);
    return headerLabel;
  }

  public static <T> Builder<T> of(
      BindableList<T> list,
      Function<T, Binding<Map<Candidate, Integer>>> votesFunc,
      Function<T, Binding<String>> headerFunc,
      Function<T, Binding<String>> subheadFunc) {
    return new Builder<>(list, votesFunc, headerFunc, subheadFunc, false);
  }

  public static <T> Builder<T> ofParties(
      BindableList<T> list,
      Function<T, Binding<Map<Party, Integer>>> votesFunc,
      Function<T, Binding<String>> headerFunc,
      Function<T, Binding<String>> subheadFunc) {
    Function<T, Binding<Map<Candidate, Integer>>> adjustedVoteFunc =
        votesFunc.andThen(b -> b.map(m -> Aggregators.adjustKey(m, k -> new Candidate("", k))));
    return new Builder<>(list, adjustedVoteFunc, headerFunc, subheadFunc, true);
  }

  @FunctionalInterface
  private interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
  }

  public static class Builder<T> {
    private final BindableList<T> list;
    private final Function<T, Binding<Map<Candidate, Integer>>> votesFunc;
    private final Function<T, Binding<String>> headerFunc;
    private final Function<T, Binding<String>> subheadFunc;
    private final boolean partiesOnly;

    private Function<T, Binding<Double>> pctReportingFunc = t -> Binding.fixedBinding(1.0);
    private Function<T, Binding<Candidate>> winnerFunc = t -> Binding.fixedBinding(null);
    private Function<T, Binding<Set<Candidate>>> runoffFunc = t -> Binding.fixedBinding(Set.of());
    private String incumbentMarker = "";

    private Function<T, Binding<Map<Party, Integer>>> prevFunc = null;
    private Function<T, Binding<String>> swingHeaderFunc = null;
    private Comparator<Party> swingPartyOrder = null;

    private Function<T, List<Pair<Shape, Binding<Color>>>> mapShapeFunc = null;
    private Function<T, List<Shape>> mapFocusFunc = null;
    private Function<T, Binding<String>> mapHeaderFunc = null;

    private Builder(
        BindableList<T> list,
        Function<T, Binding<Map<Candidate, Integer>>> votesFunc,
        Function<T, Binding<String>> headerFunc,
        Function<T, Binding<String>> subheadFunc,
        boolean partiesOnly) {
      this.list = list;
      this.votesFunc = votesFunc;
      this.headerFunc = headerFunc;
      this.subheadFunc = subheadFunc;
      this.partiesOnly = partiesOnly;
    }

    public Builder<T> withIncumbentMarker(String incumbentMarker) {
      this.incumbentMarker = incumbentMarker;
      return this;
    }

    public Builder<T> withWinner(Function<T, Binding<Candidate>> winnerFunc) {
      this.winnerFunc = winnerFunc;
      return this;
    }

    public Builder<T> withRunoff(Function<T, Binding<Set<Candidate>>> runoffFunc) {
      this.runoffFunc = runoffFunc;
      return this;
    }

    public Builder<T> withPctReporting(Function<T, Binding<Double>> pctReportingFunc) {
      this.pctReportingFunc = pctReportingFunc;
      return this;
    }

    public Builder<T> withPrev(
        Function<T, Binding<Map<Party, Integer>>> prevFunc,
        Function<T, Binding<String>> swingHeaderFunc,
        Comparator<Party> swingPartyOrder) {
      this.prevFunc = prevFunc;
      this.swingHeaderFunc = swingHeaderFunc;
      this.swingPartyOrder = swingPartyOrder;
      return this;
    }

    public <K> Builder<T> withMap(
        Function<T, Map<K, Shape>> shapesFunc,
        Function<T, K> selectedShapeFunc,
        Function<T, Binding<PartyResult>> leadingPartyFunc,
        Function<T, List<K>> focusFunc,
        Function<T, Binding<String>> mapHeaderFunc) {
      return withMap(
          shapesFunc, selectedShapeFunc, leadingPartyFunc, focusFunc, focusFunc, mapHeaderFunc);
    }

    public <K> Builder<T> withMap(
        Function<T, Map<K, Shape>> shapesFunc,
        Function<T, K> selectedShapeFunc,
        Function<T, Binding<PartyResult>> leadingPartyFunc,
        Function<T, List<K>> focusFunc,
        Function<T, List<K>> additionalHighlightsFunc,
        Function<T, Binding<String>> mapHeaderFunc) {
      this.mapHeaderFunc = mapHeaderFunc;
      this.mapFocusFunc =
          t -> {
            List<K> focus = focusFunc.apply(t);
            if (focus == null) return List.of();
            Map<K, Shape> shapes = shapesFunc.apply(t);
            return focus.stream().map(shapes::get).collect(Collectors.toList());
          };
      this.mapShapeFunc =
          t -> {
            K selected = selectedShapeFunc.apply(t);
            List<K> focus = focusFunc.apply(t);
            List<K> additionalHighlight = additionalHighlightsFunc.apply(t);
            Binding<PartyResult> leader =
                leadingPartyFunc.apply(t).map(p -> p == null ? PartyResult.NO_RESULT : p);
            return shapesFunc.apply(t).entrySet().stream()
                .map(
                    e -> {
                      if (e.getKey().equals(selected)) {
                        return ImmutablePair.of(e.getValue(), leader.map(PartyResult::getColor));
                      }
                      if (focus == null || focus.isEmpty() || focus.contains(e.getKey())) {
                        return ImmutablePair.of(
                            e.getValue(), Binding.fixedBinding(Color.LIGHT_GRAY));
                      }
                      if (additionalHighlight != null && additionalHighlight.contains(e.getKey())) {
                        return ImmutablePair.of(
                            e.getValue(), Binding.fixedBinding(Color.LIGHT_GRAY));
                      }
                      return ImmutablePair.of(
                          e.getValue(), Binding.fixedBinding(new Color(220, 220, 220)));
                    })
                .collect(Collectors.toList());
          };
      return this;
    }

    public MultiResultScreen build(Binding<String> textHeader) {
      return new MultiResultScreen(this, textHeader, mapHeaderFunc != null);
    }
  }

  private static class Result extends Bindable<Result.Property> {
    private enum Property {
      VOTES,
      WINNER,
      RUNOFF,
      MAX_BARS
    }

    private Map<Candidate, Integer> votes = new HashMap<>();
    private Candidate winner;
    private Set<Candidate> runoff = Set.of();
    private int maxBars;

    private void setVotes(Map<Candidate, Integer> votes) {
      this.votes = votes;
      onPropertyRefreshed(Property.VOTES);
    }

    private void setWinner(Candidate winner) {
      this.winner = winner;
      onPropertyRefreshed(Property.WINNER);
    }

    private void setRunoff(Set<Candidate> runoff) {
      this.runoff = (runoff == null ? Set.of() : runoff);
      onPropertyRefreshed(Property.RUNOFF);
    }

    private void setMaxBars(int maxBars) {
      this.maxBars = maxBars;
      onPropertyRefreshed(Property.MAX_BARS);
    }
  }

  private static class ResultPanel extends JPanel {

    private BarFrame barFrame;
    private SwingFrame swingFrame = null;
    private MapFrame mapFrame = null;
    private boolean displayBothRows = true;

    private final String incumbentMarker;

    private final WrappedBinding<Map<Candidate, Integer>> votes =
        new WrappedBinding<>(HashMap::new);
    private final WrappedBinding<Double> pctReporting = new WrappedBinding<>(() -> 1.0);
    private final WrappedBinding<Candidate> winner = new WrappedBinding<>(() -> null);
    private final WrappedBinding<Set<Candidate>> runoff = new WrappedBinding<>(Set::of);
    private final WrappedBinding<Map<Party, Integer>> prevVotes =
        new WrappedBinding<>(HashMap::new);
    private final WrappedBinding<Integer> maxBars = new WrappedBinding<>(() -> 5);

    ResultPanel(
        String incumbentMarker,
        Comparator<Party> swingPartyOrder,
        boolean hasMap,
        boolean partiesOnly) {
      this.incumbentMarker = incumbentMarker;
      setBackground(Color.WHITE);
      setLayout(new ResultPanelLayout());

      Result result = new Result();
      votes.getBinding().bindLegacy(result::setVotes);
      winner.getBinding().bindLegacy(result::setWinner);
      runoff.getBinding().bindLegacy(result::setRunoff);
      maxBars.getBinding().bindLegacy(result::setMaxBars);
      Binding<List<BarFrameBuilder.BasicBar>> bars =
          Binding.propertyBinding(
              result,
              r -> {
                int total = r.votes.values().stream().mapToInt(i -> i).sum();
                return Aggregators.topAndOthers(r.votes, r.maxBars, Candidate.OTHERS, r.winner)
                    .entrySet()
                    .stream()
                    .sorted(
                        Comparator.<Map.Entry<Candidate, Integer>>comparingInt(
                                e ->
                                    e.getKey() == Candidate.OTHERS
                                        ? Integer.MIN_VALUE
                                        : e.getValue())
                            .reversed())
                    .map(
                        e -> {
                          Candidate candidate = e.getKey();
                          int votes = e.getValue();
                          double pct = 1.0 * votes / total;
                          Shape shape;
                          if (candidate == r.winner) shape = ImageGenerator.createHalfTickShape();
                          else if (r.runoff.contains(candidate))
                            shape = ImageGenerator.createHalfRunoffShape();
                          else shape = null;
                          String leftLabel;
                          if (partiesOnly) {
                            leftLabel = candidate.getParty().getName().toUpperCase();
                          } else if (candidate == Candidate.OTHERS) {
                            leftLabel = "OTHERS";
                          } else {
                            leftLabel =
                                candidate.getName().toUpperCase()
                                    + "\n"
                                    + candidate.getParty().getAbbreviation()
                                    + (candidate.isIncumbent() ? " " + incumbentMarker : "");
                          }
                          String rightLabel;
                          if (Double.isNaN(pct)) {
                            rightLabel = "WAITING...";
                          } else if (partiesOnly) {
                            rightLabel = new DecimalFormat("0.0%").format(pct);
                          } else {
                            rightLabel =
                                new DecimalFormat("#,##0").format(votes)
                                    + "\n"
                                    + new DecimalFormat("0.0%").format(pct);
                          }
                          return new BarFrameBuilder.BasicBar(
                              leftLabel,
                              candidate.getParty().getColor(),
                              Double.isNaN(pct) ? 0 : pct,
                              rightLabel,
                              shape);
                        })
                    .collect(Collectors.toList());
              },
              Result.Property.VOTES,
              Result.Property.WINNER,
              Result.Property.RUNOFF,
              Result.Property.MAX_BARS);

      barFrame =
          BarFrameBuilder.basic(bars)
              .withMax(pctReporting.getBinding().map(d -> 0.5 / Math.max(1e-6, d)))
              .build();
      add(barFrame);

      if (swingPartyOrder != null) {
        swingFrame =
            SwingFrameBuilder.prevCurr(
                    prevVotes.getBinding(),
                    votes
                        .getBinding()
                        .map(
                            m -> {
                              Map<Party, Integer> ret = new LinkedHashMap<>();
                              m.forEach((k, v) -> ret.merge(k.getParty(), v, Integer::sum));
                              return ret;
                            }),
                    swingPartyOrder)
                .build();
        add(swingFrame);
      }

      if (hasMap) {
        mapFrame = new MapFrame();
        add(mapFrame);
      }
    }

    void setVotesBinding(Binding<Map<Candidate, Integer>> votes) {
      this.votes.setBinding(votes);
    }

    void setHeaderBinding(Binding<String> headerBinding) {
      barFrame.setHeaderBinding(headerBinding);
    }

    void setSubheadBinding(Binding<String> subheadBinding) {
      barFrame.setSubheadTextBinding(subheadBinding);
    }

    void setWinnerBinding(Binding<Candidate> winnerBinding) {
      this.winner.setBinding(winnerBinding);
    }

    void setRunoffBinding(Binding<Set<Candidate>> runoffBinding) {
      this.runoff.setBinding(runoffBinding);
    }

    void setPctReportingBinding(Binding<Double> pctReportingBinding) {
      this.pctReporting.setBinding(pctReportingBinding);
    }

    void setPrevBinding(Binding<Map<Party, Integer>> prevBinding) {
      prevVotes.setBinding(prevBinding);
    }

    void setSwingHeaderBinding(Binding<String> swingLabelBinding) {
      if (swingFrame != null) {
        swingFrame.setHeaderBinding(swingLabelBinding);
      }
    }

    void setMapShapeBinding(List<Pair<Shape, Binding<Color>>> shapes) {
      if (mapFrame != null) {
        mapFrame.setNumShapesBinding(Binding.fixedBinding(shapes.size()));
        mapFrame.setShapeBinding(
            IndexedBinding.listBinding(
                shapes.stream().map(Pair::getKey).collect(Collectors.toList())));
        mapFrame.setColorBinding(IndexedBinding.listBinding(shapes, Pair::getValue));
      }
    }

    void setMapFocusBinding(List<Shape> shapes) {
      if (mapFrame != null) {
        mapFrame.setFocusBoxBinding(
            Binding.fixedBinding(
                shapes.stream()
                    .map(Shape::getBounds2D)
                    .reduce(Rectangle2D::createUnion)
                    .orElse(null)));
      }
    }

    void setMapHeaderBinding(Binding<String> mapLabelBinding) {
      if (mapFrame != null) {
        mapFrame.setHeaderBinding(mapLabelBinding);
      }
    }

    void setMaxBarsBinding(Binding<Integer> maxBarsBinding) {
      this.maxBars.setBinding(maxBarsBinding);
    }

    void unbindAll() {
      setVotesBinding(Map::of);
      setHeaderBinding(() -> "");
      setSubheadBinding(() -> "");
      setWinnerBinding(() -> null);
      setRunoffBinding(Set::of);
      setPctReportingBinding(() -> 0.0);
      setPrevBinding(Map::of);
      setSwingHeaderBinding(() -> "");
      setMapShapeBinding(List.of());
      setMapFocusBinding(List.of());
      setMapHeaderBinding(() -> "");
      setMaxBarsBinding(() -> 5);
    }

    private class ResultPanelLayout implements LayoutManager {

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
        int width = parent.getWidth();
        int height = parent.getHeight();
        barFrame.setLocation(5, 5);
        boolean barsOnly = !displayBothRows || (swingFrame == null && mapFrame == null);
        barFrame.setSize(width - 10, height * (barsOnly ? 3 : 2) / 3 - 10);
        if (swingFrame != null) {
          swingFrame.setLocation(5, height * 2 / 3 + 5);
          swingFrame.setSize(width / (mapFrame == null ? 1 : 2) - 10, height / 3 - 10);
          swingFrame.setVisible(displayBothRows);
        }
        if (mapFrame != null) {
          mapFrame.setLocation((swingFrame == null ? 0 : width / 2) + 5, height * 2 / 3 + 5);
          mapFrame.setSize(width / (swingFrame == null ? 1 : 2) - 10, height / 3 - 10);
          mapFrame.setVisible(displayBothRows);
        }
      }
    }
  }

  private static class WrappedBinding<T> extends Bindable<WrappedBinding.Property> {
    private enum Property {
      PROP
    }

    private Binding<T> underBinding = () -> null;

    private T value = null;

    WrappedBinding(Binding<T> binding) {
      setBinding(binding);
    }

    Binding<T> getBinding() {
      return Binding.propertyBinding(this, t -> t.value, Property.PROP);
    }

    void setBinding(Binding<T> underBinding) {
      this.underBinding.unbind();
      this.underBinding = underBinding;
      this.underBinding.bindLegacy(this::setValue);
    }

    private void setValue(T value) {
      this.value = value;
      onPropertyRefreshed(Property.PROP);
    }
  }
}
