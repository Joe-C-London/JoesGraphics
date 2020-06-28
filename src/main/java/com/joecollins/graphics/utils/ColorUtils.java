package com.joecollins.graphics.utils;

import java.awt.Color;

public class ColorUtils {

  private ColorUtils() {}

  public static Color lighten(Color color) {
    return new Color(
        128 + color.getRed() / 2, 128 + color.getGreen() / 2, 128 + color.getBlue() / 2);
  }
}
