package com.thaiopensource.relaxng;

class AnyNameClass implements NameClass {

  public boolean contains(String namespaceURI, String localName) {
    return true;
  }

  public boolean equals(Object obj) {
    return obj != null && obj instanceof AnyNameClass;
  }

  public int hashCode() {
    return getClass().hashCode();
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitAnyName();
  }
}
