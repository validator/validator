package com.thaiopensource.relaxng;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;

abstract class BinaryPattern extends Pattern {
  Pattern p1;
  Pattern p2;

  BinaryPattern(boolean nullable, int hc, Pattern p1, Pattern p2) {
    super(nullable, Math.max(p1.getContentType(), p2.getContentType()), hc);
    this.p1 = p1;
    this.p2 = p2;
  }

  void checkRecursion(int depth) throws SAXException {
    p1.checkRecursion(depth);
    p2.checkRecursion(depth);
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    p1.checkRestrictions(context, dad, alpha);
    p2.checkRestrictions(context, dad, alpha);
  }

  boolean samePattern(Pattern other) {
    if (getClass() != other.getClass())
      return false;
    BinaryPattern b = (BinaryPattern)other;
    return p1 == b.p1 && p2 == b.p2;
  }

  Pattern getOperand1() {
    return p1;
  }

  Pattern getOperand2() {
    return p2;
  }
}
