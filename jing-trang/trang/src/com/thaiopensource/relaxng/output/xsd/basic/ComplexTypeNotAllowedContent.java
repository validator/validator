package com.thaiopensource.relaxng.output.xsd.basic;

public class ComplexTypeNotAllowedContent extends ComplexType {
  public ComplexTypeNotAllowedContent() {
  }

  public Object accept(ComplexTypeVisitor visitor) {
    return visitor.visitNotAllowedContent(this);
  }
}
