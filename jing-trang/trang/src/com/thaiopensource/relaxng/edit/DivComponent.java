package com.thaiopensource.relaxng.edit;

import java.util.List;
import java.util.Vector;

public class DivComponent extends Component implements Container {
  private final List components = new Vector();

  public List getComponents() {
    return components;
  }

  public Object accept(ComponentVisitor visitor) {
    return visitor.visitDiv(this);
  }

  public void componentsAccept(ComponentVisitor visitor) {
    for (int i = 0, len = components.size();  i < len; i++)
      ((Component)components.get(i)).accept(visitor);
  }
}
