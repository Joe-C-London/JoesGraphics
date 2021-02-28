package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.models.general.Party;
import com.joecollins.models.general.PartyResult;
import java.awt.Color;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Test;

public class BattlegroundScreenTest {

  private static Party lib = new Party("Liberal", "LIB", Color.RED);
  private static Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
  private static Party grn = new Party("Green", "GRN", Color.GREEN.darker());
  private static Party ind = new Party("Independent", "IND", Color.GRAY);
  private static Party oth = Party.OTHERS;

  @Test
  public void testSinglePartyBattleground() throws IOException {
    BindableWrapper<Map<String, Map<Party, Integer>>> prevResult =
        new BindableWrapper<>(bcPrevResult());
    BindableWrapper<Map<String, PartyResult>> currResult = new BindableWrapper<>(Map.of());
    BindableWrapper<Party> party = new BindableWrapper<>(ndp);
    BindableWrapper<Integer> targetSeats = new BindableWrapper<>(30);
    BindableWrapper<Integer> defenseSeats = new BindableWrapper<>(15);
    BindableWrapper<Integer> numRows = new BindableWrapper<>(15);
    BindableWrapper<String> title = new BindableWrapper<>("NDP BATTLEGROUND");

    Function<String, String> nameShortener = String::toUpperCase;
    BattlegroundScreen panel =
        BattlegroundScreen.singleParty(
                prevResult.getBinding(),
                currResult.getBinding(),
                nameShortener::apply,
                party.getBinding())
            .withSeatsToShow(defenseSeats.getBinding(), targetSeats.getBinding())
            .withNumRows(numRows.getBinding())
            .build(title.getBinding());
    panel.setSize(1024, 512);
    compareRendering("BattlegroundScreen", "Basic-SingleParty-1", panel);

    party.setValue(lib);
    targetSeats.setValue(15);
    defenseSeats.setValue(30);
    title.setValue("LIBERAL BATTLEGROUND");
    compareRendering("BattlegroundScreen", "Basic-SingleParty-2", panel);

    currResult.setValue(bcCurrResult());
    compareRendering("BattlegroundScreen", "Basic-SingleParty-3", panel);

    party.setValue(grn);
    targetSeats.setValue(30);
    defenseSeats.setValue(5);
    title.setValue("GREEN BATTLEGROUND");
    compareRendering("BattlegroundScreen", "Basic-SingleParty-4", panel);

    party.setValue(ndp);
    targetSeats.setValue(30);
    defenseSeats.setValue(0);
    title.setValue("NDP TARGETS");
    compareRendering("BattlegroundScreen", "Basic-SingleParty-5", panel);

    party.setValue(lib);
    targetSeats.setValue(0);
    defenseSeats.setValue(30);
    title.setValue("LIBERAL DEFENSE");
    compareRendering("BattlegroundScreen", "Basic-SingleParty-6", panel);
  }

  @Test
  public void testSinglePartyFilteredBattleground() throws IOException {
    BindableWrapper<Map<String, Map<Party, Integer>>> prevResult =
        new BindableWrapper<>(bcPrevResult());
    BindableWrapper<Map<String, PartyResult>> currResult = new BindableWrapper<>(Map.of());
    BindableWrapper<Party> party = new BindableWrapper<>(ndp);
    BindableWrapper<Integer> targetSeats = new BindableWrapper<>(30);
    BindableWrapper<Integer> defenseSeats = new BindableWrapper<>(15);
    BindableWrapper<Integer> numRows = new BindableWrapper<>(15);
    BindableWrapper<Set<String>> filteredSeats =
        new BindableWrapper<>(
            prevResult.getValue().keySet().stream()
                .filter(k -> k.startsWith("Vancouver"))
                .collect(Collectors.toSet()));
    BindableWrapper<String> title = new BindableWrapper<>("NDP BATTLEGROUND");

    Function<String, String> nameShortener = String::toUpperCase;
    BattlegroundScreen panel =
        BattlegroundScreen.singleParty(
                prevResult.getBinding(),
                currResult.getBinding(),
                nameShortener::apply,
                party.getBinding())
            .withSeatsToShow(defenseSeats.getBinding(), targetSeats.getBinding())
            .withNumRows(numRows.getBinding())
            .withSeatFilter(filteredSeats.getBinding())
            .build(title.getBinding());
    panel.setSize(1024, 512);
    compareRendering("BattlegroundScreen", "Filtered-SingleParty-1", panel);

    currResult.setValue(bcCurrResult());
    compareRendering("BattlegroundScreen", "Filtered-SingleParty-2", panel);

    filteredSeats.setValue(Set.of());
    compareRendering("BattlegroundScreen", "Filtered-SingleParty-3", panel);

    filteredSeats.setValue(null);
    compareRendering("BattlegroundScreen", "Filtered-SingleParty-4", panel);
  }

