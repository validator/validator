package com.thaiopensource.relaxng;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;

class AttributePattern extends Pattern {
  private NameClass nameClass;
  private Pattern p;

  AttributePattern(NameClass nameClass, Pattern value) {
    super(false,
	  combineHashCode(ATTRIBUTE_HASH_CODE,
			  nameClass.hashCode(),
			  value.hashCode()));
    this.nameClass = nameClass;
    this.p = value;
  }

  Pattern residual(PatternBuilder b, Atom a) {
    if (a.matchesAttribute(b, nameClass, p))
      return b.makeEmptySequence();
    else
      return b.makeEmptyChoice();
  }

  Pattern endAttributes(PatternBuilder b, boolean recovering) {
    if (recovering)
      return b.makeEmptySequence();
    else
      return b.makeEmptyChoice();
  }

  Pattern expand(PatternBuilder b) {
    Pattern ep = p.expand(b);
    if (ep != p)
      return b.makeAttribute(nameClass, ep);
    else
      return this;
  }

  int checkString(Locator[] loc) throws SAXException {
    p.checkString(loc);
    loc[0] = null;
    return 0;
  }

  boolean samePattern(Pattern other) {
    if (!(other instanceof AttributePattern))
      return false;
    AttributePattern ap = (AttributePattern)other;
    return nameClass.equals(ap.nameClass)&& p == ap.p;
  }

  void checkRecursion(int depth) throws SAXException {
    p.checkRecursion(depth);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitAttribute(nameClass, p);
  }
}
