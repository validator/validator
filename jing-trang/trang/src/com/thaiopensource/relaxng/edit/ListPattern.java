package com.thaiopensource.relaxng.edit;

public class ListPattern extends UnaryPattern {
  public ListPattern(Pattern child) {
    super(child);
  }

  public Object accept(PatternVisitor visitor) {
    return visitor.visitList(this);
  }
}
