package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.io.IOException;
import java.util.Comparator;
import org.junit.Test;

public class ResultListingFrameTest {

  @Test
  public void testNumRows() {
    ResultListingFrame frame = new ResultListingFrame();
    frame.setNumRowsBinding(Binding.fixedBinding(20));
    assertEquals(20, frame.getNumRows());
  }

  @Test
  public void testItems() {
    BindableList<Item> items = new BindableList<>();
    ResultListingFrame frame = new ResultListingFrame();
    frame.setNumItemsBinding(Binding.sizeBinding(items));
    frame.setTextBinding(IndexedBinding.propertyBinding(items, i -> i.text, Item.Property.TEXT));
    frame.setForegroundBinding(
        IndexedBinding.propertyBinding(items, i -> i.foreground, Item.Property.FOREGROUND));
    frame.setBackgroundBinding(
        IndexedBinding.propertyBinding(items, i -> i.background, Item.Property.BACKGROUND));
    frame.setBorderBinding(
        IndexedBinding.propertyBinding(items, i -> i.border, Item.Property.BORDER));

    items.add(new Item("BELFAST-MURRAY RIVER", Color.WHITE, Color.BLACK, Color.BLUE));
    items.add(new Item("SOURIS-ELMIRA", Color.WHITE, Color.BLACK, Color.BLUE));
    items.add(new Item("GEORGETOWN-ST. PETERS", Color.WHITE, Color.BLACK, Color.BLUE));
    items.add(new Item("MORRELL-MERMAID", Color.WHITE, Color.BLACK, Color.BLUE));
    items.add(new Item("RUSTICO-EMERALD", Color.WHITE, Color.BLACK, Color.BLUE));
    items.add(new Item("BORDEN-KINKORA", Color.WHITE, Color.BLACK, Color.BLUE));
    items.add(new Item("STRATFORD-KINLOCK", Color.WHITE, Color.BLACK, Color.BLUE));
    items.add(new Item("KENSINGTON-MALPEQUE", Color.WHITE, Color.BLACK, Color.BLUE));
    assertEquals(8, frame.getNumItems());
    assertEquals("BELFAST-MURRAY RIVER", frame.getText(0));
    assertEquals(Color.WHITE, frame.getBackground(1));
    assertEquals(Color.BLACK, frame.getForeground(2));
    assertEquals(Color.BLUE, frame.getBorder(3));

    items.get(4).setForeground(Color.WHITE);
    items.get(4).setBackground(Color.BLUE);
    assertEquals(Color.BLUE, frame.getBackground(4));
    assertEquals(Color.WHITE, frame.getForeground(4));
    assertEquals(Color.BLUE, frame.getBorder(4));
  }

  @Test
  public void testSingleFullColumn() throws IOException {
    BindableList<Item> items = new BindableList<>();
    items.add(new Item("Mermaid-Stratford", Color.WHITE, Color.BLACK, Color.RED)); // 0.0
    items.add(new Item("Charlottetown-Brighton", Color.WHITE, Color.BLACK, Color.RED)); // 0.8
    items.add(new Item("Summerside-Wilmot", Color.WHITE, Color.BLACK, Color.RED)); // 1.0
    items.add(new Item("Brackley-Hunter River", Color.WHITE, Color.BLACK, Color.RED)); // 1.6
    items.add(new Item("Summerside-South Drive", Color.WHITE, Color.BLACK, Color.RED)); // 4.9
    items.add(new Item("Charlottetown-West Royalty", Color.WHITE, Color.BLACK, Color.RED)); // 7.3
    items.add(new Item("O'Leary-Inverness", Color.WHITE, Color.BLACK, Color.RED)); // 9.2
    items.add(new Item("Montague-Kilmuir", Color.WHITE, Color.BLACK, Color.RED)); // 10.8
    items.add(new Item("Charlottetown-Victoria Park", Color.WHITE, Color.BLACK, Color.RED)); // 11.9
    items.add(new Item("Cornwall-Meadowbank", Color.WHITE, Color.BLACK, Color.RED)); // 12.5

    ResultListingFrame frame = new ResultListingFrame();
    frame.setNumRowsBinding(() -> 10);
    frame.setNumItemsBinding(Binding.sizeBinding(items));
    frame.setTextBinding(
        IndexedBinding.propertyBinding(items, i -> i.text.toUpperCase(), Item.Property.TEXT));
    frame.setForegroundBinding(
        IndexedBinding.propertyBinding(items, i -> i.foreground, Item.Property.FOREGROUND));
    frame.setBackgroundBinding(
        IndexedBinding.propertyBinding(items, i -> i.background, Item.Property.BACKGROUND));
    frame.setBorderBinding(
        IndexedBinding.propertyBinding(items, i -> i.border, Item.Property.BORDER));
    frame.setHeaderBinding(() -> "PC TARGETS");
    frame.setBorderColorBinding(() -> Color.BLUE);
    frame.setSize(512, 512);
    compareRendering("ResultListingFrame", "FullColumn-1", frame);

    for (int i = 0; i < 10; i++) {
      switch (i) {
        case 0:
        case 1:
        case 2:
        case 4:
        case 8:
          items.get(i).setBackground(Color.GREEN.darker());
          break;
        case 3:
        case 7:
          items.get(i).setBackground(Color.BLUE);
          break;
        case 5:
        case 6:
        case 9:
          items.get(i).setBackground(Color.RED);
          break;
        default:
          continue;
      }
      items.get(i).setForeground(Color.WHITE);
    }
    compareRendering("ResultListingFrame", "FullColumn-2", frame);
  }

