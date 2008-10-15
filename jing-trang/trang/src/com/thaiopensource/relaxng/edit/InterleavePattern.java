package com.thaiopensource.relaxng.edit;

public class InterleavePattern extends CompositePattern {
  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitInterleave(this);
  }
}
