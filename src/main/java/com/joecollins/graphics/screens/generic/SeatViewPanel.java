package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.Binding.BindingReceiver;
import com.joecollins.graphics.components.BarFrame;
import com.joecollins.graphics.components.BarFrameBuilder;
import com.joecollins.graphics.components.MapFrame;
import com.joecollins.graphics.components.MapFrameBuilder;
import com.joecollins.graphics.components.SwingFrame;
import com.joecollins.graphics.components.SwingFrameBuilder;
import com.joecollins.graphics.utils.StandardFont;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class SeatViewPanel extends JPanel {

  private static final DecimalFormat DIFF_FORMAT = new DecimalFormat("+0;-0");

  private final JLabel label;
  private final BarFrame seatFrame;
  private final BarFrame changeFrame;
  private final SwingFrame swingFrame;
  private final MapFrame mapFrame;

  private SeatViewPanel(
      JLabel label,
      BarFrame seatFrame,
      BarFrame changeFrame,
      SwingFrame swingFrame,
      MapFrame mapFrame) {
    this.label = label;
    this.seatFrame = seatFrame;
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

    panel.add(seatFrame);
    panel.add(changeFrame);

    if (swingFrame != null) panel.add(swingFrame);
    if (mapFrame != null) panel.add(mapFrame);
  }

  private static JLabel createLabel(Binding<String> textBinding) {
    JLabel headerLabel = new JLabel();
    headerLabel.setFont(StandardFont.readBoldFont(32));
    headerLabel.setHorizontalAlignment(JLabel.CENTER);
    headerLabel.setBorder(new EmptyBorder(5, 0, -5, 0));
    textBinding.bind(headerLabel::setText);
    return headerLabel;
  }

  private static class Limits extends Bindable {
    enum LimitsProp {
      PROP
    }

    private int max;

    void setMax(int max) {
      this.max = max;
      onPropertyRefreshed(LimitsProp.PROP);
    }
  }

  private enum PartyEntryProp {
    CURR,
    PREV,
    DIFF
  }

  private static class PrevCurrEntryMap<C, P, D> extends Bindable {
    private Map<Party, ? extends C> curr = new LinkedHashMap<>();
    private Map<Party, ? extends P> prev = new LinkedHashMap<>();
    private final BiFunction<C, P, D> diffFunction;
    private final C currIdentity;
    private final P prevIdentity;

    private PrevCurrEntryMap(BiFunction<C, P, D> diffFunction, C currIdentity, P prevIdentity) {
      this.diffFunction = diffFunction;
      this.currIdentity = currIdentity;
      this.prevIdentity = prevIdentity;
    }

    void setCurr(Map<Party, ? extends C> curr) {
      this.curr = curr;
      onPropertyRefreshed(PartyEntryProp.CURR);
    }

    void setPrev(Map<Party, ? extends P> prev) {
      this.prev = prev;
      onPropertyRefreshed(PartyEntryProp.PREV);
    }

    Binding<Map<Party, ? extends C>> currBinding() {
      return Binding.propertyBinding(this, m -> m.curr, PartyEntryProp.CURR);
    }

    Binding<Map<Party, ? extends Pair<D, C>>> diffBinding() {
      return Binding.propertyBinding(
          this,
          m -> {
            LinkedHashMap<Party, ImmutablePair<D, C>> ret = new LinkedHashMap<>();
            curr.forEach(
                (k, v) -> {
                  D d = diffFunction.apply(v, prev.containsKey(k) ? prev.get(k) : prevIdentity);
                  ret.put(k, ImmutablePair.of(d, v));
                });
            prev.forEach(
                (k, v) -> {
                  D d = diffFunction.apply(currIdentity, v);
                  ret.putIfAbsent(k, ImmutablePair.of(d, currIdentity));
                });
            return ret;
          },
          PartyEntryProp.CURR,
          PartyEntryProp.PREV);
    }
  }

  private static class PrevDiffEntryMap<C, D> extends Bindable {
    private Map<Party, ? extends C> curr = new LinkedHashMap<>();
    private Map<Party, ? extends D> diff = new LinkedHashMap<>();
    private final C currIdentity;
    private final D diffIdentity;

    private PrevDiffEntryMap(C currIdentity, D diffIdentity) {
      this.currIdentity = currIdentity;
      this.diffIdentity = diffIdentity;
    }

    void setCurr(Map<Party, ? extends C> curr) {
      this.curr = curr;
      onPropertyRefreshed(PartyEntryProp.CURR);
    }

    void setDiff(Map<Party, ? extends D> diff) {
      this.diff = diff;
      onPropertyRefreshed(PartyEntryProp.DIFF);
    }

    Binding<Map<Party, ? extends C>> currBinding() {
      return Binding.propertyBinding(this, m -> m.curr, PartyEntryProp.CURR);
    }

    Binding<Map<Party, ? extends Pair<D, C>>> diffBinding() {
      return Binding.propertyBinding(
          this,
          m -> {
            LinkedHashMap<Party, Pair<D, C>> ret = new LinkedHashMap<>();
            curr.forEach(
                (k, v) -> {
                  D d = diff.containsKey(k) ? diff.get(k) : diffIdentity;
                  ret.put(k, ImmutablePair.of(d, v));
                });
            diff.forEach(
                (k, v) -> {
                  ret.putIfAbsent(k, ImmutablePair.of(v, currIdentity));
                });
            return ret;
          },
          PartyEntryProp.CURR,
          PartyEntryProp.DIFF);
    }
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
      seatFrame.setLocation(5, 5);
      seatFrame.setSize(width * 3 / 5 - 10, height - 10);
      changeFrame.setLocation(width * 3 / 5 + 5, 5);
      changeFrame.setSize(width * 2 / 5 - 10, height * 2 / 3 - 10);
      if (swingFrame != null) {
        swingFrame.setLocation(width * 3 / 5 + 5, height * 2 / 3 + 5);
        swingFrame.setSize(width * (mapFrame == null ? 2 : 1) / 5 - 10, height / 3 - 10);
      }
      if (mapFrame != null) {
        mapFrame.setLocation(width * (swingFrame == null ? 3 : 4) / 5 + 5, height * 2 / 3 + 5);
        mapFrame.setSize(width * (swingFrame == null ? 2 : 1) / 5 - 10, height / 3 - 10);
      }
    }
  }

  public static class Builder {
    private JLabel headerLabel;
    private BarFrameBuilder seatFrame;
    private BarFrameBuilder changeFrame;
    private SwingFrameBuilder swingFrame;
    private MapFrameBuilder mapFrame;
    private Limits limits;

    public static Builder basicCurrPrev(
        Binding<? extends Map<Party, Integer>> currentSeats,
        Binding<? extends Map<Party, Integer>> previousSeats,
        Binding<Integer> totalSeats,
        Binding<String> header,
        Binding<String> seatHeader,
        Binding<String> seatSubhead,
        Binding<String> changeHeader) {

      class PEM extends PrevCurrEntryMap<Integer, Integer, Integer> {
        private PEM() {
          super((a, b) -> a - b, 0, 0);
        }
      }
      PEM map = new PEM();
      currentSeats.bind(map::setCurr);
      previousSeats.bind(map::setPrev);

      Builder builder = new Builder();
      builder.limits = new Limits();
      totalSeats.bind(builder.limits::setMax);
      builder.headerLabel = createLabel(header);
      builder.seatFrame =
          BarFrameBuilder.basic(
                  map.currBinding(),
                  party -> party.getName().toUpperCase(),
                  party -> party.getColor(),
                  Function.identity(),
                  seats -> seats.toString(),
                  (party, seats) -> party == Party.OTHERS ? -1 : seats)
              .withHeader(seatHeader)
              .withSubhead(seatSubhead)
              .withMax(
                  Binding.propertyBinding(
                      builder.limits, l -> Math.max(1, l.max * 2 / 3), Limits.LimitsProp.PROP));
      builder.changeFrame =
          BarFrameBuilder.basic(
                  map.diffBinding(),
                  party -> party.getAbbreviation().toUpperCase(),
                  party -> party.getColor(),
                  seats -> seats.getLeft(),
                  seats -> changeStr(seats.getLeft()),
                  (party, seats) -> party == Party.OTHERS ? -1 : seats.getRight())
              .withHeader(changeHeader)
              .withWingspan(
                  Binding.propertyBinding(
                      builder.limits, l -> Math.max(1, l.max / 20), Limits.LimitsProp.PROP));
      return builder;
    }

    public static Builder basicCurrDiff(
        Binding<? extends Map<Party, Integer>> currentSeats,
        Binding<? extends Map<Party, Integer>> seatDiff,
        Binding<Integer> totalSeats,
        Binding<String> header,
        Binding<String> seatHeader,
        Binding<String> seatSubhead,
        Binding<String> changeHeader) {
      class PEM extends PrevDiffEntryMap<Integer, Integer> {
        private PEM() {
          super(0, 0);
        }
      }
      PEM map = new PEM();
      currentSeats.bind(map::setCurr);
      seatDiff.bind(map::setDiff);

      Builder builder = new Builder();
      builder.limits = new Limits();
      totalSeats.bind(builder.limits::setMax);
      builder.headerLabel = createLabel(header);
      builder.seatFrame =
          BarFrameBuilder.basic(
                  map.currBinding(),
                  party -> party.getName().toUpperCase(),
                  party -> party.getColor(),
                  Function.identity(),
                  seats -> seats.toString(),
                  (party, seats) -> party == Party.OTHERS ? -1 : seats)
              .withHeader(seatHeader)
              .withSubhead(seatSubhead)
              .withMax(
                  Binding.propertyBinding(
                      builder.limits, l -> Math.max(1, l.max * 2 / 3), Limits.LimitsProp.PROP));
      builder.changeFrame =
          BarFrameBuilder.basic(
                  map.diffBinding(),
                  party -> party.getAbbreviation().toUpperCase(),
                  party -> party.getColor(),
                  seats -> seats.getLeft(),
                  seats -> changeStr(seats.getLeft()),
                  (party, seats) -> party == Party.OTHERS ? -1 : seats.getRight())
              .withHeader(changeHeader)
              .withWingspan(
                  Binding.propertyBinding(
                      builder.limits, l -> Math.max(1, l.max / 20), Limits.LimitsProp.PROP));
      return builder;
    }

    private static String changeStr(Integer seats) {
      return seats == 0 ? "\u00b10" : DIFF_FORMAT.format(seats);
    }

    public static Builder dualCurrPrev(
        Binding<? extends Map<Party, ? extends Pair<Integer, Integer>>> currentSeats,
        Binding<? extends Map<Party, ? extends Pair<Integer, Integer>>> previousSeats,
        Binding<Integer> totalSeats,
        Binding<String> header,
        Binding<String> seatHeader,
        Binding<String> seatSubhead,
        Binding<String> changeHeader) {
      class PEM
          extends PrevCurrEntryMap<
              Pair<Integer, Integer>, Pair<Integer, Integer>, Pair<Integer, Integer>> {
        private PEM() {
          super(
              (a, b) -> ImmutablePair.of(a.getLeft() - b.getLeft(), a.getRight() - b.getRight()),
              ImmutablePair.of(0, 0),
              ImmutablePair.of(0, 0));
        }
      }
      PEM map = new PEM();
      currentSeats.bind(map::setCurr);
      previousSeats.bind(map::setPrev);

      Builder builder = new Builder();
      builder.limits = new Limits();
      totalSeats.bind(builder.limits::setMax);
      builder.headerLabel = createLabel(header);
      builder.seatFrame =
          BarFrameBuilder.dual(
                  map.currBinding(),
                  party -> party.getName().toUpperCase(),
                  party -> party.getColor(),
                  Function.identity(),
                  seats -> seats.getLeft() + "/" + seats.getRight(),
                  (party, seats) -> party == Party.OTHERS ? -1 : seats.getRight())
              .withHeader(seatHeader)
              .withSubhead(seatSubhead)
              .withMax(
                  Binding.propertyBinding(
                      builder.limits, l -> Math.max(1, l.max * 2 / 3), Limits.LimitsProp.PROP));
      builder.changeFrame =
          BarFrameBuilder.dual(
                  map.diffBinding(),
                  party -> party.getAbbreviation().toUpperCase(),
                  party -> party.getColor(),
                  seats -> seats.getLeft(),
                  seats ->
                      changeStr(seats.getLeft().getLeft())
                          + "/"
                          + changeStr(seats.getLeft().getRight()),
                  (party, seats) -> party == Party.OTHERS ? -1 : seats.getRight().getRight())
              .withHeader(changeHeader)
              .withWingspan(
                  Binding.propertyBinding(
                      builder.limits, l -> Math.max(1, l.max / 20), Limits.LimitsProp.PROP));
      return builder;
    }

    public static Builder dualCurrDiff(
        Binding<? extends Map<Party, ? extends Pair<Integer, Integer>>> currentSeats,
        Binding<? extends Map<Party, ? extends Pair<Integer, Integer>>> seatDiff,
        Binding<Integer> totalSeats,
        Binding<String> header,
        Binding<String> seatHeader,
        Binding<String> seatSubhead,
        Binding<String> changeHeader) {
      class PEM extends PrevDiffEntryMap<Pair<Integer, Integer>, Pair<Integer, Integer>> {
        private PEM() {
          super(ImmutablePair.of(0, 0), ImmutablePair.of(0, 0));
        }
      }
      PEM map = new PEM();
      currentSeats.bind(map::setCurr);
      seatDiff.bind(map::setDiff);

      Builder builder = new Builder();
      builder.limits = new Limits();
      totalSeats.bind(builder.limits::setMax);
      builder.headerLabel = createLabel(header);
      builder.seatFrame =
          BarFrameBuilder.dual(
                  map.currBinding(),
                  party -> party.getName().toUpperCase(),
                  party -> party.getColor(),
                  Function.identity(),
                  seats -> seats.getLeft() + "/" + seats.getRight(),
                  (party, seats) -> party == Party.OTHERS ? -1 : seats.getRight())
              .withHeader(seatHeader)
              .withSubhead(seatSubhead)
              .withMax(
                  Binding.propertyBinding(
                      builder.limits, l -> Math.max(1, l.max * 2 / 3), Limits.LimitsProp.PROP));
      builder.changeFrame =
          BarFrameBuilder.dual(
                  map.diffBinding(),
                  party -> party.getAbbreviation().toUpperCase(),
                  party -> party.getColor(),
                  seats -> seats.getLeft(),
                  seats ->
                      changeStr(seats.getLeft().getLeft())
                          + "/"
                          + changeStr(seats.getLeft().getRight()),
                  (party, seats) -> party == Party.OTHERS ? -1 : seats.getRight().getRight())
              .withHeader(changeHeader)
              .withWingspan(
                  Binding.propertyBinding(
                      builder.limits, l -> Math.max(1, l.max / 20), Limits.LimitsProp.PROP));
      return builder;
    }

    public static Builder rangeCurrPrev(
        Binding<? extends Map<Party, Range<Integer>>> currentSeats,
        Binding<? extends Map<Party, Integer>> previousSeats,
        Binding<Integer> totalSeats,
        Binding<String> header,
        Binding<String> seatHeader,
        Binding<String> seatSubhead,
        Binding<String> changeHeader) {
      class PEM extends PrevCurrEntryMap<Range<Integer>, Integer, Range<Integer>> {
        private PEM() {
          super((a, b) -> Range.between(a.getMinimum() - b, a.getMaximum() - b), Range.is(0), 0);
        }
      }
      PEM map = new PEM();
      currentSeats.bind(map::setCurr);
      previousSeats.bind(map::setPrev);

      Builder builder = new Builder();
      builder.limits = new Limits();
      totalSeats.bind(builder.limits::setMax);
      builder.headerLabel = createLabel(header);
      builder.seatFrame =
          BarFrameBuilder.dual(
                  map.currBinding(),
                  party -> party.getName().toUpperCase(),
                  party -> party.getColor(),
                  seats -> ImmutablePair.of(seats.getMinimum(), seats.getMaximum()),
                  seats -> seats.getMinimum() + "-" + seats.getMaximum(),
                  (party, seats) ->
                      party == Party.OTHERS ? -1 : seats.getMinimum() + seats.getMaximum())
              .withHeader(seatHeader)
              .withSubhead(seatSubhead)
              .withMax(
                  Binding.propertyBinding(
                      builder.limits, l -> Math.max(1, l.max * 2 / 3), Limits.LimitsProp.PROP));
      builder.changeFrame =
          BarFrameBuilder.dual(
                  map.diffBinding(),
                  party -> party.getAbbreviation().toUpperCase(),
                  party -> party.getColor(),
                  seats ->
                      ImmutablePair.of(seats.getLeft().getMinimum(), seats.getLeft().getMaximum()),
                  seats ->
                      "("
                          + changeStr(seats.getLeft().getMinimum())
                          + ")-("
                          + changeStr(seats.getLeft().getMaximum())
                          + ")",
                  (party, seats) ->
                      party == Party.OTHERS
                          ? -1
                          : seats.getRight().getMinimum() + seats.getRight().getMaximum())
              .withHeader(changeHeader)
              .withWingspan(
                  Binding.propertyBinding(
                      builder.limits, l -> Math.max(1, l.max / 20), Limits.LimitsProp.PROP));
      return builder;
    }

    public static Builder rangeCurrDiff(
        Binding<? extends Map<Party, Range<Integer>>> currentSeats,
        Binding<? extends Map<Party, Range<Integer>>> seatDiff,
        Binding<Integer> totalSeats,
        Binding<String> header,
        Binding<String> seatHeader,
        Binding<String> seatSubhead,
        Binding<String> changeHeader) {
      class PEM extends PrevDiffEntryMap<Range<Integer>, Range<Integer>> {
        private PEM() {
          super(Range.is(0), Range.is(0));
        }
      }
      PEM map = new PEM();
      currentSeats.bind(map::setCurr);
      seatDiff.bind(map::setDiff);

      Builder builder = new Builder();
      builder.limits = new Limits();
      totalSeats.bind(builder.limits::setMax);
      builder.headerLabel = createLabel(header);
      builder.seatFrame =
          BarFrameBuilder.dual(
                  map.currBinding(),
                  party -> party.getName().toUpperCase(),
                  party -> party.getColor(),
                  seats -> ImmutablePair.of(seats.getMinimum(), seats.getMaximum()),
                  seats -> seats.getMinimum() + "-" + seats.getMaximum(),
                  (party, seats) ->
                      party == Party.OTHERS ? -1 : seats.getMinimum() + seats.getMaximum())
              .withHeader(seatHeader)
              .withSubhead(seatSubhead)
              .withMax(
                  Binding.propertyBinding(
                      builder.limits, l -> Math.max(1, l.max * 2 / 3), Limits.LimitsProp.PROP));
      builder.changeFrame =
          BarFrameBuilder.dual(
                  map.diffBinding(),
                  party -> party.getAbbreviation().toUpperCase(),
                  party -> party.getColor(),
                  seats ->
                      ImmutablePair.of(seats.getLeft().getMinimum(), seats.getLeft().getMaximum()),
                  seats ->
                      "("
                          + changeStr(seats.getLeft().getMinimum())
                          + ")-("
                          + changeStr(seats.getLeft().getMaximum())
                          + ")",
                  (party, seats) ->
                      party == Party.OTHERS
                          ? -1
                          : seats.getRight().getMinimum() + seats.getRight().getMaximum())
              .withHeader(changeHeader)
              .withWingspan(
                  Binding.propertyBinding(
                      builder.limits, l -> Math.max(1, l.max / 20), Limits.LimitsProp.PROP));
      return builder;
    }

    public Builder withMajorityLine(
        Binding<Boolean> showMajority, Function<? super Number, String> labelFunc) {
      BindableList<Integer> lines = new BindableList<>();
      showMajority.bind(
          show -> {
            lines.clear();
            if (show) {
              lines.add(limits.max / 2 + 1);
            }
          });
      Binding.propertyBinding(limits, l -> l.max / 2 + 1, Limits.LimitsProp.PROP)
          .bind(
              maj -> {
                if (!lines.isEmpty()) {
                  lines.set(0, maj);
                }
              });
      seatFrame = seatFrame.withLines(lines, labelFunc);
      return this;
    }

    public Builder withSwing(
        Binding<String> header,
        Binding<? extends Map<Party, Integer>> currVotes,
        Binding<? extends Map<Party, Integer>> prevVotes,
        List<Party> partyOrder) {
      return withSwing(
          header,
          currVotes,
          prevVotes,
          Comparator.comparing(
              p -> {
                int idx = partyOrder.indexOf(p);
                if (idx < 0) {
                  idx = partyOrder.indexOf(Party.OTHERS);
                }
                return idx;
              }));
    }

    public Builder withSwing(
        Binding<String> header,
        Binding<? extends Map<Party, Integer>> currVotes,
        Binding<? extends Map<Party, Integer>> prevVotes,
        Comparator<Party> comparator) {
      swingFrame = SwingFrameBuilder.prevCurr(prevVotes, currVotes, comparator).withHeader(header);
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

    public SeatViewPanel build() {
      return new SeatViewPanel(
          headerLabel,
          seatFrame.build(),
          changeFrame.build(),
          swingFrame == null ? null : swingFrame.build(),
          mapFrame == null ? null : mapFrame.build());
    }
  }
}
