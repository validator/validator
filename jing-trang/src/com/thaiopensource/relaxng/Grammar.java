package com.thaiopensource.relaxng;

import java.util.Hashtable;
import java.util.Enumeration;

class Grammar {
  private Hashtable patterns = new Hashtable();

  private PatternRefPattern start = new PatternRefPattern(null);

  private Grammar parent;

  Grammar(Grammar parent) {
    this.parent = parent;
  }

  PatternRefPattern startPatternRef() {
    return start;
  }

  Grammar getParent() {
    return parent;
  }

  Enumeration patternNames() {
    return patterns.keys();
  }

  PatternRefPattern makePatternRef(String name) {
    PatternRefPattern p = (PatternRefPattern)patterns.get(name);
    if (p == null) {
      p = new PatternRefPattern(name);
      patterns.put(name, p);
    }
    return p;
  }

}
