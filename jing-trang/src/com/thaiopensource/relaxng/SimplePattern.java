package com.thaiopensource.relaxng;

import org.xml.sax.Locator;

abstract class SimplePattern extends Pattern {
  private Locator locator;

  SimplePattern(int hc, Locator locator) {
    super(false, // XXX should depend on whether it matches empty string
	  hc);
    this.locator = locator;
  }

  Pattern residual(PatternBuilder b, Atom a) {
    if (matches(a))
      return b.makeEmptySequence();
    else
      return b.makeEmptyChoice();
  }

  int checkString(Locator[] loc) {
    if (loc[0] == null)
      loc[0] = locator;
    return DISTINGUISHES_STRINGS;
  }

  boolean distinguishesStrings() {
    return true;
  }

  abstract boolean matches(Atom a);
}
