package com.thaiopensource.relaxng;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

class PatternRefPattern extends Pattern {
  private Pattern p;
  private Locator refLoc;
  private int includeIndex = -1;
  private String name;
  private int checkRecursionDepth = -1;

  PatternRefPattern(String name) {
    // The referenced pattern may not be nullable, but the nullability
    // of a PatternRefPattern is only used by PatternBuilder for simplfying
    // choices, and in that context ip is safe to assume that ip is
    // not nullable.
    super(false);
    this.name = name;
  }

  Pattern getPattern() {
    return p;
  }
  
  void setPattern(Pattern p, int includeIndex) {
    this.p = p;
    this.includeIndex = includeIndex;
  }

  int getIncludeIndex() {
    return includeIndex;
  }
  
  Locator getRefLocator() {
    return refLoc;
  }
  
  void setRefLocator(Locator loc) {
    this.refLoc = loc;
  }
  
  void checkRecursion(int depth) throws SAXException {
    if (checkRecursionDepth == -1) {
      checkRecursionDepth = depth;
      p.checkRecursion(depth);
      checkRecursionDepth = -2;
    }
    else if (depth == checkRecursionDepth)
      // XXX try to recover from this?
      throw new SAXParseException(Localizer.message("recursive_reference",
						    name),
				  refLoc);
  }

  Pattern expand(PatternBuilder b) {
    return p.expand(b);
  }

  Pattern residual(PatternBuilder b, Atom a) {
    return p.residual(b, a);
  }

  void initialContentPatterns(String namespaceURI, String localName, PatternSet ts) {
    p.initialContentPatterns(namespaceURI, localName, ts);
  }

  Pattern combinedInitialContentPattern(PatternBuilder b,
				  String namespaceURI,
				  String localName,
				  int recoveryLevel) {
    return p.combinedInitialContentPattern(b,
					namespaceURI,
					localName,
					recoveryLevel);
  }

  Pattern endAttributes(PatternBuilder b, boolean recovering) {
    return p.endAttributes(b, recovering);
  }

  int checkString(Locator[] loc) throws SAXException {
    int flags = p.memoizedCheckString(loc);
    if ((flags & DISTINGUISHES_STRINGS) != 0)
      loc[0] = refLoc;
    return flags;
  }

  boolean samePattern(Pattern other) {
    return false;
  }

  void accept(PatternVisitor visitor) {
    p.accept(visitor);
  }
}

