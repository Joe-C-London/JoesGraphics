package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.bindings.NestedBindableList;
import com.joecollins.graphics.utils.ShapefileReader;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;

public class MapFrameTest {

  @Test
  public void testBindShapes() throws IOException {
    NestedBindableList<MapEntry, MapEntry.Property> shapes = loadShapes(this::getDistrictColor);

    MapFrame mapFrame = new MapFrame();
    mapFrame.setNumShapesBinding(Binding.sizeBinding(shapes));
    mapFrame.setShapeBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getShape, MapEntry.Property.SHAPE));
    mapFrame.setColorBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getColor, MapEntry.Property.COLOR));

    assertEquals(27, mapFrame.getNumShapes());
    assertEquals(shapes.get(0).shape, mapFrame.getShape(0));
    assertEquals(shapes.get(0).color, mapFrame.getColor(0));
  }

  @Test
  public void testDefaultFocusAreaEncompassesAllShapes() throws IOException {
    NestedBindableList<MapEntry, MapEntry.Property> shapes = loadShapes(this::getDistrictColor);

    MapFrame mapFrame = new MapFrame();
    mapFrame.setNumShapesBinding(Binding.sizeBinding(shapes));
    mapFrame.setShapeBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getShape, MapEntry.Property.SHAPE));
    mapFrame.setColorBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getColor, MapEntry.Property.COLOR));

    Rectangle2D bindingBox =
        shapes.stream()
            .map(e -> new Area(e.shape))
            .collect(Area::new, Area::add, Area::add)
            .getBounds2D();
    assertEquals(bindingBox, mapFrame.getFocusBox());
  }

  @Test
  public void testFocusBox() throws IOException {
    NestedBindableList<MapEntry, MapEntry.Property> shapes = loadShapes(this::getDistrictColor);
    Rectangle2D cityBox = loadCityBox();

    MapFrame mapFrame = new MapFrame();
    mapFrame.setNumShapesBinding(Binding.sizeBinding(shapes));
    mapFrame.setShapeBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getShape, MapEntry.Property.SHAPE));
    mapFrame.setColorBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getColor, MapEntry.Property.COLOR));
    mapFrame.setFocusBoxBinding(Binding.fixedBinding(cityBox));
    assertEquals(cityBox, mapFrame.getFocusBox());
  }

  @Test
  public void testOutlines() throws IOException {
    BindableList<Shape> regions = loadRegions();

    MapFrame mapFrame = new MapFrame();
    mapFrame.setNumOutlineShapesBinding(Binding.sizeBinding(regions));
    mapFrame.setOutlineShapesBinding(IndexedBinding.listBinding(regions));
    assertEquals(4, mapFrame.getNumOutlineShapes());
    assertEquals(regions.get(0), mapFrame.getOutlineShape(0));
  }

  @Test
  public void testRenderFull() throws IOException {
    NestedBindableList<MapEntry, MapEntry.Property> shapes = loadShapes(this::getDistrictColor);

    MapFrame mapFrame = new MapFrame();
    mapFrame.setHeaderBinding(Binding.fixedBinding("PEI"));
    mapFrame.setNumShapesBinding(Binding.sizeBinding(shapes));
    mapFrame.setShapeBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getShape, MapEntry.Property.SHAPE));
    mapFrame.setColorBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getColor, MapEntry.Property.COLOR));
    mapFrame.setSize(256, 128);

    compareRendering("MapFrame", "RenderFull", mapFrame);
  }

  @Test
  public void testRenderFullThin() throws IOException {
    NestedBindableList<MapEntry, MapEntry.Property> shapes = loadShapes(this::getDistrictColor);

    MapFrame mapFrame = new MapFrame();
    mapFrame.setHeaderBinding(Binding.fixedBinding("PEI"));
    mapFrame.setNumShapesBinding(Binding.sizeBinding(shapes));
    mapFrame.setShapeBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getShape, MapEntry.Property.SHAPE));
    mapFrame.setColorBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getColor, MapEntry.Property.COLOR));
    mapFrame.setSize(64, 128);

    compareRendering("MapFrame", "RenderFullThin", mapFrame);
  }

  @Test
  public void testRenderZoomedIn() throws IOException {
    NestedBindableList<MapEntry, MapEntry.Property> shapes =
        loadShapes(i -> i >= 9 && i <= 14 ? getDistrictColor(i) : Color.GRAY);
    Rectangle2D zoomBox = loadCityBox();

    MapFrame mapFrame = new MapFrame();
    mapFrame.setHeaderBinding(Binding.fixedBinding("CHARLOTTETOWN"));
    mapFrame.setNumShapesBinding(Binding.sizeBinding(shapes));
    mapFrame.setShapeBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getShape, MapEntry.Property.SHAPE));
    mapFrame.setColorBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getColor, MapEntry.Property.COLOR));
    mapFrame.setFocusBoxBinding(Binding.fixedBinding(zoomBox));
    mapFrame.setSize(256, 128);

    compareRendering("MapFrame", "RenderZoomedIn", mapFrame);
  }

  @Test
  public void testRenderWithBorders() throws IOException {
    NestedBindableList<MapEntry, MapEntry.Property> shapes = loadShapes(this::getDistrictColor);
    Rectangle2D zoomBox = loadCityBox();
    BindableList<Shape> regions = new BindableList<>();
    regions.addAll(shapes.stream().map(MapEntry::getShape).collect(Collectors.toList()));

    MapFrame mapFrame = new MapFrame();
    mapFrame.setHeaderBinding(Binding.fixedBinding("PEI"));
    mapFrame.setNumShapesBinding(Binding.sizeBinding(shapes));
    mapFrame.setShapeBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getShape, MapEntry.Property.SHAPE));
    mapFrame.setColorBinding(
        IndexedBinding.propertyBinding(shapes, MapEntry::getColor, MapEntry.Property.COLOR));
    mapFrame.setNumOutlineShapesBinding(Binding.sizeBinding(regions));
    mapFrame.setOutlineShapesBinding(IndexedBinding.listBinding(regions));
    mapFrame.setSize(256, 128);
    compareRendering("MapFrame", "RenderBorders-1", mapFrame);

    mapFrame.setFocusBoxBinding(Binding.fixedBinding(zoomBox));
    mapFrame.setHeaderBinding(Binding.fixedBinding("CHARLOTTETOWN"));
    compareRendering("MapFrame", "RenderBorders-2", mapFrame);
  }

  private NestedBindableList<MapEntry, MapEntry.Property> loadShapes(IntFunction<Color> colorFunc)
      throws IOException {
    Map<Integer, Shape> shapesByDistrict = shapesByDistrict();
    NestedBindableList<MapEntry, MapEntry.Property> shapes = new NestedBindableList<>();
    shapesByDistrict.forEach(
        (district, shape) -> {
          Color color = colorFunc.apply(district);
          shapes.add(new MapEntry(shape, color));
        });
    return shapes;
  }

  private BindableList<Shape> loadRegions() throws IOException {
    Map<Integer, Shape> shapesByDistrict = shapesByDistrict();
    BindableList<Shape> regions = new BindableList<>();
    regions.add(
        IntStream.rangeClosed(1, 7)
            .mapToObj(shapesByDistrict::get)
            .map(Area::new)
            .collect(Area::new, Area::add, Area::add));
    regions.add(
        IntStream.rangeClosed(9, 14)
            .mapToObj(shapesByDistrict::get)
            .map(Area::new)
            .collect(Area::new, Area::add, Area::add));
    regions.add(
        IntStream.concat(IntStream.of(8), IntStream.rangeClosed(15, 20))
            .mapToObj(shapesByDistrict::get)
            .map(Area::new)
            .collect(Area::new, Area::add, Area::add));
    regions.add(
        IntStream.rangeClosed(21, 27)
            .mapToObj(shapesByDistrict::get)
            .map(Area::new)
            .collect(Area::new, Area::add, Area::add));
    return regions;
  }

  private Color getDistrictColor(Integer district) {
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
    return color;
  }

  private Rectangle2D loadCityBox() throws IOException {
    return shapesByDistrict().entrySet().stream()
        .filter(e -> e.getKey() >= 10 && e.getKey() <= 14)
        .map(e -> new Area(e.getValue()))
        .collect(Area::new, Area::add, Area::add)
        .getBounds2D();
  }

  private Map<Integer, Shape> shapesByDistrict() throws IOException {
    URL peiMap =
        MapFrameTest.class
            .getClassLoader()
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp");
    return ShapefileReader.readShapes(peiMap, "DIST_NO", Integer.class);
  }

  private static class MapEntry extends Bindable<MapEntry.Property> {

    private enum Property {
      SHAPE,
      COLOR
    }

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
      onPropertyRefreshed(MapEntry.Property.SHAPE);
    }

    public Color getColor() {
      return color;
    }

    public void setColor(Color color) {
      this.color = color;
      onPropertyRefreshed(MapEntry.Property.COLOR);
    }
  }
}
