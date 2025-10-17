package com.thaiopensource.xml.dtd.om;

public class NotationDatatype extends EnumDatatype {
  public NotationDatatype(EnumGroup enumGroup) {
    super(enumGroup);
  }

  public int getType() {
    return NOTATION;
  }

  public void accept(DatatypeVisitor visitor) throws Exception {
    visitor.notationDatatype(getEnumGroup());
  }
}
