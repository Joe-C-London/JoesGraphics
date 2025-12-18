package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.CountdownScreen.Companion.timeWithMapFilter
import com.joecollins.graphics.screens.generic.CountdownScreen.Companion.timeWithoutMapFilter
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Dimension
import java.awt.Shape
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime

class CountdownScreenTest {

    @Test
    fun testSingleCountdown() {
        val screen = CountdownScreen.forDateWithMap(
            date = LocalDate.of(2023, 10, 2),
            times = listOf(
                timeWithMapFilter {
                    header = "PEI"
                    time = LocalTime.of(19, 0)
                    zone = ZoneId.of("Canada/Atlantic")
                },
            ),
            map = peiShapesByDistrict().asOneTimePublisher(),
            timesUpLabel = "POLLS CLOSED",
            title = "COUNTDOWN TO THE CLOSE".asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2022-08-29T18:58:39.300Z"), ZoneId.of("UTC")),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CountdownScreen", "SingleCountdown", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            COUNTDOWN TO THE CLOSE
            
            PEI: 399:03:01:21
            """.trimIndent(),
        )
    }

    @Test
    fun testSingleCountdownAsCollection() {
        val screen = CountdownScreen.forDateWithMapSingle(
            timestamp = ZonedDateTime.of(
                LocalDate.of(2023, 10, 2),
                LocalTime.of(19, 0),
                ZoneId.of("Canada/Atlantic"),
            ),
            header = "PEI",
            map = peiShapesByDistrict().values.asOneTimePublisher(),
            timesUpLabel = "POLLS CLOSED",
            title = "COUNTDOWN TO THE CLOSE".asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2022-08-29T18:58:39.300Z"), ZoneId.of("UTC")),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CountdownScreen", "SingleCountdownAsCollection", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            COUNTDOWN TO THE CLOSE
            
            PEI: 399:03:01:21
            """.trimIndent(),
        )
    }

