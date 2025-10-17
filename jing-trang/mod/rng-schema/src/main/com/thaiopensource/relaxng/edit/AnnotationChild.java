package com.thaiopensource.relaxng.edit;

public abstract class AnnotationChild extends SourceObject {
  public abstract <T> T accept(AnnotationChildVisitor<T> visitor);
}
