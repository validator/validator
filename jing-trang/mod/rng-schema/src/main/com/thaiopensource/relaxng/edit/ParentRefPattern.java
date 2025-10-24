package com.thaiopensource.relaxng.edit;

public class ParentRefPattern extends AbstractRefPattern {
  public ParentRefPattern(String name) {
    super(name);
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitParentRef(this);
  }
}
