package com.thaiopensource.relaxng.edit;

import java.util.List;
import java.util.Vector;

public class DivComponent extends Component implements Container {
  private final List<Component> components = new Vector<Component>();

  public List<Component> getComponents() {
    return components;
  }

  public <T> T accept(ComponentVisitor<T> visitor) {
    return visitor.visitDiv(this);
  }

  public void componentsAccept(ComponentVisitor<?> visitor) {
    for (Component c : components)
      c.accept(visitor);
  }
}
