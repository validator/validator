package com.thaiopensource.xml.dtd.om;

public class DefaultValue extends AttributeDefault {
  private final String value;
  
  public DefaultValue(String value) {
    this.value = value;
  }

  public int getType() {
    return DEFAULT_VALUE;
  }

  public String getValue() {
    return value;
  }
  
  public void accept(AttributeDefaultVisitor visitor) throws Exception {
    visitor.defaultValue(value);
  }

  public String getDefaultValue() {
    return value;
  }
}
