package com.thaiopensource.xml.dtd.om;

public class ImpliedValue extends AttributeDefault {
  public int getType() {
    return IMPLIED_VALUE;
  }

  public void accept(AttributeDefaultVisitor visitor) throws Exception {
    visitor.impliedValue();
  }
}
