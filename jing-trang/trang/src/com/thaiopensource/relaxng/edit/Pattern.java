package com.thaiopensource.relaxng.edit;

import com.thaiopensource.relaxng.parse.ParsedPattern;

public abstract class Pattern extends Annotated implements ParsedPattern {
  public abstract Object accept(PatternVisitor visitor);
}
