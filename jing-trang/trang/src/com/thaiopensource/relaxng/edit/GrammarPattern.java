package com.thaiopensource.relaxng.edit;

import java.util.List;
import java.util.Vector;

public class GrammarPattern extends Pattern implements Container {
  private final List components = new Vector();

  public List getComponents() {
    return components;
  }

  public Object accept(PatternVisitor visitor) {
    return visitor.visitGrammar(this);
  }

  public void componentsAccept(ComponentVisitor visitor) {
    for (int i = 0, len = components.size();  i < len; i++)
      ((Component)components.get(i)).accept(visitor);
  }
}
