package com.thaiopensource.datatype.xsd;

class PatternRestrictDatatype extends RestrictDatatype {
  private Regex pattern;

  PatternRestrictDatatype(DatatypeBase base, Regex pattern) {
    super(base);
    this.pattern = pattern;
  }

  boolean lexicallyAllows(String str) {
    return pattern.matches(str) && super.lexicallyAllows(str);
  }
}
