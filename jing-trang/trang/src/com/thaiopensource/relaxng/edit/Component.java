package com.thaiopensource.relaxng.edit;

public abstract class Component extends Annotated {
  public abstract <T> T accept(ComponentVisitor<T> visitor);
}
