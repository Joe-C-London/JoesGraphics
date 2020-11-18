package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class MultiResultScreenTest {

  private static Party lib = new Party("Liberal", "LIB", Color.RED);
  private static Party grn = new Party("Green", "GRN", Color.GREEN.darker());
  private static Party pc = new Party("Progressive Conservative", "PCP", Color.BLUE);
  private static Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
  private static Party pa = new Party("People's Alliance", "PA", Color.MAGENTA.darker());
  private static Party ind = new Party("Independent", "IND", Color.GRAY);

  @Test
  public void testSimplePanel() throws IOException {
    BindableList<District> districts = new BindableList<>();
    districts.add(
        new District(
            8,
            "Stanhope-Marshfield",
            of(
                new Candidate("Wade MacLauchlan", lib, true), 1196,
                new Candidate("Bloyce Thompson", pc), 1300,
                new Candidate("Sarah Donald", grn), 747,
                new Candidate("Marian White", ndp), 46),
            false,
            of(lib, 1938, pc, 1338, grn, 347, ndp, 443)));
    districts.add(
        new District(
            15,
            "Brackley-Hunter River",
            of(
                new Candidate("Windsor Wight", lib), 899,
                new Candidate("Dennis King", pc, true), 1315,
                new Candidate("Greg Bradley", grn), 879,
                new Candidate("Leah-Jane Hayward", ndp), 57),
            true,
            of(lib, 1389, pc, 1330, grn, 462, ndp, 516)));
    districts.add(
        new District(
            17,
            "New Haven-Rocky Point",
            of(
                new Candidate("Judy MacNevin", lib), 515,
                new Candidate("Kris Currie", pc), 1068,
                new Candidate("Peter Bevan-Baker", grn, true), 1870,
                new Candidate("Don Wills", ind), 26),
            true,
            of(lib, 1046, pc, 609, grn, 2077, ndp, 58)));

    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    List<Party> swingometerOrder = List.of(ndp, grn, lib, ind, pc);
    MultiResultScreen panel =
        MultiResultScreen.of(
                districts,
                d -> Binding.fixedBinding(d.votes),
                d -> Binding.fixedBinding("DISTRICT " + d.districtNum),
                d -> Binding.fixedBinding(d.name.toUpperCase()))
            .withIncumbentMarker("(MLA)")
            .withWinner(
                d ->
                    Binding.fixedBinding(
                        d.leaderHasWon
                            ? d.votes.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .orElseThrow()
                                .getKey()
                            : null))
            .withPrev(
                d -> Binding.fixedBinding(d.prevVotes),
                d -> Binding.fixedBinding("SWING SINCE 2015"),
                Comparator.comparingInt(swingometerOrder::indexOf))
            .withMap(
                d -> shapesByDistrict,
                d -> d.districtNum,
                d ->
                    Binding.fixedBinding(
                        new MapBuilder.Result(
                            d.votes.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .map(Candidate::getParty)
                                .orElse(null),
                            d.leaderHasWon)),
                d ->
                    d.districtNum < 10
                        ? List.of(1, 2, 3, 4, 5, 6, 7, 8)
                        : List.of(15, 16, 17, 18, 19, 20),
                d -> Binding.fixedBinding(d.districtNum < 10 ? "CARDIGAN" : "MALPEQUE"))
            .build(() -> "PARTY LEADERS");

    panel.setSize(1024, 512);
    compareRendering("MultiResultPanel", "Basic-1", panel);
  }

  @Test
  public void testVariousUpdates() throws IOException {
    District district8 =
        new District(
            8,
            "Stanhope-Marshfield",
            of(
                new Candidate("Sarah Donald", grn), 0,
                new Candidate("Wade MacLauchlan", lib, true), 0,
                new Candidate("Bloyce Thompson", pc), 0,
                new Candidate("Marian White", ndp), 0),
            false,
            of(lib, 1938, pc, 1338, grn, 347, ndp, 443),
            "0 OF 10 POLLS REPORTING",
            0.0);
    District district15 =
        new District(
            15,
            "Brackley-Hunter River",
            of(
                new Candidate("Greg Bradley", grn), 0,
                new Candidate("Leah-Jane Hayward", ndp), 0,
                new Candidate("Dennis King", pc, true), 0,
                new Candidate("Windsor Wight", lib), 0),
            false,
            of(lib, 1389, pc, 1330, grn, 462, ndp, 516),
            "0 OF 10 POLLS REPORTING",
            0.0);
    District district17 =
        new District(
            17,
            "New Haven-Rocky Point",
            of(
                new Candidate("Peter Bevan-Baker", grn, true), 0,
                new Candidate("Kris Currie", pc), 0,
                new Candidate("Judy MacNevin", lib), 0,
                new Candidate("Don Wills", ind), 0),
            false,
            of(lib, 1046, pc, 609, grn, 2077, ndp, 58),
            "0 OF 10 POLLS REPORTING",
            0.0);

    BindableList<District> districts = new BindableList<>();
    districts.add(district15);
    districts.add(district17);

    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    List<Party> swingometerOrder = List.of(ndp, grn, lib, ind, pc);
    BindableWrapper<String> title = new BindableWrapper<>("MAJOR PARTY LEADERS");
    MultiResultScreen panel =
        MultiResultScreen.of(
                districts,
                d -> d.getVotes(),
                d -> Binding.fixedBinding(d.name.toUpperCase()),
                d -> d.getStatus())
            .withIncumbentMarker("(MLA)")
            .withPctReporting(d -> d.getPctReporting())
            .withWinner(d -> d.getWinner())
            .withPrev(
                d -> Binding.fixedBinding(d.prevVotes),
                d -> Binding.fixedBinding("SWING SINCE 2015"),
                Comparator.comparingInt(swingometerOrder::indexOf))
            .withMap(
                d -> shapesByDistrict,
                d -> d.districtNum,
                d ->
                    d.getLeader()
                        .map(
                            e ->
                                new MapBuilder.Result(
                                    e.getLeft() == null ? null : e.getLeft().getParty(),
                                    e.getRight())),
                d ->
                    d.districtNum < 10
                        ? List.of(1, 2, 3, 4, 5, 6, 7, 8)
                        : List.of(15, 16, 17, 18, 19, 20),
                d -> Binding.fixedBinding(d.districtNum < 10 ? "CARDIGAN" : "MALPEQUE"))
            .build(title.getBinding());

    panel.setSize(1024, 512);
    compareRendering("MultiResultPanel", "Update-1", panel);

    district17.update(
        "1 OF 10 POLLS REPORTING",
        0.1,
        of(
            new Candidate("Peter Bevan-Baker", grn, true), 851,
            new Candidate("Kris Currie", pc), 512,
            new Candidate("Judy MacNevin", lib), 290,
            new Candidate("Don Wills", ind), 7));
    compareRendering("MultiResultPanel", "Update-2", panel);

    districts.add(0, district8);
    title.setValue("PARTY LEADERS");
    compareRendering("MultiResultPanel", "Update-3", panel);

    district15.update(
        "1 OF 10 POLLS REPORTING",
        0.1,
        of(
            new Candidate("Greg Bradley", grn), 287,
            new Candidate("Leah-Jane Hayward", ndp), 27,
            new Candidate("Dennis King", pc, true), 583,
            new Candidate("Windsor Wight", lib), 425));
    compareRendering("MultiResultPanel", "Update-4", panel);

    district8.update(
        "1 OF 10 POLLS REPORTING",
        0.1,
        of(
            new Candidate("Sarah Donald", grn), 285,
            new Candidate("Wade MacLauchlan", lib, true), 620,
            new Candidate("Bloyce Thompson", pc), 609,
            new Candidate("Marian White", ndp), 22));
    compareRendering("MultiResultPanel", "Update-5", panel);

    district15.update(
        "5 OF 10 POLLS REPORTING",
        0.5,
        of(
            new Candidate("Greg Bradley", grn), 287 + 72 + 91 + 79 + 38,
            new Candidate("Leah-Jane Hayward", ndp), 27 + 7 + 7 + 1 + 3,
            new Candidate("Dennis King", pc, true), 583 + 87 + 109 + 76 + 54,
            new Candidate("Windsor Wight", lib), 425 + 73 + 66 + 58 + 30),
        true);
    compareRendering("MultiResultPanel", "Update-6", panel);

    districts.remove(district15);
    title.setValue("PARTY LEADERS IN DOUBT");
    compareRendering("MultiResultPanel", "Update-7", panel);

    district15.update(
        "10 OF 10 POLLS REPORTING",
        1.0,
        of(
            new Candidate("Greg Bradley", grn), 879,
            new Candidate("Leah-Jane Hayward", ndp), 57,
            new Candidate("Dennis King", pc, true), 1315,
            new Candidate("Windsor Wight", lib), 899));
    // intentionally same as before, as this district is no longer displayed
    compareRendering("MultiResultPanel", "Update-7", panel);

    district17.update(
        "2 OF 10 POLLS REPORTING",
        0.2,
        of(
            new Candidate("Peter Bevan-Baker", grn, true), 851 + 117,
            new Candidate("Kris Currie", pc), 512 + 90,
            new Candidate("Judy MacNevin", lib), 290 + 28,
            new Candidate("Don Wills", ind), 7 + 4));
    compareRendering("MultiResultPanel", "Update-8", panel);

    district8.update(
        "2 OF 10 POLLS REPORTING",
        0.2,
        of(
            new Candidate("Sarah Donald", grn), 285 + 50,
            new Candidate("Wade MacLauchlan", lib, true), 620 + 68,
            new Candidate("Bloyce Thompson", pc), 609 + 112,
            new Candidate("Marian White", ndp), 22 + 7));
    compareRendering("MultiResultPanel", "Update-9", panel);
  }

  @Test
  public void testOthersPanel() throws IOException {
    BindableList<District> districts = new BindableList<>();
    districts.add(
        new District(
            30,
            "Saint John East",
            of(
                new Candidate("Glen Savoie", pc, true), 3507,
                new Candidate("Phil Comeau", lib), 1639,
                new Candidate("Gerald Irish", grn), 394,
                new Candidate("Patrick Kemp", pa), 434,
                new Candidate("Josh Floyd", ndp), 248),
            true,
            of(lib, 1775, pc, 3017, grn, 373, ndp, 402, pa, 1047)));
    districts.add(
        new District(
            32,
            "Saint John Harbour",
            of(
                new Candidate("Arlene Dunn", pc), 2181,
                new Candidate("Alice McKim", lib), 1207,
                new Candidate("Brent Harris", grn), 1224,
                new Candidate("Tony Gunn", pa), 186,
                new Candidate("Courtney Pyrke", ndp), 309,
                new Candidate("Mike Cyr", ind), 47,
                new Candidate("Arty Watson", ind), 114),
            false,
            of(lib, 1865, pc, 1855, grn, 721, ndp, 836, pa, 393)));
    districts.add(
        new District(
            33,
            "Saint John Lancaster",
            of(
                new Candidate("Dorothy Shephard", pc, true), 3560,
                new Candidate("Sharon Teare", lib), 1471,
                new Candidate("Joanna Killen", grn), 938,
                new Candidate("Paul Seelye", pa), 394,
                new Candidate("Don Durant", ndp), 201),
            true,
            of(lib, 1727, pc, 3001, grn, 582, ndp, 414, pa, 922)));

    List<Party> swingometerOrder = List.of(ndp, grn, lib, ind, pc, pa);
    MultiResultScreen panel =
        MultiResultScreen.of(
                districts,
                d -> Binding.fixedBinding(d.votes),
                d -> Binding.fixedBinding("DISTRICT " + d.districtNum),
                d -> Binding.fixedBinding(d.name.toUpperCase()))
            .withIncumbentMarker("(MLA)")
            .withWinner(
                d ->
                    Binding.fixedBinding(
                        d.leaderHasWon
                            ? d.votes.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .orElseThrow()
                                .getKey()
                            : null))
            .withPrev(
                d -> Binding.fixedBinding(d.prevVotes),
                d -> Binding.fixedBinding("SWING SINCE 2018"),
                Comparator.comparingInt(swingometerOrder::indexOf))
            .build(() -> "SAINT JOHN");

    panel.setSize(1024, 512);
    compareRendering("MultiResultPanel", "Others-1", panel);
  }

  @Test
  public void testMultipleRowsPanel() throws IOException {
    BindableList<District> districts = new BindableList<>();
    districts.add(
        new District(
            30,
            "Saint John East",
            of(
                new Candidate("Glen Savoie", pc, true), 3507,
                new Candidate("Phil Comeau", lib), 1639,
                new Candidate("Gerald Irish", grn), 394,
                new Candidate("Patrick Kemp", pa), 434,
                new Candidate("Josh Floyd", ndp), 248),
            true,
            of(lib, 1775, pc, 3017, grn, 373, ndp, 402, pa, 1047)));
    districts.add(
        new District(
            31,
            "Portland-Simonds",
            of(
                new Candidate("Trevor Holder", pc, true), 3170,
                new Candidate("Tim Jones", lib), 1654,
                new Candidate("Stefan Warner", grn), 483,
                new Candidate("Darella Jackson", pa), 282,
                new Candidate("Erik Heinze-Milne", ndp), 164),
            true,
            of(lib, 1703, pc, 3168, grn, 435, ndp, 449, ind, 191)));
    districts.add(
        new District(
            32,
            "Saint John Harbour",
            of(
                new Candidate("Arlene Dunn", pc), 2181,
                new Candidate("Alice McKim", lib), 1207,
                new Candidate("Brent Harris", grn), 1224,
                new Candidate("Tony Gunn", pa), 186,
                new Candidate("Courtney Pyrke", ndp), 309,
                new Candidate("Mike Cyr", ind), 47,
                new Candidate("Arty Watson", ind), 114),
            false,
            of(lib, 1865, pc, 1855, grn, 721, ndp, 836, pa, 393)));
    districts.add(
        new District(
            33,
            "Saint John Lancaster",
            of(
                new Candidate("Dorothy Shephard", pc, true), 3560,
                new Candidate("Sharon Teare", lib), 1471,
                new Candidate("Joanna Killen", grn), 938,
                new Candidate("Paul Seelye", pa), 394,
                new Candidate("Don Durant", ndp), 201),
            true,
            of(lib, 1727, pc, 3001, grn, 582, ndp, 414, pa, 922)));

    List<Party> swingometerOrder = List.of(ndp, grn, lib, ind, pc, pa);
    MultiResultScreen panel =
        MultiResultScreen.of(
                districts,
                d -> Binding.fixedBinding(d.votes),
                d -> Binding.fixedBinding("DISTRICT " + d.districtNum),
                d -> Binding.fixedBinding(d.name.toUpperCase()))
            .withIncumbentMarker("(MLA)")
            .withWinner(
                d ->
                    Binding.fixedBinding(
                        d.leaderHasWon
                            ? d.votes.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .orElseThrow()
                                .getKey()
                            : null))
            .withPrev(
                d -> Binding.fixedBinding(d.prevVotes),
                d -> Binding.fixedBinding("SWING SINCE 2018"),
                Comparator.comparingInt(swingometerOrder::indexOf))
            .build(() -> "ELECTION 2020: NEW BRUNSWICK DECIDES");

    panel.setSize(1024, 512);
    compareRendering("MultiResultPanel", "MultipleRows-1", panel);

    districts.add(
        new District(
            34,
            "Kings Centre",
            of(
                new Candidate("Bill Oliver", pc, true), 4583,
                new Candidate("Bruce Bryer", grn), 1006,
                new Candidate("Paul Adams", lib), 911,
                new Candidate("William Edgett", pa), 693,
                new Candidate("Margaret Anderson Kilfoil", ndp), 254),
            true,
            of(lib, 1785, pc, 3267, grn, 731, ndp, 342, pa, 1454)));

    districts.add(
        new District(
            35,
            "Fundy-The Isles-Saint John West",
            of(
                new Candidate("Andrea Anderson-Mason", pc, true), 4740,
                new Candidate("Tony Mann", lib), 726,
                new Candidate("Vincent Edgett", pa), 688,
                new Candidate("Lois Mitchell", grn), 686,
                new Candidate("Sharon Greenlaw", ndp), 291),
            true,
            of(lib, 2422, pc, 3808, grn, 469, ndp, 203, pa, 1104)));
    compareRendering("MultiResultPanel", "MultipleRows-2", panel);

    districts.remove(3);
    districts.remove(2);
    districts.remove(1);
    districts.remove(0);
    compareRendering("MultiResultPanel", "MultipleRows-3", panel);
  }

  private <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    LinkedHashMap<K, V> ret = new LinkedHashMap<>();
    ret.put(k1, v1);
    ret.put(k2, v2);
    ret.put(k3, v3);
    ret.put(k4, v4);
    return ret;
  }

  private <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    LinkedHashMap<K, V> ret = new LinkedHashMap<>();
    ret.put(k1, v1);
    ret.put(k2, v2);
    ret.put(k3, v3);
    ret.put(k4, v4);
    ret.put(k5, v5);
    return ret;
  }

  private <K, V> Map<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
    LinkedHashMap<K, V> ret = new LinkedHashMap<>();
    ret.put(k1, v1);
    ret.put(k2, v2);
    ret.put(k3, v3);
    ret.put(k4, v4);
    ret.put(k5, v5);
    ret.put(k6, v6);
    ret.put(k7, v7);
    return ret;
  }

  private Map<Integer, Shape> peiShapesByDistrict() throws IOException {
    URL peiMap =
        MapFrameTest.class
            .getClassLoader()
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp");
    return ShapefileReader.readShapes(peiMap, "DIST_NO", Integer.class);
  }

  private static class District extends Bindable<District.Property> {

    enum Property {
      PROP
    }

    private final int districtNum;
    private final String name;
    private String status;
    private boolean leaderHasWon;
    private Map<Candidate, Integer> votes;
    private Map<Party, Integer> prevVotes;
    private double pctReporting;

    private District(
        int districtNum,
        String name,
        Map<Candidate, Integer> votes,
        boolean leaderHasWon,
        Map<Party, Integer> prevVotes) {
      this(districtNum, name, votes, leaderHasWon, prevVotes, "100% REPORTING", 1.0);
    }

    private District(
        int districtNum,
        String name,
        Map<Candidate, Integer> votes,
        boolean leaderHasWon,
        Map<Party, Integer> prevVotes,
        String status,
        double pctReporting) {
      this.districtNum = districtNum;
      this.name = name;
      this.votes = votes;
      this.leaderHasWon = leaderHasWon;
      this.prevVotes = prevVotes;
      this.status = status;
      this.pctReporting = pctReporting;
    }

    public Binding<String> getStatus() {
      return Binding.propertyBinding(this, t -> t.status, Property.PROP);
    }

    public Binding<Double> getPctReporting() {
      return Binding.propertyBinding(this, t -> t.pctReporting, Property.PROP);
    }

    public Binding<Map<Candidate, Integer>> getVotes() {
      return Binding.propertyBinding(this, t -> t.votes, Property.PROP);
    }

    public Binding<Boolean> getLeaderHasWon() {
      return Binding.propertyBinding(this, t -> t.leaderHasWon, Property.PROP);
    }

    public Binding<Candidate> getWinner() {
      return Binding.propertyBinding(
          this,
          t ->
              leaderHasWon
                  ? votes.entrySet().stream()
                      .max(Map.Entry.comparingByValue())
                      .orElseThrow()
                      .getKey()
                  : null,
          Property.PROP);
    }

    public Binding<Pair<Candidate, Boolean>> getLeader() {
      return Binding.propertyBinding(
          this,
          t ->
              ImmutablePair.of(
                  votes.entrySet().stream()
                      .filter(e -> e.getValue() > 0)
                      .max(Map.Entry.comparingByValue())
                      .map(Map.Entry::getKey)
                      .orElse(null),
                  leaderHasWon),
          Property.PROP);
    }

    public void update(String status, double pctReporting, Map<Candidate, Integer> votes) {
      this.status = status;
      this.pctReporting = pctReporting;
      this.votes = votes;
      onPropertyRefreshed(Property.PROP);
    }

    public void update(
        String status, double pctReporting, Map<Candidate, Integer> votes, boolean leaderHasWon) {
      this.status = status;
      this.pctReporting = pctReporting;
      this.votes = votes;
      this.leaderHasWon = leaderHasWon;
      onPropertyRefreshed(Property.PROP);
    }
  }
}
