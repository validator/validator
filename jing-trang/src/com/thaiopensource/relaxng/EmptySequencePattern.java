package com.thaiopensource.relaxng;

class EmptySequencePattern extends Pattern {
  EmptySequencePattern() {
    super(true, EMPTY_SEQUENCE_HASH_CODE);
  }
  Pattern residual(PatternBuilder b, Atom a) {
    return b.makeEmptyChoice();
  }
  boolean samePattern(Pattern other) {
    return other instanceof EmptySequencePattern;
  }
  void accept(PatternVisitor visitor) {
    visitor.visitEmptySequence();
  }
}
