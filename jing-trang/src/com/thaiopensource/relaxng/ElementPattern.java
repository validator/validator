package com.thaiopensource.relaxng;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;

class ElementPattern extends Pattern {
  private Pattern p;
  private NameClass nameClass;
  private boolean expanded = false;
  private boolean checkedString = false;

  ElementPattern(NameClass nameClass, Pattern p) {
    super(false,
	  combineHashCode(ELEMENT_HASH_CODE,
			  nameClass.hashCode(),
			  p.hashCode()));
    this.nameClass = nameClass;
    this.p = p;
  }

  Pattern residual(PatternBuilder b, Atom a) {
    if (a.matchesElement(nameClass, p))
      return b.makeEmptySequence();
    else
      return b.makeEmptyChoice();
  }

  void initialContentPatterns(String namespaceURI,
			   String localName,
			   PatternSet ts) {
    if (nameClass.contains(namespaceURI, localName))
      ts.add(p);
  }

  Pattern combinedInitialContentPattern(PatternBuilder b,
				  String namespaceURI,
				  String localName,
                                  int recoveryLevel) {
    if (nameClass.contains(namespaceURI, localName))
      return p;
    if (recoveryLevel > 1)
      return p.combinedInitialContentPattern(b,
					  namespaceURI,
					  localName,
					  recoveryLevel - 1);
    return b.makeEmptyChoice();
  }

  PatternPair unambigContentPattern(PatternBuilder b,
			      String namespaceURI,
			      String localName) {
    if (nameClass.contains(namespaceURI, localName))
      return new PatternPair(p, b.makeEmptySequence());
    return b.makeEmptyPatternPair();
  }

  int checkString(Locator[] loc) throws SAXException {
    if (!checkedString) {
      checkedString = true;
      p.memoizedCheckString(loc);
    }
    loc[0] = null;
    return ALLOWS_CHILDREN;
  }

  Pattern expand(PatternBuilder b) {
    if (!expanded) {
      expanded = true;
      p = p.expand(b);
    }
    return this;
  }

  boolean samePattern(Pattern other) {
    if (!(other instanceof ElementPattern))
      return false;
    ElementPattern ep = (ElementPattern)other;
    return nameClass.equals(ep.nameClass) && p == ep.p;
  }

  void checkRecursion(int depth) throws SAXException {
    p.checkRecursion(depth + 1);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitElement(nameClass, p);
  }
}
