package com.thaiopensource.relaxng.edit;

public class TextAnnotation extends AnnotationChild {
  private String value;

  public TextAnnotation(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Object accept(AnnotationChildVisitor visitor) {
    return visitor.visitText(this);
  }
}
