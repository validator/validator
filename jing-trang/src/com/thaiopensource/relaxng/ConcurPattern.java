package com.thaiopensource.relaxng;

class ConcurPattern extends BinaryPattern {
  ConcurPattern(Pattern p1, Pattern p2) {
    super(p1.isNullable() && p2.isNullable(),
	  combineHashCode(CONCUR_HASH_CODE, p1.patternHashCode(), p2.patternHashCode()),
	  p1,
	  p2);
  }
  Pattern expand(PatternBuilder b) {
    Pattern ep1 = p1.expand(b);
    Pattern ep2 = p2.expand(b);
    if (ep1 != p1 || ep2 != p2)
      return b.makeConcur(ep1, ep2);
    else
      return this;
  }
  
  Pattern residual(PatternBuilder b, Atom a) {
    // Use the first subpattern for assignment.
    Pattern r = b.memoizedResidual(p2, a);
    a.clearAssignmentClass();
    return b.makeConcur(b.memoizedResidual(p1, a), r);
  }

  void initialContentPatterns(String namespaceURI, String localName, PatternSet ts) {
    p1.initialContentPatterns(namespaceURI, localName, ts);
    p2.initialContentPatterns(namespaceURI, localName, ts);
  }

  Pattern combinedInitialContentPattern(PatternBuilder b,
					String namespaceURI,
					String localName,
					int recoveryLevel) {
    Pattern cp1 = p1.combinedInitialContentPattern(b,
						   namespaceURI,
						   localName,
						   recoveryLevel);
    Pattern cp2 = p2.combinedInitialContentPattern(b,
						   namespaceURI,
						   localName,
						   recoveryLevel);
    if (recoveryLevel == 0)
      return b.makeConcur(cp1, cp2);
    else
      return b.makeChoice(cp1, cp2);
  }

  PatternPair unambigContentPattern(PatternBuilder b,
				    String namespaceURI,
				    String localName) {
    // highly unlikely to be unambiguous
    return null;
  }

  void accept(PatternVisitor visitor) {
    visitor.visitConcur(p1, p2);
  }
}
