package com.thaiopensource.relaxng.edit;

public class RefPattern extends AbstractRefPattern {
  public RefPattern(String name) {
    super(name);
  }

  public Object accept(PatternVisitor visitor) {
    return visitor.visitRef(this);
  }
}
