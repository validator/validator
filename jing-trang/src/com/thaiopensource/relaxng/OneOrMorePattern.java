package com.thaiopensource.relaxng;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class OneOrMorePattern extends Pattern {
  Pattern p;

  OneOrMorePattern(Pattern p) {
    super(p.isNullable(),
	  p.getContentType(),
	  combineHashCode(ONE_OR_MORE_HASH_CODE, p.hashCode()));
    this.p = p;
  }

  Pattern expand(PatternBuilder b) {
    Pattern ep = p.expand(b);
    if (ep != p)
      return b.makeOneOrMore(ep);
    else
      return this;
  }

  Pattern residual(PatternBuilder b, Atom a) {
    Pattern tr = b.memoizedResidual(p, a);
    if (tr.isEmptyChoice())
      return tr;
    return b.makeSequence(tr, b.makeZeroOrMore(p));
  }

  PatternPair unambigContentPattern(PatternBuilder b,
			      String namespaceURI,
			      String localName) {
    PatternPair cp = b.memoizedUnambigContentPattern(p, namespaceURI, localName);
    if (cp == null || cp.isEmpty())
      return cp;
    return new PatternPair(cp.getContentPattern(),
			b.makeSequence(cp.getResidualPattern(),
				       b.makeZeroOrMore(p)));
  }

  Pattern endAttributes(PatternBuilder b, boolean recovering) {
    Pattern cp = b.memoizedEndAttributes(p, recovering);
    if (cp == p)
      return this;
    return b.makeOneOrMore(p);
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

  void checkRecursion(int depth) throws SAXException {
    p.checkRecursion(depth);
  }

  void checkRestrictions(int context) throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_one_or_more");
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_one_or_more");
    }
    
    p.checkRestrictions(context == ELEMENT_CONTEXT
			? ELEMENT_REPEAT_CONTEXT
			: context);
    if (context != LIST_CONTEXT
	&& !contentTypeGroupable(p.getContentType(), p.getContentType()))
      throw new RestrictionViolationException("one_or_more_string");
  }

  boolean samePattern(Pattern other) {
    return (other instanceof OneOrMorePattern
	    && p == ((OneOrMorePattern)other).p);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitOneOrMore(p);
  }
}
