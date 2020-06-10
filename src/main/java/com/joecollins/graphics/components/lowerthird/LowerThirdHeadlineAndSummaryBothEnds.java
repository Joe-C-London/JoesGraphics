package com.joecollins.graphics.components.lowerthird;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.components.lowerthird.SummaryFromBothEnds.Entry;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;

public class LowerThirdHeadlineAndSummaryBothEnds extends LowerThird {

  private HeadlinePanel headlinePanel = new HeadlinePanel();
  private SummaryFromBothEnds partySummary = new SummaryFromBothEnds();

  private Binding<String> headlineBinding = Binding.fixedBinding("");
  private Binding<String> subheadBinding = Binding.fixedBinding(null);

  public LowerThirdHeadlineAndSummaryBothEnds() {
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

  String getSummaryHeader() {
    return this.partySummary.getHeadline();
  }

  public void setSummaryHeaderBinding(Binding<String> summaryHeaderBinding) {
    this.partySummary.setHeadlineBinding(summaryHeaderBinding);
  }

  public int getTotal() {
    return partySummary.getTotal();
  }

  public void setTotalBinding(Binding<Integer> totalBinding) {
    partySummary.setTotalBinding(totalBinding);
  }

  public Entry getLeft() {
    return partySummary.getLeft();
  }

  public void setLeftBinding(Binding<Entry> leftBinding) {
    partySummary.setLeftBinding(leftBinding);
  }

  public Entry getRight() {
    return partySummary.getRight();
  }

  public void setRightBinding(Binding<Entry> rightBinding) {
    partySummary.setRightBinding(rightBinding);
  }

  public Entry getMiddle() {
    return partySummary.getMiddle();
  }

  public void setMiddleBinding(Binding<Entry> middleBinding) {
    partySummary.setMiddleBinding(middleBinding);
  }
}
