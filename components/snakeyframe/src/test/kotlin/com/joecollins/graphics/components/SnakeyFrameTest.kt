package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color
import java.util.concurrent.Flow
import javax.swing.JPanel

class SnakeyFrameTest {

    @Test
    fun testBasic() {
        val lib = "LIB" to Color.RED
        val pc = "PC" to Color.BLUE
        val ndp = "NDP" to Color.ORANGE
        val bq = "BQ" to Color.CYAN
        val ref = "REF" to Color.GREEN.darker().darker()
        val ind = "IND" to Color.DARK_GRAY

        val parties1988 = listOf(pc, lib, ndp)
        val parties1993 = listOf(pc, lib, bq, ref, ndp, ind)
        val transfers = mapOf(
            (pc to ref) to 35,
            (ndp to lib) to 17,
            (pc to lib) to 79,
            (ndp to ndp) to 9,
            (ndp to ref) to 17,
            (lib to lib) to 81,
            (pc to pc) to 2,
            (pc to bq) to 52,
            (pc to ind) to 1,
            (lib to bq) to 2,
        )
        val frame = SnakeyFrame.create(
            parties1988.asOneTimePublisher(),
            parties1993.asOneTimePublisher(),
            transfers.asOneTimePublisher(),
            { first },
            { second },
            { first },
            { second },
            "SEAT CHANGES".asOneTimePublisher(),
        )
        frame.setSize(1024, 512)
        compareRendering("SnakeyFrame", "Basic-1", frame)
    }

    @Test
    fun testUpdates() {
        val lib = "LIB" to Color.RED
        val pc = "PC" to Color.BLUE
        val ndp = "NDP" to Color.ORANGE
        val bq = "BQ" to Color.CYAN
        val ref = "REF" to Color.GREEN.darker().darker()
        val ind = "IND" to Color.DARK_GRAY

        val parties1988 = Publisher(listOf(pc, lib, ndp))
        val parties1993 = Publisher(listOf(lib, pc, ref, bq, ndp, ind))
        val transfers = Publisher(
            mapOf(
                (pc to lib) to 0,
                (pc to null) to 169,
                (lib to null) to 83,
                (ndp to null) to 43,
            ),
        )
        val frame = SnakeyFrame.create(
            parties1988,
            parties1993,
            transfers,
            { first },
            { second },
            { first },
            { second },
            "SEAT CHANGES".asOneTimePublisher(),
        )
        frame.setSize(1024, 512)
        compareRendering("SnakeyFrame", "Updates-0", frame)

        transfers.submit(
            mapOf(
                (lib to lib) to 5,
                (pc to lib) to 2,
                (pc to null) to 167,
                (lib to null) to 78,
                (ndp to null) to 43,
            ),
        )
        compareRendering("SnakeyFrame", "Updates-1", frame)

        transfers.submit(
            mapOf(
                (lib to lib) to 20,
                (pc to lib) to 11,
                (pc to pc) to 1,
                (pc to null) to 157,
                (lib to null) to 63,
                (ndp to null) to 43,
            ),
        )
        compareRendering("SnakeyFrame", "Updates-2", frame)

        transfers.submit(
            mapOf(
                (lib to lib) to 73,
                (pc to lib) to 65,
                (pc to pc) to 2,
                (pc to bq) to 52,
                (pc to ind) to 1,
                (lib to bq) to 2,
                (ndp to lib) to 10,
                (pc to ref) to 1,
                (pc to null) to 48,
                (lib to null) to 8,
                (ndp to null) to 33,
            ),
        )
        compareRendering("SnakeyFrame", "Updates-3", frame)

        transfers.submit(
            mapOf(
                (lib to lib) to 78,
                (pc to lib) to 71,
                (pc to pc) to 2,
                (pc to bq) to 52,
                (pc to ind) to 1,
                (lib to bq) to 2,
                (ndp to lib) to 11,
                (pc to ref) to 2,
                (ndp to ndp) to 1,
                (pc to null) to 41,
                (lib to null) to 3,
                (ndp to null) to 31,
            ),
        )
        compareRendering("SnakeyFrame", "Updates-4", frame)

        transfers.submit(
            mapOf(
                (lib to lib) to 80,
                (pc to lib) to 76,
                (pc to pc) to 2,
                (pc to bq) to 52,
                (pc to ind) to 1,
                (lib to bq) to 2,
                (ndp to lib) to 15,
                (pc to ref) to 26,
                (ndp to ndp) to 6,
                (ndp to ref) to 2,
                (pc to null) to 12,
                (lib to null) to 1,
                (ndp to null) to 20,
            ),
        )
        compareRendering("SnakeyFrame", "Updates-5", frame)

        transfers.submit(
            mapOf(
                (lib to lib) to 81,
                (pc to lib) to 79,
                (pc to pc) to 2,
                (pc to bq) to 52,
                (pc to ind) to 1,
                (lib to bq) to 2,
                (ndp to lib) to 17,
                (pc to ref) to 35,
                (ndp to ndp) to 9,
                (ndp to ref) to 17,
                (pc to null) to 0,
                (lib to null) to 0,
                (ndp to null) to 0,
            ),
        )
        compareRendering("SnakeyFrame", "Updates-6", frame)
    }
}
