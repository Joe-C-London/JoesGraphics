package com.joecollins.graphics.components.lowerthird;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.components.lowerthird.SummaryWithoutLabels.Entry;
import java.awt.Color;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Test;

public class LowerThirdHeadlineAndSummarySingleLabelTest {

  @Test
  public void testHeadline() {
    LowerThirdHeadlineAndSummarySingleLabel lowerThird =
        new LowerThirdHeadlineAndSummarySingleLabel();
    lowerThird.setHeadlineBinding(Binding.fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"));
    assertEquals("POLLS CLOSE ACROSS CENTRAL CANADA", lowerThird.getHeadline());
  }

  @Test
  public void testSubhead() {
    LowerThirdHeadlineAndSummarySingleLabel lowerThird =
        new LowerThirdHeadlineAndSummarySingleLabel();
    lowerThird.setSubheadBinding(Binding.fixedBinding("Polls open for 30 minutes on west coast"));
    assertEquals("Polls open for 30 minutes on west coast", lowerThird.getSubhead());
  }

  @Test
  public void testSummaryPanel() {
    LowerThirdHeadlineAndSummarySingleLabel lowerThird =
        new LowerThirdHeadlineAndSummarySingleLabel();
    lowerThird.setNumSummaryEntriesBinding(Binding.fixedBinding(2));
    lowerThird.setSummaryHeaderBinding(() -> "170 SEATS FOR MAJORITY");
    lowerThird.setSummaryEntriesBinding(
        IndexedBinding.listBinding(new Entry(Color.RED, "2"), new Entry(Color.BLUE, "1")));
    assertEquals(2, lowerThird.getNumSummaryEntries());

    assertEquals(Color.RED, lowerThird.getEntryColor(0));
    assertEquals(Color.BLUE, lowerThird.getEntryColor(1));

    assertEquals("2", lowerThird.getEntryValue(0));
    assertEquals("1", lowerThird.getEntryValue(1));

    assertEquals("170 SEATS FOR MAJORITY", lowerThird.getSummaryHeader());
  }

  @Test
  public void testRenderHeadlineAndSummary() throws IOException {
    LowerThirdHeadlineAndSummarySingleLabel lowerThird =
        new LowerThirdHeadlineAndSummarySingleLabel();
    lowerThird.setSize(1024, 50);
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(
            LowerThird.createImage(
                LowerThirdHeadlineAndSummarySingleLabelTest.class
                    .getClassLoader()
                    .getResource("com/joecollins/graphics/lowerthird-left.png"))));
    lowerThird.setPlaceBinding(Binding.fixedBinding("OTTAWA"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("Canada/Eastern")));
    lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()));
    lowerThird.setHeadlineBinding(Binding.fixedBinding("CENTRAL CANADA POLLS CLOSE"));
    lowerThird.setSubheadBinding(Binding.fixedBinding("Polls open for 30 minutes on west coast"));
    lowerThird.setNumSummaryEntriesBinding(Binding.fixedBinding(2));
    lowerThird.setSummaryEntriesBinding(
        IndexedBinding.listBinding(new Entry(Color.RED, "2"), new Entry(Color.BLUE, "1")));
    lowerThird.setSummaryHeaderBinding(Binding.fixedBinding("170 SEATS FOR MAJORITY"));

    compareRendering("LowerThird", "HeadlineAndSummarySingleLabel", lowerThird);
  }

  @Test
  public void testRenderLongHeadlineAndSummary() throws IOException {
    LowerThirdHeadlineAndSummarySingleLabel lowerThird =
        new LowerThirdHeadlineAndSummarySingleLabel();
    lowerThird.setSize(1024, 50);
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(
            LowerThird.createImage(
                LowerThirdHeadlineAndSummarySingleLabelTest.class
                    .getClassLoader()
                    .getResource("com/joecollins/graphics/lowerthird-left.png"))));
    lowerThird.setPlaceBinding(Binding.fixedBinding("OTTAWA"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("Canada/Eastern")));
    lowerThird.setClock(Clock.fixed(Instant.parse("2019-10-22T01:30:00Z"), ZoneId.systemDefault()));
    lowerThird.setHeadlineBinding(Binding.fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"));
    lowerThird.setSubheadBinding(
        Binding.fixedBinding("Polls open for another 30 minutes in British Columbia, Yukon"));
    lowerThird.setNumSummaryEntriesBinding(Binding.fixedBinding(2));
    lowerThird.setSummaryEntriesBinding(
        IndexedBinding.listBinding(new Entry(Color.RED, "2"), new Entry(Color.BLUE, "1")));
    lowerThird.setSummaryHeaderBinding(Binding.fixedBinding("170 SEATS FOR MAJORITY"));

    compareRendering("LowerThird", "LongHeadlineAndSummarySingleLabel", lowerThird);
  }
}
