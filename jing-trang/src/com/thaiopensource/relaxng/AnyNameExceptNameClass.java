package com.thaiopensource.relaxng;

class AnyNameExceptNameClass implements NameClass {

  private final NameClass nameClass;

  AnyNameExceptNameClass(NameClass nameClass) {
    this.nameClass = nameClass;
  }

  public boolean contains(String namespaceURI, String localName) {
    return !nameClass.contains(namespaceURI, localName);
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof AnyNameExceptNameClass))
      return false;
    return nameClass.equals(((AnyNameExceptNameClass)obj).nameClass);
  }

  public int hashCode() {
    return ~nameClass.hashCode();
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitAnyNameExcept(nameClass);
  }
}
