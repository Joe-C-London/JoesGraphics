package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color

class DescriptionFrameTest {

    @Test
    fun renderBasic() {
        val frame = DescriptionFrame(
            header = "HOW DO MIDTERMS WORK?".asOneTimePublisher(),
            text = "The House of Representatives is elected in its entirety every two years.\n\nThe Senate is elected in thirds every two years, with Senators serving six-year terms.".asOneTimePublisher(),
        )
        frame.setSize(512, 256)
        RenderTestUtils.compareRendering("DescriptionFrame", "Basic", frame)
    }

    @Test
    fun renderWithColor() {
        val frame = DescriptionFrame(
            header = "HOW DO MIDTERMS WORK?".asOneTimePublisher(),
            text = "The House of Representatives is elected in its entirety every two years.\n\nThe Senate is elected in thirds every two years, with Senators serving six-year terms.".asOneTimePublisher(),
            colorPublisher = Color.MAGENTA.asOneTimePublisher(),
        )
        frame.setSize(512, 256)
        RenderTestUtils.compareRendering("DescriptionFrame", "WithColor", frame)
    }
}
