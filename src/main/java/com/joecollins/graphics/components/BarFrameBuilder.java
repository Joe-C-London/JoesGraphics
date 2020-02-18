package com.joecollins.graphics.components;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.awt.Shape;
import java.util.Comparator;
import java.util.List;
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
      onPropertyRefreshed(Property.MIN);
      onPropertyRefreshed(Property.MAX);
    }

    public void setLowest(Number lowest) {
      this.lowest = lowest;
      onPropertyRefreshed(Property.MIN);
      onPropertyRefreshed(Property.MAX);
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
      Function<? super Number, String> valueLabelFunc) {
    return basic(binding, labelFunc, colorFunc, Function.identity(), valueLabelFunc);
  }

  public static <T, U> BarFrameBuilder basic(
      Binding<? extends Map<? extends T, ? extends U>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super U, ? extends Number> valueFunc,
      Function<? super U, String> valueLabelFunc) {
    return basic(binding, labelFunc, colorFunc, valueFunc, valueLabelFunc, valueFunc);
  }

  public static <T, U> BarFrameBuilder basic(
      Binding<? extends Map<? extends T, ? extends U>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super U, ? extends Number> valueFunc,
      Function<? super U, String> valueLabelFunc,
      Function<? super U, ? extends Number> sortFunc) {
    return basicWithShapes(
        binding, labelFunc, colorFunc, valueFunc, valueLabelFunc, x -> null, sortFunc);
  }

  public static <T, U> BarFrameBuilder basicWithShapes(
      Binding<? extends Map<? extends T, ? extends U>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super U, ? extends Number> valueFunc,
      Function<? super U, String> valueLabelFunc,
      Function<? super U, ? extends Shape> shapeFunc) {
    return basicWithShapes(
        binding, labelFunc, colorFunc, valueFunc, valueLabelFunc, shapeFunc, valueFunc);
  }

  public static <T, U> BarFrameBuilder basicWithShapes(
      Binding<? extends Map<? extends T, ? extends U>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super U, ? extends Number> valueFunc,
      Function<? super U, String> valueLabelFunc,
      Function<? super U, ? extends Shape> shapeFunc,
      Function<? super U, ? extends Number> sortFunc) {
    BarFrameBuilder builder = new BarFrameBuilder();
    BarFrame barFrame = builder.barFrame;
    RangeFinder rangeFinder = builder.rangeFinder;

    class BarEntry {
      final String label;
      final Color color;
      final Number value;
      final String valueLabel;
      final Shape shape;

      BarEntry(String label, Color color, Number value, String valueLabel, Shape shape) {
        this.label = label;
        this.color = color;
        this.value = value;
        this.valueLabel = valueLabel;
        this.shape = shape;
      }
    }

    BindableList<BarEntry> entries = new BindableList<>();
    barFrame.setNumBarsBinding(Binding.sizeBinding(entries));
    barFrame.setLeftTextBinding(IndexedBinding.propertyBinding(entries, e -> e.label));
    barFrame.setRightTextBinding(IndexedBinding.propertyBinding(entries, e -> e.valueLabel));
    barFrame.setLeftIconBinding(IndexedBinding.propertyBinding(entries, e -> e.shape));
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
                    .sorted(
                        Comparator.<Map.Entry<? extends T, ? extends U>>comparingDouble(
                                e -> sortFunc.apply(e.getValue()).doubleValue())
                            .reversed())
                    .map(
                        e ->
                            new BarEntry(
                                labelFunc.apply(e.getKey()),
                                colorFunc.apply(e.getKey()),
                                valueFunc.apply(e.getValue()),
                                valueLabelFunc.apply(e.getValue()),
                                shapeFunc.apply(e.getValue())))
                    .collect(Collectors.toList()));
            rangeFinder.setHighest(
                map.values().stream()
                    .map(valueFunc)
                    .mapToDouble(Number::doubleValue)
                    .reduce(0, Math::max));
            rangeFinder.setLowest(
                map.values().stream()
                    .map(valueFunc)
                    .mapToDouble(Number::doubleValue)
                    .reduce(0, Math::min));
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

  public BarFrameBuilder withWingspan(Binding<? extends Number> wingspanBinding) {
    wingspanBinding.bind(
        range -> {
          Function<RangeFinder, Double> f =
              rf ->
                  Math.max(
                      range.doubleValue(),
                      Math.max(
                          Math.abs(rf.lowest.doubleValue()), Math.abs(rf.highest.doubleValue())));
          rangeFinder.setMinFunction(rf -> -f.apply(rf));
          rangeFinder.setMaxFunction(rf -> +f.apply(rf));
        });
    return this;
  }

  public <T extends Number> BarFrameBuilder withTarget(
      Binding<T> targetBinding, Function<? super T, String> labelFunc) {
    targetBinding.bind(
        target -> {
          barFrame.setNumLinesBinding(Binding.fixedBinding(1));
          barFrame.setLineLevelsBinding(IndexedBinding.listBinding(target));
          barFrame.setLineLabelsBinding(IndexedBinding.listBinding(labelFunc.apply(target)));
        });
    return this;
  }

  public <T extends Number> BarFrameBuilder withLines(
      BindableList<T> lines, Function<? super T, String> labelFunc) {
    barFrame.setNumLinesBinding(Binding.sizeBinding(lines));
    barFrame.setLineLevelsBinding(IndexedBinding.propertyBinding(lines, Function.identity()));
    barFrame.setLineLabelsBinding(IndexedBinding.propertyBinding(lines, labelFunc::apply));
    return this;
  }

  public <T extends Number> BarFrameBuilder withLines(
      Binding<List<T>> linesBinding, Function<? super T, String> labelFunc) {
    linesBinding.bind(
        lines -> {
          barFrame.setNumLinesBinding(Binding.fixedBinding(lines.size()));
          barFrame.setLineLevelsBinding(IndexedBinding.listBinding(lines.toArray(new Number[0])));
          barFrame.setLineLabelsBinding(
              IndexedBinding.listBinding(lines.stream().map(labelFunc).toArray(String[]::new)));
        });
    return this;
  }

  public BarFrame build() {
    return barFrame;
  }
}
