package com.thaiopensource.relaxng;

class EmptyChoicePattern extends Pattern {
  EmptyChoicePattern() {
    super(false, EMPTY_CHOICE_HASH_CODE);
  }
  Pattern residual(PatternBuilder b, Atom a) {
    return this;
  }
  boolean isEmptyChoice() {
    return true;
  }
  boolean samePattern(Pattern other) {
    return other instanceof EmptyChoicePattern;
  }
  void accept(PatternVisitor visitor) {
    visitor.visitEmptyChoice();
  }
}
