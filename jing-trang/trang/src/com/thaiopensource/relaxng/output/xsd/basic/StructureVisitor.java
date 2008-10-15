package com.thaiopensource.relaxng.output.xsd.basic;

public interface StructureVisitor<T> {
  T visitElement(Element element);
  T visitAttribute(Attribute attribute);
}
