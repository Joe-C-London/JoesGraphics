package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.Binding;
import com.joecollins.graphics.components.BarFrame;
import com.joecollins.graphics.components.BarFrameBuilder;
import com.joecollins.graphics.components.MapFrame;
import com.joecollins.graphics.components.MapFrameBuilder;
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
import java.awt.LayoutManager;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

  private enum PartyEntryProp {
    CURR,
    PREV,
    DIFF
  }

  private static class PrevCurrEntryMap extends Bindable {
    private Map<Candidate, Pair<Integer, Double>> curr = new LinkedHashMap<>();
    private Map<Party, Pair<Integer, Double>> prev = new LinkedHashMap<>();

    void setCurr(Map<Candidate, Integer> curr) {
      int total = curr.values().stream().mapToInt(i -> i).sum();
      this.curr = new LinkedHashMap<>();
      curr.forEach((c, v) -> this.curr.put(c, ImmutablePair.of(v, 1.0 * v / total)));
      onPropertyRefreshed(PartyEntryProp.CURR);
    }

    void setPrev(Map<Party, Integer> prev) {
      int total = prev.values().stream().mapToInt(i -> i).sum();
      this.prev = new LinkedHashMap<>();
      prev.forEach((c, v) -> this.prev.put(c, ImmutablePair.of(v, 1.0 * v / total)));
      onPropertyRefreshed(PartyEntryProp.PREV);
    }

    Binding<Map<Party, Integer>> currPartyBinding() {
      return Binding.propertyBinding(
          this,
          m -> {
            LinkedHashMap<Party, Integer> ret = new LinkedHashMap<>();
            m.curr.forEach((c, v) -> ret.put(c.getParty(), v.getLeft()));
            return ret;
          },
          PartyEntryProp.CURR);
    }

    Binding<Map<Party, Integer>> prevPartyBinding() {
      return Binding.propertyBinding(
          this,
          m -> {
            LinkedHashMap<Party, Integer> ret = new LinkedHashMap<>();
            m.prev.forEach((p, v) -> ret.put(p, v.getLeft()));
            return ret;
          },
          PartyEntryProp.PREV);
    }

    Binding<Map<Candidate, Pair<Integer, Double>>> currPctBinding() {
      return Binding.propertyBinding(this, m -> m.curr, PartyEntryProp.CURR);
    }

    Binding<Map<Party, Pair<Double, Integer>>> diffPctBinding() {
      return Binding.propertyBinding(
          this,
          m -> {
            Map<Party, Pair<Double, Integer>> ret = new LinkedHashMap<>();
            curr.forEach(
                (k, v) -> {
                  double d =
                      v.getRight()
                          - (prev.containsKey(k.getParty())
                              ? prev.get(k.getParty()).getRight()
                              : 0);
                  ret.put(k.getParty(), ImmutablePair.of(d, v.getLeft()));
                });
            prev.forEach(
                (k, v) -> {
                  double d = -(prev.containsKey(k) ? prev.get(k).getRight() : 0);
                  ret.putIfAbsent(k, ImmutablePair.of(d, 0));
                });
            return ret;
          },
          PartyEntryProp.CURR,
          PartyEntryProp.PREV);
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
      PrevCurrEntryMap map = new PrevCurrEntryMap();
      currVotes.bind(map::setCurr);
      prevVotes.bind(map::setPrev);

      Builder builder = new Builder();
      builder.headerLabel = createLabel(headerBinding);
      builder.voteFrame =
          BarFrameBuilder.basic(
                  map.currPctBinding(),
                  c -> c.getName().toUpperCase() + "\n" + c.getParty().getName().toUpperCase(),
                  c -> c.getParty().getColor(),
                  v -> v.getRight(),
                  v -> VOTE_FORMAT.format(v.getLeft()) + "\n" + PCT_FORMAT.format(v.getRight()))
              .withHeader(voteHeaderBinding)
              .withSubhead(voteSubheadBinding)
              .withMax(Binding.fixedBinding(2.0 / 3));
      builder.changeFrame =
          BarFrameBuilder.basic(
                  map.diffPctBinding(),
                  p -> p.getAbbreviation().toUpperCase(),
                  p -> p.getColor(),
                  v -> v.getLeft(),
                  v -> DIFF_FORMAT.format(v.getLeft()),
                  (p, v) -> v.getRight())
              .withHeader(changeHeaderBinding)
              .withWingspan(Binding.fixedBinding(0.10));
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
