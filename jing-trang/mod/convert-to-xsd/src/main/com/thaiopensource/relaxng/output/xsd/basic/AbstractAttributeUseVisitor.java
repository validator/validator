package com.thaiopensource.relaxng.output.xsd.basic;

public abstract class AbstractAttributeUseVisitor<T> implements AttributeUseVisitor<T> {
  public T visitAttributeUseChoice(AttributeUseChoice a) {
    return visitAttributeGroup(a);
  }
}
