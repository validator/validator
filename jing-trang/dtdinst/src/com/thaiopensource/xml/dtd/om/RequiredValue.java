package com.thaiopensource.xml.dtd.om;

public class RequiredValue extends AttributeDefault {
  public int getType() {
    return REQUIRED_VALUE;
  }

  public void accept(AttributeDefaultVisitor visitor) throws Exception {
    visitor.requiredValue();
  }

  public boolean isRequired() {
    return true;
  }
}
