package com.thaiopensource.relaxng;

import org.xml.sax.Locator;

// Matches zero or more characters.

class TextPattern extends Pattern {
  TextPattern() {
    super(true, TEXT_HASH_CODE);
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
    return other instanceof TextPattern;
  }

  void accept(PatternVisitor visitor) {
    visitor.visitText();
  }
}
