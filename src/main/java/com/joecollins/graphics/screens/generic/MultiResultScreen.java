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
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

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
        .bind(
            size -> {
              while (panels.size() < size) {
                ResultPanel newPanel =
                    new ResultPanel(builder.incumbentMarker, builder.swingPartyOrder, hasMap);
                center.add(newPanel);
                panels.add(newPanel);
              }
              while (panels.size() > size) {
                ResultPanel panel = panels.remove(size.intValue());
                panel.unbindAll();
                center.remove(panel);
              }
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
    textBinding.bind(headerLabel::setText);
    return headerLabel;
  }

  public static <T> Builder<T> of(
      BindableList<T> list,
      Function<T, Binding<Map<Candidate, Integer>>> votesFunc,
      Function<T, Binding<String>> headerFunc,
      Function<T, Binding<String>> subheadFunc) {
    return new Builder<>(list, votesFunc, headerFunc, subheadFunc);
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

    private Function<T, Binding<Double>> pctReportingFunc = t -> Binding.fixedBinding(1.0);
    private Function<T, Binding<Candidate>> winnerFunc = t -> Binding.fixedBinding(null);
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
        Function<T, Binding<String>> subheadFunc) {
      this.list = list;
      this.votesFunc = votesFunc;
      this.headerFunc = headerFunc;
      this.subheadFunc = subheadFunc;
    }

    public Builder<T> withIncumbentMarker(String incumbentMarker) {
      this.incumbentMarker = incumbentMarker;
      return this;
    }

    public Builder<T> withWinner(Function<T, Binding<Candidate>> winnerFunc) {
      this.winnerFunc = winnerFunc;
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
        Function<T, Binding<MapBuilder.Result>> leadingPartyFunc,
        Function<T, List<K>> focusFunc,
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
            Binding<MapBuilder.Result> leader = leadingPartyFunc.apply(t);
            return shapesFunc.apply(t).entrySet().stream()
                .map(
                    e -> {
                      if (e.getKey().equals(selected)) {
                        return ImmutablePair.of(
                            e.getValue(), leader.map(MapBuilder.Result::getColor));
                      }
                      if (focus == null || focus.isEmpty() || focus.contains(e.getKey())) {
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

  private static class ResultPanel extends JPanel {

    private BarFrame barFrame;
    private SwingFrame swingFrame = null;
    private MapFrame mapFrame = null;

    private final String incumbentMarker;

    private final WrappedBinding<Map<Candidate, Integer>> votes =
        new WrappedBinding<>(HashMap::new);
    private final WrappedBinding<Double> pctReporting = new WrappedBinding<>(() -> 1.0);
    private final WrappedBinding<Candidate> winner = new WrappedBinding<>(() -> null);
    private final WrappedBinding<Map<Party, Integer>> prevVotes =
        new WrappedBinding<>(HashMap::new);

    ResultPanel(String incumbentMarker, Comparator<Party> swingPartyOrder, boolean hasMap) {
      this.incumbentMarker = incumbentMarker;
      setBackground(Color.WHITE);
      setLayout(new ResultPanelLayout());
      barFrame =
          BarFrameBuilder.basicWithShapes(
                  votes
                      .getBinding()
                      .merge(
                          winner.getBinding(),
                          (m, w) -> {
                            int total = m.values().stream().mapToInt(i -> i).sum();
                            Map<Candidate, Triple<Integer, Double, Boolean>> ret =
                                new LinkedHashMap<>();
                            m.forEach(
                                (k, v) ->
                                    ret.put(
                                        k, ImmutableTriple.of(v, 1.0 * v / total, k.equals(w))));
                            return ret;
                          }),
                  p ->
                      p.getName().toUpperCase()
                          + "\n"
                          + p.getParty().getAbbreviation()
                          + (p.isIncumbent() ? (" " + incumbentMarker) : ""),
                  p -> p.getParty().getColor(),
                  v -> Double.isNaN(v.getMiddle()) ? 0 : v.getMiddle(),
                  v ->
                      Double.isNaN(v.getMiddle())
                          ? "WAITING..."
                          : new DecimalFormat("#,##0").format(v.getLeft())
                              + "\n"
                              + new DecimalFormat("0.0%").format(v.getMiddle()),
                  (p, v) -> v.getRight() ? ImageGenerator.createHalfTickShape() : null)
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

    void unbindAll() {
      setVotesBinding(Map::of);
      setHeaderBinding(() -> "");
      setSubheadBinding(() -> "");
      setWinnerBinding(() -> null);
      setPctReportingBinding(() -> 0.0);
      setPrevBinding(Map::of);
      setSwingHeaderBinding(() -> "");
      setMapShapeBinding(List.of());
      setMapFocusBinding(List.of());
      setMapHeaderBinding(() -> "");
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
        boolean barsOnly = swingFrame == null && mapFrame == null;
        barFrame.setSize(width - 10, height * (barsOnly ? 3 : 2) / 3 - 10);
        if (swingFrame != null) {
          swingFrame.setLocation(5, height * 2 / 3 + 5);
          swingFrame.setSize(width / (mapFrame == null ? 1 : 2) - 10, height / 3 - 10);
        }
        if (mapFrame != null) {
          mapFrame.setLocation((swingFrame == null ? 0 : width / 2) + 5, height * 2 / 3 + 5);
          mapFrame.setSize(width / (swingFrame == null ? 1 : 2) - 10, height / 3 - 10);
        }
      }
    }
  }

  private static class WrappedBinding<T> extends Bindable {
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
      this.underBinding.bind(this::setValue);
    }

    private void setValue(T value) {
      this.value = value;
      onPropertyRefreshed(Property.PROP);
    }
  }
}
