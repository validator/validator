package com.thaiopensource.datatype.xsd;

class NameDatatype extends TokenDatatype {
  public boolean lexicallyAllows(String str) {
    int len = str.length();
    if (len == 0)
      return false;
    if (!Naming.isNameStartChar(str.charAt(0)))
      return false;
    for (int i = 1; i < len; i++)
      if (!Naming.isNameChar(str.charAt(i)))
	return false;
    return true;
  }
  public int getLength(Object obj) {
    // Surrogates are not possible in an Name.
    return ((String)obj).length();
  }
}
