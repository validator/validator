package com.thaiopensource.relaxng.edit;

public class AttributeAnnotation extends SourceObject {
  private String namespaceUri;
  private String localName;
  private String prefix;
  private String value;

  /**
   * namespaceUri is never null; empty string is used for absent namespace
   */
  public AttributeAnnotation(String namespaceUri, String localName, String value) {
    this.namespaceUri = namespaceUri;
    this.localName = localName;
    this.value = value;
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

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Object accept(AttributeAnnotationVisitor visitor) {
    return visitor.visitAttribute(this);
  }
}
