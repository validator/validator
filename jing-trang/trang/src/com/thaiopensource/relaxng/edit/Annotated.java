package com.thaiopensource.relaxng.edit;

import com.thaiopensource.relaxng.parse.Context;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;

public abstract class Annotated extends SourceObject {
  private final List leadingComments = new Vector();
  private final List attributeAnnotations = new Vector();
  private final List childElementAnnotations = new Vector();
  private final List followingElementAnnotations = new Vector();
  private Context context;

  public List getLeadingComments() {
    return leadingComments;
  }

  public List getAttributeAnnotations() {
    return attributeAnnotations;
  }

  public List getChildElementAnnotations() {
    return childElementAnnotations;
  }

  public List getFollowingElementAnnotations() {
    return followingElementAnnotations;
  }

  public boolean mayContainText() {
    return false;
  }

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  public String getAttributeAnnotation(String ns, String localName) {
    for (Iterator iter = attributeAnnotations.iterator(); iter.hasNext();) {
      AttributeAnnotation att = (AttributeAnnotation)iter.next();
      if (att.getNamespaceUri().equals(ns) && att.getLocalName().equals(localName))
        return att.getValue();
    }
    return null;
  }

  public void attributeAnnotationsAccept(AttributeAnnotationVisitor visitor) {
    for (int i = 0, len = attributeAnnotations.size();  i < len; i++)
      ((AttributeAnnotation)attributeAnnotations.get(i)).accept(visitor);
  }

  public void childElementAnnotationsAccept(AnnotationChildVisitor visitor) {
    for (int i = 0, len = childElementAnnotations.size();  i < len; i++)
      ((AnnotationChild)childElementAnnotations.get(i)).accept(visitor);
  }

  public void followingElementAnnotationsAccept(AnnotationChildVisitor visitor) {
    for (int i = 0, len = followingElementAnnotations.size();  i < len; i++)
      ((AnnotationChild)followingElementAnnotations.get(i)).accept(visitor);
  }

  public void leadingCommentsAccept(AnnotationChildVisitor visitor) {
    for (int i = 0, len = leadingComments.size();  i < len; i++)
      ((Comment)leadingComments.get(i)).accept(visitor);
  }
}
