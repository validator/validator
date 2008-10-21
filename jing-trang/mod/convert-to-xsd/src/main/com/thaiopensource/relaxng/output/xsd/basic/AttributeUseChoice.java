package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;

public class AttributeUseChoice extends AttributeGroup {
  public AttributeUseChoice(SourceLocation location, Annotation annotation, List<AttributeUse> children) {
    super(location, annotation, children);
  }

  public <T> T accept(AttributeUseVisitor<T> visitor) {
    return visitor.visitAttributeUseChoice(this);
  }
}
