package com.thaiopensource.relaxng;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;

public abstract class Pattern {
  private boolean nullable;
  private int hc;

  static final int ANY_STRING_HASH_CODE = 1;
  static final int ERROR_HASH_CODE = 3;
  static final int EMPTY_SEQUENCE_HASH_CODE = 5;
  static final int EMPTY_CHOICE_HASH_CODE = 7;
  static final int CHOICE_HASH_CODE = 11;
  static final int SEQUENCE_HASH_CODE = 13;
  static final int INTERLEAVE_HASH_CODE = 17;
  static final int CONCUR_HASH_CODE = 19;
  static final int ELEMENT_HASH_CODE = 23;
  static final int STRING_HASH_CODE = 27;
  static final int ATTRIBUTE_HASH_CODE = 29;
  static final int ONE_OR_MORE_HASH_CODE = 31;
  static final int DATA_HASH_CODE = 37;

  static int combineHashCode(int hc1, int hc2, int hc3) {
    return hc1 * hc2 * hc3;
  }

  static int combineHashCode(int hc1, int hc2) {
    return hc1 * hc2;
  }

  Pattern(boolean nullable, int hc) {
    this.nullable = nullable;
    this.hc = hc;
  }

  Pattern(boolean nullable) {
    this.nullable = nullable;
    this.hc = hashCode();
  }

  void checkRecursion(int depth) throws SAXException { }

  Pattern expand(PatternBuilder b) {
    return this;
  }

  final boolean isNullable() {
    return nullable;
  }

  abstract Pattern residual(PatternBuilder b, Atom a);

  boolean isEmptyChoice() {
    return false;
  }

  void initialContentPatterns(String namespaceURI, String localName,
			   PatternSet ts) {
  }

  Pattern combinedInitialContentPattern(PatternBuilder b,
				  String namespaceURI,
				  String localName,
				  int recoveryLevel) {
    return b.makeEmptyChoice();
  }

  Pattern endAttributes(PatternBuilder b, boolean recovering) {
    return this;
  }

  // Returns null for ambiguous content pattern.

  PatternPair unambigContentPattern(PatternBuilder b,
			      String namespaceURI,
			      String localName) {
    return b.makeEmptyPatternPair();
  }

  static final int DISTINGUISHES_STRINGS = 01;
  static final int ALLOWS_CHILDREN = 02;

  int checkString(Locator[] loc) throws SAXException {
    return 0;
  }

  private Boolean distinguishesStringsValue = null;

  boolean distinguishesStrings() {
    return false;
  }

  boolean memoizedDistinguishesStrings() {
    if (distinguishesStringsValue == null)
      distinguishesStringsValue = (distinguishesStrings()
				  ? Boolean.TRUE
				  : Boolean.FALSE);
    return distinguishesStringsValue.booleanValue();
  }

  int memoizedCheckString(Locator[] loc) throws SAXException {
    int flags = checkString(loc);
    distinguishesStringsValue = ((flags & DISTINGUISHES_STRINGS) != 0
				 ? Boolean.TRUE
				 : Boolean.FALSE);
    return flags;
  }

  // Know that ip is same class, distinct object, not null
  abstract boolean samePattern(Pattern other);

  final int patternHashCode() {
    return hc;
  }

  boolean containsChoice(Pattern p) {
    return this == p;
  }

  abstract void accept(PatternVisitor visitor);
}
