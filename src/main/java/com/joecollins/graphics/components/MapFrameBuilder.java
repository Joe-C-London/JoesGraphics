package com.joecollins.graphics.components;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class MapFrameBuilder {

  private MapFrame mapFrame = new MapFrame();

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
    shapes.bind(list::setAll);
    return from(list);
  }

  public MapFrameBuilder withFocus(Binding<List<Shape>> focusBinding) {
    focusBinding.bind(
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
