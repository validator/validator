package com.thaiopensource.relaxng.edit;

public class OptionalPattern extends UnaryPattern {
  public OptionalPattern(Pattern child) {
    super(child);
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitOptional(this);
  }
}
