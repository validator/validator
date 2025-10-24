package com.thaiopensource.relaxng.output.xsd.basic;

public interface SimpleTypeVisitor<T> {
  T visitRestriction(SimpleTypeRestriction t);
  T visitUnion(SimpleTypeUnion t);
  T visitList(SimpleTypeList t);
  T visitRef(SimpleTypeRef t);
}
