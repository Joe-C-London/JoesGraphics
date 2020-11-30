package com.joecollins.models.general;

import java.awt.Color;
import java.util.Objects;

public class Party {

  public static Party OTHERS = new Party("Others", "OTH", Color.GRAY);

  private final String name;
  private final String abbreviation;
  private final Color color;

  private final int hash;

  public Party(String name, String abbreviation, Color color) {
    this.name = name;
    this.abbreviation = abbreviation;
    this.color = color;

    this.hash = Objects.hash(this.name, this.abbreviation, this.color);
  }

  public String getName() {
    return name;
  }

  public String getAbbreviation() {
    return abbreviation;
  }

  public Color getColor() {
    return color;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Party party = (Party) o;
    return Objects.equals(name, party.name)
        && Objects.equals(abbreviation, party.abbreviation)
        && Objects.equals(color, party.color);
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public String toString() {
    return abbreviation;
  }
}
