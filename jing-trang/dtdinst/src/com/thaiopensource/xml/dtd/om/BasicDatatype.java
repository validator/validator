package com.thaiopensource.xml.dtd.om;

public class BasicDatatype extends Datatype {
  private final String typeName;

  public BasicDatatype(String typeName) {
    this.typeName = typeName;
  }

  public int getType() {
    return BASIC;
  }

  public String getTypeName() {
    return typeName;
  }

  public void accept(DatatypeVisitor visitor) throws Exception {
    visitor.basicDatatype(typeName);
  }
}