  private static Map<String, Map<Party, Integer>> bcPrevResult() {
    Map<String, Map<Party, Integer>> ret = new TreeMap<>();
    ret.put("Nechako Lakes", Map.of(lib, 5307, ndp, 2909, grn, 878, oth, 438 + 226));
    ret.put("North Coast", Map.of(lib, 3079, ndp, 5243, grn, 826));
    ret.put("Peace River North", Map.of(lib, 9707, ndp, 973, oth, 2799 + 884 + 275));
    ret.put("Peace River South", Map.of(lib, 6634, ndp, 2102));
    ret.put("Prince George-Mackenzie", Map.of(lib, 10725, ndp, 5942, grn, 2109));
    ret.put("Prince George-Valemount", Map.of(lib, 11189, ndp, 5683, grn, 2353));
    ret.put("Skeena", Map.of(lib, 6772, ndp, 5613, oth, 580));
    ret.put("Stikine", Map.of(lib, 3531, ndp, 4748, oth, 834));
    ret.put(
        "Columbia River-Revelstoke", Map.of(lib, 6620, ndp, 5248, grn, 1708, oth, 469 + 371 + 154));
    ret.put("Kootenay East", Map.of(lib, 9666, ndp, 5069, grn, 1926, oth, 425));
    ret.put("Kootenay West", Map.of(lib, 4547, ndp, 11164, grn, 2976));
    ret.put("Nelson-Creston", Map.of(lib, 5087, ndp, 7685, grn, 5130, oth, 164 + 149));
    ret.put("Boundary-Similkameen", Map.of(lib, 9513, ndp, 7275, grn, 2274, oth, 3165));
    ret.put("Kelowna-Lake Country", Map.of(lib, 15286, ndp, 5345, grn, 4951));
    ret.put("Kelowna-Mission", Map.of(lib, 15399, ndp, 5720, grn, 3836, oth, 1976));
    ret.put("Kelowna West", Map.of(lib, 15674, ndp, 6672, grn, 3628, oth, 570));
    ret.put("Penticton", Map.of(lib, 14470, ndp, 7874, grn, 5061));
    ret.put("Shuswap", Map.of(lib, 14829, ndp, 7161, grn, 4175, oth, 410));
    ret.put("Vernon-Monashee", Map.of(lib, 13625, ndp, 8355, grn, 6139, oth, 341));
    ret.put("Cariboo-Chilcotin", Map.of(lib, 8520, ndp, 3801, grn, 2174));
    ret.put("Cariboo North", Map.of(lib, 6359, ndp, 4430, grn, 919, oth, 747));
    ret.put("Fraser-Nicola", Map.of(lib, 6597, ndp, 6005, grn, 2517, oth, 598));
    ret.put("Kamloops-North Thompson", Map.of(lib, 12001, ndp, 7538, grn, 5511, oth, 187));
    ret.put("Kamloops-South Thompson", Map.of(lib, 15465, ndp, 6072, grn, 5785, oth, 295 + 109));
    ret.put("Abbotsford-Mission", Map.of(lib, 12879, ndp, 7339, grn, 4298, oth, 644));
    ret.put("Abbotsford South", Map.of(lib, 11673, ndp, 6297, grn, 3338, oth, 942));
    ret.put("Abbotsford West", Map.of(lib, 11618, ndp, 6474, grn, 2280, oth, 516 + 149));
    ret.put("Chilliwack", Map.of(lib, 9180, ndp, 6207, grn, 3277, oth, 402));
    ret.put("Chilliwack-Kent", Map.of(lib, 11814, ndp, 7273, grn, 3335));
    ret.put("Langley", Map.of(lib, 10755, ndp, 8384, grn, 3699, oth, 1221 + 166));
    ret.put("Langley East", Map.of(lib, 16384, ndp, 8820, grn, 4968, oth, 448));
    ret.put("Maple Ridge-Mission", Map.of(lib, 10663, ndp, 10988, grn, 3467, oth, 934 + 145));
    ret.put("Maple Ridge-Pitt Meadows", Map.of(lib, 10428, ndp, 12045, grn, 3329, oth, 676 + 408));
    ret.put("Surrey-Cloverdale", Map.of(lib, 11918, ndp, 9763, grn, 3091, oth, 279));
    ret.put("Surrey-Fleetwood", Map.of(lib, 7413, ndp, 11085, grn, 2190));
    ret.put("Surrey-Green Timbers", Map.of(lib, 5056, ndp, 8945, grn, 1112, oth, 163 + 69));
    ret.put("Surrey-Guildford", Map.of(lib, 7015, ndp, 9262, grn, 1840, oth, 462));
    ret.put("Surrey-Newton", Map.of(lib, 5100, ndp, 9744, grn, 1171, oth, 988));
    ret.put("Surrey-Panorama", Map.of(lib, 10064, ndp, 12226, grn, 1620, oth, 132));
    ret.put("Surrey South", Map.of(lib, 13509, ndp, 8718, grn, 3141, oth, 634 + 311 + 140 + 67));
    ret.put("Surrey-Whalley", Map.of(lib, 5293, ndp, 10315, grn, 1893, oth, 96));
    ret.put("Surrey-White Rock", Map.of(lib, 14101, ndp, 8648, grn, 4574, oth, 950));
    ret.put("Delta North", Map.of(lib, 9319, ndp, 11465, grn, 2697));
    ret.put("Delta South", Map.of(lib, 11123, ndp, 5228, grn, 2349, ind, 6437, oth, 88));
    ret.put("Richmond North Centre", Map.of(lib, 7916, ndp, 5135, grn, 1579, oth, 336 + 117));
    ret.put("Richmond-Queensborough", Map.of(lib, 8218, ndp, 8084, grn, 2524, oth, 694 + 318));
    ret.put("Richmond South Centre", Map.of(lib, 6914, ndp, 5666, grn, 1561));
    ret.put("Richmond-Steveston", Map.of(lib, 10332, ndp, 8524, grn, 2833));
    ret.put("Burnaby-Deer Lake", Map.of(lib, 6491, ndp, 8747, grn, 2209, oth, 589 + 229));
    ret.put("Burnaby-Edmonds", Map.of(lib, 6404, ndp, 10827, grn, 2728));
    ret.put("Burnaby-Lougheed", Map.of(lib, 8391, ndp, 10911, grn, 3127, oth, 145 + 129));
    ret.put("Burnaby North", Map.of(lib, 9290, ndp, 11447, grn, 2830));
    ret.put("Coquitlam-Burke Mountain", Map.of(lib, 10388, ndp, 10301, grn, 2771));
    ret.put("Coquitlam-Maillardville", Map.of(lib, 8519, ndp, 11438, grn, 2467, oth, 175));
    ret.put("New Westminster", Map.of(lib, 5870, ndp, 14377, grn, 6939, oth, 298 + 199));
    ret.put("Port Coquitlam", Map.of(lib, 7582, ndp, 14079, grn, 3237, oth, 248 + 88));
    ret.put("Port Moody-Coquitlam", Map.of(lib, 9910, ndp, 11754, grn, 2985));
    ret.put("Vancouver-Fairview", Map.of(lib, 9436, ndp, 16035, grn, 4007, oth, 149));
    ret.put("Vancouver-False Creek", Map.of(lib, 10370, ndp, 9955, grn, 3880, oth, 213 + 91 + 90));
    ret.put("Vancouver-Fraserview", Map.of(lib, 9985, ndp, 11487, grn, 1826, oth, 179 + 174));
    ret.put("Vancouver-Hastings", Map.of(lib, 5160, ndp, 14351, grn, 4222, oth, 203));
    ret.put("Vancouver-Kensington", Map.of(lib, 7236, ndp, 12504, grn, 2580, oth, 181));
    ret.put("Vancouver-Kingsway", Map.of(lib, 5377, ndp, 12031, grn, 1848, oth, 504 + 85));
    ret.put("Vancouver-Langara", Map.of(lib, 10047, ndp, 8057, grn, 2894, oth, 172));
    ret.put(
        "Vancouver-Mount Pleasant", Map.of(lib, 3917, ndp, 15962, grn, 4136, oth, 212 + 142 + 72));
    ret.put("Vancouver-Point Grey", Map.of(lib, 8414, ndp, 14195, grn, 2604, oth, 84 + 77));
    ret.put("Vancouver-Quilchena", Map.of(lib, 12464, ndp, 6244, grn, 3301, oth, 265));
    ret.put("Vancouver-West End", Map.of(lib, 5064, ndp, 13420, grn, 3059, oth, 352 + 116));
    ret.put("North Vancouver-Lonsdale", Map.of(lib, 10373, ndp, 12361, grn, 4148, oth, 316));
    ret.put("North Vancouver-Seymour", Map.of(lib, 13194, ndp, 9808, grn, 5208, oth, 247));
    ret.put("Powell River-Sunshine Coast", Map.of(lib, 6602, ndp, 13646, grn, 6505, oth, 160));
    ret.put("West Vancouver-Capilano", Map.of(lib, 13596, ndp, 5622, grn, 4575));
    ret.put("West Vancouver-Sea to Sky", Map.of(lib, 10449, ndp, 6532, grn, 6947, oth, 186 + 143));
    ret.put("Courtenay-Comox", Map.of(lib, 10697, ndp, 10886, grn, 5351, oth, 2201));
    ret.put(
        "Cowichan Valley", Map.of(lib, 8400, ndp, 9603, grn, 11475, oth, 502 + 393 + 145 + 124));
    ret.put(
        "Mid Island-Pacific Rim", Map.of(lib, 6578, ndp, 12556, grn, 5206, oth, 878 + 298 + 86));
    ret.put("Nanaimo", Map.of(lib, 8911, ndp, 12746, grn, 5454, oth, 277));
    ret.put("Nanaimo-North Cowichan", Map.of(lib, 7380, ndp, 12275, grn, 6244, oth, 274));
    ret.put("North Island", Map.of(lib, 9148, ndp, 12255, grn, 3846, oth, 543));
    ret.put("Parksville-Qualicum", Map.of(lib, 14468, ndp, 9189, grn, 8157, oth, 245));
    ret.put("Esquimalt-Metchosin", Map.of(lib, 7055, ndp, 11816, grn, 6339, oth, 171 + 102 + 65));
    ret.put("Langford-Juan de Fuca", Map.of(lib, 6544, ndp, 13224, grn, 4795, oth, 262 + 242));
    ret.put("Oak Bay-Gordon Head", Map.of(lib, 6952, ndp, 6912, grn, 15257, oth, 67 + 58));
    ret.put("Sannich North and the Islands", Map.of(lib, 9321, ndp, 10764, grn, 14775, oth, 364));
    ret.put("Saanich South", Map.of(lib, 8716, ndp, 11912, grn, 7129, oth, 177 + 130));
    ret.put("Victoria-Beacon Hill", Map.of(lib, 4689, ndp, 16057, grn, 9194, oth, 190 + 102 + 35));
    ret.put("Victoria-Swan Lake", Map.of(lib, 4005, ndp, 13531, grn, 7491, oth, 207));
    return ret;
  }

