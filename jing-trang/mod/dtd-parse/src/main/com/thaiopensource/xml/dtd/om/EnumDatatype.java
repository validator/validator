package com.thaiopensource.xml.dtd.om;

public class EnumDatatype extends Datatype {
  private final EnumGroup enumGroup;

  public EnumDatatype(EnumGroup enumGroup) {
    this.enumGroup = enumGroup;
  }

  public int getType() {
    return ENUM;
  }

  public EnumGroup getEnumGroup() {
    return enumGroup;
  }

  public void accept(DatatypeVisitor visitor) throws Exception {
    visitor.enumDatatype(enumGroup);
  }
}

