package com.thaiopensource.relaxng.output.xsd.basic;

public abstract class ComplexType {
  public abstract Object accept(ComplexTypeVisitor visitor);
}
