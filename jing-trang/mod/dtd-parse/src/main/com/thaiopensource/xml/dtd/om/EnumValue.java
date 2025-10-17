package com.thaiopensource.xml.dtd.om;

public class EnumValue extends EnumGroupMember {
  private final String value;

  public EnumValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void accept(EnumGroupVisitor visitor) throws Exception {
    visitor.enumValue(value);
  }

  public int getType() {
    return ENUM_VALUE;
  }  
  
}
