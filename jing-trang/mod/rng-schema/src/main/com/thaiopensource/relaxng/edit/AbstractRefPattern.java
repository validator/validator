package com.thaiopensource.relaxng.edit;

public abstract class AbstractRefPattern extends Pattern {
  private String name;

  public AbstractRefPattern(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
