package com.thaiopensource.relaxng.edit;

public abstract class UnaryPattern extends Pattern {
  private Pattern child;

  public UnaryPattern(Pattern child) {
    this.child = child;
  }

  public Pattern getChild() {
    return child;
  }

  public void setChild(Pattern child) {
    this.child = child;
  }
}
