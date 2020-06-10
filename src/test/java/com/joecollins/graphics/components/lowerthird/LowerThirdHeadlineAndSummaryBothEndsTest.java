package com.joecollins.graphics.components.lowerthird;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.components.lowerthird.SummaryFromBothEnds.Entry;
import com.joecollins.graphics.utils.BindableWrapper;
import java.awt.Color;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.Test;

public class LowerThirdHeadlineAndSummaryBothEndsTest {

  @Test
  public void testHeadline() {
    LowerThirdHeadlineAndSummaryBothEnds lowerThird = new LowerThirdHeadlineAndSummaryBothEnds();
    lowerThird.setHeadlineBinding(Binding.fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"));
    assertEquals("POLLS CLOSE ACROSS CENTRAL CANADA", lowerThird.getHeadline());
  }

  @Test
  public void testSubhead() {
    LowerThirdHeadlineAndSummaryBothEnds lowerThird = new LowerThirdHeadlineAndSummaryBothEnds();
    lowerThird.setSubheadBinding(Binding.fixedBinding("Polls open for 30 minutes on west coast"));
    assertEquals("Polls open for 30 minutes on west coast", lowerThird.getSubhead());
  }

  @Test
  public void testSummaryPanel() {
    LowerThirdHeadlineAndSummaryBothEnds lowerThird = new LowerThirdHeadlineAndSummaryBothEnds();
    lowerThird.setSummaryHeaderBinding(() -> "270 TO WIN");
    lowerThird.setTotalBinding(() -> 538);

    assertEquals("270 TO WIN", lowerThird.getSummaryHeader());
    assertEquals(538, lowerThird.getTotal());
  }

  @Test
  public void testTwoPartySummary() {
    LowerThirdHeadlineAndSummaryBothEnds lowerThird = new LowerThirdHeadlineAndSummaryBothEnds();
    lowerThird.setLeftBinding(
        Binding.fixedBinding(new SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 232)));
    lowerThird.setRightBinding(
        Binding.fixedBinding(new SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 306)));

    assertEquals(Color.BLUE, lowerThird.getLeft().getColor());
    assertEquals("CLINTON", lowerThird.getLeft().getLabel());
    assertEquals(232, lowerThird.getLeft().getValue());

