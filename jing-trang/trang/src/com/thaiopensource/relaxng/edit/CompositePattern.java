package com.thaiopensource.relaxng.edit;

import java.util.List;
import java.util.Vector;

public abstract class CompositePattern extends Pattern {
  private final List children = new Vector();
  public List getChildren() {
    return children;
  }
  public void childrenAccept(PatternVisitor visitor) {
    for (int i = 0, len = children.size();  i < len; i++)
      ((Pattern)children.get(i)).accept(visitor);
  }
}
