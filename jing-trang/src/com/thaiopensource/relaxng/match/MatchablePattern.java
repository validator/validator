package com.thaiopensource.relaxng.match;

/**
 * A RELAX NG pattern that can be matched against an XML document.
 * A MatchablePattern object is safe for concurrent accesss
 * from multiple threads.
 */
public interface MatchablePattern {
  /**
   * Create a Matcher for matching against this pattern.
   * @return a Matcher, never null
   */
  Matcher createMatcher();
}
