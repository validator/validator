package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public abstract class TopLevel extends Annotated {
  private final Schema parentSchema;

  public TopLevel(SourceLocation location, Annotation annotation, Schema parentSchema) {
    super(location, annotation);
    this.parentSchema = parentSchema;
  }

  public abstract void accept(SchemaVisitor visitor);

  public Schema getParentSchema() {
    return parentSchema;
  }
}
