package com.thaiopensource.relaxng;

class ElementAtom extends Atom {
  String namespaceURI;
  String localName;
  Pattern[] contentPatterns;

  ElementAtom(String namespaceURI, String localName, Pattern[] contentPatterns) {
    this.namespaceURI = namespaceURI;
    this.localName = localName;
    this.contentPatterns = contentPatterns;
  }

  boolean matchesElement(NameClass nc, Pattern content) {
    if (nc.contains(namespaceURI, localName)) {
      for (int i = 0; i < contentPatterns.length; i++)
	if (content == contentPatterns[i])
	  return true;
    }
    return false;
  }
}
