package com.thaiopensource.datatype.xsd;

class NCNameDatatype extends NameDatatype {
  public boolean lexicallyAllows(String str) {
    return super.lexicallyAllows(str) && str.indexOf(':') == -1;
  }
}
