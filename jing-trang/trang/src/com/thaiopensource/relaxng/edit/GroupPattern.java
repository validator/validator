package com.thaiopensource.relaxng.edit;

public class GroupPattern extends CompositePattern {
  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitGroup(this);
  }
}
