package com.thaiopensource.relaxng.output.xsd.basic;

public class ComplexTypeNotAllowedContent extends ComplexType {
  public ComplexTypeNotAllowedContent() {
  }

  public <T> T accept(ComplexTypeVisitor<T> visitor) {
    return visitor.visitNotAllowedContent(this);
  }
}
