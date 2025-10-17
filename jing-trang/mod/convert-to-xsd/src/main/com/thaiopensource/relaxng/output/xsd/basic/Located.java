package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public abstract class Located {
  private final SourceLocation location;

  public Located(SourceLocation location) {
    this.location = location;
  }

  public SourceLocation getLocation() {
    return location;
  }
}
