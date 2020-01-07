package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.ShapefileReader;
import java.awt.Color;
import java.awt.Shape;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.junit.Test;

public class MapFrameTest {

  @Test
  public void testBindShapes() throws IOException {
    BindableList<MapEntry> shapes = loadShapes();

    MapFrame mapFrame = new MapFrame();
    mapFrame.setNumShapesBinding(Binding.sizeBinding(shapes));
    mapFrame.setShapeBinding(IndexedBinding.propertyBinding(shapes, MapEntry::getShape, "Shape"));
    mapFrame.setColorBinding(IndexedBinding.propertyBinding(shapes, MapEntry::getColor, "Color"));

    assertEquals(27, mapFrame.getNumShapes());
    assertEquals(shapes.get(0).shape, mapFrame.getShape(0));
    assertEquals(shapes.get(0).color, mapFrame.getColor(0));
  }

  @Test
  public void testRenderFull() throws IOException {
    BindableList<MapEntry> shapes = loadShapes();

    MapFrame mapFrame = new MapFrame();
    mapFrame.setHeaderBinding(Binding.fixedBinding("PEI"));
    mapFrame.setNumShapesBinding(Binding.sizeBinding(shapes));
    mapFrame.setShapeBinding(IndexedBinding.propertyBinding(shapes, MapEntry::getShape, "Shape"));
    mapFrame.setColorBinding(IndexedBinding.propertyBinding(shapes, MapEntry::getColor, "Color"));
    mapFrame.setSize(256, 128);

    compareRendering("MapFrame", "RenderFull", mapFrame);
  }

  @Test
  public void testRenderFullThin() throws IOException {
    BindableList<MapEntry> shapes = loadShapes();

    MapFrame mapFrame = new MapFrame();
    mapFrame.setHeaderBinding(Binding.fixedBinding("PEI"));
    mapFrame.setNumShapesBinding(Binding.sizeBinding(shapes));
    mapFrame.setShapeBinding(IndexedBinding.propertyBinding(shapes, MapEntry::getShape, "Shape"));
    mapFrame.setColorBinding(IndexedBinding.propertyBinding(shapes, MapEntry::getColor, "Color"));
    mapFrame.setSize(64, 128);

    compareRendering("MapFrame", "RenderFullThin", mapFrame);
  }

  private BindableList<MapEntry> loadShapes() throws IOException {
    URL peiMap =
        MapFrameTest.class
            .getClassLoader()
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp");
    Map<Integer, Shape> shapesByDistrict =
        ShapefileReader.readShapes(peiMap, "DIST_NO", Integer.class);
    BindableList<MapEntry> shapes = new BindableList<>();
    shapesByDistrict.forEach(
        (district, shape) -> {
          Color color;
          switch (district) {
            case 4:
            case 2:
            case 3:
            case 7:
            case 1:
            case 6:
            case 19:
            case 15:
            case 20:
            case 18:
            case 8:
            case 9:
            case 26:
              color = Color.BLUE;
              break;
            case 16:
            case 14:
            case 10:
            case 24:
            case 25:
            case 27:
              color = Color.RED;
              break;
            case 5:
            case 17:
            case 11:
            case 13:
            case 12:
            case 22:
            case 21:
            case 23:
              color = Color.GREEN;
              break;
            default:
              color = Color.BLACK;
              break;
          }
          shapes.add(new MapEntry(shape, color));
        });
    return shapes;
  }

  private static class MapEntry extends Bindable {
    private Shape shape;
    private Color color;

    public MapEntry(Shape shape, Color color) {
      this.shape = shape;
      this.color = color;
    }

    public Shape getShape() {
      return shape;
    }

    public void setShape(Shape shape) {
      this.shape = shape;
      onPropertyRefreshed("Shape");
    }

    public Color getColor() {
      return color;
    }

    public void setColor(Color color) {
      this.color = color;
      onPropertyRefreshed("Color");
    }
  }
}
