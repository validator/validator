package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.DatatypeContext;

class QNameDatatype extends DatatypeBase {
  public boolean lexicallyAllows(String str) {
    int len = str.length();
    int i = str.indexOf(':');
    if (i < 0)
      return isNcName(str, 0, len);
    else
      return isNcName(str, 0, i) && isNcName(str, i + 1, len);
  }

  private static boolean isNcName(String str, int i, int j) {
    if (i >= j)
      return false;
    char c = str.charAt(i);
    if (c == ':' || !Naming.isNameStartChar(c))
      return false;
    for (++i; i < j; i++) {
      c = str.charAt(i);
      if (c == ':' || !Naming.isNameChar(c))
	return false;
    }
    return true;
  }

  static class QName {
    private String namespaceURI;
    private String localName;
    QName(String namespaceURI, String localName) {
      this.namespaceURI = namespaceURI;
      this.localName = localName;
    }
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof QName))
	return false;
      QName other = (QName)obj;
      return namespaceURI.equals(other.namespaceURI) && localName.equals(other.localName);
    }
  }

  Object getValue(String str, DatatypeContext dc) {
    int i = str.indexOf(':');
    if (i < 0) {
      String ns = dc.getNamespaceURI("");
      if (ns == null)
	ns = "";
      return new QName(ns, str);
    }
    else {
      String prefix = str.substring(0, i);
      String ns = dc.getNamespaceURI(prefix);
      if (ns == null)
	return null;
      return new QName(ns, str.substring(i + 1));
    }
  }

  boolean allowsValue(String str, DatatypeContext dc) {
    int i = str.indexOf(':');
    return i < 0 || dc.getNamespaceURI(str.substring(0, i)) != null;
  }
}
