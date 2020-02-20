package com.joecollins.graphics.components;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.Binding;
import java.awt.Color;
import java.util.function.Function;

public class SwingFrameBuilder {

  private static class SwingProperties extends Bindable {
    enum SwingProperty {
      LEFT_COLOR,
      RIGHT_COLOR,
      VALUE,
      TEXT,
      BOTTOM_COLOR
    }

    private Color leftColor = Color.BLACK;
    private Color rightColor = Color.BLACK;
    private Number value = 0;
    private String text = "";
    private Color bottomColor = Color.BLACK;

    public void setLeftColor(Color leftColor) {
      this.leftColor = leftColor;
      onPropertyRefreshed(SwingProperty.LEFT_COLOR);
    }

    public void setRightColor(Color rightColor) {
      this.rightColor = rightColor;
      onPropertyRefreshed(SwingProperty.RIGHT_COLOR);
    }

    public void setValue(Number value) {
      this.value = value;
      onPropertyRefreshed(SwingProperty.VALUE);
    }

    public void setText(String text) {
      this.text = text;
      onPropertyRefreshed(SwingProperty.TEXT);
    }

    public void setBottomColor(Color bottomColor) {
      this.bottomColor = bottomColor;
      onPropertyRefreshed(SwingProperty.BOTTOM_COLOR);
    }
  }

  private SwingFrame swingFrame = new SwingFrame();
  private SwingProperties props = new SwingProperties();
  private Color neutralColor = Color.BLACK;

  public static <T> SwingFrameBuilder basic(
      Binding<? extends T> binding,
      Function<? super T, Color> leftColorFunc,
      Function<? super T, Color> rightColorFunc,
      Function<? super T, ? extends Number> valueFunc,
      Function<? super T, String> textFunc) {
    SwingFrameBuilder builder = new SwingFrameBuilder();
    SwingFrame swingFrame = builder.swingFrame;
    SwingProperties props = builder.props;
    swingFrame.setLeftColorBinding(
        Binding.propertyBinding(props, p -> p.leftColor, SwingProperties.SwingProperty.LEFT_COLOR));
    swingFrame.setRightColorBinding(
        Binding.propertyBinding(
            props, p -> p.rightColor, SwingProperties.SwingProperty.RIGHT_COLOR));
    swingFrame.setValueBinding(
        Binding.propertyBinding(props, p -> p.value, SwingProperties.SwingProperty.VALUE));
    swingFrame.setBottomColorBinding(
        Binding.propertyBinding(
            props, p -> p.bottomColor, SwingProperties.SwingProperty.BOTTOM_COLOR));
    swingFrame.setBottomTextBinding(
        Binding.propertyBinding(props, p -> p.text, SwingProperties.SwingProperty.TEXT));
    binding.bind(
        val -> {
          props.setLeftColor(leftColorFunc.apply(val));
          props.setRightColor(rightColorFunc.apply(val));
          props.setValue(valueFunc.apply(val));
          props.setText(textFunc.apply(val));
          if (props.value.doubleValue() > 0) {
            props.setBottomColor(leftColorFunc.apply(val));
          } else if (props.value.doubleValue() < 0) {
            props.setBottomColor(rightColorFunc.apply(val));
          } else {
            props.setBottomColor(builder.neutralColor);
          }
        });
    return builder;
  }

  public SwingFrameBuilder withRange(Binding<? extends Number> rangeBinding) {
    swingFrame.setRangeBinding(rangeBinding);
    return this;
  }

  public SwingFrameBuilder withNeutralColor(Binding<Color> neutralColorBinding) {
    neutralColorBinding.bind(
        color -> {
          this.neutralColor = color;
          if (props.value.doubleValue() == 0) {
            props.setBottomColor(color);
          }
        });
    return this;
  }

  public SwingFrameBuilder withHeader(Binding<String> headerBinding) {
    swingFrame.setHeaderBinding(headerBinding);
    return this;
  }

  public SwingFrame build() {
    return swingFrame;
  }
}
