package com.thaiopensource.relaxng.edit;

public class ElementPattern extends NameClassedPattern {
  public ElementPattern(NameClass nameClass, Pattern child) {
    super(nameClass, child);
  }

  public Object accept(PatternVisitor visitor) {
    return visitor.visitElement(this);
  }
}