  private static Map<String, PartyResult> bcCurrResult() {
    Map<String, PartyResult> ret = new TreeMap<>();
    ret.put("Nechako Lakes", PartyResult.elected(lib));
    ret.put("North Coast", PartyResult.elected(ndp));
    ret.put("Peace River North", PartyResult.elected(lib));
    ret.put("Peace River South", PartyResult.elected(lib));
    ret.put("Prince George-Mackenzie", PartyResult.elected(lib));
    ret.put("Prince George-Valemount", PartyResult.elected(lib));
    ret.put("Skeena", PartyResult.elected(lib));
    ret.put("Stikine", PartyResult.elected(ndp));
    ret.put("Columbia River-Revelstoke", PartyResult.leading(lib));
    ret.put("Kootenay East", PartyResult.elected(lib));
    ret.put("Kootenay West", PartyResult.elected(ndp));
    ret.put("Nelson-Creston", PartyResult.leading(ndp));
    ret.put("Boundary-Similkameen", PartyResult.leading(ndp));
    ret.put("Kelowna-Lake Country", PartyResult.elected(lib));
    ret.put("Kelowna-Mission", PartyResult.elected(lib));
    ret.put("Kelowna West", PartyResult.leading(lib));
    ret.put("Penticton", PartyResult.leading(lib));
    ret.put("Shuswap", PartyResult.elected(lib));
    ret.put("Vernon-Monashee", PartyResult.leading(ndp));
    ret.put("Cariboo-Chilcotin", PartyResult.elected(lib));
    ret.put("Cariboo North", PartyResult.leading(lib));
    ret.put("Fraser-Nicola", PartyResult.leading(lib));
    ret.put("Kamloops-North Thompson", PartyResult.leading(lib));
    ret.put("Kamloops-South Thompson", PartyResult.elected(lib));
    ret.put("Abbotsford-Mission", PartyResult.leading(ndp));
    ret.put("Abbotsford South", PartyResult.leading(lib));
    ret.put("Abbotsford West", PartyResult.leading(lib));
    ret.put("Chilliwack", PartyResult.leading(ndp));
    ret.put("Chilliwack-Kent", PartyResult.leading(ndp));
    ret.put("Langley", PartyResult.leading(ndp));
    ret.put("Langley East", PartyResult.leading(ndp));
    ret.put("Maple Ridge-Mission", PartyResult.elected(ndp));
    ret.put("Maple Ridge-Pitt Meadows", PartyResult.elected(ndp));
    ret.put("Surrey-Cloverdale", PartyResult.elected(ndp));
    ret.put("Surrey-Fleetwood", PartyResult.elected(ndp));
    ret.put("Surrey-Green Timbers", PartyResult.elected(ndp));
    ret.put("Surrey-Guildford", PartyResult.elected(ndp));
    ret.put("Surrey-Newton", PartyResult.elected(ndp));
    ret.put("Surrey-Panorama", PartyResult.elected(ndp));
    ret.put("Surrey South", PartyResult.leading(lib));
    ret.put("Surrey-Whalley", PartyResult.elected(ndp));
    ret.put("Surrey-White Rock", PartyResult.leading(lib));
    ret.put("Delta North", PartyResult.elected(ndp));
    ret.put("Delta South", PartyResult.elected(lib));
    ret.put("Richmond North Centre", PartyResult.elected(lib));
    ret.put("Richmond-Queensborough", PartyResult.leading(ndp));
    ret.put("Richmond South Centre", PartyResult.elected(ndp));
    ret.put("Richmond-Steveston", PartyResult.elected(ndp));
    ret.put("Burnaby-Deer Lake", PartyResult.elected(ndp));
    ret.put("Burnaby-Edmonds", PartyResult.elected(ndp));
    ret.put("Burnaby-Lougheed", PartyResult.elected(ndp));
    ret.put("Burnaby North", PartyResult.elected(ndp));
    ret.put("Coquitlam-Burke Mountain", PartyResult.elected(ndp));
    ret.put("Coquitlam-Maillardville", PartyResult.elected(ndp));
    ret.put("New Westminster", PartyResult.elected(ndp));
    ret.put("Port Coquitlam", PartyResult.elected(ndp));
    ret.put("Port Moody-Coquitlam", PartyResult.elected(ndp));
    ret.put("Vancouver-Fairview", PartyResult.elected(ndp));
    ret.put("Vancouver-False Creek", PartyResult.leading(ndp));
    ret.put("Vancouver-Fraserview", PartyResult.elected(ndp));
    ret.put("Vancouver-Hastings", PartyResult.elected(ndp));
    ret.put("Vancouver-Kensington", PartyResult.elected(ndp));
    ret.put("Vancouver-Kingsway", PartyResult.elected(ndp));
    ret.put("Vancouver-Langara", PartyResult.leading(lib));
    ret.put("Vancouver-Mount Pleasant", PartyResult.elected(ndp));
    ret.put("Vancouver-Point Grey", PartyResult.elected(ndp));
    ret.put("Vancouver-Quilchena", PartyResult.elected(lib));
    ret.put("Vancouver-West End", PartyResult.elected(ndp));
    ret.put("North Vancouver-Lonsdale", PartyResult.elected(ndp));
    ret.put("North Vancouver-Seymour", PartyResult.leading(ndp));
    ret.put("Powell River-Sunshine Coast", PartyResult.elected(ndp));
    ret.put("West Vancouver-Capilano", PartyResult.elected(lib));
    ret.put("West Vancouver-Sea to Sky", PartyResult.leading(lib));
    ret.put("Courtenay-Comox", PartyResult.elected(ndp));
    ret.put("Cowichan Valley", PartyResult.leading(grn));
    ret.put("Mid Island-Pacific Rim", PartyResult.elected(ndp));
    ret.put("Nanaimo", PartyResult.elected(ndp));
    ret.put("Nanaimo-North Cowichan", PartyResult.leading(ndp));
    ret.put("North Island", PartyResult.elected(ndp));
    ret.put("Parksville-Qualicum", PartyResult.leading(ndp));
    ret.put("Esquimalt-Metchosin", PartyResult.elected(ndp));
    ret.put("Langford-Juan de Fuca", PartyResult.elected(ndp));
    ret.put("Oak Bay-Gordon Head", PartyResult.elected(ndp));
    ret.put("Sannich North and the Islands", PartyResult.elected(grn));
    ret.put("Saanich South", PartyResult.elected(ndp));
    ret.put("Victoria-Beacon Hill", PartyResult.elected(ndp));
    ret.put("Victoria-Swan Lake", PartyResult.elected(ndp));
    return ret;
  }
}
