package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Binding;
import java.awt.Color;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Test;

public class CountdownFrameTest {

  @Test
  public void testTimeRemaining() {
    CountdownFrame frame = new CountdownFrame();
    frame.setClock(Clock.fixed(Instant.parse("2020-07-04T12:34:56Z"), ZoneId.of("UTC")));
    frame.setTimeBinding(
        Binding.fixedBinding(ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern"))));
    assertEquals(
        Duration.ofDays(1).plusHours(10).plusMinutes(25).plusSeconds(4), frame.getTimeRemaining());
  }

  @Test
  public void testTimeDisplay() {
    CountdownFrame frame = new CountdownFrame();
    frame.setClock(Clock.fixed(Instant.parse("2020-07-04T12:34:56Z"), ZoneId.of("UTC")));
    frame.setTimeBinding(
        Binding.fixedBinding(ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern"))));
    frame.setLabelFunction(CountdownFrame::formatDDHHMMSS);
    assertEquals("1:10:25:04", frame.getTimeRemainingString());

    frame.setLabelFunction(CountdownFrame::formatHHMMSS);
    assertEquals("34:25:04", frame.getTimeRemainingString());

    frame.setLabelFunction(CountdownFrame::formatMMSS);
    assertEquals("2065:04", frame.getTimeRemainingString());
  }

  @Test
  public void testCountdown() throws InterruptedException {
    CountdownFrame frame = new CountdownFrame();
    frame.setClock(Clock.fixed(Instant.parse("2020-07-04T12:34:56Z"), ZoneId.of("UTC")));
    frame.setTimeBinding(
        Binding.fixedBinding(ZonedDateTime.of(2020, 7, 5, 19, 0, 0, 0, ZoneId.of("US/Eastern"))));
    frame.setLabelFunction(CountdownFrame::formatDDHHMMSS);
    assertEquals("1:10:25:04", frame.getTimeRemainingString());

    frame.setClock(Clock.fixed(Instant.parse("2020-07-04T12:34:57Z"), ZoneId.of("UTC")));
    Thread.sleep(200);
    assertEquals("1:10:25:03", frame.getTimeRemainingString());
  }

  @Test
  public void testAdditionalInfo() {
    CountdownFrame frame = new CountdownFrame();
    frame.setAdditionalInfoBinding(() -> "ADDITIONAL INFO");
    assertEquals("ADDITIONAL INFO", frame.getAdditionalInfo());
  }

  @Test
  public void testCountdownColor() {
    CountdownFrame frame = new CountdownFrame();
    frame.setCountdownColorBinding(() -> Color.RED);
    assertEquals(Color.RED, frame.getCountdownColor());
  }

  @Test
  public void testRenderWithoutAdditionalInfo() throws IOException {
    CountdownFrame frame = new CountdownFrame();
    frame.setClock(Clock.fixed(Instant.parse("2020-07-04T19:41:10Z"), ZoneId.of("UTC")));
    frame.setTimeBinding(
        Binding.fixedBinding(ZonedDateTime.of(2021, 1, 20, 12, 0, 0, 0, ZoneId.of("US/Eastern"))));
    frame.setLabelFunction(CountdownFrame::formatDDHHMMSS);
    frame.setHeaderBinding(() -> "TRUMP TERM END");
    frame.setCountdownColorBinding(() -> Color.RED);
    frame.setBorderColorBinding(() -> Color.RED);

    frame.setSize(200, 100);
    compareRendering("CountdownFrame", "NoAdditionalInfo", frame);
  }

  @Test
  public void testRenderWithAdditionalInfo() throws IOException {
    CountdownFrame frame = new CountdownFrame();
    frame.setClock(Clock.fixed(Instant.parse("2020-07-04T19:41:10Z"), ZoneId.of("UTC")));
    frame.setTimeBinding(
        Binding.fixedBinding(ZonedDateTime.of(2020, 11, 3, 23, 0, 0, 0, ZoneId.of("UTC"))));
    frame.setLabelFunction(CountdownFrame::formatDDHHMMSS);
    frame.setHeaderBinding(() -> "1ST POLLS CLOSE");
    frame.setAdditionalInfoBinding(() -> "IN/KY");

    frame.setSize(200, 100);
    compareRendering("CountdownFrame", "AdditionalInfo", frame);
  }
}
