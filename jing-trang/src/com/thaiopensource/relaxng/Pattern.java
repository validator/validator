package com.thaiopensource.relaxng;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;

import com.thaiopensource.datatype.Datatype;

public abstract class Pattern {
  private boolean nullable;
  private int hc;
  private int contentType;

  static final int TEXT_HASH_CODE = 1;
  static final int ERROR_HASH_CODE = 3;
  static final int EMPTY_SEQUENCE_HASH_CODE = 5;
  static final int EMPTY_CHOICE_HASH_CODE = 7;
  static final int CHOICE_HASH_CODE = 11;
  static final int SEQUENCE_HASH_CODE = 13;
  static final int INTERLEAVE_HASH_CODE = 17;
  static final int ONE_OR_MORE_HASH_CODE = 19;
  static final int ELEMENT_HASH_CODE = 23;
  static final int VALUE_HASH_CODE = 27;
  static final int ATTRIBUTE_HASH_CODE = 29;
  static final int DATA_HASH_CODE = 31;
  static final int LIST_HASH_CODE = 37;

  static int combineHashCode(int hc1, int hc2, int hc3) {
    return hc1 * hc2 * hc3;
  }

  static int combineHashCode(int hc1, int hc2) {
    return hc1 * hc2;
  }

  static final int EMPTY_CONTENT_TYPE = 0;
  static final int MIXED_CONTENT_TYPE = 1;
  static final int DATA_CONTENT_TYPE = 2;

  Pattern(boolean nullable, int contentType, int hc) {
    this.nullable = nullable;
    this.contentType = contentType;
    this.hc = hc;
  }

  Pattern() {
    this.nullable = false;
    this.hc = hashCode();
    this.contentType = EMPTY_CONTENT_TYPE;
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

  static final int START_CONTEXT = 0;
  static final int ELEMENT_CONTEXT = 1;
  static final int ELEMENT_REPEAT_CONTEXT = 2;
  static final int ELEMENT_REPEAT_GROUP_CONTEXT = 3;
  static final int ELEMENT_REPEAT_INTERLEAVE_CONTEXT = 4;
  static final int ATTRIBUTE_CONTEXT = 5;
  static final int LIST_CONTEXT = 6;
  static final int DATA_EXCEPT_CONTEXT = 7;

  void checkRestrictions(int context) throws RestrictionViolationException {
  }

  // Know that ip is same class, distinct object, not null
  abstract boolean samePattern(Pattern other);

  final int patternHashCode() {
    return hc;
  }

  final int getContentType() {
    return contentType;
  }

  boolean containsChoice(Pattern p) {
    return this == p;
  }

  abstract void accept(PatternVisitor visitor);

  Datatype getDatatype() {
    return null;
  }

  static boolean contentTypeGroupable(int ct1, int ct2) {
    return (ct1 == EMPTY_CONTENT_TYPE
	    || ct2 == EMPTY_CONTENT_TYPE
	    || (ct1 == MIXED_CONTENT_TYPE && ct2 == MIXED_CONTENT_TYPE));
  }

}
