package com.thaiopensource.relaxng.edit;

public class NotAllowedPattern extends Pattern {
  public NotAllowedPattern() {
  }

  public Object accept(PatternVisitor visitor) {
    return visitor.visitNotAllowed(this);
  }
}
