package com.thaiopensource.relaxng.edit;

public class SourceObject {
  private SourceLocation sourceLocation;

  public SourceObject() {
  }

  public SourceObject(SourceLocation sourceLocation) {
    this.sourceLocation = sourceLocation;
  }

  public SourceLocation getSourceLocation() {
    return sourceLocation;
  }

  public void setSourceLocation(SourceLocation sourceLocation) {
    this.sourceLocation = sourceLocation;
  }
}
