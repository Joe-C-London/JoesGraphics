package com.joecollins.graphics.screens.generic;

import com.joecollins.graphics.utils.ColorUtils;
import com.joecollins.models.general.Party;
import java.awt.Color;

public class PartyResult {

  private final Party party;
  private final boolean elected;

  public PartyResult(Party party, boolean elected) {
    this.party = party;
    this.elected = elected;
  }

  public static PartyResult elected(Party party) {
    return new PartyResult(party, true);
  }

  public static PartyResult leading(Party party) {
    return new PartyResult(party, false);
  }

  public Color getColor() {
    if (party == null) {
      return Color.BLACK;
    }
    if (elected) {
      return party.getColor();
    }
    return ColorUtils.lighten(party.getColor());
  }
}
