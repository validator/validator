package com.thaiopensource.relaxng.edit;

import com.thaiopensource.relaxng.parse.Context;

import java.util.List;
import java.util.Vector;

public abstract class Annotated extends SourceObject {
  private final List<Comment> leadingComments = new Vector<Comment>();
  private final List<AttributeAnnotation> attributeAnnotations = new Vector<AttributeAnnotation>();
  private final List<AnnotationChild> childElementAnnotations = new Vector<AnnotationChild>();
  private final List<AnnotationChild> followingElementAnnotations = new Vector<AnnotationChild>();
  private Context context;

  public List<Comment> getLeadingComments() {
    return leadingComments;
  }

  public List<AttributeAnnotation> getAttributeAnnotations() {
    return attributeAnnotations;
  }

  public List<AnnotationChild> getChildElementAnnotations() {
    return childElementAnnotations;
  }

  public List<AnnotationChild> getFollowingElementAnnotations() {
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
    for (AttributeAnnotation a : attributeAnnotations)
      if (a.getNamespaceUri().equals(ns) && a.getLocalName().equals(localName))
        return a.getValue();

    return null;
  }

  public void attributeAnnotationsAccept(AttributeAnnotationVisitor<?> visitor) {
    for (AttributeAnnotation a : attributeAnnotations)
      a.accept(visitor);
  }

  public void childElementAnnotationsAccept(AnnotationChildVisitor<?> visitor) {
    for (AnnotationChild a : childElementAnnotations)
      a.accept(visitor);
  }

  public void followingElementAnnotationsAccept(AnnotationChildVisitor<?> visitor) {
    for (AnnotationChild a : followingElementAnnotations)
      a.accept(visitor);
  }

  public void leadingCommentsAccept(AnnotationChildVisitor<?> visitor) {
    for (Comment c : leadingComments)
      c.accept(visitor);
  }
}
