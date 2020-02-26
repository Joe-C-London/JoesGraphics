package com.joecollins.models.general;

import java.awt.Color;
import java.util.Objects;

public class Party {
  private final String name;
  private final String abbreviation;
  private final Color color;

  public Party(String name, String abbreviation, Color color) {
    this.name = name;
    this.abbreviation = abbreviation;
    this.color = color;
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
    return Objects.hash(name, abbreviation, color);
  }

  @Override
  public String toString() {
    return abbreviation;
  }
}
