package com.joecollins.graphics.screens.uk.labour;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.GenericWindow;
import com.joecollins.graphics.components.BarFrame;
import com.joecollins.graphics.components.BarFrameBuilder;
import com.joecollins.graphics.components.lowerthird.LowerThird;
import com.joecollins.graphics.components.lowerthird.LowerThirdHeadlineOnly;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class LabourLeadershipScreen extends JPanel {

  private static final Color LABOUR_COLOR = new Color(0xdc241f);

  public static void main(String[] args) {
    new GenericWindow<>(new LabourLeadershipScreen()).setVisible(true);
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
    label.setBorder(new EmptyBorder(5, 0, -5, 0));
    label.setHorizontalAlignment(JLabel.CENTER);
    return label;
  }

  private BarFrame leaderBarFrame() {
    Map<String, Integer> candidates = new HashMap<>();
    candidates.put("REBECCA LONG-BAILEY", 124);
    candidates.put("LISA NANDY", 54);
    candidates.put("KEIR STARMER", 281);
    candidates.put("EMILY THORNBERRY", 20);

    return createFrameForCandidates(candidates, "LABOUR LEADER");
  }

  private BarFrame deputyBarFrame() {
    Map<String, Integer> candidates = new HashMap<>();
    candidates.put("ROSENA ALLIN KHAN", 42);
    candidates.put("RICHARD BURGON", 57);
    candidates.put("DAWN BUTLER", 63);
    candidates.put("IAN MURRAY", 47);
    candidates.put("ANGELA RAYNER", 272);

    return createFrameForCandidates(candidates, "LABOUR DEPUTY LEADER");
  }

  private BarFrame createFrameForCandidates(Map<String, Integer> candidates, String header) {
    return BarFrameBuilder.basic(
            Binding.fixedBinding(candidates), Function.identity(), s -> LABOUR_COLOR)
        .withHeader(Binding.fixedBinding(header))
        .withSubhead(Binding.fixedBinding("CONSTITUENCY PARTY NOMINATIONS"))
        .withNotes(Binding.fixedBinding("SOURCE: Labour Party"))
        .withMax(Binding.fixedBinding(326))
        .withTarget(Binding.fixedBinding(33), n -> n + " NOMINATIONS TO ADVANCE")
        .build();
  }

  private LowerThird lowerThird() {
    LowerThirdHeadlineOnly lowerThird = new LowerThirdHeadlineOnly();
    lowerThird.setPlaceBinding(Binding.fixedBinding("WESTMINSTER"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("Europe/London")));
    lowerThird.setHeadlineBinding(Binding.fixedBinding("TWO DAYS UNTIL CLP NOMINATIONS CLOSE"));
    lowerThird.setSubheadBinding(
        Binding.fixedBinding(
            "Emily Thornberry the only candidate yet to qualify for the final ballot"));
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(
            LowerThird.createImage("LABOUR LEADERSHIP 2020", Color.WHITE, LABOUR_COLOR)));
    return lowerThird;
  }
}
