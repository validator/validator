package com.thaiopensource.relaxng;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.datatype.DatatypeContext;

class ListPattern extends Pattern {
  Pattern p;
  Locator locator;

  ListPattern(Pattern p, Locator locator) {
    super(p.isNullable(),
	  combineHashCode(LIST_HASH_CODE, p.hashCode()));
    this.p = p;
    this.locator = locator;
  }

  Pattern expand(PatternBuilder b) {
    Pattern ep = p.expand(b);
    if (ep != p)
      return b.makeList(ep, locator);
    else
      return this;
  }

  void checkRecursion(int depth) throws SAXException {
    p.checkRecursion(depth);
  }

  boolean samePattern(Pattern other) {
    return (other instanceof ListPattern
	    && p == ((ListPattern)other).p);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitList(p);
  }

  Pattern residual(PatternBuilder b, Atom a) {
    String s = a.getStringValue();
    if (s == null)
      return b.makeEmptyChoice();
    DatatypeContext dc = a.getDatatypeContext();
    int len = s.length();
    int tokenStart = -1;
    Pattern r = p;
    for (int i = 0; i < len; i++) {
      switch (s.charAt(i)) {
      case '\r':
      case '\n':
      case ' ':
      case '\t':
	if (tokenStart >= 0) {
	  r = r.residual(b, new StringAtom(s.substring(tokenStart, i),
					   dc));
	  tokenStart = -1;
	}
	break;
      default:
	if (tokenStart < 0)
	  tokenStart = i;
	break;
      }
    }
    if (tokenStart >= 0)
      r = r.residual(b, new StringAtom(s.substring(tokenStart, len),
				       dc));
    if (r.isNullable())
      return b.makeEmptySequence();
    else
      return b.makeEmptyChoice();
  }

  int checkString(Locator[] loc) {
    if (loc[0] == null)
      loc[0] = locator;
    return DISTINGUISHES_STRINGS;
  }

  boolean distinguishesStrings() {
    return true;
  }
}
