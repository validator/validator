package com.thaiopensource.xml.dtd.om;

public class CdataDatatype extends Datatype {
  public CdataDatatype() { }

  public int getType() {
    return CDATA;
  }

  public void accept(DatatypeVisitor visitor) throws Exception {
    visitor.cdataDatatype();
  }
}

