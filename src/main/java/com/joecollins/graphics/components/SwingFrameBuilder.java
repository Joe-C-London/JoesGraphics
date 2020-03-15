package com.joecollins.graphics.components;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.Binding;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ComparatorUtils;

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

  private SwingFrame swingFrame =
      new SwingFrame() {
        @Override
        public void dispose() {
          super.dispose();
          bindings.forEach(Binding::unbind);
        }
      };
  private SwingProperties props = new SwingProperties();
  private Color neutralColor = Color.BLACK;

  private final List<Binding<?>> bindings = new ArrayList<>();

  private enum SingletonProperty {
    ALL
  }

  public static SwingFrameBuilder prevCurr(
      Binding<? extends Map<Party, ? extends Number>> prevBinding,
      Binding<? extends Map<Party, ? extends Number>> currBinding,
      Comparator<Party> partyOrder) {
    return prevCurr(prevBinding, currBinding, partyOrder, false);
  }

  public static SwingFrameBuilder prevCurr(
      Binding<? extends Map<Party, ? extends Number>> prevBinding,
      Binding<? extends Map<Party, ? extends Number>> currBinding,
      Comparator<Party> partyOrder,
      boolean normalised) {
    var prevCurr = new BindablePrevCurrPct();
    Function<Map<Party, ? extends Number>, Map<Party, Double>> toPctFunc =
        map -> {
          double total =
              normalised ? 1 : map.values().stream().mapToDouble(Number::doubleValue).sum();
          return map.entrySet().stream()
              .collect(
                  Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue() / total));
        };
    prevBinding.bind(map -> prevCurr.setPrevPct(toPctFunc.apply(map)));
    currBinding.bind(map -> prevCurr.setCurrPct(toPctFunc.apply(map)));
    SwingFrameBuilder ret =
        basic(
                Binding.propertyBinding(prevCurr, Function.identity(), SingletonProperty.ALL),
                p -> {
                  if (p.fromParty == null || p.toParty == null) {
                    return Color.LIGHT_GRAY;
                  }
                  return ComparatorUtils.max(p.fromParty, p.toParty, partyOrder).getColor();
                },
                p -> {
                  if (p.fromParty == null || p.toParty == null) {
                    return Color.LIGHT_GRAY;
                  }
                  return ComparatorUtils.min(p.fromParty, p.toParty, partyOrder).getColor();
                },
                p -> {
                  if (p.fromParty == null || p.toParty == null) {
                    return 0.0;
                  }
                  return p.swing * Math.signum(partyOrder.compare(p.toParty, p.fromParty));
                },
                p -> {
                  if (p.fromParty == null || p.toParty == null) {
                    return "NOT AVAILABLE";
                  }
                  if (p.swing == 0) {
                    return "NO SWING";
                  }
                  return new DecimalFormat("0.0%").format(p.swing)
                      + " SWING "
                      + p.fromParty.getAbbreviation()
                      + " TO "
                      + p.toParty.getAbbreviation();
                })
            .withRange(Binding.fixedBinding(0.1))
            .withNeutralColor(Binding.fixedBinding(Color.LIGHT_GRAY));
    ret.bindings.add(prevBinding);
    ret.bindings.add(currBinding);
    return ret;
  }

  public static SwingFrameBuilder prevCurrNormalised(
      Binding<? extends Map<Party, Double>> prevBinding,
      Binding<? extends Map<Party, Double>> currBinding,
      Comparator<Party> partyOrder) {
    return prevCurr(prevBinding, currBinding, partyOrder, true);
  }

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
    builder.bindings.add(binding);
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
    bindings.add(neutralColorBinding);
    return this;
  }

  public SwingFrameBuilder withHeader(Binding<String> headerBinding) {
    swingFrame.setHeaderBinding(headerBinding);
    return this;
  }

  public SwingFrame build() {
    return swingFrame;
  }

  private static class BindablePrevCurrPct extends Bindable {

    private Map<Party, Double> prevPct = new HashMap<>();
    private Map<Party, Double> currPct = new HashMap<>();

    private Party fromParty = null;
    private Party toParty = null;
    private double swing = 0;

    void setPrevPct(Map<Party, Double> prevPct) {
      this.prevPct = prevPct;
      setProperties();
    }

    void setCurrPct(Map<Party, Double> currPct) {
      this.currPct = currPct;
      setProperties();
    }

    void setProperties() {
      fromParty =
          prevPct.entrySet().stream()
              .max(Comparator.comparingDouble(Map.Entry::getValue))
              .map(Map.Entry::getKey)
              .orElse(null);
      toParty =
          currPct.entrySet().stream()
              .filter(p -> !p.getKey().equals(fromParty))
              .max(Comparator.comparingDouble(Map.Entry::getValue))
              .map(Map.Entry::getKey)
              .orElse(null);
      if (fromParty != null && toParty != null) {
        double fromSwing =
            currPct.getOrDefault(fromParty, 0.0) - prevPct.getOrDefault(fromParty, 0.0);
        double toSwing = currPct.getOrDefault(toParty, 0.0) - prevPct.getOrDefault(toParty, 0.0);
        swing = (toSwing - fromSwing) / 2;
      }
      if (swing < 0) {
        swing *= -1;
        Party temp = fromParty;
        fromParty = toParty;
        toParty = temp;
      }
      onPropertyRefreshed(SingletonProperty.ALL);
    }
  }
}