    @Test
    fun testDoubleCountdownSameTime() {
        val screen = CountdownScreen.forDateWithMap(
            date = LocalDate.of(2023, 10, 2),
            times = listOf(
                timeWithMapFilter {
                    header = "CHARLOTTETOWN"
                    time = LocalTime.of(19, 0)
                    zone = ZoneId.of("Canada/Atlantic")
                    filter = { (10..14).contains(this) }
                },
                timeWithMapFilter {
                    header = "REST OF PEI"
                    time = LocalTime.of(19, 0)
                    zone = ZoneId.of("Canada/Atlantic")
                    filter = { !(10..14).contains(this) }
                },
            ),
            map = peiShapesByDistrict().asOneTimePublisher(),
            timesUpLabel = "POLLS CLOSED",
            title = "COUNTDOWN TO THE CLOSE".asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2022-08-29T18:58:39.300Z"), ZoneId.of("UTC")),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CountdownScreen", "DoubleCountdownSameTime", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            COUNTDOWN TO THE CLOSE
            
            CHARLOTTETOWN: 399:03:01:21
            REST OF PEI: 399:03:01:21
            """.trimIndent(),
        )
    }

    @Test
    fun testQuadrupleCountdownDifferentTimes() {
        val screen = CountdownScreen.forDateWithMap(
            date = LocalDate.of(2023, 10, 2),
            times = listOf(
                timeWithMapFilter {
                    header = "EGMONT"
                    time = LocalTime.of(20, 0)
                    zone = ZoneId.of("Canada/Atlantic")
                    filter = { (21..27).contains(this) }
                },
                timeWithMapFilter {
                    header = "MALPEQUE"
                    time = LocalTime.of(19, 0)
                    zone = ZoneId.of("Canada/Atlantic")
                    filter = { this == 8 || (15..20).contains(this) }
                },
                timeWithMapFilter {
                    header = "CHARLOTTETOWN"
                    time = LocalTime.of(19, 0)
                    zone = ZoneId.of("Canada/Atlantic")
                    filter = { (9..14).contains(this) }
                },
                timeWithMapFilter {
                    header = "CARDIGAN"
                    time = LocalTime.of(19, 0)
                    zone = ZoneId.of("Canada/Atlantic")
                    filter = { (1..7).contains(this) }
                },
            ),
            map = peiShapesByDistrict().asOneTimePublisher(),
            timesUpLabel = "POLLS CLOSED",
            title = "COUNTDOWN TO THE CLOSE".asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2022-08-29T18:58:39.300Z"), ZoneId.of("UTC")),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CountdownScreen", "QuadrupleCountdownDifferentTimes", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
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
        val screen = CountdownScreen.forDate(
            date = LocalDate.of(2021, 9, 20),
            times = listOf(
                timeWithoutMapFilter {
                    header = "BC/YUKON"
                    time = LocalTime.of(19, 0)
                    zone = ZoneId.of("Canada/Pacific")
                },
                timeWithoutMapFilter {
                    header = "PRAIRIES/NORTH"
                    time = LocalTime.of(19, 30)
                    zone = ZoneId.of("Canada/Mountain")
                },
                timeWithoutMapFilter {
                    header = "ONTARIO/QUÉBEC"
                    time = LocalTime.of(21, 30)
                    zone = ZoneId.of("Canada/Eastern")
                },
                timeWithoutMapFilter {
                    header = "GASPÉ/MADELEINE"
                    time = LocalTime.of(20, 30)
                    zone = ZoneId.of("Canada/Eastern")
                },
                timeWithoutMapFilter {
                    header = "MARITIMES"
                    time = LocalTime.of(20, 30)
                    zone = ZoneId.of("Canada/Atlantic")
                },
                timeWithoutMapFilter {
                    header = "NEWFOUNDLAND"
                    time = LocalTime.of(20, 30)
                    zone = ZoneId.of("Canada/Newfoundland")
                },
            ),
            timesUpLabel = "POLLS CLOSED",
            title = "COUNTDOWN TO THE CLOSE".asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2021-08-29T18:58:39.300Z"), ZoneId.of("UTC")),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CountdownScreen", "SixWithoutMap", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
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
        val screen = CountdownScreen.forDate(
            date = LocalDate.of(2021, 9, 20),
            times = listOf(
                timeWithoutMapFilter {
                    header = "BC/YUKON"
                    time = LocalTime.of(19, 0)
                    zone = ZoneId.of("Canada/Pacific")
                },
                timeWithoutMapFilter {
                    header = "PRAIRIES/NORTH"
                    time = LocalTime.of(19, 30)
                    zone = ZoneId.of("Canada/Mountain")
                },
                timeWithoutMapFilter {
                    header = "ONTARIO/QUÉBEC"
                    time = LocalTime.of(21, 30)
                    zone = ZoneId.of("Canada/Eastern")
                },
                timeWithoutMapFilter {
                    header = "GASPÉ/MADELEINE"
                    time = LocalTime.of(20, 30)
                    zone = ZoneId.of("Canada/Eastern")
                },
                timeWithoutMapFilter {
                    header = "MARITIMES"
                    time = LocalTime.of(20, 30)
                    zone = ZoneId.of("Canada/Atlantic")
                },
                timeWithoutMapFilter {
                    header = "NEWFOUNDLAND"
                    time = LocalTime.of(20, 30)
                    zone = ZoneId.of("Canada/Newfoundland")
                },
            ),
            timesUpLabel = "POLLS CLOSED",
            title = "COUNTDOWN TO THE CLOSE".asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2021-09-21T00:28:39.300Z"), ZoneId.of("UTC")),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CountdownScreen", "CompletionLabel", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
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

    @Test
    fun testDoubleCountdownDifferentDates() {
        val screen = CountdownScreen.forDateWithMap(
            date = LocalDate.of(2023, 10, 2),
            times = listOf(
                timeWithMapFilter {
                    header = "PRINCE EDWARD ISLAND"
                    time = LocalTime.of(19, 0)
                    zone = ZoneId.of("Canada/Atlantic")
                    filter = { this != 10 }
                },
                timeWithMapFilter {
                    header = "BY-ELECTION IN DISTRICT 10"
                    futureDate = Period.ofDays(14)
                    time = LocalTime.of(19, 0)
                    zone = ZoneId.of("Canada/Atlantic")
                    filter = { this == 10 }
                },
            ),
            map = peiShapesByDistrict().asOneTimePublisher(),
            timesUpLabel = "POLLS CLOSED",
            title = "COUNTDOWN TO THE CLOSE".asOneTimePublisher(),
            clock = Clock.fixed(Instant.parse("2022-08-29T18:58:39.300Z"), ZoneId.of("UTC")),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CountdownScreen", "DoubleCountdownDifferentDates", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            COUNTDOWN TO THE CLOSE
            
            PRINCE EDWARD ISLAND: 399:03:01:21
            BY-ELECTION IN DISTRICT 10: 413:03:01:21
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
