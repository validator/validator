package com.thaiopensource.relaxng.edit;

import java.util.Vector;
import java.util.List;

public class ChoiceNameClass extends NameClass {
  private final List<NameClass> children = new Vector<NameClass>();

  public List<NameClass> getChildren() {
    return children;
  }

  public Object accept(NameClassVisitor visitor) {
    return visitor.visitChoice(this);
  }

  public void childrenAccept(NameClassVisitor visitor) {
    for (NameClass nc : children)
      nc.accept(visitor);
  }
}
