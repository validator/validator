package com.thaiopensource.relaxng.edit;

import java.util.List;
import java.util.Vector;

public class GrammarPattern extends Pattern implements Container {
  private final List<Component> components = new Vector<Component>();

  public List<Component> getComponents() {
    return components;
  }

  public <T> T accept(PatternVisitor<T> visitor) {
    return visitor.visitGrammar(this);
  }

  public void componentsAccept(ComponentVisitor<?> visitor) {
    for (Component c : components)
      c.accept(visitor);
  }
}
