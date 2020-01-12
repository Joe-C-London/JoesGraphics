package com.joecollins.graphics.components.lowerthird;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Binding;
import java.awt.Color;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Test;

public class LowerThirdHeadlineOnlyTest {

  @Test
  public void testHeadline() {
    LowerThirdHeadlineOnly lowerThird = new LowerThirdHeadlineOnly();
    lowerThird.setHeadlineBinding(Binding.fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"));
    assertEquals("POLLS CLOSE ACROSS CENTRAL CANADA", lowerThird.getHeadline());
  }

  @Test
  public void testSubhead() {
    LowerThirdHeadlineOnly lowerThird = new LowerThirdHeadlineOnly();
    lowerThird.setSubheadBinding(Binding.fixedBinding("Polls open for 30 minutes on west coast"));
    assertEquals("Polls open for 30 minutes on west coast", lowerThird.getSubhead());
  }

  @Test
  public void testRenderHeadlineSubhead() throws IOException {
    LowerThirdHeadlineOnly lowerThird = new LowerThirdHeadlineOnly();
    lowerThird.setSize(1024, 50);
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(LowerThird.createImage("BREAKING NEWS", Color.WHITE, Color.RED)));
    lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()));
    lowerThird.setPlaceBinding(Binding.fixedBinding("OTTAWA"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("Canada/Eastern")));
    lowerThird.setHeadlineBinding(Binding.fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"));
    lowerThird.setSubheadBinding(Binding.fixedBinding("Polls open for 30 minutes on west coast"));

    compareRendering("LowerThird", "HeadlineAndSubhead", lowerThird);
  }

  @Test
  public void testRenderHeadlineOnly() throws IOException {
    LowerThirdHeadlineOnly lowerThird = new LowerThirdHeadlineOnly();
    lowerThird.setSize(1024, 50);
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(LowerThird.createImage("BREAKING NEWS", Color.WHITE, Color.RED)));
    lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()));
    lowerThird.setPlaceBinding(Binding.fixedBinding("OTTAWA"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("Canada/Eastern")));
    lowerThird.setHeadlineBinding(Binding.fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"));
    lowerThird.setSubheadBinding(Binding.fixedBinding(null));

    compareRendering("LowerThird", "HeadlineOnly", lowerThird);
  }
}
