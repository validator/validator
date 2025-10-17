package com.thaiopensource.relaxng.edit;

public class OneOrMorePattern extends UnaryPattern {
  public OneOrMorePattern(Pattern child) {
    super(child);
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitOneOrMore(this);
  }
}
