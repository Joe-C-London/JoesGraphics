package com.joecollins.graphics.components.lowerthird;

import com.joecollins.bindings.Binding;
import java.awt.BorderLayout;

public class LowerThirdHeadlineOnly extends LowerThird {

  private HeadlinePanel headlinePanel = new HeadlinePanel();

  private Binding<String> headlineBinding = Binding.fixedBinding("");
  private Binding<String> subheadBinding = Binding.fixedBinding(null);

  public LowerThirdHeadlineOnly() {
    add(headlinePanel, BorderLayout.CENTER);
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
}
