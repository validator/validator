package com.thaiopensource.relaxng.edit;

public abstract class AnnotationChild extends SourceObject {
  public abstract Object accept(AnnotationChildVisitor visitor);
}
