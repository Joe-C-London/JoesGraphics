package com.joecollins.models.general;

import java.util.Objects;

public class Candidate {

  public static final Candidate OTHERS = new Candidate("", Party.OTHERS);

  private final String name;
  private final Party party;
  private final boolean incumbent;

  private final int hash;

  public Candidate(String name, Party party) {
    this(name, party, false);
  }

  public Candidate(String name, Party party, boolean incumbent) {
    this.name = name;
    this.party = party;
    this.incumbent = incumbent;

    this.hash = Objects.hash(this.name, this.party, this.incumbent);
  }

  public String getName() {
    return name;
  }

  public Party getParty() {
    return party;
  }

  public boolean isIncumbent() {
    return incumbent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Candidate candidate = (Candidate) o;
    return incumbent == candidate.incumbent
        && Objects.equals(name, candidate.name)
        && Objects.equals(party, candidate.party);
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public String toString() {
    return name + " (" + party + ")" + (incumbent ? "*" : "");
  }
}
