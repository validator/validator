package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;

public class AttributeUseChoice extends AttributeGroup {
  public AttributeUseChoice(SourceLocation location, Annotation annotation, List children) {
    super(location, annotation, children);
  }

  public Object accept(AttributeUseVisitor visitor) {
    return visitor.visitAttributeUseChoice(this);
  }
}
