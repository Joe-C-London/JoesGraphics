package com.joecollins.graphics.components;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BarFrameBuilder {

  private final BarFrame barFrame = new BarFrame();
  private final RangeFinder rangeFinder = new RangeFinder();

  private static class RangeFinder extends Bindable {

    enum Property {
      MIN,
      MAX
    };

    private Number highest = 0, lowest = 0;
    private Function<RangeFinder, Number> minFunction = rf -> rf.lowest;
    private Function<RangeFinder, Number> maxFunction = rf -> rf.highest;

    public void setHighest(Number highest) {
      this.highest = highest;
      onPropertyRefreshed(Property.MAX);
    }

    public void setLowest(Number lowest) {
      this.lowest = lowest;
      onPropertyRefreshed(Property.MIN);
    }

    public void setMinFunction(Function<RangeFinder, Number> minFunction) {
      this.minFunction = minFunction;
      onPropertyRefreshed(Property.MIN);
    }

    public void setMaxFunction(Function<RangeFinder, Number> maxFunction) {
      this.maxFunction = maxFunction;
      onPropertyRefreshed(Property.MAX);
    }
  }

  public static <T> BarFrameBuilder basic(
      Binding<? extends Map<? extends T, ? extends Number>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc) {
    return basic(binding, labelFunc, colorFunc, Number::toString);
  }

  public static <T> BarFrameBuilder basic(
      Binding<? extends Map<? extends T, ? extends Number>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      DecimalFormat decimalFormat) {
    return basic(binding, labelFunc, colorFunc, decimalFormat::format);
  }

  public static <T> BarFrameBuilder basic(
      Binding<? extends Map<? extends T, ? extends Number>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super Number, String> valueFunc) {
    BarFrameBuilder builder = new BarFrameBuilder();
    BarFrame barFrame = builder.barFrame;
    RangeFinder rangeFinder = builder.rangeFinder;

    class BarEntry {
      final String label;
      final Color color;
      final Number value;

      BarEntry(String label, Color color, Number value) {
        this.label = label;
        this.color = color;
        this.value = value;
      }
    }

    BindableList<BarEntry> entries = new BindableList<>();
    barFrame.setNumBarsBinding(Binding.sizeBinding(entries));
    barFrame.setLeftTextBinding(IndexedBinding.propertyBinding(entries, e -> e.label));
    barFrame.setRightTextBinding(
        IndexedBinding.propertyBinding(entries, e -> valueFunc.apply(e.value)));
    barFrame.setMinBinding(
        Binding.propertyBinding(
            rangeFinder, rf -> rf.minFunction.apply(rf), RangeFinder.Property.MIN));
    barFrame.setMaxBinding(
        Binding.propertyBinding(
            rangeFinder, rf -> rf.maxFunction.apply(rf), RangeFinder.Property.MAX));
    barFrame.addSeriesBinding(
        "Main",
        IndexedBinding.propertyBinding(entries, e -> e.color),
        IndexedBinding.propertyBinding(entries, e -> e.value));
    binding.bind(
        map -> {
          if (map == null) {
            entries.clear();
            rangeFinder.setLowest(0);
            rangeFinder.setHighest(0);
          } else {
            entries.setAll(
                map.entrySet().stream()
                    .map(
                        e ->
                            new BarEntry(
                                labelFunc.apply(e.getKey()),
                                colorFunc.apply(e.getKey()),
                                e.getValue()))
                    .sorted(
                        Comparator.<BarEntry, Double>comparing(e -> e.value.doubleValue())
                            .reversed())
                    .collect(Collectors.toList()));
            rangeFinder.setHighest(
                map.values().stream().mapToDouble(Number::doubleValue).reduce(0, Math::max));
            rangeFinder.setLowest(
                map.values().stream().mapToDouble(Number::doubleValue).reduce(0, Math::min));
          }
        });
    return builder;
  }

  public BarFrameBuilder withHeader(Binding<String> headerBinding) {
    barFrame.setHeaderBinding(headerBinding);
    return this;
  }

  public BarFrameBuilder withSubhead(Binding<String> subheadBinding) {
    barFrame.setSubheadTextBinding(subheadBinding);
    return this;
  }

  public BarFrameBuilder withNotes(Binding<String> notesBinding) {
    barFrame.setNotesBinding(notesBinding);
    return this;
  }

  public BarFrameBuilder withBorder(Binding<Color> borderColorBinding) {
    barFrame.setBorderColorBinding(borderColorBinding);
    return this;
  }

  public BarFrameBuilder withSubheadColor(Binding<Color> subheadColorBinding) {
    barFrame.setSubheadColorBinding(subheadColorBinding);
    return this;
  }

  public BarFrameBuilder withMax(Binding<? extends Number> maxBinding) {
    rangeFinder.setMinFunction(rf -> 0);
    maxBinding.bind(
        max ->
            rangeFinder.setMaxFunction(
                rf -> Math.max(max.doubleValue(), rf.highest.doubleValue())));
    return this;
  }

  public <T extends Number> BarFrameBuilder withTarget(
      Binding<T> targetBinding, Function<T, String> labelFunc) {
    targetBinding.bind(
        target -> {
          barFrame.setNumLinesBinding(Binding.fixedBinding(1));
          barFrame.setLineLevelsBinding(IndexedBinding.listBinding(target));
          barFrame.setLineLabelsBinding(IndexedBinding.listBinding(labelFunc.apply(target)));
        });
    return this;
  }

  public BarFrame build() {
    return barFrame;
  }
}
