package com.thaiopensource.relaxng.edit;

public class ChoicePattern extends CompositePattern {
  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitChoice(this);
  }
}
