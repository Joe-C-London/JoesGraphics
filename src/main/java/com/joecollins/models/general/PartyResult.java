package com.joecollins.models.general;

import com.joecollins.graphics.utils.ColorUtils;
import java.awt.Color;

public class PartyResult {

  public static PartyResult NO_RESULT = PartyResult.leading(null);

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

  public Party getParty() {
    return party;
  }

  public boolean isElected() {
    return elected;
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
