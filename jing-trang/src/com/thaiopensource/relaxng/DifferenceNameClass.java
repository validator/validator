package com.thaiopensource.relaxng;

class DifferenceNameClass implements NameClass {

  private final NameClass nameClass1;
  private final NameClass nameClass2;

  DifferenceNameClass(NameClass nameClass1, NameClass nameClass2) {
    this.nameClass1 = nameClass1;
    this.nameClass2 = nameClass2;
  }

  public boolean contains(String namespaceURI, String localName) {
    return (nameClass1.contains(namespaceURI, localName)
	    && !nameClass2.contains(namespaceURI, localName));
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof DifferenceNameClass))
      return false;
    DifferenceNameClass other = (DifferenceNameClass)obj;
    return (nameClass1.equals(other.nameClass1)
	    && nameClass2.equals(other.nameClass2));
  }
  public void accept(NameClassVisitor visitor) {
    visitor.visitDifference(nameClass1, nameClass2);
  }
}
