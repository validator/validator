package com.thaiopensource.relaxng;

import com.thaiopensource.datatype.DatatypeContext;

class AttributeAtom extends Atom {
  String namespaceURI;
  String localName;
  Atom value;
  AttributeAtom(String namespaceURI, String localName, String value, DatatypeContext dc) {
    this.namespaceURI = namespaceURI;
    this.localName = localName;
    this.value = value == null ? null : new StringAtom(value, dc);
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