    assertEquals(Color.RED, lowerThird.getRight().getColor());
    assertEquals("TRUMP", lowerThird.getRight().getLabel());
    assertEquals(306, lowerThird.getRight().getValue());
  }

  @Test
  public void testThreePartySummary() {
    LowerThirdHeadlineAndSummaryBothEnds lowerThird = new LowerThirdHeadlineAndSummaryBothEnds();
    lowerThird.setLeftBinding(
        Binding.fixedBinding(new SummaryFromBothEnds.Entry(Color.BLUE, "HUMPHREY", 191)));
    lowerThird.setRightBinding(
        Binding.fixedBinding(new SummaryFromBothEnds.Entry(Color.RED, "NIXON", 301)));
    lowerThird.setMiddleBinding(
        Binding.fixedBinding(new SummaryFromBothEnds.Entry(Color.GRAY, "WALLACE", 46)));

    assertEquals(Color.BLUE, lowerThird.getLeft().getColor());
    assertEquals("HUMPHREY", lowerThird.getLeft().getLabel());
    assertEquals(191, lowerThird.getLeft().getValue());

    assertEquals(Color.RED, lowerThird.getRight().getColor());
    assertEquals("NIXON", lowerThird.getRight().getLabel());
    assertEquals(301, lowerThird.getRight().getValue());

    assertEquals(Color.GRAY, lowerThird.getMiddle().getColor());
    assertEquals("WALLACE", lowerThird.getMiddle().getLabel());
    assertEquals(46, lowerThird.getMiddle().getValue());
  }

  @Test
  public void testRenderRightWin() throws IOException {
    BindableWrapper<Entry> left = new BindableWrapper<>(new Entry(Color.BLUE, "CLINTON", 0));
    BindableWrapper<Entry> right = new BindableWrapper<>(new Entry(Color.RED, "TRUMP", 0));

    LowerThirdHeadlineAndSummaryBothEnds lowerThird = new LowerThirdHeadlineAndSummaryBothEnds();
    lowerThird.setSize(1024, 50);
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(LowerThird.createImage("AMERICA VOTES", Color.WHITE, Color.RED)));
    lowerThird.setClock(Clock.fixed(Instant.parse("2016-11-09T06:00:00Z"), ZoneId.systemDefault()));
    lowerThird.setPlaceBinding(Binding.fixedBinding("WASHINGTON"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("US/Eastern")));
    lowerThird.setHeadlineBinding(Binding.fixedBinding("TRUMP WINS ELECTION"));
    lowerThird.setSubheadBinding(Binding.fixedBinding(null));
    lowerThird.setSummaryHeaderBinding(Binding.fixedBinding("270 TO WIN"));
    lowerThird.setLeftBinding(left.getBinding());
    lowerThird.setRightBinding(right.getBinding());
    lowerThird.setTotalBinding(Binding.fixedBinding(538));

    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightWin-1", lowerThird);

    left.setValue(new Entry(Color.BLUE, "CLINTON", 6));
    right.setValue(new Entry(Color.RED, "TRUMP", 16));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightWin-2", lowerThird);

    left.setValue(new Entry(Color.BLUE, "CLINTON", 82));
    right.setValue(new Entry(Color.RED, "TRUMP", 56));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightWin-3", lowerThird);

    left.setValue(new Entry(Color.BLUE, "CLINTON", 232));
    right.setValue(new Entry(Color.RED, "TRUMP", 306));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightWin-4", lowerThird);
  }

  @Test
  public void testRenderLeftWin() throws IOException {
    BindableWrapper<Entry> left = new BindableWrapper<>(new Entry(Color.BLUE, "OBAMA", 0));
    BindableWrapper<Entry> right = new BindableWrapper<>(new Entry(Color.RED, "MCCAIN", 0));

    LowerThirdHeadlineAndSummaryBothEnds lowerThird = new LowerThirdHeadlineAndSummaryBothEnds();
    lowerThird.setSize(1024, 50);
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(LowerThird.createImage("AMERICA VOTES", Color.WHITE, Color.RED)));
    lowerThird.setClock(Clock.fixed(Instant.parse("2008-11-05T06:00:00Z"), ZoneId.systemDefault()));
    lowerThird.setPlaceBinding(Binding.fixedBinding("WASHINGTON"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("US/Eastern")));
    lowerThird.setHeadlineBinding(Binding.fixedBinding("OBAMA WINS ELECTION"));
    lowerThird.setSubheadBinding(Binding.fixedBinding(null));
    lowerThird.setSummaryHeaderBinding(Binding.fixedBinding("270 TO WIN"));
    lowerThird.setLeftBinding(left.getBinding());
    lowerThird.setRightBinding(right.getBinding());
    lowerThird.setTotalBinding(Binding.fixedBinding(538));

    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftWin-1", lowerThird);

    left.setValue(new Entry(Color.BLUE, "OBAMA", 10));
    right.setValue(new Entry(Color.RED, "MCCAIN", 7));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftWin-2", lowerThird);

    left.setValue(new Entry(Color.BLUE, "OBAMA", 153));
    right.setValue(new Entry(Color.RED, "MCCAIN", 28));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftWin-3", lowerThird);

    left.setValue(new Entry(Color.BLUE, "OBAMA", 365));
    right.setValue(new Entry(Color.RED, "MCCAIN", 173));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftWin-4", lowerThird);
  }

  @Test
  public void testRenderRightLandslide() throws IOException {
    BindableWrapper<Entry> left = new BindableWrapper<>(new Entry(Color.BLUE, "MONDALE", 0));
    BindableWrapper<Entry> right = new BindableWrapper<>(new Entry(Color.RED, "REAGAN", 0));

    LowerThirdHeadlineAndSummaryBothEnds lowerThird = new LowerThirdHeadlineAndSummaryBothEnds();
    lowerThird.setSize(1024, 50);
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(LowerThird.createImage("AMERICA VOTES", Color.WHITE, Color.RED)));
    lowerThird.setClock(Clock.fixed(Instant.parse("1984-11-07T06:00:00Z"), ZoneId.systemDefault()));
    lowerThird.setPlaceBinding(Binding.fixedBinding("WASHINGTON"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("US/Eastern")));
    lowerThird.setHeadlineBinding(Binding.fixedBinding("REAGAN WINS RE-ELECTION"));
    lowerThird.setSubheadBinding(Binding.fixedBinding(null));
    lowerThird.setSummaryHeaderBinding(Binding.fixedBinding("270 TO WIN"));
    lowerThird.setLeftBinding(left.getBinding());
    lowerThird.setRightBinding(right.getBinding());
    lowerThird.setTotalBinding(Binding.fixedBinding(538));

    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightLandslide-1", lowerThird);

    left.setValue(new Entry(Color.BLUE, "MONDALE", 3));
    right.setValue(new Entry(Color.RED, "REAGAN", 71));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightLandslide-2", lowerThird);

    left.setValue(new Entry(Color.BLUE, "MONDALE", 3));
    right.setValue(new Entry(Color.RED, "REAGAN", 280));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightLandslide-3", lowerThird);

    left.setValue(new Entry(Color.BLUE, "MONDALE", 13));
    right.setValue(new Entry(Color.RED, "REAGAN", 525));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightLandslide-4", lowerThird);
  }

  @Test
  public void testRenderLeftLandslide() throws IOException {
    BindableWrapper<Entry> left = new BindableWrapper<>(new Entry(Color.BLUE, "ROOSEVELT", 0));
    BindableWrapper<Entry> right = new BindableWrapper<>(new Entry(Color.RED, "LANDON", 0));

    LowerThirdHeadlineAndSummaryBothEnds lowerThird = new LowerThirdHeadlineAndSummaryBothEnds();
    lowerThird.setSize(1024, 50);
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(LowerThird.createImage("AMERICA VOTES", Color.WHITE, Color.RED)));
    lowerThird.setClock(Clock.fixed(Instant.parse("1936-11-04T06:00:00Z"), ZoneId.systemDefault()));
    lowerThird.setPlaceBinding(Binding.fixedBinding("WASHINGTON"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("US/Eastern")));
    lowerThird.setHeadlineBinding(Binding.fixedBinding("ROOSEVELT WINS RE-ELECTION"));
    lowerThird.setSubheadBinding(Binding.fixedBinding(null));
    lowerThird.setSummaryHeaderBinding(Binding.fixedBinding("266 TO WIN"));
    lowerThird.setLeftBinding(left.getBinding());
    lowerThird.setRightBinding(right.getBinding());
    lowerThird.setTotalBinding(Binding.fixedBinding(531));

    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftLandslide-1", lowerThird);

    left.setValue(new Entry(Color.BLUE, "ROOSEVELT", 169));
    right.setValue(new Entry(Color.RED, "LANDON", 0));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftLandslide-2", lowerThird);

    left.setValue(new Entry(Color.BLUE, "ROOSEVELT", 242));
    right.setValue(new Entry(Color.RED, "LANDON", 0));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftLandslide-3", lowerThird);

    left.setValue(new Entry(Color.BLUE, "ROOSEVELT", 523));
    right.setValue(new Entry(Color.RED, "LANDON", 8));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftLandslide-4", lowerThird);
  }

  @Test
  public void testRenderWithMiddleNoMajority() throws IOException {
    BindableWrapper<Entry> left = new BindableWrapper<>(new Entry(Color.BLUE, "DEMOCRATS", 27));
    BindableWrapper<Entry> right = new BindableWrapper<>(new Entry(Color.RED, "REPUBLICANS", 40));
    BindableWrapper<Entry> middle = new BindableWrapper<>(null);

    LowerThirdHeadlineAndSummaryBothEnds lowerThird = new LowerThirdHeadlineAndSummaryBothEnds();
    lowerThird.setSize(1024, 50);
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(LowerThird.createImage("AMERICA VOTES", Color.WHITE, Color.RED)));
    lowerThird.setClock(Clock.fixed(Instant.parse("2006-11-08T06:00:00Z"), ZoneId.systemDefault()));
    lowerThird.setPlaceBinding(Binding.fixedBinding("WASHINGTON"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("US/Eastern")));
    lowerThird.setHeadlineBinding(Binding.fixedBinding("NO SENATE MAJORITY"));
    lowerThird.setSubheadBinding(Binding.fixedBinding(null));
    lowerThird.setSummaryHeaderBinding(Binding.fixedBinding("51 FOR CONTROL"));
    lowerThird.setLeftBinding(left.getBinding());
    lowerThird.setRightBinding(right.getBinding());
    lowerThird.setMiddleBinding(middle.getBinding());
    lowerThird.setTotalBinding(Binding.fixedBinding(100));

    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleNoMajority-1", lowerThird);

    left.setValue(new Entry(Color.BLUE, "DEMOCRATS", 44));
    right.setValue(new Entry(Color.RED, "REPUBLICANS", 47));
    middle.setValue(new Entry(Color.GRAY, "OTHERS", 1));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleNoMajority-2", lowerThird);

    left.setValue(new Entry(Color.BLUE, "DEMOCRATS", 49));
    right.setValue(new Entry(Color.RED, "REPUBLICANS", 49));
    middle.setValue(new Entry(Color.GRAY, "OTHERS", 2));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleNoMajority-3", lowerThird);
  }

  @Test
  public void testRenderWithMiddleLeftMajority() throws IOException {
    BindableWrapper<Entry> left = new BindableWrapper<>(new Entry(Color.BLUE, "DEMOCRATS", 37));
    BindableWrapper<Entry> right = new BindableWrapper<>(new Entry(Color.RED, "REPUBLICANS", 26));
    BindableWrapper<Entry> middle = new BindableWrapper<>(new Entry(Color.GRAY, "OTHERS", 2));

    LowerThirdHeadlineAndSummaryBothEnds lowerThird = new LowerThirdHeadlineAndSummaryBothEnds();
    lowerThird.setSize(1024, 50);
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(LowerThird.createImage("AMERICA VOTES", Color.WHITE, Color.RED)));
    lowerThird.setClock(Clock.fixed(Instant.parse("2008-11-05T06:00:00Z"), ZoneId.systemDefault()));
    lowerThird.setPlaceBinding(Binding.fixedBinding("WASHINGTON"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("US/Eastern")));
    lowerThird.setHeadlineBinding(Binding.fixedBinding("DEMOCRATIC SENATE MAJORITY"));
    lowerThird.setSubheadBinding(Binding.fixedBinding(null));
    lowerThird.setSummaryHeaderBinding(Binding.fixedBinding("51 FOR CONTROL"));
    lowerThird.setLeftBinding(left.getBinding());
    lowerThird.setRightBinding(right.getBinding());
    lowerThird.setMiddleBinding(middle.getBinding());
    lowerThird.setTotalBinding(Binding.fixedBinding(100));

    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleLeftMajority-1", lowerThird);

    left.setValue(new Entry(Color.BLUE, "DEMOCRATS", 51));
    right.setValue(new Entry(Color.RED, "REPUBLICANS", 39));
    middle.setValue(new Entry(Color.GRAY, "OTHERS", 2));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleLeftMajority-2", lowerThird);

    left.setValue(new Entry(Color.BLUE, "DEMOCRATS", 57));
    right.setValue(new Entry(Color.RED, "REPUBLICANS", 41));
    middle.setValue(new Entry(Color.GRAY, "OTHERS", 2));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleLeftMajority-3", lowerThird);
  }

  @Test
  public void testRenderWithMiddleRightMajority() throws IOException {
    BindableWrapper<Entry> left = new BindableWrapper<>(new Entry(Color.BLUE, "DEMOCRATS", 29));
    BindableWrapper<Entry> right = new BindableWrapper<>(new Entry(Color.RED, "REPUBLICANS", 36));
    BindableWrapper<Entry> middle = new BindableWrapper<>(new Entry(Color.GRAY, "OTHERS", 1));

    LowerThirdHeadlineAndSummaryBothEnds lowerThird = new LowerThirdHeadlineAndSummaryBothEnds();
    lowerThird.setSize(1024, 50);
    lowerThird.setLeftImageBinding(
        Binding.fixedBinding(LowerThird.createImage("AMERICA VOTES", Color.WHITE, Color.RED)));
    lowerThird.setClock(Clock.fixed(Instant.parse("2004-11-03T06:00:00Z"), ZoneId.systemDefault()));
    lowerThird.setPlaceBinding(Binding.fixedBinding("WASHINGTON"));
    lowerThird.setTimeZoneBinding(Binding.fixedBinding(ZoneId.of("US/Eastern")));
    lowerThird.setHeadlineBinding(Binding.fixedBinding("REPUBLICAN SENATE MAJORITY"));
    lowerThird.setSubheadBinding(Binding.fixedBinding(null));
    lowerThird.setSummaryHeaderBinding(Binding.fixedBinding("51 FOR CONTROL"));
    lowerThird.setLeftBinding(left.getBinding());
    lowerThird.setRightBinding(right.getBinding());
    lowerThird.setMiddleBinding(middle.getBinding());
    lowerThird.setTotalBinding(Binding.fixedBinding(100));

    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleRightMajority-1", lowerThird);

    left.setValue(new Entry(Color.BLUE, "DEMOCRATS", 43));
    right.setValue(new Entry(Color.RED, "REPUBLICANS", 49));
    middle.setValue(new Entry(Color.GRAY, "OTHERS", 1));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleRightMajority-2", lowerThird);

    left.setValue(new Entry(Color.BLUE, "DEMOCRATS", 44));
    right.setValue(new Entry(Color.RED, "REPUBLICANS", 55));
    middle.setValue(new Entry(Color.GRAY, "OTHERS", 1));
    compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleRightMajority-3", lowerThird);
  }
}
