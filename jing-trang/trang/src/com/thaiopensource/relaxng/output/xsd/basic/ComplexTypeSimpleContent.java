package com.thaiopensource.relaxng.output.xsd.basic;

import java.util.List;

public class ComplexTypeSimpleContent extends ComplexTypeAllowedContent {
  private final SimpleType simpleType;

  public ComplexTypeSimpleContent(AttributeUse attributeUses, SimpleType simpleType) {
    super(attributeUses);
    this.simpleType = simpleType;
  }

  public SimpleType getSimpleType() {
    return simpleType;
  }

  public Object accept(ComplexTypeVisitor visitor) {
    return visitor.visitSimpleContent(this);
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof ComplexTypeSimpleContent))
      return false;
    ComplexTypeSimpleContent other = (ComplexTypeSimpleContent)obj;
    return this.getAttributeUses().equals(other.getAttributeUses()) && this.simpleType.equals(other.simpleType);
  }

  public int hashCode() {
    return getAttributeUses().hashCode() ^ simpleType.hashCode();
  }
}
