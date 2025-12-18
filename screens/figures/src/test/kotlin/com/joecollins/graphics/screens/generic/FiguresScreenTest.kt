package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color

class FiguresScreenTest {
    @Test
    fun testFigures() {
        val screen = FiguresScreen.create("PROMINENT FIGURES".asOneTimePublisher()) {
            addSection("CABINET") {
                addCandidate(
                    Candidate("Wade MacLauchlan", lib),
                    "Premier, Stanhope-Marshfield",
                    pc.asOneTimePublisher(),
                    "DEFEATED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Heath MacDonald", lib),
                    "Finance Minister, Cornwall-Meadowbank",
                    lib.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Robert Mitchell", lib),
                    "Health and Wellness Minister, Charlottetown-Winsloe",
                    lib.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Paula Biggar", lib),
                    "Transport and Energy Minister, Tyne Valley-Sherbrooke",
                    grn.asOneTimePublisher(),
                    "DEFEATED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Robert Henderson", lib),
                    "Fisheries and Communities Minister, O'Leary-Inverness",
                    lib.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Chris Palmer", lib),
                    "Tourism and Culture Minister, Summerside-Wilmot",
                    grn.asOneTimePublisher(),
                    "DEFEATED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Sonny Gallant", lib),
                    "Education Minister, Evangeline-Miscouche",
                    lib.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Tina Mundy", lib),
                    "Social Develop't Minister, Summerside-South Drive",
                    grn.asOneTimePublisher(),
                    "DEFEATED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Jordan Brown", lib),
                    "Attorney General, Charlottetown-Brighton",
                    grn.asOneTimePublisher(),
                    "DEFEATED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Richard Brown", lib),
                    "Environment Minister, Charlottetown-Victoria Park",
                    null.asOneTimePublisher(),
                    "WAITING...".asOneTimePublisher(),
                )
            }
            addSection("SHADOW CABINET") {
                addCandidate(
                    Candidate("Dennis King", pc),
                    "Leader of the Opposition, Brackley-Hunter River",
                    pc.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Darlene Compton", pc),
                    "Finance Critic, Belfast-Murray River",
                    pc.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("James Aylward", pc),
                    "Health and Wellness Critic, Stratford-Kinlock",
                    pc.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Steven Myers", pc),
                    "Transportation and Energy Critic, Georgetown-Pownal",
                    pc.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Jamie Fox", pc),
                    "Fisheries and Communities Critic, Borden-Kinkora",
                    pc.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Matthew MacKay", pc),
                    "Tourism and Culture Critic, Kensington-Malpeque",
                    pc.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Brad Trivers", pc),
                    "Education Critic, Rustico-Emerald",
                    pc.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Ernie Hudson", pc),
                    "Social Development Critic, Alberton-Bloomfield",
                    pc.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Bloyce Thompson", pc),
                    "Justice Critic, Stanhope-Marshfield",
                    pc.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Natalie Jameson", pc),
                    "Environment Critic, Charlottetown-Hillsborough Park",
                    pc.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
            }
            addSection("OTHER FIGURES") {
                addCandidate(
                    Candidate("Peter Bevan-Baker", grn),
                    "Green Party Leader, New Haven-Rocky Point",
                    grn.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Joe Byrne", ndp),
                    "NDP Leader, Charlottetown-Victoria Park",
                    grn.asOneTimePublisher(),
                    "DEFEATED".asOneTimePublisher(),
                )
            }
        }
        screen.setSize(1024, 512)
        compareRendering("FiguresScreen", "Figures-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            PROMINENT FIGURES
            
            CABINET
            DEFEATED: WADE MACLAUCHLAN, PAULA BIGGAR, CHRIS PALMER, TINA MUNDY, JORDAN BROWN
            ELECTED: HEATH MACDONALD, ROBERT MITCHELL, ROBERT HENDERSON, SONNY GALLANT
            WAITING...: RICHARD BROWN
            
            SHADOW CABINET
            ELECTED: DENNIS KING, DARLENE COMPTON, JAMES AYLWARD, STEVEN MYERS, JAMIE FOX, MATTHEW MACKAY, BRAD TRIVERS, ERNIE HUDSON, BLOYCE THOMPSON, NATALIE JAMESON
            
            OTHER FIGURES
            DEFEATED: JOE BYRNE
            ELECTED: PETER BEVAN-BAKER
            """.trimIndent(),
        )
    }

    @Test
    fun testSingleColumnFigures() {
        val screen = FiguresScreen.create("PROMINENT FIGURES".asOneTimePublisher()) {
            addBlankSection()
            addSection("CABINET") {
                addCandidate(
                    Candidate("Wade MacLauchlan", lib),
                    "Premier, Stanhope-Marshfield",
                    pc.asOneTimePublisher(),
                    "DEFEATED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Heath MacDonald", lib),
                    "Finance Minister, Cornwall-Meadowbank",
                    lib.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Robert Mitchell", lib),
                    "Health and Wellness Minister, Charlottetown-Winsloe",
                    lib.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Paula Biggar", lib),
                    "Transport and Energy Minister, Tyne Valley-Sherbrooke",
                    grn.asOneTimePublisher(),
                    "DEFEATED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Robert Henderson", lib),
                    "Fisheries and Communities Minister, O'Leary-Inverness",
                    lib.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Chris Palmer", lib),
                    "Tourism and Culture Minister, Summerside-Wilmot",
                    grn.asOneTimePublisher(),
                    "DEFEATED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Sonny Gallant", lib),
                    "Education Minister, Evangeline-Miscouche",
                    lib.asOneTimePublisher(),
                    "ELECTED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Tina Mundy", lib),
                    "Social Develop't Minister, Summerside-South Drive",
                    grn.asOneTimePublisher(),
                    "DEFEATED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Jordan Brown", lib),
                    "Attorney General, Charlottetown-Brighton",
                    grn.asOneTimePublisher(),
                    "DEFEATED".asOneTimePublisher(),
                )
                addCandidate(
                    Candidate("Richard Brown", lib),
                    "Environment Minister, Charlottetown-Victoria Park",
                    null.asOneTimePublisher(),
                    "WAITING...".asOneTimePublisher(),
                )
            }
            addBlankSection()
        }
        screen.setSize(1024, 512)
        compareRendering("FiguresScreen", "FiguresWithBlanks-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
            PROMINENT FIGURES
            
            CABINET
            DEFEATED: WADE MACLAUCHLAN, PAULA BIGGAR, CHRIS PALMER, TINA MUNDY, JORDAN BROWN
            ELECTED: HEATH MACDONALD, ROBERT MITCHELL, ROBERT HENDERSON, SONNY GALLANT
            WAITING...: RICHARD BROWN
            """.trimIndent(),
        )
    }

    companion object {
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
    }
}
