package com.thaiopensource.xml.dtd;

public class BasicDatatype extends Datatype {
  private final int type;

  public BasicDatatype(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }

  public void accept(DatatypeVisitor visitor) throws Exception {
    visitor.basicDatatype(type);
  }
}

