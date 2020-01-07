package com.joecollins.graphics.utils;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;

public class ShapefileReader {

  public static <T> Map<T, Shape> readShapes(URL file, String keyProperty, Class<T> keyType)
      throws IOException {
    Map<T, Shape> shapes = new HashMap<>();
    FileDataStore store = null;
    SimpleFeatureSource featureSource = null;
    SimpleFeatureIterator features = null;
    try {
      store = FileDataStoreFinder.getDataStore(file);
      featureSource = store.getFeatureSource();
      features = featureSource.getFeatures().features();
      while (features.hasNext()) {
        SimpleFeature feature = features.next();
        T key = keyType.cast(feature.getAttribute(keyProperty));
        Geometry geom = (Geometry) feature.getAttribute("the_geom");
        shapes.merge(
            key,
            toShape(geom),
            (s1, s2) -> {
              Area s = new Area(s1);
              s.add(new Area(s2));
              return s;
            });
      }
      return shapes;
    } finally {
      if (features != null) {
        features.close();
      }
      if (store != null) {
        store.dispose();
      }
    }
  }

  private static Shape toShape(Geometry geom) {
    ShapeWriter shapeWriter = new ShapeWriter();
    AffineTransform transform = AffineTransform.getScaleInstance(1, -1);
    return transform.createTransformedShape(shapeWriter.toShape(geom));
  }
}
