package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class SimpleTypeDefinition extends Definition {
  private SimpleType simpleType;

  public SimpleTypeDefinition(SourceLocation location, Annotation annotation, Schema parentSchema, String name, SimpleType simpleType) {
    super(location, annotation, parentSchema, name);
    this.simpleType = simpleType;
  }

  public SimpleType getSimpleType() {
    return simpleType;
  }

  public void setSimpleType(SimpleType simpleType) {
    this.simpleType = simpleType;
  }

  public void accept(SchemaVisitor visitor) {
    visitor.visitSimpleType(this);
  }
}
