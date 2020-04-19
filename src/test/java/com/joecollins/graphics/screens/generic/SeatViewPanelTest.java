package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.graphics.components.MapFrameTest;
import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.graphics.utils.ShapefileReader;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.awt.Shape;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class SeatViewPanelTest {

  @Test
  public void testBasicCurrPrev() throws IOException {
    BindableWrapper<LinkedHashMap<Party, Integer>> currentSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> previousSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Integer> totalSeats = new BindableWrapper<>(650);
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<String> header = new BindableWrapper<>("UNITED KINGDOM");
    BindableWrapper<String> seatHeader = new BindableWrapper<>("0 OF 650 CONSTITUENCIES DECLARED");
    BindableWrapper<String> seatSubhead = new BindableWrapper<>("PROJECTION: TOO EARLY TO CALL");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2017");
    Party con = new Party("Conservative", "CON", Color.BLUE);
    Party lab = new Party("Labour", "LAB", Color.RED);

    BasicResultPanel panel =
        BasicResultPanel.partySeats(
                currentSeats.getBinding(), seatHeader.getBinding(), seatSubhead.getBinding())
            .withTotal(totalSeats.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " SEATS FOR MAJORITY")
            .withPrev(previousSeats.getBinding(), changeHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SeatViewPanel", "Basic-1", panel);

    LinkedHashMap<Party, Integer> curr = new LinkedHashMap<>();
    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();

    curr.put(con, 1);
    currentSeats.setValue(curr);

    prev.put(lab, 1);
    previousSeats.setValue(prev);
    seatHeader.setValue("1 OF 650 SEATS DECLARED");
    compareRendering("SeatViewPanel", "Basic-2", panel);

    curr.put(lab, 2);
    currentSeats.setValue(curr);

    prev.put(lab, 3);
    previousSeats.setValue(prev);
    seatHeader.setValue("3 OF 650 SEATS DECLARED");
    compareRendering("SeatViewPanel", "Basic-3", panel);

    Party ld = new Party("Liberal Democrat", "LD", Color.ORANGE);
    Party snp = new Party("Scottish National Party", "SNP", Color.YELLOW);
    Party pc = new Party("Plaid Cymru", "PC", Color.GREEN.darker());
    Party grn = new Party("Green", "GRN", Color.GREEN);
    Party oth = Party.OTHERS;

    curr.put(con, 365);
    curr.put(lab, 202);
    curr.put(ld, 11);
    curr.put(snp, 48);
    curr.put(grn, 1);
    curr.put(pc, 4);
    curr.put(oth, 19);

    prev.put(con, 317);
    prev.put(lab, 262);
    prev.put(ld, 12);
    prev.put(snp, 35);
    prev.put(grn, 1);
    prev.put(pc, 4);
    prev.put(oth, 19);

    currentSeats.setValue(curr);
    previousSeats.setValue(prev);
    seatHeader.setValue("650 OF 650 SEATS DECLARED");
    seatSubhead.setValue("PROJECTION: CON MAJORITY");
    compareRendering("SeatViewPanel", "Basic-4", panel);

    header.setValue("SCOTLAND");
    seatHeader.setValue("59 OF 59 SEATS DECLARED");
    seatSubhead.setValue("");
    totalSeats.setValue(59);
    showMajority.setValue(false);
    curr.clear();
    prev.clear();

    curr.put(snp, 48);
    curr.put(con, 6);
    curr.put(ld, 4);
    curr.put(lab, 1);
    currentSeats.setValue(curr);

    prev.put(snp, 35);
    prev.put(con, 13);
    prev.put(ld, 4);
    prev.put(lab, 7);
    previousSeats.setValue(prev);
    compareRendering("SeatViewPanel", "Basic-5", panel);
  }

  @Test
  public void testBasicCurrDiff() throws IOException {
    BindableWrapper<LinkedHashMap<Party, Integer>> currentSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> seatDiff =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Integer> totalSeats = new BindableWrapper<>(650);
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<String> header = new BindableWrapper<>("UNITED KINGDOM");
    BindableWrapper<String> seatHeader = new BindableWrapper<>("0 OF 650 CONSTITUENCIES DECLARED");
    BindableWrapper<String> seatSubhead = new BindableWrapper<>("PROJECTION: TOO EARLY TO CALL");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2017");
    Party con = new Party("Conservative", "CON", Color.BLUE);
    Party lab = new Party("Labour", "LAB", Color.RED);

    BasicResultPanel panel =
        BasicResultPanel.partySeats(
                currentSeats.getBinding(), seatHeader.getBinding(), seatSubhead.getBinding())
            .withTotal(totalSeats.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " SEATS FOR MAJORITY")
            .withDiff(seatDiff.getBinding(), changeHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SeatViewPanel", "Basic-1", panel);

    LinkedHashMap<Party, Integer> curr = new LinkedHashMap<>();
    LinkedHashMap<Party, Integer> diff = new LinkedHashMap<>();

    curr.put(con, 1);
    currentSeats.setValue(curr);

    diff.put(con, +1);
    diff.put(lab, -1);
    seatDiff.setValue(diff);
    seatHeader.setValue("1 OF 650 SEATS DECLARED");
    compareRendering("SeatViewPanel", "Basic-2", panel);

    curr.put(lab, 2);
    currentSeats.setValue(curr);

    seatHeader.setValue("3 OF 650 SEATS DECLARED");
    compareRendering("SeatViewPanel", "Basic-3", panel);

    Party ld = new Party("Liberal Democrat", "LD", Color.ORANGE);
    Party snp = new Party("Scottish National Party", "SNP", Color.YELLOW);
    Party pc = new Party("Plaid Cymru", "PC", Color.GREEN.darker());
    Party grn = new Party("Green", "GRN", Color.GREEN);
    Party oth = Party.OTHERS;

    curr.put(con, 365);
    curr.put(lab, 202);
    curr.put(ld, 11);
    curr.put(snp, 48);
    curr.put(grn, 1);
    curr.put(pc, 4);
    curr.put(oth, 19);

    diff.put(con, +48);
    diff.put(lab, -60);
    diff.put(ld, -1);
    diff.put(snp, +13);
    diff.put(grn, 0);
    diff.put(pc, 0);
    diff.put(oth, 0);

    currentSeats.setValue(curr);
    seatDiff.setValue(diff);
    seatHeader.setValue("650 OF 650 SEATS DECLARED");
    seatSubhead.setValue("PROJECTION: CON MAJORITY");
    compareRendering("SeatViewPanel", "Basic-4", panel);

    header.setValue("SCOTLAND");
    seatHeader.setValue("59 OF 59 SEATS DECLARED");
    seatSubhead.setValue("");
    totalSeats.setValue(59);
    showMajority.setValue(false);
    curr.clear();
    diff.clear();

    curr.put(snp, 48);
    curr.put(con, 6);
    curr.put(ld, 4);
    curr.put(lab, 1);
    currentSeats.setValue(curr);

    diff.put(snp, +13);
    diff.put(con, -7);
    diff.put(ld, 0);
    diff.put(lab, -6);
    seatDiff.setValue(diff);
    compareRendering("SeatViewPanel", "Basic-5", panel);
  }

  @Test
  public void testSwing() throws IOException {
    BindableWrapper<LinkedHashMap<Party, Integer>> currentSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> previousSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> currentVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Integer> totalSeats = new BindableWrapper<>(650);
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<String> header = new BindableWrapper<>("UNITED KINGDOM");
    BindableWrapper<String> seatHeader = new BindableWrapper<>("0 OF 650 CONSTITUENCIES DECLARED");
    BindableWrapper<String> seatSubhead = new BindableWrapper<>("PROJECTION: TOO EARLY TO CALL");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2017");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2017");

    Party con = new Party("Conservative", "CON", Color.BLUE);
    Party lab = new Party("Labour", "LAB", Color.RED);
    Party ld = new Party("Liberal Democrat", "LD", Color.ORANGE);
    Party snp = new Party("Scottish National Party", "SNP", Color.YELLOW);
    Party pc = new Party("Plaid Cymru", "PC", Color.GREEN.darker());
    Party grn = new Party("Green", "GRN", Color.GREEN);
    Party oth = Party.OTHERS;
    List<Party> partyOrder = List.of(snp, lab, pc, grn, ld, oth, con);

    BasicResultPanel panel =
        BasicResultPanel.partySeats(
                currentSeats.getBinding(), seatHeader.getBinding(), seatSubhead.getBinding())
            .withPrev(previousSeats.getBinding(), changeHeader.getBinding())
            .withTotal(totalSeats.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " SEATS FOR MAJORITY")
            .withSwing(
                currentVotes.getBinding(),
                previousVotes.getBinding(),
                Comparator.comparingInt(partyOrder::indexOf),
                swingHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SeatViewPanel", "Swing-1", panel);

    LinkedHashMap<Party, Integer> currSeats = new LinkedHashMap<>();
    LinkedHashMap<Party, Integer> prevSeats = new LinkedHashMap<>();
    LinkedHashMap<Party, Integer> currVotes = new LinkedHashMap<>();
    LinkedHashMap<Party, Integer> prevVotes = new LinkedHashMap<>();

    currSeats.put(lab, 1);
    currentSeats.setValue(currSeats);

    prevSeats.put(lab, 1);
    previousSeats.setValue(prevSeats);

    currVotes.put(lab, 21568);
    currVotes.put(con, 9290);
    currVotes.put(ld, 2709);
    currVotes.put(grn, 1365);
    currVotes.put(oth, 2542);
    currentVotes.setValue(currVotes);

    prevVotes.put(lab, 24071);
    prevVotes.put(con, 9134);
    prevVotes.put(ld, 1812);
    prevVotes.put(grn, 595);
    prevVotes.put(oth, 1482);
    previousVotes.setValue(prevVotes);

    seatHeader.setValue("1 OF 650 SEATS DECLARED");
    compareRendering("SeatViewPanel", "Swing-2", panel);
  }

  @Test
  public void testMap() throws IOException {
    BindableWrapper<LinkedHashMap<Party, Integer>> currentSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> previousSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> currentVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Integer> totalSeats = new BindableWrapper<>(27);
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<String> header = new BindableWrapper<>("PRINCE EDWARD ISLAND");
    BindableWrapper<String> seatHeader = new BindableWrapper<>("0 OF 27 DISTRICTS DECLARED");
    BindableWrapper<String> seatSubhead = new BindableWrapper<>("PROJECTION: TOO EARLY TO CALL");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2015");
    BindableWrapper<String> mapHeader = new BindableWrapper<>("PEI");
    BindableWrapper<Map<Integer, Party>> winnersByDistrict = new BindableWrapper<>(new HashMap<>());
    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    BindableWrapper<List<Shape>> focus = new BindableWrapper<>();

    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    Party oth = Party.OTHERS;
    List<Party> partyOrder = List.of(ndp, grn, lib, oth, pc);

    BasicResultPanel panel =
        BasicResultPanel.partySeats(
                currentSeats.getBinding(), seatHeader.getBinding(), seatSubhead.getBinding())
            .withPrev(previousSeats.getBinding(), changeHeader.getBinding())
            .withTotal(totalSeats.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " SEATS FOR MAJORITY")
            .withSwing(
                currentVotes.getBinding(),
                previousVotes.getBinding(),
                Comparator.comparing(partyOrder::indexOf),
                swingHeader.getBinding())
            .withPartyMap(
                () -> shapesByDistrict,
                winnersByDistrict.getBinding(),
                focus.getBinding(),
                mapHeader.getBinding())
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SeatViewPanel", "Map-1", panel);

    LinkedHashMap<Party, Integer> currSeats = new LinkedHashMap<>();
    LinkedHashMap<Party, Integer> prevSeats = new LinkedHashMap<>();
    LinkedHashMap<Party, Integer> currVotes = new LinkedHashMap<>();
    LinkedHashMap<Party, Integer> prevVotes = new LinkedHashMap<>();
    LinkedHashMap<Integer, Party> winners = new LinkedHashMap<>();

    currSeats.put(pc, 1);
    currentSeats.setValue(currSeats);

    prevSeats.put(pc, 1);
    previousSeats.setValue(prevSeats);

    currVotes.put(pc, 1347);
    currVotes.put(lib, 861);
    currVotes.put(grn, 804);
    currentVotes.setValue(currVotes);

    prevVotes.put(pc, 1179);
    prevVotes.put(lib, 951);
    prevVotes.put(ndp, 528);
    previousVotes.setValue(prevVotes);

    winners.put(1, pc);
    winnersByDistrict.setValue(winners);

    seatHeader.setValue("1 OF 27 DISTRICTS DECLARED");
    compareRendering("SeatViewPanel", "Map-2", panel);

    focus.setValue(
        shapesByDistrict.entrySet().stream()
            .filter(e -> e.getKey() <= 7)
            .map(Map.Entry::getValue)
            .collect(Collectors.toList()));
    header.setValue("CARDIGAN");
    seatHeader.setValue("1 OF 7 DISTRICTS DECLARED");
    seatSubhead.setValue("");
    totalSeats.setValue(7);
    showMajority.setValue(false);
    mapHeader.setValue("CARDIGAN");
    compareRendering("SeatViewPanel", "Map-3", panel);

    currSeats.put(pc, 2);
    currentSeats.setValue(currSeats);

    prevSeats.put(lib, 1);
    previousSeats.setValue(prevSeats);

    currVotes.put(pc, 2720);
    currVotes.put(lib, 1646);
    currVotes.put(grn, 1478);
    currVotes.put(ndp, 124);
    currentVotes.setValue(currVotes);

    prevVotes.put(pc, 1964);
    prevVotes.put(lib, 2011);
    prevVotes.put(ndp, 1113);
    prevVotes.put(grn, 106);
    previousVotes.setValue(prevVotes);

    winners.put(3, pc);
    winnersByDistrict.setValue(winners);

    seatHeader.setValue("2 OF 7 DISTRICTS DECLARED");
    compareRendering("SeatViewPanel", "Map-4", panel);

    focus.setValue(null);
    header.setValue("PRINCE EDWARD ISLAND");
    seatHeader.setValue("2 OF 27 DISTRICTS DECLARED");
    seatSubhead.setValue("PROJECTION: TOO EARLY TO CALL");
    totalSeats.setValue(27);
    showMajority.setValue(true);
    mapHeader.setValue("PEI");
    compareRendering("SeatViewPanel", "Map-5", panel);
  }

  @Test
  public void testDualCurrPrev() throws IOException {
    BindableWrapper<LinkedHashMap<Party, Pair<Integer, Integer>>> currentSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Pair<Integer, Integer>>> previousSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Integer> totalSeats = new BindableWrapper<>(338);
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<String> header = new BindableWrapper<>("CANADA");
    BindableWrapper<String> seatHeader = new BindableWrapper<>("0 OF 338 RIDINGS REPORTING");
    BindableWrapper<String> seatSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");

    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party con = new Party("Conservative", "CON", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    Party bq = new Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker());
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party ind = new Party("Independent", "IND", Color.GRAY);

    BasicResultPanel panel =
        BasicResultPanel.partyDualSeats(
                currentSeats.getBinding(), seatHeader.getBinding(), seatSubhead.getBinding())
            .withPrev(previousSeats.getBinding(), changeHeader.getBinding())
            .withTotal(totalSeats.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " SEATS FOR MAJORITY")
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SeatViewPanel", "Dual-1", panel);

    LinkedHashMap<Party, Pair<Integer, Integer>> currSeats = new LinkedHashMap<>();
    LinkedHashMap<Party, Pair<Integer, Integer>> prevSeats = new LinkedHashMap<>();

    currSeats.put(lib, ImmutablePair.of(0, 6));
    currSeats.put(ndp, ImmutablePair.of(0, 1));
    currentSeats.setValue(currSeats);

    prevSeats.put(lib, ImmutablePair.of(0, 7));
    previousSeats.setValue(prevSeats);

    seatHeader.setValue("7 OF 338 RIDINGS REPORTING");
    compareRendering("SeatViewPanel", "Dual-2", panel);

    currSeats.put(lib, ImmutablePair.of(6, 26));
    currSeats.put(ndp, ImmutablePair.of(1, 1));
    currSeats.put(con, ImmutablePair.of(0, 4));
    currSeats.put(grn, ImmutablePair.of(0, 1));
    currentSeats.setValue(currSeats);

    prevSeats.put(lib, ImmutablePair.of(7, 32));
    previousSeats.setValue(prevSeats);

    seatHeader.setValue("32 OF 338 RIDINGS REPORTING");
    compareRendering("SeatViewPanel", "Dual-3", panel);

    currSeats.put(lib, ImmutablePair.of(26, 145));
    currSeats.put(ndp, ImmutablePair.of(1, 13));
    currSeats.put(con, ImmutablePair.of(4, 104));
    currSeats.put(bq, ImmutablePair.of(0, 32));
    currSeats.put(grn, ImmutablePair.of(1, 1));
    currentSeats.setValue(currSeats);

    prevSeats.put(lib, ImmutablePair.of(32, 166));
    prevSeats.put(ndp, ImmutablePair.of(0, 30));
    prevSeats.put(con, ImmutablePair.of(0, 89));
    prevSeats.put(bq, ImmutablePair.of(0, 10));
    previousSeats.setValue(prevSeats);

    seatHeader.setValue("295 OF 338 RIDINGS REPORTING");
    compareRendering("SeatViewPanel", "Dual-4", panel);

    currSeats.put(lib, ImmutablePair.of(145, 157));
    currSeats.put(ndp, ImmutablePair.of(13, 24));
    currSeats.put(con, ImmutablePair.of(104, 121));
    currSeats.put(bq, ImmutablePair.of(32, 32));
    currSeats.put(grn, ImmutablePair.of(1, 3));
    currSeats.put(ind, ImmutablePair.of(0, 1));
    currentSeats.setValue(currSeats);

    prevSeats.put(lib, ImmutablePair.of(166, 184));
    prevSeats.put(ndp, ImmutablePair.of(30, 44));
    prevSeats.put(con, ImmutablePair.of(89, 99));
    prevSeats.put(bq, ImmutablePair.of(10, 10));
    prevSeats.put(grn, ImmutablePair.of(0, 1));
    previousSeats.setValue(prevSeats);

    seatHeader.setValue("338 OF 338 RIDINGS REPORTING");
    compareRendering("SeatViewPanel", "Dual-5", panel);

    currSeats.put(lib, ImmutablePair.of(157, 157));
    currSeats.put(ndp, ImmutablePair.of(24, 24));
    currSeats.put(con, ImmutablePair.of(121, 121));
    currSeats.put(bq, ImmutablePair.of(32, 32));
    currSeats.put(grn, ImmutablePair.of(3, 3));
    currSeats.put(ind, ImmutablePair.of(1, 1));
    currentSeats.setValue(currSeats);

    prevSeats.put(lib, ImmutablePair.of(184, 184));
    prevSeats.put(ndp, ImmutablePair.of(44, 44));
    prevSeats.put(con, ImmutablePair.of(99, 99));
    prevSeats.put(bq, ImmutablePair.of(10, 10));
    prevSeats.put(grn, ImmutablePair.of(1, 1));
    previousSeats.setValue(prevSeats);

    seatHeader.setValue("338 OF 338 RIDINGS REPORTING");
    compareRendering("SeatViewPanel", "Dual-6", panel);
  }

  @Test
  public void testDualCurrDiff() throws IOException {
    BindableWrapper<LinkedHashMap<Party, Pair<Integer, Integer>>> currentSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Pair<Integer, Integer>>> seatDiff =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Integer> totalSeats = new BindableWrapper<>(338);
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<String> header = new BindableWrapper<>("CANADA");
    BindableWrapper<String> seatHeader = new BindableWrapper<>("0 OF 338 RIDINGS REPORTING");
    BindableWrapper<String> seatSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");

    Party lib = new Party("Liberal", "LIB", Color.RED);
    Party con = new Party("Conservative", "CON", Color.BLUE);
    Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
    Party bq = new Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker());
    Party grn = new Party("Green", "GRN", Color.GREEN.darker());
    Party ind = new Party("Independent", "IND", Color.GRAY);

    BasicResultPanel panel =
        BasicResultPanel.partyDualSeats(
                currentSeats.getBinding(), seatHeader.getBinding(), seatSubhead.getBinding())
            .withDiff(seatDiff.getBinding(), changeHeader.getBinding())
            .withTotal(totalSeats.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " SEATS FOR MAJORITY")
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SeatViewPanel", "Dual-1", panel);

    LinkedHashMap<Party, Pair<Integer, Integer>> currSeats = new LinkedHashMap<>();
    LinkedHashMap<Party, Pair<Integer, Integer>> diff = new LinkedHashMap<>();

    currSeats.put(lib, ImmutablePair.of(0, 6));
    currSeats.put(ndp, ImmutablePair.of(0, 1));
    currentSeats.setValue(currSeats);

    diff.put(lib, ImmutablePair.of(0, -1));
    diff.put(ndp, ImmutablePair.of(0, +1));
    seatDiff.setValue(diff);

    seatHeader.setValue("7 OF 338 RIDINGS REPORTING");
    compareRendering("SeatViewPanel", "Dual-2", panel);

    currSeats.put(lib, ImmutablePair.of(6, 26));
    currSeats.put(ndp, ImmutablePair.of(1, 1));
    currSeats.put(con, ImmutablePair.of(0, 4));
    currSeats.put(grn, ImmutablePair.of(0, 1));
    currentSeats.setValue(currSeats);

    diff.put(lib, ImmutablePair.of(-1, -6));
    diff.put(ndp, ImmutablePair.of(+1, +1));
    diff.put(con, ImmutablePair.of(0, +4));
    diff.put(grn, ImmutablePair.of(0, +1));
    seatDiff.setValue(diff);

    seatHeader.setValue("32 OF 338 RIDINGS REPORTING");
    compareRendering("SeatViewPanel", "Dual-3", panel);

    currSeats.put(lib, ImmutablePair.of(26, 145));
    currSeats.put(ndp, ImmutablePair.of(1, 13));
    currSeats.put(con, ImmutablePair.of(4, 104));
    currSeats.put(bq, ImmutablePair.of(0, 32));
    currSeats.put(grn, ImmutablePair.of(1, 1));
    currentSeats.setValue(currSeats);

    diff.put(lib, ImmutablePair.of(-6, -21));
    diff.put(ndp, ImmutablePair.of(+1, -17));
    diff.put(con, ImmutablePair.of(+4, +15));
    diff.put(bq, ImmutablePair.of(0, +22));
    diff.put(grn, ImmutablePair.of(+1, +1));
    seatDiff.setValue(diff);

    seatHeader.setValue("295 OF 338 RIDINGS REPORTING");
    compareRendering("SeatViewPanel", "Dual-4", panel);

    currSeats.put(lib, ImmutablePair.of(145, 157));
    currSeats.put(ndp, ImmutablePair.of(13, 24));
    currSeats.put(con, ImmutablePair.of(104, 121));
    currSeats.put(bq, ImmutablePair.of(32, 32));
    currSeats.put(grn, ImmutablePair.of(1, 3));
    currSeats.put(ind, ImmutablePair.of(0, 1));
    currentSeats.setValue(currSeats);

    diff.put(lib, ImmutablePair.of(-21, -27));
    diff.put(ndp, ImmutablePair.of(-17, -20));
    diff.put(con, ImmutablePair.of(+15, +22));
    diff.put(bq, ImmutablePair.of(+22, +22));
    diff.put(grn, ImmutablePair.of(+1, +2));
    diff.put(ind, ImmutablePair.of(0, +1));
    seatDiff.setValue(diff);

    seatHeader.setValue("338 OF 338 RIDINGS REPORTING");
    compareRendering("SeatViewPanel", "Dual-5", panel);

    currSeats.put(lib, ImmutablePair.of(157, 157));
    currSeats.put(ndp, ImmutablePair.of(24, 24));
    currSeats.put(con, ImmutablePair.of(121, 121));
    currSeats.put(bq, ImmutablePair.of(32, 32));
    currSeats.put(grn, ImmutablePair.of(3, 3));
    currSeats.put(ind, ImmutablePair.of(1, 1));
    currentSeats.setValue(currSeats);

    diff.put(lib, ImmutablePair.of(-27, -27));
    diff.put(ndp, ImmutablePair.of(-20, -20));
    diff.put(con, ImmutablePair.of(+22, +22));
    diff.put(bq, ImmutablePair.of(+22, +22));
    diff.put(grn, ImmutablePair.of(+2, +2));
    diff.put(ind, ImmutablePair.of(+1, +1));
    seatDiff.setValue(diff);

    seatHeader.setValue("338 OF 338 RIDINGS REPORTING");
    compareRendering("SeatViewPanel", "Dual-6", panel);
  }

  @Test
  public void testRangeCurrPrev() throws IOException {
    BindableWrapper<LinkedHashMap<Party, Range<Integer>>> currentSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> previousSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Integer> totalSeats = new BindableWrapper<>(76);
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<String> header = new BindableWrapper<>("AUSTRALIA");
    BindableWrapper<String> seatHeader = new BindableWrapper<>("SENATE SEATS");
    BindableWrapper<String> seatSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2013");

    Party lnp = new Party("Liberal/National Coalition", "L/NP", Color.BLUE);
    Party alp = new Party("Labor Party", "ALP", Color.RED);
    Party grn = new Party("The Greens", "GRN", Color.GREEN.darker());
    Party onp = new Party("One Nation Party", "ONP", Color.ORANGE);
    Party nxt = new Party("Nick Xenophon Team", "NXT", Color.ORANGE);
    Party oth = Party.OTHERS;

    BasicResultPanel panel =
        BasicResultPanel.partyRangeSeats(
                currentSeats.getBinding(), seatHeader.getBinding(), seatSubhead.getBinding())
            .withPrev(previousSeats.getBinding(), changeHeader.getBinding())
            .withTotal(totalSeats.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " SEATS FOR MAJORITY")
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SeatViewPanel", "Range-1", panel);

    LinkedHashMap<Party, Range<Integer>> currSeats = new LinkedHashMap<>();
    LinkedHashMap<Party, Integer> prevSeats = new LinkedHashMap<>();

    currSeats.put(lnp, Range.between(4, 5));
    currSeats.put(alp, Range.between(4, 4));
    currSeats.put(grn, Range.between(0, 1));
    currSeats.put(onp, Range.between(0, 1));
    currSeats.put(oth, Range.between(0, 2));
    currentSeats.setValue(currSeats);

    prevSeats.put(lnp, 6);
    prevSeats.put(alp, 4);
    prevSeats.put(grn, 1);
    prevSeats.put(oth, 1);
    previousSeats.setValue(prevSeats);

    compareRendering("SeatViewPanel", "Range-2", panel);

    currSeats.put(lnp, Range.between(8, 10));
    currSeats.put(alp, Range.between(7, 8));
    currSeats.put(grn, Range.between(0, 2));
    currSeats.put(onp, Range.between(1, 2));
    currSeats.put(nxt, Range.between(0, 1));
    currSeats.put(oth, Range.between(0, 4));
    currentSeats.setValue(currSeats);

    prevSeats.put(lnp, 12);
    prevSeats.put(alp, 8);
    prevSeats.put(grn, 2);
    prevSeats.put(oth, 2);
    previousSeats.setValue(prevSeats);

    compareRendering("SeatViewPanel", "Range-3", panel);

    currSeats.put(lnp, Range.between(27, 31));
    currSeats.put(alp, Range.between(25, 27));
    currSeats.put(grn, Range.between(5, 9));
    currSeats.put(onp, Range.between(1, 4));
    currSeats.put(nxt, Range.between(3, 3));
    currSeats.put(oth, Range.between(1, 8));
    currentSeats.setValue(currSeats);

    prevSeats.put(lnp, 33);
    prevSeats.put(alp, 25);
    prevSeats.put(grn, 10);
    prevSeats.put(nxt, 1);
    prevSeats.put(oth, 7);
    previousSeats.setValue(prevSeats);

    compareRendering("SeatViewPanel", "Range-4", panel);
  }

  @Test
  public void testRangeCurrDiff() throws IOException {
    BindableWrapper<LinkedHashMap<Party, Range<Integer>>> currentSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Range<Integer>>> seatDiff =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Integer> totalSeats = new BindableWrapper<>(76);
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<String> header = new BindableWrapper<>("AUSTRALIA");
    BindableWrapper<String> seatHeader = new BindableWrapper<>("SENATE SEATS");
    BindableWrapper<String> seatSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2013");

    Party lnp = new Party("Liberal/National Coalition", "L/NP", Color.BLUE);
    Party alp = new Party("Labor Party", "ALP", Color.RED);
    Party grn = new Party("The Greens", "GRN", Color.GREEN.darker());
    Party onp = new Party("One Nation Party", "ONP", Color.ORANGE);
    Party nxt = new Party("Nick Xenophon Team", "NXT", Color.ORANGE);
    Party oth = Party.OTHERS;

    BasicResultPanel panel =
        BasicResultPanel.partyRangeSeats(
                currentSeats.getBinding(), seatHeader.getBinding(), seatSubhead.getBinding())
            .withDiff(seatDiff.getBinding(), changeHeader.getBinding())
            .withTotal(totalSeats.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " SEATS FOR MAJORITY")
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SeatViewPanel", "Range-1", panel);

    LinkedHashMap<Party, Range<Integer>> currSeats = new LinkedHashMap<>();
    LinkedHashMap<Party, Range<Integer>> diff = new LinkedHashMap<>();

    currSeats.put(lnp, Range.between(4, 5));
    currSeats.put(alp, Range.between(4, 4));
    currSeats.put(grn, Range.between(0, 1));
    currSeats.put(onp, Range.between(0, 1));
    currSeats.put(oth, Range.between(0, 2));
    currentSeats.setValue(currSeats);

    diff.put(lnp, Range.between(-2, -1));
    diff.put(alp, Range.between(0, 0));
    diff.put(grn, Range.between(-1, 0));
    diff.put(onp, Range.between(0, +1));
    diff.put(oth, Range.between(-1, +1));
    seatDiff.setValue(diff);

    compareRendering("SeatViewPanel", "Range-2", panel);

    currSeats.put(lnp, Range.between(8, 10));
    currSeats.put(alp, Range.between(7, 8));
    currSeats.put(grn, Range.between(0, 2));
    currSeats.put(onp, Range.between(1, 2));
    currSeats.put(nxt, Range.between(0, 1));
    currSeats.put(oth, Range.between(0, 4));
    currentSeats.setValue(currSeats);

    diff.put(lnp, Range.between(-4, -2));
    diff.put(alp, Range.between(-1, 0));
    diff.put(grn, Range.between(-2, 0));
    diff.put(onp, Range.between(+1, +2));
    diff.put(nxt, Range.between(0, +1));
    diff.put(oth, Range.between(-2, +2));
    seatDiff.setValue(diff);

    compareRendering("SeatViewPanel", "Range-3", panel);

    currSeats.put(lnp, Range.between(27, 31));
    currSeats.put(alp, Range.between(25, 27));
    currSeats.put(grn, Range.between(5, 9));
    currSeats.put(onp, Range.between(1, 4));
    currSeats.put(nxt, Range.between(3, 3));
    currSeats.put(oth, Range.between(1, 8));
    currentSeats.setValue(currSeats);

    diff.put(lnp, Range.between(-6, -2));
    diff.put(alp, Range.between(0, +2));
    diff.put(grn, Range.between(-5, -1));
    diff.put(onp, Range.between(+1, +4));
    diff.put(nxt, Range.between(+2, +2));
    diff.put(oth, Range.between(-6, +1));
    seatDiff.setValue(diff);

    compareRendering("SeatViewPanel", "Range-4", panel);
  }

  @Test
  public void testCandidates() throws IOException {
    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> previousSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Integer> totalSeats = new BindableWrapper<>(538);
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<String> header = new BindableWrapper<>("UNITED STATES");
    BindableWrapper<String> seatHeader = new BindableWrapper<>("PRESIDENT");
    BindableWrapper<String> seatSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2012");
    BindableWrapper<Candidate> winner = new BindableWrapper<>();

    Candidate clinton = new Candidate("Hillary Clinton", new Party("Democrat", "DEM", Color.BLUE));
    Candidate trump = new Candidate("Donald Trump", new Party("Republican", "GOP", Color.RED));

    BasicResultPanel panel =
        BasicResultPanel.candidateSeats(
                currentSeats.getBinding(), seatHeader.getBinding(), seatSubhead.getBinding())
            .withWinner(winner.getBinding())
            .withPrev(previousSeats.getBinding(), changeHeader.getBinding())
            .withTotal(totalSeats.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " ELECTORAL VOTES TO WIN")
            .build(header.getBinding());
    panel.setSize(1024, 512);

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(clinton, 232);
    curr.put(trump, 306);
    currentSeats.setValue(curr);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(clinton.getParty(), 332);
    prev.put(trump.getParty(), 206);
    previousSeats.setValue(prev);

    winner.setValue(trump);
    compareRendering("SeatViewPanel", "Candidate-1", panel);
  }

  @Test
  public void testCandidatesDual() throws IOException {
    BindableWrapper<LinkedHashMap<Candidate, Pair<Integer, Integer>>> currentSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Pair<Integer, Integer>>> previousSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Integer> totalSeats = new BindableWrapper<>(538);
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<String> header = new BindableWrapper<>("UNITED STATES");
    BindableWrapper<String> seatHeader = new BindableWrapper<>("PRESIDENT");
    BindableWrapper<String> seatSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2012");
    BindableWrapper<Candidate> winner = new BindableWrapper<>();

    BasicResultPanel panel =
        BasicResultPanel.candidateDualSeats(
                currentSeats.getBinding(), seatHeader.getBinding(), seatSubhead.getBinding())
            .withWinner(winner.getBinding())
            .withPrev(previousSeats.getBinding(), changeHeader.getBinding())
            .withTotal(totalSeats.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " ELECTORAL VOTES TO WIN")
            .build(header.getBinding());
    panel.setSize(1024, 512);

    Candidate clinton = new Candidate("Hillary Clinton", new Party("Democrat", "DEM", Color.BLUE));
    Candidate trump = new Candidate("Donald Trump", new Party("Republican", "GOP", Color.RED));

    LinkedHashMap<Candidate, Pair<Integer, Integer>> curr = new LinkedHashMap<>();
    curr.put(clinton, ImmutablePair.of(218, 232));
    curr.put(trump, ImmutablePair.of(276, 306));
    currentSeats.setValue(curr);

    LinkedHashMap<Party, Pair<Integer, Integer>> prev = new LinkedHashMap<>();
    prev.put(clinton.getParty(), ImmutablePair.of(302, 332));
    prev.put(trump.getParty(), ImmutablePair.of(192, 206));
    previousSeats.setValue(prev);

    winner.setValue(trump);
    compareRendering("SeatViewPanel", "Candidate-2", panel);
  }

  @Test
  public void testCandidatesRange() throws IOException {
    BindableWrapper<LinkedHashMap<Candidate, Range<Integer>>> currentSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> previousSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Integer> totalSeats = new BindableWrapper<>(538);
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<String> header = new BindableWrapper<>("UNITED STATES");
    BindableWrapper<String> seatHeader = new BindableWrapper<>("PRESIDENT");
    BindableWrapper<String> seatSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2012");

    Candidate clinton = new Candidate("Hillary Clinton", new Party("Democrat", "DEM", Color.BLUE));
    Candidate trump = new Candidate("Donald Trump", new Party("Republican", "GOP", Color.RED));

    LinkedHashMap<Candidate, Range<Integer>> curr = new LinkedHashMap<>();
    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();

    curr.put(clinton, Range.between(303 - 65, 303 + 65));
    curr.put(trump, Range.between(235 - 65, 235 + 65));
    currentSeats.setValue(curr);

    prev.put(clinton.getParty(), 332);
    prev.put(trump.getParty(), 206);
    previousSeats.setValue(prev);

    BasicResultPanel panel =
        BasicResultPanel.candidateRangeSeats(
                currentSeats.getBinding(), seatHeader.getBinding(), seatSubhead.getBinding())
            .withPrev(previousSeats.getBinding(), changeHeader.getBinding())
            .withTotal(totalSeats.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " ELECTORAL VOTES TO WIN")
            .build(header.getBinding());
    panel.setSize(1024, 512);
    compareRendering("SeatViewPanel", "Candidate-3", panel);
  }

  @Test
  public void testPartySeatsTicked() throws IOException {
    BindableWrapper<LinkedHashMap<Party, Integer>> currentSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Integer>> previousSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Integer> totalSeats = new BindableWrapper<>(435);
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<String> header = new BindableWrapper<>("UNITED STATES");
    BindableWrapper<String> seatHeader = new BindableWrapper<>("HOUSE OF REPRESENTATIVES");
    BindableWrapper<String> seatSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2016");
    BindableWrapper<Party> winner = new BindableWrapper<>();

    BasicResultPanel panel =
        BasicResultPanel.partySeats(
                currentSeats.getBinding(), seatHeader.getBinding(), seatSubhead.getBinding())
            .withPrev(previousSeats.getBinding(), changeHeader.getBinding())
            .withWinner(winner.getBinding())
            .withTotal(totalSeats.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " SEATS FOR MAJORITY")
            .build(header.getBinding());
    panel.setSize(1024, 512);

    Party dem = new Party("Democrat", "DEM", Color.BLUE);
    Party gop = new Party("Republican", "GOP", Color.RED);

    LinkedHashMap<Party, Integer> curr = new LinkedHashMap<>();
    curr.put(dem, 235);
    curr.put(gop, 200);
    currentSeats.setValue(curr);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(dem, 194);
    prev.put(gop, 241);
    previousSeats.setValue(prev);

    winner.setValue(dem);
    compareRendering("SeatViewPanel", "PartyTick-1", panel);
  }

  @Test
  public void testPartySeatsTickedDual() throws IOException {
    BindableWrapper<LinkedHashMap<Party, Pair<Integer, Integer>>> currentSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<LinkedHashMap<Party, Pair<Integer, Integer>>> previousSeats =
        new BindableWrapper<>(new LinkedHashMap<>());
    BindableWrapper<Integer> totalSeats = new BindableWrapper<>(435);
    BindableWrapper<Boolean> showMajority = new BindableWrapper<>(true);
    BindableWrapper<String> header = new BindableWrapper<>("UNITED STATES");
    BindableWrapper<String> seatHeader = new BindableWrapper<>("HOUSE OF REPRESENTATIVES");
    BindableWrapper<String> seatSubhead = new BindableWrapper<>("");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2016");
    BindableWrapper<Party> winner = new BindableWrapper<>();

    BasicResultPanel panel =
        BasicResultPanel.partyDualSeats(
                currentSeats.getBinding(), seatHeader.getBinding(), seatSubhead.getBinding())
            .withWinner(winner.getBinding())
            .withPrev(previousSeats.getBinding(), changeHeader.getBinding())
            .withTotal(totalSeats.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " SEATS FOR MAJORITY")
            .build(header.getBinding());
    panel.setSize(1024, 512);

    Party dem = new Party("Democrat", "DEM", Color.BLUE);
    Party gop = new Party("Republican", "GOP", Color.RED);

    LinkedHashMap<Party, Pair<Integer, Integer>> curr = new LinkedHashMap<>();
    curr.put(dem, ImmutablePair.of(224, 235));
    curr.put(gop, ImmutablePair.of(192, 200));
    currentSeats.setValue(curr);

    LinkedHashMap<Party, Pair<Integer, Integer>> prev = new LinkedHashMap<>();
    prev.put(dem, ImmutablePair.of(193, 194));
    prev.put(gop, ImmutablePair.of(223, 241));
    previousSeats.setValue(prev);

    winner.setValue(dem);
    compareRendering("SeatViewPanel", "PartyTick-2", panel);
  }

  private Map<Integer, Shape> peiShapesByDistrict() throws IOException {
    URL peiMap =
        MapFrameTest.class
            .getClassLoader()
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp");
    return ShapefileReader.readShapes(peiMap, "DIST_NO", Integer.class);
  }
}
