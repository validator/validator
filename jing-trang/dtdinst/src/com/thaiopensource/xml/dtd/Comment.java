package com.thaiopensource.xml.dtd;

public class Comment extends TopLevel {
  private final String value;

  Comment(String value) {
    this.value = value;
  }

  public int getType() {
    return COMMENT;
  }
      
  public String getValue() {
    return value;
  }

  public void accept(TopLevelVisitor visitor) throws VisitException {
    try {
      visitor.comment(value);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VisitException(e);
    }
  }
}
