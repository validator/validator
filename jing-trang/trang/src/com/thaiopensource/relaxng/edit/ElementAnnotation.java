package com.thaiopensource.relaxng.edit;

import com.thaiopensource.relaxng.parse.ParsedElementAnnotation;
import com.thaiopensource.relaxng.parse.Context;

import java.util.List;
import java.util.Vector;

public class ElementAnnotation extends AnnotationChild {
  private String namespaceUri;
  private String localName;
  private String prefix;
  private Context context;
  private final List attributes = new Vector();
  private final List children = new Vector();

  public ElementAnnotation(String namespaceUri, String localName) {
    this.namespaceUri = namespaceUri;
    this.localName = localName;
  }

  public List getAttributes() {
    return attributes;
  }

  public List getChildren() {
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

  public Object accept(AnnotationChildVisitor visitor) {
    return visitor.visitElement(this);
  }

  public void attributesAccept(AttributeAnnotationVisitor visitor) {
    for (int i = 0, len = attributes.size();  i < len; i++)
      ((AttributeAnnotation)attributes.get(i)).accept(visitor);
  }

  public void childrenAccept(AnnotationChildVisitor visitor) {
    for (int i = 0, len = children.size();  i < len; i++)
      ((AnnotationChild)children.get(i)).accept(visitor);
  }
}
