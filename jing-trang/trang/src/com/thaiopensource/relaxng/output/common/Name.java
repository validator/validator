package com.thaiopensource.relaxng.output.common;

final public class Name implements Comparable {
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

  public int compareTo(Object o) {
    Name other = (Name)o;
    int ret = this.namespaceUri.compareTo(other.namespaceUri);
    if (ret != 0)
      return ret;
    return this.localName.compareTo(other.localName);
  }
}
