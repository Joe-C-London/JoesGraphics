package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color

class BattlefieldFrameTest {

    @Test
    fun testRenderBattlefieldWithDots() {
        val frame = BattlefieldFrame(
            headerPublisher = "PEI 2019 BATTLEFIELD".asOneTimePublisher(),
            limitPublisher = 80.asOneTimePublisher(),
            incrementPublisher = 10.asOneTimePublisher(),
            dotsPublisher = listOf(
                BattlefieldFrame.Dot(-45.1, -41.1, -5.7) to Color.BLUE,
                BattlefieldFrame.Dot(-48.0, -38.8, -4.8) to Color.BLUE,
                BattlefieldFrame.Dot(-31.0, -41.8, -4.2) to Color.RED,
                BattlefieldFrame.Dot(-50.0, -37.1, -5.9) to Color.BLUE,
                BattlefieldFrame.Dot(-44.4, -35.8, 0.0) to Color.BLUE,
                BattlefieldFrame.Dot(-50.3, -33.9, -7.7) to Color.BLUE,
                BattlefieldFrame.Dot(-41.3, -41.3, -8.2) to Color.RED,
                BattlefieldFrame.Dot(-47.1, -34.1, -15.1) to Color.BLUE,
                BattlefieldFrame.Dot(-33.8, -46.3, -12.1) to Color.RED,
                BattlefieldFrame.Dot(-16.1, -27.6, -54.8) to Color.GREEN.darker(),
                BattlefieldFrame.Dot(-54.3, -28.3, -10.2) to Color.BLUE,
                BattlefieldFrame.Dot(-47.2, -34.3, -9.7) to Color.BLUE,
                BattlefieldFrame.Dot(-32.9, -47.7, -8.5) to Color.RED,
                BattlefieldFrame.Dot(-38.2, -39.0, -13.0) to Color.RED,
                BattlefieldFrame.Dot(-27.0, -34.3, -8.0) to Color.RED,
                BattlefieldFrame.Dot(-26.2, -43.7, -19.2) to Color.RED,
                BattlefieldFrame.Dot(-33.1, -45.8, -9.5) to Color.RED,
                BattlefieldFrame.Dot(-27.5, -39.4, -18.8) to Color.RED,
                BattlefieldFrame.Dot(-27.8, -45.7, -8.0) to Color.RED,
                BattlefieldFrame.Dot(-36.0, -37.6, -12.5) to Color.RED,
                BattlefieldFrame.Dot(-39.9, -53.7, 0.0) to Color.RED,
                BattlefieldFrame.Dot(-25.8, -62.6, -5.5) to Color.RED,
                BattlefieldFrame.Dot(-39.6, -48.8, 0.0) to Color.RED,
                BattlefieldFrame.Dot(-36.3, -41.2, -10.6) to Color.RED,
                BattlefieldFrame.Dot(-38.4, -39.4, -9.9) to Color.RED,
                BattlefieldFrame.Dot(-32.1, -58.2, -6.5) to Color.RED,
                BattlefieldFrame.Dot(-30.3, -43.0, -9.0) to Color.RED
            ).asOneTimePublisher()
        )
        frame.setSize(1024, 512)
        RenderTestUtils.compareRendering("BattlefieldFrame", "WithDots", frame)
    }

