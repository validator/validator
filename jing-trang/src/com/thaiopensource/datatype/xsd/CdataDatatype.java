package com.thaiopensource.datatype.xsd;

class CdataDatatype extends TokenDatatype {
  CdataDatatype() {
    super(WHITE_SPACE_REPLACE);
  }
  public boolean lexicallyAllows(String str) {
    return true;
  }
}
