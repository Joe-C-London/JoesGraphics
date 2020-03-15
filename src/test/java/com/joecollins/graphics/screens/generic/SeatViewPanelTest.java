package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.graphics.components.MapFrameTest;
import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.graphics.utils.ShapefileReader;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.awt.Shape;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;

public class SeatViewPanelTest {

  @Test
  public void testBasic() throws IOException {
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

    SeatViewPanel panel =
        SeatViewPanel.Builder.basic(
                currentSeats.getBinding(),
                previousSeats.getBinding(),
                totalSeats.getBinding(),
                header.getBinding(),
                seatHeader.getBinding(),
                seatSubhead.getBinding(),
                changeHeader.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " SEATS FOR MAJORITY")
            .build();
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

    SeatViewPanel panel =
        SeatViewPanel.Builder.basic(
                currentSeats.getBinding(),
                previousSeats.getBinding(),
                totalSeats.getBinding(),
                header.getBinding(),
                seatHeader.getBinding(),
                seatSubhead.getBinding(),
                changeHeader.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " SEATS FOR MAJORITY")
            .withSwing(
                swingHeader.getBinding(),
                currentVotes.getBinding(),
                previousVotes.getBinding(),
                partyOrder)
            .build();
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

    SeatViewPanel panel =
        SeatViewPanel.Builder.basic(
                currentSeats.getBinding(),
                previousSeats.getBinding(),
                totalSeats.getBinding(),
                header.getBinding(),
                seatHeader.getBinding(),
                seatSubhead.getBinding(),
                changeHeader.getBinding())
            .withMajorityLine(showMajority.getBinding(), n -> n + " SEATS FOR MAJORITY")
            .withSwing(
                swingHeader.getBinding(),
                currentVotes.getBinding(),
                previousVotes.getBinding(),
                partyOrder)
            .withMap(
                mapHeader.getBinding(),
                shapesByDistrict,
                winnersByDistrict.getBinding(),
                focus.getBinding())
            .build();
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

  private Map<Integer, Shape> peiShapesByDistrict() throws IOException {
    URL peiMap =
        MapFrameTest.class
            .getClassLoader()
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp");
    return ShapefileReader.readShapes(peiMap, "DIST_NO", Integer.class);
  }
}
