package com.thaiopensource.relaxng;

class NotNameClass implements NameClass {

  private final NameClass nameClass;

  NotNameClass(NameClass nameClass) {
    this.nameClass = nameClass;
  }

  public boolean contains(String namespaceURI, String localName) {
    return !nameClass.contains(namespaceURI, localName);
  }

  public int hashCode() {
    return ~nameClass.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof NotNameClass))
      return false;
    return nameClass.equals(((NotNameClass)obj).nameClass);
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitNot(nameClass);
  }
}
