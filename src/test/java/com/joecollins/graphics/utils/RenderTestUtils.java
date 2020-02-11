package com.joecollins.graphics.utils;

import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenderTestUtils {

  private static Logger LOGGER = LoggerFactory.getLogger(RenderTestUtils.class);

  public static void compareRendering(String testClass, String testMethod, JPanel panel)
      throws IOException {
    File expectedFile =
        new File(
            "src\\test\\resources\\com\\joecollins\\graphics\\"
                + testClass
                + "\\"
                + testMethod
                + ".png");
    File actualFile = File.createTempFile("test", ".png");
    ImageIO.write(convertToImage(panel), "png", actualFile);
    boolean isMatch = FileUtils.contentEquals(expectedFile, actualFile);
    if (!isMatch) {
      System.out.println(expectedFile.getAbsolutePath());
      System.out.println(actualFile.getAbsolutePath());
      System.out.println(
          String.format(
              "copy /Y %s %s", actualFile.getAbsolutePath(), expectedFile.getAbsolutePath()));
    }
    assertTrue(isMatch);
    actualFile.deleteOnExit();
  }

  private static BufferedImage convertToImage(JPanel component) {
    Queue<Component> components = new LinkedList<>();
    components.offer(component);
    while (!components.isEmpty()) {
      Component c = components.poll();
      c.doLayout();
      if (c instanceof Container) {
        components.addAll(Arrays.asList(((Container) c).getComponents()));
      }
    }

    BufferedImage img =
        new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
    try {
      EventQueue.invokeAndWait(
          () -> {
            component.print(img.getGraphics());
          });
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError(e);
    } catch (InvocationTargetException e) {
      throw new AssertionError(e);
    }
    return img;
  }
}
