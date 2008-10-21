package com.thaiopensource.relaxng.edit;

public class ListPattern extends UnaryPattern {
  public ListPattern(Pattern child) {
    super(child);
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitList(this);
  }
}
