package com.thaiopensource.relaxng.edit;

public class EmptyPattern extends Pattern {
  public EmptyPattern() {
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitEmpty(this);
  }
}
