package com.thaiopensource.relaxng.output.xsd.basic;

public abstract class ComplexType {
  public abstract Object accept(ComplexTypeVisitor visitor);
  public boolean isMixed() {
    return false;
  }
}
