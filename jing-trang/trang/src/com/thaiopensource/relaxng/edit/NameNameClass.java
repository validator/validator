package com.thaiopensource.relaxng.edit;

public class NameNameClass extends NameClass {
  private String namespaceUri;
  private String localName;
  private String prefix;

  public NameNameClass(String namespaceUri, String localName) {
    this.namespaceUri = namespaceUri;
    this.localName = localName;
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }

  public void setNamespaceUri(String namespaceUri) {
    this.namespaceUri = namespaceUri;
  }

  public String getLocalName() {
    return localName;
  }

  public void setLocalName(String localName) {
    this.localName = localName;
  }

  /**
   * Returns non-empty string or null if there was no prefix.
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * prefix must be non-empty string or null if there is no prefix.
   */
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public boolean mayContainText() {
    return true;
  }

  public Object accept(NameClassVisitor visitor) {
    return visitor.visitName(this);
  }
}
