package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
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
import java.awt.LayoutManager;
import java.awt.Shape;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class BasicResultPanel extends JPanel {

  private static final DecimalFormat PCT_FORMAT = new DecimalFormat("0.0%");
  private static final DecimalFormat THOUSANDS_FORMAT = new DecimalFormat("#,##0");
  private final JLabel label;
  private final BarFrame seatFrame;
  private final BarFrame preferenceFrame;
  private final BarFrame changeFrame;
  private final SwingFrame swingFrame;
  private final MapFrame mapFrame;

  private BasicResultPanel(
      JLabel label,
      BarFrame seatFrame,
      BarFrame preferenceFrame,
      BarFrame changeFrame,
      SwingFrame swingFrame,
      MapFrame mapFrame) {
    this.label = label;
    this.seatFrame = seatFrame;
    this.preferenceFrame = preferenceFrame;
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
    if (preferenceFrame != null) panel.add(preferenceFrame);
    if (changeFrame != null) panel.add(changeFrame);

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
      seatFrame.setLocation(5, 5);
      boolean seatFrameIsAlone = changeFrame == null && swingFrame == null && mapFrame == null;
      seatFrame.setSize(
          width * (seatFrameIsAlone ? 5 : 3) / 5 - 10,
          height * (preferenceFrame == null ? 3 : 2) / 3 - 10);
      if (preferenceFrame != null) {
        preferenceFrame.setLocation(5, height * 2 / 3 + 5);
        preferenceFrame.setSize(width * (seatFrameIsAlone ? 5 : 3) / 5 - 10, height / 3 - 10);
      }
      if (changeFrame != null) {
        changeFrame.setLocation(width * 3 / 5 + 5, 5);
        changeFrame.setSize(width * 2 / 5 - 10, height * 2 / 3 - 10);
      }
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

  private static <T> Map<T, PartyResult> partyMapToResultMap(Map<T, Party> m) {
    Map<T, PartyResult> ret = new LinkedHashMap<>();
    m.forEach((k, v) -> ret.put(k, PartyResult.elected(v)));
    return ret;
  }

  private static JLabel createHeaderLabel(Binding<String> textBinding) {
    JLabel headerLabel = new JLabel();
    headerLabel.setFont(StandardFont.readBoldFont(32));
    headerLabel.setHorizontalAlignment(JLabel.CENTER);
    headerLabel.setBorder(new EmptyBorder(5, 0, -5, 0));
    textBinding.bind(headerLabel::setText);
    return headerLabel;
  }

  public static SeatScreenBuilder<Party, Integer, Integer> partySeats(
      Binding<? extends Map<Party, Integer>> seats,
      Binding<String> header,
      Binding<String> subhead) {
    return new BasicSeatScreenBuilder<>(
        new BindingReceiver<>(seats),
        new BindingReceiver<>(header),
        new BindingReceiver<>(subhead),
        new PartyTemplate());
  }

  public static SeatScreenBuilder<Candidate, Integer, Integer> candidateSeats(
      Binding<? extends Map<Candidate, Integer>> seats,
      Binding<String> header,
      Binding<String> subhead) {
    return new BasicSeatScreenBuilder<>(
        new BindingReceiver<>(seats),
        new BindingReceiver<>(header),
        new BindingReceiver<>(subhead),
        new CandidateTemplate());
  }

  public static SeatScreenBuilder<Party, Pair<Integer, Integer>, Pair<Integer, Integer>>
      partyDualSeats(
          Binding<? extends Map<Party, Pair<Integer, Integer>>> seats,
          Binding<String> header,
          Binding<String> subhead) {
    return new DualSeatScreenBuilder<>(
        new BindingReceiver<>(seats),
        new BindingReceiver<>(header),
        new BindingReceiver<>(subhead),
        new PartyTemplate());
  }

  public static SeatScreenBuilder<Candidate, Pair<Integer, Integer>, Pair<Integer, Integer>>
      candidateDualSeats(
          Binding<? extends Map<Candidate, Pair<Integer, Integer>>> seats,
          Binding<String> header,
          Binding<String> subhead) {
    return new DualSeatScreenBuilder<>(
        new BindingReceiver<>(seats),
        new BindingReceiver<>(header),
        new BindingReceiver<>(subhead),
        new CandidateTemplate());
  }

  public static SeatScreenBuilder<Party, Range<Integer>, Integer> partyRangeSeats(
      Binding<? extends Map<Party, Range<Integer>>> seats,
      Binding<String> header,
      Binding<String> subhead) {
    return new RangeSeatScreenBuilder<>(
        new BindingReceiver<>(seats),
        new BindingReceiver<>(header),
        new BindingReceiver<>(subhead),
        new PartyTemplate());
  }

  public static SeatScreenBuilder<Candidate, Range<Integer>, Integer> candidateRangeSeats(
      Binding<? extends Map<Candidate, Range<Integer>>> seats,
      Binding<String> header,
      Binding<String> subhead) {
    return new RangeSeatScreenBuilder<>(
        new BindingReceiver<>(seats),
        new BindingReceiver<>(header),
        new BindingReceiver<>(subhead),
        new CandidateTemplate());
  }

  public static VoteScreenBuilder<Party, Integer, Double, Integer> partyVotes(
      Binding<? extends Map<Party, Integer>> votes,
      Binding<String> header,
      Binding<String> subhead) {
    return new BasicVoteScreenBuilder<>(
        new BindingReceiver<>(votes),
        new BindingReceiver<>(header),
        new BindingReceiver<>(subhead),
        new PartyTemplate(),
        new PctOnlyTemplate(),
        Party.OTHERS);
  }

  public static VoteScreenBuilder<Candidate, Integer, Double, Integer> candidateVotes(
      Binding<? extends Map<Candidate, Integer>> votes,
      Binding<String> header,
      Binding<String> subhead) {
    return new BasicVoteScreenBuilder<>(
        new BindingReceiver<>(votes),
        new BindingReceiver<>(header),
        new BindingReceiver<>(subhead),
        new CandidateTemplate(),
        new VotePctTemplate(),
        Candidate.OTHERS);
  }

  public static VoteScreenBuilder<Candidate, Integer, Double, Integer> candidateVotesPctOnly(
      Binding<? extends Map<Candidate, Integer>> votes,
      Binding<String> header,
      Binding<String> subhead) {
    return new BasicVoteScreenBuilder<>(
        new BindingReceiver<>(votes),
        new BindingReceiver<>(header),
        new BindingReceiver<>(subhead),
        new CandidateTemplate(),
        new VotePctOnlyTemplate(),
        Candidate.OTHERS);
  }

  public static VoteScreenBuilder<Candidate, Integer, Double, Integer> candidateVotes(
      Binding<? extends Map<Candidate, Integer>> votes,
      Binding<String> header,
      Binding<String> subhead,
      String incumbentMarker) {
    return new BasicVoteScreenBuilder<>(
        new BindingReceiver<>(votes),
        new BindingReceiver<>(header),
        new BindingReceiver<>(subhead),
        new CandidateTemplate(incumbentMarker),
        new VotePctTemplate(),
        Candidate.OTHERS);
  }

  public static VoteScreenBuilder<Candidate, Integer, Double, Integer> candidateVotesPctOnly(
      Binding<? extends Map<Candidate, Integer>> votes,
      Binding<String> header,
      Binding<String> subhead,
      String incumbentMarker) {
    return new BasicVoteScreenBuilder<>(
        new BindingReceiver<>(votes),
        new BindingReceiver<>(header),
        new BindingReceiver<>(subhead),
        new CandidateTemplate(incumbentMarker),
        new VotePctOnlyTemplate(),
        Candidate.OTHERS);
  }

  public static VoteScreenBuilder<Party, Range<Double>, Double, Integer> partyRangeVotes(
      Binding<? extends Map<Party, Range<Double>>> votes,
      Binding<String> header,
      Binding<String> subhead) {
    return new RangeVoteScreenBuilder<>(
        new BindingReceiver<>(votes),
        new BindingReceiver<>(header),
        new BindingReceiver<>(subhead),
        new PartyTemplate(),
        new PctOnlyTemplate(),
        Party.OTHERS);
  }

  private interface KeyTemplate<KT> {
    Party toParty(KT key);

    String toMainBarHeader(KT key, boolean forceSingleLine);

    Shape winnerShape(boolean forceSingleLine);

    Shape runoffShape(boolean forceSingleLine);
  }

  private static class PartyTemplate implements KeyTemplate<Party> {

    @Override
    public Party toParty(Party key) {
      return key;
    }

    @Override
    public String toMainBarHeader(Party key, boolean forceSingleLine) {
      return key.getName().toUpperCase();
    }

    @Override
    public Shape winnerShape(boolean forceSingleLine) {
      return ImageGenerator.createTickShape();
    }

    @Override
    public Shape runoffShape(boolean forceSingleLine) {
      return ImageGenerator.createRunoffShape();
    }
  }

  private static class CandidateTemplate implements KeyTemplate<Candidate> {

    private final String incumbentMarker;

    private CandidateTemplate() {
      this.incumbentMarker = "";
    }

    private CandidateTemplate(String incumbentMarker) {
      this.incumbentMarker = " " + incumbentMarker;
    }

    @Override
    public Party toParty(Candidate key) {
      return key.getParty();
    }

    @Override
    public String toMainBarHeader(Candidate key, boolean forceSingleLine) {
      if (key == Candidate.OTHERS) {
        return key.getParty().getName().toUpperCase();
      }
      return (key.getName()
              + (key.isIncumbent() ? incumbentMarker : "")
              + (forceSingleLine
                  ? (" (" + key.getParty().getAbbreviation() + ")")
                  : ("\n" + key.getParty().getName())))
          .toUpperCase();
    }

    @Override
    public Shape winnerShape(boolean forceSingleLine) {
      return forceSingleLine
          ? ImageGenerator.createTickShape()
          : ImageGenerator.createHalfTickShape();
    }

    @Override
    public Shape runoffShape(boolean forceSingleLine) {
      return forceSingleLine
          ? ImageGenerator.createRunoffShape()
          : ImageGenerator.createHalfRunoffShape();
    }
  }

  public abstract static class SeatScreenBuilder<KT, CT, PT> {

    protected final KeyTemplate<KT> keyTemplate;

    protected BindingReceiver<Map<KT, CT>> current;
    protected BindingReceiver<String> header;
    protected BindingReceiver<String> subhead;
    protected BindingReceiver<Integer> total;
    protected BindingReceiver<Boolean> showMajority;
    protected Function<Integer, String> majorityFunction;
    protected BindingReceiver<KT> winner;
    protected BindingReceiver<String> notes;

    protected BindingReceiver<Map<Party, CurrDiff<CT>>> diff;
    protected BindingReceiver<String> changeHeader;
    protected BindingReceiver<String> changeSubhead;

    protected BindingReceiver<Map<Party, Integer>> currVotes;
    protected BindingReceiver<Map<Party, Integer>> prevVotes;
    protected BindingReceiver<String> swingHeader;
    protected Comparator<Party> swingComparator;

    protected MapBuilder mapBuilder;

    private SeatScreenBuilder(
        BindingReceiver<Map<KT, CT>> current,
        BindingReceiver<String> header,
        BindingReceiver<String> subhead,
        KeyTemplate<KT> keyTemplate) {
      this.current = current;
      this.header = header;
      this.subhead = subhead;
      this.keyTemplate = keyTemplate;
    }

    public SeatScreenBuilder<KT, CT, PT> withTotal(Binding<Integer> totalSeats) {
      this.total = new BindingReceiver<>(totalSeats);
      return this;
    }

    public SeatScreenBuilder<KT, CT, PT> withMajorityLine(
        Binding<Boolean> showMajority, Function<Integer, String> majorityLabelFunc) {
      this.showMajority = new BindingReceiver<>(showMajority);
      this.majorityFunction = majorityLabelFunc;
      return this;
    }

    public SeatScreenBuilder<KT, CT, PT> withWinner(Binding<KT> winner) {
      this.winner = new BindingReceiver<>(winner);
      return this;
    }

    public SeatScreenBuilder<KT, CT, PT> withDiff(
        Binding<? extends Map<Party, ? extends CT>> diff, Binding<String> changeHeader) {
      return withDiff(diff, changeHeader, Binding.fixedBinding(null));
    }

    public SeatScreenBuilder<KT, CT, PT> withDiff(
        Binding<? extends Map<Party, ? extends CT>> diff,
        Binding<String> changeHeader,
        Binding<String> changeSubhead) {
      this.diff =
          new BindingReceiver<>(
              current
                  .getBinding()
                  .merge(
                      diff,
                      (c, d) -> {
                        LinkedHashMap<Party, CurrDiff<CT>> ret = new LinkedHashMap<>();
                        c.forEach(
                            (k, v) ->
                                ret.put(
                                    keyTemplate.toParty(k),
                                    createFromDiff(v, d.get(keyTemplate.toParty(k)))));
                        d.forEach((k, v) -> ret.putIfAbsent(k, createFromDiff(v)));
                        return ret;
                      }));
      this.changeHeader = new BindingReceiver<>(changeHeader);
      this.changeSubhead = new BindingReceiver<>(changeSubhead);
      return this;
    }

    protected abstract CurrDiff<CT> createFromDiff(CT curr, CT diff);

    protected abstract CurrDiff<CT> createFromDiff(CT diff);

    public SeatScreenBuilder<KT, CT, PT> withPrev(
        Binding<? extends Map<Party, PT>> prev, Binding<String> changeHeader) {
      return withPrev(prev, changeHeader, Binding.fixedBinding(null));
    }

    public SeatScreenBuilder<KT, CT, PT> withPrev(
        Binding<? extends Map<Party, PT>> prev,
        Binding<String> changeHeader,
        Binding<String> changeSubhead) {
      this.diff =
          new BindingReceiver<>(
              current
                  .getBinding()
                  .merge(
                      prev,
                      (c, p) -> {
                        LinkedHashMap<Party, CurrDiff<CT>> ret = new LinkedHashMap<>();
                        c.forEach(
                            (k, v) ->
                                ret.put(
                                    keyTemplate.toParty(k),
                                    createFromPrev(v, p.get(keyTemplate.toParty(k)))));
                        p.forEach((k, v) -> ret.putIfAbsent(k, createFromPrev(v)));
                        return ret;
                      }));
      this.changeHeader = new BindingReceiver<>(changeHeader);
      this.changeSubhead = new BindingReceiver<>(changeSubhead);
      return this;
    }

    protected abstract CurrDiff<CT> createFromPrev(CT curr, PT prev);

    protected abstract CurrDiff<CT> createFromPrev(PT prev);

    public SeatScreenBuilder<KT, CT, PT> withSwing(
        Binding<? extends Map<Party, Integer>> currVotes,
        Binding<? extends Map<Party, Integer>> prevVotes,
        Comparator<Party> comparator,
        Binding<String> header) {
      this.swingHeader = new BindingReceiver<>(header);
      this.currVotes = new BindingReceiver<>(currVotes);
      this.prevVotes = new BindingReceiver<>(prevVotes);
      this.swingComparator = comparator;
      return this;
    }

    public <T> SeatScreenBuilder<KT, CT, PT> withPartyMap(
        Binding<Map<T, Shape>> shapes,
        Binding<Map<T, Party>> winners,
        Binding<List<T>> focus,
        Binding<String> headerBinding) {
      this.mapBuilder =
          new MapBuilder(
              shapes, winners.map(BasicResultPanel::partyMapToResultMap), focus, headerBinding);
      return this;
    }

    public <T> SeatScreenBuilder<KT, CT, PT> withResultMap(
        Binding<Map<T, Shape>> shapes,
        Binding<Map<T, PartyResult>> winners,
        Binding<List<T>> focus,
        Binding<String> headerBinding) {
      this.mapBuilder = new MapBuilder(shapes, winners, focus, headerBinding);
      return this;
    }

    public SeatScreenBuilder<KT, CT, PT> withNotes(Binding<String> notes) {
      this.notes = new BindingReceiver<>(notes);
      return this;
    }

    public BasicResultPanel build(Binding<String> textHeader) {
      return new BasicResultPanel(
          createHeaderLabel(textHeader),
          createFrame(),
          null,
          createDiffFrame(),
          createSwingFrame(),
          createMapFrame());
    }

    protected abstract BarFrame createFrame();

    protected abstract BarFrame createDiffFrame();

    private SwingFrame createSwingFrame() {
      if (swingHeader == null) {
        return null;
      }
      return SwingFrameBuilder.prevCurr(
              prevVotes.getBinding(), currVotes.getBinding(), swingComparator)
          .withHeader(swingHeader.getBinding())
          .build();
    }

    private MapFrame createMapFrame() {
      if (mapBuilder == null) {
        return null;
      }
      return mapBuilder.createMapFrame();
    }
  }

  private static class BasicSeatScreenBuilder<KT> extends SeatScreenBuilder<KT, Integer, Integer> {

    private BasicSeatScreenBuilder(
        BindingReceiver<Map<KT, Integer>> current,
        BindingReceiver<String> header,
        BindingReceiver<String> subhead,
        KeyTemplate<KT> keyTemplate) {
      super(current, header, subhead, keyTemplate);
    }

    @Override
    protected CurrDiff<Integer> createFromDiff(Integer curr, Integer diff) {
      return new CurrDiff<>(valOrZero(curr), valOrZero(diff));
    }

    @Override
    protected CurrDiff<Integer> createFromDiff(Integer diff) {
      return new CurrDiff<>(0, valOrZero(diff));
    }

    @Override
    protected CurrDiff<Integer> createFromPrev(Integer curr, Integer prev) {
      return new CurrDiff<>(valOrZero(curr), valOrZero(curr) - valOrZero(prev));
    }

    @Override
    protected CurrDiff<Integer> createFromPrev(Integer prev) {
      return new CurrDiff<>(0, -valOrZero(prev));
    }

    private int valOrZero(Integer val) {
      return val == null ? 0 : val;
    }

    private static class Result<KT> extends Bindable<Result.Property> {
      private enum Property {
        SEATS,
        WINNER
      }

      private Map<KT, Integer> seats = new HashMap<>();
      private KT winner;

      public void setSeats(Map<KT, Integer> seats) {
        this.seats = seats;
        onPropertyRefreshed(Property.SEATS);
      }

      public void setWinner(KT winner) {
        this.winner = winner;
        onPropertyRefreshed(Property.WINNER);
      }
    }

    protected int doubleLineBarLimit() {
      return 10;
    }

    protected BarFrame createFrame() {
      Result<KT> result = new Result<>();
      current.getBinding().bind(result::setSeats);
      if (winner != null) {
        winner.getBinding().bind(result::setWinner);
      }
      Binding<List<BarFrameBuilder.BasicBar>> bars =
          Binding.propertyBinding(
              result,
              r -> {
                int numBars = r.seats.size();
                return r.seats.entrySet().stream()
                    .sorted(
                        Comparator.<Map.Entry<KT, Integer>>comparingInt(
                                e -> e.getKey() == Party.OTHERS ? Integer.MIN_VALUE : e.getValue())
                            .reversed())
                    .map(
                        e ->
                            new BarFrameBuilder.BasicBar(
                                keyTemplate.toMainBarHeader(
                                    e.getKey(), numBars > doubleLineBarLimit()),
                                keyTemplate.toParty(e.getKey()).getColor(),
                                e.getValue(),
                                String.valueOf(e.getValue()),
                                e.getKey().equals(r.winner)
                                    ? keyTemplate.winnerShape(numBars > doubleLineBarLimit())
                                    : null))
                    .collect(Collectors.toList());
              },
              Result.Property.SEATS,
              Result.Property.WINNER);

      BarFrameBuilder builder =
          BarFrameBuilder.basic(bars)
              .withHeader(header.getBinding())
              .withSubhead(subhead.getBinding())
              .withNotes(notes == null ? (() -> null) : notes.getBinding());
      if (total != null) {
        builder = builder.withMax(total.getBinding(t -> t * 2 / 3));
      }
      if (showMajority != null) {
        BindableList<Integer> lines = new BindableList<>();
        showMajority
            .getBinding()
            .bind(
                show -> {
                  lines.clear();
                  if (show) {
                    lines.add(total.getValue() / 2 + 1);
                  }
                });
        total
            .getBinding()
            .bind(
                t -> {
                  if (!lines.isEmpty()) {
                    lines.set(0, t / 2 + 1);
                  }
                });
        builder = builder.withLines(lines, majorityFunction);
      }
      return builder.build();
    }

    protected BarFrame createDiffFrame() {
      if (changeHeader == null) {
        return null;
      }

      Binding<List<BarFrameBuilder.BasicBar>> bars =
          diff.getBinding(
              map ->
                  map.entrySet().stream()
                      .sorted(
                          Comparator.<Map.Entry<Party, CurrDiff<Integer>>>comparingInt(
                                  e ->
                                      e.getKey() == Party.OTHERS
                                          ? Integer.MIN_VALUE
                                          : e.getValue().curr)
                              .reversed())
                      .map(
                          e ->
                              new BarFrameBuilder.BasicBar(
                                  e.getKey().getAbbreviation().toUpperCase(),
                                  e.getKey().getColor(),
                                  e.getValue().diff,
                                  changeStr(e.getValue().diff)))
                      .collect(Collectors.toList()));

      BarFrameBuilder builder =
          BarFrameBuilder.basic(bars)
              .withHeader(changeHeader.getBinding())
              .withSubhead(changeSubhead.getBinding());
      if (total != null) {
        builder = builder.withWingspan(total.getBinding(t -> Math.max(1, t / 20)));
      }
      return builder.build();
    }

    private static String changeStr(Integer seats) {
      return seats == 0 ? "\u00b10" : new DecimalFormat("+0;-0").format(seats);
    }
  }

  private static class DualSeatScreenBuilder<KT>
      extends SeatScreenBuilder<KT, Pair<Integer, Integer>, Pair<Integer, Integer>> {

    private DualSeatScreenBuilder(
        BindingReceiver<Map<KT, Pair<Integer, Integer>>> current,
        BindingReceiver<String> header,
        BindingReceiver<String> subhead,
        KeyTemplate<KT> keyTemplate) {
      super(current, header, subhead, keyTemplate);
    }

    @Override
    protected CurrDiff<Pair<Integer, Integer>> createFromDiff(
        Pair<Integer, Integer> curr, Pair<Integer, Integer> diff) {
      return new CurrDiff<>(valOrZero(curr), valOrZero(diff));
    }

    @Override
    protected CurrDiff<Pair<Integer, Integer>> createFromDiff(Pair<Integer, Integer> diff) {
      return new CurrDiff<>(ImmutablePair.of(0, 0), valOrZero(diff));
    }

    @Override
    protected CurrDiff<Pair<Integer, Integer>> createFromPrev(
        Pair<Integer, Integer> curr, Pair<Integer, Integer> prev) {
      Pair<Integer, Integer> c = valOrZero(curr);
      Pair<Integer, Integer> p = valOrZero(prev);
      return new CurrDiff<>(
          c, ImmutablePair.of(c.getLeft() - p.getLeft(), c.getRight() - p.getRight()));
    }

    @Override
    protected CurrDiff<Pair<Integer, Integer>> createFromPrev(Pair<Integer, Integer> prev) {
      Pair<Integer, Integer> p = valOrZero(prev);
      return new CurrDiff<>(ImmutablePair.of(0, 0), ImmutablePair.of(-p.getLeft(), -p.getRight()));
    }

    private Pair<Integer, Integer> valOrZero(Pair<Integer, Integer> val) {
      return val == null ? ImmutablePair.of(0, 0) : val;
    }

    private static class Result<KT> extends Bindable<Result.Property> {
      private enum Property {
        SEATS,
        WINNER
      }

      private Map<KT, Pair<Integer, Integer>> seats = new HashMap<>();
      private KT winner;

      public void setSeats(Map<KT, Pair<Integer, Integer>> seats) {
        this.seats = seats;
        onPropertyRefreshed(Property.SEATS);
      }

      public void setWinner(KT winner) {
        this.winner = winner;
        onPropertyRefreshed(Property.WINNER);
      }
    }

    protected int doubleLineBarLimit() {
      return 10;
    }

    protected BarFrame createFrame() {
      Result<KT> result = new Result<>();
      current.getBinding().bind(result::setSeats);
      if (winner != null) {
        winner.getBinding().bind(result::setWinner);
      }
      Binding<List<BarFrameBuilder.DualBar>> bars =
          Binding.propertyBinding(
              result,
              r -> {
                int count = r.seats.size();
                return r.seats.entrySet().stream()
                    .sorted(
                        Comparator.<Map.Entry<KT, Pair<Integer, Integer>>>comparingInt(
                                e ->
                                    e.getKey() == Party.OTHERS
                                        ? Integer.MIN_VALUE
                                        : e.getValue().getRight())
                            .reversed())
                    .map(
                        e ->
                            new BarFrameBuilder.DualBar(
                                keyTemplate.toMainBarHeader(
                                    e.getKey(), count > doubleLineBarLimit()),
                                keyTemplate.toParty(e.getKey()).getColor(),
                                e.getValue().getLeft(),
                                e.getValue().getRight(),
                                e.getValue().getLeft() + "/" + e.getValue().getRight(),
                                e.getKey().equals(r.winner)
                                    ? keyTemplate.winnerShape(count > doubleLineBarLimit())
                                    : null))
                    .collect(Collectors.toList());
              },
              Result.Property.SEATS,
              Result.Property.WINNER);

      BarFrameBuilder builder =
          BarFrameBuilder.dual(bars)
              .withHeader(header.getBinding())
              .withSubhead(subhead.getBinding())
              .withNotes(notes == null ? (() -> null) : notes.getBinding());
      if (total != null) {
        builder = builder.withMax(total.getBinding(t -> t * 2 / 3));
      }
      if (showMajority != null) {
        BindableList<Integer> lines = new BindableList<>();
        showMajority
            .getBinding()
            .bind(
                show -> {
                  lines.clear();
                  if (show) {
                    lines.add(total.getValue() / 2 + 1);
                  }
                });
        total
            .getBinding()
            .bind(
                t -> {
                  if (!lines.isEmpty()) {
                    lines.set(0, t / 2 + 1);
                  }
                });
        builder = builder.withLines(lines, majorityFunction);
      }
      return builder.build();
    }

    protected BarFrame createDiffFrame() {
      if (changeHeader == null) {
        return null;
      }

      Binding<List<BarFrameBuilder.DualBar>> bars =
          diff.getBinding(
              map ->
                  map.entrySet().stream()
                      .sorted(
                          Comparator
                              .<Map.Entry<Party, CurrDiff<Pair<Integer, Integer>>>>comparingInt(
                                  e ->
                                      e.getKey() == Party.OTHERS
                                          ? Integer.MIN_VALUE
                                          : e.getValue().curr.getRight())
                              .reversed())
                      .map(
                          e ->
                              new BarFrameBuilder.DualBar(
                                  e.getKey().getAbbreviation().toUpperCase(),
                                  e.getKey().getColor(),
                                  e.getValue().diff.getLeft(),
                                  e.getValue().diff.getRight(),
                                  changeStr(e.getValue().diff.getLeft())
                                      + "/"
                                      + changeStr(e.getValue().diff.getRight())))
                      .collect(Collectors.toList()));
      BarFrameBuilder builder =
          BarFrameBuilder.dual(bars)
              .withHeader(changeHeader.getBinding())
              .withSubhead(changeSubhead.getBinding());
      if (total != null) {
        builder = builder.withWingspan(total.getBinding(t -> Math.max(1, t / 20)));
      }
      return builder.build();
    }

    private static String changeStr(Integer seats) {
      return seats == 0 ? "\u00b10" : new DecimalFormat("+0;-0").format(seats);
    }
  }

  private static class RangeSeatScreenBuilder<KT>
      extends SeatScreenBuilder<KT, Range<Integer>, Integer> {

    private RangeSeatScreenBuilder(
        BindingReceiver<Map<KT, Range<Integer>>> current,
        BindingReceiver<String> header,
        BindingReceiver<String> subhead,
        KeyTemplate<KT> keyTemplate) {
      super(current, header, subhead, keyTemplate);
    }

    @Override
    protected CurrDiff<Range<Integer>> createFromDiff(Range<Integer> curr, Range<Integer> diff) {
      return new CurrDiff<>(valOrZero(curr), valOrZero(diff));
    }

    @Override
    protected CurrDiff<Range<Integer>> createFromDiff(Range<Integer> diff) {
      return new CurrDiff<>(Range.is(0), valOrZero(diff));
    }

    @Override
    protected CurrDiff<Range<Integer>> createFromPrev(Range<Integer> curr, Integer prev) {
      Range<Integer> c = valOrZero(curr);
      Integer p = valOrZero(prev);
      return new CurrDiff<>(c, Range.between(c.getMinimum() - p, c.getMaximum() - p));
    }

    @Override
    protected CurrDiff<Range<Integer>> createFromPrev(Integer prev) {
      int p = valOrZero(prev);
      return new CurrDiff<>(Range.is(0), Range.is(-p));
    }

    private Range<Integer> valOrZero(Range<Integer> val) {
      return val == null ? Range.is(0) : val;
    }

    private int valOrZero(Integer val) {
      return val == null ? 0 : val;
    }

    private static class Result<KT> extends Bindable<Result.Property> {
      private enum Property {
        SEATS,
        WINNER
      }

      private Map<KT, Range<Integer>> seats = new HashMap<>();
      private KT winner;

      public void setSeats(Map<KT, Range<Integer>> seats) {
        this.seats = seats;
        onPropertyRefreshed(Property.SEATS);
      }

      public void setWinner(KT winner) {
        this.winner = winner;
        onPropertyRefreshed(Property.WINNER);
      }
    }

    protected int doubleLineBarLimit() {
      return 10;
    }

    protected BarFrame createFrame() {
      Result<KT> result = new Result<>();
      current.getBinding().bind(result::setSeats);
      if (winner != null) {
        winner.getBinding().bind(result::setWinner);
      }
      Binding<List<BarFrameBuilder.DualBar>> bars =
          Binding.propertyBinding(
              result,
              r -> {
                int count = r.seats.size();
                return r.seats.entrySet().stream()
                    .sorted(
                        Comparator.<Map.Entry<KT, Range<Integer>>>comparingInt(
                                e ->
                                    e.getKey() == Party.OTHERS
                                        ? Integer.MIN_VALUE
                                        : (e.getValue().getMinimum() + e.getValue().getMaximum()))
                            .reversed())
                    .map(
                        e ->
                            new BarFrameBuilder.DualBar(
                                keyTemplate.toMainBarHeader(
                                    e.getKey(), count > doubleLineBarLimit()),
                                keyTemplate.toParty(e.getKey()).getColor(),
                                e.getValue().getMinimum(),
                                e.getValue().getMaximum(),
                                e.getValue().getMinimum() + "-" + e.getValue().getMaximum(),
                                e.getKey().equals(r.winner)
                                    ? keyTemplate.winnerShape(count > doubleLineBarLimit())
                                    : null))
                    .collect(Collectors.toList());
              },
              Result.Property.SEATS,
              Result.Property.WINNER);

      BarFrameBuilder builder =
          BarFrameBuilder.dual(bars)
              .withHeader(header.getBinding())
              .withSubhead(subhead.getBinding())
              .withNotes(notes == null ? (() -> null) : notes.getBinding());
      if (total != null) {
        builder = builder.withMax(total.getBinding(t -> t * 2 / 3));
      }
      if (showMajority != null) {
        BindableList<Integer> lines = new BindableList<>();
        showMajority
            .getBinding()
            .bind(
                show -> {
                  lines.clear();
                  if (show) {
                    lines.add(total.getValue() / 2 + 1);
                  }
                });
        total
            .getBinding()
            .bind(
                t -> {
                  if (!lines.isEmpty()) {
                    lines.set(0, t / 2 + 1);
                  }
                });
        builder = builder.withLines(lines, majorityFunction);
      }
      return builder.build();
    }

    protected BarFrame createDiffFrame() {
      if (changeHeader == null) {
        return null;
      }

      Binding<List<BarFrameBuilder.DualBar>> bars =
          diff.getBinding(
              map -> {
                return map.entrySet().stream()
                    .sorted(
                        Comparator.<Map.Entry<Party, CurrDiff<Range<Integer>>>>comparingInt(
                                e ->
                                    e.getKey() == Party.OTHERS
                                        ? Integer.MIN_VALUE
                                        : (e.getValue().curr.getMinimum()
                                            + e.getValue().curr.getMaximum()))
                            .reversed())
                    .map(
                        e ->
                            new BarFrameBuilder.DualBar(
                                e.getKey().getAbbreviation().toUpperCase(),
                                e.getKey().getColor(),
                                e.getValue().diff.getMinimum(),
                                e.getValue().diff.getMaximum(),
                                "("
                                    + changeStr(e.getValue().diff.getMinimum())
                                    + ")-("
                                    + changeStr(e.getValue().diff.getMaximum())
                                    + ")"))
                    .collect(Collectors.toList());
              });
      BarFrameBuilder builder =
          BarFrameBuilder.dual(bars)
              .withHeader(changeHeader.getBinding())
              .withSubhead(changeSubhead.getBinding());
      if (total != null) {
        builder = builder.withWingspan(total.getBinding(t -> Math.max(1, t / 20)));
      }
      return builder.build();
    }

    private static String changeStr(Integer seats) {
      return seats == 0 ? "\u00b10" : new DecimalFormat("+0;-0").format(seats);
    }
  }

  private interface VoteTemplate {
    String toBarString(int votes, double pct, boolean forceSingleLine);
  }

  private static class PctOnlyTemplate implements VoteTemplate {

    @Override
    public String toBarString(int votes, double pct, boolean forceSingleLine) {
      return PCT_FORMAT.format(pct);
    }
  }

  private static class VotePctTemplate implements VoteTemplate {

    @Override
    public String toBarString(int votes, double pct, boolean forceSingleLine) {
      return THOUSANDS_FORMAT.format(votes)
          + (forceSingleLine ? " (" : "\n")
          + PCT_FORMAT.format(pct)
          + (forceSingleLine ? ")" : "");
    }
  }

  private static class VotePctOnlyTemplate implements VoteTemplate {

    @Override
    public String toBarString(int votes, double pct, boolean forceSingleLine) {
      return PCT_FORMAT.format(pct);
    }
  }

  public abstract static class VoteScreenBuilder<KT, CT, CPT, PT> {
    protected final KeyTemplate<KT> keyTemplate;
    protected final VoteTemplate voteTemplate;
    protected final KT others;

    protected BindingReceiver<Map<KT, CT>> current;
    protected BindingReceiver<String> header;
    protected BindingReceiver<String> subhead;
    protected BindingReceiver<Boolean> showMajority;
    protected BindingReceiver<String> majorityLabel;
    protected BindingReceiver<KT> winner;
    protected BindingReceiver<Set<KT>> runoff;
    protected BindingReceiver<Double> pctReporting;
    protected BindingReceiver<String> notes;
    protected int limit = Integer.MAX_VALUE;
    protected Set<Party> mandatoryParties = Set.of();

    protected BindingReceiver<Map<Party, PT>> prev;
    protected BindingReceiver<String> changeHeader;
    protected BindingReceiver<String> changeSubhead;

    protected BindingReceiver<Map<KT, CT>> currPreferences;
    protected BindingReceiver<Map<Party, PT>> prevPreferences;
    protected BindingReceiver<String> preferenceHeader;
    protected BindingReceiver<String> preferenceSubhead;
    protected BindingReceiver<Double> preferencePctReporting;

    protected BindingReceiver<String> swingHeader;
    protected Comparator<Party> swingComparator;

    protected MapBuilder mapBuilder;

    private VoteScreenBuilder(
        BindingReceiver<Map<KT, CT>> current,
        BindingReceiver<String> header,
        BindingReceiver<String> subhead,
        KeyTemplate<KT> keyTemplate,
        VoteTemplate voteTemplate,
        KT others) {
      this.keyTemplate = keyTemplate;
      this.current = current;
      this.header = header;
      this.subhead = subhead;
      this.voteTemplate = voteTemplate;
      this.others = others;
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withPrev(
        Binding<? extends Map<Party, PT>> prev, Binding<String> header) {
      return withPrev(prev, header, Binding.fixedBinding(null));
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withPrev(
        Binding<? extends Map<Party, PT>> prev, Binding<String> header, Binding<String> subhead) {
      this.prev = new BindingReceiver<>(prev);
      this.changeHeader = new BindingReceiver<>(header);
      this.changeSubhead = new BindingReceiver<>(subhead);
      return this;
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withPreferences(
        Binding<? extends Map<KT, CT>> preferences,
        Binding<String> preferenceHeader,
        Binding<String> preferenceSubhead) {
      this.currPreferences = new BindingReceiver<>(preferences);
      this.preferenceHeader = new BindingReceiver<>(preferenceHeader);
      this.preferenceSubhead = new BindingReceiver<>(preferenceSubhead);
      return this;
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withPrevPreferences(
        Binding<? extends Map<Party, PT>> prevPreferences) {
      this.prevPreferences = new BindingReceiver<>(prevPreferences);
      return this;
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withWinner(Binding<KT> winner) {
      this.winner = new BindingReceiver<>(winner);
      return this;
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withRunoff(Binding<Set<KT>> runoff) {
      this.runoff = new BindingReceiver<>(runoff);
      return this;
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withPctReporting(Binding<Double> pctReporting) {
      this.pctReporting = new BindingReceiver<>(pctReporting);
      return this;
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withPreferencePctReporting(
        Binding<Double> preferencePctReporting) {
      this.preferencePctReporting = new BindingReceiver<>(preferencePctReporting);
      return this;
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withSwing(
        Comparator<Party> comparator, Binding<String> header) {
      this.swingComparator = comparator;
      this.swingHeader = new BindingReceiver<>(header);
      return this;
    }

    public <T> VoteScreenBuilder<KT, CT, CPT, PT> withPartyMap(
        Binding<Map<T, Shape>> shapes,
        Binding<Map<T, Party>> winners,
        Binding<List<T>> focus,
        Binding<String> headerBinding) {
      this.mapBuilder =
          new MapBuilder(
              shapes, winners.map(BasicResultPanel::partyMapToResultMap), focus, headerBinding);
      return this;
    }

    public <T> VoteScreenBuilder<KT, CT, CPT, PT> withPartyMap(
        Binding<Map<T, Shape>> shapes,
        Binding<T> selectedShape,
        Binding<Party> leadingParty,
        Binding<List<T>> focus,
        Binding<String> header) {
      this.mapBuilder =
          new MapBuilder(
              shapes, selectedShape, leadingParty.map(PartyResult::elected), focus, header);
      return this;
    }

    public <T> VoteScreenBuilder<KT, CT, CPT, PT> withResultMap(
        Binding<Map<T, Shape>> shapes,
        Binding<Map<T, PartyResult>> winners,
        Binding<List<T>> focus,
        Binding<String> headerBinding) {
      this.mapBuilder = new MapBuilder(shapes, winners, focus, headerBinding);
      return this;
    }

    public <T> VoteScreenBuilder<KT, CT, CPT, PT> withResultMap(
        Binding<Map<T, Shape>> shapes,
        Binding<T> selectedShape,
        Binding<PartyResult> leadingParty,
        Binding<List<T>> focus,
        Binding<String> header) {
      this.mapBuilder = new MapBuilder(shapes, selectedShape, leadingParty, focus, header);
      return this;
    }

    public <T> VoteScreenBuilder<KT, CT, CPT, PT> withResultMap(
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

    public VoteScreenBuilder<KT, CT, CPT, PT> withMajorityLine(
        Binding<Boolean> showMajority, Binding<String> majorityLabel) {
      this.showMajority = new BindingReceiver<>(showMajority);
      this.majorityLabel = new BindingReceiver<>(majorityLabel);
      return this;
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withNotes(Binding<String> notes) {
      this.notes = new BindingReceiver<>(notes);
      return this;
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withLimit(int limit, Party... mandatoryParties) {
      if (limit <= 0) {
        throw new IllegalArgumentException("Invalid limit: " + limit);
      }
      this.limit = limit;
      this.mandatoryParties = Set.of(mandatoryParties);
      return this;
    }

    public BasicResultPanel build(Binding<String> textHeader) {
      return new BasicResultPanel(
          createHeaderLabel(textHeader),
          createFrame(),
          createPreferenceFrame(),
          createDiffFrame(),
          createSwingFrame(),
          createMapFrame());
    }

    protected abstract BarFrame createFrame();

    protected abstract BarFrame createPreferenceFrame();

    protected abstract BarFrame createDiffFrame();

    protected abstract SwingFrame createSwingFrame();

    private MapFrame createMapFrame() {
      if (mapBuilder == null) {
        return null;
      }
      return mapBuilder.createMapFrame();
    }
  }

  private static class BasicVoteScreenBuilder<KT>
      extends VoteScreenBuilder<KT, Integer, Double, Integer> {

    public BasicVoteScreenBuilder(
        BindingReceiver<Map<KT, Integer>> current,
        BindingReceiver<String> header,
        BindingReceiver<String> subhead,
        KeyTemplate<KT> keyTemplate,
        VoteTemplate voteTemplate,
        KT others) {
      super(current, header, subhead, keyTemplate, voteTemplate, others);
    }

    private static class Result<KT> extends Bindable<Result.Property> {
      private enum Property {
        VOTES,
        WINNER,
        RUNOFF
      }

      private Map<KT, Integer> votes = new HashMap<>();
      private KT winner;
      private Set<KT> runoff = new HashSet<>();

      public void setVotes(Map<KT, Integer> votes) {
        this.votes = votes;
        onPropertyRefreshed(Property.VOTES);
      }

      public void setWinner(KT winner) {
        this.winner = winner;
        onPropertyRefreshed(Property.WINNER);
      }

      public void setRunoff(Set<KT> runoff) {
        this.runoff = (runoff == null ? Set.of() : runoff);
        onPropertyRefreshed(Property.RUNOFF);
      }
    }

    protected int doubleLineBarLimit() {
      return currPreferences == null ? 10 : 6;
    }

    @Override
    protected BarFrame createFrame() {
      Result<KT> result = new Result<>();
      current.getBinding().bind(result::setVotes);
      if (winner != null) {
        winner.getBinding().bind(result::setWinner);
      }
      if (runoff != null) {
        runoff.getBinding().bind(result::setRunoff);
      }
      Binding<List<BarFrameBuilder.BasicBar>> bars =
          Binding.propertyBinding(
              result,
              r -> {
                int total =
                    r.votes.values().stream().filter(Objects::nonNull).mapToInt(i -> i).sum();
                var mandatory =
                    (KT[])
                        Stream.concat(
                                r.votes.keySet().stream()
                                    .filter(k -> mandatoryParties.contains(keyTemplate.toParty(k))),
                                Stream.concat(r.runoff.stream(), Stream.of(r.winner))
                                    .filter(Objects::nonNull))
                            .filter(Objects::nonNull)
                            .toArray();
                var aggregatedResult = Aggregators.topAndOthers(r.votes, limit, others, mandatory);
                int count = aggregatedResult.size();
                boolean partialDeclaration = r.votes.values().stream().anyMatch(Objects::isNull);
                return aggregatedResult.entrySet().stream()
                    .sorted(
                        Comparator.<Map.Entry<KT, Integer>>comparingInt(
                                e -> {
                                  if (e.getKey() == others) return Integer.MIN_VALUE;
                                  if (e.getValue() == null) return -1;
                                  return e.getValue();
                                })
                            .reversed())
                    .map(
                        e -> {
                          double pct =
                              e.getValue() == null ? Double.NaN : (1.0 * e.getValue() / total);
                          String valueLabel;
                          if (count == 1) {
                            valueLabel = "UNCONTESTED";
                          } else if (Double.isNaN(pct)) {
                            valueLabel = "WAITING...";
                          } else if (partialDeclaration) {
                            valueLabel = THOUSANDS_FORMAT.format(e.getValue());
                          } else {
                            valueLabel =
                                voteTemplate.toBarString(
                                    e.getValue(), pct, count > doubleLineBarLimit());
                          }
                          Shape shape;
                          if (e.getKey().equals(r.winner))
                            shape = keyTemplate.winnerShape(count > doubleLineBarLimit());
                          else if (r.runoff.contains(e.getKey()))
                            shape = keyTemplate.runoffShape(count > doubleLineBarLimit());
                          else shape = null;
                          return new BarFrameBuilder.BasicBar(
                              keyTemplate.toMainBarHeader(e.getKey(), count > doubleLineBarLimit()),
                              keyTemplate.toParty(e.getKey()).getColor(),
                              Double.isNaN(pct) ? 0 : pct,
                              valueLabel,
                              shape);
                        })
                    .collect(Collectors.toList());
              },
              Result.Property.VOTES,
              Result.Property.WINNER,
              Result.Property.RUNOFF);
      BarFrameBuilder builder =
          BarFrameBuilder.basic(bars)
              .withHeader(header.getBinding())
              .withSubhead(subhead.getBinding())
              .withNotes(notes == null ? (() -> null) : notes.getBinding())
              .withMax(
                  pctReporting == null
                      ? (() -> 2.0 / 3)
                      : pctReporting.getBinding(x -> 2.0 / 3 / Math.max(1e-6, x)));
      if (showMajority != null) {
        BindableList<Double> lines = new BindableList<>();
        showMajority
            .getBinding()
            .bind(
                show -> {
                  lines.clear();
                  if (show) {
                    lines.add(
                        pctReporting == null ? 0.5 : 0.5 / Math.max(1e-6, pctReporting.getValue()));
                  }
                });
        if (pctReporting != null) {
          pctReporting
              .getBinding()
              .bind(
                  pct -> {
                    if (!lines.isEmpty()) {
                      lines.set(0, 0.5 / Math.max(1e-6, pct));
                    }
                  });
        }
        showMajority
            .getBinding()
            .bind(
                label -> {
                  if (!lines.isEmpty()) {
                    lines.setAll(lines);
                  }
                });
        builder = builder.withLines(lines, n -> majorityLabel.getValue());
      }
      return builder.build();
    }

    private static class Change<KT> extends Bindable<Change.Property> {
      private enum Property {
        CURR,
        PREV
      }

      private Map<KT, Integer> currVotes = new HashMap<>();
      private Map<Party, Integer> prevVotes = new HashMap<>();

      public void setCurrVotes(Map<KT, Integer> currVotes) {
        this.currVotes = currVotes;
        onPropertyRefreshed(Property.CURR);
      }

      public void setPrevVotes(Map<Party, Integer> prevVotes) {
        this.prevVotes = prevVotes;
        onPropertyRefreshed(Property.PREV);
      }
    }

    @Override
    protected BarFrame createPreferenceFrame() {
      if (currPreferences == null) {
        return null;
      }
      Result<KT> result = new Result<>();
      currPreferences.getBinding().bind(result::setVotes);
      if (winner != null) {
        winner.getBinding().bind(result::setWinner);
      }
      Binding<List<BarFrameBuilder.BasicBar>> bars =
          Binding.propertyBinding(
              result,
              r -> {
                int total =
                    r.votes.values().stream().filter(Objects::nonNull).mapToInt(i -> i).sum();
                boolean partialDeclaration = r.votes.values().stream().anyMatch(Objects::isNull);
                int count = r.votes.size();
                return r.votes.entrySet().stream()
                    .sorted(
                        Comparator.<Map.Entry<KT, Integer>>comparingInt(
                                e ->
                                    e.getKey() == others
                                        ? Integer.MIN_VALUE
                                        : (e.getValue() == null ? -1 : e.getValue()))
                            .reversed())
                    .map(
                        e -> {
                          double pct =
                              e.getValue() == null ? Double.NaN : 1.0 * e.getValue() / total;
                          String valueLabel;
                          if (count == 1) {
                            valueLabel = "ELECTED";
                          } else if (Double.isNaN(pct)) {
                            valueLabel = "WAITING...";
                          } else if (partialDeclaration) {
                            valueLabel = THOUSANDS_FORMAT.format(e.getValue());
                          } else {
                            valueLabel = voteTemplate.toBarString(e.getValue(), pct, true);
                          }
                          Shape shape;
                          if (e.getKey().equals(r.winner)) shape = keyTemplate.winnerShape(true);
                          else if (r.runoff.contains(e.getKey()))
                            shape = keyTemplate.runoffShape(true);
                          else shape = null;
                          return new BarFrameBuilder.BasicBar(
                              keyTemplate.toMainBarHeader(e.getKey(), true),
                              keyTemplate.toParty(e.getKey()).getColor(),
                              Double.isNaN(pct) ? 0 : pct,
                              valueLabel,
                              shape);
                        })
                    .collect(Collectors.toList());
              },
              Result.Property.VOTES,
              Result.Property.WINNER,
              Result.Property.RUNOFF);
      return BarFrameBuilder.basic(bars)
          .withHeader(preferenceHeader.getBinding())
          .withSubhead(preferenceSubhead.getBinding())
          .withLines(
              preferencePctReporting == null
                  ? Binding.fixedBinding(List.of(0.5))
                  : preferencePctReporting.getBinding(p -> List.of(0.5 / Math.max(p, 1e-6))),
              x -> "50%")
          .withMax(
              preferencePctReporting == null
                  ? Binding.fixedBinding(2.0 / 3)
                  : preferencePctReporting.getBinding(p -> 2.0 / 3 / Math.max(p, 1e-6)))
          .build();
    }

    @Override
    protected BarFrame createDiffFrame() {
      if (prev == null) {
        return null;
      }

      Change<KT> change = new Change<>();
      current.getBinding().bind(change::setCurrVotes);
      prev.getBinding().bind(change::setPrevVotes);
      Binding<List<BarFrameBuilder.BasicBar>> bars =
          Binding.propertyBinding(
              change,
              r -> {
                if (r.currVotes.values().stream().anyMatch(Objects::isNull)) {
                  return List.of();
                }
                var prevWinner =
                    r.prevVotes.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
                if (prevWinner == null
                    || r.currVotes.keySet().stream()
                        .map(keyTemplate::toParty)
                        .noneMatch(prevWinner::equals)) {
                  return List.of();
                }
                int currTotal = r.currVotes.values().stream().mapToInt(i -> i).sum();
                int prevTotal = r.prevVotes.values().stream().mapToInt(i -> i).sum();
                if (currTotal == 0 || prevTotal == 0) {
                  return List.of();
                }
                Map<Party, Integer> partyTotal =
                    Aggregators.topAndOthers(
                        currTotalByParty(r.currVotes),
                        limit,
                        Party.OTHERS,
                        mandatoryParties.toArray(new Party[0]));
                Map<Party, Integer> prevVotes = new HashMap<>(r.prevVotes);
                r.prevVotes.entrySet().stream()
                    .filter(e -> !partyTotal.containsKey(e.getKey()))
                    .forEach(
                        e -> {
                          partyTotal.putIfAbsent(Party.OTHERS, 0);
                          prevVotes.merge(Party.OTHERS, e.getValue(), Integer::sum);
                        });
                return partyTotal.entrySet().stream()
                    .sorted(
                        Comparator.<Map.Entry<Party, Integer>>comparingInt(
                                e -> e.getKey() == Party.OTHERS ? Integer.MIN_VALUE : e.getValue())
                            .reversed())
                    .map(
                        e -> {
                          double cpct = 1.0 * e.getValue() / currTotal;
                          double ppct = 1.0 * (prevVotes.getOrDefault(e.getKey(), 0)) / prevTotal;
                          return new BarFrameBuilder.BasicBar(
                              e.getKey().getAbbreviation().toUpperCase(),
                              e.getKey().getColor(),
                              cpct - ppct,
                              new DecimalFormat("+0.0%;-0.0%").format(cpct - ppct));
                        })
                    .collect(Collectors.toList());
              },
              Change.Property.CURR,
              Change.Property.PREV);
      return BarFrameBuilder.basic(bars)
          .withWingspan(
              pctReporting == null
                  ? (() -> 0.1)
                  : pctReporting.getBinding(x -> 0.1 / Math.max(1e-6, x)))
          .withHeader(changeHeader.getBinding())
          .withSubhead(changeSubhead.getBinding())
          .build();
    }

    @Override
    protected SwingFrame createSwingFrame() {
      if (swingHeader == null) {
        return null;
      }
      Binding<Map<Party, Integer>> curr;
      Binding<Map<Party, Integer>> prev;
      if (currPreferences != null && prevPreferences != null) {
        curr = currPreferences.getBinding(this::currTotalByParty);
        prev =
            prevPreferences
                .getBinding()
                .merge(
                    currPreferences.getBinding(this::currTotalByParty),
                    (p, c) -> {
                      if (!c.keySet().equals(p.keySet())) {
                        return Map.of();
                      }
                      return p;
                    });
      } else {
        curr = current.getBinding(this::currTotalByParty);
        prev =
            this.prev
                .getBinding()
                .merge(
                    current.getBinding(),
                    (p, c) -> {
                      var prevWinner =
                          p.entrySet().stream()
                              .max(Map.Entry.comparingByValue())
                              .map(Map.Entry::getKey)
                              .orElse(null);
                      if (prevWinner == null
                          || c.keySet().stream()
                              .map(keyTemplate::toParty)
                              .noneMatch(prevWinner::equals)) {
                        return Map.of();
                      }
                      return p;
                    });
      }
      return SwingFrameBuilder.prevCurr(prev, curr, swingComparator)
          .withHeader(swingHeader.getBinding())
          .build();
    }

    protected Map<Party, Integer> currTotalByParty(Map<KT, ? extends Integer> curr) {
      if (curr.values().stream().anyMatch(Objects::isNull)) {
        return Map.of();
      }
      Map<Party, Integer> ret = new LinkedHashMap<>();
      curr.forEach((k, v) -> ret.merge(keyTemplate.toParty(k), v, Integer::sum));
      return ret;
    }
  }

  private static class RangeVoteScreenBuilder<KT>
      extends VoteScreenBuilder<KT, Range<Double>, Double, Integer> {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");
    private static final DecimalFormat CHANGE_DECIMAL_FORMAT = new DecimalFormat("+0.0;-0.0");

    private RangeVoteScreenBuilder(
        BindingReceiver<Map<KT, Range<Double>>> current,
        BindingReceiver<String> header,
        BindingReceiver<String> subhead,
        KeyTemplate<KT> keyTemplate,
        VoteTemplate voteTemplate,
        KT others) {
      super(current, header, subhead, keyTemplate, voteTemplate, others);
    }

    @Override
    protected BarFrame createFrame() {
      Binding<List<BarFrameBuilder.DualBar>> bars =
          current.getBinding(
              r -> {
                return r.entrySet().stream()
                    .sorted(
                        Comparator.<Map.Entry<KT, Range<Double>>>comparingDouble(
                                e ->
                                    e.getKey() == others
                                        ? Double.MIN_VALUE
                                        : (e.getValue().getMinimum() + e.getValue().getMaximum()))
                            .reversed())
                    .map(
                        e -> {
                          String valueLabel =
                              DECIMAL_FORMAT.format(100 * e.getValue().getMinimum())
                                  + "-"
                                  + new DecimalFormat("0.0").format(100 * e.getValue().getMaximum())
                                  + "%";
                          return new BarFrameBuilder.DualBar(
                              keyTemplate.toMainBarHeader(e.getKey(), false),
                              keyTemplate.toParty(e.getKey()).getColor(),
                              e.getValue().getMinimum(),
                              e.getValue().getMaximum(),
                              valueLabel);
                        })
                    .collect(Collectors.toList());
              });
      BarFrameBuilder builder =
          BarFrameBuilder.dual(bars)
              .withHeader(header.getBinding())
              .withSubhead(subhead.getBinding())
              .withNotes(notes == null ? (() -> null) : notes.getBinding())
              .withMax(() -> 2.0 / 3);
      if (showMajority != null) {
        BindableList<Double> lines = new BindableList<>();
        showMajority
            .getBinding()
            .bind(
                show -> {
                  lines.clear();
                  if (show) {
                    lines.add(0.5);
                  }
                });
        showMajority
            .getBinding()
            .bind(
                label -> {
                  if (!lines.isEmpty()) {
                    lines.setAll(lines);
                  }
                });
        builder = builder.withLines(lines, n -> majorityLabel.getValue());
      }
      return builder.build();
    }

    private static class Change<KT> extends Bindable<RangeVoteScreenBuilder.Change.Property> {
      private enum Property {
        CURR,
        PREV
      }

      private Map<KT, Range<Double>> currVotes = new HashMap<>();
      private Map<Party, Integer> prevVotes = new HashMap<>();

      public void setCurrVotes(Map<KT, Range<Double>> currVotes) {
        this.currVotes = currVotes;
        onPropertyRefreshed(RangeVoteScreenBuilder.Change.Property.CURR);
      }

      public void setPrevVotes(Map<Party, Integer> prevVotes) {
        this.prevVotes = prevVotes;
        onPropertyRefreshed(RangeVoteScreenBuilder.Change.Property.PREV);
      }
    }

    @Override
    protected BarFrame createDiffFrame() {
      if (prev == null) {
        return null;
      }

      RangeVoteScreenBuilder.Change<KT> change = new RangeVoteScreenBuilder.Change<>();
      current.getBinding().bind(change::setCurrVotes);
      prev.getBinding().bind(change::setPrevVotes);
      Binding<List<BarFrameBuilder.DualBar>> bars =
          Binding.propertyBinding(
              change,
              r -> {
                int prevTotal = r.prevVotes.values().stream().mapToInt(i -> i).sum();
                if (prevTotal == 0) {
                  return List.of();
                }
                Map<Party, Range<Double>> partyTotal = currTotalByParty(r.currVotes);
                Map<Party, Integer> prevVotes = new HashMap<>(r.prevVotes);
                r.prevVotes.entrySet().stream()
                    .filter(e -> !partyTotal.containsKey(e.getKey()))
                    .forEach(
                        e -> {
                          partyTotal.putIfAbsent(Party.OTHERS, Range.is(0.0));
                          prevVotes.merge(Party.OTHERS, e.getValue(), Integer::sum);
                        });
                return partyTotal.entrySet().stream()
                    .sorted(
                        Comparator.<Map.Entry<Party, Range<Double>>>comparingDouble(
                                e ->
                                    e.getKey() == Party.OTHERS
                                        ? Double.MIN_VALUE
                                        : (e.getValue().getMinimum() + e.getValue().getMaximum()))
                            .reversed())
                    .map(
                        e -> {
                          double cpctMin = e.getValue().getMinimum();
                          double cpctMax = e.getValue().getMaximum();
                          double ppct = 1.0 * (prevVotes.getOrDefault(e.getKey(), 0)) / prevTotal;
                          return new BarFrameBuilder.DualBar(
                              e.getKey().getAbbreviation().toUpperCase(),
                              e.getKey().getColor(),
                              cpctMin - ppct,
                              cpctMax - ppct,
                              "("
                                  + CHANGE_DECIMAL_FORMAT.format(100.0 * (cpctMin - ppct))
                                  + ")-("
                                  + CHANGE_DECIMAL_FORMAT.format(100.0 * (cpctMax - ppct))
                                  + ")%");
                        })
                    .collect(Collectors.toList());
              },
              RangeVoteScreenBuilder.Change.Property.CURR,
              RangeVoteScreenBuilder.Change.Property.PREV);
      return BarFrameBuilder.dual(bars)
          .withWingspan(() -> 0.1)
          .withHeader(changeHeader.getBinding())
          .withSubhead(changeSubhead.getBinding())
          .build();
    }

    @Override
    protected BarFrame createPreferenceFrame() {
      if (currPreferences == null) {
        return null;
      }

      Binding<List<BarFrameBuilder.DualBar>> bars =
          currPreferences.getBinding(
              r -> {
                return r.entrySet().stream()
                    .sorted(
                        Comparator.<Map.Entry<KT, Range<Double>>>comparingDouble(
                                e ->
                                    e.getKey() == others
                                        ? Double.MIN_VALUE
                                        : (e.getValue().getMinimum() + e.getValue().getMaximum()))
                            .reversed())
                    .map(
                        e -> {
                          String valueLabel =
                              DECIMAL_FORMAT.format(100 * e.getValue().getMinimum())
                                  + "-"
                                  + new DecimalFormat("0.0").format(100 * e.getValue().getMaximum())
                                  + "%";
                          return new BarFrameBuilder.DualBar(
                              keyTemplate.toMainBarHeader(e.getKey(), false),
                              keyTemplate.toParty(e.getKey()).getColor(),
                              e.getValue().getMinimum(),
                              e.getValue().getMaximum(),
                              valueLabel);
                        })
                    .collect(Collectors.toList());
              });
      BarFrameBuilder builder =
          BarFrameBuilder.dual(bars)
              .withHeader(preferenceHeader.getBinding())
              .withSubhead(preferenceSubhead.getBinding())
              .withMax(() -> 2.0 / 3);
      BindableList<Double> lines = new BindableList<>();
      lines.setAll(List.of(0.5));
      builder = builder.withLines(lines, n -> "50%");
      return builder.build();
    }

    @Override
    protected SwingFrame createSwingFrame() {
      if (swingHeader == null) {
        return null;
      }
      Binding<Map<Party, Integer>> curr =
          current
              .getBinding(this::currTotalByParty)
              .map(
                  m -> {
                    Map<Party, Integer> ret = new LinkedHashMap<>();
                    m.forEach(
                        (p, r) ->
                            ret.put(
                                p,
                                (int)
                                    Math.round(1_000_000 * (r.getMinimum() + r.getMaximum()) / 2)));
                    return ret;
                  });
      return SwingFrameBuilder.prevCurr(prev.getBinding(), curr, swingComparator)
          .withHeader(swingHeader.getBinding())
          .build();
    }

    protected Map<Party, Range<Double>> currTotalByParty(Map<KT, ? extends Range<Double>> curr) {
      Map<Party, Range<Double>> ret = new LinkedHashMap<>();
      curr.forEach(
          (k, v) ->
              ret.merge(
                  keyTemplate.toParty(k),
                  v,
                  (a, b) ->
                      Range.between(
                          a.getMinimum() + b.getMinimum(), a.getMaximum() + b.getMaximum())));
      return ret;
    }
  }

  private static class CurrDiff<CT> {
    private final CT curr;
    private final CT diff;

    private CurrDiff(CT curr, CT diff) {
      this.curr = curr;
      this.diff = diff;
    }
  }
}
