package com.thaiopensource.relaxng.edit;

public class RefPattern extends AbstractRefPattern {
  public RefPattern(String name) {
    super(name);
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitRef(this);
  }
}
