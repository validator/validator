package com.thaiopensource.relaxng;

final class PatternPair {
  Pattern p1;
  Pattern p2;

  PatternPair() {
  }

  PatternPair(Pattern p1, Pattern p2) {
    this.p1 = p1;
    this.p2 = p2;
  }

  Pattern getContentPattern() {
    return p1;
  }

  Pattern getResidualPattern() {
    return p2;
  }

  boolean isEmpty() {
    return p1 == null;
  }
}
