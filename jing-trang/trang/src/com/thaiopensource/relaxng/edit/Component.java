package com.thaiopensource.relaxng.edit;

public abstract class Component extends Annotated {
  public abstract Object accept(ComponentVisitor visitor);
}
