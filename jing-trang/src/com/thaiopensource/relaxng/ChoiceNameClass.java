package com.thaiopensource.relaxng;

class ChoiceNameClass implements NameClass {

  private final NameClass nameClass1;
  private final NameClass nameClass2;

  ChoiceNameClass(NameClass nameClass1, NameClass nameClass2) {
    this.nameClass1 = nameClass1;
    this.nameClass2 = nameClass2;
  }

  public boolean contains(String namespaceURI, String localName) {
    return (nameClass1.contains(namespaceURI, localName)
	    || nameClass2.contains(namespaceURI, localName));
  }

  public int hashCode() {
    return nameClass1.hashCode() ^ nameClass2.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof ChoiceNameClass))
      return false;
    ChoiceNameClass other = (ChoiceNameClass)obj;
    return (nameClass1.equals(other.nameClass1)
	    && nameClass2.equals(other.nameClass2));
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitChoice(nameClass1, nameClass2);
  }

  public boolean isOpen() {
    return nameClass1.isOpen() || nameClass2.isOpen();
  }
}
