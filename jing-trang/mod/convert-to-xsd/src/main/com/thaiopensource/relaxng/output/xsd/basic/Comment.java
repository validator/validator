package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class Comment extends Located implements TopLevel {
  private String content;

  public Comment(SourceLocation location, String content) {
    super(location);
    this.content = content;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void accept(SchemaVisitor visitor) {
    visitor.visitComment(this);
  }
}
