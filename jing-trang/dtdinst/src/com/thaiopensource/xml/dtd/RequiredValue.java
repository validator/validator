package com.thaiopensource.xml.dtd;

public class RequiredValue extends AttributeDefault {
  public int getType() {
    return REQUIRED_VALUE;
  }

  public void accept(AttributeDefaultVisitor visitor) throws Exception {
    visitor.requiredValue();
  }
}
