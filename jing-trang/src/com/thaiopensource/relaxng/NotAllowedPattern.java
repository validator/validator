package com.thaiopensource.relaxng;

class NotAllowedPattern extends Pattern {
  NotAllowedPattern() {
    super(false, EMPTY_CONTENT_TYPE, EMPTY_CHOICE_HASH_CODE);
  }
  Pattern residual(PatternBuilder b, Atom a) {
    return this;
  }
  boolean samePattern(Pattern other) {
    return other instanceof NotAllowedPattern;
  }
  void accept(PatternVisitor visitor) {
    visitor.visitEmptyChoice();
  }
  Pattern expand(PatternBuilder b) {
    return b.makeEmptyChoice();
  }
}
