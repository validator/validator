package com.thaiopensource.relaxng.edit;

public class GroupPattern extends CompositePattern {
  public Object accept(PatternVisitor visitor) {
    return visitor.visitGroup(this);
  }
}
