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
    return (nc.contains(namespaceURI, localName)
	    && (value == null
		|| valuePattern.residual(b, value).isNullable()));
  }
  boolean isAttribute() {
    return true;
  }
}
