package com.joecollins.graphics.components.lowerthird;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JPanel;

public class LowerThirdHeadlineAndSummary extends LowerThird {

  private HeadlinePanel headlinePanel = new HeadlinePanel();
  private SummaryWithLabels partySummary = new SummaryWithLabels();

  private Binding<String> headlineBinding = Binding.fixedBinding("");
  private Binding<String> subheadBinding = Binding.fixedBinding(null);

  private Binding<Integer> numSummaryEntriesBinding = () -> 0;
  private IndexedBinding<SummaryWithLabels.Entry> summaryEntryBinding =
      IndexedBinding.emptyBinding();

  public LowerThirdHeadlineAndSummary() {
    JPanel center = new JPanel();
    center.setLayout(new GridLayout(1, 2));
    add(center, BorderLayout.CENTER);

    center.add(headlinePanel);
    center.add(partySummary);
  }

  String getHeadline() {
    return headlinePanel.getHeadline();
  }

  public void setHeadlineBinding(Binding<String> headlineBinding) {
    this.headlineBinding.unbind();
    this.headlineBinding = headlineBinding;
    this.headlineBinding.bind(headlinePanel::setHeadline);
  }

  String getSubhead() {
    return headlinePanel.getSubhead();
  }

  public void setSubheadBinding(Binding<String> subheadBinding) {
    this.subheadBinding.unbind();
    this.subheadBinding = subheadBinding;
    this.subheadBinding.bind(headlinePanel::setSubhead);
  }

  public void setNumSummaryEntriesBinding(Binding<Integer> numEntriesBinding) {
    this.partySummary.setNumEntriesBinding(numEntriesBinding);
  }

  int getNumSummaryEntries() {
    return this.partySummary.getNumEntries();
  }

  public void setSummaryEntriesBinding(IndexedBinding<SummaryWithLabels.Entry> entriesBinding) {
    this.partySummary.setEntriesBinding(entriesBinding);
  }

  Color getEntryColor(int index) {
    return this.partySummary.getEntryColor(index);
  }

  String getEntryLabel(int index) {
    return this.partySummary.getEntryLabel(index);
  }

  String getEntryValue(int index) {
    return this.partySummary.getEntryValue(index);
  }
}
