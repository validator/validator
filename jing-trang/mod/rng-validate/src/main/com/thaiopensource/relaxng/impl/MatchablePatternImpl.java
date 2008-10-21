package com.thaiopensource.relaxng.impl;

import com.thaiopensource.relaxng.match.MatchablePattern;
import com.thaiopensource.relaxng.match.Matcher;

public class MatchablePatternImpl implements MatchablePattern {
  private final SchemaPatternBuilder spb;
  private final Pattern start;

  public MatchablePatternImpl(SchemaPatternBuilder spb, Pattern start) {
    this.spb = spb;
    this.start = start;
  }

  public Matcher createMatcher() {
    return new PatternMatcher(start, new ValidatorPatternBuilder(spb));
  }

}
