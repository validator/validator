package com.thaiopensource.relaxng.edit;

import java.util.List;
import java.util.Vector;

public abstract class CompositePattern extends Pattern {
  private final List children = new Vector();
  public List getChildren() {
    return children;
  }
}
