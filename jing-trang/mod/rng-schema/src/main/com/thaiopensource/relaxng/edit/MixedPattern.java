package com.thaiopensource.relaxng.edit;

public class MixedPattern extends UnaryPattern {
  public MixedPattern(Pattern child) {
    super(child);
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitMixed(this);
  }
}
