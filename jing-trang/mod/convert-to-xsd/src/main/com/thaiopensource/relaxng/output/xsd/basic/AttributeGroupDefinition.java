package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class AttributeGroupDefinition extends Definition {
  private AttributeUse attributeUses;

  public AttributeGroupDefinition(SourceLocation location, Annotation annotation, Schema parentSchema, String name, AttributeUse attributeUses) {
    super(location, annotation, parentSchema, name);
    this.attributeUses = attributeUses;
  }

  public AttributeUse getAttributeUses() {
    return attributeUses;
  }

  public void setAttributeUses(AttributeUse attributeUses) {
    this.attributeUses = attributeUses;
  }

  public void accept(SchemaVisitor visitor) {
    visitor.visitAttributeGroup(this);
  }
}
