package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
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
        val screen = CountdownScreen.forDateWithMap(LocalDate.of(2023, 10, 2), peiShapesByDistrict().asOneTimePublisher())
            .atTime("PEI", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic")) { true }
            .withClock(Clock.fixed(Instant.parse("2022-08-29T18:58:39.300Z"), ZoneId.of("UTC")))
            .build("COUNTDOWN TO THE CLOSE".asOneTimePublisher())
        screen.size = Dimension(1024, 512)
        compareRendering("CountdownScreen", "SingleCountdown", screen)
        assertPublishes(
            screen.altText,
            """
            COUNTDOWN TO THE CLOSE
            
            PEI: 399:03:01:21
            """.trimIndent(),
        )
    }

    @Test
    fun testSingleCountdownAsCollection() {
        val screen = CountdownScreen.forDateWithMapSingle(LocalDate.of(2023, 10, 2), peiShapesByDistrict().values.asOneTimePublisher())
            .atTime("PEI", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic"))
            .withClock(Clock.fixed(Instant.parse("2022-08-29T18:58:39.300Z"), ZoneId.of("UTC")))
            .build("COUNTDOWN TO THE CLOSE".asOneTimePublisher())
        screen.size = Dimension(1024, 512)
        compareRendering("CountdownScreen", "SingleCountdownAsCollection", screen)
        assertPublishes(
            screen.altText,
            """
            COUNTDOWN TO THE CLOSE
            
            PEI: 399:03:01:21
            """.trimIndent(),
        )
    }

    @Test
    fun testDoubleCountdownSameTime() {
        val screen = CountdownScreen.forDateWithMap(LocalDate.of(2023, 10, 2), peiShapesByDistrict().asOneTimePublisher())
            .atTime("CHARLOTTETOWN", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic")) { (10..14).contains(it) }
            .atTime("REST OF PEI", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic")) { !(10..14).contains(it) }
            .withClock(Clock.fixed(Instant.parse("2022-08-29T18:58:39.300Z"), ZoneId.of("UTC")))
            .build("COUNTDOWN TO THE CLOSE".asOneTimePublisher())
        screen.size = Dimension(1024, 512)
        compareRendering("CountdownScreen", "DoubleCountdownSameTime", screen)
        assertPublishes(
            screen.altText,
            """
            COUNTDOWN TO THE CLOSE
            
            CHARLOTTETOWN: 399:03:01:21
            REST OF PEI: 399:03:01:21
            """.trimIndent(),
        )
    }

    @Test
    fun testQuadrupleCountdownDifferentTimes() {
        val screen = CountdownScreen.forDateWithMap(LocalDate.of(2023, 10, 2), peiShapesByDistrict().asOneTimePublisher())
            .atTime("EGMONT", LocalTime.of(20, 0), ZoneId.of("Canada/Atlantic")) { (21..27).contains(it) }
            .atTime("MALPEQUE", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic")) { it == 8 || (15..20).contains(it) }
            .atTime("CHARLOTTETOWN", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic")) { (9..14).contains(it) }
            .atTime("CARDIGAN", LocalTime.of(19, 0), ZoneId.of("Canada/Atlantic")) { (1..7).contains(it) }
            .withClock(Clock.fixed(Instant.parse("2022-08-29T18:58:39.300Z"), ZoneId.of("UTC")))
            .build("COUNTDOWN TO THE CLOSE".asOneTimePublisher())
        screen.size = Dimension(1024, 512)
        compareRendering("CountdownScreen", "QuadrupleCountdownDifferentTimes", screen)
        assertPublishes(
            screen.altText,
            """
            COUNTDOWN TO THE CLOSE
            
            EGMONT: 399:04:01:21
            MALPEQUE: 399:03:01:21
            CHARLOTTETOWN: 399:03:01:21
            CARDIGAN: 399:03:01:21
            """.trimIndent(),
        )
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
        compareRendering("CountdownScreen", "SixWithoutMap", screen)
        assertPublishes(
            screen.altText,
            """
            COUNTDOWN TO THE CLOSE
            
            BC/YUKON: 22:07:01:21
            PRAIRIES/NORTH: 22:06:31:21
            ONTARIO/QUÉBEC: 22:06:31:21
            GASPÉ/MADELEINE: 22:05:31:21
            MARITIMES: 22:04:31:21
            NEWFOUNDLAND: 22:04:01:21
            """.trimIndent(),
        )
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
        compareRendering("CountdownScreen", "CompletionLabel", screen)
        assertPublishes(
            screen.altText,
            """
            COUNTDOWN TO THE CLOSE
            
            BC/YUKON: 1:31:21
            PRAIRIES/NORTH: 1:01:21
            ONTARIO/QUÉBEC: 1:01:21
            GASPÉ/MADELEINE: 1:21
            MARITIMES: POLLS CLOSED
            NEWFOUNDLAND: POLLS CLOSED
            """.trimIndent(),
        )
    }

    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = CountdownScreenTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return ShapefileReader.readShapes(peiMap, "DIST_NO", Int::class.java)
    }
}
