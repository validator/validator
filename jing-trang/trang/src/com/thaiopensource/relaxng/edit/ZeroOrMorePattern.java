package com.thaiopensource.relaxng.edit;

public class ZeroOrMorePattern extends UnaryPattern {
  public ZeroOrMorePattern(Pattern child) {
    super(child);
  }

  public Object accept(PatternVisitor visitor) {
    return visitor.visitZeroOrMore(this);
  }
}
