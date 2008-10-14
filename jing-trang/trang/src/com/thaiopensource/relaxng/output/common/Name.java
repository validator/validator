package com.thaiopensource.relaxng.output.common;

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

  // We include this, but don't derive from Comparator<Name> to avoid a dependency on Java 5.
  static public int compare(Name n1, Name n2) {
    int ret = n1.namespaceUri.compareTo(n2.namespaceUri);
    if (ret != 0)
      return ret;
    return n1.localName.compareTo(n2.localName);
  }
}
