package com.thaiopensource.relaxng.edit;

public class AttributePattern extends NameClassedPattern {
  public AttributePattern(NameClass nameClass, Pattern child) {
    super(nameClass, child);
  }

  public Object accept(PatternVisitor visitor) {
    return visitor.visitAttribute(this);
  }
}
