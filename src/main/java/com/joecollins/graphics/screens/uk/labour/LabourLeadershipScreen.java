package com.joecollins.graphics.screens.uk.labour;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.GenericWindow;
import com.joecollins.graphics.components.BarFrame;
import com.joecollins.graphics.components.lowerthird.LowerThird;
import com.joecollins.graphics.components.lowerthird.LowerThirdHeadlineOnly;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.ZoneId;
import java.util.Comparator;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class LabourLeadershipScreen extends JPanel {

  private static final Color LABOUR_COLOR = new Color(0xdc241f);

  public static void main(String[] args) {
    new GenericWindow(new LabourLeadershipScreen()).setVisible(true);
  }

  public LabourLeadershipScreen() {
    setBackground(Color.WHITE);
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(1024, 512));
    add(headerLabel(), BorderLayout.NORTH);
    add(lowerThird(), BorderLayout.SOUTH);
    JPanel center = new JPanel();
    center.setBackground(Color.WHITE);
    center.setBorder(new EmptyBorder(5, 5, 5, 5));
    center.setLayout(new GridLayout(1, 0, 5, 5));
    center.add(leaderBarFrame());
    center.add(deputyBarFrame());
    add(center, BorderLayout.CENTER);
  }

  private JLabel headerLabel() {
    JLabel label = new JLabel("LABOUR LEADERSHIP NOMINATIONS");
    label.setFont(StandardFont.readBoldFont(32));
    label.setHorizontalAlignment(JLabel.CENTER);
    return label;
  }

  private BarFrame leaderBarFrame() {
    BindableList<Candidate> candidates = new BindableList<>();
    candidates.add(new Candidate("CLIVE LEWIS", 0));
    candidates.add(new Candidate("REBECCA LONG-BAILEY", 33));
    candidates.add(new Candidate("LISA NANDY", 31));
    candidates.add(new Candidate("JESS PHILLIPS", 23));
    candidates.add(new Candidate("KEIR STARMER", 88));
    candidates.add(new Candidate("EMILY THORNBERRY", 23));

    return createFrameForCandidates(candidates, "LABOUR LEADER");
  }

  private BarFrame deputyBarFrame() {
    BindableList<Candidate> candidates = new BindableList<>();
    candidates.add(new Candidate("ROSENA ALLIN KHAN", 23));
    candidates.add(new Candidate("RICHARD BURGON", 22));
    candidates.add(new Candidate("DAWN BUTLER", 29));
    candidates.add(new Candidate("IAN MURRAY", 34));
    candidates.add(new Candidate("ANGELA RAYNER", 88));

    return createFrameForCandidates(candidates, "LABOUR DEPUTY LEADER");
  }

  private BarFrame createFrameForCandidates(BindableList<Candidate> candidates, String header) {
    candidates.sort(Comparator.<Candidate>comparingInt(c -> c.nominations).reversed());
    BarFrame frame = new BarFrame();
    frame.setHeaderBinding(Binding.fixedBinding(header));
    frame.setSubheadTextBinding(Binding.fixedBinding("MP AND MEP NOMINATIONS"));
    frame.setNotesBinding(Binding.fixedBinding("SOURCE: Labour Party"));
    frame.setMaxBinding(Binding.fixedBinding(100));
    frame.setNumLinesBinding(Binding.fixedBinding(1));
    frame.setLineLevelsBinding(IndexedBinding.singletonBinding(22));
    frame.setLineLabelsBinding(IndexedBinding.singletonBinding("22 NOMINATIONS TO ADVANCE"));
    frame.setNumBarsBinding(Binding.sizeBinding(candidates));
    frame.setLeftTextBinding(IndexedBinding.propertyBinding(candidates, c -> c.name));
    frame.setRightTextBinding(
        IndexedBinding.propertyBinding(
            candidates, c -> c.nominations == 0 ? "WITHDREW" : String.valueOf(c.nominations)));
    frame.addSeriesBinding(
        "Nominations",
        IndexedBinding.propertyBinding(candidates, c -> LABOUR_COLOR),
        IndexedBinding.propertyBinding(candidates, c -> c.nominations));
    return frame;
  }

  private LowerThird lowerThird() {
    LowerThirdHeadlineOnly lowerThird = new LowerThirdHeadlineOnly();
    lowerThird.setPlaceBinding(Binding.fixedBinding("WESTMINSTER"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("Europe/London")));
    lowerThird.setHeadlineBinding(
        Binding.fixedBinding("LABOUR LEADERSHIP MP/MEP NOMINATIONS CLOSE"));
    lowerThird.setSubheadBinding(
        Binding.fixedBinding(
            "Candidates begin to seek nominations from local parties, trade unions"));
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(
            LowerThird.createImage("LABOUR LEADERSHIP 2020", Color.WHITE, LABOUR_COLOR)));
    return lowerThird;
  }

  private class Candidate extends Bindable {
    private final String name;
    private final int nominations;

    private Candidate(String name, int nominations) {
      this.name = name;
      this.nominations = nominations;
    }
  }
}
