package com.thaiopensource.relaxng.edit;

import com.thaiopensource.relaxng.parse.Context;

import java.util.List;
import java.util.Vector;

public class ElementAnnotation extends AnnotationChild {
  private String namespaceUri;
  private String localName;
  private String prefix;
  private Context context;
  private final List<AttributeAnnotation> attributes = new Vector<AttributeAnnotation>();
  private final List<AnnotationChild> children = new Vector<AnnotationChild>();

  public ElementAnnotation(String namespaceUri, String localName) {
    this.namespaceUri = namespaceUri;
    this.localName = localName;
  }

  public List<AttributeAnnotation> getAttributes() {
    return attributes;
  }

  public List<AnnotationChild> getChildren() {
    return children;
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

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  public <T> T accept(AnnotationChildVisitor<T> visitor) {
    return visitor.visitElement(this);
  }

  public void attributesAccept(AttributeAnnotationVisitor<?> visitor) {
    for (AttributeAnnotation a : attributes)
      a.accept(visitor);
  }

  public void childrenAccept(AnnotationChildVisitor<?> visitor) {
    for (AnnotationChild c : children)
      c.accept(visitor);
  }
}
