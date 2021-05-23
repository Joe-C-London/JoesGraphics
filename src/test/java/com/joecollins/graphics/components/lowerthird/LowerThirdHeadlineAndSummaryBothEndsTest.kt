package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.components.lowerthird.LowerThird.Companion.createImage
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert
import org.junit.Test

class LowerThirdHeadlineAndSummaryBothEndsTest {
    @Test
    fun testHeadline() {
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImageBinding = fixedBinding(createImage("", Color.RED, Color.WHITE)),
            placeBinding = fixedBinding(""),
            timezoneBinding = fixedBinding(ZoneId.systemDefault()),
            headlineBinding = fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"),
            subheadBinding = fixedBinding("Polls open for 30 minutes on west coast"),
            summaryHeaderBinding = fixedBinding("270 TO WIN"),
            summaryTotalBinding = fixedBinding(538),
            summaryLeftBinding = fixedBinding(SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 232)),
            summaryRightBinding = fixedBinding(SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 306))
        )
        Assert.assertEquals("POLLS CLOSE ACROSS CENTRAL CANADA", lowerThird.headline)
    }

    @Test
    fun testSubhead() {
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImageBinding = fixedBinding(createImage("", Color.RED, Color.WHITE)),
            placeBinding = fixedBinding(""),
            timezoneBinding = fixedBinding(ZoneId.systemDefault()),
            headlineBinding = fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"),
            subheadBinding = fixedBinding("Polls open for 30 minutes on west coast"),
            summaryHeaderBinding = fixedBinding("270 TO WIN"),
            summaryTotalBinding = fixedBinding(538),
            summaryLeftBinding = fixedBinding(SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 232)),
            summaryRightBinding = fixedBinding(SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 306))
        )
        Assert.assertEquals("Polls open for 30 minutes on west coast", lowerThird.subhead)
    }

    @Test
    fun testSummaryPanel() {
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImageBinding = fixedBinding(createImage("", Color.RED, Color.WHITE)),
            placeBinding = fixedBinding(""),
            timezoneBinding = fixedBinding(ZoneId.systemDefault()),
            headlineBinding = fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"),
            subheadBinding = fixedBinding("Polls open for 30 minutes on west coast"),
            summaryHeaderBinding = fixedBinding("270 TO WIN"),
            summaryTotalBinding = fixedBinding(538),
            summaryLeftBinding = fixedBinding(SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 232)),
            summaryRightBinding = fixedBinding(SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 306))
        )
        Assert.assertEquals("270 TO WIN", lowerThird.summaryHeader)
        Assert.assertEquals(538, lowerThird.total.toLong())
    }

    @Test
    fun testTwoPartySummary() {
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImageBinding = fixedBinding(createImage("", Color.RED, Color.WHITE)),
            placeBinding = fixedBinding(""),
            timezoneBinding = fixedBinding(ZoneId.systemDefault()),
            headlineBinding = fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"),
            subheadBinding = fixedBinding("Polls open for 30 minutes on west coast"),
            summaryHeaderBinding = fixedBinding("270 TO WIN"),
            summaryTotalBinding = fixedBinding(538),
            summaryLeftBinding = fixedBinding(SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 232)),
            summaryRightBinding = fixedBinding(SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 306))
        )
        Assert.assertEquals(Color.BLUE, lowerThird.left!!.color)
        Assert.assertEquals("CLINTON", lowerThird.left!!.label)
        Assert.assertEquals(232, lowerThird.left!!.value.toLong())
        Assert.assertEquals(Color.RED, lowerThird.right!!.color)
        Assert.assertEquals("TRUMP", lowerThird.right!!.label)
        Assert.assertEquals(306, lowerThird.right!!.value.toLong())
    }

    @Test
    fun testThreePartySummary() {
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImageBinding = fixedBinding(createImage("", Color.RED, Color.WHITE)),
            placeBinding = fixedBinding(""),
            timezoneBinding = fixedBinding(ZoneId.systemDefault()),
            headlineBinding = fixedBinding("POLLS CLOSE ACROSS CENTRAL CANADA"),
            subheadBinding = fixedBinding("Polls open for 30 minutes on west coast"),
            summaryHeaderBinding = fixedBinding("270 TO WIN"),
            summaryTotalBinding = fixedBinding(538),
            summaryLeftBinding = fixedBinding(SummaryFromBothEnds.Entry(Color.BLUE, "HUMPHREY", 191)),
            summaryRightBinding = fixedBinding(SummaryFromBothEnds.Entry(Color.RED, "NIXON", 301)),
            summaryMiddleBinding = fixedBinding(SummaryFromBothEnds.Entry(Color.GRAY, "WALLACE", 46))
        )
        Assert.assertEquals(Color.BLUE, lowerThird.left!!.color)
        Assert.assertEquals("HUMPHREY", lowerThird.left!!.label)
        Assert.assertEquals(191, lowerThird.left!!.value.toLong())
        Assert.assertEquals(Color.RED, lowerThird.right!!.color)
        Assert.assertEquals("NIXON", lowerThird.right!!.label)
        Assert.assertEquals(301, lowerThird.right!!.value.toLong())
        Assert.assertEquals(Color.GRAY, lowerThird.middle!!.color)
        Assert.assertEquals("WALLACE", lowerThird.middle!!.label)
        Assert.assertEquals(46, lowerThird.middle!!.value.toLong())
    }

    @Test
    @Throws(IOException::class)
    fun testRenderRightWin() {
        val left = BindableWrapper(SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 0))
        val right = BindableWrapper(SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 0))
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImageBinding = fixedBinding(createImage("AMERICA VOTES", Color.WHITE, Color.RED)),
            placeBinding = fixedBinding("WASHINGTON"),
            timezoneBinding = fixedBinding(ZoneId.of("US/Eastern")),
            headlineBinding = fixedBinding("TRUMP WINS ELECTION"),
            subheadBinding = fixedBinding(null),
            summaryHeaderBinding = fixedBinding("270 TO WIN"),
            summaryTotalBinding = fixedBinding(538),
            summaryLeftBinding = left.binding,
            summaryRightBinding = right.binding,
            clock = Clock.fixed(Instant.parse("2016-11-09T06:00:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightWin-1", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 6)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 16)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightWin-2", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 82)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 56)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightWin-3", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 232)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 306)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightWin-4", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderLeftWin() {
        val left = BindableWrapper(SummaryFromBothEnds.Entry(Color.BLUE, "OBAMA", 0))
        val right = BindableWrapper(SummaryFromBothEnds.Entry(Color.RED, "MCCAIN", 0))
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImageBinding = fixedBinding(createImage("AMERICA VOTES", Color.WHITE, Color.RED)),
            placeBinding = fixedBinding("WASHINGTON"),
            timezoneBinding = fixedBinding(ZoneId.of("US/Eastern")),
            headlineBinding = fixedBinding("OBAMA WINS ELECTION"),
            subheadBinding = fixedBinding(null),
            summaryHeaderBinding = fixedBinding("270 TO WIN"),
            summaryTotalBinding = fixedBinding(538),
            summaryLeftBinding = left.binding,
            summaryRightBinding = right.binding,
            clock = Clock.fixed(Instant.parse("2008-11-05T06:00:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftWin-1", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "OBAMA", 10)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "MCCAIN", 7)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftWin-2", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "OBAMA", 153)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "MCCAIN", 28)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftWin-3", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "OBAMA", 365)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "MCCAIN", 173)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftWin-4", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderRightLandslide() {
        val left = BindableWrapper(SummaryFromBothEnds.Entry(Color.BLUE, "MONDALE", 0))
        val right = BindableWrapper(SummaryFromBothEnds.Entry(Color.RED, "REAGAN", 0))
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImageBinding = fixedBinding(createImage("AMERICA VOTES", Color.WHITE, Color.RED)),
            placeBinding = fixedBinding("WASHINGTON"),
            timezoneBinding = fixedBinding(ZoneId.of("US/Eastern")),
            headlineBinding = fixedBinding("REAGAN WINS RE-ELECTION"),
            subheadBinding = fixedBinding(null),
            summaryHeaderBinding = fixedBinding("270 TO WIN"),
            summaryTotalBinding = fixedBinding(538),
            summaryLeftBinding = left.binding,
            summaryRightBinding = right.binding,
            clock = Clock.fixed(Instant.parse("1984-11-07T06:00:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightLandslide-1", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "MONDALE", 3)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "REAGAN", 71)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightLandslide-2", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "MONDALE", 3)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "REAGAN", 280)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightLandslide-3", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "MONDALE", 13)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "REAGAN", 525)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightLandslide-4", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderLeftLandslide() {
        val left = BindableWrapper(SummaryFromBothEnds.Entry(Color.BLUE, "ROOSEVELT", 0))
        val right = BindableWrapper(SummaryFromBothEnds.Entry(Color.RED, "LANDON", 0))
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImageBinding = fixedBinding(createImage("AMERICA VOTES", Color.WHITE, Color.RED)),
            placeBinding = fixedBinding("WASHINGTON"),
            timezoneBinding = fixedBinding(ZoneId.of("US/Eastern")),
            headlineBinding = fixedBinding("ROOSEVELT WINS RE-ELECTION"),
            subheadBinding = fixedBinding(null),
            summaryHeaderBinding = fixedBinding("266 TO WIN"),
            summaryTotalBinding = fixedBinding(531),
            summaryLeftBinding = left.binding,
            summaryRightBinding = right.binding,
            clock = Clock.fixed(Instant.parse("1936-11-04T06:00:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftLandslide-1", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "ROOSEVELT", 169)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "LANDON", 0)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftLandslide-2", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "ROOSEVELT", 242)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "LANDON", 0)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftLandslide-3", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "ROOSEVELT", 523)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "LANDON", 8)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftLandslide-4", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderWithMiddleNoMajority() {
        val left = BindableWrapper(SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 27))
        val right = BindableWrapper(SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 40))
        val middle = BindableWrapper<SummaryFromBothEnds.Entry?>(null)
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImageBinding = fixedBinding(createImage("AMERICA VOTES", Color.WHITE, Color.RED)),
            placeBinding = fixedBinding("WASHINGTON"),
            timezoneBinding = fixedBinding(ZoneId.of("US/Eastern")),
            headlineBinding = fixedBinding("NO SENATE MAJORITY"),
            subheadBinding = fixedBinding(null),
            summaryHeaderBinding = fixedBinding("51 FOR CONTROL"),
            summaryTotalBinding = fixedBinding(100),
            summaryLeftBinding = left.binding,
            summaryRightBinding = right.binding,
            summaryMiddleBinding = middle.binding,
            clock = Clock.fixed(Instant.parse("2006-11-08T06:00:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleNoMajority-1", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 44)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 47)
        middle.value = SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 1)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleNoMajority-2", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 49)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 49)
        middle.value = SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 2)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleNoMajority-3", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderWithMiddleLeftMajority() {
        val left = BindableWrapper(SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 37))
        val right = BindableWrapper(SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 26))
        val middle = BindableWrapper(SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 2))
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImageBinding = fixedBinding(createImage("AMERICA VOTES", Color.WHITE, Color.RED)),
            placeBinding = fixedBinding("WASHINGTON"),
            timezoneBinding = fixedBinding(ZoneId.of("US/Eastern")),
            headlineBinding = fixedBinding("DEMOCRATIC SENATE MAJORITY"),
            subheadBinding = fixedBinding(null),
            summaryHeaderBinding = fixedBinding("51 FOR CONTROL"),
            summaryTotalBinding = fixedBinding(100),
            summaryLeftBinding = left.binding,
            summaryRightBinding = right.binding,
            summaryMiddleBinding = middle.binding,
            clock = Clock.fixed(Instant.parse("2008-11-05T06:00:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleLeftMajority-1", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 51)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 39)
        middle.value = SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 2)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleLeftMajority-2", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 57)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 41)
        middle.value = SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 2)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleLeftMajority-3", lowerThird)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderWithMiddleRightMajority() {
        val left = BindableWrapper(SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 29))
        val right = BindableWrapper(SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 36))
        val middle = BindableWrapper(SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 1))
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImageBinding = fixedBinding(createImage("AMERICA VOTES", Color.WHITE, Color.RED)),
            placeBinding = fixedBinding("WASHINGTON"),
            timezoneBinding = fixedBinding(ZoneId.of("US/Eastern")),
            headlineBinding = fixedBinding("REPUBLICAN SENATE MAJORITY"),
            subheadBinding = fixedBinding(null),
            summaryHeaderBinding = fixedBinding("51 FOR CONTROL"),
            summaryTotalBinding = fixedBinding(100),
            summaryLeftBinding = left.binding,
            summaryRightBinding = right.binding,
            summaryMiddleBinding = middle.binding,
            clock = Clock.fixed(Instant.parse("2004-11-03T06:00:00Z"), ZoneId.systemDefault())
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleRightMajority-1", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 43)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 49)
        middle.value = SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 1)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleRightMajority-2", lowerThird)
        left.value = SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 44)
        right.value = SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 55)
        middle.value = SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 1)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleRightMajority-3", lowerThird)
    }
}
