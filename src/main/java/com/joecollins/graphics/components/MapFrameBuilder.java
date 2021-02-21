package com.joecollins.graphics.components;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class MapFrameBuilder {

  private MapFrame mapFrame = new MapFrame();

  private final List<Binding<?>> bindings = new ArrayList<>();

  public static MapFrameBuilder from(BindableList<Pair<Shape, Color>> shapes) {
    MapFrameBuilder mapFrameBuilder = new MapFrameBuilder();
    MapFrame mapFrame = mapFrameBuilder.mapFrame;
    mapFrame.setNumShapesBinding(Binding.sizeBinding(shapes));
    mapFrame.setShapeBinding(IndexedBinding.propertyBinding(shapes, Pair::getLeft));
    mapFrame.setColorBinding(IndexedBinding.propertyBinding(shapes, Pair::getRight));
    return mapFrameBuilder;
  }

  public static MapFrameBuilder from(Binding<List<Pair<Shape, Color>>> shapes) {
    BindableList<Pair<Shape, Color>> list = new BindableList<>();
    shapes.bindLegacy(list::setAll);
    MapFrameBuilder ret = from(list);
    ret.bindings.add(shapes);
    return ret;
  }

  public static <T> MapFrameBuilder from(
      Binding<List<T>> itemsBinding,
      Function<T, Shape> shapeFunc,
      Function<T, Binding<Color>> colorFunc) {
    BindableList<Pair<Shape, Color>> list = new BindableList<>();
    List<Binding<Color>> bindings = new ArrayList<>();
    itemsBinding.bindLegacy(
        items -> {
          bindings.forEach(Binding::unbind);
          list.clear();
          for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            Shape shape = shapeFunc.apply(item);
            Binding<Color> colorBinding = colorFunc.apply(item);
            list.add(ImmutablePair.of(shape, colorBinding.getValue()));
            int idx = i;
            colorBinding.bindLegacy(color -> list.set(idx, ImmutablePair.of(shape, color)));
            bindings.add(colorBinding);
          }
        });
    MapFrameBuilder ret = from(list);
    ret.bindings.add(itemsBinding);
    return ret;
  }

  public MapFrameBuilder withFocus(Binding<List<Shape>> focusBinding) {
    focusBinding.bindLegacy(
        list -> {
          if (list == null) {
            list = Collections.emptyList();
          }
          Rectangle2D bounds =
              list.stream()
                  .map(Shape::getBounds2D)
                  .reduce(
                      (a, b) -> {
                        a.add(b);
                        return a;
                      })
                  .orElse(null);
          mapFrame.setFocusBoxBinding(Binding.fixedBinding(bounds));
        });
    bindings.add(focusBinding);
    return this;
  }

  public MapFrameBuilder withHeader(Binding<String> headerBinding) {
    mapFrame.setHeaderBinding(headerBinding);
    return this;
  }

  public MapFrame build() {
    return mapFrame;
  }
}