  @Test
  public void testVaryingItems() throws IOException {
    BindableList<Item> items = new BindableList<>();

    ResultListingFrame frame = new ResultListingFrame();
    frame.setNumRowsBinding(() -> 10);
    frame.setNumItemsBinding(Binding.sizeBinding(items));
    frame.setTextBinding(
        IndexedBinding.propertyBinding(items, i -> i.text.toUpperCase(), Item.Property.TEXT));
    frame.setForegroundBinding(
        IndexedBinding.propertyBinding(items, i -> i.foreground, Item.Property.FOREGROUND));
    frame.setBackgroundBinding(
        IndexedBinding.propertyBinding(items, i -> i.background, Item.Property.BACKGROUND));
    frame.setBorderBinding(
        IndexedBinding.propertyBinding(items, i -> i.border, Item.Property.BORDER));
    frame.setHeaderBinding(() -> "SEATS CHANGING");
    frame.setSize(512, 256);
    compareRendering("ResultListingFrame", "Varying-1", frame);

    items.add(new Item("Montague-Kilmuir", Color.BLUE, Color.WHITE, Color.RED));
    items.add(new Item("Brackley-Hunter River", Color.BLUE, Color.WHITE, Color.GRAY));
    items.add(
        new Item("Charlottetown-Victoria Park", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.add(new Item("Summerside-South Drive", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.sort(Comparator.comparing(i -> i.text));
    compareRendering("ResultListingFrame", "Varying-2", frame);

    items.add(new Item("Mermaid-Stratford", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.add(new Item("Charlottetown-Belvedere", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.add(new Item("Stanhope-Marshfield", Color.BLUE, Color.WHITE, Color.RED));
    items.add(new Item("Charlottetown-Brighton", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.add(new Item("Alberton-Bloomfield", Color.BLUE, Color.WHITE, Color.RED));
    items.add(new Item("Summerside-Wilmot", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.add(new Item("Tyne Valley-Sherbrooke", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.sort(Comparator.comparing(i -> i.text));
    compareRendering("ResultListingFrame", "Varying-3", frame);
  }

  @Test
  public void testVaryingItemsInReverse() throws IOException {
    BindableList<Item> items = new BindableList<>();

    ResultListingFrame frame = new ResultListingFrame();
    frame.setNumRowsBinding(() -> 10);
    frame.setReversedBinding(() -> true);
    frame.setNumItemsBinding(Binding.sizeBinding(items));
    frame.setTextBinding(
        IndexedBinding.propertyBinding(items, i -> i.text.toUpperCase(), Item.Property.TEXT));
    frame.setForegroundBinding(
        IndexedBinding.propertyBinding(items, i -> i.foreground, Item.Property.FOREGROUND));
    frame.setBackgroundBinding(
        IndexedBinding.propertyBinding(items, i -> i.background, Item.Property.BACKGROUND));
    frame.setBorderBinding(
        IndexedBinding.propertyBinding(items, i -> i.border, Item.Property.BORDER));
    frame.setHeaderBinding(() -> "SEATS CHANGING");
    frame.setSize(512, 256);
    compareRendering("ResultListingFrame", "Reversed-1", frame);

    items.add(new Item("Montague-Kilmuir", Color.BLUE, Color.WHITE, Color.RED));
    items.add(new Item("Brackley-Hunter River", Color.BLUE, Color.WHITE, Color.GRAY));
    items.add(
        new Item("Charlottetown-Victoria Park", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.add(new Item("Summerside-South Drive", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.sort(Comparator.comparing(i -> i.text));
    compareRendering("ResultListingFrame", "Reversed-2", frame);

    items.add(new Item("Mermaid-Stratford", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.add(new Item("Charlottetown-Belvedere", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.add(new Item("Stanhope-Marshfield", Color.BLUE, Color.WHITE, Color.RED));
    items.add(new Item("Charlottetown-Brighton", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.add(new Item("Alberton-Bloomfield", Color.BLUE, Color.WHITE, Color.RED));
    items.add(new Item("Summerside-Wilmot", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.add(new Item("Tyne Valley-Sherbrooke", Color.GREEN.darker(), Color.WHITE, Color.RED));
    items.sort(Comparator.comparing(i -> i.text));
    compareRendering("ResultListingFrame", "Reversed-3", frame);
  }

  private static class Item extends Bindable {
    enum Property {
      TEXT,
      FOREGROUND,
      BACKGROUND,
      BORDER
    }

    private String text;
    private Color background;
    private Color foreground;
    private Color border;

    public Item(String text, Color background, Color foreground, Color border) {
      this.text = text;
      this.background = background;
      this.foreground = foreground;
      this.border = border;
    }

    public void setText(String text) {
      this.text = text;
      onPropertyRefreshed(Property.TEXT);
    }

    public void setForeground(Color foreground) {
      this.foreground = foreground;
      onPropertyRefreshed(Property.FOREGROUND);
    }

    public void setBackground(Color background) {
      this.background = background;
      onPropertyRefreshed(Property.BACKGROUND);
    }

    public void setBorder(Color border) {
      this.border = border;
      onPropertyRefreshed(Property.BORDER);
    }
  }
}
