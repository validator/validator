package com.thaiopensource.relaxng.edit;

public class AttributePattern extends NameClassedPattern {
  public AttributePattern(NameClass nameClass, Pattern child) {
    super(nameClass, child);
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitAttribute(this);
  }
}
