package com.thaiopensource.relaxng;

class ChoicePattern extends BinaryPattern {
  ChoicePattern(Pattern p1, Pattern p2) {
    super(p1.isNullable() || p2.isNullable(),
	  combineHashCode(CHOICE_HASH_CODE, p1.hashCode(), p2.hashCode()),
	  p1,
	  p2);
  }
  Pattern expand(PatternBuilder b) {
    Pattern ep1 = p1.expand(b);
    Pattern ep2 = p2.expand(b);
    if (ep1 != p1 || ep2 != p2)
      return b.makeChoice(ep1, ep2);
    else
      return this;
  }

  Pattern residual(PatternBuilder b, Atom a) {
    return b.makeChoice(b.memoizedResidual(p1, a),
			b.memoizedResidual(p2, a));
  }
  void initialContentPatterns(String namespaceURI, String localName, PatternSet ts) {
    p1.initialContentPatterns(namespaceURI, localName, ts);
    p2.initialContentPatterns(namespaceURI, localName, ts);
  }

  Pattern combinedInitialContentPattern(PatternBuilder b,
				  String namespaceURI,
				  String localName,
				  int recoveryLevel) {
    return b.makeChoice(p1.combinedInitialContentPattern(b,
						      namespaceURI,
						      localName,
						      recoveryLevel),
			p2.combinedInitialContentPattern(b,
						      namespaceURI,
						      localName,
						      recoveryLevel));
  }

  Pattern endAttributes(PatternBuilder b, boolean recovering) {
    Pattern cp1 = b.memoizedEndAttributes(p1, recovering);
    Pattern cp2 = b.memoizedEndAttributes(p2, recovering);
    if (cp1 == p1 && cp2 == p2)
      return this;
    return b.makeChoice(cp1, cp2);
  }

  PatternPair unambigContentPattern(PatternBuilder b,
			      String namespaceURI,
			      String localName) {
    PatternPair cp1 = b.memoizedUnambigContentPattern(p1, namespaceURI, localName);
    if (cp1 == null)
      return null;
    if (cp1.isEmpty())
      return b.memoizedUnambigContentPattern(p2, namespaceURI, localName);
    PatternPair cp2 = b.memoizedUnambigContentPattern(p2, namespaceURI, localName);
    if (cp2 == null)
      return null;
    if (cp2.isEmpty())
      return cp1;
    if (cp1.getContentPattern() == cp2.getContentPattern())
      return new PatternPair(cp1.getContentPattern(),
			  b.makeChoice(cp1.getResidualPattern(),
				       cp2.getResidualPattern()));
    return null;
  }

  boolean containsChoice(Pattern p) {
    return p1.containsChoice(p) || p2.containsChoice(p);
  }

  void accept(PatternVisitor visitor) {
    visitor.visitChoice(p1, p2);
  }
}
