package com.thaiopensource.relaxng;

class AnyContentElementAtom extends Atom {
  String namespaceURI;
  String localName;

  AnyContentElementAtom(String namespaceURI, String localName) {
    this.namespaceURI = namespaceURI;
    this.localName = localName;
  }

  boolean matchesElement(NameClass nc, Pattern content) {
    return nc.contains(namespaceURI, localName);
  }
}
