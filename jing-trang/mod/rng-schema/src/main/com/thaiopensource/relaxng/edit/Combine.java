package com.thaiopensource.relaxng.edit;

public class Combine {
  public static final Combine CHOICE = new Combine("choice");
  public static final Combine INTERLEAVE = new Combine("interleave");

  private final String value;

  private Combine(String value) {
    this.value = value;
  }

  public String toString() {
    return value;
  }
}
