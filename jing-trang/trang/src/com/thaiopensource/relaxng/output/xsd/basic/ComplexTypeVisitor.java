package com.thaiopensource.relaxng.output.xsd.basic;

public interface ComplexTypeVisitor<T> {
  T visitComplexContent(ComplexTypeComplexContent t);
  T visitSimpleContent(ComplexTypeSimpleContent t);
  T visitNotAllowedContent(ComplexTypeNotAllowedContent t);
}
