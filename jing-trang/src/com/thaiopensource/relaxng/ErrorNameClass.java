package com.thaiopensource.relaxng;

class ErrorNameClass implements NameClass {
  public boolean contains(String namespaceURI, String localName) {
    return false;
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitError();
  }
}
