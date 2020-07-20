package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.graphics.ImageGenerator;
import com.joecollins.graphics.components.BarFrame;
import com.joecollins.graphics.components.BarFrameBuilder;
import com.joecollins.graphics.components.MapFrame;
import com.joecollins.graphics.components.SwingFrame;
import com.joecollins.graphics.components.SwingFrameBuilder;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
  private final BarFrame changeFrame;
  private final SwingFrame swingFrame;
  private final MapFrame mapFrame;

  private BasicResultPanel(
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
      seatFrame.setSize(width * (seatFrameIsAlone ? 5 : 3) / 5 - 10, height - 10);
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

  private static <T> Map<T, Result> partyMapToResultMap(Map<T, Party> m) {
    Map<T, Result> ret = new LinkedHashMap<>();
    m.forEach((k, v) -> ret.put(k, MapBuilder.Result.elected(v)));
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
          Binding<? extends Map<Party, ? extends Pair<Integer, Integer>>> seats,
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
      Binding<? extends Map<Party, ? extends Range<Integer>>> seats,
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
        new PctOnlyTemplate());
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
        new VotePctTemplate());
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
        new VotePctOnlyTemplate());
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
        new VotePctTemplate());
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
        new VotePctOnlyTemplate());
  }

  private interface KeyTemplate<KT> {
    Party toParty(KT key);

    String toMainBarHeader(KT key);

    Shape winnerShape();
  }

  private static class PartyTemplate implements KeyTemplate<Party> {

    @Override
    public Party toParty(Party key) {
      return key;
    }

    @Override
    public String toMainBarHeader(Party key) {
      return key.getName().toUpperCase();
    }

    @Override
    public Shape winnerShape() {
      return ImageGenerator.createTickShape();
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
    public String toMainBarHeader(Candidate key) {
      return (key.getName()
              + (key.isIncumbent() ? incumbentMarker : "")
              + "\n"
              + key.getParty().getName())
          .toUpperCase();
    }

    @Override
    public Shape winnerShape() {
      return ImageGenerator.createHalfTickShape();
    }
  }

  private static class ResultAndWinner<CT> {
    private CT result;
    private boolean winner;

    public ResultAndWinner(CT result) {
      this.result = result;
      this.winner = false;
    }
  }

  public abstract static class SeatScreenBuilder<KT, CT, PT> {

    protected final KeyTemplate<KT> keyTemplate;

    protected BindingReceiver<Map<KT, ? extends CT>> current;
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
        BindingReceiver<Map<KT, ? extends CT>> current,
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
        Binding<Map<T, Result>> winners,
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

    protected Binding<Map<KT, ResultAndWinner<CT>>> createResultAndWinner() {
      Binding<KT> winnerBinding = winner == null ? (() -> null) : winner.getBinding();
      return current
          .getBinding()
          .merge(
              winnerBinding,
              (s, w) -> {
                Map<KT, ResultAndWinner<CT>> ret = new LinkedHashMap<>();
                s.forEach((k, v) -> ret.put(k, new ResultAndWinner<>(v)));
                if (w != null) {
                  ret.computeIfAbsent(w, x -> new ResultAndWinner<>(createDefault())).winner = true;
                }
                return ret;
              });
    }

    protected abstract CT createDefault();
  }

  private static class BasicSeatScreenBuilder<KT> extends SeatScreenBuilder<KT, Integer, Integer> {

    private BasicSeatScreenBuilder(
        BindingReceiver<Map<KT, ? extends Integer>> current,
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

    protected BarFrame createFrame() {
      Shape tick = keyTemplate.winnerShape();
      BarFrameBuilder builder =
          BarFrameBuilder.basicWithShapes(
                  createResultAndWinner(),
                  party -> keyTemplate.toMainBarHeader(party),
                  party -> keyTemplate.toParty(party).getColor(),
                  result -> result.result,
                  result -> String.valueOf(result.result),
                  (party, result) -> result.winner ? tick : null,
                  (party, result) -> party == Party.OTHERS ? -1 : result.result)
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
      BarFrameBuilder builder =
          BarFrameBuilder.basic(
                  diff.getBinding(),
                  party -> party.getAbbreviation().toUpperCase(),
                  party -> party.getColor(),
                  seats -> seats.diff,
                  seats -> changeStr(seats.diff),
                  (party, seats) -> party == Party.OTHERS ? -1 : seats.curr)
              .withHeader(changeHeader.getBinding())
              .withSubhead(changeSubhead.getBinding());
      if (total != null) {
        builder = builder.withWingspan(total.getBinding(t -> Math.max(1, t / 20)));
      }
      return builder.build();
    }

    @Override
    protected Integer createDefault() {
      return 0;
    }

    private static String changeStr(Integer seats) {
      return seats == 0 ? "\u00b10" : new DecimalFormat("+0;-0").format(seats);
    }
  }

  private static class DualSeatScreenBuilder<KT>
      extends SeatScreenBuilder<KT, Pair<Integer, Integer>, Pair<Integer, Integer>> {

    private DualSeatScreenBuilder(
        BindingReceiver<Map<KT, ? extends Pair<Integer, Integer>>> current,
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

    protected BarFrame createFrame() {
      Shape tick = keyTemplate.winnerShape();
      BarFrameBuilder builder =
          BarFrameBuilder.dualWithShapes(
                  createResultAndWinner(),
                  party -> keyTemplate.toMainBarHeader(party),
                  party -> keyTemplate.toParty(party).getColor(),
                  result -> result.result,
                  result -> result.result.getLeft() + "/" + result.result.getRight(),
                  (party, result) -> result.winner ? tick : null,
                  (party, result) -> party == Party.OTHERS ? -1 : result.result.getRight())
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
      BarFrameBuilder builder =
          BarFrameBuilder.dual(
                  diff.getBinding(),
                  party -> party.getAbbreviation().toUpperCase(),
                  party -> party.getColor(),
                  seats -> seats.diff,
                  seats -> changeStr(seats.diff.getLeft()) + "/" + changeStr(seats.diff.getRight()),
                  (party, seats) -> party == Party.OTHERS ? -1 : seats.curr.getRight())
              .withHeader(changeHeader.getBinding())
              .withSubhead(changeSubhead.getBinding());
      if (total != null) {
        builder = builder.withWingspan(total.getBinding(t -> Math.max(1, t / 20)));
      }
      return builder.build();
    }

    @Override
    protected Pair<Integer, Integer> createDefault() {
      return ImmutablePair.of(0, 0);
    }

    private static String changeStr(Integer seats) {
      return seats == 0 ? "\u00b10" : new DecimalFormat("+0;-0").format(seats);
    }
  }

  private static class RangeSeatScreenBuilder<KT>
      extends SeatScreenBuilder<KT, Range<Integer>, Integer> {

    private RangeSeatScreenBuilder(
        BindingReceiver<Map<KT, ? extends Range<Integer>>> current,
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

    protected BarFrame createFrame() {
      Shape tick = keyTemplate.winnerShape();
      BarFrameBuilder builder =
          BarFrameBuilder.dualWithShapes(
                  createResultAndWinner(),
                  party -> keyTemplate.toMainBarHeader(party),
                  party -> keyTemplate.toParty(party).getColor(),
                  result ->
                      ImmutablePair.of(result.result.getMinimum(), result.result.getMaximum()),
                  result -> result.result.getMinimum() + "-" + result.result.getMaximum(),
                  (party, result) -> result.winner ? tick : null,
                  (party, result) ->
                      party == Party.OTHERS
                          ? -1
                          : result.result.getMinimum() + result.result.getMaximum())
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
      BarFrameBuilder builder =
          BarFrameBuilder.dual(
                  diff.getBinding(),
                  party -> party.getAbbreviation().toUpperCase(),
                  party -> party.getColor(),
                  seats -> ImmutablePair.of(seats.diff.getMinimum(), seats.diff.getMaximum()),
                  seats ->
                      "("
                          + changeStr(seats.diff.getMinimum())
                          + ")-("
                          + changeStr(seats.diff.getMaximum())
                          + ")",
                  (party, seats) ->
                      party == Party.OTHERS
                          ? -1
                          : seats.curr.getMinimum() + seats.curr.getMaximum())
              .withHeader(changeHeader.getBinding())
              .withSubhead(changeSubhead.getBinding());
      if (total != null) {
        builder = builder.withWingspan(total.getBinding(t -> Math.max(1, t / 20)));
      }
      return builder.build();
    }

    @Override
    protected Range<Integer> createDefault() {
      return Range.is(0);
    }

    private static String changeStr(Integer seats) {
      return seats == 0 ? "\u00b10" : new DecimalFormat("+0;-0").format(seats);
    }
  }

  private interface VoteTemplate<CT, CPT> {
    String toBarString(CT votes, CPT pct);
  }

  private static class PctOnlyTemplate implements VoteTemplate<Integer, Double> {

    @Override
    public String toBarString(Integer votes, Double pct) {
      return votes == 0 ? "WAITING..." : PCT_FORMAT.format(pct);
    }
  }

  private static class VotePctTemplate implements VoteTemplate<Integer, Double> {

    @Override
    public String toBarString(Integer votes, Double pct) {
      return votes == 0
          ? "WAITING..."
          : (THOUSANDS_FORMAT.format(votes) + "\n" + PCT_FORMAT.format(pct));
    }
  }

  private static class VotePctOnlyTemplate implements VoteTemplate<Integer, Double> {

    @Override
    public String toBarString(Integer votes, Double pct) {
      return votes == 0 ? "WAITING..." : (PCT_FORMAT.format(pct));
    }
  }

  public abstract static class VoteScreenBuilder<KT, CT, CPT, PT> {
    protected final KeyTemplate<KT> keyTemplate;
    protected final VoteTemplate<CT, CPT> voteTemplate;

    protected BindingReceiver<Map<KT, ? extends CT>> current;
    protected BindingReceiver<String> header;
    protected BindingReceiver<String> subhead;
    protected BindingReceiver<Boolean> showMajority;
    protected BindingReceiver<String> majorityLabel;
    protected BindingReceiver<KT> winner;
    protected BindingReceiver<Double> pctReporting;
    protected BindingReceiver<String> notes;

    protected BindingReceiver<Map<Party, ? extends PT>> prev;
    protected BindingReceiver<String> changeHeader;

    protected BindingReceiver<String> swingHeader;
    protected Comparator<Party> swingComparator;

    protected MapBuilder mapBuilder;

    private VoteScreenBuilder(
        BindingReceiver<Map<KT, ? extends CT>> current,
        BindingReceiver<String> header,
        BindingReceiver<String> subhead,
        KeyTemplate<KT> keyTemplate,
        VoteTemplate<CT, CPT> voteTemplate) {
      this.keyTemplate = keyTemplate;
      this.current = current;
      this.header = header;
      this.subhead = subhead;
      this.voteTemplate = voteTemplate;
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withPrev(
        Binding<? extends Map<Party, ? extends PT>> prev, Binding<String> header) {
      this.prev = new BindingReceiver<>(prev);
      this.changeHeader = new BindingReceiver<>(header);
      return this;
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withWinner(Binding<KT> winner) {
      this.winner = new BindingReceiver<>(winner);
      return this;
    }

    public VoteScreenBuilder<KT, CT, CPT, PT> withPctReporting(Binding<Double> pctReporting) {
      this.pctReporting = new BindingReceiver<>(pctReporting);
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
              shapes, selectedShape, leadingParty.map(MapBuilder.Result::elected), focus, header);
      return this;
    }

    public <T> VoteScreenBuilder<KT, CT, CPT, PT> withResultMap(
        Binding<Map<T, Shape>> shapes,
        Binding<Map<T, Result>> winners,
        Binding<List<T>> focus,
        Binding<String> headerBinding) {
      this.mapBuilder = new MapBuilder(shapes, winners, focus, headerBinding);
      return this;
    }

    public <T> VoteScreenBuilder<KT, CT, CPT, PT> withResultMap(
        Binding<Map<T, Shape>> shapes,
        Binding<T> selectedShape,
        Binding<Result> leadingParty,
        Binding<List<T>> focus,
        Binding<String> header) {
      this.mapBuilder = new MapBuilder(shapes, selectedShape, leadingParty, focus, header);
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

    public BasicResultPanel build(Binding<String> textHeader) {
      return new BasicResultPanel(
          createHeaderLabel(textHeader),
          createFrame(),
          createDiffFrame(),
          createSwingFrame(),
          createMapFrame());
    }

    protected abstract BarFrame createFrame();

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
        BindingReceiver<Map<KT, ? extends Integer>> current,
        BindingReceiver<String> header,
        BindingReceiver<String> subhead,
        KeyTemplate<KT> keyTemplate,
        VoteTemplate<Integer, Double> voteTemplate) {
      super(current, header, subhead, keyTemplate, voteTemplate);
    }

    @Override
    protected BarFrame createFrame() {
      Binding<Map<KT, VotesPct<Integer, Double>>> votesPct =
          current.getBinding(
              map -> {
                int total = map.values().stream().mapToInt(i -> i).sum();
                Map<KT, VotesPct<Integer, Double>> ret = new LinkedHashMap<>();
                map.forEach(
                    (k, v) -> ret.put(k, new VotesPct<>(v, total == 0 ? 0 : 1.0 * v / total)));
                return ret;
              });
      Binding<KT> winner = this.winner == null ? (() -> null) : this.winner.getBinding();
      BarFrameBuilder builder =
          BarFrameBuilder.basicWithShapes(
                  votesPct.merge(
                      winner,
                      (v, w) -> {
                        if (w != null) {
                          v.computeIfAbsent(w, x -> new VotesPct<>(0, 0.0)).winner = true;
                        }
                        return v;
                      }),
                  keyTemplate::toMainBarHeader,
                  key -> keyTemplate.toParty(key).getColor(),
                  votes -> votes.percent,
                  votes -> voteTemplate.toBarString(votes.votes, votes.percent),
                  (party, votes) -> votes.winner ? keyTemplate.winnerShape() : null,
                  (party, votes) -> party == Party.OTHERS ? -1 : votes.percent)
              .withHeader(header.getBinding())
              .withSubhead(subhead.getBinding())
              .withNotes(notes == null ? (() -> null) : notes.getBinding())
              .withMax(
                  pctReporting == null
                      ? (() -> 2.0 / 3)
                      : pctReporting.getBinding(x -> 2.0 / 3 / x));
      if (showMajority != null) {
        BindableList<Double> lines = new BindableList<>();
        showMajority
            .getBinding()
            .bind(
                show -> {
                  lines.clear();
                  if (show) {
                    lines.add(pctReporting == null ? 0.5 : 0.5 / pctReporting.getValue());
                  }
                });
        if (pctReporting != null) {
          pctReporting
              .getBinding()
              .bind(
                  pct -> {
                    if (!lines.isEmpty()) {
                      lines.set(0, 0.5 / pct);
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

    @Override
    protected BarFrame createDiffFrame() {
      if (prev == null) {
        return null;
      }
      Binding<? extends Map<Party, CurrDiff<Double>>> currDiff =
          current
              .getBinding()
              .merge(
                  prev.getBinding(),
                  (c, p) -> {
                    int currTotal = c.values().stream().mapToInt(i -> i).sum();
                    int prevTotal = p.values().stream().mapToInt(i -> i).sum();
                    Map<Party, Integer> partyTotal = currTotalByParty(c);
                    Map<Party, CurrDiff<Double>> ret = new LinkedHashMap<>();
                    if (currTotal == 0 || prevTotal == 0) {
                      return ret;
                    }
                    partyTotal.forEach(
                        (party, cv) -> {
                          double cpct = 1.0 * cv / currTotal;
                          double ppct = 1.0 * (p.containsKey(party) ? p.get(party) : 0) / prevTotal;
                          ret.put(party, new CurrDiff<>(cpct, cpct - ppct));
                        });
                    p.forEach(
                        (party, pv) -> {
                          double ppct = 1.0 * pv / prevTotal;
                          ret.putIfAbsent(party, new CurrDiff<>(0.0, -ppct));
                        });
                    return ret;
                  });
      return BarFrameBuilder.basic(
              currDiff,
              party -> party.getAbbreviation().toUpperCase(),
              party -> party.getColor(),
              value -> value.diff,
              value -> new DecimalFormat("+0.0%;-0.0%").format(value.diff),
              (party, value) -> party == Party.OTHERS ? -1 : value.curr)
          .withWingspan(pctReporting == null ? (() -> 0.1) : pctReporting.getBinding(x -> 0.1 / x))
          .withHeader(changeHeader.getBinding())
          .build();
    }

    @Override
    protected SwingFrame createSwingFrame() {
      if (swingHeader == null) {
        return null;
      }
      Binding<Map<Party, Integer>> curr = current.getBinding(this::currTotalByParty);
      return SwingFrameBuilder.prevCurr(prev.getBinding(), curr, swingComparator)
          .withHeader(swingHeader.getBinding())
          .build();
    }

    protected Map<Party, Integer> currTotalByParty(Map<KT, ? extends Integer> curr) {
      Map<Party, Integer> ret = new LinkedHashMap<>();
      curr.forEach((k, v) -> ret.merge(keyTemplate.toParty(k), v, Integer::sum));
      return ret;
    }
  }

  private static class VotesPct<T, PT> {
    private final T votes;
    private final PT percent;
    private boolean winner;

    private VotesPct(T votes, PT percent) {
      this.votes = votes;
      this.percent = percent;
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
