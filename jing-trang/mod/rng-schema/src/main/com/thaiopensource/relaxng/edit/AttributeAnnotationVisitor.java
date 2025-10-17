package com.thaiopensource.relaxng.edit;

public interface AttributeAnnotationVisitor<T> {
  T visitAttribute(AttributeAnnotation a);
}
