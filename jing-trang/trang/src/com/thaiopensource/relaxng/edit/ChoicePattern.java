package com.thaiopensource.relaxng.edit;

public class ChoicePattern extends CompositePattern {
  public Object accept(PatternVisitor visitor) {
    return visitor.visitChoice(this);
  }
}
