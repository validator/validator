package com.thaiopensource.relaxng.output.xsd.basic;

public abstract class ComplexType {
  public abstract <T> T accept(ComplexTypeVisitor<T> visitor);
  public boolean isMixed() {
    return false;
  }
}
