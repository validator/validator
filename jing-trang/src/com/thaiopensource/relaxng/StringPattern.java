package com.thaiopensource.relaxng;

import org.xml.sax.Locator;

abstract class StringPattern extends Pattern {
  StringPattern(boolean nullable, int hc) {
    super(nullable, DATA_CONTENT_TYPE, hc);
  }
}
