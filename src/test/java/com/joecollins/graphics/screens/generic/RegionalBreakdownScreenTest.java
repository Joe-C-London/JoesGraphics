package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.io.IOException;
import java.util.Map;
import org.junit.Test;

public class RegionalBreakdownScreenTest {

  private static Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
  private static Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
  private static Party lib = new Party("Liberal", "LIB", Color.RED);
  private static Party grn = new Party("Green", "GRN", Color.GREEN.darker());
  private static Party ind = new Party("Independent", "IND", Color.GRAY);

  @Test
  public void testSeats() throws IOException {
    BindableWrapper<Map<Party, Integer>> peiSeats = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> cardiganSeats = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> malpequeSeats = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> charlottetownSeats = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> egmontSeats = new BindableWrapper<>(Map.of());

    var screen =
        RegionalBreakdownScreen.seats(
                Binding.fixedBinding("PRINCE EDWARD ISLAND"),
                peiSeats.getBinding(),
                Binding.fixedBinding(27),
                Binding.fixedBinding("SEATS BY REGION"))
            .withBlankRow()
            .withRegion(
                Binding.fixedBinding("CARDIGAN"),
                cardiganSeats.getBinding(),
                Binding.fixedBinding(7))
            .withRegion(
                Binding.fixedBinding("MALPEQUE"),
                malpequeSeats.getBinding(),
                Binding.fixedBinding(7))
            .withRegion(
                Binding.fixedBinding("CHARLOTTETOWN"),
                charlottetownSeats.getBinding(),
                Binding.fixedBinding(6))
            .withRegion(
                Binding.fixedBinding("EGMONT"), egmontSeats.getBinding(), Binding.fixedBinding(7))
            .build(Binding.fixedBinding("PRINCE EDWARD ISLAND"));
    screen.setSize(1024, 512);
    compareRendering("RegionalBreakdownScreen", "Seats-1", screen);

    peiSeats.setValue(Map.of(grn, 1));
    cardiganSeats.setValue(Map.of(grn, 1));
    compareRendering("RegionalBreakdownScreen", "Seats-2", screen);

    peiSeats.setValue(Map.of(pc, 13, grn, 8, lib, 6));
    cardiganSeats.setValue(Map.of(pc, 6, grn, 1));
    malpequeSeats.setValue(Map.of(pc, 5, grn, 1, lib, 1));
    charlottetownSeats.setValue(Map.of(grn, 3, lib, 2, pc, 1));
    egmontSeats.setValue(Map.of(grn, 3, lib, 3, pc, 1));
    compareRendering("RegionalBreakdownScreen", "Seats-3", screen);
  }

  @Test
  public void testSeatsWithDiff() throws IOException {
    BindableWrapper<Map<Party, Integer>> peiSeats = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> cardiganSeats = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> malpequeSeats = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> charlottetownSeats = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> egmontSeats = new BindableWrapper<>(Map.of());

    BindableWrapper<Map<Party, Integer>> peiDiff = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> cardiganDiff = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> malpequeDiff = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> charlottetownDiff = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> egmontDiff = new BindableWrapper<>(Map.of());

    var screen =
        RegionalBreakdownScreen.seatsWithDiff(
                Binding.fixedBinding("PRINCE EDWARD ISLAND"),
                peiSeats.getBinding(),
                peiDiff.getBinding(),
                Binding.fixedBinding(27),
                Binding.fixedBinding("SEATS BY REGION"))
            .withBlankRow()
            .withRegion(
                Binding.fixedBinding("CARDIGAN"),
                cardiganSeats.getBinding(),
                cardiganDiff.getBinding(),
                Binding.fixedBinding(7))
            .withRegion(
                Binding.fixedBinding("MALPEQUE"),
                malpequeSeats.getBinding(),
                malpequeDiff.getBinding(),
                Binding.fixedBinding(7))
            .withRegion(
                Binding.fixedBinding("CHARLOTTETOWN"),
                charlottetownSeats.getBinding(),
                charlottetownDiff.getBinding(),
                Binding.fixedBinding(6))
            .withRegion(
                Binding.fixedBinding("EGMONT"),
                egmontSeats.getBinding(),
                egmontDiff.getBinding(),
                Binding.fixedBinding(7))
            .build(Binding.fixedBinding("PRINCE EDWARD ISLAND"));
    screen.setSize(1024, 512);
    compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-1", screen);

    peiSeats.setValue(Map.of(grn, 1));
    peiDiff.setValue(Map.of(grn, +1, lib, -1));
    cardiganSeats.setValue(Map.of(grn, 1));
    cardiganDiff.setValue(Map.of(grn, +1, lib, -1));
    compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-2", screen);

    peiSeats.setValue(Map.of(pc, 13, grn, 8, lib, 6));
    peiDiff.setValue(Map.of(pc, +5, grn, +7, lib, -12));
    cardiganSeats.setValue(Map.of(pc, 6, grn, 1));
    cardiganDiff.setValue(Map.of(pc, +1, grn, +1, lib, -2));
    malpequeSeats.setValue(Map.of(pc, 5, grn, 1, lib, 1));
    malpequeDiff.setValue(Map.of(pc, +2, grn, 0, lib, -1));
    charlottetownSeats.setValue(Map.of(grn, 3, lib, 2, pc, 1));
    charlottetownDiff.setValue(Map.of(grn, +3, lib, -5, pc, +1));
    egmontSeats.setValue(Map.of(grn, 3, lib, 3, pc, 1));
    egmontDiff.setValue(Map.of(grn, +3, lib, -4, pc, +1));
    compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-3", screen);
  }

