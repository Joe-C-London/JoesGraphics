package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.components.FiguresFrame;
import com.joecollins.graphics.utils.StandardFont;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class FiguresScreen extends JPanel {

  private FiguresScreen(JLabel headerLabel, FiguresFrame[] frames) {
    setBackground(Color.WHITE);
    setLayout(new BorderLayout());
    add(headerLabel, BorderLayout.NORTH);

    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setLayout(new GridLayout(1, 0, 5, 5));
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    add(panel, BorderLayout.CENTER);

    for (FiguresFrame frame : frames) {
      panel.add(frame);
    }
  }

  public static Builder of() {
    return new Builder();
  }

  public static Section section(String sectionHeader) {
    return new Section(sectionHeader);
  }

  public static class Section {
    private final String name;
    private final List<Entry> entries = new ArrayList<>();

    public Section(String name) {
      this.name = name;
    }

    public Section withCandidate(
        Candidate candidate, String description, Binding<Party> leader, Binding<String> status) {
      Entry entry = new Entry(candidate, description);
      leader.bind(entry::setLeader);
      status.bind(entry::setStatus);
      entries.add(entry);
      return this;
    }

    public FiguresFrame createFrame() {
      FiguresFrame frame = new FiguresFrame();
      frame.setHeaderBinding(Binding.fixedBinding(name));
      frame.setNumEntriesBinding(Binding.fixedBinding(entries.size()));
      frame.setNameBinding(
          IndexedBinding.listBinding(
              entries, e -> Binding.fixedBinding(e.candidate.getName().toUpperCase())));
      frame.setColorBinding(
          IndexedBinding.listBinding(
              entries, e -> Binding.fixedBinding(e.candidate.getParty().getColor())));
      frame.setDescriptionBinding(
          IndexedBinding.listBinding(entries, e -> Binding.fixedBinding(e.description)));
      frame.setResultColorBinding(
          IndexedBinding.listBinding(
              entries,
              e ->
                  Binding.propertyBinding(
                      e,
                      x -> Optional.ofNullable(x.leader).orElse(Party.OTHERS).getColor(),
                      Entry.Property.LEADER)));
      frame.setResultBinding(
          IndexedBinding.listBinding(
              entries, e -> Binding.propertyBinding(e, x -> x.status, Entry.Property.STATUS)));
      return frame;
    }
  }

  private static class Entry extends Bindable<Entry.Property> {
    private enum Property {
      LEADER,
      STATUS
    }

    private final Candidate candidate;
    private final String description;
    private Party leader;
    private String status;

    public Entry(Candidate candidate, String description) {
      this.candidate = candidate;
      this.description = description;
    }

    public void setLeader(Party leader) {
      this.leader = leader;
      onPropertyRefreshed(Property.LEADER);
    }

    public void setStatus(String status) {
      this.status = status;
      onPropertyRefreshed(Property.STATUS);
    }
  }

  public static class Builder {

    private List<Section> sections = new LinkedList<>();

    public FiguresScreen build(Binding<String> titleBinding) {
      JLabel headerLabel = new JLabel();
      headerLabel.setFont(StandardFont.readBoldFont(32));
      headerLabel.setHorizontalAlignment(JLabel.CENTER);
      headerLabel.setBorder(new EmptyBorder(5, 0, -5, 0));
      titleBinding.bind(headerLabel::setText);

      var frames = sections.stream().map(Section::createFrame).toArray(FiguresFrame[]::new);
      return new FiguresScreen(headerLabel, frames);
    }

    public Builder withSection(Section section) {
      sections.add(section);
      return this;
    }
  }
}
