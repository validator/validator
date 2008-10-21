package com.thaiopensource.relaxng.edit;

public class Comment extends AnnotationChild {
  private String value;

  public Comment(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public <T> T accept(AnnotationChildVisitor<T> visitor) {
    return visitor.visitComment(this);
  }
}
