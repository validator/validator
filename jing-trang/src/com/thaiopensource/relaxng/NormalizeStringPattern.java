package com.thaiopensource.relaxng;

import org.xml.sax.Locator;

class NormalizeStringPattern extends PreserveStringPattern {
  NormalizeStringPattern(String str, Locator locator) {
    super(StringNormalizer.normalize(str), locator);
  }

  boolean matches(Atom a) {
    return a.matchesNormalizeString(str);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitString(true, str);
  }
}
