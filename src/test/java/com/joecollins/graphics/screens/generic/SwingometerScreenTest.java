package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.models.general.Party;
import com.joecollins.models.general.PartyResult;
import java.awt.Color;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class SwingometerScreenTest {

  private static Party lib = new Party("Liberal", "LIB", Color.RED);
  private static Party grn = new Party("Green", "GRN", Color.GREEN.darker());
  private static Party pc = new Party("Progressive Conservative", "PCP", Color.BLUE);
  private static Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
  private static Party pa = new Party("People's Alliance", "PA", Color.MAGENTA.darker());
  private static Party ind = new Party("Independent", "IND", Color.GRAY);

  @Test
  public void testBasicTwoParties() throws IOException {
    BindableWrapper<Map<String, Map<Party, Integer>>> prevResult =
        new BindableWrapper<>(nbPrevResult());
    BindableWrapper<Map<String, PartyResult>> currResult = new BindableWrapper<>(nbCurrResult());
    BindableWrapper<Pair<Party, Party>> parties = new BindableWrapper<>(ImmutablePair.of(lib, pc));
    BindableWrapper<Map<Party, Double>> swing =
        new BindableWrapper<>(
            Map.of(
                pc, +0.0745, lib, -0.0345, grn, +0.0336, pa, -0.0339, ndp, -0.0335, ind, -0.0062));

    SwingometerScreen panel =
        SwingometerScreen.of(
                prevResult.getBinding(),
                currResult.getBinding(),
                swing.getBinding(),
                parties.getBinding(),
                Binding.fixedBinding("SWINGOMETER"))
            .withSeatLabelIncrements(Binding.fixedBinding(3))
            .build(Binding.fixedBinding("NEW BRUNSWICK"));
    panel.setSize(1024, 512);
    compareRendering("SwingometerScreen", "Basic-TwoParty-1", panel);

    parties.setValue(ImmutablePair.of(grn, pc));
    compareRendering("SwingometerScreen", "Basic-TwoParty-2", panel);

    parties.setValue(ImmutablePair.of(pa, pc));
    compareRendering("SwingometerScreen", "Basic-TwoParty-3", panel);
  }

  @Test
  public void testBasicSwingUpdates() throws IOException {
    BindableWrapper<Map<String, Map<Party, Integer>>> prevResult =
        new BindableWrapper<>(nbPrevResult());
    var result = nbCurrResult();
    result.keySet().forEach(riding -> result.put(riding, null));
    BindableWrapper<Map<String, PartyResult>> currResult = new BindableWrapper<>(result);
    BindableWrapper<Pair<Party, Party>> parties = new BindableWrapper<>(ImmutablePair.of(lib, pc));
    BindableWrapper<Map<Party, Double>> swing = new BindableWrapper<>(Map.of());

    SwingometerScreen panel =
        SwingometerScreen.of(
                prevResult.getBinding(),
                currResult.getBinding(),
                swing.getBinding(),
                parties.getBinding(),
                Binding.fixedBinding("SWINGOMETER"))
            .withSeatLabelIncrements(Binding.fixedBinding(3))
            .build(Binding.fixedBinding("NEW BRUNSWICK"));
    panel.setSize(1024, 512);
    compareRendering("SwingometerScreen", "Basic-Updates-1", panel);

    currResult.setValue(nbCurrResult());
    swing.setValue(
        Map.of(pc, +0.0745, lib, -0.0345, grn, +0.0336, pa, -0.0339, ndp, -0.0335, ind, -0.0062));
    compareRendering("SwingometerScreen", "Basic-Updates-2", panel);
  }

  @Test
  public void testFilteredTwoParties() throws IOException {
    BindableWrapper<Map<String, Map<Party, Integer>>> prevResult =
        new BindableWrapper<>(nbPrevResult());
    BindableWrapper<Map<String, PartyResult>> currResult = new BindableWrapper<>(nbCurrResult());
    BindableWrapper<Pair<Party, Party>> parties = new BindableWrapper<>(ImmutablePair.of(lib, pc));
    BindableWrapper<Map<Party, Double>> swing =
        new BindableWrapper<>(
            Map.of(
                pc, +0.0745, lib, -0.0345, grn, +0.0336, pa, -0.0339, ndp, -0.0335, ind, -0.0062));
    BindableWrapper<Set<String>> seatsFiltered =
        new BindableWrapper<>(
            Set.of(
                "Oromocto-Lincoln-Fredericton",
                "Fredericton-Grand Lake",
                "New Maryland-Sunbury",
                "Fredericton South",
                "Fredericton North",
                "Fredericton-York",
                "Fredericton West-Hanwell",
                "Carleton-York"));

    SwingometerScreen panel =
        SwingometerScreen.of(
                prevResult.getBinding(),
                currResult.getBinding(),
                swing.getBinding(),
                parties.getBinding(),
                Binding.fixedBinding("SWINGOMETER"))
            .withSeatLabelIncrements(Binding.fixedBinding(3))
            .withSeatFilter(seatsFiltered.getBinding())
            .build(Binding.fixedBinding("NEW BRUNSWICK"));
    panel.setSize(1024, 512);
    compareRendering("SwingometerScreen", "Filtered-TwoParty-1", panel);

    seatsFiltered.setValue(Set.of());
    compareRendering("SwingometerScreen", "Filtered-TwoParty-2", panel);

    seatsFiltered.setValue(null);
    compareRendering("SwingometerScreen", "Filtered-TwoParty-3", panel);
  }

  private static Map<String, Map<Party, Integer>> nbPrevResult() {
    var ret = new TreeMap<String, Map<Party, Integer>>();
    ret.put("Restigouche West", Map.of(lib, 4233, pc, 961, grn, 2540, ndp, 263, ind, 62));
    ret.put("Campbellton-Dalhousie", Map.of(lib, 3720, pc, 1761, grn, 637, ndp, 721, pa, 558));
    ret.put("Restigouche-Chaleur", Map.of(lib, 4430, pc, 826, grn, 831, ndp, 621));
    ret.put("Bathurst West-Beresford", Map.of(lib, 4351, pc, 1082, grn, 503, ndp, 443, ind, 64));
    ret.put(
        "Bathurst East-Nepisguit-Saint-Isidore", Map.of(lib, 3550, pc, 858, grn, 421, ndp, 2026));
    ret.put("Caraquet", Map.of(lib, 5420, pc, 1827, grn, 330, ndp, 548, ind, 373));
    ret.put("Shippagan-Lameque-Miscou", Map.of(lib, 3949, pc, 4048, ndp, 578, ind, 178));
    ret.put("Tracadie-Sheila", Map.of(lib, 4320, pc, 2390, grn, 390, ndp, 1213, ind, 544));
    ret.put("Miramichi Bay-Neguac", Map.of(lib, 3512, pc, 1741, grn, 349, ndp, 718, pa, 2047));
    ret.put("Miramichi", Map.of(lib, 2825, pc, 1154, grn, 189, ndp, 110, pa, 3788));
    ret.put(
        "Southwest Miramichi-Bay du Vin",
        Map.of(lib, 1909, pc, 2960, grn, 447, ndp, 97, pa, 2925, ind, 19));
    ret.put("Kent North", Map.of(lib, 3301, pc, 1112, grn, 4056, ndp, 171, ind, 194));
    ret.put("Kent South", Map.of(lib, 5595, pc, 1848, grn, 1304, ndp, 436));
    ret.put("Shediac Bay-Dieppe", Map.of(lib, 6162, pc, 1353, grn, 906, ndp, 764));
    ret.put("Shediac-Beaubassin-Cap-Pele", Map.of(lib, 5919, pc, 2081, grn, 888, ndp, 428));
    ret.put("Memramcook-Tantramar", Map.of(lib, 3137, pc, 1518, grn, 3148, ndp, 410));
    ret.put("Dieppe", Map.of(lib, 5173, pc, 998, ndp, 1057));
    ret.put("Moncton East", Map.of(lib, 3626, pc, 2771, grn, 925, ndp, 424));
    ret.put("Moncton Centre", Map.of(lib, 2698, pc, 982, grn, 771, ndp, 229, pa, 309, ind, 1200));
    ret.put("Moncton South", Map.of(lib, 3099, pc, 2090, grn, 628, ndp, 249, pa, 466));
    ret.put("Moncton Northwest", Map.of(lib, 2963, pc, 3186, grn, 437, ndp, 297, pa, 875));
    ret.put("Moncton Southwest", Map.of(lib, 2667, pc, 2920, grn, 907, ndp, 503));
    ret.put("Riverview", Map.of(lib, 2053, pc, 3701, grn, 542, ndp, 249, pa, 1005));
    ret.put("Albert", Map.of(lib, 1775, pc, 3479, grn, 870, ndp, 375, pa, 1546, ind, 87));
    ret.put(
        "Gagetown-Petitcodiac",
        Map.of(lib, 1153, pc, 3674, grn, 1097, ndp, 165, pa, 1892, ind, 56));
    ret.put(
        "Sussex-Fundy-St. Martins",
        Map.of(lib, 1212, pc, 3816, grn, 505, ndp, 254, pa, 1874, ind, 54));
    ret.put("Hampton", Map.of(lib, 1454, pc, 3702, grn, 743, ndp, 384, pa, 1246));
    ret.put("Quispamsis", Map.of(lib, 2078, pc, 4691, grn, 445, ndp, 239, pa, 795));
    ret.put("Rothesay", Map.of(lib, 2001, pc, 3542, grn, 571, ndp, 251, pa, 722));
    ret.put("Saint John East", Map.of(lib, 1775, pc, 3017, grn, 373, ndp, 402, pa, 1047));
    ret.put("Portland-Simonds", Map.of(lib, 1703, pc, 3168, grn, 435, ndp, 449, ind, 191));
    ret.put("Saint John Harbour", Map.of(lib, 1865, pc, 1855, grn, 721, ndp, 836, pa, 393));
    ret.put("Saint John Lancaster", Map.of(lib, 1727, pc, 3001, grn, 582, ndp, 414, pa, 922));
    ret.put("Kings Centre", Map.of(lib, 1785, pc, 3267, grn, 731, ndp, 342, pa, 1454));
    ret.put(
        "Fundy-The Isles-Saint John West",
        Map.of(lib, 2422, pc, 3808, grn, 469, ndp, 203, pa, 1104));
    ret.put("Saint Croix", Map.of(lib, 2436, pc, 3249, grn, 1047, ndp, 89, pa, 1466));
    ret.put(
        "Oromocto-Lincoln-Fredericton", Map.of(lib, 2306, pc, 2399, grn, 903, ndp, 159, pa, 1741));
    ret.put(
        "Fredericton-Grand Lake",
        Map.of(lib, 955, pc, 2433, grn, 472, ndp, 114, pa, 4799, ind, 19));
    ret.put(
        "New Maryland-Sunbury", Map.of(lib, 2210, pc, 3844, grn, 902, ndp, 143, pa, 2214, ind, 14));
    ret.put("Fredericton South", Map.of(lib, 1525, pc, 1042, grn, 4273, ndp, 132, pa, 616));
    ret.put("Fredericton North", Map.of(lib, 2443, pc, 2182, grn, 1313, ndp, 139, pa, 1651));
    ret.put(
        "Fredericton-York", Map.of(lib, 1652, pc, 2777, grn, 1393, ndp, 103, pa, 3033, ind, 34));
    ret.put("Fredericton West-Hanwell", Map.of(lib, 2404, pc, 2739, grn, 1490, ndp, 171, pa, 1803));
    ret.put("Carleton-York", Map.of(lib, 1556, pc, 3118, grn, 837, ndp, 255, pa, 2583, ind, 40));
    ret.put("Carleton", Map.of(lib, 1197, pc, 2982, grn, 1247, ndp, 82, pa, 2026));
    ret.put("Carleton-Victoria", Map.of(lib, 3116, pc, 2872, grn, 503, ndp, 114, pa, 960, ind, 58));
    ret.put("Victoria-La Vallee", Map.of(lib, 3570, pc, 3212, grn, 468, ndp, 307));
    ret.put("Edmunston-Madawaska Centre", Map.of(lib, 4668, pc, 1437, grn, 702, ndp, 206));
    ret.put("Madawaska Les Lacs-Edmunston", Map.of(lib, 4191, pc, 1826, grn, 945, ndp, 156));
    return ret;
  }

  private static Map<String, PartyResult> nbCurrResult() {
    var ret = new TreeMap<String, PartyResult>();
    ret.put("Restigouche West", PartyResult.elected(lib));
    ret.put("Campbellton-Dalhousie", PartyResult.elected(lib));
    ret.put("Restigouche-Chaleur", PartyResult.elected(lib));
    ret.put("Bathurst West-Beresford", PartyResult.elected(lib));
    ret.put("Bathurst East-Nepisguit-Saint-Isidore", PartyResult.elected(lib));
    ret.put("Caraquet", PartyResult.elected(lib));
    ret.put("Shippagan-Lameque-Miscou", PartyResult.elected(lib));
    ret.put("Tracadie-Sheila", PartyResult.elected(lib));
    ret.put("Miramichi Bay-Neguac", PartyResult.elected(lib));
    ret.put("Miramichi", PartyResult.elected(pa));
    ret.put("Southwest Miramichi-Bay du Vin", PartyResult.elected(pc));
    ret.put("Kent North", PartyResult.elected(grn));
    ret.put("Kent South", PartyResult.elected(lib));
    ret.put("Shediac Bay-Dieppe", PartyResult.elected(lib));
    ret.put("Shediac-Beaubassin-Cap-Pele", PartyResult.elected(lib));
    ret.put("Memramcook-Tantramar", PartyResult.elected(grn));
    ret.put("Dieppe", PartyResult.elected(lib));
    ret.put("Moncton East", PartyResult.elected(pc));
    ret.put("Moncton Centre", PartyResult.elected(lib));
    ret.put("Moncton South", PartyResult.elected(pc));
    ret.put("Moncton Northwest", PartyResult.elected(pc));
    ret.put("Moncton Southwest", PartyResult.elected(pc));
    ret.put("Riverview", PartyResult.elected(pc));
    ret.put("Albert", PartyResult.elected(pc));
    ret.put("Gagetown-Petitcodiac", PartyResult.elected(pc));
    ret.put("Sussex-Fundy-St. Martins", PartyResult.elected(pc));
    ret.put("Hampton", PartyResult.elected(pc));
    ret.put("Quispamsis", PartyResult.elected(pc));
    ret.put("Rothesay", PartyResult.elected(pc));
    ret.put("Saint John East", PartyResult.elected(pc));
    ret.put("Portland-Simonds", PartyResult.elected(pc));
    ret.put("Saint John Harbour", PartyResult.elected(pc));
    ret.put("Saint John Lancaster", PartyResult.elected(pc));
    ret.put("Kings Centre", PartyResult.elected(pc));
    ret.put("Fundy-The Isles-Saint John West", PartyResult.elected(pc));
    ret.put("Saint Croix", PartyResult.elected(pc));
    ret.put("Oromocto-Lincoln-Fredericton", PartyResult.elected(pc));
    ret.put("Fredericton-Grand Lake", PartyResult.elected(pa));
    ret.put("New Maryland-Sunbury", PartyResult.elected(pc));
    ret.put("Fredericton South", PartyResult.elected(grn));
    ret.put("Fredericton North", PartyResult.elected(pc));
    ret.put("Fredericton-York", PartyResult.elected(pc));
    ret.put("Fredericton West-Hanwell", PartyResult.elected(pc));
    ret.put("Carleton-York", PartyResult.elected(pc));
    ret.put("Carleton", PartyResult.elected(pc));
    ret.put("Carleton-Victoria", PartyResult.elected(pc));
    ret.put("Victoria-La Vallee", PartyResult.elected(lib));
    ret.put("Edmunston-Madawaska Centre", PartyResult.elected(lib));
    ret.put("Madawaska Les Lacs-Edmunston", PartyResult.elected(lib));
    return ret;
  }
}
