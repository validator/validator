package com.thaiopensource.relaxng.edit;

public class NotAllowedPattern extends Pattern {
  public NotAllowedPattern() {
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitNotAllowed(this);
  }
}
