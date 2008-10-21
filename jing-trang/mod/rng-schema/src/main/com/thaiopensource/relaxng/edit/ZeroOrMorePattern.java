package com.thaiopensource.relaxng.edit;

public class ZeroOrMorePattern extends UnaryPattern {
  public ZeroOrMorePattern(Pattern child) {
    super(child);
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitZeroOrMore(this);
  }
}
