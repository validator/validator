package com.thaiopensource.relaxng;

import org.xml.sax.Locator;

abstract class SimplePattern extends StringPattern {
  private Locator locator;

  SimplePattern(int hc, Locator locator) {
    super(false, hc, locator);
    this.locator = locator;
  }

  Pattern residual(PatternBuilder b, Atom a) {
    if (matches(a))
      return b.makeEmptySequence();
    else
      return b.makeEmptyChoice();
  }

  abstract boolean matches(Atom a);
}
