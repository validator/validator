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

  public String getPrefix() {
    return prefix;
  }

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
