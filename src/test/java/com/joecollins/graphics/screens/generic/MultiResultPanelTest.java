package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.graphics.components.MapFrameTest;
import com.joecollins.graphics.utils.ShapefileReader;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.awt.Shape;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class MultiResultPanelTest {

  private static Party lib = new Party("Liberal", "LIB", Color.RED);
  private static Party grn = new Party("Green", "GRN", Color.GREEN.darker());
  private static Party pc = new Party("Progressive Conservative", "PCP", Color.BLUE);
  private static Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
  private static Party ind = new Party("Independent", "IND", Color.GRAY);

  @Test
  public void testSimplePanel() throws IOException {
    BindableList<District> districts = new BindableList<>();
    districts.add(
        new District(
            8,
            "Stanhope-Marshfield",
            Map.of(
                new Candidate("Wade MacLauchlan", lib, true), 1196,
                new Candidate("Bloyce Thompson", pc), 1300,
                new Candidate("Sarah Donald", grn), 747,
                new Candidate("Marian White", ndp), 46),
            false,
            Map.of(lib, 1938, pc, 1338, grn, 347, ndp, 443)));
    districts.add(
        new District(
            15,
            "Brackley-Hunter River",
            Map.of(
                new Candidate("Windsor Wight", lib), 899,
                new Candidate("Dennis King", pc, true), 1315,
                new Candidate("Greg Bradley", grn), 879,
                new Candidate("Leah-Jane Hayward", ndp), 57),
            true,
            Map.of(lib, 1389, pc, 1330, grn, 462, ndp, 516)));
    districts.add(
        new District(
            17,
            "New Haven-Rocky Point",
            Map.of(
                new Candidate("Judy MacNevin", lib), 515,
                new Candidate("Kris Currie", pc), 1068,
                new Candidate("Peter Bevan-Baker", grn, true), 1870,
                new Candidate("Don Wills", ind), 26),
            true,
            Map.of(lib, 1046, pc, 609, grn, 2077, ndp, 58)));

    Map<Integer, Shape> shapesByDistrict = peiShapesByDistrict();
    List<Party> swingometerOrder = List.of(ndp, grn, lib, ind, pc);
    MultiResultPanel panel =
        MultiResultPanel.of(
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

  private Map<Integer, Shape> peiShapesByDistrict() throws IOException {
    URL peiMap =
        MapFrameTest.class
            .getClassLoader()
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp");
    return ShapefileReader.readShapes(peiMap, "DIST_NO", Integer.class);
  }

  private static class District {
    private final int districtNum;
    private final String name;
    private final Map<Candidate, Integer> votes;
    private final boolean leaderHasWon;
    private final Map<Party, Integer> prevVotes;

    private District(
        int districtNum,
        String name,
        Map<Candidate, Integer> votes,
        boolean leaderHasWon,
        Map<Party, Integer> prevVotes) {
      this.districtNum = districtNum;
      this.name = name;
      this.votes = votes;
      this.leaderHasWon = leaderHasWon;
      this.prevVotes = prevVotes;
    }
  }
}
