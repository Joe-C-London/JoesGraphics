package com.joecollins.graphics.utils;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StandardFont {

  private StandardFont() {}

  private static final Font KLAVIKA_BOLD;
  private static final Font KLAVIKA_REGULAR;

  static {
    try {
      KLAVIKA_BOLD =
          Font.createFont(
              Font.TRUETYPE_FONT,
              StandardFont.class.getClassLoader().getResourceAsStream("Klavika Bold.otf"));
      KLAVIKA_REGULAR =
          Font.createFont(
              Font.TRUETYPE_FONT,
              StandardFont.class.getClassLoader().getResourceAsStream("Klavika Regular.otf"));
    } catch (FontFormatException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Map<Integer, Font> boldFontCache = new HashMap<>();
  private static Map<Integer, Font> normalFontCache = new HashMap<>();

  private static Function<Integer, Font> boldFont =
      size -> KLAVIKA_BOLD.deriveFont(Font.PLAIN, size);

  private static Function<Integer, Font> normalFont =
      size -> KLAVIKA_REGULAR.deriveFont(Font.PLAIN, size);

  public static synchronized Font readBoldFont(int size) {
    return boldFontCache.computeIfAbsent(size, boldFont);
  }

  public static synchronized Font readNormalFont(int size) {
    return normalFontCache.computeIfAbsent(size, normalFont);
  }

  public static synchronized Void setFont(String name, int style) {
    boldFont =
        size -> {
          try {
            return Font.createFont(
                    Font.TRUETYPE_FONT,
                    StandardFont.class.getClassLoader().getResourceAsStream(name))
                .deriveFont(Font.BOLD | style, size);
          } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
          }
        };
    normalFont =
        size -> {
          try {
            return Font.createFont(
                    Font.TRUETYPE_FONT,
                    StandardFont.class.getClassLoader().getResourceAsStream(name))
                .deriveFont(style, size);
          } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
          }
        };
    boldFontCache.clear();
    normalFontCache.clear();
    return null;
  }
}
