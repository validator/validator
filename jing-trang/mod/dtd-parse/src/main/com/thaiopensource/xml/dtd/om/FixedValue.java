package com.thaiopensource.xml.dtd.om;

public class FixedValue extends AttributeDefault {
  private final String value;
  
  public FixedValue(String value) {
    this.value = value;
  }

  public int getType() {
    return FIXED_VALUE;
  }

  public String getValue() {
    return value;
  }
  
  public void accept(AttributeDefaultVisitor visitor) throws Exception {
    visitor.fixedValue(value);
  }

  public String getDefaultValue() {
    return value;
  }

  public String getFixedValue() {
    return value;
  }
}