  @Test
  public void testSeatsWithPrev() throws IOException {
    BindableWrapper<Map<Party, Integer>> peiSeats = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> cardiganSeats = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> malpequeSeats = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> charlottetownSeats = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> egmontSeats = new BindableWrapper<>(Map.of());

    BindableWrapper<Map<Party, Integer>> peiPrev = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> cardiganPrev = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> malpequePrev = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> charlottetownPrev = new BindableWrapper<>(Map.of());
    BindableWrapper<Map<Party, Integer>> egmontPrev = new BindableWrapper<>(Map.of());

    var screen =
        RegionalBreakdownScreen.seatsWithPrev(
                Binding.fixedBinding("PRINCE EDWARD ISLAND"),
                peiSeats.getBinding(),
                peiPrev.getBinding(),
                Binding.fixedBinding(27),
                Binding.fixedBinding("SEATS BY REGION"))
            .withBlankRow()
            .withRegion(
                Binding.fixedBinding("CARDIGAN"),
                cardiganSeats.getBinding(),
                cardiganPrev.getBinding(),
                Binding.fixedBinding(7))
            .withRegion(
                Binding.fixedBinding("MALPEQUE"),
                malpequeSeats.getBinding(),
                malpequePrev.getBinding(),
                Binding.fixedBinding(7))
            .withRegion(
                Binding.fixedBinding("CHARLOTTETOWN"),
                charlottetownSeats.getBinding(),
                charlottetownPrev.getBinding(),
                Binding.fixedBinding(6))
            .withRegion(
                Binding.fixedBinding("EGMONT"),
                egmontSeats.getBinding(),
                egmontPrev.getBinding(),
                Binding.fixedBinding(7))
            .build(Binding.fixedBinding("PRINCE EDWARD ISLAND"));
    screen.setSize(1024, 512);
    compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-1", screen);

    peiSeats.setValue(Map.of(grn, 1));
    peiPrev.setValue(Map.of(lib, 1));
    cardiganSeats.setValue(Map.of(grn, 1));
    cardiganPrev.setValue(Map.of(lib, 1));
    compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-2", screen);

    peiSeats.setValue(Map.of(pc, 13, grn, 8, lib, 6));
    peiPrev.setValue(Map.of(pc, 8, grn, 1, lib, 18));
    cardiganSeats.setValue(Map.of(pc, 6, grn, 1));
    cardiganPrev.setValue(Map.of(pc, 5, lib, 2));
    malpequeSeats.setValue(Map.of(pc, 5, grn, 1, lib, 1));
    malpequePrev.setValue(Map.of(pc, 3, grn, 1, lib, 2));
    charlottetownSeats.setValue(Map.of(grn, 3, lib, 2, pc, 1));
    charlottetownPrev.setValue(Map.of(lib, 7));
    egmontSeats.setValue(Map.of(grn, 3, lib, 3, pc, 1));
    egmontPrev.setValue(Map.of(lib, 7));
    compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-3", screen);
  }
}
