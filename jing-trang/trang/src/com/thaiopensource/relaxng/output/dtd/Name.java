package com.thaiopensource.relaxng.output.dtd;

final public class Name {
  private final String namespaceUri;
  private final String localName;

  public Name(String namespaceUri, String localName) {
    this.namespaceUri = namespaceUri;
    this.localName = localName;
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }

  public String getLocalName() {
    return localName;
  }

  public int hashCode() {
    return namespaceUri.hashCode() ^ localName.hashCode();
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof Name))
      return false;
    Name other = (Name)obj;
    return this.namespaceUri.equals(other.namespaceUri) && this.localName.equals(other.localName);
  }
}
