package com.thaiopensource.xml.dtd.om;

public class Comment extends TopLevel {
  private final String value;

  public Comment(String value) {
    this.value = value;
  }

  public int getType() {
    return COMMENT;
  }
      
  public String getValue() {
    return value;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.comment(value);
  }
}
