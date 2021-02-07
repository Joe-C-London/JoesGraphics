package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class PartySummaryScreenTest {

  Party lib = new Party("Liberal", "LIB", Color.RED);
  Party con = new Party("Conservative", "CON", Color.BLUE);
  Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
  Party bq = new Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker());
  Party grn = new Party("Green", "GRN", Color.GREEN.darker());
  Party oth = Party.OTHERS;

  @Test
  public void testBasicPartySummaryWithDiff() throws IOException {
    Region canada = new Region("Canada");
    Region bc = new Region("British Columbia");
    Region prairies = new Region("Prairies");
    Region ontario = new Region("Ontario");
    Region quebec = new Region("Qu\u00e9bec");
    Region atlantic = new Region("Atlantic");
    Region north = new Region("North");

    BindableWrapper<Party> partySelected = new BindableWrapper<>(lib);

    PartySummaryScreen screen =
        PartySummaryScreen.ofDiff(
                canada,
                r -> Binding.fixedBinding(r.name.toUpperCase()),
                r -> Binding.propertyBinding(r, x -> x.seats, Region.Property.SEATS),
                r -> Binding.propertyBinding(r, x -> x.seatDiff, Region.Property.SEAT_DIFF),
                r -> Binding.propertyBinding(r, x -> x.votePct, Region.Property.VOTE_PCT),
                r -> Binding.propertyBinding(r, x -> x.votePctDiff, Region.Property.VOTE_PCT_DIFF),
                3)
            .withRegion(bc)
            .withRegion(prairies)
            .withRegion(ontario)
            .withRegion(quebec)
            .withRegion(atlantic)
            .withRegion(north)
            .build(partySelected.getBinding());
    screen.setSize(1024, 512);
    compareRendering("PartySummaryScreen", "SingleParty-1", screen);

    atlantic.setSeats(Map.of(lib, 26, con, 4, ndp, 1, grn, 1));
    atlantic.setSeatDiff(Map.of(lib, -6, con, +4, ndp, +1, grn, +1));
    atlantic.setVotePct(Map.of(lib, 0.4089, con, 0.2863, ndp, 0.1583, grn, 0.1227, oth, 0.0238));
    atlantic.setVotePctDiff(
        Map.of(lib, -0.1784, con, +0.0960, ndp, -0.0209, grn, +0.0874, oth, +0.0159));

    canada.setSeats(Map.of(lib, 26, con, 4, ndp, 1, grn, 1));
    canada.setSeatDiff(Map.of(lib, -6, con, +4, ndp, +1, grn, +1));
    canada.setVotePct(Map.of(lib, 0.4089, con, 0.2863, ndp, 0.1583, grn, 0.1227, oth, 0.0238));
    canada.setVotePctDiff(
        Map.of(lib, -0.1784, con, +0.0960, ndp, -0.0209, grn, +0.0874, oth, +0.0159));

    compareRendering("PartySummaryScreen", "SingleParty-2", screen);

    quebec.setSeats(Map.of(lib, 35, con, 10, ndp, 1, bq, 32));
    quebec.setSeatDiff(Map.of(lib, -5, con, -2, ndp, -15, bq, +22));
    quebec.setVotePct(
        Map.of(lib, 0.3428, con, 0.1598, ndp, 0.1084, grn, 0.0451, bq, 0.3237, oth, 0.0201));
    quebec.setVotePctDiff(
        Map.of(lib, -0.0146, con, -0.0074, ndp, -0.1451, grn, +0.0227, bq, +0.1301, oth, +0.0143));

    ontario.setSeats(Map.of(lib, 79, con, 36, ndp, 6));
    ontario.setSeatDiff(Map.of(lib, -1, con, +3, ndp, -2));
    ontario.setVotePct(Map.of(lib, 0.4155, con, 0.3305, ndp, 0.1681, grn, 0.0623, oth, 0.0235));
    ontario.setVotePctDiff(
        Map.of(lib, -0.0323, con, -0.0201, ndp, +0.0021, grn, +0.0339, oth, +0.0164));

    prairies.setSeats(Map.of(lib, 4, con, 54, ndp, 4));
    prairies.setSeatDiff(Map.of(lib, -8, con, +10, ndp, -2));
    prairies.setVotePct(Map.of(lib, 0.1574, con, 0.6381, ndp, 0.1470, grn, 0.0321, oth, 0.0253));
    prairies.setVotePctDiff(
        Map.of(lib, -0.1258, con, +0.1054, ndp, +0.0026, grn, +0.0064, oth, +0.0115));

    north.setSeats(Map.of(lib, 1, ndp, 1));
    north.setSeatDiff(Map.of(lib, -1, ndp, +1));
    north.setVotePct(Map.of(lib, 0.3645, con, 0.2574, ndp, 0.2914, grn, 0.0752, oth, 0.0115));
    north.setVotePctDiff(
        Map.of(lib, -0.1141, con, +0.0491, ndp, 0.0016, grn, +0.0520, oth, +0.0115));

    canada.setSeats(Map.of(lib, 145, con, 104, ndp, 13, grn, 1, bq, 32));
    canada.setSeatDiff(Map.of(lib, -21, con, +15, ndp, -17, grn, +1, bq, +22));
    canada.setVotePct(
        Map.of(lib, 0.3418, con, 0.3439, ndp, 0.1469, grn, 0.0564, bq, 0.0880, oth, 0.0230));
    canada.setVotePctDiff(
        Map.of(lib, -0.0595, con, 0.0216, ndp, -0.0405, grn, 0.0296, bq, +0.0340, oth, +0.0148));

    compareRendering("PartySummaryScreen", "SingleParty-3", screen);

    partySelected.setValue(con);
    compareRendering("PartySummaryScreen", "SingleParty-4", screen);

    north.setSeats(Map.of(lib, 2, ndp, 1));
    north.setSeatDiff(Map.of(lib, -1, ndp, +1));
    north.setVotePct(Map.of(lib, 0.3511, con, 0.2889, ndp, 0.2591, grn, 0.0885, oth, 0.0124));
    north.setVotePctDiff(
        Map.of(lib, -0.1505, con, +0.0669, ndp, +0.0072, grn, +0.0640, oth, +0.0124));

    bc.setSeats(Map.of(lib, 11, con, 17, ndp, 11, grn, 2, oth, 1));
    bc.setSeatDiff(Map.of(lib, -6, con, +7, ndp, -3, grn, +1, oth, +1));
    bc.setVotePct(Map.of(lib, 0.2616, con, 0.3398, ndp, 0.2444, grn, 0.1248, oth, 0.0294));
    bc.setVotePctDiff(Map.of(lib, -0.0893, con, +0.0404, ndp, -0.0158, grn, +0.0424, oth, +0.0223));

    canada.setSeats(Map.of(lib, 157, con, 121, ndp, 24, grn, 3, bq, 32, oth, 1));
    canada.setSeatDiff(Map.of(lib, -27, con, +22, ndp, -20, grn, +2, bq, +22, oth, +1));
    canada.setVotePct(
        Map.of(lib, 0.3312, con, 0.3434, ndp, 0.1598, grn, 0.0655, bq, 0.0763, oth, 0.0238));
    canada.setVotePctDiff(
        Map.of(lib, -0.0634, con, +0.0243, ndp, -0.0374, grn, +0.0312, bq, +0.0297, oth, +0.0158));

    compareRendering("PartySummaryScreen", "SingleParty-5", screen);
  }

  @Test
  public void testBasicPartySummaryWithPrev() throws IOException {
    Region canada = new Region("Canada");
    Region bc = new Region("British Columbia");
    Region prairies = new Region("Prairies");
    Region ontario = new Region("Ontario");
    Region quebec = new Region("Qu\u00e9bec");
    Region atlantic = new Region("Atlantic");
    Region north = new Region("North");

    BindableWrapper<Party> partySelected = new BindableWrapper<>(lib);

    PartySummaryScreen screen =
        PartySummaryScreen.ofPrev(
                canada,
                r -> Binding.fixedBinding(r.name.toUpperCase()),
                r -> Binding.propertyBinding(r, x -> x.seats, Region.Property.SEATS),
                r -> Binding.propertyBinding(r, x -> x.getPrevSeats(), Region.Property.SEAT_DIFF),
                r -> Binding.propertyBinding(r, x -> x.votePct, Region.Property.VOTE_PCT),
                r ->
                    Binding.propertyBinding(
                        r, x -> x.getPrevVotePct(), Region.Property.VOTE_PCT_DIFF),
                3)
            .withRegion(bc)
            .withRegion(prairies)
            .withRegion(ontario)
            .withRegion(quebec)
            .withRegion(atlantic)
            .withRegion(north)
            .build(partySelected.getBinding());
    screen.setSize(1024, 512);
    compareRendering("PartySummaryScreen", "SingleParty-1", screen);

    atlantic.setSeats(Map.of(lib, 26, con, 4, ndp, 1, grn, 1));
    atlantic.setSeatDiff(Map.of(lib, -6, con, +4, ndp, +1, grn, +1));
    atlantic.setVotePct(Map.of(lib, 0.4089, con, 0.2863, ndp, 0.1583, grn, 0.1227, oth, 0.0238));
    atlantic.setVotePctDiff(
        Map.of(lib, -0.1784, con, +0.0960, ndp, -0.0209, grn, +0.0874, oth, +0.0159));

    canada.setSeats(Map.of(lib, 26, con, 4, ndp, 1, grn, 1));
    canada.setSeatDiff(Map.of(lib, -6, con, +4, ndp, +1, grn, +1));
    canada.setVotePct(Map.of(lib, 0.4089, con, 0.2863, ndp, 0.1583, grn, 0.1227, oth, 0.0238));
    canada.setVotePctDiff(
        Map.of(lib, -0.1784, con, +0.0960, ndp, -0.0209, grn, +0.0874, oth, +0.0159));

    compareRendering("PartySummaryScreen", "SingleParty-2", screen);

    quebec.setSeats(Map.of(lib, 35, con, 10, ndp, 1, bq, 32));
    quebec.setSeatDiff(Map.of(lib, -5, con, -2, ndp, -15, bq, +22));
    quebec.setVotePct(
        Map.of(lib, 0.3428, con, 0.1598, ndp, 0.1084, grn, 0.0451, bq, 0.3237, oth, 0.0201));
    quebec.setVotePctDiff(
        Map.of(lib, -0.0146, con, -0.0074, ndp, -0.1451, grn, +0.0227, bq, +0.1301, oth, +0.0143));

    ontario.setSeats(Map.of(lib, 79, con, 36, ndp, 6));
    ontario.setSeatDiff(Map.of(lib, -1, con, +3, ndp, -2));
    ontario.setVotePct(Map.of(lib, 0.4155, con, 0.3305, ndp, 0.1681, grn, 0.0623, oth, 0.0235));
    ontario.setVotePctDiff(
        Map.of(lib, -0.0323, con, -0.0201, ndp, +0.0021, grn, +0.0339, oth, +0.0164));

    prairies.setSeats(Map.of(lib, 4, con, 54, ndp, 4));
    prairies.setSeatDiff(Map.of(lib, -8, con, +10, ndp, -2));
    prairies.setVotePct(Map.of(lib, 0.1574, con, 0.6381, ndp, 0.1470, grn, 0.0321, oth, 0.0253));
    prairies.setVotePctDiff(
        Map.of(lib, -0.1258, con, +0.1054, ndp, +0.0026, grn, +0.0064, oth, +0.0115));

    north.setSeats(Map.of(lib, 1, ndp, 1));
    north.setSeatDiff(Map.of(lib, -1, ndp, +1));
    north.setVotePct(Map.of(lib, 0.3645, con, 0.2574, ndp, 0.2914, grn, 0.0752, oth, 0.0115));
    north.setVotePctDiff(
        Map.of(lib, -0.1141, con, +0.0491, ndp, 0.0016, grn, +0.0520, oth, +0.0115));

    canada.setSeats(Map.of(lib, 145, con, 104, ndp, 13, grn, 1, bq, 32));
    canada.setSeatDiff(Map.of(lib, -21, con, +15, ndp, -17, grn, +1, bq, +22));
    canada.setVotePct(
        Map.of(lib, 0.3418, con, 0.3439, ndp, 0.1469, grn, 0.0564, bq, 0.0880, oth, 0.0230));
    canada.setVotePctDiff(
        Map.of(lib, -0.0595, con, 0.0216, ndp, -0.0405, grn, 0.0296, bq, +0.0340, oth, +0.0148));

    compareRendering("PartySummaryScreen", "SingleParty-3", screen);

    partySelected.setValue(con);
    compareRendering("PartySummaryScreen", "SingleParty-4", screen);

    north.setSeats(Map.of(lib, 2, ndp, 1));
    north.setSeatDiff(Map.of(lib, -1, ndp, +1));
    north.setVotePct(Map.of(lib, 0.3511, con, 0.2889, ndp, 0.2591, grn, 0.0885, oth, 0.0124));
    north.setVotePctDiff(
        Map.of(lib, -0.1505, con, +0.0669, ndp, +0.0072, grn, +0.0640, oth, +0.0124));

    bc.setSeats(Map.of(lib, 11, con, 17, ndp, 11, grn, 2, oth, 1));
    bc.setSeatDiff(Map.of(lib, -6, con, +7, ndp, -3, grn, +1, oth, +1));
    bc.setVotePct(Map.of(lib, 0.2616, con, 0.3398, ndp, 0.2444, grn, 0.1248, oth, 0.0294));
    bc.setVotePctDiff(Map.of(lib, -0.0893, con, +0.0404, ndp, -0.0158, grn, +0.0424, oth, +0.0223));

    canada.setSeats(Map.of(lib, 157, con, 121, ndp, 24, grn, 3, bq, 32, oth, 1));
    canada.setSeatDiff(Map.of(lib, -27, con, +22, ndp, -20, grn, +2, bq, +22, oth, +1));
    canada.setVotePct(
        Map.of(lib, 0.3312, con, 0.3434, ndp, 0.1598, grn, 0.0655, bq, 0.0763, oth, 0.0238));
    canada.setVotePctDiff(
        Map.of(lib, -0.0634, con, +0.0243, ndp, -0.0374, grn, +0.0312, bq, +0.0297, oth, +0.0158));

    compareRendering("PartySummaryScreen", "SingleParty-5", screen);
  }

  private static class Region extends Bindable<Region, Region.Property> {
    private enum Property {
      SEATS,
      SEAT_DIFF,
      VOTE_PCT,
      VOTE_PCT_DIFF
    }

    private final String name;
    private Map<Party, Integer> seats = Map.of();
    private Map<Party, Integer> seatDiff = Map.of();
    private Map<Party, Double> votePct = Map.of();
    private Map<Party, Double> votePctDiff = Map.of();

    private Region(String name) {
      this.name = name;
    }

    public void setSeats(Map<Party, Integer> seats) {
      this.seats = seats;
      onPropertyRefreshed(Property.SEATS);
    }

    public void setSeatDiff(Map<Party, Integer> seatDiff) {
      this.seatDiff = seatDiff;
      onPropertyRefreshed(Property.SEAT_DIFF);
    }

    public void setVotePct(Map<Party, Double> votePct) {
      this.votePct = votePct;
      onPropertyRefreshed(Property.VOTE_PCT);
    }

    public void setVotePctDiff(Map<Party, Double> votePctDiff) {
      this.votePctDiff = votePctDiff;
      onPropertyRefreshed(Property.VOTE_PCT_DIFF);
    }

    public Map<Party, Integer> getPrevSeats() {
      return Stream.concat(seats.keySet().stream(), seatDiff.keySet().stream())
          .distinct()
          .collect(
              Collectors.toMap(
                  Function.identity(),
                  p -> seats.getOrDefault(p, 0) - seatDiff.getOrDefault(p, 0)));
    }

    public Map<Party, Double> getPrevVotePct() {
      return Stream.concat(votePct.keySet().stream(), votePctDiff.keySet().stream())
          .distinct()
          .collect(
              Collectors.toMap(
                  Function.identity(),
                  p -> votePct.getOrDefault(p, 0.0) - votePctDiff.getOrDefault(p, 0.0)));
    }
  }
}
