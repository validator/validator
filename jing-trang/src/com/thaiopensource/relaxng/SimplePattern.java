package com.thaiopensource.relaxng;

import org.xml.sax.Locator;

abstract class SimplePattern extends StringPattern {
  SimplePattern(int hc) {
    super(false, hc);
  }

  Pattern residual(PatternBuilder b, Atom a) {
    if (matches(a))
      return b.makeEmptySequence();
    else
      return b.makeEmptyChoice();
  }

  abstract boolean matches(Atom a);
}
