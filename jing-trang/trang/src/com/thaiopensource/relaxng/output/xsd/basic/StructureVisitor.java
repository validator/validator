package com.thaiopensource.relaxng.output.xsd.basic;

public interface StructureVisitor {
  Object visitElement(Element element);
  Object visitAttribute(Attribute attribute);
}
