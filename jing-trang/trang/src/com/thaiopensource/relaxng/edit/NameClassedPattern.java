package com.thaiopensource.relaxng.edit;

public abstract class NameClassedPattern extends UnaryPattern {
  private NameClass nameClass;

  public NameClassedPattern(NameClass nameClass, Pattern child) {
    super(child);
    this.nameClass = nameClass;
  }

  public NameClass getNameClass() {
    return nameClass;
  }

  public void setNameClass(NameClass nameClass) {
    this.nameClass = nameClass;
  }
}
