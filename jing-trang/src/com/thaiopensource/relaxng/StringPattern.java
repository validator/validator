package com.thaiopensource.relaxng;

abstract class StringPattern extends Pattern {
  StringPattern(int hc) {
    super(false, DATA_CONTENT_TYPE, hc);
  }
  Pattern residual(PatternBuilder b, Atom a) {
    if (matches(b, a))
      return b.makeEmptySequence();
    else
      return b.makeEmptyChoice();
  }

  abstract boolean matches(PatternBuilder b, Atom a);
}
