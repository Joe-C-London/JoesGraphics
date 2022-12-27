package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color

class FiguresScreenTest {
    @Test
    fun testFigures() {
        val screen = FiguresScreen.of()
            .withSection(
                FiguresScreen.section("CABINET")
                    .withCandidate(
                        Candidate("Wade MacLauchlan", lib),
                        "Premier, Stanhope-Marshfield",
                        pc.asOneTimePublisher(),
                        "DEFEATED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Heath MacDonald", lib),
                        "Finance Minister, Cornwall-Meadowbank",
                        lib.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Robert Mitchell", lib),
                        "Health and Wellness Minister, Charlottetown-Winsloe",
                        lib.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Paula Biggar", lib),
                        "Transport and Energy Minister, Tyne Valley-Sherbrooke",
                        grn.asOneTimePublisher(),
                        "DEFEATED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Robert Henderson", lib),
                        "Fisheries and Communities Minister, O'Leary-Inverness",
                        lib.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Chris Palmer", lib),
                        "Tourism and Culture Minister, Summerside-Wilmot",
                        grn.asOneTimePublisher(),
                        "DEFEATED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Sonny Gallant", lib),
                        "Education Minister, Evangeline-Miscouche",
                        lib.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Tina Mundy", lib),
                        "Social Develop't Minister, Summerside-South Drive",
                        grn.asOneTimePublisher(),
                        "DEFEATED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Jordan Brown", lib),
                        "Attorney General, Charlottetown-Brighton",
                        grn.asOneTimePublisher(),
                        "DEFEATED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Richard Brown", lib),
                        "Environment Minister, Charlottetown-Victoria Park",
                        grn.asOneTimePublisher(),
                        "DEFEATED".asOneTimePublisher()
                    )
            )
            .withSection(
                FiguresScreen.section("SHADOW CABINET")
                    .withCandidate(
                        Candidate("Dennis King", pc),
                        "Leader of the Opposition, Brackley-Hunter River",
                        pc.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Darlene Compton", pc),
                        "Finance Critic, Belfast-Murray River",
                        pc.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("James Aylward", pc),
                        "Health and Wellness Critic, Stratford-Kinlock",
                        pc.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Steven Myers", pc),
                        "Transportation and Energy Critic, Georgetown-Pownal",
                        pc.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Jamie Fox", pc),
                        "Fisheries and Communities Critic, Borden-Kinkora",
                        pc.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Matthew MacKay", pc),
                        "Tourism and Culture Critic, Kensington-Malpeque",
                        pc.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Brad Trivers", pc),
                        "Education Critic, Rustico-Emerald",
                        pc.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Ernie Hudson", pc),
                        "Social Development Critic, Alberton-Bloomfield",
                        pc.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Bloyce Thompson", pc),
                        "Justice Critic, Stanhope-Marshfield",
                        pc.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Natalie Jameson", pc),
                        "Environment Critic, Charlottetown-Hillsborough Park",
                        pc.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
            )
            .withSection(
                FiguresScreen.section("OTHER FIGURES")
                    .withCandidate(
                        Candidate("Peter Bevan-Baker", grn),
                        "Green Party Leader, New Haven-Rocky Point",
                        grn.asOneTimePublisher(),
                        "ELECTED".asOneTimePublisher()
                    )
                    .withCandidate(
                        Candidate("Joe Byrne", ndp),
                        "NDP Leader, Charlottetown-Victoria Park",
                        grn.asOneTimePublisher(),
                        "DEFEATED".asOneTimePublisher()
                    )
            )
            .build("PROMINENT FIGURES".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("FiguresScreen", "Figures-1", screen)
    }

    companion object {
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
        private val ind = Party("Independent", "IND", Color.GRAY)
    }
}
