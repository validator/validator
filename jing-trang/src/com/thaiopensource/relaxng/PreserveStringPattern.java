package com.thaiopensource.relaxng;

import org.xml.sax.Locator;

class PreserveStringPattern extends SimplePattern {
  String str;

  PreserveStringPattern(String str, Locator locator) {
    super(combineHashCode(STRING_HASH_CODE, str.hashCode()), locator);
    this.str = str;
  }

  boolean matches(Atom a) {
    return a.matchesPreserveString(str);
  }

  boolean samePattern(Pattern other) {
    if (getClass() != other.getClass())
      return false;
    return str.equals(((PreserveStringPattern)other).str);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitString(false, str);
  }
}
