package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class TokenDatatype extends DatatypeBase implements Measure {

  TokenDatatype() { }
  TokenDatatype(int whiteSpace) {
    super(whiteSpace);
  }

  public boolean lexicallyAllows(String str) {
    return true;
  }

  Object getValue(String str, ValidationContext vc) {
    return str;
  }

  Measure getMeasure() {
    return this;
  }

  public int getLength(Object obj) {
    String str = (String)obj;
    int len = str.length();
    int nSurrogatePairs = 0;
    for (int i = 0; i < len; i++)
      if (isSurrogate1(str.charAt(i)))
	nSurrogatePairs++;
    return len - nSurrogatePairs;
  }

  // Is first char of surrogate pair?
  static private boolean isSurrogate1(char c) {
    return (c & 0xFC00) == 0xD800;
  }
}
