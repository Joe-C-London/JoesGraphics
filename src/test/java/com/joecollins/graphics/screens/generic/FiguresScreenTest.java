package com.joecollins.graphics.screens.generic;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;

import com.joecollins.bindings.Binding;
import com.joecollins.models.general.Candidate;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.io.IOException;
import org.junit.Test;

public class FiguresScreenTest {

  private static Party ndp = new Party("New Democratic Party", "NDP", Color.ORANGE);
  private static Party pc = new Party("Progressive Conservative", "PC", Color.BLUE);
  private static Party lib = new Party("Liberal", "LIB", Color.RED);
  private static Party grn = new Party("Green", "GRN", Color.GREEN.darker());
  private static Party ind = new Party("Independent", "IND", Color.GRAY);

  @Test
  public void testFigures() throws IOException {
    var screen =
        FiguresScreen.of()
            .withSection(
                FiguresScreen.section("CABINET")
                    .withCandidate(
                        new Candidate("Wade MacLauchlan", lib),
                        "Premier, Stanhope-Marshfield",
                        Binding.fixedBinding(pc),
                        Binding.fixedBinding("DEFEATED"))
                    .withCandidate(
                        new Candidate("Heath MacDonald", lib),
                        "Finance Minister, Cornwall-Meadowbank",
                        Binding.fixedBinding(lib),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("Robert Mitchell", lib),
                        "Health and Wellness Minister, Charlottetown-Winsloe",
                        Binding.fixedBinding(lib),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("Paula Biggar", lib),
                        "Transport and Energy Minister, Tyne Valley-Sherbrooke",
                        Binding.fixedBinding(grn),
                        Binding.fixedBinding("DEFEATED"))
                    .withCandidate(
                        new Candidate("Robert Henderson", lib),
                        "Fisheries and Communities Minister, O'Leary-Inverness",
                        Binding.fixedBinding(lib),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("Chris Palmer", lib),
                        "Tourism and Culture Minister, Summerside-Wilmot",
                        Binding.fixedBinding(grn),
                        Binding.fixedBinding("DEFEATED"))
                    .withCandidate(
                        new Candidate("Sonny Gallant", lib),
                        "Education Minister, Evangeline-Miscouche",
                        Binding.fixedBinding(lib),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("Tina Mundy", lib),
                        "Social Develop't Minister, Summerside-South Drive",
                        Binding.fixedBinding(grn),
                        Binding.fixedBinding("DEFEATED"))
                    .withCandidate(
                        new Candidate("Jordan Brown", lib),
                        "Attorney General, Charlottetown-Brighton",
                        Binding.fixedBinding(grn),
                        Binding.fixedBinding("DEFEATED"))
                    .withCandidate(
                        new Candidate("Richard Brown", lib),
                        "Environment Minister, Charlottetown-Victoria Park",
                        Binding.fixedBinding(grn),
                        Binding.fixedBinding("DEFEATED")))
            .withSection(
                FiguresScreen.section("SHADOW CABINET")
                    .withCandidate(
                        new Candidate("Dennis King", pc),
                        "Leader of the Opposition, Brackley-Hunter River",
                        Binding.fixedBinding(pc),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("Darlene Compton", pc),
                        "Finance Critic, Belfast-Murray River",
                        Binding.fixedBinding(pc),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("James Aylward", pc),
                        "Health and Wellness Critic, Stratford-Kinlock",
                        Binding.fixedBinding(pc),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("Steven Myers", pc),
                        "Transportation and Energy Critic, Georgetown-Pownal",
                        Binding.fixedBinding(pc),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("Jamie Fox", pc),
                        "Fisheries and Communities Critic, Borden-Kinkora",
                        Binding.fixedBinding(pc),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("Matthew MacKay", pc),
                        "Tourism and Culture Critic, Kensington-Malpeque",
                        Binding.fixedBinding(pc),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("Brad Trivers", pc),
                        "Education Critic, Rustico-Emerald",
                        Binding.fixedBinding(pc),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("Ernie Hudson", pc),
                        "Social Development Critic, Alberton-Bloomfield",
                        Binding.fixedBinding(pc),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("Bloyce Thompson", pc),
                        "Justice Critic, Stanhope-Marshfield",
                        Binding.fixedBinding(pc),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("Natalie Jameson", pc),
                        "Environment Critic, Charlottetown-Hillsborough Park",
                        Binding.fixedBinding(pc),
                        Binding.fixedBinding("ELECTED")))
            .withSection(
                FiguresScreen.section("OTHER FIGURES")
                    .withCandidate(
                        new Candidate("Peter Bevan-Baker", grn),
                        "Green Party Leader, New Haven-Rocky Point",
                        Binding.fixedBinding(grn),
                        Binding.fixedBinding("ELECTED"))
                    .withCandidate(
                        new Candidate("Joe Byrne", ndp),
                        "NDP Leader, Charlottetown-Victoria Park",
                        Binding.fixedBinding(grn),
                        Binding.fixedBinding("DEFEATED")))
            .build(Binding.fixedBinding("PROMINENT FIGURES"));
    screen.setSize(1024, 512);
    compareRendering("FiguresScreen", "Figures-1", screen);
  }
}
