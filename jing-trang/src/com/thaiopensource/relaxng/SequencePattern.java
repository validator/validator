package com.thaiopensource.relaxng;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class SequencePattern extends BinaryPattern {
  SequencePattern(Pattern p1, Pattern p2) {
    super(p1.isNullable() && p2.isNullable(),
	  combineHashCode(SEQUENCE_HASH_CODE, p1.hashCode(), p2.hashCode()),
	  p1,
	  p2);
  }

  Pattern expand(PatternBuilder b) {
    Pattern ep1 = p1.expand(b);
    Pattern ep2 = p2.expand(b);
    if (ep1 != p1 || ep2 != p2)
      return b.makeSequence(ep1, ep2);
    else
      return this;
  }

  Pattern residual(PatternBuilder b, Atom a) {
    if (a.isAttribute())
      return b.makeChoice(b.makeSequence(b.memoizedResidual(p1, a), p2),
			  b.makeSequence(p1, b.memoizedResidual(p2, a)));
    else {
      Pattern p = b.makeSequence(b.memoizedResidual(p1, a), p2);
      if (!p1.isNullable())
	return p;
      return b.makeChoice(b.memoizedResidual(p2, a), p);
    }
  }

  PatternPair unambigContentPattern(PatternBuilder b,
			      String namespaceURI,
			      String localName) {
    PatternPair cp1 = b.memoizedUnambigContentPattern(p1, namespaceURI, localName);
    if (cp1 == null)
      return null;
    if (!p1.isNullable()) {
      if (cp1.isEmpty())
	return cp1;
      return new PatternPair(cp1.getContentPattern(),
			     b.makeSequence(cp1.getResidualPattern(), p2));
    }
    else {
      PatternPair cp2 = b.memoizedUnambigContentPattern(p2, namespaceURI, localName);
      if (cp2 == null)
	return null;
      if (cp1.isEmpty())
	return cp2;
      if (cp2.isEmpty())
	return new PatternPair(cp1.getContentPattern(),
			       b.makeSequence(cp1.getResidualPattern(), p2));
      if (cp1.getContentPattern() == cp2.getContentPattern())
	return new PatternPair(cp1.getContentPattern(),
			       b.makeChoice(cp2.getResidualPattern(),
					    b.makeSequence(cp1.getResidualPattern(),
							   p2)));
      return null;
    }
  }

  void initialContentPatterns(String namespaceURI, String localName, PatternSet ts) {
    p1.initialContentPatterns(namespaceURI, localName, ts);
    if (p1.isNullable())
      p2.initialContentPatterns(namespaceURI, localName, ts);
  }

  Pattern combinedInitialContentPattern(PatternBuilder b,
					String namespaceURI,
					String localName,
					int recoveryLevel) {
    Pattern tem = p1.combinedInitialContentPattern(b,
						   namespaceURI,
						   localName,
						   recoveryLevel);
    if (!p1.isNullable() && recoveryLevel == 0)
      return tem;
    return b.makeChoice(tem, p2.combinedInitialContentPattern(b,
							      namespaceURI,
							      localName,
							      recoveryLevel));
  }

  Pattern endAttributes(PatternBuilder b, boolean recovering) {
    Pattern cp1 = b.memoizedEndAttributes(p1, recovering);
    Pattern cp2 = b.memoizedEndAttributes(p2, recovering);
    if (cp1 == p1 && cp2 == p2)
      return this;
    return b.makeSequence(cp1, cp2);
  }

  void checkRestrictions(int context) throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_group");
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_group");
    }
    super.checkRestrictions(context == ELEMENT_REPEAT_CONTEXT
			    ? ELEMENT_REPEAT_GROUP_CONTEXT
			    : context);
    if (context != LIST_CONTEXT
	&& !contentTypeGroupable(p1.getContentType(), p2.getContentType()))
      throw new RestrictionViolationException("group_string");
  }

  void accept(PatternVisitor visitor) {
    visitor.visitSequence(p1, p2);
  }
}
