package com.thaiopensource.relaxng;

import org.xml.sax.Locator;

abstract class StringPattern extends Pattern {
  private Locator locator;

  StringPattern(boolean nullable, int hc, Locator locator) {
    super(nullable, hc);
    this.locator = locator;
  }

  int checkString(Locator[] loc) {
    if (loc[0] == null)
      loc[0] = locator;
    return DISTINGUISHES_STRINGS;
  }

  boolean distinguishesStrings() {
    return true;
  }

  Locator getLocator() {
    return locator;
  }
}
