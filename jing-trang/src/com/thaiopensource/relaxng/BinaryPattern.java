package com.thaiopensource.relaxng;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;

abstract class BinaryPattern extends Pattern {
  Pattern p1;
  Pattern p2;
  BinaryPattern(boolean nullable, int hc, Pattern p1, Pattern p2) {
    super(nullable, hc);
    this.p1 = p1;
    this.p2 = p2;
  }
  void checkRecursion(int depth) throws SAXException {
    p1.checkRecursion(depth);
    p2.checkRecursion(depth);
  }

  int checkString(Locator[] loc) throws SAXException {
    return p1.memoizedCheckString(loc) | p2.memoizedCheckString(loc);
  }

  boolean distinguishesStrings() {
    return (p1.memoizedDistinguishesStrings()
	    || p2.memoizedDistinguishesStrings());
  }

  boolean samePattern(Pattern other) {
    if (getClass() != other.getClass())
      return false;
    BinaryPattern b = (BinaryPattern)other;
    return p1 == b.p1 && p2 == b.p2;
  }

}
