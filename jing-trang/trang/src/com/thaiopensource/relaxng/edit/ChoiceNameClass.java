package com.thaiopensource.relaxng.edit;

import java.util.Vector;
import java.util.List;

public class ChoiceNameClass extends NameClass {
  private final List children = new Vector();

  public List getChildren() {
    return children;
  }

  public Object accept(NameClassVisitor visitor) {
    return visitor.visitChoice(this);
  }

  public void childrenAccept(NameClassVisitor visitor) {
    for (int i = 0, len = children.size();  i < len; i++)
      ((NameClass)children.get(i)).accept(visitor);
  }
}
