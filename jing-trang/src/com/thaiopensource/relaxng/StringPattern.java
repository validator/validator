package com.thaiopensource.relaxng;

abstract class StringPattern extends Pattern {
  StringPattern(boolean nullable, int hc) {
    super(nullable, DATA_CONTENT_TYPE, hc);
  }
  Pattern residual(PatternBuilder b, Atom a) {
    if (matches(a))
      return b.makeEmptySequence();
    else
      return b.makeEmptyChoice();
  }

  abstract boolean matches(Atom a);
}
