package com.thaiopensource.xml.dtd;

public class EnumValue extends EnumGroupMember {
  private final String value;

  public EnumValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void accept(EnumGroupVisitor visitor) throws VisitException {
    try {
      visitor.enumValue(value);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VisitException(e);
    }
  }

  public int getType() {
    return ENUM_VALUE;
  }  
  
}
