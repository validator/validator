package com.thaiopensource.relaxng.edit;

public interface AnnotationChildVisitor {
  Object visitText(TextAnnotation ta);
  Object visitComment(Comment c);
  Object visitElement(ElementAnnotation ea);
}
