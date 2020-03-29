package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.Test;

public class SimpleVoteViewPanelTest {

  @Test
  public void testCandidatesBasicResult() throws IOException {
    Candidate ndp =
        new Candidate("Billy Cann", new Party("New Democratic Party", "NDP", Color.ORANGE));
    Candidate pc =
        new Candidate("Cory Deagle", new Party("Progressive Conservative", "PC", Color.BLUE));
    Candidate lib = new Candidate("Daphne Griffin", new Party("Liberal", "LIB", Color.RED));
    Candidate grn =
        new Candidate("John Allen MacLean", new Party("Green", "GRN", Color.GREEN.darker()));

    LinkedHashMap<Candidate, Integer> curr = new LinkedHashMap<>();
    curr.put(ndp, 124);
    curr.put(pc, 1373);
    curr.put(lib, 785);
    curr.put(grn, 674);

    LinkedHashMap<Party, Integer> prev = new LinkedHashMap<>();
    prev.put(ndp.getParty(), 585);
    prev.put(pc.getParty(), 785);
    prev.put(lib.getParty(), 1060);
    prev.put(grn.getParty(), 106);

    BindableWrapper<LinkedHashMap<Candidate, Integer>> currentVotes = new BindableWrapper<>(curr);
    BindableWrapper<LinkedHashMap<Party, Integer>> previousVotes = new BindableWrapper<>(prev);
    BindableWrapper<String> header = new BindableWrapper<>("MONTAGUE-KILMUIR");
    BindableWrapper<String> voteHeader = new BindableWrapper<>("9 OF 9 POLLS REPORTING");
    BindableWrapper<String> voteSubhead = new BindableWrapper<>("PROJECTION: PC GAIN FROM LIB");
    BindableWrapper<String> changeHeader = new BindableWrapper<>("CHANGE SINCE 2015");
    BindableWrapper<String> swingHeader = new BindableWrapper<>("SWING SINCE 2015");
    List<Party> swingPartyOrder =
        Arrays.asList(ndp.getParty(), grn.getParty(), lib.getParty(), pc.getParty());

    SimpleVoteViewPanel panel =
        SimpleVoteViewPanel.Builder.basicCurrPrev(
                currentVotes.getBinding(),
                previousVotes.getBinding(),
                header.getBinding(),
                voteHeader.getBinding(),
                voteSubhead.getBinding(),
                changeHeader.getBinding(),
                swingHeader.getBinding(),
                swingPartyOrder)
            .build();
    panel.setSize(1024, 512);
    compareRendering("SimpleVoteViewPanel", "Basic-1", panel);
  }
}
