package com.thaiopensource.relaxng;

class EmptySequencePattern extends Pattern {
  EmptySequencePattern() {
    super(true, EMPTY_CONTENT_TYPE, EMPTY_SEQUENCE_HASH_CODE);
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
  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_empty");
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_empty");
    }
  }
}
