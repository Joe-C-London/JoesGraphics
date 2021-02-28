package com.joecollins.graphics.components.lowerthird;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Binding;
import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Test;

public class LowerThirdTest {

  @Test
  public void testLeftImage() {
    LowerThird lowerThird = new LowerThird();
    Image image = LowerThird.createImage("BREAKING NEWS", Color.WHITE, Color.RED);
    lowerThird.setLeftImageBinding(Binding.fixedBinding(image));
    assertEquals(image, lowerThird.getLeftImage());
  }

  @Test
  public void testLocationAndTimeZone() throws InterruptedException {
    LowerThird lowerThird = new LowerThird();
    lowerThird.setPlaceBinding(Binding.fixedBinding("OTTAWA"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("Canada/Eastern")));
    lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()));
    Thread.sleep(100);
    assertEquals("OTTAWA", lowerThird.getPlace());
    assertEquals("21:30", lowerThird.getTime());
  }

  @Test
  public void testRenderBlankMiddle() throws IOException {
    LowerThird lowerThird = new LowerThird();
    lowerThird.setSize(1024, 50);
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(LowerThird.createImage("BREAKING NEWS", Color.WHITE, Color.RED)));
    lowerThird.setPlaceBinding(Binding.fixedBinding("OTTAWA"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("Canada/Eastern")));
    lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()));

    compareRendering("LowerThird", "BlankMiddle", lowerThird);
  }
}
