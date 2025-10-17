package com.thaiopensource.relaxng.edit;

public interface AnnotationChildVisitor<T> {
  T visitText(TextAnnotation ta);
  T visitComment(Comment c);
  T visitElement(ElementAnnotation ea);
}
