package com.thaiopensource.relaxng;

interface NameClass {
  boolean contains(String namespaceURI, String localName);
  void accept(NameClassVisitor visitor);
}
