package com.thaiopensource.relaxng;

import org.relaxng.datatype.ValidationContext;

import org.xml.sax.SAXException;

class AttributeAtom extends Atom {
  String namespaceURI;
  String localName;
  StringAtom value;

  AttributeAtom(String namespaceURI, String localName, String value, ValidationContext vc) {
    this.namespaceURI = namespaceURI;
    this.localName = localName;
    this.value = value == null ? null : new StringAtom(value, vc);
  }
  boolean matchesAttribute(PatternBuilder b, NameClass nc, Pattern valuePattern) {
    if (!nc.contains(namespaceURI, localName))
      return false;
    if (value == null)
      return true;
    if (value.isEmpty() && valuePattern.isNullable())
      return true;
    return valuePattern.residual(b, value).isNullable();
  }
  boolean isAttribute() {
    return true;
  }
}
