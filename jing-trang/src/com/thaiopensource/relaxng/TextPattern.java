package com.thaiopensource.relaxng;

import org.xml.sax.Locator;

// Matches zero or more characters.

class TextPattern extends Pattern {
  TextPattern() {
    super(true, MIXED_CONTENT_TYPE, TEXT_HASH_CODE);
  }

  Pattern residual(PatternBuilder b, Atom a) {
    if (a.matchesString())
      return this;
    else
      return b.makeEmptyChoice();
  }

  boolean samePattern(Pattern other) {
    return other instanceof TextPattern;
  }

  void accept(PatternVisitor visitor) {
    visitor.visitText();
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad)
    throws RestrictionViolationException {
    switch (context) {
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_text");
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_text");
    case LIST_CONTEXT:
      throw new RestrictionViolationException("list_contains_text");
    }
  }

}
