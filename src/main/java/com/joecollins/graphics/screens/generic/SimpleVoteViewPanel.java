package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.Binding.BindingReceiver;
import com.joecollins.graphics.ImageGenerator;
import com.joecollins.graphics.components.BarFrame;
import com.joecollins.graphics.components.BarFrameBuilder;
import com.joecollins.graphics.components.MapFrame;
import com.joecollins.graphics.components.MapFrameBuilder;
import com.joecollins.graphics.components.SwingFrame;
import com.joecollins.graphics.components.SwingFrameBuilder;
import com.joecollins.graphics.screens.generic.SeatViewPanel.Builder;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class SimpleVoteViewPanel extends JPanel {

  private static final DecimalFormat VOTE_FORMAT = new DecimalFormat("#,##0");
  private static final DecimalFormat PCT_FORMAT = new DecimalFormat("0.0%");
  private static final DecimalFormat DIFF_FORMAT = new DecimalFormat("+0.0%;-0.0%");

  private final JLabel label;
  private final BarFrame voteFrame;
  private final BarFrame changeFrame;
  private final SwingFrame swingFrame;
  private final MapFrame mapFrame;

  public SimpleVoteViewPanel(
      JLabel label,
      BarFrame voteFrame,
      BarFrame changeFrame,
      SwingFrame swingFrame,
      MapFrame mapFrame) {
    this.label = label;
    this.voteFrame = voteFrame;
    this.changeFrame = changeFrame;
    this.swingFrame = swingFrame;
    this.mapFrame = mapFrame;

    setLayout(new BorderLayout());
    setBackground(Color.WHITE);

    add(label, BorderLayout.NORTH);

    JPanel panel = new JPanel();
    panel.setLayout(new ScreenLayout());
    panel.setBackground(Color.WHITE);
    add(panel, BorderLayout.CENTER);

    panel.add(voteFrame);
    panel.add(changeFrame);

    if (swingFrame != null) panel.add(swingFrame);
    if (mapFrame != null) panel.add(mapFrame);
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
      voteFrame.setLocation(5, 5);
      voteFrame.setSize(width * 3 / 5 - 10, height - 10);
      changeFrame.setLocation(width * 3 / 5 + 5, 5);
      changeFrame.setSize(width * 2 / 5 - 10, height * 2 / 3 - 10);
      swingFrame.setLocation(width * 3 / 5 + 5, height * 2 / 3 + 5);
      swingFrame.setSize(width * (mapFrame == null ? 2 : 1) / 5 - 10, height / 3 - 10);
      if (mapFrame != null) {
        mapFrame.setLocation(width * 4 / 5 + 5, height * 2 / 3 + 5);
        mapFrame.setSize(width / 5 - 10, height / 3 - 10);
      }
    }
  }

  private enum PartyEntryProp {
    CURR,
    PREV,
    DIFF,
    WINNER
  }

  private abstract static class PrevCurrEntryMap<CT> extends Bindable {
    private Map<CT, Result> curr = new LinkedHashMap<>();
    private Map<Party, Result> prev = new LinkedHashMap<>();

    protected abstract Party toParty(CT val);

    void setCurr(Map<CT, Integer> curr) {
      int total = Math.max(1, curr.values().stream().mapToInt(i -> i).sum());
      this.curr = new LinkedHashMap<>();
      curr.forEach(
          (c, v) -> this.curr.computeIfAbsent(c, x -> new Result()).setResult(v, 1.0 * v / total));
      onPropertyRefreshed(PartyEntryProp.CURR);
    }

    public void setWinner(CT winner) {
      this.curr.forEach((c, v) -> v.setWinner(c.equals(winner)));
      if (winner != null) {
        this.curr.computeIfAbsent(winner, x -> new Result()).setWinner(true);
      }
      onPropertyRefreshed(PartyEntryProp.WINNER);
    }

    void setPrev(Map<Party, Integer> prev) {
      int total = Math.max(1, prev.values().stream().mapToInt(i -> i).sum());
      this.prev = new LinkedHashMap<>();
      prev.forEach(
          (c, v) -> this.prev.computeIfAbsent(c, x -> new Result()).setResult(v, 1.0 * v / total));
      onPropertyRefreshed(PartyEntryProp.PREV);
    }

    Binding<Map<Party, Integer>> currPartyBinding() {
      return Binding.propertyBinding(
          this,
          m -> {
            LinkedHashMap<Party, Integer> ret = new LinkedHashMap<>();
            m.curr.forEach((c, v) -> ret.put(toParty(c), v.getVotes()));
            return ret;
          },
          PartyEntryProp.CURR);
    }

    Binding<Map<Party, Integer>> prevPartyBinding() {
      return Binding.propertyBinding(
          this,
          m -> {
            LinkedHashMap<Party, Integer> ret = new LinkedHashMap<>();
            m.prev.forEach((p, v) -> ret.put(p, v.getVotes()));
            return ret;
          },
          PartyEntryProp.PREV);
    }

    Binding<Map<CT, Result>> currPctBinding() {
      return Binding.propertyBinding(this, m -> m.curr, PartyEntryProp.CURR, PartyEntryProp.WINNER);
    }

    Binding<Map<Party, Result>> diffPctBinding() {
      return Binding.propertyBinding(
          this,
          m -> {
            Map<Party, Result> ret = new LinkedHashMap<>();
            if (curr.values().stream().mapToInt(Result::getVotes).sum() == 0) {
              return ret;
            }
            curr.forEach(
                (k, v) -> {
                  double d =
                      v.getPct()
                          - (prev.containsKey(toParty(k)) ? prev.get(toParty(k)).getPct() : 0);
                  ret.put(toParty(k), new Result(v.getVotes(), d));
                });
            prev.forEach(
                (k, v) -> {
                  double d = -(prev.containsKey(k) ? prev.get(k).getPct() : 0);
                  ret.putIfAbsent(k, new Result(0, d));
                });
            return ret;
          },
          PartyEntryProp.CURR,
          PartyEntryProp.PREV);
    }

    private static class Result {
      private int votes;
      private double pct;
      private boolean winner;

      public Result() {}

      public Result(int votes, double pct) {
        this.votes = votes;
        this.pct = pct;
      }

      public int getVotes() {
        return votes;
      }

      public double getPct() {
        return pct;
      }

      public void setResult(int votes, double pct) {
        this.votes = votes;
        this.pct = pct;
      }

      public boolean isWinner() {
        return winner;
      }

      public void setWinner(boolean winner) {
        this.winner = winner;
      }
    }
  }

  public static class Builder {

    private JLabel headerLabel;
    private BarFrameBuilder voteFrame;
    private BarFrameBuilder changeFrame;
    private SwingFrameBuilder swingFrame;
    private MapFrameBuilder mapFrame;

    public static Builder basicCurrPrev(
        Binding<? extends Map<Candidate, Integer>> currVotes,
        Binding<? extends Map<Party, Integer>> prevVotes,
        Binding<String> headerBinding,
        Binding<String> voteHeaderBinding,
        Binding<String> voteSubheadBinding,
        Binding<String> changeHeaderBinding,
        Binding<String> swingHeaderBinding,
        List<Party> swingPartyOrder) {
      return basicCurrPrev(
          currVotes,
          prevVotes,
          Binding.fixedBinding(1.0),
          headerBinding,
          voteHeaderBinding,
          voteSubheadBinding,
          changeHeaderBinding,
          swingHeaderBinding,
          swingPartyOrder);
    }

    public static Builder basicCurrPrev(
        Binding<? extends Map<Candidate, Integer>> currVotes,
        Binding<? extends Map<Party, Integer>> prevVotes,
        Binding<Double> pctReporting,
        Binding<String> headerBinding,
        Binding<String> voteHeaderBinding,
        Binding<String> voteSubheadBinding,
        Binding<String> changeHeaderBinding,
        Binding<String> swingHeaderBinding,
        List<Party> swingPartyOrder) {
      return basicCurrPrev(
          currVotes,
          () -> null,
          prevVotes,
          pctReporting,
          headerBinding,
          voteHeaderBinding,
          voteSubheadBinding,
          changeHeaderBinding,
          swingHeaderBinding,
          swingPartyOrder);
    }

    public static Builder basicCurrPrev(
        Binding<? extends Map<Candidate, Integer>> currVotes,
        Binding<Candidate> winner,
        Binding<? extends Map<Party, Integer>> prevVotes,
        Binding<Double> pctReporting,
        Binding<String> headerBinding,
        Binding<String> voteHeaderBinding,
        Binding<String> voteSubheadBinding,
        Binding<String> changeHeaderBinding,
        Binding<String> swingHeaderBinding,
        List<Party> swingPartyOrder) {
      Builder builder = new Builder();
      PrevCurrEntryMap<Candidate> map =
          new PrevCurrEntryMap<>() {
            @Override
            protected Party toParty(Candidate val) {
              return val.getParty();
            }
          };
      currVotes.bind(map::setCurr);
      prevVotes.bind(map::setPrev);
      winner.bind(map::setWinner);

      Shape tick = ImageGenerator.createHalfTickShape();
      BindingReceiver<Double> pctReportingReceiver = new BindingReceiver<>(pctReporting);
      builder.headerLabel = createLabel(headerBinding);
      builder.voteFrame =
          BarFrameBuilder.basicWithShapes(
                  map.currPctBinding(),
                  c -> c.getName().toUpperCase() + "\n" + c.getParty().getName().toUpperCase(),
                  c -> c.getParty().getColor(),
                  v -> v.getPct(),
                  v ->
                      v.getVotes() == 0
                          ? "WAITING..."
                          : (VOTE_FORMAT.format(v.getVotes())
                              + "\n"
                              + PCT_FORMAT.format(v.getPct())),
                  (c, v) -> v.isWinner() ? tick : null)
              .withHeader(voteHeaderBinding)
              .withSubhead(voteSubheadBinding)
              .withMax(pctReportingReceiver.getBinding(i -> 2.0 / 3 / Math.max(i, 1e-6)));
      builder.changeFrame =
          BarFrameBuilder.basic(
                  map.diffPctBinding(),
                  p -> p.getAbbreviation().toUpperCase(),
                  p -> p.getColor(),
                  v -> v.getPct(),
                  v -> DIFF_FORMAT.format(v.getPct()),
                  (p, v) -> v.getVotes())
              .withHeader(changeHeaderBinding)
              .withWingspan(pctReportingReceiver.getBinding(i -> 0.10 / Math.max(i, 1e-6)));
      builder.swingFrame =
          SwingFrameBuilder.prevCurr(
                  map.prevPartyBinding(),
                  map.currPartyBinding(),
                  Comparator.comparing(
                      p -> {
                        int idx = swingPartyOrder.indexOf(p);
                        if (idx < 0) {
                          idx = swingPartyOrder.indexOf(Party.OTHERS);
                        }
                        return idx;
                      }))
              .withHeader(swingHeaderBinding);
      return builder;
    }

    public static Builder basicPartyCurrPrev(
        Binding<? extends Map<Party, Integer>> currVotes,
        Binding<? extends Map<Party, Integer>> prevVotes,
        Binding<Double> pctReporting,
        Binding<String> headerBinding,
        Binding<String> voteHeaderBinding,
        Binding<String> voteSubheadBinding,
        Binding<String> changeHeaderBinding,
        Binding<String> swingHeaderBinding,
        List<Party> swingPartyOrder) {
      Builder builder = new Builder();
      PrevCurrEntryMap<Party> map =
          new PrevCurrEntryMap<>() {
            @Override
            protected Party toParty(Party val) {
              return val;
            }
          };
      currVotes.bind(map::setCurr);
      prevVotes.bind(map::setPrev);

      BindingReceiver<Double> pctReportingReceiver = new BindingReceiver<>(pctReporting);
      builder.headerLabel = createLabel(headerBinding);
      builder.voteFrame =
          BarFrameBuilder.basic(
                  map.currPctBinding(),
                  c -> c.getName().toUpperCase(),
                  c -> c.getColor(),
                  v -> v.getPct(),
                  v -> PCT_FORMAT.format(v.getPct()))
              .withHeader(voteHeaderBinding)
              .withSubhead(voteSubheadBinding)
              .withMax(pctReportingReceiver.getBinding(i -> 2.0 / 3 / Math.max(i, 1e-6)));
      builder.changeFrame =
          BarFrameBuilder.basic(
                  map.diffPctBinding(),
                  p -> p.getAbbreviation().toUpperCase(),
                  p -> p.getColor(),
                  v -> v.getPct(),
                  v -> DIFF_FORMAT.format(v.getPct()),
                  (p, v) -> v.getVotes())
              .withHeader(changeHeaderBinding)
              .withWingspan(pctReportingReceiver.getBinding(i -> 0.10 / Math.max(i, 1e-6)));
      builder.swingFrame =
          SwingFrameBuilder.prevCurr(
                  map.prevPartyBinding(),
                  map.currPartyBinding(),
                  Comparator.comparing(
                      p -> {
                        int idx = swingPartyOrder.indexOf(p);
                        if (idx < 0) {
                          idx = swingPartyOrder.indexOf(Party.OTHERS);
                        }
                        return idx;
                      }))
              .withHeader(swingHeaderBinding);
      return builder;
    }

    private static JLabel createLabel(Binding<String> textBinding) {
      JLabel headerLabel = new JLabel();
      headerLabel.setFont(StandardFont.readBoldFont(32));
      headerLabel.setHorizontalAlignment(JLabel.CENTER);
      headerLabel.setBorder(new EmptyBorder(5, 0, -5, 0));
      textBinding.bind(headerLabel::setText);
      return headerLabel;
    }

    public <T> Builder withMap(
        Binding<String> headerBinding,
        Map<T, Shape> shapes,
        Binding<T> selectedShape,
        Binding<Party> leadingParty,
        Binding<List<Shape>> focus) {
      BindableList<Pair<Shape, Color>> shapesList = new BindableList<>();
      BindingReceiver<List<Shape>> focusList = new BindingReceiver<>(focus);
      BindingReceiver<T> selected = new BindingReceiver<>(selectedShape);
      BindingReceiver<Party> party = new BindingReceiver<>(leadingParty);
      Map<T, Integer> index = new HashMap<>();
      for (Map.Entry<T, Shape> shape : shapes.entrySet()) {
        index.put(shape.getKey(), shapesList.size());
        shapesList.add(ImmutablePair.of(shape.getValue(), Color.LIGHT_GRAY));
      }
      selected
          .getBinding()
          .bind(
              selectedUpdate -> {
                index.forEach(
                    (key, idx) -> {
                      Color color =
                          extractColor(
                              focusList.getValue(),
                              shapes.get(key),
                              shapes.get(selectedUpdate),
                              party.getValue());
                      ImmutablePair<Shape, Color> entry = ImmutablePair.of(shapes.get(key), color);
                      shapesList.set(idx, entry);
                    });
              });
      focusList
          .getBinding()
          .bind(
              focusUpdate -> {
                index.forEach(
                    (key, idx) -> {
                      Color color =
                          extractColor(
                              focusUpdate,
                              shapes.get(key),
                              shapes.get(selected.getValue()),
                              party.getValue());
                      ImmutablePair<Shape, Color> entry = ImmutablePair.of(shapes.get(key), color);
                      shapesList.set(idx, entry);
                    });
              });
      party
          .getBinding()
          .bind(
              p -> {
                index.forEach(
                    (key, idx) -> {
                      Color color =
                          extractColor(
                              focusList.getValue(),
                              shapes.get(key),
                              shapes.get(selected.getValue()),
                              p);
                      ImmutablePair<Shape, Color> entry = ImmutablePair.of(shapes.get(key), color);
                      shapesList.set(idx, entry);
                    });
              });
      mapFrame =
          MapFrameBuilder.from(shapesList)
              .withFocus(focusList.getBinding())
              .withHeader(headerBinding);
      return this;
    }

    public <T> Builder withMap(
        Binding<String> headerBinding, Map<T, Shape> shapes, Binding<Map<T, Party>> winner) {
      return withMap(headerBinding, shapes, winner, () -> null);
    }

    public <T> Builder withMap(
        Binding<String> headerBinding,
        Map<T, Shape> shapes,
        Binding<Map<T, Party>> winner,
        Binding<List<Shape>> focus) {
      BindableList<Pair<Shape, Color>> shapesList = new BindableList<>();
      BindingReceiver<List<Shape>> focusList = new BindingReceiver<>(focus);
      BindingReceiver<Map<T, Party>> winnerMap = new BindingReceiver<>(winner);
      Map<T, Integer> index = new HashMap<>();
      for (Map.Entry<T, Shape> shape : shapes.entrySet()) {
        index.put(shape.getKey(), shapesList.size());
        shapesList.add(ImmutablePair.of(shape.getValue(), Color.LIGHT_GRAY));
      }
      winnerMap
          .getBinding()
          .bind(
              winnerUpdate -> {
                index.forEach(
                    (key, idx) -> {
                      Color color =
                          extractColor(
                              focusList.getValue(), shapes.get(key), winnerUpdate.get(key));
                      ImmutablePair<Shape, Color> entry = ImmutablePair.of(shapes.get(key), color);
                      shapesList.set(idx, entry);
                    });
              });
      focusList
          .getBinding()
          .bind(
              focusUpdate -> {
                index.forEach(
                    (key, idx) -> {
                      Color color =
                          extractColor(focusUpdate, shapes.get(key), winnerMap.getValue().get(key));
                      ImmutablePair<Shape, Color> entry = ImmutablePair.of(shapes.get(key), color);
                      shapesList.set(idx, entry);
                    });
              });
      mapFrame =
          MapFrameBuilder.from(shapesList)
              .withFocus(focusList.getBinding())
              .withHeader(headerBinding);
      return this;
    }

    private Color extractColor(List<Shape> focus, Shape shape, Party winner) {
      Color color;
      if (winner != null) {
        color = winner.getColor();
      } else {
        if (focus == null || focus.isEmpty() || focus.contains(shape)) {
          color = Color.LIGHT_GRAY;
        } else {
          color = new Color(220, 220, 220);
        }
      }
      return color;
    }

    private Color extractColor(List<Shape> focus, Shape shape, Shape selected, Party party) {
      Color color;
      if (selected == shape) {
        color = Optional.ofNullable(party).map(Party::getColor).orElse(Color.BLACK);
      } else {
        if (focus == null || focus.isEmpty() || focus.contains(shape)) {
          color = Color.LIGHT_GRAY;
        } else {
          color = new Color(220, 220, 220);
        }
      }
      return color;
    }

    public SimpleVoteViewPanel build() {
      return new SimpleVoteViewPanel(
          headerLabel,
          voteFrame.build(),
          changeFrame.build(),
          swingFrame.build(),
          mapFrame == null ? null : mapFrame.build());
    }
  }
}
