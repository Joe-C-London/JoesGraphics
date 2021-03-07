package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.screens.generic.FiguresScreen.Companion.of
import com.joecollins.graphics.screens.generic.FiguresScreen.Companion.section
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import java.awt.Color
import java.io.IOException
import kotlin.Throws
import org.junit.Test

class FiguresScreenTest {
    @Test
    @Throws(IOException::class)
    fun testFigures() {
        val screen = of()
                .withSection(
                        section("CABINET")
                                .withCandidate(
                                        Candidate("Wade MacLauchlan", lib),
                                        "Premier, Stanhope-Marshfield",
                                        fixedBinding(pc),
                                        fixedBinding("DEFEATED"))
                                .withCandidate(
                                        Candidate("Heath MacDonald", lib),
                                        "Finance Minister, Cornwall-Meadowbank",
                                        fixedBinding(lib),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("Robert Mitchell", lib),
                                        "Health and Wellness Minister, Charlottetown-Winsloe",
                                        fixedBinding(lib),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("Paula Biggar", lib),
                                        "Transport and Energy Minister, Tyne Valley-Sherbrooke",
                                        fixedBinding(grn),
                                        fixedBinding("DEFEATED"))
                                .withCandidate(
                                        Candidate("Robert Henderson", lib),
                                        "Fisheries and Communities Minister, O'Leary-Inverness",
                                        fixedBinding(lib),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("Chris Palmer", lib),
                                        "Tourism and Culture Minister, Summerside-Wilmot",
                                        fixedBinding(grn),
                                        fixedBinding("DEFEATED"))
                                .withCandidate(
                                        Candidate("Sonny Gallant", lib),
                                        "Education Minister, Evangeline-Miscouche",
                                        fixedBinding(lib),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("Tina Mundy", lib),
                                        "Social Develop't Minister, Summerside-South Drive",
                                        fixedBinding(grn),
                                        fixedBinding("DEFEATED"))
                                .withCandidate(
                                        Candidate("Jordan Brown", lib),
                                        "Attorney General, Charlottetown-Brighton",
                                        fixedBinding(grn),
                                        fixedBinding("DEFEATED"))
                                .withCandidate(
                                        Candidate("Richard Brown", lib),
                                        "Environment Minister, Charlottetown-Victoria Park",
                                        fixedBinding(grn),
                                        fixedBinding("DEFEATED")))
                .withSection(
                        section("SHADOW CABINET")
                                .withCandidate(
                                        Candidate("Dennis King", pc),
                                        "Leader of the Opposition, Brackley-Hunter River",
                                        fixedBinding(pc),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("Darlene Compton", pc),
                                        "Finance Critic, Belfast-Murray River",
                                        fixedBinding(pc),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("James Aylward", pc),
                                        "Health and Wellness Critic, Stratford-Kinlock",
                                        fixedBinding(pc),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("Steven Myers", pc),
                                        "Transportation and Energy Critic, Georgetown-Pownal",
                                        fixedBinding(pc),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("Jamie Fox", pc),
                                        "Fisheries and Communities Critic, Borden-Kinkora",
                                        fixedBinding(pc),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("Matthew MacKay", pc),
                                        "Tourism and Culture Critic, Kensington-Malpeque",
                                        fixedBinding(pc),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("Brad Trivers", pc),
                                        "Education Critic, Rustico-Emerald",
                                        fixedBinding(pc),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("Ernie Hudson", pc),
                                        "Social Development Critic, Alberton-Bloomfield",
                                        fixedBinding(pc),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("Bloyce Thompson", pc),
                                        "Justice Critic, Stanhope-Marshfield",
                                        fixedBinding(pc),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("Natalie Jameson", pc),
                                        "Environment Critic, Charlottetown-Hillsborough Park",
                                        fixedBinding(pc),
                                        fixedBinding("ELECTED")))
                .withSection(
                        section("OTHER FIGURES")
                                .withCandidate(
                                        Candidate("Peter Bevan-Baker", grn),
                                        "Green Party Leader, New Haven-Rocky Point",
                                        fixedBinding(grn),
                                        fixedBinding("ELECTED"))
                                .withCandidate(
                                        Candidate("Joe Byrne", ndp),
                                        "NDP Leader, Charlottetown-Victoria Park",
                                        fixedBinding(grn),
                                        fixedBinding("DEFEATED")))
                .build(fixedBinding("PROMINENT FIGURES"))
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
