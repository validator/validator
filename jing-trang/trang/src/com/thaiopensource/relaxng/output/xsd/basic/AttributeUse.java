package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public abstract class AttributeUse extends Annotated {
  public AttributeUse(SourceLocation location, Annotation annotation) {
    super(location, annotation);
  }

  public abstract Object accept(AttributeUseVisitor visitor);
}
