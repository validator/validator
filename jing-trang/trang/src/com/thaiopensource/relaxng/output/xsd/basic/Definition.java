package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public abstract class Definition extends TopLevel {
  private final String name;

  public Definition(SourceLocation location, Annotation annotation, Schema parentSchema, String name) {
    super(location, annotation, parentSchema);
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
