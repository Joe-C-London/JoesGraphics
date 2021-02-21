package com.joecollins.graphics.components;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ListingFrameBuilder {

  private BarFrame barFrame = new BarFrame();

  private final List<Binding<?>> bindings = new ArrayList<>();

  public static <T> ListingFrameBuilder of(
      BindableList<T> list,
      Function<T, String> leftTextFunc,
      Function<T, String> rightTextFunc,
      Function<T, Color> colorFunc) {
    ListingFrameBuilder builder = new ListingFrameBuilder();
    BarFrame barFrame = builder.barFrame;
    barFrame.setNumBarsBinding(Binding.sizeBinding(list));
    barFrame.setLeftTextBinding(IndexedBinding.propertyBinding(list, leftTextFunc::apply));
    barFrame.setRightTextBinding(IndexedBinding.propertyBinding(list, rightTextFunc::apply));
    barFrame.addSeriesBinding(
        "Item",
        IndexedBinding.propertyBinding(list, colorFunc::apply),
        IndexedBinding.propertyBinding(list, x -> 1));
    return builder;
  }

  public static <T> ListingFrameBuilder of(
      Binding<List<T>> list,
      Function<T, String> leftTextFunc,
      Function<T, String> rightTextFunc,
      Function<T, Color> colorFunc) {
    BindableList<T> bindableList = new BindableList<>();
    list.bindLegacy(bindableList::setAll);
    ListingFrameBuilder ret = of(bindableList, leftTextFunc, rightTextFunc, colorFunc);
    ret.bindings.add(list);
    return ret;
  }

  public static <T> ListingFrameBuilder ofFixedList(
      List<T> list,
      Function<T, Binding<String>> leftTextFunc,
      Function<T, Binding<String>> rightTextFunc,
      Function<T, Binding<Color>> colorFunc) {
    ListingFrameBuilder builder = new ListingFrameBuilder();
    BarFrame barFrame = builder.barFrame;
    barFrame.setNumBarsBinding(Binding.fixedBinding(list.size()));
    barFrame.setLeftTextBinding(IndexedBinding.listBinding(list, leftTextFunc::apply));
    barFrame.setRightTextBinding(IndexedBinding.listBinding(list, rightTextFunc::apply));
    barFrame.addSeriesBinding(
        "Item",
        IndexedBinding.listBinding(list, colorFunc::apply),
        IndexedBinding.listBinding(list, x -> Binding.fixedBinding(1)));
    return builder;
  }

  public ListingFrameBuilder withHeader(Binding<String> headerBinding) {
    barFrame.setHeaderBinding(headerBinding);
    return this;
  }

  public ListingFrameBuilder withSubhead(Binding<String> subheadBinding) {
    barFrame.setSubheadTextBinding(subheadBinding);
    return this;
  }

  public ListingFrameBuilder withNotes(Binding<String> notesBinding) {
    barFrame.setNotesBinding(notesBinding);
    return this;
  }

  public BarFrame build() {
    return barFrame;
  }
}
