package com.thaiopensource.relaxng;

class ErrorPattern extends Pattern {
  ErrorPattern() {
    super(false, ERROR_HASH_CODE);
  }
  Pattern residual(PatternBuilder b, Atom a) {
    return this;
  }
  boolean samePattern(Pattern other) {
    return other instanceof ErrorPattern;
  }
  void accept(PatternVisitor visitor) {
    visitor.visitError();
  }
}
