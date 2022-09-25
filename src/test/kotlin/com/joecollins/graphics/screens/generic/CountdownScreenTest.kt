package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrameTest
import com.joecollins.graphics.utils.RenderTestUtils
import com.joecollins.graphics.utils.ShapefileReader
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.Test
import java.awt.Dimension
import java.awt.Shape
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class CountdownScreenTest {

    @Test
    fun testSingleCountdown() {
        val screen = CountdownScreen.forDateWithMap(LocalDate.of(2023, 10, 2), peiShapesByDistrict())
            .atTime("PEI", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic")) { true }
            .withClock(Clock.fixed(Instant.parse("2022-08-29T18:58:39.300Z"), ZoneId.of("UTC")))
            .build("COUNTDOWN TO THE CLOSE".asOneTimePublisher())
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CountdownScreen", "SingleCountdown", screen, 10)
    }

    @Test
    fun testSingleCountdownAsCollection() {
        val screen = CountdownScreen.forDateWithMap(LocalDate.of(2023, 10, 2), peiShapesByDistrict().values)
            .atTime("PEI", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic"))
            .withClock(Clock.fixed(Instant.parse("2022-08-29T18:58:39.300Z"), ZoneId.of("UTC")))
            .build("COUNTDOWN TO THE CLOSE".asOneTimePublisher())
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CountdownScreen", "SingleCountdownAsCollection", screen)
    }

    @Test
    fun testDoubleCountdownSameTime() {
        val screen = CountdownScreen.forDateWithMap(LocalDate.of(2023, 10, 2), peiShapesByDistrict())
            .atTime("CHARLOTTETOWN", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic")) { (10..14).contains(it) }
            .atTime("REST OF PEI", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic")) { !(10..14).contains(it) }
            .withClock(Clock.fixed(Instant.parse("2022-08-29T18:58:39.300Z"), ZoneId.of("UTC")))
            .build("COUNTDOWN TO THE CLOSE".asOneTimePublisher())
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CountdownScreen", "DoubleCountdownSameTime", screen, 10)
    }

    @Test
    fun testQuadrupleCountdownDifferentTimes() {
        val screen = CountdownScreen.forDateWithMap(LocalDate.of(2023, 10, 2), peiShapesByDistrict())
            .atTime("EGMONT", LocalTime.of(20, 0), ZoneId.of("Canada/Atlantic")) { (21..27).contains(it) }
            .atTime("MALPEQUE", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic")) { it == 8 || (15..20).contains(it) }
            .atTime("CHARLOTTETOWN", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic")) { (9..14).contains(it) }
            .atTime("CARDIGAN", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic")) { (1..7).contains(it) }
            .withClock(Clock.fixed(Instant.parse("2022-08-29T18:58:39.300Z"), ZoneId.of("UTC")))
            .build("COUNTDOWN TO THE CLOSE".asOneTimePublisher())
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CountdownScreen", "QuadrupleCountdownDifferentTimes", screen)
    }

    @Test
    fun testSixWithoutMap() {
        val screen = CountdownScreen.forDate(LocalDate.of(2021, 9, 20))
            .atTime("BC/YUKON", LocalTime.of(19, 0), ZoneId.of("Canada/Pacific"))
            .atTime("PRAIRIES/NORTH", LocalTime.of(19, 30), ZoneId.of("Canada/Mountain"))
            .atTime("ONTARIO/QUÉBEC", LocalTime.of(21, 30), ZoneId.of("Canada/Eastern"))
            .atTime("GASPÉ/MADELEINE", LocalTime.of(20, 30), ZoneId.of("Canada/Eastern"))
            .atTime("MARITIMES", LocalTime.of(20, 30), ZoneId.of("Canada/Atlantic"))
            .atTime("NEWFOUNDLAND", LocalTime.of(20, 30), ZoneId.of("Canada/Newfoundland"))
            .withClock(Clock.fixed(Instant.parse("2021-08-29T18:58:39.300Z"), ZoneId.of("UTC")))
            .build("COUNTDOWN TO THE CLOSE".asOneTimePublisher())
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CountdownScreen", "SixWithoutMap", screen)
    }

    @Test
    fun testCompletionLabel() {
        val screen = CountdownScreen.forDate(LocalDate.of(2021, 9, 20))
            .atTime("BC/YUKON", LocalTime.of(19, 0), ZoneId.of("Canada/Pacific"))
            .atTime("PRAIRIES/NORTH", LocalTime.of(19, 30), ZoneId.of("Canada/Mountain"))
            .atTime("ONTARIO/QUÉBEC", LocalTime.of(21, 30), ZoneId.of("Canada/Eastern"))
            .atTime("GASPÉ/MADELEINE", LocalTime.of(20, 30), ZoneId.of("Canada/Eastern"))
            .atTime("MARITIMES", LocalTime.of(20, 30), ZoneId.of("Canada/Atlantic"))
            .atTime("NEWFOUNDLAND", LocalTime.of(20, 30), ZoneId.of("Canada/Newfoundland"))
            .withClock(Clock.fixed(Instant.parse("2021-09-21T00:28:39.300Z"), ZoneId.of("UTC")))
            .withTimesUpLabel("POLLS CLOSED")
            .build("COUNTDOWN TO THE CLOSE".asOneTimePublisher())
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CountdownScreen", "CompletionLabel", screen)
    }

    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = MapFrameTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return ShapefileReader.readShapes(peiMap, "DIST_NO", Int::class.java)
    }
}
