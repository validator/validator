package com.thaiopensource.relaxng.output.xsd.basic;

public abstract class ComplexTypeAllowedContent extends ComplexType {
  private final AttributeUse attributeUses;

  public ComplexTypeAllowedContent(AttributeUse attributeUses) {
    this.attributeUses = attributeUses;
  }

  public AttributeUse getAttributeUses() {
    return attributeUses;
  }
}
