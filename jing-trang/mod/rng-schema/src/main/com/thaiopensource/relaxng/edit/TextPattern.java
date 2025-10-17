package com.thaiopensource.relaxng.edit;

public class TextPattern extends Pattern {
  public TextPattern() {
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitText(this);
  }
}
