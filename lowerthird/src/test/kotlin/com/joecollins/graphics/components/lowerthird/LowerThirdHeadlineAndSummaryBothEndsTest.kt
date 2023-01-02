package com.joecollins.graphics.components.lowerthird

import com.joecollins.graphics.components.lowerthird.LowerThird.Companion.createImage
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.awt.Color
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class LowerThirdHeadlineAndSummaryBothEndsTest {
    @Test
    fun testHeadline() {
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "270 TO WIN".asOneTimePublisher(),
            summaryTotalPublisher = 538.asOneTimePublisher(),
            summaryLeftPublisher = SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 232).asOneTimePublisher(),
            summaryRightPublisher = SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 306).asOneTimePublisher(),
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.headline }, IsEqual("POLLS CLOSE ACROSS CENTRAL CANADA"))
    }

    @Test
    fun testSubhead() {
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "270 TO WIN".asOneTimePublisher(),
            summaryTotalPublisher = 538.asOneTimePublisher(),
            summaryLeftPublisher = SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 232).asOneTimePublisher(),
            summaryRightPublisher = SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 306).asOneTimePublisher(),
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.subhead }, IsEqual("Polls open for 30 minutes on west coast"))
    }

    @Test
    fun testSummaryPanel() {
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "270 TO WIN".asOneTimePublisher(),
            summaryTotalPublisher = 538.asOneTimePublisher(),
            summaryLeftPublisher = SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 232).asOneTimePublisher(),
            summaryRightPublisher = SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 306).asOneTimePublisher(),
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.summaryHeader }, IsEqual("270 TO WIN"))
        Assertions.assertEquals(538, lowerThird.total.toLong())
    }

    @Test
    fun testTwoPartySummary() {
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "270 TO WIN".asOneTimePublisher(),
            summaryTotalPublisher = 538.asOneTimePublisher(),
            summaryLeftPublisher = SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 232).asOneTimePublisher(),
            summaryRightPublisher = SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 306).asOneTimePublisher(),
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.left }, IsNot(IsNull()))
        Assertions.assertEquals(Color.BLUE, lowerThird.left!!.color)
        Assertions.assertEquals("CLINTON", lowerThird.left!!.label)
        Assertions.assertEquals(232, lowerThird.left!!.value.toLong())
        Assertions.assertEquals(Color.RED, lowerThird.right!!.color)
        Assertions.assertEquals("TRUMP", lowerThird.right!!.label)
        Assertions.assertEquals(306, lowerThird.right!!.value.toLong())
    }

    @Test
    fun testThreePartySummary() {
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImagePublisher = createImage("", Color.RED, Color.WHITE).asOneTimePublisher(),
            placePublisher = ("" to ZoneId.systemDefault()).asOneTimePublisher(),
            headlinePublisher = "POLLS CLOSE ACROSS CENTRAL CANADA".asOneTimePublisher(),
            subheadPublisher = "Polls open for 30 minutes on west coast".asOneTimePublisher(),
            summaryHeaderPublisher = "270 TO WIN".asOneTimePublisher(),
            summaryTotalPublisher = 538.asOneTimePublisher(),
            summaryLeftPublisher = SummaryFromBothEnds.Entry(Color.BLUE, "HUMPHREY", 191).asOneTimePublisher(),
            summaryRightPublisher = SummaryFromBothEnds.Entry(Color.RED, "NIXON", 301).asOneTimePublisher(),
            summaryMiddlePublisher = SummaryFromBothEnds.Entry(Color.GRAY, "WALLACE", 46).asOneTimePublisher(),
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ lowerThird.left }, IsNot(IsNull()))
        Assertions.assertEquals(Color.BLUE, lowerThird.left!!.color)
        Assertions.assertEquals("HUMPHREY", lowerThird.left!!.label)
        Assertions.assertEquals(191, lowerThird.left!!.value.toLong())
        Assertions.assertEquals(Color.RED, lowerThird.right!!.color)
        Assertions.assertEquals("NIXON", lowerThird.right!!.label)
        Assertions.assertEquals(301, lowerThird.right!!.value.toLong())
        Assertions.assertEquals(Color.GRAY, lowerThird.middle!!.color)
        Assertions.assertEquals("WALLACE", lowerThird.middle!!.label)
        Assertions.assertEquals(46, lowerThird.middle!!.value.toLong())
    }

    @Test
    fun testRenderRightWin() {
        val left = Publisher(SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 0))
        val right = Publisher(SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 0))
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImagePublisher = createImage("AMERICA VOTES", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = ("WASHINGTON" to ZoneId.of("US/Eastern")).asOneTimePublisher(),
            headlinePublisher = "TRUMP WINS ELECTION".asOneTimePublisher(),
            subheadPublisher = null.asOneTimePublisher(),
            summaryHeaderPublisher = "270 TO WIN".asOneTimePublisher(),
            summaryTotalPublisher = 538.asOneTimePublisher(),
            summaryLeftPublisher = left,
            summaryRightPublisher = right,
            clock = Clock.fixed(Instant.parse("2016-11-09T06:00:00Z"), ZoneId.systemDefault()),
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightWin-1", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 6))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 16))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightWin-2", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 82))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 56))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightWin-3", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "CLINTON", 232))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "TRUMP", 306))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightWin-4", lowerThird)
    }

    @Test
    fun testRenderLeftWin() {
        val left = Publisher(SummaryFromBothEnds.Entry(Color.BLUE, "OBAMA", 0))
        val right = Publisher(SummaryFromBothEnds.Entry(Color.RED, "MCCAIN", 0))
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImagePublisher = createImage("AMERICA VOTES", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = ("WASHINGTON" to ZoneId.of("US/Eastern")).asOneTimePublisher(),
            headlinePublisher = "OBAMA WINS ELECTION".asOneTimePublisher(),
            subheadPublisher = null.asOneTimePublisher(),
            summaryHeaderPublisher = "270 TO WIN".asOneTimePublisher(),
            summaryTotalPublisher = 538.asOneTimePublisher(),
            summaryLeftPublisher = left,
            summaryRightPublisher = right,
            clock = Clock.fixed(Instant.parse("2008-11-05T06:00:00Z"), ZoneId.systemDefault()),
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftWin-1", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "OBAMA", 10))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "MCCAIN", 7))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftWin-2", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "OBAMA", 153))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "MCCAIN", 28))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftWin-3", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "OBAMA", 365))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "MCCAIN", 173))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftWin-4", lowerThird)
    }

    @Test
    fun testRenderRightLandslide() {
        val left = Publisher(SummaryFromBothEnds.Entry(Color.BLUE, "MONDALE", 0))
        val right = Publisher(SummaryFromBothEnds.Entry(Color.RED, "REAGAN", 0))
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImagePublisher = createImage("AMERICA VOTES", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = ("WASHINGTON" to ZoneId.of("US/Eastern")).asOneTimePublisher(),
            headlinePublisher = "REAGAN WINS RE-ELECTION".asOneTimePublisher(),
            subheadPublisher = null.asOneTimePublisher(),
            summaryHeaderPublisher = "270 TO WIN".asOneTimePublisher(),
            summaryTotalPublisher = 538.asOneTimePublisher(),
            summaryLeftPublisher = left,
            summaryRightPublisher = right,
            clock = Clock.fixed(Instant.parse("1984-11-07T06:00:00Z"), ZoneId.systemDefault()),
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightLandslide-1", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "MONDALE", 3))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "REAGAN", 71))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightLandslide-2", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "MONDALE", 3))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "REAGAN", 280))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightLandslide-3", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "MONDALE", 13))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "REAGAN", 525))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-RightLandslide-4", lowerThird)
    }

    @Test
    fun testRenderLeftLandslide() {
        val left = Publisher(SummaryFromBothEnds.Entry(Color.BLUE, "ROOSEVELT", 0))
        val right = Publisher(SummaryFromBothEnds.Entry(Color.RED, "LANDON", 0))
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImagePublisher = createImage("AMERICA VOTES", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = ("WASHINGTON" to ZoneId.of("US/Eastern")).asOneTimePublisher(),
            headlinePublisher = "ROOSEVELT WINS RE-ELECTION".asOneTimePublisher(),
            subheadPublisher = null.asOneTimePublisher(),
            summaryHeaderPublisher = "266 TO WIN".asOneTimePublisher(),
            summaryTotalPublisher = 531.asOneTimePublisher(),
            summaryLeftPublisher = left,
            summaryRightPublisher = right,
            clock = Clock.fixed(Instant.parse("1936-11-04T06:00:00Z"), ZoneId.systemDefault()),
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftLandslide-1", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "ROOSEVELT", 169))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "LANDON", 0))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftLandslide-2", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "ROOSEVELT", 242))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "LANDON", 0))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftLandslide-3", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "ROOSEVELT", 523))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "LANDON", 8))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-LeftLandslide-4", lowerThird)
    }

    @Test
    fun testRenderWithMiddleNoMajority() {
        val left = Publisher(SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 27))
        val right = Publisher(SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 40))
        val middle = Publisher<SummaryFromBothEnds.Entry?>(null)
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImagePublisher = createImage("AMERICA VOTES", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = ("WASHINGTON" to ZoneId.of("US/Eastern")).asOneTimePublisher(),
            headlinePublisher = "NO SENATE MAJORITY".asOneTimePublisher(),
            subheadPublisher = null.asOneTimePublisher(),
            summaryHeaderPublisher = "51 FOR CONTROL".asOneTimePublisher(),
            summaryTotalPublisher = 100.asOneTimePublisher(),
            summaryLeftPublisher = left,
            summaryRightPublisher = right,
            summaryMiddlePublisher = middle,
            clock = Clock.fixed(Instant.parse("2006-11-08T06:00:00Z"), ZoneId.systemDefault()),
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleNoMajority-1", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 44))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 47))
        middle.submit(SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 1))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleNoMajority-2", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 49))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 49))
        middle.submit(SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 2))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleNoMajority-3", lowerThird)
    }

    @Test
    fun testRenderWithMiddleLeftMajority() {
        val left = Publisher(SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 37))
        val right = Publisher(SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 26))
        val middle = Publisher(SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 2))
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImagePublisher = createImage("AMERICA VOTES", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = ("WASHINGTON" to ZoneId.of("US/Eastern")).asOneTimePublisher(),
            headlinePublisher = "DEMOCRATIC SENATE MAJORITY".asOneTimePublisher(),
            subheadPublisher = null.asOneTimePublisher(),
            summaryHeaderPublisher = "51 FOR CONTROL".asOneTimePublisher(),
            summaryTotalPublisher = 100.asOneTimePublisher(),
            summaryLeftPublisher = left,
            summaryRightPublisher = right,
            summaryMiddlePublisher = middle,
            clock = Clock.fixed(Instant.parse("2008-11-05T06:00:00Z"), ZoneId.systemDefault()),
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleLeftMajority-1", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 51))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 39))
        middle.submit(SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 2))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleLeftMajority-2", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 57))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 41))
        middle.submit(SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 2))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleLeftMajority-3", lowerThird)
    }

    @Test
    fun testRenderWithMiddleRightMajority() {
        val left = Publisher(SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 29))
        val right = Publisher(SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 36))
        val middle = Publisher(SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 1))
        val lowerThird = LowerThirdHeadlineAndSummaryBothEnds(
            leftImagePublisher = createImage("AMERICA VOTES", Color.WHITE, Color.RED).asOneTimePublisher(),
            placePublisher = ("WASHINGTON" to ZoneId.of("US/Eastern")).asOneTimePublisher(),
            headlinePublisher = "REPUBLICAN SENATE MAJORITY".asOneTimePublisher(),
            subheadPublisher = null.asOneTimePublisher(),
            summaryHeaderPublisher = "51 FOR CONTROL".asOneTimePublisher(),
            summaryTotalPublisher = 100.asOneTimePublisher(),
            summaryLeftPublisher = left,
            summaryRightPublisher = right,
            summaryMiddlePublisher = middle,
            clock = Clock.fixed(Instant.parse("2004-11-03T06:00:00Z"), ZoneId.systemDefault()),
        )
        lowerThird.setSize(1024, 50)
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleRightMajority-1", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 43))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 49))
        middle.submit(SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 1))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleRightMajority-2", lowerThird)
        left.submit(SummaryFromBothEnds.Entry(Color.BLUE, "DEMOCRATS", 44))
        right.submit(SummaryFromBothEnds.Entry(Color.RED, "REPUBLICANS", 55))
        middle.submit(SummaryFromBothEnds.Entry(Color.GRAY, "OTHERS", 1))
        compareRendering("LowerThird", "HeadlineAndSummaryBothEnds-MiddleRightMajority-3", lowerThird)
    }
}
