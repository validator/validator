package com.thaiopensource.relaxng;

import org.xml.sax.Locator;

// Matches zero or more characters.

class AnyStringPattern extends Pattern {
  AnyStringPattern() {
    super(true, ANY_STRING_HASH_CODE);
  }

  Pattern residual(PatternBuilder b, Atom a) {
    if (a.matchesString())
      return this;
    else
      return b.makeEmptyChoice();
  }

  int checkString(Locator[] loc) {
    return ALLOWS_CHILDREN;
  }

  boolean samePattern(Pattern other) {
    return other instanceof AnyStringPattern;
  }

  void accept(PatternVisitor visitor) {
    visitor.visitAnyString();
  }
}