    @Test
    fun testRenderBattlefieldWithDotsAndLines() {
        val frame = BattlefieldFrame(
            headerPublisher = "PEI 2019 BATTLEFIELD".asOneTimePublisher(),
            limitPublisher = 80.asOneTimePublisher(),
            incrementPublisher = 10.asOneTimePublisher(),
            swingPublisher = BattlefieldFrame.Dot(-0.66, -11.43, +19.75).asOneTimePublisher(),
            dotsPublisher = listOf(
                BattlefieldFrame.Dot(-45.1, -41.1, -5.7) to Color.BLUE,
                BattlefieldFrame.Dot(-48.0, -38.8, -4.8) to Color.BLUE,
                BattlefieldFrame.Dot(-31.0, -41.8, -4.2) to Color.BLUE,
                BattlefieldFrame.Dot(-50.0, -37.1, -5.9) to Color.BLUE,
                BattlefieldFrame.Dot(-44.4, -35.8, 0.0) to Color.BLUE,
                BattlefieldFrame.Dot(-50.3, -33.9, -7.7) to Color.BLUE,
                BattlefieldFrame.Dot(-41.3, -41.3, -8.2) to Color.GREEN.darker(),
                BattlefieldFrame.Dot(-47.1, -34.1, -15.1) to Color.BLUE,
                BattlefieldFrame.Dot(-33.8, -46.3, -12.1) to Color.RED,
                BattlefieldFrame.Dot(-16.1, -27.6, -54.8) to Color.GREEN.darker(),
                BattlefieldFrame.Dot(-54.3, -28.3, -10.2) to Color.BLUE,
                BattlefieldFrame.Dot(-47.2, -34.3, -9.7) to Color.BLUE,
                BattlefieldFrame.Dot(-32.9, -47.7, -8.5) to Color.BLUE,
                BattlefieldFrame.Dot(-38.2, -39.0, -13.0) to Color.GREEN.darker(),
                BattlefieldFrame.Dot(-27.0, -34.3, -8.0) to Color.RED,
                BattlefieldFrame.Dot(-26.2, -43.7, -19.2) to Color.GREEN.darker(),
                BattlefieldFrame.Dot(-33.1, -45.8, -9.5) to Color.RED,
                BattlefieldFrame.Dot(-27.5, -39.4, -18.8) to Color.GREEN.darker(),
                BattlefieldFrame.Dot(-27.8, -45.7, -8.0) to Color.BLUE,
                BattlefieldFrame.Dot(-36.0, -37.6, -12.5) to Color.BLUE,
                BattlefieldFrame.Dot(-39.9, -53.7, 0.0) to Color.BLUE,
                BattlefieldFrame.Dot(-25.8, -62.6, -5.5) to Color.RED,
                BattlefieldFrame.Dot(-39.6, -48.8, 0.0) to Color.RED,
                BattlefieldFrame.Dot(-36.3, -41.2, -10.6) to Color.GREEN.darker(),
                BattlefieldFrame.Dot(-38.4, -39.4, -9.9) to Color.GREEN.darker(),
                BattlefieldFrame.Dot(-32.1, -58.2, -6.5) to Color.RED,
                BattlefieldFrame.Dot(-30.3, -43.0, -9.0) to Color.GREEN.darker()
            ).asOneTimePublisher(),
            linesPublisher = listOf(
                BattlefieldFrame.Line(
                    listOf(
                        BattlefieldFrame.Dot(11.9, 0.0, -27.2),
                        BattlefieldFrame.Dot(10.8, 0.0, 0.0),
                        BattlefieldFrame.Dot(9.2, 0.0, 20.6),
                        BattlefieldFrame.Dot(7.3, 0.0, 24.5),
                        BattlefieldFrame.Dot(4.9, 0.0, 25.1),
                        BattlefieldFrame.Dot(1.6, 0.0, 26.0),
                        BattlefieldFrame.Dot(1.0, 0.0, 26.3),
                        BattlefieldFrame.Dot(0.8, 0.0, 29.5),
                        BattlefieldFrame.Dot(0.0, 0.0, 30.6),
                        BattlefieldFrame.Dot(-0.0, 0.0, 33.1),
                        BattlefieldFrame.Dot(-4.0, 0.0, 34.0),
                        BattlefieldFrame.Dot(-8.6, 0.0, 34.2),
                        BattlefieldFrame.Dot(-9.2, 0.0, 36.3),
                        BattlefieldFrame.Dot(-12.9, 0.0, 37.6),
                        BattlefieldFrame.Dot(-12.9, 0.0, 37.7),
                        BattlefieldFrame.Dot(-13.0, 0.0, 39.2),
                        BattlefieldFrame.Dot(-16.4, 0.0, 48.8),
                        BattlefieldFrame.Dot(-26.0, 0.0, 51.7)
                    )
                ) to Color.RED,
                BattlefieldFrame.Line(
                    listOf(
                        BattlefieldFrame.Dot(0.0, -4.9, -38.7),
                        BattlefieldFrame.Dot(0.0, -7.3, 0.0),
                        BattlefieldFrame.Dot(0.0, -9.2, 32.0),
                        BattlefieldFrame.Dot(0.0, -10.8, 37.5),
                        BattlefieldFrame.Dot(0.0, -11.9, 39.4),
                        BattlefieldFrame.Dot(0.0, -12.5, 42.6),
                        BattlefieldFrame.Dot(0.0, -12.7, 43.2),
                        BattlefieldFrame.Dot(0.0, -12.7, 44.1),
                        BattlefieldFrame.Dot(0.0, -13.8, 44.1),
                        BattlefieldFrame.Dot(0.0, -14.8, 44.4)
                    )
                ) to Color.BLUE,
                BattlefieldFrame.Line(
                    listOf(
                        BattlefieldFrame.Dot(38.7, -37.7, 0.0),
                        BattlefieldFrame.Dot(0.0, -37.6, 0.0),
                        BattlefieldFrame.Dot(-32.0, -36.3, 0.0),
                        BattlefieldFrame.Dot(-37.5, -34.2, 0.0),
                        BattlefieldFrame.Dot(-39.4, -34.0, 0.0),
                        BattlefieldFrame.Dot(-42.6, -33.1, 0.0),
                        BattlefieldFrame.Dot(-43.2, -30.6, 0.0),
                        BattlefieldFrame.Dot(-44.1, -29.5, 0.0),
                        BattlefieldFrame.Dot(-44.1, -26.3, 0.0),
                        BattlefieldFrame.Dot(-44.4, -26.0, 0.0)
                    )
                ) to Color.GREEN.darker()
            ).asOneTimePublisher()
        )
        frame.setSize(1024, 512)
        RenderTestUtils.compareRendering("BattlefieldFrame", "WithDotsAndLines", frame)
    }
}