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
}
