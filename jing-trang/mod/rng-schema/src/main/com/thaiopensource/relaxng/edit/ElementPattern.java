package com.thaiopensource.relaxng.edit;

public class ElementPattern extends NameClassedPattern {
  public ElementPattern(NameClass nameClass, Pattern child) {
    super(nameClass, child);
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitElement(this);
  }
}
