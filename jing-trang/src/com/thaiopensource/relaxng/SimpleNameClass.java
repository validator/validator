package com.thaiopensource.relaxng;

class SimpleNameClass implements NameClass {

  private final String namespaceURI;
  private final String localName;

  SimpleNameClass(String namespaceURI, String localName) {
    this.namespaceURI = namespaceURI;
    this.localName = localName;
  }

  public boolean contains(String namespaceURI, String localName) {
    return (this.localName.equals(localName)
	    && this.namespaceURI.equals(namespaceURI));
  }

  public int hashCode() {
    return namespaceURI.hashCode() ^ localName.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof SimpleNameClass))
      return false;
    SimpleNameClass other = (SimpleNameClass)obj;
    return (namespaceURI.equals(other.namespaceURI)
	    && localName.equals(other.localName));
  }

  String getNamespaceURI() {
    return namespaceURI;
  }

  String getLocalName() {
    return localName;
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitName(namespaceURI, localName);
  }

  public boolean isOpen() {
    return false;
  }
}
