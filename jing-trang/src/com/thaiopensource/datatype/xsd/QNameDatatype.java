package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

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
    public int hashCode() {
      return localName.hashCode() ^ namespaceURI.hashCode();
    }
  }

  Object getValue(String str, ValidationContext vc) {
    int i = str.indexOf(':');
    if (i < 0) {
      String ns = vc.resolveNamespacePrefix("");
      if (ns == null)
	ns = "";
      return new QName(ns, str);
    }
    else {
      String prefix = str.substring(0, i);
      String ns = vc.resolveNamespacePrefix(prefix);
      if (ns == null)
	return null;
      return new QName(ns, str.substring(i + 1));
    }
  }

  boolean allowsValue(String str, ValidationContext vc) {
    int i = str.indexOf(':');
    return i < 0 || vc.resolveNamespacePrefix(str.substring(0, i)) != null;
  }

  public boolean isContextDependent() {
    return true;
  }
}
