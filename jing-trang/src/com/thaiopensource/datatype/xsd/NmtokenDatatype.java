package com.thaiopensource.datatype.xsd;

class NmtokenDatatype extends TokenDatatype {
  public boolean lexicallyAllows(String str) {
    int len = str.length();
    if (len == 0)
      return false;
    for (int i = 0; i < len; i++)
      if (!Naming.isNameChar(str.charAt(i)))
	return false;
    return true;
  }
  public int getLength(Object obj) {
    // Surrogates are not possible in an NMTOKEN.
    return ((String)obj).length();
  }
}
