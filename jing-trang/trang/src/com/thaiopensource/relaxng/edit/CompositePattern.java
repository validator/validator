package com.thaiopensource.relaxng.edit;

import java.util.List;
import java.util.Vector;

public abstract class CompositePattern extends Pattern {
  private final List<Pattern> children = new Vector<Pattern>();
  public List<Pattern> getChildren() {
    return children;
  }
  public void childrenAccept(PatternVisitor<?> visitor) {
    for (Pattern p : children)
      p.accept(visitor);
  }
}
