package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public abstract class SimpleType extends Annotated {
  public SimpleType(SourceLocation location, Annotation annotation) {
    super(location, annotation);
  }

  public abstract <T> T accept(SimpleTypeVisitor<T> visitor);
}
